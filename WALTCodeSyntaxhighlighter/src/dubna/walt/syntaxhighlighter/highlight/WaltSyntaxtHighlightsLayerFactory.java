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
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

@MimeRegistration(mimeType = "text/x-walt", service = HighlightsLayerFactory.class) 

public class WaltSyntaxtHighlightsLayerFactory implements HighlightsLayerFactory {

    public static WaltSyntaxHighlighter getMarkOccurrencesHighlighter(Document doc) {
        WaltSyntaxHighlighter highlighter =
               (WaltSyntaxHighlighter) doc.getProperty(WaltSyntaxHighlighter.class);
        if (highlighter == null) {
            doc.putProperty(WaltSyntaxHighlighter.class,
               highlighter = new WaltSyntaxHighlighter(doc));
        }
        return highlighter;
    }

    @Override
    public HighlightsLayer[] createLayers(Context context) {
        return new HighlightsLayer[]{
                    HighlightsLayer.create(
                    WaltSyntaxHighlighter.class.getName(),
                    ZOrder.CARET_RACK.forPosition(2000),
                    true,
                    getMarkOccurrencesHighlighter(context.getDocument()).getHighlightsBag())
                };
    }

}