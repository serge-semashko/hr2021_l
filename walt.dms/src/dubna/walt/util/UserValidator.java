package dubna.walt.util;

import javax.servlet.http.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.sql.*;

/**
 *
 * @author serg
 */
public class UserValidator {

    /**
     *
     */
    protected static Tuner usersTuner = null;

    /**
     *
     */
    public DBUtil dbUtilLogin = null;

    /**
     *
     *
     * @param rm
     * @return
     * @throws java.lang.Exception
     */
    public 
//            synchronized 
        boolean validate(ResourceManager rm) throws Exception {
        Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
        cfgTuner.deleteParameter("logged");
        cfgTuner.deleteParameter("uname");
        String qn = rm.getString("loginCookieName", false);
        if (qn.length() < 2) {
            rm.setParam("loginCookieName", "adbl", true);
        }
        String q = cfgTuner.getParameter("q");
//	System.out.println("validate1: dbUtilLogin:" + dbUtilLogin);
//        makeLoginDBUtil(rm);
//        if (dbUtilLogin == null) {
//            return false;
//        }
//	System.out.println("validate2: dbUtilLogin:" + dbUtilLogin);

        if (q.length() < 3) {
            q = getCookieValue((HttpServletRequest) rm.getObject("request", true, false), qn);
        }
//			, rm.getString("loginCookieName", false, "adbl"));
//  System.out.println(" ============= q=" + q);
        q = StrUtil.unescape(q);
//  System.out.println(" =========== UserValidator.validate: " + qn  + "=" + q);
        if (q.length() > 10) {
            try {
                StringTokenizer st = new StringTokenizer(q, ":");
                String fp0 = st.nextToken();
                String sess_id = st.nextToken();
                String user_id = st.nextToken();
                String user_name = st.nextToken();
                cfgTuner.addParameter("uname", user_name);

                String loginURL = checkLogin(q, rm);
//    System.out.println("----------- UserValidator.validate: loginURL=" + loginURL);

                if (loginURL.equals("TRUE")) {
                    cfgTuner.addParameter("logged", "YES");
                    cfgTuner.addParameter("SESS_ID", sess_id);
                    cfgTuner.addParameter("USER_ID", user_id);
                    cfgTuner.addParameter("FIO", usersTuner.getParameter(user_id, "FIO"));
//    System.out.println("----------- UserValidator.validate:" + user_name + ": logged");
                    cfgTuner.deleteParameter("loginURL");
//				if (dbUtilLogin != null) //УБРАНО 01.06.2015 - сохранить dbUtilLogin для EDO UserValidator
//				{ dbUtilLogin.close();
//					dbUtilLogin = null;
//				}
                    return true;
                } else {
                    cfgTuner.addParameter("loginURL", loginURL);
                    cfgTuner.deleteParameter("logged");
                    cfgTuner.deleteParameter("SESS_ID");
                    cfgTuner.deleteParameter("USER_ID");
                    cfgTuner.deleteParameter("FIO");
//    System.out.println(user_name + ": NOT LOGGED");
//        System.out.println("******** loginURL=" + loginURL);
                }
            } catch (Exception e) {
                System.out.println(" =========== UserValidator Exception:");
                e.printStackTrace(System.out);
            }
        }
        String c = cfgTuner.getParameter("c");
        boolean log = (c.indexOf("login") >= 0);
//  System.out.println(" =========== UserValidator log:" + log);
//	если логин не прошел - сбрасываем dbUtilLogin для обновления коннекта
        if (dbUtilLogin != null) {
            try {
                dbUtilLogin.close();
            } catch (Exception e) {;
            }
            dbUtilLogin = null;
        }
        return (log);
//  return true;
    }

