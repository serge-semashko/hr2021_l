package dubna.walt.service;


/**
 *
 * @author serg
 */
public class ServiceLogout extends dubna.walt.service.Service {
    	public void start () throws Exception
	{	
            super.start();
            cfgTuner.session.invalidate();
	}

}
