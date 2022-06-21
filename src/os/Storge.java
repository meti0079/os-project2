package os;

import logMe.Logger;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Storge {

    private ArrayList<Connection > connections;
    private HashMap<String, String> dataLeader;
    private ArrayList<String> dataValue;

    private Logger logger;
    private boolean running;
    private ServerSocket storgeSocket;
    private static int port;
    private static int workerNumber;

    public Storge() {
        dataLeader = new HashMap<>();
        dataValue = new ArrayList<>();
        connections=new ArrayList<>();
        running = true;
        logger = new Logger("Storge");
        logger.write("storge start on port " + port);
    }

    public static void main(String[] args) {

        port = Integer.parseInt(args[0]);
        workerNumber=Integer.parseInt(args[1]);
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


    private void start() throws IOException {
        establishStorge();
        waiteForServerConnection();
        logger.write("server connect ");
        waiteForWorkerConnection();

    }

    private void waiteForWorkerConnection() throws IOException {
        logger.write("worker number "+workerNumber);
        for (int i = 0; i < workerNumber; i++) {
            waiteForServerConnection();
            logger.write("worker connect ");
        }
    }

    private void waiteForServerConnection() throws IOException {
        Socket socket= storgeSocket.accept();
        Connection connection= new Connection(socket,this);
        connections.add(connection);
    }

    private void establishStorge() throws IOException {
        storgeSocket = new ServerSocket(port);
        logger.write("storge established ");
    }


//    private void handleWorker(WorkerHandler workerHandler) throws IOException {
//        logger.write("now in handle works ");
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    String request = null;
//                    try {
//                        request = workerHandler.listenForResponse();
//
//                        if (!handleRequest(request, workerHandler)) {
////                            workerHandler.close();
//                            break;
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//        thread.start();
//
//    }


    public boolean handleRequest(String request, Connection connection) throws IOException {
        logger.write("storge recive " + request);
        String[] reqSplited = request.split(" ");
        if (reqSplited[0].equalsIgnoreCase("push")) {
            dataValue.add(reqSplited[1]);
            dataLeader.put(String.valueOf(dataValue.size() - 1), "-1");
            logger.write("data pushed in dataMap " + (dataValue.size() - 1)+" "+reqSplited[1]);
        } else if (reqSplited[0].equalsIgnoreCase("obtain")) {
            String id = reqSplited[1];
            String index = reqSplited[2];
            connection.sendRequest("answer " + obtainReq(index, id));
            logger.write("send answer to id : " + id);
        } else if (reqSplited[0].equalsIgnoreCase("released")) {
            //TODO
        } else if (reqSplited[0].equalsIgnoreCase("worker")) {
            workerNumber=Integer.parseInt(reqSplited[1]);
        }

        return true;
    }

    private String obtainReq(String index, String id) {
//        if(dataLeader.get(index).equalsIgnoreCase("-1") || dataLeader.get(index).equalsIgnoreCase(id) ){
        dataLeader.replace(index, id);
//                dataValue.get(Integer.parseInt(index));
//        }

        return dataValue.get(Integer.parseInt(index));

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
}

