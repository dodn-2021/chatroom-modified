package kit.entity;

import java.io.Serializable;

public class CommunicationEntity implements Serializable
{
    protected int ID;
    protected String name;
    protected byte[] icon;
    protected String iconPath;

    public CommunicationEntity()
    {
    }

    public CommunicationEntity( int ID, String name, byte[] icon )
    {
        this.ID = ID;
        this.name = name;
        this.icon = icon;
    }


    public int getID()
    {
        return ID;
    }

    public void setID( int ID )
    {
        this.ID = ID;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public byte[] getIcon()
    {
        return icon;
    }

    public void setIcon( byte[] icon )
    {
        this.icon = icon;
    }

    public String getIconPath()
    {
        return iconPath;
    }

    public void setIconPath( String iconPath )
    {
        this.iconPath = iconPath;
    }

    public String toString()
    {
        return String.format("{ID: %d, name: %s}", ID, name);
    }
}
