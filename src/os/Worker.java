package os;

import logMe.Logger;
import master.MasterMain;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Worker {
    private Logger logger;
    private boolean running;
    private int port;
    private int storgePort;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket socket;
    private StorgeHandler storgeHandler;
    private Task task;
    private Thread taskThread;
    private Object lock;

    public Worker(int port, int sport) {
        this.storgePort = sport;
        lock = new Object();
        this.port = port;
        logger = new Logger("Worker");
    }

    public static void main(String[] args) {
        int sPort = Integer.parseInt(args[0]);
        int port = MasterMain.port;
        Worker worker = new Worker(port, sPort);

        try {
            worker.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            worker.close();
        }));
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
        logger.write("worker connect to server ");
        Socket storgeSocket = new Socket(InetAddress.getLocalHost(), storgePort);
        storgeHandler = new StorgeHandler(storgeSocket);
        logger.write("worker connect to storge");
    }

    private void listen() throws IOException {
        logger.write("worker now listen to server ");
        while (running) {
            String message = dis.readUTF();
            logger.write("worker recive message from worker : " + message);
            handleMessage(message);
        }
    }

    private void listen2Storge() throws IOException {
        while (running) {
            String message = storgeHandler.listenForResponse();
            logger.write("worker recive message from storge : " + message);
            handleMessage(message);
        }


    }


    private synchronized void  handleMessage(String message) throws IOException {
        logger.write("in handle message : " + message);
        if (message.equalsIgnoreCase("close storge")) {
            storgeHandler.close();
            return;
        }
        if (message.equalsIgnoreCase("close server")) {
            socket.close();
            return;
        }

        String[] req = message.split(" ");
        if (req[0].equalsIgnoreCase("TASK")) {
            int id = Integer.parseInt(req[1]);
            task = new Task(changeTask2String(req), id);
            handleTask(task);

        } else if (req[0].equalsIgnoreCase("answer")) {
            synchronized (lock) {
                task.setRes(task.getRes() + Integer.parseInt(req[1]));
                logger.write("task update to id: " + task.getId() + "  res: " + task.getRes());
                lock.notify();
            }

        } else if (req[0].equalsIgnoreCase("intrupt")) {
            //TODO
        } else if (req[0].equalsIgnoreCase("preemption")) {
            //TODO
        }

    }

    private void handleTask(Task task) {
        taskThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    synchronized (lock) {
                        try {
                            if (task.idDone()) {
                                taskThread.stop();
                                dos.writeUTF("response " + task.getRes());
                            }
                            String s[] = task.getTask();

                            Thread.sleep(Integer.parseInt(s[0]));
                            storgeHandler.sendRequest("obtain " + task.getId() + " " + s[1]);
                            logger.write("send request to storge for " + "obtain " + task.getId() + " " + s[1]);
                            lock.wait();
                        } catch (InterruptedException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        taskThread.start();
    }

    private void close() {
        logger.write("bye bye");
        running = false;
    }

    private String changeTask2String(String[] res) {
        String ss = "";
        for (int i = 2; i < res.length; i++) {
            if (i % 2 == 2) {
                ss += res[i];
            } else {
                ss += (" " + res[i]);
            }
        }
        System.out.println("changTask2String : " + ss);
        return ss;
    }

}
