package dubna.walt.service;

import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FileTransferClient;
import java.io.IOException;

/**
 *
 * @author serg
 */
public class FtpService extends dubna.walt.service.Service
{

private FileTransferClient ftp = null;
	
public void start() throws Exception
{
	ftp = (FileTransferClient) rm.getObject("FileTransferClient", false);
	if (cfgTuner.enabledOption("cop=close"))
	{ closeConnect();
	}
	else if (cfgTuner.enabledOption("user_id"))
  {
		if (ftp == null)
			ftpConnect();
		if (ftp != null)
		{
		  try {
		    long tm = System.currentTimeMillis();
		    System.out.print( 
//				 cfgTuner.getParameter("srcPath") + 
				 cfgTuner.getParameter("srcFile") 
		     + " => " + cfgTuner.getParameter("ftpIP") 
		     + "/" + cfgTuner.getParameter("ftpPath") + cfgTuner.getParameter("destFile"));
				ftp.uploadFile(cfgTuner.getParameter("srcPath") + cfgTuner.getParameter("srcFile"),
					cfgTuner.getParameter("ftpPath") + cfgTuner.getParameter("destFile"));
		    tm = System.currentTimeMillis() - tm;
		    System.out.println("; upload time:" + tm + "ms. ");
		  } catch (Exception e) {
		      e.printStackTrace();
//		      cfgTuner.addParameter("FTP_ERROR", e.toString());
					closeConnect ( );					
		    try {
		      ftpConnect();
		      if (ftp != null)
						ftp.uploadFile(cfgTuner.getParameter("srcPath") + cfgTuner.getParameter("srcFile"),
							cfgTuner.getParameter("ftpPath") + cfgTuner.getParameter("destFile"));
		    } catch (Exception ne) {
				  cfgTuner.addParameter("FTP_ERROR", e.toString());
					throw ne;
				}
		  }
		}
  }
  super.start();
}

private void ftpConnect()
{
	System.out.print("FTP connect... ");
	try {
		long tm = System.currentTimeMillis();
		ftp = new FileTransferClient();
		ftp.setRemoteHost(cfgTuner.getParameter("ftpIP"));
		ftp.setUserName(cfgTuner.getParameter("ftpUser"));
		ftp.setPassword(cfgTuner.getParameter("ftpPw"));
		ftp.connect();
		tm = System.currentTimeMillis() - tm;
		System.out.println(" OK, connect time:" + tm + "ms. ");
		rm.setObject("FileTransferClient", ftp, true);
	} catch (Exception e) {
			e.printStackTrace();
			cfgTuner.addParameter("FTP_ERROR", e.toString());
	}
}

private void closeConnect()
{
	if (ftp != null)
	{
		System.out.println("Closibg FTP client");
			try
			{
				rm.removeKey("FileTransferClient");
				ftp.disconnect();		
				ftp = null;
			}
			catch( Exception e )
			{ ftp = null; 
				e.printStackTrace();
			}
		}
}

}