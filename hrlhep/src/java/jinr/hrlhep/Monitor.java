package jinr.hrlhep;

import dubna.walt.service.Service;
import dubna.walt.util.ConnectionPool;
import dubna.walt.util.DBUtil;
import dubna.walt.util.Fmt;
import dubna.walt.util.IOUtil;
import dubna.walt.util.ResourceManager;
import dubna.walt.util.Tuner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Date;

/**
 * 
 * @author serg
 */
public class Monitor implements Runnable {

    private static ResourceManager rm_Global;
    protected boolean stop = false;
    protected boolean running = false;
    protected static int run_id=0;
    
    public Monitor(ResourceManager rm_Global) {
        Monitor.rm_Global = rm_Global;
        stop = false;
        run_id=0;
    }

    @Override
    public void run() {
        stop = false;
        long sleepTime = (long) rm_Global.getInt("MonitorInterval", 60000);
        System.out.println("+++ SED.MONITOR STARTED +++ sleepTime=" + sleepTime);

        while (!stop) {
            running = true;
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
                ex.printStackTrace(System.out);
            }
            if (!stop) {
                try {
                    run_id++;
                    runScheduledTasks();
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }
        }
        running = false;
        System.out.println("+++ SED.MONITOR - STOP +++");
//        if (dbUtil != null && dbUtil.isAlive()) {
//            dbUtil.close();
//            dbUtil = null;
//            System.out.println("+++ MONITOR - dbUtil.close() +++");
//        }
    }
    
    /**
     * 
     */
    private void runScheduledTasks() {
        ResourceManager rms;
        int task_id = -1;
        String time = "-1";
        long tm = System.currentTimeMillis();

        try {
            DBUtil db = makeDBUtil(rm_Global);
            Date dat = new java.util.Date();
            System.out.println("...> SED.Monitor.runScheduledTasks() " + run_id +"  [" + Fmt.shortDateStr(dat) + ":" + dat.getSeconds() + "] ");
            IOUtil.writeLog(1, " ...>SED.Monitor_" + run_id +"  [" + Fmt.shortDateStr(dat) + ":" + dat.getSeconds() + "] ", rm_Global);

            ResultSet rs = db.getResults("select id, module, param from schedule where active=1 and nextCall<now()");
//            System.out.println(" ... SQL " + rs);
            while (rs.next()) {
                try {
//                    System.out.print(" ... TASK " );
                    tm = System.currentTimeMillis();
                    task_id = rs.getInt("id");
                    System.out.println(" id=" + task_id);
                    db.update("update schedule set active=0, lastCall=now(), lastResult='Task is running...', time=-2 where id=" + task_id);

                    time = "-1";
                    rms = rm_Global.cloneRM();
                    rms.setObject("rm_Global", rm_Global);
                    rms.setObject("queryLabel", "MONITOR.StartTask_" + task_id);
//                    rms.setObject("ServerPath", "");
//                    rms.setObject("ServletPath", "");
                    startTask(rs.getString("module"), rs.getString("param"), task_id, rms);
                    Tuner tts = (Tuner) rms.getObject("cfgTuner");
                    time = Long.toString(System.currentTimeMillis() - tm);
                    if (tts.enabledOption("ERROR")) {
                        System.out.println(" *** TASK ERROR: " + tts.getParameter("ERROR"));
                        db.update("update schedule set active=0"
                                + ", param=concat(param, '&err=" + Long.toString(tm) + "')"
                                 + ", lastCall=now(), lastResult='"
                                + tts.getParameter("RESULT") + "; ERROR=" + tts.getParameter("ERROR") + "', time=" + time + " where id=" + task_id);
                    } else {
                        db.update("update schedule set lastCall=now(), lastResult='" + tts.getParameter("RESULT") + "', time=" + time + " where id=" + task_id);
                    }
                } catch (Exception ex) {
                    System.out.println("===> SED.MONITOR EXCEPTION:" + ex.toString());
                    ex.printStackTrace();
                    db.update("update schedule set active=0, param=concat(param, '&err=" + Long.toString(tm) + "'), lastCall=now(), lastResult='" 
                            + ex.toString() + "', time=" + time + " where id=" + task_id);
                }
            }
            db.closeResultSet(rs);
            db.close();
            db = null;
        } catch (Exception e) {
            e.printStackTrace();
            stop=true;
        }
    }

