package dubna.walt;


import dubna.walt.util.ResourceManager;

/**
 *
 * @author serg
 */
public interface QueryThread
{


/**
  *
     * @param rm
     * @throws Exception
  */
public void init(ResourceManager rm) throws Exception;

/**
  *
  *
     * @throws Exception
  */
public void start() throws Exception;

//public void finished();

/**
	 * 
	 * @param e
	 */
	public void logException(Exception e);

}