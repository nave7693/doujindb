package org.dyndns.doujindb.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.concurrent.*;
import javax.swing.*;

import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.ui.desk.DialogEx;

@SuppressWarnings({"serial","unused"})
final class ConfigurationWizard  extends JComponent implements Runnable, LayoutManager
{
	private JLabel uiBottomDivisor;
	private JButton uiButtonNext;
	private JButton uiButtonPrev;
	private JButton uiButtonFinish;
	private JButton uiButtonCanc;
	private JLabel uiLabelHeader;
	private JLabel uiLabelHeaderImage;
	// STEP 1
	private JLabel uiLabelWelcome;
	// STEP 2
	private JComponent uiCompDatabase;
	private JLabel uiCompDatabaseLabelDriver;
	private JTextField uiCompDatabaseTextDriver;
	private JLabel uiCompDatabaseLabelURL;
	private JTextField uiCompDatabaseTextURL;
	private JLabel uiCompDatabaseLabelUsername;
	private JTextField uiCompDatabaseTextUsername;
	private JLabel uiCompDatabaseLabelPassword;
	private JTextField uiCompDatabaseTextPassword;
	private JButton uiCompDatabaseTest;
	private JLabel uiCompDatabaseLabelResult;
	// STEP 3
	private JComponent uiCompDatastore;
	private JLabel uiCompDatastoreLabelStore;
	private JTextField uiCompDatastoreTextStore;
	private JLabel uiCompDatastoreLabelTemp;
	private JTextField uiCompDatastoreTextTemp;
	private JButton uiCompDatastoreTest;
	private JLabel uiCompDatastoreLabelResult;
	// STEP 4
	private JLabel uiLabelFinish;
	
	private static final Color foreground = (Color) Configuration.configRead("org.dyndns.doujindb.ui.theme.color");
	private static final Color background = (Color) Configuration.configRead("org.dyndns.doujindb.ui.theme.background");
	private static final Color linecolor = background.brighter();
	
	enum Step
	{
		WELCOME (1),
		DATABASE (2),
		DATASTORE (3),
		//TODO ? INTERFACE (4),
		FINISH (5);
		
		private final double value;
		
		Step()
		{
			this(1);
		}
		Step(int value)
		{
			this.value = value;
		}
	}
	
	private Step progress = Step.WELCOME;
	
