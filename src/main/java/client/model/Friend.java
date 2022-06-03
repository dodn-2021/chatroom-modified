package client.model;

import client.model.dialog.FriendDialog;
import kit.entity.UserEntity;

import java.io.IOException;

public class Friend
{
    private UserEntity userEntity;
    private FriendDialog friendDialog;

    public Friend() {}

    public Friend( UserEntity userEntity )
    {
        this.userEntity = userEntity;
        User.getInstance().getManager().storeIcon(userEntity);
        this.userEntity.prepareUserCard();
    }

    /**
     * @param dialog 将收集到的对话用来初始化一个好友
     * @throws IOException set chat view 返回
     */
    public void init( FriendDialog dialog ) throws IOException
    {
        friendDialog = dialog;
        if( friendDialog.getHasNewMessage().get() ){
            userEntity.getUserCard().showCircle();
        }
        friendDialog.setChatView();
        friendDialog.getHasNewMessage().addListener(( obs, ov, nv ) -> {
            if( nv ){
                userEntity.getUserCard().showCircle();
            } else {
                userEntity.getUserCard().hideCircle();
            }
        });
    }

    public void setFriendDialog( FriendDialog friendDialog )
    {
        this.friendDialog = friendDialog;
    }

    public void setUserInfo( UserEntity userEntity )
    {
        this.userEntity = userEntity;
    }

    public UserEntity getUserEntity()
    {
        return userEntity;
    }

    public FriendDialog getFriendDialog()
    {
        return friendDialog;
    }

    public String getFriendName()
    {
        return userEntity.getName();
    }

}
