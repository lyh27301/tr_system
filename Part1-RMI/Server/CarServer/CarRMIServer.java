package Server.CarServer;

import Server.Common.ResourceRMIServer;
import Server.Interface.ICarManager;

import java.rmi.server.UnicastRemoteObject;

public class CarRMIServer extends ResourceRMIServer
{

    public CarRMIServer() {
        super();
    }

    public static void main(String args[])
    {
        setUpHost("CarServer", 1016);
        securitySetUp();
        try {
            CarResourceManager server = new CarResourceManager();
            ICarManager carManagerProxy = (ICarManager) UnicastRemoteObject.exportObject(server, 0);
            bindRMIRegistory(carManagerProxy);
        }catch(Exception e) {
            System.out.println("Car server exception");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
