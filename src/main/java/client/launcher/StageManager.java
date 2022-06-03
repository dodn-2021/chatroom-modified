package client.launcher;

import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理主要的 stage, loginStage, registerStage, addFriendStage, mainStage.
 *
 * @apiNote 通过这个类统一实现stage的关闭和显示, 切换stage
 */
public class StageManager
{
    private static final StageManager stageManager = new StageManager();

    public static StageManager getManager()
    {
        return stageManager;
    }

    private static final Map< String, Stage > stageMap = new HashMap<>(); // 通过 Resource 中的名称来查找对应的 stage 实例

    public void addStage( String viewID, Stage stage )
    {
        stageMap.put(viewID, stage);
    }

    public void resetStage( String viewID, Stage stage )
    {
        if( stageMap.get(viewID) == null ){
            stageMap.put(viewID, stage);
            return;
        }
        stageMap.replace(viewID, stage);
    }

    public Stage getStage( String viewID )
    {
        return stageMap.get(viewID);
    }

    public void show( String viewID )
    {
        getStage(viewID).show();
    }

    public void close( String viewID )
    {
        getStage(viewID).close();
    }

    public void shift( String viewIDA, String viewIDB )
    {
        close(viewIDA);
        show(viewIDB);
    }
}