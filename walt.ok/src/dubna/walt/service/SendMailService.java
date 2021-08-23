package dubna.walt.service;

import dubna.walt.util.StrUtil;

import java.sql.*;
import java.util.StringTokenizer;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 *
 * @author serg
 */
public class SendMailService extends dubna.walt.service.Service
{

public void beforeStart() throws Exception
{ 
  super.beforeStart();
  String[] msg = cfgTuner.getCustomSection("mail body");
  String body = "";
  for (int i=0; i<msg.length; i++)
    body = body + msg[i] + "\r";
  String subject = cfgTuner.getParameter("subject");
  String addrs = cfgTuner.getParameter("email_address");
  if (addrs.length() > 0)
    SendMailService.sendMail(cfgTuner.getParameter("mailserver")
      , cfgTuner.getParameter("email_from"), addrs, subject, body);
}

    /**
     *
     * @param mailserver
     * @param from
     * @param to
     * @param sbj
     * @param msg_txt
     * @throws Exception
     */
    public static void sendMail(String mailserver, String from, String to
  , String sbj, String msg_txt) throws Exception
{
   Properties props = new Properties();
//   props.put("mail.smtp.host", "smtp.cern.ch");
   props.put("mail.smtp.host", mailserver);
System.out.println("++++ mailserver: " +  mailserver + "; from:" + from + ";");
System.out.println("++++ to:" + to);
System.out.println("++++ sbj: " +  sbj);
System.out.println("++++ msg_txt: " +  msg_txt);
/**/
   Session session = Session.getDefaultInstance(props, null);
 
//   try
   { MimeMessage msg = new MimeMessage(session);
     msg.setFrom(new InternetAddress(from));
//     msg.setFrom(new InternetAddress("SpotDB <serguei.kouniaev@cern.ch>"));
//     InternetAddress[] address = {new InternetAddress(to)};
//     InternetAddress[] address = {new InternetAddress("serguei.kouniaev@cern.ch"), new InternetAddress("john.ferguson@cern.ch"), new InternetAddress("jurgen.de.jonghe@cern.ch")};
     String[] addrs = StrUtil.splitStr(to,';');
     int numAddrs = 0;
     for (int i = 0; i< addrs.length; i++)
     {  //System.out.println(i + ": '" + addrs[i]+"'");
        try
        {  InternetAddress dummy = new InternetAddress(addrs[i]);
           numAddrs++;
        }
        catch (Exception e)
        { System.out.println("Invalid e-mail address: '" + addrs[i]+"'");
          addrs[i] = "";
        }
     }
     InternetAddress[] address = new InternetAddress[numAddrs];
     int j = 0;
     for (int i = 0; i< addrs.length; i++)
     {  // System.out.println(i + ": '" + addrs[i]+"'");
        if (addrs[i].length() > 0)
          address[j++] = new InternetAddress(addrs[i]);
     }
     msg.setRecipients(Message.RecipientType.TO, address);
//     msg.setContentType("text/html"); 
     msg.setSubject(sbj);
 
     // create and fill the first message part
     MimeBodyPart mbp1 = new MimeBodyPart();
 
     mbp1.setText(msg_txt);
 
     // create the second message part
//     MimeBodyPart mbp2 = new MimeBodyPart();
 
  // attach the file to the message
  //   FileDataSource fds = new FileDataSource(fileName);
  //   mbp2.setDataHandler(new DataHandler(fds));
  //   mbp2.setFileName(fds.getName());
 
     // create the Multipart and its parts to it
     Multipart mp = new MimeMultipart();
     mp.addBodyPart(mbp1);
//     mp.addBodyPart(mbp2);
 
     // add the Multipart to the message
     msg.setContent(mp);
 
     // set the Date: header
     msg.setSentDate(new java.util.Date());
 
     // send the message
     Transport.send(msg);
   }
/*    catch (Exception mex)
    {
     mex.printStackTrace(System.out);
   }
*/   
  }

}