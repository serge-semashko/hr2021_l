package dubna.walt.service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import dubna.walt.util.*;

/**
 *
 * @author serg
 */
public class ServiceTableCopir extends dubna.walt.service.Service

{

    /**
     *
     */
    public String sqlSectionName = "SrcSQL";

    /**
     *
     */
    public String sqlDestSectionName = "CopyToDestSQL";

    /**
     *
     */
    protected String[] colNames = null;

    /**
     *
     */
    protected int numColumns;
  
public void start() throws Exception
{
  cfgTuner.outCustomSection(headerSectionName,out);
  copyData();
  cfgTuner.outCustomSection(footerSectionName, out);
}

    /**
     *
     * @throws Exception
     */
    protected void copyData() throws Exception
{ try
  { ResultSet r=runScript(sqlSectionName);
    if (r == null) return;
    ResultSetMetaData metaData = r.getMetaData();
    colNames = DBUtil.getColNames(r);
    numColumns = colNames.length;

    while (r.next())
    { processRecord(r);
    }
    dbUtil.closeResultSet(r);
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
     * @param r
     * @throws Exception
     */
    protected void processRecord(ResultSet r) throws Exception
{ String val = "";
  for (int colNr = 0; colNr < numColumns; colNr++)
  { val = r.getString(colNr+1);
    val = (val == null)? "" : val;
    cfgTuner.addParameter(colNames[colNr], val);
  }
  getData (sqlDestSectionName);
}


/**
  * Run SQL all queries from a section.
  *
     * @param scriptSectionName
  * @return <BR>
     * @throws java.lang.Exception
  */
protected ResultSet runScript(String scriptSectionName) throws Exception
{ int sqlNr = 1;
  String nextSql = "";
  String sql=getSqlNr(cfgTuner, scriptSectionName, sqlNr);
  while (sql != null)
  { nextSql = getSqlNr(cfgTuner, scriptSectionName, ++sqlNr);
    if (nextSql == null)
    { return dbUtil.getResults(sql);
    }
    else
    { getPreData(sql);
    }
    sql = nextSql;
  }
  return null;
}


}

