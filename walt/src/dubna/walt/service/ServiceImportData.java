package dubna.walt.service;

import java.sql.ResultSet;import dubna.walt.util.DBUtil;
import dubna.walt.util.IOUtil;
import java.sql.Connection;

/**
 *
 * @author serg
 */
public class ServiceImportData extends dubna.walt.service.Service
{

public void start() throws Exception
{
  cfgTuner.outCustomSection("report header",out);
	
  try
    { doCopy(getSQL("SQL"));
    }
    catch (Exception e)
    { IOUtil.writeLogLn("XXXXXXXX Exception: " + e.toString(), rm);
      cfgTuner.addParameter("ImportDataError", e.toString());
      cfgTuner.addParameter("ERROR", e.toString());
//      out.print(e.toString()); 
//      out.flush();  
    }
  cfgTuner.outCustomSection("[report footer]",out);
}

    /**
     *
     * @param sql
     * @throws Exception
     */
    public void doCopy(String sql) throws Exception
	{ if (sql == null || cfgTuner.enabledOption("NotConnected"))
			return;
	  DBUtil srcDBUtil = dbUtil;
	  System.out.println("doCopy: dbUtil=" + srcDBUtil);    
		String val = "";
		
		if (sql.toUpperCase().indexOf("SELECT_SP") == 0)
			sql = sql.substring(9);
		ResultSet r = srcDBUtil.getResults(sql);
	  cfgTuner.outCustomSection("start loop", out);
	  Connection conn = dbUtil.getConnection();
	  conn.setAutoCommit(false);
		if (r != null)
		{ String[] headers = DBUtil.getColNames(r);
			int nr = 0;
			while (r.next())
			{ for (int i = 0; i < headers.length; i++)
				{ val = r.getString(i+1);
					if (val != null && val.length() > 0 && !val.equalsIgnoreCase("NULL"))
					  cfgTuner.addParameter(headers[i], val);
					else
					  cfgTuner.addParameter(headers[i], "");
				}
				nr++;
			  cfgTuner.addParameter("REC_NR", Integer.toString(nr));
			  cfgTuner.addParameter("REC_100", Integer.toString(nr % 100));
			  cfgTuner.outCustomSection("record", out);
			}
//			r.close();
			srcDBUtil.closeResultSet(r);
		  srcDBUtil.close();
		  dbUtil.commit();
		}
	}

}