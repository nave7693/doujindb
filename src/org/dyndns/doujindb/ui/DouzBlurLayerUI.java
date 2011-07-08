package org.dyndns.doujindb.ui;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.plaf.*;

@SuppressWarnings("serial")
final class DouzBlurLayerUI extends LayerUI<JComponent>
{
	private BufferedImage mOffscreenImage;
	private BufferedImageOp mOperation;
	private boolean uiActive = false;
	
	public DouzBlurLayerUI()
	{
		float ninth = 1.0f / 9.0f;
	    float[] blurKernel = {
	    		ninth, ninth, ninth,
	    		ninth, ninth, ninth,
	    		ninth, ninth, ninth
	    	};
	    mOperation = new ConvolveOp(
	    		new Kernel(3, 3, blurKernel),
		    	ConvolveOp.EDGE_NO_OP, null);
	}

	public boolean getActive()
	{
		return uiActive;
	}
	
	public void setActive(boolean active)
	{
		uiActive = active;
	}
	
	@Override
	public void paint (Graphics g, JComponent c)
	{
		if(!uiActive)
		{
			super.paint(g, c);
			return;
		}
		
		int w = c.getWidth();
	    int h = c.getHeight();
	    
		if (w == 0 || h == 0)
	      return;

		if (mOffscreenImage == null ||
				mOffscreenImage.getWidth() != w ||
	            mOffscreenImage.getHeight() != h)
			mOffscreenImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		
		Graphics2D ig2 = mOffscreenImage.createGraphics();
	    ig2.setClip(g.getClip());
	    super.paint(ig2, c);
	    ig2.dispose();
		
	    Graphics2D g2 = (Graphics2D)g;
	    g2.drawImage(mOffscreenImage, mOperation, 0, 0);
	}
}
