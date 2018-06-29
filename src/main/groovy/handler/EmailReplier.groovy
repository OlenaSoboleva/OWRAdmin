package handler

import com.sun.mail.util.MailSSLSocketFactory
import loader.Util
import org.apache.commons.lang.StringUtils

import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.search.FlagTerm

public class EmailReplier {
    Properties props = new Properties()
    String username
    String password
    String mailErrorAddress
    Session session
    Store store
    FetchProfile fetchProfile
    Flags seen
    FlagTerm unseenFlagTerm
    List<File> attachments

    EmailReplier() {
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        props.put("mail.smtp.host", "smtp.gmail.com")
        props.put("mail.smtp.socketFactory.port", "465")
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
        props.put("mail.smtp.auth", "true")
        props.put("mail.smtp.port", "465")
        props.setProperty("mail.debug", "true");
        props.setProperty("https.protocols", "TLSv1.2")
        this.username = Util.getUserNameMail()
        this.password = Util.getPasswordMail()
        this.mailErrorAddress = Util.getMailErrorAdress()
        this.session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password)
                    }
                });
        this.store = session.getStore("imaps")
        store.connect("imap.gmail.com", username, password)
        this.fetchProfile = new FetchProfile()
        this.seen = new Flags(Flags.Flag.SEEN)
        this.unseenFlagTerm = new FlagTerm(seen, false)
        fetchProfile.add(FetchProfile.Item.ENVELOPE)
        this.attachments = new ArrayList<File>()
    }

    public void emailReply(String replyfolder, Boolean result) {
        Folder folder = store.getFolder(replyfolder)
        folder.open(Folder.READ_WRITE)
        Message[] messages = folder.search(unseenFlagTerm)
        folder.fetch(messages, fetchProfile)
        for (int messageIndex = 0; messageIndex <= messages.length - 1; messageIndex++) {
            int lastMessageIndex = messages.length - 1
            if (lastMessageIndex >= 0) {
                Multipart multipart = (Multipart) messages[lastMessageIndex].getContent()

                for (int partIndex = 0; partIndex < multipart.getCount(); partIndex++) {
                    def bodyPart = multipart.getBodyPart(partIndex)
                    if (!(Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) || StringUtils.isNotBlank(bodyPart.getFileName()))) {
                        continue; // dealing with attachments only
                    }
                   if(result) {
                       //to change to successReply  when correct gmail mailbox will be used
                     successReply(messages, lastMessageIndex)
//                       successReplyToAdmin(messages, lastMessageIndex)
                   }
                    else{
                       errorReply(messages, lastMessageIndex)
                   }
                }
            }
        }
    }

    private errorReply(Message[] messages, int lastMessageIndex) {
        Address[] to = new InternetAddress(mailErrorAddress)
        Message message = new MimeMessage(session)
        message = message.reply(false)
        message.setSubject("RE: ERROR FILE LOADING " + messages[lastMessageIndex].subject)
        message.setFrom(new InternetAddress(username))
        message.addRecipients(Message.RecipientType.TO, to)
        message.setText("ERROR FILE LOADING")
        Transport.send(message)
        println(messages[lastMessageIndex].subject + " received on: " + messages[lastMessageIndex].receivedDate + " message replied successfully with error status")
        messages[lastMessageIndex].setFlag(Flags.Flag.SEEN, true);
    }

    private successReply(Message[] messages, int lastMessageIndex) {
        Address[] all = messages[lastMessageIndex].allRecipients
        Address[] cc = all
                .findAll { !(it == (new InternetAddress(username))) }
        Address[] to = messages[lastMessageIndex].from
        Message message = new MimeMessage(session)
        message = message.reply(false)
        message.setSubject("RE: " + messages[lastMessageIndex].subject)
        message.setFrom(new InternetAddress(username))
        message.setReplyTo(cc)
        message.setReplyTo(to)
        message.addRecipients(Message.RecipientType.TO, to)
        message.addRecipients(Message.RecipientType.CC, cc)
        message.setText("Hi all,\n" +
                "the file has been uploaded.")
        Transport.send(message)
        println(messages[lastMessageIndex].subject + " received on: " + messages[lastMessageIndex].receivedDate + " message replied successfully")
        messages[lastMessageIndex].setFlag(Flags.Flag.SEEN, true)
    }

    private successReplyToAdmin(Message[] messages, int lastMessageIndex) {
        Address[] to = new InternetAddress(mailErrorAddress)
        Message message = new MimeMessage(session)
        message = message.reply(false)
        message.setSubject("RE: " + messages[lastMessageIndex].subject)
        message.setFrom(new InternetAddress(mailErrorAddress))
        message.addRecipients(Message.RecipientType.TO, to)
        message.setText("Hi all,\n" +
                "the file has been uploaded.")
        Transport.send(message)
        println(messages[lastMessageIndex].subject + " received on: " + messages[lastMessageIndex].receivedDate + " message replied successfully")
        messages[lastMessageIndex].setFlag(Flags.Flag.SEEN, true);
    }
}