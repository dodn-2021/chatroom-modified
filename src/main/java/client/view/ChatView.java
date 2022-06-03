package client.view;

import client.controller.ChatViewController;
import javafx.application.Platform;
import kit.Resource;
import kit.utilities.UiUtilities;
import kit.entity.CommunicationEntity;
import kit.Message;
import javafx.beans.property.ListProperty;
import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;
import javafx.scene.control.Label;

import javafx.scene.layout.*;

import javafx.stage.Stage;

import java.io.IOException;

import static kit.Resource.ChatViewResource;

public class ChatView extends Stage
{
    private final ChatViewController controller;

    public ChatView( CommunicationEntity to, ListProperty< Message > messageList, boolean is_group )
    {

        controller = new ChatViewController(to, messageList);
        controller.isGroup = is_group;

        try{
            FXMLLoader loader = new FXMLLoader();

            loader.setController(controller);

            // loader 获得 chatview resource 的位置.
            loader.setLocation(this.getClass().getResource(ChatViewResource));

            GridPane root = loader.load();

            ((Label) ((HBox) root.getChildren().get(0)).getChildren().get(0)).setText("" + to.getName());

            setTitle("chatting chamber");
            setScene(new Scene(root));
            setResizable(false);

            setOnCloseRequest(e -> Platform.runLater(() -> {
                if( controller.isGroup ){
                    MainView.clearGroupListSelection();
                } else {
                    MainView.clearFriendListSelection();
                }
            }));

        } catch(IOException e) {
            // class loader 无法正确载入信息
            System.out.println("\n FATAL ERROR: fxml loader unable to load ChatView");
            System.exit(-1);
        }
    }

    public ChatViewController getController()
    {
        return controller;
    }

}