package EmailUtil.bean;

import javax.mail.Address;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Aimer1027 on 2016/1/30.
 * This class wraps attributes in Email
 */
@SuppressWarnings("serial")
public class EmailBean implements Serializable{

    private static final long serialVersionUID = 1L;

    private String subject ;
    private String sender ;
    private Date recvDate ;
    private Date sentDate ;
    private String content = null ;
    private String  ccTypeList  ;
    private String bccTypeList ;
    private String toTypeList ;

    // ---- getters and setters



    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        if ( this.content == null )
            this.content = content;
        else{
            StringBuilder sb = new StringBuilder() ;
            sb.append(this.content) ;
            sb.append(content) ;
            this.content = sb.toString() ;
        }
    }

    public Date getRecvDate() {
        return recvDate;
    }

    public void setRecvDate(Date recvDate) {
        this.recvDate = recvDate;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBccTypeList() {
        return bccTypeList;
    }

    public void setBccTypeList(String bccTypeList) {
        this.bccTypeList = bccTypeList;
    }

    public String getCcTypeList() {
        return ccTypeList;
    }

    public void setCcTypeList(String ccTypeList) {
        this.ccTypeList = ccTypeList;
    }

    public String getToTypeList() {
        return toTypeList;
    }

    public void setToTypeList(String toTypeList) {
        this.toTypeList = toTypeList;
    }

    @Override
    public String toString(){
        return "[Subject] "+this.subject +"\n"
                +"[Sender] " +this.sender +"\n"
                +"[Content] " + this.content ;
    }

    public String getEmailText(){
        return this.subject+","+this.content+","+this.sender ;
    }
}
