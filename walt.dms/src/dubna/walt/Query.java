package dubna.walt;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import dubna.walt.util.*;

import javax.servlet.http.HttpServlet;

/**
 *
 * @author serg
 */
public class Query {

    private ResourceManager rm = null;
//    private HttpServletRequest req = null;
//    private HttpServletResponse res = null;
    public String queryLabel = null;
    private long timer;
//    private long timer_q;
    private BasicServlet servlet;
    
    /**
     * Constructor.
     *
     * @param req
     * @param res
     * @param rm_Global
     * @param queryLabel
     * @param servlet
     */
    public Query(HttpServletRequest req,
             HttpServletResponse res,
             ResourceManager rm_Global,
             String queryLabel,
             BasicServlet servlet) {
        
        this.rm = rm_Global.cloneRM();
        this.queryLabel = queryLabel;

        rm.setObject("rm_Global", rm_Global);
        rm.setObject("request", req);
        rm.setObject("response", res);
        rm.setObject("queryLabel", queryLabel);
        rm.setObject("Servlet", servlet);
        timer = System.currentTimeMillis();
        this.servlet = servlet;
//        System.out.println("    Q: runQuery " + queryLabel + ";" );
//        System.out.println("    Q: runQuery " + queryLabel + "; numRunning=" + servlet.numRunning);
//        if (servlet.numRunning > 2) {
////            try {
//                int i = (servlet.numRunning > 10)? 4000 : servlet.numRunning * 400;
//                System.out.println("*" + servlet.numRunning + "   : " + queryLabel + " sleep " + i);
//                System.gc();
////                Thread.sleep(i);
////            } catch (InterruptedException ex) {
////            }
//        }

//        timer_q = System.currentTimeMillis();
        try {
//            servlet.numRunning++;
            String className = rm.getString("QueryThreadClass");
            if (className.length() == 0) // set the default QueryThread class
            {
                className = "dubna.walt.SimpleQueryThread";
            }
    // System.out.println("----- makeQueryThread... '" + className + "'");
            Class cl = Class.forName(className);
            QueryThread t = (QueryThread) (cl.newInstance());
            t.init(rm);
            t.start();
        } catch (Exception e) {
            if (!(e instanceof java.net.SocketException)) {
                System.out.println("EXCEPTION: " + e.getMessage());
                e.printStackTrace(System.out);
                res = null;
                PrintWriter outWriter = null;
                try {
                    res = (HttpServletResponse) rm.getObject("", false, false);
                    outWriter = new PrintWriter(res.getOutputStream());
                    outWriter.println("</center><H3> Servlet error - </h3>" + e.getMessage());
                    e.printStackTrace(System.out);
                    outWriter.close();
                } catch (Exception ex) {
                    System.out.println( ex.toString() + " ------- res = " + res + " ; outWriter=" + outWriter);
//                    ex.printStackTrace(System.out);
                }
            }
        } finally {
//            this.rm = null;
//            this.req = null;
//            this.res = null;
//            this.queryLabel = null;
//
//  System.gc();
//            servlet.numRunning--;
            timer = System.currentTimeMillis() - timer;
//            timer_q = System.currentTimeMillis() - timer_q;
            String mem = Query.getMem();
            System.out.println("*************** " + queryLabel + " FINISHED in " + timer + "ms. MEM:" + mem + "\n\r");
            
        }
    }
    
    public static String getMem(){
            Runtime rt = Runtime.getRuntime();
            String mem = "max=" + Long.toString(rt.maxMemory() / (1024 * 1024))
                + "MB total=" + Long.toString(rt.totalMemory() / (1024 * 1024))
                + "MB, free=" + Long.toString(rt.freeMemory() / (1024 * 1024)) + "MB";
            return mem;
        }

}
