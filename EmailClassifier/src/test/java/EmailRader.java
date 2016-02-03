import org.jsoup.Jsoup;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * Created by win-7 on 2016/1/30.
 */
public class EmailRader {

    public static void readTrainingMail(String fileNameWithPath ){
        InputStream is ;

        try{
            is = new FileInputStream(fileNameWithPath) ;

            Properties props = System.getProperties() ;
            props.put("mail.host","stmp.gmail.com") ;
            props.put("mail.transport.protocol","stmp") ;
            Session mailSession = Session.getDefaultInstance(props, null) ;

            MimeMessage message = new MimeMessage(mailSession, is) ;

            if ( message.getSubject() != null){
                System.out.println("subject ---> " + message.getSubject()) ;
            }

            if ( message.getFrom() != null ){
                System.out.println(message.getFrom()[0].toString()) ;
            }


            /**
             * The Message.RecipientType is divided into
             * TO , the primary recipients --
             * CC ,carbon copy recipients -- 抄送
             * BCC, blind carbon copy recipients-- 发送邮件副本，又不想让原始接收人知道，密抄送
             * */
            if(message.getRecipients(MimeMessage.RecipientType.CC) != null){
                System.out.println("CC : found " + message.getRecipients(Message.RecipientType.CC)) ;
            }

            if ( message.getRecipients(Message.RecipientType.BCC) != null ){
                System.out.println("BCc : Found " + message.getRecipients(Message.RecipientType.BCC)) ;
            }

            if (message.getRecipients(Message.RecipientType.TO) != null ){
                System.out.println("To Found " + message.getRecipients(Message.RecipientType.TO)) ;
                Address[] AddressList = message.getRecipients(Message.RecipientType.TO) ;

                for ( Address internetAddress : AddressList){
                    InternetAddress addr = (InternetAddress)internetAddress ;
                    System.out.println(addr.getAddress()) ;
                }

            }

            if ( message.getSentDate() != null ){
                System.out.println("message Date : " + message.getSentDate())  ;
            }

            System.out.println("here is the email 's content type name") ;
            System.out.println(message.getContentType()) ;
            System.out.println("here is the email content ") ;
            System.out.println(message.getContent()) ;

            if ( message.getContentType().startsWith("multipart")){
                Multipart multiPart = (Multipart)message.getContent() ;

                for ( int x = 0 ; x < multiPart.getCount() ; x++){
                    BodyPart bodyPart = multiPart.getBodyPart(x) ;
                    String disposition = bodyPart.getDisposition() ;

                    if ( disposition != null){
                        String content = html2text((String)bodyPart.getContent()).trim() ;

                        System.out.println( "Content : " + content ) ;
                        System.out.println("disposition " + disposition) ;
                    }

                    System.out.println("is email contains disposition ? ") ;
                    if ( disposition!=null  && disposition.equals(BodyPart.ATTACHMENT))
                    System.out.println( "yes") ;
                    else
                        System.out.println("no") ;
                }
            }

        } catch( Exception e){
            e.printStackTrace();
        }

    }
/**
 * Function: html2text Converts html String to plain texts
 * */
    public static String html2text ( String html ){
        return Jsoup.parse(html).text() ;
    }

    public static void main (String [] args ){
        String emailPathName ="C:\\Users\\win-7\\Downloads\\spam\\CSDMC2010_SPAM\\CSDMC2010_SPAM\\TRAINING\\TRAIN_00000.eml" ;
        String ename2 = "C:\\Users\\win-7\\Downloads\\spam\\2016\\01\\1451627453.13045_857.lorien" ;

        EmailRader.readTrainingMail(ename2);
    }


}
