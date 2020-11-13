package Server.MiddlewareServer;



import Server.Common.ClientConnection;
import Server.Common.Trace;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MiddlewareServer {

    //middleware server port
    static int port = 6116;
    static ServerSocket serverSocket = null;
    static TransactionManager transactionManager = new TransactionManager();
    static ArrayList<ClientConnection> allThreadsConnections = new ArrayList<>();

    static ArrayList<Integer> timeoutTransactions = new ArrayList<>();

    static void addTimeoutTransaction(int xid){
        synchronized (timeoutTransactions){
            timeoutTransactions.add(xid);
        }
    }

    static String getTimeoutTransaction () {
        synchronized (timeoutTransactions){
            String s = "";
            for (int i=0; i<timeoutTransactions.size(); i++){
                if (i==0) {
                    s = s + timeoutTransactions.get(i);
                } else {
                    s = s + ", " + timeoutTransactions.get(i);
                }
            }
            timeoutTransactions.clear();
            return s;
        }
    }



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


    public static void main(String[] args) throws IOException{
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


        serverSocket = new ServerSocket(port);
        Trace.info("Middleware server is running ...");

        Thread shutdown = new ShutdownThread();
        shutdown.start();

        while (true) {
            //accept a new client connection
            Socket clientSocket = null;
            ObjectOutputStream outputStream = null;
            ObjectInputStream inputStream = null;
            try {
                try {
                    clientSocket = serverSocket.accept();
                    Trace.info("A new client is connected");
                } catch (IOException e) {
                    Trace.warn("Server socket connection is closed.");
                    return;
                }
                outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                inputStream = new ObjectInputStream(clientSocket.getInputStream());
                outputStream.flush();


                ClientConnection connection = new ClientConnection(clientSocket, inputStream, outputStream);
                allThreadsConnections.add(connection);

                Thread t = new MiddlewareClientHandler(clientSocket, inputStream, outputStream,
                        carServerHost, flightServerHost, roomServerHost, customerServerHost, transactionManager);
                t.start();
            } catch (IOException e) {
                if (!clientSocket.isClosed()) clientSocket.close();
                return;
            }
        }
    }


}
