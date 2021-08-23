package dubna.walt.util;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;

/// Some simple single-arg sprintf-like routines.
// <P>
// It is apparently impossible to declare a Java method that accepts
// variable numbers of any type of argument.  You can declare it to take
// Objects, but numeric variables and constants are not in fact Objects.
// <P>
// However, using the built-in string concatenation, it's almost as
// convenient to make a series of single-argument formatting routines.
// <P>
// Fmt can format the following types:
// <BLOCKQUOTE><CODE>
// byte short int long float double char String Object
// </CODE></BLOCKQUOTE>
// For each type there is a set of overloaded methods, each returning
// a formatted String.  There's the plain formatting version:
// <BLOCKQUOTE><PRE>
// Fmt.fmt( x )
// </PRE></BLOCKQUOTE>
// There's a version specifying a minimum field width:
// <BLOCKQUOTE><PRE>
// Fmt.fmt( x, minWidth )
// </PRE></BLOCKQUOTE>
// And there's a version that takes flags:
// <BLOCKQUOTE><PRE>
// Fmt.fmt( x, minWidth, flags )
// </PRE></BLOCKQUOTE>
// Currently available flags are:
// <BLOCKQUOTE><PRE>
// Fmt.ZF - zero-fill
// Fmt.LJ - left justify
// Fmt.HX - hexadecimal
// Fmt.OC - octal
// </PRE></BLOCKQUOTE>
// The HX and OC flags imply unsigned output.
// <P>
// For doubles and floats, there's a significant-figures parameter before
// the flags:
// <BLOCKQUOTE><PRE>
// Fmt.fmt( d )
// Fmt.fmt( d, minWidth )
// Fmt.fmt( d, minWidth, sigFigs )
// Fmt.fmt( d, minWidth, sigFigs, flags )
// </PRE></BLOCKQUOTE>
// <P>
// <A HREF="/resources/classes/Acme/Fmt.java">Fetch the software.</A><BR>
// <A HREF="/resources/classes/Acme.tar.gz">Fetch the entire Acme package.</A>
// <HR>
// Similar classes:
// <UL>
// <LI> Andrew Scherpbier's <A HREF="http://www.sdsu.edu/doc/java-SDSU/sdsu.FormatString.html">FormatString</A>
// Tries to allow variable numbers of arguments by
// supplying overloaded routines with different combinations of parameters,
// but doesn't actually supply that many.  The floating point conversion
// is described as "very incomplete".
// <LI> Core Java's <A HREF="http://www.apl.jhu.edu/~hall/java/CoreJava-Format.html">Format</A>.
// The design seems a little weird.  They want you to create an instance,
// passing the format string to the constructor, and then call an instance
// method with your data to do the actual formatting.  The extra steps are
// pointless; better to just use static methods.
// </UL>

/**
 *
 * @author serg
 */

public class Fmt
{

    // Flags.
    /// Zero-fill.

    /**
     *
     */
    public static final int ZF = 1;
    /// Left justify.

    /**
     *
     */
    public static final int LJ = 2;
    /// Hexadecimal.

    /**
     *
     */
    public static final int HX = 4;
    /// Octal.

    /**
     *
     */
    public static final int OC = 8;
    // Was a number - internal use.
    private static final int WN = 16;

    // byte

    /**
     *
     * @param b
     * @return
     */
    public static String fmt( byte b )
	{
	return fmt( b, 0, 0 );
	}

    /**
     *
     * @param b
     * @param minWidth
     * @return
     */
    public static String fmt( byte b, int minWidth )
	{
	return fmt( b, minWidth, 0 );
	}

    /**
     *
     * @param b
     * @param minWidth
     * @param flags
     * @return
     */
    public static String fmt( byte b, int minWidth, int flags )
	{
	boolean hexadecimal = ( ( flags & HX ) != 0 );
	boolean octal = ( ( flags & OC ) != 0 );
	if ( hexadecimal )
	    return fmt( Integer.toString( b & 0xff, 16 ), minWidth, flags|WN );
	else if ( octal )
	    return fmt( Integer.toString( b & 0xff, 8 ), minWidth, flags|WN );
	else
	    return fmt( Integer.toString( b & 0xff ), minWidth, flags|WN );
	}

