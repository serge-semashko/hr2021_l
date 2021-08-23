package dubna.walt.util;

import java.text.Collator;

/**
 *
 * @author serg
 */
public class ParamValidator
{

    /**
     *
     */
    protected Tuner paramTuner = null;

    /**
     *
     */
    protected Tuner cfgTuner = null;
//protected ResourceManager rm;

    /**
     *
     * @param rm
     * @throws Exception
     */
    public void init(ResourceManager rm) throws Exception
{
//  this.rm = rm;
  String fileName = rm.getString("parametersDefinitionFile", true);
  if (fileName.length() > 0)
    paramTuner = new Tuner(null, fileName, rm.getString("CfgRootPath"), rm);
}

/**
  *
  *
     * @param cfgTuner
     * @param rm
     * @return 
  */
public boolean validate(Tuner cfgTuner, ResourceManager rm) //throws Exception
{ boolean result = true;
  boolean ok = true;
//  if (cfgTuner.enabledOption("debug=on"))
//    System.out.println("............. " + this.getClass().getName() + ": " + paramTuner);
  if (paramTuner == null) return true;
  this.cfgTuner = cfgTuner;
  paramTuner.parameters = cfgTuner.getParameters();

  String par = "";
  String s = "";
  String[] params = paramTuner.getCustomSection("param list");
//  System.out.print("=   ========== ParamValidator is here! " + params);
  if (params==null || params.length == 0)
    return true;

  for (int i = 0; i < params.length; i++)
  { par = cfgTuner.getParameter(params[i]);
//    System.out.print(i + ": " + params[i] + "='" + par + "' " );
    if (par.length() > 0)
    { s = paramTuner.getParameter(params[i], "type");
      if (s.equals("real")) 
        ok = validateReal(params[i], par);
      else if (s.equals("int")) 
        ok = validateInt(params[i], par);
      else if (s.equals("long")) 
        ok = validateLong(params[i], par);
      else if (s.equals("date")) 
        ok = validateDate(params[i], par);
      else if (s.equals("date1")) 
        ok = validateDate1(params[i], par);
      else if (s.equals("date2")) 
        ok = validateDate2(params[i], par);
      else if (s.equals("string")) 
        ok = validateString(params[i], par);
      else if (s.equals("time-text")) 
        ok = validateTimeText(params[i], par);
      else
        ok = validateCustom(params[i], par, s);
        
      if (ok)
        ok=validateLimits(params[i], par, s);
      
      if (!ok)
      { result = false;
        if (!cfgTuner.enabledOption("ERR:" + params[i]))
        { String err_msg = paramTuner.getParameter(params[i], "errMsg");
          if (err_msg.length() == 0)
            err_msg = paramTuner.getParameter("ERR_" + s);
          cfgTuner.addParameter("ERR:" + params[i], err_msg);
          cfgTuner.addParameter("INPUT_ERRORS", err_msg);
        }
//        System.out.println("; Error! ");
      }
    }
    else
    { s = paramTuner.getParameter(params[i], "default");
      if ( s.length() > 0)
      { cfgTuner.addParameter(params[i], s);
//        System.out.println("; default: " + s );
      }
    }
  }
  if (!result) 
  { cfgTuner.addParameter("INPUT_ERRORS","YES");
    System.out.println("............. " + this.getClass().getName() + "INPUT_ERRORS!");
  }
  return result;
}

    /**
     *
     * @param name
     * @param paramValue
     * @param type
     * @return
     */
    public boolean validateLimits(String name, String paramValue, String type)
{
  if (("real int long").indexOf(type) >=0)
  {
    double val = (Double.valueOf(paramValue.trim())).doubleValue();
    
    String s = paramTuner.getParameter(name, "min");
    if (s.length() > 0)
    { double min = (Double.valueOf(s.trim())).doubleValue();
      if (val < min) return false;
    }

    s = paramTuner.getParameter(name, "max");
    if (s.length() > 0)
    { double max = (Double.valueOf(s.trim())).doubleValue();
      if (val > max) return false;
    }
  }
  else
  {
    String min = paramTuner.getParameter(name, "min");
    String max = paramTuner.getParameter(name, "max");
    Collator collator = null;
    if (min.length() > 0)
    { collator = Collator.getInstance();
//            System.out.println(name + " ***** compare ***** " + min);
          if( collator.compare(paramValue, min) < 0 )
          {
            System.out.println(paramValue + " is less than " + min);
            return false;
          }
        }
  }
  return true;
  
}

    /**
     *
     * @param paramName
     * @param paramValue
     * @param paramType
     * @return
     */
    public boolean validateCustom(String paramName, String paramValue, String paramType)
{
  return true;
}

