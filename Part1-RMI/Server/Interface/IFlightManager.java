package Server.Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IFlightManager extends Remote {

    /**
     * Add seats to a flight.
     *
     * In general this will be used to create a new
     * flight, but it should be possible to add seats to an existing flight.
     * Adding to an existing flight should overwrite the current price of the
     * available seats.
     *
     * @return Success
     */
    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
            throws RemoteException;

    /**
     * Delete the flight.
     *
     * deleteFlight implies whole deletion of the flight. If there is a
     * reservation on the flight, then the flight cannot be deleted
     *
     * @return Success
     */
    public boolean deleteFlight(int id, int flightNum)
            throws RemoteException;

    /**
     * Query the status of a flight.
     *
     * @return Number of empty seats
     */
    public int queryFlight(int id, int flightNumber)
            throws RemoteException;

    /**
     * Query the status of a flight.
     *
     * @return Price of a seat in this flight
     */
    public int queryFlightPrice(int id, int flightNumber)
            throws RemoteException;

    public boolean reserveFlight(int xid, int customerID, int flightNum) throws RemoteException;

    public boolean cancelFlight(int xid, int customerID, String key, int count) throws RemoteException;
}
