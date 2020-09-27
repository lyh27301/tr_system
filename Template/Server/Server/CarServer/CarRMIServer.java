// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.CarServer;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CarRMIServer extends CarResourceManager
{
    private static String s_serverName = "CarServer";
    private static String s_rmiPrefix = "group_16_";
    private static int registryPort = 1016;

    public CarRMIServer() {
        super();
    }

    public static void main(String args[])
    {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }


        try {
            CarResourceManager server = new CarResourceManager();
            ICarManager carManagerProxy = (ICarManager)UnicastRemoteObject.exportObject(server, 0);

            Registry l_registry;
            try {
                l_registry = LocateRegistry.createRegistry(registryPort);
            } catch (RemoteException e) {
                l_registry = LocateRegistry.getRegistry(registryPort);
            }
            final Registry registry = l_registry;
            registry.rebind(s_rmiPrefix + s_serverName, carManagerProxy);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                try {
                    registry.unbind(s_rmiPrefix + s_serverName);
                    System.out.println("Car resource manager unbound");
                }
                catch(Exception e) {
                    System.out.println("Fail to unbound car resource manager");
                    e.printStackTrace();
                }
                }
            });
            System.out.println("Car server ready and car resource manager is bound to '" + s_rmiPrefix + s_serverName + "'");
        }
        catch (Exception e) {
            System.out.println("Car server exception");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
