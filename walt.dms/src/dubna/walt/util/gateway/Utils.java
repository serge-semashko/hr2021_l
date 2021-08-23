package dubna.walt.util.gateway;

import dubna.walt.util.IOUtil;
import dubna.walt.util.ResourceManager;
import dubna.walt.util.Tuner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author serg
 */
public class Utils {

  /**
   * Посылает POST-запрос
   *
   * @param host - URL хоста, куда слать запрос
   * @param encodedData - содержимое запроса
   * @param rm
   * @return - полученный ответ или сообщение об ошибке
   */
  public static String postRequest(String host, String encodedData, ResourceManager rm) {
    Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
    String responce = "";
    int respCode = -1;
    try {
      IOUtil.writeLogLn(1, "POST to:" + host, rm);
      HttpURLConnection connection = (HttpURLConnection) new URL(host).openConnection();
      connection.setConnectTimeout(10000);
      connection.setReadTimeout(100000);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//               conn.setRequestProperty( "Content-Type", "application/json; charset=UTF-8");

      connection.setRequestProperty("Content-Length", String.valueOf(encodedData.length()));
      connection.setDoOutput(true);

      final OutputStream outputStream = connection.getOutputStream();
      outputStream.write(encodedData.getBytes());
      outputStream.close();

      IOUtil.writeLogLn(2, "Reading responce...", rm);
      respCode = connection.getResponseCode();
      String respMsg = connection.getResponseMessage();
      IOUtil.writeLogLn(1, "+++++ respCode = " + respCode + ": " + respMsg, rm);
      cfgTuner.addParameter("responceCode", Integer.toString(respCode));
      cfgTuner.addParameter("responceMsg", respCode + ": " + respMsg);
//            final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
      final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), cfgTuner.getParameter("encoding")));

      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        IOUtil.writeLogLn(9, ": " + line, rm);
//                IOUtil.writeLogLn(line);
        responce += line + "\n";
      }

      reader.close();
      connection.disconnect();
    } catch (Throwable e) {
      cfgTuner.addParameter("ERROR", e.toString());
      cfgTuner.addParameter("ResultCode", "3");
      cfgTuner.addParameter("Result", e.toString());

//      e.printStackTrace();
        System.out.println(" *****  dubna.walt.util.gateway.UtilspostRequest() ERROR: " + e.toString());
        System.out.println(" *****  host: " + host + "; responce=" + responce + "; respCode=" + respCode);
      return e.toString();
    }

    return responce;
  }

  /**
   * Парсит строку с JSON-объектом и раскладывает значения параметров в cfgTuner
   * запроса
   *
   * @param json
   * @param rm
   * @return
   * @throws ParseException
   */
  public static String parseJson(String json, ResourceManager rm) 
