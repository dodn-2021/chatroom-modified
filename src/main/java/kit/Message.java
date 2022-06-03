package kit;

import kit.entity.CommunicationEntity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message implements Comparable< Message >, Serializable
{
    public boolean is_broadcast;
    public CommunicationEntity receiver;
    public CommunicationEntity sender;
    public String mess_type_name;
    public byte[] binary_text;
    public Date date;
    private String url;

    public Message( CommunicationEntity receiver, CommunicationEntity sender, byte[] content, Date date, boolean is_broadcast )
    {
        this.receiver = receiver;
        this.sender = sender;
        this.mess_type_name = "text";
        this.binary_text = content;
        this.date = date;
        this.is_broadcast = is_broadcast;
    }

    public Message( CommunicationEntity receiver, CommunicationEntity sender, String mess_type_name, byte[] content, Date date, boolean is_broadcast )
    {
        this(receiver, sender, content, date, is_broadcast);
        this.mess_type_name = mess_type_name;
    }

    public Date getDate()
    {
        return date;
    }

    private String getFormattedDate()
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    @Override
    public String toString()
    {
        // 显示一个消息的具体信息
        return String.format("{receiver:%s,sender:%s,content:%s,date:%s}", receiver.getName(), sender.getName(), binary_text, date);
    }

    @Override
    public int compareTo( Message o )
    {
        return date.compareTo(o.date);
    }

    public String getHead()
    {
        return getFormattedDate() + " " + sender.getName() + ":\n";
    }

    public byte[] getBinary_text()
    {
        return binary_text;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    public String getUrl()
    {
        return this.url;
    }
}
