package dubna.walt;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;

// http parameters needed:
// c - relative path to the help .cfg file (starting from
// "CfgRootPath" parameter defined in the resource file
// s - name of the section in the .cfg file to be displayed

/**
 *
 * @author serg
 */

public class TestServlet2 extends HttpServlet
{

    /**
     *
     * @param config
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException
{
  super.init(config);
/*  try
  {
    rm = new ResourceManager("popuphelp");
  }
  catch (Exception e)
  {
    e.printStackTrace(System.out);
//    throw new ServletException("Could not get ResourceManager.");
  }
*/  
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
      out.println("<h3> TestServlet - Here we are!</h3><br>");
      out.println("Проба кириллицы<br>");
      return;
        
  }
  catch(Exception e)
  {
    out.println("<h3> TestServlet Exception: query: '" + req.getQueryString() + "'</h3>");
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
  (new TestServlet2()).go(req,res);
  System.gc();
}

}