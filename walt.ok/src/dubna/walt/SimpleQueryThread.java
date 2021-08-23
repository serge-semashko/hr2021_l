package dubna.walt;

import java.io.*;
import java.util.*;
import javax.servlet.ServletInputStream;
import javax.servlet.http.*;

import dubna.walt.util.*;
import dubna.walt.service.Service;

/**
 *
 * @author serg
 */
public class SimpleQueryThread implements QueryThread {

    /**
     *
     */
    protected HttpServletResponse response;

    /**
     *
     */
    protected HttpServletRequest request;

    /**
     *
     */
    protected OutputStream outStream = null;

    /**
     *
     */
    protected PrintWriter outWriter = null;

    /**
     *
     */
    public ServletInputStream inpStream = null;

    /**
     *
     */
    protected Tuner cfgTuner = null;

    /**
     *
     */
    protected ResourceManager rm;

    /**
     *
     */
    protected Service srv = null;
//protected static ResourceManager rm_Global;

    /**
     *
     */
    protected String cfgFileName = null;
    protected String c = null;

    /**
     *
     */
    protected Vector params;

    /**
     *
     */
    protected Cookie[] cookies = null;

    /**
     *
     */
    protected String queryLabel = "";

    /**
     *
     */
    protected String of = "h";

    /**
     *
     */
    protected String serverEncoding;

    /**
     *
     */
    protected String clientEncoding;

    /**
     *
     */
    public boolean headOnly = false;

    /**
     *
     */
    public DBUtil dbUtil = null;

    /**
     *
     */
    public long startTm = 0;

    /**
     *
     */
    public Hashtable ht_locked_params = null;

    /**
     *
     */
    public String excludeFromLog = "";

     /**
     * Тип запроса (ставится DIRECT, AJAX или INTERNAL)
     */
    public String requestType = "";
    
    /**
     * Режим проверки 
     * STRICT - принудительная установка расширения имени файлапри внешних запросах в соответствии с типом запроса 
     */
    public String securityMode = "SIMPLE";
//        public String securityMode = "STRICT";

    
    /**
     *
     */
    static public final String KEY_MULTIPART = "multipart/form-data";
//static public final String KEY_NAME = "Content-Disposition: form-data; name=\"";

    /**
     *
     */
    static public final String KEY_CONTTYPE = "Content-Type: ";

    /**
     *
     */
    static public final String KEY_FILENAME = "filename=\"";

    /**
     *
     */
    static public final String MULTIPART_SEPARATOR = "¦";

    /**
     *
     */
    static public final String EOL = "\r\n";

    /**
     *
     */
    static public final String KEY_NAME = "name=\"";

    public void init(ResourceManager rm) throws Exception {
        request = (HttpServletRequest) rm.getObject("request");
        response = (HttpServletResponse) rm.getObject("response");
//  rm_Global = (ResourceManager) rm.getObject("rm_Global");
        this.rm = rm;
        this.queryLabel = rm.getString("queryLabel");
        serverEncoding = rm.getString("serverEncoding", false, "Cp1251");
        clientEncoding = rm.getString("clientEncoding", false, "Cp1251");
        rm.putObject("QueryThread", this);
        securityMode = rm.getString("securityMode", false, "SIMPLE");   // "STRICT";
        if (outWriter == null) {
            inpStream = request.getInputStream();
            outStream = response.getOutputStream();
//    outWriter = new PrintWriter(outStream);
            try {
                outWriter = new PrintWriter(new OutputStreamWriter(outStream, clientEncoding));
            } catch (Exception e) {
                rm.println("******* EXCEPTION:" + e.toString());
                outWriter = new PrintWriter(new OutputStreamWriter(outStream, clientEncoding));
            }
        }

        rm.setObject("outWriter", outWriter);
        rm.setObject("outStream", outStream);
        params = new Vector(200, 40);
        excludeFromLog = rm.getString("excludeFromLog", false,"");
    }

