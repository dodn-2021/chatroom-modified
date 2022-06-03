package kit.utilities;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Optional;

public class UiUtilities
{
    public static void showAlert( String message )
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(null);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    public static void showMessage( String message )
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("information");
        alert.setContentText(null);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    public static void showWarning( String message )
    {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setContentText(null);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    public static boolean showConfirm( String header, String message )
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, new ButtonType("No", ButtonBar.ButtonData.NO), new ButtonType("Yes", ButtonBar.ButtonData.YES));
        alert.setTitle("确认");
        alert.setHeaderText(header);
        Optional< ButtonType > _buttonType = alert.showAndWait();
        return _buttonType.get().getButtonData().equals(ButtonBar.ButtonData.YES);
    }

    public static File showFileChooser( String title, String ... formats )
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        for( String format : formats ){
            fileChooser.getExtensionFilters().
                    add(new FileChooser.ExtensionFilter(format, "*." + format));
        }
        return fileChooser.showOpenDialog(null);
    }

    public static void FXMLLoaderIsLoadedTest( Class c, FXMLLoader loader )
    {
        System.out.println("\n DEBUG: fxml loader in class " + c.getName()
                + " loading file location: " + loader.getLocation());
    }
}
