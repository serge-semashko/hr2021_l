package dubna.walt.util;

	import java.awt.Graphics2D;
	import java.awt.RenderingHints;
	import java.awt.geom.AffineTransform;
	import java.awt.image.BufferedImage;
	import java.io.ByteArrayInputStream;
	import java.io.ByteArrayOutputStream;

import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author serg
 */
public class ImgUtil_sav
{

    /**
     *
     */
    public int height = 0;

    /**
     *
     */
    public int width = 0;

    /**
     *
     */
    public int size = 0;

    /**
     *
     */
    public String formatName="JPG";

    /**
     *
     */
    protected BufferedImage img = null;

    /**
     *
     * @param src
     * @param formatName
     * @throws IOException
     */
    public ImgUtil_sav(byte[] src, String formatName) throws IOException
	{
	
	img = ImageIO.read( new ByteArrayInputStream( src ) );
	height = img.getHeight();
	width = img.getWidth();
	size = src.length;
	this.formatName = formatName;
}

    /**
     *
     * @param img
     * @param formatName
     * @throws IOException
     */
    public ImgUtil_sav(BufferedImage img, String formatName) throws IOException
	    {
	    
	    this.img = img;
	    height = img.getHeight();
	    width = img.getWidth();
	    this.formatName = formatName;
	  }

    /**
     *
     * @param newWidth
     * @param newHeight
     * @param keepAspectRatio
     * @return
     * @throws IOException
     */
    public ImgUtil resize(int newWidth, int newHeight, boolean keepAspectRatio) throws IOException
	{
	
			    System.out.println("max w:"+newWidth + "; src w:"+width);
			    System.out.println("max h:"+newHeight + "; src h:"+height);
		  int destWidth = newWidth;
		  int destHeight = newHeight;
			if (keepAspectRatio)
			{
			  float koeff;
			  float kh = ( float ) height / newHeight;
			  float kw = ( float ) width / newWidth;
				if( kh > kw )
					koeff = kh;
				else
					koeff = kw;
			  destWidth = ( int ) ( width / koeff );
			  destHeight = ( int ) ( height / koeff );
			}
		  BufferedImage dest = img;

			System.out.println("dest h:"+destHeight + "; dest w:"+destWidth);
			dest = new BufferedImage( destWidth, destHeight, BufferedImage.TYPE_INT_RGB );
			Graphics2D g = dest.createGraphics();
			AffineTransform at = AffineTransform.getScaleInstance( ( double ) destWidth / width, ( double ) destHeight / height );
			RenderingHints renderingHint = new RenderingHints( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
			g.addRenderingHints( renderingHint );
			g.drawRenderedImage( img, at );
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ImageIO.write( dest, formatName, output );
			return new ImgUtil(dest, formatName);
		}
		
    /**
     *
     * @return
     * @throws IOException
     */
    public byte[] getBytes() throws IOException
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write( img, formatName, output );
		byte[] b = output.toByteArray();
		size = b.length;
		return b;	
	}

    /**
     *
     * @param path
     * @param fileName
     * @throws Exception
     */
    public void saveToFile(String path, String fileName) throws Exception
	{
		System.out.println("Store image to file: " + path + " " +  fileName);
//	  FileContent fc = new FileContent( getBytes() );
	  (new FileContent( getBytes() )).storeToDisk( path, fileName );
	}
}
