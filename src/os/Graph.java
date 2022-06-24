package os;

import java.util.ArrayList;
import java.util.List;


public class Graph {
    private List<List<Integer>> adjacencyList;
    private int tasknum;
    private int resourceNum;

    public Graph(int taskNum, int resourceNum) {
        this.adjacencyList = new ArrayList<>();
        this.resourceNum = resourceNum;
        this.tasknum = taskNum;
        for (int i = 0; i < resourceNum + taskNum; i++) {
            adjacencyList.add(new ArrayList<>());
        }
    }

    private int getTaskID(int task) {
        return task;
    }

    private int getresID(int res) {
        return res + tasknum;
    }

    public void getRes(int task, int res) {
        if (!adjacencyList.get(getTaskID(task)).contains(getresID(res))) {
            adjacencyList.get(getTaskID(task)).add(getresID(res));
        }
    }

    public void freeRes(int task) {
        for (List<Integer> arrayList : adjacencyList) {
            arrayList.remove(new Integer(getTaskID(task)));
        }
    }

    public void changeDirection(int task, int res) {
        adjacencyList.get(getTaskID(task)).remove(new Integer(getresID(res)));
        adjacencyList.get(getresID(res)).add(new Integer(getTaskID(task)));
    }

    public boolean hasCycle(int sourceVertex) {
        ArrayList<Integer> visited = new ArrayList<>();
        return dfs(sourceVertex, visited, sourceVertex);

    }

    private boolean dfs(int root, ArrayList<Integer> visited, int task) {
        visited.add(root);
        for (int i = 0; i < adjacencyList.get(root).size(); i++) {
            int nextRoot = adjacencyList.get(root).get(i);
            if (visited.contains(nextRoot)) {
                if (nextRoot == task)
                    return true;
            } else {
                if (dfs(nextRoot, visited, task)) {
                    return true;
                }
            }
        }
        return false;
    }
}
