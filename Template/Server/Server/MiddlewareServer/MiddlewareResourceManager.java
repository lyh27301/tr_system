package Server.MiddlewareServer;

import Server.CarServer.CarResourceManager;
import Server.Common.RMHashMap;
import Server.Common.RMItem;
import Server.Common.ResourceManager;
import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.util.Vector;

public class MiddlewareResourceManager extends ResourceManager implements IMiddlewareResourceManager {

    private IResourceManager carResourceManager = null;
    private IResourceManager flightResourceManager = null;
    private IResourceManager roomResourceManager = null;

    public MiddlewareResourceManager()
    {
        super("MiddlewareResourceManager");
    }

    public void setCarResourceManager(IResourceManager carResourceManager) {
        this.carResourceManager = carResourceManager;
    }

    public void setFlightResourceManager(IResourceManager flightResourceManager) {
        this.flightResourceManager = flightResourceManager;
    }

    public void setRoomResourceManager(IResourceManager roomResourceManager) {
        this.roomResourceManager = roomResourceManager;
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






    //unimplemented methods



    @Override
    public boolean addRooms(int id, String location, int numRooms, int price) throws RemoteException {
        return false;
    }

    @Override
    public int newCustomer(int id) throws RemoteException {
        return 0;
    }

    @Override
    public boolean newCustomer(int id, int cid) throws RemoteException {
        return false;
    }



    @Override
    public boolean deleteRooms(int id, String location) throws RemoteException {
        return false;
    }

    @Override
    public boolean deleteCustomer(int id, int customerID) throws RemoteException {
        return false;
    }



    @Override
    public int queryRooms(int id, String location) throws RemoteException {
        return 0;
    }

    @Override
    public String queryCustomerInfo(int id, int customerID) throws RemoteException {
        return null;
    }




    @Override
    public int queryRoomsPrice(int id, String location) throws RemoteException {
        return 0;
    }



    @Override
    public boolean reserveRoom(int id, int customerID, String location) throws RemoteException {
        return false;
    }

    @Override
    public boolean bundle(int id, int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException {
        return false;
    }

    @Override
    public String getName() throws RemoteException {
        return null;
    }
}