    // short

    /**
     *
     * @param s
     * @return
     */
    public static String fmt( short s )
	{
	return fmt( s, 0, 0 );
	}

    /**
     *
     * @param s
     * @param minWidth
     * @return
     */
    public static String fmt( short s, int minWidth )
	{
	return fmt( s, minWidth, 0 );
	}

    /**
     *
     * @param s
     * @param minWidth
     * @param flags
     * @return
     */
    public static String fmt( short s, int minWidth, int flags )
	{
	boolean hexadecimal = ( ( flags & HX ) != 0 );
	boolean octal = ( ( flags & OC ) != 0 );
	if ( hexadecimal )
	    return fmt(
		Integer.toString( s & 0xffff, 16 ), minWidth, flags|WN );
	else if ( octal )
	    return fmt(
		Integer.toString( s & 0xffff, 8 ), minWidth, flags|WN );
	else
	    return fmt( Integer.toString( s ), minWidth, flags|WN );
	}

    // int

    /**
     *
     * @param i
     * @return
     */
    public static String fmt( int i ) {
	return fmt( i, 0, 0 );
    }
    
    /**
     * Вставить разделители тысяч 
     * @param l
     * @return 
     */
    public static String ftm_delim(long l){
        return String.format("%,d", l);
    }

    /**
     *
     * @param i
     * @param minWidth
     * @return
     */
    public static String fmt( int i, int minWidth )
	{
	return fmt( i, minWidth, 0 );
	}

    /**
     *
     * @param i
     * @param minWidth
     * @param flags
     * @return
     */
    public static String fmt( int i, int minWidth, int flags )
	{
	boolean hexadecimal = ( ( flags & HX ) != 0 );
	boolean octal = ( ( flags & OC ) != 0 );
	if ( hexadecimal )
	    return fmt(
		Long.toString( i & 0xffffffffL, 16 ), minWidth, flags|WN );
	else if ( octal )
	    return fmt(
		Long.toString( i & 0xffffffffL, 8 ), minWidth, flags|WN );
	else
	    return fmt( Integer.toString( i ), minWidth, flags|WN );
	}

    // long

    /**
     *
     * @param l
     * @return
     */
    public static String fmt( long l )
	{
	return fmt( l, 0, 0 );
	}

    /**
     *
     * @param l
     * @param minWidth
     * @return
     */
    public static String fmt( long l, int minWidth )
	{
	return fmt( l, minWidth, 0 );
	}

    /**
     *
     * @param l
     * @param minWidth
     * @param flags
     * @return
     */
    public static String fmt( long l, int minWidth, int flags )
	{
	boolean hexadecimal = ( ( flags & HX ) != 0 );
	boolean octal = ( ( flags & OC ) != 0 );
	if ( hexadecimal )
	    {
	    if ( ( l & 0xf000000000000000L ) != 0 )
		return fmt(
		    Long.toString( l >>> 60, 16 ) +
		    fmt( l & 0x0fffffffffffffffL, 15, HX|ZF ),
		    minWidth, flags|WN );
	    else
		return fmt( Long.toString( l, 16 ), minWidth, flags|WN );
	    }
	else if ( octal )
	    {
	    if ( ( l & 0x8000000000000000L ) != 0 )
		return fmt(
		    Long.toString( l >>> 63, 8 ) +
		    fmt( l & 0x7fffffffffffffffL, 21, OC|ZF ),
		    minWidth, flags|WN );
	    else
		return fmt( Long.toString( l, 8 ), minWidth, flags|WN );
	    }
	else
	    return fmt( Long.toString( l ), minWidth, flags|WN );
	}

    // float

