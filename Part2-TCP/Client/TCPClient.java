package Client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TCPClient extends Client {
    private static String s_serverHost = "localhost";
    private static int s_serverPort = 2016;
    private static String s_groupPrefix = "group_16_";

    private PrintWriter outToServer = null;
    private BufferedReader inFromServer = null;
    private Socket socket = null;

    public static void main(String args[]) throws IOException {

        // Check if continue with the default value.
        if (args.length == 1) {
            s_serverHost = s_groupPrefix + args[0];
        }
        if (args.length == 2) {
            s_serverPort = Integer.parseInt(args[1]);
        }
        if (args.length > 2) {
            System.err.println((char) 27 + "[31;1mClient exception: " + (char) 27
                    + "[0mUsage: java client.RMIClient [server_hostname [server_rmiobject]]");
            System.exit(1);
        }

        // Set security policy.
        System.setProperty("java.security.policy",
                "/Users/yanhan/Documents/Github/travel_reservation_system/Template/Server/Server/MiddlewareServer/security.policy");

        try {
            TCPClient client = new TCPClient();
            client.start();
        } catch (Exception e) {
            System.err.println((char) 27 + "[31;1mClient exception: " + (char) 27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }

    }

    public TCPClient() {
        super();
        connectServer();
    }

    public void connectServer() {
        boolean connected = false;

        try {
            while (!connected) // works forever
            {
                // Try to connect the middleware server
                try {

                    // Establish a client socket using the given port#
                    socket = new Socket(s_serverHost, s_serverPort);

                    // Open an output stream to the server.
                    outToServer = new PrintWriter(socket.getOutputStream(), true);

                    // Open an input stream from the server.
                    inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    // Print the connection success message
                    System.out.println(
                            "Connected to host: [" + s_serverHost + "] on port: " + s_serverPort + " successfully.");

                    // When successfully connected, start to read user's input
                    connected = true;

                } catch (Exception e) {
                    if (!connected) {
                        // If the connecting failed, print the waiting message and try again.
                        System.out.println("Host: [" + s_serverHost + "] on port: " + s_serverPort
                                + " is busy, waiting to connect.");
                    }
                }
                Thread.sleep(500);
            }

        } catch (Exception e) {
            e.printStackTrace();
			System.exit(1);
        }

    }
    
	public void outToMiddleware(String args, String successMsg, String failureMsg, TYPE type) throws IOException{
        // Send out the string of arguments
        outToServer.println(args);

        String response = "";
        
		String inputLine;
		while ((inputLine = inFromServer.readLine()) != null) {
			if (response.length() == 0)
				response += inputLine;
			else
				response += "\n" + inputLine;
        }
        
        // connectServer(false);
		// //System.out.println("Response: " + response);
		// // if (response.equals("")) {
		// // 	client.connect(false);
		// // 	response = client.sendMessage(args);
		// // }

		if (response != null) {
			try {
				if (type == TYPE.BOOL && toBoolean(response)) {
					System.out.println(successMsg);
					return;
				}
				else if (type == TYPE.INT) {
					System.out.println(successMsg + toInt(response));
					return;
				}
				else if (type == TYPE.STR) {
					System.out.println(successMsg + response);
					return;
				}
			}catch(Exception e) {
				System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0m" + e.getLocalizedMessage());
			}
		}
		System.out.println(failureMsg);
	}

    public void endTCPClient(Socket socket, BufferedReader bufferedReader, BufferedReader inFromServer,
            PrintWriter outToServer) throws IOException {
        bufferedReader.close();
        inFromServer.close();
        outToServer.close();
        socket.close();
    }
}
