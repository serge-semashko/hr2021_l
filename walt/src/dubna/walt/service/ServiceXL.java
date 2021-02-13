package dubna.walt.service;

import java.io.OutputStream;
import dubna.walt.util.*;

/**
 * The root "Service" class.<p>
 *
 * @author Serguei Kouniaev
 */

public class ServiceXL extends Service
{


/**
 * The main query processing method Usually used for overwriting.
 * By default outputs the section: "reportSectionName".
 * 
 * @exception java.lang.Exception
 */
public void start() throws Exception
{
//  outParameters();
  String xlTemplateFileName = cfgTuner.getParameter("XLTemplateFileName");
  System.out.println(xlTemplateFileName);
//  XLUtil.fillXLTemplate(xlTemplateFileName, outFileName, cfgTuner);
  XLUtil.fillXLTemplate(xlTemplateFileName
      , (OutputStream) rm.getObject("outStream"), cfgTuner);
//  cfgTuner.outCustomSection("XLReport",out);
}


}