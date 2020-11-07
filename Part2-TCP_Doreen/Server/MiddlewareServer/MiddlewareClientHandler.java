package Server.MiddlewareServer;

import Server.Common.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

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


    public MiddlewareClientHandler(Socket clientSocket,
                                   ObjectInputStream inputStream,
                                   ObjectOutputStream outputStream,
                                   String carServerHost,
                                   String flightServerHost,
                                   String roomServerHost,
                                   String customerServerHost) {

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
    }

    @Override
    public void run() {
        while (true) {
            try {
                // receive client request
                String receivedFromClient = ((Message)clientInputStream.readObject()).getMessageText();


                String[] parsed = receivedFromClient.split(",");

                String command = parsed[0];
                if (command.equals("Quit")) {
                    if (executeRequestInResourceManager(ServerType.CAR, receivedFromClient).equals("Quit Received")){
                        Trace.info("Quitting the car server connection in a thread...");
                    }
                    if (executeRequestInResourceManager(ServerType.FLIGHT, receivedFromClient).equals("Quit Received")){
                        Trace.info("Quitting the flight server connection in a thread...");
                    }
                    if (executeRequestInResourceManager(ServerType.ROOM, receivedFromClient).equals("Quit Received")){
                        Trace.info("Quitting the room server connection in a thread...");
                    }
                    if (executeRequestInResourceManager(ServerType.CUSTOMER, receivedFromClient).equals("Quit Received")){
                        Trace.info("Quitting the customer server connection in a thread...");
                    }

                    clientOutputStream.writeObject(new Message("Good Bye"));
                    break;
                }

                //Choose the responsible service
                else if (command.equals("AddCars") || command.equals("DeleteCars") || command.equals("QueryCars")
                        || command.equals("QueryCarsPrice")) {
                    String response = executeRequestInResourceManager(ServerType.CAR, receivedFromClient);
                    clientOutputStream.writeObject(new Message(response));
                }
                else if (command.equals("AddFlight") || command.equals("DeleteFlight") || command.equals("QueryFlight")
                        || command.equals("QueryFlightPrice") ) {
                    String response = executeRequestInResourceManager(ServerType.FLIGHT, receivedFromClient);
                    clientOutputStream.writeObject(new Message(response));
                }
                else if (command.equals("AddRooms") || command.equals("DeleteRooms") || command.equals("QueryRooms")
                        || command.equals("QueryRoomsPrice") ) {
                    String response = executeRequestInResourceManager(ServerType.ROOM, receivedFromClient);
                    clientOutputStream.writeObject(new Message(response));
                }
                else if (command.equals("AddCustomer") || command.equals("AddCustomerID")
                        || command.equals("QueryCustomer")) {
                    String response = executeRequestInResourceManager(ServerType.CUSTOMER, receivedFromClient);
                    clientOutputStream.writeObject(new Message(response));
                }
                else if(command.equals("ReserveRoom")){
                    if(checkCustomerExists(Integer.parseInt(parsed[1]),Integer.parseInt(parsed[2]))){
                        String response = executeRequestInResourceManager(ServerType.ROOM, receivedFromClient);
                        if(response.equals("false")){
                            clientOutputStream.writeObject(new Message("Room could not be reserved"));
                        }else{
                            String request = "ReserveItem,"+parsed[1]+","+parsed[2]+","+Room.getKey(parsed[3])+","+parsed[3]+","+Integer.valueOf(response);
                            response = executeRequestInResourceManager(ServerType.CUSTOMER, request);
                            clientOutputStream.writeObject(new Message(response));
                        }
                    }else{
                        clientOutputStream.writeObject(new Message("Failed-Customer does not exists"));
                    }

                }
                else if (command.equals("ReserveFlight")){
                    if(checkCustomerExists(Integer.parseInt(parsed[1]),Integer.parseInt(parsed[2]))){
                        String response = executeRequestInResourceManager(ServerType.FLIGHT, receivedFromClient);
                        if(response.equals("false")){
                            clientOutputStream.writeObject(new Message("Flight could not be reserved"));
                        }else{
                            String request = "ReserveItem,"+parsed[1]+","+parsed[2]+","+ Flight.getKey(Integer.parseInt(parsed[3]))+","+parsed[3]+","+Integer.valueOf(response);
                            response = executeRequestInResourceManager(ServerType.CUSTOMER, request);
                            clientOutputStream.writeObject(new Message(response));
                        }
                    }else{
                        clientOutputStream.writeObject(new Message("Failed-Customer does not exists"));
                    }
                }
                else if( command.equals("ReserveCar")){
                    if(checkCustomerExists(Integer.parseInt(parsed[1]),Integer.parseInt(parsed[2]))){
                        String response = executeRequestInResourceManager(ServerType.CAR, receivedFromClient);
                        if(response.equals("false")){
                            clientOutputStream.writeObject(new Message("Car could not be reserved"));
                        }else{
                            String request = "ReserveItem,"+parsed[1]+","+parsed[2]+","+ Car.getKey(parsed[3])+","+parsed[3]+","+Integer.valueOf(response);
                            response = executeRequestInResourceManager(ServerType.CUSTOMER, request);
                            clientOutputStream.writeObject(new Message(response));
                        }
                    }else{
                        clientOutputStream.writeObject(new Message("Failed-Customer does not exists"));
                    }

                }
                else if (command.equals("DeleteCustomer")){
                    String response = executeRequestInResourceManager(ServerType.CUSTOMER, receivedFromClient);
                    if(response.equals("false")){
                        clientOutputStream.writeObject(new Message("Failed-Customer does not exists"));
                    }
                    else if(response.equals("")){
                        clientOutputStream.writeObject(new Message("Customer does not any reservation and was deleted successfully."));
                    }
                    else{
                        String[] reservations = response.split(",");
                        int count = 0;
                        while(count < reservations.length){
                            String type = reservations[count].split("-")[0];
                            if(type.equals("car")){
                                String addBack = "CancelCar,"+parsed[1]+","+parsed[2]+","+reservations[count] +","+ reservations[count+1];
                                executeRequestInResourceManager(ServerType.CAR, addBack);
                            }else if(type.equals("room")){
                                String addBack = "CancelRoom,"+parsed[1]+","+parsed[2]+","+reservations[count] +","+ reservations[count+1];
                                executeRequestInResourceManager(ServerType.ROOM, addBack);
                            }else{
                                String addBack = "CancelFlight,"+parsed[1]+","+parsed[2]+","+reservations[count] +","+ reservations[count+1];
                                executeRequestInResourceManager(ServerType.FLIGHT, addBack);
                            }
                            count +=2;
                        }
                        clientOutputStream.writeObject(new Message("Customer was deleted and all his/her reservations were canceled"));
                    }
                }
                else if (command.equals("Bundle")) {
                    String response = executeBundleSendBackToClient(receivedFromClient);
                    clientOutputStream.writeObject(new Message(response));
                }
                else {
                    clientOutputStream.writeObject(new Message("Unsupported command! Please check the user guide!"));
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        try {
            carInputStream.close();
            carOutputStream.close();
            Trace.info("Car server connection is closed in a thread.");

            flightInputStream.close();
            flightOutputStream.close();
            Trace.info("Flight server connection is closed in a thread.");

            roomInputStream.close();
            roomOutputStream.close();
            Trace.info("Room server connection is closed in a thread.");

            customerInputStream.close();
            customerOutputStream.close();
            Trace.info("Customer server connection is closed in a thread.");

            this.clientSocket.close();
            this.clientOutputStream.close();
            this.clientInputStream.close();
            Trace.info("Client connection is closed in a thread.");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private boolean checkCustomerExists(int xid, int customerID) throws IOException, ClassNotFoundException {
        String checkCustomer = String.format("QueryCustomer,%d,%d",xid,customerID);
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



    public String executeBundleSendBackToClient(String receivedFromClient) throws IOException, ClassNotFoundException {

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
                executeRequestInResourceManager(ServerType.CAR, addBack);
            }
            if(bookRoom && roomSuccess){
                String addBack = "CancelRoom,"+xid+","+customerID+","+Room.getKey(location)+","+ 1;
                executeRequestInResourceManager(ServerType.ROOM, addBack);
            }
            for(int flightNum: reservedFlights ){
                String addBack = "CancelFlight,"+xid+","+customerID+","+Flight.getKey(flightNum)+","+ 1;
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
            System.out.println("connect server "+serverType.name()+" server.");
        } catch (Exception e) {
            Trace.error("Fail to connect server "+serverType.name()+" server.");
            e.printStackTrace();
        }
    }
}
