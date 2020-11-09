package Server.RoomServer;

import Server.Common.BasicResourceManager;
import Server.Common.Room;
import Server.Common.Trace;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;

public class RoomResourceManager extends BasicResourceManager {

//    public RoomResourceManager(Socket clientSocket, ObjectInputStream inputStream, ObjectOutputStream outputStream)
//    {
//        super("RoomResourceManager", clientSocket, inputStream, outputStream);
//    }
    public RoomResourceManager()
    {
        super("RoomResourceManager");
    }

    @Override
    public String executeRequest(String[] parsed) {
        String response = "";
        if (parsed[0].equals("AddRooms")){
            if (addRooms(stringToInt(parsed[1]), parsed[2], stringToInt(parsed[3]), stringToInt(parsed[4]))) {
                response = "Rooms added";
            }else{
                response = "Rooms could not be added";
            }

        }else if (parsed[0].equals("DeleteRooms")){
            if (deleteRooms(stringToInt(parsed[1]), parsed[2])){
                response = "Rooms Deleted";
            }else{
                response = "Rooms could not be deleted";
            }

        }else if(parsed[0].equals("QueryRooms")){
            int numRooms = queryRooms(stringToInt(parsed[1]), parsed[2]);
            response = "Number of rooms at this location: " + numRooms;

        }else if(parsed[0].equals("QueryRoomsPrice")){
            int price = queryRoomsPrice(stringToInt(parsed[1]), parsed[2]);
            response = "Price of rooms at this location: " + price;

        }else if(parsed[0].equals("ReserveRoom")){
            if (reserveRoom(stringToInt(parsed[1]), stringToInt(parsed[2]), parsed[3])) {
                response = String.valueOf(queryRoomsPrice(Integer.valueOf(parsed[1]),parsed[3]));
            } else {
                response = "false";
            }
        }
        else if((parsed[0].equals("CancelRoom"))){
            response = String.valueOf(cancelRoom(Integer.valueOf(parsed[1]),Integer.valueOf(parsed[2]),parsed[3],Integer.valueOf(parsed[4])));
        }
        return response;
    }

    protected boolean cancelRoom(int xid, int customerID, String key, int count){
        cancelReservation(xid,customerID,key,count);
        return true;
    }
    // Create a new room location or add rooms to an existing location
    // NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public boolean addRooms(int xid, String location, int count, int price)
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
    public boolean deleteRooms(int xid, String location)
    {
        return deleteItem(xid, Room.getKey(location));
    }

    // Returns the amount of rooms available at a location
    public int queryRooms(int xid, String location)
    {
        return queryNum(xid, Room.getKey(location));
    }

    // Returns room price at this location
    public int queryRoomsPrice(int xid, String location)
    {
        return queryPrice(xid, Room.getKey(location));
    }

    // Adds room reservation to this customer
    public boolean reserveRoom(int xid, int customerID, String location)
    {
        return reserveItem(xid, customerID, Room.getKey(location), location);
    }


}
 
