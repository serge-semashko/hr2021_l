package dubna.walt.service;

//import dubna.walt.util.IOUtil;
import dubna.walt.util.IOUtil;
import java.sql.ResultSet;

/**
 *
 * @author serg
 */
public class TableServiceSpecial extends dubna.walt.service.TableServiceSimple {

    /**
     *
     * @param resultSet
     * @return
     * @throws Exception
     */
    @Override
    protected int outTableBody(ResultSet resultSet) throws Exception {
//	System.out.println("================= of=" + cfgTuner.getParameter("of") + "---" );
        if (terminated) {
            return 0;
        }
        int numItems = super.outTableBody(resultSet);
        if (terminated) {
            return 0;
        }
        setRowLinks(numItems);
        return numItems;
    }

    /**
     *
     * @throws Exception
     */
    @Override
    protected void processRecord() throws Exception {
        IOUtil.writeLogLn(5, "currentRow=" + currentRow + "; srn=" + srn + "; rpp=" + rpp, rm); 
        //IOUtil.writeLogLn(7, currentRow + "." + colNr + ": " + colNames[colNr] + "=" + record[colNr] + ";", rm); 
        if (currentRow > srn - 2 && currentRow < srn + rpp) {
            for (int colNr = 0; colNr < numSqlColumns; colNr++) {
//IOUtil.writeLogLn(7, currentRow + "." + colNr + ": " + colNames[colNr] + "=" + record[colNr] + ";", rm); 
                if (record[colNr] == null || record[colNr].length() < 1) {
                    record[colNr] = " ";
                }

                if (cfgTuner.getParameter("of").equals("xlh")) {
                    record[colNr] = record[colNr].replaceAll("<br>", "\n");
                }
                cfgTuner.addParameter(colNames[colNr], record[colNr]);
                if (record[colNr].indexOf("$CALL_SERVICE") != 0) {
                    cfgTuner.deleteParameter("subservice");
                    // ========= добавлено 20.09.2005, но не работает
                    try { //if (currentRow >= srn-2 && currentRow < srn+rpp-2)
                        if (totalRow != null && makeTotalsForCols.contains("," + colNames[colNr] + ",")) {
                            cell.setValue(record[colNr]);
                            colTotals[colNr] += cell.getDValue();
                            if (makeSubtotals) {
                                colSubtotals[colNr] += cell.getDValue();
                            }
                        }
                    } catch (Exception ex) {
                        /* we don't care, if the value is not numeric */ }
                    // ========================
                } else {
                    cfgTuner.addParameter("subservice", record[colNr].substring(14));
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
     * @param resultSet
     */
    @Override
    protected void outTableHeader(ResultSet resultSet) {
        if (cfgTuner.enabledOption("defHeader")) {
            super.outTableHeader(resultSet);
        } else {
            try {
                cfgTuner.outCustomSection("table header", out);
            } catch (Exception e) {
            }
        }
        headerRow = null;
    }

    /**
     *
     * @param numItems
     */
    protected void setRowLinks(int numItems) {
        if (numItems <= 0) {
            return;
        }
        String s = "";
        int ern_i;
        int crp_i = 1;
        if (srn > 1) {
            cfgTuner.addParameter("srn_i", Integer.toString(srn - rpp));
            s += cfgTuner.getParameter("prevLink");
        }
        for (int srn_i = 1; srn_i <= numItems; srn_i += rpp) {
            ern_i = srn_i + rpp - 1;
            if (ern_i > numItems) {
                ern_i = numItems;
            }
            if (srn_i == srn) {
                cfgTuner.addParameter("currentPage", "YES");
            } else {
                cfgTuner.deleteParameter("currentPage");
            }

            cfgTuner.addParameter("crp_i", Integer.toString(crp_i++));
            cfgTuner.addParameter("srn_i", Integer.toString(srn_i));
            cfgTuner.addParameter("ern_i", Integer.toString(ern_i));
            s += cfgTuner.getParameter("rowLink");
        }
        if (srn < numItems - rpp) {
            cfgTuner.addParameter("srn_i", Integer.toString(srn + rpp));
            s += cfgTuner.getParameter("nextLink");
        }

        cfgTuner.addParameter("rowLinks", s);
    }

}
