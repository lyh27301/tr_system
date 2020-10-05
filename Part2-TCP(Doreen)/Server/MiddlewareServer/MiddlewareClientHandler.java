package Server.MiddlewareServer;

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
    static String carServerHost = "localhost";
    static int carServerPort = 6316;

    static String flightServerHost = "localhost";
    static int flightServerPort = 6216;

    static String roomServerHost = "localhost";
    static int roomServerPort = 6416;

    static String customerServerHost = "localhost";
    static int customerServerPort = 6516;

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


    public MiddlewareClientHandler(Socket clientSocket, DataInputStream inputStream, DataOutputStream outputStream) {
        this.clientInputStream = inputStream;
        this.clientOutputStream = outputStream;
        this.clientSocket = clientSocket;
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
                    //tell resource servers to quit
                    //executeRequestAndSendBackToClient(ServerType.CAR, receivedFromClient);
                    //executeRequestAndSendBackToClient(ServerType.FLIGHT, receivedFromClient);
                    //executeRequestAndSendBackToClient(ServerType.ROOM, receivedFromClient);
                    //executeRequestAndSendBackToClient(ServerType.CUSTOMER, receivedFromClient);
                    carSocket.close();
                    flightSocket.close();
                    roomSocket.close();
                    customerSocket.close();
                    clientOutputStream.writeUTF("Good Bye");
                    this.clientSocket.close();
                    Trace.info("Quit Command received.");
                    break;
                }

                //Choose the responsible service
                if (command.equals("AddCars") || command.equals("DeleteCars") || command.equals("QueryCars")
                        || command.equals("QueryCarsPrice") || command.equals("ReserveCar")) {
                    executeRequestAndSendBackToClient(ServerType.CAR, receivedFromClient);
                }
                if (command.equals("AddFlight") || command.equals("DeleteFlights") || command.equals("QueryFlight")
                        || command.equals("QueryFlightPrice") || command.equals("ReserveFlight")) {
                    executeRequestAndSendBackToClient(ServerType.FLIGHT, receivedFromClient);
                }
                if (command.equals("AddRooms") || command.equals("DeleteRooms") || command.equals("QueryRooms")
                        || command.equals("QueryRoomsPrice") || command.equals("ReserveRoom")) {
                    executeRequestAndSendBackToClient(ServerType.ROOM, receivedFromClient);
                }
                if (command.equals("AddCustomer") || command.equals("AddCustomerID") || command.equals("DeleteCustomer")
                        || command.equals("QueryCustomer")) {
                    executeRequestAndSendBackToClient(ServerType.CUSTOMER, receivedFromClient);
                }
                if (command.equals("Bundle")) {
                    //executeBundleSendBackToClient(receivedFromClient);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        try {
            carInputStream.close();
            carOutputStream.close();
            flightInputStream.close();
            flightOutputStream.close();
            roomInputStream.close();
            roomOutputStream.close();
            customerInputStream.close();
            customerOutputStream.close();
            this.clientOutputStream.close();
            this.clientInputStream.close();
            Trace.info("Connections to the client and resource managers are closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void executeRequestAndSendBackToClient(ServerType serverType, String message) throws IOException {
        switch (serverType) {
            case CAR:
                this.carOutputStream.writeUTF(message);
                String receivedFromCarResourceManager = carInputStream.readUTF();
                clientOutputStream.writeUTF(receivedFromCarResourceManager);
                break;
            case FLIGHT:
                this.flightOutputStream.writeUTF(message);
                String receivedFromFlightResourceManager = flightInputStream.readUTF();
                clientOutputStream.writeUTF(receivedFromFlightResourceManager);
                break;
            case ROOM:
                this.roomOutputStream.writeUTF(message);
                String receivedFromRoomResourceManager = roomInputStream.readUTF();
                clientOutputStream.writeUTF(receivedFromRoomResourceManager);
                break;
            case CUSTOMER:
                this.customerOutputStream.writeUTF(message);
                String receivedFromCustomerResourceManager = customerInputStream.readUTF();
                clientOutputStream.writeUTF(receivedFromCustomerResourceManager);
                break;
        }

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
