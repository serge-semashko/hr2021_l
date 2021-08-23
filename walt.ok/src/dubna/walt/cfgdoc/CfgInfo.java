package dubna.walt.cfgdoc;

import dubna.walt.util.IOUtil;
import dubna.walt.util.ResourceManager;
import dubna.walt.util.StrUtil;
import dubna.walt.util.Tuner;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author serg
 */
public class CfgInfo {

    protected String filename = "";
    protected String filepath = "";
    protected String fileext = "";
    protected String[] cfg = null;
    protected String content = "";
    protected String[] comments = null;
    protected String key = "";
    protected String descr = "";
    protected String testURL = "";
    protected String input = "";
    protected String output = "";
    protected String author = "";
    protected String service = "";
    protected List<String> parents = new ArrayList<String>();
    protected List<String> childs = new ArrayList<String>();
    protected List<String> sections = new ArrayList<String>();
    protected Tuner tuner;

    /**
     * Описательная информация модуля (файла .cfg).
     * Выбирает теги из секции [comments]
     * 
     * @param cfgFileName
     * @param rm
     */
    public CfgInfo(String cfgFileName, ResourceManager rm) {
        try {
            String cfgRootPath = rm.getString("CfgRootPath", true);
            tuner = new Tuner(null, cfgFileName, cfgRootPath, rm);
            IOUtil.writeLogLn(3, "<b>CfgInfo: open " + cfgFileName + ";</b> tuner=" + tuner, rm);
            int k = cfgFileName.lastIndexOf("/");
            if (k > 0) {
                filepath = cfgFileName.substring(0, k);
                filename = cfgFileName.substring(k + 1);
                if (filepath.startsWith(cfgRootPath)) {
                    filepath = filepath.substring(cfgRootPath.length());
                }
            } else {
                filename = cfgFileName;
            }
            k = cfgFileName.lastIndexOf(".");
            if (k > 0) {
                fileext = cfgFileName.substring(k + 1);
            }
            this.cfg = tuner.cfg;
            if (cfg != null) {
                IOUtil.writeLogLn(3, "<b>CfgInfo: process " + cfgFileName + "...</b>", rm);
                comments = tuner.getSection(null, "comments");
                descr = tuner.getFinalParameter("comments", "descr");
                author = tuner.getFinalParameter("comments", "author");
                input = tuner.getFinalParameter("comments", "input");
                output = tuner.getFinalParameter("comments", "output");
                service = tuner.getFinalParameter("service");
                testURL = tuner.getFinalParameter("comments", "testURL") 
                        + tuner.getFinalParameter("comments", "test_URL");
//    IOUtil.writeLogLn("<b>testURL=" + testURL + ";</b>", rm);
                if (tuner.getFinalParameter("comments", "childs").length() > 0) {
                    String[] sa = tuner.getFinalParameter("comments", "childs").split(",");
//    IOUtil.writeLogLn("<b>childs.length=" + sa.length + ";</b>", rm);
                    for (int i = 0; i < sa.length; i++) {
//    IOUtil.writeLogLn("<b>child " + i + ":" + sa[i] + ";</b>", rm);
                        sa[i] = sa[i].trim();
                        if (sa[i].length() > 0) {
                            if (!sa[i].contains(".")) {
                                sa[i] += ".cfg";
                            }
                            childs.add(sa[i]);
                        }
                    }
                }
                if (tuner.getFinalParameter("comments", "parents").length() > 0) {
                    String[] sa = tuner.getFinalParameter("comments", "parents").split(",");
                    for (int i = 0; i < sa.length; i++) {
                        sa[i] = sa[i].trim();
                        if (sa[i].length() != 0) {
                            if (!sa[i].contains(".")) {
                                sa[i] += ".cfg";
                            }
                            parents.add(sa[i]);
                        }
                    }
                }
                sections = tuner.getSectionsList(true);               
                content = StrUtil.strFromArray(cfg, "\r\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
