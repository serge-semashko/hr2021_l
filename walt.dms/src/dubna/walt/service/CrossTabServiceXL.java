package dubna.walt.service;

import java.sql.*;
import java.io.*;
import java.util.*;

import org.apache.poi.hssf.usermodel.*;
//import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import dubna.walt.util.*;

/**
 * @author serg
 */
public class CrossTabServiceXL extends TableServiceSimpleXL
{

    /**
     *
     */
    protected String[] crossValues = null;
String crossColName = "";
int numCrossValues = 0;
 
//  long timer = 0;
/**
 * the standard entry point of the service
 * 
 * @exception java.lang.Exception
 * @see dubna.walt.service.Service
 */
public void start() throws Exception
{
//  timer = System.currentTimeMillis() / 10;
  readXLTemplate();

  makeTable();

  setParameters();
 
  OutputStream outXL = (OutputStream) rm.getObject("outStream");
  wb.write(outXL);
  outXL.close();
}

    /**
     *
     * @throws Exception
     */
    protected void makeTable() throws Exception
{
  getCrossValues();
  
  ResultSet resultSet=runSQL(sqlSectionName);
  if (resultSet == null) return;
  metaData = resultSet.getMetaData();
  colNames = DBUtil.getColNames(resultSet);
  numTableColumns = colNames.length;
  cfgTuner.addParameter("numTableColumns", Integer.toString(numTableColumns + numCrossValues - 1));
  numDigitsForCols = new int[numTableColumns];
  
  outTableHeader(resultSet);

  outTableBody(resultSet);

  dbUtil.closeResultSet(resultSet);
}

    /**
     *
     * @throws Exception
     */
    protected void getCrossValues() throws Exception
{
  ResultSet resultSet=runSQL("SQL_Cross_Values");
  if (resultSet == null) return;
  metaData = resultSet.getMetaData();
  crossColName = DBUtil.getColNames(resultSet)[0];
  String tag = cfgTuner.getParameter(colTagsSectionName, crossColName);
  if (tag.length() > 0) crossColName = tag;
  Vector cv = new Vector(20,20);
  while (resultSet.next())
  { cv.addElement(resultSet.getString(1));
  }
  numCrossValues = cv.size();
  crossValues = new String[numCrossValues];
  cv.copyInto(crossValues);
  cfgTuner.addParameter("numCrossValues", Integer.toString(numCrossValues));
}

    /**
     *
     * @param resultSet
     */
    protected void outTableHeader(ResultSet resultSet)
{ 
  HSSFCellStyle style = XLUtil.makeStyle(wb, -1);
  int numDigits = cfgTuner.getIntParameter("numDigits");
  row = sheet.createRow(currRow++);
  
  cell = row.createCell((short) (numTableColumns-1));
  setCellValue(cell, crossColName, style);

  cell = row.createCell((short) (numTableColumns+numCrossValues-1));
  setCellValue(cell, "Âñåãî", style);

  row = sheet.createRow(currRow);
  
  for (int i = 0; i < numTableColumns-2; i++)
  { String tag = cfgTuner.getParameter(colTagsSectionName, colNames[i]);
    tag = (tag.equals(""))? colNames[i] : tag;

    int j = cfgTuner.getIntParameter("numDigitsForCols", colNames[i]);
    if (j >= -1)
      numDigitsForCols[i] = j;
    else
      numDigitsForCols[i] = numDigits;

    tag = StrUtil.replaceInString(tag,"&nbsp;"," ");
    tag = StrUtil.replaceInString(tag,"<br>"," ");
    cell = row.createCell((short) (i+1));
    setCellValue(cell, tag.trim(), style);
  }
  
  for (int i = 0; i < numCrossValues; i++)
  { cell = row.createCell((short) (i+numTableColumns - 1));
    setCellValue(cell, crossValues[i], style);
  }
}

    /**
     *
     * @param resultSet
     * @throws Exception
     */
    protected void outTableBody(ResultSet resultSet) throws Exception
{ String prevRowKey = "";
  String nextRowKey = "";
  String[] vals = new String[numTableColumns];
  int colNr;
  numTableRows = 0;

  while (resultSet.next())
  { nextRowKey = "";
    /* Get the next record and the row key */
    for (colNr = 0; colNr < numTableColumns; colNr++)
    { vals[colNr] = resultSet.getString(colNr + 1);
      if (colNr < numTableColumns -2) 
        nextRowKey += vals[colNr];
    }
    
    /* Start the new row */
    if (! nextRowKey.equals(prevRowKey))
    { /* Put "TOTAL" formula for the prev. row */
      if (prevRowKey.length() > 0)  //create TOTAL cell
      { cell = row.createCell((short) (numTableColumns + numCrossValues - 1));
        setCellValue(cell, "\"=SUM(RC[-"+numCrossValues+"]:RC[-1])", null);
      }
      prevRowKey = nextRowKey;
      nextRowKey = "";
      numTableRows++;
      row = sheet.createRow(++currRow); 
      for (colNr = 0; colNr < numTableColumns - 2; colNr++)
      { cell = row.createCell((short) (colNr + 1));
        setCellValue(cell, vals[colNr], null);
      }
    }
    
    /* Find the data position and put data */
    for (colNr = 0; colNr < numCrossValues; colNr++)
    { if ( vals[numTableColumns - 2].equals(crossValues[colNr]))
      { cell = row.createCell((short) (numTableColumns - 1 + colNr));
        try
        { double dv = Double.valueOf(vals[numTableColumns - 1]).doubleValue();
          setCellValue(cell, dv, 2);
        }
        catch (Exception e)
        { setCellValue(cell, vals[numTableColumns - 1], null);
          System.out.println(e.toString() + " : " + vals[numTableColumns - 1]);
        }
      }
    }
  }
  
  /* Put "TOTAL" formula for the last table row */
  if (prevRowKey.length() > 0)  //create TOTAL cell
  { cell = row.createCell((short) (numTableColumns + numCrossValues - 1));
    setCellValue(cell, "\"=SUM(RC[-"+numCrossValues+"]:RC[-1])", null);
  }

  /* Create "TOTALS" row and put formulas there*/
  row = sheet.createRow(++currRow); 
  cell = row.createCell((short) 1);
  setCellValue(cell, "ÂÑÅÃÎ:", null);
  for (colNr = 0; colNr <= numCrossValues; colNr++)
  { cell = row.createCell((short) (numTableColumns - 1 + colNr));
    setCellValue(cell, "\"=SUM(R[-"+numTableRows+"]C:R[-1]C)", null);
  }

  currRow++;
  cfgTuner.addParameter("numTableRows", Integer.toString(numTableRows));
}


}