package kit.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

// Defining constants for json parsers
// Only used in JSONObject and JSONArray
enum CONSTANTS
{

    CURLY_OPEN_BRACKETS('{'),
    CURLY_CLOSE_BRACKETS('}'),
    SQUARE_OPEN_BRACKETS('['),
    SQUARE_CLOSE_BRACKETS(']'),
    COLON(':'),
    COMMA(','),
    SPECIAL('|'),

    // for generating json string, use single quotation marks
    QUOTATION_MARKS('\'');

    private final char constant;

    // Constructor
    CONSTANTS( char constant )
    {
        this.constant = constant;
    }

    // Method
    // Overriding exiting toString() method
    @Override
    public String toString()
    {
        return String.valueOf(constant);
    }
}

public class JsonUtilities
{

    /*
     *  raw json format generator.
     *   packages not used.
     *
     *  简单的 json 生成/解码
     *   例子:
     *       {'type':'pos_update','uid':1234,'pos_list':[{'name':'slime1','x_pos':1000,'y_pos':1000}]}
     *       {'type':'login','user_name':'admin','password':'root'}
     * */

    // To parse json object
    public static class JSONObject
    {

        private final static char specialChar;
        private final static char commaChar;
        private HashMap< String, String > objects;

        static{
            specialChar = String.valueOf(CONSTANTS.SPECIAL)
                    .toCharArray()[0];
            commaChar = String.valueOf(CONSTANTS.COMMA)
                    .toCharArray()[0];
        }

        // Constructor if this class
        public JSONObject( String arg )
        {
            getJSONObjects(arg);
        }


        // Create as an empty JSON object
        private JSONObject()
        {
            objects = new HashMap<>();
        }

        public JSONObject getJSONObject( String key )
        {
            return new JSONObject(getValue(key));
        }

        // Storing json objects as key value pair in hash map
        private void getJSONObjects( String arg )
        {

            objects = new HashMap< String, String >();

            if( arg.startsWith(String.valueOf(
                    CONSTANTS.CURLY_OPEN_BRACKETS))
                    && arg.endsWith(String.valueOf(
                    CONSTANTS.CURLY_CLOSE_BRACKETS)) ){

                StringBuilder builder = new StringBuilder(arg);
                builder.deleteCharAt(0);
                builder.deleteCharAt(builder.length() - 1);
                builder = replaceCOMMA(builder);

                for( String objects : builder.toString().split(
                        String.valueOf(CONSTANTS.COMMA)) ){

                    String[] objectValue = objects.split(
                            String.valueOf(CONSTANTS.COLON), 2);

                    if( objectValue.length == 2 ){
                        this.objects.put(
                                objectValue[0]
                                        .replace("'", "")
                                        .replace("\"", ""),
                                objectValue[1]
                                        .replace("'", "")
                                        .replace("\"", ""));
                    }
                }
            }
        }

        public StringBuilder replaceCOMMA( StringBuilder arg )
        {

            boolean isJsonArray = false;

            for( int i = 0; i < arg.length(); i++ ){
                char a = arg.charAt(i);

                if( isJsonArray ){

                    if( String.valueOf(a).compareTo(
                            String.valueOf(CONSTANTS.COMMA))
                            == 0 ){
                        arg.setCharAt(i, specialChar);
                    }
                }

                if( String.valueOf(a).compareTo(String.valueOf(
                        CONSTANTS.SQUARE_OPEN_BRACKETS))
                        == 0 ){
                    isJsonArray = true;
                }
                if( String.valueOf(a).compareTo(String.valueOf(
                        CONSTANTS.SQUARE_CLOSE_BRACKETS))
                        == 0 ){
                    isJsonArray = false;
                }
            }

            return arg;
        }

        // Getting json object value by key from hash map
        public String getValue( String key )
        {
            if( objects != null ){
                return objects.get(key).replace(specialChar,
                        commaChar);
            }
            return null;
        }

        // Getting json array by key from hash map
        public JSONArray getJSONArray( String key )
        {
            if( objects != null ){
                return new JSONArray(
                        objects.get(key).replace('|', ','));
            }
            return null;
        }

        // Putting new key-value pairs into hash map (generic, using string value)
        public JSONObject putPair( String key, String value )
        {
            objects.put(key, value);
            return this;
        }

        // Putting key (string) and value (int)
        public JSONObject putPair( String key, int value )
        {
            objects.put(key, "" + value);
            return this;
        }

