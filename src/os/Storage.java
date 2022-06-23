package os;

import logMe.Logger;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Storage {

    private ArrayList<Connection > connections;
    private ArrayList<String>dataLeader;
    private ArrayList<String> dataValue;

    private Logger logger=Logger.getInstance();
    private boolean running;
    private ServerSocket storageSocket;
    private static int port;
    private static int workerNumber;

    public Storage() {
        dataLeader = new ArrayList<>();
        dataValue = new ArrayList<>();
        connections=new ArrayList<>();
        running = true;

        Logger.getInstance().write("storage start on port " + port);
    }

    public static void main(String[] args) {
        Logger.getInstance().setName("storage");
        port = Integer.parseInt(args[0]);
        workerNumber=Integer.parseInt(args[1]);
        Storage storage = new Storage();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            storage.close();
        }));
        try {
            storage.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void start() throws IOException {
        establishStorage();
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
        Socket socket= storageSocket.accept();
        Connection connection= new Connection(socket,this);
        connections.add(connection);
    }

    private void establishStorage() throws IOException {
        storageSocket = new ServerSocket(port);
        logger.write("storage established ");
    }

    public boolean handleRequest(String request, Connection connection) throws IOException {
        logger.write("storage recive " + request);
        String[] reqSplited = request.split(" ");
        if (reqSplited[0].equalsIgnoreCase("push")) {
            dataValue.add(reqSplited[1]);
            dataLeader.put(String.valueOf(dataValue.size() - 1), "-1");
            logger.write("data pushed in dataMap " + (dataValue.size() - 1)+" "+reqSplited[1]);
        } else if (reqSplited[0].equalsIgnoreCase("obtain")) {
            String id = reqSplited[1];
            String index = reqSplited[2];
            String s="answer " + obtainReq(index, id);
            connection.sendRequest(s);
            logger.write("send "+s+" answer to id : " + id);
        } else if (reqSplited[0].equalsIgnoreCase("released")) {
            //TODO
        } else if (reqSplited[0].equalsIgnoreCase("worker")) {
            workerNumber=Integer.parseInt(reqSplited[1]);
        }

        return true;
    }

    private String obtainReq(String index, String id) {
//        if(dataLeader.get(index).equalsIgnoreCase("-1") || dataLeader.get(index).equalsIgnoreCase(id) ){
//        dataLeader.replace(index, id);
//                dataValue.get(Integer.parseInt(index));
//        }

        return dataValue.get(Integer.parseInt(index));

    }
    private void close() {
        try {
            storageSocket.close();
            logger.write("bye bye");
            running = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

