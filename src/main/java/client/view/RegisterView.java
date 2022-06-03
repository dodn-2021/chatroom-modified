package client.view;

import client.launcher.StageManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import kit.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

import static kit.Resource.RegisterViewResource;

public class RegisterView extends Stage {
    public RegisterView() {

        try{
            Pane root = FXMLLoader.load(Objects.requireNonNull(this.getClass().getResource(RegisterViewResource)));

            this.setTitle("Register");

            FileInputStream fileInputStream = new FileInputStream(new File("src/main/java/client/view/images/defaultUserIcon.jpeg"));
            ((ImageView) root.lookup("#selectedIcon")).setImage(new Image(fileInputStream));

            root.setOnMouseClicked(event -> {
                root.requestFocus();
            });

            this.setScene(new Scene(root));
            this.setOnCloseRequest(e -> StageManager.getManager().show(Resource.LoginViewID));
            setResizable(false);
            getIcons().add(new Image(String.valueOf(this.getClass().getResource("images/AppIcon.png"))));
        } catch(IOException | NullPointerException e) {
            System.out.println("\n FATAL ERROR: fxml loader unable to load RegisterView or default user icon");
            System.exit(-1);
        }
    }
}
