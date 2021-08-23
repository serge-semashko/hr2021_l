package dubna.walt.service;

import dubna.walt.util.StrUtil;

/**
 *
 * @author serg
 */
public class EditDoc extends Service
{

public void beforeStart() throws Exception
{ if (cfgTuner.enabledOption("cop=s"))
  { String s = StrUtil.replaceInString(
      cfgTuner.getParameter("des"),"\r\n\r\n","<p>");
    s =StrUtil.replaceInString(s ,"\r\n","<br>");
    cfgTuner.addParameter("des", s);
  }
  super.beforeStart();
}

public void start() throws Exception
{ if (cfgTuner.enabledOption("cop=e"))
  { String s = StrUtil.replaceInString(
      cfgTuner.getParameter("des"), "<p>", "\r\n\r\n");
    cfgTuner.addParameter("des",
      StrUtil.replaceInString(s, "<br>", "\r\n"));
  }

  cfgTuner.outCustomSection(reportSectionName,out);
}

}