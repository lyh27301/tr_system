package Server.CustomerServer;

import Server.Common.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Calendar;

public class CustomerResourceManager extends BasicResourceManager {

    public CustomerResourceManager(Socket clientSocket, DataInputStream inputStream, DataOutputStream outputStream)
    {
        super("CustomerResourceManager", clientSocket, inputStream, outputStream);
    }

    @Override
    public String executeRequest(String[] parsed) {
        String response = "";
        if (parsed[0].equals("QueryCustomer")){
            response = "Customer Info: "+queryCustomerInfo(stringToInt(parsed[1]), stringToInt(parsed[2]));

        }else if (parsed[0].equals("AddCustomerID")){
             if (newCustomer(stringToInt(parsed[1]), stringToInt(parsed[2]))){
                 response = "Created a new customer with customer ID: "+ parsed[2];
             }else{
                 response = "Fail to create a new customer. customer already exists";
             }

        }else if(parsed[0].equals("AddCustomer")){
            int cid = newCustomer(stringToInt(parsed[1]));
            response = "New customer added. Customer ID: " + cid;


        }else if(parsed[0].equals("DeleteCustomer")){
            response = deleteCustomer(stringToInt(parsed[1]), stringToInt(parsed[2]));
            if (response.equals("false")){
                response = "false";
            }
        }else if(parsed[0].equals("ReserveItem")){
            if(reserveItem(stringToInt(parsed[1]),stringToInt(parsed[2]),parsed[3],parsed[4],stringToInt(parsed[5]))){
                response = "Reserved successfully";
            }
        }
        return response;
    }

    public int newCustomer(int xid)
    {
        Trace.info("RM::newCustomer(" + xid + ") called");
        // Generate a globally unique ID for the new customer
        int cid = Integer.parseInt(String.valueOf(xid) +
                String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                String.valueOf(Math.round(Math.random() * 100 + 1)));
        Customer customer = new Customer(cid);
        writeData(xid, customer.getKey(), customer);
        Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid);
        return cid;
    }

    public boolean newCustomer(int xid, int customerID)
    {
        Trace.info("RM::newCustomer(" + xid + ", " + customerID + ") called");
        Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
        if (customer == null)
        {
            customer = new Customer(customerID);
            writeData(xid, customer.getKey(), customer);
            Trace.info("RM::newCustomer(" + xid + ", " + customerID + ") created a new customer");
            return true;
        }
        else
        {
            Trace.info("INFO: RM::newCustomer(" + xid + ", " + customerID + ") failed--customer already exists");
            return false;
        }
    }

    public String deleteCustomer(int xid, int customerID)
    {
        Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") called");
        Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
        if (customer == null)
        {
            Trace.warn("RM::deleteCustomer(" + xid + ", " + customerID + ") failed--customer doesn't exist");
            return "false";
        }
        else
        {
            // Increase the reserved numbers of all reservable items which the customer reserved.
            RMHashMap reservations = customer.getReservations();
            String items = "";
            boolean ifFirstItem = true;
            for (String reservedKey : reservations.keySet())
            {
                ReservedItem reserveditem = customer.getReservedItem(reservedKey);
                Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times");
                if(ifFirstItem == true){
                    items = items + reservedKey + "," + reserveditem.getCount();
                    ifFirstItem = false;
                }
                else{
                    items = items + ","+reservedKey + "," +reserveditem.getCount();
                }
//                ReservableItem item  = (ReservableItem)readData(xid, reserveditem.getKey());
//                Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") has reserved " + reserveditem.getKey() + " which is reserved " +  item.getReserved() +  " times and is still available " + item.getCount() + " times");
//                item.setReserved(item.getReserved() - reserveditem.getCount());
//                item.setCount(item.getCount() + reserveditem.getCount());
//                writeData(xid, item.getKey(), item);
            }

            // Remove the customer from the storage
            removeData(xid, customer.getKey());
            Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") succeeded");
            return items ;
        }
    }

    public String queryCustomerInfo(int xid, int customerID)
    {
        Trace.info("RM::queryCustomerInfo(" + xid + ", " + customerID + ") called");
        Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
        if (customer == null)
        {
            Trace.warn("RM::queryCustomerInfo(" + xid + ", " + customerID + ") failed--customer doesn't exist");
            // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
            return "";
        }
        else
        {
            Trace.info("RM::queryCustomerInfo(" + xid + ", " + customerID + ")");
            System.out.println(customer.getBill());
            return customer.getBill();
        }
    }

    protected boolean reserveItem(int xid, int customerID, String key, String location, int price) {
        Trace.info("RM::reserveItem(" + xid + ", customer=" + customerID + ", " + key + ", " + location + ") called" );
        // Read customer object if it exists (and read lock it)
        Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
//        if (customer == null)
//        {
//            Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
//            return false;
//        }
        customer.reserve(key, location, price);
        writeData(xid, customer.getKey(), customer);

        return true;
    }

}
 
