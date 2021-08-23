package dubna.walt.service;

import java.sql.ResultSet;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 *
 * @author serg
 */
public class TableServiceComplex extends TableServiceSimple
{

    /**
     *
     * @param resultSet
     * @return
     * @throws Exception
     */
    protected int outTableBody(ResultSet resultSet) throws Exception
{
  int colNr;
  currentRow = -1;
  int numRows = 0;

  row.setAttr(tableTuner.getParameter("rowBgColor"));
  while (resultSet.next())
  {
    numRows++;
    row.setValue("");
    getRecord(resultSet);

    for (colNr = 0; colNr < numSqlColumns; colNr++)
    {
      cell.setAttr("");
      if (colLabels[colNr].indexOf("$CALL_SERVICE") >=0)
      {
        out.println(row.toHTML());
        row.setValue("");
        callSubService(colLabels[colNr] + record[colNr]);
      }
      else
      {
        cell.setValue(record[colNr]);
        cell.setFormatParams(numDigits, thsnDelimiter);
        row.addValue(cell);
      }
    }
    currentRow++;
    out.println(row.toHTML());
    if (currentRow % 4 == 1)
      row.setAttr(tableTuner.getParameter("altBgColorAttr"));
    else if (currentRow % 4 == 3)
      row.setAttr(tableTuner.getParameter("rowBgColor"));
  }
  return numRows;
}

    /**
     *
     * @param cmd
     * @throws Exception
     */
    public void callSubService(String cmd) throws Exception
{
  int i = cmd.indexOf("$CALL_SERVICE");
  if (i >= 0)
  {
//    System.out.println(cmd);
    StringTokenizer st = new StringTokenizer(cmd.substring(i + ("$CALL_SERVICE").length())," ");
    if (st.hasMoreTokens())
    {
      String cfg = st.nextToken();
      if (cfg.indexOf("#") >=0)
        cfg = cfgTuner.parseString(cfg);
      Vector params = new Vector(10);
      while (st.hasMoreTokens())
        params.addElement(st.nextToken());
      callService(cfg, params);
    }
  }
}
        
}