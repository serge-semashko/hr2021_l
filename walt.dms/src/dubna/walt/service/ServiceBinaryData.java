package dubna.walt.service;

import java.sql.*;
import javax.sql.rowset.serial.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import dubna.walt.util.FileContent;

/**
 *
 * @author serg
 */
public class ServiceBinaryData extends dubna.walt.service.Service
{

	public void start() throws Exception

	{ String file_path = cfgTuner.getParameter("FILE_PATH");
		String content_type = cfgTuner.getParameter("CONTENT_TYPE");
		String file_name = cfgTuner.getParameter("NAME");
		String file_size = cfgTuner.getParameter("SIZE");
		boolean getFromDB = file_path.length() == 0 || file_path.equalsIgnoreCase("DB");
		ServletOutputStream outStream = (ServletOutputStream)rm.getObject("outStream");
		try
		{
	//        System.out.println("file_name:" + file_name + "| file_path:" + file_path);
				if (content_type.length() < 2)
				{ ServletContext sc = (ServletContext) rm.getObject("ServletContext");
					content_type = sc.getMimeType(file_path);
				}
				if (file_name.length() < 1)
				{ int i = file_path.lastIndexOf("/");
					file_name = file_path.substring(i+1);
				}
				if (!getFromDB && file_size.length() < 1)
				{ file_size = Long.toString(FileContent.getFileSize(file_path));
				}
//				System.out.println("ContentType:" + content_type + "; File Name=" + file_name + "; size=" + file_size + "; Path:" + file_path);
			
				response.setContentType(content_type);
				if (!cfgTuner.enabledOption("inline"))
					response.setHeader ("Content-Disposition", "attachment; filename=" + file_name);
				response.setHeader ("Content-length", file_size);
				outStream.flush();
				
				if (getFromDB)
				  serveDbBlob(outStream);
				else
				  FileContent.copyFileData(file_path, outStream);
		}
		catch (Exception e) 
		{ e.printStackTrace(out);
		}
		finally
		{ outStream.flush();
			outStream.close();
		}
	}

    /**
     *
     * @param outStream
     * @throws Exception
     */
    public void serveDbBlob(ServletOutputStream outStream) throws Exception
	{
		int numPages = cfgTuner.getIntParameter("NUM_PAGES");
		for (int i=0; i<=numPages; i++)
		{	cfgTuner.addParameter("PAGE_NUMBER", Integer.toString(i));
			String sql = getSQL ("getFilePageSQL");
//			System.out.println(" sql=" + sql);
			//  SerialBlob sb = null;
			//  oracle.sql.BLOB bl = null;
			java.sql.Blob bl = null;
			ResultSet r = dbUtil.getResults(sql);
//			System.out.println(" r=" + r);
			if (r.next())
			{ 
//				System.out.print(i + ": ");
				bl = new SerialBlob( r.getBlob(1));
		//        bl = (oracle.sql.BLOB) r.getBlob(2);
		//       System.out.println("BLOB=" + bl + "; LENGTH=" + bl.length());
//		 System.out.print(i + ".");
				byte[] dat = bl.getBytes(1, (int)bl.length());
//				System.out.println(" size=" + bl.length());
				outStream.write(dat);
				outStream.flush();
//				Thread.sleep(2000);
			}
			outStream.flush();
			dbUtil.closeResultSet(r); 
		}
	}

/*

public void x_serveDbBlob(ServletOutputStream outStream) throws Exception
	  {
	    String sql = getSQL ("getFileBodySQL");
	    System.out.println(" sql=" + sql);
	    //  SerialBlob sb = null;
	    //  oracle.sql.BLOB bl = null;
	    java.sql.Blob bl = null;
	    ResultSet r = dbUtil.getResults(sql);
	    System.out.println(" r=" + r);
	    int i=0;
	    while (r.next())
	    { 
	      System.out.print(++i + ": ");
	      bl = new SerialBlob( r.getBlob(1));
	  //        bl = (oracle.sql.BLOB) r.getBlob(2);
	  //       System.out.println("BLOB=" + bl + "; LENGTH=" + bl.length());
	   System.out.print(i + ".");
	      byte[] dat = bl.getBytes(1, (int)bl.length());
	      System.out.println(" size=" + bl.length());
	      outStream.write(dat);
	      outStream.flush();
	      Thread.sleep(2000);
	    }
	    outStream.flush();
	    dbUtil.closeResultSet(r); 
	  }


	/* debug - optimize print 
		public void serveDbBlob(ServletOutputStream outStream) throws Exception
		{
			String sql = getSQL ("getFileBodySQL");
			//  SerialBlob sb = null;
			//  oracle.sql.BLOB bl = null;
			java.sql.Blob bl = null;
			System.out.println("+++++++++++++++++++ Start fetching file data +++++++++++++++++++");
			long tm = System.currentTimeMillis();
			long t2 = System.currentTimeMillis();
			ResultSet r = dbUtil.getResults(sql);
			while (r.next())
			{ bl = new SerialBlob( r.getBlob(1));
		//        bl = (oracle.sql.BLOB) r.getBlob(2);
		//       System.out.println("BLOB=" + bl + "; LENGTH=" + bl.length());
				byte[] dat = bl.getBytes(1, (int)bl.length());
				long t1 = System.currentTimeMillis();
					System.out.println("+++ got blob:" + (t1 - t2) + "; total:" + (t1 - tm) + "ms. " + Long.toString(dat.length));
		//  byte[] dat = sb.getBytes(1, (int)sb.length());
		//  out.println("file_name:" + file_name + "<p>");
				outStream.write(dat);
		//    outStream.flush();
				t2 = System.currentTimeMillis();
				long t_flush = t2 - t1;
				t1 = System.currentTimeMillis() - tm;
				System.out.println("     FLUSHED:" + t_flush + "; total:" + t1);
			}
			outStream.flush();
			dbUtil.closeResultSet(r); 
		}
	*/

	}
