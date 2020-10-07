// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.Common;


import java.rmi.RemoteException;


public class BasicResourceManager
{
	protected String m_name = "";
	protected RMHashMap m_data = new RMHashMap();

	public BasicResourceManager(String p_name)
	{
		m_name = p_name;
	}

	// Reads a data item
	protected RMItem readData(int xid, String key)
	{
		synchronized(m_data) {
			RMItem item = m_data.get(key);
			if (item != null) {
				return (RMItem)item.clone();
			}
			return null;
		}
	}

	// Writes a data item
	protected void writeData(int xid, String key, RMItem value)
	{
		synchronized(m_data) {
			m_data.put(key, value);
		}
	}

	// Remove the item out of storage
	protected void removeData(int xid, String key)
	{
		synchronized(m_data) {
			m_data.remove(key);
		}
	}

	// Deletes the encar item
	protected boolean deleteItem(int xid, String key)
	{
		Trace.info("RM::deleteItem(" + xid + ", " + key + ") called");
		ReservableItem curObj = (ReservableItem)readData(xid, key);
		// Check if there is such an item in the storage
		if (curObj == null)
		{
			Trace.warn("RM::deleteItem(" + xid + ", " + key + ") failed--item doesn't exist");
			return false;
		}
		else
		{
			if (curObj.getReserved() == 0)
			{
				removeData(xid, curObj.getKey());
				Trace.info("RM::deleteItem(" + xid + ", " + key + ") item deleted");
				return true;
			}
			else
			{
				Trace.info("RM::deleteItem(" + xid + ", " + key + ") item can't be deleted because some customers have reserved it");
				return false;
			}
		}
	}

	// Query the number of available seats/rooms/cars
	protected int queryNum(int xid, String key)
	{
		Trace.info("RM::queryNum(" + xid + ", " + key + ") called");
		ReservableItem curObj = (ReservableItem)readData(xid, key);
		int value = 0;  
		if (curObj != null)
		{
			value = curObj.getCount();
		}
		Trace.info("RM::queryNum(" + xid + ", " + key + ") returns count=" + value);
		return value;
	}    

	// Query the price of an item
	protected int queryPrice(int xid, String key)
	{
		Trace.info("RM::queryPrice(" + xid + ", " + key + ") called");
		ReservableItem curObj = (ReservableItem)readData(xid, key);
		int value = 0; 
		if (curObj != null)
		{
			value = curObj.getPrice();
		}
		Trace.info("RM::queryPrice(" + xid + ", " + key + ") returns cost=$" + value);
		return value;        
	}

	// Reserve an item
	protected boolean reserveItem(int xid, int customerID, String key, String location)
	{
		Trace.info("RM::reserveItem(" + xid + ", customer=" + customerID + ", " + key + ", " + location + ") called" );        
		// Read customer object if it exists (and read lock it)
		/*
		Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
		if (customer == null)
		{
			Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
			return false;
		}
		*/


		// Check if the item is available
		ReservableItem item = (ReservableItem)readData(xid, key);
		if (item == null)
		{
			Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") failed--item doesn't exist");
			return false;
		}
		else if (item.getCount() == 0)
		{
			Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") failed--No more items");
			return false;
		}
		else
		{            
			//customer.reserve(key, location, item.getPrice());
			//writeData(xid, customer.getKey(), customer);

			// Decrease the number of available items in the storage
			item.setCount(item.getCount() - 1);
			item.setReserved(item.getReserved() + 1);
			writeData(xid, item.getKey(), item);

			Trace.info("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") succeeded");
			return true;
		}        
	}

	public String getName() throws RemoteException
	{
		return m_name;
	}

	protected boolean cancelReservation(int xid, int customerID, String key, int count){
		ReservableItem item  = (ReservableItem)readData(xid, key);
		Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") has reserved " + key + " which is reserved " +  item.getReserved() +  " times and is still available " + item.getCount() + " times");
		item.setReserved(item.getReserved() - count);
		item.setCount(item.getCount() + count);
		writeData(xid, item.getKey(), item);
		return true;
	}

}
 
