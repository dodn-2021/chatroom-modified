package client.controller;

import kit.utilities.UiUtilities;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import kit.Data;
import kit.utilities.CommunicationUtilities;

import java.io.*;

public class RegisterViewController
{
    @FXML
    private GridPane root;
    @FXML
    private TextField userName;
    @FXML
    private TextField userPassword;
    @FXML
    private TextField userPasswordConfirm;
    @FXML
    private TextField userSignature;
    @FXML
    private ImageView selectedIcon;

    private String IconUrl = "src/main/java/client/view/images/defaultUserIcon.jpeg";

    @FXML
    protected void submitButton( ActionEvent event )
    {
        if( userName.getText().isEmpty() ){
            UiUtilities.showAlert("昵称不能为空");
            return;
        }
        if( userPassword.getText().isEmpty() ){
            UiUtilities.showAlert("请设置密码");
            return;
        }
        if( userPasswordConfirm.getText().isEmpty() ){
            UiUtilities.showAlert("请再次输入密码");
            return;
        }
        String name = userName.getText();
        String password = userPassword.getText();
        String passwordConfirm = userPasswordConfirm.getText();
        String signature = userSignature.getText();

        if( !password.equals(passwordConfirm) ){
            UiUtilities.showAlert("两次输入的密码不同,请重新输入");
            userPasswordConfirm.setText("");
            return;
        }
        try{
//            int ID = Integer.parseInt(Connector.getInstance().register(name, password, signature));

            UiUtilities.showMessage("your image url is: " + IconUrl);
            File file = new File(IconUrl);
            FileInputStream fileInputStream = new FileInputStream(file);
//            selectedIcon.setImage(new Image(fileInputStream));
            byte[] iconByte = new byte[(int) (file.length() + 1)];
            fileInputStream.read(iconByte);
            int ID = CommunicationUtilities.register(new Data(name, password, signature, iconByte));
            UiUtilities.showMessage("你获得的ID为" + ID);
        } catch(Exception e) {
            e.printStackTrace();
            UiUtilities.showAlert("服务器分配ID错误");
        }
    }

    @FXML
    public void paneKeyAction( KeyEvent keyEvent )
    {
        if( keyEvent.getCode() == KeyCode.ENTER ){
            submitButton(new ActionEvent());
        }
    }

    public void chooseFile( ActionEvent actionEvent ) throws IOException
    {
        File file = UiUtilities.showFileChooser("选择头像文件", "jpg", "png", "jpeg");
        if( file != null ){
            IconUrl = file.getPath();
            FileInputStream fileInputStream = new FileInputStream(file);
            selectedIcon.setImage(new Image(fileInputStream));
            fileInputStream.close();
        }
    }
}
