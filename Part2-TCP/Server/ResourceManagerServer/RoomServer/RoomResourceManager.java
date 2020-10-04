package Server.ResourceManagerServer.RoomServer;

import Server.Common.BasicResourceManager;
import Server.Common.Room;
import Server.Common.Trace;

import java.io.IOException;
import java.util.Vector;

public class RoomResourceManager extends BasicResourceManager {
    public RoomResourceManager(String p_name) {
        super(p_name);
    }
    public enum TYPE {
        BOOL, INT, STR
    }
    public String execute(Vector<String> command) {

        TYPE type = TYPE.STR;

        try {
            switch (command.get(0).toLowerCase()) {


                case "addrooms": {
                    type = TYPE.BOOL;
                    int xid = Integer.parseInt(command.get(1));
                    String location = command.get(2);
                    int num = Integer.parseInt(command.get(3));
                    int price = Integer.parseInt(command.get(4));
                    return Boolean.toString(addRooms(xid, location, num, price));
                }

                case "deleterooms": {
                    type = TYPE.BOOL;
                    int xid = Integer.parseInt(command.get(1));
                    String location = command.get(2);
                    return Boolean.toString(deleteRooms(xid, location));
                }

                case "queryrooms": {
                    type = TYPE.INT;
                    int xid = Integer.parseInt(command.get(1));
                    String location = command.get(2);
                    return Integer.toString(queryRooms(xid, location));
                }

                case "queryroomsprice": {
                    type = TYPE.INT;
                    int xid = Integer.parseInt(command.get(1));
                    String location = command.get(2);
                    return Integer.toString(queryRoomsPrice(xid, location));
                }

                case "reserveroom": {
                    type = TYPE.BOOL;
                    int xid = Integer.parseInt(command.get(1));
                    int customerID = Integer.parseInt(command.get(2));
                    String location = command.get(3);
                    return Boolean.toString(reserveRoom(xid, customerID, location));
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
    // Create a new room location or add rooms to an existing location
    // NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public boolean addRooms(int xid, String location, int count, int price) throws IOException {
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
    public boolean deleteRooms(int xid, String location) throws IOException {
        return deleteItem(xid, Room.getKey(location));
    }

    // Returns the amount of rooms available at a location
    public int queryRooms(int xid, String location) throws IOException {
        return queryNum(xid, Room.getKey(location));
    }
    // Returns room price at this location
    public int queryRoomsPrice(int xid, String location) throws IOException {
        return queryPrice(xid, Room.getKey(location));
    }
    // Adds room reservation to this customer
    public boolean reserveRoom(int xid, int customerID, String location) throws IOException {
        return reserveItem(xid, customerID, Room.getKey(location), location);
    }

}