package dubna.walt.service;

import java.sql.*;
import java.io.*;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import dubna.walt.util.*;

/**
 * @author serg
 */
public class TableServiceSimpleXL extends Service
{
  int currRow = 1;
  HSSFWorkbook wb = null;
  HSSFSheet sheet = null;
  HSSFRow row = null;
  HSSFCell cell = null;
  HSSFCellStyle styleN = null;
  HSSFCellStyle styleN2 = null;

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
    protected String makeTotalsForCols = null;

//  protected String[] record = null;

    /**
     *
     */
  protected ResultSetMetaData metaData = null;
  
    /**
     *
     */
    protected int[] numDigitsForCols = null;

    /**
     *
     */
    protected String[] colNames = null;
//  protected String[] colLabels = null;

    /**
     *
     */
  protected int numTableColumns = 0;
  int numTableRows = 0;
  
  long timer = 0;
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
  setParameters();

  makeTotalsForCols = "," + cfgTuner.getParameter("totalsFor") + ",";
  makeTable();
 
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
  ResultSet resultSet=runSQL(sqlSectionName);
  if (resultSet == null) return;
  metaData = resultSet.getMetaData();
  colNames = DBUtil.getColNames(resultSet);
  numTableColumns = colNames.length;
  numDigitsForCols = new int[numTableColumns];
  
  outTableHeader(resultSet);
//  System.out.println("=== TableServiceSimpleXL - point 1 - " + (System.currentTimeMillis() / 10 - timer) / 100. + " sec.");
  outTableBody(resultSet);
//  System.out.println("=== TableServiceSimpleXL - point 2 - " + (System.currentTimeMillis() / 10 - timer) / 100. + " sec.");
  dbUtil.closeResultSet(resultSet);
  outTableTotals();
}

    /**
     *
     */
    protected void outTableTotals()
{ if(makeTotalsForCols.length() < 3) return;
  row = sheet.createRow(currRow++);
  for (int colNr = 0; colNr < numTableColumns; colNr++)
  { cell = row.createCell((short) (colNr+1));
    if (makeTotalsForCols.indexOf("," + colNames[colNr] + ",") >=0 )
      setCellValue(cell, "\"=SUM(R[-" + numTableRows + "]C:R[-1]C)" , null);
    else if (colNr == 0) 
      setCellValue(cell, "Всего:", null);
  }
}

    /**
     *
     * @param resultSet
     */
    protected void outTableHeader(ResultSet resultSet)
{ 
  HSSFCellStyle style = XLUtil.makeStyle(wb, 1);
  int numDigits = cfgTuner.getIntParameter("numDigits");
  row = sheet.createRow(currRow);
  
  for (int i = 0; i < numTableColumns; i++)
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
}

    /**
     *
     * @param resultSet
     * @throws Exception
     */
    protected void outTableBody(ResultSet resultSet) throws Exception
{ int colNr;
  numTableRows = 0;
  while (resultSet.next())
  { numTableRows++;
    row = sheet.createRow(++currRow);
    for (colNr = 1; colNr <= numTableColumns; colNr++)
    {  cell = row.createCell((short) (colNr));
      if (metaData.getColumnType(colNr) == 12)
        setCellValue(cell, resultSet.getString(colNr), null);
      else
        setCellValue(cell, resultSet.getDouble(colNr), numDigitsForCols[colNr - 1]);
    }
  }
  dbUtil.closeResultSet(resultSet);
  currRow++;
  cfgTuner.addParameter("NumTableRows", Integer.toString(numTableRows));
}


/**
  * Create and run SQL query.
  *
     * @param sqlSectionName
  * @return <BR>
     * @throws java.lang.Exception
  */
protected ResultSet runSQL(String sqlSectionName) throws Exception
{ if (dbUtil == null) return null;
  ResultSet resultSet = dbUtil.getResults(getSQL(sqlSectionName));
  IOUtil.writeLogLn(1, " <B> Processed in "+dbUtil.timeSpent+"ms. </B><BR>", rm);
  return resultSet;
}

    /**
     *
     * @param cell
     * @param value
     * @param numDigits
     */
    protected void setCellValue(HSSFCell cell, double value, int numDigits)
{ cell.setCellValue(value);
  if (numDigits < 1)
  { if (styleN == null)  styleN = XLUtil.makeStyle(wb, 0);
    cell.setCellStyle(styleN);
  }
  else
  { if (styleN2 == null) styleN2 = XLUtil.makeStyle(wb, 2);
    cell.setCellStyle(styleN2); 
  }
}

    /**
     *
     * @param cell
     * @param value
     * @param style
     */
    protected void setCellValue(HSSFCell cell, String value, HSSFCellStyle style)
{ cell.setEncoding(HSSFWorkbook.ENCODING_UTF_16);
  cell.setCellValue(value);
  if (style != null) cell.setCellStyle(style);
}

    /**
     *
     * @throws Exception
     */
    protected void readXLTemplate() throws Exception
{
  String xlTemplateFileName = cfgTuner.getParameter("XLTemplateFileName");
  System.out.println(xlTemplateFileName);
  POIFSFileSystem fs = new POIFSFileSystem
    (new FileInputStream(rm.getString("CfgRootPath") + xlTemplateFileName));
  wb = new HSSFWorkbook(fs);
  sheet = wb.getSheetAt(0);
}

    /**
     *
     * @throws Exception
     */
    protected void setParameters() throws Exception
{ currRow = -1;
  Iterator rows = sheet.rowIterator();
    
  while (rows.hasNext())
  { currRow++;
    System.out.println("currRow:" + currRow);
    row = (HSSFRow) rows.next();
    Iterator cells = row.cellIterator();
    while(cells.hasNext())
    { cell = (HSSFCell) cells.next();
      if(cell.getCellType() == HSSFCell.CELL_TYPE_STRING)
      { String s = cell.getStringCellValue();
        if (currRow < 10)
        {
    System.out.println("val:" + s);
        }
        if (s.indexOf("#") >= 0)
        { 
    System.out.println("param:" + s);
          s = cfgTuner.parseString(s);
    System.out.println("res:" + s);
          cell.setEncoding(HSSFWorkbook.ENCODING_UTF_16);
          cell.setCellValue(s);
        }
      }
    }
  }
}

}