package org.dyndns.doujindb.ui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.*;

import org.dyndns.doujindb.Main;
import org.dyndns.doujindb.ui.*;

@SuppressWarnings("serial")
public final class DialogAbout extends JPanel
{
	private JLabel fLabelAboutImage;
	private JLabel fLabelName;
	private JLabel fLabelSpecVersion;
	private JLabel fLabelImplName;
	private JLabel fLabelImplVersion;
	private JLabel fLabelImplVendor;
	private JLabel fLabelWebsite;
	private JButton fButtonClose;
	
	public static final Icons Icon = UI.Icon;
	public static final Font Font = UI.Font;
	
	public DialogAbout()
	{
		super.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		
		fLabelAboutImage = new JLabel(Icon.window_dialog_about);
		gbc.anchor = GridBagConstraints.PAGE_START;
		gbc.insets = new Insets(5, 5, 5, 5);
		super.add(fLabelAboutImage, gbc);
		
		fLabelName = new JLabel(Main.class.getPackage().getSpecificationTitle());
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(1, 5, 1, 5);
		super.add(fLabelName, gbc);
		
		fLabelSpecVersion = new JLabel("version : "+Main.class.getPackage().getSpecificationVersion());
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(1, 5, 1, 5);
		super.add(fLabelSpecVersion, gbc);
		
		fLabelImplName = new JLabel("codename : " + Main.class.getPackage().getImplementationTitle());
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(1, 5, 1, 5);
		super.add(fLabelImplName, gbc);
		
		fLabelImplVersion = new JLabel("buildnum : " + Main.class.getPackage().getImplementationVersion());
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(1, 5, 1, 5);
		super.add(fLabelImplVersion, gbc);
		
		fLabelImplVendor = new JLabel("copyright : " + Main.class.getPackage().getImplementationVendor());
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(1, 5, 1, 5);
		super.add(fLabelImplVendor, gbc);
		
		fLabelWebsite = new JLabel("<html><body><a href='https://github.com/loli10K/doujindb'>https://github.com/loli10K/doujindb</a></body></html>");
		fLabelWebsite.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent me) {
				try {
					java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
					desktop.browse(new URI("https://github.com/loli10K/doujindb"));
				} catch (IOException | URISyntaxException e) { }
			}
		});
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(1, 5, 1, 5);
		super.add(fLabelWebsite, gbc);
		
		fButtonClose = new JButton("Ok");
		fButtonClose.setFont(Font);
		fButtonClose.setMnemonic('O');
		fButtonClose.setFocusable(false);
		fButtonClose.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae) 
			{
				DialogEx window = (DialogEx)((JComponent)ae.getSource()).getRootPane().getParent();
				window.dispose();
			}
		});
		gbc.anchor = GridBagConstraints.PAGE_END;
		gbc.insets = new Insets(5, 5, 5, 5);
		super.add(fButtonClose, gbc);
	}
}
