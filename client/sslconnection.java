import java.util.*;
import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class sslconnection{

    private SSLSocket socket = null;
    private BufferedInputStream byteIn = null;
    private BufferedOutputStream byteOut = null;
    private BufferedReader messageIn = null;
    private BufferedWriter messageOut = null;

    private String trustStorefile = "clientcert";
    private String password = "123456";
    private static LoggerOutput log = client.log;

    public sslconnection(String host_name, int port){
        log.startLogMethod();
        try {
            //set trust store
            if (System.getProperty("javax.net.ssl.trustStore") == null) System.setProperty("javax.net.ssl.trustStore", this.trustStorefile);
            if (System.getProperty("javax.net.ssl.trustStorePassword") == null) System.setProperty("javax.net.ssl.trustStorePassword", this.password);
            //System.setProperty("javax.net.debug", "all"); //debugs

            //create connection
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            this.socket = (SSLSocket) sslsocketfactory.createSocket(host_name, port);

            //These streams are used for transferring raw bytes
            this.byteIn = new BufferedInputStream(this.socket.getInputStream());
            this.byteOut = new BufferedOutputStream(this.socket.getOutputStream());

            //These streams are used for tansferring string messages
            this.messageIn = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.messageOut = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        } catch (Exception e) {
            printExceptionAndExit(e);
        }
        log.endLogMethod("Estabilished connection with server");
    }

    private void printExceptionAndExit(Exception e){
        log.error(Thread.currentThread().getStackTrace()[2].getMethodName(), e.toString());
        System.exit(0);
    }

    public void sendMessageToServer(String msg){
        log.startLogMethod();
        try{
            this.messageOut.write(msg);
            this.messageOut.flush();
        }catch (Exception e){
            printExceptionAndExit(e);
        }
        log.endLogMethod("Sent message to server");
    }

    public String receiveMessageFromServer(){
        log.startLogMethod();
        String msg = "";
        int value;
        try{
            while((value = this.messageIn.read()) != 0) msg += Character.toString((char)value);
        }catch(Exception e){
            printExceptionAndExit(e);
        }
        log.endLogMethod("Recieved message of length " + msg.length());
        return msg;
    }


    public void sendFileToServer(String filename){
        log.startLogMethod();
        BufferedInputStream file_contents = null;
        int next;
        try{
            file_contents = new BufferedInputStream(new FileInputStream(filename));
            while((next = file_contents.read()) != -1) this.byteOut.write(next);
            this.byteOut.flush();
        }catch(IOException e){
            printExceptionAndExit(e);
        }
        log.endLogMethod("Sent file to server");
    }

    public String receiveFileFromServer(long filesize, String save_to_file){
        log.startLogMethod();
        String file = "";
        try{
            FileOutputStream fos = null;
            if (!save_to_file.equals("")) fos = new FileOutputStream(save_to_file);
            long current_recieved_bytes = 0;
            while(filesize != current_recieved_bytes){
                int next = this.byteIn.read();
                if (!save_to_file.equals("")) {fos.write(next);}
                else {file += (char)next;}
                current_recieved_bytes++;
            }
            if (!save_to_file.equals("")) fos.close();
        }catch(IOException e){
            printExceptionAndExit(e);
        }
        log.endLogMethod("Recieved file from server");
        return file;
    }

    public void closeConnection(){
        log.startLogMethod();
        try{
            this.byteIn.close();
            this.byteOut.close();
            this.messageIn.close();
            this.messageOut.close();
            this.socket.close();
        }catch(IOException e){
            printExceptionAndExit(e);
        }
        log.endLogMethod("Closed connection to server");
    }


}
