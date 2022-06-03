package kit;

import client.model.User;
import kit.entity.UserEntity;
import kit.utilities.JsonUtilities;
import kit.utilities.JsonUtilities.*;

import java.io.Serializable;
import java.util.List;

public class Data implements Serializable
{

    private String operateType;

    public int ID;
    public String name;
    public String signature;
    public String password;
    public List listA;
    public List listB;
    public byte[] iconBytes;
    public Message message;
    public String builder;
    public UserEntity operatorInfo;

    private JSONObject data_json;


    public Data()
    {
    }

    public String getJSONString()
    {
        return data_json.getJSONString();
    }

    public void setOperatorInfo()
    {
        operatorInfo = User.getInstance().getUserInfo();
    }

    public Data( int ID, String password )
    {//log in data package
        this.ID = ID;
        this.password = password;

        data_json = JsonUtilities.getEmptyJSONObject();
        data_json.putPair("type", Resource.LOAD_USER_INFO);
    }

    public Data( String name, int ID )
    {
        this.ID = ID;
        this.name = name;
    }

    public Data( UserEntity info )
    {
        this.ID = info.getID();
        this.name = info.getName();
        this.signature = info.getSig();
        this.iconBytes = info.getIcon();
    }

    public Data( int ID )
    {//get ID
        this.ID = ID;
    }

    public Data( String name )
    {//search friend
        this.name = name;
    }

    public Data( Message message )
    {
        this.message = message;
    }

    public Data( String name, String password, String signature, byte[] iconBytes )
    {//register
        this.name = name;
        this.password = password;
        this.signature = signature;
        this.iconBytes = iconBytes;
    }

    public Data( List listA )
    {
        this.listA = listA;
    }

    public void setOperateType( String operateType )
    {
        this.operateType = operateType;
    }

    public boolean isOperate( String operateType )
    {
        if( this.operateType == null ){
            return false;
        }
        return this.operateType.equals(operateType);
    }
}
