package os;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class WorkerHandler {
    boolean running;
    private int id;
    private Socket clientSocket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private ArrayList<Task> tasks;
    private ArrayList<Integer> workW;
    private String state;
    Server server;


    public WorkerHandler(int id, Socket clientSocket, Server server) throws IOException {
        this.id = id;
        state="ALIVE";
        running=true;
        this.clientSocket = clientSocket;
        this.dis = new DataInputStream(clientSocket.getInputStream());
        this.dos = new DataOutputStream(clientSocket.getOutputStream());
        workW=new ArrayList<>();
        tasks =new ArrayList<>();
        System.out.println(String.format("worker %d start",id));
        listenForRes();
    }
    private void listenForRes(){
        Thread thread= new Thread(new Runnable() {
            @Override
            public void run() {
                while (running){
                    try {
                        handleRes(listenForResponse());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }


    public void setWork(Task task, int w){
//        String req= task.getProgram2done()+" "+ task.getInput()+ " "+ task.getPriority();
//        tasks.add(task);
//        weightworker+=w;
//        workW.add(w);
//        try {
//            sendRequest(req);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    private void sendRequest(String request) throws IOException {
        dos.writeUTF(request);
    }

    private String listenForResponse() throws IOException {
        return dis.readUTF();
    }

    private void handleRes(String res){
        String [] response=res.split(" ");

    }


    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Task> getWorks() {
        return tasks;
    }

    public boolean checkWorkerIsAlive() {
        try {
            if(dis.read()==-1){
             return   false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}