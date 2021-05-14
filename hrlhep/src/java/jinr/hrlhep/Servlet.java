package jinr.hrlhep;


import dubna.walt.Query;
import dubna.walt.util.ResourceManager;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Servlet extends dubna.walt.BasicServlet { 
     static Monitor monitor = null;

    public ResourceManager obtainResourceManager() throws Exception {
        System.out.println(".\n\r.\n\r.\n\r*** GATEWAY - INIT ...");
//  Load static resources for the app
        ResourceManager rm = new ResourceManager("hrlhep");
        try {
            Class.forName(rm.getString("dbDriver"));        // init the JDBC driver
            System.out.println("*** TEST: dbDriver=" + rm.getString("dbDriver"));
        } catch (Exception ex) {
            Logger.getLogger(Servlet.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace(System.out);
        }
        return rm;
    }

    @Override
    public void customInit() {
        try {
            if (monitor != null) {
                monitor.stop = true;
            }
            monitor = null;
            
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void setResourceManager(ResourceManager rm) {
        this.rm_Global = rm;
        System.out.println("*** GATEWAY: rm_Global=" + rm_Global);
        try {
            Class.forName(rm.getString("dbDriver"));        // init the JDBC driver
            System.out.println("*** GATEWAY: dbDriver=" + rm.getString("dbDriver"));
        } catch (Exception ex) {
            Logger.getLogger(Servlet.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace(System.out);
        }

    }
        protected static void startMonitor() {
        String server = rm_Global.getString("ServerPath");
        System.out.println("*** HRJINR - startMonitor: NO_MONITOR=" + rm_Global.getBoolean("NO_MONITOR"));
        if(!rm_Global.getBoolean("NO_MONITOR")) {
            System.out.println("*** HRJINR - startMonitor: rm_Global=" + rm_Global);
            try {
                if (monitor != null) {
                    monitor.stop = true; //НА ВСЯКИЙ СЛУЧАЙ остановим
                }
                monitor = new Monitor(rm_Global);
                System.out.println("*** HRJINR: Monitor=" + monitor);
                new Thread(monitor).start();
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        } else {
            System.out.println("*** HRJINR - startMonitor: NOT STARTED! server=" + server);
        }
    }

    
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            super.doGet(req, res);
            if (monitor == null || !monitor.isRunning()) {
                System.out.println("*** HRJINR - CALL startMonitor");
                startMonitor();
            } else{
                System.out.println("*** HRJINR - NOT CALL startMonitor");
                
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  
    
    /**
 * This method is called when the servlet's URL is accessed.<P>
     * @param req
     * @param res
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
 */
// http://www.getinfo.ru/article296.html - КОДИРОВКИ в JAVA
    
    
//    @Override
//    public void doGet(HttpServletRequest req, HttpServletResponse res)
//    throws ServletException, IOException
//{ 
////  rm_Global.println("=== Do doGet works...");
////  rm_Global.println("=== req:" + req);
////String s = rm_Global.getString("clientEncoding",false,"Cp1251");
////System.out.println("");
////System.out.println("***** clientEncoding='" + s + "'");
////	 res.setCharacterEncoding(s);
////	 res.setCharacterEncoding("UTF-8");
// req.setCharacterEncoding(rm_Global.getString("clientEncoding",false,"Cp1251")); //ADDED 09.09.2014
// res.setCharacterEncoding(rm_Global.getString("clientEncoding",false,"Cp1251"));
// 
//// req.setCharacterEncoding("Cp1251");
////    System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkkk");
//   if ("XMLHttpRequest".equals(req.getHeader("X-Requested-With")) ){
//        req.setCharacterEncoding("utf8"); //ajax ВСЕГДА пересылается в UTF-8.
//        res.setCharacterEncoding(rm_Global.getString("clientEncoding",false,"Cp1251"));
//  } 
//   
//   //req.setCharacterEncoding("Cp1251");
//  String reset = req.getParameter("reset");
//  if (reset != null && reset.equals("yes"))
//  { destroy();
////    initServlet();
//    init((ServletConfig) rm_Global.getObject("ServletConfig"));
//
//  }
//  new Query(req, res, rm_Global, newQueryLabel(), this);
//}
    
}
