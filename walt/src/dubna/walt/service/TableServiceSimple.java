package dubna.walt.service;

import dubna.walt.QueryThread;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.io.FileNotFoundException;

import dubna.walt.util.*;
import java.sql.SQLException;

/**
 * This Service class produces a table-formatted output based on the records,
 * obtained by execution of the SQL from section [SQL] of the configuration
 * file.<p>
 *
 * <b>The sections of the configuration file:</b><ul>
 * <li><b>[SQL]</b> - the template for the SQL to be executed;<br>
 * <li><b>[ColNames]</b> - the headers of the columns to be displayed;<br>
 * </ul><p>
 *
 * <b>The parameters in the configuration file:</b><ul>
 * <li><b>tableCfg</b> - name of the config. file, containing the table
 * definition tags;<br>
 * Value "this" means to use the current config file. The default value is
 * "common/table.cfg". If this file does not exists - the current config. file
 * will be used.
 * <li><b>numDigits</b> - number of decimal digits after dot to be displayed for
 * numeric cells;<br>
 * <li><b>thsnDelimiter</b> - a character to be used to separate thousends in
 * numeric fields;<br>
 * </ul>
 *
 * <b>Parameters in the "tableCfg":</b><ul>
 * <li><b>table_beg</b> - the start table tag. Default: "&lt;table border=0
 * cellspacing=1 cellpadding=3>";
 * <li><b>table_end</b> - the end table tag. Default: "&lt;/table>");<br>
 * <li><b>headerBgColor</b> - the "bgcolor"-tag for the table header. Default:
 * "bgcolor=F0F4F4";<br>
 * <li><b>rowBgColor</b> - the "bgcolor"-tag for the table rows. Default:
 * "bgcolor=FFFFFF";<br>
 * <li><b>altBgColorAttr</b>- the "bgcolor"- alternative tag for the table rows.
 * Default: "bgcolor=EEFFFF";<br>
 * <li><b>tr_beg</b> - the "<tr> tag. Default: "&lt;tr ";<br>
 * <li><b>th_beg</b> - the "<th> tag. Default: "&lt;th ";<br>
 * <li><b>td_beg</b> - the "<td> tag. Default: "&lt;td ";<br>
 * <li><b>negativeValueAttr</b> - the attribute which should be applied for
 * negative numeric value. F.ex: "&lt;font color=FF0000>". Default: empty.
 * <li><b>wrapperTable</b> - wrapper table tags which should be output before
 * the report table. Default: empty.<br>
 * <li><b>wrapperTableEnd</b> - wrapper table tags which should be output after
 * the report table. Default: empty.<p>
 * </ul>
 *
 * <b>So, TableServiceSimple outputs tags in the following order:</b><ul>
 * <li>wrapperTable
 * <li>table_beg
 * <li>tr_beg + headerBgColor + ">"
 * <li>th_beg [+ attribute] + ">" {the header} &lt;/th> (for all table
 * columns)<br>
 * <li>&lt;/tr> tr_beg + rowBgColor + ">"
 * <li>td_beg [+ attributes] + ">" {cell value} (for all table columns)<br>
 * <li>&lt;/td>&lt;/tr> {for all table rows}
 * <li>table_end
 * <li>wrapperTableEnd
 * </ul>
 * <p>
 * <b>Example of the configuration file:</b>
 *
 * <p>
 * <b>Example of the tableCfg file:</b>
 *
 * <p>
 * <b>The output, produced by this example:</b>
 * <p>
 * <b>Special features:</b><ul>
 * <li>Header line may contain "$ATTR:attribute" directive after the header
 * name. In this case the "attribute" will be added to the "&lt;th " tags for
 * this and for the following headers until the last header or until "$ATTR:X" -
 * directive.<br>
 * <li>Header line may contain "$CALL_SERVICE" directive. That will cause a
 * sub-service call for each data cell in the corresponding column.
 * </ul>
 * <p>
 * @author Serguei Kouniaev
 */
public class TableServiceSimple extends dubna.walt.service.Service {

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
    public int numDigits = -1;

    /**
     *
     */
    protected int[] numDigitsForCols = null;

