
public class MiddlewareTCPServer extends MiddlewareResourceManager{

    private static TCPMiddleware middleware = null;

    private static int serverPort = 6116;
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

            middleware = new MiddlewareTCPServer("Middleware",flightHost, flightPort, carHost, carPort, roomHost, roomPort, customerHost, customerPort));

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    middleware.stop();
                }
            });
            System.out.println("Starting 'Middleware:" + s_serverPort + "'");
            middleware.start(s_serverPort);
        } catch(Exception e) {
            System.err.println((char)27 + "[31;1mMiddleware exception: " + (char)27 + e.toString());
            System.exit(1);
        }
    }

    public TCPMiddleware(String p_name, String flightIP, int flightPort, String carIP, int carPort, String roomIP, int roomPort)
    {
        super(p_name,flightIP,flightPort,carIP,carPort,roomIP,roomPort);
    }

    private void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Listening on port: " + port);
            while (true)
                new ClientHandler(serverSocket.accept()).start();
        } catch(IOException e) {
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
        }
        catch(IOException e) {
            System.err.println((char)27 + "[31;1mMiddleware exception: " + (char)27 + e.toString());
        }
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

                Vector<String> parsedCommand = Parser.parse(inputLine);

                if (parsedCommand == null) {
                    out.println("");
                    in.close();
                    out.close();
                    clientSocket.close();
                    return;
                }

                String result = middleware.execute(parsedCommand);

                out.println(result);
                in.close();
                out.close();
                clientSocket.close();
            } catch(IOException e) {
                System.err.println((char)27 + "[31;1mMiddleware exception: " + (char)27 + "[0m" + e.getLocalizedMessage());
            }
        }
    }
}