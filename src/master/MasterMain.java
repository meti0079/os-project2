package master;

import logMe.Logger;
import os.Storage;
import os.Task;
import os.Worker;
import os.Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class MasterMain {
    public static int port;
    public static int storagePort;
    public static int workerNumber;
    public static int RRTime;
    public static int workNumber;
    public static String alg;
    public static String deadlock;
    public static List<String> command;
    public static ArrayList<String> data;
    public static ArrayList<Task> tasks;
    private static ArrayList<Process> proccess;


    public static void main(String[] args) throws FileNotFoundException {
        Logger.getInstance().clear();
        Logger.getInstance().setName("master");
        proccess = new ArrayList<>();
        command = new LinkedList<>();
        tasks = new ArrayList<>();
        data = new ArrayList<>();

        readInputs();

        try {
            makeStorage();
            Thread.sleep(500);
            Server server = new Server(port, workerNumber, data, tasks, storagePort, RRTime, alg, deadlock);
            server.start();
            Thread.sleep(1000);
            command.remove(command.size() - 1);
            command.remove(command.size() - 1);
            command.remove(command.size() - 1);
            String className = Worker.class.getName();
            command.add(className);
            command.add(String.valueOf(storagePort));
            command.add(String.valueOf(port));
            for (int i = 0; i < workerNumber; i++) {
                makeWorker();
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                server.close();
                proccess.forEach(process -> process.destroy());
            }));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void makeStorage() {
        String className = Storage.class.getName();
        try {
            command.add(className);
            command.add(String.valueOf(storagePort));
            command.add(String.valueOf(workerNumber));
            System.out.println(command);
            ProcessBuilder builder = new ProcessBuilder(command);
            Process process = builder.start();
            proccess.add(process);
            Scanner scanner = new Scanner(process.getErrorStream());
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        System.out.println(scanner.nextLine());
                    }
                }
            });
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void makeWorker() {
        try {


            System.out.println(command);
            ProcessBuilder builder = new ProcessBuilder(command);
            Process process = builder.start();
            proccess.add(process);
            Scanner scanner = new Scanner(process.getErrorStream());
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        System.out.println(scanner.nextLine());
                    }
                }
            });
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void readInputs() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File("C:\\Users\\Mehdi\\IdeaProjects\\OS_HW2\\input11.txt"));
        int argNum = Integer.parseInt(scanner.nextLine());
        for (int i = 0; i < argNum; i++) {
            command.add(scanner.nextLine());
        }

        port = Integer.parseInt(scanner.nextLine());
        workerNumber = Integer.parseInt(scanner.nextLine());
        alg = scanner.nextLine();
        if (alg.equalsIgnoreCase("RR")) RRTime = Integer.parseInt(scanner.nextLine());
        deadlock = scanner.nextLine();
        storagePort = Integer.parseInt(scanner.nextLine());
        String d = scanner.nextLine();
        String ds[] = d.split(" ");
        for (String s : ds) {
            data.add(s);
        }
        workNumber = Integer.parseInt(scanner.nextLine());
        for (int i = 0; i < workNumber; i++) {
            tasks.add(new Task(scanner.nextLine(), i));
        }
    }
}