    /**
     *
     * @param cfgFileName
     * @param param
     * @param task_id
     */
    public static void startTask(String cfgFileName, String param, int task_id, ResourceManager rms) {
        System.out.println(".==> SED.MONITOR start task " + task_id + ": " + cfgFileName + ": " + param);

//        System.out.println("+++ MONITOR - DATA PROCESS +++ queueCfg=" + queueCfg);
        Tuner cfgTuner = null;
        DBUtil db = null;

        try {
//            IOUtil.writeLogLn(1, "<b>===> MONITOR.callService( " + cfgFileName + " )</b>", rms);

            /* Подготовка параметров для запуска модуля */
            String s = "c=" + cfgFileName + "&task_id=" + task_id;
            if (param.length() > 0) {
                s += "&" + param;
            }
            String[] queryParam = s.split("&");

            /* LOG */
            Date dat = new java.util.Date();
            long tm = System.currentTimeMillis();
            IOUtil.writeLogLn(0, "<br><span class='req_start pt' onClick='toggleDiv(\"MON_" + run_id + "\");'> MON_" + run_id 
                    + " [" + Fmt.shortDateStr(dat) + ":" + dat.getSeconds() + "] MONITOR.callService( " + cfgFileName 
                    + " ) </span><div id='MON_" + run_id + "' class='req'> queryString:" + s + "; <br>"
                    , rms);

            /* Создание Tuner */
            rms.setParam("requestType", "MONITOR");
            rms.setParam("securityMode", "SIMPLE");
            cfgTuner = new Tuner(queryParam, cfgFileName, rms.getString("CfgRootPath", false), rms);
            rms.setObject("cfgTuner", cfgTuner);
            cfgTuner.addParameter("tm", Long.toString(System.currentTimeMillis()));
            cfgTuner.addParameter("MONITOR_TASK_ID", Integer.toString(task_id));
            if (cfgTuner.enabledOption("LOG=OFF")) {
                rms.setParam("log", "false");
            } else if (cfgTuner.enabledOption("LOG=ON")) {
                rms.setParam("log", "true");
            }

            /* Создание dbUtil  */
            db = makeDBUtil(rms);
            if (db == null) {
                cfgTuner.addParameter("NotConnected", "Y");
            } 
            rms.setObject("DBUtil", db, false);

            /* Определение и создание сервиса  */
            String className = cfgTuner.getParameter("parameters", "service");
            if (className == null || className.length() == 0) {
                className = "dubna.walt.service.Service";
            }
            Class cl = Class.forName(className);
            Service srv = (Service) (cl.newInstance());

            /* Запуск сервиса  */
            srv.doIt(rms);          // START OF THE WALT SERVICE with CFG-file
            tm = System.currentTimeMillis() - tm;
            IOUtil.writeLogLn(0, "</div><span style='border:solid 1px red; font-weight:bold; background-color:#FFFFA0;'>BACK to MONITOR.callService() (" + tm + "ms)</span>"
                    + srv, rms);
        } catch (Exception e) {
            e.printStackTrace();
            cfgTuner.addParameter("ERROR", e.toString());
        } finally {
            if (db != null) {
                try {
                    db.commit();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                db.close();
                db = null;
            }
        }
    }

    public boolean isRunning() {
        return running;
    }


/**
     *
     * @return @throws Exception
     */
    public static
//            synchronized 
        DBUtil makeDBUtil(ResourceManager rm) throws Exception {

            DBUtil dbUtil = null;
            ConnectionPool cp = (ConnectionPool) rm.getObject("ConnectionPool", false);
            long startTm = System.currentTimeMillis();
            String connectString=  rm.getString("connString", true)
                        + rm.getString("database", false)
                        + rm.getString("connParam", false);
                    
        try {
//            System.out.print(" *** DBQueryThread.makeDBUtil() " + queryLabel + ": " + rm.getString("connString") + "; user=" + rm.getString("usr", false));
           
            if(cp == null) {
                System.out.print("monitor:  connect:" 
                    + " DB: " 
                    + rm.getString("DB", true, "")
                    + " / " + connectString
                    + " / " + rm.getString("usr", true)
                //	+ cfgTuner.getParameter("pw")
                );
                dbUtil = new DBUtil(
                    rm.getString("DB", true, "")
                    , connectString
                    , rm.getString("usr", true)
                    , rm.getString("pw", true)
                    , "Monitor"
//                        , 1
                    );
                startTm = System.currentTimeMillis() - startTm;
                System.out.println(" - OK " + startTm + "ms.");
            }
            else {
                dbUtil = new DBUtil(cp, "Monitor");
            }
//            rm.setObject("DBUtil", dbUtil, false);
        } catch (Exception e) {
            System.out.println("[" + Fmt.shortDateStr(new java.util.Date()) + "] Connection to " + rm.getString("connString") + " FAILED!..." + e.toString());
            e.printStackTrace(System.out);
            return null;
        }
        return dbUtil;
    }
    
    
    
    /**
     * Коннект к БД
     *
     * @param cfgTuner
     * @return DBUtil, который будет использоваться для работы с БД.
     * @throws Exception
     * /
    public static 
//            synchronized 
        DBUtil makeDBUtil_(ResourceManager rm) throws Exception {
//        if (dbUtil != null && dbUtil.isAlive()) {
//            return dbUtil;
//        }
//        System.out.println("Monitor connect:" + rm.getString("connString")
//                + rm.getString("database")
//                + rm.getString("connParam") + " / " + rm.getString("usr") + " / *** "); // + rm.getString("pw"));
        DBUtil dbUtil = null;
        Connection conn = DriverManager.getConnection(rm.getString("connString")
                + rm.getString("database")
                + rm.getString("connParam"), rm.getString("usr"), rm.getString("pw"));
        conn.setAutoCommit(false);
        dbUtil = new DBUtil(conn, "MONITOR");
        dbUtil.db = DBUtil.DB_MySQL;
        return dbUtil;
    }
/**/
        
}
