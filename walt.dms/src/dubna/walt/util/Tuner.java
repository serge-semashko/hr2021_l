package dubna.walt.util;

import java.io.PrintWriter;
import java.util.*;
import javax.servlet.http.*;

/**
 * The main purpuse of this class is customization of a plain text template
 * (section) of the configuration file (array). Tuner understands a simple
 * "customization" language which can be used in the template.<p>
 *
 * <b>Main functionality:</b>:
 * <ul>
 * <li>substitution of parameters values;
 * <li>excluding of optional lines using simple boolean expressions;
 * <li>including into one section other section(s) from the same or from another
 * configuration file.
 * <li>output to the client a customized section;
 * </ul>
 *
 * The values for the parameters can be set in the Tuner object in different
 * ways: <ul>
 * <li>using the constructor;
 * <li>within the [parameters] section of the configuration file;
 * <li>in run-time (from JAVA code or from template).
 * </ul><p>
 *
 * <b>Advanced functionality:</b>:
 * <ul>
 * <li>call a sub-service while outputs a customized section;
 * <li>set parameter(s) while a section processing;
 * <li>debug print on the System.out while a section processing;
 * </ul>
 * <p>
 *
 * <b>The Tuner's language:</b>
 * <ul>
 * <li> #paramName# - substitution of a parameter with it's value;
 * <li> &lt;the body of the line> ??&lt;BOOLEAN EXPRESSION> - conditional
 * including of a line;
 * <li> $CALL_SERVICE c=cfgFileName [; param1=value1 [; param2=value2] ...] - a
 * sub-service call with possibility to define some parameters for the
 * sub-service;
 * <li> $SET_PARAMETERS param1=value1 [; param2=value2] ... - set paremeters
 * from a section;
 * <li> $INCLUDE [&lt;fileName>.]&lt;[sectionName]> [param: param1=value1 [;
 * param2=value2]...] - include a section from the same or from another cfg.
 * file with possibility to define some parameters for the section included;
 * </ul>
 *
 * The following <b>operators</b> can be used in the <b>BOOLEAN
 * EXPRESSION:</b><ul>
 * <li>- "!" - negotiation;
 * <li>- "&" - AND operator;
 * <li>- "|" - OR operator;
 * </ul>
 * An expression is processed from left to right until its result is known.<br>
 * <b>NOTE:</b> There is no brackets processing!
 * <p>
 *
 * <table border=1 width=95% cellspacing=0 cellpadding=5>
 * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor"><td>
 * <B> EXAMPLE: </B></td></tr>
 * <TR BGCOLOR="#EEEEFF" CLASS="TableSubHeadingColor"><td>
 * <b>Confuration file:</b></td></tr><tr><td>
 * [parameters]<br>
 * param1=This is the param1 value <br>
 * param2=PARAM2<br>
 * [end]
 * <p>
 *
 * [report]<br>
 * &lt;html>&lt;body><br>
 * The value of the parameter param1 is: '#param1#' ??param1<br>
 * The parameter param1 is not defined ??!param1
 * <p>
 *
 * $INCLUDE [sct1] &nbsp; param: p2=#param2#; p4=Par4;  <br>
 * [end]
 * <p>
 *
 * [sct1] &nbsp; param: p1; p2=defPar2; p3=defPar3; p4<br>
 * ======= the [sct1] section (some comments) ======== ??<br>
 * p1='#p1#' p2='#p2#' p3='#p3#' p4='#p4#'<br>
 * p1 is not defined! ??!p1 p2 equals PARAM2! ??p2=PARAM2
 * <br>
 * [end]</td></tr>
 *
 * <TR BGCOLOR="#EEEEFF" CLASS="TableSubHeadingColor"><td>
 * <b>Customized "[report]" section:</b>
 * </td></tr><tr><td>
 *
 * &lt;html>&lt;body><br>
 * The value of the parameter param1 is: 'This is the param1 value'
 * <p>
 *
 * p1='' p2='PARAM2' p3='defPar3' p4='Par4'<br>
 * p1 is not defined! p2 equals PARAM2!
 * </td></tr></table>
 * <hr>
 * <P>
 * &nbsp<P>
 *
 * @see BasicTuner
 */
public class Tuner extends BasicTuner {

