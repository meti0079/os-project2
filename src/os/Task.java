package os;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class Task {
    private ArrayList<String> indexes;
    private ArrayList<String> times;
    private int res;

    public Task(String task) {
        indexes = new ArrayList<>();
        times = new ArrayList<>();
        String[] taskSplit = task.split(" ");
        for (int i = 0; i < taskSplit.length; i++) {
            if (i % 2 == 0) {
                times.add(taskSplit[i]);
            } else {
                indexes.add(taskSplit[i]);
            }
        }
        res = 0;
    }

    public String[] getTask() {
        String[] s = new String[2];
        s[0] = times.remove(0);
        s[1] = indexes.remove(0);
        return s;
    }

    public boolean idDone(){
        return times.isEmpty();
    }

    public int getAnswer(){
        return res;
    }
    public void miniTaskDone(int x){
        res+=x;
    }




}
