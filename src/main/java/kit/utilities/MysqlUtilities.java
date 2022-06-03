package kit.utilities;

import javafx.application.Platform;
import kit.*;
import kit.entity.CommunicationEntity;
import kit.entity.GroupEntity;
import kit.entity.UserEntity;
import server.JServer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * 功能类, 通过 mysql j connector 向数据库直接执行语句, 得到的结果作为方法返回值.
 *
 * 处理了所有的数据库错误, 通过控制台输出错误信息.
 */
public class MysqlUtilities
{
    private static Connection mysql_connection;

    public MysqlUtilities()
    {

        // 数据库地址, 账户
        String driver = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/chat_room?serverTimezone=Asia/Shanghai";
        String user = "root";
        String pass = "root";

        try{

            Class.forName(driver);

        } catch(ClassNotFoundException e) {

            // 没有 j connector 方法
            System.out.println("\n FATAL ERROR: j connector class not found");

            // 直接结束
            System.exit(-1);

        }

        // 不断尝试连接到数据库
        while( true ){
            try{
                mysql_connection = DriverManager.getConnection(url, user, pass);
                break;
            } catch(SQLException e) {

                // 无法连接至数据库
                System.out.println("\n WARNING: cannot connect to mysql, retry in 1000 milliseconds");
                mysql_connection = null;

                try{
                    Thread.sleep(1000);
                } catch(InterruptedException ignored) {
                    // 线程不会被打扰
                }
            } finally {
                System.out.println("\n Mysql Utilities: mysql connection successfully established.");
            }
        }
    }

    public static int register( Data data ) throws SQLException
    {
        addCurrentUsersAmount();
        int ID = getCurrentID();
        byte[] icon = data.iconBytes;
        PreparedStatement pstmt = mysql_connection.prepareStatement("INSERT INTO users_info(ID, name, signature, password, icon) VALUES(?, ?, ?, ?, ?)");
        pstmt.setInt(1, ID);
        pstmt.setString(2, data.name);
        pstmt.setString(3, data.signature);
        pstmt.setString(4, data.password);
        pstmt.setBlob(5, new ByteArrayInputStream(icon));
        pstmt.executeUpdate();
        pstmt.close();
        return ID;
    }

    private static int getCurrentID() throws SQLException
    {
        Statement stmt = mysql_connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT current_ID FROM global_info");
        int ID = -1;
        if( rs.next() ){
            ID = rs.getInt(1);
        }
        stmt.executeUpdate("UPDATE global_info SET current_ID=" + (ID + 1) + " WHERE current_ID=" + ID);
        stmt.close();
        return ID;
    }

    private static void addCurrentUsersAmount() throws SQLException
    {
        Statement stmt = mysql_connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT users FROM global_info");
        if( rs.next() ){
            stmt.executeUpdate("UPDATE global_info SET users=" + (rs.getInt(1) + 1) + " WHERE users=" + rs.getInt(1));
        }
        stmt.close();
    }

