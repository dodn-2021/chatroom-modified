package client.model.dialog;

import client.view.ChatView;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import kit.entity.CommunicationEntity;
import kit.entity.UserEntity;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupDialog extends AbstractDialog implements Serializable {
    private transient CommunicationEntity group;
    private transient List< UserEntity > members;

    public GroupDialog( String userA, CommunicationEntity group, List< UserEntity > members){
        super(userA);
        this.group = group;
        this.members = members;
    }

    public void setChatView() throws IOException{
        chatView = new ChatView(group, messageList, true);
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(messageList.toArray());
        oos.writeUTF(userA);
        oos.writeBoolean(hasNewMessage.get());
        oos.writeObject(group);
        oos.writeObject(members);
    }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ArrayList list = new ArrayList(Arrays.asList((Object[]) ois.readObject()));
        messageList = new SimpleListProperty<>(FXCollections.observableArrayList(list));
        userA = ois.readUTF();
        hasNewMessage = new SimpleBooleanProperty(ois.readBoolean());
        group = (CommunicationEntity) ois.readObject();
        members = (List< UserEntity >) ois.readObject();
    }
}
