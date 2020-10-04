package Server.ResourceManagerServer.RoomServer;

import Server.ResourceManagerServer.ResourceManagerTCPServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class RoomTCPServer extends RoomResourceManager{

    public RoomTCPServer(String p_name) {
        super(p_name);
    }
    private static RoomTCPServer manager = null;

    private ServerSocket serverSocket;

    private static int port = 6416;

    public static void main(String[] args) {
        String name = null;

        try {

            if (args.length == 1) {
                String[] info = args[0].split(",");

                name = info[0];
                port = Integer.parseInt(info[1]);
            } else {
                System.err.println(
                        (char) 27 + "[31;1mResource Manager exception: " + (char) 27 + "Must specify name,port");
                System.exit(1);
            }

            manager = new RoomTCPServer(name);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    manager.stop();
                }
            });
            System.out.println("Starting '" + name + ":" + port + "'");
            manager.start(port);
        } catch (Exception e) {
            System.err.println((char) 27 + "[31;1mResource Manager exception: " + (char) 27 + e.toString());
            System.exit(1);
        }
    }

    private void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Listening on port: " + port);
            while (true)
                new RoomTCPServer.ClientHandler(serverSocket.accept()).start();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            serverSocket.close();
            System.out.println("'" + this.getName() + ":" + port + "' Server Socket closed");
        } catch (IOException e) {
            System.err.println((char) 27 + "[31;1mResource Manager exception: " + (char) 27 + e.toString());
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public static Vector<String> parse(String input) {
            if (input != null && input.length() != 0) {

                String command;

                if (input.charAt(0) == '[' && input.charAt(input.length() - 1) == ']') {
                    // If there are brackets, remove them.
                    command = input.substring(1, input.length() - 1);
                } else {
                    command = input;
                }
                Vector<String> arguments = new Vector<String>();
                String[] commandParts = command.split(", ");
                for (int i = 0; i < commandParts.length; i++) {
                    arguments.add(commandParts[i].trim());
                }

                return arguments;
            }

            return null;

        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine = in.readLine();

                Vector<String> parsedCommand = parse(inputLine);

                if (parsedCommand == null) {
                    out.println("");
                    in.close();
                    out.close();
                    clientSocket.close();
                    return;
                }

                String result = manager.execute(parsedCommand);
                //String result = execute(parsedCommand);

                out.println(result);
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println(
                        (char) 27 + "[31;1mResource Manager exception: " + (char) 27 + "[0m" + e.getLocalizedMessage());
            }
        }

    }


}
