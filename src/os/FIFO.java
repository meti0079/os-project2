package os;

import java.util.ArrayList;

public class FIFO extends TaskManeger{
    int index;
    public FIFO(ArrayList<Task> tasks) {
        super(tasks);
    }


    @Override
    public Task getTask() {
        Task task=this.getTasks().get(index);
        index++;
        return task;
    }
}
