package Client;

import Server.Common.Message;
import Server.Common.Trace;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;


public class TCP_Client {


    static int middlewarePort = 6116;

    public static void main (String[] args) throws Exception{

        String middlewareHost = "localhost";
        if (args.length > 0) middlewareHost = args[0];

        InetAddress ip = InetAddress.getByName(middlewareHost);
        Socket s = new Socket(ip, middlewarePort);

        ObjectInputStream inputStream = new ObjectInputStream(s.getInputStream());
        ObjectOutputStream outputStream = new ObjectOutputStream(s.getOutputStream());
        outputStream.flush();

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
            parsed = command.split(",");
            if(parsed[0].equals("Help")){
                if (parsed.length == 1) {
                    System.out.println(Command.description());
                } else if (parsed.length == 2) {
                    Command l_cmd = Command.fromString(parsed[1]);
                    System.out.println(l_cmd.toString());
                } else {
                    System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mImproper use of help command. Location \"help\" or \"help,<CommandName>\"");
                }
                continue;
            }

            if(parsed[0].equals("Quit")){
                System.out.println("Good bye!");
                break;
            }

            if(parsed[0].equals("Shutdown")){
                outputStream.writeObject(new Message(command));
                System.out.println("Server has been shutdown");
                break;
            }

            try {
                outputStream.writeObject(new Message(command));
                String response = ((Message) inputStream.readObject()).getMessageText();
                System.out.println(response);
            }catch (IOException e){
                break;
            }

        }

        stdin.close();
        try{
            s.close();
            inputStream.close();
            outputStream.close();
            if (!s.isClosed()) s.close();
        }catch (IOException e){
            Trace.warn("Server has been shutdown");
            return;
        }
    }


}
