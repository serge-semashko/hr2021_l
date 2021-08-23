package dubna.walt.util;

import java.io.*;
import java.util.StringTokenizer;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * The IOUtil class extends standard JAVA io-possibilities and makes them easier
 * to use (you may do not care about opening files, read/write exceptions etc.).
 */
public class IOUtil extends java.lang.Object {

    /**
     *
     */
    public static final int COPY_OK = 0;

    /**
     *
     */
    public static final int SRC_FILE_NOT_FOUND = 1;

    /**
     *
     */
    public static final int OUT_FILE_ERROR = 2;


    /* ========= METHODS FOR "INPUT" OPERATIONS ==============*/
    /**
     * Load a file from any .jar file in the classpath.
     *
     * @param fileName
     * @param jarFileName
     * @return
     */
    public static InputStream getResourceAsInputStream(String fileName, String jarFileName) {
        InputStream stream = null;
        ZipFile zipFile = null;
        String classpath = System.getProperty("java.class.path");
        if (!classpath.contains(";")) {
            classpath = classpath.replace(':', ';');
        }
        System.out.println("=== classpath='" + classpath + "'");

        StringTokenizer st = new StringTokenizer(classpath, ";");
        String mask = jarFileName;
        if (jarFileName == null || jarFileName.length() == 0) {
            mask = ".jar";
        }

        while (st.hasMoreTokens() && stream == null) {
            String classFolder = st.nextToken();
            if (classFolder.endsWith(mask)) {
                try {
                    zipFile = new ZipFile(classFolder);
                    if (zipFile != null) {
                        ZipEntry zipEntry = zipFile.getEntry(fileName);
                        //  System.out.println(p_zipFile + ": looking for " + p_fileName + "; zipEntry=" + zipEntry);
                        if (zipEntry == null && fileName.charAt(0) == '/') {
                            zipEntry = zipFile.getEntry(fileName.substring(1));
                        }
                        if (zipEntry != null) {
                            stream = zipFile.getInputStream(zipEntry);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
//      if (stream != null) System.out.println(fileName + " found in " + classFolder);
            }
        }
        return stream;
    }

    /**
     * Opens a file given by fileName for "READ".
     *
     * @param fileName file to open
     * @return FileInputStream (or null if fault).
     */
    public static FileInputStream openInputFile(String fileName) {
        try {
            return new FileInputStream(fileName);
        } catch (Exception e) {
            processException(e);
        }
        return null;
    }

// Copy the input to the output until EOF.
    /**
     *
     * @param in
     * @param out
     * @throws IOException
     */
    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        int bufSize = 8192;
        byte[] buf = new byte[bufSize];
        int numBytes = 1;
        while (numBytes > 0) {
            numBytes = in.read(buf, 0, bufSize);
            if (numBytes > 0) {
                out.write(buf, 0, numBytes);
            }
        }
    }

    /**
     * Copy the input to the output until EOF.
     * @param srcFileName
     * @param destFileName
     * @return
     * @throws IOException
     */
    public static 
//            synchronized 
        int copyFile(String srcFileName, String destFileName) throws IOException {
        FileInputStream in = null;
        FileOutputStream out = null;
        System.out.println(" ... copyFile " + srcFileName + " => " + destFileName);
        try {
            in = new FileInputStream(srcFileName.replace('/', File.separatorChar));
        } catch (FileNotFoundException e) {
            return SRC_FILE_NOT_FOUND;
        }

        try {
            out = new FileOutputStream(destFileName.replace('/', File.separatorChar));
        } catch (IOException e) {
            return OUT_FILE_ERROR;
        }

        IOUtil.copyStream(in, out);
        in.close();
        out.close();
        return COPY_OK;
    }

// Copy the input to the output until EOF.
    /**
     * Check if file exists
     * @param srcFileName
     * @return
     * @throws IOException
     */
    public static 
//            synchronized 
        boolean fileExists(String srcFileName) {
        boolean res = false;
          try {   
        File f = new File(srcFileName);
         res = f.canRead() & (f.length()>0);
         } catch (Exception e) {
            e.printStackTrace();
        }
//        if(!res)
//            System.out.println(" ... file NOT Exists " + srcFileName + " (fileExists(...))");
        return res;
    }
    
       /* ========= METHODS FOR "LOG"-OPERATIONS ==============*/
    /**
     *
     * @param rm
     */
    public static void clearLogFile(ResourceManager rm) {
        try {
//            System.out.println("clearLogFile: copyFile(" + rm.getString("logPath") + "logHead.html" + " ==> " +  rm.getString("logPath") + getLogFileName(rm));
            System.out.println("clearLogFile...");
            copyFile(rm.getString("logPath") + "logHead.html", rm.getString("logPath") + getLogFileName(rm));
        } catch (Exception e) {
            System.out.println("******* IOUtil.clearLogFile ERROR:");
            e.printStackTrace();
            System.out.println("******* Try to clear log file using clearLogFileOld...");
            clearLogFileOld(rm);
        }        
    }
    /* ========= METHODS FOR "LOG"-OPERATIONS ==============*/
    /**
     *
     * @param rm
     */
    public static void clearLogFileOld(ResourceManager rm) {

        PrintWriter pw = getLogWriter(rm, false);
        if (pw == null) {
            return;
        }
        try {
            String cs = rm.getString("logCharSet", false, rm.getString("clientEncoding", false, "Cp1251"));
            pw.println("<HTML><HEAD><meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + cs + "\"><TITLE>Log</TITLE><style>xmp {margin:0;} body{background-color:#eeeeee;}</style></HEAD><BODY>");
            pw.flush();
            pw = null;
        } catch (Exception e) {
            processException(e);
        } finally {
            try {
                pw.close();
            } catch (Exception ex) {;
            }
        }
    }

    /**
     *
     * @param rm
     * @param append
     * @return
     */
static  Object o = new Object();
static int log_req_num = 0;

    public static PrintWriter getLogWriter(ResourceManager rm, boolean append) {
        if (rm == null) {
            System.out.println("IOUtil.getLogWriter() - can't get log file - ResourceManager is NULL");
            return null;
        }
        Tuner t = (Tuner) rm.getObject("cfgTuner", false, false);
        if(t != null && t.enabledOption("LOG=OFF"))
            return null;
        PrintWriter lw = null;
        synchronized (o) {
        ResourceManager rmg = rm.getGlobalRM();
        if(rmg == null) rmg = rm;
        try {
            if (!rmg.getBoolean("log")) {
                return null;
            }
            String lf_name = getLogFileName(rmg);
            String prev_lf_name = rmg.getString("lf_name", false, "");
//            System.out.println("=== IOUtil.getLogWriter(...): logPath = " + rmg.getString("logPath") + " lf_name: " + lf_name);
            String logPath = (rmg.getString("logPath")+"/").replaceAll("[\\\\/]{1,}", "/");
            rmg.putString("logPath", logPath);
//            System.out.println("=== IOUtil.getLogWriter(...): logPath = " + logPath + " lf_name: " + lf_name);
            String logFileFullPath = logPath + lf_name ;
//            System.out.println("=== IOUtil.getLogWriter(...): logFileFullPath = " + logFileFullPath);

            lw = (PrintWriter) rmg.getObject("LogWriter", false);
            if (!lf_name.equals(prev_lf_name)) {  // имя сменилось - закрываем старый лог-файл
                rmg.putString("lf_name", lf_name);
                if (lw != null) 
                    lw.close();
                lw = null;
            }
            if (append &&  lw != null && !lw.checkError() ) // файл ОК - возвращаем его, если надо дописывать
                return lw;
            
            if (lw != null) 
                lw.close();
            
            System.out.println("IOUtil.getLogWriter(" + ++log_req_num + ")...");
            if (!lf_name.equals(prev_lf_name) || !append )   // если файл новый или старый, но не дописывать- копируем шапку
                copyFile(rmg.getString("logPath") + "logHead.html", logFileFullPath);
            
//          Открываем файл
            Path path = FileSystems.getDefault().getPath(logFileFullPath); // новый Path файла
            Charset cs = Charset.forName(rmg.getString("clientEncoding", false, "UTF-8"));
            OpenOption o = StandardOpenOption.CREATE; //TRUNCATE_EXISTING;
            lw = (new PrintWriter(Files.newBufferedWriter(path, cs, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND)));           
            rmg.setObject("LogWriter", lw, false);
            System.out.println("IOUtil.getLogWriter(" + log_req_num-- + "): " + lw);
    //       System.out.println("LogWriter=" + lw);
        } catch (Exception e) {
            e.printStackTrace(System.out);
//	  processException(e);
        }
        }
        return lw;
    }

    public static String getLogFileName(ResourceManager rm) {
//        System.out.println("rm.getBoolean(\"log\")=" + rm.getBoolean("log") + "; rm.getString(log)=" + rm.getString("log"));
        if (!rm.getBoolean("log")) {
            return null;
        }
        Date date = new Date();
        int month = date.getMonth() + 1;
        int day = date.getDate();
        int hour = date.getHours();
        int min = date.getMinutes() / 1;
//        return rm.getString("logFileName", false, "log") + "_" + month + "_" + day + "_" + hour + "_" + min + ".html";
        return rm.getString("logFileName", false, "log") + "_" + month + "_" + day + "_" + hour + ".html";
    }
    
    /**
     *
     * @param header
     * @param a
     * @param rm
     */
    public static void writeLogXmp(String header, String[] a, ResourceManager rm) {
        PrintWriter pw = getLogWriter(rm, true);
        if (pw == null) {
            return;
        }
        try {
            pw.println("<B>=== " + header + " ===</B><xmp>");
            for (int i = 0; i < a.length; i++) {
                pw.println(i + ": '" + a[i] + "'");
            }
            pw.println("</xmp>=========================<BR>");
            pw.flush();
            pw = null;
        } catch (Exception e) {
            processException(e);
        } finally {
            try {
                pw.close();
            } catch (Exception ex) {;
            }
        }
    }

    /**
     *
     * @param level - уровень, до которого выводить сообщения в лог (0 - 9)
     * @param header
     * @param a
     * @param rm
     */
    public static void writeLog(int level, String header, String[] a, ResourceManager rm) {
        if (level <= rm.getInt("LogLevel", 1)) {
            PrintWriter pw = getLogWriter(rm, true);
//            System.out.println("IOUtil.writeLog(): level=" + level + "; pw=" + pw);
            if (pw == null) {
                return;
            }
            try {
                pw.println("<B>=== " + header + " ===</B><BR>");
                for (String a1 : a) {
                    pw.println(a1.concat("<BR>"));
                }
                pw.println("=========================<BR>");
                pw.flush();
                pw = null;
            } catch (Exception e) {
                processException(e);
            } finally {
                try {
                    pw.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    /**
     *
     * @param header
     * @param a
     * @param rm
     */
    public static void writeLog(String header, String[] a, ResourceManager rm) {
        writeLog(0, header, a, rm);
    }

    /**
     *
     * @param text
     * @param rm
     */
    public static void writeLog(int level, String text, ResourceManager rm) {
        if (level <= rm.getInt("LogLevel", 0)) {
            PrintWriter pw = getLogWriter(rm, true);
//            System.out.println("IOUtil.writeLog(): level=" + level + "; pw=" + pw);
            if (pw == null) {
                return;
            }
            try {
                pw.print(text);
                pw.flush();
                pw = null;
            } catch (Exception e) {
                processException(e);
            } finally {
                try {
                    pw.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    /**
     *
     * @param text
     * @param rm
     */
    public static void writeLog(String text, ResourceManager rm) {
        writeLog(0, text, rm);
    }

    /**
     *
     * @param text
     * @param rm
     */
    public static void writeLogLn(int level, String text, ResourceManager rm) {
        writeLog(level, text.concat("<BR>\r\n"), rm);
    }

    /**
     *
     * @param text
     * @param rm
     */
    public static void writeLogLn(String text, ResourceManager rm) {
        writeLog(0, text.concat("<BR>\r\n"), rm);
    }

    static void processException(Exception e) {
        System.out.println("Log Exception:" + e.toString());
    }

}
