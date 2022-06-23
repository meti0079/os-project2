package os;

import java.io.IOException;

public class Timer implements Runnable {

    private int timeSleep;
    private Server server;
    int taskID;
    public Timer(int x, int taskId,Server server){
        this.timeSleep=x;
        this.server=server;
        this.taskID=taskId;
        this.run();
    }


    @Override
    public void run() {
        try {
            Thread.sleep(timeSleep);
            server.getTaskFromWorker(taskID);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
