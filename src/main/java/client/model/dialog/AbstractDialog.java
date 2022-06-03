package client.model.dialog;

import client.model.User;
import client.view.ChatView;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import kit.entity.CommunicationEntity;
import kit.Message;

import javax.imageio.stream.FileImageOutputStream;
import java.io.*;
import java.util.Date;

public abstract class AbstractDialog implements Serializable
{

    protected transient ListProperty< Message > messageList;
    protected transient String userA;

    protected transient ChatView chatView;
    protected transient BooleanProperty hasNewMessage;

    public AbstractDialog()
    {
    }

    public AbstractDialog( String userA )
    {
        this.userA = userA;
        ObservableList< Message > observableList = FXCollections.observableArrayList();
        this.messageList = new SimpleListProperty<>(observableList);
        this.hasNewMessage = new SimpleBooleanProperty(false);
    }

    public abstract void setChatView() throws IOException;

    public void synchronizeMessage()
    {
        // 调用 controller 的同步信息方法
        chatView.getController().synchroniseMessages(messageList);
    }

    public void updateMessage( Message message )
    {
        CommunicationEntity user_id = User.getInstance().getUserInfo();
        CommunicationEntity to;
        if( message.is_broadcast ){
            to = message.receiver;
        } else {
            to = (message.sender == user_id) ? message.receiver : message.sender;
        }

        switch( message.mess_type_name.replaceAll("/.*", "") ){
            case "image" -> {

                // 修改为保存在用户名称 -> 对话人用户名 -> images 的文件夹下
                String img_dir_path = "out/production/chatroom/client/data/" + User.getInstance().getName() + "/" + to.getName() + "/images";
                File img_directory = new File(img_dir_path);
                if( !img_directory.exists() ){
                    // 不存在聊天图片存储文件夹
                    System.out.println("\n WARNING: img file folder does not exist: " + img_dir_path);

                    // 创建新的聊天图片存储文件夹
                    img_directory.mkdir();
                }
                Date imgDate = new Date();
                String imgSuffix = "." + message.mess_type_name.replaceAll(".*/", "");

                // 合并图像文件夹路径, 加上图像时间戳, 还有图像后缀.
                // 得到保存在会话文件夹中的图像文件路径
                String img_file_path = img_directory.getPath() + "/img" + imgDate.getTime() + imgSuffix;

                // 测试文件路径
                System.out.println("\n DEBUG: the image file path is: " + img_file_path);

                File imgFile = new File(img_directory.getPath() + "/img" + imgDate.getTime() + imgSuffix);
                message.setUrl(".." + imgFile.getPath().replaceAll(".*?client", "").replaceAll("\\\\", "/"));
                try{
                    imgFile.createNewFile();
                    FileImageOutputStream fios = new FileImageOutputStream(imgFile);
                    fios.write(message.binary_text);
                    fios.flush();
                    fios.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
            case "audio" -> {
                File audDir = new File("out/production/chatroom/client/data/" + User.getInstance().getName() + "/" + to.getID() + "/audios");
                Date audDate = new Date();
                String audSuffix = "." + message.mess_type_name.replaceAll(".*/", "");
                File audFile = new File(audDir.getPath() + "/aud" + audDate.getTime() + audSuffix);
                message.setUrl(".." + audFile.getPath().replaceAll(".*?client", "").replaceAll("\\\\", "/"));
                try{
                    audFile.createNewFile();
                    FileImageOutputStream fios = new FileImageOutputStream(audFile);
                    fios.write(message.binary_text);
                    fios.flush();
                    fios.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
            default -> {
            }
        }
        if( !chatView.isShowing() ){
            hasNewMessage.set(true);
        }
        messageList.add(message);
    }

    public ListProperty< Message > getMessageList()
    {
        return messageList;
    }

    public void show()
    {
        chatView.show();
    }

    public void hide()
    {
        chatView.hide();
    }

    public ChatView getChatView()
    {
        return chatView;
    }

    public BooleanProperty getHasNewMessage()
    {
        return hasNewMessage;
    }

}

