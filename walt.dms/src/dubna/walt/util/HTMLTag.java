package dubna.walt.util;

/**
 *
 * @author serg
 */
public class HTMLTag {

    /**
     *
     */
    public String attr = "";

    /**
     *
     */
    public String startTag = "<td";

    /**
     *
     */
    public String endTag = "</td>";

    /**
     *
     */
    protected int numDigits = 2;

    /**
     *
     */
    protected String thsnDelimiter = "&nbsp;";
    protected String fractDelimiter = ".";

    /**
     *
     */
    protected String value = "";

    /**
     *
     */
    protected double dValue = 0.;

    /**
     *
     */
    protected String link = "";

    /**
     *
     */
    protected String link2 = "";

    /**
     *
     */
    protected boolean isNumeric = false;

    /**
     *
     */
    protected Tuner tableTuner = null;

    /**
     *
     * @param tableTuner
     */
    public HTMLTag(Tuner tableTuner) {
        this.tableTuner = tableTuner;
    }

    /**
     *
     * @param startTag
     * @param endTag
     * @param tableTuner
     */
    public HTMLTag(String startTag, String endTag, Tuner tableTuner) {
        this.startTag = startTag;
        this.endTag = endTag;
        this.tableTuner = tableTuner;
        String fd = tableTuner.getParameter("fractDelimiter");
        if(!fd.isEmpty()) fractDelimiter = fd;
    }

    /**
     *
     * @param numDigits
     * @param thsnDelimiter
     */
    public void setFormatParams(int numDigits, String thsnDelimiter) {
        this.numDigits = numDigits;
        this.thsnDelimiter = thsnDelimiter;
    }

    /**
     *
     * @param startTag
     * @param endTag
     */
    public void setTags(String startTag, String endTag) {
        this.startTag = startTag;
        this.endTag = endTag;
    }

    /**
     *
     * @param attrib
     */
    public void setAttr(String attrib) {
        attr = attrib.trim();
    }

    /**
     *
     * @param attrib
     */
    public void addAttr(String attrib) {
        if (attr.indexOf(attrib) < 0) {
            attr = attrib.trim() + " " + attr;
        }
    }

    /**
     *
     * @param href
     */
    public void setLink(String href) {
        if (href.length() > 0) {
            link = "<a href='" + href + "'>";
            link2 = "</a>";
        } else {
            link = "";
            link2 = "";
        }
    }

    /**
     *
     * @param tag
     */
    public void addValue(HTMLTag tag) {
        addValue(tag.toHTML());
    }

    /**
     *
     * @param val
     */
    public void addValue(String val) {
        value = value + val;
        dValue = 0.;
        isNumeric = false;
    }

    /*
public void setTextValue(String val)
{ if (val.indexOf(">--") > 0 
   && val.indexOf("--<") > val.indexOf(">--") )
  { setValue(val);
  }
  else
  { value = val;
    isNumeric = false;
    dValue = 0.;
  }
}
     */
    /**
     *
     * @param val
     */
    public void setValue(String val) {
        String beg = "", end = "";
        String data = val;
        dValue = 0.;

        int i1 = data.indexOf(">--");
        int i2 = data.indexOf("--<");
        if (i1 > 0 && i2 > i1 + 3) {
            beg = data.substring(0, i1 + 1);
            end = data.substring(i2 + 2);
//System		
            data = data.substring(i1 + 3, i2);
        }

//  if (StrUtil.isNumber(data) && (!thsnDelimiter.equalsIgnoreCase("none")))
        String v = data;
        if(!fractDelimiter.equals("."))
            v = v.replace(fractDelimiter, ".");
        
        if (StrUtil.isNumber(v)) {
            isNumeric = true;
            dValue = (new Double(v)).doubleValue();
            data = StrUtil.formatDouble(dValue, numDigits, thsnDelimiter);
            if(!fractDelimiter.equals("."))
                data = data.replace(".", fractDelimiter);
            if (dValue < 0. && tableTuner != null) {
                beg = beg + tableTuner.getParameter("negativeValueAttr");
            }
            addAttr("align=right");
        } else {
            if (data.length() >= 1) {
                data = (data.charAt(0) == '\'') ? data.substring(1) : data;
                isNumeric = false;
            }
        }
        value = beg + data + end;
    }

    /**
     *
     * @return
     */
    public boolean isNumeric() {
        return isNumeric;
    }

    /**
     *
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     *
     * @return
     */
    public double getDValue() {
        return dValue;
    }

    /**
     *
     * @return
     */
    public String toHTML() {
        attr = attr.trim();
        if (attr.equalsIgnoreCase("X")) {
            attr = "";
        }
        if (attr.length() > 0) {
            attr = " " + attr;
        }
        if (value.trim().length() == 0) {
            value = "&nbsp;";
        }
        String beg = (startTag.trim() + attr).trim();
        if (beg.length() > 0) {
            beg += '>';
        }
        return beg + link + value + link2 + endTag;
    }

    /**
     *
     * @param numDigits
     * @param thsnDelimiter
     */
    public void reset(int numDigits, String thsnDelimiter) {
        reset();
        setFormatParams(numDigits, thsnDelimiter);
    }

    /**
     * Сброс значения и параметров
     */
    public void reset() {
//  startTag = "<td";
//  endTag = "</td>";
        value = "";
        dValue = 0.;
        attr = "";
        link = "";
        link2 = "";
        isNumeric = false;
        numDigits = 2;
        thsnDelimiter = " ";
    }

}
