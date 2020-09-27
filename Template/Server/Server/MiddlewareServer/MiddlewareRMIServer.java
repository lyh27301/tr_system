package Server.MiddlewareServer;

import Server.CarServer.CarResourceManager;
import Server.Interface.IResourceManager;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MiddlewareRMIServer extends MiddlewareResourceManager{

    enum ServerType {CAR,FLIGHT, ROOM}

    private static String s_rmiPrefix = "group_16_";
    private static String s_serverName = "Middleware";
    private static int registryPort = 2016;

    //Car server config
    private static String car_serverHost = "localhost";
    private static int car_serverPort = 1016;
    private static String car_serverName = "CarServer";

    //Flight server config
    private static String flight_serverHost = "localhost";
    private static int flight_serverPort = 3016;
    private static String flight_serverName = "FlightServer";

    //Room server config
    private static String room_serverHost = "localhost";
    private static int room_serverPort = 4016;
    private static String room_serverName = "RoomServer";

    //Servers
    private CarResourceManager carResourceManager = null;



    public MiddlewareRMIServer() {
        super();
    }


    public static void main(String args[]) {
        System.setProperty("java.security.policy", "/Users/doreenhe/Documents/MySrc/travel_reservation_system/Template/Server/Server/MiddlewareServer/security.policy");
        if (System.getSecurityManager() == null) System.setSecurityManager(new SecurityManager());

        try {
            MiddlewareRMIServer middlewareServer = new MiddlewareRMIServer();
            MiddlewareResourceManager middlewareResourceManager = new MiddlewareResourceManager();

            //connect to servers
            IResourceManager carResourceManager = middlewareServer.findServer(ServerType.CAR);
            middlewareResourceManager.setCarResourceManager(carResourceManager);

            IResourceManager flightResourceManager = middlewareServer.findServer(ServerType.FLIGHT);
            middlewareResourceManager.setFlightResourceManager(flightResourceManager);

            IResourceManager roomResourceManager = middlewareServer.findServer(ServerType.ROOM);
            middlewareResourceManager.setRoomResourceManager(roomResourceManager);

            //provide RMI to client
            IMiddlewareResourceManager middlewareResourceManagerProxy = (IMiddlewareResourceManager)UnicastRemoteObject
                    .exportObject(middlewareResourceManager,0);

            Registry l_registry;
            try {
                l_registry = LocateRegistry.createRegistry(registryPort);
            } catch (RemoteException e) {
                l_registry = LocateRegistry.getRegistry(registryPort);
            }
            final Registry registry = l_registry;
            registry.rebind(s_rmiPrefix + s_serverName, middlewareResourceManagerProxy);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        registry.unbind(s_rmiPrefix + s_serverName);
                        System.out.println("Middleware resource manager unbounded");
                    }
                    catch(Exception e) {
                        System.out.println("Fail to unbound middleware resource manager");
                        e.printStackTrace();
                    }
                }
            });
            System.out.println("Middleware server is ready and bound to '" + s_rmiPrefix + s_serverName + "'");


        }catch (Exception e){
            System.out.println("Middleware exception");
            e.printStackTrace();
            System.exit(1);
        }

    }



    private IResourceManager findServer(ServerType type)
    {
        IResourceManager resourceManager = null;
        switch(type) {
            case CAR:
                resourceManager = connectServer(car_serverHost, car_serverPort, car_serverName);
                break;
            case FLIGHT:
                resourceManager = connectServer(flight_serverHost, flight_serverPort, flight_serverName);
                break;
            case ROOM:
                resourceManager = connectServer(room_serverHost, room_serverPort, room_serverName);
                break;
        }
        return resourceManager;
    }

    private IResourceManager connectServer(String server, int port, String name)
    {
        try {
            boolean first = true;
            while (true) {
                try {
                    Registry registry = LocateRegistry.getRegistry(server, port);
                    IResourceManager resourceManager = (IResourceManager)registry.lookup(s_rmiPrefix + name);
                    System.out.println("Connected to '" + name + "' server [" + server + ":" + port + "/" + s_rmiPrefix + name + "]");
                    return resourceManager;
                } catch (NotBoundException | RemoteException e) {
                    if (first) {
                        System.out.println("Connection failure due to "+ e.getMessage());
                        System.out.println("Waiting for '" + name + "' server [" + server + ":" + port + "/" + s_rmiPrefix + name + "]");
                        first = false;
                    }
                }
                Thread.sleep(500);
            }
        } catch (Exception e) {
            System.out.println(server + "exception");
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }




}
