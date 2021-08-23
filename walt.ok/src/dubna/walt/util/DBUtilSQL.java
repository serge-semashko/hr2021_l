package dubna.walt.util;

import java.sql.*;
import java.util.Hashtable;
//import java.util.Enumeration;

/**
 *
 * @author serg
 */
public class DBUtilSQL extends dubna.walt.util.DBUtil
{

/**
 *
     * @param connStr
     * @param usr
     * @param pw
     * @param queryLabel
     * @param numberInChain
     * @throws Exception
 */
public DBUtilSQL(String connStr,
              String usr,
              String pw,
              String queryLabel,
              int numberInChain) throws Exception
{
  this.connStr = connStr;
  this.usr = usr;  
  this.pw = pw;

//  this.queryLabel=queryLabel;
//  this.numberInChain = numberInChain;
  myName = this.toString().substring(15) + "(" + queryLabel + ")/" 
    + Integer.toString(numberInChain);
  connect();
}

    /**
     *
     * @throws Exception
     */
    public void connect() throws Exception
{
//  System.out.println("Connecting: '" + connStr + "' (" + usr + "/*****)" ); 
//  System.out.println(pw); 
  try
  { m_conn = DriverManager.getConnection(connStr, usr, pw);
  } catch (Exception e) 
  { System.out.println("*Not Connected: " + e.toString());
    throw e;
  }
//  System.out.println("*Connected: " + myName);

  m_conn.setAutoCommit(false);
  stmnts = new Hashtable();  
}


}