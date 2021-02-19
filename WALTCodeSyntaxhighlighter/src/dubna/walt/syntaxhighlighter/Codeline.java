
 
package dubna.walt.syntaxhighlighter;

/**
 *
 * 
 */
public class Codeline {
     public enum Type {
        // %MPF1000
        PROGNR,
        // T5
        TOOLCHANGE,
        // Comments like: ( hallo ) or ;hallo
        COMMENT,
        // other code
        CODE
    }
    public Type type;
    public String text;
    public int begin;
    public int end; // Linebreak is not included
   
}
