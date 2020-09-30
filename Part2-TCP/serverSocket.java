import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;


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
