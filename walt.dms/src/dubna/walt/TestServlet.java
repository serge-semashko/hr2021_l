package dubna.walt;

import java.io.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;

/**
 *
 * @author serg
 */
public class TestServlet extends HttpServlet {

    /**
     * This method is called when the servlet's URL is accessed.<P>
     * @param req
     * @param res
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
//		req.setCharacterEncoding("Cp1251");
        req.setCharacterEncoding("UTF-8");

//	res.setCharacterEncoding("cp1251");
//	res.setContentType("text/html;charset=cp1251");
        res.setCharacterEncoding("UTF-8");
        res.setContentType("text/html;charset=utf-8");
        OutputStream outStream = res.getOutputStream();

        String a = req.getParameter("a");
        if(a == null) a = "NOT DEFINED!";
        try {
            //	String b = new String(a.getBytes("UTF-8"),"cp1251");
            String b = new String(a.getBytes("ISO-8859-1"), "UTF-8");
            //	String b = new String(a.getBytes("ISO-8859-1"),"cp1251");
            PrintWriter outWriter = new PrintWriter(new OutputStreamWriter(outStream, "UTF-8"));
//		PrintWriter outWriter = new PrintWriter( new OutputStreamWriter(outStream, "cp1251") );
            outWriter.write("Hello, World! a=" + a + "; b=" + b);
            outWriter.close();
        } catch (Exception e) {
            System.out.println("******* EXCEPTION:" + e.toString());
            e.printStackTrace();
        }
    }

}
