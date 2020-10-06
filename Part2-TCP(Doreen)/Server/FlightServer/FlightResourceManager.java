package Server.FlightServer;

import Server.Common.BasicResourceManager;
import Server.Common.Flight;
import Server.Common.Trace;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;


public class FlightResourceManager extends BasicResourceManager {

    public FlightResourceManager(Socket clientSocket, DataInputStream inputStream, DataOutputStream outputStream)
    {
        super("FlightResourceManager", clientSocket, inputStream, outputStream);
    }

    @Override
    public String executeRequest(String[] parsed) {
        String response = "";
        if (parsed[0].equals("AddFlight")){
            if (addFlight(stringToInt(parsed[1]), stringToInt(parsed[2]), stringToInt(parsed[3]), stringToInt(parsed[4]))) {
                response = "Flight added";
            }else{
                response = "Flight could not be added";
            }

        }else if (parsed[0].equals("DeleteFlight")){
            if (deleteFlight(stringToInt(parsed[1]), stringToInt(parsed[2]))){
                response = "Flight Deleted";
            }else{
                response = "Flight could not be deleted";
            }

        }else if(parsed[0].equals("QueryFlight")){
            int numFlights = queryFlight(stringToInt(parsed[1]), stringToInt(parsed[2]));
            response = "Number of flights at this location: " + numFlights;

        }else if(parsed[0].equals("QueryFlightPrice")){
            int price = queryFlightPrice(stringToInt(parsed[1]), stringToInt(parsed[2]));
            response = "Price of this flight: " + price;

        }else if(parsed[0].equals("ReserveFlight")){
            if (reserveFlight(stringToInt(parsed[1]), stringToInt(parsed[2]), stringToInt((parsed[3])))) {
                response = String.valueOf(queryFlightPrice(Integer.valueOf(parsed[1]),Integer.valueOf(parsed[3])));
            } else {
                response = "false";
            }
        }
        return response;
    }

    // Create a new flight, or add seats to existing flight
    // NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice)
    {
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
    public boolean deleteFlight(int xid, int flightNum)
    {
        return deleteItem(xid, Flight.getKey(flightNum));
    }

    // Returns the number of empty seats in this flight
    public int queryFlight(int xid, int flightNum)
    {
        return queryNum(xid, Flight.getKey(flightNum));
    }

    // Returns price of a seat in this flight
    public int queryFlightPrice(int xid, int flightNum)
    {
        return queryPrice(xid, Flight.getKey(flightNum));
    }

    // Adds flight reservation to this customer
    public boolean reserveFlight(int xid, int customerID, int flightNum)
    {
        return reserveItem(xid, customerID, Flight.getKey(flightNum), String.valueOf(flightNum));
    }


}