//          throws ParseException 
  {
    Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
    IOUtil.writeLogLn(5, "<br><b>+++parseJson:</b> '" + json + "'", rm);
    String o = json;
    if (json.length() < 2) {
      return "";
    }
    if(!json.startsWith("{")) {
        System.out.println(" ***** dubna.walt.util.gateway.parseJson() ERROR: JSON =" + json);
    }
    else {
    try {
      JSONParser parser = new JSONParser();
      JSONObject jsonObj = (JSONObject) parser.parse(json);
      String keys = "";
      String comma = "";
      String key;
      String key2;
      String val;
      Object item;

//        JSONParser aParser = new JSONParser();
      JSONObject aObj;
      Object aItem;
//        IOUtil.writeLogLn(jsonObj.get("paramsStr"), rm);

      for (Iterator iterator = jsonObj.keySet().iterator(); iterator.hasNext();) {
        key = (String) iterator.next();
        item = jsonObj.get(key);
//        IOUtil.writeLogLn(3," --- NEXT KEY=" + key + "; item=" + item, rm);
        key2 = key;
        if (key.contains(" ")) {
          key = key.replaceAll(" ", "_");
          o = o.replaceAll("\"" + key2 + "\"", "\"" + key + "\"");
        }
        // ======= parse String
        if (item instanceof String) {
          val = (String) item;
          cfgTuner.addParameter(key, val);
          IOUtil.writeLogLn(3, " " + key + "=" + val, rm);
        } /* ====== parse OBJECT =====*/ 
        else if (item instanceof JSONObject) {
          aObj = (JSONObject) item;  // (JSONObject) aParser.parse(aItem);
          IOUtil.writeLogLn(5, "<br><b> ...PARSE OBJECT:</b> " + key + "." + "=" + aObj.toString(), rm);
          String eVal = "";
          String eKey;
          Object eItem;
          for (Iterator aiterator = aObj.keySet().iterator(); aiterator.hasNext();) {
            eKey = (String) aiterator.next();
            key2 = eKey;
            if (eKey.contains(" ")) {
              eKey = eKey.replaceAll(" ", "_");
              o = o.replaceAll("\"" + key2 + "\"", "\"" + eKey + "\"");
            }
            eItem = aObj.get(eKey);
//                        IOUtil.writeLog(" ......* " + eKey + "...", rm);
            eKey = key + "." + eKey;
            IOUtil.writeLogLn(3, " ...* " + eKey + "=" + eItem.toString(), rm);
            if (eItem instanceof Double) { //parse Double
              eVal = eItem.toString() ;
            } else if (eItem instanceof Long) { //parse Integer
              eVal = eItem.toString() ;
            } else if (eItem instanceof String) { //parse String
              eVal = (String) eItem;
            } else { //error - no nested arrays allowed
              eVal = eItem.toString();  
//              cfgTuner.addParameter("ERROR", "JSON: Ошибка во вложенном объекте " + key + "." + eKey + "; eItem=" + eItem ); 
              IOUtil.writeLogLn(0, "***** ERROR: JSON: Ошибка во вложенном объекте " + key + "." + eKey + "; eItem=" + eItem + "; eItem.toString()=" + eItem.toString() + "; <b> set as String.</b>", rm);
            }
            IOUtil.writeLogLn(7, " ...* " + eKey + "=" + eVal, rm);
            cfgTuner.addParameter(eKey, eVal);
          }
        } /* ====== parse ARRAY =====*/ 
        else if (item instanceof JSONArray) {
          val = item.toString();
          String arrays = "," + cfgTuner.getParameter("ARRAYS") + ",";
          if (!arrays.contains("," + key + ",")) {
            IOUtil.writeLogLn(0, "<br><b>*** UNKNOWN ARRAY:</b> " + key + ";", rm);
          }
          IOUtil.writeLogLn(3, "<br><b>*** ARRAY: </b>" + key + ":", rm);
//                     + val
          cfgTuner.addParameter(key, val);

          int itemNr = 1;
          String itemList = "";
          JSONArray msg = (JSONArray) jsonObj.get(key);
          Iterator<String> a_iterator = msg.iterator();

//                    System.out.println("ARRAY " + key + ":");
          while (a_iterator.hasNext()) {
            aItem = a_iterator.next();
            if (aItem instanceof JSONObject) {
              val += aItem.toString();
//                            JSONParser aParser = new JSONParser();
              aObj = (JSONObject) aItem;  // (JSONObject) aParser.parse(aItem);
              IOUtil.writeLogLn(5, "<br><b>...PARSE ELEMENT " + itemNr + ":</b> " + key + "." + itemNr + "=" + aObj.toString(), rm);
              String eVal = "";
              String eKey;
              Object eItem;
              for (Iterator aiterator = aObj.keySet().iterator(); aiterator.hasNext();) {
                eKey = (String) aiterator.next();
                eItem = aObj.get(eKey);
                eVal = "";
//                                IOUtil.writeLog(" ......* " + eKey + "...", rm);
                if (eKey.contains(" ")) {
                  eKey = eKey.replaceAll(" ", "_");
                  o = o.replaceAll("\"" + key2 + "\"", "\"" + eKey + "\"");
                }
//                                IOUtil.writeLogLn(" ...* " + eKey + "=" + eItem.toString(), rm);
                eKey = key + "." + itemNr + "." + eKey;
                if (eItem != null && eItem instanceof String) { //parse String
                  eVal = (String) eItem;
                } else { //error - no nested arrays allowed
                  if (eItem == null) {
                    cfgTuner.addParameter("ERROR", "JSON: Ошибка в табличной части - отсутствует значение: " + eKey+ "; val=null");
                    IOUtil.writeLogLn(0, "***** ERROR: JSON: Ошибка в табличной части - отсутствует значение: " +  eKey + "; val=null", rm);                    
                  }
                  else {
                    cfgTuner.addParameter("ERROR", "JSON: Ошибка в табличной части " + eKey+ "; val=" + eItem.toString());
                    IOUtil.writeLogLn(0, "***** ERROR: JSON: Ошибка в табличной части " +  eKey + "; val=" + eItem.toString(), rm);
                  }
                }
                IOUtil.writeLogLn(3, " ...* " + eKey + "=" + eVal, rm);
                cfgTuner.addParameter(eKey, eVal.replace(";", ","));
              }
              itemList += "," + Integer.toString(itemNr);
              itemNr++;
//                            System.out.println(aItem.toString());
            } else {
              cfgTuner.addParameter("ERROR", "JSON: Ошибка в табличной части " + key);
              cfgTuner.addParameter("ERR_CODE", "1");
            }
          }
          if(itemList.length()>1)
            cfgTuner.addParameter(key + ".items", itemList.substring(1));
          cfgTuner.addParameter(key + ".count", Integer.toString(itemNr - 1));
          IOUtil.writeLogLn(3, "<b> ...* ARRAY TOTAL: </b>" + key + ".count=" + cfgTuner.getParameter(key + ".count")
                  + "; " + key + ".items=" + cfgTuner.getParameter(key + ".items") + ";<br>", rm);

        } else {  /*  все остальное */
//          val = (String) item;
          val = item.toString();
          cfgTuner.addParameter(key, val);
          IOUtil.writeLogLn(3, " " + key + "=" + val, rm);
        } /* ====== parse OBJECT =====*/ 

        keys += comma + key;
        comma = ", ";
      }
      cfgTuner.addParameter("JSON Keys", keys);
      IOUtil.writeLogLn(2, "* JSON Keys=" + keys, rm);
    } catch (Exception e) {
      IOUtil.writeLogLn(0, "<b>JSON ERROR: " + e.toString() + "</b> json=`" + json + "`", rm);
      System.out.println("***** ERROR PARSING JSON ***** ");
//      System.out.println("ERROR PARSING `" + json + "`");
      e.printStackTrace(System.out);
      cfgTuner.addParameter("ERROR_JSON", e.toString());
      cfgTuner.addParameter("ERROR", "JSON - ошибка в формате:" + e.toString());
      cfgTuner.addParameter("ERR_CODE", "1");
      return null;
    }
    }
    return o;
  }

  /**
   * Преобразовать строку из вида: "name=value" к строке запроса
   * name=encValue&name2=encValue2&...
   *
   * @param src
   * @param rm
   * @return
   * @throws UnsupportedEncodingException
   */
  public static String encodeString(String src, ResourceManager rm) throws UnsupportedEncodingException {
    Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
    String enc = cfgTuner.getParameter("encoding");
    if (enc.length() < 2) {
      enc = "utf-8";
    }

    return URLEncoder.encode(src, enc);
  }

  /**
   * Преобразовать содержимое секции из вида: "name=value" к строке запроса
   * name=encValue&name2=encValue2&...
   *
   * @param sectionName
   * @param rm
   * @return
   * @throws UnsupportedEncodingException
   */
  public static String getEncodedSection(String sectionName, ResourceManager rm) throws UnsupportedEncodingException {
    Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
    String encodedData = "";

    String[] sa = cfgTuner.getCustomSection(sectionName);
    String[] pair;
    String amp = "";
    for (String sa1 : sa) {
      pair = sa1.split("=");
      String val = "";
      if (pair.length > 1) {
        IOUtil.writeLogLn(9, pair[0].trim() + "=" + pair[1].trim() + ";", rm);
        val = pair[1].trim();
        if (cfgTuner.enabledOption("encoding")) {
          val = URLEncoder.encode(val, cfgTuner.getParameter("encoding"));
        }
        encodedData += amp + pair[0] + "=" + val;
        amp = "&";
      }
//            encodedData += amp + pair[0] + "=" + pair[1];
    }
    return encodedData;
  }

  /**
   * Преобразовать содержимое секции из вида: name=value к строке в JSON:
   * "name":"encValue", "name2":"encValue2",... Внешние скобки { и } не
   * добавляются
   *
   * @param sectionName
   * @param rm
   * @param encode
   * @return
   * @throws UnsupportedEncodingException
   */
  public static String getEncodedJSON(String sectionName, ResourceManager rm, boolean encode) throws UnsupportedEncodingException {
    Tuner cfgTuner = (Tuner) rm.getObject("cfgTuner");
    String encodedJson = "";

    String[] sa = cfgTuner.getCustomSection(sectionName);
//        String[] pair;
    String comma = "";
    for (String sa1 : sa) {
      int k = sa1.indexOf("=");
      String val;
      if (k > 0) {
//                val = sa1.substring(k+1).trim().replace("\"", "\\\"");
        val = sa1.substring(k + 1).trim();
        val = val.replace("\\", "");
        val = val.replace("\"", "`");

//        val = val.replace("\\", "\\\\");
//        val = val.replace("/", "\\/");
//        val = val.replace("\r", "\\\r");
//        val = val.replace("\n", "\\\n");
//        val = val.replace("\t", "\\\t");
//        val = val.replace("\b", "\\\b");
        val = val.replace("\r", " ");
        val = val.replace("\n", " ");
        val = val.replace("\t", " ");
        val = val.replace("\b", "");
//        val = val.replace("&", "\\\\u0026");
        IOUtil.writeLogLn(9, sa1.substring(0, k) + "/=" + val + ";", rm);
        if (encode && cfgTuner.enabledOption("encoding")) {
          val = URLEncoder.encode(val, cfgTuner.getParameter("encoding"));
        }
        encodedJson += comma + " \"" + sa1.substring(0, k) + "\":\"" + val + "\"";
      } else {
        encodedJson += comma + " \"" + sa1 + "\":\"\"";
      }
      /*            pair = sa1.split("=");
            if (pair.length > 1) {
                val = pair[1].trim().replace("\"", "\\\"");
//        IOUtil.writeLogLn(pair[0].trim() + "=" + val + ";", rm);
                if (encode && cfgTuner.enabledOption("encoding")) {
                    val = URLEncoder.encode(val, cfgTuner.getParameter("encoding"));
                }
            }
            encodedJson += comma + " \"" + pair[0] + "\":\"" + val + "\"";
/**/
      comma = ",";
//            encodedData += amp + pair[0] + "=" + pair[1];
    }
    IOUtil.writeLogLn(9, "[" + sectionName + "]=>" + encodedJson + ";", rm);
    return encodedJson;
  }

}
