package client.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

import static kit.Resource.AddFriendViewResource;

public class AddFriendView extends Stage {
    public AddFriendView() {

        try{
            GridPane root = FXMLLoader.load(Objects.requireNonNull(this.getClass().getResource(AddFriendViewResource)));

            this.setTitle("Add New Friend");

            root.setOnMouseClicked(event -> root.requestFocus());

            this.setScene(new Scene(root, 600, 300));
            setResizable(false);
            getIcons().add(new Image(String.valueOf(this.getClass().getResource("images/AppIcon.png"))));

        } catch(NullPointerException | IOException e ) {

            // 在 loader 中没有成功加载
            System.out.println("\n FATAL ERROR: fxml loader unable to load AddFriendView");
            System.exit(-1);

        }
    }
}
