/*
package EmailUtil;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import EmailUtil.bean.EmailBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

*/
/**
 * Created by Aimer1027 on 2016/1/30.
 *//*

@SuppressWarnings("serial")
public class EmailReader  implements  Serializable {
    private static List<EmailBean> emailBeanList = null ;
    private static Logger logger = Logger.getLogger(EmailReader.class) ;

    public  static List<EmailBean> getAllEmailBeanFromFiles(String folderPathName ){
        // in this method we get all the file names under folderPathName path
        String [] emailFileArray = null ;

        */
/* 1. whether 'folderPathName' exists *//*

        File dirPath  = new File (folderPathName) ;
        if (!dirPath.isDirectory() || !dirPath.isAbsolute() || !dirPath.exists()){
            logger.error("[error] illegal file path name");
            return emailBeanList;
        }

        */
/* 2. readin all files under the target folder *//*

        emailBeanList = new ArrayList<EmailBean>() ;
        emailFileArray = (new File(folderPathName)).list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".eml") ;
            }
        }) ;

        */
/* 3. if the file-name array not null , traverse every email file , read the file data in *//*

        if (emailFileArray.length == 0 ) {
            logger.info("[info] no .eml files found under "+folderPathName );
            return null ;
        }
        for (String emFileName : emailFileArray){
           EmailBean emailBean =  readEmailBeanFromFile(folderPathName +System.getProperty("file.separator") + emFileName);

            if ( emailBean != null ){
               emailBeanList.add(emailBean) ;
           }
        }

        return emailBeanList ;
    }
    private static EmailBean readEmailBeanFromFile (String emailFileName){
        EmailBean emailBean = new EmailBean() ;
        InputStream inputStream = null ;
        MimeMessage msg = null ;
        try{
            */
/* 1. open target file *//*

            logger.info("[info] read in file " + emailFileName);
            inputStream = new FileInputStream(emailFileName) ;

            Properties props = new Properties() ;
            props.setProperty("mail.smtp.host", "host") ;

            Session session = Session.getDefaultInstance(props, null ) ;

           msg = new MimeMessage(session,inputStream) ;

            */
/* 2. extract email attributes into EmailBean *//*

            if ( msg.getSubject() != null ){
                emailBean.setSubject(msg.getSubject());

            }

            if (msg.getFrom() != null ){
                Address[] addrArray = msg.getFrom() ;

                InternetAddress internetAddress= (InternetAddress)addrArray[0] ;
                emailBean.setSender(internetAddress.getAddress());
               // System.out.println(internetAddress.getAddress()) ;
            }

            if(msg.getReceivedDate() != null ){
                emailBean.setRecvDate(msg.getReceivedDate());
            }

            if (msg.getSentDate() != null ){
                emailBean.setSentDate( msg.getSentDate());
            }

            if ( msg.getRecipients(Message.RecipientType.BCC) != null ){
                Address addrArray [] =  msg.getRecipients(Message.RecipientType.BCC);

                if ( addrArray.length > 0 ){
                    InternetAddress addr = (InternetAddress)addrArray[0] ;
                    String strAddr = addr.getAddress() ;

                    emailBean.setBccTypeList(strAddr);
                } else{
                    emailBean.setBccTypeList(null);
                }
            }
            if (msg.getRecipients(Message.RecipientType.CC) != null ){
                Address addrArray [] =  msg.getRecipients(Message.RecipientType.CC);

                if ( addrArray.length > 0 ){
                    InternetAddress addr = (InternetAddress)addrArray[0] ;
                    String strAddr = addr.getAddress() ;

                    emailBean.setCcTypeList(strAddr);
                } else{
                    emailBean.setCcTypeList(null);
                }
            }

            if (msg.getRecipients(Message.RecipientType.TO) != null ){
                Address addrArray [] =  msg.getRecipients(Message.RecipientType.TO);

                if ( addrArray.length > 0 ){
                InternetAddress addr = (InternetAddress)addrArray[0] ;
                String strAddr = addr.getAddress() ;

                emailBean.setToTypeList(strAddr);
                } else{
                    emailBean.setToTypeList(null);
                }
            }

            if (msg.getContentType().startsWith("multipart")){
                Multipart multiPart = (Multipart) msg.getContent() ;
             //   System.out.println("multipart ") ;

                for ( int x = 0 ; x < multiPart.getCount() ; x++){
                    BodyPart bodyPart = multiPart.getBodyPart(x) ;


                    String emailContent = parseEmailContent( bodyPart.getContent().toString().trim()) ;
                  //  System.out.println(emailContent ) ;
                    emailBean.setContent(emailContent);
                }
            } else if (msg.getContentType().startsWith("text/html")){
             */
/*   System.out.println("============ new type : text/html======================== \n\n") ;
               System.out.println( parseEmailContent((msg.getContent().toString().trim()))) ;*//*


                emailBean.setContent(parseEmailContent((msg.getContent().toString().trim())));

            } else{

               */
/* System.out.println("============ new type text/plain ======================== \n\n") ;
                System.out.println(msg.getContentType()) ;
                System.out.println(msg.getContent()) ;*//*

                emailBean.setContent(msg.getContent().toString());
            }
            inputStream.close();
            return emailBean ;

        }catch(AddressException e){
            logger.error("[error] something wrong with sender's email address "  );
            emailBean.setSender("address parse error");
        } catch(ParseException e ){
            logger.error("[error] parse error ");
            e.printStackTrace();
        }catch(FileNotFoundException e){
            logger.error("[error] no file " + emailFileName + " found");
            e.printStackTrace();
        } catch(Exception e){
            logger.error("[error] something wrong happen") ;
            e.printStackTrace();
        }

        return null ;
    }

    public static String parseEmailContent ( String content ) {
        return Jsoup.parse(content).text() ;
    }


    public static void main (String [] args ) throws Exception {

        List<EmailBean> emailBeansList = null ;

        String emailPathName ="C:\\Users\\win-7\\Downloads\\spam\\CSDMC2010_SPAM\\CSDMC2010_SPAM\\TRAINING";

        emailBeansList = EmailReader.getAllEmailBeanFromFiles(emailPathName);

        System.out.println("total " + emailBeansList.size() ) ;

        for (EmailBean eBean : emailBeansList){
            System.out.println("======================================================\n") ;
            System.out.println(eBean) ;
            System.out.println("======================================================\n") ;
        }

    }

}
*/
