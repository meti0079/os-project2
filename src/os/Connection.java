package os;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Connection {
    private Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream dataInputStream;
    private Storge storge;


    public Connection(Socket socket,Storge storge) throws IOException {
        this.socket=socket;
        dataInputStream= new DataInputStream(socket.getInputStream());
        this.storge=storge;
        outputStream= new DataOutputStream(socket.getOutputStream());
        listenForRes();

    }


    private void listenForRes(){
        Thread thread= new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        String s=listenForResponse();
                        storge.handleRequest(s,Connection.this);
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


}