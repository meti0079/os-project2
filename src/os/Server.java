package os;

import logMe.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

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
    private ArrayList<Task> taskHaveDeadlock;
    private Object lock;
    private boolean deadlockBool;

    public Server(int port, int n, ArrayList<String> data, ArrayList<Task> tasks, int storegPort, int RRTime, String alg, String deadlock) {
        lock = new Object();
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

        taskHaveDeadlock = new ArrayList<>();

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
        storageHandler.sendRequest(makeGraph());

    }

    private String makeGraph() {
        String s="graph "+taskNum+" "+data.size()+" ";
        for (int i = 0; i < taskNum; i++) {
            for (int j = 0; j < tasks.get(i).getIndexes().size(); j++) {
                s+=tasks.get(i).getIndexes().get(j)+"_";

            }
            s=s.substring(0,s.length()-1);
            s+="##";
        }
        s=s.substring(0,s.length()-2);
        logger.write("graph "+s);
        return s;
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
                        for (WorkerHandler workerHandler : workerHandlers) {
                            Task task = getTask();
                            if (task != null) {
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
//        handleTaskWithDeadlock();
        if (alg == 2) {
            RRHandling();
            return;
        }
        while (running) {
            sleep(1);
            WorkerHandler workerHandler = getWorker();
            Task task = null;
            if (workerHandler!=null)
             task= getTask();
            if (workerHandler != null && task != null) {
//                if (!haveDeadLock(task)) {
                    logger.write("worker " + workerHandler.getId() + " and task " + task.getId() + " choose");
                    workerHandler.setTask2Worker(task);
                    removeTask(task.getId());

//                } else {
//                    synchronized (taskHaveDeadlock) {
//                        taskHaveDeadlock.add(task);
//                        removeTask(task.getId());
//                        logger.write("task " + task.getId() + " have deadlock");
//                    }
//                }
            }


        }
    }

    private void handleTaskWithDeadlock() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    sleep(100);
                    if (!taskHaveDeadlock.isEmpty()) {
                        synchronized (taskHaveDeadlock) {
                            addTask(taskHaveDeadlock.remove(0));
                            logger.write("a task free");
                        }
                    }
                }
            }
        });
        thread.start();
    }

    private boolean haveDeadLock(Task task) {
        try {
            if (deadlock.equalsIgnoreCase("prevention")) {
                storageHandler.sendRequest("prevention " + task.getId() + " " + task.getTaskRes());
            } else if (deadlock.equalsIgnoreCase("detection")) {
                storageHandler.sendRequest("detection "+task.getId());
            } else {
                return false;
            }
            String res = storageHandler.listenForResponse();
            if (res.equalsIgnoreCase("false")) {
               logger.write("dont have deadlock");
                return false;
            } else{
                logger.write("have deadlock");
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
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
        if (!deadlockBool){
            Collections.sort(tasks);
            logger.write("sort now");
        }
        if (haveDeadLock(tasks.get(0))){
            tasks.add(tasks.remove(0));
            deadlockBool=true;
            return null;
        }
        deadlockBool=false;
        return tasks.get(0);
    }

    private synchronized Task RR() {
        return FCFS();
    }

    private synchronized Task FCFS() {
        if (tasks.size() == 0) return null;
        if (haveDeadLock(tasks.get(0))){
            tasks.add(tasks.remove(0));
            return null;
        }
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
        try {
            storageHandler.sendRequest("released " + task.getId());
            logger.write("released " + task.getId() + "  to storage");
        } catch (IOException e) {
            e.printStackTrace();
        }
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