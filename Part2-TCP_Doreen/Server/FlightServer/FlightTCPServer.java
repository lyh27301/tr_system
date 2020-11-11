package Server.FlightServer;

import Server.CarServer.CarTCPServer;
import Server.Common.ClientConnection;
import Server.Common.RMHashMap;
import Server.Common.Trace;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class FlightTCPServer {

    static int port = 6216;
    static ServerSocket serverSocket = null;
    static RMHashMap flight_data = new RMHashMap();
    static ArrayList<ClientConnection> allThreadsConnections = new ArrayList<>();

    static Object shutdownSignal = new Object();

    static class ShutdownThread extends Thread{
        @Override
        public void run() {
            synchronized(shutdownSignal){
                try{
                    shutdownSignal.wait();
                } catch(InterruptedException e){
                    e.printStackTrace();
                    return;
                }
                try {
                    for (ClientConnection c : allThreadsConnections) {
                        c.getInputStream().close();
                        c.getOutputStream().close();
                        c.getSoket().close();
                    }
                    serverSocket.close();
                }catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                return;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(port);
        Trace.info("Flight server is running ...");

        Thread shutdown = new FlightTCPServer.ShutdownThread();
        shutdown.start();

        while (true) {
            Socket clientSocket = null;
            try{
                try {
                    clientSocket = serverSocket.accept();
                    Trace.info("A new client is connected");
                }catch (IOException e){
                    Trace.warn("Server socket connection is closed.");
                    return;
                }
                ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());

                outputStream.flush();

                ClientConnection connection = new ClientConnection(clientSocket, inputStream, outputStream);
                allThreadsConnections.add(connection);

                Thread t = new FlightResourceManager(clientSocket, inputStream, outputStream, flight_data);
                t.start();

            } catch (IOException e) {
                if (!clientSocket.isClosed()) clientSocket.close();
                e.printStackTrace();
            }
        }
    }
}
