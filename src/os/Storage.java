package os;

import logMe.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class Storage {

    private ArrayList<Connection> connections;
    private ArrayList<String> dataLeader;
    private ArrayList<String> dataValue;
    private ArrayList<LinkedList> queues;
    private Logger logger = Logger.getInstance();
    private boolean running;
    private ServerSocket storageSocket;
    private static int port;
    private static int workerNumber;

    public Storage() {
        dataLeader = new ArrayList<>();
        dataValue = new ArrayList<>();
        connections = new ArrayList<>();
        running = true;
        queues = new ArrayList<>();
        Logger.getInstance().write("storage start on port " + port);
    }

    public static void main(String[] args) {
        Logger.getInstance().setName("storage");
        port = Integer.parseInt(args[0]);
        workerNumber = Integer.parseInt(args[1]);
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
        logger.write("worker number " + workerNumber);
        for (int i = 0; i < workerNumber; i++) {
            waiteForServerConnection();
            logger.write("worker connect ");
        }
    }

    private void waiteForServerConnection() throws IOException {
        Socket socket = storageSocket.accept();
        Connection connection = new Connection(socket, this);
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
            dataLeader.add("-1");
            queues.add(new LinkedList());
            logger.write("data pushed in dataMap " + (dataValue.size() - 1) + " " + reqSplited[1]);
        } else if (reqSplited[0].equalsIgnoreCase("obtain")) {
            String id = reqSplited[1];
            String index = reqSplited[2];
            if (obtainReq(index, id)) {
                connection.sendRequest("answer " + dataValue.get(Integer.parseInt(index)));
                logger.write("send //" + "answer " + dataValue.get(Integer.parseInt(index)) + "//  to id : " + id);
            }
        } else if (reqSplited[0].equalsIgnoreCase("released")) {
            logger.write("now in released ");
            int id = Integer.parseInt(reqSplited[1]);
            for (int i = 0; i < dataLeader.size(); i++) {
                if (dataLeader.get(i).equalsIgnoreCase(String.valueOf(id))) {
                    logger.write("found one " + i + " in hand task " + id);
                    if (queues.get(i).isEmpty()) {
                        logger.write("queue is empty ");
                        dataLeader.set(i, "-1");
                    } else {
                        dataLeader.set(i, (String) queues.get(i).poll());
                        logger.write("queue is not empty so has to sent to " + dataLeader.get(i) + " data" + dataValue.get(i));
                        sendDataToTask(dataLeader.get(i), dataValue.get(i));
                    }
                }
            }

        } else if (reqSplited[0].equalsIgnoreCase("worker")) {
            workerNumber = Integer.parseInt(reqSplited[1]);
        } else if (reqSplited[0].equalsIgnoreCase("MYTASK")) {
            connection.setTaskID(reqSplited[1]);
            logger.write("my task id now " + connection.getTaskID());
        } else if (reqSplited[0].equalsIgnoreCase("IAMINTRUPT")) {
            for (int i = 0; i < queues.size(); i++) {
                LinkedList<String> queue = queues.get(i);
                for (int j = 0; j < queue.size(); j++) {
                    if (queue.get(j).equalsIgnoreCase(reqSplited[1])) {
                        queue.remove(j);
                        break;
                    }
                }
            }


        } else if (reqSplited[0].equalsIgnoreCase("prevention")) {
            String id = reqSplited[1];
            ArrayList<String> indexes = new ArrayList<>();
            for (int i = 2; i < reqSplited.length; i++) {
                indexes.add(reqSplited[i]);
            }
            for (int i = 0; i < indexes.size(); i++) {
                if (dataLeader.get(Integer.parseInt(indexes.get(i))).equalsIgnoreCase(id) || dataLeader.get(Integer.parseInt(indexes.get(i))).equalsIgnoreCase("-1")) {

                } else {
                    connection.sendRequest("true");
                    return true;
                }
            }
            for (int i = 0; i < indexes.size(); i++) {
                dataLeader.set(Integer.parseInt(indexes.get(i)),id);
            }
            connection.sendRequest("false");
        }

        return true;
    }

    private void sendDataToTask(String id, String value) throws IOException {
        for (Connection connection : connections) {
            if (connection.getTaskID().equalsIgnoreCase(id)) {
                logger.write("found a worker to sent value");
                connection.sendRequest("answer " + value);
            }
        }
    }

    private boolean obtainReq(String index, String id) {
        if (Integer.parseInt(dataLeader.get(Integer.parseInt(index))) == Integer.parseInt(id)) {
            return true;
        } else if (Integer.parseInt(dataLeader.get(Integer.parseInt(index))) == -1) {
            dataLeader.set(Integer.parseInt(index), id);
            return true;
        } else {
            queues.get(Integer.parseInt(index)).add(id);
            return false;
        }
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

