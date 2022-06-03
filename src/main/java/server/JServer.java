package server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class JServer extends Application
{
    int cnt = 0;

    public static void main( String[] args )
    {
        launch(args);
    }

    Label show_selection_label = new Label("[None]");
    private static ListView< String > connected_users_list;

    private static TextArea log = new TextArea();

    public static String MAIN = "Main Page";

    public static HashMap< String, String > userLog = new HashMap<>();
    public static HashMap< String, ObservableValue< Boolean > > userSelected = new HashMap<>();

    @Override
    public void start( Stage primaryStage )
    {
        GridPane gridPane = new GridPane();

        Label pageLbl = new Label("Select Users:");
        connected_users_list = new ListView<>();
        connected_users_list.setPrefSize(120, 400);

        userLog.put("Main Page", "");

        userSelected.put("Main Page", new SimpleBooleanProperty(true));

        connected_users_list.setEditable(true);

        connected_users_list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        connected_users_list.getItems().addAll(userSelected.keySet());

        Callback< String, ObservableValue< Boolean > > itemToBoolean = ( String item ) -> userSelected.get(item);

        connected_users_list.setCellFactory(CheckBoxListCell.forListView(itemToBoolean));

        connected_users_list.getSelectionModel().selectedItemProperty().addListener(this::selectionChanged);

//        Button printBtn = new Button("Print Selected Log");
//        printBtn.setOnAction(e -> printSelection());

        gridPane.setHgap(10);
        gridPane.setVgap(5);

        gridPane.addColumn(0, pageLbl, connected_users_list);

        Label selectionLbl = new Label("Your selection: ");
        gridPane.add(selectionLbl, 0, 3);
        gridPane.add(show_selection_label, 1, 3, 2, 1);

        log = new TextArea();
        log.setPrefSize(400, 400);
        log.setWrapText(true);
        log.setEditable(false);

        gridPane.add(log, 1, 1);

        gridPane.setStyle("-fx-padding: 10;");
        Scene scene = new Scene(gridPane);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Server Log");
        primaryStage.setOnCloseRequest(( e ) -> {
            Platform.exit();
            System.exit(0);
        });

        connected_users_list.getSelectionModel().selectFirst();

        primaryStage.show();

        new Thread(() -> {

            // 新建一个 host monitor 的实例, 开始运行
            new HostMonitor();

        }).start();

    }

    public void selectionChanged( ObservableValue< ? extends String > observable, String oldValue, String newValue )
    {
        ObservableList< String > selectedItems = connected_users_list.getSelectionModel().getSelectedItems();
        String selectedValues = (selectedItems.isEmpty()) ? "[None]" : selectedItems.toString();
        this.show_selection_label.setText(selectedValues);
        log.setText(userLog.get(connected_users_list.getSelectionModel().getSelectedItem()));
    }

    // log area 使用的时间前缀格式
    private static final SimpleDateFormat console_prefix_time_format = new SimpleDateFormat("yy/MM/dd hh:mm:ss.SSS");

    public static void update( String user, String s )
    {

        // 在数据库中更新记录的格式: 时间 + 信息 + 空一行
        String cur_date_time = console_prefix_time_format.format(new Date(System.currentTimeMillis()));
        String to_append = "[" + cur_date_time + "] " + s + "\n\n";

        // 首先在当前视图进行更新, 因为切换视图会导致自动更新 (从 user log 字典中读取)
        if( connected_users_list.getSelectionModel().getSelectedItem() != null && connected_users_list.getSelectionModel().getSelectedItem().equals(user) ){
            log.appendText(to_append);
        }

        // 在 user log 中更新 user 信息
        String l = userLog.get(user);
        l = l + to_append;
        userLog.put(user, l);
    }

    public static void addUser( String user )
    {
        if( userLog.containsKey(user) ){
            return;
        }

        connected_users_list.getItems().add(user);
        userLog.put(user, "");
        userSelected.put(user, new SimpleBooleanProperty(false));
    }
}
