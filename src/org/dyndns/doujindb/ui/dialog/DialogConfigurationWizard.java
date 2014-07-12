package org.dyndns.doujindb.ui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import javax.swing.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.ui.DialogEx;
import org.dyndns.doujindb.ui.Icons;
import org.dyndns.doujindb.ui.UI;

@SuppressWarnings({"serial","unused"})
public final class DialogConfigurationWizard  extends DialogEx implements LayoutManager
{
	private JPanel panel;
	
	private JLabel uiBottomDivisor;
	private JButton uiButtonNext;
	private JButton uiButtonBack;
	private JButton uiButtonFinish;
	private JButton uiButtonCanc;
	private JLabel uiLabelHeader;
	// STEP 1
	private Dialog uiCompWelcome;
	// STEP 2
	private Dialog uiCompDependency;
	// STEP 3
	private Dialog uiCompDatabase;
	// STEP 4
	private Dialog uiCompDatastore;
	// STEP 5
	private Dialog uiCompFinish;
	
	private static final Color foreground = (Color) Configuration.configRead("org.dyndns.doujindb.ui.theme.color");
	private static final Color background = (Color) Configuration.configRead("org.dyndns.doujindb.ui.theme.background");
	private static final Color linecolor = background.brighter();
	
	public static final Icons Icon = UI.Icon;
	public static final Font Font = UI.Font;
	
	enum Progress
	{
		WELCOME (1),
		DEPENDENCY (2),
		DATABASE (3),
		DATASTORE (4),
		FINISH (5);
		
		private final double value;
		
		Progress() {
			this(1);
		}
		Progress(int value) {
			this.value = value;
		}
	}
	
	private Progress fProgress = Progress.WELCOME;
	
	public DialogConfigurationWizard()
	{
		super(Icon.window_dialog_configwiz_icon, "Configuration Wizard");
		// reset progress
		doBack();
	}
	
