package client.controller;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import kit.entity.CommunicationEntity;
import kit.Message;
import client.model.User;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.layout.TilePane;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import kit.utilities.UiUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatViewController implements Initializable
{
    private final Font EMOJI_FONT = Font.font("Segoe UI Emoji", 20);
    @FXML
    private GridPane root;
    @FXML
    private Label chatToLabel;
    @FXML
    private WebView dialogView;
    @FXML
    private WebView show;
    @FXML
    private TextArea typeArea;
    @FXML
    private TilePane emojiView;
    @FXML
    private ToggleButton emojiControl;
    @FXML
    private ToggleButton fmlControl;

    private ListProperty< Message > messageList;
    public boolean isGroup;
    private WebEngine webEngine1;
    private WebEngine webEngine2;
    private Document document;
    private Node body;


    private SnuggleEngine engine = new SnuggleEngine();
    private SnuggleSession session = engine.createSession();
    private CommunicationEntity chatTo;

    public ChatViewController( CommunicationEntity chatToID, ListProperty< Message > messageList )
    {
        this.chatTo = chatToID;
        this.messageList = messageList;
    }

    @FXML
    private void sendMessage() throws Exception
    {
        String content = typeArea.getText();
        typeArea.setText("");
        if( content.equals("") ){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("warning: can not send an empty message!");
            alert.show();
            return;
        }
        synchronized(dialogView){
            Date now = new Date();
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            Message message = new Message(chatTo, User.getInstance().getUserInfo(), contentBytes, now, isGroup);
            User.getInstance().sendMessage(message);
        }
    }

    @FXML
    private void imageSelect() throws Exception
    {
        File file = UiUtilities.showFileChooser("选择要发送的图片", "jpg", "png", "jpeg", "bmp");
        if( file == null ){
            return;
        }
        String ctype = Files.probeContentType(Paths.get(file.getPath()));
        FileImageInputStream fiis = new FileImageInputStream(file);
        byte[] content = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while( (len = fiis.read(buf)) != -1 ){
            baos.write(buf, 0, len);
        }
        content = baos.toByteArray();
        Date date = new Date();
        Message message = new Message(chatTo, User.getInstance().getUserInfo(), ctype, content, date, isGroup);
        // 在 html 中连接文件时只能从当前目录出发, 绝对路径和 project 下路径都没有效果
        User.getInstance().sendMessage(message);
        baos.close();
        fiis.close();
    }

    @FXML
    private void audioSelect() throws Exception
    {
        File file = UiUtilities.showFileChooser("选择要发送的声音", "mp3", "mpeg", "wma", "aac");
        if( file == null ){
            return;
        }
        String ctype = Files.probeContentType(Paths.get(file.getPath()));
        FileImageInputStream fiis = new FileImageInputStream(file);
        byte[] content = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while( (len = fiis.read(buf)) != -1 ){
            baos.write(buf, 0, len);
        }
        content = baos.toByteArray();
        Date date = new Date();
        Message message = new Message(chatTo, User.getInstance().getUserInfo(), ctype, content, date, isGroup);
        // 在 html 中连接文件时只能从当前目录出发, 绝对路径和 project 下路径都没有效果
        User.getInstance().sendMessage(message);
        baos.close();
        fiis.close();
    }

    @Override
    public void initialize( URL location, ResourceBundle resources )
    {
        typeArea.setWrapText(true);

        webEngine1 = dialogView.getEngine();
        webEngine1.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:71.0) Gecko/20100101 Firefox/71.0");
        webEngine1.getLoadWorker().stateProperty().addListener(( obs, ov, nv ) -> {
            if( nv == Worker.State.SUCCEEDED ){
                document = webEngine1.getDocument();
                body = document.getElementsByTagName("body").item(0);
                for( Message message : messageList ){
                    showMessage(message);
                }
            }
        });

        webEngine1.load(getClass().getResource("Dialog.html").toExternalForm());
        webEngine2 = show.getEngine();

        typeArea.textProperty().addListener(new ChangeListener< String >()
        {
            @Override
            public void changed( ObservableValue< ? extends String > observable, String oldValue, String newValue )
            {
                if( show.isVisible() ){
                    webEngine2.loadContent(flushIntoNode());
                }
            }
        });
        typeArea.setFont(EMOJI_FONT);

        // 将 unicode 编码为 0x1F600 到 0x1F644 的所有 emoji 写到一个个的 Label 上
        for( int i = 0x1F600; i < 0x1F644; i++ ){
            Label label = new Label(Character.toString(i));
            label.setFont(EMOJI_FONT);
            label.setOnMouseClicked(( e ) -> typeArea.setText(typeArea.getText() + label.getText()));
            emojiView.getChildren().add(label);
        }

        ToggleGroup group = new ToggleGroup();
        emojiControl.setToggleGroup(group);
        fmlControl.setToggleGroup(group);
        group.selectedToggleProperty().addListener(( obs, ov, nv ) -> {
            if( nv == null ){
                emojiView.setVisible(false);
                emojiView.setManaged(false);
                show.setVisible(false);
                show.setManaged(false);
            } else if( nv == emojiControl ){
                show.setVisible(false);
                show.setManaged(false);
                emojiView.setVisible(true);
                emojiView.setManaged(true);
            } else if( nv == fmlControl ){
                emojiView.setVisible(false);
                emojiView.setManaged(false);
                show.setVisible(true);
                show.setManaged(true);
            }
        });

    }

    public void synchroniseMessages( ListProperty< Message > messageList )
    {
        messageList.addListener(( obs, ov, nv ) -> {

            // 每次读取最后一条消息
            Message newMessage = nv.get(nv.size() - 1);

            showMessage(newMessage);
        });
    }

    /**
     * 在 friend dialog show chat view 时, 运行这个方法让聊天滑到底端
     */
    public void scrollToBottom()
    {

        Platform.runLater(() -> {
            webEngine1.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        });

    }

    private String snuggleTranslateBlock( String s )
    {
        SnuggleInput input = new SnuggleInput(s);
        try{
            session.parseInput(input);
        } catch(IOException ignored) {
            // 不考虑转换公式错误
        }
        String out = session.buildXMLString();
        out = out.replaceAll(" display=\"block\"", "");
        input = null;
        session.reset();
        return out;
    }

    public void snuggleTranslateInText( String s, Element element )
    {
        SnuggleInput input = new SnuggleInput(s);
        try{
            session.parseInput(input);
        } catch(IOException ignored) {
            // 不考虑公式转换失败的情况
        }

        session.buildDOMSubtree(element);
        input = null;
        session.reset();
    }

    /**
     * @return 在公式侧栏上显示的预览
     */
    private String flushIntoNode()
    {
        String text = typeArea.getText();
        Pattern p = Pattern.compile("\\$\\$.*?\\$\\$");
        Matcher m = p.matcher(text);
        StringBuilder result = new StringBuilder();
        int lt = 0, rt = 0;
        while( m.find() ){
            rt = m.start();
            result.append(text.substring(lt, rt));
            lt = m.end();
            result.append(snuggleTranslateBlock(text.substring(rt, lt)));
        }
        result.append(text.substring(lt));
        return result.toString();
    }

    /**
     * 将 text 文本转换为含有公式的内容, 并且输出到 web view 上.
     *
     * @param text    一条文本
     * @param web_view_node web view text node
     */
    private void flushIntoNode( String text, Element web_view_node )
    {
        Pattern p = Pattern.compile("\\$\\$.*?\\$\\$");
        Matcher m = p.matcher(text);
        int lt = 0, rt = 0;
        Element appendElement;

        // 从前到后寻找 $$ equation $$ 出现的地方
        while( m.find() ){

            rt = m.start();

            // 将正常文本段放入 web view 节点
            appendElement = document.createElement("a");
            appendElement.setTextContent(text.substring(lt, rt));
            web_view_node.appendChild(appendElement);

            lt = m.end();

            // 将新的公式嵌入 web view 节点
            snuggleTranslateInText(text.substring(rt, lt), web_view_node);
        }

        appendElement = document.createElement("a");
        appendElement.setTextContent(text.substring(lt));

        web_view_node.appendChild(appendElement);
    }


    public void showMessage( Message message )
    {
        Platform.runLater(() -> {
            Element div = document.createElement("div");
            int user_id = User.getInstance().getID();

            // 按照消息来源设置左对齐或者右对齐
            div.setAttribute("class", (message.sender.getID() == user_id) ? "rt_div" : "lt_div");
            div.setAttribute("align", (message.sender.getID() == user_id) ? "RIGHT" : "LEFT");

            Element pHead = document.createElement("p");
            pHead.setTextContent(message.getHead());
            div.appendChild(pHead);
            Element pContent;

            switch( message.mess_type_name.replaceAll("/.*", "") ){
                case "text" -> {
                    pContent = document.createElement("p");
                    pContent.setAttribute("class", "content");
                    flushIntoNode(new String(message.getBinary_text()), pContent);
                    NodeList mathNodeList = pContent.getElementsByTagName("math");
                    for( int i = 0; i < mathNodeList.getLength(); i++ ){
                        mathNodeList.item(i).getAttributes().getNamedItem("display").setTextContent("inline");
                    }
                }
                case "image" -> {
                    // 发现渲染, 图片显示宽度为 width
                    int width = 150;
                    int height = 0;
                    File file = new File(message.getUrl().replaceAll("\\.\\.", "out/production/chatroom/client"));
                    try{

                        BufferedImage bufferedImage = ImageIO.read(file);

                        // 等比例放缩图片
                        height = (int) (((double) bufferedImage.getHeight() / bufferedImage.getWidth()) * width);

                    } catch(IOException e) {

                        // 读图片失败的情况
                        // ...

                    }
                    pContent = document.createElement("img");
                    pContent.setAttribute("width", "" + width);
                    pContent.setAttribute("height", "" + height);
                    pContent.setAttribute("class", "content");
                    pContent.setAttribute("src", message.getUrl());
                }
                case "audio" -> {
                    pContent = document.createElement("audio");
                    pContent.setAttribute("class", "content");
                    pContent.setAttribute("controls", "controls");
                    Element source = document.createElement("source");
                    source.setAttribute("src", message.getUrl());
                    source.setAttribute("type", message.mess_type_name);
                    pContent.appendChild(source);
                }
                default -> pContent = null;
            }
            div.appendChild(pContent);
            body.appendChild(div);

            // 立即将聊天记录滑到底端
            webEngine1.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        });
    }
}