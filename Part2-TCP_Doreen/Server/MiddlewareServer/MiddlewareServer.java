package Server.MiddlewareServer;


import Server.Common.Trace;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MiddlewareServer {

    //middleware server port
    static int port = 6116;

    enum ServerType {CAR, FLIGHT, ROOM, CUSTOMER}

    //default configuration
    static String carServerHost = "localhost";
    static int carServerPort = 6316;

    static String flightServerHost = "localhost";
    static int flightServerPort = 6216;

    static String roomServerHost = "localhost";
    static int roomServerPort = 6416;

    static String customerServerHost = "localhost";
    static int customerServerPort = 6516;



    //connections
    static Socket carSocket;
    static ObjectInputStream carInputStream;
    static ObjectOutputStream carOutputStream;

    static Socket flightSocket;
    static ObjectInputStream flightInputStream;
    static ObjectOutputStream flightOutputStream;

    static Socket roomSocket;
    static ObjectInputStream roomInputStream;
    static ObjectOutputStream roomOutputStream;

    static Socket customerSocket;
    static ObjectInputStream customerInputStream;
    static ObjectOutputStream customerOutputStream;




    public static void main(String[] args) throws IOException {

        //set host from arguments
        if (args.length > 0) carServerHost = args[0];
        if (args.length > 1) flightServerHost = args[1];
        if (args.length > 2) roomServerHost = args[2];
        if (args.length > 3) customerServerHost = args[3];

        ServerSocket serverSocket = new ServerSocket(port);

        //Connect to resource manager servers
        connectToServer(ServerType.CAR);
        connectToServer(ServerType.FLIGHT);
        connectToServer(ServerType.ROOM);
        connectToServer(ServerType.CUSTOMER);


        //Transaction Manager
        TransactionManager transactionManager = new TransactionManager();

        while (true) {
            Socket clientSocket = null;
            ObjectOutputStream outputStream = null;
            ObjectInputStream inputStream = null;
            try{
                clientSocket = serverSocket.accept();
                Trace.info("A new client is connected");
                outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                inputStream = new ObjectInputStream(clientSocket.getInputStream());
                outputStream.flush();
                Thread t = new MiddlewareClientHandler(clientSocket,
                        inputStream, outputStream,
                        carInputStream,carOutputStream,
                        flightInputStream,flightOutputStream,
                        roomInputStream,roomOutputStream,
                        customerInputStream,customerOutputStream,
                        transactionManager);
                t.start();

            } catch (IOException e) {
                clientSocket.close();
                inputStream.close();
                outputStream.close();
                Trace.info("Client connection is closed.");
            }
        }
    }


    private static void connectToServer(ServerType serverType) {
        //establish connection with resource manager server
        try {
            switch (serverType) {
                case CAR:
                    InetAddress carIp = InetAddress.getByName(carServerHost);
                    carSocket = new Socket(carIp, carServerPort);
                    carInputStream = new ObjectInputStream(carSocket.getInputStream());
                    carOutputStream = new ObjectOutputStream(carSocket.getOutputStream());
                    carOutputStream.flush();
                    break;
                case FLIGHT:
                    InetAddress flightIp = InetAddress.getByName(flightServerHost);
                    flightSocket = new Socket(flightIp, flightServerPort);
                    flightInputStream = new ObjectInputStream(flightSocket.getInputStream());
                    flightOutputStream = new ObjectOutputStream(flightSocket.getOutputStream());
                    flightOutputStream.flush();
                    break;
                case ROOM:
                    InetAddress roomIp = InetAddress.getByName(roomServerHost);
                    roomSocket = new Socket(roomIp, roomServerPort);
                    roomInputStream = new ObjectInputStream(roomSocket.getInputStream());
                    roomOutputStream = new ObjectOutputStream(roomSocket.getOutputStream());
                    roomOutputStream.flush();
                    break;
                case CUSTOMER:
                    InetAddress customerIp = InetAddress.getByName(customerServerHost);
                    customerSocket = new Socket(customerIp, customerServerPort);
                    customerInputStream = new ObjectInputStream(customerSocket.getInputStream());
                    customerOutputStream = new ObjectOutputStream(customerSocket.getOutputStream());
                    customerOutputStream.flush();
                    break;
            }
            System.out.println("connect server "+serverType.name()+" server.");
        } catch (Exception e) {
            Trace.error("Fail to connect server "+serverType.name()+" server.");
        }
    }


}
