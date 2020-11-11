package Server.Common;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnection {

    ObjectOutputStream outputStream;
    ObjectInputStream inputStream;
    Socket soket;

    public ClientConnection(Socket soket, ObjectInputStream inputStream, ObjectOutputStream outputStream) {
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        this.soket = soket;
    }

    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    public ObjectInputStream getInputStream() {
        return inputStream;
    }

    public Socket getSoket() {
        return soket;
    }
}
