package org.dyndns.doujindb.plug.impl.imagescanner;

/*
 * Part of the Java Image Processing Cookbook, please see
 * http://www.lac.inpe.br/~rafael.santos/JIPCookbook.jsp
 * for information on usage and distribution.
 * Rafael Santos (rafael.santos@lac.inpe.br)
 */

import java.awt.*;
import java.awt.image.*;
import java.util.*;

public class NaiveSimilarityFinder
{
	private int[][] signature;
	private int pps; // Pixel per Square
	
	private NaiveSimilarityFinder() { }
	
	private NaiveSimilarityFinder(BufferedImage bi, int pps)
	{
		if(pps < 1 || pps > 15)
			throw new IllegalArgumentException("Pixel-Per-Square parameter must be 1 <= pps <= 15.");
		this.pps = pps;
		signature = signature(bi);
	}
	
	private NaiveSimilarityFinder(BufferedImage bi)
	{
		this(bi, 9);
	}
	
	public static NaiveSimilarityFinder getInstance(BufferedImage bi)
	{
		return new NaiveSimilarityFinder(bi);
	}
	
	public static NaiveSimilarityFinder getInstance(BufferedImage bi, int pps)
	{
		return new NaiveSimilarityFinder(bi, pps);
	}
	
	int[][] getSignature()
	{
		return signature;
	}
	
	public static int[][] getSignature(BufferedImage bi, int pps)
	{
		if(pps < 1 || pps > 15)
			throw new IllegalArgumentException("Pixel-Per-Square parameter must be 1 <= pps <= 15.");
		return signature(bi, pps);
	}
	
