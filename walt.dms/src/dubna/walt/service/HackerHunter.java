package dubna.walt.service;

/** Description of class
 *
 * @author kouniaev
 * @version 1.0
 */

public class HackerHunter extends Service
{


public void start() throws Exception
{
   out.println("<p><b>Sorry, you are not authorized for this page.</b>");
   cfgTuner.outCustomSection("not authorized",out);
   out.println("</body></html>");
   out.flush();
   out.close();
   out=null;
}


}