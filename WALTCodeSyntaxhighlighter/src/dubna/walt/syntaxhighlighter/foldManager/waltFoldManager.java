/*
 */
package dubna.walt.syntaxhighlighter.foldManager;

import dubna.walt.syntaxhighlighter.Codeline;
import dubna.walt.syntaxhighlighter.Codeline;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.util.Exceptions;

/**
 *
 *
 */
public class waltFoldManager implements FoldManager, Runnable {

    private FoldHierarchyTransaction transaction;
    private FoldOperation operation;
    public static final FoldType section_FOLD_TYPE = FoldType.create("...", "Section ", null);
    private int change_counter = 0;
    private int sectEnd;
    private int sectStart;
    private boolean endSection;
    private boolean StartSection;

    @Override
    public void init(FoldOperation operation) {
        log2("Init");
        this.operation = operation;
    }

    @Override
    public void initFolds(FoldHierarchyTransaction fht) {
        log2("InitFolds");
        change_counter = 0;
        this.update_folds_request(fht);

    }

    public static void log2(String lstr) {
//        System.out.println("#FM_:" + lstr);
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
    public void insertUpdate(DocumentEvent de, FoldHierarchyTransaction fht
    ) {
//        log2("InsertUpdate");
        this.update_folds_request(fht);
    }

    @Override
    public void removeUpdate(DocumentEvent de, FoldHierarchyTransaction fht
    ) {
//        log2("removeUpdate");
        //this.update_folds_request(fht);
        this.update_folds_request(fht);
    }

    @Override
    public void changedUpdate(DocumentEvent de, FoldHierarchyTransaction fht
    ) {
//        log2("changeUpdate");
        //this.update_folds_request(fht);
        this.update_folds_request(fht);

    }

    @Override
    public void removeEmptyNotify(Fold fold) {
        log2("removeEmptyNotify");
    }

    @Override
    public void removeDamagedNotify(Fold fold) {
        log2("removeDamagedNotify");
    }

    @Override
    public void expandNotify(Fold fold) {
        log2("expandNotify");

    }

    @Override
    public void release() {
        log2("release");
    }

    @Override
    public void run() {
        update_folds();
    }

    public void update_folds_request(FoldHierarchyTransaction fht) {
//        log2("update_folds_request");

        change_counter = 0;
        this.transaction = fht;
        FoldHierarchy hierarchy = operation.getHierarchy();
        Document document = hierarchy.getComponent().getDocument();
        document.render(this);

    }

    public boolean needInsert(int startFold, int endFold, ArrayList<Fold> folds, ArrayList foldsOk) {
        boolean newFold = true;
        log2("Fold range = " + startFold + " - " + endFold);
        for (Fold f : folds) {
            log2("check " + f.getStartOffset() + " - " + f.getEndOffset());
            if ((f.getStartOffset() == startFold && f.getEndOffset() == endFold)) {
                log2("Fold exists add hash = " + f.hashCode());
                newFold = false;
                foldsOk.add(f.hashCode());
                break;
            }
        }
        return newFold;
    }

    public void update_folds() {
// *System.out.println("#fold manager#  Update folds");
        log2("===============update_folds===============================");

        Iterator<Fold> iter = operation.foldIterator();
        ArrayList foldsOk = new ArrayList<>();
        ArrayList<Fold> folds = new ArrayList<>();
        while (iter.hasNext()) {
            Fold f = iter.next();
            folds.add(f);
            foldsOk.add(f);

        }
//        for (Fold f : folds) {
//            System.out.print(" [ " + f.getStartOffset() + "-" + f.getEndOffset() + "]");
//        }
//        System.out.println();

//        for (Fold f : folds) {
//            operation.removeFromHierarchy(f, transaction);
//        }
        FoldHierarchy hierarchy = operation.getHierarchy();
        Document document = hierarchy.getComponent().getDocument();

        FoldType type = null;
        int start = 0;
        int end = 0;
        int offset = 0;
        boolean in_code = false;
        ArrayList<Codeline> lines = new ArrayList<>();
        type = section_FOLD_TYPE;
//        try {
//            operation.addToHierarchy(type, 5, 10, false, FoldTemplate.DEFAULT, type.toString(), null, transaction);
//            operation.addToHierarchy(type, 20, 23, false, FoldTemplate.DEFAULT, type.toString(), null, transaction);
//        } catch (BadLocationException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//
//    }
//}

        try {
            String text = document.getText(0, document.getLength());
            InputStream is = new ByteArrayInputStream(text.getBytes());

            BufferedReader br;
            String line;

            sectStart = -1;
            br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            while ((line = br.readLine()) != null) {

                Matcher mStart = Pattern.compile("^ *\\[[\\s\\d\\w]*\\]").matcher(line);
                Matcher mEnd = Pattern.compile("^ *\\[end\\]").matcher(line);
                if (mEnd.find()) {
                    endSection = true;
                } else {
                    endSection = false;
                }
                if (mStart.find()) {
                    StartSection = true;
                } else {
                    StartSection = false;
                }

//                log2(line + " mstart = " + StartSection + " mend = " + endSection);
                if (StartSection && !endSection) {
//                    log2("sect started = " + offset);
                    sectStart = offset + line.length();;
                }
                if (sectStart > -1 && endSection) {
//                    log2("sect ended = " + offset);
                    sectEnd = offset + line.length();
                    if (needInsert(sectStart, sectEnd, folds, foldsOk)) {
                        Fold newF = operation.addToHierarchy(type, sectStart, sectEnd, false, FoldTemplate.DEFAULT, type.toString(), null, transaction);
                        folds.add(newF);
                        foldsOk.add(newF.hashCode());
                        log2("add fold");
                    } else {
//                        log2("fold exists");

                    }
                    sectStart = -1;
                }

                offset += line.length() + 1;

            }
            // create folds on every Toolchange:

            // create the fold for the codeblock before the first toolchange:
        } catch (Exception e) {

        }
        for (Fold f : folds) {
//            System.out.print(" [ " + f.getStartOffset() + "-" + f.getEndOffset() + "]");
            if (!foldsOk.contains(f.hashCode())) {
                operation.removeFromHierarchy(f, transaction);
                log2("fold delete");

            }
        }
//        System.out.println(" ###");

    }
}
