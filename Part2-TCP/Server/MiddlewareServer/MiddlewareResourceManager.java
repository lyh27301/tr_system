package Server.MiddlewareServer;
import Server.Common.*;

import java.io.IOException;
import java.util.Vector;

public class MiddlewareResourceManager extends ResourceManager{
    protected ResourceManagerTCPClient flightTCPClient = null;
    protected ResourceManagerTCPClient carTCPClient = null;
    protected ResourceManagerTCPClient customerTCPClient = null;
    protected ResourceManagerTCPClient roomTCPClient = null;

    public enum TYPE {
		BOOL, INT, STR
	}

    public MiddlewareResourceManager(String name, String flightHost, int flightPort, String carHost, int carPort, String roomHost, int roomPort, String customerHost, int customerPort)
    {
        super(name);
        flightTCPClient = new ResourceManagerTCPClient(flightHost,flightPort);
        carTCPClient = new ResourceManagerTCPClient(carHost,carPort);
        roomTCPClient = new ResourceManagerTCPClient(roomHost,roomPort);
        customerTCPClient = new ResourceManagerTCPClient(customerHost, customerPort);

    }
    public void close() {
        flightTCPClient.stopTCPClient();
        carTCPClient.stopTCPClient();
        roomTCPClient.stopTCPClient();
        customerTCPClient.stopTCPClient();
    }
    //TODO: distribute commands
    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice){
        Trace.info("addFlight - Redirect to Flight Resource Manager");
        String command = String.format("AddFlight,%d,%d,%d,%d",id,flightNum,flightSeats,flightPrice);
        return toBool(send(flightTCPClient,TYPE.BOOL,command,true));
    }

    public boolean addCars(int id, String location, int numCars, int price)
    {
        Trace.info("addCars - Redirect to Car Resource Manager");
        String command = String.format("AddCars,%d,%s,%d,%d",id,location,numCars,price);
        return toBool(send(carTCPClient,TYPE.BOOL,command,true));
    }

    public boolean addRooms(int id, String location, int numRooms, int price)
    {
        Trace.info("addRooms - Redirect to Room Resource Manager");
        String command = String.format("AddRooms,%d,%s,%d,%d",id,location,numRooms,price);
        return toBool(send(roomTCPClient,TYPE.BOOL,command,true));
    }

    public int newCustomer(int xid){
        Trace.info("addCustomer - Redirect to Customer Resource Manager");
        String command = String.format("AddCustomer,%d",xid);
        return toInt(send(customerTCPClient,TYPE.INT,command,true));
    }
    public boolean newCustomer(int xid, int customerID){
        Trace.info("addCustomer - Redirect to Customer Resource Manager");
        String command = String.format("AddCustomerID,%d,%d",xid, customerID);
        return toBool(send(customerTCPClient,TYPE.BOOL,command,true));
    }

    public boolean deleteFlight(int id, int flightNum)
    {
        Trace.info("deleteFlight - Redirect to Flight Resource Manager");
        String command = String.format("DeleteFlight,%d,%d",id,flightNum);
        return toBool(send(flightTCPClient,TYPE.BOOL,command,true));
    }

    public boolean deleteCars(int id, String location)
    {
        Trace.info("deleteCars - Redirect to Car Resource Manager");
        String command = String.format("DeleteCars,%d,%s",id,location);
        return toBool(send(carTCPClient,TYPE.BOOL,command,true));
    }

    public boolean deleteRooms(int id, String location)
    {
        Trace.info("deleteRooms - Redirect to Room Resource Manager");
        String command = String.format("DeleteRooms,%d,%s",id,location);
        return toBool(send(roomTCPClient,TYPE.BOOL,command,true));
    }
    public boolean deleteCustomer(int xid, int customerID)
    {
        Trace.info("deleteCustomer - Redirect to Customer Resource Manager");
        String command = String.format("DeleteCustomer,%d,%d",xid,customerID);
        return toBool(send(customerTCPClient,TYPE.BOOL,command,true));
    }
