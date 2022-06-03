package kit.entity;

import kit.UserCard;

import java.io.Serializable;
import java.util.List;

public class GroupEntity extends CommunicationEntity implements Serializable
{
    private transient UserCard groupCard;
    private List< UserEntity > members;
    private int groupOwner;

    public GroupEntity( int groupID, String groupName, byte[] groupIcon, List< UserEntity > members, int groupOwner )
    {
        super(groupID, groupName, groupIcon);
        this.members = members;
        this.groupOwner = groupOwner;
    }


    public void prepareGroupCard()
    {
        groupCard = new UserCard(ID, name, "group", iconPath);
    }

    public UserCard getGroupCard()
    {
        return groupCard;
    }

    public void setGroupCard( UserCard groupCard )
    {
        this.groupCard = groupCard;
    }

    public List< UserEntity > getMembers()
    {
        return members;
    }

    public void setMembers( List< UserEntity > members )
    {
        this.members = members;
    }
}
