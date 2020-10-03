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

                String result = execute(parsedCommand);
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
        public String execute(Vector<String> command){
            try {
                switch (command.get(0).toLowerCase()) {
                    case "addflight": {
                        type = 'B';
                        int xid = Integer.parseInt(command.get(1));
                        int flightNumber = Integer.parseInt(command.get(2));
                        int num = Integer.parseInt(command.get(3));
                        int price = Integer.parseInt(command.get(4));
                        return Boolean.toString(middleware.addFlight(xid, flightNumber, num, price));
                    }
                    case "addcars": {
                        type = 'B';
                        int xid = Integer.parseInt(command.get(1));
                        String location = command.get(2);
                        int num = Integer.parseInt(command.get(3));
                        int price = Integer.parseInt(command.get(4));
                        return Boolean.toString(middleware.addCars(xid, location, num, price));
                    }
                    case "addrooms": {
                        type = 'B';
                        int xid = Integer.parseInt(command.get(1));
                        String location = command.get(2);
                        int num = Integer.parseInt(command.get(3));
                        int price = Integer.parseInt(command.get(4));
                        return Boolean.toString(middleware.addRooms(xid, location, num, price));
                    }
                    case "addcustomer": {
                        type = 'I';
                        int xid = Integer.parseInt(command.get(1));
                        return Integer.toString(middleware.newCustomer(xid));
                    }
                    case "addcustomerid": {
                        type = 'B';
                        int xid = Integer.parseInt(command.get(1));
                        int id = Integer.parseInt(command.get(2));
                        return Boolean.toString(middleware.newCustomer(xid, id));
                    }
                    case "deleteflight": {
                        type = 'B';
                        int xid = Integer.parseInt(command.get(1));
                        int flightNum = Integer.parseInt(command.get(2));
                        return Boolean.toString(middleware.deleteFlight(xid, flightNum));
                    }
                    case "deletecars": {
                        type = 'B';
                        int xid = Integer.parseInt(command.get(1));
                        String location = command.get(2);
                        return Boolean.toString(middleware.deleteCars(xid, location));
                    }
                    case "deleterooms": {
                        type = 'B';
                        int xid = Integer.parseInt(command.get(1));
                        String location = command.get(2);
                        return Boolean.toString(middleware.deleteRooms(xid, location));
                    }
                    case "deletecustomer": {
                        type = 'B';
                        int xid = Integer.parseInt(command.get(1));
                        int customerID = Integer.parseInt(command.get(2));
                        return Boolean.toString(middleware.deleteCustomer(xid, customerID));
                    }
                    case "queryflight": {
                        type = 'I';
                        int xid = Integer.parseInt(command.get(1));
                        int flightNum = Integer.parseInt(command.get(2));
                        return Integer.toString(middleware.queryFlight(xid, flightNum));
                    }
                    case "querycars": {
                        type = 'I';
                        int xid = Integer.parseInt(command.get(1));
                        String location = command.get(2);
                        return Integer.toString(middleware.queryCars(xid, location));
                    }
                    case "queryrooms": {
                        type = 'I';
                        int xid = Integer.parseInt(command.get(1));
                        String location = command.get(2);
                        return Integer.toString(middleware.queryRooms(xid, location));
                    }
                    case "querycustomer": {
                        type = 'S';
                        int xid = Integer.parseInt(command.get(1));
                        int customerID = Integer.parseInt(command.get(2));
                        return manager.queryCustomerInfo(xid, customerID);
                    }
//                    case "summary": {
//                        type = 'S';
//                        int xid = Integer.parseInt(command.get(1));
//                        return manager.Summary(xid);
//                    }
//                    case "analytics": {
//                        type = 'S';
//                        int xid = Integer.parseInt(command.get(1));
//                        int upperBound = Integer.parseInt(command.get(2));
//                        return manager.Analytics(xid,upperBound);
//                    }
                    case "queryflightprice": {
                        type = 'I';
                        int xid = Integer.parseInt(command.get(1));
                        int flightNum = Integer.parseInt(command.get(2));
                        return Integer.toString(middleware.queryFlightPrice(xid, flightNum));
                    }
                    case "querycarsprice": {
                        type = 'I';
                        int xid = Integer.parseInt(command.get(1));
                        String location = command.get(2);
                        return Integer.toString(middleware.queryCarsPrice(xid, location));
                    }
                    case "queryroomsprice": {
                        type = 'I';
                        int xid = Integer.parseInt(command.get(1));
                        String location = command.get(2);
                        return Integer.toString(middleware.queryRoomsPrice(xid, location));
                    }
                    case "reserveflight": {
                        type = 'B';
                        int xid = Integer.parseInt(command.get(1));
                        int customerID = Integer.parseInt(command.get(2));
                        int flightNum = Integer.parseInt(command.get(3));
                        return Boolean.toString(middleware.reserveFlight(xid, customerID, flightNum));
                    }
                    case "reservecar": {
                        type = 'B';
                        int xid = Integer.parseInt(command.get(1));
                        int customerID = Integer.parseInt(command.get(2));
                        String location = command.get(3);
                        return Boolean.toString(middleware.reserveCar(xid, customerID, location));
                    }
                    case "reserveroom": {
                        type = 'B';
                        int xid = Integer.parseInt(command.get(1));
                        int customerID = Integer.parseInt(command.get(2));
                        String location = command.get(3);
                        return Boolean.toString(middleware.reserveRoom(xid, customerID, location));
                    }
                    case "bundle": {
                        type = 'B';
                        int xid = Integer.parseInt(command.get(1));
                        int customerID = Integer.parseInt(command.get(2));

                        Vector<String> flightNumbers = new Vector<String>();
                        for (int i = 0; i < command.size() - 6; ++i) {
                            flightNumbers.add(command.elementAt(3 + i));
                        }

                        // Location
                        String location = command.get(command.size() - 3);
                        boolean car = toBoolean(command.get(command.size() - 2));
                        boolean room = toBoolean(command.get(command.size() - 1));

                        return Boolean.toString(middleware.bundle(xid, customerID, flightNumbers, location, car, room));
                    }
//                    case "removereservation": {
//                        type = 'B';
//                        int xid = Integer.parseInt(command.get(1));
//                        int customerID = Integer.parseInt(command.get(2));
//                        String reserveditemKey = command.get(3);
//                        int reserveditemCount = Integer.parseInt(command.get(4));
//
//                        return Boolean.toString(middleware.removeReservation(xid, customerID, reserveditemKey, reserveditemCount));
//                    }
//                    case "itemsavailable": {
//                        type = 'I';
//                        int xid = Integer.parseInt(command.get(1));
//                        String key = command.get(2);
//                        int quantity = Integer.parseInt(command.get(3));
//
//                        return Integer.toString(middleware.itemsAvailable(xid, key, quantity));
//                    }
                }
            } catch(Exception e) {
                System.err.println((char)27 + "[31;1mExecution exception: " + (char)27 + "[0m" + e.getLocalizedMessage());
            }
            if (type == 'S')
                return defaultString;
            else if (type == 'B')
                return defaultBool;
            else
                return defaultInt;

        }

    }
}