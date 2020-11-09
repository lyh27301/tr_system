package Server.RoomServer;

import Server.CarServer.CarTCPServer;
import Server.Common.Message;
import Server.Common.RMItem;
import Server.Common.Trace;
import Server.CustomerServer.CustomerTCPServer;
import Server.FlightServer.FlightTCPServer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RoomTCPServer extends RoomResourceManager{
    static int port = 6416;
    private static RoomTCPServer manager = null;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        Trace.info("Room server is running ...");
        manager = new RoomTCPServer();
        while (true) {
//            Socket clientSocket = null;
            try{
//                clientSocket = serverSocket.accept();
//                new ClientHandler(clientSocket).start();
                new ClientHandler(serverSocket.accept()).start();
                //Trace.info("A new client is connected");
                //ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                //ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());

                //outputStream.flush();

                //Thread t = new CustomerResourceManager(clientSocket, inputStream, outputStream);
                //t.start();

            } catch (IOException e) {
                //clientSocket.close();
                e.printStackTrace();
            }
//            finally {
//                try {
//                    serverSocket.close();
//                    System.out.println("'" + "RoomTCPServer" + ":" + port + "' Server Socket closed");
//                }
//                catch(IOException e) {
//                    System.err.println((char)27 + "[31;1mResource Manager exception: " + (char)27 + e.toString());
//                }
//            }
        }
    }
    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private ObjectOutputStream outputStream;
        private ObjectInputStream inputStream;

        public ClientHandler(Socket socket){
            this.clientSocket = socket;

        }
        public void run() {
            try {
                Trace.info("A new client is connected");
                outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                inputStream = new ObjectInputStream(clientSocket.getInputStream());
                outputStream.flush();

                Message message = (Message)inputStream.readObject();
                String received = message.getMessageText();
                String[] parsed = received.split(",");
                if (parsed[0].equals("Quit")){
                    outputStream.writeObject(new Message("Quit Received"));
                    Trace.info("Quitting a client connection...");
                }

                if (parsed[0].equals("ReadObject")){
                    Object obj = manager.readData(manager.stringToInt(parsed[1]), parsed[2]);
                    outputStream.writeObject(obj);
                    Trace.info("Return object with key "+ parsed[2]);
                }

                else if (parsed[0].equals("WriteObject")){
                    manager.writeData(manager.stringToInt(parsed[1]), parsed[2], (RMItem) message.getMessageObject());
                    outputStream.writeObject(new Message("SUCCESS"));
                    Trace.info("Successfully write object with key "+ parsed[2]);
                }

                else{
                    String response = manager.executeRequest(parsed);
                    outputStream.writeObject(new Message(response));
                }

                this.clientSocket.close();

                this.inputStream.close();
                this.outputStream.close();
                Trace.info("Client connection is closed.");

            }catch (IOException | ClassNotFoundException e){
                e.printStackTrace();
            }

        }
    }
}
