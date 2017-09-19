package groovy

import groovy.time.TimeCategory
import loader.StreamUtil
import loader.Util
import org.apache.commons.lang.StringUtils

import javax.mail.*
import javax.mail.search.ComparisonTerm
import javax.mail.search.FlagTerm
import javax.mail.search.ReceivedDateTerm

class AttachmentLoader {

    public static final String host = "imap.gmail.com"
    public static final String port = "993"

    Properties props = new Properties()
    String username
    String password
    Session session
    Store store
    FetchProfile fetchProfile
    Flags seen
    FlagTerm unseenFlagTerm
    Date currentDate = new Date()
    Date sinceDate
    List<File> attachments
    Map map = [:]

    AttachmentLoader() {
        props.setProperty("mail.store.protocol", "imaps")
        props.setProperty("mail.imap.host", host)
        props.setProperty("mail.imap.port", port)
        props.setProperty("mail.imap.ssl.enable", "true")

        this.username = Util.getUserNameMail()
        this.password = Util.getPasswordMail()
        this.session = Session.getDefaultInstance(props, null)
        this.store = session.getStore("imaps")
        store.connect(host, username, password)
        this.fetchProfile = new FetchProfile()
        this.seen = new Flags(Flags.Flag.SEEN)
        this.unseenFlagTerm = new FlagTerm(seen, false)
        fetchProfile.add(FetchProfile.Item.ENVELOPE)
        this.sinceDate = use(TimeCategory) {
            sinceDate = currentDate.clone() - 1.month
        }
        sinceDate;
        this.attachments = new ArrayList<File>()
    }

    public Map<String,File> uploadAttachment(String malefolder) {
            Folder folder = store.getFolder(malefolder)
            folder.open(Folder.READ_WRITE)
            Message[] messagesUnsorted = folder.search(unseenFlagTerm)
            Message[] messages = folder.search(new ReceivedDateTerm(ComparisonTerm.GE, sinceDate), messagesUnsorted)
            folder.fetch(messages, fetchProfile)

            Boolean hasAttach = false
            File f
            for (int messageIndex = 0; messageIndex < messages.length - 1; messageIndex++) {
                messages[messageIndex].setFlag(Flags.Flag.SEEN, true)
            }
            int lastMessageIndex = messages.length - 1
            if (lastMessageIndex >= 0) {
                println "***************************************************"
                println "***************************************************"
                String date = messages[lastMessageIndex].sentDate.format("ddMMYYYY")
                println "${messages[lastMessageIndex].receivedDate}"
                println "${messages[lastMessageIndex].sender}"
                println "${messages[lastMessageIndex].from}"
                println "${messages[lastMessageIndex].subject}"
                println "${messages[lastMessageIndex].allRecipients}"
                println "${messages[lastMessageIndex].getHeader("Message-ID")}"

                Multipart multipart = (Multipart) messages[lastMessageIndex].getContent();

                for (int partIndex = 0; partIndex < multipart.getCount(); partIndex++) {
                    def bodyPart = multipart.getBodyPart(partIndex)
                    if (!(Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) || StringUtils.isNotBlank(bodyPart.getFileName()))) {
                        messages[lastMessageIndex].setFlag(Flags.Flag.SEEN, true)
                        continue; // dealing with attachments only
                    }
                    hasAttach = true
                    InputStream is = bodyPart.getInputStream()
                    StreamUtil streamUtil = new StreamUtil()
                    f = streamUtil.stream2file(is,malefolder + "_" + date)
                }
                if (hasAttach) {
                    messages[lastMessageIndex].setFlag(Flags.Flag.SEEN, false)
                    map.put(malefolder, f)
                } else {
                    messages[lastMessageIndex].setFlag(Flags.Flag.SEEN, true)
                }
                println hasAttach
                println "***************************************************"
                println "***************************************************"
            }
        return map
    }
}