    /**
     *
     *
     */
    @Override
    public void start() // throws Exception
    {
        try {
            startTm = System.currentTimeMillis();
            parseRequest(request);
            getAddresses();
            makeTuner();
            setContentType();
            dbUtil = makeDBUtil();

            //    rm.setObject("cfgTuner", cfgTuner);
            if (!headOnly) {
                if (cfgTuner.enabledOption("ResetLog=true")) {
                    IOUtil.clearLogFile(rm);
                }
//				if (!cfgTuner.enabledOption("KeepLog=true")
//				  || cfgTuner.enabledOption("ClearLog=true"))
//					IOUtil.clearLogFile(rm);

                //       writeHttpHeaders();
//                if (cfgTuner.getParameter("c").contains("showLog")) {
//                    rm.setParam("log", "false");
//                }

                if (validateUser()) {
                    logQuery();
                    validateParameters();
                    startService();
                } else {
                    System.out.println(rm.getString("queryLabel")
                            + " [" + Fmt.fullDateStr(new java.util.Date()) + "] "
                            + cfgTuner.getParameter("ClientIP")
                            + ": NOT LOGGED,"
                            + ": " + cfgTuner.getParameter("c")
                    );
                    cfgTuner.outCustomSection("not identified", outWriter);
                }
            }
            logQuery(null);
        } catch (Exception e) {
            logException(e);
            logQuery(e);
        } finally {
//            finished();
            if (outWriter != null) {
                outWriter.flush();
                outWriter.close();
                try {
                    outStream.close();
                    //         inpStream.close();
                } catch (Exception e) {
                    /* don't warry! */ }
            }
            finish();
        }

        logRequestFinished();
    }

//    public void finished(){
//        RequestQueue rq = (RequestQueue) rm.getObject("RequestQueue", true, true);
//        rq.queryFinished(rm.getString("queryLabel"));
//    }
    
    /**
     * ********************** Разбор параметров запроса ******************
     */
    /**
     * Вытаскивание из request набора путей и добавление их в параметры:
     * ContextPath - request.getContextPath() ServletPath protocol ServerName
     * Host ServerPath ClientIP user_agent Windows=YES MSIE=YES
     *
     * Кроме того, из .properties копируются параметры: imgPath (def.= /images/)
     * jsPath (def.= /js/) cssPath (def.= /css/)
     */
    public void getAddresses() {

        /* Вытаскивание данных из request */
        String cp = request.getContextPath();
        params.addElement("ContextPath=" + cp);
//        params.addElement("ServletPath=" + request.getContextPath() + request.getServletPath());
        if(rm.getString("ServletPath", false,"").isEmpty()) {
            rm.setObject("ServletPath", request.getContextPath() + request.getServletPath(), true);
        }

        params.addElement("protocol=" + request.getScheme());
        String host = request.getHeader("Host");

        String serverName = request.getServerName().toLowerCase();
        if (serverName.indexOf(".") > 0) {
            serverName = serverName.substring(0, serverName.indexOf("."));
        }
        params.addElement("ServerName=" + serverName);

        if(host != null && !host.equals("null")){
            if (host.indexOf(":") > 0) {
                params.addElement("ServerPort=" + host.substring(host.indexOf(":") + 1));
                params.addElement("Host=" + host.substring(0, host.indexOf(":")));
            } else {
                params.addElement("Host=" + host);
            }
    //        params.addElement("ServerPath=" + request.getScheme() + "://" + host); 
            // пусть каждому запросу ставится путь к серверу с тем урлом, по которому обратились 19.12.2017
            //if(rm.getString("ServerPath", false,"").isEmpty() || rm.getString("ServerPath", false,"").contains("localhost") ) {
                rm.setObject("ServerPath", request.getScheme() + "://" + host, true);
            //}
        }
        else {
            params.addElement("Host=NULL");            
        }
        

        String clientIP = request.getRemoteAddr();
        while (clientIP.indexOf("/") == 0) {
            clientIP = clientIP.substring(1);
        }
        params.addElement("ClientIP=" + clientIP);
        rm.putString("clientIP", clientIP);

        String ag = request.getHeader("user-agent");
//Chrome:	 h_user-agent=Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/532.5 (KHTML, like Gecko) Chrome/4.1.249.1059 Safari/532.5
//IE6:  	 h_user-agent=Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)
//Mozilla: h_user-agent=Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.9.0.4) Gecko/2008102920 Firefox/3.0.4 

        if (ag != null && ag.length() > 1) {
            if (ag.indexOf("Win") > 0) {
                params.addElement("Windows=YES");
            }
            if (ag.indexOf("MSIE") > 0) {
                params.addElement("MSIE=YES");
                params.addElement("user_agent=MSIE");
            } else if (ag.indexOf("Chrome") > 0) {
                params.addElement("user_agent=Chrome");
            } else if (ag.indexOf("Firefox") > 0) {
                params.addElement("user_agent=Firefox");
            }

        }

        /* Копирование из .properties */
        params.addElement("imgPath=" + rm.getString("imgPath", false, cp + "/images/"));
        params.addElement("jsPath=" + rm.getString("jsPath", false, cp + "/js/"));
        params.addElement("cssPath=" + rm.getString("cssPath", false, cp + "/css/"));

    }

