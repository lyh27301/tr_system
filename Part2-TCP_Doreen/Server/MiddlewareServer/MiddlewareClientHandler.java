package Server.MiddlewareServer;

import Server.Common.*;
import Server.LockManager.DeadlockException;
import Server.LockManager.TransactionLockObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MiddlewareClientHandler extends Thread {

    //connection to the client
    final Socket clientSocket;
    final ObjectInputStream clientInputStream;
    final ObjectOutputStream clientOutputStream;


    enum ServerType {CAR, FLIGHT, ROOM, CUSTOMER}

    //configuration
    String carServerHost = "localhost";
    int carServerPort = 6316;

    String flightServerHost = "localhost";
    int flightServerPort = 6216;

    String roomServerHost = "localhost";
    int roomServerPort = 6416;

    String customerServerHost = "localhost";
    int customerServerPort = 6516;

    //connections
    Socket carSocket;
    ObjectInputStream carInputStream;
    ObjectOutputStream carOutputStream;

    Socket flightSocket;
    ObjectInputStream flightInputStream;
    ObjectOutputStream flightOutputStream;

    Socket roomSocket;
    ObjectInputStream roomInputStream;
    ObjectOutputStream roomOutputStream;

    Socket customerSocket;
    ObjectInputStream customerInputStream;
    ObjectOutputStream customerOutputStream;

    TransactionManager transactionManager;


    public MiddlewareClientHandler(Socket clientSocket,
                                   ObjectInputStream inputStream,
                                   ObjectOutputStream outputStream,
                                   String carServerHost,
                                   String flightServerHost,
                                   String roomServerHost,
                                   String customerServerHost,
                                   TransactionManager transactionManager) {

        this.clientInputStream = inputStream;
        this.clientOutputStream = outputStream;
        this.clientSocket = clientSocket;

        this.carServerHost = carServerHost;
        this.flightServerHost = flightServerHost;
        this.roomServerHost = roomServerHost;
        this.customerServerHost = customerServerHost;

        connectToServer(ServerType.CAR);
        connectToServer(ServerType.FLIGHT);
        connectToServer(ServerType.ROOM);
        connectToServer(ServerType.CUSTOMER);

        this.transactionManager = transactionManager;
    }

    @Override
    public void run() {

        while (true) {
            try {
                try {
                    // receive client request
                    String receivedFromClient = ((Message) clientInputStream.readObject()).getMessageText();


                    String[] parsed = receivedFromClient.split(",");
                    String command = parsed[0];


                    if (command.equals("Shutdown")) {
                        this.carOutputStream.writeObject(new Message(command));
                        this.flightOutputStream.writeObject(new Message(command));
                        this.customerOutputStream.writeObject(new Message(command));
                        this.roomOutputStream.writeObject(new Message(command));

                        synchronized (MiddlewareServer.shutdownSignal) {
                            MiddlewareServer.shutdownSignal.notify();
                        }
                        return;
                    }


                    if (command.equals("Start")) {
                        int xid = startTransaction();
                        Message message = new Message("Transaction-" + xid + " has started");
                        message.setMessageObject(Integer.valueOf(xid));
                        clientOutputStream.writeObject(message);
                        continue;
                    }


                    if (parsed.length > 1 && !transactionManager.existsTransaction(Integer.parseInt(parsed[1]))) {
                        clientOutputStream.writeObject(new Message("Transaction-" + parsed[1] + " does not exist."));
                        continue;
                    }


                    if (command.equals("Commit")) {
                        if (commit(Integer.parseInt(parsed[1]))) {
                            clientOutputStream.writeObject(new Message("Transaction-" + parsed[1] + " is committed"));
                        } else {
                            clientOutputStream.writeObject(new Message("Failed to commit Transaction-" + parsed[1]));
                        }
                        continue;
                    }


                    if (command.equals("Abort")) {
                        if (abort(Integer.parseInt(parsed[1]))) {
                            clientOutputStream.writeObject(new Message("Transaction-" + parsed[1] + " is aborted"));
                        } else {
                            clientOutputStream.writeObject(new Message("Failed to abort Transaction-" + parsed[1]));
                        }
                        continue;
                    }


                    if (command.equals("AddCars") || command.equals("DeleteCars") || command.equals("QueryCars")
                            || command.equals("QueryCarsPrice")) {

                        TransactionLockObject.LockType lockType = (command.equals("AddCars") || command.equals("DeleteCars")) ?
                                TransactionLockObject.LockType.LOCK_WRITE : TransactionLockObject.LockType.LOCK_READ;
                        beforeOperation(Integer.parseInt(parsed[1]), "car-" + parsed[2], lockType);
                        String response = executeRequestInResourceManager(ServerType.CAR, receivedFromClient);
                        clientOutputStream.writeObject(new Message(response));
                        continue;
                    }


                    if (command.equals("AddFlight") || command.equals("DeleteFlight") || command.equals("QueryFlight")
                            || command.equals("QueryFlightPrice")) {

                        TransactionLockObject.LockType lockType = (command.equals("AddFlight") || command.equals("DeleteFlight")) ?
                                TransactionLockObject.LockType.LOCK_WRITE : TransactionLockObject.LockType.LOCK_READ;
                        beforeOperation(Integer.parseInt(parsed[1]), "flight-" + parsed[2], lockType);

                        String response = executeRequestInResourceManager(ServerType.FLIGHT, receivedFromClient);
                        clientOutputStream.writeObject(new Message(response));
                        continue;
                    }


                    if (command.equals("AddRooms") || command.equals("DeleteRooms") || command.equals("QueryRooms")
                            || command.equals("QueryRoomsPrice")) {

                        TransactionLockObject.LockType lockType = (command.equals("AddRooms") || command.equals("DeleteRooms")) ?
                                TransactionLockObject.LockType.LOCK_WRITE : TransactionLockObject.LockType.LOCK_READ;
                        beforeOperation(Integer.parseInt(parsed[1]), "room-" + parsed[2], lockType);

                        String response = executeRequestInResourceManager(ServerType.ROOM, receivedFromClient);
                        clientOutputStream.writeObject(new Message(response));
                        continue;
                    }


                    if (command.equals("AddCustomer") || command.equals("AddCustomerID")
                            || command.equals("QueryCustomer")) {

                        TransactionLockObject.LockType lockType = (command.equals("AddCustomer") || command.equals("AddCustomerID")) ?
                                TransactionLockObject.LockType.LOCK_WRITE : TransactionLockObject.LockType.LOCK_READ;
                        beforeOperation(Integer.parseInt(parsed[1]), "customer-" + parsed[2], lockType);

                        String response = executeRequestInResourceManager(ServerType.CUSTOMER, receivedFromClient);
                        clientOutputStream.writeObject(new Message(response));
                        continue;
                    }


                    if (command.equals("ReserveRoom")) {
                        if (checkCustomerExists(Integer.parseInt(parsed[1]), Integer.parseInt(parsed[2]))) {
                            TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
                            beforeOperation(Integer.parseInt(parsed[1]), "room-" + parsed[3], lockType);
                            String response = executeRequestInResourceManager(ServerType.ROOM, receivedFromClient);
                            if (response.equals("false")) {
                                clientOutputStream.writeObject(new Message("Room could not be reserved"));
                            } else {
                                String request = "ReserveItem," + parsed[1] + "," + parsed[2] + "," + Room.getKey(parsed[3]) + "," + parsed[3] + "," + Integer.valueOf(response);
                                TransactionLockObject.LockType customerLockType = TransactionLockObject.LockType.LOCK_WRITE;
                                beforeOperation(Integer.parseInt(parsed[1]), "customer" + parsed[2], customerLockType);
                                response = executeRequestInResourceManager(ServerType.CUSTOMER, request);
                                clientOutputStream.writeObject(new Message(response));
                            }
                        } else {
                            clientOutputStream.writeObject(new Message("Failed-Customer does not exists"));
                        }
                        continue;
                    }


                    if (command.equals("ReserveFlight")) {
                        if (checkCustomerExists(Integer.parseInt(parsed[1]), Integer.parseInt(parsed[2]))) {
                            TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
                            beforeOperation(Integer.parseInt(parsed[1]), "flight-" + parsed[3], lockType);
                            String response = executeRequestInResourceManager(ServerType.FLIGHT, receivedFromClient);
                            if (response.equals("false")) {
                                clientOutputStream.writeObject(new Message("Flight could not be reserved"));
                            } else {
                                String request = "ReserveItem," + parsed[1] + "," + parsed[2] + "," + Flight.getKey(Integer.parseInt(parsed[3])) + "," + parsed[3] + "," + Integer.valueOf(response);
                                TransactionLockObject.LockType customerLockType = TransactionLockObject.LockType.LOCK_WRITE;
                                beforeOperation(Integer.parseInt(parsed[1]), "customer" + parsed[2], customerLockType);
                                response = executeRequestInResourceManager(ServerType.CUSTOMER, request);
                                clientOutputStream.writeObject(new Message(response));
                            }
                        } else {
                            clientOutputStream.writeObject(new Message("Failed-Customer does not exists"));
                        }
                        continue;
                    }


                    if (command.equals("ReserveCar")) {
                        if (checkCustomerExists(Integer.parseInt(parsed[1]), Integer.parseInt(parsed[2]))) {
                            TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
                            beforeOperation(Integer.parseInt(parsed[1]), "car-" + parsed[3], lockType);
                            String response = executeRequestInResourceManager(ServerType.CAR, receivedFromClient);
                            if (response.equals("false")) {
                                clientOutputStream.writeObject(new Message("Car could not be reserved"));
                            } else {
                                String request = "ReserveItem," + parsed[1] + "," + parsed[2] + "," + Car.getKey(parsed[3]) + "," + parsed[3] + "," + Integer.valueOf(response);
                                TransactionLockObject.LockType customerLockType = TransactionLockObject.LockType.LOCK_WRITE;
                                beforeOperation(Integer.parseInt(parsed[1]), "customer" + parsed[2], customerLockType);
                                response = executeRequestInResourceManager(ServerType.CUSTOMER, request);
                                clientOutputStream.writeObject(new Message(response));
                            }
                        } else {
                            clientOutputStream.writeObject(new Message("Failed-Customer does not exists"));
                        }
                        continue;
                    }


                    if (command.equals("DeleteCustomer")) {
                        TransactionLockObject.LockType writeLockType = TransactionLockObject.LockType.LOCK_WRITE;
                        beforeOperation(Integer.parseInt(parsed[1]), "customer-" + parsed[2], writeLockType);
                        String response = executeRequestInResourceManager(ServerType.CUSTOMER, receivedFromClient);
                        if (response.equals("false")) {
                            clientOutputStream.writeObject(new Message("Failed-Customer does not exists"));
                        } else if (response.equals("")) {
                            clientOutputStream.writeObject(new Message("Customer does not any reservation and was deleted successfully."));
                        } else {
                            String[] reservations = response.split(",");
                            int count = 0;
                            while (count < reservations.length) {
                                String type = reservations[count].split("-")[0];
                                if (type.equals("car")) {
                                    String addBack = "CancelCar," + parsed[1] + "," + parsed[2] + "," + reservations[count] + "," + reservations[count + 1];
                                    beforeOperation(Integer.parseInt(parsed[1]), "car-" + reservations[count], writeLockType);
                                    executeRequestInResourceManager(ServerType.CAR, addBack);
                                } else if (type.equals("room")) {
                                    String addBack = "CancelRoom," + parsed[1] + "," + parsed[2] + "," + reservations[count] + "," + reservations[count + 1];
                                    beforeOperation(Integer.parseInt(parsed[1]), "room-" + reservations[count], writeLockType);
                                    executeRequestInResourceManager(ServerType.ROOM, addBack);
                                } else {
                                    String addBack = "CancelFlight," + parsed[1] + "," + parsed[2] + "," + reservations[count] + "," + reservations[count + 1];
                                    beforeOperation(Integer.parseInt(parsed[1]), "flight-" + reservations[count], writeLockType);
                                    executeRequestInResourceManager(ServerType.FLIGHT, addBack);
                                }
                                count += 2;
                            }
                            clientOutputStream.writeObject(new Message("Customer was deleted and all his/her reservations were canceled"));
                        }
                        continue;
                    }


                    if (command.equals("Bundle")) {
                        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
                        String response = executeBundleSendBackToClient(receivedFromClient);
                        clientOutputStream.writeObject(new Message(response));
                        continue;
                    }


                    clientOutputStream.writeObject(new Message("Unsupported command! Please check the user guide!"));


                } catch (InvalidTransactionException | DeadlockException e) {
                    clientOutputStream.writeObject(new Message(e.getMessage()));
                } catch (IOException e) {
                    Trace.warn("A client is disconnected! Close the client connection in thread.");
                    break;
                }
                catch (Exception e) {
                    clientOutputStream.writeObject(new Message("Unsupported command! Please check the user guide!"));
                }
            }catch(Exception e){
                e.printStackTrace();
                Trace.error("Unhandled Exception! Close connection in a thread.");
                break;
            }
        }

        //close connection in a thread
        try {
            carInputStream.close();
            carOutputStream.close();
            carSocket.close();
            Trace.info("Car server connection is closed in a thread.");

            flightInputStream.close();
            flightOutputStream.close();
            flightSocket.close();
            Trace.info("Flight server connection is closed in a thread.");

            roomInputStream.close();
            roomOutputStream.close();
            roomSocket.close();
            Trace.info("Room server connection is closed in a thread.");

            customerInputStream.close();
            customerOutputStream.close();
            customerSocket.close();
            Trace.info("Customer server connection is closed in a thread.");

            this.clientOutputStream.close();
            this.clientInputStream.close();
            this.clientSocket.close();
            Trace.info("Client connection is closed in a thread.");

        } catch (IOException e) {
            Trace.error("Fail to close all resource manager server connection.");
            //e.printStackTrace();
        }
    }


    private boolean checkCustomerExists(int xid, int customerID) throws IOException, ClassNotFoundException, InvalidTransactionException, DeadlockException {
        String checkCustomer = String.format("QueryCustomer,%d,%d",xid,customerID);
        beforeOperation(xid, "customer-"+customerID, TransactionLockObject.LockType.LOCK_READ);
        String response = executeRequestInResourceManager(ServerType.CUSTOMER, checkCustomer);
        if(response.equals("")){
            return false;
        }
        return true;
    }

    private String executeRequestInResourceManager (ServerType serverType, String message) throws IOException, ClassNotFoundException {
        String response ="";
        switch (serverType) {
            case CAR:
                this.carOutputStream.writeObject(new Message(message));
                response = ((Message)carInputStream.readObject()).getMessageText();
                break;
            case FLIGHT:
                this.flightOutputStream.writeObject(new Message(message));
                response = ((Message)flightInputStream.readObject()).getMessageText();
                break;
            case ROOM:
                this.roomOutputStream.writeObject(new Message(message));
                response = ((Message)roomInputStream.readObject()).getMessageText();
                break;
            case CUSTOMER:
                this.customerOutputStream.writeObject(new Message(message));
                response = ((Message)customerInputStream.readObject()).getMessageText();
                break;
        }
        return response;
    }

    public RMItem readRemoteObject (int xid, String key) throws IOException, ClassNotFoundException {
        String[] parsed = key.split("-");
        String message = "ReadObject,"+ xid + "," +key;

        ObjectOutputStream outputStream = null;
        ObjectInputStream inputStream = null;

        if (parsed[0].equals("car")){
            outputStream = this.carOutputStream;
            inputStream = this.carInputStream;
        }
        if (parsed[0].equals("flight")){
            outputStream = this.flightOutputStream;
            inputStream = this.flightInputStream;
        }
        if (parsed[0].equals("room")){
            outputStream = this.roomOutputStream;
            inputStream = this.roomInputStream;
        }
        if (parsed[0].equals("customer")){
            outputStream = this.customerOutputStream;
            inputStream = this.customerInputStream;
        }
        if (outputStream!=null && inputStream!=null){
            outputStream.writeObject(new Message(message));
            RMItem object = (RMItem) inputStream.readObject();
            return object;
        }
        return null;
    }

    public boolean writeRemoteObject (int xid, String key, RMItem objectToWrite) throws IOException, ClassNotFoundException{
        String[] parsed = key.split("-");
        String m = "WriteObject,"+ xid + "," +key;
        Message message = new Message(m);
        message.setMessageObject(objectToWrite);

        ObjectOutputStream outputStream = null;
        ObjectInputStream inputStream = null;

        if (parsed[0].equals("car")){
            outputStream = this.carOutputStream;
            inputStream = this.carInputStream;
        }
        if (parsed[0].equals("flight")){
            outputStream = this.flightOutputStream;
            inputStream = this.flightInputStream;
        }
        if (parsed[0].equals("room")){
            outputStream = this.roomOutputStream;
            inputStream = this.roomInputStream;
        }
        if (parsed[0].equals("customer")){
            outputStream = this.customerOutputStream;
            inputStream = this.customerInputStream;
        }

        if (outputStream!=null && inputStream!=null){
            outputStream.writeObject(message);
            if (((Message)inputStream.readObject()).getMessageText().equals("SUCCESS")){
                Trace.info("Write object ("+ key + ") successful");
                return true;
            }
        }
        return false;
    }


    private String executeBundleSendBackToClient(String receivedFromClient) throws IOException, ClassNotFoundException, InvalidTransactionException, DeadlockException{

        String response = "";
        String failMessage = "";

        String[] parsed = receivedFromClient.split(",");
        int argumentsLength = parsed.length;
        int xid = Integer.parseInt(parsed[1]);
        int customerID = Integer.parseInt(parsed[2]);
        String location = parsed[argumentsLength - 3];
        boolean bookCar = parsed[argumentsLength - 2].equals("1");
        boolean bookRoom = parsed[argumentsLength - 1].equals("1");
        int flightAmount = argumentsLength - 6;

        ArrayList<Integer> reservedFlights = new ArrayList<>();
        boolean carSuccess = false;
        boolean roomSuccess = false;
        boolean flightSuccess = false;
        int carPrice = 0;
        int roomPrice = 0;
        ArrayList<Integer> flightsPrices = new ArrayList<>();

        TransactionLockObject.LockType wLock = TransactionLockObject.LockType.LOCK_WRITE;
        TransactionLockObject.LockType rLock = TransactionLockObject.LockType.LOCK_READ;

        // Check if client exists
        if (!checkCustomerExists(xid, customerID)) {
            response += "Failed-Customer does not exists\n";
            return response;
        }

        if (flightAmount <= 0 ){
            response += "Failed-Customer should provide at least one flight number.\n";
            return response;
        }

        // Book a car if requested.
        if (bookCar) {
            String simulatedCmd = "ReserveCar," + xid + "," + customerID + "," + location;
            beforeOperation(xid, "car-" + location, wLock);
            String bookCarResponse = executeRequestInResourceManager(ServerType.CAR, simulatedCmd);
            if (bookCarResponse.equals("false")) {
                failMessage += "Car could not be reserved. There might not be a car available for reservation at this time.\n";
            }else {
                carPrice = Integer.valueOf(bookCarResponse);
                carSuccess = true;
                response+="Car-"+location+ " is reserved by customer " + customerID + ".\n";
            }
        }else{
            carSuccess = true;
        }

        // Book a room if requested.
        if (bookRoom) {
            String simulatedCmd = "ReserveRoom," + xid + "," + customerID + "," + location;
            beforeOperation(xid, "room-" + location, wLock);
            String bookRoomResponse = executeRequestInResourceManager(ServerType.ROOM, simulatedCmd);
            if (bookRoomResponse.equals("false")) {
                failMessage+="Room could not be reserved. There might not be a room available for reservation at this time.\n";
            }else{
                roomPrice = Integer.valueOf(bookRoomResponse);
                roomSuccess = true;
                response+="Room-"+location+ " is reserved by customer " + customerID + ".\n";
            }
        }else{
            roomSuccess = true;
        }
        // Reserve flights
        for (int i = 0; i < flightAmount; i++) {
            int flightNumber = Integer.parseInt(parsed[3 + i]);
            String simulatedCmd = "ReserveFlight," + xid + "," + customerID + "," + flightNumber;
            beforeOperation(xid, "flight-" + flightNumber, wLock);
            String bookFlightResponse = executeRequestInResourceManager(ServerType.FLIGHT, simulatedCmd);
            if(bookFlightResponse.equals("false")){
                failMessage+="Flight " +flightNumber+" could not be reserved\n";
                break;
            }
            reservedFlights.add(flightNumber);
            flightsPrices.add(Integer.valueOf(bookFlightResponse));
            if(i == flightAmount-1){
                flightSuccess = true;
            }
            response+="Flight-"+flightNumber+ " is reserved by customer " + customerID + ".\n";
        }
        //reserve in customer side
        if(roomSuccess && carSuccess && flightSuccess){
            beforeOperation(xid, "customer-" + customerID, wLock);
            if(bookCar){
                String request = "ReserveItem,"+xid+","+customerID+","+ Car.getKey(location)+","+location+","+carPrice;
                executeRequestInResourceManager(ServerType.CUSTOMER, request);
            }
            if(bookRoom){
                String request = "ReserveItem,"+xid+","+customerID+","+Room.getKey(location)+","+location+","+roomPrice;
                executeRequestInResourceManager(ServerType.CUSTOMER, request);
            }
            int i =0;
            for(int flightNum: reservedFlights ){
                String request = "ReserveItem,"+xid+","+customerID+","+ Flight.getKey(flightNum)+","+flightNum+","+flightsPrices.get(i);
                executeRequestInResourceManager(ServerType.CUSTOMER, request);
                i++;
            }
            response+="Bundle is reserved successfully!\n";
            return response;
        }
        //add back reserved items.
        else{
            if(bookCar && carSuccess){
                String addBack = "CancelCar,"+xid+","+customerID+","+Car.getKey(location)+","+ 1;
                beforeOperation(xid, "car-" + location, wLock);
                executeRequestInResourceManager(ServerType.CAR, addBack);
            }
            if(bookRoom && roomSuccess){
                String addBack = "CancelRoom,"+xid+","+customerID+","+Room.getKey(location)+","+ 1;
                beforeOperation(xid, "room-" + location, wLock);
                executeRequestInResourceManager(ServerType.ROOM, addBack);
            }
            for(int flightNum: reservedFlights ){
                String addBack = "CancelFlight,"+xid+","+customerID+","+Flight.getKey(flightNum)+","+ 1;
                beforeOperation(xid, "flight-" + flightNum, wLock);
                executeRequestInResourceManager(ServerType.FLIGHT, addBack);
            }
            failMessage+="Bundle failed!\n";
            return failMessage;
        }

    }


    private void connectToServer(ServerType serverType) {
        //establish connection with resource manager server
        try {
            switch (serverType) {
                case CAR:
                    InetAddress carIp = InetAddress.getByName(carServerHost);
                    this.carSocket = new Socket(carIp, carServerPort);
                    this.carInputStream = new ObjectInputStream(this.carSocket.getInputStream());
                    this.carOutputStream = new ObjectOutputStream(this.carSocket.getOutputStream());
                    this.carOutputStream.flush();
                    break;
                case FLIGHT:
                    InetAddress flightIp = InetAddress.getByName(flightServerHost);
                    this.flightSocket = new Socket(flightIp, flightServerPort);
                    this.flightInputStream = new ObjectInputStream(this.flightSocket.getInputStream());
                    this.flightOutputStream = new ObjectOutputStream(this.flightSocket.getOutputStream());
                    this.flightOutputStream.flush();
                    break;
                case ROOM:
                    InetAddress roomIp = InetAddress.getByName(roomServerHost);
                    this.roomSocket = new Socket(roomIp, roomServerPort);
                    this.roomInputStream = new ObjectInputStream(this.roomSocket.getInputStream());
                    this.roomOutputStream = new ObjectOutputStream(this.roomSocket.getOutputStream());
                    this.roomOutputStream.flush();
                    break;
                case CUSTOMER:
                    InetAddress customerIp = InetAddress.getByName(customerServerHost);
                    this.customerSocket = new Socket(customerIp, customerServerPort);
                    this.customerInputStream = new ObjectInputStream(this.customerSocket.getInputStream());
                    this.customerOutputStream = new ObjectOutputStream(this.customerSocket.getOutputStream());
                    this.customerOutputStream.flush();
                    break;
            }
            Trace.info("connect to "+serverType.name()+" server.");
        } catch (Exception e) {
            Trace.error("Fail to connect "+serverType.name()+" server.");
        }
    }

    public int startTransaction() throws IOException {
        int xid = transactionManager.createNewTransaction();
        return xid;
    }



    public void beforeOperation (int xid, String key, TransactionLockObject.LockType lockType) throws IOException, InvalidTransactionException, DeadlockException {

        try {
            if (!transactionManager.aquireLock(xid, key, lockType)) {
                throw new InvalidTransactionException(xid, "Failed to aquire lock. Invalid Parameters.");
            }
        } catch (DeadlockException e) {
            Trace.warn("Deadlock on object (" + key + ")");
            abort(xid);
            throw new DeadlockException(xid, "Transaction-"+xid+" is aborted. Please try again later.");
        }

        if (!transactionManager.containsData(xid, key)) {
            try {
                RMItem objectData = readRemoteObject(xid, key);
                transactionManager.addBeforeImage(xid, key, objectData);
            } catch (IOException e) {
                Trace.error("I/O exception while getting remote object data.");
            } catch (ClassNotFoundException e) {
                Trace.error(e.getMessage());
            }
        }
    }

    public boolean abort(int xid) throws InvalidTransactionException {
        HashMap<String, RMItem> transactionHistory = transactionManager.getTransactionHistory(xid);

        for (Map.Entry<String, RMItem> entry : transactionHistory.entrySet()) {
            String key = entry.getKey();
            RMItem value = entry.getValue();
            try {
                writeRemoteObject(xid, key, value);
            } catch (Exception e) {
                throw new InvalidTransactionException(xid, "Failed to undo operations on object (" + key + ")");
            }
        }

        if (transactionManager.releaseLockAndRemoveTransaction (xid)) {
            return true;
        }else{
            throw new InvalidTransactionException(xid, "Failed to release all locks held by Transaction-"+ xid);
        }

    }

    public boolean commit(int xid) throws InvalidTransactionException {
        if (transactionManager.releaseLockAndRemoveTransaction (xid)) {
            return true;
        }else{
            throw new InvalidTransactionException(xid, "Failed to release all locks held by Transaction-"+ xid);
        }
    }

}
