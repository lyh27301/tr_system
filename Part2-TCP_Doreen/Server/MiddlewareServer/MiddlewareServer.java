package Server.MiddlewareServer;


import Server.Common.Trace;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MiddlewareServer {

    //middleware server port
    static int port = 6116;


    public static void main(String[] args) throws IOException {

        //default hosts
        String carServerHost = "localhost";
        String flightServerHost = "localhost";
        String roomServerHost = "localhost";
        String customerServerHost = "localhost";

        //set host from arguments
        if (args.length > 0) carServerHost = args[0];
        if (args.length > 1) flightServerHost = args[1];
        if (args.length > 2) roomServerHost = args[2];
        if (args.length > 3) customerServerHost = args[3];

        ServerSocket serverSocket = new ServerSocket(port);

        while (true) {
            Socket clientSocket = null;
            try{
                clientSocket = serverSocket.accept();
                Trace.info("A new client is connected");
                ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
                outputStream.flush();
                Thread t = new MiddlewareClientHandler(clientSocket, inputStream, outputStream,
                        carServerHost, flightServerHost, roomServerHost, customerServerHost);
                t.start();

            } catch (IOException e) {
                clientSocket.close();
                e.printStackTrace();
            }
        }
    }


}
