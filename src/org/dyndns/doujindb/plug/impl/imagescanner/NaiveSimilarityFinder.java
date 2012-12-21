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
	private Color[][] sig;
	private int pps; // Pixel per Square
	
	private NaiveSimilarityFinder() { }
	
	private NaiveSimilarityFinder(BufferedImage bi, int pps)
	{
		if(pps < 1 || pps > 15)
			throw new IllegalArgumentException("Pixel-Per-Square parameter must be 1 <= pps <= 15.");
		this.pps = pps;
		sig = signature(bi);
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
	
	public Image getSignature()
	{
		Image image = new BufferedImage(sig.length * pps, sig[0].length * pps, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		for(int i=0;i<sig.length;i++)
			for(int k=0;k<sig[0].length;k++)
			{
				g.setColor(sig[i][k]);
				g.fillRect(i * pps, k * pps, pps, pps);
				//g.setColor(Color.black);
				//g.drawLine(i * pps, 0, i * pps, sig[0].length * pps);
				//g.drawLine(0, k * pps, sig.length * pps, k * pps);
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
	
	private Color[][] signature(BufferedImage bi)
	{
		Color[][] sig = new Color[bi.getWidth()/pps][bi.getHeight()/pps];
		for (int x = 0; x < sig.length; x++)
			for (int y = 0; y < sig[0].length; y++)
				sig[x][y] = getAverageColor(bi, x * pps, y * pps, pps, pps);
		return sig;
    }
	
	@SuppressWarnings("unused")
	private static Color getAverageColor(BufferedImage bi)
	{
		long r=0, g=0, b=0;
		long length = 0;
		for(int k=1;k<bi.getWidth()-2;k++)
			for(int i=1;i<bi.getHeight()-2;i++)
			{
				Color color = getPixelColor(bi, i, k);
				r += color.getRed();
				g += color.getGreen();
				b += color.getBlue();
				length++;
			}
		r = ((int)r/length);
		g = ((int)g/length);
		b = ((int)b/length);
		return new Color((int)r, (int)g, (int)b);
	}
	
	private static Color getAverageColor(BufferedImage bi, int x, int y, int width, int height)
	{
		long r=0, g=0, b=0;
		long length = 0;
		for(int k=0;k<width;k++)
			for(int i=0;i<height;i++)
			{
				Color color = getPixelColor(bi, x+i, y+k);
				r += color.getRed();
				g += color.getGreen();
				b += color.getBlue();
				length++;
			}
		r = ((int)r/length);
		g = ((int)g/length);
		b = ((int)b/length);
		return new Color((int)r, (int)g, (int)b);
	}
	
	private static Color getPixelColor(BufferedImage bi, int x, int y)
	{
		int c = bi.getRGB(x, y);
		int red = (c & 0x00ff0000) >> 16;
		int green = (c & 0x0000ff00) >> 8;
		int blue = c & 0x000000ff;
		return new Color(red, green, blue);
	}
	
	private long distance(BufferedImage bi)
	{
		Color[][] sigO = signature(bi);
		long dist = 0;
		for (int x = 0; x < Math.min(sig.length, sigO.length); x++)
			for (int y = 0; y < Math.min(sig[0].length, sigO[0].length); y++)
			{
				int r1 = sig[x][y].getRed();
				int g1 = sig[x][y].getGreen();
				int b1 = sig[x][y].getBlue();
				int r2 = sigO[x][y].getRed();
				int g2 = sigO[x][y].getGreen();
				int b2 = sigO[x][y].getBlue();
				double tempDist = Math.sqrt((r1 - r2) * (r1 - r2) + (g1 - g2)
						* (g1 - g2) + (b1 - b2) * (b1 - b2));
				dist += tempDist;
			}
		return dist;
    }
}