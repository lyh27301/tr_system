package Server.CustomerServer;

import Server.Common.ResourceRMIServer;

import java.rmi.server.UnicastRemoteObject;

public class CustomerRMIServer extends ResourceRMIServer {

    public CustomerRMIServer(){
        super();
    }

    public static void main (String args[]){
        setUpHost("CustomerServer", 5016);
        securitySetUp();

        try{
            CustomerResourceManager server = new CustomerResourceManager();
            ICustomerManager customerManagerProxy = (ICustomerManager) UnicastRemoteObject.exportObject(server, 0);
            bindRMIRegistory(customerManagerProxy);
        }catch(Exception e) {
            System.out.println("Customer server exception");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
