package Server.CustomerServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

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
     * @return Success
     */
    public boolean deleteCustomer(int id, int customerID)
            throws RemoteException;


    /**
     * Query the customer reservations.
     *
     * @return A formatted bill for the customer
     */
    public String queryCustomerInfo(int id, int customerID)
            throws RemoteException;


}
