package dubna.walt.service;

import java.sql.ResultSet;
import java.util.Vector;
import dubna.walt.util.*;


/** Description of class
 *
 * @author kouniaev
 * @version 1.0
 */


public class CrossTab2SubtotService extends CrossTab2Service 
{
  
    /**
     *
     */
    protected double rowSubtotal=0.;

    /**
     *
     */
    protected String oldCrossValue = "";

    /**
     *
     */
    protected int numRowSubtotals = -1;

    /**
     *
     */
    protected int numCurrRowSubtotals = 0;

    /**
     *
     */
    protected int numCurrSectionCols = 0;

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

  cell_h.setAttr("");

  /* Output the 1st row */

  row.setValue("");

  cell_h.addAttr("rowspan=1 colspan=" + Integer.toString(crossColIndex));
  cell_h.setValue(" " );
  row.addValue(cell_h);

  /* The 1st Cross Values columns */
  String name1="";
  String prevName="";
  int numCols= 0;
  for (int i = 0; i < numCrossValues; i++)
  {
    name1 = crossValues[i].substring(0, crossValues[i].indexOf(CrossFieldsDelimiter));
    if (name1.equals(prevName) || i==0)
    {
      numCols++;
    }
    else
    {
      cell_h.setAttr("colspan=" + Integer.toString(numCols));
      cell_h.setValue("'" + prevName);
      row.addValue(cell_h);
      // if (numCols > 1)
      { cell_h.setAttr("class=rowSubtotal rowspan=2");
        cell_h.setValue("Всего<br>" + prevName);
        numRowSubtotals +=1;
        row.addValue(cell_h);
      }
      numCols = 1;
      
    }
    prevName = name1;
  }

  cell_h.setAttr("colspan=" + Integer.toString(numCols));
  cell_h.setValue("'" + prevName);
  row.addValue(cell_h);

      // if (numCols > 1)
      { cell_h.setAttr("class=rowSubtotal rowspan=2");
        cell_h.setValue("Всего<br>" + prevName);
        row.addValue(cell_h);
        numRowSubtotals +=1;
      }

  colTotals = new double[numCrossValues + numRowSubtotals + 2];

  cell_h.reset(numDigits, thsnDelimiter);

  /* The "TOTAL" column header */
  cell_h.setValue(totColLabel);
  cell_h.setAttr("rowspan=2");
  row.addValue(cell_h);
  
  out.write(row.toHTML());
  cell_h.setAttr("rowspan=1");

  /* Prepare the 2nd row */
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
  row.setValue("");
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
     * @return
     */

protected boolean putValue()
{ try {
  double d = 0.;
//  IOUtil.writeLogLn("=== currentColumn=" + currentColumn 
//    + "; numCrossValues: " + numCrossValues 
//    + "; crossColIndex=" + crossColIndex, rm);

  for (int i = currentColumn; i < numCrossValues; i++)
  { 
//  IOUtil.writeLogLn(i + ": " + record[crossColIndex] + "; crossValues[i]: " 
//  + crossValues[i]
//  + "; val:" + record[crossColIndex + 1], rm);
    if (isNewSection(crossValues[i]))
    {  
      collectColTotal (i + numCurrRowSubtotals, rowSubtotal);
      putRowSubtotal();
      numCurrSectionCols = 0;
    }
    else
      ++numCurrSectionCols;
    if (record[crossColIndex].equals(crossValues[i]))
    { 

      cell.setValue(record[crossColIndex + 1]);
      if (cell.isNumeric())
      { d = cell.getDValue();
        collectColTotal (i + numCurrRowSubtotals, d);
        rowTotal += d;
        rowSubtotal += d;
        numRowValues++;
      }
      row.addValue(cell);
      cell.setAttr("");
      currentColumn = i + 1;
      return true;
    }
    else
    {
      row.addValue("<td>&nbsp;</td>");
    }
  }
  currentColumn = numCrossValues;
}
  catch (Exception e)
  { e.printStackTrace(System.out);}
  return false;
}

    /**
     *
     * @param crossValue
     * @return
     */
    protected boolean isNewSection(String crossValue)
{ if (oldCrossValue.length() == 0)
  { oldCrossValue = crossValue;
    return false;
  }
  int i = crossValue.indexOf(CrossFieldsDelimiter);
  if (crossValue.substring(0,i).equals(oldCrossValue.substring(0,i)))
    return false;
  oldCrossValue = crossValue;
  return true;
}

    /**
     *
     */
    protected void endRow()
{ try 
  {
    if (currentColumn == 0)
    { row.reset();
      return;
    }
    for (int colNr = currentColumn 
                   + numCurrRowSubtotals;
                   colNr < numCrossValues + numRowSubtotals
                   ; colNr++)
    { 
      if (isNewSection(crossValues[colNr - numCurrRowSubtotals]))
      {  collectColTotal (colNr, rowSubtotal);
         putRowSubtotal();
      }
      else 
      { row.addValue("<td>&nbsp;</td>");
        ++numCurrSectionCols;
      }
    }
    collectColTotal (numCrossValues + numRowSubtotals, rowSubtotal);
    putRowSubtotal();

    if (numRowValues > 0)
    { cell.setAttr(tableTuner.getParameter("totalBgColor"));
      cell.setValue("<b>--" + Double.toString(rowTotal) + "--</b>");
      row.addValue(cell);

      collectColTotal(numCrossValues + numRowSubtotals + 1, rowTotal);
      rowTotal = 0.;
    }
    out.println(row.toHTML());
    row.reset();
    oldCrossValue = "";
    numCurrRowSubtotals = 0;
    numCurrSectionCols = 0;
  }
  catch (Exception e)
  { e.printStackTrace(System.out);}
}

    /**
     *
     */
    protected void putRowSubtotal()
{ // if (numCurrSectionCols > 1)
  { cell.setValue(Double.toString(rowSubtotal));
    cell.setAttr("class=rowSubtotal align=right");
    row.addValue(cell);
    numCurrRowSubtotals += 1;
  }
  rowSubtotal = 0.;
  cell.setAttr("");
}

    /**
     *
     * @param rowName
     */
    protected void outTableTotal(String rowName)
{ numCrossValues += numRowSubtotals + 1;
  super.outTableTotal(rowName);
}

}