package dubna.walt.util;

/**
 * Используется для организации задержки в .bat-файлах
 * @author serg
 */
public class Delay 
{

    /**
     *
     * @param args
     */
    public static void main(String[] args)
  {
//    Delay delay = new Delay();
    int sec = Integer.parseInt(args[0]);
    System.out.print(" delay " + sec + " sec...");
    try{
    Thread.sleep(sec*1000);
    } catch (Exception e) {}
    System.out.println("OK.");
  }
}