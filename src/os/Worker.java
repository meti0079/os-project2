package os;

import logMe.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

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

    public Worker(int port, int sport) {
        this.storgePort = sport;
        this.port = port;
        logger = new Logger("Worker"+new Random().nextInt());
    }

    public static void main(String[] args) {
        int sPort = Integer.parseInt(args[0]);
        int port = Integer.parseInt(args[1]);
        Worker worker = new Worker(port, sPort);


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            worker.close();
        }));
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
//        Thread thread1= new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    listen2Storge();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread1.start();
    }

    private void establishConnection() throws IOException {
        socket = new Socket(InetAddress.getLocalHost(), port);
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        logger.write("worker connect to server ");
        Socket storgeSocket = new Socket(InetAddress.getLocalHost(), storgePort);
        storgeHandler = new StorgeHandler(storgeSocket);
        logger.write("worker connect to storage");
    }

    private void listen() throws IOException {
        logger.write("worker now listen to server ");
        while (running) {
            String message = dis.readUTF();
            logger.write("worker recive message from server : " + message);
            handleMessage(message);
        }
    }

//    private void listen2Storge() throws IOException {
//        while (running) {
//            String message = storgeHandler.listenForResponse();
//            logger.write("worker recive message from storge : " + message);
//            handleMessage(message);
//        }
//
//
//    }


    private synchronized void handleMessage(String message) throws IOException {
        logger.write("in handle message : " + message);

        String[] req = message.split(" ");
        if (req[0].equalsIgnoreCase("TASK")) {
            int id = Integer.parseInt(req[1]);
            task = new Task(changeTask2String(req), id);
            logger.write("in handlemessage 222222");
            handleTask(task);

//        } else if (req[0].equalsIgnoreCase("answer")) {
//                task.setRes(task.getRes() + Integer.parseInt(req[1]));
//                logger.write("task update to id: " + task.getId() + "  res: " + task.getRes());
//

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
//                    synchronized (lock) {
                        try {
                            if (task.idDone()) {
                                dos.writeUTF("response " + task.getRes());
                                break;
                            }
                            String s[] = task.getSubTask();
                            storgeHandler.sendRequest("obtain " + task.getId() + " " + s[1]);
                            logger.write("send request to storge for " + "obtain " + task.getId() + " " + s[1]);
                            String asnwer[]=storgeHandler.listenForResponse().split(" ");
                            task.setRes(task.getRes() + Integer.parseInt(asnwer[1]));
                            Thread.sleep(Integer.parseInt(s[0]));
                            logger.write("task update to id: " + task.getId() + "  res: " + task.getRes());
//                          lock.wait();

                        } catch (InterruptedException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
//            }
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
            if (i % 2 == 0) {
                ss += res[i];
            } else {
                ss += (" " + res[i]+" ");
            }
        }
        logger.write("changTask2String : " + ss);
        return ss.substring(0,ss.length()-1);
    }
}
