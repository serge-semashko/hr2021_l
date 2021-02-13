package dubna.walt.util;

import dubna.walt.QueryThread;
import java.io.*;
import java.util.*;
import dubna.walt.service.Service;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import javax.net.ssl.HttpsURLConnection;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.regexp.*;
import javax.servlet.http.*;

/**
 * Provides the basic functionality for the Tuner class.<br>
 * Normally this class should not be used directly - use Tuner instead.
 *
 * @see Tuner
 */
public class BasicTuner {

    /**
     * Поддержка использования JavaScript на стороне сервера в CFG AJM и других
     * файлах
     */
    // 
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine_JS = manager.getEngineByName("JavaScript");
    /**
     * Для использовании в javascript
     */
    PrintWriter BTout = null;
    /**
     *
     */
    public Deque<String[]> parastack = new ArrayDeque<String[]>();
    /**
     * The array of the dynamic parameters.
     */
    public String[] parameters = null;

    /**
     * Content of the current configuration file.
     */
    public String[] cfg = null;

    /**
     *
     */
    public String cfgFileName = "";
    /**
     * The current ResourceManager.
     */
    public ResourceManager rm = null;

    /**
     * The current ProfileResourceManager.
     */
    public ResourceManager prm = null;
    /**
     * The current StaticResourceManager.
     */
    public ResourceManager srm = null;

    /**
     * "Flash"-parameters, used within "$INCLUDE" directive.
     */
    protected String[] flashParameters = null;

    /**
     * Root path to templates
     */
    protected String cfgRootPath = "";

    /**
     * flag: "Process strings". If FALSE - no source parsing will be executed.
     */
    protected boolean parseData = true;
//  private boolean recursive = false;

    /**
     * line delimiter character for the output string
     */
    // public final char c='\n';
    protected boolean keepFlashParameters = false;

    /**
     *
     */
    public HttpSession session = null;

    private long tm = 0;
    public boolean traceProcessing = false;

    /**
     * Sets the session attribute.<p>
     *
     * @param pName
     * @param pVal
     */
    public void setParameterSession(String pName, String pVal) {
        if (session == null) {
            HttpServletRequest req = (HttpServletRequest) rm.getObject("request", false);
//      rm.println("***REQUEST:" + req + "; " + rm.toString());
            if (req == null) {
                IOUtil.writeLogLn(3, " BasicTuner.setParameterSession(" + pName + "," + pVal
                        + ") FAILED! request is null!", rm);

                return;
            }
            session = req.getSession();
        }
//		if(pVal == null || pVal.length() == 0) //
//		  session.removeAttribute(pName);  // почему-то иногда ЛОМАЕТ поиск в [parameters] по условию типа: f_year=#CURR_YR# ??!f_year
//		else 
        session.setAttribute(pName, pVal);
    }

    /**
     *
     * @param fileName
     * @param sectionName
     * @return
     */
    public String[] getCustomSection(String fileName, String sectionName) {
        return getCustomSection(fileName, sectionName, null);
    }

    /**
     * Obtains a customized section from a template file.
     *
     * This is one of the most often used methods.<p>
     *
     * Processes each line in the section, substitutes the parameters,
     * includes/excludes optional lines (marked with '??') depending on the
     * current parameters set, processes $INCLUDE, $CALL_SERVICE,
     * $SET_PARAMETERS, $PRINT, $LOG, $GET_URL, $GET_AUTH_URL $GET_ID, $USE_DB,
     * $WAIT, $GET_DATA, $EXECUTE_LOOP directives.<p>
     *
     * $GET_URL url_string - open URL given in the url_string, the responce adds
     * to the resulting array/ puts as single string value of the parameter with
     * name #URL_RESPONCE_PARAM# if it's defined otherwise and puts to the out,
     * if it's not null.
     *
     *
     * @param fileName name of the template file. If it is null the current
     * Tuner's configuration file will be used.
     * @param sectionName name of the section (without square brackets "[]"). If
     * it is null - the first section of the configuration file will be
     * returned.
     * @param out output stream to the client
     * @return String array, containing the customized section.
     */
    public String[] getCustomSection(String fileName, String sectionName, PrintWriter out) {
        BTout = out;
        String line, result;
        String[] sectionBody = null;
        String[] source = null;
        String ifState = "";
        String fn = (fileName == null || fileName.trim().length() == 0) ? null : fileName.trim();


        /* read the source file, if specified */
        try {
            if (fn != null) {
                fn = getModFileName(fn, "SIMPLE");
            }
            source = (fn == null) ? cfg : readFile(cfgRootPath + fn);
        } catch (Exception e) {
            source = null;
        } finally {
            if (source == null) {
                return null;
            }
        }

        Vector sectionLines = new Vector(source.length, 1);

        /* find the beginning of the section */
        int i = (sectionName == null) ? 0 : (getSectionPosition(source, sectionName, 0));
//  rm.println("~~~~~ sectionName=" + sectionName + "; position="+i);
        if (i < 0) {
            return null;
        }

        /* set "Flash" - parameters, if specified in the section name line */
        if (source[i].indexOf("param:") > 0) {
            setFlashParameters((source[i].substring(source[i].indexOf("param:") + 6)).trim());
        }

        /*======= process the section body (the "source" array) =======*/
        int optIndex;
        for (i++; i < source.length; i++) {
            result = source[i];                 // the next section line
            if ((optIndex = result.indexOf("??")) > 0) // check if the option enabled
            {
//			rm.println("result:" + result);
//			rm.println(result.substring(optIndex + 2).trim());
//			rm.println(" enabled:" + enabledExpression(result.substring(optIndex + 2).trim()));
                if (enabledExpression(result.substring(optIndex + 2).trim())) {
                    result = result.substring(0, optIndex).trim();
                } else {
                    continue;    // ignore line, if the option disabled
                }
            }

            line = result.trim();
            /* check for the end of section */
            if (line.toUpperCase().indexOf("[END]") == 0 && sectionName != null) {
                break;
            }
            //Прекращение обработки секции 
            if (line.toUpperCase().indexOf("$BREAK") == 0) {
                break;
            }

            // process the $SET_PARAMETERS directive
            if (line.indexOf("$SET_PARAMETERS") == 0 & parseData) {
                _$SET_PARAMETERS(line, sectionLines, out, sectionName);
                continue;
            }

            /* process the $INCLUDE directive */
            if (line.indexOf("$INCLUDE") == 0 & parseData) {
                _$INCLUDE(line, sectionLines, out, sectionName);
                continue;
            }
            // Process "$GET_URL Directive 
            if (line.indexOf("$GET_URL") == 0 & parseData) {
                _$GET_URL(line, sectionLines, out, sectionName);
                continue;
            } // Process "$GET_AUTH_URL Directive  
            if (line.indexOf("$GET_AUTH_URL") == 0 & parseData) {
                _$GET_AUTH_URL(line, sectionLines, out, sectionName);
                continue;
            }
            if (line.indexOf("$PRINT") == 0 & parseData) { // Process "$PRINT Directive
                _$PRINT(line, sectionLines, out, sectionName);
                continue;
            }
            if (line.indexOf("$STORE_PARAMETERS") == 0 & parseData) {
                _$STORE_PARAMETERS(line, sectionLines, out, sectionName);
                continue;
            }
            if (line.indexOf("$RESTORE_PARAMETERS") == 0 & parseData) {
                _$RESTORE_PARAMETERS(line, sectionLines, out, sectionName);
                continue;
            }
            if (line.indexOf("$LOG_ERROR") == 0 & parseData) { // Process "$LOG_ERROR Directive - store message to DB
                _$LOG_ERROR(line, sectionLines, out, sectionName);
                continue;
            }
            if (line.indexOf("$LOG") == 0 & parseData) { // Process "$LOG Directive 
                _$LOG(line, sectionLines, out, sectionName);
                continue;
            }
            if (line.indexOf("$GET_ID") == 0 & parseData) { // Process "$GET_ID Directive 
                _GET_ID(line, sectionLines, out, sectionName);
                continue;
            }
            if (line.indexOf("$USE_DB") == 0 & parseData) { // Process $USE_DB Directive
                _$USE_DB(line, sectionLines, out, sectionName);
                continue;
            }
            if (line.startsWith("$CLOSE_DB") & parseData) { // Process $CLOSE_DB Directive
                _$CLOSE_DB(line, sectionLines, out, sectionName);
                continue;
            }
            if (line.startsWith("$TIMER") & parseData) { // Process $CLOSE_DB Directive
                _$TIMER(line, sectionLines, out, sectionName);
                continue;
            }
            if (line.indexOf("$WAIT") == 0 & parseData) { // Process "$WAIT Directive
                _$WAIT(line, sectionLines, out, sectionName);
                continue;
            }
            if (line.indexOf("$GET_DATA") >= 0 & parseData) { // Process "$GET_DATA Directive
                _$GET_DATA(line, sectionLines, out, sectionName);
                continue;
            }
            // Process "$EXECUTE_LOOP Directive 
            if (line.indexOf("$EXECUTE_LOOP") >= 0 & parseData) {
                _$EXECUTE_LOOP(line, sectionLines, out, sectionName);
                continue;
            }
            // Process "$CALL_SERVICE Directive 
            if (line.indexOf("$CALL_SERVICE") >= 0 & parseData) {
                _$CALL_SERVICE(line, sectionLines, out, sectionName);
                continue;
            }
            // Process "$COPY_FILE srcFilePath destFilePath Directive
            if (line.indexOf("$COPY_FILE") >= 0 & parseData) {
                _$COPY_FILE(line, sectionLines, out, sectionName);
                continue;
            }
            // Process "$MOVE_FILE srcFilePath destFilePath Directive
            if (line.indexOf("$MOVE_FILE") >= 0 & parseData) {
                _$MOVE_FILE(line, sectionLines, out, sectionName);
                continue;
            }
            // Process "$DELETE_FILE srcFilePath  Directive
            if (line.indexOf("$DELETE_FILE") >= 0 & parseData) {
                _$DELETE_FILE(line, sectionLines, out, sectionName);
                continue;
            }
            // Process "$GET_FILE_SIZE srcFilePath Directive
            if (line.indexOf("$GET_FILE_SIZE") >= 0 & parseData) {
                _$GET_FILE_SIZE(line, sectionLines, out, sectionName);
                continue;
            }
            // выполнение одиносной строки как javascript
            if (line.indexOf("$JS ") == 0 & parseData) {
                _$JS(line, sectionLines, out, sectionName);
                continue;
            }
            //Проверка на закрытие $IF
            if (line.startsWith("$EIF") & parseData) { // Process ELSE part of IF
                logExecutedLine(line, out, sectionName);
                continue;
            }
            if (line.startsWith("$ENDIF") & parseData) { // Process ELSE part of IF
                logExecutedLine(line, out, sectionName);
                continue;
            }
            // У IF условие было ИСТИНА и секция закончилась - теперь надо  пропустить секцию после ELSE
            if (line.startsWith("$ELSE") & parseData) { // Process ELSE part of IF
                logExecutedLine(line, out, sectionName);
                int iStart = i;
                for (i++; i < source.length; i++) {
                    line = (source[i].trim());
                    line = checkConditionAndParse(line);
                    if (line.indexOf("[END]") == 0) {
                        break;
                    };
                    if (line.startsWith("$ENDIF")) {
                        logExecutedLine(line, out, sectionName);
                        break;
                    };
                    if (line.startsWith("$EIF")) {
                        logExecutedLine(line, out, sectionName);
                        break;
                    };
                }
                IOUtil.writeLogLn(2, "<font color=green>$IF state True. ELSE part. skip  " + (i - 1 - iStart) + " lines</font>", rm);
                continue;
            }
            if (line.startsWith("$IF ") & parseData) { // 
                logExecutedLine(line, out, sectionName);
                String expression = line.substring(4);
                if (enabledExpression(expression.trim())) {
                    IOUtil.writeLogLn(2, "<font color=green>" + line + "  state=True</font>", rm);
                    continue;
                } else {
                    int iStart = i;
                    for (i++; i < source.length; i++) {
                        line = (source[i].trim());
                        line = checkConditionAndParse(line);
                        if (line.startsWith("$ELSE")) {
                            logExecutedLine(line, out, sectionName);
                            break;
                        };
                        if (line.indexOf("[END]") == 0) {
                            break;
                        };
                        if (line.startsWith("$ENDIF")) {
                            logExecutedLine(line, out, sectionName);
                            break;
                        };
                        if (line.startsWith("$EIF")) {
                            logExecutedLine(line, out, sectionName);
                            break;
                        };
                    }
                    IOUtil.writeLogLn(2, "<font color=green>" + line + " state=FALSE. skip  " + (i - 1 - iStart) + " lines</font>", rm);
                }

                continue;
            }

            // От директивы $JS_BEGIN/$JS_{ до $JS_END/$JS_} иди до конца секции блок будет будет исполняться как javascript код
            if (((line.indexOf("$JS_BEGIN") == 0) || (line.indexOf("$JS_{") == 0) || (line.indexOf("$JS{") == 0)) & parseData) {
                i = _$JS_BLOCK(source, i, line, sectionLines, out, sectionName);
                continue;
            }

            // process the normal line (not a directive)
            if (traceProcessing) {
                if ((out != null) & !(sectionName.equals("report_"))) {
                    String outline = parseString(result);
                    outline = outline.replace("<", "&lt;");
                    outline = outline.replace(">", "&gt;");
//                    WriteLog(3, "<br> out[" + sectionName + "]" + outline + "<br>");
                    WriteLog(3, "<font color=green>out&gt;" + outline + "</font><br>");
                }
            }

            addLine(parseString(result), sectionLines, out);

        } // end of the foop through the section lines

        /* finally copy the resulting vector into a String array */
        if ((sectionLines.size()) > 0) {
            sectionBody = new String[sectionLines.size()];
            sectionLines.copyInto(sectionBody);
        }

        return sectionBody;
    }

