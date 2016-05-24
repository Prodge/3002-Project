import java.util.*;
import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import org.json.simple.*;
import org.json.simple.parser.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class client{

    static final String USAGE_DOC =
        "Usage: java client [-arg PARAM(S) -arg PARAM(S) ...]\n"
        +"    -a FILENAME                 add or replace a file on the oldtrusty server\n\n"
        +"    -c NUMBER                   provide the required circumference (length) of a circle of trust\n\n"
        +"    -f FILENAME                 fetch an existing file from the oldtrusty server\n\n"
        +"    -h HOSTNAME:PORT            provide the remote address hosting the oldtrusty server\n\n"
        +"    -l                          list all stored files and how they are protected\n\n"
        +"    -n NAME                     require a circle of trust to involve the named person (i.e. their certificate)\n\n"
        +"    -u CERTIFICATE              upload a certificate to the oldtrusty server\n\n"
        +"    -v FILENAME CERTIFICATE     vouch for the authenticity of an existing file in the\n"
        +"                                oldtrusty server using the indicated certificate";

    static List<String> ARGS_PARAMS = new ArrayList<String>();
    static List<String> set_success_msg = new ArrayList<String>();
    static String HOSTNAME = "188.166.215.84";
    static int PORT = 3000;

    private static String generateHeader(List<String> value_list){
        HashMap<String, String> dictionary = new HashMap<String, String>();
        dictionary.put("Operation", value_list.get(0));
        if (!value_list.get(0).equals("list")){
            dictionary.put("filename", value_list.get(1));
            if (value_list.get(0).equals("add") || value_list.get(0).equals("cert")){
                dictionary.put("file_size", value_list.get(2));
            }else if (value_list.get(0).equals("vouch")){
                dictionary.put("certname", value_list.get(2));
            }
        }
        JSONObject obj = new JSONObject(dictionary);
        return (obj.toString());
    }

    private static JSONObject parseHeader(String header){
        JSONParser jParser = new JSONParser();
        JSONObject obj = null;
        try{
            obj = (JSONObject) jParser.parse(header);
        }catch(Exception e){
            System.out.println("Failed to parse!");
        }
        return obj;
    }

    private static boolean fileExists(String filename){
        boolean exists = Files.isRegularFile(Paths.get(filename));
        System.out.println(exists ? "File exists" : "Please check filename");
        return exists;
    }

    private static void addOrReplaceFile(String filename){
        if (!fileExists(filename)) return;
        sslconnection cdoi = new sslconnection(HOSTNAME, PORT);
        cdoi.sendMessageToServer(generateHeader(
                    Arrays.asList("add", filename, String.valueOf(new File(filename).length())))
        );
        if (cdoi.receiveMessageFromServer().equals("ready to receive")) cdoi.sendFileToServer(filename);
        String response = cdoi.receiveMessageFromServer();
        cdoi.closeConnection();
    }

    private static void setLengthOfTrust(int length){
        set_success_msg.add("Not yet implemented");
    }

    private static void getExistingFile(String filename){
        sslconnection cdoi = new sslconnection(HOSTNAME, PORT);
        cdoi.sendMessageToServer(generateHeader(
                    Arrays.asList("fetch", filename))
        );
        JSONObject response_header = parseHeader(cdoi.receiveMessageFromServer());
        if (((String) response_header.get("exists")).equals("True")) cdoi.receiveFileFromServer((Integer) response_header.get("file_size"));
        cdoi.sendMessageToServer("200 OK");
        cdoi.closeConnection();
    }

    private static void setHostAddress(String host_name, int port){
        HOSTNAME = host_name;
        PORT = port;
        set_success_msg.add("Hostname and port set!");
    }

    private static void getFileListInfo(){
        sslconnection cdoi = new sslconnection(HOSTNAME, PORT);
        cdoi.sendMessageToServer(generateHeader(
                    Arrays.asList("list"))
        );
        System.out.println(parseHeader(cdoi.receiveMessageFromServer().toString()));
        cdoi.closeConnection();
    }

    private static void setTrustName(String name){
        set_success_msg.add("Not yet implemented");
    }

    private static void uploadCertificate(String certificate){
        if (!fileExists(certificate)) return;
        sslconnection cdoi = new sslconnection(HOSTNAME, PORT);
        cdoi.sendMessageToServer(generateHeader(
                    Arrays.asList("cert", certificate, String.valueOf(new File(certificate).length())))
        );
        if (cdoi.receiveMessageFromServer().equals("ready to receive")) cdoi.sendFileToServer(certificate);
        String response = cdoi.receiveMessageFromServer();
        cdoi.closeConnection();
    }

    private static void checkAuthenticity(String filename, String certificate){
        sslconnection cdoi = new sslconnection(HOSTNAME, PORT);
        cdoi.sendMessageToServer(generateHeader(
                    Arrays.asList("vouch", filename, certificate)
        ));
        String response = cdoi.receiveMessageFromServer();
        cdoi.closeConnection();
    }

    private static boolean validInteger(String num){
        try {
            Integer.parseInt(num);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }

    private static boolean validString(String str){
        if (str.contains("-") && str.length()==2) return false;
        return (str.length()>1);
    }

    private static boolean parseArguments(String[] args){
        List<String> string_args = Arrays.asList("-a", "-f", "-n", "-u", "-c");
        for (int i=0; i<args.length; i++){
            if (string_args.contains(args[i]) && i!=args.length-1 ){
                if (args[i].equals("-c")){
                    if (!validInteger(args[i+1])) return false;
                }else{
                    if (!validString(args[i+1])) return false;
                }
            }else if (args[i].equals("-v") && i!=args.length-2){
                if (!(validString(args[i+1]) && validString(args[i+2]))) return false;
            }else if (args[i].equals("-h") && i!=args.length-1){
                if (args[i+1].endsWith(":") || !args[i+1].contains(":")) return false;
                String[] data = args[i+1].split(":");
                if (!(validString(data[0]) && validInteger(data[1]))) return false;
            }else if (args[i].equals("-l")){
                ARGS_PARAMS.add(args[i]);
                continue;
            }else{
                return false;
            }
            ARGS_PARAMS.add(args[i]);
            if (args[i].equals("-v")) ARGS_PARAMS.add(args[++i]);
            ARGS_PARAMS.add(args[++i]);
        }
        return true;
    };

    public static void main(String[] args) {
        if (!parseArguments(args)){
            System.out.println("Invalid argument. PLEASE CHECK USAGE BELOW...\n" + USAGE_DOC);
            System.exit(0);
        }
        String[] set_args = new String[] {"-c","-h","-n"};
        for (String arg : set_args){
            int index = ARGS_PARAMS.indexOf(arg);
            if (index == -1) continue;
            if (arg.equals("-h")){
                String[] data = ARGS_PARAMS.get(index+1).split(":");
                setHostAddress(data[0], Integer.parseInt(data[1]));
            }else if (arg.equals("-c")){ setLengthOfTrust(Integer.parseInt(ARGS_PARAMS.get(index+1)));
            }else if (arg.equals("-n")){ setTrustName(ARGS_PARAMS.get(index+1));}
        }
        if ((set_success_msg.size()*2) == ARGS_PARAMS.size()){
            System.out.println("Please use an operation");
            System.exit(0);
        }
        set_success_msg.forEach(System.out::println);
        for(int i=0; i<ARGS_PARAMS.size(); i++){
            switch (ARGS_PARAMS.get(i)){
                case "-a":
                    addOrReplaceFile(ARGS_PARAMS.get(++i));
                    break;
                case "-f":
                    getExistingFile(ARGS_PARAMS.get(++i));
                    break;
                case "-l":
                    getFileListInfo();
                    break;
                case "-u":
                    uploadCertificate(ARGS_PARAMS.get(++i));
                    break;
                case "-v":
                    checkAuthenticity(ARGS_PARAMS.get(++i), ARGS_PARAMS.get(++i));
                    break;
            }
        }
    }
}
