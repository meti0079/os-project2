package os;

import java.util.ArrayList;
import java.util.LinkedList;

public class SJF extends TaskManeger{
    LinkedList<Task> sorted;
    public SJF(ArrayList<Task> tasks) {
        super(tasks);
        sorted=new LinkedList<>();
        sortTask(sorted, (ArrayList<Task>) tasks.clone());
    }

    private void sortTask(LinkedList<Task> sorted, ArrayList<Task> tasks) {
        Task t=null;
        for (int i = 0; i < getTaskNum(); i++) {
            t=tasks.get(0);
            int time=Integer.MAX_VALUE;
            for (int j = 0; j < tasks.size(); j++) {
                if (time>tasks.get(j).getTimeSum()){
                    t=tasks.get(j);
                    time=tasks.get(j).getTimeSum();
                }
            }
            sorted.addLast(t);
            tasks.remove(t);
        }
    }

    @Override
    public Task getTask() {
        return sorted.peekFirst();
    }
}
