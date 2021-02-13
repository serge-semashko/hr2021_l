package dubna.walt.service;

import javax.servlet.*;
import dubna.walt.util.FileContent;

/**
 *
 * @author serg
 */
public class ServiceFileData extends dubna.walt.service.Service {

    public void start() throws Exception {
        String file_path = cfgTuner.getParameter("FILE_PATH");
        String content_type = cfgTuner.getParameter("CONTENT_TYPE");
        String file_name = cfgTuner.getParameter("NAME");
        System.out.println("*** file_path:" + file_path);

        if (content_type.length() < 2) {
            ServletContext sc = (ServletContext) rm.getObject("ServletContext");
            content_type = sc.getMimeType(file_path);
        }
        if (file_name.length() < 1) {
            int i = file_path.lastIndexOf("/");
            file_name = file_path.substring(i + 1);
        }
        long file_size = FileContent.getFileSize(file_path);

        System.out.println("ContentType:" + content_type + "; File Name=" + file_name + "; size=" + Long.toString(file_size));

        if (file_size > 0) {
            ServletOutputStream outStream = (ServletOutputStream) rm.getObject("outStream");
            response.setContentType(content_type);
            response.setHeader("Content-Disposition", "attachment; filename=" + file_name);
            response.setHeader("Content-length", Long.toString(file_size));
            outStream.flush();

            FileContent.copyFileData(file_path, outStream);
            outStream.flush();
            outStream.close();
        } else {
            cfgTuner.addParameter("ERROR", "Файл не найден! fs=" +  Long.toString(file_size));
        }

    }
}
