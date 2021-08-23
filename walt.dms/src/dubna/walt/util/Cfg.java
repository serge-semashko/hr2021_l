package dubna.walt.util;

/**
 * Используется в BasicTuner как базовый класс для скомпилированных .cfg-файлов
 * @author serg
 */
public class Cfg
{

    /**
     *
     */
    public String[][] body=null;

    /**
     *
     * @return
     */
    public String[] getContent()
{ 
  //System.out.println(this + " ++ getContext - body:'" + body + "'");

  if (body == null) return (new String[] {""});
  int len = 0;
  for (int i = 0; i < body.length; i++)
  {
    len += body[i].length;
  }
//  System.out.println("*** len=" + len);
  String[] result = new String[len];
  int k = 0;
  for (int i = 0; i < body.length; i++)
    for (int j = 0; j < body[i].length; j++)
    {
      result[k++] = body[i][j];
//      System.out.println(body[i][j]);
    }
  
  return result;
}


}