package dubna.walt.service;

import dubna.walt.util.*;
import java.util.StringTokenizer;

/**
 *
 * @author serg
 */
public class ServiceWLogin extends dubna.walt.service.Service
{

public void beforeStart() throws Exception
{
//  System.out.println("+++ beforeStart:");
  if (cfgTuner.enabledOption("back_url"))
  { String r = cfgTuner.getParameter("back_url");
    if (r.indexOf("c=login") >= 0)
      cfgTuner.addParameter("self","Y");
  }
  super.beforeStart();
}


public void start() throws Exception
{
//  System.out.println("+++ ServiceLogin:");
   if (cfgTuner.enabledOption("NEW_SESS_ID"))
  { String sess_id=cfgTuner.getParameter("NEW_SESS_ID");
	  String user_id=cfgTuner.getParameter("USER_ID");
	  String user_name=cfgTuner.getParameter("loginName");
	  String fio=cfgTuner.getParameter("FIO");
	  String ip=cfgTuner.getParameter("ClientIP"); 
	  String ip_mask=cfgTuner.getParameter("IP_MASK"); 
//    if (ip_mask.length() > 1 && ip_mask.indexOf(ip) < 0)
//  System.out.print("+++ ServiceLogin: " + ip + " mask:" + ip_mask);
    if (!checkIP(ip,ip_mask))
    { cfgTuner.deleteParameter("NEW_SESS_ID");
      cfgTuner.addParameter("ERROR", "WRONG IP");
  System.out.println(" +++++++ ERROR ++++++");
    }
    else
    { long t = System.currentTimeMillis();
//      String q = sess_id + ":" + user_id + ":" + user_name + ":" + fio + ":" + ip + ":" + Long.toString(t); 
      String q = sess_id + ":" + user_id + ":" + user_name + ":" + ip + ":" + Long.toString(t);
      cfgTuner.addParameter("q", MD5.getHashString(q + sess_id + ip) + ":" + q );
  System.out.println(" - OK");
    }
  }
  else if (cfgTuner.enabledOption("q"))
  { if (cfgTuner.enabledOption("logged=YES"))
      out.print("OK<p>");
    out.print(cfgTuner.getParameter("q"));
//    out.flush();
//    out.close();
    return;
  }
//  if (cfgTuner.enabledOption("cop=t"))
//   System.out.println("********** test=" 
//    + dubna.walt.util.UserValidator.checkLogin(cfgTuner.getParameter("q_" + cfgTuner.getParameter("loginCookieName")), rm));
	cfgTuner.outCustomSection(reportSectionName,out);
}

private boolean checkIP(String ip, String ip_mask)
{ if (ip_mask.length() == 0) return true;
  StringTokenizer ipmt = new StringTokenizer(ip_mask, ",");
  String ipm="";
  while (ipmt.hasMoreTokens())
  { ipm = ipmt.nextToken();
    if (StrUtil.match(ipm, ip)) return true;    
  }
  return false;
}

}