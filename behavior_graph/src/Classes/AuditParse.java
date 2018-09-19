package Classes;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;


/**
 * A class dedicated to parsing audit log files and mapping them to the appropriate data structure
 *
 * @author Matthew Jones
 * @email mfj38472@uga.edu
 *
 *
 */
public class AuditParse {
    public BufferedReader buff;
    String line;

    List<Record> recordList = new ArrayList<Record>(); //list of all records
    HashMap<String, Record> recordHashMap = new HashMap<>();


    /**
     * Parses the audit log file given to it into a list of records. Each record is identified by it's ID and type, and within each record is a list of tokens containing key=value pairs
     * that pertain to the properties of the system call. Please note this method assumes that the file object given to it is an audit log, any other filed given to this method will result
     * in unexpected behavior.
     *
     * @param auditlog audit log file to analyze
     * @throws IOException
     */
    public void parseAuditLog(File auditlog) throws IOException {

        try {
            buff = new BufferedReader(new FileReader(auditlog)); //Reads the audit log file into a buffer
        } catch (FileNotFoundException filex) {
            System.out.println("Error file not found!");
            System.out.println(new File(".").getAbsolutePath());
            System.exit(0);
        }

        while ((line = buff.readLine()) != null) { //Parses each line of the audit log file
            List<String> LineSplit = splitString(line); //split string by space
            if (LineSplit.get(0).equals("type=SYSCALL")) {
                Record newRec = new Record(LineSplit); //create new record
                if(recordHashMap.containsKey(newRec.getId())){
                    recordHashMap.get(newRec.getId()).tokenList.addAll(newRec.tokenList);
                }else if(newRec.getType().equals("SYSCALL")){
                    recordHashMap.put(newRec.getId(), newRec);
                }

            }

        }
    }


    private List<String> splitString(String line){

        boolean quoteMode = false;
        String tempString = "";
        ArrayList<String> stringList = new ArrayList<>();
        for(int i = 0; i<line.length(); i++){


            if(line.substring(i, i+1).equals("\"") && quoteMode == false){
            quoteMode = true;
            }else if(line.substring(i, i+1).equals("\"") && quoteMode == true){
            quoteMode = false;
            tempString+="\"";
            stringList.add(tempString);
            tempString = "";
            continue;
            }

            if(quoteMode == true){
            tempString+=line.substring(i,i+1);
            }else if(quoteMode==false){
                if(!line.substring(i,i+1).equals(" ")){
                    tempString+=line.substring(i,i+1);
                }else if(line.substring(i,i+1).equals(" ") && tempString.isEmpty() == false){
                    stringList.add(tempString);
                    tempString = "";
                }
            }


        }

    return stringList;
    }


    public static void main(String[] args){
        AuditParse audit = new AuditParse();
        try {
            audit.parseAuditLog(new File("audit.log"));
        } catch(IOException iox){
            System.out.println("IO Exception Error!");
            System.exit(0);
        }
    }
}
