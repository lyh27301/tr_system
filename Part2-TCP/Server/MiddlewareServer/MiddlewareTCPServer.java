package Server.MiddlewareServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.*;
import java.util.Vector;

public class MiddlewareTCPServer extends MiddlewareResourceManager {

    private static MiddlewareTCPServer middleware = null;

    private static int s_serverPort = 6116;
    private ServerSocket serverSocket;

    private static String flightHost = "localhost";
    private static int flightPort = 6216;

    private static String carHost = "localhost";
    private static int carPort = 6316;

    private static String roomHost = "localhost";
    private static int roomPort = 6416;

    private static String customerHost = "localhost";
    private static int customerPort = 6516;

    public static void main(String[] args) {
        try {

            if (args.length > 2) {
                String[] flightInfo = args[0].split(",");
                String[] carInfo = args[1].split(",");
                String[] roomInfo = args[2].split(",");
                String[] customerInfo = args[3].split(",");

                flightHost = flightInfo[0];
                flightPort = Integer.parseInt(flightInfo[1]);

                carHost = carInfo[0];
                carPort = Integer.parseInt(carInfo[1]);

                roomHost = roomInfo[0];
                roomPort = Integer.parseInt(roomInfo[1]);

                customerHost = customerInfo[0];
                customerPort = Integer.parseInt(customerInfo[1]);
            }

            middleware = new MiddlewareTCPServer("Middleware", flightHost, flightPort, carHost, carPort, roomHost,
                    roomPort, customerHost, customerPort);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    middleware.stop();
                }
            });
            System.out.println("Starting 'Middleware:" + s_serverPort + "'");
            middleware.start(s_serverPort);
        } catch (Exception e) {
            System.err.println((char) 27 + "[31;1mMiddleware exception: " + (char) 27 + e.toString());
            System.exit(1);
        }
    }

    public MiddlewareTCPServer(String name, String flightHost, int flightPort, String carHost, int carPort,
            String roomHost, int roomPort, String customerHost, int customerPort) {
        super(name, flightHost, flightPort, carHost, carPort, roomHost, roomPort, customerHost, customerPort);
    }

    private void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Listening on port: " + port);
            while (true)
                new ClientHandler(serverSocket.accept()).start();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            this.close();
            serverSocket.close();
            System.out.println("'Middleware:" + s_serverPort + "' Server Socket closed");
        } catch (IOException e) {
            System.err.println((char) 27 + "[31;1mMiddleware exception: " + (char) 27 + e.toString());
        }
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

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
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

                String result = middleware.execute(parsedCommand);
                // in resourceManager :
//                protected String execute(Vector<String> command) {
//                    System.out.println(this.getName() + "-Execute: " + command);
//                    return Execution.execute(this,command);
//                }
//                public String getName()
//                {
//                    return m_name;
//                }

                out.println(result);
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println(
                        (char) 27 + "[31;1mMiddleware exception: " + (char) 27 + "[0m" + e.getLocalizedMessage());
            }
        }
    }
}