package kit.utilities;

import kit.utilities.JsonUtilities.*;

import static kit.utilities.JsonUtilities.getEmptyJSONArray;
import static kit.utilities.JsonUtilities.getEmptyJSONObject;

public class test
{

    public static void main(String[] args)
    {

        // testing JSON Object

        JSONObject json = getEmptyJSONObject();
        json.putPair("type", "pos_update").putPair("uid", 1234);
        JSONArray arr = getEmptyJSONArray();
        arr.addItem(getEmptyJSONObject().putPair("name", "slime1").putPair("x_pos", 1000).putPair("y_pos", 1000).toString());
        json.putPair("pos_list", arr.toString());

        System.out.println(json.getJSONString());

        JSONObject json2 = getEmptyJSONObject().putPair("name", "debug").putPair("content", json.toString());

        // nested json test
        System.out.println(json2.getJSONString());

        System.out.printf("x position of first object: %s\n", json2.getJSONObject("content")
                .getJSONArray("pos_list")
                .getJSONObject(0).getValue("x_pos"));

        System.out.printf("name of the first object: %s\n", json2.getJSONObject("content").
                getJSONArray("pos_list").getJSONObject(0).getValue("name"));

    }
}
