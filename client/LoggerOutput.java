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
        String result = String.format("[%s] Operation %s started", DateFormat.getTimeInstance().format(OPERATION_BEGIN), OPERATION_NAME);
        if (DEBUG) System.err.println(result);
    }

    public void endLog(){
        long time_run = (new Date()).getTime() - OPERATION_BEGIN.getTime();
        String result = String.format("[%s] Operation %s finished in %dms", DateFormat.getTimeInstance().format(OPERATION_BEGIN), OPERATION_NAME, time_run);
        if (DEBUG) System.err.println(result);
    }

    public void startLogMethod(){
        METHOD_BEGIN = new Date();
    }

    public void endLogMethod(String msg){
        long time_run = (new Date()).getTime() - METHOD_BEGIN.getTime();
        String result = String.format("    -- %s in %dms", msg, time_run);
        if (DEBUG) System.err.println(result);
    }

    public void error(String method_name, String msg){
        String result = "";
        if (method_name.equals("")){ result = String.format("ERROR: %s", msg);}
        else { result = String.format("ERROR: Method %s %s",method_name, msg);}
        System.err.println(result + "\nClient terminated");
    }

    public void info(String msg){
        if (DEBUG) System.err.println("    -- " + msg);
    }

    public void success(String msg){
        if (DEBUG) System.err.println( msg);
    }
}
