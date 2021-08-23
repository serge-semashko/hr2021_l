package dubna.walt.util;

import java.sql.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Locale;

/**
 *
 * @author serg
 */
public class DBUtil {

//public static boolean busy=false;
    public long timer = 0, t0;

    /**
     *
     */
    public int numQueries = 0;

    /**
     *
     */
    public String timeSQL = "";

    /**
     *
     */
    public String timeSpent = "";

    /**
     *
     */
    public String myName = "";

    /**
     *
     */
    public String connStr = "";

    /**
     *
     */
    public String usr = "";

    /**
     *
     */
    public String pw = "";

    /**
     * NOT USED
     */
    public String queryLabel="DBUtil";
    /**
     *
     */
    public Connection m_conn = null;

    /**
     *
     */
    public Hashtable stmnts = null;
//    public Statement curr_stmt = null;

//private DBUtil childDB = null;
    /**
     *
     */
    public boolean terminated = false;
//private boolean inUse = false;

    /**
     *
     */
    public static int DB_ORA = 0;

    /**
     *
     */
    public static int DB_MSSQL = 1;

    /**
     *
     */
    public static int DB_MySQL = 2;

    /**
     *
     */
    public int db = DB_ORA;
    
    private ConnectionPool cp;
    private ResourceManager rm;

    /**
     *
     * @throws Exception
     */
    public DBUtil() throws Exception {
    }
    
       /**
     *
     * @param rm
     * @throws java.lang.Exception
     */
    public DBUtil(ResourceManager rm) throws Exception {              
        this.cp = (ConnectionPool) rm.getObject("ConnectionPool", false);
        this.queryLabel = rm.getString("queryLabel", false, "ql:?");
        this.rm = rm;
        this.myName = queryLabel + this.toString().substring(15);       

        if(cp == null) {
            this.connStr =  rm.getString("connString", true)
                        + rm.getString("database", false)
                        + rm.getString("connParam", false);
            this.usr = rm.getString("usr", true);
            this.pw = rm.getString("pw", true);
        }
        connect();
    }
    
    /**
     *
     * @param cp
     * @param queryLabel
     * @throws java.lang.Exception
     */
    public DBUtil(
            ConnectionPool cp,
            String queryLabel          
        ) throws Exception {
        
        this.cp = cp;
        this.queryLabel = queryLabel;
        this.rm = cp.rm;
        myName = queryLabel + this.toString().substring(15);
        connect();
        System.out.println("..... DBUtil." + myName + ": CP => m_conn=" + m_conn);
//        setConnOptions();
    }
    
    /**
     *
     * @param db_type
     * @param connStr
     * @param usr
     * @param pw
     * @param queryLabel
     * @throws java.lang.Exception
     */
    public DBUtil(
            String db_type,
            String connStr,
            String usr,
            String pw,
            String queryLabel
            
        ) throws Exception {
        this.connStr = connStr;
        this.usr = usr;
        this.pw = pw;
        this.queryLabel = queryLabel;
        setDbType(db_type);
        myName = queryLabel + this.toString().substring(15);
        connect();
//        setConnOptions();
    }
    
   /**
     *
     * @param conn
     * @param queryLabel
     * @throws Exception
     */
    public DBUtil(Connection conn, String queryLabel) throws Exception {
        m_conn = conn;
//        this.queryLabel = queryLabel;
        stmnts = new Hashtable();
        myName = queryLabel + this.toString().substring(15);
    }

    /*
    private void setConnOptions() throws SQLException{
        stmnts = new Hashtable();
        if (db == DB_MySQL) {
            m_conn.setAutoCommit(false);  //***** 14.05.06    
        } //  if (db == DB_ORA)
        else {
            m_conn.setAutoCommit(true);  //***** 01.04.06
            try //***** 01.04.06
            {
                Statement stmt = m_conn.createStatement();
                int res = stmt.executeUpdate("ALTER SESSION SET NLS_NUMERIC_CHARACTERS='. ' ");
                stmt.close();
            } catch (Exception e) /**/  /*{
                /* we don't care - this is valid for ORACLE only */ /*}
        }        
    }
    /**/
    
