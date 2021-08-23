package dubna.walt.service;

//import java.sql.ResultSet;


/** Description of class
 *
 * @author kouniaev
 * @version 1.0
 */


public class TableServiceSortableSpecial extends dubna.walt.service.TableServiceSortable
{

    /**
     *
     * @throws Exception
     */
    @Override
    protected void processRecord() throws Exception
{ 
//  String rowStartTag = row.startTag;
//  String rowAttr = row.attr;
  cfgTuner.clearFlashParameters();

  for (int colNr = 0; colNr < numSqlColumns; colNr++)
  { cell.setFormatParams(numDigitsForCols[colNr], thsnDelimiter);
//    if (record[colNr].trim().length() == 0) record[colNr] = "&nbsp;";
    cell.setValue(record[colNr]);      
//    cfgTuner.setFlashParameter(colNames[colNr], cell.getValue()); //ÓÁÐÀÍÎ 13.08.2012 - $INCLUDE סבנאסûגאכא ןאנאלוענû
		cfgTuner.addParameter(colNames[colNr], cell.getValue());
    // Calculate subtotals
    try
    { if (cell.isNumeric() && totalRow != null && makeTotalsForCols.contains("," + colNames[colNr] + ",") )
      { 
//				cell.setValue(record[colNr]);
        colTotals[colNr] += cell.getDValue();
        if (makeSubtotals)
          colSubtotals[colNr] += cell.getDValue();
      }
    } catch (Exception ex) {
		ex.printStackTrace(System.out);
		/* we don't care, if the value is not numeric */ }
  }
  cfgTuner.outCustomSection("item", out);
}

}