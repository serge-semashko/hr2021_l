package dubna.walt.util;

import java.util.Vector;
import java.io.PrintWriter;

/**
 *
 * @author serg
 */
public class Stack
{

private Vector v = null;
private int ind = 0;

/**
 * Class constructor.  <P>
 *
     * @throws java.lang.Exception
 */
public Stack() throws Exception
{
  v = new Vector(10,10);
}

    /**
     *
     * @return
     */
    public int size()
{
  return v.size();
}

    /**
     *
     * @param l
     */
    public void push (long l)
{
  v.addElement(new Long(l));
//  System.out.println("Stack: push " + l + "; size=" + v.size());
}

    /**
     *
     * @param obj
     */
    public void push (Object obj)
{
  v.addElement(obj);
//  System.out.println("Stack: push " + obj + "; size=" + v.size());
}

    /**
     *
     * @return
     */
    public Object getFirst()
{
//  System.out.println("Stack: getFirst; size=" + v.size());
  if (v.size() == 0) return null;
  ind = 0;
  Object l = v.firstElement();
//  System.out.println("Stack: getFirst " + l + "; size=" + v.size());
  return l;
}

    /**
     *
     * @return
     */
    public Object getNext()
{
//  System.out.println("Stack: getNext; size=" + v.size());
  if (++ind >= v.size()) return null;
  Object l = v.elementAt(ind);
//  System.out.println("Stack: getNext " + l + "; size=" + v.size());
  return l;
}

    /**
     *
     * @param out
     */
    public void outContents(PrintWriter out)
{
  out.print("=== Stack contents: ");
  for (int i=0; i < v.size(); i++)
  {
    Object o = v.elementAt(i);
    out.print(((Long) o).longValue() + "; ");
  }
//  out.println("<br>");
}

    /**
     *
     * @return
     */
    public Object getPrev()
{
//  System.out.println("Stack: getNext; size=" + v.size());
  if (--ind < 0) return null;
  Object l = v.elementAt(ind);
//  System.out.println("Stack: getPrev " + l + "; size=" + v.size());
  return l;
}

    /**
     *
     * @return
     */
    public Object getLast()
{
//  System.out.println("Stack: getLast; size=" + v.size());
  if (v.size() == 0) return null;
  Object l = v.lastElement();
  ind = v.size() - 1;
//  System.out.println("Stack: getLast " + l + "; size=" + v.size());
  return l;
}

    /**
     *
     * @return
     */
    public Object pop()
{
//  System.out.println("Stack: pop; size=" + v.size());
  if (v.size() == 0) return null;
  Object l = getLast();
  v.removeElementAt(v.size() - 1);
//  try { System.out.println("Stack: pop " + ((Long) l).longValue() + "; size=" + v.size()); }
//  catch (Exception e) {}
  return l;
}

}