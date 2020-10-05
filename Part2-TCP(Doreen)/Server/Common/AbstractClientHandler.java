package Server.Common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public abstract class AbstractClientHandler extends Thread{
    final Socket clientSocket;
    final DataInputStream inputStream;
    final DataOutputStream outputStream;



    public AbstractClientHandler(Socket clientSocket, DataInputStream inputStream, DataOutputStream outputStream, BasicResourceManager resourceManager) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run(){
        while(true){
            try {
                String received = inputStream.readUTF();

                String[] parsed = received.split(",");

                if (parsed[0].equals("Quit")){
                    this.clientSocket.close();
                    Trace.info("A client connection is closed");
                    break;
                }

                String response = executeRequest(parsed);
                outputStream.writeUTF(response);
            }catch (IOException e) {
                e.printStackTrace();
            }

        }
        try{
            this.outputStream.close();
            this.inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public abstract String executeRequest(String[] parsed);

    protected int stringToInt(String s){
        return Integer.valueOf(s);
    }

}
