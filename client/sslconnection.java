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

    public sslconnection(String host_name, int port){
        try {
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            this.socket = (SSLSocket) sslsocketfactory.createSocket(host_name, port);
            //These streams are used for transferring raw bytes
            this.byteIn = new BufferedInputStream(this.socket.getInputStream());
            this.byteOut = new BufferedOutputStream(this.socket.getOutputStream());
            //These streams are used for tansferring string messages
            this.messageIn = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.messageOut = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
