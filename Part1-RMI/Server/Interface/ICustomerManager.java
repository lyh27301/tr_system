package Server.Interface;

import Server.Common.ReservedItem;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ICustomerManager extends Remote {
    /**
     * Add customer.
     *
     * @return Unique customer identifier
     */
    public int newCustomer(int id)
            throws RemoteException;

    /**
     * Add customer with id.
     *
     * @return Success
     */
    public boolean newCustomer(int id, int cid)
            throws RemoteException;

    /**
     * Delete a customer and associated reservations.
     *
     * @return items string
     */
    public String deleteCustomer(int xid, int customerID) throws RemoteException;


    /**
     * Query the customer reservations.
     *
     * @return A formatted bill for the customer
     */
    public String queryCustomerInfo(int id, int customerID)
            throws RemoteException;


    /**
     * Add item in customer reservations.
     *
     * @return Success
     */
    public boolean reserveItem (int xid, int customerID, String key, String location, int price) throws RemoteException;;



}