    /**
     *
     * @param fname
     * @param securityMode
     * @return
     */
    public String getModFileName(String fname, String securityMode) {
//        System.out.println("\n\r***** BasicTuner.getModFileName(): " + fname + "; securityMode=" + securityMode); 
        String requestType = rm.getString("requestType");
        if (securityMode.equals("")) {
            securityMode = rm.getString("securityMode", false, "SIMPLE");
        }
        String fn = fname;

//        System.out.print("***** BasicTuner.getModFileName(): " + fname + "; requestType=" + requestType + "; securityMode=" + securityMode);
        if (securityMode.equalsIgnoreCase("SIMPLE")) {
            if (!fn.contains(".")) {
                fn += ".cfg";
            }
            if (!IOUtil.fileExists(cfgRootPath + fn)) {
                fn = fn.replace(".cfg", ".ajm");
            }
            if (!IOUtil.fileExists(cfgRootPath + fn)) {
                fn = fn.replace(".ajm", ".mod");
            }
            if (!IOUtil.fileExists(cfgRootPath + fn)) {
                fn = fn.replace(".mod", ".dat");
            }
            if (!IOUtil.fileExists(cfgRootPath + fn)) {
                ((Logger) rm.getObject("logger")).logRequest2DB(rm, "file not found(s): " + fname, null);
                fn = fname;
            }

        } else {
            if (requestType.equals("DIRECT")) {
                if (!fn.contains(".")) {
                    fn += ".mod";
                }
                if (!IOUtil.fileExists(cfgRootPath + fn)) {
                    ((Logger) rm.getObject("logger")).logRequest2DB(rm, "FILE NOT FOUND(d): " + fname + "; requestType=" + requestType + "; securityMode=" + securityMode, null);
                }
            } else {
                if (!fn.contains(".")) {
                    fn += ".ajm";
                }
                if (!IOUtil.fileExists(cfgRootPath + fn)) {
                    fn = fn.replace(".ajm", ".mod");
                    if (!IOUtil.fileExists(cfgRootPath + fn)) {
                        ((Logger) rm.getObject("logger")).logRequest2DB(rm, "FILE NOT FOUND(a): " + fname, null);
                        fn = fname;
                    }
                }
            }
        }
//        System.out.println(" => " + fn + ";");
        return fn;
    }

    public void storeParameters() {
        parastack.push(parameters.clone());
    }

    public void restoreParameters() {
        parameters = parastack.pop();
    }

    /**
     *
     * @return
     */
    public synchronized String getNewIntID() {
        long startId = Long.parseLong(rm.getString("startId", false, "0")) + 1;
        rm.setParam("startId", Long.toString(startId), true);
        return Long.toString(startId);
    }

    /**
     *
     * @return
     */
    public synchronized String getNewID() {
        Calendar calendar = new GregorianCalendar();
        Integer i_newID = (Integer) rm.getObject("i_newID", false);
        if (i_newID == null) {
            i_newID = 0;
        }
        int i = i_newID + 1;
        if (i >= 999) {
            i = 0;
        }
        rm.setObject("i_newID", i, true);

        String newID = Fmt.fmt(calendar.get(Calendar.YEAR), 4, Fmt.ZF)
                + Fmt.fmt(calendar.get(Calendar.MONTH) + 1, 2, Fmt.ZF)
                + Fmt.fmt(calendar.get(Calendar.DAY_OF_MONTH), 2, Fmt.ZF)
                + Fmt.fmt(calendar.get(Calendar.HOUR_OF_DAY), 2, Fmt.ZF)
                + Fmt.fmt(calendar.get(Calendar.MINUTE), 2, Fmt.ZF)
                + Fmt.fmt(calendar.get(Calendar.SECOND), 2, Fmt.ZF)
                + Fmt.fmt(i, 4, Fmt.ZF);
        return newID;
    }

    /**
     *
     * @param line
     * @param sectionLines
     * @param out
     */
    protected void addLine(String line, Vector sectionLines, PrintWriter out) {
        if (line.length() > 0 && !line.equals("\r\n")) {
            sectionLines.addElement(line);
            if (out != null) {

                out.println(line);
                out.flush();
            }
        }
    }

