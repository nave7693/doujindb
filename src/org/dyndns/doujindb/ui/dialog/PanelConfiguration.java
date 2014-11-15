package org.dyndns.doujindb.ui.dialog;

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
	            TreeNodeEditor<?> editor = new TreeNodeEditor(key, itemsData.get(key));
	            setRightComponent(editor);
	    	}
	    });
		render = new TreeNodeRenderer();
		tree.setCellRenderer(render);
		super.setResizeWeight(1);
		super.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		super.setLeftComponent(new JScrollPane(tree));
		super.setRightComponent(null);
		super.setContinuousLayout(true);
	}
	
	public PanelConfiguration(Class<?> config) {
		this();
		parseConfiguration(config);
		initTreeNode();
	}
	
	public PanelConfiguration(Object config) {
		this();
		parseConfiguration(config);
		initTreeNode();
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
	
	private void initTreeNode() {
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
	
	private class TreeNodeEditor<T> extends JPanel implements LayoutManager
	{
		private JButton fButtonClose;
		private JLabel fLabelKey;
		private JLabel fLabelInfo;
//		private JComponent ${compontent};
		private JButton fButtonApply;
		private JButton fButtonDiscard;
		private JButton fButtonReset;
		
		private ConfigurationItem<T> configItem;
		private T configValue;
		
		public TreeNodeEditor(final String key, final ConfigurationItem<T> item) {
			super();
			super.setLayout(this);
			configItem = item;
			fLabelKey = new JLabel(key, Icon.window_tab_settings_tree_value, JLabel.LEFT);
			fLabelKey.setFont(Font);
			add(fLabelKey);
			fLabelInfo = new JLabel("<html>" + 
				"<body>" + 
				"<b>Type</b> : " + configItem.get().getClass().getName() + "<br/>" + 
				"<b>Info</b> : " + configItem.getInfo() + 
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
			fLabelKey.setBounds(1, 1, width - 21, 20);
			fButtonClose.setBounds(width - 21, 1, 20, 20);
			fLabelInfo.setBounds(1, 21, width - 2, 75);
//			${compontent}.setBounds(1, 100, width - 2, height - 120 - 65);
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
	}
}
