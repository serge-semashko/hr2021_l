package dubna.walt.util;

import java.io.*;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;


/**
 * The XLUtil 
 */
public class XLUtil extends java.lang.Object 
{

//public static void fillXLTemplate(String templateFileName, String outFileName, Tuner cfgTuner)

    /**
     *
     * @param templateFileName
     * @param out
     * @param cfgTuner
     */
public static void fillXLTemplate(String templateFileName, OutputStream out, Tuner cfgTuner)
{
  try
  { POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(templateFileName));
    HSSFWorkbook wb = new HSSFWorkbook(fs);
    HSSFSheet sheet = wb.getSheetAt(0);
    Iterator rows = sheet.rowIterator();
    HSSFCell cell = null;
    while (rows.hasNext())
    {  HSSFRow row = (HSSFRow) rows.next();
       Iterator cells = row.cellIterator();
       while(cells.hasNext())
       { cell = (HSSFCell) cells.next();
         if(cell.getCellType() == HSSFCell.CELL_TYPE_STRING)
        { String s = cell.getStringCellValue();
          if (s.indexOf("#") >= 0)
          { s = cfgTuner.parseString(s);
            cell.setEncoding(HSSFWorkbook.ENCODING_UTF_16);
            cell.setCellValue(s);
          }
        }
       }
    }
    wb.write(out);
    // Write the output to a file
//    FileOutputStream fileOut = new FileOutputStream(outFileName);
//    wb.write(fileOut);
//    fileOut.close();
  }
  catch (Exception e)
  { e.printStackTrace(System.out);
  }
}

    /**
     *
     * @param wb
     * @param type
     * @return
     */
    public static HSSFCellStyle makeStyle(HSSFWorkbook wb, int type)
{
  HSSFCellStyle style = wb.createCellStyle();
  if (type==1)      // Table Header style
  { style.setBorderTop(HSSFCellStyle.BORDER_DOUBLE);
    style.setBorderBottom(HSSFCellStyle.BORDER_DOUBLE);
    style.setBorderLeft(HSSFCellStyle.BORDER_DOTTED);
    style.setBorderRight(HSSFCellStyle.BORDER_DOTTED);

    style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
    style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
    
    HSSFFont fontH = wb.createFont();
    fontH.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
    style.setFont(fontH);
    style.setWrapText(true);
  }
  else if (type == 0)
  { style.setDataFormat((short) 1);
  }
  else if (type == 2)
  { style.setDataFormat((short) 4);
  }
  return style;
}

}