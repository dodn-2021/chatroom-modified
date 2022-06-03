package client.model;

import client.model.dialog.GroupDialog;
import kit.entity.GroupEntity;

import java.io.IOException;

public class Group {
    private GroupEntity groupEntity;
    private GroupDialog groupDialog;

    public Group( GroupEntity info){
        this.groupEntity = info;
        User.getInstance().getManager().storeIcon(groupEntity);
        groupEntity.prepareGroupCard();
    }

    public void init(GroupDialog dialog) throws IOException {
        groupDialog = dialog;
        if (groupDialog.getHasNewMessage().get()) groupEntity.getGroupCard().showCircle();
        groupDialog.setChatView();
        groupDialog.getHasNewMessage().addListener((obs, ov, nv)->{
            if (nv) groupEntity.getGroupCard().showCircle();
            else groupEntity.getGroupCard().hideCircle();
        });
    }

    public String getGroupName() {
        return groupEntity.getName();
    }

    public GroupEntity getGroupInfo() {return groupEntity;}

    public GroupDialog getGroupDialog() {return groupDialog;}
}