    /**
     * Class constructor.
     * <P>
     *
     * @param parameters
     * @param cfgFileName name of the configuration file to be used.
     * @param cfgRootPath base path to the configuration files.
     * @param rm the current ResourceManager.
     * @throws java.lang.Exception
     *
     */
    public Tuner(String[] parameters,
            String cfgFileName,
            String cfgRootPath,
            ResourceManager rm) throws Exception {
        this.parameters = parameters;
        this.rm = rm;
        this.cfgRootPath = cfgRootPath;
        if (cfgFileName != null && cfgFileName.length() > 0) {
            this.cfgFileName = cfgFileName;
            String f = getModFileName(cfgFileName, "");
            cfg = readFile(cfgRootPath + f);
        }
        try {
            HttpServletRequest req = (HttpServletRequest) rm.getObject("request", false);
            session = req.getSession();
//		session.setMaxInactiveInterval( 1);      
        } catch (Exception e) {
        }
    }

    /**
     * Class constructor.
     * <P>
     *
     * @param parameters
     * @param cfg
     * @param rm the current ResourceManager.
     * @throws java.lang.Exception
     *
     */
    public Tuner(String[] parameters,
            String[] cfg,
            ResourceManager rm) throws Exception {
        this.parameters = parameters;
        this.rm = rm;
        this.cfg = cfg;
        try {
            HttpServletRequest req = (HttpServletRequest) rm.getObject("request", false);
            session = req.getSession();
        } catch (Exception e) {
        }
    }

    /**
     * Get the value of the parameter.<P>
     *
     * Makes the processing of the parameter (processes nested parameters)
     * .<p>
     *
     * The value for the parameter will be searched in the following places:
     * <ul>
     * <li>"Flash-parameters" (used in the "$INCLUDE" - directive with
     * parameters);
     * <li>dynamic Tuner's parameters (String[] parameters);
     * <li>parameters, defined in the [parameters] section of the config. file;
     * <li>in the current RresourceManager;
     * </ul><p>
     *
     * @param paramName name of the parameter.
     * @return the value of the parameter or an empty string if the parameter is
     * not defined.
     * @see getParameter(String sectionName, String paramName)
     */
    public String getParameter(String paramName) {
        return getParameter(null, null, paramName);
    }

    /**
     * Get the value of the parameter.<P>
     *
     * Makes the processing of the parameter (processes nested parameters)
     * .<p>
     *
     * The value for the parameter will be searched in the following places:
     * <ul>
     * <li>"Flash-parameters" (used in the "$INCLUDE" - directive with
     * parameters);
     * <li>dynamic Tuner's parameters (String[] parameters);
     * <li>parameters, defined in the [parameters] section of the config. file;
     * <li>in the current RresourceManager;
     * </ul>
     *
     * @param sectionName name of the section of the configuration file,
     * where to search the parameter.
     * @param paramName name of the parameter.
     * @return the value of the parameter or an empty string if the parameter is
     * not defined.
     * @see getParameter(String paramName)
     */
    public String getParameter(String sectionName, String paramName) {
        return getParameter(null, sectionName, paramName);
    }

    /**
     * Get the value of the parameter without any processing.<P>
     *
     * @param sectionName name of the section of the configuration file,
     * where to search the parameter.
     * @param paramName name of the parameter.
     * @return the parameter value as it is defined in the Tuner without any
     * substitution<BR>
     * or an empty string if the parameter is not defined.
     */
    public String getFinalParameter(String sectionName, String paramName) {
        boolean b = setParseData(false);
        String s = getParameter(null, sectionName, paramName);
        setParseData(b);
        return s;
    }

    /**
     * Get the value of a parameter. 
     * Look only in dynamic parameters, setted by addParameter(...) in parameters array
     * Do not search in section [parameters], Flash parameters, session variables, ResourceManager parameters
     *
     * @param parameterName the name of the section.
     * @return String. Contains the parameter's value, if the parameter has been
     * found and an empty string otherwise.
     *
     */
    public String getDynamicParameter(String parameterName) {
            // Than look in the dynamic parameters Only
        return getParameterValue(parameters, parameterName, false);
    }


    
    /**
     * Get the value of the parameter without any processing.<P>
     *
     * @param paramName name of the parameter.
     * @return the parameter value as it is defined in the Tuner without any
     * substitution<BR>
     * or an empty string if the parameter is not defined.
     */
    public String getFinalParameter(String paramName) {
        return getFinalParameter(null, paramName);
    }

    /**
     *
     * @return
     */
    public ArrayList<String> getSectionsList(boolean comments) {
        ArrayList<String> al = new ArrayList<String>();
        for (String line : cfg) {
            if (line.indexOf('[') == 0 && line.indexOf(']') > 0 && !line.contains("[end]")) {
                if(!comments)
                    line = line.substring(1, line.indexOf("]"));
                al.add(line);
            }
        }
        return al;
    }