    /**
     *
     */
    public String thsnDelimiter = "&nbsp;";

    /**
     *
     */
    public int numRowsInGroup = 1;

    /**
     *
     */
    protected String[] colNames = null;

    /**
     *
     */
    protected String[] colLabels = null;

    /**
     *
     */
    protected String[] record = null;

    /**
     *
     */
    protected String[] oldRecord = null;

    /**
     *
     */
    protected String oldKeyValue = "";

    /**
     *
     */
    protected double[] colTotals = null;

    /**
     *
     */
    protected double[] colSubtotals = null;

    /**
     *
     */
    protected String makeTotalsForCols = null;

    /**
     *
     */
    protected String initCapCols = null;

    /**
     *
     */
    protected boolean makeSubtotals = false;

    /**
     *
     */
    protected double rowTotal = 0.;

    /**
     *
     */
    protected int numRowValues = 0;

    /**
     *
     */
    protected String totRowLabel = "Всего:";

    /**
     *
     */
    protected String totColLabel = "Всего:";

    /**
     *
     */
    protected boolean unicodeHeaders = false;

    /**
     *
     */
    protected int numSqlColumns = 0;

    /**
     *
     */
    protected int numTableColumns = 0;

    /**
     *
     */
    protected int numSpecialCols = 0;

    /**
     *
     */
    protected Tuner tableTuner;

    /**
     *
     */
    protected HTMLTag headerRow = null;

    /**
     *
     */
    protected HTMLTag row = null;

    /**
     *
     */
    protected HTMLTag totalRow = null;

    /**
     *
     */
    protected HTMLTag subtotalRow = null;

    /**
     *
     */
    protected HTMLTag cell_h = null;

    /**
     *
     */
    protected HTMLTag cell = null;

    /**
     *
     */
    protected long timer;

    /**
     *
     */
    protected ResultSetMetaData metaData = null;

    /**
     *
     */
    protected String f_search = "";

    /**
     *
     */
    protected String mark_before = "";

    /**
     *
     */
    protected String mark_after = "";

    /**
     *
     */
    protected int currentRow = 0;

    /**
     *
     */
    protected int srn = -1;

    /**
     *
     */
    protected int rpp = -1;

    /**
     * the standard entry point of the service
     *
     * @exception java.lang.Exception
     * @see dubna.walt.service.Service
     */
    @Override
    public void start() throws Exception {
        cfgTuner.outCustomSection(headerSectionName, out);

//  System.out.println("=== TableServiceSimple - Start - " + (System.currentTimeMillis() / 10 - timer) / 100. + " sec.");
        makeTableTuner();
        initFormatParams();
        makeTotalsForCols = "," + cfgTuner.getParameter("makeTotalsForCols") + ",";
        makeSubtotals = cfgTuner.getParameter("makeSubtotals").equals("y");
        unicodeHeaders = cfgTuner.getParameter("unicodeHeaders").equals("y");
        initTableTagsObjects();

//  logParameters();
        /* actually make the table */
        if (!cfgTuner.enabledOption("NotConnected")) {
            makeTable();
        }

        /* Output the report footer */
        cfgTuner.outCustomSection(footerSectionName, out);
    }

    /**
     * Set formatting parameters for numbers
     */
    protected void initFormatParams() {
        try {
            int nd = cfgTuner.getIntParameter("numDigits");
            if (nd >= 0) {
                numDigits = nd;
            }
        } catch (Exception e) {
            /* don't care - keep the default value */        }
        String s = cfgTuner.getParameter("thsnDelimiter");
        if (s.length() == 0) {
            s = tableTuner.getParameter("thsnDelimiter");
        }
        if (s.length() > 0) {
            thsnDelimiter = s;
        }
        if (s.equalsIgnoreCase("N")) {
            thsnDelimiter = "";
        }
        if (s.equalsIgnoreCase("S")) {
            thsnDelimiter = " ";
        }

        f_search = cfgTuner.getParameter("f_search");
        mark_before = cfgTuner.getParameter("mark_before");
        mark_after = cfgTuner.getParameter("mark_after");
        initCapCols = "," + cfgTuner.getParameter("initCapCols") + ",";
    }

