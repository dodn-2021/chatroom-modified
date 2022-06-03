package client.model.dialog;

import client.view.ChatView;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import kit.entity.CommunicationEntity;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * dialogue between user and one friend
 */

public class FriendDialog extends AbstractDialog implements Serializable
{

    private transient CommunicationEntity userB;

    public FriendDialog( CommunicationEntity userB, String userA ) throws IOException
    {
        super(userA);
        this.userB = userB;
    }

    /**
     * friend dialog -> chat view 显示聊天窗口方法
     */
    public void setChatView()
    {

        chatView = new ChatView(userB, messageList, false);

        chatView.setOnShown(e -> {
            chatView.getController().scrollToBottom();
        });

    }

    @Serial
    private void writeObject( ObjectOutputStream oos ) throws IOException
    {
        oos.defaultWriteObject();
        oos.writeObject(messageList.toArray());
        oos.writeUTF(userA);
        oos.writeBoolean(hasNewMessage.get());
        oos.writeObject(userB);
    }

    @Serial
    private void readObject( ObjectInputStream ois ) throws IOException, ClassNotFoundException
    {
        ArrayList list = new ArrayList(Arrays.asList((Object[]) ois.readObject()));
        messageList = new SimpleListProperty<>(FXCollections.observableArrayList(list));
        userA = ois.readUTF();
        hasNewMessage = new SimpleBooleanProperty(ois.readBoolean());
        userB = (CommunicationEntity) ois.readObject();
    }
}