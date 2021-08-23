package dubna.walt.service;

import dubna.walt.*;
import dubna.walt.util.BasicTuner;
import dubna.walt.util.DBUtil;
import dubna.walt.util.FileContent;
import dubna.walt.util.IOUtil;
import dubna.walt.util.MD5;
import dubna.walt.util.ParamValidator;
import dubna.walt.util.ResourceManager;
import dubna.walt.util.StrUtil;
import dubna.walt.util.Terminator;
import dubna.walt.util.Tuner;
import dubna.walt.util.TunerSQL;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The root "Service" class.<p>
 * It makes a lot of very usefull work: parameter validation, processing of
 * "preSQLs" section, output of "report" section. Very often it's enougth to
 * create a report and/or to perform some action on the server side.<p>
 *
 * The entry point for this class is method <b>doIt(ResourceManager rm)</b>.<br>
 * The method, which usually should be overwritten is <b>start()</b>.<br>
 * Another important method is <b>beforeStart()</b>
 * <p>
 *
 * Service can call a subservice using another configuration file. To do it use
 * directive: "<b>$CALL_SERVICE</b>", described in <b>Tuner</b> class.<br>
 * Also Service contains some useful methods for logging.
 *
 * @see #doIt
 * @see #start
 * @see #beforeStart
 *
 * @author Serguei Kouniaev
 */
public class Service {

    /**
     * The name of the section to be output as the report; Default: "report"
     */
    public String reportSectionName = "report";

    /**
     * The name of the section to be output as the form; Default: "form"
     */
    public String formSectionName = "form";

    /**
     * The name of the section to be output as the report header; Default:
     * "report header" (not used in the "Service" class.
     */
    public String headerSectionName = "report header";

    /**
     * The name of the section to be output as the report footer; Default:
     * "report footer" (not used in the "Service" class.
     */
    public String footerSectionName = "report footer";

    /**
     * The name of the section to be used in method "beforeStart"; Default:
     * "preSQLs"
     *
     * @see #beforeStart
     */
    public String preSQLs = "preSQLs";

    /**
     * contains name of the particular Service class
     */
    public String thisClassName;

    /**
     */
    public HttpServletRequest request;

    /**
     *
     */
    public HttpServletResponse response;

    /**
     * PrintWriter to the client
     */
    public PrintWriter out;

    /**
     *
     */
    protected Cookie cookies[] = null;

    /**
     * The current cfgTuner
     */
    public Tuner cfgTuner;
    /**
     * The current module
     */
    protected String module;

    /**
     * The current ResourceManager
     */
    public ResourceManager rm;
    /**
     * Static instance of the current ResourceManager. Used for "logSQL()" in
     * "getSqlNr()"
     */
    protected static ResourceManager rms;
    /**
     * Global ResourceManager instance
     */
    protected static ResourceManager rmg;

    /**
     * The currend DBUtil
     */
    public DBUtil dbUtil = null;
    /**
     */
    public boolean terminated = false;
    /**
     *
     */
    protected Terminator terminator = null;

    /**
     *
     */
    protected String currDB = "";

    protected long tm;
    protected String excludeFromLog;
//    protected String c;
    
    /**
     * The main method (entry point) for this class. Processing of the query has
     * following steps:<ol>
     * <li>Parameters validation (if ParamValidatorClassName specified in the
     * resource file);
     * <li>No parameter errors steps:<ul>
     * <li>method "beforeStart()" - processing of the "preSQLs" section.
     * <li>method "start()" - outputs the "[reportSectionName]" section.
     * <li>method "afterStart()" - by default does nothing.</ul>
     * <li>In case of input errors or paramete "f=y" specified:<ul>
     * <li>outputs the "[formSectionName]" section.
     * <li>calls method "afterStart()" - by default does nothing.</ul>
     * </ol><p>
     *
     * If section "[reportSectionName]" (or "[formSectionName]") contains
     * "$CALL_SERVICE" directive - a corresponding sub-service will be called
     * during output of the section.<p>
     *
     * @param rm - The ResourceManager, containing all the report-specific
     * information: cfgTuner, request, response, outWriter
     * @throws java.lang.Exception
     *
     */
    public void doIt(ResourceManager rm) throws Exception {
        this.rm = rm;
//        rms = rm;
        dbUtil = (DBUtil) rm.getObject("DBUtil", false);
//        debugPrintln(".");
        System.out.println(" ====> START SERVICE " + this.getClass()  + "; dbUtil=" + dbUtil );
//        if(dbUtil != null)
//            debugPrint("; dbUtil.isAlive()=" + dbUtil.isAlive() );
        if (terminator == null && !rm.getBoolean("noterminator")) {
            terminator = new Terminator(rm, dbUtil, this);
        }

        try {
            this.cfgTuner = (Tuner) rm.getObject("cfgTuner");
            this.module = cfgTuner.getParameter("c");
//            System.out.println(" === module=" + module);
            this.request = (HttpServletRequest) rm.getObject("request", false);
            this.response = (HttpServletResponse) rm.getObject("response", false);
            if (out == null) {
                this.out = (PrintWriter) rm.getObject("outWriter", false);
            }
            rm.setObject("service", this);
            rms = rm;
            rmg = rm.getGlobalRM();

            thisClassName = this.getClass().getName();
            
            if(BasicServlet.LogIt(cfgTuner.getParameter("c"))) {
//                System.out.println("  srv: " + cfgTuner.getParameter("queryLabel") + ": "
//                    + cfgTuner.getParameter("user") + cfgTuner.getParameter("userName") + "; "
//                    + cfgTuner.getParameter("cfgFile") + "; " + this.getClass().getName());
            }
            
            validateParameters();

            if (!cfgTuner.enabledOption("INPUT_ERRORS")) {
                beforeStart();
            }

            fixTextFields();
            if (cfgTuner.enabledOption("f=y") || cfgTuner.enabledOption("INPUT_ERRORS")) {
                cfgTuner.outCustomSection(formSectionName, out);
            } else {
                start();
            }

            afterStart();
        } catch (Exception e) {
            if (!terminated) {
                throw e;
            }
        } finally {
            if (terminator != null) {
                terminator.finished = true;
                terminator.interrupt();
                terminator = null;
            }
//            finalize();
        }
    }
/*
    @Override
    public void finalize() {
        try {
            if (dbUtil != null) {
//		debugPrintln("++++ CLOSE dbUtil_" + currDB + " ++++");
                dbUtil.close();
                dbUtil = null;
            } 
        } catch (Exception e) {
            System.out.println("ERROR: Service.finalize: " + e.toString());
        } finally {
            try {
                super.finalize();
            } catch (Throwable ex) {
                {
                    System.out.println("ERROR: Service.super.finalize: " + ex.toString());
                }
            }
        }
    }
*/
    /**
     * Switch to an alternative DB connection
     *
     * @param db
     */
    public void useDb(String db) {
        String db_ = db.replace("default", "");
        if (db_.equals(currDB)) {
            if( dbUtil!=null){  // check current dbUtil
                if(dbUtil.isAlive()) {
                    return;  // do nothing if it's correct and alive
                }
                else {  // otherwise drop it
                    dbUtil.close();
                    dbUtil = null;
                    rm.removeKey("DBUtil" + db_);
                }
            }
        }
        makeDBUtil(db_);
        if (dbUtil == null) {
            cfgTuner.addParameter("NotConnected", "Y");
            cfgTuner.addParameter("ERROR", "Connection to " + db + " failed!");
            System.out.println("+++++  connection to " + db + ": failed ");
            if(!db_.isEmpty())
                useDb("");
        } else {
            IOUtil.writeLogLn(3,"+++ USING DB " + db + "; keepAlive=" + rm.getBoolean("keepAlive") + "; isAlive=" + dbUtil.isAlive(), rm);
            currDB = db_;
        }
    }
 
