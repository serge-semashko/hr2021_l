/*
 */
package dubna.walt.syntaxhighlighter.foldManager;
import dubna.walt.syntaxhighlighter.foldManager.waltFoldManager;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;

@MimeRegistration(mimeType="text/x-walt",service=FoldManagerFactory.class)
public class WaltFoldManagerFactory implements FoldManagerFactory {
    
    @Override
    public FoldManager createFoldManager() {
        
        log2("#%^$&^%$&^%$&^%$ fold manager#  create manager");
        return new waltFoldManager();
    }

    public static void log2(String lstr) {
//        System.out.println("#FMF:" + lstr);
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
}