    /**
     * Get the value of a parameter. If there are several definitions of this
     * parameter - returns the first one.
     *
     * if the sectionName=null looks in: flashParameters, dynamic parameters,
     * [parameters] section of the config.file, ResourceManager.
     *
     * If the parameter value starts with "==" - returns the value "as is" -
     * without any parsing / substitution
     *
     * @param fileName
     * @param sectionName the name of the section.
     * @param parameterName the name of the section.
     * @return String. Contains the parameter's value, if the parameter has been
     * found and an empty string otherwise.
     *
     */
    public String getParameter(String fileName, String sectionName, String parameterName) {
        String value;
        //  rm.println("***** Tuner - look for: [" + sectionName + "] : '" + parameterName + "'");

// if the parameter name starts with '^' - it's a static resource parameter 
// defined in the profile resource file or in the language resource file.
// 
        if (parameterName.indexOf("^") == 0) // look in profile and language parameters
        {
            return getStaticParameter(parameterName);
        } else if (sectionName == null) // section not specified
        { // - look in Flash parameters first
            value = getParameterValue(flashParameters, parameterName, false);
            //  rm.println("***** Tuner: " + parameterName + "='" + value + "'");
            if (value != null) {
                return value;
            }

            // Than look in the dynamic parameters
            value = getParameterValue(parameters, parameterName, false);
            if (value != null) {
                return value;
            }

            // Than look in the session variables
            if (session != null) {
                try {
                    value = (String) session.getAttribute(parameterName);
                } catch (Exception ex) {
                    System.out.println(" BasicTuner.getParameter() ERROR: Session invalidated!");
                }
            }
            if (value != null) {
                return value;
            }

            // Than look in the [parameters] section of the config. file
            value = getParameterValue(
                    getSection(null, "parameters"), parameterName);

            if (value != null) {
                return value;
            }

            // finally - look in the ResourceManager
            if (rm != null) {
                return (rm.getString(parameterName, false));
            }

            return "";  // parameter not found
        } else { // section specified - get the parameter from it
            if (parseData) {
                value = getParameterValue(
                        getCustomSection(fileName, sectionName), parameterName, false);
            } else {
                value = getParameterValue(
                        getSection(fileName, sectionName), parameterName, false);
            }
            if (value == null) {
                value = "";
            }
            return value;
        }
    }


    /*
 * 
     */
    private String getStaticParameter(String parameterName) {
        //System.out.println("+++ search for:" + parameterName + "; prm=" + prm);
        // Search Profile Resource first
        String value;
        if (prm == null) {
            prm = (ResourceManager) rm.getObject("prm", false);
        }
        if (prm != null) {
            value = prm.getString(parameterName, false, null);
            if (value != null) {
                return value;
            }
        }

        // If not found - search Language Resource 
        String lang = null;
        if (session != null) {
            lang = (String) session.getAttribute("lang");
        }
        if (lang == null) {
            lang = "english";
        }
        //    System.out.println("+++lang=" + lang + "; prm=" + prm);

        if (srm == null) {
            srm = (ResourceManager) rm.getObject("srm_" + lang, false);
        }
        if (srm != null) {
            value = srm.getString(parameterName, false, null);
            if (value != null) {
                return value;
            }
        }
        return rm.getString(parameterName, false, parameterName);
        //      return parameterName;
    }

    /**
     * Checks: if the boolean expression is TRUE or FALSE.<P>
     *
     * The following <b>operators</b> can be used in the <b>boolean
     * expression:</b><ul>
     * <li>- "!" - negotiation;
     * <li>- "&" - AND operator;
     * <li>- "|" - OR operator;
     * </ul>
     * An expression is processed from left to right until its result is
     * known.<br>
     * <b>NOTE:</b> There is no brackets processing!
     * <p>
     *
     * @param s
     * @return true if the expression is enabled and false otherwize.
     * @see #enabledOption
     */
    public boolean enabledExpression(String s) {
        boolean and, or, result;
        String nextOption, theRest;
        int indOpr = -1;
        int indRe = -1;
        int optEnd = 0;

//rm.println("---expr:'" + s +"'");
//  RE nextOption = new RE("[,-zА-я]+=??[{{[^}]]*}}]??|[&|][^{}&|]+|$");
        RE reStart = new RE("[A-я]+=\\x7B\\x7B");
//        IOUtil.writeLogLn(9, "<br><b>BasicTuner.enabledExpression(" + s + ")</b> reStart=" + "[A-я]+=\\x7B\\x7B" + "; ", rm);

        theRest = s;
        while (theRest.length() > 0) {
            and = false;
            or = false;
            indOpr = -1;
            optEnd = 1;

//rm.println("---theRest:'" + theRest +"'");
            if (reStart.match(theRest)) {
                indRe = reStart.getParenStart(0);
            }
            if (indRe > 1) {
                indRe = -1;
            }

            if (indRe >= 0) {
                optEnd = theRest.indexOf("}}") + 2;
                //     nextOption = theRest.substring(0,optEnd);    
            }

            for (int i = optEnd; i < theRest.length() - 1; i++) {
                if ((theRest.charAt(i) == '&')
                        || (theRest.charAt(i)) == '|') {
                    indOpr = i;
                    optEnd = i;
                    break;
                }
            }

            if (indOpr > 0) {
                and = (theRest.charAt(indOpr) == '&');
                or = !and;
                nextOption = theRest.substring(0, optEnd);
                theRest = theRest.substring(optEnd + 1);
//rm.println(nextOption);
            } else {
                nextOption = theRest;
                theRest = "";
            }
            result = enabledOption(nextOption);
            IOUtil.writeLogLn(9, "result=" + result + "; and=" + and + "; or=" + or, rm);
            if (and && !result) {
                return false;
            }
            if (or && result) {
                return true;
            }
            if (!(and || or)) {
                return result;
            }
        }
        IOUtil.writeLogLn(9, "FALSE! ", rm);
        return false;
    }

    /**
     *
     * @param s
     * @return
     */
    public boolean xxxenabledExpression(String s) {
        boolean and, or, result;
        String nextOption, theRest;
        int indOpr;

        theRest = s;
        while (theRest.length() > 0) {
            and = false;
            or = false;
            indOpr = -1;

            for (int i = 1; i < theRest.length() - 1; i++) {
                if ((theRest.charAt(i) == '&')
                        || (theRest.charAt(i)) == '|') {
                    indOpr = i;
                    break;
                }
            }

            if (indOpr > 0) {
                and = (theRest.charAt(indOpr) == '&');
                or = !and;
                nextOption = theRest.substring(0, indOpr);
                theRest = theRest.substring(indOpr + 1);
            } else {
                nextOption = theRest;
                theRest = "";
            }
            result = enabledOption(nextOption);
            if (and && !result) {
                return false;
            }
            if (or && result) {
                return true;
            }
            if (!(and || or)) {
                return result;
            }
        }
        return false;
    }

