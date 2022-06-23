package os;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class StorageHandler {

    private Socket clientSocket;
    private DataInputStream dis;
    private DataOutputStream dos;


    public StorageHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.dis = new DataInputStream(clientSocket.getInputStream());
        this.dos = new DataOutputStream(clientSocket.getOutputStream());

    }

    public void sendRequest(String request) throws IOException {
        dos.writeUTF(request);
    }

    public String listenForResponse() throws IOException {
        return dis.readUTF();
    }

    public void close(){
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
