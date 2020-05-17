package com.wuxi.tools.mail;


import org.apache.commons.lang3.StringUtils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainUtil {

    private static String mailHost = "smtp.163.com";
    private static String mailFrom = "demo@163.com";
    // 注意163邮箱这里的不是密码，是授权码
    private static String mailPwd = "pwddemo";

    private static ExecutorService executor = Executors.newFixedThreadPool(1);

    /**
     * @param to 收件人邮箱列表
     * @param title 邮件主题
     * @param htmlContent 邮件内容，HTML字符串
     */
    public static void send(List<String> to, String title, String htmlContent){

        Properties prop = new Properties();
        prop.setProperty("mail.smtp.host", mailHost);

        // 注意下面两句非常重要，阿里云、腾讯云、aws等都限制了25端口，所以必须通过465做转发
        prop.setProperty("mail.smtp.port", "465");
        prop.setProperty("mail.smtp.ssl.enable", "true");

        prop.setProperty("mail.transport.protocol", "smtp");
        prop.setProperty("mail.smtp.auth", "true");
        prop.setProperty("mail.smtp.user", mailFrom);
        prop.setProperty("mail.smtp.pass", mailPwd);

        // 使用JavaMail发送邮件的5个步骤
        executor.submit(() -> {
            try {
                // 1、创建session
                Session session = Session.getInstance(prop, new Authenticator(){
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(prop.getProperty("mail.smtp.user"),prop.getProperty("mail.smtp.pass"));
                    }
                });
                // 开启Session的debug模式，这样就可以查看到程序发送Email的运行状态
                session.setDebug(true);
                // 2、通过session得到transport对象
                Transport ts = session.getTransport();
                // 3、使用邮箱的用户名和密码连上邮件服务器，发送邮件时，发件人需要提交邮箱的用户名和密码给smtp服务器，用户名和密码都通过验证之后才能够正常发送邮件给收件人。
                ts.connect(mailHost, mailFrom,  mailPwd);
                // 4、创建邮件
                InternetAddress[] addressList = new InternetAddress[to.size()];
                for (int i = 0; i < to.size(); i++) {
                    String receiver = to.get(i);
                    if(StringUtils.isEmpty(receiver))continue;
                    addressList[i] = new InternetAddress(receiver.trim());
                }
                Message message = createSimpleMail(session, mailFrom, addressList, title, htmlContent);
                // 5、发送邮件
                Multipart mainPart = new MimeMultipart();
                // 创建一个包含HTML内容的MimeBodyPart
                BodyPart html = new MimeBodyPart();
                // 设置HTML内容
                html.setContent(htmlContent, "text/html; charset=utf-8");
                mainPart.addBodyPart(html);
                // 将MiniMultipart对象设置为邮件内容
                message.setContent(mainPart);

                Transport.send(message);

                ts.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    private static MimeMessage createSimpleMail(Session session, String mailfrom, InternetAddress[] mailTo, String mailTittle,
                                               String mailText) throws Exception {
        // 创建邮件对象
        MimeMessage message = new MimeMessage(session);
        // 指明邮件的发件人
        message.setFrom(new InternetAddress(mailfrom));
        // 指明邮件的收件人
        message.addRecipients(Message.RecipientType.TO, mailTo);
        // 邮件的标题
        message.setSubject(mailTittle);
        // 邮件的文本内容
        message.setContent(mailText, "text/html;charset=UTF-8");
        // 返回创建好的邮件对象
        return message;
    }
}