    /**
     * Creates a Tuner object, containing some table tags
     *
     * @exception java.lang.Exception
     */
    protected void makeTableTuner() throws Exception {
        /*===== Make the table tags definition Tuner =====*/
        String tableCfg = cfgTuner.getParameter("tableCfg");

        if (tableCfg.length() == 0) // TableCfg not specified - try to find some default file 
        {
            /* look for table.cfg first */
            if ((cfgTuner.getParameter("table.cfg", "parameters", "tr_beg")).length() > 0
                    || (cfgTuner.getParameter("table.cfg", "parameters", "table_beg")).length() > 0) {
                tableCfg = "table.cfg";
            } /* look for common/table.cfg finally */ else if ((cfgTuner.getParameter("common/table.cfg", "parameters", "tr_beg")).length() > 0
                    || (cfgTuner.getParameter("common/table.cfg", "parameters", "table_beg")).length() > 0) {
                tableCfg = "common/table.cfg";
            }
//    if (!(new File(rm.getString("CfgRootPath") + tableCfg)).exists())
//      tableCfg = "common/table.cfg";
        } else if (tableCfg.equalsIgnoreCase("this")) {
            tableCfg = cfgTuner.getParameter("c");
        }
        tableCfg = cfgTuner.getModFileName(tableCfg, "SIMPLE");
        if (!tableCfg.contains(".")) {
            tableCfg += ".cfg";
        }

        try {
            tableTuner = new Tuner(cfgTuner.getParameters(), tableCfg, rm.getString("CfgRootPath", false), rm);
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                tableCfg = cfgTuner.getParameter("c");
                if (!tableCfg.contains(".")) {
                    tableCfg += ".cfg";
                }
//      System.out.println("***** tableCfg = " + tableCfg);
                tableTuner = new Tuner(cfgTuner.getParameters(), tableCfg, rm.getString("CfgRootPath", false), rm);
            } else {
                throw e;
            }
        }
        cfgTuner.addParameter("tableCfgUsed", tableCfg);

        /*===== Set some default table tags =====*/
        setTableTag("wrapperTable", "NONE");
        setTableTag("wrapperTableEnd", "NONE");
        setTableTag("table_beg", "<table border=0 cellspacing=1 cellpadding=3>");
        setTableTag("table_end", "</table>");
        setTableTag("headerBgColor", "bgcolor=F0F4F4");
        setTableTag("totalBgColor", "bgcolor=F0F4F4");
        setTableTag("altBgColorAttr", "bgcolor=EEFFFF");
        setTableTag("rowBgColor", "bgcolor=white");
        setTableTag("numRowsInGroup", "1");
        numRowsInGroup = tableTuner.getIntParameter("numRowsInGroup");

