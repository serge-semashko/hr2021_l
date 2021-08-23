package dubna.walt.service;

import java.sql.ResultSet;
import java.util.Vector;

import dubna.walt.util.*;

/** Description of class
 *
 * @author kouniaev
 * @version 1.0
 */


public class CrossTabServiceTot extends TableServiceSimple
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
     * @throws Exception
     */

protected void makeTable() throws Exception
{
//  tableTuner = new Tuner(null, "common/table.cfg", rm.getString("CfgRootPath"), rm);
  if (cfgTuner.enabledOption("noTotals")) 
  { makeTotals = false;
    subtotalRow = null;
    makeTotalsForCols = "";
  }
  getCrossValues();

  ResultSet resultSet = runSQL(sqlSectionName);
  colNames = DBUtil.getColNames(resultSet);
  numSpecialCols = cfgTuner.getIntParameter("NumSpecialCols");
  if (numSpecialCols < 0) numSpecialCols = 0;
  numSqlColumns = colNames.length - numSpecialCols;
  crossColIndex = numSqlColumns - 2; // minus crossValue and value fields
  numTableColumns = numSqlColumns + numCrossValues - 1;

  initArrays();
  colTotals = new double[numCrossValues + 1];

  IOUtil.writeLogLn("numSqlColumns: " + Integer.toString(numSqlColumns)
      + "; crossColIndex: " + Integer.toString(crossColIndex)
      + "; numCrossValues: " + Integer.toString(numCrossValues), rm);

  outTag("wrapperTable");
  outTableHeader(resultSet);
  outTableBody(resultSet);
  dbUtil.closeResultSet(resultSet);

  if (makeTotals) outTableTotal(totRowLabel);
  
  outTableFooter();
  outTag("wrapperTableEnd");

//  cfgTuner.outCustomSection(footerSectionName, out);
}

/*
 *
 */

    /**
     *
     * @param resultSet
     * @return
     * @throws Exception
     */

protected int outTableBody(ResultSet resultSet) throws Exception
{
  currentColumn = 0;
  int numRows = 0;

  while (resultSet.next())
  { getRecord(resultSet);
    if (isNewRow()) startRow();

    if (!putValue())
    { startRow();
      putValue();
    }
    numRows++;
  }
  endRow();
  tableTuner.addParameter("NumTableRows", Integer.toString(numRows));
  cfgTuner.addParameter("NumTableRows", Integer.toString(numRows));
  return numRows;
}

/*
 *
 */

    /**
     *
     * @return
     */

protected boolean putValue()
{
  double d = 0.;
//  IOUtil.writeLogLn("=== currentColumn=" + currentColumn + "; numCrossValues: " + numCrossValues, rm);

  for (int i = currentColumn; i < numCrossValues; i++)
  { if (record[crossColIndex].equals(crossValues[i]))
    { cell.setValue(record[crossColIndex + 1]);
      if (cell.isNumeric())
      { d = cell.getDValue();
        collectColTotal (i, d);
        rowTotal = rowTotal + d;
        numRowValues++;
      }
      row.addValue(cell);
      cell.setAttr("");
      currentColumn = i + 1;
      return true;
    }
    else
      row.addValue("<td>&nbsp;</td>");
  }
  currentColumn = numCrossValues;
  return false;
}

    /**
     *
     * @param colIndex
     * @param val
     */
    protected void collectColTotal(int colIndex, double val)
{ if (colTotals == null) return;
  colTotals[colIndex] += val;
}

    /**
     *
     */
    protected void endRow()
{
  if (currentColumn == 0)
  { row.reset();
    return;
  }
  for (int colNr = currentColumn; colNr < numCrossValues; colNr++)
  { row.addValue("<td>&nbsp;</td>");
  }

  if (numRowValues > 0)
  { cell.setAttr(tableTuner.getParameter("totalBgColor"));
    cell.setValue("<b>--" + Double.toString(rowTotal) + "--</b>");
    row.addValue(cell);

    collectColTotal(numCrossValues, rowTotal);
    rowTotal = 0.;
  }
  out.println(row.toHTML());
  row.reset();
}

    /**
     *
     */
    protected void startRow()
{ endRow();

  cell.reset(numDigits, thsnDelimiter);
  row.setAttr(tableTuner.getParameter("rowBgColor"));

  for (int colNr = 0; colNr < crossColIndex; colNr++)
  { cell.setValue(record[colNr]);
    row.addValue(cell);
  }
  currentColumn = 0;
}

