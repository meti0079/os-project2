package logMe;

import java.io.FileWriter;
import java.io.IOException;

public class Logger {
    private FileWriter logger;





    public Logger(String name){
        try {
            logger=new FileWriter("."+name+".log");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String message){
        try {
            logger.write(message);
            logger.write("\n");
            logger.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

























}
