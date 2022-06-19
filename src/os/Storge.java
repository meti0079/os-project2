package os;

import logMe.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Storge {

    HashMap<String, Integer> data;


    private Logger logger;
    private boolean running;
    private ServerSocket storgeSocket;
    private static int PORT=100;


    public Storge() {
        data = new HashMap<>();
        running=true;
        logger=new Logger("Storge");
        logger.write("storge start on port "+PORT);

    }

    public static void main(String[] args) {
        PORT=Integer.parseInt(args[0]);
        Storge storge = new Storge();
        try {
            storge.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void start() throws IOException {
        establishStorge();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (running){
                        listen();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }

    private void establishStorge() throws IOException {
        storgeSocket = new ServerSocket(PORT);
        logger.write("storge established ");
    }

    private void listen() throws IOException {
          Socket socket =storgeSocket.accept();
          logger.write("some one connect");
          handleWorker(socket);


    }

    private void handleWorker(Socket socket) throws IOException {
        logger.write("now in handle works ");
      DataInputStream  dis = new DataInputStream(socket.getInputStream());
      DataOutputStream  dos = new DataOutputStream(socket.getOutputStream());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    String request = null;
                    try {
                        request = dis.readUTF();
                        logger.write("storge recive "+request);
                       if (!handleRequest(request,dos)){
                           socket.close();
                           break;
                       }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();

    }


    private boolean handleRequest(String request,DataOutputStream outputStream) throws IOException {
        if (request.equalsIgnoreCase("close")) {
            outputStream.writeUTF("close");
            return false;
        }
        String []reqSplited=request.split(" ");
        if (reqSplited[0].equalsIgnoreCase("push")){

            //TODO
        }else if(reqSplited[0].equalsIgnoreCase("obtain")){

            //TODO
        }else if (reqSplited[0].equalsIgnoreCase("released")) {

            //TODO
        }

        return true;
    }

}

