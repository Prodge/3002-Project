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
    static List<String> SET_SUCCESS_MSG = new ArrayList<String>();
    static String HOSTNAME = "188.166.215.84";
    static int PORT = 3000;
    static String TRUST_NAME = "";
    static int TRUST_SIZE = 0;
    public static LoggerOutput log = null;

    private static String generateHeader(List<String> value_list){
        HashMap<String, String> dictionary = new HashMap<String, String>();
        dictionary.put("Operation", value_list.get(0));
        if (!value_list.get(0).equals("list")){
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
        log.error(Thread.currentThread().getStackTrace()[2].getMethodName(), msg.equals("") ? e.toString() : "Server threw the following error:\n" + msg);
        System.exit(0);
    }

    private static String addOrReplaceFile(String filename){
        if (!Files.isRegularFile(Paths.get(filename))) return ("File does not exits");
        sslconnection cdoi = new sslconnection(HOSTNAME, PORT);
        cdoi.sendMessageToServer(generateHeader(
                    Arrays.asList("add", parseFileName(filename), String.valueOf(new File(filename).length())))
        );
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
        String otpt = "";
        sslconnection cdoi = new sslconnection(HOSTNAME, PORT);
        cdoi.sendMessageToServer(generateHeader(
                    Arrays.asList("list"))
        );
        JSONArray filelist = parseFileList(cdoi.receiveMessageFromServer());
        otpt = String.format("%s%32s%32s%32s\n","FILE NAME", "CERTIFICATE NAME", "LENGTH OF COT", "FILE SIZE");
        if (filelist.size()==0) otpt += "--------------------------NO FILES IN SERVER-----------------------";
        for (int i=0; i<filelist.size(); i++){
            JSONObject obj = (JSONObject) filelist.get(i);
            otpt += String.format("%s%32s%32s%32s\n",obj.get("filename"), obj.get("certname"), obj.get("cot_size"), obj.get("filesize"));
        }
        cdoi.closeConnection();
        return otpt;
    }

    private static void setTrustName(String name){
        TRUST_NAME = name;
        SET_SUCCESS_MSG.add("Trust name set!");
    }

    private static String uploadCertificate(String certificate){
        if (!Files.isRegularFile(Paths.get(certificate))) return ("Certificate does not exits");
        sslconnection cdoi = new sslconnection(HOSTNAME, PORT);
        cdoi.sendMessageToServer(generateHeader(
                    Arrays.asList("cert", parseFileName(certificate), String.valueOf(new File(certificate).length())))
        );
        JSONObject response = parseHeader(cdoi.receiveMessageFromServer());
        if (extractStatusCode(response) != 200) displayErrorAndExit(null,extractMessage(response));
        cdoi.sendFileToServer(certificate);
        cdoi.closeConnection();
        return ("Certificate added successfully");
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
        List<String> string_args = Arrays.asList("-a", "-f", "-n", "-u", "-c", "-o");
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
            }else if (args[i].equals("-l") || args[i].equals("--verbose")){
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
                    System.err.println(result);
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
                    System.err.println(result);
                    break;
                case "-v":
                    log.startLog("vouching for file in server");
                    result = checkAuthenticity(ARGS_PARAMS.get(++i), ARGS_PARAMS.get(++i));
                    log.endLog();
                    System.out.println(result);
                    break;
            }
        }
    }
}
