package dubna.walt.service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import dubna.walt.util.*;

/**
 *
 * @author serg
 */
public class ServiceList extends dubna.walt.service.Service

{

    /**
     *
     */
    public String sqlSectionName = "SQL";

    /**
     *
     */
    public String colTagsSectionName = "ColNames";

    /**
     *
     */
    protected String[] colNames = null;

    /**
     *
     */
    protected String[] record = null;

    /**
     *
     */
    protected String[] oldRecord = null;

    /**
     *
     */
    protected ResultSetMetaData metaData = null;

    /**
     *
     */
    protected int numColumns;
  
public void start() throws Exception
{
  cfgTuner.outCustomSection(headerSectionName,out);
  makeList();
  cfgTuner.outCustomSection(footerSectionName, out);
}

    /**
     *
     * @throws Exception
     */
    protected void makeList() throws Exception
{
  try
  { ResultSet resultSet=runSQL(sqlSectionName);
    if (resultSet == null) return;
    metaData = resultSet.getMetaData();
    colNames = DBUtil.getColNames(resultSet);
    numColumns = colNames.length;
    record = new String[numColumns];
    oldRecord = new String[numColumns];

    while (resultSet.next())
    {   getRecord(resultSet);
        processRecord();
    }
    dbUtil.closeResultSet(resultSet);
  }
  catch (Exception e)
  { out.println("<xmp>");
    e.printStackTrace(out);
    out.println("</xmp>");
    throw(e);
  }
}

    /**
     *
     * @throws Exception
     */
    protected void processRecord() throws Exception
{ int level = 0;
  boolean b = true;
  for (int colNr = 0; colNr < numColumns; colNr++)
  { cfgTuner.addParameter(colNames[colNr], record[colNr]);
    if (!record[colNr].equals(oldRecord[colNr]))
    { oldRecord[colNr] = record[colNr];
      if (b) 
      { b = false;
        level = colNr+1; 
      }
    }
  }
  if (level == 0) level = numColumns;
  cfgTuner.addParameter("level", Integer.toString(level));
  cfgTuner.outCustomSection("item", out);
}

/*
 *
 */

    /**
     *
     * @param r
     * @throws Exception
     */

protected void getRecord(ResultSet r) throws Exception
{ 
  for (int colNr = 0; colNr < numColumns; colNr++)
  { try
    { record[colNr] = r.getString(colNr+1);
      record[colNr] = (record[colNr] == null)? "" : record[colNr];
    }
    catch (Exception e) 
    { throw e;}
  }
}

/**
  * Create and run SQL query.
  *
     * @param sqlSectionName
  * @return <BR>
     * @throws java.lang.Exception
  */
protected ResultSet runSQL(String sqlSectionName) throws Exception
{ //if (!terminated) Thread.sleep(2000);
  ResultSet resultSet = dbUtil.getResults(getSQL(sqlSectionName));
//  if (!terminated) Thread.sleep(2000);
  IOUtil.writeLogLn(1, " <B> Processed in "+dbUtil.timeSpent+"ms. </B><BR>", rm);
  if (terminated) return null;
  return resultSet;
}

}