	public ConfigurationWizard()
	{
		uiLabelHeader = new JLabel();
		uiLabelHeader.setOpaque(true);
		uiLabelHeader.setBackground(background);
		super.add(uiLabelHeader);
		uiLabelHeaderImage = new JLabel(UI.Icon.window_dialog_configwiz_header);
		uiLabelHeaderImage.setOpaque(true);
		uiLabelHeaderImage.setBackground(background);
		super.add(uiLabelHeaderImage);
		uiLabelWelcome = new JLabel("<html>Welcome to DoujinDB.<br/>" +
				"<br/>" +
				"We couldn't find any configuration file, so either you deleted it or this is the first time you run DoujinDB.<br/>" +
				"<br/>" +
				"This wizard will help you through the process of configuring the program.<br/>" +
				"<br/>" +
				"Click <b>Next</b> to proceed." +
				"</html>");
		uiLabelWelcome.setOpaque(false);
		super.add(uiLabelWelcome);
		{
			uiCompDatabase = new JPanel();
			uiCompDatabaseLabelDriver = new JLabel("Driver");
			uiCompDatabase.add(uiCompDatabaseLabelDriver);
			uiCompDatabaseTextDriver = new JTextField("org.sqlite.JDBC");
			uiCompDatabase.add(uiCompDatabaseTextDriver);
			uiCompDatabaseLabelURL = new JLabel("URL");
			uiCompDatabase.add(uiCompDatabaseLabelURL);
			uiCompDatabaseTextURL = new JTextField("jdbc:sqlite:doujindb.sqlite");
			uiCompDatabase.add(uiCompDatabaseTextURL);
			uiCompDatabaseLabelUsername = new JLabel("Username");
			uiCompDatabase.add(uiCompDatabaseLabelUsername);
			uiCompDatabaseTextUsername = new JTextField("");
			uiCompDatabase.add(uiCompDatabaseTextUsername);
			uiCompDatabaseLabelPassword = new JLabel("Password");
			uiCompDatabase.add(uiCompDatabaseLabelPassword);
			uiCompDatabaseTextPassword = new JTextField("");
			uiCompDatabase.add(uiCompDatabaseTextPassword);
			uiCompDatabaseLabelResult = new JLabel("");
			uiCompDatabase.add(uiCompDatabaseLabelResult);
			uiCompDatabaseTest = new JButton(UI.Icon.window_dialog_configwiz_dbtest);
			uiCompDatabaseTest.setBorder(null);
			uiCompDatabaseTest.setFocusable(false);
			uiCompDatabaseTest.setText("Test");
			uiCompDatabaseTest.setToolTipText("Test");
			uiCompDatabaseTest.setMnemonic('T');
			uiCompDatabaseTest.setBorderPainted(true);
			uiCompDatabaseTest.setBorder(BorderFactory.createLineBorder(linecolor));
			uiCompDatabaseTest.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					uiCompDatabaseTest.setIcon(UI.Icon.window_dialog_configwiz_loading);
					uiCompDatabaseTextDriver.setEditable(false);
					uiCompDatabaseTextURL.setEditable(false);
					uiCompDatabaseTextUsername.setEditable(false);
					uiCompDatabaseTextPassword.setEditable(false);
					;
					new Thread()
					{
						public void run()
						{
							try {
								Class.forName(uiCompDatabaseTextDriver.getText());
								ExecutorService executor = Executors.newCachedThreadPool();
								Callable<Connection> task = new Callable<Connection>()
								{
								   public Connection call()
								   {
								      try {
										return DriverManager.getConnection(uiCompDatabaseTextURL.getText(),
												uiCompDatabaseTextUsername.getText(),
												uiCompDatabaseTextPassword.getText());
									} catch (SQLException sqle) {
										/**
										 * SQL error messages are too verbose,
										 * mask them off with a common error message
										 * and print the stack trace to the standard output.
										 */
										uiCompDatabaseLabelResult.setText("<html>Error connecting to SQL resource '" + uiCompDatabaseTextURL.getText() + "'.</html>");
										uiCompDatabaseLabelResult.setIcon(UI.Icon.window_dialog_configwiz_error);
										uiCompDatabaseLabelResult.setForeground(Color.RED);
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
											uiCompDatabaseLabelResult.setText("<html>Connection established.</html>");
											uiCompDatabaseLabelResult.setIcon(UI.Icon.window_dialog_configwiz_success);
											uiCompDatabaseLabelResult.setForeground(Color.GREEN);
											conn.close();
										} catch (Exception e) {}
									} else {
										uiCompDatabaseLabelResult.setText("<html>Error connecting to SQL resource '" + uiCompDatabaseTextURL.getText() + "'.</html>");
										uiCompDatabaseLabelResult.setIcon(UI.Icon.window_dialog_configwiz_error);
										uiCompDatabaseLabelResult.setForeground(Color.RED);
									}
								} catch (TimeoutException te) {
									uiCompDatabaseLabelResult.setText("<html>Timeout Exception while obtaining SQL connection.</html>");
									uiCompDatabaseLabelResult.setIcon(UI.Icon.window_dialog_configwiz_error);
									uiCompDatabaseLabelResult.setForeground(Color.RED);
								} catch (InterruptedException ie) {
									uiCompDatabaseLabelResult.setText("<html>Interrupted Exception while obtaining SQL connection.</html>");
									uiCompDatabaseLabelResult.setIcon(UI.Icon.window_dialog_configwiz_error);
									uiCompDatabaseLabelResult.setForeground(Color.RED);
								} catch (ExecutionException ee) {
									uiCompDatabaseLabelResult.setText("<html>Execution Exception while obtaining SQL connection.</html>");
									uiCompDatabaseLabelResult.setIcon(UI.Icon.window_dialog_configwiz_error);
									uiCompDatabaseLabelResult.setForeground(Color.RED);
								} finally {
								   future.cancel(true);
								}
							} catch (ClassNotFoundException cnfe) {
								uiCompDatabaseLabelResult.setText("<html>Cannot load jdbc driver '" + uiCompDatabaseTextDriver.getText() + "' : Class not found.</html>");
								uiCompDatabaseLabelResult.setIcon(UI.Icon.window_dialog_configwiz_error);
								uiCompDatabaseLabelResult.setForeground(Color.RED);
							}
							uiCompDatabaseTextPassword.setEditable(true);
							uiCompDatabaseTextUsername.setEditable(true);
							uiCompDatabaseTextURL.setEditable(true);
							uiCompDatabaseTextDriver.setEditable(true);
							uiCompDatabaseTest.setIcon(UI.Icon.window_dialog_configwiz_dbtest);
						}
					}.start();
				}					
			});
			uiCompDatabase.add(uiCompDatabaseTest);
			uiCompDatabase.setLayout(new LayoutManager()
			{
				@Override
				public void addLayoutComponent(String key,Component c){}
				@Override
				public void removeLayoutComponent(Component c){}
				@Override
				public Dimension minimumLayoutSize(Container parent)
				{
					return new Dimension(250,200);
				}
				@Override
				public Dimension preferredLayoutSize(Container parent)
				{
					return new Dimension(250,200);
				}
				@Override
				public void layoutContainer(Container parent)
				{
					int width = parent.getWidth(),
						height = parent.getHeight();
					int labelLength = 85;
					uiCompDatabaseLabelDriver.setBounds(5,5,labelLength,20);
					uiCompDatabaseTextDriver.setBounds(labelLength+5,5,width-labelLength-5,20);
					uiCompDatabaseLabelURL.setBounds(5,25,labelLength,20);
					uiCompDatabaseTextURL.setBounds(labelLength+5,25,width-labelLength-5,20);
					uiCompDatabaseLabelUsername.setBounds(5,45,labelLength,20);
					uiCompDatabaseTextUsername.setBounds(labelLength+5,45,width-labelLength-5,20);
					uiCompDatabaseLabelPassword.setBounds(5,65,labelLength,20);
					uiCompDatabaseTextPassword.setBounds(labelLength+5,65,width-labelLength-5,20);
					uiCompDatabaseLabelResult.setBounds(5,90,width-10,45);
					uiCompDatabaseTest.setBounds(width/2-40,height-25,80,20);
				}
			});
			super.add(uiCompDatabase);
		}
		{
			uiCompDatastore = new JPanel();
			uiCompDatastoreLabelStore = new JLabel("Store Directory");
			uiCompDatastore.add(uiCompDatastoreLabelStore);
			uiCompDatastoreTextStore = new JTextField("/path/to/store/");
			uiCompDatastore.add(uiCompDatastoreTextStore);
			uiCompDatastoreLabelTemp = new JLabel("Temporary Directory");
			uiCompDatastore.add(uiCompDatastoreLabelTemp);
			uiCompDatastoreTextTemp = new JTextField("/tmp/");
			uiCompDatastore.add(uiCompDatastoreTextTemp);
			uiCompDatastoreLabelResult = new JLabel("");
			uiCompDatastore.add(uiCompDatastoreLabelResult);
			uiCompDatastoreTest = new JButton(UI.Icon.window_dialog_configwiz_dstest);
			uiCompDatastoreTest.setBorder(null);
			uiCompDatastoreTest.setFocusable(false);
			uiCompDatastoreTest.setText("Test");
			uiCompDatastoreTest.setToolTipText("Test");
			uiCompDatastoreTest.setMnemonic('T');
			uiCompDatastoreTest.setBorderPainted(true);
			uiCompDatastoreTest.setBorder(BorderFactory.createLineBorder(linecolor));
			uiCompDatastoreTest.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					uiCompDatastoreTest.setIcon(UI.Icon.window_dialog_configwiz_loading);
					uiCompDatastoreTextStore.setEditable(false);
					uiCompDatastoreTextTemp.setEditable(false);
					;
					new Thread()
					{
						public void run()
						{
							try {
								File store = new File(uiCompDatastoreTextStore.getText());
								if(!store.exists() || !store.isDirectory())
									throw new RuntimeException("Store folder is not a valid directory path.");
								File store_rw = new File(store, ".rw-store");
								store_rw.createNewFile();
								store_rw.deleteOnExit();
								if(!store_rw.exists())
									throw new RuntimeException("Store directory is not writable: check your permissions.");
								if(!store_rw.canRead())
									throw new RuntimeException("Store directory is not readable: check your permissions.");
								if(!store_rw.canWrite())
									throw new RuntimeException("Store directory is not writable: check your permissions.");
								File temp = new File(uiCompDatastoreTextTemp.getText());
								if(!temp.exists() || !temp.isDirectory())
									throw new RuntimeException("Temporary folder is not a valid directory path.");
								File temp_rw = new File(temp, ".rw-temp");
								temp_rw.createNewFile();
								temp_rw.deleteOnExit();
								if(!temp_rw.exists())
									throw new RuntimeException("Temporary directory is not writable: check your permissions.");
								if(!temp_rw.canRead())
									throw new RuntimeException("Temporary directory is not readable: check your permissions.");
								if(!temp_rw.canWrite())
									throw new RuntimeException("Temporary directory is not writable: check your permissions.");
								;
								uiCompDatastoreLabelResult.setText("<html>Both directories are valid.</html>");
								uiCompDatastoreLabelResult.setIcon(UI.Icon.window_dialog_configwiz_success);
								uiCompDatastoreLabelResult.setForeground(Color.GREEN);
							} catch (RuntimeException re) {
								uiCompDatastoreLabelResult.setText("<html>" + re.getMessage() + "</html>");
								uiCompDatastoreLabelResult.setIcon(UI.Icon.window_dialog_configwiz_error);
								uiCompDatastoreLabelResult.setForeground(Color.RED);
							} catch (IOException ioe) {
								uiCompDatastoreLabelResult.setText("<html>" + ioe.getMessage() + "</html>");
								uiCompDatastoreLabelResult.setIcon(UI.Icon.window_dialog_configwiz_error);
								uiCompDatastoreLabelResult.setForeground(Color.RED);
							}
							uiCompDatastoreTextTemp.setEditable(true);
							uiCompDatastoreTextStore.setEditable(true);
							uiCompDatastoreTest.setIcon(UI.Icon.window_dialog_configwiz_dstest);
						}
					}.start();
				}					
			});
			uiCompDatastore.add(uiCompDatastoreTest);
			uiCompDatastore.setLayout(new LayoutManager()
			{
				@Override
				public void addLayoutComponent(String key,Component c){}
				@Override
				public void removeLayoutComponent(Component c){}
				@Override
				public Dimension minimumLayoutSize(Container parent)
				{
					return new Dimension(250,200);
				}
				@Override
				public Dimension preferredLayoutSize(Container parent)
				{
					return new Dimension(250,200);
				}
				@Override
				public void layoutContainer(Container parent)
				{
					int width = parent.getWidth(),
						height = parent.getHeight();
					uiCompDatastoreLabelStore.setBounds(5,5,width-10,20);
					uiCompDatastoreTextStore.setBounds(5,25,width-10,20);
					uiCompDatastoreLabelTemp.setBounds(5,45,width-10,20);
					uiCompDatastoreTextTemp.setBounds(5,65,width-10,20);
					uiCompDatastoreLabelResult.setBounds(5,90,width-10,45);
					uiCompDatastoreTest.setBounds(width/2-40,height-25,80,20);
				}
			});
			super.add(uiCompDatastore);
		}
		uiLabelFinish = new JLabel("<html>DoujinDB is now configured.<br/>" +
				"<br/>" +
				"You can later change all these settings from the <b>Settings</b> tab (where you'll find more things to be customized).<br/>" +
				"<br/>" +
				"Click <b>Finish</b> to end this Wizard." +
				"</html>");
		uiLabelFinish.setOpaque(false);
		super.add(uiLabelFinish);
		uiBottomDivisor = new JLabel();
		uiBottomDivisor.setOpaque(true);
		uiBottomDivisor.setBackground(background);
		super.add(uiBottomDivisor);
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
				next();
			}					
		});
		super.add(uiButtonNext);
		uiButtonPrev = new JButton(UI.Icon.window_dialog_configwiz_prev);
		uiButtonPrev.setEnabled(false);
		uiButtonPrev.setBorder(null);
		uiButtonPrev.setFocusable(false);
		uiButtonPrev.setText("Back");
		uiButtonPrev.setToolTipText("Back");
		uiButtonPrev.setMnemonic('B');
		uiButtonPrev.setBorderPainted(true);
		uiButtonPrev.setBorder(BorderFactory.createLineBorder(linecolor));
		uiButtonPrev.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae) 
			{
				back();
			}					
		});
		super.add(uiButtonPrev);
		uiButtonFinish = new JButton(UI.Icon.window_dialog_configwiz_finish);
		uiButtonFinish.setVisible(false);
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
				Configuration.configWrite("org.dyndns.doujindb.db.driver", uiCompDatabaseTextDriver.getText());
				Configuration.configWrite("org.dyndns.doujindb.db.url", uiCompDatabaseTextURL.getText());
				Configuration.configWrite("org.dyndns.doujindb.db.username", uiCompDatabaseTextUsername.getText());
				Configuration.configWrite("org.dyndns.doujindb.db.password", uiCompDatabaseTextPassword.getText());
				Configuration.configWrite("org.dyndns.doujindb.dat.datastore", uiCompDatastoreTextStore.getText());
				Configuration.configWrite("org.dyndns.doujindb.dat.temp", uiCompDatastoreTextTemp.getText());
				Configuration.configSave();
				DialogEx window = (DialogEx)((JComponent)ae.getSource()).getRootPane().getParent();
				window.dispose();
			}					
		});
		super.add(uiButtonFinish);
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
				DialogEx window = (DialogEx)((JComponent)ae.getSource()).getRootPane().getParent();
				window.dispose();
			}					
		});
		super.add(uiButtonCanc);
		super.setLayout(this);
	}
	
	@Override
	public void addLayoutComponent(String key,Component c){}
	@Override
	public void removeLayoutComponent(Component c){}
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
		uiLabelHeader.setBounds(0,0,width-48,48);
		uiLabelHeaderImage.setBounds(width-48,0,48,48);
		uiBottomDivisor.setBounds(5,height-30,width-10,1);
		uiButtonNext.setBounds(width-80,height-25,75,20);
		uiButtonFinish.setBounds(width-80,height-25,75,20);
		uiButtonPrev.setBounds(width-160,height-25,75,20);
		uiButtonCanc.setBounds(5,height-25,75,20);
		uiLabelWelcome.setBounds(0,0,0,0);
		uiCompDatabase.setBounds(0,0,0,0);
		uiCompDatastore.setBounds(0,0,0,0);
		uiLabelFinish.setBounds(0,0,0,0);
		switch(progress)
		{
		case WELCOME:
			uiLabelWelcome.setBounds(5,50,width-10,height-85);
			break;
		case DATABASE:
			uiCompDatabase.setBounds(5,50,width-10,height-85);
			uiCompDatabase.getLayout().layoutContainer(uiCompDatabase);
			break;
		case DATASTORE:
			uiCompDatastore.setBounds(5,50,width-10,height-85);
			uiCompDatastore.getLayout().layoutContainer(uiCompDatastore);
			break;
		case FINISH:
			uiLabelFinish.setBounds(5,50,width-10,height-85);
			break;
		}
	}
	@Override
	public void run()
	{
		
	}
	private void next() throws RuntimeException
	{
		switch(progress)
		{
		case WELCOME:
			progress = Step.DATABASE;
			uiButtonPrev.setEnabled(true);
			break;
		case DATABASE:
			progress = Step.DATASTORE;
			break;
		case DATASTORE:
			progress = Step.FINISH;
			uiButtonNext.setEnabled(false);
			uiButtonNext.setVisible(false);
			uiButtonFinish.setVisible(true);
			break;
		case FINISH:
			throw new RuntimeException("Already reached the last step.");
		}
		super.getLayout().layoutContainer(this);
	}
	private void back() throws RuntimeException
	{
		switch(progress)
		{
		case WELCOME:
			throw new RuntimeException("Already reached the first step.");
		case DATABASE:
			progress = Step.WELCOME;
			uiButtonPrev.setEnabled(false);
			break;
		case DATASTORE:
			progress = Step.DATABASE;
			break;
		case FINISH:
			progress = Step.DATASTORE;
			uiButtonNext.setEnabled(true);
			uiButtonNext.setVisible(true);
			uiButtonFinish.setVisible(false);
			break;
		}
		super.getLayout().layoutContainer(this);
	}
}
