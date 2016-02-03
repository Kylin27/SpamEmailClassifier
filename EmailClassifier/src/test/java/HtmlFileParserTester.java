import org.jsoup.Jsoup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by win-7 on 2016/1/31.
 */
public class HtmlFileParserTester {

    public static void main (String [] args ) throws Exception {
        String content ="<HTML><HEAD><TITLE>Umjwqmqsqse Newsletter</TITLE></HEAD>\n" +
                "<BODY>\n" +
                "<STYLE>\n" +
                "border1 {background-color: #666;}\n" +
                "border2 {background-color: #333;}\n" +
                "bg1 {background-color: #EFEFEF;}\n" +
                "bgBlack {background-color: #000; padding-left: 10px; padding-top: 25px; padding-bottom: 25px;}\n" +
                "rule {background-color: #CCC;}\n" +
                "hed {font-family: arial; font-size: 24px; color: #777; font-weight: bold;}\n" +
                "hed1 {font-family: arial; font-size: 11px; color: #414141; font-weight: bold;}\n" +
                "dek {font-family: arial; font-size: 15px; color: #999; font-weight: bold;}\n" +
                "subhed {font-family: arial; font-size: 14px; color: #333; font-weight: bold;}\n" +
                "blurb {font-family: arial; font-size: 11px; color: #333; line-height: 15px;}\n" +
                "a {font-family: arial; font-size: 11px; color: #0066CC;}\n" +
                "adlinks {font-family: arial; font-size: 11px; color: #0066CC; font-weight: bold;}\n" +
                "arrow {font-family: arial; font-size: 11px; color: #0066CC;}\n" +
                "rule2 {background-color: #FFD306;}\n" +
                "ad {font-family: arial; font-weight: bold; font-size: 8px; color: #999;}\n" +
                "linkad {font-family: arial; font-weight: bold; font-size: 10px; color: #333; padding-left: 15px; padding-bottom: 5px;}\n" +
                "navlink:link, .navlink:visited, .navlink:active {font-family: arial; font-weight: bold; font-size: 10px; color: #FFF; text-decoration: none;}\n" +
                "navlink:hover {text-decoration: underline;}\n" +
                "vertbar {font-family: arial; font-weight: bold; font-size: 10px; color: #999;}\n" +
                "footer {font-family: arial; font-weight: bold; font-size: 10px; color: #FFF;}\n" +
                "nav {background-color: #777;}\n" +
                "footerlink:link, .footerlink:visited, .footerlink:active {line-height: 20px;font-family: arial; font-weight: bold; font-size: 10px; color: #FFD306; text-decoration: none;}\n" +
                "footerlink:hover {text-decoration: underline;}\n" +
                "</STYLE>\n" +
                "\n" +
                "<p><b><a href=\"http://www.cokodage.ru/?jwuevqyqbebyn=ceb5d662abcb7\" style=\"color: #999966; font-size: xx-small\">\n" +
                "View this email as web page</a></b></p>\n" +
                "<p><a href=\"http://www.cokodage.ru/?jsoevud=ceb5d662abcb7\" target=\"_blank\">\n" +
                "<img border=\"0\" alt=\"Click here to open image in browser\" height=\"320\" src=\"http://www.cokodage.ru/x.jpg\" width=\"550\"></a>\n" +
                "</p>\n" +
                "<TABLE cellSpacing=0 cellPadding=0 bgColor=#000 border=0 style=\"width: 550px\">\n" +
                "<TBODY>\n" +
                "<TR>\n" +
                "\n" +
                "<TD vAlign=top>\n" +
                "<A style=\"COLOR: rgb(255,211,6); TEXT-DECORATION: none\" href=\"http://www.cokodage.ru/?evypabjuxiujsop=ceb5d662abcb7&mail=hibody@csmining.org\" target=new>Unsubscribe</A>\n" +
                "<SPAN class=vertbar>&nbsp;&nbsp;|&nbsp;&nbsp;</SPAN>\n" +
                "<A style=\"COLOR: rgb(255,211,6); TEXT-DECORATION: none\" href=\"http://www.cokodage.ru/?ozuyujs=ceb5d662abcb7\" target=new>Privacy Policy</A>\n" +
                "<SPAN class=vertbar>&nbsp;&nbsp;|&nbsp;&nbsp;</SPAN><A style=\"COLOR: rgb(255,211,6); TEXT-DECORATION: none\" href=\"http://www.cokodage.ru/?posoribo=ceb5d662abcb7\" target=new>Feedback</A>\n" +
                "<SPAN class=vertbar>&nbsp;&nbsp;|&nbsp;&nbsp;</SPAN><A style=\"COLOR: rgb(255,211,6); TEXT-DECORATION: none\" href=\"http://www.cokodage.ru/?ysygexyfymekj=ceb5d662abcb7\" target=new>Subscribe To More Newsletters</A>\n" +
                "<SPAN class=vertbar>&nbsp;&nbsp;|&nbsp;&nbsp;</SPAN><A style=\"COLOR: rgb(255,211,6); TEXT-DECORATION: none\" href=\"http://www.cokodage.ru/?bivedum=ceb5d662abcb7\" target=new>Sign In To Your Account</A><BR>\n" +
                "<BR clear=all>\n" +
                "<FONT style=\"FONT-SIZE: 11px; COLOR: #999999; FONT-STYLE: normal; FONT-FAMILY: arial\">&copy; 2009 Eheqbusu. All rights reserved. <BR>\n" +
                "\n" +
                "<BR>\n" +
                "The material on this site may not be reproduced, distributed, transmitted, cached, or otherwise used, except with the prior written permission of Jlqmiziwqvux. <BR>\n" +
                "<BR>\n" +
                "For more information, please <A style=\"COLOR: rgb(255,211,6); TEXT-DECORATION: none\" href=\"http://www.cokodage.ru/?ysqolik=ceb5d662abcb7\">contact us</A>. </FONT><BR clear=all></TD>\n" +
                "\n" +
                "</TR>\n" +
                "</TBODY></TABLE></BODY>\n" +
                "\n" +
                "</HTML>" ;

        parseHtml(content);
    }

    private static void parseHtml(String htmlContent ){
      String empty ="" ;
        if ( htmlContent != null){
            Pattern p = Pattern.compile("\\s*|\t|\r|\n") ;
            Matcher m = p.matcher(htmlContent) ;
            htmlContent = m.replaceAll("") ;
        }

        htmlContent = Jsoup.parseBodyFragment(htmlContent).text();
        System.out.println(htmlContent) ;
    }

}
