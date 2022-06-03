package client.launcher;

import client.view.*;
import javafx.application.Application;
import javafx.stage.Stage;
import kit.Resource;

import java.io.IOException;


public class JClient extends Application
{

    public static void main( String[] args )
    {
        launch(args);
    }

    @Override
    public void start( Stage primaryStage ) throws IOException
    {

        LoginView loginView = new LoginView();
        StageManager.getManager().addStage(Resource.LoginViewID, loginView);
        StageManager.getManager().show(Resource.LoginViewID);

    }
}
