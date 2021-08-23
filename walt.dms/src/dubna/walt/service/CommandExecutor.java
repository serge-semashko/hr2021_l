package dubna.walt.service;

import java.io.InputStream;

/** Description of class
 *
 * @author kouniaev
 * @version 1.0
 */


public class CommandExecutor extends Service
{

boolean silent = false;

public void start() throws Exception
{
  int numCommandsExecuted = 0;
  silent = cfgTuner.enabledOption("silent")
        &&!cfgTuner.enabledOption("debug=on");
        
  if (!silent)
    cfgTuner.outCustomSection("report header",out);

  if (validateInput())
  {
    Runtime rt = Runtime.getRuntime();
    Process p;
    String[] commands = cfgTuner.getCustomSection("commands");
    if (commands != null)
    {
      for (int i=0; i < commands.length; i++)
      {
        output("<p><b>" + (i + 1) + ": Command: '" + commands[i] +"'</b>");
        log (i + ": Starting command: '" + commands[i] +"'");
        p = rt.exec(commands[i]);
        InputStream from = p.getInputStream();
        log (" waiting...");
        output("<pre>");
        copyAll(from);
        output("</pre>");
    //      p.waitFor();
        log (" === Finished!");
        log ("     Exit code=" + Integer.toString(exitValue(p)));
        numCommandsExecuted++;
      }
    }
    else
      output("<p><b> Commands not executed - there are input mistakes!</b><p>");
  }

  if (numCommandsExecuted == 0)
    cfgTuner.addParameter("ERROR", "Nothing has been executed");

  cfgTuner.addParameter("NumComands", Integer.toString(numCommandsExecuted));

/**/  if (!silent)
  {
    cfgTuner.outCustomSection("report footer",out);
    out.flush();
  }
/**/  
}

    /**
     *
     * @param from
     */
    public void copyAll(InputStream from)
{
  int ch =0;
  try
  {
    while(ch >= 0 )
    {
      ch = from.read();
      output(ch);
    }
  }
  catch (Exception e)
  {
    System.out.println(e.toString());
  }
}

    /**
     *
     * @param p
     * @return
     */
    public int exitValue(Process p)
{
  int val = 0;
  try
  {
    val = p.exitValue();
    return val;
  }
  catch (IllegalThreadStateException e)
  {
    log ("CommandExecutor-Warning: IllegalThreadStateException.");
    return -1;
  }
}

    /**
     *
     * @param s
     */
    public void log(String s)
{
  if (silent) return;
  System.out.println(s);
}

    /**
     *
     * @param s
     */
    public void output(String s)
{
  if (silent) return;
  out.println(s);
  out.flush();
}

    /**
     *
     * @param ch
     */
    public void output(int ch)
{
  if (silent) return;
  out.write(ch);
  out.flush();
}

    /**
     *
     * @return
     * @throws Exception
     */
    public boolean validateInput() throws Exception
{
  return true;
}

}