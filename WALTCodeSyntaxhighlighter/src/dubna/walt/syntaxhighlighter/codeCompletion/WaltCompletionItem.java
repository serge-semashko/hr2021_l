/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dubna.walt.syntaxhighlighter.codeCompletion;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.windows.WindowManager;

/**
 *
 * @author Semashko
 */
public class WaltCompletionItem implements CompletionItem {

    private String text;
    char lastkey = ' ';
    private static ImageIcon fieldIcon = new ImageIcon(ImageUtilities.loadImage("16.png"));
    private static Color fieldColor = Color.decode("0x0000B2");
    private int caretOffset;
    private final int dOffset;

    public WaltCompletionItem(String text, int dotOffset, int caretOffset) {
       log2("##create '" + text + "' caretffset=" + caretOffset + " dotoffset" + dotOffset);
        if (text.length() > 0 && text.charAt(0) == '$') {
            text = text.substring(0, text.length()) + ' ';
        }
        this.dOffset = dotOffset;
        this.text = text;
        this.caretOffset = caretOffset;

    }

    @Override
    public void defaultAction(JTextComponent jtc) {
        log2("##defaultAct##");
        try {
            StyledDocument doc = (StyledDocument) jtc.getDocument();
            if (text.charAt(0) == '[') {
                if (!doc.getText(caretOffset - 1, 1).equals(" ")) {
                    text = " "+text;
                }

            }
            doc.insertString(caretOffset, text.substring(dOffset - 1), null);
//This statement will close the code completion box: 
            if (lastkey == '\n') {
                int setok = 0;
                for (String cmpWord : WaltCompletionProvider.sectWord) {
                    if (cmpWord.equals(text.trim())) {
                        setok = 1;
                    }
                }

                if (setok != 1) {
                    Completion.get().hideAll();
                }
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void processKeyEvent(KeyEvent ke) {
//        log2("##proc key event## param str=" + ke.paramString());
//        log2("##proc key event## tostr=" + ke.toString());
//        log2("##proc key event## kchar=" + ke.getKeyChar() + " kcode:" + ke.getKeyCode());
        lastkey = ke.getKeyChar();
//        if (ke.getKeyCode() == 0){
//            ke.setKeyCode(0);
//            ke.setKeyChar((char)0);
//            doc.insertString(caretOffset, ke.getKeyChar(), null);
//        }
    }

    @Override
    public int getPreferredWidth(Graphics graphics, Font font) {
//        log2("##get preffered width## text='" + text + "'");
        return CompletionUtilities.getPreferredWidth(text, null, graphics, font);
    }

    public static void log2(String lstr) {
//        System.out.println("wci:" + lstr);
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

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(fieldIcon, text, null, g, defaultFont, (selected ? Color.white : fieldColor), width, height, true);
    }

    @Override
    public CompletionTask createDocumentationTask() {
//        log2("##createDocumentationTask##");
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return null;
    }

    @Override
    public CompletionTask createToolTipTask() {
//        log2("##createToolTipTask##");
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return null;
    }

    @Override
    public boolean instantSubstitution(JTextComponent jtc) {
//        log2("##instantSubstitution##");
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return false;
    }

    @Override
    public int getSortPriority() {
//        log2("##getSortPriority##");
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return 0;
    }

    @Override
    public CharSequence getSortText() {
//        log2("##getSortedtext## text=" + text + "'");
        return text;
    }

    @Override
    public CharSequence getInsertPrefix() {
//        log2("##getinsert## text=" + text + "'");
        return text;
    }
}