    /**
     * Разбор запроса: - вытаскивание параметров (headers) из request-a -
     * вытаскивание собственно параметров из GET или POST - запроса,
     *
     * @param request
     * @throws Exception
     */
    public void parseRequest(HttpServletRequest request) throws Exception {
        /* Вытаскивание параметров (headers)  из request-a */
        getHeaders();

        /* Вытаскивание собственно параметров запроса  */
        Hashtable ht = new Hashtable(100);  // working temporary Hashtable
        ht_locked_params = new Hashtable(10);  // locked parameters names
        Enumeration e;
        String name;
        String val;
        String method = request.getMethod();

        String s = getMultiPartData(request, ht); // преобразование параметров MultiPart запроса - в URL-строку
//  rm.println("1:getMultiPartData: '" + s + "'");
        if (s.length() == 0) {  // Если не было MultiPart-параметров, то берем параметры из request-а
            e = request.getParameterNames();
            while (e.hasMoreElements()) {
                name = (String) e.nextElement();
                String[] sa = request.getParameterValues(name);  // все значения параметра (будут слеплены через запятую)

                if (!ht.containsKey(name)) {
                    for (String v : sa) {
                        if (v.length() > 0 && !name.equals("tm")) {
                            val = v;
                            if (method.equals("GET")) {
//                                val = new String(val.getBytes("ISO-8859-1"), clientEncoding);
                            }
                            s = s + "&" + name + "=" + val;
                            registerParameter(name, val, ht);
//            rm.println("... " + name + "='" + val + "'");
                        }
                    }
                }
            }
        }

        if (s.length() == 0) {  // параментов в запросе нет - берем строку по умолчанию
            s = rm.getString("DefaultParameters", false);
            StringTokenizer st = new StringTokenizer(s, "&");
            int i = 0;
            while (st.hasMoreTokens()) {
                name = st.nextToken();
                i = name.indexOf("=");
                if (i > 0 && i < name.length()) {
                    registerParameter(name.substring(0, i), name.substring(i + 1), ht);
                }
            }
        } else {
            s = s.substring(1);
        }

        params.addElement("queryString===" + s);

        /* Перенос параметров из временной Hashtable ht в Vector params*/
        e = ht.keys();
        while (e.hasMoreElements()) {
            name = (String) e.nextElement();
            val = (String) ht.get(name);
            val = StrUtil.unescape(val);
            if (val.length() > 0) {
                params.addElement(name + "=" + val);
//  rm.println("******* param: '" + name + "'='"+ val + "'");
            }
        }
        if (cfgFileName == null) {
            cfgFileName = rm.getString("DefaultCfgFileName", false);
            registerParameter("c", cfgFileName, ht);
        }
        params.addElement("cfgFile=" + cfgFileName);
        params.addElement("tm=" + Long.toString(System.currentTimeMillis()));
    }

    /**
     * Занесение имени и значения параметра в Hashtable Для последущего переноса
     * в параметры Тюнера
     *
     * Специальным образом обрабатываются параметры "c", "SQL_TEXT", с префиксом
     * "FIXED_"
     *
     * @param name
     * @param val
     * @param ht
     */
    public void registerParameter(String name, String val, Hashtable ht) {
//        System.out.println("------- registerParameter: '" + name + "'='" + val + "'");
        if (val.length() > 0) {
            if (name.equals("c")) // The module file name
            {
                cfgFileName = val;
//                System.out.println("registerParameter: cfgFileName=" + cfgFileName + "; securityMode=" + securityMode + "; requestType=" + requestType);
                if (securityMode.equalsIgnoreCase("STRICT")) { // СТРОГИЙ режим проверки расширения файла модуля 
/**/    
                    if(cfgFileName.contains("."))   // отрезаем расширение
                        cfgFileName = cfgFileName.substring(0, cfgFileName.indexOf("."));
                    if (requestType.equals("DIRECT")) {  // добавляем разрешенное расширение - .mod
                        cfgFileName += ".mod";
                    } else if (requestType.equals("AJAX")) { // добавляем разрешенные расширения - .ajm  или .mod
                        if(IOUtil.fileExists(rm.getString("CfgRootPath", false) + cfgFileName + ".mod"))
                            cfgFileName += ".mod";
                        else
                            cfgFileName += ".ajm";
                    }
                    /**/
                } else {  // НЕ строгий режим проверки расширения файла модуля (для совместимости)
                    if (!val.contains(".cfg")) {
                        cfgFileName = val + ".cfg";
                    }
                }
//                System.out.println("***** REQUEST:" + rm.getString("requestType") + " cfgFileName=" + cfgFileName + "; securityMode=" + securityMode);
//                cfgFileName = Tuner.
                rm.putString("cfgFileName", cfgFileName);
            } else if (name.equals("debug")) {
                val = "";
            } else if (ht.containsKey(name)) {
                val = val + "," + (String) ht.get(name);
            }
            if (!name.startsWith("SQL_TEXT") && !name.startsWith("FIXED_")) {
                val = val.replace('\'', '`');	// was commented before 08.04.2006
                int i = val.toLowerCase().indexOf(" union ");  // защита от SQL-инъекций (union + select)
                if (i > 0 && val.toLowerCase().indexOf(" select ") > 0) {
                    rm.println("***** ATTACK: " + name + "=" + val);
                    IOUtil.writeLogLn("***** ATTACK: " + name + "=" + val, rm);
                    val = " ";
                }
            }
//			rm.println("----->> param: '" + name + "'='"+ val + "'");
            if (!ht_locked_params.containsKey(name)) //do not overwrite locked params
            {
                ht.put(name, val);
            }
            if (name.equals("request_param")) {   // НЕПОНЯТНО, что это за хрень, где и для чего она использовалась
//rm.println("----->> param: '" + name + "'='"+ val + "'");
                String[] params = val.split("&");
                for (int i = 0; i < params.length; i++) {
                    int j = params[i].indexOf("=");
//				  rm.println("----->> param " + i + ": j=" + j);
                    if (j > 0) {
                        name = params[i].substring(0, j);
                        ht.put(name, params[i].substring(j + 1)); //ignore prev.param.value
                        ht_locked_params.put(name, ""); //remember name to prevent overwriting
//					  registerParameter(params[i].substring(0,j), params[i].substring(j+1), ht);
                    }
                }
            }
        }
    }

