package dubna.walt.service;

/** 
 *
 * @author kouniaev
 * @version 1.0
 */

public class SqlScriptExecutor extends Service
{

public void start() throws Exception
{
//  System.out.println("SqlScriptExecutor...");
  String sql;
  int nextSqlNr = 1;

  while ((sql = getSqlNr(cfgTuner, "SQLs", nextSqlNr)).length() > 1)
  {
    out.println("<hr><b>SQL: " + Integer.toString(nextSqlNr) + ":</b> " + sql);
    out.flush();
    getPreData(sql);
    nextSqlNr++;
  }

  if (nextSqlNr == 1)
    cfgTuner.addParameter("ERROR", "Nothing has been executed");
//    cfgTuner.addOption("ERROR");

  cfgTuner.addParameter("NumSQLs", Integer.toString(nextSqlNr-1));

}

}