package dubna.walt.service;

import dubna.walt.util.BasicTuner;
import dubna.walt.util.FileContent;
import dubna.walt.util.IOUtil;
import dubna.walt.util.StrUtil;

/**
 *
 * @author serg
 */
public class ServiceEditFile extends Service {

    @Override
    public void beforeStart() throws Exception {
        String startPath = cfgTuner.getParameter("DataStartPath");
        if (startPath.length() < 1) {
            startPath = rm.getString("CfgRootPath");
        }
        String filePath = cfgTuner.getParameter("filePath");
        if (filePath.length() < 2) {
            filePath = cfgTuner.getParameter("dir") + cfgTuner.getParameter("name");
        }

        if (cfgTuner.enabledOption("cop=save")) {
            IOUtil.writeLogLn(2, "===> Writing file: " + startPath + filePath, rm);
            String cont = cfgTuner.getFinalParameter("FIXED_file_content");
            IOUtil.writeLogLn(3, "<hr><b>cont:</b><xmp>" + cont + "</xmp><hr>", rm);
            FileContent fc = new FileContent(cfgTuner.getFinalParameter("FIXED_file_content").getBytes(rm.getString("serverEncoding", false, "Cp1251")), "");
            fc.storeToDisk(startPath, filePath);

        } else {
            IOUtil.writeLogLn(2, "<=== Reading file: " + startPath + filePath, rm);
            try {
                String[] sa = BasicTuner.readFileFromDisk(startPath + filePath, rm.getString("serverEncoding", false, "Cp1251"), 0);
//      cfgTuner.readFile( startPath + filePath);
                String cont = "==";
                for (int i = 0; i < sa.length; i++) {
                    cont = cont + sa[i].replaceAll("<", "&lt;") + "\n";
                }
            IOUtil.writeLogLn(3, "<hr><b>cont:</b><xmp>" + cont + "</xmp><hr>", rm);
                cfgTuner.addParameter("FIXED_file_content", cont);
            } catch (Exception e) {
                cfgTuner.addParameter("FIXED_file_content", "NEW FILE");
            }
        }
        super.beforeStart();
    }

    @Override
    public void start() throws Exception {
        /*
  if (cfgTuner.enabledOption("cop=e"))
  { String s = StrUtil.replaceInString(
      cfgTuner.getParameter("des"), "<p>", "\r\n\r\n");
    cfgTuner.addParameter("des",
      StrUtil.replaceInString(s, "<br>", "\r\n"));
  }
         */

        cfgTuner.outCustomSection("report", out);
    }

}