//
//    public boolean deleteCustomer(int xid, int customerID)
//    {
//        Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") called");
//        Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
//        if (customer == null)
//        {
//            Trace.warn("RM::deleteCustomer(" + xid + ", " + customerID + ") failed--customer doesn't exist");
//            return false;
//        }
//        else
//        {
//            synchronized (customer) {
//                // Increase the reserved numbers of all reservable items which the customer reserved.
//                RMHashMap reservations = customer.getReservations();
//                for (String reservedKey : reservations.keySet()) {
//                    String type = reservedKey.split("-")[0];
//                    ReservedItem reserveditem = customer.getReservedItem(reservedKey);
//                    String command = String.format("RemoveReservation,%d,%d,%s,%d", xid, customerID, reserveditem.getKey(), reserveditem.getCount());
//
//                    if (type.equals("flight")) {
//                        synchronized (flightTCPClient) {
//                            try {
//                                flightTCPClient.sendMessage(command);
//                            } catch (Exception e) {
//                            }
//                        }
//                    } else if (type.equals("car")) {
//                        synchronized (carTCPClient) {
//                            try {
//                                carTCPClient.sendMessage(command);
//                            } catch (Exception e) {
//                            }
//                        }
//                    } else if (type.equals("room")) {
//                        synchronized (roomTCPClient) {
//                            try {
//                                roomTCPClient.sendMessage(command);
//                            } catch (Exception e) {
//                            }
//                        }
//                    } else
//                        Trace.error("RM::deleteCustomer(" + xid + ", " + customerID + ") failed--reservedKey (" + reservedKey + ") wasn't of expected type.");
//                }
//                // Remove the customer from the storage
//                removeData(xid, customer.getKey());
//                Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") succeeded");
//            }
//            return true;
//        }
//
//    }

    public int queryFlight(int id, int flightNumber)
    {
        Trace.info("queryFlight - Redirect to Flight Resource Manager");
        String command = String.format("QueryFlight,%d,%d",id,flightNumber);
        return toInt(send(flightTCPClient,TYPE.INT,command,false));
    }

    public int queryCars(int id, String location)
    {
        Trace.info("queryCars - Redirect to Car Resource Manager");
        String command = String.format("QueryCars,%d,%s",id,location);
        return toInt(send(carTCPClient,TYPE.INT,command,false));
    }

    public int queryRooms(int id, String location)
    {
        Trace.info("queryRooms - Redirect to Room Resource Manager");
        String command = String.format("QueryRooms,%d,%s",id,location);
        return toInt(send(roomTCPClient,TYPE.INT,command,false));
    }
    public String queryCustomerInfo(int xid, int customerID)
    {
        Trace.info("queryCustomer - Redirect to Customer Resource Manager");
        String command = String.format("QueryRooms,%d,%d",xid,customerID);
        return send(customerTCPClient,TYPE.STR,command,false);
    }

    public int queryFlightPrice(int id, int flightNumber)
    {
        Trace.info("queryFlightPrice - Redirect to Flight Resource Manager");
        String command = String.format("QueryFlightPrice,%d,%d",id,flightNumber);
        return toInt(send(flightTCPClient,TYPE.INT,command,false));
    }

    public int queryCarsPrice(int id, String location)
    {
        Trace.info("queryCarsPrice - Redirect to Car Resource Manager");
        String command = String.format("QueryCarsPrice,%d,%s",id,location);
        return toInt(send(carTCPClient,TYPE.INT,command,false));
    }

    public int queryRoomsPrice(int id, String location)
    {
        Trace.info("queryRoomsPrice - Redirect to Room Resource Manager");
        String command = String.format("QueryRoomsPrice,%d,%s",id,location);
        return toInt(send(roomTCPClient,TYPE.INT,command,false));
    }
    public boolean reserveFlight(int xid, int customerID, int flightNumber){
        Trace.info("reserveFlight - Redirect to Flight Resource Manager");
        String command = String.format("ReserveFlight,%d,%d,%d", xid, customerID, flightNumber);
        return toBool(send(flightTCPClient,TYPE.BOOL,command,true));
    }
