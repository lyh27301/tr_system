package Server.MiddlewareServer;
import Server.Common.*;

public class MiddlewareResourceManager extends ResourceManager{
    protected ResourceManagerTCPClient flightTCPClient = null;
    protected ResourceManagerTCPClient carTCPClient = null;
    protected ResourceManagerTCPClient customerTCPClient = null;
    protected ResourceManagerTCPClient roomTCPClient = null;

    public MiddlewareResourceManager(String name, String flightHost, int flightPort, String carHost, int carPort, String roomHost, int roomPort, String customerHost, int customerPort)
    {
        super(name);
        flightTCPClient = new ResourceManagerTCPClient(flightHost,flightPort);
        carTCPClient = new ResourceManagerTCPClient(carHost,carPort);
        roomTCPClient = new ResourceManagerTCPClient(roomHost,roomPort);
        customerTCPClient = new ResourceManagerTCPClient(customerHost, customerPort);

    }
    public void close() {
        flightTCPClient.stopTCPClient();
        carTCPClient.stopTCPClient();
        roomTCPClient.stopTCPClient();
        customerTCPClient.stopTCPClient();
    }
    //TODO: distribute commands
}
