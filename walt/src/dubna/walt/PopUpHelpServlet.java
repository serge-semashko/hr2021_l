package dubna.walt;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import dubna.walt.util.ResourceManager;
import dubna.walt.util.Tuner;

// http parameters needed:
// c - relative path to the help .cfg file (starting from
// "CfgRootPath" parameter defined in the resource file
// s - name of the section in the .cfg file to be displayed

/**
 *
 * @author serg
 */

public class PopUpHelpServlet extends HttpServlet
{

    /**
     *
     */
    public static ResourceManager rm = null;

    /**
     *
     */
    public static String cfgFileName = "";

    /**
     *
     */
    public static Tuner tuner = null;

    /**
     *
     * @param config
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException
{
  super.init(config);
  try
  {
    rm = new ResourceManager("popuphelp");
  }
  catch (Exception e)
  {
    e.printStackTrace(System.out);
    throw new ServletException("Could not get ResourceManager.");
  }
}

    /**
     *
     * @param fileName
     * @throws Exception
     */
    protected static void makeTuner(String fileName) throws Exception
{
  System.out.println("makeTuner...");
  if (tuner == null || !fileName.equals(cfgFileName))
  {
    System.out.println("Create Tuner with '" + fileName + "'");
    cfgFileName = "";
    tuner = new Tuner(null, fileName + ".cfg", rm.getString("CfgRootPath"), rm);
    tuner.addParameter("imgPath",rm.getString("imgPath"));
    cfgFileName = fileName;
  }
}

    /**
     *
     * @param req
     * @param res
     * @throws IOException
     */
    protected void go(HttpServletRequest req, HttpServletResponse res) throws IOException
{
  res.setContentType("text/html");
  PrintWriter out = new PrintWriter(res.getOutputStream());
  try
  {
    String fileName = req.getParameter("c");
    if (fileName != null)
      makeTuner(fileName);
    else
    {
      out.println("<h3> cfg. file not specified!</h3>");
      return;
    }
        
    String sctName = req.getParameter("s");
    if (sctName != null)
    {
      tuner.addParameter(sctName,"sctName");
      tuner.addParameter("section", sctName);
      tuner.outCustomSection("header", out);
      tuner.outCustomSection(sctName, out);
      tuner.outCustomSection("footer", out);
      tuner.deleteParameter(sctName);
      tuner.deleteParameter("section");
    }
    else
    {
      tuner.outCustomSection("header", out);
      tuner.outCustomSection("coming_msg", out);
      tuner.outCustomSection("footer", out);
    }
  }
  catch(Exception e)
  {
    System.out.println(e.toString());
    out.println("<h3> PopUpHelpServlet Exception: query: '" + req.getQueryString() + "'</h3>");
    e.printStackTrace(out);
  }
  finally
  {
    out.flush();
    out.close();    
  }
}


/**
 * This method is called when the servlet's URL is accessed.<P>
     * @param req
     * @param res
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
 */
public void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException
{
  (new PopUpHelpServlet()).go(req,res);
  System.gc();
}

}