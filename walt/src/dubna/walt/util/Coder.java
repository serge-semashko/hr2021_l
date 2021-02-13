package dubna.walt.util;

/**
 *
 * @author serg
 */
public class Coder extends Object {

/** Replaces substring of the input string by another substring
 *
 * @param src the input String
 * @param what the substring to be replaced
 * @param byWhat the string to put instead of "what"
 *
 * @return String the resulting string
 */
static public String sReplaceN(String src, String what, String byWhat){
    int j,i=src.indexOf(what);
    if (i<0) return src;
    String s = new String(src);
    boolean repeat=true;
    while (repeat){
        StringBuilder sb = new StringBuilder(s.length() * 2);
        j=0;
        while (i>=0){
          sb.append(s.substring(j,i));
          sb.append(byWhat);
          j=i+what.length();
          i=s.indexOf(what,j);
        }
        sb.append(s.substring(j,s.length()));
        s=sb.toString();
        i=s.indexOf(what);
        repeat=(i>=0);
    }
    return s;
}

    /**
     *
     * @param s
     * @param key
     * @return
     */
    public static String encode(String s,String key){
    byte[] sb=s.getBytes();
    byte[] kb=key.getBytes();
    int j=0;
    for (int i=0; i<s.length();i++){
        if (isCod(sb[i]))
            sb[i] = sDcod((byte)((sCod(sb[i])+sCod(kb[j++])) % 64));
        if (j>=kb.length) j=0;
    }
    return (new String(sb));
}

    /**
     *
     * @param s
     * @param key
     * @return
     */
    public static String decode(String s,String key){
    byte[] sb=(sReplaceN(s,"%20"," ")).getBytes();
//    System.out.println("//" + new String(sb));
    byte[] kb=key.getBytes();
    int j=0;
    for (int i=0; i<sb.length;i++){
        if (isCod(sb[i])) {
            sb[i] = (byte)(sCod(sb[i])-sCod(kb[j++]));
            if (sb[i] < 0) sb[i] +=64;
            sb[i]=sDcod(sb[i]);
            if (j>=key.length()) j=0;
        }
    }
    return new String(sb);
}

static boolean isCod(byte s){
    if (s >= 48 && s <= 57) return true;
    if (s >= 65 && s <= 90) return true;
    if (s >= 97 && s <= 122) return true;
    if (s == 45 || s == 32) return true;
    return false;
}

static byte sCod(byte s){
    if (s >= 48 && s <= 57) return (byte)(s-48);
    if (s >= 65 && s <= 90) return (byte)(s-55);
    if (s >= 97 && s <= 122) return (byte)(s-61);
    if (s == 45) return 62;
    if (s == 32) return 63;
    return (byte)(s%64);
}

static byte sDcod(byte s){
    if (s <= 9) return (byte)(s+48);
    if (s <= 35) return (byte)(s+55);
    if (s <= 61) return (byte)(s+61);
    if (s == 62) return 45;
    if (s == 63) return 32;
    return (byte)(s%64);
}

    /**
     *
     * @return
     */
    public static String getKey(){
    String key=Long.toString(System.currentTimeMillis()).substring(7);
    key=key.concat(Long.toString(System.currentTimeMillis()));
    key=key.concat(Long.toString(System.currentTimeMillis()));
//    System.out.println("key="+key);
//    return encode(key,"13958735067123");
    return key;
//    return encode(key,Long.toString(System.currentTimeMillis()).substring(8));
}

}