    /**
     * Finds a cookie with the given name and returns its value if the cookie is
     * found or null if the cookie is not found.
     *
     * @param request
     * @param cookieName
     * @return
     */
    public String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (int i = cookies.length - 1; i >= 0; i--) {
                if (cookies[i].getName().trim().equals(cookieName.trim())) {
                    return cookies[i].getValue().trim();
                }
            }
        }
        return "";
    }

    /**
     *
     * @param q
     * @param rm
     * @return
     * @throws Exception
     */
    public String checkLogin(String q, ResourceManager rm) // throws Exception
    { // if (q.length() < 32) return "*****";
//System.out.println("checkLogin... q = " + q);
        Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");

        String sqlFunc = "{?=call "
                + rm.getString("proc", false, "WL.CHECKLOGIN")
                + "(?)}";
        String loginURL = rm.getString("loginURL", false);
//  IOUtil.writeLogLn("<b>Executing procedure : </b>" + sqlFunc + "; q=" + q, rm);
        try {
            makeLoginDBUtil(rm);
            if (usersTuner == null) {
                makeUsersTuner(rm);
            }
            Connection conn = null;
            CallableStatement cs = null;
            try {
                conn = dbUtilLogin.getConnection();
                cs = conn.prepareCall(sqlFunc);
            }
            catch(Exception ex) {
                ex.printStackTrace();
                dbUtilLogin.close();
                dbUtilLogin = null;
                makeLoginDBUtil(rm);
                conn = dbUtilLogin.getConnection();
                cs = conn.prepareCall(sqlFunc);
            }
            cs.registerOutParameter(1, java.sql.Types.VARCHAR);
            cs.setString(2, q);
            cs.execute();
            loginURL = (String) cs.getObject(1);
//            cs.getResultSet().close();
            cs.close();
//            r = (ResultSet) e.nextElement();
//                closeResultSet(r);
//            conn.           
//            dbUtilLogin.closeAllStatements();
                    
//    System.out.println("******** loginURL=" + loginURL);
    IOUtil.writeLogLn(7, "******** UserValidator: loginURL=" + loginURL, rm);
        } catch (Exception e) {
            cfgTuner.addParameter("PLSQL_ERROR", e.toString());
            IOUtil.writeLogLn("<b>Executing procedure : </b>" + sqlFunc + "; q=" + q, rm);
            IOUtil.writeLogLn("PLSQL_ERROR: " + e.toString(), rm);
            e.printStackTrace(System.out);
            dbUtilLogin = null;
            rm.removeKey("dbUtilLogin");
        }
        return loginURL;
    }

    /**
     *
     * @param rm
     * @throws Exception
     */
    public void makeLoginDBUtil(ResourceManager rm) throws Exception {
        boolean isAlive=false;
        dbUtilLogin = (DBUtil) rm.getObject("dbUtilLogin", false);
//        System.out.print("dbUtilLogin=" + dbUtilLogin);
        if (dbUtilLogin != null) {
            isAlive = dbUtilLogin.isAlive();
//            System.out.println("; " + dbUtilLogin.myName + " isAlive=" + isAlive);
            if (!isAlive) {
                dbUtilLogin.close();
                dbUtilLogin = null;
                rm.removeKey("dbUtilLogin");
            }
        }
        if (dbUtilLogin == null) {
            String usr = rm.getString("usrLogin", false, rm.getString("usr", true));
            String pw = rm.getString("pwLogin", false, rm.getString("pw", false));
            String connStr = rm.getString("connStringLogin", false, rm.getString("connString", true));
//            IOUtil.writeLogLn(2, "+++++ UserValidator: connecting... " + connStr + "/" + usr, rm);
//            System.out.print("+++++ UserValidator: connecting... " + connStr + "/" + usr + "/***"); // +  pw);
            System.out.print("-----> UserValidator: connecting...  ");
            long tm = System.currentTimeMillis();
            try {
                dbUtilLogin = new DBUtil("MySQL", connStr, usr, pw, "dbUtilLogin");
                tm = System.currentTimeMillis() - tm;
                System.out.println(" - OK " + tm + "ms");
                rm.setObject("dbUtilLogin", dbUtilLogin, true);
            } catch (Exception e) {
                dbUtilLogin = null;
                System.out.println(" - ERROR! ");
                System.out.println("=======  [" + Fmt.shortDateStr(new java.util.Date()) + "] UserValidator.makeLoginDBUtil() - ERROR: " + e.toString());
//                        System.out.println("===" + connStr + "/" + usr + "/" + pw);
            }
        }
    }

    /**
     *
     * @param rm
     */
    public void makeUsersTuner(ResourceManager rm) //throws Exception
    {
        try {
            System.out.println("..... WL: makeUsersTuner...");
            Vector params = new Vector(200, 100);
//		String sql="select id, LOGINNAME, FIO from " + rm.getString("usr", true) + ".wu";
            String sql = "select id, LOGINNAME, FIO from wl.wu";
//      + " where last > CDate('11.01.2002')";
            IOUtil.writeLogLn(3, "===== getUsers (SQL):" + sql, rm);

            ResultSet r = dbUtilLogin.getResults(sql);
            String[] columns = DBUtil.getColNames(r);
            while (r.next()) {
                params.addElement("[" + r.getString(1) + "]");
                for (int i = 2; i <= columns.length; i++) {
                    String s = r.getString(i);
                    if (s != null && !s.equals("null")) {
                        params.addElement(columns[i - 1] + "=" + s);
                    }
//          params.addElement(columns[i-1] + "=" + StrUtil.unicode(s));
                }
                params.addElement("[end]");
            }
            dbUtilLogin.closeResultSet(r);
            String[] param = new String[params.size()];
            params.copyInto(param);
            IOUtil.writeLog(3, "===== getUsers (USERS):", param, rm);

            usersTuner = new Tuner(null, null, null, rm);
            usersTuner.addSection(params);

        } catch (Exception e) {
            e.printStackTrace(System.out);
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
            dbUtilLogin.close();
        } finally {
            super.finalize();
        }
      }

}
