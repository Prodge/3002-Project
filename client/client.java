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
        +"    -a FILENAME                 add or replace a file on the oldtrusty server\n"
        +"    -c NUMBER                   provide the required circumference (length) of a circle of trust\n"
        +"    -f FILENAME                 fetch an existing file from the oldtrusty server\n"
        +"    -o FILENAME                 can only be used when -f is used to write fetched file to a file\n"
        +"    -h HOSTNAME:PORT            provide the remote address hosting the oldtrusty server\n"
        +"    -l                          list all stored files and how they are protected\n"
        +"    -n NAME                     require a circle of trust to involve the named person (i.e. their certificate)\n"
        +"    -u CERTIFICATE              upload a certificate to the oldtrusty server\n"
        +"    -v FILENAME CERTIFICATE     vouch for the authenticity of an existing file in the\n"
        +"                                oldtrusty server using the indicated certificate\n"
        +"    -k                          server will return a unique secret key for storing and recieving files\n"
        +"    -e KEY                      use the secret key given by the server to store and recieve files and list files linked to the key\n"
        +"    --verbose                   debugs the operations performed\n"
        +"    --help                      shows the this usage document";

    static List<String> ARGS_PARAMS = new ArrayList<String>();
    static List<String> SET_SUCCESS_MSG = new ArrayList<String>();
    static String HOSTNAME = "188.166.215.84";
    static int PORT = 3000;
    static String TRUST_NAME = "";
    static int TRUST_SIZE = 0;
    static String KEY = "";
    public static LoggerOutput log = null;

    private static String generateHeader(List<String> value_list){
        HashMap<String, String> dictionary = new HashMap<String, String>();
        dictionary.put("Operation", value_list.get(0));
        if (!value_list.get(0).equals("list") && !value_list.get(0).equals("get_key")){
            dictionary.put("filename", value_list.get(1));
            if (value_list.get(0).equals("add") || value_list.get(0).equals("cert")){
                dictionary.put("file_size", value_list.get(2));
            }else if (value_list.get(0).equals("vouch")){
                dictionary.put("certname", value_list.get(2));
            }else if (value_list.get(0).equals("fetch")){
                if (!value_list.get(2).equals("")) dictionary.put("cot_name", value_list.get(2));
                if (!value_list.get(3).equals("0")) dictionary.put("cot_size", value_list.get(3));
            }
        }
        if (!value_list.get(0).equals("cert") && !value_list.get(0).equals("vouch") && !value_list.get(0).equals("get_key")){
            if (!KEY.equals("")) dictionary.put("key", KEY);
        }
        JSONObject obj = new JSONObject(dictionary);
        return (obj.toString());
    }

    private static JSONArray parseFileList(String header){
        JSONParser jParser = new JSONParser();
        JSONArray arry = null;
        try{
            arry = (JSONArray) jParser.parse(header);
        }catch(Exception e){
            displayErrorAndExit(e,"");
        }
        return arry;
    }

    private static JSONObject parseHeader(String header){
        JSONParser jParser = new JSONParser();
        JSONObject obj = null;
        try{
            obj = (JSONObject) jParser.parse(header);
        }catch(Exception e){
            displayErrorAndExit(e,"");
        }
        return obj;
    }

    private static String parseFileName(String filename){
        return filename.substring(filename.lastIndexOf("/")+1);
    }

    private static int extractStatusCode(JSONObject header){
        return (((Long) header.get("status_code")).intValue());
    }

    private static String extractMessage(JSONObject header){
        return ((String) header.get("msg"));
    }

    private static Long extractFileSize(JSONObject header){
        return ((Long) header.get("file_size"));
    }

    private static void displayErrorAndExit(Exception e, String msg){
        log.error(Thread.currentThread().getStackTrace()[2].getMethodName(), msg.equals("") ? e.toString() : "the server threw the following error:\n   " + msg);
        System.exit(0);
    }

    private static String generateFileInfoLine(JSONObject obj){
        String result = "";
        JSONArray cert_list = (JSONArray) obj.get("certname");
        String certs = ""; boolean line_1 = true;
        if (cert_list.size()==0) result += String.format("%30s%50s%20s\n",obj.get("filename"),obj.get("cot_size"),obj.get("filesize"));
        for (int j=0; j<cert_list.size(); j++){
            String cert = (String) cert_list.get(j);
            if (certs.length()+cert.length()<=27 && j!=cert_list.size()-1){
                certs += cert + ",";
            }else{
                if (certs.length()+cert.length()<=27) certs += cert + (j==cert_list.size()-1 ? "" : ",");
                if (line_1){
                    result += String.format("%30s%30s%20s%20s\n",obj.get("filename"),certs,obj.get("cot_size"),obj.get("filesize"));
                    line_1 = false;
                }else{
                    result += String.format("%30s%30s\n"," ",certs);
                    certs = cert + ",";
                }
                if (!certs.contains(cert) && j==cert_list.size()-1) result += String.format("%30s%30s\n"," ",cert);
            }
        }
        return result;
    }

    private static String addOrReplaceFile(String filename){
        if (!Files.isRegularFile(Paths.get(filename))) return ("File does not exist");
        sslconnection cdoi = new sslconnection(HOSTNAME, PORT);
        cdoi.sendMessageToServer(generateHeader(
                    Arrays.asList("add", parseFileName(filename), String.valueOf(new File(filename).length()))
        ));
        JSONObject response = parseHeader(cdoi.receiveMessageFromServer());
        if (extractStatusCode(response) != 200) displayErrorAndExit(null,extractMessage(response));
        cdoi.sendFileToServer(filename);
        cdoi.closeConnection();
        return ("File added successfully");
    }

    private static void setLengthOfTrust(int length){
        TRUST_SIZE = length;
        SET_SUCCESS_MSG.add("Trust size set!");
    }

    private static String getExistingFile(String filename, String save_to_file){
        sslconnection cdoi = new sslconnection(HOSTNAME, PORT);
        cdoi.sendMessageToServer(generateHeader(
                    Arrays.asList("fetch", parseFileName(filename), TRUST_NAME, String.valueOf(TRUST_SIZE)))
        );
        JSONObject response = parseHeader(cdoi.receiveMessageFromServer());
        if (extractStatusCode(response) != 200) displayErrorAndExit(null,extractMessage(response));
        String file = cdoi.receiveFileFromServer(extractFileSize(response),save_to_file);
        cdoi.closeConnection();
        return file;
    }

    private static void setHostAddress(String host_name, int port){
        HOSTNAME = host_name;
        PORT = port;
        SET_SUCCESS_MSG.add("Hostname and port set!");
    }

    private static String getFileListInfo(){
        String result = "";
        sslconnection cdoi = new sslconnection(HOSTNAME, PORT);
        cdoi.sendMessageToServer(generateHeader(
                    Arrays.asList("list"))
        );
        JSONArray filelist = parseFileList(cdoi.receiveMessageFromServer());
        result = String.format("%30s%30s%20s%20s\n","FILE NAME", "CERTIFICATE NAME", "LENGTH OF COT", "FILE SIZE(bytes)");
        if (filelist.size()==0) result += String.format("%30s%15s","","------N O    F I L E S    I N    S E R V E R------");
        for (int i=0; i<filelist.size(); i++) result += generateFileInfoLine((JSONObject) filelist.get(i));
        cdoi.closeConnection();
        return result;
    }

    private static void setTrustName(String name){
        TRUST_NAME = name;
        SET_SUCCESS_MSG.add("Trust name set!");
    }

    private static String uploadCertificate(String certificate){
        if (!Files.isRegularFile(Paths.get(certificate))) return ("Certificate does not exist");
        sslconnection cdoi = new sslconnection(HOSTNAME, PORT);
        cdoi.sendMessageToServer(generateHeader(
                    Arrays.asList("cert", parseFileName(certificate), String.valueOf(new File(certificate).length())))
        );
        JSONObject response = parseHeader(cdoi.receiveMessageFromServer());
        if (extractStatusCode(response) != 200) displayErrorAndExit(null,extractMessage(response));
        cdoi.sendFileToServer(certificate);
        response = parseHeader(cdoi.receiveMessageFromServer());
        if (extractStatusCode(response) != 200) displayErrorAndExit(null,extractMessage(response));
        cdoi.closeConnection();
        return (extractMessage(response));
    }

    private static String checkAuthenticity(String filename, String certificate){
        sslconnection cdoi = new sslconnection(HOSTNAME, PORT);
        cdoi.sendMessageToServer(generateHeader(
                    Arrays.asList("vouch", parseFileName(filename),parseFileName(certificate))
        ));
        JSONObject response = parseHeader(cdoi.receiveMessageFromServer());
        cdoi.closeConnection();
        return (extractMessage(response));
    }

    private static String getKey(){
        String result = "";
        sslconnection cdoi = new sslconnection(HOSTNAME, PORT);
        cdoi.sendMessageToServer(generateHeader(
                    Arrays.asList("get_key")
        ));
        result = cdoi.receiveMessageFromServer();
        cdoi.closeConnection();
        return result;
    }

    private static void setKey(String key){
        KEY = key;
        SET_SUCCESS_MSG.add("Key set!");
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
        List<String> string_args = Arrays.asList("-a", "-f", "-n", "-u", "-c", "-o", "-e");
        for (int i=0; i<args.length; i++){
            if (string_args.contains(args[i]) && i!=args.length-1 ){
                if (args[i].equals("-c")){
                    if (!validInteger(args[i+1])) return false;
                }else{
                    if (!validString(args[i+1])) return false;
                }
            }else if (args[i].equals("-v") && i<args.length-2){
                if (!(validString(args[i+1]) && validString(args[i+2]))) return false;
            }else if (args[i].equals("-h") && i!=args.length-1){
                if (args[i+1].endsWith(":") || !args[i+1].contains(":")) return false;
                String[] data = args[i+1].split(":");
                if (!(validString(data[0]) && validInteger(data[1]))) return false;
            }else if (args[i].equals("-l") || args[i].equals("-k") || args[i].equals("--verbose")){
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
        log = new LoggerOutput(false);

        if (!parseArguments(args)){
            if (!Arrays.asList(args).contains("--help")) log.error("", "Invalid argument. PLEASE CHECK USAGE BELOW...");
            System.out.println(USAGE_DOC);
            System.exit(0);
        }

        String[] set_args = new String[] {"-c","-h","-n","-e"};
        for (String arg : set_args){
            int index = ARGS_PARAMS.indexOf(arg);
            if (index == -1) continue;
            if (arg.equals("-h")){
                String[] data = ARGS_PARAMS.get(index+1).split(":");
                setHostAddress(data[0], Integer.parseInt(data[1]));
            }else if (arg.equals("-c")){ setLengthOfTrust(Integer.parseInt(ARGS_PARAMS.get(index+1)));
            }else if (arg.equals("-n")){ setTrustName(ARGS_PARAMS.get(index+1));
            }else if (arg.equals("-e")){ setKey(ARGS_PARAMS.get(index+1));}
        }

        if ((SET_SUCCESS_MSG.size()*2) == ARGS_PARAMS.size()){
            log.error("", "Please use an operation");
            System.exit(0);
        }
        if (ARGS_PARAMS.contains("--verbose")) log.DEBUG = true;
        if ((!ARGS_PARAMS.contains("-f") && !ARGS_PARAMS.contains("-l")) || ARGS_PARAMS.contains("--verbose"))
            for (String msg : SET_SUCCESS_MSG) log.success(msg);

        for(int i=0; i<ARGS_PARAMS.size(); i++){
            String result = "";
            switch (ARGS_PARAMS.get(i)){
                case "-a":
                    log.startLog("adding/replacing file to server");
                    result = addOrReplaceFile(ARGS_PARAMS.get(++i));
                    log.endLog();
                    System.out.println(result);
                    break;
                case "-f":
                    log.startLog("fetching file from server");
                    result = getExistingFile(ARGS_PARAMS.get(++i),
                            ARGS_PARAMS.contains("-o") ? ARGS_PARAMS.get(ARGS_PARAMS.indexOf("-o")+1) : "");
                    log.endLog();
                    if (!result.equals("")) System.out.println(result);
                    break;
                case "-l":
                    log.startLog("listing files in server");
                    result = getFileListInfo();
                    log.endLog();
                    System.out.println(result);
                    break;
                case "-u":
                    log.startLog("uploading certificate to server");
                    result = uploadCertificate(ARGS_PARAMS.get(++i));
                    log.endLog();
                    System.out.println(result);
                    break;
                case "-v":
                    log.startLog("vouching for file in server");
                    result = checkAuthenticity(ARGS_PARAMS.get(++i), ARGS_PARAMS.get(++i));
                    log.endLog();
                    System.out.println(result);
                    break;
                case "-k":
                    log.startLog("getting private aes key from server");
                    result = getKey();
                    log.endLog();
                    System.out.println("Your key is: " + result);
                    break;
            }
        }

        log.endLoggerOutput();
    }
}
