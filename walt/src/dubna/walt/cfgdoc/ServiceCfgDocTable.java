package dubna.walt.cfgdoc;

import dubna.walt.util.IOUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author serg
 */
public class ServiceCfgDocTable extends dubna.walt.service.Service {

    public static String cfgRootPath = "";
    public static String dir = "";
    private List<CfgInfo> cfgFiles = null;
    private ArrayList<String> dirList = null;

    @Override
    public void start() throws Exception {
        cfgRootPath = cfgTuner.getParameter("CfgRootPath").replaceAll("\\\\", "/");;
        dir = cfgTuner.getParameter("dir");
        String charset = rm.getString("serverEncoding", false, "Cp1251");

        setDir(dir);

        cfgFiles = (List<CfgInfo>) new ArrayList<CfgInfo>();
        dirList = new ArrayList<String>();
        walkDir(cfgRootPath + dir, charset);

        Collections.sort(cfgFiles, cfgFileInfoComparator);
        Collections.sort(dirList, new Comparator<String>() {
                public int compare(String o1, String o2) {
                return o1.compareTo(o2);
                }}
        );
//        String dl = getCfgFileList(dir, cfgTuner.getParameter("name"));
//        IOUtil.writeLogLn("dl=" + dl, rm);
        cfgTuner.addParameter("dir_list", getDirList());
        cfgTuner.addParameter("file_list", getCfgFileList(dir, cfgTuner.getParameter("name")));
//        IOUtil.writeLogLn("<b>file_list=</b>" + cfgTuner.getParameter("file_list"), rm);
        cfgTuner.outCustomSection("report", out);
    }

    /**
     * Готовится и ставится параметр this_dir_path со ссылками на отделные директории
     * @param dir 
     */
    private void setDir(String dir) {
        String[] aDir = dir.split("/");
        String c = cfgTuner.getParameter("comments", "parents");
        String dirPath = "";
        String  link = "/ <a href='?c=" + c + "'>root</a>&nbsp;/&nbsp;";
        if (aDir.length > 0) {
            for (int i = 0; i < aDir.length; i++) {
                String d = aDir[i];
                if(d.length() > 0) {
                if (i == aDir.length - 1)
                    cfgTuner.addParameter("upper_dir_path", link);
                dirPath += d + "/";
                link += "<a href='?c=" + c  + "&dir=" + dirPath + "'>" + d + "</a>&nbsp;/&nbsp;";
                }
            }
        }
        cfgTuner.addParameter("this_dir_path", link);
    }

    
//    private String getDirList(String parentdir) throws Exception {
    private String getDirList() throws Exception {
        String s = "";
        for (String d : dirList) {
            String dirPath = "";
            String dirName = d;
            int i = d.lastIndexOf("/");
            if(i>0) {
                dirPath = d.substring(0, i+1).replace(cfgRootPath,"");
                dirName = d.substring(i+1);
            }
//            System.out.println("d=" + d + "; dirPath=" + dirPath + "; dirName=" + dirName + ";");
            cfgTuner.setFlashParameters("dir_name=" + dirName + ";dir_path=" + dirPath);
            s += cfgTuner.getCustomSectionAsString("cfg_dir_item");
        }
        return s;
    }

    /**
     * Заполняет коллекцию cfgFiles полными путями к модулям в директории path
     * 
     * @param path
     * @param charset
     */
    public void walkDir(String path, String charset) {
        File root = new File(path);
        File[] list = root.listFiles();
        if (list == null) {
            return;
        }
        for (File f : list) {
            String fpath = f.getAbsolutePath().replaceAll("\\\\", "/").replace(cfgRootPath, "");
//IOUtil.writeLogLn("fpath=" + fpath, rm);
            if (f.isDirectory()) {
                dirList.add(fpath);
            } else if (fpath.endsWith(".cfg") || fpath.endsWith(".dat") || fpath.endsWith(".mod")  || fpath.endsWith(".ajm")) {
                cfgFiles.add(new CfgInfo(fpath, rm));
                IOUtil.writeLogLn(" +++ ADD FILE:" + fpath, rm);
            }
        }
    }

    /**
     *
     */
    public static final Comparator cfgFileInfoComparator = new Comparator<CfgInfo>() {
        @Override
        public int compare(CfgInfo o1, CfgInfo o2) {
            return o1.filename.compareTo(o2.filename);
        }
    };

    private String getCfgFileList(String dir, String activeFilename) throws Exception {
        String s = "";
        for (CfgInfo cfi : cfgFiles) {
IOUtil.writeLogLn(" +++ ADD: path=" + cfi.filepath + "; name="+ cfi.filename + "; ext=" + cfi.fileext + "; descr=" + cfi.descr, rm);
                cfgTuner.setFlashParameters("cfg_name=" + cfi.filename + ";cfg_descr===" + cfi.descr + ";cfg_ext=" + cfi.fileext);
//                cfgTuner.addParameter("cfg_name", cfi.filename);
//                cfgTuner.addParameter("cfg_descr", cfi.descr);
//                cfgTuner.addParameter("cfg_ext", cfi.fileext);
//                
//                cfgTuner.setFlashParameter("cfg_name", cfi.filename);
//                cfgTuner.setFlashParameter("cfg_descr", cfi.descr);
//                cfgTuner.setFlashParameter("cfg_ext", cfi.fileext);

                if (cfi.filename.equals(activeFilename)) {
                    cfgTuner.setFlashParameter("is_active", "y");
                } else {
                    cfgTuner.setFlashParameter("is_active", "");
                }
                if(cfi.comments == null)
                    cfgTuner.setFlashParameter("no_comments", "Y");
                else
                    cfgTuner.setFlashParameter("no_comments", "");
                s += cfgTuner.getCustomSectionAsString("cfg_item");
        }
        return s;
    }

}
