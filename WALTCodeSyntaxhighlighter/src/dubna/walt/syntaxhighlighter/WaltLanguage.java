/*
 */
package dubna.walt.syntaxhighlighter;

import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import dubna.walt.syntaxhighlighter.lexer.waltTokenId;

@LanguageRegistration(mimeType = "text/x-walt")
//public class WaltLanguage  {
//}

public class WaltLanguage extends DefaultLanguageConfig {

    @Override
    public Language<?> getLexerLanguage() {
        Language<?> l = waltTokenId.getLanguage();
//                return l;
        return null;
    }

    @Override
    public String getDisplayName() {
        return "cfg";
    }

    @Override
    public String getLineCommentPrefix() {
        return ";";
    }

    @Override
    public String getPreferredExtension() {
        return "cfg";
    }

    @Override
    public boolean isIdentifierChar(char c) {
        return Character.isJavaIdentifierPart(c);
    }
}
