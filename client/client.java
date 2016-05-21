
public class client{

    private static void addOrReplaceFile(String filename){
        System.out.println(filename);
    }

    private static void getLengthOfTrust(int length){
        System.out.println(length);
    }

    private static void getExistingFile(String filename){
        System.out.println(filename);
    }

    private static void setHostAddress(String host_name, int port){
        System.out.println(host_name + " " + port);
    }

    private static void getFileListInfo(){
        System.out.println("filelist info");
    }

    private static void setTrustName(String name){
        System.out.println(name);
    }
    
    private static void uploadCertificate(String certificate){
        System.out.println(certificate);
    }

    private static void checkAuthenticity(String filename, String certificate){
        System.out.println(filename + " " + certificate);
    }

    public static void main(String[] args) {

        for(int i=0 ; i<args.length; i++){
            switch (args[i]){
                case "-a":
                    addOrReplaceFile(args[++i]);
                    break;
                case "-c":
                    getLengthOfTrust(Integer.parseInt(args[++i]));
                    break;
                case "-f":
                    getExistingFile(args[++i]);
                    break;
                case "-h":
                    String[] data = args[++i].split(":");
                    setHostAddress(data[0], Integer.parseInt(data[1]));
                    break;
                case "-l":
                    getFileListInfo();
                    break;
                case "-n":
                    setTrustName(args[++i]);
                    break;
                case "-u":
                    uploadCertificate(args[++i]);
                    break;
                case "-v":
                    checkAuthenticity(args[++i], args[++i]);
                    break;
                default:
                    System.out.println("Usage :...");
            }
        }

    }
}
