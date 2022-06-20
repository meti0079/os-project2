package master;

import os.Task;
import os.Worker;
import os.Server;
import os.Storge;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class MasterMain {
    public static int port;
    public static int storegPort;
    public static int workerNumber;
    public static int RRTime;
    public static int workNumber;
    public static String alg;
    public static String deadlock;

    public static List<String> command;
    public static ArrayList<String> data;
    public static ArrayList<Task> tasks;
    private static ArrayList<Process> proccess;


    public static void main(String[] args) {
        proccess = new ArrayList<>();
        command = new LinkedList<>();
        tasks = new ArrayList<>();
        data = new ArrayList<>();

        readInputs();

        try {
            makeStorge();
            Thread.sleep(500);
            Server server = new Server(port, workerNumber, data, tasks, storegPort, RRTime, alg, deadlock);
            server.start();
            Thread.sleep(500);

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

    public static void makeStorge() {
        String className = Storge.class.getName();
        try {
            command.add(className);
            command.add(String.valueOf(storegPort));
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
        String className = Worker.class.getName();
        try {
            command.remove(command.size() - 1);
            command.remove(command.size() - 1);
            command.add(className);
            command.add(String.valueOf(storegPort));
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


    public static void readInputs() {
        Scanner scanner = new Scanner(System.in);
        int argNum = Integer.parseInt(scanner.nextLine());
        for (int i = 0; i < argNum; i++) {
            command.add(scanner.nextLine());
        }

        port = Integer.parseInt(scanner.nextLine());
        workerNumber = Integer.parseInt(scanner.nextLine());
        alg = scanner.nextLine();
        if (alg.equalsIgnoreCase("RR")) RRTime = Integer.parseInt(scanner.nextLine());
        deadlock = scanner.nextLine();
        storegPort = Integer.parseInt(scanner.nextLine());
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