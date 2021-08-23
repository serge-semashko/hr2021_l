package dubna.walt.util;

import java.math.*;
import java.util.StringTokenizer;

/**
 * Contains a set of useful static methods to process strings;
 */
public class StrUtil {

    /**
     * Retrieves a token from the source string.
     *
     * Parses the source string into tokens using delimiter characters,
     * specified in String "delim" and returns the token number "tokenNr"
     *
     * @param src the source string;
     * @param tokenNr number of the token to be obtained starting from 1;
     * @param delim the delimiter's characters, concatenated in string.
     * @return token Nr. "tokenNr" or empty string if there is no such a token.
     */
    public static String getToken(String src, int tokenNr, String delim) {
        StringTokenizer st = new StringTokenizer(src, delim);
        String s = "";
        int i = 1;
//  System.out.println("... src: '" + src + "' tokenNr='" + tokenNr + "' delim='" + delim + "'");
        while (st.hasMoreTokens()) {
            s = st.nextToken();
//  System.out.println("... i: '" + i + "' token='" + s + "'");
            if (i++ == tokenNr) {
                return s.trim();
            }
        }
        return "";
    }

    /**
     *
     * @param strB
     * @param strA
     * @param maxTestLength
     * @return
     */
    public static int fuzzyMatch(String strB, String strA, int maxTestLength) {
        if (strA == null || strB == null) {
            return 0;
        }
        byte[] a = strA.getBytes();
        byte[] b = strB.getBytes();

        if (a.length == b.length
                && StrUtil.indexOf(a, 0, a.length, b, 0, b.length) == 0) {
            return 100;
        }

        int m1 = 0;
        int m2 = 0;
        int maxLen = maxTestLength;
        if (maxLen > a.length) {
            maxLen = a.length;
        }
        if (maxLen > b.length) {
            maxLen = b.length;
        }
        if (maxLen == 0) {
            return 0;
        }
        int minLen = 3;
        if (minLen >= maxLen / 2) {
            minLen = 1;
        }

        //  int currLen=0; текущая длина подстроки
        for (int currLen = minLen; currLen <= maxLen; currLen++) {
            for (int z = 0; z < a.length - currLen; z++) {
                if (StrUtil.indexOf(b, 0, b.length - 1, a, z, currLen) >= 0) {
                    m1 += 1;
                }
            }
            for (int z = 0; z < b.length - currLen; z++) {
                if (StrUtil.indexOf(a, 0, a.length - 1, b, z, currLen) >= 0) {
                    m1 += 1;
                }
            }
            m2 += a.length + b.length - 2 * currLen;
        }

        if (m2 == 0) {
            return 0;
        }
        return (m1 * 100 / m2);
    }