    /**
     *
     * @param f
     * @return
     */
    public static String fmt( float f )
	{
	return fmt( f, 0, 0, 0 );
	}

    /**
     *
     * @param f
     * @param minWidth
     * @return
     */
    public static String fmt( float f, int minWidth )
	{
	return fmt( f, minWidth, 0, 0 );
	}

    /**
     *
     * @param f
     * @param minWidth
     * @param sigFigs
     * @return
     */
    public static String fmt( float f, int minWidth, int sigFigs )
	{
	return fmt( f, minWidth, sigFigs, 0 );
	}

    /**
     *
     * @param f
     * @param minWidth
     * @param sigFigs
     * @param flags
     * @return
     */
    public static String fmt( float f, int minWidth, int sigFigs, int flags )
	{
	if ( sigFigs != 0 )
	    return fmt(
		sigFigFix( Float.toString( f ), sigFigs ), minWidth,
		flags|WN );
	else
	    return fmt( Float.toString( f ), minWidth, flags|WN );
	}

    // double

    /**
     *
     * @param d
     * @return
     */
    public static String fmt( double d )
	{
	return fmt( d, 0, 0, 0 );
	}

    /**
     *
     * @param d
     * @param minWidth
     * @return
     */
    public static String fmt( double d, int minWidth )
	{
	return fmt( d, minWidth, 0, 0 );
	}

    /**
     *
     * @param d
     * @param minWidth
     * @param sigFigs
     * @return
     */
    public static String fmt( double d, int minWidth, int sigFigs )
	{
	return fmt( d, minWidth, sigFigs, 0 );
	}

    /**
     *
     * @param d
     * @param minWidth
     * @param sigFigs
     * @param flags
     * @return
     */
    public static String fmt( double d, int minWidth, int sigFigs, int flags )
	{
	if ( sigFigs != 0 )
	    return fmt(
		sigFigFix( doubleToString( d ), sigFigs ), minWidth,
		flags|WN );
	else
	    return fmt( doubleToString( d ), minWidth, flags|WN );
	}

    // char

    /**
     *
     * @param c
     * @return
     */
    public static String fmt( char c )
	{
	return fmt( c, 0, 0 );
	}

    /**
     *
     * @param c
     * @param minWidth
     * @return
     */
    public static String fmt( char c, int minWidth )
	{
	return fmt( c, minWidth, 0 );
	}

    /**
     *
     * @param c
     * @param minWidth
     * @param flags
     * @return
     */
    public static String fmt( char c, int minWidth, int flags )
	{
	// return fmt( Character.toString( c ), minWidth, flags );
	// Character currently lacks a static toString method.  Workaround
	// is to make a temporary instance and use the instance toString.
	return fmt( new Character( c ).toString(), minWidth, flags );
	}

    // Object

    /**
     *
     * @param o
     * @return
     */
    public static String fmt( Object o )
	{
	return fmt( o, 0, 0 );
	}

    /**
     *
     * @param o
     * @param minWidth
     * @return
     */
    public static String fmt( Object o, int minWidth )
	{
	return fmt( o, minWidth, 0 );
	}

    /**
     *
     * @param o
     * @param minWidth
     * @param flags
     * @return
     */
    public static String fmt( Object o, int minWidth, int flags )
	{
	return fmt( o.toString(), minWidth, flags );
	}

    // String

    /**
     *
     * @param s
     * @return
     */
    public static String fmt( String s )
	{
	return fmt( s, 0, 0 );
	}

    /**
     *
     * @param s
     * @param minWidth
     * @return
     */
    public static String fmt( String s, int minWidth )
	{
	return fmt( s, minWidth, 0 );
	}

