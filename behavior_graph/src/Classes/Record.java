package Classes;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The record represents an individual line within the audit log file. Each line is converted into a record, and inside each record, each key=value pair is broken up into "tokens"
 * @author Matt Jones
 *
 */
public class Record {
    String id;
    String type;
    HashMap<Integer, Token> tokenList = new HashMap<>();
    Record(List<String> list) throws IOException{

            //Extract the type from the string type=value
            type = list.get(0).substring(list.get(0).indexOf("=")+1);

            //Extracts the substring from the : up until the ) in audit(TIMESTAMP:ID)
            id = list.get(1).substring(list.get(1).indexOf(":")+1, list.get(1).indexOf(")"));
            list.remove(0);
            list.remove(0);

        for(int i = 0; i<list.size(); i++){
            Token newToken = new Token(list.get(i));
            tokenList.put(i, newToken);
        }

    }


    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * Get a specific token based on the name of the key in the key=value pair
     * @param tokenType the name of key within the key=value pair
     * @return the token if found and null if not
     */
    public Token getToken(String tokenType){
        for(int i = 0; i<tokenList.size(); i++){
            if(tokenList.get(i).getKey().equals(tokenType)){
                return tokenList.get(i);
            }

        }
        System.out.println("Error token not found!");
        System.out.println(this.getId());
        return null;
    }

    /**
     * A single token represents an individual key=value pair within a record object
     */
    class Token{

        private String key;
        private String value;

        //Constructor class for each individual token
        Token(String keyValuePair){

            key = keyValuePair.substring(0, keyValuePair.indexOf("="));
            value = keyValuePair.substring(keyValuePair.indexOf("=")+1);
        }

        //Sets the variable value of each individual token
        public void setValue(String value) {
            this.value = value;
        }
        //Sets the key value of each individual token
        public void setKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

    }


    /**
     * Creates a new token using a key=value pair string, which is then initialized into a token and added to the end of the token list
     * @param keyValue New key value pair to instantiate as token and append to the end of the token list
     * @throws IOException
     */
    public void appendToken(String keyValue){
        Token appendToken = new Token(keyValue);
        tokenList.put(tokenList.size(), appendToken);
    }


}
