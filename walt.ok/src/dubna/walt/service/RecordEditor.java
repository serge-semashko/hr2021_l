package dubna.walt.service;

import java.sql.ResultSet;
import dubna.walt.util.*;

/** Description of class
 *
 * @author kouniaev
 * @version 1.0
 */


public class RecordEditor extends Service
{

    /**
     *
     */
    protected String sql;

    /**
     *
     */
    protected ResultSet r;

public void start() throws Exception
{
  System.out.println("============RecordEditor==============");
  validateInput();
  if (!cfgTuner.enabledOption("INPUT_ERRORS"))
  {
    String cop = cfgTuner.getParameter("cop");
    if (cop.equalsIgnoreCase("U"))  updateRecord();
    else if (cop.equalsIgnoreCase("D"))  deleteRecord();
    else if (cop.equalsIgnoreCase("C"))  insertRecord();
    getRecord();
  }
  else
    cfgTuner.addParameter("msg"," - ошибка в данных!");

  cfgTuner.outCustomSection("report",out);
}

    /**
     *
     * @throws Exception
     */
    protected void getRecord() throws Exception
{
  String s = "";
  if (cfgTuner.getParameter("key").equals("0")) return;
  r = dbUtil.getResults(getSQL("get sql"));
  String[] headers = DBUtil.getColNames(r);

  if (r.next())
    for (int i = 0 ; i < headers.length; i++)
    {
      s = r.getString(i+1);
//      IOUtil.writeLogLn(headers[i] + "='" + s + "'", rm);
      if (s != null && s.length() > 0)
        cfgTuner.addParameter(headers[i], s);
      else
        cfgTuner.deleteParameter(headers[i]);
    }
  else
    for (int i = 0; i < headers.length; i++)
      cfgTuner.deleteParameter(headers[i]);
      
  dbUtil.closeResultSet(r);
}

    /**
     *
     * @throws Exception
     */
    protected synchronized void insertRecord() throws Exception
{
  sql=getSQL("getKey sql");
  r = dbUtil.getResults(sql);
  if (r.next())
  {
    long key = r.getLong(1);
    cfgTuner.addParameter("key",Long.toString(key));

    sql=getSQL("insert sql");

    if (dbUtil.update(sql) == 1)
      cfgTuner.addParameter("msg"," успешно добавлена!");
    else
      cfgTuner.addParameter("msg"," - ошибка при добавлении!");
    dbUtil.commit();
    Thread.sleep(100);
  }
  else
    cfgTuner.addParameter("msg"," - ошибка при добавлении - не могу получить ключ записи!");
  dbUtil.closeResultSet(r);
}

    /**
     *
     * @throws Exception
     */
    protected synchronized void updateRecord() throws Exception
{
  IOUtil.writeLog(3, "InfoEditor/Update parameters: ", cfgTuner.parameters, rm);

  sql=getSQL("update sql");
  int num = dbUtil.update(sql);
  if ( num >= 1)
  {
    if (num == 1)
      cfgTuner.addParameter("msg"," успешно обновлена.");
    else
      cfgTuner.addParameter("msg"," успешно обновлено " + Integer.toString(num) + " записей.");
  }
  else
  {
    cfgTuner.addParameter("msg"," - ошибка при обновлении!");
  }
  dbUtil.commit();
  Thread.sleep(100);
}

    /**
     *
     * @throws Exception
     */
    protected synchronized void deleteRecord() throws Exception
{
  sql = getSQL("delete sql");
  if (dbUtil.update(sql) >= 1)
  {
    cfgTuner.addParameter("msg"," успешно удалена!");
    cfgTuner.addParameter("key", "0");
  }
  else
    cfgTuner.addParameter("msg"," - ошибка при удалении!");
  dbUtil.commit();
  Thread.sleep(100);
}

public String getSQL(String sqlName) throws Exception
{
  String[] sql;
  IOUtil.writeLogLn(3, sqlName + ":", rm);
  sql = cfgTuner.getCustomSection(sqlName);
  IOUtil.writeLog(3, sqlName + ":", sql, rm);
  return StrUtil.strFromArray(sql);
//  return StrUtil.replaceInString(StrUtil.strFromArray(sql),"','",",");
}

    /**
     *
     * @throws Exception
     */
    public void validateInput() throws Exception
{
}

}