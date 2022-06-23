package os;

import logMe.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

public class Worker {
    private Logger logger = Logger.getInstance();
    private boolean running;
    private int port;
    private int storagePort;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket socket;
    private StorageHandler storageHandler;
    private Task task;
    private Thread taskThread;
    private Thread threadForRes;
    private Object lock;
    private Object lock2;


    public Worker(int port, int sport) {
        lock = new Object();
        lock2 = new Object();
        this.storagePort = sport;
        this.port = port;
    }

    public static void main(String[] args) {
        Logger.getInstance().setName("worker" + new Random().nextInt(10));
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
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void establishConnection() throws IOException {
        socket = new Socket(InetAddress.getLocalHost(), port);
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        logger.write("worker connect to server ");
        Socket storageSocket = new Socket(InetAddress.getLocalHost(), storagePort);
        storageHandler = new StorageHandler(storageSocket);
        waitForStorageRes();
        logger.write("worker connect to storage");
    }

    private void listen() throws IOException, InterruptedException {
        logger.write("worker now listen to server ");
        while (running) {
            String message = dis.readUTF();
            logger.write("worker recive message from server : " + message);
            handleMessage(message);
        }
    }


    private  void handleMessage(String message) throws IOException, InterruptedException {
        synchronized (lock2) {
            String[] req = message.split(" ");
            if (req[0].equalsIgnoreCase("TASK")) {
                int id = Integer.parseInt(req[req.length-2]);
                task = new Task(changeTask2String(req), id);
                task.setRes(Integer.parseInt(req[req.length-1]));
                handleTask(task);
            } else if (req[0].equalsIgnoreCase("intrupt")) {
                logger.write(" in intrupt handlling");
                synchronized (lock) {
                    lock.notify();
                    logger.write("notif ");
                }
            } else if (req[0].equalsIgnoreCase("preemption")) {
                //TODO
            }
        }
    }

    private void handleTask(Task task) {
        if (taskThread!=null)taskThread.stop();
        taskThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (task.idDone()) {
                            dos.writeUTF("response " + task.getRes());
                            break;
                        }
                        String s[] = task.getSubTask();
                        if (handleSleepTime(Integer.parseInt(s[0]))) {
                            logger.write("after sleep time");
                            task.setLastTime("0");
                        } else {
                            dos.writeUTF("taskUnFinished " + task.getTaskString());
                            logger.write("send task unfinished to server ");
                            break;
                        }
                        if (handleIndex(s[1])) {
                            logger.write("task wait for response from storage");
                            task.setRes(task.getRes() + task.getLastData());
                            logger.write("task update to id: " + task.getId() + "  res: " + task.getRes());
                        } else {
                            logger.write("task intrupt in wait for storage ");
                            task.intruptInSleep("0");
                            dos.writeUTF("taskUnFinished " + task.getTaskString());
                            break;
                        }
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        taskThread.start();
    }

    private void waitForStorageRes() {
        final String[] x = {""};
        threadForRes = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String asnwer = storageHandler.listenForResponse();
                        logger.write("response from storage : " +asnwer);
                        x[0] = asnwer.split(" ")[1];

                        task.setLastData(Integer.parseInt(x[0]));
                        synchronized (lock) {
                            logger.write("before notify from storage");
                            lock.notify();
                            logger.write("after notify from storage");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        threadForRes.start();
    }



    private  boolean handleIndex(String index) throws IOException, InterruptedException {
        logger.write("send request to storage for " + "obtain " + task.getId() + " " + index);
        task.setLastData(null);
        storageHandler.sendRequest("obtain " + task.getId() + " " + index);
        synchronized (lock) {
            lock.wait();
        }
//        logger.write("after wait for storage wait ");
        if (task.getLastData() == null) {
            return false;
        }
        return true;
    }

    private synchronized boolean handleSleepTime(int time) throws InterruptedException {
        if (time == 0) return true;
        logger.write("befor sleep for time ");
        long st = System.currentTimeMillis();
        synchronized (lock) {
            lock.wait(time + 1);
        }
        long et = System.currentTimeMillis();
        long dif = et - st;
        logger.write("after sleep time 1");
        logger.write("dif "+dif+"   time :"+time);
        if (time > dif) {
            logger.write("task intrupt in sleep time " + String.valueOf(time - dif));
            task.intruptInSleep(String.valueOf(time - dif));
            return false;
        }
        logger.write("task dont intrupt in sleep time");
        return true;
    }

    private void close() {
        logger.write("bye bye");
        running = false;
    }

    private String changeTask2String(String[] res) {
        String ss = "";
        for (int i = 1; i < res.length-2; i++) {
            if (i % 2 == 1) {
                ss += res[i];
            } else {
                ss += (" " + res[i] + " ");
            }
        }
//        logger.write("changTask2String : " + ss);
        return ss.substring(0, ss.length() - 1);
    }

}
