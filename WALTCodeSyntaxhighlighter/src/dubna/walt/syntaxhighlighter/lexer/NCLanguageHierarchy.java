/*
 */
package dubna.walt.syntaxhighlighter.lexer;

import java.util.*;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 */
public class NCLanguageHierarchy extends LanguageHierarchy<waltTokenId> {

    private static List<waltTokenId> tokens;
    private static Map<Integer, waltTokenId> idToToken;

    private static void init() {
        tokens = Arrays.asList(new waltTokenId[]{
            new waltTokenId("EOF", "whitespace", 0),
            new waltTokenId("WHITESPACE", "whitespace", 1),
            new waltTokenId("SINGLE_LINE_COMMENT", "URL", 2),
            new waltTokenId("START_EXPRESSION", "comment", 3),
            new waltTokenId("EXPRESSION", "expression", 4),
            new waltTokenId("endEXPRESSION", "expression", 5),
            new waltTokenId("START_PARAM_LIST", "comment", 6),
            new waltTokenId("FUNCTION_CALL", "expression", 7),
            new waltTokenId("LABEL", "comment", 9),
            new waltTokenId("AXIS", "axis", 10),
            new waltTokenId("G", "go", 11),
            new waltTokenId("M", "machine", 12),
            new waltTokenId("TOOL", "tool", 13),
            new waltTokenId("SPEED", "speed", 14),
            new waltTokenId("LPARA", "lpara", 15),
            new waltTokenId("PARA", "keyword", 16),
            new waltTokenId("PROGNR", "prognr", 17),
            new waltTokenId("KEYWORD", "keyword", 18),
            new waltTokenId("INTEGER_LITERAL", "literal", 19),
            new waltTokenId("DECIMAL_LITERAL", "literal", 20),
            new waltTokenId("FLOATING_POINT_LITERAL", "literal", 21),
            new waltTokenId("DECIMAL_FLOATING_POINT_LITERAL", "literal", 22),
            new waltTokenId("DECIMAL_EXPONENT", "literal", 23),
            new waltTokenId("IDENTIFIER", "identifier", 24),
            new waltTokenId("LETTER", "literal", 25),
            new waltTokenId("PART_LETTER", "literal", 26),
            new waltTokenId("ERROR", "error", 27)
        });
        idToToken = new HashMap<Integer, waltTokenId>();
        int i = 0;
        for (waltTokenId token : tokens) {
            //token.id = i;
            idToToken.put(token.ordinal(), token);
            i++;
        }
    }

    static synchronized waltTokenId getToken(int id) {
        if (idToToken == null) {
            init();
        }
        waltTokenId a = idToToken.get(id);
        if (a == null) {
            a = idToToken.get(27);
        }
        return a;
    }

    @Override
    protected synchronized Collection<waltTokenId> createTokenIds() {
        if (tokens == null) {
            init();
        }
        return tokens;
    }

    @Override
    protected synchronized Lexer<waltTokenId> createLexer(LexerRestartInfo<waltTokenId> info) {
        return new NCLexer(info);
    }

    @Override
    protected String mimeType() {
        return "text/x-walt";
    }

    @Override
    protected LanguageEmbedding<?> embedding(Token<waltTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
        return null;
    }

}
