package dubna.walt;

//import java.io.*;
//import javax.servlet.*;
//import javax.servlet.http.*;
import dubna.walt.util.ResourceManager;

/**
 *
 * @author serg
 */
public class LoginServlet extends dubna.walt.BasicServlet
{

    /**
     *
     * @return
     * @throws Exception
     */
    public ResourceManager obtainResourceManager() throws Exception
{ return new ResourceManager("login");
}
   
}
