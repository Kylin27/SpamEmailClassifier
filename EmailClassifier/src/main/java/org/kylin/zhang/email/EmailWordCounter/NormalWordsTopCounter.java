package org.kylin.zhang.email.EmailWordCounter;

import org.kylin.zhang.email.EmailFileReader;
import org.kylin.zhang.email.bean.EmailBean;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by win-7 on 2016/2/2.
 */
public class NormalWordsTopCounter extends  WordsCounter {

    private static boolean isSpamEmail = false ;
    public NormalWordsTopCounter(){
        super();
    }
    public NormalWordsTopCounter( int topN ){
        this() ;
        this.topN = topN ;
    }

     public void updateStopWordFile( List<EmailBean> emailBeanList){
         URL url ;
         File f ;
         BufferedWriter bufferedWriter ;
         List<Map.Entry<String,Integer>> sortedMapList = null ;

         sortedMapList = super.run(emailBeanList, isSpamEmail) ;

         if (sortedMapList == null || sortedMapList.size() <= 0){
             logger.error("[error] failed to get sorted map list from parent");
             return ;
         }

         try {
              url = NormalWordsTopCounter.class.getResource("/stop_word_set.txt");
             f = new File(url.getPath()) ;
             bufferedWriter = new BufferedWriter( new FileWriter(f,true)) ;

             if ( sortedMapList == null || sortedMapList.size() <= 0){
                 logger.error("[error] top words pair is empty ") ;
                 return ;
             }

             bufferedWriter.newLine();

            for(Map.Entry<String,Integer> mapping: sortedMapList){
                if ( mapping.getKey() == null ) continue ;
                bufferedWriter.write(mapping.getKey());
              //  System.out.println( mapping.getKey()) ;
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }

            bufferedWriter.close();

         } catch(Exception e){
             logger.error("[error] something wrong happen when wring data to stop words file") ;
             e.printStackTrace();
         }
     }
    public static void main (String [] args ) throws Exception {
        List<EmailBean> emailBeanList = EmailFileReader.getAllBeansFromFiles(EmailFileReader.trainFolderName) ;
        NormalWordsTopCounter normalWordsTopCounter = new NormalWordsTopCounter(500) ;

        normalWordsTopCounter.updateStopWordFile(emailBeanList);

    }
}
