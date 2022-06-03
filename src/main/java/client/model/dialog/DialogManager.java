package client.model.dialog;

import client.model.Friend;
import client.model.Group;
import client.model.User;
import client.model.dialog.FriendDialog;
import client.model.dialog.GroupDialog;
import javafx.beans.property.MapProperty;
import kit.entity.GroupEntity;
import kit.entity.CommunicationEntity;
import kit.entity.UserEntity;

import java.io.*;

public class DialogManager
{
    private String userName;
    private String mDirPath;
    private File mDir;

    public DialogManager( String userName )
    {

        // 预先设置 data 文件夹

        this.userName = userName;

        String data_folder_path = "out/production/chatroom/client/data";

        File data_folder = new File(data_folder_path);
        if( !data_folder.exists() ){
            if( !data_folder.mkdir() ){
                System.out.println("\n WARNING: unable to create data folder location: " + data_folder.getPath());
            }
        }

//        StringBuilder mDirPath = new StringBuilder("out/production/chatroom/client/data/");
//        mDirPath.append(userName);
//        mDir = new File(mDirPath.toString());
        mDirPath = "out/production/chatroom/client/data/" + userName + "/";
        mDir = new File(mDirPath);

        // 测试 username 所属文件夹是否存在, 如果不存在就新建这个文件夹
        File username_folder = new File(mDirPath.substring(0, mDirPath.length() - 1));
//        System.out.println(mDirPath.substring(0, mDirPath.length() - 1));

        if( !username_folder.exists() ){
            if( !username_folder.mkdir() ){
                System.out.println("\n WARNING: unable to create data folder location: " + username_folder.getPath());
            }
        }

        // 写入 User 自己的图片文件
        File userIcon = new File("out/production/chatroom/client/data/" + userName + "/icon.png");
        storeIcon(userIcon, User.getInstance().getMyIconBytes());

    }

    /**
     * @param friends 对所有好友列表中的好友, 读取本地聊天记录
     * @throws IOException 不能正确读取聊天记录
     */
    public void initFriendsDialog( MapProperty< Integer, Friend > friends ) throws IOException
    {
        for( Friend friend : friends.values() ){
            UserEntity info = friend.getUserEntity();
            String friendName = friend.getFriendName();
            File friendDir = new File(mDirPath + friendName);
            if( !friendDir.exists() ){
                friendDir.mkdir();
            }
            File friendDialogFile = new File(
                    mDirPath + friendName + "/dialog.dat");

            // 读入或初始化 dialogue
            FriendDialog friendDialog = null;
            if( !friendDialogFile.exists() ){

                // 不存在好友会话记录

                System.out.println("\n WARNING: friend dialog file does not exist: " + friendDialogFile.getPath());

                // 创建新的会话记录

                friendDialogFile.createNewFile();
                friendDialog = new FriendDialog(info, userName);

            } else {
                FileInputStream is = new FileInputStream(friendDialogFile);
                if( is.available() != 0 ){
                    ObjectInputStream ois = new ObjectInputStream(is);
                    try{
                        friendDialog = (FriendDialog) ois.readObject();
                    } catch(ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch(InvalidClassException ignored) {
                        // 如果 serial version uid 不一样, 无视它
                    } finally {
                        ois.close();
                        is.close();
                    }
                } else {

                    // 如果会话记录为空, 初始化会话信息

                    System.out.println("\n WARNING: cannot initiate on an empty dialog.dat file: " + friendDialogFile.getPath());

                    // 创建新的会话记录

                    friendDialogFile.createNewFile();
                    friendDialog = new FriendDialog(info, userName);

                }
            }
            friend.init(friendDialog);
        }
    }

    public void initGroupsDialog( MapProperty< Integer, Group > groups ) throws IOException
    {
        for( Group group : groups.values() ){
            GroupEntity info = group.getGroupInfo();
            String groupName = info.getName();
            File groupDir = new File(mDirPath + groupName);
            if( !groupDir.exists() ){
                groupDir.mkdir();
            }
            File groupDialogFile = new File(
                    mDirPath + groupName + "/dialog.dat");

            //读入或初始化dialogue
            GroupDialog groupDialog = null;
            if( !groupDialogFile.exists() ){
                groupDialogFile.createNewFile();
                groupDialog = new GroupDialog(User.getInstance().getName(),
                        info, info.getMembers());
            } else {
                FileInputStream is = new FileInputStream(groupDialogFile);
                ObjectInputStream ois = new ObjectInputStream(is);
                try{
                    groupDialog = (GroupDialog) ois.readObject();
                } catch(ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    ois.close();
                    is.close();
                }
            }
            group.init(groupDialog);
        }
    }

    public void updateMyDialogues( MapProperty< Integer, Friend > friends, MapProperty< Integer, Group > groups )
            throws IOException
    {
        for( Friend friend : friends.values() ){
            File friendDialogFile = new File(mDirPath + friend.getFriendName() + "/dialog.dat");
            OutputStream os = new FileOutputStream(friendDialogFile);
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(friend.getFriendDialog());
            oos.close();
            os.close();
        }
        for( Group group : groups.values() ){
            File friendDialogFile = new File(mDirPath + group.getGroupInfo().getName() + "/dialog.dat");
            OutputStream os = new FileOutputStream(friendDialogFile);
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(group.getGroupDialog());
            oos.close();
            os.close();
        }
    }

    public void storeIcon( CommunicationEntity info )
    {
        File dir = new File(mDirPath + info.getName());
        if( !dir.exists() ){
            dir.mkdir();
        }
        File file = new File(mDirPath + info.getName() + "/icon.jpg");
        info.setIconPath("file:" + mDirPath + info.getName() + "/icon.jpg");
        storeIcon(file, info.getIcon());
    }

    public void storeIcon( File file, byte[] bytes )
    {
        if( !file.exists() ){
            try{
                file.createNewFile();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        try{
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.flush();
            fos.close();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public String getMDirPath()
    {
        return mDirPath;
    }

}