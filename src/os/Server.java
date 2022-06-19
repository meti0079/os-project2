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

    private boolean running;

    private StorgeHandler storgeHandler;

    private String alg;
    private String deadlock;



    private ArrayList<WorkerHandler> workerHandlers;
    private ArrayList<Task> tasks;
    private ArrayList<String> data;

    private ArrayList<Task> taskInProcesses;
    private ArrayList<Task> taskFinished;
    private int taskNum;





    public Server(int port, int n,ArrayList<String> data , ArrayList<Task> tasks,int storegPort,int RRTime,String alg,String deadlock) {
       taskNum=tasks.size();
        this.data=data;
        this.port = port;
        this.storegPort=storegPort;
        workerNumbers = n;
        workerHandlers = new ArrayList<>();
        this.tasks = tasks;
        this.RRTime=RRTime;
        this.deadlock=deadlock;
        this.alg=alg;

        taskInProcesses =new ArrayList<>();
        taskFinished =new ArrayList<>();


        System.out.println("server start on port "+port);
        logger =new Logger("server");
        logger.write("server start on port "+port);

    }


    @Override
    public void run() {
        try {
            waiteForWorkerConnection();
            connect2Storge();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (running) {
                handleWorks();
        }
    }

    private void handleWorks() {






    }



    public void TaskFinished(Task task){
        System.out.println("task "+task.getId()+" executed successfully with result "+task.getRes());
        taskFinished.add(task);
        if (taskFinished.size()==taskNum){
            close();
            System.exit(0);
        }

    }




    private void connect2Storge() {
        Socket socket= null;
        try {
            socket = new Socket(InetAddress.getLocalHost(),storegPort);
            storgeHandler=new StorgeHandler(socket);
            logger.write("server conect to storge");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void waiteForWorkerConnection() throws IOException {
        int x=0;
        while (x<workerNumbers) {
            try {
                clientSocket = this.serverSocket.accept();
                int id =x;
                WorkerHandler workerHandler = new WorkerHandler(id, clientSocket, this);
                workerHandlers.add(workerHandler);
                logger.write("a worker connect to server "+x);
            } catch (IOException e) {
                clientSocket.close();
                e.printStackTrace();
            }
            x++;
        }
    }



    private void sleep() {
        try {
            Thread.sleep(1000);
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


}