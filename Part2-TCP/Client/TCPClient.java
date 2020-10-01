package Client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TCPClient extends Client {

    private static int serverPort = 2016;
    private static String groupPrefix = "group_16_";
    private static String serverName = groupPrefix + "Client";

    public static void main(String args[]) throws IOException {

        // Check if continue with the default value.
        if (args.length == 1)
		{
			serverName = groupPrefix + args[0];
		}
		if (args.length == 2)
		{
			serverPort = Integer.parseInt(args[1]);
		}
		if (args.length > 2)
		{
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUsage: java client.RMIClient [server_hostname [server_rmiobject]]");
			System.exit(1);
        }
        
        // Set security policy.
        System.setProperty("java.security.policy", "/Users/yanhan/Documents/Github/travel_reservation_system/Template/Server/Server/MiddlewareServer/security.policy"); 

		try {
			TCPClient client = new TCPClient();
			client.start();
        } 
        
		catch (Exception e) {    
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
        }

        
    }

    public TCPClient() {
        super();
    }

    public void connectServer()  
	{
        try {
            Socket socket = new Socket(serverName, serverPort); // establish a socket with a server using the given port#

            PrintWriter outToServer = new PrintWriter(socket.getOutputStream(), true); // open an output stream to the server...
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream())); // open an input stream from the server...
    
            BufferedReader bufferedReader = new java.io.BufferedReader(new InputStreamReader(System.in)); // to read user's input

            while (true) // works forever
            {
                String readerInput = bufferedReader.readLine(); // read user's input
                if (readerInput.equals("quit"))
                    break;
                outToServer.println(readerInput); // send the user's input via the output stream to the server
                String res = inFromServer.readLine(); // receive the server's result via the input stream from the server
                System.out.println("result: " + res); // print the server result to the user
            }
    
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
       
       
	}
}
