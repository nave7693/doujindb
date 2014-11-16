package org.dyndns.doujindb.ui.dialog;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.dyndns.doujindb.conf.ConfigurationItem;
import org.dyndns.doujindb.ui.Icons;
import org.dyndns.doujindb.ui.UI;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@SuppressWarnings("serial")
public final class PanelConfiguration extends JSplitPane
{
	private JTree tree;
	private TreeCellRenderer render;
	private DefaultTreeModel model = new DefaultTreeModel(null);
	
	private static final Font Font = UI.Font;
	private static final Icons Icon = UI.Icon;
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(PanelConfiguration.class);
	
	private String itemsNamespace;
	private final HashMap<String, ConfigurationItem<?>> itemsData = new HashMap<String, ConfigurationItem<?>>();
	private final HashMap<Class<?>, ConfigurationItemEditor<?>> itemsEditor = new HashMap<Class<?>, ConfigurationItemEditor<?>>();
	
	private PanelConfiguration() {
		super();
		tree = new JTree();
		tree.setModel(model);
		tree.setFocusable(false);
		tree.setFont(Font);
		tree.setEditable(false);
		tree.setRootVisible(true);
		tree.setScrollsOnExpand(true);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
	    	@SuppressWarnings({ "rawtypes", "unchecked" })
			public void valueChanged(TreeSelectionEvent tse) {
	    		DefaultMutableTreeNode dmtnode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
	    		// Return if no TreeNode is selected
	            if(dmtnode == null)
	            	return;
	            // Return if it's a 'Directory' TreeNode
	            if(!dmtnode.isLeaf())
	            	return;
	            // Calculate configuration key based on TreeNodes path
	            // skip first path-object since it's just the namespace
				Object paths[] = dmtnode.getUserObjectPath();
				String key = "";
				for(int index=1; index<paths.length; index++)
				    key += paths[index] + ".";
				// Remove trailing '.' character
				key = key.substring(0, key.length() - 1);
				// Refresh editor component
				ConfigurationItem item = itemsData.get(key);
				ConfigurationItemEditor editor = itemsEditor.get(item.getType());
				setRightComponent(editor);
				if(editor == null) {
					LOG.warn("Error loading ConfigurationItemEditor for [{}]: unsupported type [{}]", key, item.getType());
					return;
				}
				editor.setItem(key, item);
	    	}
	    });
		render = new TreeNodeRenderer();
		tree.setCellRenderer(render);
		super.setResizeWeight(1);
		super.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		super.setLeftComponent(new JScrollPane(tree));
		super.setRightComponent(null);
		super.setContinuousLayout(true);
		// Finally load TreeNodeEditors
		loadEditors();
	}
	
	public PanelConfiguration(Class<?> config) {
		this();
		parseConfiguration(config);
		loadTreeNode();
	}
	
	public PanelConfiguration(Object config) {
		this();
		parseConfiguration(config);
		loadTreeNode();
	}
	
	private void parseConfiguration(Class<?> config)
	{
		itemsNamespace = config.getPackage().getName();
		for(Field field : config.getDeclaredFields())
		{
			if(field.getType().equals(ConfigurationItem.class) && Modifier.isStatic(field.getModifiers()))
			{
				String configName = field.getName().replaceAll("_", ".");
				try {
					ConfigurationItem<?> configItem = (ConfigurationItem<?>) field.get(config);
					itemsData.put(configName, configItem);
				} catch (IllegalArgumentException | IllegalAccessException iae) {
					LOG.error("Error parsing ConfigurationItem [{}]", configName, iae);
				}
			}
		}
	}
	
	private void parseConfiguration(Object config)
	{
		itemsNamespace = config.getClass().getPackage().getName();
		for(Field field : config.getClass().getDeclaredFields())
		{
			if(field.getType().equals(ConfigurationItem.class))
			{
				String configName = field.getName().replaceAll("_", ".");
				try {
					ConfigurationItem<?> configItem = (ConfigurationItem<?>) field.get(config);
					itemsData.put(configName, configItem);
				} catch (IllegalArgumentException | IllegalAccessException iae) {
					LOG.error("Error parsing ConfigurationItem [{}]", configName, iae);
				}
			}
		}
	}
	
	private void loadTreeNode() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(itemsNamespace);
		for(String key : itemsData.keySet())
			addNode(root, key);
		model = new DefaultTreeModel(root);
		tree.setModel(model);
	}
	
	private void addNode(DefaultMutableTreeNode node, String key) {
		if(key.indexOf(".") != -1) {
		    loop:
		    {
				String subkey = key.substring(0, key.indexOf("."));
		        key = key.substring(key.indexOf(".") + 1);
		        Enumeration<?> e = node.children();
		        while(e.hasMoreElements()) {
			       DefaultMutableTreeNode subnode = (DefaultMutableTreeNode) e.nextElement();
			       if(subkey.equals(subnode.getUserObject())) {
			          addNode(subnode, key);
			          break loop;
			       }
		        }
		        DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(subkey);
		        node.add(dmtn);
		        addNode(dmtn, key);
		     }
		} else {
			node.add(new DefaultMutableTreeNode(key));
		}
	}
	
	private final class TreeNodeRenderer extends DefaultTreeCellRenderer
	{
		public TreeNodeRenderer() {
			setBackgroundSelectionColor(super.getBackground());
		}
	
		public Component getTreeCellRendererComponent(JTree tree,
		    Object value,
		    boolean sel,
		    boolean expanded,
		    boolean leaf,
		    int row,
		    boolean hasFocus)
		{
			super.getTreeCellRendererComponent(
                tree,
		        value,
		        sel,
		        expanded,
		        leaf,
		        row,
		        hasFocus);
			if(!((DefaultMutableTreeNode)value).isLeaf())
				setIcon((ImageIcon)Icon.window_tab_settings_tree_directory);
			else
				setIcon((ImageIcon)Icon.window_tab_settings_tree_value);
		    return this;
		}
	}
	
	private abstract class ConfigurationItemEditor<T> extends JPanel implements LayoutManager
	{
		protected JButton fButtonClose;
		protected JLabel fLabelKey;
		protected JLabel fLabelInfo;
		protected JComponent fCompontent;
		protected JButton fButtonApply;
		protected JButton fButtonDiscard;
		protected JButton fButtonReset;
		
		private ConfigurationItem<T> configItem;
		protected T configValue;
		
		public ConfigurationItemEditor() {
			super();
			super.setLayout(this);
			fLabelKey = new JLabel("", Icon.window_tab_settings_tree_value, JLabel.LEFT);
			fLabelKey.setFont(Font);
			add(fLabelKey);
			fLabelInfo = new JLabel("<html>" + 
				"<body>" + 
				"<b>Type</b> : <br/>" + 
				"<b>Info</b> : " + 
				"</body>" + 
				"</html>");
			fLabelInfo.setVerticalAlignment(JLabel.TOP);
			fLabelInfo.setFont(Font);
			add(fLabelInfo);
			fButtonClose = new JButton(Icon.window_tab_settings_editor_close);
			fButtonClose.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					setRightComponent(null);
					tree.setSelectionRow(0);
				}
			});
			fButtonClose.setBorder(null);
			fButtonClose.setFocusable(false);
			add(fButtonClose);
			fButtonApply = new JButton("Apply", Icon.window_tab_settings_editor_apply);
			fButtonApply.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					configItem.set(configValue);
				}
			});
			fButtonApply.setFont(Font);
			fButtonApply.setFocusable(false);
			add(fButtonApply);
			fButtonDiscard = new JButton("Discard", Icon.window_tab_settings_editor_discard);
			fButtonDiscard.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					setRightComponent(null);
					tree.setSelectionRow(0);
				}
			});
			fButtonDiscard.setFont(Font);
			fButtonDiscard.setFocusable(false);
			add(fButtonDiscard);
			fButtonReset = new JButton("Reset", Icon.window_tab_settings_editor_reset);
			fButtonReset.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					configItem.reset();
					//TODO update ${compontent}
				}
			});
			fButtonReset.setFont(Font);
			fButtonReset.setFocusable(false);
			add(fButtonReset);
			fCompontent = new JPanel();
			add(fCompontent);
			super.setPreferredSize(new Dimension(250, 250));
			super.setMinimumSize(new Dimension(200, 200));
		}
		
		@Override
		public void addLayoutComponent(String name, Component comp) { }
		@Override
		public void layoutContainer(Container comp)
		{
			int width = comp.getWidth(),
				height = comp.getHeight();
			fLabelKey.setBounds(5, 1, width - 21, 20);
			fButtonClose.setBounds(width - 21, 1, 20, 20);
			fLabelInfo.setBounds(5, 21, width - 10, 75);
			fCompontent.setBounds(5, 100, width - 10, height - 120 - 65);
			fButtonApply.setBounds((width - 125) / 2, height - 80, 125, 20);
			fButtonDiscard.setBounds((width - 125) / 2, height - 60, 125, 20);
			fButtonReset.setBounds((width - 125) / 2, height - 40, 125, 20);
		}
		@Override
		public Dimension minimumLayoutSize(Container comp) {
			return getMinimumSize();
		}
		@Override
		public Dimension preferredLayoutSize(Container comp) {
			return getPreferredSize();
		}
		@Override
		public void removeLayoutComponent(Component comp) { }
		
		protected void setItem(String key, ConfigurationItem<T> item) {
			configItem = item;
			fLabelKey.setText(key);
			fLabelInfo.setText("<html>" + 
				"<body>" + 
				"<b>Type</b> : " + configItem.get().getClass().getName() + "<br/>" + 
				"<b>Info</b> : " + configItem.getInfo() + 
				"</body>" + 
				"</html>");
		}
	}
	
	private final class IntegerEditor extends ConfigurationItemEditor<Integer>
	{
		public IntegerEditor() {
			super();
			fCompontent = new JPanel();
			//TODO
			add(fCompontent);
		}
		
		public void setItem(String key, ConfigurationItem<Integer> item) {
			super.setItem(key, item);
			//TODO
		}
	}
	
	private final class StringEditor extends ConfigurationItemEditor<String>
	{
		public StringEditor() {
			super();
			fCompontent = new JPanel();
			//TODO
			add(fCompontent);
		}
		
		public void setItem(String key, ConfigurationItem<String> item) {
			super.setItem(key, item);
			//TODO
		}
	}
	
	private final class FloatEditor extends ConfigurationItemEditor<Float>
	{
		public FloatEditor() {
			super();
			fCompontent = new JPanel();
			//TODO
			add(fCompontent);
		}
		
		public void setItem(String key, ConfigurationItem<Float> item) {
			super.setItem(key, item);
			//TODO
		}
	}
	
	private final class BooleanEditor extends ConfigurationItemEditor<Boolean>
	{
		public BooleanEditor() {
			super();
			fCompontent = new JPanel();
			//TODO
			add(fCompontent);
		}
		
		public void setItem(String key, ConfigurationItem<Boolean> item) {
			super.setItem(key, item);
			//TODO
		}
	}
	
	private final class LevelEditor extends ConfigurationItemEditor<Level>
	{
		public LevelEditor() {
			super();
			fCompontent = new JPanel();
			//TODO
			add(fCompontent);
		}
		
		public void setItem(String key, ConfigurationItem<Level> item) {
			super.setItem(key, item);
			//TODO
		}
	}
	
	private final class ColorEditor extends ConfigurationItemEditor<Color>
	{
		private JLabel fLabelDisplay;
		private JSlider fSliderRed;
		private JSlider fSliderGreen;
		private JSlider fSliderBlue;
		
		public ColorEditor() {
			super();
			fCompontent = new JPanel();
			fLabelDisplay = new JLabel();
			fLabelDisplay.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			fLabelDisplay.setOpaque(true);
			fLabelDisplay.setBackground(new Color(0,0,0,0));
			fCompontent.add(fLabelDisplay);
			fSliderRed = new JSlider(JSlider.HORIZONTAL);
			fSliderRed.setMaximum(255);
			fSliderRed.setMinimum(0);
			fSliderRed.setValue(0);
			fSliderRed.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce) {
					updateItem();
				}
			});
			fCompontent.add(fSliderRed);
			fSliderGreen = new JSlider(JSlider.HORIZONTAL);
			fSliderGreen.setMaximum(255);
			fSliderGreen.setMinimum(0);
			fSliderGreen.setValue(0);
			fSliderGreen.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce) {
					updateItem();
				}
			});
			fCompontent.add(fSliderGreen);
			fSliderBlue = new JSlider(JSlider.HORIZONTAL);
			fSliderBlue.setMaximum(255);
			fSliderBlue.setMinimum(0);
			fSliderBlue.setValue(0);
			fSliderBlue.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce) {
					updateItem();
				}
			});
			fCompontent.add(fSliderBlue);
			add(fCompontent);
			fCompontent.setLayout(new LayoutManager() {
				@Override
				public void addLayoutComponent(String name, Component comp) {}
				@Override
				public void layoutContainer(Container comp)
				{
					int width = comp.getWidth();
					fSliderRed.setBounds(5, 5, width - 10, 20);
					fSliderGreen.setBounds(5, 25, width - 10, 20);
					fSliderBlue.setBounds(5, 45, width - 10, 20);
					fLabelDisplay.setBounds(60, 125, width - 120, 60);
				}
				@Override
				public Dimension minimumLayoutSize(Container comp) {return new Dimension(200, 200);}
				@Override
				public Dimension preferredLayoutSize(Container comp) {return new Dimension(200, 200);}
				@Override
				public void removeLayoutComponent(Component comp) {}
			});
		}
		
		private void updateItem() {
			Color color = new Color(fSliderRed.getValue(), fSliderGreen.getValue(), fSliderBlue.getValue(), 255);
			fLabelDisplay.setBackground(color);
			configValue = color;
		}
		
		public void setItem(String key, ConfigurationItem<Color> item) {
			super.setItem(key, item);
			Color color = item.get();
			fSliderRed.setValue(color.getRed());
			fSliderGreen.setValue(color.getGreen());
			fSliderBlue.setValue(color.getBlue());
			fLabelDisplay.setBackground(color);
		}
	}
	
	private final class FileEditor extends ConfigurationItemEditor<File>
	{
		private JButton fButtonFile;
		
		public FileEditor() {
			super();
			fCompontent = new JPanel();
			fButtonFile = new JButton("");
			fButtonFile.setFocusable(false);
			fButtonFile.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					JFileChooser fileChooser = UI.FileChooser;
					fileChooser.setMultiSelectionEnabled(true);
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					fileChooser.setSelectedFile(configValue);
					if(fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
						return;
					File file = fileChooser.getSelectedFile();
					try {
						fButtonFile.setText(file.getCanonicalPath());
					} catch (IOException ioe) {
						LOG.error("Error getting CanonicalPath from file {}", file, ioe);
					}
					configValue = file;
				}
			});
			fCompontent.add(fButtonFile);
			add(fCompontent);
			fCompontent.setLayout(new LayoutManager() {
				@Override
				public void addLayoutComponent(String name, Component comp) {}
				@Override
				public void layoutContainer(Container comp)
				{
					int width = comp.getWidth(),
						height = comp.getHeight();
					fButtonFile.setBounds(5, height / 2, width - 10, 20);
				}
				@Override
				public Dimension minimumLayoutSize(Container comp) {return new Dimension(200, 200);}
				@Override
				public Dimension preferredLayoutSize(Container comp) {return new Dimension(200, 200);}
				@Override
				public void removeLayoutComponent(Component comp) {}
			});
		}
		
		public void setItem(String key, ConfigurationItem<File> item) {
			super.setItem(key, item);
			try {
				fButtonFile.setText(item.get().getCanonicalPath());
			} catch (IOException ioe) {
				LOG.error("Error getting CanonicalPath from file {}", item.get(), ioe);
			}
		}
	}
	
	private final class FontEditor extends ConfigurationItemEditor<Font>
	{
		private JList<String> fListName;
		private JScrollPane fListNameScroll;
		private JComboBox<Integer> fComboBoxSite;
		private JTextField fTextTest;
		
		public FontEditor() {
			super();
			fCompontent = new JPanel();
			fListName = new JList<String>();
			DefaultListModel<String> model = new DefaultListModel<String>();
			Font envfonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
			for(Font envfont : envfonts)
				model.addElement(envfont.getFontName());
			fListName.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			fListName.setModel(model);
			fListName.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent lse) {
					updateItem();
				}
			});
			fListName.setCellRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					if(value instanceof Font)
						setText(((Font)value).getFontName());
					return this;
				}
			});
			fListNameScroll = new JScrollPane(fListName);
			fCompontent.add(fListNameScroll);
			fComboBoxSite = new JComboBox<Integer>();
			for(int size=8; size<25; size++)
				fComboBoxSite.addItem(size);
			fComboBoxSite.setFocusable(false);
			fComboBoxSite.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ie) {
					updateItem();
				}
			});
			fCompontent.add(fComboBoxSite);
			fTextTest = new JTextField("Test string");
			fCompontent.add(fTextTest);
			add(fCompontent);
			fCompontent.setLayout(new LayoutManager() {
				@Override
				public void addLayoutComponent(String name, Component comp) {}
				@Override
				public void layoutContainer(Container comp)
				{
					int width = comp.getWidth(),
						height = comp.getHeight();
					fListNameScroll.setBounds(5, 5, width - 10, height - 85);
					fComboBoxSite.setBounds(5, height - 80, width - 10, 20);
					fTextTest.setBounds(5, height - 60, width - 10, 20);
				}
				@Override
				public Dimension minimumLayoutSize(Container comp) {return new Dimension(200, 200);}
				@Override
				public Dimension preferredLayoutSize(Container comp) {return new Dimension(200, 200);}
				@Override
				public void removeLayoutComponent(Component comp) {}
			});
		}
		
		private void updateItem() {
			Font font = new Font(fListName.getSelectedValue(), java.awt.Font.PLAIN, (Integer) fComboBoxSite.getSelectedItem());
			fTextTest.setFont(font);
			configValue = font;
		}
		
		public void setItem(String key, ConfigurationItem<Font> item) {
			super.setItem(key, item);
			fListName.setSelectedValue(item.get().getFontName(), true);
			fComboBoxSite.setSelectedItem(item.get().getSize());
		}
	}
	
	private void loadEditors() {
		itemsEditor.put(String.class, new StringEditor());
		itemsEditor.put(Integer.class, new IntegerEditor());
		itemsEditor.put(Float.class, new FloatEditor());
		itemsEditor.put(Boolean.class, new BooleanEditor());
		itemsEditor.put(Level.class, new LevelEditor());
		itemsEditor.put(Color.class, new ColorEditor());
		itemsEditor.put(File.class, new FileEditor());
		itemsEditor.put(Font.class, new FontEditor());
	}
}