/*
 *
 */

    /**
     *
     * @param resultSet
     */

protected void outTableHeader(ResultSet resultSet)
{ outTag("table_beg");  // The <TABLE> tag

  row.setAttr(tableTuner.getParameter("headerBgColor"));

  /* The empty cell, spanned for crossColIndex columns */
  cell_h.setValue(".");
  cell_h.addAttr("colspan=" + Integer.toString(crossColIndex));
  row.addValue(cell_h);

  /* The name of the CrossValue field */
  cell_h.setValue(cfgTuner.getParameter(colTagsSectionName, colNames[crossColIndex]));
  cell_h.setAttr("colspan=" + Integer.toString(numCrossValues));
  row.addValue(cell_h);

  /* The "TOTAL" column header */
//  if (makeTotals) //16.09.2005 - здесь колонка "всего" почему-то есть всегда - независимо от makeTotals

  { cell_h.setValue(totColLabel);
    cell_h.setAttr("rowspan=2");
    row.addValue(cell_h);
  }

  cell_h.setAttr("");

  /* Output the 1st row */
  out.write(row.toHTML());
  row.setValue("");

  /* The names of the starting columns */
  for (int i = 0; i < crossColIndex; i++)
  {
    String tag = cfgTuner.getParameter(colTagsSectionName, colNames[i]);
    cell_h.setValue((tag.equals(""))? colNames[i] : tag);
    row.addValue(cell_h);
  }
  /* The Cross Values columns */
  for (int i = 0; i < numCrossValues; i++)
  {
    cell_h.setValue("'" + crossValues[i]);
    row.addValue(cell_h);
  }
  cell_h.reset(numDigits, thsnDelimiter);
  /* Output the 2nd row */
  out.write(row.toHTML());
}

/*
 *
 */

    /**
     *
     * @param rowName
     */

protected void outTableTotal(String rowName)
{ row.reset();
  row.setAttr(tableTuner.getParameter("totalBgColor"));
  cell.reset(numDigits, thsnDelimiter);
  cell.setValue("<b>" + rowName + "</b>");
  cell.setAttr("align=right colspan=" + Integer.toString(crossColIndex));
  row.addValue(cell);
  cell.setAttr("align=right");
  cell.setValue("-");

  for (int i = 0; i <= numCrossValues; i++)
  { if (colTotals != null)
      cell.setValue("<b>--" + StrUtil.formatDouble(colTotals[i], numDigits, thsnDelimiter) + "--</b>");
    row.addValue(cell);
  }
  out.println(row.toHTML());
}

/*
 *
 */

    /**
     *
     */

protected void outTableFooter()
{
  outTag("table_end");
}

/*
 *
 */

    /**
     *
     * @return
     */

protected boolean isNewRow()
{
  if (currentColumn >= numCrossValues) return true;
  for (int colNr = 0; colNr < crossColIndex; colNr++)
    if (!record[colNr].equals(oldRecord[colNr]))
      return true;
  return false;
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
{ for (int colNr = 0; colNr < numSqlColumns; colNr++)
  { oldRecord[colNr] = record[colNr];
    record[colNr] = r.getString(colNr + 1);
    record[colNr] = (record[colNr] == null)? "" : record[colNr];
//    IOUtil.writeLog(record[colNr] + "; ", rm);
  }
//  IOUtil.writeLogLn(";", rm);
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