	@Override
	public JComponent createComponent()
	{
		panel = new JPanel();
		uiLabelHeader = new JLabel(UI.Icon.window_dialog_configwiz_header);
		uiLabelHeader.setOpaque(true);
		uiLabelHeader.setBackground(Color.WHITE);
		panel.add(uiLabelHeader);
		panel.add(uiCompWelcome = new DialogWelcome());
		panel.add(uiCompDependency = new DialogDependency());
		panel.add(uiCompDatabase = new DialogDatabase());
		panel.add(uiCompDatastore = new DialogDatastore());
		panel.add(uiCompFinish = new DialogFinish());
		uiBottomDivisor = new JLabel();
		uiBottomDivisor.setOpaque(true);
		uiBottomDivisor.setBackground(background);
		panel.add(uiBottomDivisor);
		uiButtonNext = new JButton(UI.Icon.window_dialog_configwiz_next);
		uiButtonNext.setBorder(null);
		uiButtonNext.setFocusable(false);
		uiButtonNext.setText("Next");
		uiButtonNext.setToolTipText("Next");
		uiButtonNext.setMnemonic('N');
		uiButtonNext.setBorderPainted(true);
		uiButtonNext.setBorder(BorderFactory.createLineBorder(linecolor));
		uiButtonNext.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae) 
			{
				doNext();
			}					
		});
		panel.add(uiButtonNext);
		uiButtonBack = new JButton(UI.Icon.window_dialog_configwiz_prev);
		uiButtonBack.setEnabled(true);
		uiButtonBack.setBorder(null);
		uiButtonBack.setFocusable(false);
		uiButtonBack.setText("Back");
		uiButtonBack.setToolTipText("Back");
		uiButtonBack.setMnemonic('B');
		uiButtonBack.setBorderPainted(true);
		uiButtonBack.setBorder(BorderFactory.createLineBorder(linecolor));
		uiButtonBack.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae) 
			{
				doBack();
			}					
		});
		panel.add(uiButtonBack);
		uiButtonFinish = new JButton(UI.Icon.window_dialog_configwiz_finish);
		uiButtonFinish.setBorder(null);
		uiButtonFinish.setFocusable(false);
		uiButtonFinish.setText("Finish");
		uiButtonFinish.setToolTipText("Finish");
		uiButtonFinish.setMnemonic('F');
		uiButtonFinish.setBorderPainted(true);
		uiButtonFinish.setBorder(BorderFactory.createLineBorder(linecolor));
		uiButtonFinish.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae) 
			{
				Configuration.configSave();
				dispose();
			}					
		});
		panel.add(uiButtonFinish);
		uiButtonCanc = new JButton(UI.Icon.window_dialog_configwiz_cancel);
		uiButtonCanc.setBorder(null);
		uiButtonCanc.setFocusable(false);
		uiButtonCanc.setText("Cancel");
		uiButtonCanc.setToolTipText("Cancel");
		uiButtonCanc.setMnemonic('C');
		uiButtonCanc.setBorderPainted(true);
		uiButtonCanc.setBorder(BorderFactory.createLineBorder(linecolor));
		uiButtonCanc.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae) 
			{
				dispose();
			}					
		});
		panel.add(uiButtonCanc);
		panel.setLayout(this);
		
		return panel;
	}
	
	@Override
	public void addLayoutComponent(String key,Component c) { }
	
	@Override
	public void removeLayoutComponent(Component c) { }
	
	@Override
	public Dimension minimumLayoutSize(Container parent)
	{
		return new Dimension(300,250);
	}
	
	@Override
	public Dimension preferredLayoutSize(Container parent)
	{
		return new Dimension(300,250);
	}
	
	@Override
	public void layoutContainer(Container parent)
	{
		int width = parent.getWidth(),
			height = parent.getHeight();
		uiLabelHeader.setBounds(0,0,width,48);
		uiBottomDivisor.setBounds(5,height-30,width-10,1);
		uiButtonNext.setBounds(width-80,height-25,75,20);
		uiButtonFinish.setBounds(width-80,height-25,75,20);
		uiButtonBack.setBounds(width-160,height-25,75,20);
		uiButtonCanc.setBounds(5,height-25,75,20);
		uiCompWelcome.setBounds(0,0,0,0);
		uiCompDependency.setBounds(0,0,0,0);
		uiCompDatabase.setBounds(0,0,0,0);
		uiCompDatastore.setBounds(0,0,0,0);
		uiCompFinish.setBounds(0,0,0,0);
		switch(fProgress)
		{
		case WELCOME:
			uiCompWelcome.setBounds(5,50,width-10,height-85);
			break;
		case DEPENDENCY:
			uiCompDependency.setBounds(5,50,width-10,height-85);
			uiCompDependency.doLayout();
			break;
		case DATABASE:
			uiCompDatabase.setBounds(5,50,width-10,height-85);
			uiCompDatabase.doLayout();
			break;
		case DATASTORE:
			uiCompDatastore.setBounds(5,50,width-10,height-85);
			uiCompDatastore.doLayout();
			break;
		case FINISH:
			uiCompFinish.setBounds(5,50,width-10,height-85);
			break;
		}
	}
	private void doNext() throws RuntimeException
	{
		switch(fProgress)
		{
			case WELCOME:
				fProgress = Progress.DEPENDENCY;
				uiCompDependency.doDisplay();
				uiButtonBack.setVisible(true);
				uiButtonNext.setVisible(true);
				uiButtonNext.setEnabled(false);
				uiButtonFinish.setVisible(false);
				break;
			case DEPENDENCY:
				fProgress = Progress.DATABASE;
				uiCompDatabase.doDisplay();
				uiButtonBack.setVisible(true);
				uiButtonNext.setVisible(true);
				uiButtonNext.setEnabled(false);
				uiButtonFinish.setVisible(false);
				break;
			case DATABASE:
				fProgress = Progress.DATASTORE;
				uiCompDatastore.doDisplay();
				uiButtonBack.setVisible(true);
				uiButtonNext.setVisible(true);
				uiButtonNext.setEnabled(false);
				uiButtonFinish.setVisible(false);
				break;
			case DATASTORE:
				fProgress = Progress.FINISH;
				uiCompFinish.doDisplay();
				uiButtonBack.setVisible(true);
				uiButtonNext.setVisible(false);
				uiButtonNext.setEnabled(true);
				uiButtonFinish.setVisible(true);
				break;
			case FINISH:
				uiCompFinish.doDisplay();
				uiButtonBack.setVisible(true);
				uiButtonNext.setVisible(false);
				uiButtonNext.setEnabled(true);
				uiButtonFinish.setVisible(true);
		}
		getLayout().layoutContainer(this);
	}
	
	private void doBack() throws RuntimeException
	{
		switch(fProgress)
		{
			case WELCOME:
				uiCompWelcome.doDisplay();
				uiButtonBack.setVisible(false);
				uiButtonNext.setVisible(true);
				uiButtonNext.setEnabled(true);
				uiButtonFinish.setVisible(false);
			case DEPENDENCY:
				fProgress = Progress.WELCOME;
				uiCompWelcome.doDisplay();
				uiButtonBack.setVisible(false);
				uiButtonNext.setVisible(true);
				uiButtonNext.setEnabled(true);
				uiButtonFinish.setVisible(false);
				break;
			case DATABASE:
				fProgress = Progress.DEPENDENCY;
				uiCompDependency.doDisplay();
				uiButtonBack.setVisible(true);
				uiButtonNext.setVisible(true);
				uiButtonNext.setEnabled(true);
				uiButtonFinish.setVisible(false);
				break;
			case DATASTORE:
				fProgress = Progress.DATABASE;
				uiCompDatabase.doDisplay();
				uiButtonBack.setVisible(true);
				uiButtonNext.setVisible(true);
				uiButtonNext.setEnabled(true);
				uiButtonFinish.setVisible(false);
				break;
			case FINISH:
				fProgress = Progress.DATASTORE;
				uiCompDatastore.doDisplay();
				uiButtonBack.setVisible(true);
				uiButtonNext.setVisible(true);
				uiButtonNext.setEnabled(true);
				uiButtonFinish.setVisible(false);
				break;
		}
		getLayout().layoutContainer(this);
	}
	
	private static abstract class Dialog extends JComponent implements LayoutManager
	{
		protected abstract void doDisplay();
		
		@Override
		public void addLayoutComponent(String key,Component c) { }
		
		@Override
		public void removeLayoutComponent(Component c) { }
		
		@Override
		public Dimension minimumLayoutSize(Container parent)
		{
			return new Dimension(300,250);
		}
		
		@Override
		public Dimension preferredLayoutSize(Container parent)
		{
			return new Dimension(300,250);
		}
	}
	
	private final class DialogWelcome extends Dialog
	{
		private JLabel uiLabelWelcome;
		private String rcLabelWelcome = "<html>Welcome to DoujinDB.<br/>" +
				"<br/>" +
				"We couldn't find any configuration file, so either you deleted it or this is the first time you run DoujinDB.<br/>" +
				"<br/>" +
				"This wizard will help you through the process of configuring the program.<br/>" +
				"<br/>" +
				"Click <b>Next</b> to proceed." +
				"</html>";
		
		private DialogWelcome()
		{
			uiLabelWelcome = new JLabel(rcLabelWelcome);
			uiLabelWelcome.setOpaque(false);
			super.add(uiLabelWelcome);
			super.setLayout(this);
		}
		
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			uiLabelWelcome.setBounds(0, 0, width, height);
		}

		@Override
		protected void doDisplay() {
			panel.doLayout();
		}
	}
	
	private final class DialogDependency extends Dialog
	{
		private JLabel uiLabelDependency;
		private String rcLabelDependency = "<html>Apache Cayenne (and dependencies) are required to run DoujinDB.</html>";
		
		private String[][] mLibraries = new String[][]{
			new String[]{ "ashwood",             "http://search.maven.org/remotecontent?filepath=org/objectstyle/ashwood/ashwood/2.0/ashwood-2.0.jar" },
			new String[]{ "cayenne-server",      "http://search.maven.org/remotecontent?filepath=org/apache/cayenne/cayenne-server/3.0.2/cayenne-server-3.0.2.jar" },
			new String[]{ "commons-collections", "http://search.maven.org/remotecontent?filepath=commons-collections/commons-collections/3.1/commons-collections-3.1.jar" },
			new String[]{ "commons-logging",     "http://search.maven.org/remotecontent?filepath=commons-logging/commons-logging-api/1.1/commons-logging-api-1.1.jar" },
			new String[]{ "velocity",            "http://search.maven.org/remotecontent?filepath=velocity/velocity/1.3/velocity-1.3.jar" }
		};
		private Map<String, JLabel> mLibLabel = new HashMap<String, JLabel>();
		private Map<String, JButton> mLibButton = new HashMap<String, JButton>();
		
		private DialogDependency()
		{
			uiLabelDependency = new JLabel(rcLabelDependency);
			uiLabelDependency.setOpaque(false);
			
			for(String[] lib : mLibraries)
			{
				final String libName = lib[0];
				final String libURL = lib[1];
				
				JLabel uiLabelLib = new JLabel(libName);
				uiLabelLib.setIcon(UI.Icon.window_loading);
				mLibLabel.put(libName, uiLabelLib);
				super.add(uiLabelLib);
				
				JButton uiButtonLib = new JButton(UI.Icon.window_dialog_configwiz_download);
				uiButtonLib.setText("Download");
				uiButtonLib.setFocusable(false);
				uiButtonLib.setVisible(false);
				uiButtonLib.setBorderPainted(true);
				uiButtonLib.setBorder(BorderFactory.createLineBorder(linecolor));
				uiButtonLib.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae) 
					{
						final JButton source = ((JButton) ae.getSource());
						source.setVisible(false);
						mLibLabel.get(libName).setIcon(UI.Icon.window_loading);
						
						new SwingWorker<Void, String>() {
							private boolean errorOccurred = true;
							@Override
							protected Void doInBackground() throws Exception {
								try {
									File libFile = new File(new File(Core.DOUJINDB_HOME, "lib"), libName + ".jar");
									libFile.getParentFile().mkdirs();
									FileOutputStream libFileOS = new FileOutputStream(libFile);
									libFileOS.getChannel().transferFrom(Channels.newChannel(new URL(libURL).openStream()), 0, Long.MAX_VALUE);
									libFileOS.close();
									errorOccurred = false;
								} catch (IOException ioe) {
									errorOccurred = true;
									ioe.printStackTrace();
								}
								return null;
							}
							
							@Override
							protected void done() {
								if(errorOccurred) {
									source.setVisible(true);
									mLibLabel.get(libName).setIcon(UI.Icon.window_dialog_configwiz_error);
								} else {
									// check again
									doDisplay();
								}
							}
						}.execute();
					}
				});
				mLibButton.put(libName, uiButtonLib);
				super.add(uiButtonLib);
			}
			
			super.add(uiLabelDependency);
			super.setLayout(this);
		}
		
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight(),
				index = 0;
			uiLabelDependency.setBounds(0, 0, width, 40);
			for(String lib : mLibLabel.keySet())
			{
				mLibLabel.get(lib).setBounds(5,40 + index * 20,width-85,20);
				mLibButton.get(lib).setBounds(width-85,40 + index * 20,80,20);
				index++;
			}
		}

		@Override
		protected void doDisplay() {
			// reset status
			for(String lib : mLibLabel.keySet())
			{
				mLibLabel.get(lib).setIcon(UI.Icon.window_loading);
				mLibButton.get(lib).setVisible(false);
			}
			// async check every library
			new SwingWorker<Void, String>() {
				private boolean missingLib = false;
				@Override
				protected Void doInBackground() throws Exception {
					for(String lib : mLibLabel.keySet())
					{
						if(new File(new File(Core.DOUJINDB_HOME, "lib"), lib + ".jar").exists())
						{
							mLibLabel.get(lib).setIcon(UI.Icon.window_dialog_configwiz_success);
							mLibButton.get(lib).setVisible(false);
						} else {
							missingLib = true;
							mLibLabel.get(lib).setIcon(UI.Icon.window_dialog_configwiz_error);
							mLibButton.get(lib).setVisible(true);
						}
					}
					return null;
				}
				
				@Override
				protected void done() {
					if(missingLib)
						uiButtonNext.setEnabled(false);
					else
						uiButtonNext.setEnabled(true);
				}
			}.execute();
			
			panel.doLayout();
		}
	}
	
	private final class DialogDatabase extends Dialog
	{
		private JLabel uiLabelDatabase;
		private String rcLabelDatabase = "<html>The Database is where all the metadata is stored: book info, tags, authors ...</html>";
		
		private JLabel uiLabelDriver;
		private JComboBox<String> uiComboboxDriver;
		private Map<String, String> drivers = new HashMap<String, String>();
		private JLabel uiLabelURL;
		private JTextField uiTextURL;
		private JLabel uiLabelUsername;
		private JTextField uiTextUsername;
		private JLabel uiLabelPassword;
		private JTextField uiTextPassword;
		private JButton uiTest;
		private JLabel uiLabelResult;
		
		private DialogDatabase()
		{
			uiLabelDatabase = new JLabel(rcLabelDatabase);
			uiLabelDatabase.setOpaque(false);
			super.add(uiLabelDatabase);
			
			uiLabelDriver = new JLabel("Driver");
			super.add(uiLabelDriver);
			uiComboboxDriver = new JComboBox<String>();
			uiComboboxDriver.setFocusable(false);
			uiComboboxDriver.setLightWeightPopupEnabled(true);
			drivers.put("SQLite", "org.sqlite.JDBC");
			uiComboboxDriver.addItem("SQLite");
			drivers.put("MySQL", "com.mysql.jdbc.Driver");
			uiComboboxDriver.addItem("MySQL");
			drivers.put("HyperSQL", "org.hsqldb.jdbcDriver");
			uiComboboxDriver.addItem("HyperSQL");
			drivers.put("DerbyDB", "org.apache.derby.jdbc.EmbeddedDriver");
			uiComboboxDriver.addItem("DerbyDB");
			drivers.put("PostgreSQL", "org.postgresql.Driver");
			uiComboboxDriver.addItem("PostgreSQL");
			drivers.put("Oracle", "oracle.jdbc.OracleDriver");
			uiComboboxDriver.addItem("Oracle");
			drivers.put("Microsoft SQL Server", "com.microsoft.jdbc.sqlserver.SQLServerDriver");
			uiComboboxDriver.addItem("Microsoft SQL Server");
			super.add(uiComboboxDriver);
			uiLabelURL = new JLabel("URL");
			super.add(uiLabelURL);
			uiTextURL = new JTextField("jdbc:sqlite:doujindb.sqlite");
			super.add(uiTextURL);
			uiLabelUsername = new JLabel("Username");
			super.add(uiLabelUsername);
			uiTextUsername = new JTextField("");
			super.add(uiTextUsername);
			uiLabelPassword = new JLabel("Password");
			super.add(uiLabelPassword);
			uiTextPassword = new JTextField("");
			super.add(uiTextPassword);
			uiLabelResult = new JLabel("");
			super.add(uiLabelResult);
			uiTest = new JButton(UI.Icon.window_dialog_configwiz_reload);
			uiTest.setBorder(null);
			uiTest.setFocusable(false);
			uiTest.setText("Test");
			uiTest.setToolTipText("Test");
			uiTest.setMnemonic('T');
			uiTest.setBorderPainted(true);
			uiTest.setBorder(BorderFactory.createLineBorder(linecolor));
			uiTest.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					uiTest.setIcon(UI.Icon.window_dialog_configwiz_loading);
					uiComboboxDriver.setEditable(false);
					uiTextURL.setEditable(false);
					uiTextUsername.setEditable(false);
					uiTextPassword.setEditable(false);
					
					final String dbDriver = drivers.get((String) uiComboboxDriver.getSelectedItem()),
						dbURL = uiTextURL.getText(),
						dbUsername = uiTextUsername.getText(),
						dbPassword = uiTextPassword.getText();
					Configuration.configWrite("org.dyndns.doujindb.db.driver", dbDriver);
					Configuration.configWrite("org.dyndns.doujindb.db.url", dbURL);
					Configuration.configWrite("org.dyndns.doujindb.db.username", dbUsername);
					Configuration.configWrite("org.dyndns.doujindb.db.password", dbPassword);
					
					new Thread()
					{
						public void run()
						{
							try {
								Class.forName(dbDriver);
								ExecutorService executor = Executors.newCachedThreadPool();
								Callable<Connection> task = new Callable<Connection>()
								{
								   public Connection call()
								   {
								      try {
										return DriverManager.getConnection(dbURL,
												dbUsername,
												dbPassword);
									} catch (SQLException sqle) {
										/**
										 * SQL error messages are too verbose,
										 * mask them off with a common error message
										 * and print the stack trace to the standard output.
										 */
										uiLabelResult.setText("<html>Error connecting to SQL resource '" + dbURL + "'.</html>");
										uiLabelResult.setIcon(UI.Icon.window_dialog_configwiz_error);
										uiLabelResult.setForeground(Color.RED);
										sqle.printStackTrace();
										return null;
									}
								   }
								};
								Future<Connection> future = executor.submit(task);
								try
								{
									Connection conn = future.get(3, TimeUnit.SECONDS);
									if(conn != null)
									{
										try
										{
											uiLabelResult.setText("<html>Connection established.</html>");
											uiLabelResult.setIcon(UI.Icon.window_dialog_configwiz_success);
											uiLabelResult.setForeground(Color.GREEN);
											uiButtonNext.setEnabled(true);
											conn.close();
										} catch (Exception e) {}
									} else {
										uiLabelResult.setText("<html>Error connecting to SQL resource '" + dbURL + "'.</html>");
										uiLabelResult.setIcon(UI.Icon.window_dialog_configwiz_error);
										uiLabelResult.setForeground(Color.RED);
									}
								} catch (TimeoutException te) {
									uiLabelResult.setText("<html>Timeout Exception while obtaining SQL connection.</html>");
									uiLabelResult.setIcon(UI.Icon.window_dialog_configwiz_error);
									uiLabelResult.setForeground(Color.RED);
								} catch (InterruptedException ie) {
									uiLabelResult.setText("<html>Interrupted Exception while obtaining SQL connection.</html>");
									uiLabelResult.setIcon(UI.Icon.window_dialog_configwiz_error);
									uiLabelResult.setForeground(Color.RED);
								} catch (ExecutionException ee) {
									uiLabelResult.setText("<html>Execution Exception while obtaining SQL connection.</html>");
									uiLabelResult.setIcon(UI.Icon.window_dialog_configwiz_error);
									uiLabelResult.setForeground(Color.RED);
								} finally {
								   future.cancel(true);
								}
							} catch (ClassNotFoundException cnfe) {
								uiLabelResult.setText("<html>Cannot load jdbc driver '" + dbDriver + "' : Class not found.</html>");
								uiLabelResult.setIcon(UI.Icon.window_dialog_configwiz_error);
								uiLabelResult.setForeground(Color.RED);
							}
							uiTextPassword.setEditable(true);
							uiTextUsername.setEditable(true);
							uiTextURL.setEditable(true);
							uiComboboxDriver.setEditable(true);
							uiTest.setIcon(UI.Icon.window_dialog_configwiz_reload);
						}
					}.start();
				}					
			});
			super.add(uiTest);
			super.setLayout(this);
		}
		
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			int labelLength = 85;
			uiLabelDatabase.setBounds(0, 0, width, 40);
			uiLabelDriver.setBounds(5,40,labelLength,20);
			uiComboboxDriver.setBounds(labelLength+5,40,width-labelLength-5,20);
			uiLabelURL.setBounds(5,60,labelLength,20);
			uiTextURL.setBounds(labelLength+5,60,width-labelLength-5,20);
			uiLabelUsername.setBounds(5,80,labelLength,20);
			uiTextUsername.setBounds(labelLength+5,80,width-labelLength-5,20);
			uiLabelPassword.setBounds(5,100,labelLength,20);
			uiTextPassword.setBounds(labelLength+5,100,width-labelLength-5,20);
			uiLabelResult.setBounds(5,110,width-10,45);
			uiTest.setBounds(width/2-40,height-20,80,20);
		}

		@Override
		protected void doDisplay() {
			panel.doLayout();
		}
	}
	
	private final class DialogDatastore extends Dialog
	{
		private JLabel uiLabelDatastore;
		private String rcLabelDatastore = "<html>The Datastore contains all the data files. Cache contains cached cover images.</html>";
		
		private JLabel uiLabelStore;
		private JTextField uiTextStore;
		private JLabel uiLabelCache;
		private JTextField uiTextCache;
		private JButton uiTest;
		private JLabel uiLabelResult;
		
		private DialogDatastore()
		{
			uiLabelDatastore = new JLabel(rcLabelDatastore);
			uiLabelDatastore.setOpaque(false);
			super.add(uiLabelDatastore);
			
			uiLabelStore = new JLabel("Data");
			super.add(uiLabelStore);
			uiTextStore = new JTextField("/path/to/store/");
			super.add(uiTextStore);
			uiLabelCache = new JLabel("Cache");
			super.add(uiLabelCache);
			uiTextCache = new JTextField(System.getProperty("java.io.tmpdir"));
			super.add(uiTextCache);
			uiLabelResult = new JLabel("");
			super.add(uiLabelResult);
			uiTest = new JButton(UI.Icon.window_dialog_configwiz_reload);
			uiTest.setBorder(null);
			uiTest.setFocusable(false);
			uiTest.setText("Test");
			uiTest.setToolTipText("Test");
			uiTest.setMnemonic('T');
			uiTest.setBorderPainted(true);
			uiTest.setBorder(BorderFactory.createLineBorder(linecolor));
			uiTest.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					uiTest.setIcon(UI.Icon.window_dialog_configwiz_loading);
					uiTextStore.setEditable(false);
					uiTextCache.setEditable(false);
					
					Configuration.configWrite("org.dyndns.doujindb.dat.datastore", uiTextStore.getText());
					Configuration.configWrite("org.dyndns.doujindb.dat.cache_dir", uiTextCache.getText());
					
					new Thread()
					{
						public void run()
						{
							try {
								// Test Data directory
								File store = new File(uiTextStore.getText());
								if(!store.exists() || !store.isDirectory())
									throw new RuntimeException("Data directory is not a valid path.");
								File store_rw = new File(store, ".rw-store");
								try {
									store_rw.createNewFile();
								} catch (IOException ioe) {
									throw new RuntimeException("Data directory is not writable: check your permissions.");
								}
								store_rw.deleteOnExit();
								if(!store_rw.exists())
									throw new RuntimeException("Data directory is not writable: check your permissions.");
								if(!store_rw.canRead())
									throw new RuntimeException("Data directory is not readable: check your permissions.");
								if(!store_rw.canWrite())
									throw new RuntimeException("Data directory is not writable: check your permissions.");
								// Test Cache directory
								File cache = new File(uiTextCache.getText());
								if(!cache.exists() || !cache.isDirectory())
									throw new RuntimeException("Cache directory is not a valid path.");
								File cache_rw = new File(cache, ".rw-cache");
								try {
									cache_rw.createNewFile();
								} catch (IOException ioe) {
									throw new RuntimeException("Cache directory is not writable: check your permissions.");
								}
								cache_rw.deleteOnExit();
								if(!cache_rw.exists())
									throw new RuntimeException("Cache directory is not writable: check your permissions.");
								if(!cache_rw.canRead())
									throw new RuntimeException("Cache directory is not readable: check your permissions.");
								if(!cache_rw.canWrite())
									throw new RuntimeException("Cache directory is not writable: check your permissions.");
								// Both directories are valid
								uiLabelResult.setText("<html>Both directories are valid.</html>");
								uiLabelResult.setIcon(UI.Icon.window_dialog_configwiz_success);
								uiLabelResult.setForeground(Color.GREEN);
								uiButtonNext.setEnabled(true);
							} catch (RuntimeException re) {
								uiLabelResult.setText("<html>" + re.getMessage() + "</html>");
								uiLabelResult.setIcon(UI.Icon.window_dialog_configwiz_error);
								uiLabelResult.setForeground(Color.RED);
							}
							uiTextCache.setEditable(true);
							uiTextStore.setEditable(true);
							uiTest.setIcon(UI.Icon.window_dialog_configwiz_reload);
						}
					}.start();
				}					
			});
			super.add(uiTest);
			super.setLayout(this);
		}
		
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			uiLabelDatastore.setBounds(0, 0, width, 40);
			uiLabelStore.setBounds(5,40,width-10,20);
			uiTextStore.setBounds(5,60,width-10,20);
			uiLabelCache.setBounds(5,80,width-10,20);
			uiTextCache.setBounds(5,100,width-10,20);
			uiLabelResult.setBounds(5,110,width-10,45);
			uiTest.setBounds(width/2-40,height-20,80,20);
		}

		@Override
		protected void doDisplay() {
			panel.doLayout();
		}
	}
	
	private final class DialogFinish extends Dialog
	{
		private JLabel uiLabelFinish;
		private String rcLabelFinish = "<html>DoujinDB is now configured.<br/>" +
				"<br/>" +
				"You can later change all these settings from the <b>Settings</b> tab (where you'll find more things to be customized).<br/>" +
				"<br/>" +
				"Click <b>Finish</b> to end this Wizard." +
				"</html>";
		
		private DialogFinish()
		{
			uiLabelFinish = new JLabel(rcLabelFinish);
			uiLabelFinish.setOpaque(false);
			super.add(uiLabelFinish);
			super.setLayout(this);
		}
		
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			uiLabelFinish.setBounds(0, 0, width, height);
		}

		@Override
		protected void doDisplay() {
			panel.doLayout();
		}
	}
}
