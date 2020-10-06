package Client;

import Server.Common.Trace;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Vector;

public class TCP_Client {

    static String middlewareHost = "localhost";
    static int middlewarePort = 6116;

    public static void main (String[] args) throws Exception{


        InetAddress ip = InetAddress.getByName(middlewareHost);
        Socket s = new Socket(ip, middlewarePort);

        DataInputStream inputStream = new DataInputStream(s.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(s.getOutputStream());

        // Prepare for reading commands
        System.out.println();
        System.out.println("Location \"help\" for list of supported commands");
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        while (true) {

            // Read the next command
            String command = "";
            try {
                System.out.print((char)27 + "[32;1m\n>] " + (char)27 + "[0m");
                command = stdin.readLine().trim();
            }
            catch (IOException io) {
                System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0m" + io.getLocalizedMessage());
                io.printStackTrace();
                System.exit(1);
            }
            String[] parsed = command.split(",");
            for (int i=0; i<parsed.length; i++){
                parsed[i]=parsed[i].trim();
            }
            command = String.join(",", parsed);

            outputStream.writeUTF(command);

            if(parsed[0].equals("Quit")){
                outputStream.writeUTF(command);
                String quitAck = inputStream.readUTF();
                System.out.println(quitAck);
                break;
            }

            String response = inputStream.readUTF();
            System.out.println(response);

        }
        stdin.close();
        inputStream.close();
        outputStream.close();
        Trace.info("Server connection closed");

    }


}