    /**
     * Checks: if the option is TRUE or FALSE.<P>
     *
     * An option definition can have format either "param=value" or "param".<br>
     * Option like "param=value" is TRUE if parameter "param" has value="value"
     * and FALSE otherwise.<br>
     * Wildcard chars '*' (any characters) and '?' (any character) are accepted
     * in the "value" Option like "param" is TRUE if parameter "param" defined
     * (has non-empty value) and FALSE otherwise.<p>
     *
     * @param opt
     * @return true if the option is enabled and false otherwize.
     * @see #enabledExpression
     */
    public boolean enabledOption(String opt) {
//rm.println("...opt:'" + opt +"'");
        if (!parseData) {
            return true; //flag "not parse data" is set - all options enabled
        }
        if (opt == null) {
            return false;
        }

        String option = opt;
        if (option.contains("#")) {
            option = parseString(option); // substitute parameters in the option
        }
        boolean neg = (option.charAt(0) == '!');  // "!" means "negate result"
        if (option.length() < 1) {
            return false;  // option text not defined - return "false"
        }
        boolean result = false;
        option = (neg) ? option.substring(1) : option;  //remove leading "!"
        if (option.length() < 1) {
            return false;
        }

        /* test the parameter */
        String pName = option;  // name of the parameter to be looked
        String oVal = "";       // value specified in the option
        String oper = "=";

        int q = option.indexOf("=");
        if (q < 0) // "equals" not found - look for "GT"
        {
            q = option.indexOf(">");
            if (q > 0) {
                oper = ">";
            }
        }
        if (q < 0) // "GT" not found - look for "LT"
        {
            q = option.indexOf("<");
            if (q > 0) {
                oper = "<";
            }
        }
        if (q > 0) {
            pName = option.substring(0, q);
            oVal = option.substring(q + 1);
        } //	rm.println("... option:" + opt + "; cfg=" + cfgFileName);
        else if (q == 0) // option can't start with " = > < "
        {
            return neg;
        }

        String pVal = getParameter(null, null, pName);  // get the value of the parameter
        if (pVal.length() == 0) // parameter not found - return  false^"neg"
        {
            return neg;
        }
        if (q < 0) // value not specified in the option
        {
            return (!neg);    // - return true^"neg"
        }
        /*---------- Check the parameter's value ------------*/
// if (oper.equals("<") || oper.equals(">"))
//	 rm.println(opt + "; pVal='" + pVal + "': oVal='" + oVal + "' oper=" + oper);

        if (oper.equals("=") && option.equals(pName + "=" + pVal)) // "option": "pName=pVal" - OK!
        {
            return (!neg);    // exact value found - return true^"neg"
        } else if (oper.equals(">")) // "option": "pName>pVal"
        {
            try {
                if (Double.parseDouble(pVal) > Double.parseDouble(oVal)) //- OK!
                {
                    return (!neg);    // param > value - OK - return true^"neg"
                }
            } catch (Exception e) {
                return (neg);
            } // not number
        } else if (oper.equals("<")) // "option": "pName<pVal"
        {
            try {
                if (Double.parseDouble(pVal) < Double.parseDouble(oVal)) //- OK!
                {
                    return (!neg);    // param < value - OK - return true^"neg"
                }
            } catch (Exception e) {
                return (neg);
            } // not number
        } /*---------- Check the regular expression value ------------*/ else if (oVal.indexOf("{{") == 0) // exact value not found - test for wildcards
        {
            oVal = oVal.substring(2, oVal.length() - 2);
            IOUtil.writeLog(9, "<br><b>BasicTuner.enabledOption(" + opt + ")</b> oVal=" + oVal + "; ", rm);
            try {
                RE r = new RE("^" + oVal + "$");
                result = r.match(pVal);
                IOUtil.writeLogLn(9, "RE=^" + oVal + "$; result=" + result + "; ", rm);
//    result = StrUtil.match( oVal, pVal );
//  rm.println(opt + "; pVal='" + pVal + "': oVal='" + oVal + "': result:" + result);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }

        }

        return result ^ neg;
    }

    /**
     * Parses the source string and substitutes parameter patterns
     * ('#&lt;name>#') with the parameters values.<p>
     *
     * The value for the parameter is searched in the following places: <ul>
     * <li>"Flash-parameters" (used in the "$INCLUDE" - directive with
     * parameters);
     * <li>dynamic Tuner's parameters (String[] parameters);
     * <li>parameters, defined in the [parameters] section of the config. file;
     * <li>in the current RresourceManager;
     * </ul><p>
     *
     * Combinations "##" and "\#" will be replaced by a single hash ("#").<br>
     * Parsing of the data can be disabled by setting parseData flag to FALSE.
     * In this case method will always return the source string.<p>
     *
     * @param source contains the source string.
     * @return String that contains the result of the parsing.
     * @see #setParseData
     */
    public String parseString(String source) {
        if (source == null || source.length() == 0) {
            return "";
        }

        if (!parseData) {
            return source;
        }

        /* process the MACRO-call (I think, it's not used any more) */
        while (source.contains("${")) {
            source = parseMacro(source);
        }

        int i = source.indexOf("#");
        if (i < 0) {
            return source.trim();   // no parameters found - return the source string
        }
        /*===== process the parameters =====*/
        StringBuilder result = new StringBuilder(source.length() * 2);

//  rm.println("Source:"+source);
        /* b - index of the starting '#', e - ending '#' */
        for (int b = i, e = -1; b >= 0; b = source.indexOf("#", e + 1)) {
            boolean ref = false;
            /*  combination '\#' means '#' */
            if (b > 0 && source.charAt(b - 1) == '\\') {
                result.append(source.substring(e + 1, b - 1)).append("#");
                e = b;
                i = e + 1;
            } else {
                /* ^#param_name# - double substitution */
                if (b > 0 && source.charAt(b - 1) == '^') {
                    ref = true;
                    result.append(source.substring(e + 1, b - 1)); // add substring before '^#'
                } else {
                    result.append(source.substring(e + 1, b)); // add substring before '#'
                }
                e = source.indexOf("#", b + 1);
                if (e < 0) {
                    i = b;
//      rm.println("Tuner WARNING: tag '#' not closed:'"+source+"'("+String.valueOf(b)+")" );
                    break;
                }
                /*  combination '##' also means '#' */
                if (e == (b + 1)) {
                    result.append("#");
                } else // try to substitute the parameter
                {
                    String paramName = source.substring(b + 1, e);
                    if (ref) {

                        if ((paramName.length() > 0) && (paramName.charAt(0) == ':')) {
                            InitScriptEngine();
                            String tmpvar = "var tmp_for_parameter = " + paramName.substring(1) + ";";
                            JS_Execute(tmpvar, null, BTout);
                            Object res = engine_JS.get("tmp_for_parameter");
                            String test = "";
                            if (res != null) {
                                test = res.toString();
                            }
                            paramName = test;
                        } else {
                            paramName = getParameter(null, null, paramName);
                        }

                    }

                    if ((paramName.length() > 0) && (paramName.charAt(0) == ':')) {
                        InitScriptEngine();
                        String tmpvar = "var tmp_for_parameter = " + paramName.substring(1) + ";";
                        rm.println("========== JS parameter:" + paramName.substring(1) + "\n");
                        JS_Execute(tmpvar, null, BTout);
                        rm.println("========== JS eval parameter:" + paramName.substring(1) + "\n");
                        Object res = engine_JS.get("tmp_for_parameter");
                        String test = "";
                        if (res != null) {
                            test = res.toString();
                        }
                        rm.println("========== JS result eval parameter:" + test + "\n");
                        result.append(test);
                    } else {
                        result.append(getParameter(null, null, paramName));  // add the parameter value
                    }
                }
                i = e + 1;  // shift to the next part of the 'source' string
            }
        }

        if (source.length() > i) // add the rest of the source string
        {
            result.append(source.substring(i, source.length()));
        }

//  rm.println("result:" + result.toString().trim());
        return result.toString().trim();
    }

    /**
     * Adds a dynamic parameter.<P>
     *
     * If such a parameter already defined in the dynamic parameters - replaces
     * the its value by the new one.<p>
     *
     * @param name name of the parameter to be added.
     * @param value value of the parameter.
     */
    public void addParameter(String name, String value) {
        String p = name;
        if (value != null) {
            p = p.concat("=");
        } else if (name.indexOf("=") > 0) {
            p = name.substring(0, name.indexOf("=") + 1);
            value = name.substring(name.indexOf("=") + 1);
        }
        value = (value != null) ? value : "";

        if (parameters == null) // if there are no parameters at all
        {
            parameters = new String[1];   // - create parameters array
            parameters[0] = p.concat(value);  // and add parameter "name"
            return;
        }

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].indexOf(p) == 0) // if there already exists parameter "name"
            {
                parameters[i] = p.concat(value);    // - reset its value
                return;
            }
        }

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].length() == 0) // if there is an empty space for the new parameter
            {
                parameters[i] = p.concat(value);    // - use it
                return;
            }
        }

        // otherwise we have to add an extra parameter - increase the array
        String[] t = parameters;                  // there is not such a parameter
        parameters = new String[parameters.length * 2]; // - increase the parameters array
        System.arraycopy(t, 0, parameters, 0, t.length); // and keep the existing parameters
        for (int i = t.length + 1; i < parameters.length; i++) {
            parameters[i] = "";                  // and keep the existing parameters
        }
        parameters[t.length] = p.concat(value);            // add the new parameter "name"

