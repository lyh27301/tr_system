package Server.RoomServer;

import Server.Common.ResourceRMIServer;
import Server.Interface.IRoomManager;

import java.rmi.server.UnicastRemoteObject;

public class RoomRMIServer extends ResourceRMIServer {

    public RoomRMIServer() {
        super();
    }

    public static void main(String args[]) {
        setUpHost("RoomServer", 4016);
        securitySetUp();
        try {
            RoomResourceManager server = new RoomResourceManager();
            IRoomManager roomManagerProxy = (IRoomManager) UnicastRemoteObject.exportObject(server, 0);
            bindRMIRegistory(roomManagerProxy);
        }catch(Exception e) {
            System.out.println("Room server exception");
            e.printStackTrace();
            System.exit(1);
        }
    }

}
