package os;

import java.util.*;

public class Task implements Comparable<Task>{
    private LinkedList<String> indexes;
    private LinkedList<String> times;
    private String task;
    private int res;
    private int id;
    private int timeSum;
    private String lastIndex;
    private String lastTime;
    private Integer lastData;

    public Task(String task, int id) {
        this.task = task;
        this.id = id;
        indexes = new LinkedList<>();
        times = new LinkedList<>();
        String[] taskSplit = task.split(" ");
        for (int i = 0; i < taskSplit.length; i++) {
            if (i % 2 == 0) {
                times.addLast(taskSplit[i]);
                timeSum += Integer.parseInt(taskSplit[i]);
            } else {
                indexes.addLast(taskSplit[i]);
            }
        }
        res = 0;
    }

    public String[] getSubTask() {
        String[] s = new String[2];
        s[0] = times.pollFirst();
        s[1] = indexes.pollFirst();
        lastIndex = s[1];
        lastTime = s[0];
        return s;
    }

    public boolean idDone() {
        return times.isEmpty();
    }

    public void miniTaskDone(int x) {
        res += x;
    }


    public LinkedList<String> getIndexes() {
        return indexes;
    }


    public LinkedList<String> getTimes() {
        return times;
    }

    public int getRes() {
        return res;
    }

    public void setRes(int res) {
        this.res = res;
    }

    public int getId() {
        return id;
    }

    public int getTimeSum() {
        return timeSum;
    }

    public void setTimeSum(int timeSum) {
        this.timeSum = timeSum;
    }

    public String getTaskString() {
        String x = "";
        for (int i = 0; i < indexes.size(); i++) {
            x += times.get(i);
            x += (" " + indexes.get(i) + " ");
        }
        x += id + " " + res;
        return x;
    }

    public String getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(String lastIndex) {
        this.lastIndex = lastIndex;
    }


    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public void intruptInSleep(String lastTime) {
        times.addFirst(lastTime);
        indexes.addFirst(lastIndex);
    }

    public Integer getLastData() {
        return lastData;
    }

    public void setLastData(Integer lastData) {
        this.lastData = lastData;
    }

    public String getTaskRes() {
        String s = "";
        for (String s1 : indexes) {
            s += s1 + " ";
        }
        return s.substring(0, s.length() - 1);
    }




    @Override
    public int compareTo(Task o) {
        return this.timeSum-o.timeSum;
    }
}