//    public boolean reserveFlight(int xid, int customerID, int flightNumber)
//    {
//        String key = Flight.getKey(flightNumber);
//
//        Trace.info("RM::reserveFlight(" + xid + ", customer=" + customerID + ", " + key + ") called" );
//        // Check customer exists
//        Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
//        if (customer == null)
//        {
//            Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + flightNumber + ")  failed--customer doesn't exist");
//            return false;
//        }
//        synchronized (customer) {
//            synchronized (flightTCPClient) {
//                int price = -1;
//                try {
//                    price = toInt(flightTCPClient.sendMessage(String.format("ItemsAvailable,%d,%s,%d", xid, key, 1)));
//                } catch (Exception e) {
//                }
//
//
//                if (price < 0) {
//                    Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + flightNumber + ")  failed--item unavailable");
//                    return false;
//                }
//
//                boolean reserved = false;
//
//                try {
//                    String c = String.format("ReserveFlight,%d,%d,%d", xid, customerID, flightNumber);
//                    reserved = toBool(flightTCPClient.sendMessage(c));
//                } catch (Exception e) {
//                }
//
//                if (reserved) {
//                    customer.reserve(key, String.valueOf(flightNumber), price);
//                    writeData(xid, customer.getKey(), customer);
//                    return true;
//                }
//                Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + flightNumber + ")  failed--Could not reserve item");
//                return false;
//            }
//        }
//    }
    public boolean reserveCar(int xid, int customerID, String location){
        Trace.info("reserveCar - Redirect to Car Resource Manager");
        String command = String.format("ReserveCar,%d,%d,%s", xid, customerID, location);
        return toBool(send(carTCPClient,TYPE.BOOL,command,true));
    //toBool(carTCPClient.sendMessage(String.format("ReserveCar,%d,%d,%s", xid, customerID, location)))
    }
//    public boolean reserveCar(int xid, int customerID, String location)
//    {
//        String key = Car.getKey(location);
//
//        Trace.info("RM::reserveCar(" + xid + ", customer=" + customerID + ", " + key + ") called" );
//        // Check customer exists
//        Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
//        if (customer == null)
//        {
//            Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + location + ")  failed--customer doesn't exist");
//            return false;
//        }
//        synchronized (customer) {
//            synchronized (carTCPClient) {
//                int price = -1;
//                try {
//                    price = toInt(carTCPClient.sendMessage(String.format("ItemsAvailable,%d,%s,%d", xid, key, 1)));
//                } catch (Exception e) {
//                }
//
//                if (price < 0) {
//                    Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + location + ")  failed--item unavailable");
//                    return false;
//                }
//
//                boolean reserved = false;
//
//                try {
//                    reserved = toBool(carTCPClient.sendMessage(String.format("ReserveCar,%d,%d,%s", xid, customerID, location)));
//                } catch (Exception e) {
//                }
//
//                if (reserved) {
//                    customer.reserve(key, location, price);
//                    writeData(xid, customer.getKey(), customer);
//                    return true;
//                }
//                Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + location + ")  failed--Could not reserve item");
//                return false;
//            }
//        }
//    }
    public boolean reserveRoom(int xid, int customerID, String location){
        Trace.info("reserveRoom - Redirect to room Resource Manager");
        String command = String.format("ReserveRoom,%d,%d,%s", xid, customerID, location);
        return toBool(send(roomTCPClient,TYPE.BOOL,command,true));
        //toBool(carTCPClient.sendMessage(String.format("ReserveCar,%d,%d,%s", xid, customerID, location)))
    }
