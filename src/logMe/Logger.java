package logMe;

import java.io.*;
import java.sql.Timestamp;

public class Logger {
    private PrintStream writer;
    private final String addres = "log.txt";
    private static Logger logger;
    private String name;


    private Logger() {
        try {
            writer = new PrintStream(new FileOutputStream(addres, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Logger getInstance() {
        if (logger == null) logger = new Logger();
        return logger;
    }

    public void setName(String name) {
        logger.name = name;
    }


    public synchronized void write(String message) {
        logger.writer.print(logger.name + " : " + message + "///time : " + new Timestamp(System.currentTimeMillis()).toString() + "\n");
        logger.writer.flush();
    }

    public void clear() {
        try {
            PrintWriter printWriter = new PrintWriter(addres);
            printWriter.print("");
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


}