    /**
     * Вытаскивание всех заголовков запроса и добавление соответствующих
     * параметров с префиксом h_ Также выставляются параметры с куками с
     * префиксом q_ ("q_" + cookies[i].getName(), cookies[i].getValue()) Также
     * ставится requestType = "DIRECT" в случае прямого GET-POST запроса от
     * клиента, requestType = "AJAX" в случае AJAX-запроса
     */
    public void getHeaders() {
        requestType = "DIRECT";
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            requestType = "AJAX";           
        }       
        rm.setParam("requestType", requestType);
        for (Enumeration e = request.getHeaderNames(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            String val = request.getHeader(name);
            params.addElement("h_" + name + "=" + val);
//            System.out.println("... getHeaders: " + name + "='" + val + "'");
        }
        if (cookies == null) {
            cookies = request.getCookies();
        }
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                params.addElement("q_" + cookies[i].getName().trim()
                        + "=" + StrUtil.unescape(cookies[i].getValue()));
            }
        }
    }

    /**
     *
     * @throws Exception
     */
    public void validateParameters() throws Exception {
        ParamValidator pv = (ParamValidator) rm.getObject("ParamValidator", false);
        if (pv == null) {
            return;
        }
//  rm.println("ParamValidator Class Name: " + pv.getClass().getName() );
        pv.validate(cfgTuner, rm);
//  rm.print("=   DONE! ");
    }

    /**
     * *********************** Создание Tuner, Service и запуск
     * сервиса*********************
     */
    /**
     *
     * @throws Exception
     */
    public void makeTuner() throws Exception {

        int extraSize = 100;   // some more space for parameters (to speed up dynamic parameter setting)
        String[] queryParam = new String[params.size() + extraSize];
        for (int i = 0; i < extraSize; i++) {
            queryParam[queryParam.length - i - 1] = "";
        }

        params.copyInto(queryParam);
        cfgTuner = new Tuner(queryParam,
                cfgFileName, rm.getString("CfgRootPath", false), rm);

        if (!cfgTuner.enabledOption("referer")) {
            if (request.getHeader("referer") != null) {
                cfgTuner.addParameter("referer",
                        StrUtil.replaceInString(
                                StrUtil.replaceInString(
                                        StrUtil.replaceInString(
                                                request.getHeader("referer"), "?", "%3F"), "&", "%26"), "=", "%3D"));
            }
        }

        rm.setObject("cfgTuner", cfgTuner);

    }

    /**
     *
     *
     * @throws java.lang.Exception
     */
    public void startService() throws Exception {
        srv = obtainService();
// rm.println(" XXX Start Service:" +  srv);
        if (srv == null) {
            return;
        }
//  rm.println(". " + rm.getString("queryLabel") + " user:" + cfgTuner.getParameter("user")
//    + " service: " + cfgTuner.getParameter("service") + "/" + cfgTuner.getParameter("c"));
//  outWriter.println(" <b>Service: </b>" + srv + "<br>");
        srv.doIt(rm);          // START OF THE MRT SERVICE with CFG-file
//  rm.println(" Exit from the " + cfgTuner.getParameter("service"));
        if (cfgTuner.enabledExpression("debug=on")) {
            logAll();
        }
    }

    /**
     *
     * @return @throws Exception
     */
    public Service obtainService() throws Exception {
        String className = cfgTuner.getParameter("parameters", "service");
        if (className == null || className.length() == 0) {
            className = "dubna.walt.service.Service";
        }

// rm.println(" obtainService:" + className);
        Class cl = Class.forName(className);
        Service srv = (Service) (cl.newInstance());

        logRequestStarted();

        checkSession();

        return srv;
//  return obtainService(className);
    }

    /**
     *
     * @param className
     * @return
     * @throws Exception
     */
    public static Service obtainService(String className) throws Exception {
        Class cl = Class.forName(className);
        Service srv = (Service) (cl.newInstance());
        return srv;
    }

    /**
     * Проверка времени неактивности сессии
     */
    public void checkSession() {
        HttpSession session = cfgTuner.session;
        long time_unit = 1000 * 60; //min
//        long time_unit = 1000; //sec
        long fromStart = (session.getLastAccessedTime() - session.getCreationTime()) / time_unit;
//        long inactive = (System.currentTimeMillis() - session.getLastAccessedTime()) / time_unit;
//    session.
//        int maxInt = session.getMaxInactiveInterval();
        int pause_time = (int) ((startTm / time_unit) - cfgTuner.getLongParameter("last_request"));
        cfgTuner.addParameter("pause_time", Integer.toString(pause_time));
//        cfgTuner.addParameter("sess_inactive", Long.toString(inactive));
        cfgTuner.addParameter("sess_fromStart", Long.toString(fromStart));
        if (!cfgFileName.contains("checkSession")) {
            cfgTuner.setParameterSession("last_request", Long.toString(startTm / time_unit));
        }
        IOUtil.writeLogLn(5, "+++++ session inactive=" + pause_time + "; fromStart=" + fromStart + "min", rm);
//		session.setMaxInactiveInterval( 1);     
    }

    /*
   **************** Всевозможные логи *******************
     */
    /**
     * Запись в лог-файл строки завершения сервиса
     *
     */
    public void logRequestFinished() {

        ResourceManager rmg = rm.getGlobalRM();
        if(!rmg.getBoolean("log")) return;

        startTm = System.currentTimeMillis() - startTm;
        c = cfgTuner.getParameter("c");
        if (!excludeFromLog.contains("," + c ) && !c.contains("showLog")) //        if (!c.contains("showLog")) 
        {
            rm.setParam("log", "true");
            String id = "Request_" + rm.getString("queryLabel");
            IOUtil.writeLogLn("</div><span class='req_end pt' onClick='toggleDiv(\"" + id + "\");'>"
                    + id + " (" + c + ") DONE! (" + startTm + "ms)</span> ", rm);
        }

//        if (!BasicServlet.ignoreModules.contains("," + c + ",")) {
//            System.out.println("***qt: " + rm.getString("queryLabel")
//                    + " FINISHED : " + cfgTuner.getParameter("c")
//                    + " (" + startTm + "ms)"
//            );
//        }
    }

    /**
     * Запись в лог-файл строки вызова сервиса
     *
     */
    public void logRequestStarted() {

        ResourceManager rmg = rm.getGlobalRM();
        if(!rmg.getBoolean("log")) return;

        c = cfgTuner.getParameter("c");
        if (c.length() == 0 || !excludeFromLog.contains(',' + c ) && !c.contains("showLog")) 
        {
            rm.setParam("log", "true", false);
            String s = cfgTuner.getParameter("queryString").replace("<", "&lt;");
            if (s.length() > 550) {
                s = s.substring(0, 500) + ".....";
            }
            Date dat = new java.util.Date();
            String id = "Request_" + rm.getString("queryLabel");
//            System.out.println(id + ": " + c + "; log=" + rmg.getString("log"));
            IOUtil.writeLog("</div><br><span class='req_start pt' onClick='toggleDiv(\"" + id + "\");'> "
                    + id
                    + " [" + Fmt.shortDateStr(dat) + ":" + dat.getSeconds() + "] " + cfgTuner.getParameter("USER_ID") + ": " + cfgTuner.getParameter("ClientIP")
                    + " c=" + c + "</span><div id='" + id + "' class='req'><xmp> queryString:" + s + "; log=" + rm.getBoolean("log")
                    + "; session inactive=" + cfgTuner.getParameter("pause_time") + "min;</xmp><br>" //                    + srv
                    ,
                     rm);
            if (cfgTuner.enabledOption("LOG=OFF")) {
                rm.setParam("log", "false");
                IOUtil.writeLogLn("<b>LOG=OFF</b>", rm);
            }
        } else {
            if(!c.contains("showLog"))
                rm.setParam("log", "false", false);
        }
    }

    /**
     * Print query informaition into Tomcat's log
     */
    public void logQuery() {
        if (!cfgTuner.enabledOption("c=empty")) {
            rm.println( "  sqt: "
                    + rm.getString("queryLabel")
                    + ": " + cfgTuner.getParameter("c")
                    + ": " + cfgTuner.getParameter("USER_ID")
//                    + " (" + cfgTuner.getParameter("USER_GROUP") + ") " 
//                    + ": " + cfgTuner.getParameter("ClientIP")
                    //                + "; [" + Fmt.lsDateStr( new java.util.Date() ) + "] "
//                    + "; [" + Fmt.shortDateStr(new java.util.Date()) + "] "
            );
        }
    }

    public void logException(Exception e) {
        if (!(e instanceof java.net.SocketException)) {
            if (srv == null || !srv.terminated) {
                rm.println("\n======= EXCEPTION " + e.toString() + "; cfgTuner=" + cfgTuner);
                e.printStackTrace();
                if (cfgTuner != null) {
                    rm.println("\n======= USER_ID=" + cfgTuner.getParameter("USER_ID") + " ClientIP=" + cfgTuner.getParameter("ClientIP"));
                    rm.println("\n======= QUERY =" + cfgTuner.getParameter("queryString"));

                    if (cfgTuner.enabledOption("debug=on")
                            && cfgTuner.enabledOption("USER_ID=1")) {
                        if (outWriter == null) {
                            try {
                                outWriter = new PrintWriter(response.getOutputStream());
                            } catch (Exception ex) {
                            }
                        }

                        if (outWriter != null) {
                            outWriter.println("<xmp>");
                            e.printStackTrace(outWriter);
                            outWriter.println("</xmp>");
                            logAll();
                        }
                    }
                }
                rm.logException(e);
            }
        }
    }

    /**
     * Store query information in the database
     *
     * @param e
     */
    public void logQuery(Exception e) {
        // SHOULD BE OVERWITTEN
    }

    /**
     *
     */
    public void outInfo() {
        logAll();
    }

    /**
     *
     */
    public void logAll() {
        if (of.equals("xl")) {
            return;
        }

        if (outWriter != null) {
            outWriter.println("</center></i><hr><H4> " + this.getClass().getName() + " is here!</H4>");

            if (cookies == null) {
                cookies = request.getCookies();
            }
            if (cookies != null) {
                String[] cook = new String[cookies.length];
                outWriter.println("<b>Cookies:</b><br>");
                for (int i = 0; i < cook.length; i++) {
                    outWriter.println(cookies[i].getName() + "='" + cookies[i].getValue() + "'<br>");
                }
            }

            if (cfgTuner != null) {
                cfgTuner.logParameters(outWriter);
            }
            rm.logAll(outWriter);
//    outWriter.flush();
        }
    }

    /*
   ******************** Служебные методы **********************
     */
    /**
     *
     * @return @throws Exception
     */
    public boolean validateUser() throws Exception {
        UserValidator uv = (UserValidator) rm.getObject("UserValidator", false);
        if (uv == null) {
            return true;
        }
        return uv.validate(rm);
    }

    /**
     * НЕ ИСПОЛЬЗУЕТСЯ
     *
     * @throws Exception
     */
    /*
    public void writeHttpHeaders_ZZZ() throws Exception {
        try {
            outWriter.flush();  // write http-headers and test if the client is still on-line
        } catch (Exception e) {
            rm.println("################# writeHeaders - " + e.toString());
            throw (e);
        }
    }
     */
    /**
     *
     */
    public void setContentType() {
        of = cfgTuner.getParameter("of");
        //  if (of == "") of = "h";
        //  rm.println("......... Format:" + of);
        if (of.equalsIgnoreCase("bin")) {
            return;
        }
        if (of.equalsIgnoreCase("xl") || of.equals("xlh")) {
            response.setContentType("application/vnd.ms-excel");
            if (!cfgTuner.enabledOption("inline=true")) {
                response.setHeader("Content-Disposition", "attachment; filename=" + cfgTuner.getParameter("file_name"));
            }
        } else if (of.equalsIgnoreCase("word") || of.equals("xlh")) {
            response.setContentType("application/msword");
            if (!cfgTuner.enabledOption("inline=true")) {
                response.setHeader("Content-Disposition", "attachment; filename=" + cfgTuner.getParameter("file_name"));
            }
        } else {
            String contentType = cfgTuner.getParameter("contentType");
            if (contentType.length() > 0) {
                response.setContentType(contentType);
            } else {
                response.setContentType("text/html; charset=" + serverEncoding);
            }
            int length = cfgTuner.getIntParameter("contentLength");
            if (length > 0) {
                response.setHeader("Content-length", Integer.toString(length));
            }
        }

        //  response.setHeader( "Last-modified", "Sun, 23 May 2004 11:10:40 GMT" );
        //    response.setContentType("application/msword");
        // application/x-zip-compressed    
    }

    /**
     *
     * @return @throws Exception
     */
    public 
