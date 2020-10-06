
package Server.CarServer;

import Server.Common.BasicResourceManager;
import Server.Common.Car;
import Server.Common.Trace;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;


public class CarResourceManager extends BasicResourceManager {

	public CarResourceManager(Socket clientSocket, DataInputStream inputStream, DataOutputStream outputStream)
	{
		super("CarResourceManager", clientSocket, inputStream, outputStream);
	}

	@Override
	public String executeRequest(String[] parsed) {
		String response = "";
		if (parsed[0].equals("AddCars")){
			if (addCars(stringToInt(parsed[1]), parsed[2], stringToInt(parsed[3]), stringToInt(parsed[4]))) {
				response = "Cars added";
			}else{
				response = "Cars could not be added";
			}

		}else if (parsed[0].equals("DeleteCars")){
			if (deleteCars(stringToInt(parsed[1]), parsed[2])){
				response = "Cars Deleted";
			}else{
				response = "Cars could not be deleted";
			}

		}else if(parsed[0].equals("QueryCars")){
			int numCars = queryCars(stringToInt(parsed[1]), parsed[2]);
			response = "Number of cars at this location: " + numCars;

		}else if(parsed[0].equals("QueryCarsPrice")){
			int price = queryCarsPrice(stringToInt(parsed[1]), parsed[2]);
			response = "Price of cars at this location: " + price;

		}else if(parsed[0].equals("ReserveCar")){
			if (reserveCar(stringToInt(parsed[1]), stringToInt(parsed[2]), parsed[3])) {
				response = String.valueOf(queryCarsPrice(Integer.valueOf(parsed[1]),parsed[3]));
			} else {
				response = "false";
			}
		}
		return response;
	}

	// Create a new car location or add cars to an existing location
	// NOTE: if price <= 0 and the location already exists, it maintains its current price
	public boolean addCars(int xid, String location, int count, int price)
	{
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
	public boolean deleteCars(int xid, String location)
	{
		return deleteItem(xid, Car.getKey(location));
	}

	// Returns the number of cars available at a location
	public int queryCars(int xid, String location)
	{
		return queryNum(xid, Car.getKey(location));
	}

	// Returns price of cars at this location
	public int queryCarsPrice(int xid, String location)
	{
		return queryPrice(xid, Car.getKey(location));
	}

	// Adds car reservation to this customer
	public boolean reserveCar(int xid, int customerID, String location)
	{
		return reserveItem(xid, customerID, Car.getKey(location), location);
	}


}
 
