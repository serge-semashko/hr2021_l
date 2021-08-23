package dubna.walt.service;

import dubna.walt.util.IOUtil;

/*
 * Сервис загрузки файла в ФС
 * Автор: Куняев
 */
public class ServiceUploadFile extends dubna.walt.service.Service{
    

    /**
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        try {
            cfgTuner.getCustomSection("report header");
            if (!cfgTuner.enabledExpression("ERROR")) {
                dubna.walt.util.FileContent fc = (dubna.walt.util.FileContent) rm.getObject("new_file_CONTENT");
                fc.rm = rm;
                fc.storeToDisk(cfgTuner.getParameter("FILE_PATH"), cfgTuner.getParameter("FILE_NAME"));
                if(cfgTuner.enabledExpression("FILE_COPY_PATH&file_copy_path"))
                    fc.storeToDisk(cfgTuner.getParameter("FILE_COPY_PATH"), cfgTuner.getParameter("FILE_NAME"));
                cfgTuner.addParameter("file_size", Long.toString(fc.getFileSize()));
            } 

        } catch (Exception e) {
            cfgTuner.addParameter("ERROR", e.getLocalizedMessage());
            e.printStackTrace(System.out);

        }
        cfgTuner.outCustomSection("report footer", out);
        try {
            out.flush();
            out.close();
        } catch (Exception ex) {
            IOUtil.writeLogLn(0, "<font color=red> CLOSE OUT ERROR: " + ex.toString() + "</font>", rm);
        }

    }

}
