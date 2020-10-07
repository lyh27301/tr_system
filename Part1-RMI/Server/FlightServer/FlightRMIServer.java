package Server.FlightServer;

import Server.Common.ResourceRMIServer;
import Server.Interface.IFlightManager;

import java.rmi.server.UnicastRemoteObject;

public class FlightRMIServer extends ResourceRMIServer {


    public FlightRMIServer() {
        super();
    }

    public static void main(String args[]) {
        setUpHost("FlightServer", 3016);
        securitySetUp();

        try {
            FlightResourceManager server = new FlightResourceManager();
            IFlightManager flightManagerProxy = (IFlightManager) UnicastRemoteObject.exportObject(server, 0);
            bindRMIRegistory(flightManagerProxy);
        }catch(Exception e) {
            System.out.println("Flight server exception");
            e.printStackTrace();
            System.exit(1);
        }
    }

}
