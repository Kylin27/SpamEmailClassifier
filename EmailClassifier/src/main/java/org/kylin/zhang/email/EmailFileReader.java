package org.kylin.zhang.email;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.kylin.zhang.email.bean.EmailBean;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by win-7 on 2016/2/2.
 */
public class EmailFileReader {
    public static String testFolderName  = "TESTING" ;
    public static String trainFolderName = "TRAINING" ;
    private static Logger logger = Logger.getLogger(EmailFileReader.class) ;

    public static List<EmailBean> getAllBeansFromFiles( String folderName ){
        String path = "/data/" +folderName ;
        String [] emailFileNameArray = null ; // store every email file path name
        File fileDir ;
        URL  url ;
        List<EmailBean> emailBeans = null ;

        url = EmailFileReader.class.getResource(path) ;

            // --- 1. check url and fileDir is illegal

            if ( url == null ){
                logger.error("[error] can not find file " + path ) ;
                return null ;
            }

            fileDir = new File(url.getPath()) ;

            if ( !fileDir.exists()|| !fileDir.isDirectory() ){
                logger.error("[error] illegal path name") ;
                return null ;
            }


            //--- 2. read in all files under the directory

            emailBeans = new ArrayList<EmailBean>() ; // each email-file 's attributes stored in one EmailBean object
            emailFileNameArray = (new File(url.getPath())).list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".eml") ;
                }
            }) ;

            if ( emailFileNameArray == null || emailFileNameArray.length == 0){
                logger.error("[error] no file found under "+path);
                return null ;
            }

            for (String emFileName : emailFileNameArray){
                EmailBean emBean = readEmailBeanFromFile(url.getPath()+System.getProperty("file.separator")+emFileName) ;
                emailBeans.add(emBean) ;
            }

        return emailBeans ;

    }

    private static EmailBean readEmailBeanFromFile(String emailFileName ){
        EmailBean emailBean  = new EmailBean () ;
        InputStream inStream = null ;
        MimeMessage msg = null ;
        Properties props = null ;
        Session session = null ;

        try{
            /* 1. open target file */

            logger.info("[info] read EmailBean object from file " + emailFileName) ;
            inStream = new FileInputStream(emailFileName) ;
            props = new Properties() ;
            props.setProperty("mail.smtp.host", "host") ;
            session = Session.getDefaultInstance(props, null) ;

            msg = new MimeMessage(session, inStream) ;

            /* 2. extract email attributes into EmailBean instance */

            // ---> extract email subject
            if ( msg.getSubject() != null )
            {
                logger.info("[info] extract subject " + msg.getSubject());
                emailBean.setSubject( msg.getSubject());
            }

            // ---> extract email sender
            if ( msg.getFrom() != null  )
            {
                Address[] addrArray = msg.getFrom() ;

                if ( addrArray == null || addrArray.length <= 0 ){
                    logger.info("[info] no sender info get from "+ emailFileName );

                }
                else {
                    for (Address addr : addrArray) {
                        InternetAddress internetAddress = (InternetAddress) addr;
                        emailBean.getSenderList().add(internetAddress.getAddress());
                        logger.info("[info] sender info " + internetAddress.getAddress());
                    }
                }
            }

            // ---> extract Bcc type address ; Bcc -> Blind copy
            if (msg.getRecipients(Message.RecipientType.BCC) !=  null){
                Address addrArray [] = msg.getRecipients(Message.RecipientType.BCC) ;

                if (addrArray == null || addrArray.length <= 0){
                    logger.info("[info] no BCC info extract from " + emailFileName);
                }
                else{
                    for ( Address addr : addrArray){
                    InternetAddress internetAddress = (InternetAddress)addr ;
                    emailBean.getBccTypeList().add(internetAddress.getAddress());
                    logger.info("[info] BCC address info " + internetAddress.getAddress());
                }

                }
            }

            // ---> extract Cc type address : Cc -> copy
            if (msg.getRecipients(Message.RecipientType.CC) !=  null){
                Address addrArray [] = msg.getRecipients(Message.RecipientType.CC) ;

                if (addrArray == null || addrArray.length <= 0){
                    logger.info("[info] no CC info extract from " + emailFileName);
                }
                else{
                    for ( Address addr : addrArray){
                        InternetAddress internetAddress = (InternetAddress)addr ;
                        emailBean.getCcTypeList().add(internetAddress.getAddress());
                        logger.info("[info] CC address info " + internetAddress.getAddress());
                    }
                }
            }

            // ---> extract To type address : To -> send to address list
            if (msg.getRecipients(Message.RecipientType.TO) !=  null){
                Address addrArray [] = msg.getRecipients(Message.RecipientType.TO) ;

                if (addrArray == null || addrArray.length <= 0){
                    logger.info("[info] no BCC info extract from " + emailFileName);
                }
                else {
                    for ( Address addr : addrArray){
                        InternetAddress internetAddress = (InternetAddress)addr ;
                        emailBean.getToTypeList().add(internetAddress.getAddress());
                        logger.info("[info] TO address info " + internetAddress.getAddress());
                    }
                }
            }
            // ---> extract receive date

            if ( msg.getReceivedDate() != null ){
                logger.info("[info] extract receive date "+ msg.getReceivedDate());
                emailBean.setRecvDate(msg.getReceivedDate());
            }

            // ---> extract send date
            if ( msg.getSender() != null ){
                logger.info("[info] extract send date " + msg.getSentDate());
                emailBean.setSentDate(msg.getSentDate()) ;
            }


            // ---> extract content's body
            // type1 : multipart
            if ( msg.getContent() == null ){
                System.out.println("this condition is content is null ") ;
                System.out.println( msg.getContentType()); ;
            }
            if (msg.getContentType().startsWith("multipart")){
                // System.out.println( " multipart type ") ;

                Multipart multipart = (Multipart) msg.getContent() ;

                for ( int x = 0 ; x < multipart.getCount() ; x++ ){
                    BodyPart bodyPart = multipart.getBodyPart(x) ;

                    String bodyPartContent = parseEmailContent( bodyPart.getContent().toString().trim()) ;
                    emailBean.setContent(bodyPartContent);
                    logger.info("[info] content " +x + " " + bodyPartContent) ;
                }
            }

            // type2: text/html
            if (msg.getContentType().startsWith("text/html")){
                // System.out.println("text/html") ;
               emailBean.setContent( parseEmailContent(msg.getContent().toString().trim()));
            }
            // type3: text/plain
            else {
                 // System.out.println("text/plain") ;

                if ( msg.getContent() != null )
                    emailBean.setContent( msg.getContent().toString().trim());
            }

            inStream.close();

        } catch(Exception e ){
            logger.error("[error] something goes wrong when parse email content email file name " + emailFileName);
        }

        return emailBean ;
    }

    private  static String parseEmailContent ( String content ){
       // 现将内容信息打印出来
        Document doc = Jsoup.parse(content) ;

        /*
        System.out.println("++++++++++++++++++++++++ 1 ++++++++++++++++++++++++++++++++++++") ;
        System.out.println(doc.body().text() ) ;
        System.out.println("++++++++++++++++++++++++++ 2 ++++++++++++++++++++++++++++++++++") ;
        System.out.println(doc.text()) ;
        System.out.println("++++++++++++++++++++++++++++++ 3 ++++++++++++++++++++++++++++++") ;
        */

        return doc.body().text() ;
    }

    public static void main (String [] args ) throws Exception {

        List<EmailBean> emailBeansList = null ;

        emailBeansList = EmailFileReader.getAllBeansFromFiles(trainFolderName);

        System.out.println("total " + emailBeansList.size() ) ;

       /* for (EmailBean eBean : emailBeansList){
            System.out.println("======================================================\n") ;
            System.out.println(eBean) ;
            System.out.println("======================================================\n") ;
        }*/

    }
}
