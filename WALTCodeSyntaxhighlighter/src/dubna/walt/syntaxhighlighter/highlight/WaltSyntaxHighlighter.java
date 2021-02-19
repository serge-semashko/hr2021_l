/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dubna.walt.syntaxhighlighter.highlight;

/**
 *
 * @author Semashko
 */
import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import dubna.walt.syntaxhighlighter.codeCompletion.WaltCompletionItem;
import static dubna.walt.syntaxhighlighter.codeCompletion.WaltCompletionProvider.log2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

public class WaltSyntaxHighlighter implements CaretListener {

    private static AttributeSet defaultColors
            = AttributesUtilities.createImmutable(StyleConstants.Background,
                    new Color(236, 235, 163));
    private static AttributeSet d1;
    public static boolean fdraw = true;
    public static List<String> kwords = Collections.unmodifiableList(new ArrayList<String>() {
        {
            add("$BREAK");
            add("$SET_PARAMETERS");
            add("$INCLUDE");
            add("$GET_URL");
            add("$GET_AUTH_URL");
            add("$PRINT");
            add("$STORE_PARAMETERS");
            add("$RESTORE_PARAMETERS");
            add("$LOG_ERROR");
            add("$LOG");
            add("$GET_ID");
            add("$USE_DB");
            add("$CLOSE_DB");
            add("$TIMER") ;
            add("$WAIT") ;
            add("$GET_DATA");
            add("$EXECUTE_LOOP");
            add("$CALL_SERVICE");
            add("$COPY_FILE");
            add("$MOVE_FILE");
            add("$DELETE_FILE");
            add("$GET_FILE_SIZE");
            add("$JS ");
            add("$JS_BEGIN");
            add("$JS_{");
            add("$JS{");
            add("$JS_END");
            add("$JS_}");
            add("$JS}{");
            add("$IF");
            add("$EIF");
            add("$ENDIF");
            add("$ELSE");
            
            add("$CALL_SERVICE");
            add("$INCLUDE");
            add("$GET_DATA");
            add("$SET_PARAMETERS");
            add("$SET_PARAMETERS_SESSION");
            add("$LOG");
            add("$PRINT");
            add("$LOG1");
            add("$LOG2");
            add("$EXECUTE_LOOP");
            add("$BREAK");
            add("$JS{");
            add("$JS}");
            add("$JS_BEGIN");
        }
    });
    private String txtWord;
    String condition = "";
    int condPos = 0;

    int realLineStart;
    AttributeSet variable_color;
    private static AttributeSet keyword_color;
    private static AttributeSet url_color;
    private static AttributeSet string_color;
    private static AttributeSet field_color;
    private static AttributeSet warning_color;
    private static AttributeSet error_color;
    int startPos, endPos, lineStart, pos, shift;
    int intcchar;
    char cchar;
    int nextLineStart = 0;
    String line;

    private final OffsetsBag bag;
    private JTextComponent comp;
    private final WeakReference<Document> weakDoc;

    private final RequestProcessor rp;
    private final static int REFRESH_DELAY = 100;
    private RequestProcessor.Task lastRefreshTask;
    int curPos;