//            synchronized 
        DBUtil makeDBUtil() throws Exception {
        return null;  // must be overwritten if needed DBUtil
    }

    /**
     * Finds a cookie with the given name and returns its value if the cookie is
     * found or null if the cookie is not found.
     *
     * @param name
     * @return
     */
    public String getCookieValue(String name) {
        if (cookies == null) {
            cookies = request.getCookies();
        }

        if (cookies != null) {
            for (int i = cookies.length - 1; i >= 0; i--) {
                if (cookies[i].getName().equals(name)) {
                    return cookies[i].getValue();
                }
            }
        }
        return "";
    }

    /**
     * Reads and parses the multipart form data from the ServletInputStream
     *
     * @param request
     * @param ht
     * @return
     * @throws java.lang.Exception
     */
    public String getMultiPartData(HttpServletRequest request, Hashtable ht)
            throws Exception {
        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        String contType = request.getHeader("Content-Type");
        //  rm.println("contType='" + contType + "'");
        if (contType != null && contType.startsWith(KEY_MULTIPART)) {
            String boundary = "--" + contType.substring(contType.indexOf("boundary") + 9);
//			rm.println("boundary='" + boundary + "'");
            //    rm.print("+++ Try to read from ServletInputStream...");
            String allParams = MULTIPART_SEPARATOR;
            int contLength = Integer.parseInt(request.getHeader("Content-Length"));
            String name = "";
            String filename = "";
            String val = "";

            int len = contLength;
            if (len > 4096) {
                len = 4096;
            }
            byte[] buf = new byte[len];
            int numBytes = 0;
            int rest = contLength;
//			rm.println("======================== contLength=" + contLength);

            if (inpStream == null) {
                inpStream = request.getInputStream();
            }

            int i = 0;
            for (int n = readLine(inpStream, buf, 0, rest); n > 0 && rest > 0; n = readLine(inpStream, buf, 0, rest)) {
                numBytes += n;
                rest = contLength - numBytes;
                String s = new String(buf, 0, n, isAjax ? "utf-8" : clientEncoding);

// rm.println("..0 " + n + " bytes read. Total:" + numBytes + ". Content length:" + contLength + ". Rest:" + rest);
                if (s.equals(boundary)) {
                    n = readLine(inpStream, buf, 0, rest);
                    s = new String(buf, 0, n, isAjax ? "utf-8" : clientEncoding);
                    numBytes += n;
                    rest = contLength - numBytes;
// rm.println("..1 " + n + " bytes read. Total:" + numBytes + ". Content length:" + contLength + ". Rest:" + rest);
                }

                if (n > 2) {
                    StringTokenizer st = new StringTokenizer(s, ";");
                    while (st.hasMoreTokens()) {
                        String t = st.nextToken().trim();
                        if (t.indexOf(KEY_NAME) == 0) {
                            name = t.substring(KEY_NAME.length(), t.length() - 1).trim();
                            if (name.length() > 0) {
                                if (st.hasMoreTokens()) {
                                    t = st.nextToken().trim();
                                }
//	rm.println(":::::: try to get '" + name + "'");
                                if (t.indexOf(KEY_FILENAME) == 0) {
                                    filename = t.substring(KEY_FILENAME.length(), t.length() - 1).trim();
//									 rm.println(n + ": name: " + name + ";  filename:" + filename + ";");
                                    if (filename.length() > 0) {
                                        int bufLen = 64000;
                                        FileContent fc = new FileContent(inpStream, filename, boundary.getBytes(), bufLen, rest);
//										 FileContent fc = new FileContent(inpStream, filename, boundary.getBytes(), 2000000, rest);
                                        registerParameter(name + "_SRC", StrUtil.replaceInString(filename, "\\", "/"), ht);
                                        numBytes += fc.getBytesRead();
                                        filename = fc.getFileName();
                                        rm.setObject(name + "_CONTENT", fc);
                                        registerParameter(name + "_TYPE", fc.getFileType(), ht);
                                        registerParameter(name + "_CONTENT_TYPE", fc.getContentType(), ht);
                                        registerParameter(name, filename, ht);
                                        registerParameter(name + "_SIZE", Integer.toString(fc.getFileSize()), ht);
                                        allParams = allParams + name + "=" + filename + MULTIPART_SEPARATOR
                                                + name + "_TYPE=" + fc.getFileType() + MULTIPART_SEPARATOR;
                                        //            fc.storeToDisk(rm.getString("AppRoot") + rm.getString("uploadPath"), filename);
                                        //            fc.storeToDisk(cfgTuner.getParameter("AppRoot") + cfgTuner.getParameter("uploadPath"), filename);
                                        filename = "";
                                    }
                                } else {
                                    n = readLine(inpStream, buf, 0, rest);
                                    s = new String(buf, 0, n, clientEncoding);
                                    numBytes += n;
                                    rest = contLength - numBytes;
                                    val = "";
                                    while (n > 0 && rest > 0) {
                                        n = readLine(inpStream, buf, 0, rest);
                                        s = new String(buf, 0, n, clientEncoding);
                                        numBytes += n;
                                        rest = contLength - numBytes;
                                        if (s.indexOf(boundary) == 0) {
                                            break;
                                        }
                                        val += s;
                                    }
                                    //	rm.println(n + ":++++ val:'" + val + "'");
                                    val = val.substring(0, val.length() - 2).trim();
                                    registerParameter(name, val, ht);
                                    if (val.length() > 0) {
                                        allParams = allParams + name + "=" + val + MULTIPART_SEPARATOR;
                                    }
                                    name = "";
                                    val = "";
                                }
                            }
                        }
                    }
                }
                if (rest > len) {
                    rest = len;
                }
            }
//rm.println("... bytes read. Total:" + numBytes + ". Content length:" + contLength + ". Rest:" + rest);
            if (numBytes != contLength) {
                rm.println("***************************** ERROR: " + numBytes + " bytes of " + contLength + " read;");
                //        return "";
            }
            params.addElement("multipart=yes");
            return allParams;
        } else {
            return "";
        }
    }