        tableTuner.deleteEmptyParameters();
    }

    /**
     *
     * @throws Exception
     */
    protected void initTableTagsObjects() throws Exception {
        /* Init "<tr>" object */
        String s = setTableTag("tr_beg", "<tr ");
        row = new HTMLTag(s, "</tr>", tableTuner);

        headerRow = new HTMLTag(s, "</tr>", tableTuner);

        /* Init totalRow "<tr>" object */
        if (makeTotalsForCols.length() > 2) {
            totalRow = new HTMLTag(s, "</tr>", tableTuner);
        }

        /* Init subtotalRow "<tr>" object */
        if (makeSubtotals) {
            subtotalRow = new HTMLTag(s, "</tr>", tableTuner);
        }

        /* Init "<th>" object */
        s = setTableTag("th_beg", "<th ");
        cell_h = new HTMLTag(s, "</th>", tableTuner);

        /* Init "<td>" object */
        s = setTableTag("td_beg", "<td ");
        cell = new HTMLTag(s, "</td>", tableTuner);
        cell.setFormatParams(numDigits, thsnDelimiter);

        totRowLabel = cfgTuner.getParameter("totRowLabel");
        if (totRowLabel.length() < 2) {
            totRowLabel = "Всего:";
        }
//  if (totRowLabel.length() < 2) totRowLabel = "Total";
        totColLabel = cfgTuner.getParameter("totColLabel");
        if (totColLabel.length() < 2) {
            totColLabel = "Всего";
        }
    }

    /**
     *
     * @param tagName
     * @param defValue
     * @return
     */
    protected String setTableTag(String tagName, String defValue) {
        String v = cfgTuner.getParameter(tagName);
        if (v.length() == 0) {
            v = tableTuner.getParameter(tagName);
        }
        if (v.length() == 0) {
            v = defValue;
        }
        if (v.equalsIgnoreCase("NONE")) {
            v = "";
        }
        tableTuner.addParameter(tagName, v);
        return v;
    }

    /**
     *
     * @throws Exception
     */
    protected void makeTable() throws Exception {
//  timer = System.currentTimeMillis() / 10;
//  System.out.println("=== TableServiceSimple - point 1 - " + (System.currentTimeMillis() / 10 - timer) / 100. + " sec.");
        
        srn = cfgTuner.getIntParameter("srn");
        rpp = cfgTuner.getIntParameter("rpp");
        
        System.out.println( "!!!!!!!!!!!!!getParameters() "+cfgTuner.getParameter("rpp")+" "+cfgTuner.getIntParameter("rpp"));
        if (srn <= 0 || rpp <= 0) {
            rpp = 99999;
            srn = 1;
        }

        ResultSet resultSet = runSQL(sqlSectionName);
        if (resultSet == null) {
            return;
        }

        metaData = resultSet.getMetaData();

        colNames = DBUtil.getColNames(resultSet);
        if (unicodeHeaders) {
            for (int i = 0; i < colNames.length; i++) {
                byte[] b = colNames[i].getBytes();
                for (int j = 0; j < b.length; j++) {
                    if (b[j] == -48 && b[j + 1] == 63) {
                        b[j] = (byte) 208;
                        b[j + 1] = (byte) 152;
                    }
                }
                colNames[i] = new String(b, "utf8");
            }
        }
        if (makeSubtotals) {
            for (int i = 0; i < colNames.length - 1; i++) {
                colNames[i] = colNames[i + 1];
            }
        }
        colLabels = new String[colNames.length];
        for (int i = 0; i < colNames.length; i++) {
            colLabels[i] = "";
        }
        numSqlColumns = colNames.length;
        if (makeSubtotals) {
            numSqlColumns--;
        }
        numSpecialCols = cfgTuner.getIntParameter("NumSpecialCols");
        if (numSpecialCols < 0) {
            numSpecialCols = 0;
        }
        //  numSqlColumns -= numSpecialCols;

        outTag("wrapperTable");
        outTag("table_beg");
        outTableHeader(resultSet);

        initArrays();

//  System.out.println("=== TableServiceSimple - point 2 - " + (System.currentTimeMillis() / 10 - timer) / 100. + " sec.");
        outTableBody(resultSet);
        dbUtil.closeResultSet(resultSet);
//  System.out.println("=== TableServiceSimple - point 3");
        outTag("table_end");
        outTag("wrapperTableEnd");
    }

    /**
     *
     * @param resultSet
     * @return
     * @throws Exception
     */
    protected int outTableBody(ResultSet resultSet) throws Exception {
//  System.out.println("*** srn=" + srn + "; rpp=" + rpp);

//        int colNr;
        currentRow = 0;
        int numRows = 0;
        if (srn < 0) {
            srn = 0;
        }
        if (rpp < 1) {
            rpp = 9999;
        }
        row.setAttr(tableTuner.getParameter("rowBgColor"));
        long tm = System.currentTimeMillis();
        int minRow = srn - rpp * 5;
        if (minRow < 1) {
            minRow = 1;
        }
        int maxRow = minRow + rpp * 10 - 1;
//        if (!cfgTuner.enabledOption("nextSetLink")) {
//            minRow = 1;
//            maxRow = rpp;
//        }
        IOUtil.writeLogLn(5, "outTableBody: srn=" + srn + "; rpp=" + rpp + "; minRow=" + minRow + "; maxRow=" + maxRow, rm); 
        if (rpp < 101) {
            resultSet.setFetchSize(rpp);
        }
        while (resultSet.next() && numRows < maxRow) {
            numRows++;
            if (currentRow >= srn - 1 && currentRow < srn + rpp - 1) {
                row.setValue("");
                getRecord(resultSet);
                processRecord();
//  System.out.println("numRows=" + numRows + "; currentRow=" + currentRow);      
                row.setAttr(tableTuner.getParameter("rowBgColor"));
                if (!makeSubtotals && numRowsInGroup > 0) {
                    if (currentRow % (numRowsInGroup * 2) == (numRowsInGroup - 1)) {
                        row.setAttr(tableTuner.getParameter("altBgColorAttr"));
                    } else if (currentRow % (numRowsInGroup * 2) == (numRowsInGroup * 2 - 1)) {
                        row.setAttr(tableTuner.getParameter("rowBgColor"));
                    }
                }
            }
            currentRow++;
        }
//  System.out.println("numRows="+numRows+"; Time spent: " + Long.toString(System.currentTimeMillis() - tm));

        outColTotals();
        tableTuner.addParameter("NumTableRows", Integer.toString(numRows));
        cfgTuner.addParameter("NumTableRows", Integer.toString(numRows));
        if (cfgTuner.enabledExpression("!rowLinks&srn&rpp")) {
            setRowLinks(numRows, minRow, maxRow);
        }
        IOUtil.writeLogLn(1, " <b> NumTableRows=" + numRows + "; </b>", rm);

        return numRows;
    }

    /**
     *
     * @throws Exception
     */
    protected void processRecord() throws Exception {
        String rowStartTag = row.startTag;
        String rowAttr = row.attr;

        boolean subservice = false;
//	System.out.println("numSqlColumns:" + numSqlColumns + "; colLabels.length():" + colLabels.length);
        for (int colNr = 0; colNr < numSqlColumns; colNr++) {
            cell.setAttr("");
            cell.setValue("");
//		System.out.println(colNr + ": '" + colLabels[colNr] + "'");
            if (colLabels[colNr].contains("$CALL_SUBSERVICE")) {
                String et = row.endTag;
                row.endTag = "";
                out.println(row.toHTML());
                row.setValue("");
                subservice = true;
                String cmd = colLabels[colNr] + record[colNr];
                int i = cmd.indexOf("$CALL_SUBSERVICE");
//      System.out.println(cmd);
                callService(cmd.substring(i + ("$CALL_SUBSERVICE").length()).trim());
                row.endTag = et;
                row.startTag = "";
                rowAttr = row.attr;
                row.setAttr("");
            } else {
                /*      int realColNr = colNr;
      if (makeSubtotals) realColNr++;
      if (metaData.getColumnType(realColNr + 1) == 12)  //******* REMOVED 21.03.03
      { cell.setTextValue(record[colNr]);
      }
                 */
                cell.setFormatParams(numDigitsForCols[colNr], thsnDelimiter);
                cell.setValue(record[colNr]);
                row.addValue(cell);
                subservice = false;
                try { //if (currentRow >= srn-2 && currentRow < srn+rpp-2)
                    if (totalRow != null && makeTotalsForCols.contains("," + colNames[colNr] + ",")) {
                        colTotals[colNr] += cell.getDValue();
                        if (makeSubtotals) {
                            colSubtotals[colNr] += cell.getDValue();
                        }
                    }
                } catch (Exception ex) {
                    /* we don't care, if the value is not numeric */ }
            }
        }
        if (!subservice) // && currentRow >= srn-2 && currentRow < srn+rpp-2)
        {
            out.println(row.toHTML());
        }
        row.startTag = rowStartTag;
        row.setAttr(rowAttr);

    }

    /**
     *
     * @throws Exception
     */
    protected void outSubtotals() throws Exception {
        /**/
        if (subtotalRow == null
                || colSubtotals == null) {
            return;
        }

        boolean flush = false;
        int colspan = 0;

        cell.setValue("");

        String subtotRowLabel = cfgTuner.getParameter("subtotRowLabel");
        if (subtotRowLabel.length() < 2) {
            subtotRowLabel = "Итого:";
        }

        cell.setAttr("");

        for (int colNr = 0; colNr < numSqlColumns; colNr++) {
            if (makeTotalsForCols.contains("," + colNames[colNr] + ",")) // Get subtotal data 
            {
                if (flush) // Put the previous cell into the row 
                {
                    if (colspan > 1) {
                        cell.addAttr("colspan=" + Integer.toString(colspan));
                    }
                    subtotalRow.addValue(cell);
                    cell.setValue("");
                }
                // Put the new subtotal value 
                cell.setAttr("");
                cell.setValue(Double.toString(colSubtotals[colNr]));
                colSubtotals[colNr] = 0.;
                subtotalRow.addValue(cell);
                cell.setValue("");
                colspan = 0;
                flush = false;
            } else {
                if (colNr == 0) // Make the 1st cell in the subtotal row 
                {
                    cell.setValue("<b>" + subtotRowLabel + "</b>&nbsp;");
                    cell.setAttr("align=right");
                }
                colspan++;
                flush = true;
            }
//    if (colspan > 1)
            //     cell.addAttr("colspan=" + Integer.toString(colspan));
        }

        // Put the tailing cell
        if (flush) {
            if (colspan > 1) {
                cell.setAttr("colspan=" + Integer.toString(colspan));
            }
            subtotalRow.addValue(cell);
        }

        // Output the subtotal row and reset it 
        subtotalRow.setAttr(tableTuner.getParameter("altBgColorAttr"));
        out.println(subtotalRow.toHTML());
        subtotalRow.setValue("");
        out.println("<tr bgcolor=white><td colspan=" + Integer.toString(numSqlColumns)
                + ">&nbsp;</td></tr>");
        /**/
    }

    /**
     *
     * @throws Exception
     */
    protected void outColTotals() throws Exception {
        /**/
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
            if (makeTotalsForCols.contains("," + colNames[colNr] + ",")) // Get total data 
            {
                if (flush) // Put the previous cell into the row 
                {
                    if (colspan > 1) {
                        cell.addAttr("colspan=" + Integer.toString(colspan));
                    }
                    totalRow.addValue(cell);
                }
                // Put the new subtotal value 
                cell.setAttr("class=total");
                cell.setFormatParams(numDigitsForCols[colNr], thsnDelimiter);
                cell.setValue(Double.toString(colTotals[colNr]));
                totalRow.addValue(cell);
                cell.setValue("");
                colspan = 0;
                flush = false;
            } else {
                if (colNr == 0) // Make the 1st cell in the subtotal row 
                {
                    cell.setValue("<b><i>" + totRowLabel + ":</i></b>&nbsp;");
                    cell.setAttr("align=right");
                }

                colspan++;
                flush = true;
            }
        }

        if (flush) // Put the tailing cell
        {
            if (colspan > 1) {
                cell.setAttr("colspan=" + Integer.toString(colspan));
            }
            totalRow.addValue(cell);
        }

        // Output the total row 
        totalRow.setAttr(tableTuner.getParameter("totalBgColor"));
        out.println(totalRow.toHTML());
        /**/
    }

    /**
     *
     * @param r
     * @throws Exception
     */
    protected void getRecord(ResultSet r) throws Exception {
        int shift = 1;

        /* collect and output subtotals row */
        if (makeSubtotals) {
            shift = 2;
            /* get the key value - the 1st field in the record */
            String keyValue = cfgTuner.parseString(r.getString(1));
            if (f_search.length() > 0) {
                keyValue = StrUtil.markSubstr(keyValue, f_search, mark_before, mark_after);
            }

            /* the key value changed - out subtotals */
            if (!keyValue.equals(oldKeyValue) //      &&(currentRow >= srn-1 && currentRow <= srn+rpp)
                    ) {
                if (colSubtotals != null) {
                    outSubtotals();
                } else if (cfgTuner.enabledOption("makeTotalsForCols")) {
                    colSubtotals = new double[numSqlColumns + 1];
                }

                /* out the new key value as a new row */
//      row.setAttr(tableTuner.getParameter("altBgColorAttr"));
                out.println("<tr><th " + tableTuner.getParameter("altBgColorAttr")
                        + " align=left colspan=" + Integer.toString(numSqlColumns)
                        + ">" + keyValue + "</th></tr>");
                oldKeyValue = keyValue;
                if (headerRow != null
                        && !cfgTuner.enabledOption("noRepeatHeaders")) {
                    out.println(headerRow.toHTML());
                }
//      row.setAttr(tableTuner.getParameter("bgColorAttr"));
            }
        }
        for (int colNr = 0; colNr < numSqlColumns; colNr++) {
            try {
//                System.out.println("***** TableServiceSimple.getRecord() colNr=" + colNr + ";  shift=" + shift + ";");
                try {
                    record[colNr] = r.getString(colNr + shift);
                }
                catch (ArrayIndexOutOfBoundsException ex) {
                    record[colNr] = null;
                }
                record[colNr] = (record[colNr] == null) ? "" : record[colNr];
                if (record[colNr].equalsIgnoreCase("ROWNUM")) {
                    record[colNr] = Integer.toString(currentRow + 1);
                }
                if (initCapCols.length() > 2
                        && initCapCols.indexOf(Integer.toString(colNr)) > 0) {
                    record[colNr] = StrUtil.initCap(record[colNr]);
                }

                if (f_search.length() > 0) {
                    record[colNr] = StrUtil.markSubstr(record[colNr], f_search, mark_before, mark_after);
                }
            } catch (Exception e) { // record[colNr] = "_";
                System.out.println("*****ERROR: TableServiceSimple.getRecord() colNr=" + colNr + ";  shift=" + shift + "; " + e.toString());
                e.printStackTrace(System.out);
                ((QueryThread) rm.getObject("QueryThread")).logException(e);
                record[colNr] = e.toString();
            }
//    record[colNr] = (record[colNr] == null)? "" : StrUtil.unicode(record[colNr]);
        }
    }

    /**
     *
     * @param resultSet
     */
    protected void outTableHeader(ResultSet resultSet) {
        if (headerRow == null) {
            return;
        }
        headerRow.setAttr(tableTuner.getParameter("headerBgColor"));
        for (int i = 0; i < numSqlColumns - numSpecialCols; i++) {
            String tag = cfgTuner.getParameter(colTagsSectionName, colNames[i]);
//    System.out.println("i=" + i + "; tag=" + tag + "; colName=" + colNames[i]);
            tag = (tag.equals("")) ? colNames[i] : tag;
            colLabels[i] = tag;
            if (tag.contains("$CALL_SUBSERVICE")) {
                int j = tag.indexOf(" tag=");
//		System.out.println("i=" + i + "; tag=" + tag + "; colName=" + colNames[i] + "; j=" + j);
                if (j > 0) {
                    tag = tag.substring(j + 5, tag.length() - 1);
                    if (tag.indexOf(";") > 0) {
                        tag = tag.substring(0, tag.indexOf(";"));
                    }
                } else {
                    tag = null;
                }
            }
//    if (tag.indexOf("$CALL_SERVICE") < 0)
            if (tag != null) {
                if (tag.indexOf("$ATTR:") > 0) {
                    int j = tag.indexOf("$ATTR:");
                    cell_h.setAttr(tag.substring(j + 6));
                    tag = tag.substring(0, j - 1);
                }
                cell_h.setValue(tag.trim());

                /*  try 
      { String p = "(" ;
        if (makeSubtotals)
          p += metaData.getColumnType(i+2) + ")";
        else
          p += metaData.getColumnType(i+1) + ")";
        cell_h.setValue(tag + p);
      } 
      catch (Exception e) {
        e.printStackTrace(System.out);
        cell_h.setValue(tag);
      }
/**/
                headerRow.addValue(cell_h);
            }
        }
//  System.out.println(headerRow.toHTML());
        if (!cfgTuner.enabledOption("hide_headers")) {
            out.println(headerRow.toHTML());
            out.flush();
        }
// fill the rest - special columns	
        for (int i = numSqlColumns - numSpecialCols; i < numSqlColumns; i++) {
            String tag = cfgTuner.getParameter(colTagsSectionName, colNames[i]);
            tag = (tag.equals("")) ? colNames[i] : tag;
            colLabels[i] = tag;
        }
//  row.reset();
    }

    /**
     *
     * @param text
     */
    protected void outFullRow(String text) {
        row.reset();
        row.setAttr(tableTuner.getParameter("altBgColorAttr"));
        cell.setValue(text);
        cell.setAttr("colspan=" + Integer.toString(numTableColumns));
        row.addValue(cell);
        out.println(row.toHTML());
        row.reset();
    }

    /**
     *
     */
    protected void initArrays() {
        oldRecord = new String[numSqlColumns];
        record = new String[numSqlColumns + 1];
        colTotals = new double[numSqlColumns + 1];
        numDigitsForCols = new int[numSqlColumns];
        rowTotal = 0.;
        int j;

        for (int colNr = 0; colNr <= numSqlColumns; colNr++) {
            record[colNr] = "";
            colTotals[colNr] = 0.;
            if (colNr < numSqlColumns) {
                j = cfgTuner.getIntParameter("numDigitsForCols", colNames[colNr]);
                if (j >= 0) {
                    numDigitsForCols[colNr] = j;
                } else {
                    numDigitsForCols[colNr] = numDigits;
                }
            }
        }
    }

    /**
     *
     * @param tagName
     */
    protected void outTag(String tagName) {
        String tag = tableTuner.getParameter(tagName);
//  System.out.println("=== " + tagName + ":'" + tag + "'");
        if (tag.indexOf("$INCLUDE") == 0) {
            tag = tag.substring(8).trim();
            try {
                tableTuner.outCustomSection(tag.substring(1, tag.length() - 1), out);
            } catch (Exception e) {
                out.println(tag);
                System.out.println("=========== tag:'" + tag.substring(1, tag.length() - 1) + "'");
            }
        } else if (tag.length() > 0) {
            out.println(tag);
        }
    }

    /**
     *
     * @param numItems
     * @param minRow
     * @param maxRow
     */
    protected void setRowLinks(int numItems, int minRow, int maxRow) {
        String s = "";
        int ern_i;
        cfgTuner.addParameter("srn_i", "");
        cfgTuner.addParameter("ern_i", "");

        if (minRow > 1) {
            int m1 = minRow - numItems;
            if (m1 < 1) {
                m1 = 1;
            }
            cfgTuner.addParameter("srn_i", Integer.toString(m1));
            s = s + cfgTuner.getParameter("prevSetLink");
        }
        for (int srn_i = minRow; srn_i < numItems; srn_i += rpp) {
            ern_i = srn_i + rpp - 1;
            if (ern_i > numItems) {
                ern_i = numItems;
            }
            if (srn_i == srn) {
                cfgTuner.addParameter("currentPage", "YES");
            } else {
                cfgTuner.deleteParameter("currentPage");
            }

            cfgTuner.addParameter("srn_i", Integer.toString(srn_i));
            cfgTuner.addParameter("ern_i", Integer.toString(ern_i));
            s = s + cfgTuner.getParameter("rowLink");
        }
        if (maxRow == numItems) {
            cfgTuner.addParameter("srn_i", Integer.toString(maxRow + 1));
            s = s + cfgTuner.getParameter("nextSetLink");
        }
        cfgTuner.addParameter("rowLinks", s);
    }

    /**
     * Create and run SQL query.
     *
     * @param sqlSectionName
     * @return <BR>
     * @throws java.lang.Exception
     */
    protected ResultSet runSQL(String sqlSectionName) throws Exception { //if (!terminated) Thread.sleep(2000);
        ResultSet resultSet = dbUtil.getResults(getSQL(sqlSectionName));
//  if (!terminated) Thread.sleep(2000);
        IOUtil.writeLog(1, " <b> Processed in " + dbUtil.timeSpent + "ms. </b><br>", rm);
        if (terminated) {
            return null;
        }
        return resultSet;
    }

}
