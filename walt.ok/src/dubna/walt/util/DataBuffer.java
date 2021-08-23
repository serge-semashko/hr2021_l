package dubna.walt.util;

import java.util.Vector;
import java.io.OutputStream;

/**
 *
 * @author serg
 */
public class DataBuffer
{
Vector buffers = null;

    /**
     *
     * @param data
     * @param len
     */
    public void addData(byte[] data, int len)
{
  try
  { if (buffers == null)
      buffers = new Vector(10, 20);
    byte[] buf = new byte[len];
    for (int i=0; i<len; i++)
      buf[i] = data[i];
    buffers.add(buf);
    System.out.println("======== DataBuffer Add Data - OK! ");
  }
  catch (Exception e)
  { e.printStackTrace(System.out);
  }
}

    /**
     *
     * @param out
     */
    public void outData (OutputStream out)
{ try
  {for (int i = 0; i< buffers.size(); i++)
    { byte[] buf = (byte[]) buffers.elementAt(i);
      out.write(buf);
    }
    out.flush();
//    System.out.println("======== DataBuffer OUT - OK! ");
  }
  catch (Exception e)
  { e.printStackTrace(System.out);
  }
}


}