//  rm.println(t.length+": NEW ==> '" + p + "':'" + value+"'");
    }

    /**
     * Adds a set of dynamic Flash-parameters.<P>
     *
     *
     * @param params list of the pairs "parameterName=parameterValue", separated
     * by semicolon.
     * @see #setFlashParameter
     */
    public void setFlashParameters(String params) {
//  rm.println("===== params:'" + params + "'");
        StringTokenizer st = new StringTokenizer(params, ";");
        String token;
        int i;
        while (st.hasMoreTokens()) {
            token = st.nextToken().trim();
            i = token.indexOf("=");
//    rm.println("===== token:'" + token + "'");
            if (i > 0) {
                setFlashParameter(token.substring(0, i), StrUtil.replaceInString(token.substring(i + 1), "&nbsp", "&nbsp;"));
            }
        }
    }

    /**
     * Adds a dynamic Flash-parameter.<P>
     *
     * If such a parameter already defined - replaces the parameter value by the
     * new one.<p>
     *
     * @param name name of the parameter.
     * @param value value of the parameter.
     */
    public void setFlashParameter(String name, String value) {
        String p = name.trim().concat("=");
        value = (value != null) ? value.trim() : "";

//  rm.println("setFlashParameter " + p + value);
        if (flashParameters == null) // if there are not parameters at all
        {
            flashParameters = new String[64];     // - create parameters array
            for (int i = 1; i < flashParameters.length; i++) {
                flashParameters[i] = null;
            }
            flashParameters[0] = p.concat(value);  // and add parameter "name"
            return;
        }

        for (int i = 0; i < flashParameters.length; i++) {
            if (flashParameters[i] == null) // Look for an empty element
            {
                flashParameters[i] = p.concat(value); // - set its value
                return;
            } /**/ // if there already exists parameter "name" - overwrite it !!! Dangerows - changed 29.01.03 for IDS
            else if (!keepFlashParameters && flashParameters[i].indexOf(p) == 0) {
                flashParameters[i] = p.concat(value);
                return;
            } /**/ // if there already exists parameter "name" - not overwrite it (WAS BEFORE 17.02.02)
            else if (flashParameters[i].indexOf(p) == 0) {
                return;
            }
            /**/
        }

        String[] t = flashParameters;                  // there is not such a parameter
        flashParameters = new String[flashParameters.length + 64]; // - increase the parameters array
        System.arraycopy(t, 0, flashParameters, 0, t.length); // and keep the existing parameters
        flashParameters[t.length] = p.concat(value);            // add the new parameter "name"
    }

    /**
     * Removes all Flash-parameters.<P>
     *
     */
    public void clearFlashParameters() {
        flashParameters = null;
    }

    /**
     * Searchs a String array for the start index of the section.
     *
     * (looks the array for a string starting with "[sectionName]")
     *
     * @param source array where to search.
     * @param sectionName name of the section (case insensitive).
     * @param startFrom the start index of the source <br>
     *
     * @return int the result of the search (index of the array element,
     * containing "[sectionName]")<br>
     * If returned value is less then 0, the section was not found.
     */
    private int getSectionPosition(String[] source, String sectionName, int startFrom) {
        if (source == null) {
            return -1;
        }

        String key = sectionName.toUpperCase();
        if (key.indexOf("[") != 0) {
            key = "[" + key + "]";
        }

        for (int i = startFrom; i < source.length; i++) {
            if (source[i].toUpperCase().indexOf(key) == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sets the "parseData" flag.<p>
     *
     * @param parse if TRUE - source strings parsing is enabled (the initial
     * setting) and disabled otherwise.
     * @return
     */
    public boolean setParseData(boolean parse) {
        boolean old = parseData;
        parseData = parse;
        return old;
    }

    /**
     * Returns the body of the section "as-is"
     *
     * Seems to be used for parameters obtaining only. Probably, should be
     * replaced by something more efficient.
     *
     * @param fileName name of the config. file.
     * @param sectionName name of the section.
     * @return
     */
    public String[] getSection(String fileName, String sectionName) {
        String[] all;
        if (fileName != null) {
            try {
                String fn = getModFileName(fileName, "SIMPLE");
                all = readFile(cfgRootPath + fn);
            } catch (Exception e) {
                return null;
            }
        } else {
            all = cfg;
        }

        if (all == null) {
            return null;
        }

        int i = getSectionPosition(all, sectionName, 0);
        if (i < 0) {
            return null;
        }

        Vector body = new Vector(all.length);

        for (; i < all.length; i++) {
            if (all[i].equalsIgnoreCase("[end]")) {
                break;
            } else {
                body.addElement(all[i]);
            }
        }

        all = new String[body.size()];
        body.copyInto(all);
        return all;
    }

    /**
     * Gets the value of the parameter, defined in the sectionBody.<p>
     *
     * Checks the option, if specified in the sectionBody, parses the result
     * (substitutes the parameters with their values)
     *
     * Returns null, if the parameter value could not be found.
     *
     * If the parameter value starts with "==" - returns the value "as is" -
     * without any parsing / substitution
     *
     * This method is used in the "getParameter(...)" method only.
     *
     * @param sectionBody body of the section which must be scanned.
     * @param parameterName name of the parameter to be found.
     * @see #getParameter
     */
    private String getParameterValue(String[] sectionBody, String parameterName) {
        return getParameterValue(sectionBody, parameterName, true);
    }

    protected String getParameterValue(String[] sectionBody, String parameterName, boolean parseOptions) {
        if (sectionBody == null
                || parameterName == null
                || parameterName.length() == 0) {
            return null;
        }

        String value;
        String key = parameterName.concat("=");
        String s;
        /* look for the parameter in the sectionBody */
        for (String sectionItem : sectionBody) {
            if (sectionItem != null) {
                sectionItem = sectionItem.trim();
                /*=== This is the only part which actually produces the resulting value ===*/
                if (sectionItem.indexOf(key) == 0) {
                    value = sectionItem.substring(key.length());
                    if (value.indexOf("==") == 0) // if it's a "Final" parameter - 
                    {
                        return value.substring(2);    // no substitutions needed
                    }
                    if (!parseData) // if parsing is disabled - 
                    {
                        return value;     // no substitutions needed
                    }
                    int opt = value.indexOf("??");      // is there an "option" mark ("??")
                    if (parseOptions && opt > 0) {
                        if (enabledExpression(value.substring(opt + 2))) // if the option enabled
                        {
                            return parseString(value.substring(0, opt).trim());  // parse the value and return
                        }
                    } else {
                        return parseString(value.trim());     // parse the value and return
                    }
                } //=== parse $INCLUDE directive and make a recursive call
                else if (sectionItem.indexOf("$INCLUDE") == 0) {
                    s = sectionItem.substring(8).trim();
                    int b = s.indexOf("[");  // look for the section name
                    int e = s.lastIndexOf("]");
                    if (b >= 0 && e > b + 1) // the section name present - get the section
                    {
                        return getParameterValue(
                                getSection(s.substring(0, b), s.substring(b + 1, e)), parameterName, parseOptions);
                    }
                }
            }
        }

        return null;  // parameter not found
    }

    /**
     * Parses a macro-command
     *
     * @param source
     * @deprecated The macro language is not transparent enougth. May be it will
     * be used again, if there will be a necessity. Currently replaced by
     * $INCLUDE directive with parameters defined.
     */
    private String parseMacro(String source) {
        int i = source.indexOf("${");
        int j = source.indexOf("}", i + 1);
        StringTokenizer st = new StringTokenizer(source.substring(i + 2, j), ";");
        String macroName = (st.nextToken()).trim();
        String macro;
        int n = 1;

        while (st.hasMoreTokens()) {
            addParameter("$" + macroName + n++, st.nextToken().trim());
        }
        try {
            macro = getParameter(null, null, macroName);
        } catch (Exception e) {
            return "Macro " + macroName + ": Exception:" + e.getMessage();
        }

        if (macro.length() == 0) {
            macro = "{Macro " + macroName + " not found}";
        }

        return source.substring(0, i) + macro + source.substring(j + 1);
    }

    /**
     * Reads contents of the file.<p>
     *
     * @param fileName full name of the file to read (including path).
     * @return String array. Each element of this array contains a string from
     * the file.
     * @throws java.lang.Exception
     */
    public String[] readFile(String fileName) throws Exception {
//  rm.println("===== Tuner: reading '"+fileName+"'");
        String[] outStr = null;
        boolean cacheCfg = rm.getBoolean("cache_cfg");
        boolean readFromDisk = rm.getBoolean("readFromDisk");
//	boolean cacheCfg = (getParameter(null,null,"cache_cfg") == "true");
//	boolean readFromDisk = (getParameter(null,null,"readFromDisk") == "true");
// rm.println("*** cache:" + cacheCfg + "; readFromDisk:" + readFromDisk);

// Получение из кеша
        if (cacheCfg) {
            outStr = (String[]) rm.getObject(fileName, false);
            if (outStr != null) {
                return outStr;
            }
        } else {
            rm.setObject(fileName, null, true);
        }
// Чтение файла с диска (если задан cfgRootPath)
        if (cfgRootPath.length() > 2) {
            outStr = BasicTuner.readFileFromDisk(fileName, rm);
            if (outStr != null) {
                if (cacheCfg) {
                    rm.println("===== store: '" + fileName + "'");
                    rm.setObject(fileName, outStr, true);
                }
                return outStr;
            }
        }
// Получение данных из класса
        String cfgRootPackage = rm.getString("cfgRootPackage", false);
// String cfgRootPackage=getParameter(null,null,"cfgRootPackage");
// rm.println("Path:" + cfgRootPath + "; Package:" + cfgRootPackage);

        if (cfgRootPackage.length() > 2 && !readFromDisk) {
            String className = fileName.substring(cfgRootPath.length());
            try {
                className = StrUtil.replaceIgnoreCase(className, ".cfg", "");
                className = className.substring(0, 1).toUpperCase()
                        + className.substring(1).toLowerCase();
                className = className.replace('/', '_').replace('\\', '_').replace('.', '_').replace('-', '_');
                className = cfgRootPackage + "." + className;
//      rm.println("===== Tuner - class :'"+className+"'");

                Class cl = Class.forName(className);
                //    rm.println("===== Tuner - newInstance...");
                Cfg cfg = (Cfg) (cl.newInstance());
//     rm.println("===== Tuner - cfg :'"+cfg+"'");
//     rm.println("===== found .class - file");
                outStr = cfg.getContent();
                if (cacheCfg) {
                    rm.println("----- store: '" + fileName + "'");
                    rm.setObject(fileName, outStr, true);
                }
                return outStr;
            } catch (Exception e) { //rm.println(e.toString());
                rm.println("XXXXX Tuner - class not found:'" + className + "'");
                //throw e;
            }
        }
//  rm.println(e.toString());
        rm.println("XXXXX Tuner - could not find cfg.file :'" + fileName + "'");
        IOUtil.writeLogLn(0, "<font color=red><b>ERROR: could not find cfg.file '" + fileName + "' </b></font>", rm);

        return outStr;
    }

    /**
     * Reads contents of the file.<p>
     *
     * @param fileName full name of the file to read (including path).
     * @param rm
     * @return String array. Each element of this array contains a string from
     * the file.
     */
    public static String[] readFileFromDisk(String fileName, ResourceManager rm) {
        return readFileFromDisk(fileName, rm.getString("serverEncoding", false, "Cp1251"), 0);
    }

    /**
     * Reads contents of the file.<p>
     *
     * @param fileName full path to the file to read.
     * @param charset
     * @return String array. Each element of this array contains a string from
     * @param maxLength limits total file length in bytes if > 0. If = 0 - read
     * all file content.
     *
     */
    public static String[] readFileFromDisk(String fileName, String charset, int maxLength) // throws Exception
    {
        //  rm.println("===== Tuner: reading '"+fileName+"'");
        String[] outStr;
        Vector strings = new Vector(200, 100);
        int len = 0;
        try {
//            String charset = ;
            //     rm.println("... opening " + fileName + "; charset: " + charset);
            BufferedReader br
                    = new BufferedReader(
                            new InputStreamReader(
                                    new FileInputStream(fileName), charset), 8192);

            String str;
            while ((str = br.readLine()) != null) { // rm.println(str);
                strings.addElement(str);
                len += str.length();
                if (maxLength > 0 && len > maxLength) {
                    strings.addElement("+++++ File size is more than limit of " + maxLength + " bytes. The rest of file skipped. +++++++ ");
                    break;
                }
            }
            if (strings.size() < 1) {
                return null;
            }
            outStr = new String[strings.size()];
            strings.copyInto(outStr);
            br.close();
            //    rm.println("===== found .cfg - file");
        } catch (Exception e) {
            System.out.println("*** WARNING: FILE NOT FOUND:'" + fileName + "; " + e.toString());
            return null;
        }
        return outStr;
    }

    public void _$SET_PARAMETERS(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
        line = parseString(line);
        boolean prn = false;
        if (line.indexOf("=") > 0) {
            StringTokenizer st = new StringTokenizer(
                    //          line.substring(("$SET_PARAMETERS").length()).trim(), ";");
                    //				 parseString(line.substring(line.indexOf(" "))), ";"); 
                    line.substring(line.indexOf(" ")).trim(), ";");
            boolean global = (line.indexOf("$SET_PARAMETERS_GLOBAL") == 0);
            boolean sess = (line.indexOf("$SET_PARAMETERS_SESSION") == 0);
//        if (sess && session== null)
//        { HttpServletRequest req = (HttpServletRequest) rm.getObject("request");
//	rm.println("***REQUEST:" + req + "; " + rm.toString());
//          session = req.getSession();          
//        }
            String pName;
            String pVal = null;
            while (st.hasMoreTokens()) {
                line = st.nextToken().trim();
                pName = line;
                int j = line.indexOf("=");
                if (j > 0) {
                    pName = parseString(line.substring(0, j).trim());
                    if (pName.equals("WWW")) {
                        prn = true;
                    }
                    pVal = parseString(line.substring(j + 1).trim());
//            if (pVal.equals("null")) pVal = null; //Убрано 13.11.2015. Непонятно, зачем было и используется ли где-то
                }
                if (global && rm != null) {
                    rm.setParam(pName, pVal, true);
                } else {
                    addParameter(pName, pVal);
                }
                if (sess) {
                    setParameterSession(pName, pVal);
                }
//            session.setAttribute(pName, pVal);
                if (prn) {
                    rm.println("*" + pName + ":" + pVal);
                }
            }
        } else {
            int b = line.indexOf("[");  // look for the section name
            int e = line.lastIndexOf("]");
            int j = line.indexOf(" ") + 1;
            String[] paramSection;
            if (b >= 0 && e > b + 1) // the section name found - get the section (recourcive call)
            {
//          rm.println("======= file:"+line.substring(j, b)+"; section:"+line.substring(b+1, e));
                paramSection = getCustomSection(line.substring(j, b), line.substring(b + 1, e));
                if (paramSection != null) {
                    for (j = 0; j < paramSection.length; j++) {
                        if (paramSection[j].trim().length() > 0) {
                            addParameter(paramSection[j].trim(), null);
                        }
                    }
                }
            }
        }
    }

    public void _$INCLUDE(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
        String tmp = line;
        if (enabledOption("debug=on")) {
            System.out.println(line);
        }
        line = parseString(line.substring(8).trim());
        IOUtil.writeLogLn(3, "<b>$INCLUDE </b>" + line, rm);
        int b = line.indexOf("[");  // look for the section name
        int e = line.indexOf("]", b);

        if ((b < 0) && (e < 0)) {

            String fname = line;
            String strFileExtension = "";

            int intLastDotPosition = fname.lastIndexOf(".");
            int intLastSlashPosition = fname.lastIndexOf("/");

            if (intLastDotPosition > intLastSlashPosition) {
                strFileExtension = fname.substring(intLastDotPosition + 1);
            }
            if (strFileExtension.toLowerCase().trim().equals("js")) {
                IOUtil.writeLogLn(3, "<b>$INCLUDE execute Javascript:</b>" + line, rm);
                String jScript = "";
                try {
                    StringBuilder builder = new StringBuilder();
                    String source[] = readFile(cfgRootPath + fname);
                    for (String current : source) {
                        builder.append(current);
                    }
                    jScript = builder.toString();
                    JS_Execute(jScript, sectionLines, out);
//                rm.println("========== JS_Execute file :" + fname);
                } catch (Exception ex) {
                    IOUtil.writeLogLn(5, "<font color=red>" + fname + ": JavaScript file NOT FOUND </font>", rm);
                }
                return;
            }
        }

        String[] subSection = null;

        if (line.indexOf("param:") > 0) // Flash-parameters specified?
        {
            flashParameters = null;
            setFlashParameters((line.substring(line.indexOf("param:") + 6)).trim());
            keepFlashParameters = true;
        }

        if (b >= 0 && e > b + 1) // the section name found - get the section (recourcive call)
        {
            String fname = line.substring(0, b);
//                    if (fname.length() > 0) {
//                        fname = getModFileName(fname,"SIMPLE");
//                        IOUtil.writeLogLn(7, "getModFileName=" + fname, rm);
//                    }
            subSection = getCustomSection(fname, line.substring(b + 1, e), out);

        }
        keepFlashParameters = false;
        if (subSection != null) {
            IOUtil.writeLogLn(7, "<xmp>", rm);
            for (String sectionLine : subSection) {
                IOUtil.writeLogLn(7, sectionLine, rm);
                addLine(sectionLine, sectionLines, null);
            }
            IOUtil.writeLogLn(7, "</xmp>", rm);
//          sectionLines.addElement(subSection[j]);
        } else // SubSection could not be found - put the err.msg
        {
//        rm.println("!!!!! SECTION NOT FOUND OR EMPTY: " + tmp);
//        if (enabledOption("debug=on"))
//          addLine("<br> <b>!!! NOT FOUND: '" + tmp + "'</b> (" + line + ")<br>", sectionLines, out);
            IOUtil.writeLogLn(5, "<font color=red>" + tmp + ": SECTION NOT FOUND OR EMPTY</font>", rm);
        }
        flashParameters = null; // **************** TEST 29.01.03
    }

    public void _$GET_URL(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
        String tmp = line;
        line = parseString(line.substring(8).trim());
        rm.println("+++ $GET_URL: '" + line + "'");
//		 openURL(line, sectionLines);
// sectionLines.addElement("+++ URL: " + line);
        IOUtil.writeLogLn(3, "<font color=red><b>$GET_URL: </b></font>" + line + "...", rm);
        try {
            URL u = new URL(line);
            URLConnection conn = u.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            int ln = 1;
            String url_responce_param = getParameter(null, null, "URL_RESPONCE_PARAM");
            String url_responce = "";
            IOUtil.writeLogLn(5, "<b>RESPONCE:</b>", rm);
            while ((inputLine = in.readLine()) != null) {
                IOUtil.writeLogLn(5, ln++ + ": '" + inputLine + "';", rm);
                sectionLines.addElement(inputLine);
                url_responce += inputLine + "\r\n";
            }
            in.close();
            if (url_responce_param.length() > 0) {
                addParameter(url_responce_param, url_responce);
            } else if (out != null) {
                out.print(url_responce);
                out.flush();
            }
        } catch (Exception e) {
            IOUtil.writeLogLn(0, "<font color=red> get URL " + line + "; ERROR: " + e.toString() + "</font>", rm);
            addLine("ERROR: get URL " + line + "; " + e.toString(), sectionLines, null);
            addParameter("URL_ERROR", e.toString());
        }
    }

    public void _$GET_AUTH_URL(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
        String tmp = line;
        line = parseString(line.substring(13).trim());
        rm.println("+++ $GET_AUTH_URL: '" + line + "'");
        //     openURL(line, sectionLines);
        // sectionLines.addElement("+++ URL: " + line);
//			if (out != null)
//				out.println("+++ URL: " + line + "<br>");
//			out.flush();
        try {
            String authString = "nica:nica";
            String authStringEnc = Base64.encode(authString);

            URL u = new URL(line);
            HttpURLConnection conn;
            if (line.contains("https://")) {
                conn = (HttpsURLConnection) u.openConnection();
            } else {
                conn = (HttpURLConnection) u.openConnection();
            }
            conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
            conn.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                IOUtil.writeLog(5, inputLine, rm);
//	         System.out.println(inputLine);
//			     System.out.println(inputLine.length() + ": " + line.equals("\r\n"));
                addLine(inputLine, sectionLines, out);
//					 sectionLines.addElement(inputLine);
//						if (out != null)
//							out.println(inputLine);
            }
            in.close();
            if (out != null) {
                out.flush();
            }
        } catch (Exception e) {
            addLine("ERROR: $GET_AUTH_URL " + line + "; " + e.toString(), sectionLines, null);
            if (out != null) {
                out.println("ERROR: $GET_AUTH_URL " + line + "; <b>" + e.toString() + "</b><br>");
            }
        }
    }

    public void _$LOG_ERROR(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
        String msg = parseString(line.substring(11).trim());
//                System.out.println(line + "; msg=" + msg);
        if (msg.length() > 1) {
            ((Logger) rm.getObject("logger")).logRequest2DB(rm, "ERROR:" + msg + ".", null);
        }
    }

    public void _$LOG(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
        int lev = 0;
        try {
            lev = Integer.parseInt(line.substring(4, 5));
        } catch (Exception e) {;
        }
        String tmp = line;
        line = parseString(line.substring(5).trim());
        IOUtil.writeLog(lev, line, rm);
    }

    private void _GET_ID(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        String param_name = parseString(line.substring(7).trim());
        if (param_name.length() < 1) {
            param_name = "NEW_ID";
        }
        addParameter(param_name, getNewID());
        addParameter(param_name + "_INT", getNewIntID());
    }

    public void _$USE_DB(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
        System.out.println(" --> " + line);
        try {
            Service serv = (Service) rm.getObject("service");
            String s = line.substring(("$USE_DB").length()).trim();
            serv.useDb(parseString(s));
        } catch (Exception e) {
            String m = e.toString().replaceAll("'", "`");
            addParameter("USE_DB_ERROR",
                    getParameter(null, null, "USE_DB_ERROR")
                    + m + "\n\r");
            addParameter("ERROR", m);
        }
    }

    public void _$CLOSE_DB(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
        System.out.println(" xxx " + line + " NOT SUPPORTED!");
        /*        
        try {
            Service serv = (Service) rm.getObject("service");
            String s = line.substring(("$CLOSE_DB").length()).trim();
            serv.closeDb(parseString(s));
        } catch (Exception e) {
            String m = e.toString().replaceAll("'", "`");
            addParameter("CLOSE_DB_ERROR",
                    getParameter(null, null, "CLOSE_DB_ERROR")
                    + m + "\n\r");
            addParameter("ERROR", m);
        }
/**/
    }

    /**
     * $TIMER без параметра - запомнить время в мс., поставить его в TIMER_START
     * и в TIMER. прежние значения забываются. $TIMER #TIMER# - поставить в
     * TIMER разницу в мс. между тек.временем и #TIMER# $TIMER #TIMER_START#s -
     * поставить в TIMER разницу в сек. между тек.временем и #TIMER_START#
     * (FLOAT, 1 цифра после точки) ПРИ ИСПОЛЬЗОВАНИИ $TIMER #...#s НЕЛЬЗЯ
     * ПОВТОРНО ИСПОЛЬЗОВАТЬ #TIMER#!
     *
     * @param line
     * @param sectionLines
     * @param out
     */
    public void _$TIMER(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
        boolean sec = false;
        String t = "";
        String s = parseString(line.substring(("$TIMER").length()).trim());
//        System.out.println("_$TIMER: s=" + s);
        if (s.isEmpty()) {
            tm = System.currentTimeMillis();
            t = Long.toString(tm);
            addParameter("TIMER_START", t);
//            addParameter("TIMER", t);
        } else {
            long t0 = 0;
            if (s.endsWith("s")) {
                sec = true;
                s = s.replace("s", "");
            }
            try {
                t0 = Long.parseLong(s);
            } catch (Exception e) {
                t0 = 0;
            }
            long t2 = System.currentTimeMillis() - t0;
            if (sec) {
                t = StrUtil.formatDouble(t2 / 1000, 1, "");
            } else {
                t = Long.toString(t2);
            }
        }
        addParameter("TIMER", t);
    }

    public void _$WAIT(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
        String tmp = line;
        line = parseString(line.substring(6).trim());
        rm.print("Tuner: waiting for " + line + "ms... ");
        IOUtil.writeLog("Tuner: waiting for " + line + "ms... ", rm);
        try {
            Thread.sleep((new Long(line)));
        } catch (Exception e) {
        }
        rm.println(" Continue... ");
        IOUtil.writeLogLn(" continue... ", rm);
    }

    public void _$GET_DATA(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);

        int j = line.indexOf("$GET_DATA");
//      rm.println(line);
        Service serv = (Service) rm.getObject("service");
        try {
            serv.getData(parseString((line.substring(j + ("$GET_DATA").length()))).trim());
//        rm.println("============ QUIT GET_DATA ============");
        } catch (Exception e) {
            String m = e.toString().replaceAll("'", "`");
            addParameter("GET_DATA_ERROR",
                    getParameter(null, null, "GET_DATA_ERROR")
                    + m + "\n\r");
            addParameter("ERROR", m);
        }
    }

    public void _$EXECUTE_LOOP(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
        int j = line.indexOf("$EXECUTE_LOOP");
        Service serv = (Service) rm.getObject("service");
        String s = line.substring(j + ("$EXECUTE_LOOP").length()).trim();
//      rm.println(parseString(s));
        try {
            serv.executeLoop(parseString(s));
//        rm.println("============ QUIT EXECUTE_LOOP ============");
        } catch (Exception e) {
            String m = e.toString().replaceAll("'", "`");
            addParameter("EXECUTE_LOOP_ERROR",
                    getParameter(null, null, "EXECUTE_LOOP_ERROR")
                    + m + "\n\r");
            addParameter("ERROR", m);
        }
    }

    public void _$CALL_SERVICE(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
//                IOUtil.writeLogLn("<b>PROCESSING `" + line + "`...</b> parseData=" + parseData + ";", rm);
        int j = line.indexOf("$CALL_SERVICE");
        if (j > 0) {
            addLine(parseString(line.substring(0, j)), sectionLines, out);
        }
//        sectionLines.addElement(parseString(line.substring(0,j)));
        try {
            Service srv = (Service) rm.getObject("service");
            srv.callService(parseString((line.substring(j + ("$CALL_SERVICE").length())).trim()));
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.toString().replaceAll("'", "`");
            while (msg.indexOf("Exception: ") > 0) {
                msg = msg.substring(msg.indexOf("Exception: ") + 10);
            }
            addParameter("CALL_SERVICE_ERROR",
                    getParameter(null, null, "CALL_SERVICE_ERROR")
                    + msg + "\n\r");
            addParameter("ERROR", msg);
            rm.println("========== CALL_SERVICE_ERROR:");
            QueryThread q = (QueryThread) rm.getObject("QueryThread");
            if (q != null) {
                q.logException(e);
            }
        }
    }

    public void _$COPY_FILE(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
        int j = line.indexOf("$COPY_FILE");
        if (j > 0) {
            addLine(parseString(line.substring(0, j)), sectionLines, out);
        }
//        sectionLines.addElement(parseString(line.substring(0,j)));
        IOUtil.writeLogLn("<b>" + line + " </b>", rm);
        try {
            String par = parseString(line.substring(("$COPY_FILE").length() + 1).trim());
//                     System.out.println(line + "; PARSED: " + par);
            String params[] = par.split(";");
            if (params.length > 1) {
                FileContent.copyFile(params[0], params[1]);
            } else {
                throw (new Exception("WRONG DIRECTIVE: " + line + "=>" + par));
            }
        } catch (Exception e) {
            String msg = e.toString().replaceAll("'", "`");
            while (msg.indexOf("Exception: ") > 0) {
                msg = msg.substring(msg.indexOf("Exception: ") + 10);
            }
            addParameter("COPY_FILE_ERROR",
                    getParameter(null, null, "COPY_FILE_ERROR")
                    + msg + "\n\r");
            addParameter("ERROR", msg);
            IOUtil.writeLogLn("<b> ========== COPY_FILE_ERROR:" + e.toString(), rm);
            QueryThread q = (QueryThread) rm.getObject("QueryThread");
            if (q != null) {
                q.logException(e);
            }
        }
    }

    public void _$MOVE_FILE(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
        int j = line.indexOf("$MOVE_FILE");
        if (j > 0) {
            addLine(parseString(line.substring(0, j)), sectionLines, out);
        }
//        sectionLines.addElement(parseString(line.substring(0,j)));
//                IOUtil.writeLogLn("<b>" + line + " </b>", rm);
        try {
            String par = parseString(line.substring(("$MOVE_FILE").length() + 1).trim());
//                     System.out.println(line + "; PARSED: " + par);
            String params[] = par.split(";");
            if (params.length > 1) {
                IOUtil.writeLog("<b> $MOVE_FILE </b>" + params[0] + " => " + params[1], rm);
                FileContent.moveFile(params[0], params[1]);
                IOUtil.writeLogLn("<b> OK! </b>", rm);
            } else {
                throw (new Exception("WRONG DIRECTIVE: " + line + "=>" + par));
            }
        } catch (Exception e) {
            String msg = e.toString().replaceAll("'", "`");
            while (msg.indexOf("Exception: ") > 0) {
                msg = msg.substring(msg.indexOf("Exception: ") + 10);
            }
            addParameter("MOVE_FILE_ERROR",
                    getParameter(null, null, "MOVE_FILE_ERROR")
                    + msg + "\n\r");
            addParameter("ERROR", msg);
            IOUtil.writeLogLn("<b> ========== MOVE_FILE_ERROR:" + e.toString(), rm);
            QueryThread q = (QueryThread) rm.getObject("QueryThread");
            if (q != null) {
                q.logException(e);
            }
        }
    }

    public void _$DELETE_FILE(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
        try {
            String par = parseString(line.substring(("$DELETE_FILE").length() + 1).trim());
//                     System.out.println(line + "; PARSED: " + par);
            String params[] = par.split(";");
            if (params.length > 0) {
                IOUtil.writeLog(3, "xxx DELETE FILE: " + params[0], rm);
                File f = new File(params[0]);
                if (f != null) {
                    f.delete();
                    IOUtil.writeLogLn(3, " - OK.", rm);
                } else {
                    IOUtil.writeLogLn(3, " ERROR: file not found! ", rm);
                }
            } else {
                throw (new Exception("WRONG DIRECTIVE: " + line + "=>" + par));
            }
        } catch (Exception e) {
            String msg = e.toString().replaceAll("'", "`");
            while (msg.indexOf("Exception: ") > 0) {
                msg = msg.substring(msg.indexOf("Exception: ") + 10);
            }
            addParameter("DELETE_FILE_ERROR",
                    getParameter(null, null, "DELETE_FILE_ERROR")
                    + msg + "\n\r");
            addParameter("ERROR", msg);
            IOUtil.writeLogLn("<b> ========== DELETE_FILE_ERROR:" + e.toString(), rm);
            QueryThread q = (QueryThread) rm.getObject("QueryThread");
            if (q != null) {
                q.logException(e);
            }
        }
    }

    public void _$GET_FILE_SIZE(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
        addParameter("FILE_SIZE", "-1");
//                int j = line.indexOf("$GET_FILE_SIZE");
//                if (j > 0) {
//                    addLine(parseString(line.substring(0, j)), sectionLines, out);
//                }
        try {
            String par = parseString(line.substring(("$GET_FILE_SIZE").length() + 1).trim());
//                     System.out.println(line + "; PARSED: " + par);
            String params[] = par.split(";");
            if (params.length > 0) {
                long fileSize = FileContent.getFileSize(params[0]);
                addParameter("FILE_SIZE", Long.toString(fileSize));
            } else {
                throw (new Exception("WRONG DIRECTIVE: " + line + "=>" + par));
            }
        } catch (Exception e) {
            String msg = e.toString().replaceAll("'", "`");
            while (msg.indexOf("Exception: ") > 0) {
                msg = msg.substring(msg.indexOf("Exception: ") + 10);
            }
            addParameter("GET_FILE_SIZE_ERROR",
                    getParameter(null, null, "GET_FILE_SIZE ")
                    + msg + "\n\r");
            addParameter("ERROR", msg);
            System.out.println("========== GET_FILE_SIZE ERROR:");
//				QueryThread q = (QueryThread) rm.getObject("QueryThread");
//				q.logException(e);
            ((QueryThread) rm.getObject("QueryThread")).logException(e);
        }

    }

    /*
     * Группа функций для работы с скриптами на JavaScript на стороне сервера заданных в файлах CFG MOD и ит.д.
     *
     * Выполнение скрипта JAVASCRIPT из строки. 
     *  
     * @param jScript текст скрипта
     * @param out - для заполнения переменных в контекст выполнения скрипта в функции InitScriptEngine
     *
     */
    public void JS_Execute(String jScript, Vector sectionLines, PrintWriter out) {
        InitScriptEngine();
        engine_JS.put("sectionLines", sectionLines);
        try {
            engine_JS.eval(jScript);
        } catch (ScriptException e) {
            e.printStackTrace();
            String msg = e.toString().replaceAll("'", "`");
            while (msg.indexOf("Exception: ") > 0) {
                msg = msg.substring(msg.indexOf("Exception: ") + 10);
            }
            addParameter("$JS_Execute",
                    getParameter(null, null, "JS_ERROR")
                    + msg + "\n\r");
            addParameter("ERROR", msg);
            rm.println("========== JS_Execute:");
            QueryThread q = (QueryThread) rm.getObject("QueryThread");
            if (q != null) {
                q.logException(e);
            }
        }
    }

    /*
     * Группа функций для работы с скриптами на JavaScript на стороне сервера заданных в файлах CFG MOD и ит.д.
     *
     * Выполнение функции из скрипта расположенного в JS/default.js . Для  операторa вида $JS_CALL
     *  
     * @param functionname имя функции
     * @param Parms - параметр, который будет передан в функцию
     * @param out - для заполнения переменных в контекст выполнения скрипта в функции InitScriptEngine
     * @return результат выполнения функции
     *
     */
    public Object JS_invokeFunction(String functionName, String Params, PrintWriter out) throws Exception {
        InitScriptEngine();
        System.out.println("Java Invoke: " + functionName + " params:" + Params);
        Object result = null;
        if (engine_JS instanceof Invocable) {
            Invocable invEngine = (Invocable) engine_JS;
            result = invEngine.invokeFunction(functionName, Params);
        } else {
        }
        return result;
    }

    /*
     * Группа функций для работы с скриптами на JavaScript на стороне сервера заданных в файлах CFG MOD и ит.д.
     *
     * Выполнение строки javascript. 
     *  
     */
    public void _$JS(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
        String js = parseString(line.substring(3).trim());
        IOUtil.writeLogLn(5, "<font color=green>$JS " + js + "</font>", rm);

        try {
            JS_Execute(js, sectionLines, out);
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.toString().replaceAll("'", "`");
            while (msg.indexOf("Exception: ") > 0) {
                msg = msg.substring(msg.indexOf("Exception: ") + 10);
            }
            addParameter("ERROR", msg);
            QueryThread q = (QueryThread) rm.getObject("QueryThread");
            if (q != null) {
                q.logException(e);
            }
        }
    }

    /*
     * Группа функций для работы с скриптами на JavaScript на стороне сервера заданных в файлах CFG MOD и ит.д.
     *
     * Интерфейс к IOUtil.writelog как метод BasicTuner. Для возможности писать в лог из скрипта через метод BT
     *
     */
    public void WriteLog(int Level, String msg) {
        IOUtil.writeLog(Level, msg, rm);
    }

    /*
     * Группа функций для работы с скриптами на JavaScript на стороне сервера заданных в файлах CFG MOD и ит.д.
        
     * Спискок акдивных engine
     *
     */
    private static void ListEngines() {
        ScriptEngineManager manager = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = manager.getEngineFactories();
        System.out.print("Доступные script engine :");
        for (ScriptEngineFactory factory : factories) {

            System.out.print("    " + factory.getEngineName() + ": ");
            System.out.print(" Ver=" + factory.getEngineVersion());
            System.out.print(", LangName:" + factory.getLanguageName());
            System.out.print(", LangVer:" + factory.getLanguageVersion());
            System.out.print(", Extention^" + factory.getExtensions());
            System.out.print(", MimeTypes:" + factory.getMimeTypes());
            System.out.print(", Name:" + factory.getNames());
        }
    }

    /*
     * Группа функций для работы с скриптами на JavaScript на стороне сервера заданных в файлах CFG MOD и ит.д.
     *
     * Установка переменных в контексте выполнения скрипта: 
     * prm :parameters , dbUtil, out, rm, BT - basicTuner
     * хватило бы и одного BT. Остальное для сокращения записи
     */
    public void InitScriptEngine() {
        engine_JS.put("out", rm.getObject("outWriter", false));
        if (engine_JS.get("prm") != null) {
            return;
        }

        engine_JS.put("prm", parameters);
        Service serv = (Service) rm.getObject("service");
        DBUtil dbUtil = serv.dbUtil;
        engine_JS.put("dbUtil", dbUtil);
        engine_JS.put("rm", rm);
        engine_JS.put("BT", this);
        String jScript = "";
        try {
            String source[] = readFile(cfgRootPath + "JS/default.js");
            for (String current : source) {
                jScript += current + "\n";
            }
//            rm.println("======Load JS/DEFAULT.JS :\n");
        } catch (Exception e) {
            rm.println("====== JS error read DEFAULT.JS :\n");
        }
//        rm.println("======JS/DEFAULT.JS :\n");
//        rm.println(jScript);

        try {
            engine_JS.eval(jScript);
        } catch (ScriptException e) {
            e.printStackTrace();
            String msg = e.toString().replaceAll("'", "`");
            while (msg.indexOf("Exception: ") > 0) {
                msg = msg.substring(msg.indexOf("Exception: ") + 10);
            }
            addParameter("$JS_ERROR",
                    getParameter(null, null, "JS_ERROR")
                    + msg + "\n\r");
            addParameter("ERROR", msg);
            rm.println("========== JS_ERROR process DEFAULT.JS :");
            QueryThread q = (QueryThread) rm.getObject("QueryThread");
            if (q != null) {
                q.logException(e);
            }
        }

    }

    public void _$STORE_PARAMETERS(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
        storeParameters();
    }

    public void _$RESTORE_PARAMETERS(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
        restoreParameters();
    }

    public void _$PRINT(String line, Vector sectionLines, PrintWriter out, String sectionName) {
        logExecutedLine(line, out, sectionName);
        line = parseString(line.substring(6).trim());
        rm.println(line);
    }

    private int _$JS_BLOCK(String[] source, int i, String line, Vector sectionLines, PrintWriter out, String sectionName) {
        boolean parseJS = true;
        if (line.toUpperCase().indexOf("NOPARSE")>-1){
            parseJS = false;
        }
        logExecutedLine(line, out, sectionName);
        String js = "";
        for (i++; i < source.length; i++) {
            line = (source[i].trim());
            if (parseJS){
                line = checkConditionAndParse(line);
            }
            if ((line.indexOf("$JS_END") == 0) || (line.indexOf("$JS_}") == 0) || (line.indexOf("$JS}") == 0)) {
                break;
            };
            if (line.indexOf("[END]") == 0) {
                break;
            };
            js += line + "\r";
        }
//        IOUtil.writeLogLn(2, "<font color=green>JAVASCRIPT BLOCK: <br>" + js.replace("\r", "<br>") + "</font>", rm);
        try {
            JS_Execute(js, sectionLines, out);
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.toString().replaceAll("'", "`");
            while (msg.indexOf("Exception: ") > 0) {
                msg = msg.substring(msg.indexOf("Exception: ") + 10);
            }
            addParameter("ERROR", msg);
            QueryThread q = (QueryThread) rm.getObject("QueryThread");
            if (q != null) {
                q.logException(e);
            }
        }
        return i;
    }

    private void logExecutedLine(String line, PrintWriter out, String sectionName) {
        if (!traceProcessing) {
            return;
        }
        if ((out != null) & !(sectionName.equals("report_"))) {
            String outline = parseString(line);
            outline = outline.replace("<", "&lt;");
            outline = outline.replace(">", "&gt;");
//            WriteLog(3, "Exec[" + sectionName + "]" + outline + "<br>");
            WriteLog(5, "<font color=darkblue>Exec" + outline + "</font><br>");

        }
    }

    String checkConditionAndParse(String result) {
        int optIndex;
        if ((optIndex = result.indexOf("??")) > 0) // check if the option enabled
        {
//			rm.println("result:" + result);
//			rm.println(result.substring(optIndex + 2).trim());
//			rm.println(" enabled:" + enabledExpression(result.substring(optIndex + 2).trim()));
            if (enabledExpression(result.substring(optIndex + 2).trim())) {
                result = result.substring(0, optIndex).trim();
            } else {
                result = "";    // ignore line, if the option disabled
            }
        }
        if (result.length()>0){
            result = parseString(result);
        }
        return result;
    }

}