    /**
     *
     * @param s
     * @param minWidth
     * @param flags
     * @return
     */
    public static String fmt( String s, int minWidth, int flags )
	{
	int len = s.length();
	boolean zeroFill = ( ( flags & ZF ) != 0 );
	boolean leftJustify = ( ( flags & LJ ) != 0 );
	boolean hexadecimal = ( ( flags & HX ) != 0 );
	boolean octal = ( ( flags & OC ) != 0 );
	boolean wasNumber = ( ( flags & WN ) != 0 );
	if ( ( hexadecimal || octal || zeroFill ) && ! wasNumber )
	    throw new InternalError( "Acme.Fmt: number flag on a non-number" );
	if ( zeroFill && leftJustify )
	    throw new InternalError( "Acme.Fmt: zero-fill left-justify is silly" );
	if ( hexadecimal && octal )
	    throw new InternalError( "Acme.Fmt: can't do both hex and octal" );
	if ( len >= minWidth )
	    return s;
	int fillWidth = minWidth - len;
	StringBuilder fill = new StringBuilder( fillWidth );
	for ( int i = 0; i < fillWidth; ++i )
	    if ( zeroFill )
		fill.append( '0' );
	    else
		fill.append( ' ' );
	if ( leftJustify )
	    return s + fill;
	else if ( zeroFill && s.startsWith( "-" ) )
	    return "-" + fill + s.substring( 1 );
	else
	    return fill + s;
	}


    // Internal routines.

    private static String sigFigFix( String s, int sigFigs )
	{
	// First dissect the floating-point number string into sign,
	// integer part, fraction part, and exponent.
	String sign;
	String unsigned;
	if ( s.startsWith( "-" ) || s.startsWith( "+" ) )
	    {
	    sign = s.substring( 0, 1 );
	    unsigned = s.substring( 1 );
	    }
	else
	    {
	    sign = "";
	    unsigned = s;
	    }
	String mantissa;
	String exponent;
	int eInd = unsigned.indexOf( 'e' );
	if ( eInd == -1 )	// it may be 'e' or 'E'
	    eInd = unsigned.indexOf( 'E' );
	if ( eInd == -1 )
	    {
	    mantissa = unsigned;
	    exponent = "";
	    }
	else
	    {
	    mantissa = unsigned.substring( 0, eInd );
	    exponent = unsigned.substring( eInd );
	    }
	StringBuilder number, fraction;
	int dotInd = mantissa.indexOf( '.' );
	if ( dotInd == -1 )
	    {
	    number = new StringBuilder( mantissa );
	    fraction = new StringBuilder( "" );
	    }
	else
	    {
	    number = new StringBuilder( mantissa.substring( 0, dotInd ) );
	    fraction = new StringBuilder( mantissa.substring( dotInd + 1 ) );
	    }

	int numFigs = number.length();
	int fracFigs = fraction.length();
	if ( ( numFigs == 0 || number.equals( "0" ) ) && fracFigs > 0 )
	    {
	    // Don't count leading zeros in the fraction.
	    numFigs = 0;
	    for ( int i = 0; i < fraction.length(); ++i )
		{
		if ( fraction.charAt( i ) != '0' )
		    break;
		--fracFigs;
		}
	    }
	int mantFigs = numFigs + fracFigs;
	if ( sigFigs > mantFigs )
	    {
	    // We want more figures; just append zeros to the fraction.
	    for ( int i = mantFigs; i < sigFigs; ++i )
		fraction.append( '0' );
	    }
	else if ( sigFigs < mantFigs && sigFigs >= numFigs )
	    {
	    // Want fewer figures in the fraction; chop.
	    fraction.setLength(
		fraction.length() - ( fracFigs - ( sigFigs - numFigs ) ) );
	    // Round?
	    }
	else if ( sigFigs < numFigs )
	    {
	    // Want fewer figures in the number; turn them to zeros.
	    fraction.setLength( 0 );	// should already be zero, but make sure
	    for ( int i = sigFigs; i < numFigs; ++i )
		number.setCharAt( i, '0' );
	    // Round?
	    }
	// Else sigFigs == mantFigs, which is fine.

	if ( fraction.length() == 0 )
	    return sign + number + exponent;
	else
	    return sign + number + "." + fraction + exponent;
	}


