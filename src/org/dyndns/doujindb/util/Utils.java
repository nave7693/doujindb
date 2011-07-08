package org.dyndns.doujindb.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.Set;


/**  
* Utils.java - DoujinDB utilities/hacks.
* @author  nozomu
* @version 1.0
*/
public final class Utils
{

	public void addClassPath(String s) throws IOException
	{
		File f = new File(s);
		addClassPath(f);
	}
	
	public void addClassPath(File f) throws IOException
	{
		addClassPath(f.toURI().toURL());
	}

	public void addClassPath(URL url) throws IOException
	{
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class<?> sysclass = URLClassLoader.class;
		try
		{
			Method method = sysclass.getDeclaredMethod("addURL", new Class[]{ URL.class });
			method.setAccessible(true);
			method.invoke(sysloader, new Object[]{ url });
		} catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	/**
	 * Many thanks to Chris Campbell for this wonderful method
	 * @see http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
	 */
    /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    public BufferedImage getScaledInstance(BufferedImage img,
                                           int targetWidth,
                                           int targetHeight,
                                           Object hint,
                                           boolean higherQuality)
    {
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage)img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }
        
        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }
    
    public String generateUUID(Set<String> uuids)
    {
    	String uuid;
		generate_uuid:
		{
			while(true)
			{
				uuid = "{" + java.util.UUID.randomUUID().toString() + "}";
				if(!uuids.contains(uuid))
					break generate_uuid;
			}
		}
    	return uuid;
    }
}
