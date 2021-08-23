package dubna.walt.service;

//import java.sql.*;
//import dubna.walt.util.*;

/** Description of class
 *
 * @author kouniaev
 * @version 1.0
 */


public class SqlProcedureExecutor extends Service
{

public void start() throws Exception
{
//  System.out.println("SqlScriptExecutor...");
  String sqlProc = cfgTuner.getParameter( "SqlProcedure");
  out.println("<hr><b>SQL procedure called: " + sqlProc + ":</b> ");
  out.flush();

  if (sqlProc.length() > 2)
  { Service.executeProcedure(sqlProc, dbUtil, rm);
  }
  else
  {  out.println(" - too short - Ignored!<p>");
  }
  super.start();
}

}