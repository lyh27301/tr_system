package Server.CarServer;

import Server.Common.Trace;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CarTCPServer {

    static int port = 6316;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        Trace.info("Car server is running ...");

        while (true) {
            Socket clientSocket = null;
            try{
                clientSocket = serverSocket.accept();
                Trace.info("A new client is connected");
                ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());

                outputStream.flush();

                Thread t = new CarResourceManager(clientSocket, inputStream, outputStream);
                t.start();

            } catch (IOException e) {
                clientSocket.close();
                e.printStackTrace();
            }
        }
    }

}
