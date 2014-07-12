package org.dyndns.doujindb.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.basic.*;

@SuppressWarnings("serial")
public abstract class DialogEx extends JInternalFrame
{
	protected DialogEx(Icon icon, String title)
	{
		super();
		super.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		super.setFrameIcon(icon);
		super.setTitle(title);
		super.setIconifiable(false);
		super.setClosable(true);
		super.setMaximizable(false);
		((BasicInternalFrameUI) super.getUI()).setNorthPane(null);
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
		super.setLayout(new GridBagLayout());
		GridBagConstraints gbc;
		JLabel labelTitle = new JLabel();
		labelTitle.setIcon(icon);
		labelTitle.setText(title);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(2,2,2,2);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		super.add(labelTitle, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		super.add(createComponent(), gbc);
	}
	
	public abstract JComponent createComponent();
}
