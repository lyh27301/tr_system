package Server.MiddlewareServer;

import Server.Common.Room;
import Server.Common.Trace;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MiddlewareClientHandler extends Thread {

    //connection to the client
    final Socket clientSocket;
    final DataInputStream clientInputStream;
    final DataOutputStream clientOutputStream;


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
    DataInputStream carInputStream;
    DataOutputStream carOutputStream;

    Socket flightSocket;
    DataInputStream flightInputStream;
    DataOutputStream flightOutputStream;

    Socket roomSocket;
    DataInputStream roomInputStream;
    DataOutputStream roomOutputStream;

    Socket customerSocket;
    DataInputStream customerInputStream;
    DataOutputStream customerOutputStream;


    public MiddlewareClientHandler(Socket clientSocket,
                                   DataInputStream inputStream,
                                   DataOutputStream outputStream,
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
                String receivedFromClient = clientInputStream.readUTF();

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

                    clientOutputStream.writeUTF("Good Bye");
                    break;
                }

                //Choose the responsible service
                else if (command.equals("AddCars") || command.equals("DeleteCars") || command.equals("QueryCars")
                        || command.equals("QueryCarsPrice")) {
                    String response = executeRequestInResourceManager(ServerType.CAR, receivedFromClient);
                    clientOutputStream.writeUTF(response);
                }
                else if (command.equals("AddFlight") || command.equals("DeleteFlights") || command.equals("QueryFlight")
                        || command.equals("QueryFlightPrice") ) {
                    String response = executeRequestInResourceManager(ServerType.FLIGHT, receivedFromClient);
                    clientOutputStream.writeUTF(response);
                }
                else if (command.equals("AddRooms") || command.equals("DeleteRooms") || command.equals("QueryRooms")
                        || command.equals("QueryRoomsPrice") ) {
                    String response = executeRequestInResourceManager(ServerType.ROOM, receivedFromClient);
                    clientOutputStream.writeUTF(response);
                }
                else if (command.equals("AddCustomer") || command.equals("AddCustomerID") || command.equals("DeleteCustomer")
                        || command.equals("QueryCustomer")) {
                    String response = executeRequestInResourceManager(ServerType.CUSTOMER, receivedFromClient);
                    clientOutputStream.writeUTF(response);
                }
                else if(command.equals("ReserveRoom")){
                    if(checkCustomerExists(Integer.parseInt(parsed[1]),Integer.parseInt(parsed[2]))){
                        String response = executeRequestInResourceManager(ServerType.ROOM, receivedFromClient);
                        if(response.equals(false)){
                            clientOutputStream.writeUTF("Room could not be reserved");
                        }else{
                            String request = "ReserveItem,"+parsed[1]+","+parsed[2]+","+parsed[3]+","+parsed[3]+","+Integer.valueOf(response);
                            response = executeRequestInResourceManager(ServerType.CUSTOMER, request);
                            clientOutputStream.writeUTF(response);
                        }
                    }else{
                        clientOutputStream.writeUTF("Failed-Customer does not exists");
                    }

                }
                else if (command.equals("ReserveFlight")){
                    if(checkCustomerExists(Integer.parseInt(parsed[1]),Integer.parseInt(parsed[2]))){
                        String response = executeRequestInResourceManager(ServerType.FLIGHT, receivedFromClient);
                        if(response.equals(false)){
                            clientOutputStream.writeUTF("Flight could not be reserved");
                        }else{
                            String request = "ReserveItem,"+parsed[1]+","+parsed[2]+","+parsed[3]+","+parsed[3]+","+Integer.valueOf(response);
                            response = executeRequestInResourceManager(ServerType.CUSTOMER, request);
                            clientOutputStream.writeUTF(response);
                        }
                    }else{
                        clientOutputStream.writeUTF("Failed-Customer does not exists");
                    }
                }
                else if( command.equals("ReserveCar")){
                    if(checkCustomerExists(Integer.parseInt(parsed[1]),Integer.parseInt(parsed[2]))){
                        String response = executeRequestInResourceManager(ServerType.CAR, receivedFromClient);
                        if(response.equals(false)){
                            clientOutputStream.writeUTF("Car could not be reserved");
                        }else{
                            String request = "ReserveItem,"+parsed[1]+","+parsed[2]+","+parsed[3]+","+parsed[3]+","+Integer.valueOf(response);
                            response = executeRequestInResourceManager(ServerType.CUSTOMER, request);
                            clientOutputStream.writeUTF(response);
                        }
                    }else{
                        clientOutputStream.writeUTF("Failed-Customer does not exists");
                    }

                }
                else if (command.equals("Bundle")) {
                    //executeBundleSendBackToClient(receivedFromClient);
                }
                else {
                    clientOutputStream.writeUTF("Unsupported command! Please check the user guide!");
                }

            } catch (IOException e) {
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
    private boolean checkCustomerExists(int xid, int customerID) throws IOException {
        String checkCustomer = String.format("QueryCustomer,%d,%d",xid,customerID);
        String response = executeRequestInResourceManager(ServerType.CUSTOMER, checkCustomer);
        if(response.equals("")){
            return false;
        }
        return true;
    }

    private String executeRequestInResourceManager (ServerType serverType, String message) throws IOException {
        String response ="";
        switch (serverType) {
            case CAR:
                this.carOutputStream.writeUTF(message);
                response = carInputStream.readUTF();
                break;
            case FLIGHT:
                this.flightOutputStream.writeUTF(message);
                response = flightInputStream.readUTF();
                break;
            case ROOM:
                this.roomOutputStream.writeUTF(message);
                response = roomInputStream.readUTF();
                break;
            case CUSTOMER:
                this.customerOutputStream.writeUTF(message);
                response = customerInputStream.readUTF();
                break;
        }
        return response;
    }



    public void executeBundleSendBackToClient(String receivedFromClient) {
        //ToDo: implement bundle
    }


    private void connectToServer(ServerType serverType) {
        //establish connection with resource manager server
        try {
            switch (serverType) {
                case CAR:
                    InetAddress carIp = InetAddress.getByName(carServerHost);
                    this.carSocket = new Socket(carIp, carServerPort);
                    this.carInputStream = new DataInputStream(this.carSocket.getInputStream());
                    this.carOutputStream = new DataOutputStream(this.carSocket.getOutputStream());
                    break;
                case FLIGHT:
                    InetAddress flightIp = InetAddress.getByName(flightServerHost);
                    this.flightSocket = new Socket(flightIp, flightServerPort);
                    this.flightInputStream = new DataInputStream(this.flightSocket.getInputStream());
                    this.flightOutputStream = new DataOutputStream(this.flightSocket.getOutputStream());
                    break;
                case ROOM:
                    InetAddress roomIp = InetAddress.getByName(roomServerHost);
                    this.roomSocket = new Socket(roomIp, roomServerPort);
                    this.roomInputStream = new DataInputStream(this.roomSocket.getInputStream());
                    this.roomOutputStream = new DataOutputStream(this.roomSocket.getOutputStream());
                    break;
                case CUSTOMER:
                    InetAddress customerIp = InetAddress.getByName(customerServerHost);
                    this.customerSocket = new Socket(customerIp, customerServerPort);
                    this.customerInputStream = new DataInputStream(this.customerSocket.getInputStream());
                    this.customerOutputStream = new DataOutputStream(this.customerSocket.getOutputStream());
                    break;
            }
            System.out.println("connect server "+serverType.name()+" server.");
        } catch (Exception e) {
            Trace.error("Fail to connect server "+serverType.name()+" server.");
            e.printStackTrace();
        }
    }
}
