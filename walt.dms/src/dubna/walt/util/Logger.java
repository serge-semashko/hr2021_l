package dubna.walt.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author serg
 */
public class Logger {

    private ResourceManager rm_global = null;
//    private ConnectionPool cp = null;
    public String now = "NOW()";
    private DBUtil dbUtil = null;

    public Logger() {
    }

    public Logger(ResourceManager rm_global) {
        init(rm_global);
    }

    public void init(ResourceManager rm_global) {
        this.rm_global = rm_global;
        dbUtil = makeDBUtil("LOGGER", rm_global);
    }

    public synchronized void logRequest2DB(ResourceManager rm, String msg, Exception ex) {
        if (rm_global.getBoolean("NO_LOG_TO_DB")) {
            return;
        }
        String cfgFile = "?";
        String queryLabel = "?";
        String sql = "";
        Tuner cfgTuner = null;
        long tm = System.currentTimeMillis();

        try {
            queryLabel = rm.getString("queryLabel", false, "?");
//        System.out.println("LOGGER.logRequest2DB() " + queryLabel);

            cfgTuner = (Tuner) rm.getObject("cfgTuner", false, false);

            if (cfgTuner != null) {
                cfgFile = cfgTuner.getParameter("cfgFile");
            }
            int in = cfgFile.indexOf('.');
            if (in > 1) {
                cfgFile = cfgFile.substring(0, in);
            }
        } catch (Exception e) {
            System.out.println("++++++++++++++++++++++++++++++++++++++");
            System.out.println("***** ERROR: Logger.logRequest2DB()");
            //            System.out.println(e.toString());
            e.printStackTrace(System.out);
            System.out.println("++++++++++++++++++++++++++++++++++++++");
        }
        if( ex != null) {
            System.out.println(" Logger: " + queryLabel + ": logRequest2DB(msg=" + msg + ", ex)");
            ex.printStackTrace();
        }
        else {
            System.out.println(" Logger: " + queryLabel + ": logRequest2DB(msg=" + msg + ", null)");            
        }
//            System.out.println("cfgFile=" + cfgFile);
//            System.out.println("EXCLUDE=" + rm.getString("excludeFromLog", false, "-"));
        if (!rm.getString("excludeFromLog", false, "-").contains("," + cfgFile + ",") || msg.length() > 0) {
            //            System.out.println(".......");
            //            System.out.println("  Logger.logRequest2DB(): cfgTuner=" + cfgTuner);

            if (msg.length() > 0) {
                System.out.println(" LOGGER: " + queryLabel + ": logRequest2DB(msg=" + msg + ")");
            }
            if (ex != null) {
                System.out.println(" LOGGER: " + queryLabel + ": logRequest2DB(ex=" + ex.toString() + ")");
            }

            String s = rm.getString("startTm", false, "");
            String timer = "0";
            if (s.length() > 0) {
                timer = Long.toString(System.currentTimeMillis() - Long.parseLong(s));
            }
//                String agent = cfgTuner.getParameter("h_user-agent");

//                if (ex != null) {
//                    StackTraceElement[] st = ex.getStackTrace();
//                }


//            DBUtil dbUtil = null;
            try {
/*
                if(msg.length() > 0) {
                    dbUtil = makeDBUtil(queryLabel, rm);
                }
                else {
                    try{
                        dbUtil = new DBUtil(rm);    
                    } catch(Exception e) {
                        dbUtil = makeDBUtil(queryLabel, rm);                       
                    }
                }
                dbUtil.myName = "LOG_" + dbUtil.myName;
/**/                   
                if(dbUtil == null || !dbUtil.isAlive()) {
                    dbUtil = makeDBUtil("LOGGER", rm);
                }
                if (dbUtil != null && dbUtil.isAlive()) {
                    String e = ((ex == null) ? msg : ex.toString() + " / " + msg);
                    if (e.length() > 2500) {
                        e = e.substring(0, 2000);
                    }
                    e = e.replaceAll("'", "`");

                    sql = getLogSQL(rm, e, timer);
//                System.out.println("***** Logger.logRequest2DB() SQL:" 
//                        + sql
//                );

                    try {
                        dbUtil.update(sql);
                    } catch (Exception ee) {
                        System.out.println("XXXXX LOGGER ECEPTION: " + ee.toString());
                        try {
                            dbUtil.getConnection().close();
                            dbUtil.close();
                        } catch (Exception eee) {;
                        }
                        dbUtil = new DBUtil(rm);
//                        dbUtil = makeDBUtil(queryLabel, null);
                        dbUtil.update(sql);
                    }
                } else {
                    System.out.println("***** ERROR: Logger.logRequest2DB() - NOT CONNECTED! dbUtil=" + dbUtil);
                    if (dbUtil != null) {
                        System.out.println("----- ERROR: dbUtil.isAlive=" + dbUtil.isAlive());
                    }
                }
            } catch (Exception e) {
                System.out.println("++++++++++++++++++++++++++++++++++++++");
                System.out.println("++++++++++++++++++++++++++++++++++++++");
                System.out.println("***** ERROR: Logger.logRequest2DB()");
                System.out.println("***** Logger SQL:" + sql);
                //            System.out.println(e.toString());
                e.printStackTrace(System.out);
                System.out.println("++++++++++++++++++++++++++++++++++++++");
            }
//            finish(dbUtil);
            tm = System.currentTimeMillis() - tm;
            System.out.println("  LOGGER.logRequest2DB(: " + queryLabel + ") DONE, " + tm + "ms.");
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
    public String getLogSQL(ResourceManager rm, String e, String timer) {
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
                    + "', '" + trimString(cfgTuner.getParameter("cfgFile"), 60)
                    + "', '" + cfgTuner.getParameter("request_name")
                    + "', '" + trimString(cfgTuner.getParameter("queryString"), 1000)
                    + "', " + cfgTuner.getIntParameter(null, "doc_id", 0)
                    + ", '" + trimString(cfgTuner.getParameter("h_cookie"), 1000)
                    + "', '" + trimString(e, 2000)
                    //  По умолчанию  now="NOW()" Для ORACLE поставить logger.now="SYSDATE"
                    + "', " + now
                    + ", '" + trimString(cfgTuner.getParameter("ClientIP"), 250)
                    + "', '" + trimString(cfgTuner.getParameter("h_user-agent"), 500)
                    + "', '" + trimString(cfgTuner.getParameter("h_referer"), 250)
                    + "', '" + trimString(cfgTuner.getParameter("SESS_ID"), 250)
                    + "', '" + trimString(cfgTuner.getParameter("q_JSESSIONID"), 32)
                    + "', '" + trimString(cfgTuner.getParameter("q_cwldid"), 250)
                    + "', " + timer
                    + ")";
        } else {
            HttpServletRequest request = (HttpServletRequest) rm.getObject("request");
            String user_id = rm.getString("user_id", false, "0"); // "0";
            String sess_id = "";
            Object o;
            String n = "";
            String v = "";
            String q = "";
            String vu = "null";
            String dev_id = rm.getString("ServerPath", false, "unknown");
            String referer = "*";
            String req = "internal";

            if (request != null) {
                HttpSession session = request.getSession();
                System.out.print("***** ERROR: Logger- NO cfgTuner: session=" + session);
                if (session != null) {
                    o = session.getAttribute("USER_ID");
//                    System.out.print(" USER_ID=" + o);
                    if (o != null) {
                        user_id = o.toString();
                    }
                    o = session.getAttribute("JSESSIONID");
//                    System.out.println(" JSESSIONID=" + o);
                    if (o != null) {
                        sess_id = o.toString();
                    }
                }
                user_id = (user_id.isEmpty()) ? "0" : user_id;
                Cookie[] cookies = request.getCookies();
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
                if (request.getHeader("referer") != null) {
                    referer = StrUtil.replaceInString(
                            StrUtil.replaceInString(
                                    StrUtil.replaceInString(
                                            request.getHeader("referer"), "?", "%3F"), "&", "%26"), "=", "%3D");
                }
                req = request.toString();
            }

            sql = "insert into a_req_log (USER_ID, REAL_USER_ID, REQUEST_TYPE, queryLabel"
                    + ", C, QUERY, COOKIES, ERR"
                    + ", DAT, IP, USER_AGENT, REF, SESS, DID, TIM)"
                    //                            SESS_ID,
                    + " values (" + user_id
                    + ", " + vu
                    + ", '" + rm.getString("requestType", false, "?")
                    + "', '" + rm.getString("queryLabel", false, "?")
                    //                            
                    + "', '" + trimString(rm.getString("cfgFileName", false, "?"), 60)
                    + "', '" + trimString(req, 1000)
                    + "', '" + trimString(q, 1000)
                    + "', '" + trimString(e, 2000)
                    //  По умолчанию  now="NOW()" Для ORACLE поставить logger.now="SYSDATE"
                    + "', " + now
                    + ", '" + trimString(rm.getString("clientIP", false, "?"), 250)
                    + "', '*"
                    + "', '" + trimString(referer, 250)
                    //                            + "', " + cfgTuner.getIntParameter(null, "SESS_ID", 0) 
                    + "', '" + trimString(sess_id, 32)
                    + "', '" + dev_id
                    + "', " + timer
                    + ")";
        }
        return sql;
    }

    /**
     *
     * @param queryLabel
     * @param rm
     * @return @throws Exception
     */
    public DBUtil makeDBUtil(String queryLabel, ResourceManager rm){
        DBUtil dbUtil;

        try {         
                String connectString=  rm.getString("connString", true)
                    + rm.getString("database", false)
                    + rm.getString("connParam", false);

                IOUtil.writeLog(3, "<br><small><i>Logger.makeDBUtil("  + queryLabel 
                        + ") DB: " 
                        + rm.getString("DB", true, "")
                        + " / " + connectString
                        + " / " + rm.getString("usr", true)
                        + "</i></small>...", rm);
               
                System.out.print(" Logger.makeDBUtil(): " + queryLabel + "  connect:" 
                    + ") DB: " 
                    + rm.getString("DB", true, "")
                    + " / " + connectString
                    + " / " + rm.getString("usr", true)
                //	+ rm.getString("pw", true)
                );
                dbUtil = new DBUtil(
                    rm.getString("DB", true, "")
                    , connectString
                    , rm.getString("usr", true)
                    , rm.getString("pw", true)
                    , queryLabel
                    );
                System.out.println(" - OK ");
        } catch (Exception e) {
            System.out.println("[" + Fmt.shortDateStr(new java.util.Date()) + "] Connection to " + rm.getString("connString") + " FAILED!..." + e.toString());
            e.printStackTrace(System.out);
            return null;
        }
        return dbUtil;
    } /**/
    
//    public void finish(DBUtil dbUtil) {
    public void finish() {
        System.out.println("***** Logger.finish(). Close connections...");
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
     * Finalizer.
     */
    @Override
    protected void finalize() {
        System.out.println("  ..... Logger.finalize()");
        finish();
        System.runFinalization();
        System.gc();
        try {
            super.finalize();
        } catch (Throwable tr) {
            tr.printStackTrace(System.out);
        }
    }

    public String trimString(String s, int maxLen) {
        if (s == null) {
            return "";
        }
        return s.substring(0, Math.min(s.length(), maxLen)).replaceAll("'", "`");
    }
}