    /// Improved version of Double.toString(), returns more decimal places.
    // <P>
    // The JDK 1.0.2 version of Double.toString() returns only six decimal
    // places on some systems.  In JDK 1.1 full precision is returned on
    // all platforms.
    // @deprecated
    // @see java.lang.Double#toString

    /**
     *
     * @param d
     * @return
     */
    public static String doubleToString( double d )
	{
	// Handle special numbers first, to avoid complications.
	if ( Double.isNaN( d ) )
	    return "NaN";
	if ( d == Double.NEGATIVE_INFINITY )
	    return "-Inf";
	if ( d == Double.POSITIVE_INFINITY )
	    return "Inf";

	// Grab the sign, and then make the number positive for simplicity.
	boolean negative = false;
	if ( d < 0.0D )
	    {
	    negative = true;
	    d = -d;
	    }

	// Get the native version of the unsigned value, as a template.
	String unsStr = Double.toString( d );

	// Dissect out the exponent.
	String mantStr, expStr;
	int exp;
	int eInd = unsStr.indexOf( 'e' );
	if ( eInd == -1 )	// it may be 'e' or 'E'
	    eInd = unsStr.indexOf( 'E' );
	if ( eInd == -1 )
	    {
	    mantStr = unsStr;
	    expStr = "";
	    exp = 0;
	    }
	else
	    {
	    mantStr = unsStr.substring( 0, eInd );
	    expStr = unsStr.substring( eInd + 1 );
	    if ( expStr.startsWith( "+" ) )
		exp = Integer.parseInt( expStr.substring( 1 ) );
	    else
		exp = Integer.parseInt( expStr );
	    }

	// Dissect out the number part.
	String numStr;
	int dotInd = mantStr.indexOf( '.' );
	if ( dotInd == -1 )
	    numStr = mantStr;
	else
	    numStr = mantStr.substring( 0, dotInd );
	long num;
	if ( numStr.length() == 0 )
	    num = 0;
	else
	    num = Integer.parseInt( numStr );

	// Build the new mantissa.
	StringBuilder newMantBuf = new StringBuilder( numStr + "." );
	double p = Math.pow( 10, exp );
	double frac = d - num * p;
	String digits = "0123456789";
	int nDigits = 16 - numStr.length();	// about 16 digits in a double
	for ( int i = 0; i < nDigits; ++i )
	    {
	    p /= 10.0D;
	    int dig = (int) ( frac / p );
	    if ( dig < 0 ) dig = 0;
	    if ( dig > 9 ) dig = 9;
	    newMantBuf.append( digits.charAt( dig ) );
	    frac -= dig * p;
	    }

	if ( (int) ( frac / p + 0.5D ) == 1 )
	    {
	    // Round up.
	    boolean roundMore = true;
	    for ( int i = newMantBuf.length() - 1; i >= 0; --i )
		{
		int dig = digits.indexOf( newMantBuf.charAt( i ) );
		if ( dig == -1 )
		    continue;
		++dig;
		if ( dig == 10 )
		    {
		    newMantBuf.setCharAt( i, '0' );
		    continue;
		    }
		newMantBuf.setCharAt( i, digits.charAt( dig ) );
		roundMore = false;
		break;
		}
	    if ( roundMore )
		{
		// If this happens, we need to prepend a 1.  But I haven't
		// found a test case yet, so I'm leaving it out for now.
		// But if you get this message, please let me know!
		newMantBuf.append( "ROUNDMORE" );
		}
	    }

	// Chop any trailing zeros.
	int len = newMantBuf.length();
	while ( newMantBuf.charAt( len - 1 ) == '0' )
	    newMantBuf.setLength( --len );
	// And chop a trailing dot, if any.
	if ( newMantBuf.charAt( len - 1 ) == '.' )
	    newMantBuf.setLength( --len );

	// Done.
	return ( negative ? "-" : "" ) +
	       newMantBuf +
	       ( expStr.length() != 0 ? ( "e" + expStr ) : "" );
	}

/** Returns a date string formatted in Unix ls style - if it's within
  * six months of now, Mmm dd hh:ss, else Mmm dd  yyyy.
     * @param date
     * @return 
  */
public static String lsDateStr( Date date )
{
	Calendar cal = new GregorianCalendar();
	cal.setTime( date );
        long dateTime = date.getTime();
	if ( dateTime == -1L )
	    return "------------";
        long nowTime = (new Date()).getTime();
        String[] months = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        String part1 =
	    months[cal.get(Calendar.MONTH)] +
	    Fmt.fmt( cal.get(Calendar.DATE), 3 );
        if ( Math.abs( nowTime - dateTime ) < 183L * 24L * 60L * 60L * 1000L )
            return part1 + Fmt.fmt( cal.get(Calendar.HOUR_OF_DAY), 3 ) + ":" +
                Fmt.fmt( cal.get(Calendar.MINUTE), 2, Fmt.ZF );
        else
            return part1 + Fmt.fmt( cal.get(Calendar.YEAR), 6 );
        }


