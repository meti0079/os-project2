package os;

import master.MasterMain;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Stack;

public class Server implements Runnable {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private int port;
    private int workerNumbers;
    private boolean running;
    private StorgeHandler storgeHandler;




    private ArrayList<WorkerHandler> workerHandlers;
    private ArrayList<Task> tasks;


    private ArrayList<Task> taskInProcesses;
    private ArrayList<Task> taskWaitForOther;
    private int pp=1;





    public Server(int port, int n, int programsNumber, int max_weight) {
        this.port = port;
        workerNumbers = n;
        workerHandlers = new ArrayList<>();
        tasks = new ArrayList<>();
        taskInProcesses =new ArrayList<>();
        taskWaitForOther =new ArrayList<>();
        System.out.println("server start on port "+port);

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
//                handleWorks();
        }
    }

    private void connect2Storge() {
        Socket socket= null;
        try {
            socket = new Socket(InetAddress.getLocalHost(),100);
            storgeHandler=new StorgeHandler(socket);

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
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.running = false;
    }



    private void establishServer() throws IOException {
        serverSocket = new ServerSocket(port);
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