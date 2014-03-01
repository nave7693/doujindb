package org.dyndns.doujindb.plug.impl.mugimugi;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;

import org.dyndns.doujindb.util.ImageTool;

@SuppressWarnings("unchecked")
final class CacheManager
{
	private static Map<String, ImageSignature> cacheData;
	private static File cacheFile = new File(DoujinshiDBScanner.PLUGIN_HOME, "imagesignature.ser");
	
	static {
		cacheData = new TreeMap<String, ImageSignature>();
		read();
	}
	
	public static void write() {
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(cacheFile));
			oos.writeObject(cacheData);
			oos.flush();
			oos.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public static void read()
	{
		synchronized(cacheData)
		{
			try {
				ObjectInputStream ois = new ObjectInputStream(
						new FileInputStream(cacheFile));
				cacheData = (TreeMap<String, ImageSignature>) ois.readObject();
				ois.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
			}
			if(cacheData == null)
				cacheData = new TreeMap<String, ImageSignature>();
		}
	}
	
	public static long size() {
		return cacheData.size();
	}

	public static Set<String> keys() {
		return cacheData.keySet();
	}

	public static void put(String id, BufferedImage bi) {
		synchronized(cacheData)
		{
			cacheData.put(id, new ImageSignature(bi));
		}
	}
	
	public static void remove(String id) {
		synchronized(cacheData)
		{
			cacheData.remove(id);
		}
	}
	
	public static ImageSignature get(String id) {
		return cacheData.get(id);
	}

	public static boolean contains(String id) {
		return cacheData.containsKey(id);
	}
	
	public static String search(BufferedImage bi)
	{
		String result = null;
		double diff_min = 0;
		ImageSignature ims = new ImageSignature(
			ImageTool.getScaledInstance(bi, 256, 256, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true));
		
		for(String key : keys())
		{
			ImageSignature value = get(key);
			double diff = ims.diff(value);
			if(diff >= DoujinshiDBScanner.THRESHOLD)
				if(diff > diff_min)
				{
					diff_min = diff;
					result = key;
				}
		}
		return result;
	}
	
	public static TreeMap<Double, String> search(BufferedImage bi, int count)
	{
		TreeMap<Double, String> result = new TreeMap<Double, String>(new Comparator<Double>()
		{
			@Override
			public int compare(Double a, Double b)
			{
				return b.compareTo(a);
			}
		});
		int diff_min = 0;
		ImageSignature ims = new ImageSignature(
			ImageTool.getScaledInstance(bi, 256, 256, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true));
		
		for(String key : keys())
		{
			ImageSignature value = get(key);
			double diff = ims.diff(value);
			if(diff > diff_min)
			{
				if(result.size() >= count)
					result.remove(result.lastKey());
				result.put(diff, key);
			}
		}
		return result;
	}
	
	public static long timestamp() {
		return cacheFile.lastModified();
	}
	
	/**
	 * Internal use only
	 */
	@SuppressWarnings("unused")
	private static void dump() throws Exception
	{
		File dump_folder = new File(DoujinshiDBScanner.PLUGIN_HOME, "imagesignature");
		dump_folder.mkdirs();
		for(String key : cacheData.keySet())
		{
			ImageSignature ims = cacheData.get(key);
			File dump_entry = new File(dump_folder, key + ".png");
			BufferedImage bi = new BufferedImage(256, 256, BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D g2D = bi.createGraphics();
			for(int x=0;x<ims.pixelData.length;x++)
				for(int y=0;y<ims.pixelData[0].length;y++)
				{
					int r = (ims.pixelData[x][y] & 0x00ff0000) >> 16;
					int g = (ims.pixelData[x][y] & 0x0000ff00) >> 8;
					int b =  ims.pixelData[x][y] & 0x000000ff;
					g2D.setColor(new Color(r, g, b));
					g2D.fillRect(x * ims.pixelDensity, y * ims.pixelDensity, ims.pixelDensity, ims.pixelDensity);
				}
			ImageTool.write(bi, dump_entry);
		}
	}
	
	private static final class ImageSignature implements Serializable
	{
		private static final long serialVersionUID = 1L;
		
		private int pixelDensity= 16;
		private int[][] pixelData;
		
		private ImageSignature(BufferedImage bi)
		{
			pixelData = new int[bi.getWidth()/pixelDensity][bi.getHeight()/pixelDensity];
			for (int x = 0; x < pixelData.length; x++)
				for (int y = 0; y < pixelData[0].length; y++)
					pixelData[x][y] = colorAt(bi, x * pixelDensity, y * pixelDensity, pixelDensity, pixelDensity);
		}
		
		public double diff(ImageSignature ims)
		{
			double diff = 0;
			for (int x = 0; x < Math.min(pixelData.length, ims.pixelData.length); x++)
				for (int y = 0; y < Math.min(pixelData[0].length, ims.pixelData[0].length); y++)
				{
					int r1 = (pixelData[x][y] & 0x00ff0000) >> 16;
					int g1 = (pixelData[x][y] & 0x0000ff00) >> 8;
					int b1 =  pixelData[x][y] & 0x000000ff;
					int r2 = (ims.pixelData[x][y] & 0x00ff0000) >> 16;
					int g2 = (ims.pixelData[x][y] & 0x0000ff00) >> 8;
					int b2 =  ims.pixelData[x][y] & 0x000000ff;
					double _diff = Math.sqrt(
							Math.pow(r1 - r2, 2) +
							Math.pow(g1 - g2, 2) +
							Math.pow(b1 - b2, 2));
					diff += _diff;
				}
			long width = Math.min(pixelData.length, ims.pixelData.length);
			long height = Math.min(pixelData[0].length, ims.pixelData[0].length);
			return (100 - (diff * 100 / ((double)width * height * 0xff)));
		}
		
		private static int colorAt(BufferedImage bi, int x, int y, int width, int height)
		{
			long r=0, g=0, b=0;
			long length = 0;
			for(int k=0;k<width;k++)
				for(int i=0;i<height;i++)
				{
					int color = bi.getRGB(x+i, y+k);
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
	}
}