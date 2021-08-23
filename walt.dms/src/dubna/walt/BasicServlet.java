package dubna.walt;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;
import dubna.walt.util.*;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

/**
 *
 * @author serg
 */
public class BasicServlet extends HttpServlet {

    /**
     *
     */
    protected String appName = "Basic Servlet";

    protected int totalQueryNr = 0;

    protected static ResourceManager rm_Global = null;
    protected static Object lock = new Object();
    
    
    /**
     *  модули, которые не нужно отображать в логе запросов
     */
    
    public static String ignoreModules = 
                ",empty,css/tree,doc/event_cnt,doc/setParam,adm/showLog_noDB"   //ARCH
                + ",doc/getBcInfo"  //ADB
//                + ",free/checkSession_noDB" //SED
                + "," ;

    /**
     *
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        totalQueryNr = 0;
        try {
            rm_Global = obtainResourceManager();
            rm_Global.servlet = this;
   System.out.println("--------------------------- INIT: " + this);
            rm_Global.setObject("ServletConfig", config);
            ServletContext co = config.getServletContext();
            rm_Global.setObject("ServletContext", co);
            rm_Global.setObject("ServletContextName", co.getServletContextName());
            setPaths();
            
            System.out.println("....." + appName + " Init JDBC driver: " + rm_Global.getString("dbDriver"));
            Class.forName(rm_Global.getString("dbDriver"));        // init the JDBC driver
            
            int cpSize = rm_Global.getInt("ConnectionPoolMaxSize");
            if( cpSize > 0) {
                ConnectionPool cp = new ConnectionPool(rm_Global, cpSize / 5, cpSize);
                rm_Global.setObject("ConnectionPool", cp, false);
            }
            
//		rm_Global.println("--------------------------- INIT - OK.");
        } catch (Exception e) {
            log("!!!!!" + appName + " Init - could not get ResourceManager!", e);
            e.printStackTrace();
            throw new ServletException("Could not get ResourceManager.");
        }

        try {
//            Logger logger = new Logger(rm_Global);
            String loggerClassName = rm_Global.getString("LoggerClassName", false, "dubna.walt.util.Logger");
            System.out.println("....." + appName + " Init - create Logger " + loggerClassName);
            Class cl = Class.forName(loggerClassName);
            Logger logger = (Logger) (cl.newInstance());       
            logger.init(rm_Global);
            rm_Global.setObject("logger", logger);
        } catch (Exception e) {
            log("!!!!!" + appName + " Init - could not get Logger!", e);
            e.printStackTrace();
            throw new ServletException("Could not get Logger.");
        }
            
        
        appName = rm_Global.getString("ApplicationName");
        if (appName.length() == 0) {
            appName = rm_Global.rFile.toUpperCase();
        }

        /* --- make ParamValidator object (if specified) --- */
        String className = rm_Global.getString("ParamValidatorClassName", false);
        if (className.length() > 0) {
            try {
                Class cl = Class.forName(className);
                ParamValidator pv = (ParamValidator) (cl.newInstance());
                pv.init(rm_Global);
                rm_Global.setObject("ParamValidator", pv);
            } catch (Exception e) {
                rm_Global.logException("!!!!!" + appName + " Init - could not create ParamValidator " + className + "! NULL will be passed.", e);
            }
        }

        /* --- make UserValidator object (if specified) --- */
        className = rm_Global.getString("UserValidatorClassName", false);
        if (className.length() > 0) {
            try {
                Class cl = Class.forName(className);
                rm_Global.setObject("UserValidator", (UserValidator) (cl.newInstance()));
            } catch (Exception e) {
                rm_Global.logException("!!!!!" + appName + " Init - could not create UserValidator " + className + "! NULL will be passed.", e);
            }
        }

