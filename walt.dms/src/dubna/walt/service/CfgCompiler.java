package dubna.walt.service;

import dubna.walt.util.*;
// import java.util.*;
import java.io.*;

/**
 *
 * @author serg
 */
public class CfgCompiler extends Service
{

public void start() throws Exception
{
  System.out.println("============CfgCompiler==============");
  cfgTuner.outCustomSection("report",out);
  rm.setParam("readFromDisk", "true");
  
  if (cfgTuner.enabledOption("cop=l"))
    getFileList("");  

  String[] configs=cfgTuner.getCustomSection("configs");
  if (configs != null)
    for (int i=0; i<configs.length; i++)
    { out.println(configs[i]);
      out.flush();
      compileFile(configs[i]);
    }
}

    /**
     *
     * @param path
     */
    protected void getFileList (String path)
{
//  out.println("======= <b>Folder: " + rm.getString("CfgRootPath") + path + ":</b><br>");
	out.println("======= <b>Folder: " + cfgTuner.getParameter("CfgRootPath") + path + ":</b><br>");
  int i = path.lastIndexOf(File.separator);

  /* get the directiory file list */
  File dir = new File( cfgTuner.getParameter("CfgRootPath") + path);
  String[] list = dir.list();
  for (i=0; i < list.length; i++)
  { if (list[i].indexOf(".") > 0)
    { if (list[i].indexOf(".cfg") > 0 || list[i].indexOf(".dat") > 0)
      { out.println(cfgTuner.getParameter("CfgRootPath") + path + list[i]);
        compileFile(path + list[i]);
      }
    }
    else if (list[i].indexOf("CVS") < 0)
    { out.println("folder: " + list[i] + "<br>");
      getFileList (path + list[i] + "/");
      out.println("======= <b>back to Folder: " + cfgTuner.getParameter("CfgRootPath") + path + ":</b><br>");
    }
  }
}

    /**
     *
     * @param cfgFilePath
     */
    protected void compileFile(String cfgFilePath)
{
  try
  {
    debugOut("<xmp>", out);
    /* make the top part */
    String className = StrUtil.replaceIgnoreCase(cfgFilePath, ".cfg", "");
    className = className.substring(0,1).toUpperCase()
              + className.substring(1).toLowerCase();
    className = className.replace('/', '_').replace('\\', '_').replace('.', '_').replace('-', '_');
    out.print(" --> "+ cfgTuner.getParameter("outputFolder") + className + ".java... ");
    out.flush();
    cfgTuner.addParameter("className", className);
    
    String[] content = BasicTuner.readFileFromDisk(cfgTuner.getParameter("CfgRootPath") + cfgFilePath, rm);
		if (content != null)
		{
		  String[] result = cfgTuner.getCustomSection("top");
		  PrintWriter pw = getResultWriter(className);
			cfgTuner.outCustomSection("top", pw);
			debugOut("top");

		  /* make the middle (body) part */
			String sectionsList = "";
			String sectionName = "";
			String c = "";
			StringBuilder sb = null;
			
			for (int i=0; i<content.length; i++)
			{ c = content[i].trim();     
				if ((c.length() > 0) 
				 && (c.indexOf("??") != 0) 
				 && (c.length()==1 || c.indexOf("??") != c.length() - 2))
				{ c = StrUtil.replaceInString(
							StrUtil.replaceInString(c, "\"", "/#\"")
																			 , "\\", "\\\\");
					c = StrUtil.replaceInString(c, "/#\"", "\\\"");
	//        debugOut(sectionName + ":" + i + ": " + c);
					if (c.indexOf("[") == 0 && c.indexOf("]") > 1)
					{ if (sectionName.length() == 0)
						{ sectionName = c.substring(1, c.indexOf("]"));
							sectionName = sectionName.replace('/', '_')
																			 .replace('\\', '_')
																			 .replace(' ', '_')
																			 .replace('-', '_')
																			 .replace('.', '_');
							sectionsList = sectionsList + "," + sectionName;
							sb = new StringBuilder(1000);
							sb.append("\"" + c + "\"\n");
						}
						else if (c.indexOf("[end]") == 0)
						{ cfgTuner.addParameter("sectionName",sectionName);
							sectionName = "";
							sb.append(",\"[end]\"\n");          
							cfgTuner.addParameter("section", "==" + sb.toString());
							result = cfgTuner.getCustomSection("section_body");
							cfgTuner.outCustomSection("section_body", pw);
							debugOut("section_body");
						}
						else
							sb.append(",\"" + c + "\"\n");
					}
					else if (sectionName.length() > 0)
					{ sb.append(",\"" + c + "\"\n");
					}
				}
			}
				 
			/* make the final part */
			cfgTuner.addParameter("sectionsList", sectionsList.substring(1));
			result = cfgTuner.getCustomSection("final");
			cfgTuner.outCustomSection("final_part", pw);
			debugOut("final");
			pw.flush();
			pw.close();
		}
    
    debugOut("</xmp>", out);
    out.println("ok!<br>");
    out.flush();
  }
  catch (Exception e)
  { out.println("<xmp>");
    e.printStackTrace(out);
    out.println("</xmp>");
  }
}

    /**
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    public PrintWriter getResultWriter (String fileName) throws Exception
{
  String path = cfgTuner.getParameter("outputFolder");
//  System.out.println("===> Writing file: " + path + fileName);

//  FileOutputStream lf = new FileOutputStream(path + fileName + ".java", false);
	OutputStreamWriter lf = new OutputStreamWriter 
//	(new FileOutputStream(path + fileName + ".java", false), "ISO_8859-1");
		(new FileOutputStream(path + fileName + ".java", false), rm.getString("serverEncoding", false, "Cp1251"));
	
  if (lf == null) return null;
  return (new PrintWriter(lf));
}

    /**
     *
     * @param sectionName
     * @throws Exception
     */
    public void debugOut(String sectionName) throws Exception
{ if (cfgTuner.enabledOption("debugOut=y"))
    cfgTuner.outCustomSection(sectionName, out);
}

    /**
     *
     * @param s
     * @param out
     */
    public void debugOut(String s, PrintWriter out)
{ if (cfgTuner.enabledOption("debugOut=y"))
    out.println(s);
}


}