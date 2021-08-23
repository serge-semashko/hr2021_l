package dubna.walt;

import dubna.walt.util.ResourceManager;

/**
 *
 * @author serg
 */
public class WLoginServlet extends dubna.walt.BasicServlet
{

    /**
     *
     * @return
     * @throws Exception
     */
    public ResourceManager obtainResourceManager() throws Exception
{ return new ResourceManager("wl");
}
   
}
