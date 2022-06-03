package kit.entity;

import kit.UserCard;

import java.io.Serializable;

public class UserEntity extends CommunicationEntity implements Serializable {
    private String sig;
    private transient UserCard userCard;
    public UserEntity( int ID, String name, String sig, byte[] icon){
        super(ID, name, icon);
        this.sig = sig;
    }

    public UserCard getUserCard() {
        return userCard;
    }
    public void prepareUserCard() {
        userCard = new UserCard(ID, name, sig, iconPath);
    }

    public String getSig() {
        return sig;
    }
    public void setSig(String sig) {
        this.sig = sig;
    }
}
