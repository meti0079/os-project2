package master;

import os.Worker;
import os.Server;
import os.Storge;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class MasterMain {
    public static int port;
    public static int workerNumber;
    public static int maxWeight;
    public static int programNumber;
    public static ArrayList<String> command;
    public static ArrayList<String> programNames;
    public static ArrayList<Integer> programWeights;


    public static void main(String[] args) {
        try {
            FileWriter writer= new FileWriter("log.txt",true);
                writer.write("start");
                writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        command = new ArrayList<>();
        programWeights = new ArrayList<>();
        programNames = new ArrayList<>();
        System.out.println("here");
        readInputs();
        Server server = new Server(port, workerNumber, programNumber, maxWeight);
        server.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        makeCache();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < workerNumber; i++) {
            makeWorker();
        }
    }


    public static void makeCache() {
        String className= Storge.class.getName();
        try {
            System.out.println(command);
            ProcessBuilder builder = new ProcessBuilder(String.join(" ",command) , className);
            Process process = builder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void makeWorker() {
        String className= Worker.class.getName();
        try {
            System.out.println(command);
            ProcessBuilder builder = new ProcessBuilder(String.join(" ",command) , className);
            Process process = builder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public static void readInputs() {
        Scanner scanner = new Scanner(System.in);
        port = Integer.parseInt(scanner.nextLine());
        workerNumber = Integer.parseInt(scanner.nextLine());
        maxWeight = Integer.parseInt(scanner.nextLine());
        int argNum = Integer.parseInt(scanner.nextLine());
        for (int i = 0; i < argNum; i++) {
            command.add(scanner.nextLine());
        }
        programNumber = Integer.parseInt(scanner.nextLine());
        for (int i = 0; i < programNumber; i++) {
            String[] x = scanner.nextLine().split(" ");
            programNames.add(x[0]);
            programWeights.add(Integer.parseInt(x[1]));
        }
//        System.out.println();

    }
}