    /**
     * Adds a section to the configuration file.<p>
     *
     * @param sectionBody Vector, containing the body of the section to be
     * added, including the [sectionName] and [end] lines.
     */
    public void addSection(Vector sectionBody) {
        if (sectionBody == null || sectionBody.size() < 3) {
            return;
        }

        int lOld = 0;
        if (cfg != null) {
            lOld = cfg.length;
        }
        int lNew = sectionBody.size();
        String[] newCfg = new String[lOld + lNew];

        for (int i = 0; i < lNew; i++) {
            newCfg[i] = (String) sectionBody.elementAt(i);
        }

        for (int i = 0; lOld > 0 && i < lOld; i++) {
            newCfg[i + lNew] = cfg[i];
        }

        cfg = newCfg;
        return;
    }

    /**
     * Adds a section to the configuration file.<p>
     *
     * @param sectionName name of the section to be added,
     * @param sectionBody array, containing the body of the section to be added,
     * without the [sectionName] and [end] lines.
     */
    public void addSection(String sectionName, String[] sectionBody) {
        if (sectionBody == null || sectionBody.length < 1) {
            return;
        }

        int lOld = cfg.length;
        int lNew = sectionBody.length + 2;
        String[] newCfg = new String[lOld + lNew];

        newCfg[0] = "[" + sectionName + "]";
        for (int i = 1; i < lNew - 1; i++) {
            newCfg[i] = sectionBody[i - 1];
        }
        newCfg[lNew - 1] = "[end]";

        for (int i = 0; i < lOld; i++) {
            newCfg[i + lNew] = cfg[i];
        }

        cfg = newCfg;
        return;
    }

    /**
     * Outputs the customized section to the PrintWriter.<p>
     *
     * During output of the section processes "$CALL_SERVICE" directive.<br>
     * <b>Syntax:</b><br>
     * $CALL_SERVICE c=cfgFileName [; paramName=paramValue;
     * [paramName=paramValue]...]
     * <p>
     * <b>Depricated syntax:</b><br>
     * $CALL_SERVICE cfgFileName [paramName=paramValue
     * [paramName=paramValue]...]
     * <p>
     *
     * @param sectionName name of the section of the configuration file (without
     * brackets "[]").
     * @param out
     * @throws java.lang.Exception
     * @see #getCustomSection
     * @see #callService
     */
    public void outCustomSection(String sectionName, PrintWriter out) throws Exception {
        getCustomSection(null, sectionName, out);
    }

    /**
     * Returns a customized section from a template file.<p>
     *
     * Processes each line in the section, substitutes the parameters with their
     * values, includes/excludes optional lines (marked with '??') depending on
     * the current parameters set, processes "$INCLUDE", "$SET_PARAMETERS",
     * "$PRINT" directives.<p>
     *
     * @param sectionName name of the section (without square brackets "[]"). If
     * it is null - the first section of the configuration file will be
     * returned.
     * @return String array, containing the customized section body.
     *
     */
    public String[] getCustomSection(String sectionName) {
        int i = sectionName.trim().indexOf("[");
        if (i > 0) {
            return getCustomSection(sectionName.substring(0, i), sectionName.substring(i));
        }

        return getCustomSection(null, sectionName);
    }

    /**
     *
     * @param sectionName
     * @return
     */
    public String getCustomSectionAsString(String sectionName) {
        String[] sa = getCustomSection(sectionName);
        String s = "";
        if (sa != null) {
            for (String sa1 : sa) {
                s += sa1 + " ";
            }
        }
        return s;
    }

    /**
     * Returns the parameter value as a non-negative "int" value.<p>
     *
     * @param paramName name of the parameter.
     * @return the integer value of the parameter or -99999 if the parameter is
     * not defined or its value can not be interpreted as non-negative
     * integer.<BR>
     */
    public int getIntParameter(String paramName) {
        return getIntParameter(null, paramName, -1);
    }

    /**
     * Returns the parameter value as a non-negative "int" value.<p>
     *
     * @param sectionName name of the section, containing the parameter.
     * @param paramName name of the parameter.
     * @return the integer value of the parameter or -1 if the parameter is not
     * defined or its value can not be interpreted as non-negative integer.<BR>
     */
    public int getIntParameter(String sectionName, String paramName) {
        return getIntParameter(sectionName, paramName, -1);
    }

