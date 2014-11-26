package org.dyndns.doujindb.plug.impl.imagesearch;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Average (mean) hash algorithm implementation in Java
 * @author: Elliot Shepherd
 * @see http://www.hackerfactor.com/blog/?/archives/432-Looks-Like-It.html
 */
public class ImageAHash
{
	private int size = 8;
	
	public ImageAHash() { }
	
	public ImageAHash(int size) {
		this.size = size;
	}
	
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
		 * Like Average Hash, pHash starts with a small image.
		 * However, the image is larger than 8x8; 32x32 is a good size.
		 * This is really done to simplify the DCT computation and not
		 * because it is needed to reduce the high frequencies.
		 */
		img = resize(img, size, size);
		
		/* 2. Reduce color.
		 * The image is reduced to a grayscale just to further simplify
		 * the number of computations.
		 */
		img = grayscale(img);
		
		double[][] vals = new double[size][size];
		
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				 vals[x][y] = getBlue(img, x, y);
			}
		}
		
		/* 3. Compute the average value.
		 * Like the Average Hash, compute the mean DCT value (using only
		 * the 8x8 DCT low-frequency values and excluding the first term
		 * since the DC coefficient can be significantly different from
		 * the other values and will throw off the average).
		 */
		double total = 0;
		
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				total += vals[x][y];
			}
		}
		total -= vals[0][0];
		
		double avg = total / (double) ((size * size) - 1);
		
		/* 4. Compute the bits. This is the fun part.
		 *    Each bit is simply set based on whether the color value is above or below the mean.
		 * 5. Construct the hash. Set the 64 bits into a 64-bit integer.
		 *    The order does not matter, just as long as you are consistent.
		 */
		String hash = "";
		
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				if (x != 0 && y != 0) {
					hash += (vals[x][y] > avg?"1":"0");
				}
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
