package Server.MiddlewareServer;

import Server.Interface.ICarManager;
import Server.Common.RMHashMap;
import Server.Common.Trace;
import Server.Interface.ICustomerManager;
import Server.Interface.IFlightManager;
import Server.Interface.IResourceManager;
import Server.Interface.IRoomManager;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Vector;

public class MiddlewareResourceManager implements IResourceManager {

    private ICarManager carResourceManager = null;
    private IFlightManager flightResourceManager = null;
    private IRoomManager roomResourceManager = null;
    private ICustomerManager customerResourceManager = null;

    protected String m_name = "MiddlewareResourceManager";
    protected RMHashMap m_data = new RMHashMap();

    public MiddlewareResourceManager() { }

    public void setCarResourceManager(ICarManager carResourceManager) {
        this.carResourceManager = carResourceManager;
    }

    public void setFlightResourceManager(IFlightManager flightResourceManager) {
        this.flightResourceManager = flightResourceManager;
    }

    public void setRoomResourceManager(IRoomManager roomResourceManager) {
        this.roomResourceManager = roomResourceManager;
    }

    public void setCustomerResourceManager(ICustomerManager customerResourceManager){
        this.customerResourceManager = customerResourceManager;
    }


    @Override
    public boolean addCars(int id, String location, int numCars, int price) throws RemoteException {
        return carResourceManager.addCars (id, location, numCars, price);
    }

    @Override
    public boolean deleteCars(int id, String location) throws RemoteException {
        return carResourceManager.deleteCars(id, location);
    }

    @Override
    public int queryCars(int id, String location) throws RemoteException {
        return carResourceManager.queryCars(id, location);
    }

    @Override
    public int queryCarsPrice(int id, String location) throws RemoteException {
        return carResourceManager.queryCarsPrice(id, location);
    }

    @Override
    public boolean reserveCar(int id, int customerID, String location) throws RemoteException {
        return carResourceManager.reserveCar(id, customerID, location);
    }

    @Override
    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        return flightResourceManager.addFlight(id, flightNum, flightSeats, flightPrice);
    }

    @Override
    public boolean deleteFlight(int id, int flightNum) throws RemoteException {
        return flightResourceManager.deleteFlight(id, flightNum);
    }

    @Override
    public int queryFlight(int id, int flightNumber) throws RemoteException {
        return flightResourceManager.queryFlight(id, flightNumber);
    }

    @Override
    public int queryFlightPrice(int id, int flightNumber) throws RemoteException {
        return flightResourceManager.queryFlightPrice(id, flightNumber);
    }

    @Override
    public boolean reserveFlight(int id, int customerID, int flightNumber) throws RemoteException {
        return flightResourceManager.reserveFlight(id, customerID, flightNumber);
    }

    @Override
    public boolean addRooms(int id, String location, int numRooms, int price) throws RemoteException {
        return roomResourceManager.addRooms(id, location, numRooms, price);
    }

    @Override
    public boolean deleteRooms(int id, String location) throws RemoteException {
        return roomResourceManager.deleteRooms(id, location);
    }

    @Override
    public int queryRooms(int id, String location) throws RemoteException {
        return roomResourceManager.queryRooms(id, location);
    }

    @Override
    public int queryRoomsPrice(int id, String location) throws RemoteException {
        return roomResourceManager.queryRoomsPrice(id, location);
    }

    @Override
    public boolean reserveRoom(int id, int customerID, String location) throws RemoteException {
        return roomResourceManager.reserveRoom(id, customerID, location);
    }

    @Override
    public int newCustomer(int id) throws RemoteException {
        return customerResourceManager.newCustomer(id);
    }

    @Override
    public boolean newCustomer(int id, int cid) throws RemoteException {
        return customerResourceManager.newCustomer(id, cid);
    }

    @Override
    public boolean deleteCustomer(int id, int customerID) throws RemoteException {
        return customerResourceManager.deleteCustomer(id,customerID);
    }

    @Override
    public String queryCustomerInfo(int id, int customerID) throws RemoteException {
        return customerResourceManager.queryCustomerInfo(id, customerID);
    }

    @Override
    public boolean bundle(int id, int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException {
        ArrayList<String> reservedFlights = new ArrayList<String>();

        boolean flightsBooked = true;
        for (String fn: flightNumbers) {
            if (!flightResourceManager.reserveFlight(id, customerID,(Integer.valueOf(fn)). intValue())){
                flightsBooked = false;
                break;
            }
        }

        boolean carBooked = carResourceManager.reserveCar(id,customerID, location);
        boolean roomBooked = roomResourceManager.reserveRoom(id,customerID, location);

        // if any not successful, add the booked item back
        if (!carBooked || !roomBooked || !flightsBooked) {
            if (carBooked) {
                carResourceManager.addCars(id, location, 1, carResourceManager.queryCarsPrice(id, location));
            }
            if (roomBooked) {
                roomResourceManager.addRooms(id, location, 1,roomResourceManager.queryRoomsPrice(id, location));
            }
            for (String fn: reservedFlights) {
                int flightNumber = (Integer.valueOf(fn)). intValue();
                flightResourceManager.addFlight(id, flightNumber, 1, flightResourceManager.queryFlightPrice(id, flightNumber));
            }
            Trace.error("Fail to book bundle with Customer ID" + customerID);
            return false;
        }
        return true;
    }

    @Override
    public String getName() throws RemoteException
    {
        return m_name;
    }
}
