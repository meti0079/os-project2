package os;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Connection {
    private Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream dataInputStream;
    private Storage storage;
    private String ID;


    public Connection(Socket socket, Storage storage) throws IOException {
        this.socket=socket;
        dataInputStream= new DataInputStream(socket.getInputStream());
        this.storage = storage;
        outputStream= new DataOutputStream(socket.getOutputStream());
        ID="-1";
        listenForRes();

    }


    private void listenForRes(){
        Thread thread= new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        String s=listenForResponse();
                        storage.handleRequest(s,Connection.this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }


    public void sendRequest(String request) throws IOException {
        outputStream.writeUTF(request);
    }

    public String listenForResponse() throws IOException {
        return dataInputStream.readUTF();
    }

    public void setTaskID(String id){
        this.ID=id;
    }

    public String getTaskID(){
        return ID;
    }
}
