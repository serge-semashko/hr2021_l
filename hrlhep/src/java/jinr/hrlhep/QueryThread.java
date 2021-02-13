package jinr.hrlhep;

import dubna.walt.util.*;

import java.sql.*;
import java.util.Hashtable;
import javax.servlet.http.HttpServletRequest;

public class QueryThread extends dubna.walt.SimpleQueryThread {
// public DBUtil dbUtil = null;

    public boolean logIt = true;
//    private static final String excludeFromLog = ",svs/showInfoTooltip,sys/request_log,sys/viewRequest,sys/setErrFixed,free/checkSession_noDB,";
//    private InputValidator iv = null;
    private int objectTypeId = 0;

    /**
     *
     *
     */
    @Override
    public void start() {
//                super.start();

        try {
            startTm = System.currentTimeMillis();
            rm.setParam("startTm", Long.toString(startTm));
            parseRequest(request);
            getAddresses();
         
            makeTuner();
//            checkSession();
            setContentType();
            if(cfgTuner.enabledExpression("Host=NULL&!ClientIP=159.93.33.24"))
                ((Logger) rm.getObject("logger")).logRequest2DB(rm, "Host=NULL", null);
            dbUtil = makeDBUtil();
            //    rm.setObject("cfgTuner", cfgTuner);
            if (cfgTuner.enabledOption("ResetLog=true")) {
                IOUtil.clearLogFile(rm);
//                rm.removeKey("InputValidator");
//                iv = null;
            }
            getSysConst();
            excludeFromLog = rm.getString("excludeFromLog", false, "");
//            if (cfgTuner.getParameter("c").contains("showLog")) {
//                rm.setParam("log", "false");
//            }
//       writeHttpHeaders();
            logQuery();
            if (validateUser()) {
//                logQuery();
            } else {
                rm.println("! " + rm.getString("queryLabel")
                        + " [" + Fmt.fullDateStr(new java.util.Date()) + "] "
                        + cfgTuner.getParameter("ClientIP")
                        + " NOT LOGGED, "
                );
                cfgTuner.outCustomSection("not identified", outWriter);
            }
            validateParameters();
            startService();
            logQuery(null);
        } catch (Exception e) {
            logException(e);
            logQuery(e);
        } finally {
            if (outWriter != null) {
                outWriter.flush();
                outWriter.close();
                try {
                    outStream.close();
                    //         inpStream.close();
                } catch (Exception e) {
                    /* don't warry! */ }
            }
            finish();
            logRequestFinished();
        }
    }

    
    /**
     *
     * @param request
     * @throws Exception
     */
    @Override
    public void parseRequest(HttpServletRequest request) throws Exception {
//        if(iv == null)
//            iv = (InputValidator) rm.getObject("InputValidator", false);
//        if(iv == null) {
//            iv = new InputValidator(rm.getGlobalRM());
//            rm.getGlobalRM().putObject("InputValidator", iv);
//        }
//        object_type_id
        String[] sa = request.getParameterValues("object_type_id");        
        objectTypeId = 0;
        if(sa != null && sa.length == 1) {
            try {
            objectTypeId = Integer.parseInt(sa[0]);
            } catch (Exception e) {;}
        }
        super.parseRequest(request);
    }

    @Override
    public void registerParameter(String name, String val, Hashtable ht) {
//        String v = iv.getValidatedValue(name, val, objectTypeId);
////        IOUtil.writeLogLn("+++++ param: " + name + "=" + val + "; =>" + v, rm);
//        if(v == null) {
//            String paramErr = iv.getErrMsg(name, val, objectTypeId);
//            IOUtil.writeLogLn("+++++ ERROR: param: " + name + "=" + val + "; =>" + v + "; " + paramErr + "; objectTypeId=" + objectTypeId, rm);            
//            super.registerParameter("INPUT_ERROR", paramErr + " ", ht);
//            super.registerParameter("ERROR_" + name, paramErr, ht);
//            super.registerParameter(name + "_orig", val, ht);
//            System.out.println("ERROR_" + name + "='" + paramErr + "'; " + name + "_orig='" + val +"'");
//        }
//        else if(v.length() > 0)
//            super.registerParameter(name, v, ht);
            super.registerParameter(name, val, ht);
    }

    /**/
    private void getSysConst() throws Exception {
        if (dbUtil == null) {
            makeDBUtil();
        }
        if (dbUtil == null) {
            return;
        }
        if (!rm.getBoolean("const_inited")) {
            String sql = "select alias, value from sys_const";
            IOUtil.writeLogLn("===== get System Const SQL:" + sql, rm);
            ResultSet r = dbUtil.getResults(sql);
            while (r.next()) {
                rm.setParam(r.getString(1), r.getString(2), true);
                System.out.println("- " + r.getString(1) +":" + r.getString(2));
            }
            dbUtil.closeResultSet(r);
            rm.setParam("const_inited", "true", true);
        }
    }

    @Override
    public boolean validateUser() throws Exception {
//        if (!logIt) {
            //return true;
//        }
        return super.validateUser();
    }

