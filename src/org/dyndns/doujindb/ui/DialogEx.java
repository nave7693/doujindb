package org.dyndns.doujindb.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

@SuppressWarnings("serial")
public abstract class DialogEx extends JInternalFrame
{
	protected DialogEx(Icon icon, String title)
	{
		super("", true, true, true, true);
		((BasicInternalFrameUI)super.getUI()).setNorthPane(null);
		super.getDesktopIcon().setUI(new BasicDesktopIconUI()
		{
			protected void installComponents()
			{
				super.installComponents();
			}
			@Override public Dimension getPreferredSize(JComponent c)
			{
				return new Dimension(145, 25);
			}
			protected void uninstallComponents()
			{
				super.uninstallComponents();
			}        
		});
		super.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		super.setFrameIcon(icon);
		super.setTitle(title);
		super.setLayout(new GridLayout(1,1));
		super.add(createComponent());
	}
	
	public abstract JComponent createComponent();
}
