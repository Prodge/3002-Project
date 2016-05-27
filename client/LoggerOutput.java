import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;

public class LoggerOutput{

    private Date CLASS_START;
    public boolean DEBUG;
    private String OPERATION_NAME;
    private Date OPERATION_BEGIN;
    private Date METHOD_BEGIN;

    public LoggerOutput(boolean debug){
        CLASS_START = new Date();
        DEBUG = debug;
    }

    public void startLog(String name){
        OPERATION_NAME = name;
        OPERATION_BEGIN = new Date();
        if (DEBUG) System.err.printf("[%s] Operation %s started\n", DateFormat.getTimeInstance().format(OPERATION_BEGIN), OPERATION_NAME);
    }

    public void endLog(){
        long time_run = (new Date()).getTime() - OPERATION_BEGIN.getTime();
        if (DEBUG) System.err.printf("[%s] Operation %s finished in %dms\n", DateFormat.getTimeInstance().format(new Date()), OPERATION_NAME, time_run);
    }

    public void startLogMethod(){
        METHOD_BEGIN = new Date();
    }

    public void endLogMethod(String msg){
        long time_run = (new Date()).getTime() - METHOD_BEGIN.getTime();
        if (DEBUG) System.err.printf("    -- %s in %dms\n", msg, time_run);
    }

    public void error(String method_name, String msg){
        String result = "";
        if (method_name.equals("")){
            result = String.format("ERROR: %s", msg);
        }else{
            result = String.format("ERROR: Method %s %s",method_name, msg);
        }
        System.err.println(result + "\nClient terminated");
    }

    public void info(String msg){
        if (DEBUG) System.err.println("    -- " + msg);
    }

    public void success(String msg){
        if (DEBUG) System.err.println(msg);
    }

    public void endLoggerOutput(){
        long time_run = (new Date()).getTime() - CLASS_START.getTime();
        if (DEBUG) System.err.printf("Client has run for %dms\n", time_run);
    }
}