    /**
     *
     * @param paramName
     * @param paramValue
     * @return
     */
    public boolean validateTimeText(String paramName, String paramValue)
{
  try
  {
    int l = paramValue.indexOf(":");
    if (l < 0) return false;
    String hh = paramValue.substring(0, l);
    String mm = paramValue.substring(l + 1);
    int ih = Integer.parseInt(hh);
    int im = Integer.parseInt(mm);
    
    if (ih < 0 || ih > 24) return false;
    if (ih == 24 && im > 0) return false;
    if (im < 0 || im > 59) return false;

    hh = Integer.toString(ih);
    mm = Integer.toString(im);
    if (hh.length() < 2) hh = "0" + hh;
    if (mm.length() < 2) mm = "0" + mm;
    cfgTuner.addParameter(paramName, hh + ":" + mm);

    return true;
  }
  catch (Exception ex)  { return false; }
}

    /**
     *
     * @param paramName
     * @param paramValue
     * @return
     */
    public boolean validateString(String paramName, String paramValue)
{
  try
  {
    int l = paramTuner.getIntParameter(paramName, "length");
//    l=20;
    return (l < 0 || paramValue.length() <= l);
  }
  catch (Exception ex)  { return false; }
}

    /**
     *
     * @param paramName
     * @param paramValue
     * @return
     */
    public static boolean validateReal(String paramName, String paramValue)
{
  try
  { new Double(paramValue.trim());
    return true;
  }
  catch (Exception ex)  { return false; }
}

    /**
     *
     * @param paramName
     * @param paramValue
     * @return
     */
    public static boolean validateInt(String paramName, String paramValue)
{
  try
  {
    Integer.parseInt(paramValue);
    return true;
  }
  catch (Exception ex)  { return false; }
}

    /**
     *
     * @param paramName
     * @param paramValue
     * @return
     */
    public static boolean validateLong(String paramName, String paramValue)
{
  try
  {
    Long.parseLong(paramValue);
    return true;
  }
  catch (Exception ex)  { return false; }
}

    /**
     *
     * @param paramName
     * @param paramValue
     * @return
     */
    public static boolean validateDate(String paramName, String paramValue)
{
// date format: 2000.01.31
  try
  { if (paramValue.length() != 10) return false;
//    System.out.println("'" + paramValue.substring(0,4) + ":" + paramValue.substring(5,7) + ":" + paramValue.substring(8,10) +"'");
    int yr = Integer.parseInt(paramValue.substring(0,4));
    if (yr < 1999 || yr > 2010) return false;
    int mm = Integer.parseInt(paramValue.substring(5,7));
    if (mm < 1 || mm > 12) return false;
    int dd = Integer.parseInt(paramValue.substring(8,10));
    if (dd < 1 || dd > 31) return false;
  }
  catch (Exception ex)  { return false; }

  return true;
}

    /**
     *
     * @param paramName
     * @param paramValue
     * @return
     */
    public static boolean validateDate1(String paramName, String paramValue)
{
// date format: 31.12.01 [12:59]
  try
  { if (paramValue.length() != 8 && paramValue.length() != 14) return false;
//    System.out.println("'" + paramValue.substring(0,2) + ":" + paramValue.substring(3,5) + ":" + paramValue.substring(6,8) +"'");
    int dd = Integer.parseInt(paramValue.substring(0,2));
    if (dd < 1 || dd > 31) return false;
    int mm = Integer.parseInt(paramValue.substring(3,5));
    if (mm < 1 || mm > 12) return false;
    int yr = Integer.parseInt(paramValue.substring(6,8));
    if (yr > 50) yr += 1900;
    else yr += 2000;
    if (yr < 1999 || yr > 2010) return false;
  }
  catch (Exception ex)  { return false; }

  return true;
}

    /**
     *
     * @param paramName
     * @param paramValue
     * @return
     */
    public static boolean validateDate2(String paramName, String paramValue)
{
// date2 format: 31-Jan-01 10:55
//  System.out.println("+++++ Validate " + paramName + "='" + paramValue +"'...");
  String months=",JAN,FEB,MAR,APR,MAY,JUN,JUL,AUG,SEP,OCT,NOV,DEC,";
  if (paramValue.length() < 9) return false;
  try
  {
    int dd = Integer.parseInt(paramValue.substring(0,2));
    if (dd < 1 || dd > 31) return false;

    String mon = "," + paramValue.substring(3,6).toUpperCase() + ",";
    if (months.indexOf(mon) < 0) return false;

    int yr = Integer.parseInt(paramValue.substring(7,9));
    if (yr < 00 || yr > 20) return false;
    
    if (paramValue.length() >= 11)
    {
      int hh = Integer.parseInt(paramValue.substring(10,12));
      int mm = 0;
      if (hh > 24) return false;
      if (paramValue.length() >= 14)
      {
//    System.out.println("'" + paramValue.substring(0,2) + ":" + paramValue.substring(3,6) + ":" + paramValue.substring(7,9)
//      + ":" + paramValue.substring(10,12) + ":" + paramValue.substring(13,15) +"'");
        mm = Integer.parseInt(paramValue.substring(13,15));
        if (mm > 59) return false;
      }
//  System.out.println("+++++ HH:MM= '" + hh + ":" + mm +"'");
    }
  }
  catch (Exception ex)  { return false; }

  return true;
}

}