    /// Returns "s" for numbers other than one, and "" for one.

    /**
     *
     * @param n
     * @return
     */
public static String pluralStr( long n )
{	if ( n == 1 )
	    return "";
	else
	    return "s";
}

/** Returns a date formatted string:
  * dd.mm hh24:mi
     * @param date
     * @return 
  */
public static String shortDateStr( Date date )
{
	Calendar cal = new GregorianCalendar();
	cal.setTime( date );
//  String[] months = {
//      "Jan", "Feb", "Mar", "Apr", "May", "Jun",
//      "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
  String s =
	    Fmt.fmt( cal.get(Calendar.DATE), 2, Fmt.ZF ) + "."
	  + Fmt.fmt( cal.get(Calendar.MONTH)+1, 2, Fmt.ZF ) + " "
//    + months[cal.get(Calendar.MONTH)] + ", "
    + Fmt.fmt( cal.get(Calendar.HOUR_OF_DAY), 2, Fmt.ZF ) + ":"
    + Fmt.fmt( cal.get(Calendar.MINUTE), 2, Fmt.ZF );
  return s;
}

/** Returns a date formatted string:
  * dd.mm hh24:mi:ss
     * @param date
     * @return 
  */
public static String fullDateStr( Date date )
{
	Calendar cal = new GregorianCalendar();
	cal.setTime( date );
  String s =
	    Fmt.fmt( cal.get(Calendar.DATE), 2, Fmt.ZF ) + "."
	  + Fmt.fmt( cal.get(Calendar.MONTH)+1, 2, Fmt.ZF ) + " "
    + Fmt.fmt( cal.get(Calendar.HOUR_OF_DAY), 2, Fmt.ZF ) + ":"
    + Fmt.fmt( cal.get(Calendar.MINUTE), 2, Fmt.ZF ) + ":"
    + Fmt.fmt( cal.get(Calendar.SECOND ), 2, Fmt.ZF );
  return s;
}

    /**
     *
     * @param d
     * @return
     */
    public static String toGMTString(Date d)
{
  return ( new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z"
   , java.util.Locale.UK).format(d));
}

    /**
     *
     * @param s
     * @return
     */
    public static Date gmtToDate(String s)
{ Date d = null;
  try
 { d = (new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z"
   , java.util.Locale.UK)).parse(s);
 }
 catch (Exception e)
 { d = new Date(); }
  return d;
}


