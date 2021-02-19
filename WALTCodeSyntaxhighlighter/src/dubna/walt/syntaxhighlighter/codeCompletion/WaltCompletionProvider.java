package dubna.walt.syntaxhighlighter.codeCompletion;

import dubna.walt.syntaxhighlighter.highlight.WaltSyntaxHighlighter;
import java.awt.Component;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;

@MimeRegistration(mimeType = "text/x-walt", service = CompletionProvider.class)

public class WaltCompletionProvider implements CompletionProvider {

    public static String[] sectWord = {"$INCLUDE", "$GET_DATA"};

    public static void log2(String lstr) {
//        System.out.println("##wcp:"+lstr);
//        try {
//            FileWriter file = new FileWriter("f:/home/nb/logccp.txt", true);
//            String ll = lstr + "\n";
//            file.append(ll);
//            file.flush();
//            file.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public CompletionTask createTask(int queryType, final JTextComponent jtc) {

        log2("createTask = " + jtc.getText());
//        throw new UnsupportedOperationException("Not supported yet.");
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            log2("create task ret NULL!!! querytype="+queryType+" != "+CompletionProvider.COMPLETION_QUERY_TYPE+" CompletionProvider.COMPLETION_QUERY_TYPE");
            return null;
        }

        return new AsyncCompletionTask(new AsyncCompletionQuery() {

            protected void addSections(Document document, CompletionResultSet cRS, int caretOffset) {
                String txtWord, sectName;
                String[] sections = {};
                log2("addsect in");

                try {
                    int startName, endName;
                    txtWord = document.getText(0, document.getLength());
                    String[] lines = txtWord.split("\n");
//                    log2("lines count " + Integer.toString(lines.length));
                    for (String line : lines) {
                        log2("sect line:" + line);
                        line = line.trim();
                        startName = line.indexOf('[');
                        endName = line.indexOf(']');
                        if ((startName == 0) && (endName > 2)) {
                            sectName = line.substring(startName, endName + 1);
                            log2("sect sect:" + sectName);
                            if (!sectName.equalsIgnoreCase("[end]") && !sectName.equalsIgnoreCase("[comments]") && !sectName.equalsIgnoreCase("[description]")) {
                                cRS.addItem(new WaltCompletionItem(sectName, 1, caretOffset));
                            }

                        }
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            @Override
            protected void query(CompletionResultSet completionResultSet, Document document, int caretOffset) {

//Iterate through the available locales 
//and assign each country display name 
//to a CompletionResultSet:
//                Document d1 = jtc.getDocument();
                Document d1 = document;
                String dstr1;
                try {
                    dstr1 = jtc.getDocument().getText(0, jtc.getDocument().getLength());
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                String s1;
                Boolean setok = false;
                log2("QUERY START doc len = " + Integer.toString(jtc.getDocument().getLength()) + "caretOffset = " + Integer.toString(caretOffset));
                if (jtc.getDocument().getLength() > 0) {
                    s1 = getFromStarOffLine(jtc.getDocument(), caretOffset-1);
                    log2("LFS '"+s1+"' l="+s1.length());
                    String s1ltrim = s1.replaceAll("^\\s+", "");
                    //                        log2("s1 = " + s1);
                    if ((s1ltrim.length() == 0) || ((s1ltrim.length() != 0) && (s1ltrim.charAt(0) != '$'))) {
                        completionResultSet.finish();
                        return;
                    }
                    setok = false;
                    for (String cmpWord : sectWord) {
                        if (cmpWord.equals(s1.trim())) {
                            setok = true;
//                                    log2("addsect");
                            addSections(d1, completionResultSet, caretOffset);
                        }
                    }
                    if (!setok) {
                        for (String kword : WaltSyntaxHighlighter.kwords) {
                            if (kword.startsWith(s1ltrim)) {
                                
                                completionResultSet.addItem(new WaltCompletionItem(kword, s1ltrim.length()+1, caretOffset));
                            }
                        }
                    }

                }
                completionResultSet.finish();
            }

        },
                jtc
        );
    }

    private String getFromStarOffLine(Document doc, int beforeCaret) {
        String line = "";
        int curpos = beforeCaret ;
//        log2("GFSTL start doclen = "+doc.getLength()+" curpos = "+curpos);
        try {
            if (curpos > doc.getLength() - 1 || (checkEOL(doc.getText(curpos, 1).charAt(0)))) {
                return "";
            }
            while ((curpos > 0) && (!checkEOL(doc.getText(curpos, 1).charAt(0)))) {
//                System.out.println(curpos + " #HYPER back#=" + doc.getText(curpos, 1).charAt(0) + " = " + String.valueOf((int) doc.getText(curpos, 1).charAt(0)));
                curpos--;
            }
            
            if (beforeCaret != curpos) {
                // Сдвигались назад до начала файла или конца строли.
                if (checkEOL(doc.getText(curpos, 1).charAt(0))) {
//                    Если сдвигалсись до концп строки - надо сдвинуться от него вперед
                    curpos++;
                }
            } else {
                //были в начале файла
                return doc.getText(curpos, 1);
            }
            line = doc.getText(curpos, beforeCaret - curpos + 1);
            //            System.out.println("#HYPER# start=" + startOffset + " len=" + endOffset + " line='" + line + "'");
            if ((line.trim().length() == 0) || (line.trim().charAt(0) != '$')) {
                line = "";
                log2("GFSOL1:'"+line+"'");
                return line;
                
            }
                log2("GFSOL2:'"+line+"'");
            return line;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return "";
        }
    }

    private boolean checkEOL(char charAt) {
        if ((charAt == '\n') || (charAt == '\r')) {
            return true;
        }
        return false;
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        log2("typed " + typedText);
        if (!typedText.equals("$")){
            return 0;
        }
        JTextComponent jtc = component;
        Document doc = component.getDocument();
        String curstr = getFromStarOffLine(doc, jtc.getCaretPosition()-1);
        log2("line full= '"+curstr+"' l="+curstr.length()+" co="+jtc.getCaretPosition());
        String s1 = curstr.trim();
        log2("line wci proc='"+s1+"' ln="+s1.length());
        if (s1.length() == 0 || s1.charAt(0) != '$') {
            
            log2(" ret 0 1");
            return 0;
        }
        if (s1.charAt(0) == '$' && s1.length() == 1) {
            log2(" ret 1 1");

            return 1;
        } 
            log2(" ret 0 2");
        return 0;
//        ПРовека что ы начала стоит $INCLUDE или $GET_DATA и выдача списка секций
//        else {
//            int setok = 0;
//            for (String cmpWord : sectWord) {
//                if (cmpWord.equals(s1.trim())) {
//                    setok = 1;
//                    
//                }
//            }
//            return setok;
//        }
    }
}
