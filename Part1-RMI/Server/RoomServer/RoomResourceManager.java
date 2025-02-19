package Server.RoomServer;

import Server.Common.BasicResourceManager;
import Server.Common.Car;
import Server.Common.Room;
import Server.Common.Trace;
import Server.Interface.IRoomManager;

import java.rmi.RemoteException;

public class RoomResourceManager extends BasicResourceManager implements IRoomManager {

    public RoomResourceManager(){
        super ("RoomResourceManager");
    }

    // Create a new room location or add rooms to an existing location
    // NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public boolean addRooms(int xid, String location, int count, int price) throws RemoteException
    {
        Trace.info("RM::addRooms(" + xid + ", " + location + ", " + count + ", $" + price + ") called");
        Room curObj = (Room)readData(xid, Room.getKey(location));
        if (curObj == null)
        {
            // Room location doesn't exist yet, add it
            Room newObj = new Room(location, count, price);
            writeData(xid, newObj.getKey(), newObj);
            Trace.info("RM::addRooms(" + xid + ") created new room location " + location + ", count=" + count + ", price=$" + price);
        } else {
            // Add count to existing object and update price if greater than zero
            curObj.setCount(curObj.getCount() + count);
            if (price > 0)
            {
                curObj.setPrice(price);
            }
            writeData(xid, curObj.getKey(), curObj);
            Trace.info("RM::addRooms(" + xid + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price);
        }
        return true;
    }

    // Delete rooms at a location
    public boolean deleteRooms(int xid, String location) throws RemoteException
    {
        return deleteItem(xid, Room.getKey(location));
    }

    // Returns the amount of rooms available at a location
    public int queryRooms(int xid, String location) throws RemoteException
    {
        return queryNum(xid, Room.getKey(location));
    }

    // Returns room price at this location
    public int queryRoomsPrice(int xid, String location) throws RemoteException
    {
        return queryPrice(xid, Room.getKey(location));
    }

    // Adds room reservation to this customer
    public boolean reserveRoom(int xid, int customerID, String location) throws RemoteException
    {
        return reserveItem(xid, customerID, Room.getKey(location), location);
    }

    public boolean cancelRoom(int xid, int customerID, String key, int count){
        cancelReservation(xid,customerID,key,count);
        return true;
    }


}
