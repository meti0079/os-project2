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

    private StorgeHandler storgeHandler;

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
        if (alg.equalsIgnoreCase("RR")) this.alg = 1;
        if (alg.equalsIgnoreCase("SJF")) this.alg = 1;


        taskInProcesses = new ArrayList<>();


        System.out.println("server start on port " + port);
        logger = new Logger("server");
        logger.write("server start on port " + port);

    }


    @Override
    public void run() {
        try {
            waiteForWorkerConnection();
            connect2Storge();
            pushDataOnStorge();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (running) {
            handleWorks();
        }
    }

    private void pushDataOnStorge() throws IOException {
        for (int i = 0; i < taskNum; i++) {
            storgeHandler.sendRequest("push " + data.get(i));
        }
    }

    private void handleWorks() {
        sleep(1);
        WorkerHandler workerHandler = getWorker();
        Task task = getTask();
        if (workerHandler != null && task != null) {
            workerHandler.setTask2Worker(task);
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

    private Task SJF() {
        if (tasks.size() == 0) {
            return null;
        }
        Task t = tasks.get(0);
        int time = Integer.MAX_VALUE;
        for (int j = 1; j < tasks.size(); j++) {
            if (time > tasks.get(j).getTimeSum()) {
                t = tasks.get(j);
                time = tasks.get(j).getTimeSum();
            }
        }
        tasks.remove(t);
        return t;
    }

    private Task RR() {
        return null;
    }

    private Task FCFS() {
        return tasks.remove(0);
    }

    private WorkerHandler getWorker() {
        for (WorkerHandler workerHandler : workerHandlers) {
            if (workerHandler.getTask() == null) {
                return workerHandler;
            }
        }
        return null;
    }


    public void TaskFinished(Task task) {
        System.out.println("task " + task.getId() + " executed successfully with result " + task.getRes());
        taskDone++;
        if (taskDone == taskNum) {
            System.exit(0);
        }
    }


    private void connect2Storge() {
        Socket socket = null;
        try {
            socket = new Socket(InetAddress.getLocalHost(), storegPort);
            storgeHandler = new StorgeHandler(socket);
            logger.write("server conect to storge with port : " + storegPort);
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

    public void addTask(Task task) {
        tasks.add(task);
    }
}