package jinr.hrlhep.gateway;

import dubna.walt.util.gateway.Utils;

/**
 *
 * @author serg
 */
public class ServiceReceiveObject extends dubna.walt.service.Service {
        @Override
        
    public void start() throws Exception {

        cfgTuner.getCustomSection("LOG REQUEST");        
        String object = cfgTuner.getParameter("Object");
        if(!cfgTuner.enabledOption("ERROR"))
            Utils.parseJson(object, rm);  
//parseJson(object);
        if(!cfgTuner.enabledOption("ERROR"))
            cfgTuner.getCustomSection("process request");        

        super.start();

//        cfgTuner.addParameter("RESPONCE", responce);
//        IOUtil.writeLogLn("RESPONCE:" + responce, rm);
         
         
//        cfgTuner.getCustomSection("process post result");   
        cfgTuner.addParameter("CloseSession", "Y");
    }

       
}
