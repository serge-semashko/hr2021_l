/*
 */
package dubna.walt.syntaxhighlighter.lexer;

import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
class NCLexer implements Lexer<waltTokenId> {

    private LexerRestartInfo<waltTokenId> info;
//    private NcParserTokenManager ncParserTokenManager;
    private int curTokenId = 1;
    private boolean newLine = true;

    NCLexer(LexerRestartInfo<waltTokenId> info) {
        this.info = info;
// *System.out.println("ncLexer init ");
//        JavaCharStream stream = new JavaCharStream(info.input());
//        ncParserTokenManager = new NcParserTokenManager(stream);
    }

    void skipSpace() {
        int a;
        while ((char)(a =  info.input().read()) == ' ') {
// *System.out.println("skip read='" + (char)a + "' read len = " + info.input().readLength());
        }
        info.input().backup(1);
    }

    @Override
    public org.netbeans.api.lexer.Token<waltTokenId> nextToken() {
//        return null;
        int s = -10;
        String tkn = "";
        skipSpace();
//        if (info.input().readLength() > 0) {
//            return null;
//        }
        while (!info.input().consumeNewline()) {
            s = info.input().read();
            if (s < 0) {
                if (info.input().readLength() > 0) {
                    break;
                }
// *System.out.println("NULL read='" + s + "' read len = " + info.input().readLength() + "' Token id = " + curTokenId);
                info.input().backup(1);
                return null;
            }
            if ((char) s == ' ') {
                break;
            }
            tkn = tkn + (char) s;
        }
// *System.out.println(newLine+ " read token='" + tkn + "' read len = " + info.input().readLength() + "' Token id = " + curTokenId);
        if (info.input().readLength() < 1) {
            s = info.input().read();
        }
        curTokenId = ((curTokenId + 1) % 30);
        if (curTokenId == 0) {
            curTokenId = 1;
        }
        org.netbeans.api.lexer.Token rettoken;
// *System.out.println("read len = " + info.input().readLength() + "' Token id = " + curTokenId);
        return info.tokenFactory().createToken(NCLanguageHierarchy.getToken(1));
//        System.out.println("retToken='" + rettoken.length());
//        return rettoken;

    }

    @Override
    public Object state() {
        return null;
    }

    @Override
    public void release() {
    }

}