    public static void log2(String lstr) {
//        System.out.println("#WHL:" + lstr);
//        try {
//            FileWriter file = new FileWriter("f:/home/nb/log.txt");
//            String ll = lstr+"\n";
//            file.append(ll);
//            file.flush();
//            file.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    private final AttributeSet default_color;
    private final AttributeSet comment_color;

    AttributeSet getFCSColor(FontColorSettings fcs, String cname) {
        if (fcs.getFontColors(cname) != null) {
            log2("color ok:" + cname);
            return fcs.getTokenFontColors(cname);
        }
        if (fcs.getTokenFontColors(cname) != null) {
            log2("token  color ok:" + cname);
            return fcs.getTokenFontColors(cname);
        }
        log2("No color ##err:" + cname);
        return null;

    }

    void addLight(int startPos, int endPos, AttributeSet attr) {
        log2("addLight " + startPos + "-" + endPos);
        //        HighlightsSequence a = bag.getHighlights(startPos, endPos);
        //        if (a == null) {
        //            return;
        //        }

        bag.addHighlight(startPos, endPos, attr);
    }

    public WaltSyntaxHighlighter(Document doc) {
        MimePath mimePath = MimePath.parse("text/x-java");
        FontColorSettings fcs = (FontColorSettings) MimeLookup.getLookup(mimePath).lookup(FontColorSettings.class);
        log2("fcs=" + fcs.toString());

        if (fcs.getTokenFontColors("error") != null) {
            // log2("#mark# tok keyword!!");
            defaultColors = fcs.getTokenFontColors("error");
        }
        keyword_color = getFCSColor(fcs, "keyword");
        default_color = getFCSColor(fcs, "default");
        comment_color = getFCSColor(fcs, "comment");
        warning_color = getFCSColor(fcs, "warning");
        string_color = getFCSColor(fcs, "string");
        field_color = getFCSColor(fcs, "field");
        url_color = getFCSColor(fcs, "url");
        error_color = getFCSColor(fcs, "error");

//        System.out.println("#mark# WaltSyntaxHighlighter");
        rp = new RequestProcessor(WaltSyntaxHighlighter.class);
        bag = new OffsetsBag(doc);
        weakDoc = new WeakReference<Document>((Document) doc);
        DataObject dobj = NbEditorUtilities.getDataObject(weakDoc.get());
        if (dobj != null) {
            EditorCookie pane = dobj.getLookup().lookup(EditorCookie.class);
            JEditorPane[] panes = pane.getOpenedPanes();
            if (panes != null && panes.length > 0) {
                comp = panes[0];
                comp.addCaretListener(this);

            }
        }
    }

    @Override
    public void caretUpdate(CaretEvent e) {

//        if (fdraw) {
        log2("#CLEAR");
        HighlightsSequence a = bag.getHighlights(0, 999999);
//            fdraw = false;
//        }
        setupAutoRefresh();
    }

    public void setupAutoRefresh() {
        log2("setupAutoRefresh");
        if (lastRefreshTask == null) {
            lastRefreshTask = rp.create(new Runnable() {
                @Override
                public void run() {
                    txtWord = comp.getText();
                    lineStart = 0;
                    nextLineStart = 0;
                    realLineStart = 0;
                    pos = 0;
//                    for (int shf = 0; shf<20;shf+=3 ){
//                       addLight(shf, shf+2, error_color);
//                        
//                    }
                    log2("#start color ");
                    int lNumber = 0;
                    while (realLineStart < txtWord.length() - 1) {
                        lNumber++;
                        Getline();
                        log2("№" + lNumber + "(" + lineStart + ") '" + line + "'");
                        if (line.equals("")) {
                            continue;
                        }
                        if (line.length() >= 2 && line.trim().endsWith("??")) {
                            addLight(lineStart, lineStart + line.length(), comment_color);
                            continue;
                        } else {
                            addLight(lineStart, lineStart + line.length(), default_color);
                        }

                        shift = 0;
                        while ((line.length() > 0) && (line.charAt(0) == ' ')) {
                            line = line.substring(1, line.length());
                            shift++;
                        }
                        line = line.trim();
                        startPos = -1;
                        endPos = -1;
                        String vrb = line.split(" ")[0];
                        condPos = -1;
                        if ((condPos = line.indexOf("??")) > 0) {
// log2("cond pos" + condPos + " Llen=" + line.length());

                            condition = line.substring(line.indexOf("??"), line.length());
                            line = line.substring(0, line.indexOf("??") - 1);
                        }
                        if (kwords.contains(vrb)) {
// log2("kwerd" + vrb + " LE=" + lineStart + "  line= " + line);
                            curPos = line.indexOf(vrb) + vrb.length();
                            addLight(lineStart + shift, lineStart + shift + vrb.length(), keyword_color);
                            while (curPos < line.length() && (line.charAt(curPos) == ' ')) {
                                curPos++;
                            }
                            if (curPos < line.length()) {
                                if (vrb.equals("$SET_PARAMETERS")) {
                                    process_SET_PARAMETERS();
                                }
                                if (vrb.equals("$CALL_SERVICE")) {
                                    color_serviceName_in_CALL_SERVICE();
                                }
                                if (vrb.equals("$GET_DATA") || vrb.equals("$INCLUDE")) {
                                    color_sectionName_in_GET_DATA_INCLUDE();
                                }
                            }

                        }

                        if ((line.length() > 0) && (line.trim().charAt(0) == '[')) {
                            processSection();

                        }
                        if (condPos > -1) {
                            ColorCondition();
                        }
                        if (line.indexOf("#") > -1) {
                            ColorParameters();
                        }

                    }

                }

                private void Getline() {
                    if (pos == 32 && realLineStart == 32) {
                        line = "1";
                    }

                    line = "";
                    lineStart = nextLineStart;
                    realLineStart = pos;
                    if (realLineStart > txtWord.length() - 1) {
                        return;
                    }
                    intcchar = txtWord.charAt(pos);
                    cchar = txtWord.charAt(pos);
                    while (pos < txtWord.length()) {
                        if ((txtWord.charAt(pos) == '\n') || (txtWord.charAt(pos) == '\r')) {
                            break;
                        }
//                        System.out.println(pos + " #HYPER# getchar= " + txtWord.charAt(pos));
                        pos++;
                    }
                    if (pos > txtWord.length() - 1) {
                        pos--;
                    }

//                    System.out.println(txtWord.length() + " #HYPER# get line=" + lineStart + "-" + pos);
                    if (lineStart < pos) {
                        line = txtWord.substring(realLineStart, pos);
                    }
                    nextLineStart += line.length() + 1;
                    intcchar = txtWord.charAt(pos);
                    cchar = txtWord.charAt(pos);
                    if ((txtWord.charAt(pos) == '\n')) {
                        pos++;
                    } else {
                        if (pos < txtWord.length() - 2) {
// log2(pos + " #HYPER# endlineg" + (int) txtWord.charAt(pos) + " " + (int) txtWord.charAt(pos + 1));
                            if (txtWord.charAt(pos) == '\r') {
                                if (txtWord.charAt(pos + 1) == '\n') {
                                    pos += 2;

                                }
                            }
                        } else if ((txtWord.charAt(pos) == '\r')) {
                            pos++;
                        }

                    }
//                    intcchar = txtWord.charAt(pos);
//                    cchar = txtWord.charAt(pos);

//                        System.out.println(pos + " #HYPER# skip crlf");
                }

                private void processLine() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                private void process_SET_PARAMETERS() {
                    int endSect;
                    int startSect;
                    startSect = curPos;
                    endSect = line.trim().length() + startSect;
                }

                private void color_serviceName_in_CALL_SERVICE() {
                    int endSect;
                    int startSect;
                    startSect = curPos;
                    endSect = line.trim().length() + startSect;
                }

                private void color_sectionName_in_GET_DATA_INCLUDE() {
                    int endSect;
                    int startSect;
                    startSect = curPos;
                    endSect = line.trim().length() + startSect;
                    if ((line.indexOf("[") > -1) & (line.indexOf("]") > -1) & (line.indexOf("]") > line.indexOf("["))) {
                        endSect = line.indexOf("]");
                        startSect = line.indexOf("[");
                        if (endSect > 0 && startSect >= 0 && startSect < endSect - 2) {
                            addLight(lineStart + shift + startSect, lineStart + shift + endSect, string_color);

                        }
                    }
                }
                String part;
                int linePos;
                int endParam;

                private void ColorParameters() {
                    linePos = 0;
                    while (findStartParam()) {
                        int begVar = linePos - 1;
                        if (!findEndParam()) {
                            return;
                        }
                        addLight(lineStart + shift + begVar, lineStart + shift + linePos, keyword_color);

                    }
                }

                private boolean findStartParam() {
                    while ((line.length() > linePos + 3)) {
                        log2("FSP line:'" + line + "' pos:" + linePos);
                        if (line.charAt(linePos) != '#') {
                            linePos++;
                        } else {
                            if (line.charAt(linePos + 1) != '#') {
                                linePos++;
                                return true;
                            }
                            linePos += 2;
                        }
                    }
                    return false;
                }

                private boolean findEndParam() {
                    log2("FEP beg line:'" + line + "' pos:" + linePos);
                    while ((line.length() > linePos) && (line.charAt(linePos) != '#')) {
                        linePos++;
                    }
                    if (line.length() > linePos) {
                        linePos++;
                        endParam = linePos;
                        log2("FEP ret line:'" + line + "' pos:" + linePos);
                        return true;
                    }
                    log2("FEP ret false");
                    return false;
                }

            }
            );
        }
        lastRefreshTask.schedule(REFRESH_DELAY);
    }

    private void processSection() {
        startPos = line.indexOf('[');
        endPos = line.indexOf(']');
        if ((startPos == 0) && (endPos > 2)) {
            String sectName = line.substring(startPos, endPos) + ']';
//                            if (!sectName.equalsIgnoreCase("[end]") && !sectName.equalsIgnoreCase("[comments]") && !sectName.equalsIgnoreCase("[description]")) {
// log2("#HYPER section: " + sectName + (lineStart + shift + startPos) + "-" + (lineStart + shift + endPos + 1));

            addLight(lineStart + shift + startPos, lineStart + shift + endPos + 1, string_color);
//                            }
//            addLight(lineStart + shift + startPos , lineStart + shift + endPos , error_color);

        }
    }

    public OffsetsBag getHighlightsBag() {
//        System.out.println("#mark# getHighlightsBag");
        return bag;
    }

    private void ColorCondition() {
        addLight(lineStart + shift + condPos, lineStart + shift + condPos + condition.length() + 1, field_color);
    }

}
