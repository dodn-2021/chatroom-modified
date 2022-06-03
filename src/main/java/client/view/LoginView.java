package client.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import kit.Resource;

import java.io.IOException;
import java.util.Objects;

import static kit.Resource.LoginViewResource;

/**
 * 登陆窗口
 */

public class LoginView extends Stage
{

    public LoginView()
    {
        try{
            // 从 resource 类中的 login view resource String类中获得 fxml 文件的路径
            Parent root = FXMLLoader.load(Objects.requireNonNull(this.getClass().getResource(LoginViewResource)));

            ((TextField) root.lookup("#IDField")).setPromptText("请输入你的ID");
            ((TextField) root.lookup("#passwordField")).setPromptText("请输入你的密码");
            root.setOnMouseClicked(event -> root.requestFocus());

            this.setScene(new Scene(root));
            setResizable(false);
            getIcons().add(new Image(String.valueOf(this.getClass().getResource("images/AppIcon.png"))));

        } catch(NullPointerException | IOException e) {
            // 无法读取 fxml
            System.out.println("\n FATAL ERROR: fxml loader unable to load LoginView");
            System.exit(-1);
        }
    }
}
