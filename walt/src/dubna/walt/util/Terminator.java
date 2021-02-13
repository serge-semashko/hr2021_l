package dubna.walt.util;

import dubna.walt.service.*;
import java.io.OutputStream;

/**
 *
 * @author serg
 */
public class Terminator extends Thread
{

    /**
     *
     */
    public Service srv=null;

    /**
     *
     */
    protected ResourceManager rm=null;

    /**
     *
     */
    protected DBUtil dbUtil=null;

    /**
     *
     */
    protected OutputStream out = null;

    /**
     *
     */
    public boolean finished=false;

    /**
     *
     */
    public int intervalMs = 1000;
 
    /**
     *
     * @param rm
     * @param dbUtil
     * @param srv
     */
    public Terminator(ResourceManager rm, DBUtil dbUtil, Service srv)
{ this.rm=rm;
  this.dbUtil=dbUtil;
  this.srv=srv;
  Thread thread = new Thread( this );
	thread.start();
}

public void run()
{ int b = (int) ' ';
  try { Thread.sleep(intervalMs);} catch (InterruptedException e) { return; }
//  System.out.println("~~~~~~~ Terminator ~~~~~~~~~");
  while (!finished)
  { try { Thread.sleep(intervalMs);} catch (InterruptedException e) { return; }
    if (out == null)
        out = (OutputStream) rm.getObject("outStream", false);
    if (out != null)  
    { try
      { if (finished) return;
        out.write(b);  out.flush();
//        System.out.print(".");
      }
      catch (Exception ex)
      { if (finished) return;
        System.out.println("********** TERMINATING QUERY!");
        ex.printStackTrace(System.out);
        if (srv != null) srv.setTerminated();
        return;
      }
    }
  }
}

}