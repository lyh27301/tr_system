package Server.ResourceManagerServer.FlightServer;

import Server.Common.BasicResourceManager;
import Server.Common.Flight;
import Server.Common.Trace;

import java.io.IOException;
import java.util.Vector;

public class FlightResourceManager extends BasicResourceManager {
    public FlightResourceManager(String p_name) {
        super(p_name);
    }
    public enum TYPE {
        BOOL, INT, STR
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
                    return Boolean.toString(addFlight(xid, flightNumber, num, price));
                }
                case "deleteflight": {
                    type = TYPE.BOOL;
                    int xid = Integer.parseInt(command.get(1));
                    int flightNum = Integer.parseInt(command.get(2));
                    return Boolean.toString(deleteFlight(xid, flightNum));
                }
                case "queryflight": {
                    type = TYPE.INT;
                    int xid = Integer.parseInt(command.get(1));
                    int flightNum = Integer.parseInt(command.get(2));
                    return Integer.toString(queryFlight(xid, flightNum));
                }
                case "queryflightprice": {
                    type = TYPE.INT;
                    int xid = Integer.parseInt(command.get(1));
                    int flightNum = Integer.parseInt(command.get(2));
                    return Integer.toString(queryFlightPrice(xid, flightNum));
                }
                case "reserveflight": {
                    type = TYPE.BOOL;
                    int xid = Integer.parseInt(command.get(1));
                    int customerID = Integer.parseInt(command.get(2));
                    int flightNum = Integer.parseInt(command.get(3));
                    return Boolean.toString(reserveFlight(xid, customerID, flightNum));
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
            default: {
                return "";
            }
        }
    }
    // Create a new flight, or add seats to existing flight
    // NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice) throws IOException {
        Trace.info("RM::addFlight(" + xid + ", " + flightNum + ", " + flightSeats + ", $" + flightPrice + ") called");
        Flight curObj = (Flight)readData(xid, Flight.getKey(flightNum));
        if (curObj == null)
        {
            // Doesn't exist yet, add it
            Flight newObj = new Flight(flightNum, flightSeats, flightPrice);
            writeData(xid, newObj.getKey(), newObj);
            Trace.info("RM::addFlight(" + xid + ") created new flight " + flightNum + ", seats=" + flightSeats + ", price=$" + flightPrice);
        }
        else
        {
            // Add seats to existing flight and update the price if greater than zero
            curObj.setCount(curObj.getCount() + flightSeats);
            if (flightPrice > 0)
            {
                curObj.setPrice(flightPrice);
            }
            writeData(xid, curObj.getKey(), curObj);
            Trace.info("RM::addFlight(" + xid + ") modified existing flight " + flightNum + ", seats=" + curObj.getCount() + ", price=$" + flightPrice);
        }
        return true;
    }
    // Deletes flight
    public boolean deleteFlight(int xid, int flightNum) throws IOException {
        return deleteItem(xid, Flight.getKey(flightNum));
    }
    // Returns the number of empty seats in this flight
    public int queryFlight(int xid, int flightNum) throws IOException {
        return queryNum(xid, Flight.getKey(flightNum));
    }
    // Returns price of a seat in this flight
    public int queryFlightPrice(int xid, int flightNum) throws IOException {
        return queryPrice(xid, Flight.getKey(flightNum));
    }
    // Adds flight reservation to this customer
    public boolean reserveFlight(int xid, int customerID, int flightNum) throws IOException {
        return reserveItem(xid, customerID, Flight.getKey(flightNum), String.valueOf(flightNum));
    }
}