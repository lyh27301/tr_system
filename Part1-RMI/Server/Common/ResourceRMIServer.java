package Server.Common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ResourceRMIServer {

    private static String s_rmiPrefix = "group_16_";
    private static String s_serverName;
    private static int registryPort;
    private static Registry registry;
    public ResourceRMIServer() {
        super();
    }

    protected static void setUpHost (String serverName, int port){
        s_serverName = serverName;
        registryPort = port;
    }

    protected static void securitySetUp(){
        // System.setProperty("java.security.policy","/Users/doreenhe/Documents/MySrc/travel_reservation_system/Part1-RMI/security.policy");
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
    }

    protected static void bindRMIRegistory(Remote proxy) throws RemoteException {
        Registry l_registry;
        try {
            l_registry = LocateRegistry.createRegistry(registryPort);
        } catch (RemoteException e) {
            l_registry = LocateRegistry.getRegistry(registryPort);
        }
        registry = l_registry;
        registry.rebind(s_rmiPrefix + s_serverName, proxy);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    registry.unbind(s_rmiPrefix + s_serverName);
                    System.out.println(s_serverName + " resource manager unbound");
                }
                catch(Exception e) {
                    System.out.println("Fail to unbound " + s_serverName + " resource manager");
                    e.printStackTrace();
                }
            }
        });
        System.out.println(s_serverName+" ready and resource manager is bound to '" + s_rmiPrefix + s_serverName + "'");
    }

}
