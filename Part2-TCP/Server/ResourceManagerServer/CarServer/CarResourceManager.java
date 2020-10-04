package Server.ResourceManagerServer.CarServer;

import Server.Common.BasicResourceManager;
import Server.Common.Car;
import Server.Common.Trace;

import java.io.IOException;
import java.util.Vector;

public class CarResourceManager extends BasicResourceManager {
    public CarResourceManager(String p_name)
    {
        super("CarResourceManager");
    }
    public enum TYPE {
        BOOL, INT, STR
    }
    public String execute(Vector<String> command) {

        TYPE type = TYPE.STR;

        try {
            switch (command.get(0).toLowerCase()) {

                case "addcars": {
                    type = TYPE.BOOL;
                    int xid = Integer.parseInt(command.get(1));
                    String location = command.get(2);
                    int num = Integer.parseInt(command.get(3));
                    int price = Integer.parseInt(command.get(4));
                    return Boolean.toString(addCars(xid, location, num, price));
                }


                case "deletecars": {
                    type = TYPE.BOOL;
                    int xid = Integer.parseInt(command.get(1));
                    String location = command.get(2);
                    return Boolean.toString(deleteCars(xid, location));
                }

                case "querycars": {
                    type = TYPE.INT;
                    int xid = Integer.parseInt(command.get(1));
                    String location = command.get(2);
                    return Integer.toString(queryCars(xid, location));
                }

                case "querycarsprice": {
                    type = TYPE.INT;
                    int xid = Integer.parseInt(command.get(1));
                    String location = command.get(2);
                    return Integer.toString(queryCarsPrice(xid, location));
                }

                case "reservecar": {
                    type = TYPE.BOOL;
                    int xid = Integer.parseInt(command.get(1));
                    int customerID = Integer.parseInt(command.get(2));
                    String location = command.get(3);
                    return Boolean.toString(reserveCar(xid, customerID, location));
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
    // Create a new car location or add cars to an existing location
    // NOTE: if price <= 0 and the location already exists, it maintains its current price
    public boolean addCars(int xid, String location, int count, int price) throws IOException {
        Trace.info("RM::addCars(" + xid + ", " + location + ", " + count + ", $" + price + ") called");
        Car curObj = (Car)readData(xid, Car.getKey(location));
        if (curObj == null)
        {
            // Car location doesn't exist yet, add it
            Car newObj = new Car(location, count, price);
            writeData(xid, newObj.getKey(), newObj);
            Trace.info("RM::addCars(" + xid + ") created new location " + location + ", count=" + count + ", price=$" + price);
        }
        else
        {
            // Add count to existing car location and update price if greater than zero
            curObj.setCount(curObj.getCount() + count);
            if (price > 0)
            {
                curObj.setPrice(price);
            }
            writeData(xid, curObj.getKey(), curObj);
            Trace.info("RM::addCars(" + xid + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price);
        }
        return true;
    }
    // Delete cars at a location
    public boolean deleteCars(int xid, String location) throws IOException {
        return deleteItem(xid, Car.getKey(location));
    }
    // Returns the number of cars available at a location
    public int queryCars(int xid, String location) throws IOException {
        return queryNum(xid, Car.getKey(location));
    }
    // Returns price of cars at this location
    public int queryCarsPrice(int xid, String location) throws IOException {
        return queryPrice(xid, Car.getKey(location));
    }
    // Adds car reservation to this customer
    public boolean reserveCar(int xid, int customerID, String location) throws IOException {
        return reserveItem(xid, customerID, Car.getKey(location), location);
    }
}
