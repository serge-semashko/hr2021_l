/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dubna.walt.util;

//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author serg
 */
public class Logger {

    private ResourceManager rm_global;
    private ConnectionPool cp = null;
    private DBUtil dbUtil = null;
    private String now="NOW()";

    public Logger(ResourceManager rm_global) {
        this.rm_global = rm_global;
        if(!rm_global.getBoolean("NO_LOG_TO_DB")) {
            try{
                cp = (ConnectionPool) rm_global.getObject("ConnectionPool", false);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void logRequest2DB(ResourceManager rm, String msg, Exception ex) {
            System.out.print("Enter LOGGER.logRequest2DB() dbUtil=" + dbUtil+" msg="+msg+"\n");
            System.out.print("rm_global.getBoolean(\"NO_LOG_TO_DB\")=" + rm_global.getBoolean("NO_LOG_TO_DB")+"\n");
        
        if(!rm_global.getBoolean("NO_LOG_TO_DB")) {
            long tm = System.currentTimeMillis();
            System.out.print("LOGGER.logRequest2DB() dbUtil=" + dbUtil+"\n");
            String sql="";
            String queryLabel=rm.getString("queryLabel", false, "?");
            System.out.print("  log: " + queryLabel + " "+"\n");

            Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
            String cfgFile ="?";
            if(cfgTuner != null)
                cfgFile = cfgTuner.getParameter("cfgFile");
            int in = cfgFile.indexOf('.');
            if(in > 1)
                cfgFile = cfgFile.substring(0,in);
            System.out.println("cfgFile=" + cfgFile+"\n");
            System.out.println("EXCLUDE=" + rm.getString("excludeFromLog", false, "-")+"\n");
        if (!rm.getString("excludeFromLog", false, "-").contains("," + cfgFile + ",") )
            try {
                makeDBUtil(queryLabel);
                System.out.println(".......make dbutil "+dbUtil+"\n");
                System.out.println("  Logger.logRequest2DB(): cfgTuner=" + cfgTuner+"\n");
    
                if(msg.length() > 0)
                    System.out.println("Logger: " + queryLabel + ": logRequest2DB(msg=" + msg + ")"+"\n");
                if(ex != null)
                    System.out.println("Logger: " + queryLabel + ": logRequest2DB(ex=" + ex.toString()+"\n"); 
        
                String s = rm.getString("startTm");
                String timer = "0";
                if (s.length() > 0) {
                    timer = Long.toString(System.currentTimeMillis() - Long.parseLong(s));
                }
//                String agent = cfgTuner.getParameter("h_user-agent");

                if(ex != null) {
                    StackTraceElement[] st = ex.getStackTrace();
                }

                if (dbUtil != null && dbUtil.isAlive()) {
                    String e =  ((ex == null) ? msg : ex.toString() + " / " + msg);
                    if(e.length()> 2500)
                        e = e.substring(0, 2000);
                    e = e.replaceAll("'", "`");

                    sql = getLogSQL(rm, e, timer);
                    System.out.println("***** Logger.logRequest2DB() SQL:");

                    try {
                        dbUtil.update(sql);
                    }
                    catch(Exception ee){
                        System.out.println("XXXXX LOGGER ECEPTION: " + ee.toString());
                        if(dbUtil != null) {
                            dbUtil.getConnection().close();
                            dbUtil.close();
                            dbUtil = null;
                        }
                        makeDBUtil(queryLabel);
                        dbUtil.update(sql);
                    }
                } else {
                    System.out.println("***** ERROR: Logger.logRequest2DB() - NOT CONNECTED! ");
                }
            } catch (Exception e) {
                System.out.println("++++++++++++++++++++++++++++++++++++++");
                System.out.println("++++++++++++++++++++++++++++++++++++++");
                System.out.println("***** ERROR: Logger.logRequest2DB()");
                System.out.println("***** Logger SQL:" + sql);
    //            System.out.println(e.toString());
                e.printStackTrace();                
                System.out.println("++++++++++++++++++++++++++++++++++++++");                
            }
            finally {
                finish(queryLabel);
            }
            tm = System.currentTimeMillis() - tm;
            System.out.println("  log: " + queryLabel + " DONE, " + tm + "ms.");
        }
    }

    /*
                    if(msg.contains("Выберите, кому нужно отправить документ")
                        || msg.contains("Введите результат")
                        || msg.contains("Неверный пароль")
    //                    || msg.contains("Неверный пароль")                 
                  ) {
                    agent = "ERROR: " + msg + "<br>" + agent;
                    msg="";
                }

    */
    
    public String getLogSQL(ResourceManager rm, String e, String timer){
        String sql = "";
        Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");

        if (cfgTuner != null) {
//                    System.out.println("\n\r msg=" + e);
            sql = "insert into a_req_log (USER_ID, REAL_USER_ID, REQUEST_TYPE, queryLabel"
                    + ", C, REQUEST_NAME, QUERY, DOC_ID, COOKIES, ERR"
                    + ", DAT, IP, USER_AGENT, REF, SESS_ID, SESS, DID, TIM)"
                    + " values (" + cfgTuner.getIntParameter(null, "USER_ID", 0)
                    + ", " + cfgTuner.getIntParameter(null, "VU", 0)
                    + ", '" + rm.getString("requestType", false, "?")
                    + "', '" + rm.getString("queryLabel", false, "?")
                    + "', '" + cfgTuner.getParameter("cfgFile")
                    + "', '" + cfgTuner.getParameter("request_name")
                    + "', '" + trimString(cfgTuner.getParameter("queryString"), 2047)
                    + "', " + cfgTuner.getIntParameter(null, "doc_id", 0)
                    + ", '" + trimString(cfgTuner.getParameter("h_cookie"), 2047)
                    + "', '" + e
                    + "', " + now
//                            + "', SYSDATE"
//                            + "', NOW()"
                    + ", '" + cfgTuner.getParameter("ClientIP")
                    + "', '" + cfgTuner.getParameter("h_user-agent")
                    + "', '" + cfgTuner.getParameter("h_referer")
                    + "', " + cfgTuner.getIntParameter(null, "SESS_ID", 0)
                    + ", '" + cfgTuner.getParameter("q_JSESSIONID")
                    + "', '" + cfgTuner.getParameter("q_cwldid")
                    + "', " + timer
                    + ")";
        } else {           
            HttpServletRequest request = (HttpServletRequest) rm.getObject("request");
            String user_id = "0";
            String sess_id = "";
            Object o;
            HttpSession session = request.getSession();
            System.out.print("***** ERROR: Logger- NO cfgTuner: session=" + session);
            if (session != null) {
                o = session.getAttribute("USER_ID");
                System.out.print(" USER_ID=" + o);
                if (o != null) {
                    user_id = o.toString();
                }
                o = session.getAttribute("JSESSIONID");
                System.out.println(" JSESSIONID=" + o);
                if (o != null) {
                    sess_id = o.toString();
                }
            }
            user_id = (user_id.isEmpty()) ? "0" : user_id;
            Cookie[] cookies = request.getCookies();
            String n = "";
            String v = "";
            String q = "";
            String vu = "null";
            String dev_id = "";
            if (cookies != null) {
                for (int i = 0; i < cookies.length; i++) {
                    n = cookies[i].getName().trim();
                    v = StrUtil.unescape(cookies[i].getValue());
                    if (n.equals("VU")) {
                        vu = v;
                    } else if (n.equals("cwldid")) {
                        dev_id = v;
                    }
                    q += n + "=" + v + "; ";
                }
            }
            String referer = "*";
            if (request.getHeader("referer") != null) {
                referer = StrUtil.replaceInString(
                        StrUtil.replaceInString(
                                StrUtil.replaceInString(
                                        request.getHeader("referer"), "?", "%3F"), "&", "%26"), "=", "%3D");
            }

            sql = "insert into a_req_log (USER_ID, REAL_USER_ID, REQUEST_TYPE"
                    + ", C, QUERY, COOKIES, ERR"
                    + ", DAT, IP, USER_AGENT, REF, SESS, DID, TIM)"
                    //                            SESS_ID,
                    + " values (" + user_id
                    + ", " + vu
                    + ", '" + rm.getString("requestType")
                    //                            
                    + "', '" + rm.getString("cfgFileName")
                    + "', '" + trimString(request.toString(), 2047)
                    + "', '" + trimString(q, 2047)
                    + "', '" + e
                    + "', " + now
//                            + "', SYSDATE"
//                            + "', NOW()"
                    + ", '" + rm.getString("clientIP")
                    + "', '*"
                    + "', '" + referer
                    //                            + "', " + cfgTuner.getIntParameter(null, "SESS_ID", 0) 
                    + "', '" + sess_id
                    + "', '" + dev_id
                    + "', " + timer
                    + ")";
        }            
        return sql;
    }
    
    /**
     * Коннект к БД
     *
     * @return объект DBUtil, который далее будет использоваться для обращения к БД.
     * @throws Exception
     * 
     */
    private void makeDBUtil(String queryLabel) throws Exception {
        
        if(dbUtil != null && dbUtil.isAlive())
            return;
        
        long startTm = System.currentTimeMillis();
        try {
            String connString = rm_global.getString("connString", true)
                    + rm_global.getString("database", false)
                    + rm_global.getString("connParam", false);
            if(cp == null) {
                IOUtil.writeLog(3, "<br><small><i>Logger.makeDBUtil() "
                    + rm_global.getString("DB", true, "")
                    + " / " + connString
                    + " / " + rm_global.getString("usr", true)
                    + "</i></small>...", rm_global);

                System.out.print("Logger: connect: " + rm_global.getString("DB", true, "")
                    + " / " + connString
                    + " / " + rm_global.getString("usr", true) + "/*** "
                //	+ cfgTuner.getParameter("pw")
                );
                dbUtil = new DBUtil(
                    rm_global.getString("DB", true, "")
                    , connString
                    , rm_global.getString("usr", true)
                    , rm_global.getString("pw", true)
                    , queryLabel + "_logger" 
                );
                String connectTime=Long.toString(System.currentTimeMillis() - startTm);
                System.out.println(" - OK " + connectTime + "ms.");
                IOUtil.writeLog(3, " - OK! " + connectTime + "ms. ", rm_global);
            }
            else {
//                System.out.print("Logger: connect: cp=" + cp);
                dbUtil = new DBUtil(cp, queryLabel + "_logger");
            }

//    rm_global.setObject("DBUtil", dbUtil, true);
        } catch (Exception e) {
            System.out.println("[" + Fmt.shortDateStr(new java.util.Date()) + "] Connection to " + rm_global.getString("connString") + " FAILED!..." + e.toString());
            e.printStackTrace(System.out);
            dbUtil=null;
        }
    }
    
    public void finish(String queryLabel){
//        System.out.println("Logger: " + queryLabel + " finish()");
        try {
            if (dbUtil != null) {
                dbUtil.close();
                dbUtil = null;
            }
        } catch (Throwable tr) {
            tr.printStackTrace(System.out);
        }
        
    }
    
    /**
     * Finalizer. Закрытие коннекта к базе
     */
    @Override
    protected void finalize() {
        System.out.println("  ..... Logger.finalize()");
        finish(" FINALIZE ");
        System.runFinalization();
        System.gc();
        try {
            super.finalize();
        } catch (Throwable tr) {
            tr.printStackTrace(System.out);
        }
    }

    private String trimString(String s, int maxLen) {
        return s.substring(0, Math.min(s.length(), maxLen));
    }
}