//    public boolean reserveRoom(int xid, int customerID, String location)
//    {
//        String key = Room.getKey(location);
//
//        Trace.info("RM::reserveRoom(" + xid + ", customer=" + customerID + ", " + key + ") called" );
//        // Check customer exists
//        Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
//        if (customer == null)
//        {
//            Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + location + ")  failed--customer doesn't exist");
//            return false;
//        }
//        synchronized (customer) {
//            synchronized (roomTCPClient) {
//                int price = -1;
//                try {
//                    price = toInt(roomTCPClient.sendMessage(String.format("ItemsAvailable,%d,%s,%d", xid, key, 1)));
//                } catch (Exception e) {
//                }
//
//                if (price < 0) {
//                    Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + location + ")  failed--item unavailable");
//                    return false;
//                }
//
//                boolean reserved = false;
//
//                try {
//                    reserved = toBool(roomTCPClient.sendMessage(String.format("ReserveRoom,%d,%d,%s", xid, customerID, location)));
//                } catch (Exception e) {
//                }
//
//                if (reserved) {
//                    customer.reserve(key, location, price);
//                    writeData(xid, customer.getKey(), customer);
//                    return true;
//                }
//                Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + location + ")  failed--Could not reserve item");
//                return false;
//            }
//        }
//    }
    public boolean bundle(int xid, int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room) {
        //check flight availability
        boolean success = true;
        for(String flight: flightNumbers){
            if(queryFlight(xid,toInt(flight))<0) {
                Trace.warn("RM::reserveFlight(" + xid + ", " + customerID + ", " + flight + ", " + location + ") failed--flight doesn't exist");
                success = false;
            }
            else if(queryFlight(xid,toInt(flight))==0){
                Trace.warn("RM::reserveFlight(" + xid + ", " + customerID + ", " + flight + ", " + location + ") failed--No more seats");
                success = false;
            }
        }
        if(car){
            if (queryCars(xid,location)<0){
                Trace.warn("RM::reserveCar(" + xid + ", " + customerID + ", " + location + ") failed--car doesn't exist");
                success = false;
            }
            else if(queryCars(xid,location)==0){
                Trace.warn("RM::reserveCar(" + xid + ", " + customerID + ", " + location + ") failed--No more cars");
                success = false;
            }
        }
        if (room){
            if (queryRooms(xid,location)<0){
                Trace.warn("RM::reserveRoom(" + xid + ", " + customerID + ", " + location + ") failed--room doesn't exist");
                success = false;
            }
            else if(queryRooms(xid,location)==0){
                Trace.warn("RM::reserveRoom(" + xid + ", " + customerID + ", " + location + ") failed--No more rooms");
                success = false;
            }
        }
        if (!success){
            return false;
        }
        //start reservation
        for(String flight: flightNumbers){
            reserveFlight(xid,customerID,toInt(flight));
        }
        reserveCar(xid,customerID,location);
        reserveRoom(xid,customerID,location);
        return true;
    }

    private String send(ResourceManagerTCPClient comm, TYPE returnType, String command, boolean sync) {
        String res;
        try {
            if (sync) {
                synchronized (comm) {
                    try {
                        res = comm.sendMessage(command);
                        if (res.equals(""))
                            throw new IOException();
                        return res;
                    } catch (IOException e) {
                        comm.connect();
                        return comm.sendMessage(command);
                    }
                }
            }
            else {
                try {
                    res = comm.sendMessage(command);
                    if (res.equals(""))
                        throw new IOException();
                    return res;
                } catch (IOException e) {
                    comm.connect();
                    return comm.sendMessage(command);
                }
            }

        } catch(Exception e) {
            Trace.error(e.toString());
            if (returnType == TYPE.BOOL)
                return "false";
            else if (returnType == TYPE.INT)
                return "-1";
            else
                return "";
        }
    }

    public String getName() {
        return m_name;
    }

    private boolean toBool(String s) {
        try {
            return Boolean.parseBoolean(s);
        } catch (Exception e) {
            System.out.println("toBool exception: " + e.getLocalizedMessage());
            return false;
        }
    }

    private int toInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            System.out.println("toInt exception: " + e.getLocalizedMessage());
            return -1;
        }
    }
}
