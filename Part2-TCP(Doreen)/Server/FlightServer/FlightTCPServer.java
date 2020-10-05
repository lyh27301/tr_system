package Server.FlightServer;

import Server.Common.Trace;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FlightTCPServer {

    static int port = 6216;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);

        while (true) {
            Socket clientSocket = null;
            try{
                clientSocket = serverSocket.accept();
                Trace.info("A new client is connected");

                DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());

                Thread t = new FlightResourceManager(clientSocket, inputStream, outputStream);
                t.start();

            } catch (IOException e) {
                clientSocket.close();
                e.printStackTrace();
            }
        }
    }
}
