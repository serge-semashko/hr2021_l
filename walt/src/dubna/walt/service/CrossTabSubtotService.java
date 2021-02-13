package dubna.walt.service;

import dubna.walt.QueryThread;
import dubna.walt.util.DBUtil;
import dubna.walt.util.IOUtil;
import dubna.walt.util.StrUtil;
import java.sql.ResultSet;


/** Description of class
 *
 * @author kouniaev
 * @version 1.0
 */


public class CrossTabSubtotService extends CrossTabService
{

    /**
     *
     */
    protected String[] crossValues = null;

    /**
     *
     */
    protected int numCrossValues;

    /**
     *
     */
    protected int crossColIndex;

    /**
     *
     */
    protected boolean makeTotals=true;

    /**
     *
     */
    protected int currentColumn = 0;



 /*
	*
	*/

    /**
     *
     * @param r
     * @throws Exception
     */

protected void getRecord(ResultSet r) throws Exception
 { int shift = 1;

	 /* collect and output subtotals row */
	 if (makeSubtotals) 
	 { shift = 2;
		 /* get the key value - the 1st field in the record */
		 String keyValue = cfgTuner.parseString(r.getString(1)); 
			 
		 /* the key value changed - out subtotals */
		 if (!keyValue.equals(oldKeyValue) 
 //      &&(currentRow >= srn-1 && currentRow <= srn+rpp)
			 )
		 {
			 if (colSubtotals != null)
				 outSubtotals();
			 else if (cfgTuner.enabledOption("makeTotalsForCols"))
				 colSubtotals = new double[numCrossValues];

			 /* out the new key value as a new row */ 
 //      row.setAttr(tableTuner.getParameter("altBgColorAttr"));
			 out.println("<tr><th " + tableTuner.getParameter("altBgColorAttr") 
				 + " align=left colspan=" + Integer.toString(numSqlColumns) 
				 + ">" + keyValue + "</th></tr>");
			 oldKeyValue = keyValue;
		 }
	 }
	 for (int colNr = 0; colNr < numSqlColumns; colNr++)
	 { try
		 { record[colNr] = r.getString(colNr + shift);
			 record[colNr] = (record[colNr] == null)? "" : record[colNr];
			 if (record[colNr].equalsIgnoreCase("ROWNUM")) 
				 record[colNr] = Integer.toString(currentRow+1);
			 if (initCapCols.length() > 2
				&& initCapCols.indexOf(Integer.toString(colNr)) > 0)
				 record[colNr] = StrUtil.initCap(record[colNr]);
				 
			 if (f_search.length() > 0)
				 record[colNr] = StrUtil.markSubstr(record[colNr], f_search, mark_before, mark_after);
 }
		 catch (Exception e)
		 { // record[colNr] = "_";
			 record[colNr] = e.toString();
			 ((QueryThread) rm.getObject("QueryThread")).logException(e);
		 }
 //    record[colNr] = (record[colNr] == null)? "" : StrUtil.unicode(record[colNr]);
	 }
 }



}