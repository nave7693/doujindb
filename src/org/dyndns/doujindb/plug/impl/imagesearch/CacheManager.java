package org.dyndns.doujindb.plug.impl.imagesearch;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;

import org.dyndns.doujindb.util.ImageTool;

@SuppressWarnings("unchecked")
public final class CacheManager
{
	private static Map<String, ImageSignature> fCacheData;
	private static File fCacheFile = ImageSearch.PLUGIN_IMAGEINDEX;
	
	static
	{
		fCacheData = new TreeMap<String, ImageSignature>();
		read();
	}
	
	public static void write() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(fCacheFile));
			oos.writeObject(fCacheData);
			oos.flush();
			oos.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public static void read() {
		synchronized(fCacheData) {
			try {
				ObjectInputStream ois = new ObjectInputStream(
						new FileInputStream(fCacheFile));
				fCacheData = (TreeMap<String, ImageSignature>) ois.readObject();
				ois.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
			}
			if(fCacheData == null)
				fCacheData = new TreeMap<String, ImageSignature>();
		}
	}
	
	public static long size() {
		return fCacheData.size();
	}

	public static Set<String> keys() {
		return fCacheData.keySet();
	}

	public static void put(String id, BufferedImage bi) {
		synchronized(fCacheData) {
			fCacheData.put(id, new ImageSignature(bi));
		}
	}
	
	public static void remove(String id) {
		synchronized(fCacheData) {
			fCacheData.remove(id);
		}
	}
	
	public static ImageSignature get(String id) {
		return fCacheData.get(id);
	}

	public static boolean contains(String id) {
		return fCacheData.containsKey(id);
	}
	
	public static String search(BufferedImage bi) {
		return search(bi, 1).firstEntry().getValue();
	}
	
	public static TreeMap<Double, String> search(BufferedImage bi, int count) {
		TreeMap<Double, String> result = new TreeMap<Double, String>(new Comparator<Double>() {
			@Override
			public int compare(Double a, Double b)
			{
				return b.compareTo(a);
			}
		});

		ImageSignature ims = new ImageSignature(
			ImageTool.getScaledInstance(bi, 256, 256, true));
		
		for(String key : keys()) {
			ImageSignature value = get(key);
			double diff = ims.diff(value);
			if(diff > ImageSearch.fThreshold) {
				if(result.size() >= count)
				{
					result.put(diff, key);
					result.remove(result.lastKey());
				} else
					result.put(diff, key);
			}
		}
		return result;
	}
	
	public static long timestamp() {
		return fCacheFile.lastModified();
	}
	
	public static void dump() {
		File folder = new File(ImageSearch.PLUGIN_HOME, "imageindex");
		folder.mkdirs();
		for(String key : fCacheData.keySet()) {
			ImageSignature ims = fCacheData.get(key);
			File file = new File(folder, key + ".png");
			try {
				ImageTool.write(ims.toImage(), file);
			} catch (IOException ioe) { }
		}
	}
	
	private static final class ImageSignature implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private final int fDensity = ImageSearch.fImageScaling;
		private int[][] fData;
		
		private ImageSignature(BufferedImage bi) {
			fData = new int[bi.getWidth()/fDensity][bi.getHeight()/fDensity];
			for (int x = 0; x < fData.length; x++)
				for (int y = 0; y < fData[0].length; y++)
					fData[x][y] = colorAt(bi, x * fDensity, y * fDensity, fDensity, fDensity);
		}
		
		public double diff(ImageSignature ims) {
			double diff = 0;
			for (int x = 0; x < Math.min(fData.length, ims.fData.length); x++)
				for (int y = 0; y < Math.min(fData[0].length, ims.fData[0].length); y++) {
					int r1 = (fData[x][y] & 0x00ff0000) >> 16;
					int g1 = (fData[x][y] & 0x0000ff00) >> 8;
					int b1 =  fData[x][y] & 0x000000ff;
					int r2 = (ims.fData[x][y] & 0x00ff0000) >> 16;
					int g2 = (ims.fData[x][y] & 0x0000ff00) >> 8;
					int b2 =  ims.fData[x][y] & 0x000000ff;
					double _diff = Math.sqrt(
							Math.pow(r1 - r2, 2) +
							Math.pow(g1 - g2, 2) +
							Math.pow(b1 - b2, 2));
					diff += _diff;
				}
			long width = Math.min(fData.length, ims.fData.length);
			long height = Math.min(fData[0].length, ims.fData[0].length);
			return (100 - (diff * 100 / ((double)width * height * 0xff)));
		}
		
		public BufferedImage toImage() {
			BufferedImage bi = new BufferedImage(fData.length * fDensity, fData[0].length * fDensity, BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D g2D = bi.createGraphics();
			for(int x=0; x < fData.length; x++)
				for(int y=0; y < fData[0].length; y++) {
					int r = (fData[x][y] & 0x00ff0000) >> 16;
					int g = (fData[x][y] & 0x0000ff00) >> 8;
					int b =  fData[x][y] & 0x000000ff;
					g2D.setColor(new Color(r, g, b));
					g2D.fillRect(x * fDensity, y * fDensity, fDensity, fDensity);
				}
			return bi;
		}
		
		private static int colorAt(BufferedImage bi, int x, int y, int width, int height) {
			long r=0, g=0, b=0;
			long length = 0;
			for(int k=0;k<width;k++)
				for(int i=0;i<height;i++) {
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
