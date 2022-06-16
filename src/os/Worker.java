package os;

import master.MasterMain;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Worker {

    private boolean running;
    private int port;

    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket socket;
    private StorgeHandler storgeHandler;


    public Worker(int port) {
        this.port=port;
    }

    public static void main(String[] args) {

        int port = MasterMain.port;
        Worker worker= new Worker(port);

        try {
            worker.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void start() throws IOException {
        running = true;

        establishConnection();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        Thread threadStorge = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listen2Storge();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        threadStorge.start();


    }

    private void establishConnection() throws IOException {
        socket = new Socket(InetAddress.getLocalHost(), port);
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());

        Socket storgeSocket = new Socket(InetAddress.getLocalHost(), 100);
        storgeHandler= new StorgeHandler(storgeSocket);

    }

    private void listen() throws IOException {
        while (running) {
            String message = dis.readUTF();
            handleMessage(message);
        }
    }
    private void listen2Storge() throws IOException {
        while (running) {
            String message = storgeHandler.listenForResponse();
            handleMessage(message);
        }


    }


    private void handleMessage(String message) throws IOException {
        if (message.equalsIgnoreCase("close storge")){
            storgeHandler.close();
            return;
        }
        if (message.equalsIgnoreCase("close server")){
            socket.close();
            return;
        }

        String [] req=message.split(" ");
        if (req[0].equalsIgnoreCase("TASK")){
        //TODO
        }else if(req[0].equalsIgnoreCase("answer")){
        //TODO
        }else if (req[0].equalsIgnoreCase("intrupt")){
        //TODO
        }else if (req[0].equalsIgnoreCase("preemption")){
        //TODO
        }

    }

}
