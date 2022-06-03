package client.controller;

import kit.utilities.UiUtilities;
import client.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.TextField;

import javafx.scene.layout.GridPane;
import kit.utilities.CommunicationUtilities;

import java.io.IOException;

public class AddFriendViewController
{
    @FXML
    private GridPane root;
    @FXML
    private TextField friendNameField;
    @FXML
    private TextField groupNameField;

    public void SearchButtonAction( ActionEvent actionEvent )
    {
        String friendName;
        friendName = friendNameField.getText();
        if( friendName == null ){
            UiUtilities.showWarning("请输入好友的信息");
            return;
        }
        if( friendName.equals(User.getInstance().getName()) ){
            UiUtilities.showWarning("添加的好友不能为自己");
            return;
        }
        if( User.getInstance().getFriendIDs().contains(friendName) ){
            UiUtilities.showMessage("你已添加 " + friendName + " 为好友");
            return;
        }
        try{
            if( UiUtilities.showConfirm("确认添加好友", friendName) ){
                if( CommunicationUtilities.makeFriendWith(friendName) ){
//                    User.getInstance().addFriend(friendName);
                    UiUtilities.showMessage("添加好友成功");
                } else {
                    UiUtilities.showMessage("好友未找到，请确认好友信息");
                }
            } else {
                return;
            }

        } catch(IOException e) {
            UiUtilities.showAlert(e.getMessage());
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void searchGroupButtonAction( ActionEvent actionEvent )
    {
        String groupName;
        groupName = groupNameField.getText();
        if( groupName == null ){
            UiUtilities.showWarning("请输入群信息");
            return;
        }

        if( User.getInstance().getGroups().containsKey(groupName) ){
            UiUtilities.showMessage("你已添加群" + groupName);
            return;
        }
        try{
            if( UiUtilities.showConfirm("确认进群", groupName) ){
                if( CommunicationUtilities.joinGroup(groupName) ){
//                    User.getInstance().addFriend(friendName);
                    UiUtilities.showMessage("成功进入群");
                } else {
                    UiUtilities.showMessage("未找到该群，请确认群信息");
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void keyAction( ActionEvent actionEvent )
    {
        SearchButtonAction(new ActionEvent());
    }
}
