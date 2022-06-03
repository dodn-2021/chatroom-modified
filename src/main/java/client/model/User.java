package client.model;

import client.launcher.ClientSocketThread;
import client.model.dialog.DialogManager;
import client.model.dialog.FriendDialog;
import kit.utilities.CommunicationUtilities;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import kit.*;
import kit.entity.CommunicationEntity;
import kit.entity.GroupEntity;
import kit.entity.UserEntity;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static kit.Resource.*;

public class User {
    private UserEntity userEntity;
    private SimpleMapProperty<Integer, Friend> friends;
    private SimpleMapProperty<Integer, Group> groups;

    private DialogManager manager;
    private Socket mySocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private static User instance = new User();

    public static User getInstance() {
        return instance;
    }

    public void setField(Data u) {
        this.userEntity = new UserEntity(u.ID, u.name, u.signature, u.iconBytes);
        manager = new DialogManager(u.name);
        this.friends = new SimpleMapProperty<>(FXCollections.observableHashMap());

        for ( UserEntity userEntity : (List< UserEntity >)u.listA) {
            Friend friend = new Friend(userEntity);
            friends.putIfAbsent(userEntity.getID(), friend);
        }
        this.groups = new SimpleMapProperty<>(FXCollections.observableHashMap());
        for ( GroupEntity groupEntity : (List< GroupEntity >)u.listB){
            Group group = new Group(groupEntity);
            groups.putIfAbsent(groupEntity.getID(), group);
        }

    }

    public void initialise() throws Exception {
        manager.initFriendsDialog(friends);
        manager.initGroupsDialog(groups);

        loadRemoteData();

        this.mySocket = CommunicationUtilities.connectToRemote();
        this.inputStream = mySocket.getInputStream();

        receiveMessages();
    }

    public void loadRemoteData() throws Exception {

        ArrayList<Message> messages =
                CommunicationUtilities.loadMessage(LOAD_MESSAGE);

//        System.out.println(messages);

        for (Message message : messages) {
            if (message.is_broadcast )
                groups.get(message.receiver).getGroupDialog().updateMessage(message);
            else
                friends.get(message.sender).getFriendDialog().updateMessage(message);
        }
    }

    public void addFriend(Friend friend) {
        friends.put(friend.getUserEntity().getID(), friend);

        // 此处为主界面更新好友列表
    }

    public void addGroup(Group group) {
        groups.put(group.getGroupInfo().getID(), group);
    }

    public DialogManager getManager() {
        return manager;
    }

    public String getName() {
        return userEntity.getName();
    }

    public byte[] getMyIconBytes() {
        return userEntity.getIcon();
    }

    public String getSignature() {
        return userEntity.getSig();
    }

    public int getID() {
        return userEntity.getID();
    }

    public FriendDialog getDialogueFrom( int friendID) {
        return friends.get(friendID).getFriendDialog();
    }

    public Collection<Friend> getFriendList() {
        return friends.values();
    }

    public Collection<Integer> getFriendIDs() {
        return friends.keySet();
    }

    public SimpleMapProperty<Integer, Friend> getFriends() {
        return friends;
    }

    public SimpleMapProperty<Integer, Group> getGroups() {return groups;}

    public void sendMessage(Message message) throws Exception {
        CommunicationEntity receiver = message.receiver;
        Data data = new Data(message);
        data.setOperateType(NO_OP);
        if (message.is_broadcast ) {
            groups.get(receiver.getID()).getGroupDialog().updateMessage(message);
        }
        else {
            friends.get(receiver.getID()).getFriendDialog().updateMessage(message);
        }
        CommunicationUtilities.send(mySocket, data, false);
    }

    private void receiveMessages() {
        ClientSocketThread clientSocketThread = new ClientSocketThread(friends, groups, mySocket);
        clientSocketThread.start();
    }

    public void exit() throws Exception {
        Data data = new Data();
        data.setOperateType(EXIT);

        CommunicationUtilities.send(mySocket, data, false);

        // 登出时储存文件
        manager.updateMyDialogues(friends, groups);
    }

    public UserEntity getUserInfo() {
        return userEntity;
    }


}