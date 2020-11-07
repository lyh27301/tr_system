package Server.FlightServer;

import Server.Common.Trace;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FlightTCPServer {

    static int port = 6216;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        Trace.info("Flight server is running ...");

        while (true) {
            Socket clientSocket = null;
            try{
                clientSocket = serverSocket.accept();
                Trace.info("A new client is connected");
                ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());

                outputStream.flush();

                Thread t = new FlightResourceManager(clientSocket, inputStream, outputStream);
                t.start();

            } catch (IOException e) {
                clientSocket.close();
                e.printStackTrace();
            }
        }
    }
}