    /**
     * Returns the parameter value as a non-negative "int" value.<p>
     *
     * @param sectionName name of the section, containing the parameter.
     * @param paramName name of the parameter.
     * @param defaultValue
     * @return the integer value of the parameter or defaultValue if the
     * parameter is not defined or its value can not be interpreted as
     * non-negative integer.<BR>
     */
    public int getIntParameter(String sectionName, String paramName, int defaultValue) {
        int i = defaultValue;
        try {
            i = Integer.parseInt(getParameter(sectionName, paramName));
        } catch (Exception e) {
            return defaultValue;
        }
        return i;
    }

    /**
     * Returns the parameter value as a non-negative "int" value.<p>
     *
     * @param paramName name of the parameter.
     * @return the integer value of the parameter or -99999 if the parameter is
     * not defined or its value can not be interpreted as non-negative
     * integer.<BR>
     */
    public long getLongParameter(String paramName) {
        return getLongParameter(null, paramName, -1);
    }

    /**
     * Returns the parameter value as a non-negative "int" value.<p>
     *
     * @param sectionName name of the section, containing the parameter.
     * @param paramName name of the parameter.
     * @return the integer value of the parameter or -1 if the parameter is not
     * defined or its value can not be interpreted as non-negative integer.<BR>
     */
    public long getLongParameter(String sectionName, String paramName) {
        return getLongParameter(sectionName, paramName, -1);
    }

    /**
     * Returns the parameter value as a non-negative "int" value.<p>
     *
     * @param sectionName name of the section, containing the parameter.
     * @param paramName name of the parameter.
     * @param defaultValue
     * @return the integer value of the parameter or defaultValue if the
     * parameter is not defined or its value can not be interpreted as
     * non-negative integer.<BR>
     */
    public long getLongParameter(String sectionName, String paramName, int defaultValue) {
        long i = defaultValue;
        try {
            i = Long.parseLong(getParameter(sectionName, paramName));
        } catch (Exception e) {
            return defaultValue;
        }
        return i;
    }

    /**
     * Returns a copy of the Tuner's array of parameters.<p>
     *
     * @return
     */
    public String[] getParameters() {
        int l = parameters.length;
        String[] p = new String[l];
        for (int i = 0; i < l; i++) {
            p[i] = parameters[i];
        }
        return p;
    }

    /**
     * Sets the Tuner's array of parameters.<p>
     * Leaves the original array unchaged
     *
     * @param params
     * @return the previous array of parameters;
     *
     */
    public String[] setParameters(String[] params) {
        String p[] = parameters;
        int l = params.length;
//  rm..println("Set parameters: l=" + l);
        parameters = new String[l];
        for (int i = 0; i < l; i++) {
            parameters[i] = params[i];
        }
        return p;
    }

    /**
     * Deletes a parameter from the Tuner's parameters array.<p>
     *
     * If there is no such a parameter - does nothing.<BR>
     *
     * @param paramName name of the parameter to be deleted.
     */
    public void deleteParameter(String paramName) {
        if (parameters == null) {
            return;
        }
        String p = paramName + "=";

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].startsWith(p)) {
                parameters[i] = "";
                break;
            }
        }
    }

    /**
     * Deletes a parameter from the Tuner's parameters array.<p>
     *
     * If there is no such a parameter - does nothing.<BR>
     *
     */
    public void deleteEmptyParameters() {
        if (parameters == null) {
            return;
        }

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].indexOf("=NONE") > 0) {
                parameters[i] = "";
            }
        }
        if (cfg == null || cfg.length < 2) {
            return;
        }
        for (int i = 0; i < cfg.length; i++) {
            if (cfg[i].indexOf("[end]") > 0) {
                break;
            }
            if (cfg[i].indexOf("=NONE") > 0) {
                cfg[i] = "";
            }
        }
    }

    /**
     * Outputs current Tuner's parameters to the PrintWriter
     * .<p>
     *
     * @param out the PrintWriter to be used.
     */
    public void logParameters(PrintWriter out) {
        out.println("<hr><b>===== Parameters =====</b><xmp>");
        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] != null && parameters[i].length() > 0) {
                    out.println(i + ": " + parameters[i]);
                }
                //    out.println("-------------------------");
            }
        } else {
            out.println("====== No parameters! ======");
        }

        out.println("</xmp><hr><b>====== Session variables ======</b><xmp>");
        Enumeration e = session.getAttributeNames();
        String val = "";
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            val = session.getAttribute(name).toString();
            out.println(name + ": " + val);
        }
        out.println("</xmp><hr>");
    }

}