    /**
     * Switch to an alternative DB connection
     *
     * @param db
     */
    public void closeDb(String db) throws Exception {
        String db_ = db.replace("default", "");
        DBUtil dbu = null;
        if (db_.equals(currDB) || (db_.isEmpty() & currDB.isEmpty())) {
            dbu = dbUtil;
            dbUtil = null;
        }
        else
            dbu = (DBUtil) rm.getObject("DBUtil" + db_,false);
        if(dbu != null) {
            System.out.println(" srv " + dbu.myName + " closeDb(" + db + "): " );
//            if((dbu.curr_stmt != null) & (!dbu.curr_stmt.isClosed())) {
//                System.out.println("********* ERROR: closeDb(" + db + "): STATEMENT NOT CLOSED!");
//                return;
//            }
            dbu.close();
            rm.removeKey("DBUtil" + db_);
            IOUtil.writeLogLn(3,"+++ CLOSE DB " + db + " - OK", rm);  
        }
    }

    private 
//            synchronized 
        void makeDBUtil(String db) {
        dbUtil = null;
        if (!cfgTuner.enabledOption("connString" + db)) {
            System.out.println("+++++---  connect to '" + db + "' FAILED - connection description not found.");
            return;
        }
        System.out.print("makeDBUtil(" + db + "): ");

        try {
            dbUtil = (DBUtil) rm.getObject("DBUtil" + db, false);
            if (dbUtil != null) {
                if (dbUtil.isAlive()) {
                        System.out.println("Connection IS ALIVE " + dbUtil.myName);
                    return;
                } else {
                    dbUtil.close();
                    rm.removeKey("DBUtil" + db);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        try {
            Class.forName(cfgTuner.getParameter("dbDriver" + db));        // init the JDBC driver
            /* Establish connection to the database and make DBUtil */
            System.out.print("  srv: " + rm.getString("queryLabel") + "  connect to " + db + ": "
            	 + cfgTuner.getParameter("connString" + db)
            				 + cfgTuner.getParameter("database" + db) + cfgTuner.getParameter("connParam" + db) 
            				 + " / " + cfgTuner.getParameter("usr" + db)
//                        +  "/" + cfgTuner.getParameter("pw" + db)
            );
            Long ttt = System.currentTimeMillis();
            IOUtil.writeLog(3, "<br><i>connect to " + db + ": " + cfgTuner.getParameter("connString" + db)
                        + cfgTuner.getParameter("database" + db) + cfgTuner.getParameter("connParam" + db) + " / " + cfgTuner.getParameter("usr" + db) + "</i>...", rm);
//            if (cfgTuner.enabledOption("debug=on")) {
//                debugPrintln(cfgTuner.getParameter("connString" + db)
//                        + cfgTuner.getParameter("database" + db) + cfgTuner.getParameter("connParam" + db)
//                        + " / " + cfgTuner.getParameter("usr" + db) + "/" + cfgTuner.getParameter("pw" + db)
//                );
//            }
            dbUtil = new DBUtil( rm.getString("DB" + db, false, "")
                    , cfgTuner.getParameter("connString" + db)
                    + cfgTuner.getParameter("database" + db)
                    + cfgTuner.getParameter("connParam" + db),
                    cfgTuner.getParameter("usr" + db),
                    cfgTuner.getParameter("pw" + db),
                    "DB:" + db);

            if (cfgTuner.getParameter("DB_TYPE_" + db).equals("MySQL")) {
                dbUtil.db = DBUtil.DB_MySQL;
                Connection conn = dbUtil.getConnection();
                conn.setAutoCommit(false);
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("set max_sp_recursion_depth=30");
                stmt.close();
            }
            
            if (cfgTuner.getParameter("autocommit" + db).equals("false")) 
                dbUtil.getConnection().setAutoCommit(false);
            else
                dbUtil.getConnection().setAutoCommit(true);
            
//            rm.setObject("DBUtil" + db, dbUtil, rm.getBoolean("keepAlive" + db));  //09.11.2018 - never keep globally
// непонятно, почему не хранить. Вероятно, не закрывалось.
            rm.setObject("DBUtil" + db, dbUtil, false);
            IOUtil.writeLogLn(3, " Connect OK! " + Long.toString(System.currentTimeMillis() - ttt) + "ms. " + dbUtil.myName, rm);
            System.out.println(" OK! " + Long.toString(System.currentTimeMillis() - ttt) + "ms. " + dbUtil.myName);
        } catch (Exception e) {
            System.out.println(" " + e.toString());
            e.printStackTrace();
            dbUtil = null;
            rm.logException("Connection to " + cfgTuner.getParameter("connString" + db) + " FAILED!...", e);
            IOUtil.writeLogLn(cfgTuner.getParameter("connString" + db) + cfgTuner.getParameter("database" + db) 
                    + cfgTuner.getParameter("connParam" + db) + " / " + cfgTuner.getParameter("usr" + db)
                        + " <b>Connect ERROR: </b>" + e.toString(), rm);
            cfgTuner.addParameter("NotConnected", "Y");
            if (out != null //     && !rm.getBoolean("NeedToSetup") 
                    ) {
                String msg = e.getMessage().replaceAll("'", "`");
                cfgTuner.addParameter("ERROR", msg);
                cfgTuner.addParameter("ERROR_DES",
                        "Could not connect to the Database " + cfgTuner.getParameter("connString" + db));
                out.println("<small>" + msg + "</small>");
                out.println("<table border=1 bgcolor=#FFEEBB cellpadding=8><tr><th>"
                        + "Could not connect to the Database '" + "!</th></tr></table></center><p>");
//			   + cfgTuner.getParameter("connString" + db)
                out.flush();
            }
            return;
        }
    }

    /**
     * The main query processing method. Usually overwrited. By default outputs
     * the section: "reportSectionName".
     *
     * @exception java.lang.Exception
     */
    public void start() throws Exception {
//  outParameters();
        String initCapParameters = "," + cfgTuner.getParameter("initCapParameters") + ",";
        if (initCapParameters.length() > 0) {
            StringTokenizer st = new StringTokenizer(initCapParameters, ",");
            String p;
            String v;
            while (st.hasMoreTokens()) {
                p = st.nextToken();
                v = cfgTuner.getParameter(p);
                if (v.length() > 2) {
                    cfgTuner.addParameter(p, StrUtil.initCap(v));
                }
            }
        }
        cfgTuner.outCustomSection(reportSectionName, out);
    }

    /**
     * Called at the beginning of the query processing.
     * <p>
     * getData with the default section name, defined in preSQLs
     *
     * @exception java.lang.Exception
     * @see #getData
     */
    public void beforeStart() throws Exception {
        getData(preSQLs);
        if (cfgTuner.enabledExpression("of=bin&contentType")) {
            String contentType = cfgTuner.getParameter("contentType");
            response.setContentType(contentType);
            if (cfgTuner.enabledOption("saveAsFile")) {
                response.setHeader("Content-Disposition", "attachment; filename=" + cfgTuner.getParameter("saveAsFile"));
            }
        }
        String src = cfgTuner.getParameter("MD5_SRC");
        if (src.length() > 1) {
            cfgTuner.addParameter("MD5_KEY", MD5.getHashString(src));
        }
    }

    /**
     * Called at the beginning of the query processing.
     * <p>
     * Executes the SQL's or reads / writes the data files according to the
     * [preSQLs] section of the config.file. Obtains customized [preSQLs]
     * section, splits it into different statements (statement separator is ";")
     * and executes consequently the statements.<p>
     *
     * After execution of a statement it customizes the [preSQLs] section again,
     * so, parameters, obtained during the statement execution can be used
     * within the following statements.<p>
     *
     * The statements in the [preSQLs] section can be:<ul>
     * <li>SQL statements (SELECT, INSERT or any others);
     * <li>"read file: fileName" directive;
     * <li>"write file: fileName" directive;
     * <li>"delete file: fileName" directive;
     * </ul>
     *
     * @param sectionName
     * @exception java.lang.Exception
     * @see #getPreData
     * @see #readDataFile(String fileName)
     * @see #writeDataFile(String fileName)
     * @see #deleteDataFile(String fileName)
     */
    public void getData(String sectionName) throws Exception {
        try{
        String s; //, cmd="";
        IOUtil.writeLogLn(9, "<br><b>$GET_DATA</b> " + sectionName, rm);
        if (sectionName.indexOf("SQL:") == 0) {
            s = sectionName.substring(4).trim();
            IOUtil.writeLogLn(2, "------ <b>exec SQL: </b>'" + s + "'", rm);
            getPreData(s);
            return;
        }

        boolean fatal;
        int cmdNr = 1;

        while ((s = getSqlNr(cfgTuner, sectionName, cmdNr)) != null) {
            if (terminated) {
                return;
            }
            if (s.equalsIgnoreCase("$BREAK")) {
                return;
            }
            int i = s.indexOf("try:");
            if (i == 0) {
                s = s.substring(4).trim();
                fatal = false;
            } else {
                fatal = true;
            }

            try {
                execGetDataCmd(s);
            } catch (Exception e) {
                IOUtil.writeLogLn("XXXXXXXX Exception: " + e.toString(), rm);
                if (fatal) {
                    IOUtil.writeLogLn("XXXXXXXX FATAL Exception - QUIT ", rm);
                    throw e;
                } else {
                    IOUtil.writeLogLn(1, "--------- NOT FATAL Exception - Continue...", rm);
                    String msg = e.toString();
                    int ie = 0;
                    while ((ie = msg.indexOf("Exception: ")) > 0) {
                        msg = msg.substring(ie + ("Exception: ").length());
                    }
                    ie = msg.indexOf("(.DBUtil@");
                    if (ie > 0) {
                        msg = msg.substring(0, ie);
                    }
                    msg = msg.replaceAll("'", "`");
                    cfgTuner.addParameter("NON_FATAL_ERROR",  // добавлено для удобства использования
                            cfgTuner.getParameter("NON_FATAL_ERROR") + "\n\r"
                            + msg);
                    cfgTuner.addParameter("getPreDataError",  // сохранено для совместимости
                            cfgTuner.getParameter("getPreDataError") + "\n\r"
                            + msg);
                    cfgTuner.addParameter("ERROR", msg);
                }
            }
            cmdNr++;
        }
        }
        catch(Exception e){
            e.printStackTrace();
            throw(e);
        }
//  IOUtil.writeLogLn("========== Quit beforeStart: cmdNr=" + cmdNr, rm);
    }

    private void execGetDataCmd(String s) throws Exception {
        IOUtil.writeLogLn(4, "------ <b>execGetDataCmd: </b>'" + s + "'", rm);
        if (s.length() > 5) {
            long timer = System.currentTimeMillis();
            String cmd = s.substring(s.indexOf(":") + 1).trim();
            if (s.indexOf("read file:") == 0) {
                int i = cmd.indexOf("maxLength=");
                if (i > 0) {
                    String ml = cmd.substring(i + 10);
                    cmd = cmd.substring(0, i).trim();
                    IOUtil.writeLogLn(4, "*** file: '" + cmd + "' max.length=" + ml + "<br>", rm);
                    readDataFile(cmd, Integer.parseInt(ml));
                } else {
                    readDataFile(cmd, 0);
                }
            } else if (s.indexOf("write file:") == 0) {
                writeDataFile(cmd);
            } else if (s.indexOf("delete file:") == 0) {
                deleteDataFile(cmd);
            } else if (s.indexOf("execute:") == 0) {
                String paramName = "command"; // + Integer.toString(cmdNr);
                int i = cmd.indexOf(":=");
                if (i > 0) {
                    paramName = cmd.substring(0, i).trim();
                    cmd = cmd.substring(i + 2).trim();
                }
                System.out.println("executeCommand: '" + paramName + "':='" + cmd + "'");
                cfgTuner.addParameter(paramName, executeCommand(cmd, cfgTuner));
            } else if (s != null) {
//            } else {
                getPreData(s);
            }
            timer = System.currentTimeMillis() - timer;
            IOUtil.writeLogLn(1, "------ SQL done, " + timer + "ms.<br>", rm);
            cfgTuner.addParameter("CMD_TIME", Long.toString(timer));
        }
    }

    /**
     *
     * @param cmd
     * @throws Exception
     */
    public void executeLoop(String cmd) throws Exception {
        StringTokenizer st = new StringTokenizer(cmd, ";");
        executeLoop(st.nextToken().trim(), st.nextToken().trim(), st.nextToken().trim());
    }

    /**
     *
     * @param paramName
     * @param paramList
     * @param section
     * @throws Exception
     */
    public void executeLoop(String paramName, String paramList, String section) throws Exception {
//   System.out.println("+++++++:" + paramName+"-s:"+ paramList);

        /**
         * ********** MOVE THIS TO BasicTuner.getCustomSection(...)
         * *******************
         */
        int i = section.indexOf("[");
        String sectionName = section;
        String fileName = null;
        if (i > 0) {
            fileName = section.substring(0, i);
            sectionName = section.substring(i);
        }
        /**
         * *****************************
         */
//   System.out.println("+++++++ executeLoop: fileName='" + fileName +"', sectionName='"+ sectionName + "'");
        if (paramList.length() < 1) {
            return;
        }
        StringTokenizer st = new StringTokenizer(paramList, ",");
        int loopIteration = 0;
        while (st.hasMoreTokens()) {
            loopIteration++;
            String v = st.nextToken();
            if (v.length() > 0) {
                cfgTuner.addParameter(paramName, v);
                cfgTuner.addParameter("___LOOP_ITERATION", loopIteration + "");
                String pv = cfgTuner.getParameter(v);
//        System.out.println("+++++++:" + paramName+":"+ v + " /" + pv);
                cfgTuner.addParameter("LOOP_PARAMETER_VALUE", pv);
//       getData(sqlSection); 
                cfgTuner.getCustomSection(fileName, sectionName, out);
            }
        }
    }

    /**
     * Called at the end of the query processing.
     * <p>
     * By default does nothing. It can be usefull if you need to do something
     * finally.
     * @throws java.lang.Exception
     */
    public void afterStart() throws Exception {
        try {
//  if (cfgTuner.enabledOption("debug=on"))
//    outParameters();
            IOUtil.writeLogLn(3, dbUtil.timeSQL, rm);
//            dbUtil.release();
        } catch (Exception e) {
        }
    }

    /**
     * Reads the file and stores it's contents as parameter "contents1" in the
     * cfgTuner.<p>
     *
     * The path to the file can be specified in the "DataStartPath" parameter.
     * If this parameter not specified - "CfgRootPath" will be used.<p>
     *
     * If there is section "allowedReadFiles" in the config. file - checks, if
     * the fileName is listed there.
     *
     * @param fileName the name of the file to read
     * @param maxLength
     * @see #writeDataFile
     * @see #deleteDataFile
     */
    public void readDataFile(String fileName, int maxLength) // throws Exception
    {
        String allowedFiles = " " + StrUtil.strFromArray(cfgTuner.getCustomSection("allowedReadFiles"));
        if (allowedFiles.length() < 2
                || allowedFiles.contains(" " + fileName + " ")) {
            String startPath = cfgTuner.getParameter("DataStartPath");
            if (startPath.length() < 1) {
                startPath = rm.getString("CfgRootPath");
            }
            String fileContentParam = cfgTuner.getParameter("FileContentParam");
            if(fileContentParam.isEmpty())
                fileContentParam = "contents1";
            System.out.println("==> Reading file: " + startPath + fileName + " ==> " + fileContentParam);
            try {
                String[] sa = BasicTuner.readFileFromDisk(startPath + fileName, rm.getString("serverEncoding", false, "Cp1251"), maxLength);
//      cfgTuner.readFile( startPath + fileName);
                if(sa != null) {
                    String cont = "==";
                    for (String sa1 : sa) {
                        cont = cont + "\n" + sa1;
                    }
                    cfgTuner.addParameter(fileContentParam, cont);
                } else {
                    cfgTuner.addParameter(fileContentParam, "file not found " + startPath + fileName );                    
                }               
            } catch (Exception e) {
                cfgTuner.addParameter(fileContentParam, "file not found " + startPath + fileName + ".  error:" + e.toString());
            }
        } else {
            cfgTuner.addParameter("disabled", "yes");
        }
    }

    /**
     * Writes into the file value of the parameter "contents2"
     * .<p>
     *
     * The path to the file can be specified in the "DataStartPath" parameter.
     * If this parameter not specified - "CfgRootPath" will be used.<p>
     *
     * If there is section "allowedReadFiles" in the config. file - checks, if
     * the fileName is listed there.
     *
     * @param cmd : [content=>]fileName, content can be the name of the
     * parameter, containing the data to be written or name of a section
     * @exception java.lang.Exception
     * @see #readDataFile
     * @see #deleteDataFile
     */
    public void writeDataFile(String cmd) throws Exception {
        String fileName = cmd;
        String src = "contents2";
        int i = cmd.indexOf("=>");
        if (i > 0) {
            src = cmd.substring(0, i);
            fileName = cmd.substring(i + 2);
//     debugPrintln("'" + src + "' : '" +  fileName + "'");    
        }
//  String cont = "";
        String conta[];
        FileContent fc;
        i = src.indexOf("[");
        if (i < 0) {
//            conta = new String[1];
//    cont = cfgTuner.getParameter(src);
            fc = new FileContent(cfgTuner.getParameter(src).getBytes(), "");
        } else {
            if (i == 0) {
                conta = cfgTuner.getCustomSection(src);
            } else { // debugPrintln("*** source:'" + src.substring(0,i) + "':'" + src.substring(i) + "'");
                conta = cfgTuner.getCustomSection(src.substring(0, i), src.substring(i));
            }
            fc = new FileContent(StrUtil.strFromArray(conta, "\r\n").getBytes(), "");
        }
        ;
        String allowedFiles = " " + StrUtil.strFromArray(cfgTuner.getCustomSection("allowedWriteFiles"));
        if (allowedFiles.length() < 2
                || allowedFiles.contains(" " + fileName + " ")) {
            String startPath = cfgTuner.getParameter("DataStartPath");
            if (startPath.length() < 1) {
                startPath = rm.getString("CfgRootPath");
            }
            System.out.println("===> Writing file: " + src + "==>" + startPath + fileName);
            fc.storeToDisk(startPath, fileName);
        } else {
            cfgTuner.addParameter("disabled", "yes");
        }
    }

    /**
     * Deletes the file.<p>
     *
     * The path to the file can be specified in the "DataStartPath" parameter.
     * If this parameter not specified - "CfgRootPath" will be used.<p>
     *
     * If there is section "allowedReadFiles" in the config. file - checks, if
     * the fileName is listed there.
     *
     * @param fileName the name of the file to read
     * @exception java.lang.Exception
     * @see #readDataFile
     * @see #deleteDataFile
     */
    public void deleteDataFile(String fileName) throws Exception {
        String allowedFiles = " " + StrUtil.strFromArray(cfgTuner.getCustomSection("allowedWriteFiles"));
        if (allowedFiles.length() < 2
                || allowedFiles.contains(" " + fileName + " ")) {
            String startPath = cfgTuner.getParameter("DataStartPath");
            if (startPath.length() < 1) {
                startPath = rm.getString("CfgRootPath");
            }
            IOUtil.writeLog(1, "XXX Deleting file: " + startPath + fileName, rm);

            File f = new File(startPath, fileName);
//            if (f == null) {
//                return;
//            }
            f.delete();
            IOUtil.writeLogLn(1, " - OK.", rm);
        } else {
            cfgTuner.addParameter("disabled", "yes");
            IOUtil.writeLog("XXXXX ERROR: Deleting file  " + fileName + " DISABLED! allowedFiles=" + allowedFiles, rm);
        }
    }

    /**
     * Executes an SQL and stores results in the cfgTuner.<p>
     *
     * It the SQL is a "SELECT" statement - sets parameters according to names
     * of the obtained fields. In case of several records makes a
     * comma-separated list of values.<p>
     *
     * Any other SQL statements will be just executed without setting of
     * parameters.
     *
     * @param sql - the SQL to be executed.
     * @exception java.lang.Exception
     * @see #beforeStart
     */
    public void getPreData(String sql) throws Exception {
        if (dbUtil == null || cfgTuner.enabledOption("NotConnected")) {
            IOUtil.writeLogLn(0, "xxxxx NOT CONNECTED TO DB xxxxx!", rm);
            return;
        }
//	debugPrintln("getPreData: dbUtil=" + dbUtil);    
        String finalMark = "";

        String val;
        int i = sql.indexOf("final:");
        if (i == 0) {
            sql = sql.substring(6).trim();
            finalMark = "==";
        }

        if (sql.toUpperCase().indexOf("SELECT") == 0) {
//debugPrintln("SQL:" + sql + ";");
            if (sql.toUpperCase().indexOf("SELECT_SP") == 0) {
                sql = sql.substring(9);
            }
            ResultSet r = dbUtil.getResults(sql);
            if (r != null) {
                IOUtil.writeLogLn(9, "*** done r = " +r.toString() , rm);
                String[] headers = DBUtil.getColNames(r);
                String[] params = new String[headers.length];
                for (i = 0; i < params.length; i++) {
                    params[i] = finalMark;
                }

                int nr = 0;
                while (r.next()) {
                    for (i = 0; i < params.length; i++) {
                        val = r.getString(i + 1);
                        if (val != null && val.length() > 0 && !val.equalsIgnoreCase("NULL")) {
                            IOUtil.writeLogLn(9, "***  " + i + ":" + val, rm);
                            params[i] = params[i] + val;
                        }
                        //          params[i] = params[i] + StrUtil.unicode(val);
                    }
                    nr++;
                }
                dbUtil.closeResultSet(r);

                String s = "";
                int n = 0;
                for (i = 0; i < params.length; i++) {
                    if (params[i].length() > 0) {
                        s = s + "\r\n" + headers[i] + "=" + params[i];
                        cfgTuner.addParameter(headers[i], params[i]);
//						System.out.println(n + ":" + headers[i]+ "=" +params[i]);
                        n++;
                    }
                }
//                if (s.length() > 2) {
//                    s = s.substring(2);
//                }
                IOUtil.writeLog(1, "<xmp>***** " + nr + " record(s) retrieved. Filled "
                        + n + " parameters: " + s + "</xmp>", rm);
            }
        } else if (sql.toUpperCase().indexOf("$EXEC_PROCEDURE") == 0) {
            String proc = sql.substring(("$EXEC_PROCEDURE").length()).trim();
            dubna.walt.service.Service.executeProcedure(proc, dbUtil, rm);
        } else {
            int numRecs = dbUtil.update(sql);
            cfgTuner.addParameter("NUM_RECORDS_AFFECTED", Integer.toString(numRecs));
            IOUtil.writeLog(1, " DONE! " + numRecs + " record(s) affected", rm);
        }
    }

    /**
     *
     * @param command
     * @return
     * @throws Exception
     * https://qnikst.wordpress.com/2010/05/09/java-runtime-exec/
     */
    public static String executeCommand(String command, Tuner t) throws Exception {
        System.out.println("executeCommand: " + command);
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(command);
        InputStream from = p.getInputStream();
        InputStream err = p.getErrorStream();

        String pid = t.getParameter("PROCESS_ID");
        ResourceManager rmg = null;
        if(pid.length() > 1) {
            rmg = t.rm.getGlobalRM();
            rmg.putObject(pid, p);
            IOUtil.writeLogLn(3, "<b>executeCommand: process stored: pid=" + pid + "; p=" + p + "</b>", rmg); 
        }
        p.waitFor();
        System.out.println("executeCommand done.");

//        http://developer.alexanderklimov.ru/android/java/inputstream.php
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        StringBuilder result = new StringBuilder();
        Reader in = new InputStreamReader(from, "UTF-8");
        for (; ; ) {
            int rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0)
                break;
            result.append(buffer, 0, rsz);
        }
 
        Reader errStream = new InputStreamReader(err, "UTF-8");
        for (; ; ) {
            int rsz = errStream.read(buffer, 0, buffer.length);
            if (rsz < 0)
                break;
            result.append(buffer, 0, rsz);
        }
        if(rmg != null) {
            IOUtil.writeLogLn(3, "<b>executeCommand: process finished: pid=" + pid + "; p=" + p + ";</b> result=" + result, rmg); 
            rmg.removeKey(pid);
//            rmg.putObject(pid, "PROCESS_FINISHED");
        }
        System.out.println("executeCommand result=" + result.toString());
        return (result.toString());

    }

    /**
     * Find the value for the cookie.<P>
     *
     * @param name - the name of the cookie.
     * @return the string presentation of the cookie value<BR>
     * or empty string if the cookie not exists.
     */
    public String getCookieValue(String name) {
        if (cookies == null && request!=null) {
            cookies = request.getCookies();
        }

        if (cookies != null) {
//    System.out.println(" Looking for :"+paramName);
            for (Cookie cookie : cookies) {
                //System.out.println(" next cookie:"+cookies[i].getName());
                if (cookie.getName().equals(name)) {
                    return cookie.getValue().trim();
                }
            }
        }
        return "";
    }

    /**
     * Termination of the query processing.
     */
    public void setTerminated() {
        terminated = true;
//  debugPrint("=========== SERVICE - setTerminated");
        try {
            out.close();
            out = null;
        } catch (Exception e) {
        }
        //       if (dbUtil != null) dbUtil.terminate();
        if (terminator != null) {
            terminator.finished = true;
            terminator = null;
        }
    }

    /**
     * Obtains the customized SQL from the template.
     *
     * @param sqlSectionName
     * @return the customized SQL
     * @throws java.lang.Exception
     * @see dubna.walt.util.Tuner
     * @see dubna.walt.util.TunerSQL
     */
    public String getSQL(String sqlSectionName) throws Exception {
        String sql[];
        IOUtil.writeLogLn(1, " SQL: [" + sqlSectionName + "]", rm);
        sql = cfgTuner.getCustomSection(sqlSectionName);
        if (sql == null) {
            return null;
        }

//  TunerSQL.like2OrLike(sql);
        TunerSQL.cleanSQL(sql);
        logSQL(sqlSectionName, sql, rm);
        return StrUtil.strFromArray(sql);
    }

    /**/
    /**
     * Writes the SQL into the log file.
     *
     * @param sqlSectionName the name of the section, containing the SQL (used
     * as header)
     * @param sql the SQL
     * @param rms the Static copy of ResourceManager.
     */
    public static void logSQL(String sqlSectionName, String[] sql, ResourceManager rms) {
        IOUtil.writeLog(1, "<br> <b>SQL: [" + sqlSectionName + "]:</b><xmp>", rms);
        for (String sql1 : sql) {
            if (sql1.trim().length() > 0) {
                IOUtil.writeLog(2, sql1 + "\r\n", rms);
            }
        }
        IOUtil.writeLog(1, "</xmp>", rms);
    }

    /**
     * Writes the SQL string into the log file.
     *
     * @param sqlLabel label for the SQL text
     * @param sql the SQL
     * @param rms the ResourceManager.
     */
    public static void logSQL(String sqlLabel, String sql, ResourceManager rms) {
        IOUtil.writeLog(1, "<b>" + sqlLabel + ":</b>", rms);
        IOUtil.writeLog(2, "<xmp>" + sql + "</xmp>", rms);
    }

    /**
     * Fetches the statement from the section of the configuration file.
     *
     * Customizes the statement. The statements in the section must be separated
     * by semicolon (;) at the end of the line.
     *
     * @param cfgTuner - the config. Tuner
     * @param scriptSectionName - the name of the section
     * @param sqlNr - the number of the statement to be fetched
     * @return 
     * @exception java.lang.Exception
     * @see #beforeStart
     */
    public static String getSqlNr(Tuner cfgTuner, String scriptSectionName, int sqlNr) throws Exception {
        String sql = "";
        cfgTuner.addParameter("sqlNr", Integer.toString(sqlNr));
        String[] sql_s = cfgTuner.getCustomSection(scriptSectionName);
//  System.out.println(" === " + scriptSectionName + " : sql_s=" + sql_s + "; cfgTuner:"+cfgTuner);
        if (sql_s == null) {
            return null;
        }
        int numLines = sql_s.length;
//  System.out.println(" === " + scriptSectionName + " : sql_s.numLines=" + numLines);
        String[] sqla = new String[numLines];

        int i = 0;
        /* copy script to working array */
        for (; i < numLines; i++) {
            sqla[i] = " " + sql_s[i];
        }

//  System.out.println(cfgTuner.getParameter("cfgFile") + " === " + scriptSectionName + " : " + sqla);
//  IOUtil.writeLog(9, " SRC for Script " + sqlNr, sqla, rms);
        /* erase leading 'sqlNr-1' SQLs */
        int j = 0;
        for (i = 0; i < sqlNr - 1; i++) {
            for (; j < numLines && sqla[j].indexOf(";") < sqla[j].length() - 1; j++) {
                sqla[j] = " ";
            }

            if (j < numLines) {
                sqla[j] = " ";
            } else {
                return null;
            }
        }

//  IOUtil.writeLog(" Search for the end... " + sqlNr + "; j=" + j, sqla, rms);
//  IOUtil.writeLogLn(" j=" +j + ":" + sqla[j].indexOf(";") + ":" + sqla[j].length(), rms);
        /* find the end of the SQL */
        for (; j < numLines - 1 && (sqla[j].indexOf(";") < sqla[j].length() - 1); j++) 
  ;

//  IOUtil.writeLog(9, " Start Script " + sqlNr + "; j=" + j, sqla, rms);
        /* remove the last ; */
        if (sqla[j].indexOf(";") == sqla[j].length() - 1) {
            sqla[j] = sqla[j].substring(0, sqla[j].length() - 1);
        }
        /* and erase from there to the end */
        for (i = j + 1; i < numLines; i++) {
            sqla[i] = "";
        }

//  IOUtil.writeLog(8, " Resulting SQL " + Integer.toString(sqlNr), sqla, rms);
//  TunerSQL.like2OrLike(sqla);
        TunerSQL.cleanSQL(sqla);
        logSQL( scriptSectionName + " (" + Integer.toString(sqlNr) + ") ", sqla, rms);

        return StrUtil.strFromArray(sqla).trim();
//  return StrUtil.strFromArray(sqla).replace(';',' ').trim();
    }

    /**
     * Processes "$CALL_SERVICE" directive.<p>
     *
     * Parses the parameters and calls method:
     * dubna.walt.service.Service.callSubService.<br>
     *
     * <b>Syntax:</b><br>
     * c=cfgFileName [; paramName=paramValue; [paramName=paramValue]...]
     * <p>
     * <b>Depricated syntax:</b><br>
     * cfgFileName [paramName=paramValue [paramName=paramValue]...]
     *
     * @param params name of the section of the configuration file (without
     * brackets "[]").
     * @throws java.lang.Exception
     *
     */
    public void callService(String params) throws Exception {
        String cfg = "";
        Vector par = new Vector(10);
        /* new sintax of the $CALL_SERVICE directive */
 /* $CALL_SERVICE c=cfgFileName [; paramName=paramValue; [paramName=paramValue]...] */
        if (params.contains("c=")) {
            StringTokenizer st = new StringTokenizer(params, ";");
            while (st.hasMoreTokens()) {
                String token = st.nextToken().trim();
                if (token.indexOf("c=") == 0) {
                    cfg = token.substring(2);
                    cfg = cfgTuner.getModFileName(cfg, "SIMPLE");
                } else {
                    par.addElement(token);
                }
            }
            callService(cfg, par);
//    dubna.walt.service.Service.callSubService(cfg, par, rm);
        } /* depricated sintax of the $CALL_SERVICE directive */ /* $CALL_SERVICE cfgFileName [ paramName=paramValue [ paramName=paramValue]...] */ else {
            StringTokenizer st = new StringTokenizer(params, " ");
            if (st.hasMoreTokens()) {
                cfg = st.nextToken();
                if (cfg.contains("#")) {
                    cfg = cfgTuner.parseString(cfg);
                }
                while (st.hasMoreTokens()) {
                    par.addElement(st.nextToken());
                }
                IOUtil.writeLogLn(2, "+++++ Depricated syntax: $CALL_SERVICE " + params, rmg);
                callService(cfg, par);
            }
        }
    }

    /**
     *
     * @param cfg
     * @param params
     * @throws Exception
     */
    public void callService(String cfg, Vector params) throws Exception {
        if (cfg == null || cfg.length() == 0) {
            return;
        }
        if (terminated) {
            return;
        }

        if (!cfg.contains(".")) {
            cfg = cfg + ".cfg";
        }

        String parentLog = rm.getString("log", false, "false");
        IOUtil.writeLogLn(5, " =====> $CALL_SERVICE cfg=" + cfg, rm);
//        if(cfg.equalsIgnoreCase("process_client.cfg"))
//System.out.println("+++++ $CALL_SERVICE cfg=" + cfg);

        String stackTrace = rm.getString("stackTrace", false, module) + " -> " + cfg;
//        stackTrace += " / " + cfg;
        rm.putString("stackTrace", stackTrace);
        
        ResourceManager ssrm = rm.cloneRM();
        Tuner serviceTuner = new Tuner(cfgTuner.getParameters(), cfg, rm.getString("CfgRootPath", false), ssrm);

        Tuner thisTuner = cfgTuner;
        String thisCfg = cfgTuner.getParameter("cfgFile");
        serviceTuner.addParameter("cfgFile", cfg);
        serviceTuner.addParameter("c", cfg.substring(0, cfg.lastIndexOf('.')));
        serviceTuner.addParameter("LOG", cfgTuner.getParameter("LOG")); 

//  if (cfgTuner.enabledOption("debug=on"))
        /* Now set the additional parameters, which may be defined in the Vector "params" */
        for (int i = 0; i < params.size(); i++) {
            serviceTuner.addParameter((String) params.elementAt(i), null);
        }
        
        String service = serviceTuner.getParameter("service");
        
        if (service.length() == 0) 
            service = "dubna.walt.service.Service";
        
        rms = ssrm;      
        
        logSubserviceStarted(serviceTuner, thisCfg);

        /* create and start the subservice */
        try {
            Service subsrv = dubna.walt.SimpleQueryThread.obtainService(service);
//System.out.println("+++++ $CALL_SERVICE subsrv=" + subsrv);
            subsrv.out = this.out;
            ssrm.setObject("cfgTuner", serviceTuner);
            if (terminated) {
                return;
            }
            if (terminator != null) {
                terminator.srv = subsrv;
            }
            subsrv.terminator = this.terminator;
//System.out.println("+++++ $CALL_SERVICE subsrv.doIt()");
            subsrv.doIt(ssrm);
            if (subsrv.terminated) {
                setTerminated();
            } else if (terminator != null) {
                terminator.srv = this;
            }
        } catch (java.lang.ClassNotFoundException e) {
            out.println("<b>Could not find SubService class: " + service + " called from: '" + cfg + "'</b>");
            return;
        }

        stackTrace = rm.getString("stackTrace", false, module).replace( " -> " + cfg, "");
//        stackTrace += " / " + cfg;
        rm.putString("stackTrace", stackTrace);

        logSubserviceFinished(serviceTuner, thisCfg);
        ssrm.setParam("log", parentLog);
        rm.setParam("log", parentLog);
        cfgTuner = thisTuner;
        cfgTuner.parameters = serviceTuner.getParameters();
        cfgTuner.addParameter("cfgFile", thisCfg);
        if(thisCfg.contains("."))
            cfgTuner.addParameter("c", thisCfg.substring(0, thisCfg.lastIndexOf('.')));
        else
            cfgTuner.addParameter("c", thisCfg);
        rm.setObject("cfgTuner", cfgTuner);
        rms = rm;

//        debugPrint(" ====> BACK TO SERVICE: " + cfg + "=>" + thisCfg );
        if(dbUtil != null) {
//            System.out.println("; dbUtil=" + dbUtil.myName + ". isAlive()=" + dbUtil.isAlive());
            if(!dbUtil.isAlive()) {
                dbUtil = null;
                System.out.println(" XXXXXX ERROR: dbUtil IS DEAD!");
                makeDBUtil("");
            }
        }
        else
            System.out.println("; dbUtil IS NULL!"); 
        if(dbUtil == null) 
            makeDBUtil("");
    }
    
    /**
     * Запись в лог-файл строки завершения $CALL_SERVICE
     *
     * @param serviceTuner
     * @param thisCfg
     */
    public void logSubserviceFinished(Tuner serviceTuner, String thisCfg) {
        if(rm.getInt("LogLevel", 0) < 0) return;
        long tm2 = System.currentTimeMillis() - tm;
        String ss_c = serviceTuner.getParameter("c");
        String this_c = thisCfg.replace(".cfg", "");

        if (!excludeFromLog.contains(',' + ss_c + ',')
                && !excludeFromLog.contains(',' + this_c + ',')
                && !serviceTuner.enabledOption("LOG=OFF")
           )

        {
            IOUtil.writeLogLn(1, "</div><span class='ss_back pt' "
                    + " onClick='toggleDiv(\"" + (ss_c + tm) + "\");'> BACK: <b>" + ss_c + " => "
                    + thisCfg + "; (" + tm2 + "ms.)</b></span>", rmg);
        }
    }

//    ,wf/show_wf_for_doc,wf/graph_show_wf_for_doc,sys/checkAdminRights,wf/show_wf_status,docs/out_files_list,docs/doc_files_list,docs/doc_field_file,sys/request_log,viewer/page_image,viewer/show_thumbnails,viewer/show_markup_icons,viewer/show_markup_list,viewer/show_file_body,svs/getParamLength,free/checkSession_noDB,
    /**
     * Запись в лог-файл строки вызова $CALL_SERVICE
     *
     * @param serviceTuner
     * @param thisCfg
     */
    public void logSubserviceStarted(Tuner serviceTuner, String thisCfg) {

        if(rm.getInt("LogLevel", 0) < 0) return;
        tm = System.currentTimeMillis();
        serviceTuner.addParameter("tm", Long.toString(tm));
        excludeFromLog = rm.getGlobalRM().getString("excludeFromLog", false, "");
        String ss_c = serviceTuner.getParameter("c");
        String this_c = thisCfg.replace(".cfg", "");


        if (!excludeFromLog.contains(',' + ss_c + ',')
                && !excludeFromLog.contains(',' + this_c + ',')
                && !serviceTuner.enabledOption("LOG=OFF")
           )
        {
            IOUtil.writeLog(1, "<br><span class='ss_start pt' "
                    + " onClick='toggleDiv(\"" + (ss_c + tm) + "\");'> $CALL_SERVICE: <b>"
                    + rm.getString("stackTrace", false) + "</b><small> (request:"
//                    + thisCfg + " => " + ss_c + "</b><small> (request:"
                    + rm.getString("queryLabel") 
                    + ")</small></span><div id='" + (ss_c + tm) + "' class='ss'>", rmg);

            if (serviceTuner.enabledOption("LOG=OFF")) {
                IOUtil.writeLog(" <b>LOG=OFF</b> ", rmg);
                rms.setParam("log", "false");
            } else if (serviceTuner.enabledOption("LOG=ON")) {
                rms.setParam("log", rm.getGlobalRM().getString("log", false,"true"));
            }
            IOUtil.writeLog(5, "Parameters:", serviceTuner.parameters, rms);
        }
//        else
//            rms.setParam("log", "false", false);       
    }


    /**
     * Executes the SQL and collects the obtained results using section of the
     * configuration file.
     *
     * For every record obtained sets the parameters in the cfgTuner, customizes
     * the specified section and appends it to the result.
     *
     * @param sqlSectionName - the name of the section, containing the template
     * for the SQL to be executed.
     * @param itemsSectionName - the name of the section of the config. file to
     * be used for formatting of every record retrieved.
     * @return 
     * @exception java.lang.Exception
     */
    public String collectFormattedData(String sqlSectionName, String itemsSectionName) throws Exception {
        String sql = getSQL(sqlSectionName);
        if (sql == null || cfgTuner.enabledOption("NotConnected")) {
            return "";
        }

        String result = "";
        String val;

        ResultSet r = dbUtil.getResults(sql);
        String[] headers = DBUtil.getColNames(r);

        while (r.next()) {
            for (int i = 0; i < headers.length; i++) {
                val = r.getString(i + 1);
                if (val == null || val.equalsIgnoreCase("NULL")) {
                    val = "";
                }
//      cfgTuner.addParameter(headers[i], cfgTuner.parseString(val));
                cfgTuner.addParameter(headers[i], val);
            }
            String[] sa = cfgTuner.getCustomSection(itemsSectionName);
            result += StrUtil.strFromArray(sa);
        }
        dbUtil.closeResultSet(r);
        return result;
    }

    /**
     *
     * @param sqlProc
     * @param dbUtil
     * @param rm
     */
    public static void executeProcedure(String sqlProc, DBUtil dbUtil, ResourceManager rm) {
        Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
        try {
            IOUtil.writeLog(1, "<b>Executing procedure : </b>" + sqlProc + "...", rm);
            Connection conn = dbUtil.getConnection();
            CallableStatement cs = conn.prepareCall(sqlProc);
            cs.execute();
            IOUtil.writeLogLn(1, " <b>OK - executed.</b>", rm);
            cs.close();
        } catch (Exception e) {
            String msg = e.toString().replaceAll("'", "`");
            cfgTuner.addParameter("PLSQL_ERROR", msg);
            cfgTuner.addParameter("ERROR", msg);
            IOUtil.writeLogLn("PLSQL: " + sqlProc +"; ERROR:"+ msg, rm);
            rm.logException("PLSQL_ERROR:", e);
        }
    }

    /**
     * Validates the query pararameters.
     *
     * If "ParamValidator" class name not specified in the resource file - does
     * nothing.
     *
     * @see dubna.walt.util.ParamValidator
     * @exception java.lang.Exception
     */
    public void validateParameters() throws Exception {
        ParamValidator pv = (ParamValidator) rm.getObject("ParamValidator", false);
        if (pv != null) {
//  System.out.println("ParamValidator Class Name: " + pv.getClass().getName() );
            pv.validate(cfgTuner, rm);
        }
        fixTextFields();
//  System.out.print("=   DONE! ");
    }

    /**
     *
     * @exception java.lang.Exception
     */
    public void fixTextFields() throws Exception {
        String s = cfgTuner.getParameter("fixTextFields");
        if (s.length() > 0) {
            StringTokenizer st = new StringTokenizer(s, ",");
            String v;
            while (st.hasMoreTokens()) {
                s = st.nextToken();
                v = cfgTuner.getParameter(s);
                if (v.length() > 0 && v.indexOf("'") > 0) {
                    v = v.replace('\'', '`');
                    cfgTuner.addParameter(s, v);
//        System.out.println(s+"=" + v );
                }
            }
        }
//  System.out.print("=   DONE! ");
    }

    /**
     *
     * @param msg
     */
    public void debugPrint(String msg) {
        if (cfgTuner == null || cfgTuner.enabledExpression("debug=on")) {
            System.out.print(msg);
        }
    }
    /**
     *
     * @param msg
     */
    public void debugPrintln(String msg) {
        if (cfgTuner == null || cfgTuner.enabledExpression("debug=on")) {
            System.out.println(msg);
        }
    }

}
