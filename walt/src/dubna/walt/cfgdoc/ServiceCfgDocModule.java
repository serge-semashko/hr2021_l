package dubna.walt.cfgdoc;

import dubna.walt.util.IOUtil;
import dubna.walt.util.StrUtil;

/**
 *
 * @author serg
 */
public class ServiceCfgDocModule extends dubna.walt.service.Service {

    /**
     *
     */
    public static String cfgRootPath = "";
    private String path = "";
    private String name = "";
    private String sct = "";
    
    @Override
    public void start() throws Exception {
        cfgRootPath = cfgTuner.getParameter("CfgRootPath").replaceAll("\\\\", "/");
        path = cfgTuner.getParameter("dir");
        name = cfgTuner.getParameter("name");
        name = cfgTuner.getModFileName(name, "SIMPLE");

        sct = cfgTuner.getParameter("sct");
        IOUtil.writeLogLn(3, "<b>path=" + path + "; name=" + name + "[" + sct + "]</b>", rm);

        if (name.length() > 0) {
            if (sct.length() > 0) {
                showSection();
            } else {
                makeCfgDescription();
                cfgTuner.outCustomSection("report", out);
            }
        }
    }

    /**
     * Отображение содержимого одной секции модуля
     * 
     * @throws Exception 
     */
    private void showSection() throws Exception {
        cfgTuner.outCustomSection("sct header", out);
        String sa[] = cfgTuner.getSection(path + name, sct);
        String content = StrUtil.strFromArray(sa, "\r\n");
        content = content.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;").replaceAll("\r\n", "<br/>");
        IOUtil.writeLogLn(3, "<b>content=</b><br>" + content + "<br>[end]<hr>", rm);
        out.print(content);
        cfgTuner.outCustomSection("sct footer", out);
//        cfgTuner.addParameter("cfg_sct_html", sct);
    }
/**
 * Отображение информации о модуле
 * 
 * @throws Exception 
 */
    private void makeCfgDescription() throws Exception {

        CfgInfo cf = new CfgInfo(path + name, rm);
        if (cf.cfg != null) {
            cfgTuner.addParameter("file_found", "y");
            cfgTuner.addParameter("this_cfg_name", cf.filename);
            if (cf.comments == null) {
                cfgTuner.addParameter("cfg_comments", "Секция comments отсутствует");
                cfgTuner.addParameter("no_comments_section", "y");
            } else {
//                cfgTuner.addParameter("this_cfg_comments", cf.comments.replaceAll("\r\n", "<br/>"));
// Выбор данных из секции [comments]
                cfgTuner.addParameter("cfg_descr", "==" + cf.descr);
                cfgTuner.addParameter("cfg_input", cf.input);
                cfgTuner.addParameter("cfg_output", cf.output);
                cfgTuner.addParameter("cfg_test_url", cf.testURL);
                cfgTuner.addParameter("cfg_author", cf.author);
                cfgTuner.addParameter("cfg_service", cf.service);
            }

// Разбор параметра parents из секции [comments] - подготовка списка
            if (cf.parents.size() > 0) {
                String parents_list = "";
                for (String parent : cf.parents) {
                    String filename = "";
                    String filePath = "";
                    parent = cfgTuner.getModFileName(parent, "SIMPLE");
                    if (parent.contains("/")) {
                        filename = parent.substring(parent.lastIndexOf("/") + 1, parent.length());
                        filePath = parent.substring(0, parent.lastIndexOf("/") + 1);
                    } else {
                        filename = parent;
                    }
                    cfgTuner.setFlashParameter("parent_dir", filePath);
                    cfgTuner.setFlashParameter("parent_name", filename);
                    parents_list += cfgTuner.getCustomSectionAsString("parent_item");
                }
                cfgTuner.addParameter("parents_list", parents_list);
            }
// Разбор параметра childs из секции [comments] - подготовка списка
            if (cf.childs.size() > 0) {
                String children_list = "";
                for (String child : cf.childs) {
                    String filename = "";
                    String filePath = "";
                    child = cfgTuner.getModFileName(child, "SIMPLE");
                    if (child.contains("/")) {
                        filename = child.substring(child.lastIndexOf("/") + 1, child.length());
                        filePath = child.substring(0, child.lastIndexOf("/") + 1);
                    } else {
                        filename = child;
                    }
                    cfgTuner.setFlashParameter("child_dir", filePath);
                    cfgTuner.setFlashParameter("child_name", filename);
                    children_list += cfgTuner.getCustomSectionAsString("child_item");
                }
                cfgTuner.addParameter("children_list", children_list);
            }

// Разбор sections модуля - подготовка списка секций
            if (cf.sections.size() > 0) {
                String sections_list = "";
                for (String section : cf.sections) {
                    cfgTuner.setFlashParameter("cfg_section_line", section);
                    IOUtil.writeLogLn(3, "section: <b>" + section + "</b>", rm);
                    section = section.substring(1, section.indexOf("]"));
                    cfgTuner.setFlashParameter("cfg_section_name", section);
//                    String sectionBody = escapehtml(StrUtil.strFromArray(cf.tuner.getSection(null, section), "\n\r"));
//                    IOUtil.writeLogLn("<b>section body:</b><xmp>" + sectionBody + "</xmp>", rm);
//                    cfgTuner.setFlashParameter("cfg_section_body", sectionBody);
                    sections_list += cfgTuner.getCustomSectionAsString("section_item");
                }
                cfgTuner.addParameter("cfg_sections_list", sections_list);
            }
// Полный исходный текст модуля
//            cfgTuner.addParameter("cfg_src", cf.content);
            cfgTuner.addParameter("cfg_src_html", escapehtml(cf.content));
        }
    }

    /**
     *
     * @param in
     * @return
     */
    public String escapehtml(String in) {
        return in.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;").replaceAll("\r\n", "<br/>").replaceAll("#", "##");
//        .replaceAll("/", "&#47;")
    }
}