/******************************************************************************
    /// Test program.
    public static void main( String[] args )
	{
	System.out.println( "Starting tests." );
	show( Fmt.fmt( "Hello there." ) );
	show( Fmt.fmt( 123 ) );
	show( Fmt.fmt( 123, 10 ) );
	show( Fmt.fmt( 123, 10, Fmt.ZF ) );
	show( Fmt.fmt( 123, 10, Fmt.LJ ) );
	show( Fmt.fmt( -123 ) );
	show( Fmt.fmt( -123, 10 ) );
	show( Fmt.fmt( -123, 10, Fmt.ZF ) );
	show( Fmt.fmt( -123, 10, Fmt.LJ ) );
	show( Fmt.fmt( (byte) 0xbe, 22, Fmt.OC ) );
	show( Fmt.fmt( (short) 0xbabe, 22, Fmt.OC ) );
	show( Fmt.fmt( 0xcafebabe, 22, Fmt.OC ) );
	show( Fmt.fmt( 0xdeadbeefcafebabeL, 22, Fmt.OC ) );
	show( Fmt.fmt( 0x8000000000000000L, 22, Fmt.OC ) );
	show( Fmt.fmt( (byte) 0xbe, 16, Fmt.HX ) );
	show( Fmt.fmt( (short) 0xbabe, 16, Fmt.HX ) );
	show( Fmt.fmt( 0xcafebabe, 16, Fmt.HX ) );
	show( Fmt.fmt( 0xdeadbeefcafebabeL, 16, Fmt.HX ) );
	show( Fmt.fmt( 0x8000000000000000L, 16, Fmt.HX ) );
	show( Fmt.fmt( 'c' ) );
	show( Fmt.fmt( new java.util.Date() ) );
	show( Fmt.fmt( 123.456F ) );
	show( Fmt.fmt( 123456000000000000.0F ) );
	show( Fmt.fmt( 123.456F, 0, 8 ) );
	show( Fmt.fmt( 123.456F, 0, 7 ) );
	show( Fmt.fmt( 123.456F, 0, 6 ) );
	show( Fmt.fmt( 123.456F, 0, 5 ) );
	show( Fmt.fmt( 123.456F, 0, 4 ) );
	show( Fmt.fmt( 123.456F, 0, 3 ) );
	show( Fmt.fmt( 123.456F, 0, 2 ) );
	show( Fmt.fmt( 123.456F, 0, 1 ) );
	show( Fmt.fmt( 123456000000000000.0F, 0, 4 ) );
	show( Fmt.fmt( -123.456F, 0, 4 ) );
	show( Fmt.fmt( -123456000000000000.0F, 0, 4 ) );
	show( Fmt.fmt( 123.0F ) );
	show( Fmt.fmt( 123.0D ) );
	show( Fmt.fmt( 1.234567890123456789F ) );
	show( Fmt.fmt( 1.234567890123456789D ) );
	show( Fmt.fmt( 1234567890123456789F ) );
	show( Fmt.fmt( 1234567890123456789D ) );
	show( Fmt.fmt( 0.000000000000000000001234567890123456789F ) );
	show( Fmt.fmt( 0.000000000000000000001234567890123456789D ) );
	show( Fmt.fmt( 12300.0F ) );
	show( Fmt.fmt( 12300.0D ) );
	show( Fmt.fmt( 123000.0F ) );
	show( Fmt.fmt( 123000.0D ) );
	show( Fmt.fmt( 1230000.0F ) );
	show( Fmt.fmt( 1230000.0D ) );
	show( Fmt.fmt( 12300000.0F ) );
	show( Fmt.fmt( 12300000.0D ) );
	show( Fmt.fmt( Float.NaN ) );
	show( Fmt.fmt( Float.POSITIVE_INFINITY ) );
	show( Fmt.fmt( Float.NEGATIVE_INFINITY ) );
	show( Fmt.fmt( Double.NaN ) );
	show( Fmt.fmt( Double.POSITIVE_INFINITY ) );
	show( Fmt.fmt( Double.NEGATIVE_INFINITY ) );
	show( Fmt.fmt( 1.0F / 8.0F ) );
	show( Fmt.fmt( 1.0D / 8.0D ) );
	show( Fmt.shortDateStr( new Date() ) );
	System.out.println( "Done with tests." );
}

private static void show( String str )
{
	System.out.println( "#" + str + "#" );
}
/******************************************************************************/

}