    /**
     *
     * @throws Exception
     */
    public void reconnect() throws Exception {
        commit();
        close();
        System.gc();
        Thread.sleep(1000);
        connect();
    }

    /**
     *
     * @throws Exception
     */
    public void connect() throws Exception {
        try {
            Locale.setDefault(Locale.ENGLISH);
            if(cp != null){
                setDbType(cp.getDB());
                if(rm != null)
                    m_conn = cp.getConnection(rm);
                else
                    m_conn = cp.getConnection(queryLabel);
//                System.out.println("...... DBUtil." + myName + ": CP => m_conn=" + m_conn);
            }
            else {
                m_conn = DriverManager.getConnection(connStr, usr, pw);
                System.out.println("_____. DBUtil." + myName + ": NOCP! m_conn=" + m_conn);
            }
        } catch (Exception e) { //rm.println("*Not Connected: " + e.toString());
            throw e;
        }
//  System.out.println("*Connected: " + myName); // + "; m_conn=" + m_conn);
//        setConnOptions();

        stmnts = new Hashtable();
        if (db == DB_MySQL) {
            m_conn.setAutoCommit(false);  //***** 14.05.06    
        } //  if (db == DB_ORA)
        else {
            m_conn.setAutoCommit(true);  //***** 01.04.06
            try //***** 01.04.06
            {
                Statement stmt = m_conn.createStatement();
                int res = stmt.executeUpdate("ALTER SESSION SET NLS_NUMERIC_CHARACTERS='. ' ");
                stmt.close();
            } catch (Exception e) /**/ {
                /* we don't care - this is valid for ORACLE only */ }
        }

    }
   
   
    /**
     *
     *
     * @throws java.lang.Throwable
     */
    @Override
      protected void finalize() throws Throwable {
        try { 
            close();
        } finally {
            super.finalize();
        }
      }
     
    /**
     *
     * @return
     */
    public boolean isAlive() {
// System.out.println( "***** isAlive() ..." + myName);
        if(m_conn == null)
            return false;
        try {
//  System.out.println( "***** isClosed():" + m_conn.isClosed() );
            if (m_conn.isClosed()) {
                throw (new Exception("Connection is closed"));
            }
//    if (!m_conn.isValid(5)) 
//        throw (new Exception("Connection not is valid")); 
            Statement stmt = m_conn.createStatement();
            stmt.close();
//    System.out.println("..... " + myName + "; Connection is alive.");
            return true;
        } catch (Exception e) {
//		System.out.println("xxxxx " + myName + "; Connection is dead: " + e.toString());
            closeAllStatements();
            try {
                m_conn.close();
            } catch (Exception ex) {
            }
            return false;
        }
    } 

    /**
     *
     * @return
     */
    public Connection getConnection() {
        return m_conn;
    }

    /**
     *
     * @param r
     */
    public void closeResultSet(ResultSet r) {
        if (stmnts == null || r == null) {
            return;
        }
        try { // System.out.println(stmnts.size() + ": removing " + r);
            Statement stmt = (Statement) stmnts.get(r);
            stmnts.remove(r);
//            if(stmt == null)
//                System.out.println("   db: " + myName + " CLS " + r + " / " + stmt + "; stmnts.size=" + stmnts.size());
            r.close();
            if (stmt != null) {
                stmt.close();
            }
//    System.out.println(stmnts.size() + ": removed... ");
        } catch (Exception e) {
            System.out.println("+++++++ " + myName + "; stmnts =" + stmnts + "; r=" + r);
            e.printStackTrace(System.out);
        }
    }

    private void closeAllStatements() {
        if (stmnts == null || stmnts.isEmpty()) {
            return;
        }
        ResultSet r;
        Enumeration e = stmnts.keys();
        while (e.hasMoreElements()) {
            try {
                r = (ResultSet) e.nextElement();
                closeResultSet(r);
                System.out.println(" ..... " + myName + ":  closing statements - OK");
            } catch (Exception ex) {
                System.out.println("XXXXXXX ERROR: " + myName + ": closeAllStatements(): " + ex.toString());
            }
        }
        stmnts.clear();
    }

