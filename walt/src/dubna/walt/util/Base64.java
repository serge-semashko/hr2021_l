package dubna.walt.util;

import java.util.StringTokenizer;

// http://www.wikihow.com/Encode-a-String-to-Base64-With-Java

/**
 *
 * @author serg
 */

public class Base64 {
 
	private static final String base64code = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
							+ "abcdefghijklmnopqrstuvwxyz" + "0123456789" + "+/";
	 
			private static final int splitLinesAt = 76;
	 
    /**
     *
     * @param length
     * @param bytes
     * @return
     */
    public static byte[] zeroPad(int length, byte[] bytes) {
					byte[] padded = new byte[length]; // initialized to zero by JVM
					System.arraycopy(bytes, 0, padded, 0, bytes.length);
					return padded;
			}
	 
    /**
     *
     * @param string
     * @return
     */
    public static String encode(String string) {
	 
					String encoded = "";
					byte[] stringArray;
					try {
							stringArray = string.getBytes("UTF-8");  // use appropriate encoding string!
					} catch (Exception ignored) {
							stringArray = string.getBytes();  // use locale default rather than croak
					}
					// determine how many padding bytes to add to the output
					int paddingCount = (3 - (stringArray.length % 3)) % 3;
					// add any necessary padding to the input
					stringArray = zeroPad(stringArray.length + paddingCount, stringArray);
					// process 3 bytes at a time, churning out 4 output bytes
					// worry about CRLF insertions later
					for (int i = 0; i < stringArray.length; i += 3) {
							int j = ((stringArray[i] & 0xff) << 16) +
									((stringArray[i + 1] & 0xff) << 8) + 
									(stringArray[i + 2] & 0xff);
							encoded = encoded + base64code.charAt((j >> 18) & 0x3f) +
									base64code.charAt((j >> 12) & 0x3f) +
									base64code.charAt((j >> 6) & 0x3f) +
									base64code.charAt(j & 0x3f);
					}
					// replace encoded padding nulls with "="
					 return encoded;
//					return splitLines(encoded.substring(0, encoded.length() -
//							paddingCount) + "==".substring(0, paddingCount));
	 
			}

    /**
     *
     * @param string
     * @return
     */
    public static String splitLines(String string) {
	 
					String lines = "";
					for (int i = 0; i < string.length(); i += splitLinesAt) {
	 
							lines += string.substring(i, Math.min(string.length(), i + splitLinesAt));
							lines += "\r\n";
	 
					}
					return lines;
	 
			}

    /**
     *
     * @param base64
     * @return
     * @throws Exception
     */
    public static byte[] decode(byte[] base64) throws Exception 
{ return decode(new String(base64, "UTF-8"));
}

/**
  * Decode a Base64-encoded string to a byte array.
  *
  * @param base64 <code>String</code> encoded string (single line only !!)
  * @return Decoded data in a byte array
  * @throws Exception
  */
public static byte[] decode(String base64) throws Exception 
{ 
   //strip whitespace from anywhere in the string.  Not the most memory
   //efficient solution but elegant anyway :-)
     StringTokenizer tok = new StringTokenizer(base64, " \n\r\t", false);
     StringBuilder buf = new StringBuilder(base64.length());
     while (tok.hasMoreElements()) 
       buf.append(tok.nextToken());

     base64 = buf.toString();
     int pad = 0;
     for (int i = base64.length() - 1; (i > 0) && (base64.charAt(i) == '='); i--) 
        pad++;

     int length = base64.length() / 4 * 3 - pad;
     byte[] raw = new byte[length];

     for (int i = 0, rawIndex = 0; i < base64.length(); i += 4, rawIndex += 3) 
     { int block = (getValue(base64.charAt(i)) << 18)
                 + (getValue(base64.charAt(i + 1)) << 12)
                 + (getValue(base64.charAt(i + 2)) << 6)
                 + (getValue(base64.charAt(i + 3)));

       for (int j = 2; j >= 0; j--) 
       { if (rawIndex + j < raw.length) 
           raw[rawIndex + j] = (byte) (block & 0xff);
           block >>= 8;
       }
     }
     return raw;
}

    /**
     *
     * @param c
     * @return
     */
    protected static int getValue(char c) 
{ if ((c >= 'A') && (c <= 'Z')) return c - 'A';
  if ((c >= 'a') && (c <= 'z')) return c - 'a' + 26;
  if ((c >= '0') && (c <= '9')) return c - '0' + 52;
  if (c == '+') return 62;
  if (c == '/') return 63;
  if (c == '=') return 0;
  return -1;
}

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception 
{  String s = "a291bmlhZXY6cXdlcnR5";
   System.out.println(s);
   String res = new String(decode(s));
   System.out.println(res);
        
}

}
