package dubna.walt.service;

import java.sql.*;
import dubna.walt.util.*;

/** Description of class
 *
 * @author kouniaev
 * @version 1.0
 */


public class RecordEditorU extends Service
{

    /**
     *
     */
    protected String table;

    /**
     *
     */
    protected String sql;

    /**
     *
     */
    protected ResultSet r;

    /**
     *
     */
    protected String cop;

    /**
     *
     */
    protected String[] headers = null;

    /**
     *
     */
    protected ResultSetMetaData md = null;

public void start() throws Exception
{
  cfgTuner.outCustomSection("report header", out);

  cop = cfgTuner.getParameter("cop");
  table = cfgTuner.getParameter("table");
  r = dbUtil.getResults(getSQL("get SQL"));
  headers = DBUtil.getColNames(r);
  md = r.getMetaData();

  if (cop.equalsIgnoreCase("U"))  updateRecord();
  else if (cop.equalsIgnoreCase("D")) deleteRecord();
  else if (cop.equalsIgnoreCase("I")) insertRecord();
  else getRecord();

  dbUtil.closeResultSet(r);

  cfgTuner.outCustomSection("report footer", out);
}

    /**
     *
     * @throws Exception
     */
    protected void getRecord() throws Exception
{
  String val = "";
  int type = 0;
  int l = 10;
  String typeName = "";

  if (r.next())
    for (int i = 0 ; i < headers.length; i++)
    { 
      /* get the field value, type and size */
      val = r.getString(i+1);
      type = md.getColumnType(i + 1);
      typeName = md.getColumnTypeName(i + 1);
      l = md.getColumnDisplaySize(i+1);
      cfgTuner.addParameter("type", typeName + " (" + l + ")");

      IOUtil.writeLogLn(headers[i] + "='" + val 
        + "' (" + Integer.toString(type) + ": " +  typeName + ", " + Integer.toString(l)+ ")", rm);

    /* Calculate the size of the input field */
      if (cop.equalsIgnoreCase("new"))
      { cfgTuner.addParameter("size", Integer.toString(l));
      }
      else
      { if (val == null)
          val = "";
        else
          l = (l + val.length() + 1) / 2;
      }

      if (l > 50)
      { cfgTuner.addParameter("long","y");
        l = l / 50 + 1;
        if (l > 3) l = l - l / 3;
        if (l > 10) l = 10;
        cfgTuner.addParameter("numRows", Integer.toString(l));
      }
      else
      { cfgTuner.deleteParameter("long");
        if (!cop.equalsIgnoreCase("new"))
        cfgTuner.addParameter("size", Integer.toString((l/10+1) * 10));
      }

    /* Output the field */
      cfgTuner.addParameter("field", headers[i]);
      if (! (cop.equalsIgnoreCase("new")))
        cfgTuner.addParameter("value", val);
      cfgTuner.outCustomSection("field", out);
    }     
}

    /**
     *
     * @throws Exception
     */
    protected synchronized void updateRecord() throws Exception
{ String val = "";
  int type = 0;
  String typeName = "";
  
  String sql = "update " + table + " set ";
  String comm = "";
  String quot = "";
  
  if (r.next())
    for (int i = 0 ; i < headers.length; i++)
    {
      val = cfgTuner.getParameter(headers[i]);
      type = md.getColumnType(i + 1);
      typeName = md.getColumnTypeName(i + 1);
      if (type == 2 || type == 3) quot = ""; // number
      else quot = "'";
      if (val.length() == 0 
       &&(type == 2 || type == 3 || type == 93)) // number or date
      { val = "null";
        quot = "";
      }
      sql += comm +  headers[i] + "=" + quot + val + quot;
      
      IOUtil.writeLogLn(headers[i] + "='" + val 
        + "' (" + Integer.toString(type) + ": " +  typeName + ")" , rm);
      comm = ", ";
    }
  dbUtil.closeResultSet(r);
  sql += " where rowid='" + cfgTuner.getParameter("rowid") + "'";
  
  IOUtil.writeLogLn(sql , rm);
  dbUtil.update(sql);
  dbUtil.commit();
  Thread.sleep(100);
}

    /**
     *
     * @throws Exception
     */
    protected synchronized void insertRecord() throws Exception
{ String val = "";
  int type = 0;
  String typeName = "";
  
  String sql = "insert into " + table + " values (";
  String comm = "";
  String quot = "";
  
  if (r.next())
    for (int i = 0 ; i < headers.length; i++)
    { val = cfgTuner.getParameter(headers[i]);
      type = md.getColumnType(i + 1);
      typeName = md.getColumnTypeName(i + 1);
      
      if (type == 2 || type == 3) quot = ""; // number
      else quot = "'";
      if (val.length() == 0 
       &&(type == 2 || type == 3 || type == 93)) // number or date
      { val = "null";
        quot = "";
      }
      sql += comm + quot + val + quot;
      
      IOUtil.writeLogLn(headers[i] + "='" + val 
        + "' (" + Integer.toString(type) + ": " +  typeName + ")" , rm);
      comm = ", ";
    }
  dbUtil.closeResultSet(r);
  sql += ")";
  
  IOUtil.writeLogLn(sql , rm);
  dbUtil.update(sql);
  dbUtil.commit();
  Thread.sleep(100);

}

    /**
     *
     * @throws Exception
     */
    protected synchronized void deleteRecord() throws Exception
{
  dbUtil.update("delete from " + table 
    + " where rowid='" + cfgTuner.getParameter("rowid") + "'");
  dbUtil.commit();
  Thread.sleep(100);
}

public String getSQL(String sqlName) throws Exception
{
  String[] sql;
  IOUtil.writeLogLn(sqlName + ":", rm);
  sql = cfgTuner.getCustomSection(sqlName);
  IOUtil.writeLog(sqlName + ":", sql, rm);
  return StrUtil.strFromArray(sql);
//  return StrUtil.replaceInString(StrUtil.strFromArray(sql),"','",",");
}

    /**
     *
     * @throws Exception
     */
    public void validateInput() throws Exception
{
}

}