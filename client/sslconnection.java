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

    public sslconnection(String host_name, int port){
        try {
            //set trust store
            System.setProperty("javax.net.ssl.trustStore", this.trustStorefile);
            System.setProperty("javax.net.ssl.trustStorePassword", this.password);
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

            System.out.println("Connection estabilished!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToServer(String msg){
        System.out.println("Sending message to server...");
        try{
            this.messageOut.write(msg);
            this.messageOut.flush();
            System.out.println("Message sent!");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String receiveMessageFromServer(){
        String msg = "";
        int value;
        try{
            while((value = this.messageIn.read()) != 0) msg += Character.toString((char)value);
        }catch(Exception e){
            e.printStackTrace();
        }
        return msg;
    }
    

    public void sendFileToServer(String filename){
        System.out.println("Starting to send file");
        BufferedInputStream file_contents = null;
        int next;
        try{
            file_contents = new BufferedInputStream(new FileInputStream(filename));
            while((next = file_contents.read()) != -1) this.byteOut.write(next);
            this.byteOut.flush();
            System.out.println("File sent successfully!");
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void receiveFileFromServer(){
        System.out.println("Starting to recieve file");
        try{
            System.out.println("------------Start of file-------------\n");
            int next = 0;
            while((next = this.byteIn.read()) != -1){
                System.out.print((char)next);
            }
            System.out.println("\n------------End of file-------------\nFile recieved successfully!");
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void closeConnection(){
        try{
            this.byteIn.close();
            this.byteOut.close();
            this.messageIn.close();
            this.messageOut.close();
            this.socket.close();
            System.out.println("Connection closed!");
        }catch(IOException e){
            e.printStackTrace();
        }
    }


}
