package org.seamless.util.mail;

import com.xpeng.airplay.service.NsdConstants;
import java.util.Date;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.eclipse.jetty.http.MimeTypes;
/* loaded from: classes.dex */
public class EmailSender {
    protected final String host;
    protected final String password;
    protected final Properties properties = new Properties();
    protected final String user;

    public EmailSender(String host, String user, String password) {
        if (host == null || host.length() == 0) {
            throw new IllegalArgumentException("Host is required");
        }
        this.host = host;
        this.user = user;
        this.password = password;
        this.properties.put("mail.smtp.port", "25");
        this.properties.put("mail.smtp.socketFactory.fallback", "false");
        this.properties.put("mail.smtp.quitwait", "false");
        this.properties.put("mail.smtp.host", host);
        this.properties.put("mail.smtp.starttls.enable", NsdConstants.AIRPLAY_TXT_VALUE_DA);
        if (user != null && password != null) {
            this.properties.put("mail.smtp.auth", NsdConstants.AIRPLAY_TXT_VALUE_DA);
        }
    }

    public Properties getProperties() {
        return this.properties;
    }

    public String getHost() {
        return this.host;
    }

    public String getUser() {
        return this.user;
    }

    public String getPassword() {
        return this.password;
    }

    public void send(Email email) throws MessagingException {
        Session session = createSession();
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(email.getSender()));
        InternetAddress[] receipients = {new InternetAddress(email.getRecipient())};
        msg.setRecipients(Message.RecipientType.TO, receipients);
        msg.setSubject(email.getSubject());
        msg.setSentDate(new Date());
        msg.setContent(createContent(email));
        Transport transport = createConnectedTransport(session);
        transport.sendMessage(msg, msg.getAllRecipients());
        transport.close();
    }

    protected Multipart createContent(Email email) throws MessagingException {
        MimeBodyPart partOne = new MimeBodyPart();
        partOne.setText(email.getPlaintext());
        MimeMultipart mimeMultipart = new MimeMultipart("alternative");
        mimeMultipart.addBodyPart(partOne);
        if (email.getHtml() != null) {
            MimeBodyPart partTwo = new MimeBodyPart();
            partTwo.setContent(email.getHtml(), MimeTypes.TEXT_HTML);
            mimeMultipart.addBodyPart(partTwo);
        }
        return mimeMultipart;
    }

    protected Session createSession() {
        return Session.getInstance(this.properties, (Authenticator) null);
    }

    protected Transport createConnectedTransport(Session session) throws MessagingException {
        Transport transport = session.getTransport("smtp");
        if (this.user != null && this.password != null) {
            transport.connect(this.user, this.password);
        } else {
            transport.connect();
        }
        return transport;
    }
}
