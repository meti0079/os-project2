package os;

import logMe.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Storge {


    private HashMap<String, String> dataLeader;
    private ArrayList<String> dataValue;

    private Logger logger;
    private boolean running;
    private ServerSocket storgeSocket;
    private static int port;


    public Storge() {
        dataLeader = new HashMap<>();
        dataValue = new ArrayList<>();
        running = true;
        logger = new Logger("Storge");
        logger.write("storge start on port " + port);
    }

    public static void main(String[] args) {

        port = Integer.parseInt(args[0]);
        Storge storge = new Storge();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            storge.close();
        }));
        try {
            storge.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void close() {
        try {
            logger.write("bye bye");
            storgeSocket.close();
            running = false;

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
                    while (running) {
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
        storgeSocket = new ServerSocket(port);
        logger.write("storge established ");
    }

    private void listen() throws IOException {
        Socket socket = storgeSocket.accept();
        logger.write("someone connect ");
        handleWorker(socket);


    }

    private void handleWorker(Socket socket) throws IOException {
        logger.write("now in handle works ");
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String request = null;
                    try {
                        request = dis.readUTF();
                        logger.write("storge recive " + request);
                        if (!handleRequest(request, dos)) {
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


    private boolean handleRequest(String request, DataOutputStream outputStream) throws IOException {
        if (request.equalsIgnoreCase("close")) {
            outputStream.writeUTF("close");
            return false;
        }
        String[] reqSplited = request.split(" ");
        if (reqSplited[0].equalsIgnoreCase("push")) {
            dataValue.add(reqSplited[1]);
            dataLeader.put(String.valueOf(dataValue.size()-1),"-1");
            logger.write("data pushed in dataMap "+reqSplited[1]);
        } else if (reqSplited[0].equalsIgnoreCase("obtain")) {
            String id=reqSplited[1];
            String index=reqSplited[2];
            outputStream.writeUTF("answer "+obtainReq(index,id));
            logger.write("send answer to id : "+id);
        } else if (reqSplited[0].equalsIgnoreCase("released")) {
            //TODO
        }

        return true;
    }

    private String obtainReq(String index, String id) {
//        if(dataLeader.get(index).equalsIgnoreCase("-1") || dataLeader.get(index).equalsIgnoreCase(id) ){
                dataLeader.replace(index,id);
//                dataValue.get(Integer.parseInt(index));
//        }

        return dataValue.get(Integer.parseInt(index));

    }

}

