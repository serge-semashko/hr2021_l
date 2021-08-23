package dubna.walt.service;

import java.sql.ResultSet;

/** Description of class
 *
 * @author kouniaev
 * @version 1.0
 */


public class TableServiceSortable extends dubna.walt.service.TableServiceSimple
{

    /**
     *
     * @param resultSet
     */
    @Override
    protected void outTableHeader(ResultSet resultSet)
{ outTag("table_beg");
  headerRow.setAttr(tableTuner.getParameter("headerBgColor"));
//  String si = "";
  String sortCol;
  String sortCols = "," + cfgTuner.getParameter("sortCols") + ",";

  for (int i = 0; i < numSqlColumns- numSpecialCols; i++)
  {
//    si = Integer.toString(i + 1);
    String tag = cfgTuner.getParameter(colTagsSectionName, colNames[i]);
//    System.out.println("i=" + i + "; tag=" + tag + "; colName=" + colNames[i]);
    tag = (tag.equals(""))? colNames[i] : tag;
    colLabels[i] = tag;

    if (tag.contains("$CALL_SUBSERVICE"))
    { int j = tag.indexOf(" tag=");
      if (j > 0)
        tag = tag.substring(j+5);
      else
        tag = null;
    }
    
    if (tag.indexOf("$ATTR:") > 0)
    { int j = tag.indexOf("$ATTR:");
      cell_h.setAttr(tag.substring(j+6));
      tag = tag.substring(0, j-1);
    }
    
    if (!colLabels[i].contains("$CALL_SUBSERVICE")
      && (sortCols.length() < 3 || sortCols.contains(","+colNames[i]+",")))
    { sortCol = cfgTuner.getParameter("SortBy", colNames[i]);
      if (sortCol.length() == 0) 
      { if (makeSubtotals)
          sortCol = Integer.toString(i + 2);
        else
          sortCol = Integer.toString(i + 1);
      }   
      tag += "<br><input type=radio name=srt value='" + sortCol
        + "' onClick='setSort(\"" + sortCol + "\");' ";
      if (cfgTuner.enabledOption("srt=" + sortCol))
         tag +="CHECKED";
      tag += ">";
      if (cfgTuner.enabledOption("srt=" + sortCol))
        tag +=tableTuner.getParameter("sortArrow");
    }
//    colLabels[i] = tag;
    cell_h.setValue(tag);
    headerRow.addValue(cell_h);
  }
  if (!cfgTuner.enabledOption("hide_headers"))
    out.println(headerRow.toHTML());
  if (makeSubtotals)
  {
    headerRow = null;
//    super.outTableHeader(resultSet);
  }
}

}