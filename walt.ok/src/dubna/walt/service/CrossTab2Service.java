package dubna.walt.service;

import java.sql.ResultSet;
import java.util.Vector;


/** Description of class
 *
 * @author kouniaev
 * @version 1.0
 */


public class CrossTab2Service extends CrossTabService 
{
  
    /**
     *
     */
    public static String CrossFieldsDelimiter = "|/|";

/*
 *
 */

    /**
     *
     * @param resultSet
     */

protected void outTableHeader(ResultSet resultSet)
{
try
{
  outTag("table_beg");  // The <TABLE> tag

  row.setAttr(tableTuner.getParameter("headerBgColor"));

  /* The empty cell, spanned for crossColIndex columns */
  cell_h.addAttr("rowspan=2 colspan=" + Integer.toString(crossColIndex));
  row.addValue(cell_h);

  /* The name of the CrossValue field */
  cell_h.setValue(cfgTuner.getParameter(colTagsSectionName, colNames[crossColIndex]));
  cell_h.setAttr("colspan=" + Integer.toString(numCrossValues));
  row.addValue(cell_h);

  /* The "TOTAL" column header */
  cell_h.setValue("Всего");
  cell_h.setAttr("rowspan=3");
  row.addValue(cell_h);

  cell_h.setAttr("");

  /* Output the 1st row */
  out.write(row.toHTML());
  row.setValue("");

  /* The 1st Cross Values columns */
  String name1="";
  String prevName="";
  int numCols= 0;
  for (int i = 0; i < numCrossValues; i++)
  {
    name1 = crossValues[i].substring(0, crossValues[i].indexOf(CrossFieldsDelimiter));
    if (name1.equals(prevName) || i==0)
      numCols++;
    else
    {
      cell_h.setAttr("colspan=" + Integer.toString(numCols));
      cell_h.setValue("'" + prevName);
      row.addValue(cell_h);
      numCols = 1;
    }
    prevName = name1;
  }
  cell_h.setAttr("colspan=" + Integer.toString(numCols));
  cell_h.setValue("'" + prevName);
  row.addValue(cell_h);
  cell_h.reset(numDigits, thsnDelimiter);
  
  /* Output the 2nd row */
  out.write(row.toHTML());
  row.setValue("");

  /* The names of the starting columns */
  for (int i = 0; i < crossColIndex; i++)
  {
    String tag = cfgTuner.getParameter(colTagsSectionName, colNames[i]);
    cell_h.setValue((tag.equals(""))? colNames[i] : tag);
    row.addValue(cell_h);
  }
  
  /* The 2nd Cross Values columns */
  for (int i = 0; i < numCrossValues; i++)
  {
    cell_h.setValue("'" + crossValues[i].substring(crossValues[i].indexOf(CrossFieldsDelimiter) + CrossFieldsDelimiter.length()));
    row.addValue(cell_h);
  }
  cell_h.reset(numDigits, thsnDelimiter);
  /* Output the 2nd row */
  out.write(row.toHTML());
}
catch (Exception e)
{ e.printStackTrace(System.out);
  
}
}


/*
 *
 */

    /**
     *
     * @throws Exception
     */

public void getCrossValues() throws Exception
{
  ResultSet resultSet = runSQL("SQL_Cross_Values");
  Vector v = new Vector(20, 20);
  while (resultSet.next())
  {
    v.addElement(resultSet.getString(1));
  }
  dbUtil.closeResultSet(resultSet);
  crossValues = new String[v.size()];
  v.copyInto(crossValues);
  numCrossValues = crossValues.length;
}

}