    /**
     * Вывод в консоль Томката информации о запросе
     */ 
    @Override
    public void logQuery() {
//        if (logIt) 
        {
            System.out.println(rm.getString("queryLabel")
//                    + " [" + Fmt.fullDateStr(new java.util.Date()) + "] "
//                    + cfgTuner.getParameter("ClientIP") 
                    + ": " + cfgTuner.getParameter("USER_ID")
//                    + ": " + cfgTuner.getParameter("c") 
                    + "; " + cfgTuner.getParameter("queryString")
            );
//            System.out.println(". " + rm.getString("queryLabel") + ": " + cfgTuner.getParameter("queryString"));
        }
    }

    /**
     * Запись в лог в БД основных параметров http-запроса
     *
     * @param e зафиксированная exception при выполнения запроса Если exception
     * не возник, то регистрируется параметр ERROR из Tuner
     */
    @Override
    public void logQuery(Exception e) {
        String c = cfgTuner.getParameter("c");
        System.out.println("+++ logQuery +++ c=" + c + "; logIt=" + logIt + "; e=" + e);
        if (!logIt) {
            return;
        }
        if (excludeFromLog.contains("," + c + ",")) 
            return;
        Logger logger = (Logger) rm.getObject("logger");
        logger.logRequest2DB(rm, cfgTuner.getParameter("ERROR"), e);
        
    }


    /**
     * Коннект к БД в начале выполнения запроса Коннект не делается для
     * cfg-файлов, содержащих "_noDB" в имени файла или в пути к нему
     *
     * @return объект DBUtil, который далее будет использоваться для обращения к
     * БД.
     * @throws Exception
     */
    @Override
    public synchronized DBUtil makeDBUtil() throws Exception {
        if (cfgFileName == null || cfgFileName.contains("_noDB")) {
            logIt = false;
            return null;
        }
        if (cfgFileName.contains("free/")
                || cfgFileName.contains("svs/")) {
            logIt = false;
        }

        if (!cfgTuner.enabledOption("connString")) {
            return null;
        }
        try {
            IOUtil.writeLog(5, "<br><i>connect: " + cfgTuner.getParameter("connString")
                        + cfgTuner.getParameter("database") + cfgTuner.getParameter("connParam") + "</i>...", rm);
            /* Establish connection to the database and make DBUtil */
            Connection conn = DriverManager.getConnection(cfgTuner.getParameter("connString")
                    + cfgTuner.getParameter("database")
                    + cfgTuner.getParameter("connParam"), cfgTuner.getParameter("usr"), cfgTuner.getParameter("pw"));
            conn.setAutoCommit(true);
            dbUtil = new DBUtil(conn, queryLabel);
            dbUtil.db = DBUtil.DB_MySQL;

//=============== SET DB Connection properties =====================
//	   Statement stmt = conn.createStatement();
//	   stmt.executeUpdate("set max_sp_recursion_depth=30");
//	   stmt.executeUpdate("SET NAMES cp1251");
//	   stmt.executeUpdate("set max_allowed_packet=5000000");
//	   conn.commit();
//	   stmt.close();
            rm.setObject("DBUtil", dbUtil, false);
            IOUtil.writeLog(5, " Connect OK! " + Long.toString(System.currentTimeMillis() - startTm) + "ms.", rm);
        } catch (Exception e) {
            IOUtil.writeLogLn(cfgTuner.getParameter("connString") + cfgTuner.getParameter("database") + cfgTuner.getParameter("connParam")
                        + " <b>Connect ERROR: </b>" + e.toString(), rm);
            System.out.println("Connection to " + cfgTuner.getParameter("connString") + " FAILED!..." + e.toString());
//		 e.printStackTrace(System.out);
            cfgTuner.addParameter("NotConnected", "Y");
            if (outWriter != null) {
                cfgTuner.addParameter("ERR_MSG", e.getMessage());
                cfgTuner.addParameter("ERR_MSG_DES",
                        "Could not connect to the Database " + cfgTuner.getParameter("connString"));
            }
            e.printStackTrace(System.out);
            return null;
        }
        if (cfgTuner.enabledOption("debug=on")) {
            System.out.println(" Connect OK!");
        }
        return dbUtil;
    }

    /**
     * Finalizer. Закрытие коннектов к базе, запись в консоль Томката в режиме
     * "debug=on"
     */
    @Override
    protected void finish() {
        closeDBUtils();
        if (cfgTuner != null) {
            try {
                if (cfgTuner.enabledOption("debug=on")) {
                    System.out.println("\n" + "[" + Fmt.lsDateStr(new java.util.Date()) + "] "
                            + rm.getString("queryLabel") + ": --- finish()");
                }
            } catch (Exception e) {;
            }
        }
    }

    /**
     * Закрытие коннектов к базе, удаление DBUtils из ResourceManager rm Если
     * включены транзакции и были ошибки, то откат, иначе - коммит
     */
    protected void closeDBUtils() {
        if (dbUtil != null) {
            try {
                if (cfgTuner.enabledOption("ERROR")) {
                    dbUtil.update("ROLLBACK");
                } else {
                    dbUtil.update("COMMIT");
                }
            } catch (Exception e) {
            }
            dbUtil.close();
        }

        dbUtil = (DBUtil) rm.getObject("DBUtil", false);
        if (dbUtil != null) {
            dbUtil.close();
            rm.setObject("DBUtil", null);
        }
        dbUtil = null;
//  System.out.println("---------------- closeDBUtils() " + queryLabel);
    }

}
