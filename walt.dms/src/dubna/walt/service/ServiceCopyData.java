package dubna.walt.service;

import java.sql.ResultSet;import dubna.walt.util.DBUtil;
import dubna.walt.util.IOUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
// import java.sql.ResultSet;
import java.util.StringTokenizer;

/**
 *
 * @author serg
 */
public class ServiceCopyData extends dubna.walt.service.Service
{
boolean	debugPrint=false;

public void start() throws Exception
{
  cfgTuner.outCustomSection("report header",out);
	out.flush();
	cfgTuner.outCustomSection("prepare data",out);
	debugPrint = cfgTuner.enabledOption("debugPrint");
  try
    { doCopy(getSQL("SQL"));
    }
    catch (Exception e)
    { e.printStackTrace(System.out);
			IOUtil.writeLogLn("XXXXXXXX Exception: " + e.toString(), rm);
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
	  System.out.println("doCopy: srcDBUtil=" + srcDBUtil);    
		
		if (sql.toUpperCase().indexOf("SELECT_SP") == 0)
			sql = sql.substring(9);
		ResultSet r = srcDBUtil.getResults(sql);
		
		if (r != null)
		{ cfgTuner.outCustomSection("start copy", out);
		  int numBatch = cfgTuner.getIntParameter("numBatch");
		  if (numBatch < 1) numBatch=100;
		  Connection conn = dbUtil.getConnection();
		  conn.setAutoCommit(false);
		  PreparedStatement stmt = conn.prepareStatement ( cfgTuner.getParameter("InsertSQL") );
		  System.out.println(cfgTuner.getParameter("InsertSQL") );
			
			String[] headers = DBUtil.getColNames(r);
		  String[] types = cfgTuner.getCustomSection("destTypes");
			int nr = 0;
			
			while (r.next())
			{ for (int i = 0; i < headers.length; i++)
				  copyValue(i+1, r, stmt, types[i]);
			  stmt.executeUpdate();
				if ((nr % 10)==0)
				  dbUtil.commit();
			  if ((nr % numBatch)==0)
					out.print(nr + " "); out.flush();
			  nr++;
			}
			
			srcDBUtil.closeResultSet(r);
		  srcDBUtil.close();
		  dbUtil.commit();
		  out.print("<br>" + nr + " records copied. "); out.flush();
		}
		else
		{
			throw (new Exception("COULD NOT READ SOURCE TABLE"));
		}
	}

    /**
     *
     * @param n
     * @param r
     * @param stmt
     * @param type
     * @throws Exception
     */
    public void copyValue(int n, ResultSet r, PreparedStatement stmt, String type) throws Exception
{ try
	{ StringTokenizer st = new StringTokenizer(type," (");
		String t = st.nextToken();
	  if (st.hasMoreTokens()) t = st.nextToken();
		if (debugPrint)
			System.out.println(n + ":" + t + ": '" + r.getString(n) + "'" );
			
		if (t.equalsIgnoreCase("varchar2")
		 || t.equalsIgnoreCase("char")	)
		{ // System.out.println(" str: '" + r.getString(n).trim() + "'");
			String v = r.getString(n);
			if (v == null)
			  stmt.setString(n, null);
			else
				stmt.setString(n, v.trim() );
		}
		else if (t.equalsIgnoreCase("date"))
		{ 
			stmt.setDate(n, r.getDate(n));
		}
		else if (t.equalsIgnoreCase("int"))
			stmt.setInt(n, r.getInt(n));
		else if (t.equalsIgnoreCase("number"))
			stmt.setFloat(n, r.getFloat(n));
	}
	catch (Exception e)
	{ e.printStackTrace(System.out);
		throw e;
	}
}

}