        // recursively generate the json String
        public String getJSONString( boolean with_quotes )
        {
            StringBuilder str = new StringBuilder();
            str.append(CONSTANTS.CURLY_OPEN_BRACKETS);
            for( String key : objects.keySet() ){
                // put key string before the colon
                if( with_quotes ){
                    str.append(CONSTANTS.QUOTATION_MARKS).append(key).append(CONSTANTS.QUOTATION_MARKS);
                } else {
                    str.append(key);
                }
                // put colon
                str.append(CONSTANTS.COLON);
                // put value string after the colon
                String value = objects.get(key);
                if( value.startsWith(String.valueOf(CONSTANTS.SQUARE_OPEN_BRACKETS)) ){
                    str.append(getJSONArray(key).getJSONString(with_quotes));
                } else if( value.startsWith(String.valueOf(CONSTANTS.CURLY_OPEN_BRACKETS)) ){
                    JSONObject nest_json = new JSONObject(getValue(key));
                    str.append(nest_json.getJSONString(with_quotes));
                } else {
                    if( with_quotes ){
                        str.append(CONSTANTS.QUOTATION_MARKS).append(objects.get(key)).append(CONSTANTS.QUOTATION_MARKS);
                    } else {
                        str.append(objects.get(key));
                    }
                }
                // put comma
                str.append(CONSTANTS.COMMA);
            }
            str.deleteCharAt(str.length() - 1); // delete the last comma
            str.append(CONSTANTS.CURLY_CLOSE_BRACKETS);
            return str.toString();
        }

        // default method for retrieving JSON string (with single quotation marks)
        public String getJSONString()
        {
            return getJSONString(true);
        }

        // default method for generating internal json expressions
        public String toString()
        {
            return getJSONString(false);
        }
    }

    // To parse json array
    public static class JSONArray
    {

        private final static char specialChar;
        private final static char commaChar;

        private ArrayList< String > objects;

        static{
            specialChar = String.valueOf(CONSTANTS.SPECIAL)
                    .toCharArray()[0];
            commaChar = String.valueOf(CONSTANTS.COMMA)
                    .toCharArray()[0];
        }

        // Constructor of this class
        public JSONArray( String arg )
        {
            getJSONObjects(arg);
        }

        // Creating empty JSON array
        private JSONArray()
        {
            objects = new ArrayList<>();
        }

        // Storing json objects in array list
        public void getJSONObjects( String arg )
        {

            objects = new ArrayList< String >();

            if( arg.startsWith(String.valueOf(
                    CONSTANTS.SQUARE_OPEN_BRACKETS))
                    && arg.endsWith(String.valueOf(
                    CONSTANTS.SQUARE_CLOSE_BRACKETS)) ){

                StringBuilder builder = new StringBuilder(arg);

                builder.deleteCharAt(0);
                builder.deleteCharAt(builder.length() - 1);

                builder = replaceCOMMA(builder);

                // Adding all elements
                // using addAll() method of Collections class
                Collections.addAll(
                        objects,
                        builder.toString().split(
                                String.valueOf(CONSTANTS.COMMA)));
            }
        }

        public StringBuilder replaceCOMMA( StringBuilder arg )
        {
            boolean isArray = false;

            for( int i = 0; i < arg.length(); i++ ){
                char a = arg.charAt(i);
                if( isArray ){

                    if( String.valueOf(a).compareTo(
                            String.valueOf(CONSTANTS.COMMA))
                            == 0 ){
                        arg.setCharAt(i, specialChar);
                    }
                }

                if( String.valueOf(a).compareTo(String.valueOf(
                        CONSTANTS.CURLY_OPEN_BRACKETS))
                        == 0 ){
                    isArray = true;
                }

                if( String.valueOf(a).compareTo(String.valueOf(
                        CONSTANTS.CURLY_CLOSE_BRACKETS))
                        == 0 ){
                    isArray = false;
                }
            }

            return arg;
        }

        // Getting json object by index from array list
        public String getObject( int index )
        {
            if( objects != null ){
                return objects.get(index).replace(specialChar,
                        commaChar);
            }

            return null;
        }

        // Getting json object from array list
        public JSONObject getJSONObject( int index )
        {

            if( objects != null ){
                return new JSONObject(
                        objects.get(index).replace('|', ','));
            }

            return null;
        }

        // Getting json string from array list
        public String getJSONString( boolean with_quotes )
        {
            StringBuilder str = new StringBuilder(String.valueOf(CONSTANTS.SQUARE_OPEN_BRACKETS));
            for( int i = 0; i < objects.size(); i++ ){
                // put the json inside meow
                str.append(getJSONObject(i).getJSONString(with_quotes));

                // put the comma
                str.append(CONSTANTS.COMMA);
            }
            str.deleteCharAt(str.length() - 1); // delete the last comma
            str.append(CONSTANTS.SQUARE_CLOSE_BRACKETS);
            return str.toString();
        }

        // add an object (JSONObject or JSONArray) to the items
        public JSONArray addItem( String object )
        {
            objects.add(object.replace(",", "|"));
            return this;
        }

        public String getJSONString()
        {
            return getJSONString(true);
        }

        public String toString()
        {
            return getJSONString(false);
        }
    }

    public static JSONObject getEmptyJSONObject()
    {
        return new JSONObject();
    }

    public static JSONArray getEmptyJSONArray()
    {
        return new JSONArray();
    }

}