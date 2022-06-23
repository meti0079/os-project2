package os;

import logMe.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable {

    private Logger logger;
    private ServerSocket serverSocket;
    private Socket clientSocket;

    private int port;
    private int workerNumbers;
    private int storegPort;
    private int RRTime;
    private int taskNum;
    private int taskDone;
    private boolean running;

    private StorageHandler storageHandler;

    private int alg;
    private String deadlock;


    private ArrayList<WorkerHandler> workerHandlers;
    private ArrayList<Task> tasks;
    private ArrayList<String> data;
    private ArrayList<Task> taskInProcesses;


    public Server(int port, int n, ArrayList<String> data, ArrayList<Task> tasks, int storegPort, int RRTime, String alg, String deadlock) {
        taskNum = tasks.size();
        this.data = data;
        this.port = port;
        this.storegPort = storegPort;
        workerNumbers = n;
        workerHandlers = new ArrayList<>();
        this.tasks = tasks;
        this.RRTime = RRTime;
        this.deadlock = deadlock;
        if (alg.equalsIgnoreCase("FCFS")) this.alg = 1;
        if (alg.equalsIgnoreCase("RR")) this.alg = 2;
        if (alg.equalsIgnoreCase("SJF")) this.alg = 3;

        taskInProcesses = new ArrayList<>();

        System.out.println("server start on port " + port);
        logger = Logger.getInstance();
        logger.write("server start on port " + port);

    }


    @Override
    public void run() {
        try {
            connect2Storage();
            pushDataOnStorage();
            sleep(1000);
            waiteForWorkerConnection();
            sleep(1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        handleWorks();

    }

    private void pushDataOnStorage() throws IOException {
        for (int i = 0; i < data.size(); i++) {
            storageHandler.sendRequest("push " + data.get(i));
            logger.write("pushed " + data.get(i) + " on storage");
        }
        storageHandler.sendRequest("worker " + workerNumbers);
        logger.write("pushed worker numbers" + workerNumbers + " on storage");
    }

    private void RRHandling() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (running) {
                        sleep(RRTime);
                        for (WorkerHandler workerHandler : workerHandlers) {
                            if (workerHandler.getTask() != null) {
                                workerHandler.sendRequest("intrupt " + workerHandler.getTask().getId());
                                synchronized (workerHandler.lock) {
                                    workerHandler.lock.wait();
                                }

                            }
                        }
                        //TODO remove all queue in storage
                        for (WorkerHandler workerHandler:workerHandlers) {
                            Task task=getTask();
                            if (task!=null){
                                workerHandler.setTask2Worker(task);
                                tasks.remove(task);
                            }
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
        thread.start();

    }

    private void handleWorks() {
        if (alg == 2) {
            RRHandling();
            return;
        }
        while (running) {
            sleep(1);

            WorkerHandler workerHandler = getWorker();
            Task task = getTask();
            if (workerHandler != null && task != null) {
                logger.write("worker " + workerHandler.getId() + " and task " + task.getId() + " choose");
                workerHandler.setTask2Worker(task);
                removeTask(task.getId());

            }

        }
    }

    private Task getTask() {
        switch (alg) {
            case 1:
                return FCFS();
            case 2:
                return RR();
            case 3:
                return SJF();
        }
        return null;
    }

    private synchronized Task SJF() {
        if (tasks.size() == 0) {
            return null;
        }
        Task t = tasks.get(0);
        int time = tasks.get(0).getTimeSum();
        for (int j = 1; j < tasks.size(); j++) {
            if (time > tasks.get(j).getTimeSum()) {
                t = tasks.get(j);
                time = tasks.get(j).getTimeSum();
            }
        }
        return t;
    }

    private synchronized Task RR() {
        return FCFS();
    }

    private synchronized Task FCFS() {
        if (tasks.size() == 0) return null;
        return tasks.get(0);
    }

    private WorkerHandler getWorker() {
        for (WorkerHandler workerHandler : workerHandlers) {
            if (workerHandler.getTask() == null) {
                return workerHandler;
            }
        }
        return null;
    }


    public synchronized void TaskFinished(Task task) {
        System.out.println("task " + task.getId() + " executed successfully with result " + task.getRes());
        taskDone++;
        if (taskDone == taskNum) {
            System.exit(0);
        }
    }

    private void removeTask(int id) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() == id) {
                tasks.remove(i);
                break;
            }
        }
    }


    private void connect2Storage() {
        Socket socket = null;
        try {
            socket = new Socket(InetAddress.getLocalHost(), storegPort);
            storageHandler = new StorageHandler(socket);
            logger.write("server conect to storage with port : " + storegPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void waiteForWorkerConnection() throws IOException {
        int x = 0;
        while (x < workerNumbers) {
            try {
                clientSocket = this.serverSocket.accept();
                int id = x;
                WorkerHandler workerHandler = new WorkerHandler(id, clientSocket, this);
                workerHandlers.add(workerHandler);
                logger.write("a worker connect to server " + x);
            } catch (IOException e) {
                e.printStackTrace();
            }
            x++;
        }
    }

    private void sleep(int x) {
        try {
            Thread.sleep(x);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            serverSocket.close();
            logger.write("server closed!!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.running = false;
    }

    private void establishServer() throws IOException {
        serverSocket = new ServerSocket(port);
        logger.write("server established");
    }

    public void start() {
        try {
            establishServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        running = true;
        Thread thread = new Thread(this);
        thread.start();
    }

    public synchronized void addTask(Task task) {
        tasks.add(task);
    }

    public synchronized void getTaskFromWorker(int taskID) throws IOException {
        logger.write("a task time finished with id : " + taskID);
        for (WorkerHandler workerHandler : workerHandlers) {
            if (workerHandler.getTask() != null && workerHandler.getTask().getId() == taskID) {
                workerHandler.sendRequest("intrupt " + workerHandler.getTask().getId());
                logger.write("intrupt send to worker " + workerHandler.getId() + "for task " + taskID);
                break;
            }
        }
    }
}