    /**
     *
     * @param sql
     * @return
     * @throws Exception
     */
//public synchronized ResultSet getResults(String sql) throws Exception
    public ResultSet getResults(String sql) throws Exception {
        if (terminated) {
            return null;
        }
        if (m_conn == null) {
//		System.out.println("++++++++++ DBUtil closed! Could not execute 'getResults'" + this); 
            return null;
        }
        if (sql == null || sql.length() < 3) {
            return null;
        }

        if (stmnts.size() > 2)
            System.out.println("   db: " + myName + ": # of statements=" + stmnts.size() + ";");
        if (stmnts.size() > 10) {
            System.out.println("========== ERROR: Exception: " + myName + ": total number of statements is " + stmnts.size() + "; CLOSING ALL OF THEM ...");
            closeAllStatements();
        }

        try {
            numQueries++;
//    System.out.println(queryLabel+ " - executing " + numQueries + "...");
            Statement stmt = m_conn.createStatement();
//            curr_stmt = stmt;
            t0 = System.currentTimeMillis();
// System.out.println(queryLabel+ ": " + sql + ";");
            ResultSet r = stmt.executeQuery(sql);
            t0 = System.currentTimeMillis() - t0;
            timer += t0;
            timeSpent = StrUtil.formatDouble(t0, 0, " ");
//    timeSQL  += "<br>  SQL " + numQueries + " processed in " +timeSpent+"ms.";
            try {
                stmnts.put(r, stmt);
            //    System.out.println("   db: " + myName + " ADD " + r + " / "  + stmt + "; # of statements=" + stmnts.size() + ";");
            } catch (Exception exx) {
                exx.printStackTrace();
//			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
//      System.out.println("stmnts:" + stmnts + ";\n\r curr_stmt: "+ stmt + ";\n\r r:" + r);
//      System.out.println(exx.toString());
//      System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
                throw exx;
            }

//    curr_stmt = null;
//    if (stmnts.size() > 5)
//      System.out.println("~~~~~~~ " + myName + ": - there are " + stmnts.size() + " opened statements!!!");
//            stmt.close();
            return r;
            
        } catch (Exception ex) {
            if (terminated) {
                return null;
            }
            
            if(ex.getMessage().contains("Communications link failure")) {
                try {         
                    if(cp == null) {
                        closeAllStatements();
                        m_conn.close();
                        m_conn = null;
                        System.out.println("   Connecting: '" + connStr + "' (" + usr + "/*****)" ); 
                        connect();
                    }
                    else {
                        closeAllStatements();
                        cp.closeConnection(m_conn); // Закрыть и удалить отвалившуюся коннекцию в CP
                    }
                }
                catch (Exception ee) { 
                    ee.printStackTrace();
                }
            }
//    System.out.println("++++++++ " + myName + " - Exception:");
//    System.out.println("++++++++ " + ex.toString());
            throw new java.sql.SQLException(ex.getMessage().replaceAll(";",":") + ": SQL: `" + sql + "`");
//    throw ex;
        }
    }

    /**
     *
     */
    public void terminate() {
        terminated = true;
        try {
//		System.out.println( myName + "/terminate()");
//            if (curr_stmt != null) {
//                curr_stmt.cancel();
//                curr_stmt.close();
//            curr_stmt = null;
//            }
        } catch (Exception ex) {
//		System.out.println("++++++++ " + myName);
//    ex.printStackTrace(System.out);
        } finally {
            closeAllStatements();
        }
    }

