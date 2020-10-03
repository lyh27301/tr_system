package Server.ResourceManagerServer;

import Server.Common.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class ResourceManagerTCPServer extends ResourceManager {
    private static ResourceManagerTCPServer manager = null;

    private ServerSocket serverSocket;

    private static int port = 6116;

    public enum TYPE {
        BOOL, INT, STR
    }

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

            manager = new ResourceManagerTCPServer(name);

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

    public ResourceManagerTCPServer(String p_name) {
        super(p_name);
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

                // String result = manager.execute(parsedCommand);
                String result = execute(parsedCommand);

                out.println(result);
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println(
                        (char) 27 + "[31;1mResource Manager exception: " + (char) 27 + "[0m" + e.getLocalizedMessage());
            }
        }

        public String execute(Vector<String> command) {

            TYPE type = TYPE.STR;

            try {
                switch (command.get(0).toLowerCase()) {
                    case "addflight": {
                        type = TYPE.BOOL;
                        int xid = Integer.parseInt(command.get(1));
                        int flightNumber = Integer.parseInt(command.get(2));
                        int num = Integer.parseInt(command.get(3));
                        int price = Integer.parseInt(command.get(4));
                        return Boolean.toString(manager.addFlight(xid, flightNumber, num, price));
                    }
                    case "addcars": {
                        type = TYPE.BOOL;
                        int xid = Integer.parseInt(command.get(1));
                        String location = command.get(2);
                        int num = Integer.parseInt(command.get(3));
                        int price = Integer.parseInt(command.get(4));
                        return Boolean.toString(manager.addCars(xid, location, num, price));
                    }

                    case "addrooms": {
                        type = TYPE.BOOL;
                        int xid = Integer.parseInt(command.get(1));
                        String location = command.get(2);
                        int num = Integer.parseInt(command.get(3));
                        int price = Integer.parseInt(command.get(4));
                        return Boolean.toString(manager.addRooms(xid, location, num, price));
                    }
                    case "addcustomer": {
                        type = TYPE.INT;
                        int xid = Integer.parseInt(command.get(1));
                        return Integer.toString(manager.newCustomer(xid));
                    }
                    case "addcustomerid": {
                        type = TYPE.BOOL;
                        int xid = Integer.parseInt(command.get(1));
                        int id = Integer.parseInt(command.get(2));
                        return Boolean.toString(manager.newCustomer(xid, id));
                    }
                    case "deleteflight": {
                        type = TYPE.BOOL;
                        int xid = Integer.parseInt(command.get(1));
                        int flightNum = Integer.parseInt(command.get(2));
                        return Boolean.toString(manager.deleteFlight(xid, flightNum));
                    }
                    case "deletecars": {
                        type = TYPE.BOOL;
                        int xid = Integer.parseInt(command.get(1));
                        String location = command.get(2);
                        return Boolean.toString(manager.deleteCars(xid, location));
                    }
                    case "deleterooms": {
                        type = TYPE.BOOL;
                        int xid = Integer.parseInt(command.get(1));
                        String location = command.get(2);
                        return Boolean.toString(manager.deleteRooms(xid, location));
                    }
                    case "deletecustomer": {
                        type = TYPE.BOOL;
                        int xid = Integer.parseInt(command.get(1));
                        int customerID = Integer.parseInt(command.get(2));
                        return Boolean.toString(manager.deleteCustomer(xid, customerID));
                    }
                    case "queryflight": {
                        type = TYPE.INT;
                        int xid = Integer.parseInt(command.get(1));
                        int flightNum = Integer.parseInt(command.get(2));
                        return Integer.toString(manager.queryFlight(xid, flightNum));
                    }
                    case "querycars": {
                        type = TYPE.INT;
                        int xid = Integer.parseInt(command.get(1));
                        String location = command.get(2);
                        return Integer.toString(manager.queryCars(xid, location));
                    }
                    case "queryrooms": {
                        type = TYPE.INT;
                        int xid = Integer.parseInt(command.get(1));
                        String location = command.get(2);
                        return Integer.toString(manager.queryRooms(xid, location));
                    }
                    case "querycustomer": {
                        type = TYPE.STR;
                        int xid = Integer.parseInt(command.get(1));
                        int customerID = Integer.parseInt(command.get(2));
                        return manager.queryCustomerInfo(xid, customerID);
                    }
                    case "queryflightprice": {
                        type = TYPE.INT;
                        int xid = Integer.parseInt(command.get(1));
                        int flightNum = Integer.parseInt(command.get(2));
                        return Integer.toString(manager.queryFlightPrice(xid, flightNum));
                    }
                    case "querycarsprice": {
                        type = TYPE.INT;
                        int xid = Integer.parseInt(command.get(1));
                        String location = command.get(2);
                        return Integer.toString(manager.queryCarsPrice(xid, location));
                    }
                    case "queryroomsprice": {
                        type = TYPE.INT;
                        int xid = Integer.parseInt(command.get(1));
                        String location = command.get(2);
                        return Integer.toString(manager.queryRoomsPrice(xid, location));
                    }
                    case "reserveflight": {
                        type = TYPE.BOOL;
                        int xid = Integer.parseInt(command.get(1));
                        int customerID = Integer.parseInt(command.get(2));
                        int flightNum = Integer.parseInt(command.get(3));
                        return Boolean.toString(manager.reserveFlight(xid, customerID, flightNum));
                    }
                    case "reservecar": {
                        type = TYPE.BOOL;
                        int xid = Integer.parseInt(command.get(1));
                        int customerID = Integer.parseInt(command.get(2));
                        String location = command.get(3);
                        return Boolean.toString(manager.reserveCar(xid, customerID, location));
                    }
                    case "reserveroom": {
                        type = TYPE.BOOL;
                        int xid = Integer.parseInt(command.get(1));
                        int customerID = Integer.parseInt(command.get(2));
                        String location = command.get(3);
                        return Boolean.toString(manager.reserveRoom(xid, customerID, location));
                    }
                    case "bundle": {
                        type = TYPE.BOOL;
                        int xid = Integer.parseInt(command.get(1));
                        int customerID = Integer.parseInt(command.get(2));

                        Vector<String> flightNumbers = new Vector<String>();
                        for (int i = 0; i < command.size() - 6; ++i) {
                            flightNumbers.add(command.elementAt(3 + i));
                        }

                        // Location
                        String location = command.get(command.size() - 3);
                        boolean car = command.get(command.size() - 2) == "1" ? true : false;
                        boolean room = command.get(command.size() - 1) == "1" ? true : false;

                        return Boolean.toString(manager.bundle(xid, customerID, flightNumbers, location, car, room));
                    }
                }
            } catch (Exception e) {
                System.err.println(
                        (char) 27 + "[31;1mExecution exception: " + (char) 27 + "[0m" + e.getLocalizedMessage());
            }

            switch (type) {
                case BOOL: {
                    return "false";
                }
                case INT: {
                    return "-1";
                }
                case STR: {
                    return "";
                }
                default: {
                    return "";
                }
            }
        }
    }

}