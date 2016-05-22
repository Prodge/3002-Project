import java.util.*;
import java.net.*;
import java.io.*;
import javax.net.ssl.*;

public class sslconnection{

    private SSLSocket socket;
    private BufferedInputStream byteIn;
    private BufferedOutputStream byteOut;
    private BufferedReader messageIn;
    private PrintWriter messageOut;

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
            this.messageOut = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));

            System.out.println("Connection estabilished!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToServer(String msg){
        System.out.println("Sending message to server...");
        try{
            this.messageOut.println(msg);
            this.messageOut.flush();
            System.out.println("Message sent!");
        }catch (Exception e){
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
