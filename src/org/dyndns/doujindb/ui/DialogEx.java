package org.dyndns.doujindb.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

@SuppressWarnings("serial")
public final class DialogEx extends JInternalFrame implements LayoutManager
{
	private JComponent root = new JPanel();
	
	DialogEx(JComponent comp, Icon icon, String title)
	{
		super("", true, true, true, true);
		setLayout(this);
		((BasicInternalFrameUI)getUI()).setNorthPane(null);
		getDesktopIcon().setUI(new BasicDesktopIconUI()
		{
			protected void installComponents()
			{
				super.installComponents();
			}
			@Override public Dimension getPreferredSize(JComponent c)
			{
				return new Dimension(145,25);
			}
			protected void uninstallComponents()
			{
				super.uninstallComponents();
			}        
		});
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		setFrameIcon(icon);
		setTitle(title);
		root = comp;
		setLayout(new GridLayout(1,1));
		add(root);
		super.setVisible(true);
	}
	
	@Override
	public void layoutContainer(Container parent)
	{
		int width = parent.getWidth(),
			height = parent.getHeight();
		root.setBounds(0, 0, width, height);
	}
	
	@Override
	public void addLayoutComponent(String key,Component c) {}
	
	@Override
	public void removeLayoutComponent(Component c) {}
	
	@Override
	public Dimension minimumLayoutSize(Container parent)
	{
		return getMinimumSize();
	}
	
	@Override
	public Dimension preferredLayoutSize(Container parent)
	{
		return getPreferredSize();
	}
}