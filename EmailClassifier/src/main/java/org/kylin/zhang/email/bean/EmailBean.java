package org.kylin.zhang.email.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by win-7 on 2016/2/2.
 */

@SuppressWarnings("serial")
public class EmailBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String subject ;
    private List<String> senderList ;
    private Date recvDate ;
    private Date sentDate ;
    private String content = null ;
    private List<String>  ccTypeList  ;
    private List<String> bccTypeList ;
    private List<String> toTypeList ;


    // ---- getters and setters

    public EmailBean(){
        this.senderList = new ArrayList<String>() ;
        this.ccTypeList = new ArrayList<String>() ;
        this.bccTypeList = new ArrayList<String>() ;
        this.toTypeList = new ArrayList<String>() ;
    }

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


    public List<String> getBccTypeList() {
        return bccTypeList;
    }

    public void setBccTypeList(List<String> bccTypeList) {
        this.bccTypeList = bccTypeList;
    }

    public List<String> getCcTypeList() {
        return ccTypeList;
    }

    public void setCcTypeList(List<String> ccTypeList) {
        this.ccTypeList = ccTypeList;
    }

    public List<String> getSenderList() {
        return senderList;
    }

    public void setSenderList(List<String> senderList) {
        this.senderList = senderList;
    }

    public List<String> getToTypeList() {
        return toTypeList;
    }

    public void setToTypeList(List<String> toTypeList) {
        this.toTypeList = toTypeList;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }



    @Override
    public String toString(){
        return "[Subject] "+this.subject +"\n"
                +"[Content] " + this.content ;
    }

    public String getEmailText(){

        String addressContent ="" ;

        if ( senderList != null && senderList.size() > 0){
            for (String str : senderList){
                addressContent+= str+" " ;
            }
        }

        return this.content +" "+this.subject+" "+addressContent ;
    }
}