	public static Image getImage(int[][] sig, int pps)
	{
		Image image = new BufferedImage(sig.length * pps, sig[0].length * pps, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		for(int i=0;i<sig.length;i++)
			for(int k=0;k<sig[0].length;k++)
			{
				Color color = new Color(
					(sig[i][k] & 0x00ff0000) >> 16,	// RED
					(sig[i][k] & 0x0000ff00) >> 8,	// GREEN
					 sig[i][k] & 0x000000ff,		// BLUE
					 255);							// ALPHA
				g.setColor(color);
				g.fillRect(i * pps, k * pps, pps, pps);
				// Border lines
//				g.setColor(Color.black);
//				g.drawLine(i * pps, 0, i * pps, sig[0].length * pps);
//				g.drawLine(0, k * pps, sig.length * pps, k * pps);
			}
		return image;
	}
	
	public Image getImage()
	{
		Image image = new BufferedImage(signature.length * pps, signature[0].length * pps, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		for(int i=0;i<signature.length;i++)
			for(int k=0;k<signature[0].length;k++)
			{
				Color color = new Color(
					(signature[i][k] & 0x00ff0000) >> 16,	// RED
					(signature[i][k] & 0x0000ff00) >> 8,	// GREEN
					signature[i][k] & 0x000000ff,			// BLUE
					 255);									// ALPHA
				g.setColor(color);
				g.fillRect(i * pps, k * pps, pps, pps);
				// Border lines
//				g.setColor(Color.black);
//				g.drawLine(i * pps, 0, i * pps, signature[0].length * pps);
//				g.drawLine(0, k * pps, signature.length * pps, k * pps);
			}
		return image;
	}

	public Image getSimilarity(Iterable<BufferedImage> bis)
	{
		BufferedImage result = null;
		long similarity = 0;
		long similarity_ = 0;
		for(BufferedImage bi : bis)
			if(similarity > (similarity_ = distance(bi)))
			{
				similarity = similarity_;
				result = bi;
			} else
				if(result == null)
				{
					similarity = similarity_;
					result = bi;
				}
		return result;
	}
	
	public Iterable<BufferedImage> getSimilarity(Iterable<BufferedImage> bis, int limit)
	{
		TreeMap<Long,BufferedImage> result = new TreeMap<Long,BufferedImage>();
		for(BufferedImage bi : bis)
		{
			long similarity = distance(bi);
			if(result.size() >= 10)
			{
				result.put(similarity, bi);
				result.remove(result.lastKey());
			} else {
				result.put(similarity, bi);
			}
		}
		return result.values();
	}
	
	public long getSimilarity(BufferedImage bi)
	{
		return distance(bi);
	}
	
	public long getSimilarity(int[][] sig)
	{
		return distance(sig);
	}
	
	public double getPercentSimilarity(BufferedImage bi)
	{
		return distance_percent(bi);
	}
	
	public double getPercentSimilarity(int[][] sig)
	{
		return distance_percent(sig);
	}
	
	private int[][] signature(BufferedImage bi)
	{
		int[][] signature_ = new int[bi.getWidth()/pps][bi.getHeight()/pps];
		for (int x = 0; x < signature_.length; x++)
			for (int y = 0; y < signature_[0].length; y++)
				signature_[x][y] = color(bi, x * pps, y * pps, pps, pps);
		return signature_;
    }
	
	private static int[][] signature(BufferedImage bi, int pps)
	{
		int[][] signature_ = new int[bi.getWidth()/pps][bi.getHeight()/pps];
		for (int x = 0; x < signature_.length; x++)
			for (int y = 0; y < signature_[0].length; y++)
				signature_[x][y] = color(bi, x * pps, y * pps, pps, pps);
		return signature_;
    }
	
	private static int color(BufferedImage bi, int x, int y, int width, int height)
	{
		long r=0, g=0, b=0;
		long length = 0;
		for(int k=0;k<width;k++)
			for(int i=0;i<height;i++)
			{
				int color = pixel(bi, x+i, y+k);
				r += (color & 0x00ff0000) >> 16;
				g += (color & 0x0000ff00) >> 8;
				b +=  color & 0x000000ff;
				length++;
			}
		r = (r/length);
		g = (g/length);
		b = (b/length);
		return new Color((int)r, (int)g, (int)b).getRGB();
	}
	
	private static int pixel(BufferedImage bi, int x, int y)
	{
		return bi.getRGB(x, y);
	}
	
	private long distance(int[][] sigO)
	{
		long dist = 0;
		for (int x = 0; x < Math.min(signature.length, sigO.length); x++)
			for (int y = 0; y < Math.min(signature[0].length, sigO[0].length); y++)
			{
				int r1 = (signature[x][y] & 0x00ff0000) >> 16;
				int g1 = (signature[x][y] & 0x0000ff00) >> 8;
				int b1 =  signature[x][y] & 0x000000ff;
				int r2 = (sigO[x][y] & 0x00ff0000) >> 16;
				int g2 = (sigO[x][y] & 0x0000ff00) >> 8;
				int b2 =  sigO[x][y] & 0x000000ff;
				double tempDist = Math.sqrt((r1 - r2) * (r1 - r2) + (g1 - g2)
						* (g1 - g2) + (b1 - b2) * (b1 - b2));
				dist += tempDist;
			}
		return dist;
    }
	
	private long distance(BufferedImage bi)
	{
		return distance(signature(bi));
    }
	
	private double distance_percent(int[][] sigO)
	{
		long dist = 0;
		long width = Math.min(signature.length, sigO.length);
		long height = Math.min(signature[0].length, sigO[0].length);
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
			{
				int r1 = (signature[x][y] & 0x00ff0000) >> 16;
				int g1 = (signature[x][y] & 0x0000ff00) >> 8;
				int b1 =  signature[x][y] & 0x000000ff;
				int r2 = (sigO[x][y] & 0x00ff0000) >> 16;
				int g2 = (sigO[x][y] & 0x0000ff00) >> 8;
				int b2 =  sigO[x][y] & 0x000000ff;
				double tempDist = Math.sqrt((r1 - r2) * (r1 - r2) + (g1 - g2)
						* (g1 - g2) + (b1 - b2) * (b1 - b2));
				dist += tempDist;
			}
		return dist * 100 / (width * height * 255 * 255 * 255);
    }
	
	private double distance_percent(BufferedImage bi)
	{
		return distance_percent(signature(bi));
    }
}