    /**
     * @param source the characters being searched.
     * @param sourceOffset offset of the source string.
     * @param sourceCount count of the source string.
     * @param target the characters being searched for.
     * @param targetOffset offset of the target string.
     * @param targetCount count of the target string.
     * @return 
     */
    public static int indexOf(byte[] source, int sourceOffset, int sourceCount,
            byte[] target, int targetOffset, int targetCount) {
        byte first = target[targetOffset];
        int max = sourceOffset + (sourceCount - targetCount);
        int j;
        int end;
        int k;

        for (int i = sourceOffset; i <= max; i++) {
            /* Look for first character. */
            if (source[i] != first) {
                while (++i <= max && source[i] != first);
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                j = i + 1;
                end = j + targetCount - 1;
                for (k = targetOffset + 1; j < end && source[j]
                        == target[k]; j++, k++);

                if (j == end) {
                    /* Found whole string. */
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }

    /**
     * Converts the source string to unicode.
     *
     * @param s the source string
     * @return the Unicode string
     * @deprecated not necessary since JDK 1.3
     */
    public static String XXXunicode(String s) {
//  if ((System.getProperty("java.version")).compareTo("1.3") >= 0) return s;

        if (s == null || s.length() == 0) {
            return "";
        }
        int n = s.length();
        byte[] b = new byte[n];
        for (int i = 0; i < n; i++) {
            b[i] = (byte) s.charAt(i);
        }
        return (new String(b)).replace('ї', 'я');
    }

    /**
     * Obtains an Integer, containing in a certain position in a string.
     *
     * @param s the source string
     * @param i1 start index of the integer;
     * @param i2 end index of the integer;
     * @return Integer, containing in the source substring(i1,i2+1) or -1 if
     * this substring does not represent an integer value.
     * @deprecated getLeadingInt should be used.
     */
    static public Integer getInteger(String s, int i1, int i2) {
        int i = 0;
        String s1 = s.substring(i1, i2 + 1).trim();
        if (s1.length() > 0) {
            try {
                i = Integer.parseInt(s1);
            } //        catch (NumberFormatException e) {
            catch (Exception e) {
                e.printStackTrace(System.out);
                i = -1;
            }
        }
        return new Integer(i);
    }

    /**
     * Obtains an "int" value, containing in a certain position in a string.
     *
     * @param s the source string
     * @param i1 start index of the integer;
     * @param i2 end index of the integer;
     * @return int, containing in the source substring(i1,i2+1) or -1 if this
     * substring does not represent an integer value.
     * @deprecated getLeadingInt should be used
     */
    static public int getInt(String s, int i1, int i2) {
        return getInteger(s, i1, i2).intValue();
    }

    /**
     * Parses a string and obtaines a positive "int" value, containing at the
     * beginning of the string.
     *
     * @param s the source string
     * @return the starting integer or "-1" if the source string does not starts
     * with an integer number.
     */
    public static int getLeadingInt(String s) {
        int i = 0;
        while (isDigit(s.charAt(i))) {
            i++;
        }
        if (i > 0) {
            return (Integer.valueOf(s.substring(0, i))).intValue();
        } else {
            return -1;
        }
    }

    /**
     * Obtains an "int" value, containing at the beginning of a string.
     *
     * The starting digits must be separated from the rest of the string by ":"
     * or by space.
     *
     * @param s the source string.
     * @return int the starting integer or "-1".
     * @deprecated getLeadingInt should be used instead
     */
    static public int getInt(String s) {
        try {
            int i = s.indexOf(":");
            if (i <= 0) {
                i = s.indexOf(" ");
            }
            if (i > 0) {
                return new Integer(s.substring(0, i).trim()).intValue();
            } else {
                return -1;
            }
        } catch (Exception ex) {
//    System.out.println(ex.toString());
            return -1;
        }
    }

    /**
     * Converts a String array to a String.
     *
     * Concatenates elements of the array, separating them by space.
     *
     * @param sa the String Array to be converted
     * @param endString
     * @return String the resulting String.
     */
    static public String strFromArray(String[] sa, String endString) {
        if (sa == null) {
            return "";
        }
        StringBuilder s = new StringBuilder(1024);
        for (int i = 0; i < sa.length; i++) {
            s.append(sa[i]).append(endString);
        }
        return s.toString();
    }

    /**
     * Converts a String array to a String.
     *
     * Concatenates elements of the array, separating them by space.
     *
     * @param sa the String Array to be converted
     * @return String the resulting String.
     */
    static public String strFromArray(String[] sa) {
        return strFromArray(sa, " ");
    }

    /**
     * Creates a string presentation of a double value.
     *
     * Rounds the value according to the number of decimal digits to be shown.
     * Adds tailing zeros if necessary.
     *
     * @param d the source double value;
     * @param numFractialDigits number of digits after the decimal point;
     * @param thousendsSeparator the character to be used to separate thousends
     * @return 
     */
    static public String formatDouble(double d, int numFractialDigits, String thousendsSeparator) {
        if (numFractialDigits >= 20) {
            return Double.toString(d);
        }
        String s = "xxx";
        int startPos = 0, dotPos = 0, i = 0;
        int numFract = numFractialDigits;
        if (numFract == 0) {
            numFract = -1;
        }
        try {
            double delta = 5.;
            if (d < 0.) {
                delta = -delta;
                startPos = 1;
            }
            for (i = 0; i <= numFract; i++) {
                delta = delta / 10.;
            }
            if (numFract < 0) {
                delta = delta / 10.;
            }
            d += delta;
//        if (d >0.) d = d+(); else d = d-(5.E-3);
            BigDecimal b = new BigDecimal(d);
            s = b.toString();
            dotPos = s.indexOf(".");
            if (dotPos < 0) {
                s = s.concat(".00000");
                dotPos = s.indexOf(".");
            }
            s = s.concat("0000").substring(0, dotPos + 1 + numFract);
            if(!thousendsSeparator.equals("empty") && !thousendsSeparator.equals("none")) {
                StringBuilder sb = new StringBuilder(20).append(s);
                for (i = dotPos - 3; i > startPos; i -= 3) // tmp - для Вали
                {
                    sb.insert(i, thousendsSeparator);  // tmp - для Вали
                }
//    if (numFract == 0) sb.setLength(sb.length()-1);
                s = sb.toString();
            }
            if (s.equals("-0.00")) {
                s = "0.00";
            } else if (s.equals("-0")) {
                s = "0";
            }
            return s;
        } catch (Exception x) {
            System.out.println(x.toString());
            System.out.println("StrUtil/doubleToString d =" + d + " dotPos=" + dotPos);
//        System.out.println("StrUtil/doubleToString d =" + d + " jj =" + jj + " dotPos=" + dotPos);
        }
        return s;
    }

    /**
     * Obtains the word (token) from a string
     *
     * @param src
     * @param wordNumber
     * @param comma
     * @return 
     * @deprecated StringTokenizer shoud be used.
     */
    public static String getWord(String src, int wordNumber, char comma) {
        int currWordNr = 0, j;
        for (int i = 0; i < src.length(); i++) {
            if (i == 0 || isWordDelimiter(src.charAt(i), comma)) {
                if (i > 0) {
                    i++;
                }
                currWordNr++;
                if (currWordNr == wordNumber) {
                    for (j = i + 1; j < src.length() && !isWordDelimiter(src.charAt(j), comma); j++);
                    return src.substring(i, j);
                }
            }
        }
        return "";
    }

    /**
     *
     * @param src
     * @return
     */
    public static String initCap(String src) {
        StringTokenizer st = new StringTokenizer(src, " ");
        StringBuilder sb = new StringBuilder(src.length());
        String s = "";
        while (st.hasMoreTokens()) {
            s = st.nextToken();
            sb.append(s.substring(0, 1).toUpperCase()
                    + s.substring(1).toLowerCase() + " ");
        }
        return sb.toString();
    }

    /**
     *
     * @param c
     * @param delim
     * @return
     */
    public static boolean isWordDelimiter(char c, char delim) {
        return ((c == ' ') || (c == delim));
//    return ((c == ' ') || (c == ','));
    }

    /**
     * Returns a double value presented by a string.
     *
     * @param s the source string.
     * @return the double value presented by the string.
     * @exception java.lang.Exception
     */
    static public double parseDouble(String s) throws Exception {
        double d = Double.valueOf(s).doubleValue();
        return d;
    }

    /**
     * Checks if a string represents a double value
     *
     * @param s the source string.
     * @return true if s represents a double value, false otherwise.
     */
    static public boolean isNumber(String s) {
        try {
            Double d = new Double(s.trim());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Checks if a string represents an integer value
     *
     * @param s the source string.
     * @return true if s represents an integer value, false otherwise.
     */
    static public boolean isInteger(String s) {
        try {
            Integer i = new Integer(s.trim());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     *
     * @param in
     * @return
     */
    public static String escapeHtml(String in) {
        return in.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;").replaceAll("/", "&#47;").replaceAll("\r\n", "<br/>").replaceAll("#", "##");
    }

    /*
static public String escape(String s){
//    String s2=s;
    String s2=replaceInString(s,"%","%25");
//    s2=replaceInString(s2,"&","%26");
//    s2=replaceInString(s2,"=","%3D");
    s2=replaceInString(s2," ","%20");
//    System.out.println(s+" / " + s2);
    return s2;
}
     */
    /**
     * Finds the index of the string which contains a token.
     *
     * @param sa The input String array.
     * @param token The token to be found in the array.
     * @param startIndex The starting index of the string in the input array.
     * @return The index of the string in the input "sql" array, which contains
     * the token.<br>
     *
     * -1 if the token was not found inside sql[i], where i >= startIndex,
     *
     */
    static public int indexOfToken(String[] sa,
            String token,
            int startIndex) {
        String tOKEN = token.toUpperCase();
        int i = startIndex;
        for (; i < sa.length
                && sa[i] != null
                && sa[i].toUpperCase().indexOf(tOKEN) < 0; i++) ;

        if (i >= sa.length) {
            return -1;
        }
        return i;
    }

    /**
     * Replaces substring of the input string by another substring
     *
     * @param src the input String
     * @param what the substring to be replaced
     * @param byWhat the string to put instead of "what"
     *
     * @return String the resulting string Note: "convertStringToHTML()" and
     * "convertStringToJava()" both depend on this function.
     */
    static public String replaceInString(String src, String what, String byWhat) {
        if (src == null || what == null || byWhat == null) {
            return src;
        }

        int lwhat = what.length();
        if (lwhat == 0) {
            return src;
        }
        StringBuilder result = new StringBuilder(src.length() * 2);

        int beg = 0;
        for (int i = src.indexOf(what);
                i >= 0;
                i = src.indexOf(what, i + lwhat)) {
            result.append(src.substring(beg, i) + byWhat);
            beg = i + lwhat;
        }
        result.append(src.substring(beg));
        return result.toString();
    }

    /**
     * Replaces case-incensitive substring of the input string by another
     * substring.
     *
     * @param src the input String
     * @param what the substring to be replaced
     * @param byWhat the string to put instead of "what"
     *
     * @return String the resulting string Note: "convertStringToHTML()" and
     * "convertStringToJava()" both depend on this function.
     */
    static public String replaceIgnoreCase(String src, String what, String byWhat) {
        if (src == null || what == null || byWhat == null) {
            return src;
        }
        String srcU = src.toUpperCase();
        String whatU = what.toUpperCase();

        int lwhat = what.length();
        if (lwhat == 0) {
            return src;
        }
        StringBuilder result = new StringBuilder(src.length() * 2);

        int beg = 0;
        for (int i = srcU.indexOf(whatU);
                i >= 0;
                i = srcU.indexOf(whatU, i + lwhat)) {
            result.append(src.substring(beg, i) + byWhat);
            beg = i + lwhat;
        }
        result.append(src.substring(beg));
        return result.toString();
    }

    /**
     * Adds extra-strings before and after the last accurance of a substring in
     * the source string. Case insensitive.
     *
     * Usefull, f.ex., if it is necessary to mark a search substring in the HTML
     * output.
     *
     * @param src the source string;
     * @param substr the substring to be marked;
     * @param before the string, which must be inserted before "substr";
     * @param after the string, which must be inserted after "substr";
     * @return 
     */
    public static String markSubstr(String src, String substr, String before, String after) {
        if (src == null || substr == null) {
            return "";
        }
        if (src.length() == 0 || substr.length() == 0) {
            return src;
        }

        String s = src.toUpperCase();
        String su = substr.toUpperCase();
        int i = s.lastIndexOf(su);
        if (i < 0) {
            return src;
        }
        int l = su.length();
        return (src.substring(0, i) + before + src.substring(i, i + l) + after + src.substring(i + l));
    }

    /**
     * Replaces ' ', '&', '<', '>', '"', ''', '\' in the input string by special
     * HTML tags.
     *
     * @param src the input String
     *
     * @return String the resulting string
     */
    static public String convertStringToHTML(String src) {
        String result = replaceInString(src, "&", "&amp;");
        result = replaceInString(result, "<", "&lt;");
        result = replaceInString(result, ">", "&gt;");
        result = replaceInString(result, " ", "&nbsp;");
        result = replaceInString(result, "\"", "&quot;");
        result = replaceInString(result, "'", "&#39;");
        result = replaceInString(result, "\\", "&#92;");
        return result;
    }

    /**
     * Replaces '"', ''', '\', '&', '%', '#' in the input string<br>
     * by '\"', '\\x27', '\\\\', '\\x38', '\\x37', '\\x35'.
     *
     * @param src the input String
     *
     * @return String the resulting string
     */
    static public String convertStringToJava(String src) {
//    String result="";
//    for(int i = 0,l=src.length();i<l;i++) result+="\\x"+Integer.toHexString((int)src.charAt(i)) ;
        String result = replaceInString(src, "\\", "\\\\");
        result = replaceInString(result, "\'", "\\x27");
        result = replaceInString(result, "\"", "\\x22");
        result = replaceInString(result, "&", "\\x26");
        result = replaceInString(result, "%", "\\x25");
        result = replaceInString(result, "#", "\\x23");
        result = replaceInString(result, "\n", "\\n");
        result = replaceInString(result, "\f", "\\f");
        result = replaceInString(result, "\r", "\\r");
        result = replaceInString(result, "\t", "\\t");
        result = replaceInString(result, "\b", "\\b");
        result = replaceInString(result, "<", "\\x3C");
        result = replaceInString(result, ">", "\\x3E");
        return result;
    }

    /**
     * Compares two strings, which may represent integer values.
     *
     * If both strings start with integer values - compares these values.
     * Otherwise - performs normal literal string comparision.
     *
     * @param s1 the string 1
     * @param s2 thw string 2
     * @return true if s1 > s2, false otherwise.
     */
    public static boolean compareIntStr(String s1, String s2) {
        int i1 = getLeadingInt(s1);
        if (i1 >= 0) {
            int i2 = getLeadingInt(s2);
            if (i2 >= 0) {
                return (i1 > i2);
            }
        }
        return (s1.compareTo(s2) > 0);
    }

    /**
     * Checks if the character represents a digit.
     *
     * @param c the source character.
     * @return true if c is a digit, false - otherwise.
     */
    public static boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    /**
     * Analog of JavaScript unescape function
     *
     * @param str the source string.
     * @return the unescaped string.
     */
    public static String unescape(String str) {
        return unescape(str, "WINDOWS-1251");
    }

    /**
     * Analog of JavaScript unescape function
     *
     * @param str the source string.
     * @param charsetName the name of the characterset for UNICODE characters
     * conversion.
     * @return the unescaped string.
     */
    public static String unescape(String str, String charsetName) {
        if (str == null) {
            return null;
        }
        int l = str.length();
        if (l < 3) {
            return str;
        }
//  System.out.println("unescape:'"+str+"'");

        char c1, c2, c3, c4;
        StringBuilder result = new StringBuilder();
        byte[] res = new byte[1];
        int j = 0;
        try {
            for (int i = 0; i < l; ++i) {
                char c = str.charAt(i);
                if (c == '%' && i < l - 2) {
                    c1 = str.charAt(i + 1);
                    c2 = str.charAt(i + 2);
                    if (c1 == 'u' && i < l - 5) {
                        c1 = c2;
                        c2 = str.charAt(i + 3);
                        c3 = str.charAt(i + 4);
                        c4 = str.charAt(i + 5);
                        if (isHexit(c1) && isHexit(c2) && isHexit(c3) && isHexit(c4)) {
                            res[0] = (byte) (hexit(c1) * 4096
                                    + hexit(c2) * 256
                                    + hexit(c3) * 16
                                    + hexit(c4));
                            result.append(new String(res, 0, 1, charsetName));

                            /*          result.append( (char) 
		        ( hexit(c1)*4096 
		        + hexit(c2)*256 
		        + hexit(c3)*16 
		        + hexit(c4)));
                             */
                            i += 5;
                        }
                    } else if (isHexit(c1) && isHexit(c2)) {
//        System.out.println( c1 + c2 + ":" + hexit(c1) + " " + hexit(c2));
                        res[0] = (byte) (hexit(c1) * 16 + hexit(c2));
                        result.append(new String(res, 0, 1, charsetName));
                        //       result.append( (char) (hexit(c1)*16 + hexit(c2)));
                        i += 2;
                    } else //       res[j++] = c;
                    {
                        result.append(c);
                    }
                } else //      res[j++] = c;
                {
                    result.append(c);
                }
            }
        } catch (Exception e) {
            result.append(e.toString());
        }
//  return (r);
        return result.toString();
    }

    /**
     * Analog of JavaScript unescape function
     *
     * @param str the source string.
     * @return the unescaped string.
     */
/*    
    public static String ZZZ_unescape(String str) {
        if (str == null) {
            return null;
        }
        int l = str.length();
        if (l < 3) {
            return str;
        }
//  System.out.println("unescape:'"+str+"'");

        char c1, c2, c3, c4;
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < l; ++i) {
            char c = str.charAt(i);
            if (c == '%' && i < l - 2) {
                c1 = str.charAt(i + 1);
                c2 = str.charAt(i + 2);
                if (c1 == 'u' && i < l - 5) {
                    c1 = c2;
                    c2 = str.charAt(i + 3);
                    c3 = str.charAt(i + 4);
                    c4 = str.charAt(i + 5);
                    if (isHexit(c1) && isHexit(c2) && isHexit(c3) && isHexit(c4)) {
                        result.append((char) (hexit(c1) * 4096
                                + hexit(c2) * 256
                                + hexit(c3) * 16
                                + hexit(c4)));
                        i += 5;
                    }
                } else if (isHexit(c1) && isHexit(c2)) {
                    System.out.println(c1 + c2 + ":" + hexit(c1) + " " + hexit(c2));
                    result.append((char) (hexit(c1) * 16 + hexit(c2)));
                    i += 2;
                } else {
                    result.append(c);
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
/**/
    
    /**
     * Checks if the character represents a hexadecimal digit.
     *
     * @param c the source character.
     * @return true if c is a hexadecimal digit, false otherwise.
     */
    public static boolean isHexit(char c) {
        String legalChars = "0123456789abcdefABCDEF";
        return (legalChars.indexOf(c) != -1);
    }

    /**
     *
     * @param hex
     * @return
     */
    public static int hexToInt(String hex) {
        try { // BigInteger bi = new BigInteger(hex, 16);
            return (new BigInteger(hex, 16)).intValue();
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Converts a hexadecimal character to the corresponding int value. Case
     * insencitive.
     *
     * @param c the source hexadecimal character.
     * @return the corresponding int value or 0 if the source value is not a
     * hexadecimal character.
     */
    public static int hexit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        return 0;	// shouldn't happen, we're guarded by isHexit()
    }

    /**
     * Turns a String into an array of Strings splitted at whitespaces.
     * @param str
     * @return 
     */
    public static String[] splitStr(String str) {
        StringTokenizer st = new StringTokenizer(str);
        int n = st.countTokens();
        String[] strs = new String[n];
        for (int i = 0; i < n; ++i) {
            strs[i] = st.nextToken();
        }
        return strs;
    }

    /**
     * Turns a String into an array of Strings, by splitting it at the specified
     * character.
     *
     * This method does not use StringTokenizer, and therefore can handle empty
     * fields.
     * @param str
     * @param delim
     * @return 
     */
    public static String[] splitStr(String str, char delim) {
        int n = 1;
        int index = -1;
        while (true) {
            index = str.indexOf(delim, index + 1);
            if (index == -1) {
                break;
            }
            ++n;
        }
        String[] strs = new String[n];
        index = -1;
        for (int i = 0; i < n - 1; ++i) {
            int nextIndex = str.indexOf(delim, index + 1);
            strs[i] = str.substring(index + 1, nextIndex);
            index = nextIndex;
        }
        strs[n - 1] = str.substring(index + 1);
        return strs;
    }

    /**
     * Checks whether a string matches a given wildcard pattern. Only does ? and
     * *, and multiple patterns separated by |.
     * @param pattern
     * @param string
     * @return 
     */
    public static boolean match(String pattern, String string) {
        for (int p = 0;; ++p) //pattern index
        {
            for (int s = 0;; ++p, ++s) //string index
            {
                boolean sEnd = (s >= string.length());
                boolean pEnd = (p >= pattern.length() || pattern.charAt(p) == '|');
                if (sEnd && pEnd) {
                    return true;
                }
                if (sEnd || pEnd) {
                    break;
                }
                if (pattern.charAt(p) == '?') {
                    continue;
                }
                if (pattern.charAt(p) == '*') {
                    int i;
                    ++p;
                    for (i = string.length(); i >= s; --i) {
                        if (match(
                                pattern.substring(p),
                                string.substring(i))) /* not quite right */ {
                            return true;
                        }
                    }
                    break;
                }
                if (pattern.charAt(p) != string.charAt(s)) {
                    break;
                }
            }
            p = pattern.indexOf('|', p);
            if (p == -1) {
                return false;
            }
        }
    }

    /**
     * Sorts a string array. Implements a simple bubblesort algorithm.
     *
     * @param strings the string array.
     */
    public static void sortStrings(String[] strings) {	// Just does a bubblesort.
        for (int i = 0; i < strings.length - 1; ++i) {
            for (int j = i + 1; j < strings.length; ++j) {
                if (strings[i].compareTo(strings[j]) > 0) {
                    String t = strings[i];
                    strings[i] = strings[j];
                    strings[j] = t;
                }
            }
        }
    }

    /**
     * The testing entry point
     *
     * @param args
     * @exception java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
//  String src="123' / \\ ";
//  String what="3";
//  String byWhat="33";
//  System.out.println(src + " : " + what + " : " + byWhat);
//  System.out.println(StrUtil.replaceInString(src, what, byWhat));
        // String src="%41%30%C0";
// String src="АБВГД%C4%D5%C2%CE%C1";

        String src = "%CD%C5%C2%C5%CC%D8%CE%C1%D1 %C6%C1%C2%D2%C9%CB%C1 %C7%CF%D2%CF%C4 %C4%D5%C2%CE%C1";
        System.out.println(src + ":" + StrUtil.unescape(src, "KOI8-R"));
        src = "АБВГД%C4%D5%C2%CE%C1";
        System.out.println(src + ":" + StrUtil.unescape(src, "KOI8-R"));
        src = "АБВГД%u00c4%u00D5%u00C2%u00ce%u00C1";
        System.out.println(src + ":" + StrUtil.unescape(src, "KOI8-R"));
//  System.out.println( java.net.URLEncoder.encode(src) );

    }

}
