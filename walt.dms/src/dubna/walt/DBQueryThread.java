package dubna.walt;

import dubna.walt.util.*;


/**
 *
 * @author serg
 */
public class DBQueryThread extends SimpleQueryThread {

    public void init(ResourceManager rm) throws Exception {
        super.init(rm);
//  System.out.println("\n\r start " + queryLabel);
//  dbUtil = makeDBUtil();
//  System.out.println(queryLabel + ": got DBUtil: " + dbUtil.myName);
    }

    /*	public void XXXwriteHttpHeaders() throws Exception {
		super.writeHttpHeaders();
		dbUtil = makeDBUtil();
//  System.out.println(queryLabel + ": got DBUtil: " + dbUtil.myName); 
	}
     */
    /**
     *
     * @return @throws Exception
     */
    public DBUtil makeDBUtil() throws Exception {
        if (cfgFileName.contains("_noDB")) {
            return null;
        }
        ConnectionPool cp = (ConnectionPool) rm.getObject("ConnectionPool", false);
        String connectTime="?";
        String connectString=  rm.getString("connString", true)
                    + rm.getString("database", false)
                    + rm.getString("connParam", false);
                    
        try {
            IOUtil.writeLog(7, "<br><small><i>DBQueryThread.makeDBUtil("  + queryLabel 
                    + ") DB: " 
                    + rm.getString("DB", true, "")
                    + " / " + connectString
                    + " / " + rm.getString("usr", true)
                    + "</i> cp:" + cp + "</small>...", rm);
//            System.out.print(" *** DBQueryThread.makeDBUtil() " + queryLabel + ": " + rm.getString("connString") + "; user=" + rm.getString("usr", false));
           
            /* Establish connection to the database and make DBUtil */
            if(cp == null) {
                System.out.print("   qt: " + rm.getString("queryLabel") + "  connect:" 
                    + ") DB: " 
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
                    , queryLabel
//                        , 1
                    );
                connectTime=Long.toString(System.currentTimeMillis() - startTm);
                System.out.println(" - OK " + connectTime + "ms.");
            }
            else {
//                dbUtil = new DBUtil(cp, queryLabel);
                System.out.print(" *** DBQueryThread." + rm.getString("queryLabel") + ".makeDBUtil(): cp=" + cp + "; ");          
                dbUtil = new DBUtil(cp, rm.getString("queryLabel"));
                System.out.println("     got:" + dbUtil + "; m_conn=" + dbUtil.m_conn);

            }
//            dbUtil.nrConnsToKeep = 0;
//            dbUtil.allocate();
            rm.setObject("DBUtil", dbUtil, false);
            connectTime=Long.toString(System.currentTimeMillis() - startTm);
//            System.out.println(" - OK " + connectTime + "ms.");
            IOUtil.writeLog(7, " - OK! " + connectTime + "ms. ", rm);
            cfgTuner.addParameter("connectTime", connectTime);
//    rm.setObject("DBUtil", dbUtil, true);
        } catch (Exception e) {
            System.out.println("[" + Fmt.shortDateStr(new java.util.Date()) + "] Connection to " + rm.getString("connString") + " FAILED!..." + e.toString());
            e.printStackTrace(System.out);
            if (outWriter != null) {
                outWriter.println("<small>" + e.getMessage() + "</small>");
                outWriter.println("<center><br><br><table border=1 bgcolor=#FFEEBB cellpadding=8><tr><th>"
                        + "Нет связи с базой данных!"
                        //					+ rm.getString("connString") 
                        + "</th></tr></table></center><p>");
                outWriter.flush();
            }
            return null;
        }
        getInitParams();
        return dbUtil;
    }

    /**
     *
     */
    public void getInitParams() {
    }

}
