package Server.FlightServer;

import Server.Common.Flight;
import Server.Common.BasicResourceManager;
import Server.Common.Trace;
import Server.Interface.IFlightManager;

import java.rmi.RemoteException;

public class FlightResourceManager extends BasicResourceManager implements IFlightManager {

    public FlightResourceManager()
    {
        super("FlightResourceManager");
    }

    // Create a new flight, or add seats to existing flight
    // NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice) throws RemoteException
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
    public boolean deleteFlight(int xid, int flightNum) throws RemoteException
    {
        return deleteItem(xid, Flight.getKey(flightNum));
    }

    // Returns the number of empty seats in this flight
    public int queryFlight(int xid, int flightNum) throws RemoteException
    {
        return queryNum(xid, Flight.getKey(flightNum));
    }

    // Returns price of a seat in this flight
    public int queryFlightPrice(int xid, int flightNum) throws RemoteException
    {
        return queryPrice(xid, Flight.getKey(flightNum));
    }

    // Adds flight reservation to this customer
    public boolean reserveFlight(int xid, int customerID, int flightNum) throws RemoteException
    {
        return reserveItem(xid, customerID, Flight.getKey(flightNum), String.valueOf(flightNum));
    }

    public boolean cancelFlight(int xid, int customerID, String key, int count) throws RemoteException{
        cancelReservation(xid,customerID,key,count);
        return true;
    }

}