    public static Data selectByIDAndPassword( int id, String password ) throws SQLException
    {
        if( id < 0 ){
            return null;
        }
        Statement stmt = mysql_connection.createStatement();
        ResultSet rs1;
        String name;
        String sig;
        byte[] icon;
        try{
            PreparedStatement pstmt = mysql_connection.prepareStatement("select * from users_info where id=" + id);
            rs1 = pstmt.executeQuery();

            // 如果在数据库中查询了不存在的用户ID, 返回一个空集
            if( !rs1.next() ){
                return null;
            }

            if( !rs1.getString(4).equals(password) ){
                return null;
            }
            name = rs1.getString(2);
            sig = rs1.getString(3);
            Blob iconBlob = rs1.getBlob(5);
            icon = iconBlob.getBinaryStream().readAllBytes();
        } catch(SQLException | IOException e) {
            e.printStackTrace();
            return null;
        }
        ResultSet rs2;
        ResultSet rs3;
        ArrayList< UserEntity > friendList = new ArrayList<>();
        ArrayList< GroupEntity > groupList = new ArrayList<>();

        try{
            rs2 = stmt.executeQuery("select friend_name from friend_map where name='" + name + "'");
            while( rs2.next() ){
                String friendName = rs2.getString(1);
                Statement stmt2 = mysql_connection.createStatement();
                rs3 = stmt2.executeQuery("select * from users_info where name='" + friendName + "'");
                if( rs3.next() ){
                    Blob blob = rs3.getBlob(5);
                    friendList.add(new UserEntity(rs3.getInt(1), friendName, rs3.getString(3), blob.getBinaryStream().readAllBytes()));
                }
            }

            rs2 = stmt.executeQuery("select group_id from users_group where name='" + name + "'");
            while( rs2.next() ){
                int groupID = rs2.getInt(1);
                Statement stmt2 = mysql_connection.createStatement();
                rs3 = stmt2.executeQuery("select * from group_info where group_id=" + groupID);
                if( rs3.next() ){
                    Blob blob = rs3.getBlob(6);
                    ArrayList< UserEntity > list = (ArrayList< UserEntity >) getMembers(groupID, false);
                    groupList.add(new GroupEntity(groupID, rs3.getString(2), blob.getBinaryStream().readAllBytes(), list, 0));
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
        stmt.close();
        Data data = new Data();
        data.ID = id;
        data.name = name;
        data.signature = sig;
        data.listA = friendList;
        data.listB = groupList;
        data.iconBytes = icon;
        return data;
    }


    public static Data makeFriend( String info, String byName ) throws SQLException, IOException
    {
        Statement stmt = mysql_connection.createStatement();
        String name;
        UserEntity userEntity;
        ResultSet rs = stmt.executeQuery("SELECT * FROM users_info WHERE name='" + info + "'");
        if( rs.next() ){
            name = rs.getString(2);
            Blob blob = rs.getBlob(5);
            userEntity = new UserEntity(rs.getInt(1), name, rs.getString(3), blob.getBinaryStream().readAllBytes());
        } else {
            rs = stmt.executeQuery("SELECT * FROM users_info WHERE ID=" + info);
            if( rs.next() ){
                name = rs.getString(2);
                Blob blob = rs.getBlob(5);
                userEntity = new UserEntity(rs.getInt(1), name, rs.getString(3), blob.getBinaryStream().readAllBytes());
            } else {
                stmt.close();
                return new Data(-1);
            }
        }

        stmt.executeUpdate("INSERT INTO friend_map(name,friend_name) " + "VALUES('" + name + "','" + byName + "')");
        stmt.executeUpdate("INSERT INTO friend_map(name,friend_name) " + "VALUES('" + byName + "','" + name + "')");

        stmt.close();
        return new Data(userEntity);
    }

    public static void storeMessage( Message message ) throws Exception
    {

        // 如果消息不是群发, 群标记为 -1;
        // 所以在 Mysql 中储存信息的 group ID 为 int, 减少了一半的 group ID 空间
        if( !message.is_broadcast ){
            stroeMessage(message, -1);
        } else {

            // 获得群发消息接收者的 array list
            ArrayList< CommunicationEntity > list = (ArrayList< CommunicationEntity >) getMembers(message.receiver.getID(), false);

            // 给 list 上的每个人单发
            for( CommunicationEntity recID : list ){
                Message msg = new Message(recID, message.sender, message.mess_type_name, message.binary_text, message.date, true);
                stroeMessage(msg, message.receiver.getID());
            }
        }
    }

    private static void stroeMessage( Message message, int fromGroup ) throws SQLException
    {
        PreparedStatement pstmt = mysql_connection.prepareStatement("INSERT INTO messages(receiver, sender, ctype, content, datetime, fromgrp) VALUES(?, ?, ?, ?, ?, ?)");
        pstmt.setInt(1, message.receiver.getID());
        pstmt.setInt(2, message.sender.getID());
        pstmt.setString(3, message.mess_type_name);
        pstmt.setBlob(4, new ByteArrayInputStream(message.binary_text));
        pstmt.setTimestamp(5, new Timestamp(message.date.getTime()));
        pstmt.setInt(6, fromGroup);
        pstmt.execute();
    }

    public static Data loadDialogues( int ID ) throws Exception
    {
        PreparedStatement pstmt = mysql_connection.prepareStatement("SELECT * FROM messages WHERE receiver=?");
        pstmt.setInt(1, ID);
        ResultSet rs = pstmt.executeQuery();
        ArrayList< Message > dialogues = new ArrayList<>();
        while( rs.next() ){
            int sender = rs.getInt(2);
            String ctype = rs.getString(3);
            Blob contentBlob = rs.getBlob(4);
            byte[] content = new byte[(int) contentBlob.length()];
            InputStream inputStream = contentBlob.getBinaryStream();
            try{
                inputStream.read(content);
            } catch(IOException e) {
                e.printStackTrace();
            }
            Date date = new Date(rs.getTimestamp(5).getTime());
//            boolean isMass = rs.getBoolean(6);
            int fromGroup = rs.getInt(6);
            Message newMsg;
            if( fromGroup == -1 ){
                newMsg = new Message(getInfo(ID, false), getInfo(sender, false), ctype, content, date, false);
            } else {
                newMsg = new Message(getInfo(fromGroup, true), getInfo(sender, false), ctype, content, date, true);
            }

            dialogues.add(newMsg);
        }
        Statement stmt = mysql_connection.createStatement();
        stmt.executeUpdate("DELETE FROM messages WHERE receiver=" + ID + "");
        stmt.close();
        return new Data(dialogues);
    }

    private static CommunicationEntity getInfo( int id, boolean isGroup ) throws Exception
    {
        int ID = id;
        String name = null;
        byte[] icon = new byte[0];

        Statement stmt = mysql_connection.createStatement();
        ResultSet resultSet;

        if( !isGroup ){
            resultSet = stmt.executeQuery("SELECT * FROM group_info WHERE group_id = " + id);

            if( resultSet.next() ){

                name = resultSet.getString("group_name");
                icon = resultSet.getBlob("icon").getBinaryStream().readAllBytes();
            } else {
                return null;
            }
        } else {
            resultSet = stmt.executeQuery("SELECT * FROM users_info WHERE ID = " + id);

            if( resultSet.next() ){
                name = resultSet.getString("name");
                icon = resultSet.getBlob("icon").getBinaryStream().readAllBytes();

            } else {
                return null;
            }
        }

        return new CommunicationEntity(ID, name, icon);
    }

    private static void addCurrentGroupAmount() throws SQLException
    {
        Statement stmt = mysql_connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT grps FROM global_info");
        if( rs.next() ){
            stmt.executeUpdate("UPDATE global_info SET grps=" + (rs.getInt(1) + 1) + " WHERE grps=" + rs.getInt(1));
        }
        stmt.close();
    }

    private static int getCurrentGroupID() throws SQLException
    {
        Statement stmt = mysql_connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT current_group_ID FROM global_info");
        int ID = -1;
        if( rs.next() ){
            ID = rs.getInt(1);
        }
        stmt.executeUpdate("UPDATE global_info SET current_group_ID=" + (ID + 1) + " WHERE current_group_ID=" + ID);
        return ID;
    }

    public static int createGroup( Data data )
    {
        int ID;
        try{
            addCurrentGroupAmount();
            ID = getCurrentGroupID();
            PreparedStatement pstmt = mysql_connection.prepareStatement("INSERT INTO group_info" + "(group_id, group_name, builder, member_count, members, icon) VALUES(?, ?, ?, ?, ?, ?)");
            pstmt.setInt(1, ID);
            pstmt.setString(2, data.name);
            pstmt.setString(3, data.operatorInfo.getName());
            pstmt.setInt(4, 1);
            pstmt.setString(5, "" + data.operatorInfo.getID());
            pstmt.setBlob(6, new ByteArrayInputStream(data.iconBytes));
            pstmt.executeUpdate();

            Statement stmt = mysql_connection.createStatement();
            stmt.executeUpdate("INSERT INTO users_group(name, group_id) " + "VALUES(\'" + data.operatorInfo.getName() + "\',\'" + ID + "\')");
        } catch(Exception e) {
            e.printStackTrace();
            updateLog(data.operatorInfo.getName(), "创建群聊过程中，数据库操作出现错误");
            return -1;
        }

        return ID;
    }

    public static Data joinGroup( Data info )
    {
        //name, groupID
        //更新group members，count
        //更新用户的group info
        Data data = new Data();
        try{
            String name = info.operatorInfo.getName();
            int group_id = -1;
            boolean found = false;
            if( info.name != null ){
                PreparedStatement pstmt = mysql_connection.prepareStatement("select * from group_info where group_name=?");
                pstmt.setString(1, info.name);
                ResultSet rs = pstmt.executeQuery();
                if( rs.next() ){
                    group_id = rs.getInt(1);
                    String groupName = rs.getString(2);
                    String builder = rs.getString(3);
                    String memberStr = rs.getString(5);
                    Scanner scan = new Scanner(memberStr);
                    scan.useDelimiter(",");
                    List members = getMembers(group_id, false);
                    Blob blob = rs.getBlob(6);
                    byte[] bytes = blob.getBinaryStream().readAllBytes();

                    data.ID = group_id;
                    data.name = groupName;
                    data.builder = builder;
                    data.listA = members;
                    data.iconBytes = bytes;

                    found = true;
                }
            }
            if( !found && info.ID != -1 ){
                PreparedStatement pstmt = mysql_connection.prepareStatement("select group_id from group_info where group_id=?");
                pstmt.setInt(1, info.ID);
                ResultSet rs = pstmt.executeQuery();
                if( rs.next() ){
                    group_id = rs.getInt(1);
                    String groupName = rs.getString(2);
                    String builder = rs.getString(3);
                    String memberStr = rs.getString(5);
                    Scanner scan = new Scanner(memberStr);
                    scan.useDelimiter(",");
                    List members = getMembers(group_id, false);
                    Blob blob = rs.getBlob(6);
                    byte[] bytes = blob.getBinaryStream().readAllBytes();

                    data.ID = group_id;
                    data.name = groupName;
                    data.builder = builder;
                    data.listA = members;
                    data.iconBytes = bytes;

                    found = true;
                }
            }
            if( found ){

                Statement stmt = mysql_connection.createStatement();

                stmt.executeUpdate("INSERT INTO users_group(name,group_id) " + "VALUES('" + name + "','" + group_id + "')");

                PreparedStatement pstmt = mysql_connection.prepareStatement("select * from group_info where group_id=" + group_id);
                ResultSet rs = pstmt.executeQuery();
                rs.next();

                int memCount = rs.getInt(4);
                String members = rs.getString(5);

                memCount++;
                members = members + "," + info.operatorInfo.getID();

                stmt.executeUpdate("update group_info set member_count=" + memCount + " where group_id=" + group_id);
                stmt.executeUpdate("update group_info set members='" + members + "' where group_id=" + group_id);

                stmt.close();
            } else {
                data.ID = -1;
            }
        } catch(Exception e) {
            e.printStackTrace();
            updateLog(info.operatorInfo.getName(), "加入群聊过程中，数据库操作出现错误");
            data.ID = -1;
        }
        return data;
    }

    public static List getMembers( int groupID, boolean isOnlyId ) throws Exception
    {
        PreparedStatement pstmt = mysql_connection.prepareStatement("select members from group_info where group_id=" + groupID);
        ResultSet rs = pstmt.executeQuery();
        rs.next();

        String[] IDs = rs.getString(1).split(",");

        Statement stmt = mysql_connection.createStatement();

        ArrayList< UserEntity > members = new ArrayList<>();
        ArrayList< Integer > memberIDs = new ArrayList<>();

        for( String id : IDs ){
            int ID = Integer.parseInt(id);
            memberIDs.add(ID);
            if( isOnlyId ){
                continue;
            }

            rs = stmt.executeQuery("select * from users_info where ID='" + ID + "'");

            if( rs.next() ){
                Blob blob = rs.getBlob(5);
                members.add(new UserEntity(ID, rs.getString(2), rs.getString(3), blob.getBinaryStream().readAllBytes()));
            }
        }
        if( isOnlyId ){
            return memberIDs;
        } else {
            return members;
        }
    }

    private static void updateLog( String user, String log )
    {
        Platform.runLater(() -> {
            if( user != null ){
                JServer.update(user, log);
            } else {
                JServer.update(JServer.MAIN, log);
            }
        });
    }
}