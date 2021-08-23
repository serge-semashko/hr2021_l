package dubna.walt.service;

import java.sql.ResultSet;

/**
 *
 * @author serg
 */
public class TableServiceSpecial2
        extends TableServiceSpecial {

    /**
     *
     * @throws Exception
     */
    @Override
    protected void processRecord()
            throws Exception {
        if ((currentRow >= srn - 2) && (currentRow < srn + rpp)) {
            for (int colNr = 0; colNr < numSqlColumns; colNr++) {
                if ((record[colNr] == null) || (record[colNr].length() < 1)) {
                    record[colNr] = " ";
                }
                if (cfgTuner.getParameter("of").equals("xlh")) {
                    record[colNr] = record[colNr].replaceAll("<br>", "\n");
                }
                cell.setAttr("");
                cell.setFormatParams(numDigitsForCols[colNr], thsnDelimiter);
                cell.setValue(record[colNr]);
                if (record[colNr].indexOf("$CALL_SERVICE") != 0) {
                    cfgTuner.deleteParameter("subservice");
                    try {
                        if ((totalRow != null) && (makeTotalsForCols.contains("," + colNames[colNr] + ","))) {
                            colTotals[colNr] += cell.getDValue();
                            if (makeSubtotals) {
                                colSubtotals[colNr] += cell.getDValue();
                            }
                        }
                    } catch (Exception ex) {
                    }
                    cfgTuner.addParameter(colNames[colNr], cell.getValue());
                } else {
                    cfgTuner.addParameter("subservice", record[colNr].substring(14));
                    cfgTuner.addParameter(colNames[colNr], record[colNr]);
                    record[colNr] = "";
                }
            }
            if (terminated) {
                return;
            }
            cfgTuner.addParameter("oddRow", Integer.toString(currentRow % 2));
            cfgTuner.addParameter("currentRow", Integer.toString(currentRow + 1));
            cfgTuner.outCustomSection("item", out);
        }
    }

    /**
     *
     * @throws Exception
     */
    @Override
    protected void outColTotals()
            throws Exception {
        if (totalRow == null) {
            return;
        }
        outSubtotals();
        if (cfgTuner.enabledOption("hide_totals")) {
            return;
        }
        boolean flush = false;
        int colspan = 0;
        cell.setValue("");
        cell.setAttr("");
        for (int colNr = 0; colNr < numSqlColumns - numSpecialCols; colNr++) {
            if (makeTotalsForCols.contains("," + colNames[colNr] + ",")) {
                if (flush) {
                    if (colspan > 1) {
                        cell.addAttr("colspan=" + Integer.toString(colspan));
                    }
                    totalRow.addValue(cell);
                }
                cell.setAttr("class=total");
                cell.setFormatParams(numDigitsForCols[colNr], thsnDelimiter);
                cell.setValue(Double.toString(colTotals[colNr]));
                totalRow.addValue(cell);
                cell.setValue("");
                colspan = 0;
                flush = false;
            } else {
                if (colNr == 0) {
                    cell.setValue(totRowLabel);
                    cell.setAttr("class='total_label'");
                }
                colspan++;
                flush = true;
            }
        }
        if (flush) {
            if (colspan > 1) {
                cell.setAttr("colspan=" + Integer.toString(colspan));
            }
            totalRow.addValue(cell);
        }
        totalRow.setAttr(tableTuner.getParameter("totalBgColor"));
        out.println(totalRow.toHTML());
    }

    /**
     *
     * @param resultSet
     */
    @Override
    protected void outTableHeader(ResultSet resultSet) {
        super.outTableHeader(resultSet);
    }
}
