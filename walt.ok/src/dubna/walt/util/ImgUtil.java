package dubna.walt.util;

import java.awt.Graphics2D;
	import java.awt.RenderingHints;
	import java.awt.geom.AffineTransform;
	import java.awt.image.BufferedImage;
	import java.io.ByteArrayInputStream;
	import java.io.ByteArrayOutputStream;

import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author serg
 */
public class ImgUtil
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
    public ImgUtil(byte[] src, String formatName) throws IOException
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
    public ImgUtil(BufferedImage img, String formatName) throws IOException
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

	/**
			 * This method takes in an image as a byte array (currently supports GIF, JPG, PNG and possibly other formats) and
			 * resizes it to have a width no greater than the pMaxWidth parameter in pixels. It converts the image to a standard
			 * quality JPG and returns the byte array of that JPG image.
			 *
			 * @param pImageData
			 *                the image data.
			 * @param pMaxWidth
			 *                the max width in pixels, 0 means do not scale.
			 * @return the resized JPG image.
			 * @throws IOException
			 *                 if the iamge could not be manipulated correctly.
			 */
	public byte[] resizeImageAsJPG ( byte[] pImageData, int pMaxWidth ) throws IOException
	{
		// Create an ImageIcon from the image data
		ImageIcon imageIcon = new ImageIcon( pImageData );
		int width = imageIcon.getIconWidth();
		int height = imageIcon.getIconHeight();
		System.out.println( "imageIcon width: " + +width + ";  height: " + height );
		// If the image is larger than the max width, we need to resize it
		if( pMaxWidth > 0 && width > pMaxWidth )
		{
			// Determine the shrink ratio
			double ratio = ( double ) pMaxWidth / imageIcon.getIconWidth();
			System.out.println( "resize ratio: " + ratio );
			height = ( int ) ( imageIcon.getIconHeight() * ratio );
			width = pMaxWidth;
			System.out.println( "imageIcon post scale width: " + width + "; height: " + height );
		}
		// Create a new empty image buffer to "draw" the resized image into
		BufferedImage bufferedResizedImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
		// Create a Graphics object to do the "drawing"
		Graphics2D g2d = bufferedResizedImage.createGraphics();
		g2d.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
		// Draw the resized image
		g2d.drawImage( imageIcon.getImage(), 0, 0, width, height, null );
		g2d.dispose();
		// Now our buffered image is ready
		// Encode it as a JPEG
		ByteArrayOutputStream encoderOutputStream = new ByteArrayOutputStream();
                ImageIO.write(bufferedResizedImage, "JPG", encoderOutputStream);
		byte[] resizedImageByteArray = encoderOutputStream.toByteArray();
		return resizedImageByteArray;
	}

}
