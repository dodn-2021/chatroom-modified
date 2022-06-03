package server;

import javafx.application.Platform;
import kit.*;
import kit.utilities.CommunicationUtilities;
import kit.utilities.MysqlUtilities;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import static kit.Resource.*;

public class ServerSocketThread extends Thread
{

    private Socket socket;
    private Map< Integer, Socket > socketMap;

    private int id;

    public ServerSocketThread( Socket socket, MysqlUtilities manager, Map< Integer, Socket > socketMap )
    {
        this.socket = socket;
        this.socketMap = socketMap;
    }

    void setThreadID( int id )
    {
        this.id = id;
    }

    @Override
    public void run()
    {

        // 在 JServer 界面显示运行信息
        JServer.update(JServer.MAIN, "收到请求, 线程 " + id + " 开始运行");

        try{

            // 接受信息
            Data receive = CommunicationUtilities.receive(socket, false);

            if( receive == null ){

                // 如果接收到的信息为空, 返回
                System.out.println("\n WARNING: an unknown exception occurs or someone logs out");

                socket.close();
                return;

            }

            Data sends = disposeInMessage(receive);

            if( socket == null ){
                return;
            }

            if( sends != null ){
                CommunicationUtilities.send(socket, sends, true);
            } else {

                // 如果用户退出, 返回一个 exit message 确认
                updateLog(receive.operatorInfo.getName(), "User log out");

                sends = new Data();
                sends.setOperateType(EXIT);
                CommunicationUtilities.send(socket, sends, false);

            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            JServer.update(JServer.MAIN, "线程 " + id + " 运行结束");
        }
    }

    private Data disposeInMessage( Data inMessage ) throws Exception
    {
        Data output = new Data();
        if( inMessage.isOperate(LOAD_USER_INFO) ){
            updateLog(JServer.MAIN, "-------正在加载用户信息------");
            output = sendUserInfo(inMessage);
            updateLog(JServer.MAIN, "-------加载完毕---------");
        } else if( inMessage.isOperate(REGISTER) ){
            updateLog(JServer.MAIN, "-------正在注册中-------");
            output = register(inMessage);//userInfo
            updateLog(JServer.MAIN, "-------注册完毕-------");
        } else if( inMessage.isOperate(ADD_FRIEND) ){
            updateLog(inMessage.operatorInfo.getName(), "-------正在查找用户的信息-------");
            output = makeFriend(inMessage);//userInfo
            updateLog(inMessage.operatorInfo.getName(), "-------添加朋友完毕-------");
        } else if( inMessage.isOperate(CONNECT) ){
            updateLog(inMessage.operatorInfo.getName(), "-------与用户开始进行通讯-------");
            output = connectToClient(inMessage);//Message
        } else if( inMessage.isOperate(LOAD_MESSAGE) ){
            updateLog(inMessage.operatorInfo.getName(), "-------正在查找用户的对话信息-------");
            output = loadDialogues(inMessage);//ArrayList<Message>
            System.out.println("\n DEBUG: user's operatorInfo.getName() " + inMessage.operatorInfo.getName());
            updateLog(inMessage.operatorInfo.getName(), "-------查找用户的对话信息完毕-------");
        }
        //
        else if( inMessage.isOperate(CREATE_GROUP) ){
            updateLog(inMessage.operatorInfo.getName(), "--------正在创建新的群---------");
            output = createGroup(inMessage);
            updateLog(inMessage.operatorInfo.getName(), "--------创建新的群完毕---------");
        } else if( inMessage.isOperate(JOIN_GROUP) ){
            updateLog(inMessage.operatorInfo.getName(), "--------正在加入群" + inMessage.ID + "----------");
            output = joinGroup(inMessage);
            updateLog(inMessage.operatorInfo.getName(), "--------加入群" + inMessage.ID + "完毕----------");
        } else if( inMessage.isOperate(GET_GROUP_MEM) ){
            updateLog(inMessage.operatorInfo.getName(), "--------获取群" + inMessage.ID + "的群成员---------");
            output = getMembers(inMessage);
            updateLog(inMessage.operatorInfo.getName(), "--------获取群" + inMessage.ID + " 成员列表完毕---------");
        }

        return output;
    }

    private Data loadDialogues( Data inMessage ) throws Exception
    {
        return MysqlUtilities.loadDialogues(inMessage.operatorInfo.getID());
    }

    private Data connectToClient( Data inMessage ) throws Exception
    {
        String currentUserName = inMessage.name;
        int currentID = inMessage.ID;

        socketMap.put(currentID, socket);

        Data data;
        while( true ){
            data = CommunicationUtilities.receive(socket, false);

            updateLog(currentUserName, "服务器收到一个DataPackage");

            if( data == null ){
                break;
            }
            if( data.isOperate(EXIT) ){
                break;
            }

            sendMessage(currentID, currentUserName, data.message);
        }

        socketMap.remove(currentID);

        return null;
    }

    private void sendMessage( int currentID, String currentUser, Message msg ) throws Exception
    {
        if( msg.is_broadcast ){
            updateLog(currentUser, "用户发送了一条群消息 " + msg);
            ArrayList< Integer > targetIDs = (ArrayList< Integer >) MysqlUtilities.getMembers(msg.receiver.getID(), true);
            for( int id : targetIDs ){
                if( id == currentID ){
                    continue;
                }
                Socket targetSocket = socketMap.get(id);
                sendMsg(targetSocket, msg);
            }
        } else {
            updateLog(currentUser, "用户发送了一条消息 " + msg);

            // 将信息发送给接收人
            System.out.println("\n DEBUG: the message is sent to user: " + msg.receiver.getName());

            sendMsg(socketMap.get(msg.receiver), msg);
        }
        updateLog(currentUser, "发送完毕");
    }

    private void sendMsg( Socket socket, Message msg ) throws Exception
    {
        if( socket == null ){
            MysqlUtilities.storeMessage(msg);
        } else {
            CommunicationUtilities.send(socket, new Data(msg), false);
        }
    }

    private Data makeFriend( Data message ) throws SQLException, IOException
    {
        Data data = MysqlUtilities.makeFriend(message.name, message.operatorInfo.getName());
        Socket socket = socketMap.get(data.name);
        if( data.ID != -1 && socket != null ){
            Data data1 = new Data(message.operatorInfo);
            data1.setOperateType(ADD_FRIEND);
            CommunicationUtilities.send(socket, data1, false);
        }
        return data;
    }

    private Data sendUserInfo( Data userInfo ) throws Exception
    {
        int ID = userInfo.ID;
        updateLog(null, "接受到ID=" + ID);

        String password = userInfo.password;
        System.out.println();
        updateLog(null, "接受到密码=" + password);

        Data proceeded = MysqlUtilities.selectByIDAndPassword(ID, password);
        Data tmp = new Data(-1);
        if( proceeded == null ){
            System.out.println();
            updateLog(null, "ID与密码不匹配");
            return tmp;
        }
        addUser(proceeded.name);
        return proceeded;
    }

    private Data register( Data info ) throws SQLException
    {
        int ID = MysqlUtilities.register(info);
        return new Data(ID);
    }

    private Data createGroup( Data info )
    {
        /*
        info 数据包需要包括 name：群名， operator，operatorID
         */

        return new Data(MysqlUtilities.createGroup(info));
    }

    private Data joinGroup( Data info )
    {
        /*
        info 数据包需要包括 name：群名 或 ID：群ID， operator， operatorID
         */
        Data data = MysqlUtilities.joinGroup(info);
        if( data.ID != -1 ){
            info.setOperateType(JOIN_GROUP);
            CommunicationUtilities.send(socketMap.get(info.operatorInfo.getID()), info, false);
        }
        return data;
    }

    private Data getMembers( Data data )
    {
        try{
            return new Data(MysqlUtilities.getMembers(data.ID, false));
        } catch(Exception e) {
            updateLog(data.operatorInfo.getName(), "获得群聊成员列表错误");
            e.printStackTrace();
        }
        return new Data(new ArrayList());
    }

    private void updateLog( String user, String log )
    {
        Platform.runLater(() -> {
            if( user != null ){
                JServer.update(user, log);
            } else {
                JServer.update(JServer.MAIN, log);
            }
        });
    }

    private void addUser( String user )
    {
        Platform.runLater(() -> {
            JServer.addUser(user);
        });
    }
}
