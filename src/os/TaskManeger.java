package os;

import java.util.ArrayList;

public  abstract  class TaskManeger {

    private ArrayList<Task> tasks;
    private int taskNum;

    public TaskManeger(ArrayList<Task> tasks){
        this.tasks=tasks;
        taskNum= tasks.size();


}


    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    public int getTaskNum() {
        return taskNum;
    }

    public void setTaskNum(int taskNum) {
        this.taskNum = taskNum;
    }

    public abstract Task getTask();


}
