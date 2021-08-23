package dubna.walt.service;

import dubna.walt.util.IOUtil;
import java.sql.*;

/**
 *
 * @author serg
 */
public class SQLExecService extends TableServiceSimple
{

public void start() throws Exception
{ 
  makeTableTuner();
  initFormatParams();
  makeTotalsForCols = ",,";
  makeSubtotals = false;
  
  cfgTuner.outCustomSection(headerSectionName,out);
  String sqlScript = cfgTuner.getParameter("SQL_TEXT");

  int i = 0;
  int n = 1;
  String[] sqa = sqlScript.split(";\r");
  cfgTuner.addParameter("NUM_QUERIES", Integer.toString(sqa.length));
    for(String sql: sqa) 
  {
      
    if(sql.length()>1 && sql.indexOf("--") != 0){
        sqlSectionName = sql;
        
        long tm = System.currentTimeMillis();
//        System.out.println(sqlSectionName);
        logSQL("SQL " + n , sqlSectionName, rm);
        cfgTuner.addParameter("SQL_NR", Integer.toString(n++));
        cfgTuner.addParameter("SQL_TEXT", sql);
        /* actually make the table */
        cfgTuner.outCustomSection("begin_results",out);
        initTableTagsObjects();
        try
        { 
            sqlSectionName = sqlSectionName.replace("\r", " ").trim();
//            rpp = 99999;
            srn = 1;
          makeTable();
          tm = System.currentTimeMillis() - tm;
         IOUtil.writeLogLn(3, "Processed in " + tm + "ms.", rm); 
        }
        catch (Exception e)
        { out.println("<p><xmp>");
          e.printStackTrace(out);
          out.println("</xmp><p>");
          sqlScript = "";
        }
    }
    cfgTuner.outCustomSection("end_results",out);
  }
  
  /* Output the report footer */
  cfgTuner.outCustomSection(footerSectionName, out);
}


protected ResultSet runSQL(String sqlSectionName) throws Exception
{ 
  ResultSet resultSet = null;
//  Thread.sleep(1000);
  try
  { if (sqlSectionName.toUpperCase().indexOf("SELECT ") == 0)
    { resultSet = dbUtil.getResults(sqlSectionName);
      try
       { resultSet.getMetaData().getColumnCount();
       }
       catch (Exception ex)
       { cfgTuner.addParameter("no_results", "y");
       }
      }
  else 
      dbUtil.update(sqlSectionName);

     cfgTuner.addParameter("timer", dbUtil.timeSpent+" ms.");
     cfgTuner.outCustomSection("finished", out);
//      dbUtil.cancelAllStatements();
  }
  catch (Exception e)
  { cfgTuner.addParameter("timer", dbUtil.timeSpent+" sec.");
    e.printStackTrace(System.out);
    String s = e.toString();
    int i = s.indexOf("SQL: ");
    if (i > 0) s = s.substring(0, i-1);
    i = s.indexOf(" ORA-");
    if (i > 0) s = s.substring(i+1);
    cfgTuner.addParameter("ERROR", s);
    cfgTuner.outCustomSection("err msg", out);
    Exception ex = new Exception (s);
//    ex.setStackTrace(e.getStackTrace());
    throw ex;
  }
  return resultSet;
}

}