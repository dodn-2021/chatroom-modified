package client.view;

import client.model.User;

import javafx.scene.Node;
import kit.Resource;
import kit.UserCard;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

public class MainView extends Stage
{
    private static ListView< UserCard > friendListView;
    private static ListView< UserCard > groupListView;

    public MainView( User user )
    {

        SplitPane root = null;
        try{
            root = FXMLLoader.load(Objects.requireNonNull(this.getClass().getResource(Resource.MainViewResource)));
        } catch(IOException e) {
            System.out.println("\n FATAL ERROR: fxml loader unable to load MainView");
            System.exit(-1);
        }

        ObservableList< Node > items = root.getItems();
        GridPane userInfoGridPane = (GridPane) items.get(0);
        TabPane tabPane = (TabPane) items.get(1);
        ObservableList< Tab > tabs = tabPane.getTabs();
        ((Tab) tabs.get(0)).getContent().lookup("#friendListView");

        ((Label) userInfoGridPane.getChildren().get(5)).setText(String.valueOf(user.getID()));
        ((Label) userInfoGridPane.getChildren().get(5)).setTooltip(new Tooltip(String.valueOf(user.getID())));

        ((Label) userInfoGridPane.getChildren().get(4)).setText(user.getName());
        ((Label) userInfoGridPane.getChildren().get(4)).setTooltip(new Tooltip(user.getName()));

        ((Label) userInfoGridPane.getChildren().get(7)).setText(user.getSignature());
        ((Label) userInfoGridPane.getChildren().get(7)).setTooltip(new Tooltip(user.getSignature()));

        File userIcon = new File("out/production/chatroom/client/data/" + user.getName() + "/icon.png");

        try{
            ((ImageView) userInfoGridPane.getChildren().get(0)).setImage(new Image(new FileInputStream(userIcon)));
        } catch(FileNotFoundException ignored) {
        }
        friendListView = (ListView< UserCard >) ((Tab) tabs.get(0)).getContent().lookup("#friendListView");
        groupListView = (ListView< UserCard >) ((Tab) tabs.get(1)).getContent().lookup("#groupListView");

        setScene(new Scene(root));

        setOnCloseRequest(( e ) -> {
            try{
                // 关闭时向服务器发送 exit 信息
                User.getInstance().exit();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            Platform.exit();
            System.exit(0);
        });
        setOnShowing(( e ) -> {
            this.requestFocus();
        });
        setTitle("Chatting Room");
        getIcons().add(new Image(String.valueOf(this.getClass().getResource("images/AppIcon.png"))));
    }

    public static void clearFriendListSelection()
    {
        friendListView.getSelectionModel().clearSelection();
    }

    public static void clearGroupListSelection()
    {
        groupListView.getSelectionModel().clearSelection();
    }
}