    /**
     *
     */
    public void close() //throws Exception
    {
//        System.out.println("  dbu: " + myName + ".close() cp=" + cp);
        if(m_conn == null) return;
        
        closeAllStatements();
//        if(cp != null)
//            System.out.println("DBUtil." + myName + ".close() : CP!; m_conn=" + m_conn);
//        else
//            System.out.println("DBUtil." + myName + ".close() : NO CP!; m_conn=" + m_conn);

        if (m_conn != null) {
            if(cp != null){
                try {
                    cp.putback(m_conn, myName);
                }
                catch(Exception e){
//                    e.printStackTrace(System.out);
                    System.out.println(" ****** ERROR: DBUtil." + myName + ".close(): " + e.toString());
//                    cp = null;
                }
            }
            if(cp == null){
                System.out.println("---- dbu: " + myName + ".close() NO CP!");
                try {
                    m_conn.close();
                } catch (Exception ex) {
                    System.out.println( myName + " DBUtil." + myName + ".close " + ex.toString());
                    ex.printStackTrace(System.out);
    //    throw new Exception(ex.toString() + " (" + myName + "/close)");
                }
            }
        }           
        m_conn = null;
    }

    /**
     *
     * @param sql
     * @return
     * @throws java.lang.Exception
     */
    public int update(String sql) throws Exception {
//  System.out.println("------- update: " + sql);
        try {
            /**/
            int i = 0;
//   if (db != DB_MySQL)
            {
                if (sql.equalsIgnoreCase("COMMIT")) {
                    m_conn.commit();
                    return 1;
                }
                if (sql.equalsIgnoreCase("ROLLBACK")) {
                    m_conn.rollback();
                    return -1;
                }
            }
            /**/
// System.out.println(" ======== " + myName + " - 'UPDATE' ... db=" + db);
            numQueries++;
            t0 = System.currentTimeMillis();
            Statement stmt = m_conn.createStatement();
            int res = stmt.executeUpdate(sql);
            stmt.close();
            t0 = System.currentTimeMillis() - t0;
//    System.out.println(" === Done in " + t0 + " ms.");
            timeSpent = StrUtil.formatDouble(t0, 0, " ");
//    timeSQL  += "<br>  Update SQL " + numQueries + " processed in " +timeSpent+"ms.";

            return res;
        } catch (Exception ex) { //busy=false;
//    System.out.println("+++++++ " + myName + ": " + ex.toString() + ". SQL:" + sql);
//    ex.printStackTrace(System.out);
            throw new Exception(ex.toString() + ". SQL:" + sql + ";"); // (" + myName + "/update)");
        }
    }

    /**
     *
     * @param resultSet
     * @return
     * @throws java.lang.Exception
     */
    public static int getNumCols(ResultSet resultSet) throws Exception {
        if (resultSet == null) {
            return 0;
        }
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            return metaData.getColumnCount();
        } catch (Exception ex) {
            throw new Exception(ex.toString() + " (in DBUtil/getNumCols)");
        }
    }

    /**
     *
     * @param resultSet
     * @return
     * @throws java.lang.Exception
     */
    public static String[] getColNames(ResultSet resultSet) throws Exception {
        if (resultSet == null) {
            return null;
        }
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int numCols = metaData.getColumnCount();
            String[] sqlColNames = new String[numCols];
            for (int i = 0; i < numCols; i++) {
//      sqlColNames[i]=metaData.getColumnName(i+1);
                sqlColNames[i] = metaData.getColumnLabel(i + 1);
//			System.out.println("+ col." + i + "; ColumnName='" + sqlColNames[i] + "'");
            }
            metaData = null;
            return sqlColNames;
        } catch (Exception ex) {
            throw new Exception(ex.toString() + " (in DBUtil.getColNames)");
        }
    }

    /**
     *
     * @throws java.lang.Exception
     */
    public 
//            synchronized 
        void commit() throws Exception {
        try {
            m_conn.commit();
        } catch (Exception ex) {
            throw new Exception(ex.toString() + "/ " + myName + "/commit()");
        }
        Thread.sleep(100);  // delay for Access - to finish updating
    }

    private void setDbType(String db_type){
        switch (db_type) {
            case "ORA": this.db = 0; break;
            case "MSSQL": this.db = 1; break;
            case "MySQL": this.db = 2; break;
            default: this.db = 0; 
        }       
    }
}
