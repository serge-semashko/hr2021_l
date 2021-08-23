package dubna.walt.service;

import dubna.walt.util.MD5;

/**
 *
 * @author serg
 */
public class ServiceLogin extends dubna.walt.service.Service
{

public void beforeStart() throws Exception
{
//  System.out.println("+++ beforeStart:");
  if (cfgTuner.enabledOption("r"))
  { String r = cfgTuner.getParameter("r");
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
	  String user_name=cfgTuner.getParameter("uname");
	  String fio=cfgTuner.getParameter("FIO");
	  String ip=cfgTuner.getParameter("ClientIP");   
    long t = System.currentTimeMillis();
    String q = sess_id + ":" + user_id + ":" + user_name + ":" + fio + ":" + ip + ":" + Long.toString(t);
	  cfgTuner.addParameter("q", MD5.getHashString(q + sess_id + ip) + ":" + q );
  }
  else if (cfgTuner.enabledOption("q"))
  { if (cfgTuner.enabledOption("logged=YES"))
      out.print("OK<p>");
    out.print(cfgTuner.getParameter("q"));
//    out.flush();
//    out.close();
    return;
  }

  super.start();
}

}