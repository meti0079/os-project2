package os;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class WorkerHandler {
    boolean running;
    private int id;
    private Socket clientSocket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Task task;
    Server server;


    public WorkerHandler(int id, Socket clientSocket, Server server) throws IOException {
        this.id = id;
        running=true;
        this.clientSocket = clientSocket;
        this.dis = new DataInputStream(clientSocket.getInputStream());
        this.dos = new DataOutputStream(clientSocket.getOutputStream());
        System.out.println(String.format("worker %d start",id));
        listenForRes();
        this.server=server;
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

    public void setTask2Worker(Task task){
        String req= "TASK "+task.getId()+" "+task.getTaskString();
        this.task=task;
        try {
            sendRequest(req);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRequest(String request) throws IOException {
        dos.writeUTF(request);
    }

    public String listenForResponse() throws IOException {
        return dis.readUTF();
    }

    private void handleRes(String res){
        String [] response=res.split(" ");
        if (response[0].equalsIgnoreCase("response")){
            task.setRes(Integer.parseInt(response[1]));
            server.TaskFinished(task);
            task=null;

        }else if (response[0].equalsIgnoreCase("taskUnFinished")){
            int taskId= task.getId();
            server.addTask(new Task(changeTask2String(response),taskId));
            task=null;
        }
    }


    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Task getTask(){
        return task;
    }
    private String changeTask2String(String [] res){
        String ss="";
        for (int i = 1; i < res.length ; i++) {
            if (i%2==1){
                ss+=res[i];
            }else {
                ss+=(" "+res[i]);
            }
        }
        System.out.println("changTask2String : "+ss);
        return ss;
    }


}
