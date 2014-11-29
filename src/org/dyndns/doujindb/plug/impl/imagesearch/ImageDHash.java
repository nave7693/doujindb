package org.dyndns.doujindb.plug.impl.imagesearch;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * Difference hash algorithm implementation in Java
 * @author: Elliot Shepherd
 * @see http://www.hackerfactor.com/blog/?/archives/529-Kind-of-Like-That.html
 */
public class ImageDHash
{
	private int size = 8;
	
	public ImageDHash() { }
	
	public int distance(String s1, String s2) {
		int counter = 0;
		for (int k = 0; k < s1.length();k++) {
			if(s1.charAt(k) != s2.charAt(k)) {
				counter++;
			}
		}
		return counter;
	}
	
	// Returns a 'binary string' (like. 001010111011100010) which is easy to do a hamming distance on.
	public String getHash(InputStream is) throws Exception {
		BufferedImage img = ImageIO.read(is);
		
		/* 1. Reduce size.
		 * The fastest way to remove high frequencies and detail is to shrink the image.
		 * In this case, shrink it to 9x8 so that there are 72 total pixels.
		 * By ignoring the size and aspect ratio, this hash will match any similar picture regardless of how it is stretched.
		 */
		img = resize(img, size + 1, size);
		
		/* 2. Reduce color.
		 * Convert the image to a grayscale picture.
		 * This changes the hash from 72 pixels to a total of 72 colors.
		 */
		img = grayscale(img);
		
		double[][] vals = new double[img.getWidth()][img.getHeight()];
		
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				 vals[x][y] = getBlue(img, x, y);
			}
		}
		
		/* 3. Compute the difference.
		 * The dHash algorithm works on the difference between adjacent pixels.
		 * This identifies the relative gradient direction.
		 * In this case, the 9 pixels per row yields 8 differences between adjacent pixels.
		 * Eight rows of eight differences becomes 64 bits.
		 * 4. Assign bits.
		 * Each bit is simply set based on whether the left pixel is brighter than the right pixel.
		 * The order does not matter, just as long as you are consistent.
		 */
		String hash = "";
		
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				hash += (vals[x][y] < vals[x + 1][y] ? "1" : "0");
			}
		}
		
		return hash;
	}
	
	private BufferedImage resize(BufferedImage image, int width, int height) {
		BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	}
	
	private ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
	
	private BufferedImage grayscale(BufferedImage img) {
		colorConvert.filter(img, img);
		return img;
	}
	
	private static int getBlue(BufferedImage img, int x, int y) {
		return (img.getRGB(x, y)) & 0xff;
	}
}
