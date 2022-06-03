package client.launcher;

import client.model.Friend;
import client.model.Group;
import client.model.User;
import client.model.dialog.FriendDialog;
import javafx.application.Platform;
import javafx.beans.property.MapProperty;
import kit.Data;
import kit.Message;
import kit.entity.UserEntity;
import kit.utilities.CommunicationUtilities;

import java.io.IOException;
import java.net.Socket;

import static kit.Resource.*;

public class ClientSocketThread extends Thread
{
    private final MapProperty< Integer, Friend > friends;
    private final MapProperty< Integer, Group > groups;
    private final Socket mySocket;

    public ClientSocketThread( MapProperty< Integer, Friend > friends, MapProperty< Integer, Group > groups, Socket mySocket )
    {
        this.friends = friends;
        this.groups = groups;
        this.mySocket = mySocket;
    }

    @Override
    public void run()
    {

        // 连接线程输出信息到控制台
        System.out.println("\n DEBUG: connection thread start receiving text");

        for( Friend friend : friends.values() ){
            friend.getFriendDialog().synchronizeMessage();
        }
        for( Group group : groups.values() ){
            group.getGroupDialog().synchronizeMessage();
        }

        while( true ){
            Data receive = CommunicationUtilities.receive(mySocket, false);
            if( receive.isOperate(EXIT) ){
                try{
                    mySocket.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            if( receive.isOperate(ADD_FRIEND) ){
                Platform.runLater(() -> {
                    try{
                        UserEntity userEntity = new UserEntity(receive.ID, receive.name, receive.signature, receive.iconBytes);
                        Friend friend = new Friend(userEntity);
                        User.getInstance().addFriend(friend);
                        FriendDialog dialog = new FriendDialog(friend.getUserEntity(), User.getInstance().getName());
                        friend.init(dialog);
                        dialog.synchronizeMessage();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                });
            } else if( receive.isOperate(JOIN_GROUP) ){
                String groupName = receive.name;//从Data中取得组名
                UserEntity userEntity = receive.operatorInfo;//从Data中获取新成员的信息
                groups.get(groupName).getGroupInfo().getMembers().add(userEntity);
            } else {
                Message message = receive.message;

                // 判断消息是否来自群中
                if( message.is_broadcast ){

                    groups.get(message.receiver).getGroupDialog().updateMessage(message);

                } else {

                    friends.get(message.sender).getFriendDialog().updateMessage(message);

                }
            }
        }
    }
}
