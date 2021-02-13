package jinr.hrlhep;

import dubna.walt.service.Service;
import dubna.walt.util.IOUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;

public class ServiceMimeData extends Service
{
  @Override
  public void start()
    throws Exception
  {
		String tmpFileName = cfgTuner.getParameter("logPath") + "ADB" + cfgTuner.getParameter("tm")+"_"+(System.nanoTime()/1000);
		System.out.println("++++++++++++++++ ServiceMimeData create file = "+tmpFileName);
		out = new PrintWriter(tmpFileName, "utf-8");
//		out = new PrintWriter(tmpFileName, "Cp1251");
		System.out.println("++++++++++++++++ ServiceMimeData file created ");
                cfgTuner.outCustomSection("report", out);
		System.out.println("++++++++++++++++ ServiceMimeData file writed ");
		out.flush();
		System.out.println("++++++++++++++++ ServiceMimeData file flushed ");
		out.close();
		System.out.println("++++++++++++++++ ServiceMimeData file closed ");
    
    try
    {
//      String tmpFilePath = (String)this.rm.getObject("tmpFilePath");
      File tmpFile = new File(tmpFileName);
      long l = tmpFile.length();
      System.out.println("+++ ServiceMimeData tmpFile:" + tmpFileName + "; LENGTH=" + l);
//			response.setContentType(cfgTuner.getParameter("contentType")); - is in Service.beforeStart()
      response.setHeader("Content-length", Long.toString(tmpFile.length()));
      FileInputStream is = new FileInputStream(tmpFile);
      ServletOutputStream outStream = (ServletOutputStream)this.rm.getObject("outStream");
      IOUtil.copyStream(is, outStream);
      is.close();
      outStream.flush();
      outStream.close();
      tmpFile.delete();
//      System.out.println("+++ DELETE tmpFile " + tmpFileName);
    }
    catch (Exception e)
    {
      e.printStackTrace(System.out);
    }
  }
}