//	public static int readLine(ServletInputStream inpStream, byte[] b, int off, int rest) throws IOException
    /**
     *
     * @param inpStream
     * @param b
     * @param off
     * @param rest
     * @return
     * @throws IOException
     */
    public static int readLine(InputStream inpStream, byte[] b, int off, int rest) throws IOException {
        int bufLen = b.length;

        if (bufLen > rest) {
            bufLen = rest;
        }
        int off2 = off;
        boolean foundCR = false;
        int r;

        while (off2 - off < bufLen) {
            r = inpStream.read();
            if (r == -1) {
                if (off2 == off) {
                    return -1;
                }
                break;
            }

            b[off2++] = (byte) r;
            if (r == 13) {
                foundCR = true;
                continue;
            } else if (foundCR && r == 10) {
                break;
            } else {
                foundCR = false;
            }
        }
//	  rm.println("*EOL|");

        return off2 - off;
    }

    /**
     *
     * @param buf
     * @param start
     * @param end
     * @return
     */
    public static byte[] copyBuffer(byte[] buf, int start, int end) {
        byte[] b = new byte[end - start];
        for (int i = 0; i < end - start; i++) {
            b[i] = buf[i + start];
        }
        return b;
    }

    /**
     *
     */
    protected void finish() {
        String[] sa = rm.getKeys("D");
        DBUtil dbu;
        for (String sa1 : sa) {
//            System.out.println( " SimpleQueryThread.finish(): " + i + ": " + sa[i]);
            if (!sa1.contains("dbUtil_LOGGER") && !sa1.contains("dbUtilLogin")) {
                try {
                    Object o = rm.getObject(sa1, false, false);
                    if (o instanceof dubna.walt.util.DBUtil) {
                        dbu = (DBUtil) o;
                        System.out.print("  sqt: " +  ".finish() close " + dbu.myName);
                        try {
                            dbu.commit();
                        } catch (Exception e) {
                        }
                        dbu.close();
                        rm.removeKey(sa1, false);
                    }
                }catch (Exception e) {
                    rm.println("Finalize ERROR: " + e.toString());
                }
            }
        }
//        System.runFinalization();
//        System.gc();
    }

    /**
     *
     *
     * @throws java.lang.Throwable
     */
    @Override
    protected void finalize() throws Throwable {
      try { 
          finish();
      } finally {
//          super.finalize();
        ;
      }
    }
}
