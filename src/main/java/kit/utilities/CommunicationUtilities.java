package kit.utilities;

import client.model.*;
import client.model.dialog.FriendDialog;
import client.model.dialog.GroupDialog;
import kit.*;
import kit.entity.GroupEntity;
import kit.entity.UserEntity;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import static kit.Resource.*;

public class CommunicationUtilities
{
    private static final int BUFFER_LENGTH = 1024; // 数据缓冲区长度
    private static final String HOST = "127.0.0.1"; // 主机IP地址
    private static final int PORT = 5432; // 主机端口

    /**
     * @param socket    传输使用的 Socket类
     * @param data      传输的 Data类
     * @param is_closing 如果设置为 true, 正常传输完成后关闭 socket.
     */
    public static void send( Socket socket, Data data, boolean is_closing )
    {
        DataOutputStream dataOutput;
        byte[] buffer;

        try{
            dataOutput = new DataOutputStream(socket.getOutputStream());

            buffer = getBytesFromObject(data);

            dataOutput.writeLong(buffer.length);

            // 在控制台输出发送数据的长度
//            System.out.println("\n I/O Module: sending data buffer length: " + buffer.length);

            dataOutput.write(buffer);

            if( is_closing ){
                socket.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
            showTransmissionAlert();
        }
    }

    public static Data receive( Socket socket, boolean is_closing )
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataInputStream dataInput;
        byte[] buffer;
        long length;

        try{
            dataInput = new DataInputStream(socket.getInputStream());

            length = dataInput.readLong();

            if( length == 0 ){
                return null;
            }

            buffer = new byte[BUFFER_LENGTH];

            // 在控制台输出接收数据的长度
//            System.out.println("\n I/O Module: receiving data buffer length: " + length);

            long current = 0;

            while( current < length ){
                int tmp = dataInput.read(buffer);
                current += tmp;
                byteArrayOutputStream.write(buffer, 0, tmp);
            }

            if( is_closing ){
                socket.close();
            }

            // 在控制台显示接收数据 byteArrayOutputStream 长度
//            System.out.println("\n I/O Module: receiving data (to array output stream) length: " + byteArrayOutputStream.toByteArray().length);

            return (Data) getObjectFromBytes(byteArrayOutputStream.toByteArray());

        } catch(Exception e) {
            e.printStackTrace();
            showTransmissionAlert();
        }
        return null;
    }

    private static void showTransmissionAlert()
    {
        UiUtilities.showAlert("WARNING: Exceptions During Data Transmissions.");
    }

    private static Object getObjectFromBytes( byte[] objectBytes ) throws IOException, ClassNotFoundException
    {
        if( objectBytes == null || objectBytes.length == 0 ){
            return null;
        }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(objectBytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

        return objectInputStream.readObject();
    }

    private static byte[] getBytesFromObject( Serializable object ) throws IOException
    {
        if( object == null ){
            return null;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.close();

        return byteArrayOutputStream.toByteArray();
    }

    public static boolean loadUserInfo( int ID, String password ) throws IOException
    {
        Socket socket = new Socket(HOST, PORT);

        Data data = new Data(ID, password);
        data.setOperateType(LOAD_USER_INFO);

        send(socket, data, false);

        Data receive = receive(socket, false);

        if( receive.ID == -1 ){
            return false;
        } else {
            User.getInstance().setField(receive);
            return true;
        }
    }

    public static int register( Data data ) throws IOException
    {
        Socket socket = new Socket(HOST, PORT);

        data.setOperateType(REGISTER);

        send(socket, data, false);

        Data receiveData = receive(socket, false);

        return receiveData.ID;
    }

    public static boolean makeFriendWith( String info ) throws Exception
    {
        Socket socket = new Socket(HOST, PORT);

        Data data = new Data(info);
        data.setOperateType(ADD_FRIEND);
        data.setOperatorInfo();

        send(socket, data, false);

        Data receive = receive(socket, false);

        if( receive.ID == -1 ){
            return false;
        }

        UserEntity userEntity1 = new UserEntity(receive.ID, receive.name, receive.signature, receive.iconBytes);
        Friend friend = new Friend(userEntity1);
        User.getInstance().addFriend(friend);

        // 出错的位置, dialog 创建失败

        FriendDialog dialog = new FriendDialog(friend.getUserEntity(), User.getInstance().getName());
        friend.init(dialog);
        dialog.synchronizeMessage();
        return true;
    }

    public static boolean createGroup( String groupName, byte[] bytes ) throws IOException
    {
        Socket socket = new Socket(HOST, PORT);

        Data data = new Data();
        data.name = groupName;
        data.iconBytes = bytes;
        data.setOperatorInfo();
        data.setOperateType(CREATE_GROUP);
        send(socket, data, false);

        Data receiveData = receive(socket, false);
        if( receiveData.ID == -1 ){
            return false;
        }
        ArrayList< UserEntity > members = new ArrayList<>();
        members.add(User.getInstance().getUserInfo());
        GroupEntity groupEntity = new GroupEntity(receiveData.ID, groupName, bytes, members, 0);
        //服务器将新的group封装成一个group_info发回
        setGroup(groupEntity);
        return true;
    }

    public static boolean joinGroup( String info ) throws IOException
    {
        Socket socket = new Socket(HOST, PORT);

        //暂时只能按群名加群
        Data data = new Data();
        data.name = info;
        data.ID = -1;
        data.setOperatorInfo();
        data.setOperateType(JOIN_GROUP);
        send(socket, data, false);

        Data recvData = receive(socket, false);
        if( recvData.ID == -1 ){
            return false;
        }
        int groupOwner = -1;
        ArrayList< UserEntity > members = (ArrayList< UserEntity >) recvData.listA;
        String builder = recvData.builder;
        while( !members.get(++groupOwner).getName().equals(builder) ) ;
        GroupEntity groupEntity = new GroupEntity(recvData.ID, recvData.name, recvData.iconBytes, members, groupOwner);
        setGroup(groupEntity);
        return true;
    }

    private static void setGroup( GroupEntity groupEntity ) throws IOException
    {
        Group group = new Group(groupEntity);
        GroupDialog dialog = new GroupDialog(User.getInstance().getName(), groupEntity, groupEntity.getMembers());
        group.init(dialog);
        dialog.synchronizeMessage();
        User.getInstance().addGroup(group);
    }

    public static Socket connectToRemote() throws Exception
    {
        Socket socket = new Socket(HOST, PORT);
        Data data = new Data(User.getInstance().getName(), User.getInstance().getID());
        data.setOperateType(CONNECT);
        data.setOperatorInfo();

        send(socket, data, false);

        return socket;
    }

    public static ArrayList< Message > loadMessage( String type ) throws Exception
    {
        Socket socket = new Socket(HOST, PORT);

        Data data = new Data();
        data.setOperatorInfo();
        data.setOperateType(type);

        send(socket, data, false);

        Data receive = receive(socket, false);

        return (ArrayList< Message >) receive.listA;
    }

}