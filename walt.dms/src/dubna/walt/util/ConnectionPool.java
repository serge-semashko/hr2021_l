package dubna.walt.util;

/**
 *
 * @author serg
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;
import java.util.Vector;

public class ConnectionPool {

    public ResourceManager rm;
    private Vector<Connection> availableConns = new Vector<Connection>();
    private Vector<Connection> usedConns = new Vector<Connection>();
    private String db;
    private int maxConnCnt;

    public ConnectionPool(ResourceManager rm, int initConnCnt, int maxConnCnt) {
        this.rm = rm;
        this.maxConnCnt = maxConnCnt;
        try {
            Class.forName(rm.getString("dbDriver", true));
            Locale.setDefault(Locale.ENGLISH);
            this.db = rm.getString("DB", true, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < initConnCnt; i++) {
            availableConns.addElement(createConnection());
        }
        System.out.println("  ConnectionPool:  " + getAvailableConnsCnt() + " connections created. initConnCnt=" + initConnCnt + "; maxConnCnt=" + maxConnCnt);
    }

    private Connection createConnection() {
        Connection conn = null;
        String connString =  rm.getString("connString")
                    + rm.getString("database", false, "")
                    + rm.getString("connParam", false, "");
        
        System.out.print("   cp: connect:" 
                    + connString
                    + " //|| " + rm.getString("usr", false) + "/*** ");
        long tm = System.currentTimeMillis();

        try {
            conn = DriverManager.getConnection(
                connString
                , rm.getString("usr", false)
                , rm.getString("pw", false)
            );
            tm = System.currentTimeMillis() - tm;
            System.out.println("  OK " + tm + "ms.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * For back-compatibility ONLY! (Probably not used)
     * @param queryLabel
     * @return
     * @throws Exception 
     */   
    public Connection getConnection(String queryLabel) throws Exception {
        ResourceManager rml = rm;
        rml.putString("queryLabel", queryLabel);
        return getConnection(rml);
    }
        
    public synchronized Connection getConnection(ResourceManager rml) throws Exception {
        Connection conn = null;
        String queryLabel = rml.getString("queryLabel", false, "?");
//        try{
//            if(usedConns.size() > 0) {
//                System.out.println("   cp: " + queryLabel + " WARNING: getConnection(); " + getAvailableConnsCnt() + " conns free " + usedConns.size() + " used. maxConnCnt=" + maxConnCnt);
////                rm.putString("queryLabel", queryLabel);
//                ((Logger) rm.getObject("logger")).logRequest2DB(rml, "ConnectionPool: " + getAvailableConnsCnt() + " conns free " + usedConns.size() + " used.", null);
//            }
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
        if(usedConns.size() > maxConnCnt / 2) {
            System.out.println("   cp: WARNING: getConnection(); " + getAvailableConnsCnt() + " conns free " + usedConns.size() + " used. maxConnCnt=" + maxConnCnt);
//            rm.putString("queryLabel", queryLabel);
            ((Logger) rm.getObject("logger")).logRequest2DB(rml, "ConnectionPool WARNING: " + getAvailableConnsCnt() + " conns free " + usedConns.size() + " used.", null);
//        if (availableConns.isEmpty()) { 
            if(usedConns.size() >= maxConnCnt) {
                System.out.println("   cp:  ERROR: " + queryLabel + " getConnection ERROR: Max conn count " + maxConnCnt + " exeeded!  maxConnCnt=" + maxConnCnt);
                ((Logger) rm.getObject("logger")).logRequest2DB(rml, "ConnectionPool ERROR: Max conn count " + maxConnCnt + " exeeded! " + usedConns.size() + "conns used.", null);
                throw new Exception("Max conn count " + maxConnCnt + " exeeded!");               
            }
        } 
        
        if(availableConns.size() > 0) {
            conn = (Connection) availableConns.lastElement();
            availableConns.removeElement(conn);
            if(!checkConnection(conn)) {
                conn = createConnection();
            }
        } else if(usedConns.size() < maxConnCnt){
            conn = createConnection();
        }
            
        if(conn != null)
            usedConns.addElement(conn);
//        if(getAvailableConnsCnt() < 1)
//            System.out.println("   cp: NEW CONNECTION: " + conn + "");
        if(getAvailableConnsCnt() < 2)
            System.out.println("   cp: " + queryLabel + ".getConnection(); " + getAvailableConnsCnt() + " conns free " + usedConns.size() + " used. " + maxConnCnt + " max.");
        return conn;
    }

   
    private boolean checkConnection(Connection c) {
        try {   
            Statement s = c.createStatement();
            ResultSet r = null;
            if(db.equals("ORA"))
                r = s.executeQuery("select 1 from dual");
            else // if(db.equals("MySQL"))
                r = s.executeQuery("select 1");
            r.close();
            s.close();
            return true;
        }
        catch(Exception e){
            return false;
        }        
    }
    
    public String getDB() {
        return db;
    }
    
    public synchronized void putback(Connection c, String queryLabel) throws NullPointerException {
//        if(getAvailableConnsCnt() < 2)
//            System.out.println("   cp: " + queryLabel + " putback(); " + c + "; Avail.Conns: " + getAvailableConnsCnt());
        if (c != null) {
            if (usedConns.removeElement(c)) {
                availableConns.addElement(c);
            } else {
                throw new NullPointerException("Connection not in the usedConns array. queryLabel=" 
                    + queryLabel + "; Connection=" + c + "; AvailableConnsCnt= " + getAvailableConnsCnt());
            }
        }
        if(getAvailableConnsCnt() < 3)
            System.out.println("   cp: " + queryLabel + " putback();  Avail.Conns: " + getAvailableConnsCnt());
    }

    public synchronized void closeConnection(Connection c) throws NullPointerException {
        System.out.print("   cp:  closeConnection(); " + c );
        if (c != null) {
            try {
                c.close();
            }
            catch(Exception e) {                
            }
            if (!usedConns.removeElement(c) && !availableConns.removeElement(c)) {               
               throw new NullPointerException("Connection not in the Conns arrays");
            }
        }
        System.out.println("  OK: " + getAvailableConnsCnt() + " connections left.");
    }

    public int getAvailableConnsCnt() {
        return availableConns.size();
    }
    
    public void closeConnections(){
        System.out.println("  ..... ConnectionPool.closeConnections()");
        Connection conn = null;
        
        while(availableConns.size() > 0) {
            conn = (Connection) availableConns.lastElement();
            availableConns.removeElement(conn);
            try {
                conn.close();
            }
            catch (Exception e) {}
        }

        while(usedConns.size() > 0) {
            conn = (Connection) usedConns.lastElement();
            usedConns.removeElement(conn);
            try {
                conn.close();
            }
            catch (Exception e) {}
        }        
    }
    
        /**
     * Finalizer. Закрытие коннектов к базе, запись в консоль Томката в режиме
     * "debug=on"
     */
    @Override
    protected void finalize() {
        System.out.println("  ..... ConnectionPool.finalize()");
        closeConnections();
        System.gc();
        try {
            super.finalize();
        } catch (Throwable tr) {
            tr.printStackTrace(System.out);
        }
    }


}