        customInit();
        rm_Global.println("--------------------------- INIT " + appName + " - OK! ");
    }

    /**
     * Установка в rm_Global глобальных параметров: TomcatRoot, AppRoot,
     * CfgRootPath, logPath,
     */
    public void setPaths() {
        String myPath = getServletConfig().getServletContext().getRealPath("/");
        myPath = StrUtil.replaceInString(myPath, "\\", "/");
        //  rm_Global.println("... " +  myPath );

// Path to the application root in the server's file system
        rm_Global.setParam("AppRoot", myPath, true);
        if (rm_Global.getString("logPath", false, "").length() < 4) {
            rm_Global.setParam("logPath", myPath + "/WEB-INF/", true);
        }

// Path to the Tomcat root in the server's file system
        String t = myPath.substring(0, myPath.length() - 2);
        int i = t.lastIndexOf("/");
        if (i > 0) {
            t = t.substring(0, i + 1);
        }
        rm_Global.setParam("TomcatRoot", t, true);

// Path to the configs root folder
        if (rm_Global.getString("CfgRootPath", false, "").length() == 0) {
            rm_Global.setParam("CfgRootPath", myPath + "WEB-INF/classes/configs/", true);
        }
        rm_Global.println("CfgRootPath:" + rm_Global.getString("CfgRootPath"));

    }

    /**
     *
     */
    public void customInit() {
    }

    /**
     *
     */
    @Override
    public void destroy() {
        System.gc();
        System.out.println("--------------------------- DESTROY: " + this);
        try {
            String[] keys = rm_Global.getKeys("A");
            for (String key : keys) {
                if (key.contains("dbUtil") || key.contains("DBUtil")) {
                    try {
                        ((DBUtil) rm_Global.getObject(key)).close();
                    } catch (Exception ex) {;
                        /* We don't care */
                    }
                }
            }
        } catch (Exception e) {
            /* We don't care */
        }
        
        ConnectionPool cp = (ConnectionPool) rm_Global.getObject("ConnectionPool", false, false);
        if(cp != null)
            cp.closeConnections();
        Logger logger = (Logger) rm_Global.getObject("logger");
        System.out.println("--------------------------- DESTROY: logger=" + logger);
        if(logger != null)
            logger.finish();
        
        System.gc();
        System.out.println("--------------------------- DESTROY: drivers... ");
        
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                System.out.println("deregistering jdbc driver: " + driver);
            } catch (Exception e) {
                System.out.println("Error deregistering driver " + driver + " : " + e.toString());
            }

        }
        
        super.destroy();
    }
/**/
    
    /**
     *
     * @return @throws Exception
     */
    public ResourceManager obtainResourceManager() throws Exception {
        return new ResourceManager("def");
    }

    /**
     *
     * @param req
     * @param res
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
//  rm_Global.println("=== Do Post works... - call DoGet");
//  rm_Global.println("=== req:" + req);
        doGet(req, res);
    }

    /**
     * This method is called when the servlet's URL is accessed.<P>
     * @param req
     * @param res
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
// http://www.getinfo.ru/article296.html - КОДИРОВКИ в JAVA
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        if ("XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) {
            req.setCharacterEncoding("utf8"); //ajax ВСЕГДА пересылается в UTF-8.
            res.setCharacterEncoding(rm_Global.getString("clientEncoding", false, "Cp1251"));
        } else {
            req.setCharacterEncoding(rm_Global.getString("clientEncoding", false, "Cp1251")); //ADDED 09.09.2014
            res.setCharacterEncoding(rm_Global.getString("clientEncoding", false, "Cp1251"));
        }
        
        String queryLabel = newQueryLabel(); 
        String reset = req.getParameter("reset");
        if (rm_Global.getString("AppRoot").isEmpty() 
         || reset != null && reset.equals("yes")) {
            destroy();
            init((ServletConfig) rm_Global.getObject("ServletConfig"));
        }
        if (rm_Global.getString("logPath", false, "").length() < 4) {
            setPaths();
        }
        if (rm_Global.getString("lf_name", false, "").isEmpty()) {
            String lf_name = IOUtil.getLogFileName(rm_Global); 
            if(lf_name != null)
                rm_Global.setParam("lf_name", lf_name, false);
        }

        logRequest(req, queryLabel);
      
        new Query(req, res, rm_Global, queryLabel, this);
    }
    
              
    public void logRequest(HttpServletRequest req, String queryLabel) {
        //System.out.println(""); getRemoteHost getRemoteAddr
        String c = req.getParameter("c");
        if(!LogIt(c)) {
            return;
        }

        String ip = req.getRemoteHost();
        if(ip.indexOf("159.93.") == 0)
            ip=ip.replace("159.93.", "~");
        else
            ip="EXT " + ip;
        String s = "\ndoGet: " + queryLabel + " [" + Fmt.fullDateStr(new java.util.Date()) + "] " + ip + "; ";
        Runtime rt = Runtime.getRuntime();
        String mem = "; max=" + Long.toString(rt.maxMemory() / (1024 * 1024))
                + "MB total=" + Long.toString(rt.totalMemory() / (1024 * 1024))
                + "MB, free=" + Long.toString(rt.freeMemory() / (1024 * 1024)) + "MB";
        if (req.getMethod().equals("GET")) {
            System.out.println(s + "GET: c=" + c + "; query=" + req.getQueryString() + mem);
        } else {
            System.out.println(s + req.getMethod() + " length=" + req.getContentLength() + " c=" + c + mem);
        }

//HttpSession sess = req.getSession(false);
//if(sess != null) System.out.println( " sess:" + sess.getId()  + "; ");
    }

    public static boolean LogIt(String c){
        return !ignoreModules.contains("," + c + ",");
    }

    /**
     *
     * @return
     */
    public String newQueryLabel() {
        return appName + "_" + (++totalQueryNr);
    }

}
