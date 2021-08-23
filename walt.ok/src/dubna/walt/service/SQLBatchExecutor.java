package dubna.walt.service;

import java.sql.*;
import dubna.walt.util.*;

/**
 *
 * @author serg
 */
public class SQLBatchExecutor extends Service
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
  cfgTuner.outCustomSection("report header",out);
  out.flush();
  cfgTuner.addParameter("msg"," Запрос успешно выполнен!");
  String[] sqls = cfgTuner.getCustomSection("SQLS");
  for (int i = 0; i < sqls.length; i++)
  {
    String[] actions = cfgTuner.getCustomSection("Action MSGS");
    out.println(actions[i]);
    out.flush();
    if (!processSQL(sqls[i]))
    {
      cfgTuner.addParameter("msg"," Возникла ошибка! Выполнение запроса прекращено.");
      break;
    }
    String[] results = cfgTuner.getCustomSection("Results MSGS");
    out.println(results[i]);
  }

  cfgTuner.outCustomSection("report footer",out);
  out.flush();
  IOUtil.writeLog("SQLBatchExecutor / Last parameters: ", cfgTuner.parameters, rm);
}


synchronized boolean processSQL(String sqlName) throws Exception
{
  sql=getSQL(sqlName);
  if (sql.toUpperCase().indexOf("SELECT") == 0)
  { getPreData(sql);
    return true;
  }
  else
  { int numRec = dbUtil.update(sql);
    dbUtil.commit();
    Thread.sleep(100);
    cfgTuner.addParameter("numrec",Integer.toString(numRec));
    return (numRec >= 0);
  }
}

public String getSQL(String sqlName) throws Exception
{
  String[] sql;
  IOUtil.writeLogLn(sqlName + ":", rm);
  sql = cfgTuner.getCustomSection(sqlName);
  IOUtil.writeLog(sqlName + ":", sql, rm);
  return StrUtil.strFromArray(sql);
//  return StrUtil.replaceInString(StrUtil.strFromArray(sql),"','",",");
}

}