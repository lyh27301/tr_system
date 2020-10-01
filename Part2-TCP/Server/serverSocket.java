package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class serverSocket
{

    public static void main(String args[])
    {

        serverSocket server= new serverSocket();
        try
        {
            server.runServerThread();
        }
        catch (IOException e)
        { }
    }

    public void runServerThread() throws IOException
    {
        ServerSocket serverSocket = new ServerSocket(9090);
        System.out.println("Server ready...");
        while (true)
        {
            Socket socket=serverSocket.accept();
            new serverSocketThread(socket).start();
        }
    }
}
