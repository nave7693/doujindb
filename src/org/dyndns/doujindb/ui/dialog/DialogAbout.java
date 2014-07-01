package org.dyndns.doujindb.ui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.util.jar.*;
import java.util.jar.Attributes.*;

import org.dyndns.doujindb.ui.*;

@SuppressWarnings("serial")
public final class DialogAbout extends JPanel
{
	private JLabel fLabelAboutImage;
	private JLabel fLabelName;
	private JLabel fLabelSpecVersion;
	private JLabel fLabelImplName;
	private JLabel fLabelImplVersion;
	private JLabel fLabelBuildDate;
	private JLabel fLabelImplVendor;
	private JLabel fLabelImplURL;
	private JButton fButtonClose;
	
	private static String SPECIFICATION_NAME = "";
	private static String SPECIFICATION_VERSION = "";
	private static String IMPLEMENTATION_NAME = "";
	private static String IMPLEMENTATION_VERSION = "";
	private static String BUILD_DATE = "";
	private static String IMPLEMENTATION_VENDOR = "";
	private static String IMPLEMENTATION_URL = "";
	
	public static final Icons Icon = UI.Icon;
	public static final Font Font = UI.Font;
	
	static
	{
		JarInputStream jis = null;
		
		try {
			URL location = org.dyndns.doujindb.Main.class.getProtectionDomain().getCodeSource().getLocation();
			if (location == null)
				throw new Exception("Source not found for main class");

			if (!location.toExternalForm().endsWith(".jar"))
				throw new Exception("Source is not contained in a .jar file");

			File file = new File(location.getPath());
			if (!file.exists())
				throw new Exception("File " + file.getPath() + " was not found");

			jis = new JarInputStream(location.openStream());
			Manifest manifest = jis.getManifest();

			if (manifest == null)
				throw new Exception("Could not read manifest");

			Attributes attr = manifest.getMainAttributes();
			SPECIFICATION_NAME = attr.getValue(Name.SPECIFICATION_TITLE);
			SPECIFICATION_VERSION = attr.getValue(Name.SPECIFICATION_VERSION);
			IMPLEMENTATION_NAME = attr.getValue(Name.IMPLEMENTATION_TITLE);
			IMPLEMENTATION_VERSION = attr.getValue(Name.IMPLEMENTATION_VERSION);
			BUILD_DATE = attr.getValue("Build-Date");
			IMPLEMENTATION_VENDOR = attr.getValue(Name.IMPLEMENTATION_VENDOR);
			IMPLEMENTATION_URL = attr.getValue(Name.IMPLEMENTATION_URL);
		} catch (Exception e) { } finally {
			try { jis.close(); } catch (Exception e) { }
		}
	}
	
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
		
		fLabelName = new JLabel(SPECIFICATION_NAME);
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(1, 5, 1, 5);
		super.add(fLabelName, gbc);
		
		fLabelSpecVersion = new JLabel("version : " + SPECIFICATION_VERSION);
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(1, 5, 1, 5);
		super.add(fLabelSpecVersion, gbc);
		
		fLabelImplName = new JLabel("codename : " + IMPLEMENTATION_NAME);
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(1, 5, 1, 5);
		super.add(fLabelImplName, gbc);
		
		fLabelImplVersion = new JLabel("build-num : " + IMPLEMENTATION_VERSION);
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(1, 5, 1, 5);
		super.add(fLabelImplVersion, gbc);
		
		fLabelBuildDate = new JLabel("build-date : " + BUILD_DATE);
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(1, 5, 1, 5);
		super.add(fLabelBuildDate, gbc);
		
		fLabelImplVendor = new JLabel("copyright : " + IMPLEMENTATION_VENDOR);
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(1, 5, 1, 5);
		super.add(fLabelImplVendor, gbc);
		
		fLabelImplURL = new JLabel("<html><body><a href='" + IMPLEMENTATION_URL + "'>" + IMPLEMENTATION_URL + "</a></body></html>");
		fLabelImplURL.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent me) {
				try {
					java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
					desktop.browse(new URI(IMPLEMENTATION_URL));
				} catch (IOException | URISyntaxException e) { }
			}
		});
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(1, 5, 1, 5);
		super.add(fLabelImplURL, gbc);
		
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
