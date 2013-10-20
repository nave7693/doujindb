package org.dyndns.doujindb.ui.desk.panels;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import java.beans.*;
import java.util.Vector;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.log.Logger;
import org.dyndns.doujindb.ui.desk.*;

@SuppressWarnings("serial")
public final class PanelTrash extends JPanel implements DataBaseListener, LayoutManager, ListSelectionListener
{
	@SuppressWarnings("unused")
	private WindowEx m_ParentWindow;
	
	private JSplitPane m_SplitPane;
	private JLabel m_LabelInfo;
	private JLabel m_LabelCount;
	private JLabel m_LabelTask;
	private JButton m_ButtonRestore;
	private JButton m_ButtonDelete;
	private JButton m_ButtonEmpty;
	
	private DynamicPanel m_PanelFrame[] = new DynamicPanel[6];
	private JScrollPane m_ScrollPaneBase;
	private JPanel m_PanelBase;
	private JList<Artist> m_ListArtist;
	private JLabel m_LabelListArtist;
	private JList<Circle> m_ListCircle;
	private JLabel m_LabelListCircle;
	private JList<Book> m_ListBook;
	private JLabel m_LabelListBook;
	private JList<Convention> m_ListConvention;
	private JLabel m_LabelListConvention;
	private JList<Content> m_ListContent;
	private JLabel m_LabelListContent;
	private JList<Parody> m_ListParody;
	private JLabel m_LabelListParody;
	
	private DialogTrash m_PopupDialog = null;
	
	private static Logger Logger = Core.Logger;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public PanelTrash()
	{
		super();
		super.setLayout(this);
		JPanel panel1 = new JPanel();
		panel1.setLayout(null);
		panel1.setMaximumSize(new Dimension(130,130));
		panel1.setMinimumSize(new Dimension(130,130));
		m_LabelCount = new JLabel("Items : 0");
		m_LabelCount.setVerticalAlignment(JLabel.TOP);
		m_LabelCount.setFont(Core.Resources.Font);
		panel1.add(m_LabelCount);
		m_LabelInfo = new JLabel(" Info");
		m_LabelInfo.setOpaque(true);
		m_LabelInfo.setFont(Core.Resources.Font);
		panel1.add(m_LabelInfo);
		m_LabelTask = new JLabel(" Tasks");
		m_LabelTask.setOpaque(true);
		m_LabelTask.setFont(Core.Resources.Font);
		panel1.add(m_LabelTask);
		m_ButtonRestore = new JButton("Restore", Core.Resources.Icons.get("Frame/Trash/Restore"));
		m_ButtonRestore.setFocusable(false);
		m_ButtonRestore.setFont(Core.Resources.Font);
		m_ButtonRestore.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				if(m_ListArtist.getSelectedIndices().length == 0 &&
					m_ListBook.getSelectedIndices().length == 0 &&
					m_ListCircle.getSelectedIndices().length == 0 &&
					m_ListContent.getSelectedIndices().length == 0 &&
					m_ListConvention.getSelectedIndices().length == 0 &&
					m_ListParody.getSelectedIndices().length == 0)
				return;
				
				m_PopupDialog = new DialogTrash("<html>" +
					"<body>" +
					"Restore selected items from the Trash?<br/>" +
					"</body>" +
					"</html>", new SwingWorker<Void,Record>()
				{
					private DefaultListModel mArtist = (DefaultListModel) m_ListArtist.getModel();
					private DefaultListModel mBook = (DefaultListModel) m_ListBook.getModel();
					private DefaultListModel mCircle = (DefaultListModel) m_ListCircle.getModel();
					private DefaultListModel mContent = (DefaultListModel) m_ListContent.getModel();
					private DefaultListModel mConvention = (DefaultListModel) m_ListConvention.getModel();
					private DefaultListModel mParody = (DefaultListModel) m_ListParody.getModel();
								
					@Override
					protected Void doInBackground() throws Exception
					{
						try
						{
							int cSelected, cProcessed;
							Vector<Artist> artists = new Vector<Artist>();
							Vector<Book> books = new Vector<Book>();
							Vector<Circle> circles = new Vector<Circle>();
							Vector<Content> contents = new Vector<Content>();
							Vector<Convention> conventions = new Vector<Convention>();
							Vector<Parody> parodies = new Vector<Parody>();
							
							for(int index : m_ListArtist.getSelectedIndices())
								artists.add(m_ListArtist.getModel().getElementAt(index));
							for(int index : m_ListBook.getSelectedIndices())
								books.add(m_ListBook.getModel().getElementAt(index));
							for(int index : m_ListCircle.getSelectedIndices())
								circles.add(m_ListCircle.getModel().getElementAt(index));
							for(int index : m_ListContent.getSelectedIndices())
								contents.add(m_ListContent.getModel().getElementAt(index));
							for(int index : m_ListConvention.getSelectedIndices())
								conventions.add(m_ListConvention.getModel().getElementAt(index));
							for(int index : m_ListParody.getSelectedIndices())
								parodies.add(m_ListParody.getModel().getElementAt(index));
							
							cProcessed = 0;
							cSelected = artists.size() +
								books.size() +
								circles.size() +
								contents.size() +
								conventions.size() +
								parodies.size();
							
							for(Artist artist : artists)
							{
								if(super.isCancelled())
									break;
								artist.doRestore();
								super.setProgress(100 * ++cProcessed / cSelected);
								publish(artist);
							}
							for(Book book : books)
							{
								if(super.isCancelled())
									break;
								book.doRestore();
								super.setProgress(100 * ++cProcessed / cSelected);
								publish(book);
							}
							for(Circle circle : circles)
							{
								if(super.isCancelled())
									break;
								circle.doRestore();
								super.setProgress(100 * ++cProcessed / cSelected);
								publish(circle);
							}
							for(Content content : contents)
							{
								if(super.isCancelled())
									break;
								content.doRestore();
								super.setProgress(100 * ++cProcessed / cSelected);
								publish(content);
							}
							for(Convention convention : conventions)
							{
								if(super.isCancelled())
									break;
								convention.doRestore();
								super.setProgress(100 * ++cProcessed / cSelected);
								publish(convention);
							}
							for(Parody parody : parodies)
							{
								if(super.isCancelled())
									break;
								parody.doRestore();
								super.setProgress(100 * ++cProcessed / cSelected);
								publish(parody);
							}
							if(Core.Database.isAutocommit())
								Core.Database.doCommit();
						} catch(ArrayIndexOutOfBoundsException aioobe) {
							Logger.logError(aioobe.getMessage(), aioobe);
							aioobe.printStackTrace();
						} catch (DataBaseException dbe) {
							Logger.logError(dbe.getMessage(), dbe);
							dbe.printStackTrace();
						} catch (Exception e) {
							Logger.logError(e.getMessage(), e);
							e.printStackTrace();
						}
						return null;
					}
					@Override
					protected void process(java.util.List<Record> data)
					{
						for(final Record r : data)
						{
							if(r instanceof Artist)
								mArtist.removeElement(r);
							if(r instanceof Book)
								mBook.removeElement(r);
							if(r instanceof Circle)
								mCircle.removeElement(r);
							if(r instanceof Content)
								mContent.removeElement(r);
							if(r instanceof Convention)
								mConvention.removeElement(r);
							if(r instanceof Parody)
								mParody.removeElement(r);
						}
					}
					@Override
					protected void done()
					{
						loadData();
						m_PopupDialog.dispose();
					}
				});
			}			
		});
		panel1.add(m_ButtonRestore);
		m_ButtonDelete = new JButton("Delete", Core.Resources.Icons.get("Frame/Trash/Delete"));
		m_ButtonDelete.setFocusable(false);
		m_ButtonDelete.setFont(Core.Resources.Font);
		m_ButtonDelete.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				if(m_ListArtist.getSelectedIndices().length == 0 &&
					m_ListBook.getSelectedIndices().length == 0 &&
					m_ListCircle.getSelectedIndices().length == 0 &&
					m_ListContent.getSelectedIndices().length == 0 &&
					m_ListConvention.getSelectedIndices().length == 0 &&
					m_ListParody.getSelectedIndices().length == 0)
				return;
				
				m_PopupDialog = new DialogTrash("<html>" +
					"<body>" +
					"Delete selected items from the Trash?<br/>" +
					"<i>(This cannot be undone)</i>" +
					"</body>" +
					"</html>", new SwingWorker<Void,Record>()
				{
					private DefaultListModel mArtist = (DefaultListModel) m_ListArtist.getModel();
					private DefaultListModel mBook = (DefaultListModel) m_ListBook.getModel();
					private DefaultListModel mCircle = (DefaultListModel) m_ListCircle.getModel();
					private DefaultListModel mContent = (DefaultListModel) m_ListContent.getModel();
					private DefaultListModel mConvention = (DefaultListModel) m_ListConvention.getModel();
					private DefaultListModel mParody = (DefaultListModel) m_ListParody.getModel();
							
					@Override
					protected Void doInBackground() throws Exception
					{
						try
						{
							int cSelected, cProcessed;
							Vector<Artist> artists = new Vector<Artist>();
							Vector<Book> books = new Vector<Book>();
							Vector<Circle> circles = new Vector<Circle>();
							Vector<Content> contents = new Vector<Content>();
							Vector<Convention> conventions = new Vector<Convention>();
							Vector<Parody> parodies = new Vector<Parody>();
							
							for(int index : m_ListArtist.getSelectedIndices())
								artists.add(m_ListArtist.getModel().getElementAt(index));
							for(int index : m_ListBook.getSelectedIndices())
								books.add(m_ListBook.getModel().getElementAt(index));
							for(int index : m_ListCircle.getSelectedIndices())
								circles.add(m_ListCircle.getModel().getElementAt(index));
							for(int index : m_ListContent.getSelectedIndices())
								contents.add(m_ListContent.getModel().getElementAt(index));
							for(int index : m_ListConvention.getSelectedIndices())
								conventions.add(m_ListConvention.getModel().getElementAt(index));
							for(int index : m_ListParody.getSelectedIndices())
								parodies.add(m_ListParody.getModel().getElementAt(index));
							
							cProcessed = 0;
							cSelected = artists.size() +
								books.size() +
								circles.size() +
								contents.size() +
								conventions.size() +
								parodies.size();
							
							for(Artist artist : artists)
							{
								if(super.isCancelled())
									break;
								artist.removeAll();
								Core.Database.doDelete(artist);
								super.setProgress(100 * ++cProcessed / cSelected);
								publish(artist);
							}
							for(Book book : books)
							{
								if(super.isCancelled())
									break;
								book.removeAll();
								Core.Database.doDelete(book);
								super.setProgress(100 * ++cProcessed / cSelected);
								publish(book);
							}
							for(Circle circle : circles)
							{
								if(super.isCancelled())
									break;
								circle.removeAll();
								Core.Database.doDelete(circle);
								super.setProgress(100 * ++cProcessed / cSelected);
								publish(circle);
							}
							for(Content content : contents)
							{
								if(super.isCancelled())
									break;
								content.removeAll();
								Core.Database.doDelete(content);
								super.setProgress(100 * ++cProcessed / cSelected);
								publish(content);
							}
							for(Convention convention : conventions)
							{
								if(super.isCancelled())
									break;
								convention.removeAll();
								Core.Database.doDelete(convention);
								super.setProgress(100 * ++cProcessed / cSelected);
								publish(convention);
							}
							for(Parody parody : parodies)
							{
								if(super.isCancelled())
									break;
								parody.removeAll();
								Core.Database.doDelete(parody);
								super.setProgress(100 * ++cProcessed / cSelected);
								publish(parody);
							}
							if(Core.Database.isAutocommit())
								Core.Database.doCommit();
						} catch(ArrayIndexOutOfBoundsException aioobe) {
							Logger.logError(aioobe.getMessage(), aioobe);
							aioobe.printStackTrace();
						} catch (DataBaseException dbe) {
							Logger.logError(dbe.getMessage(), dbe);
							dbe.printStackTrace();
						} catch (Exception e) {
							Logger.logError(e.getMessage(), e);
							e.printStackTrace();
						}
						return null;
					}
					@Override
					protected void process(java.util.List<Record> data)
					{
						for(final Record r : data)
						{
							if(r instanceof Artist)
								mArtist.removeElement(r);
							if(r instanceof Book)
								mBook.removeElement(r);
							if(r instanceof Circle)
								mCircle.removeElement(r);
							if(r instanceof Content)
								mContent.removeElement(r);
							if(r instanceof Convention)
								mConvention.removeElement(r);
							if(r instanceof Parody)
								mParody.removeElement(r);
						}
					}
					@Override
					protected void done()
					{
						loadData();
						m_PopupDialog.dispose();
					}
				});
			}			
		});
		panel1.add(m_ButtonDelete);
		m_ButtonEmpty = new JButton("Empty", Core.Resources.Icons.get("Frame/Trash/Empty"));
		m_ButtonEmpty.setFocusable(false);
		m_ButtonEmpty.setFont(Core.Resources.Font);
		m_ButtonEmpty.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				if(m_ListArtist.getModel().getSize() +
					m_ListBook.getModel().getSize() +
					m_ListCircle.getModel().getSize() +
					m_ListContent.getModel().getSize() +
					m_ListConvention.getModel().getSize() +
					m_ListParody.getModel().getSize() == 0)
				return;
				
				m_PopupDialog = new DialogTrash("<html>" +
					"<body>" +
					"Empty all of the items from the Trash?<br/>" +
					"<i>(This cannot be undone)</i>" +
					"</body>" +
					"</html>", new SwingWorker<Void,Record>()
				{
					private DefaultListModel mArtist = (DefaultListModel) m_ListArtist.getModel();
					private DefaultListModel mBook = (DefaultListModel) m_ListBook.getModel();
					private DefaultListModel mCircle = (DefaultListModel) m_ListCircle.getModel();
					private DefaultListModel mContent = (DefaultListModel) m_ListContent.getModel();
					private DefaultListModel mConvention = (DefaultListModel) m_ListConvention.getModel();
					private DefaultListModel mParody = (DefaultListModel) m_ListParody.getModel();
								
					@Override
					protected Void doInBackground() throws Exception
					{
						try
						{
							int cSelected, cProcessed;
							Vector<Artist> artists = new Vector<Artist>();
							Vector<Book> books = new Vector<Book>();
							Vector<Circle> circles = new Vector<Circle>();
							Vector<Content> contents = new Vector<Content>();
							Vector<Convention> conventions = new Vector<Convention>();
							Vector<Parody> parodies = new Vector<Parody>();
							
							for(int i=0;i<m_ListArtist.getModel().getSize();i++)
								artists.add(m_ListArtist.getModel().getElementAt(i));
							for(int i=0;i<m_ListBook.getModel().getSize();i++)
								books.add(m_ListBook.getModel().getElementAt(i));
							for(int i=0;i<m_ListCircle.getModel().getSize();i++)
								circles.add(m_ListCircle.getModel().getElementAt(i));
							for(int i=0;i<m_ListContent.getModel().getSize();i++)
								contents.add(m_ListContent.getModel().getElementAt(i));
							for(int i=0;i<m_ListConvention.getModel().getSize();i++)
								conventions.add(m_ListConvention.getModel().getElementAt(i));
							for(int i=0;i<m_ListParody.getModel().getSize();i++)
								parodies.add(m_ListParody.getModel().getElementAt(i));
							
							cProcessed = 0;
							cSelected = artists.size() +
								books.size() +
								circles.size() +
								contents.size() +
								conventions.size() +
								parodies.size();
							
							for(Artist artist : artists)
							{
								if(super.isCancelled())
									break;
								artist.removeAll();
								Core.Database.doDelete(artist);
								super.setProgress(100 * ++cProcessed / cSelected);
								publish(artist);
							}
							for(Book book : books)
							{
								if(super.isCancelled())
									break;
								book.removeAll();
								Core.Database.doDelete(book);
								super.setProgress(100 * ++cProcessed / cSelected);
								publish(book);
							}
							for(Circle circle : circles)
							{
								if(super.isCancelled())
									break;
								circle.removeAll();
								Core.Database.doDelete(circle);
								super.setProgress(100 * ++cProcessed / cSelected);
								publish(circle);
							}
							for(Content content : contents)
							{
								if(super.isCancelled())
									break;
								content.removeAll();
								Core.Database.doDelete(content);
								super.setProgress(100 * ++cProcessed / cSelected);
								publish(content);
							}
							for(Convention convention : conventions)
							{
								if(super.isCancelled())
									break;
								convention.removeAll();
								Core.Database.doDelete(convention);
								super.setProgress(100 * ++cProcessed / cSelected);
								publish(convention);
							}
							for(Parody parody : parodies)
							{
								if(super.isCancelled())
									break;
								parody.removeAll();
								Core.Database.doDelete(parody);
								super.setProgress(100 * ++cProcessed / cSelected);
								publish(parody);
							}
							if(Core.Database.isAutocommit())
								Core.Database.doCommit();
						} catch(ArrayIndexOutOfBoundsException aioobe) {
							Logger.logError(aioobe.getMessage(), aioobe);
							aioobe.printStackTrace();
						} catch (DataBaseException dbe) {
							Logger.logError(dbe.getMessage(), dbe);
							dbe.printStackTrace();
						} catch (Exception e) {
							Logger.logError(e.getMessage(), e);
							e.printStackTrace();
						}
						return null;
					}
					@Override
					protected void process(java.util.List<Record> data)
					{
						for(final Record r : data)
						{
							if(r instanceof Artist)
								mArtist.removeElement(r);
							if(r instanceof Book)
								mBook.removeElement(r);
							if(r instanceof Circle)
								mCircle.removeElement(r);
							if(r instanceof Content)
								mContent.removeElement(r);
							if(r instanceof Convention)
								mConvention.removeElement(r);
							if(r instanceof Parody)
								mParody.removeElement(r);
						}
					}
					@Override
					protected void done()
					{
						loadData();
						m_PopupDialog.dispose();
					}
				});
			}			
		});
		panel1.add(m_ButtonEmpty);
		JSplitPane splitListA = new JSplitPane();
		JSplitPane splitListB = new JSplitPane();
		JSplitPane splitListC = new JSplitPane();
		JSplitPane splitListE = new JSplitPane();
		JSplitPane splitListT = new JSplitPane();
		JSplitPane splitListP = new JSplitPane();
		Color foreground = Core.Properties.get("org.dyndns.doujindb.ui.theme.color").asColor();
		Color background = (Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor()).darker();
		{
			m_ListArtist = new JList<Artist>();
			m_ListArtist.setModel(new DefaultListModel());
			m_ListArtist.setBackground(background);
			m_ListArtist.setForeground(foreground);
			m_ListArtist.setSelectionBackground(foreground);
			m_ListArtist.setSelectionForeground(background);
			m_ListArtist.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			splitListA.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitListA.setBottomComponent(new JScrollPane(m_ListArtist));
			splitListA.setDividerSize(0);
			splitListA.setEnabled(false);	
		}
		{
			m_ListBook = new JList<Book>();
			m_ListBook.setModel(new DefaultListModel());
			m_ListBook.setBackground(background);
			m_ListBook.setForeground(foreground);
			m_ListBook.setSelectionBackground(foreground);
			m_ListBook.setSelectionForeground(background);
			m_ListBook.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			splitListB.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitListB.setBottomComponent(new JScrollPane(m_ListBook));
			splitListB.setDividerSize(0);
			splitListB.setEnabled(false);	
		}
		{
			m_ListCircle = new JList<Circle>();
			m_ListCircle.setModel(new DefaultListModel());
			m_ListCircle.setBackground(background);
			m_ListCircle.setForeground(foreground);
			m_ListCircle.setSelectionBackground(foreground);
			m_ListCircle.setSelectionForeground(background);
			m_ListCircle.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			splitListC.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitListC.setBottomComponent(new JScrollPane(m_ListCircle));
			splitListC.setDividerSize(0);
			splitListC.setEnabled(false);	
		}
		{
			m_ListConvention = new JList<Convention>();
			m_ListConvention.setModel(new DefaultListModel());
			m_ListConvention.setBackground(background);
			m_ListConvention.setForeground(foreground);
			m_ListConvention.setSelectionBackground(foreground);
			m_ListConvention.setSelectionForeground(background);
			m_ListConvention.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			splitListE.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitListE.setBottomComponent(new JScrollPane(m_ListConvention));
			splitListE.setDividerSize(0);
			splitListE.setEnabled(false);	
		}
		{
			m_ListContent = new JList<Content>();
			m_ListContent.setModel(new DefaultListModel());
			m_ListContent.setBackground(background);
			m_ListContent.setForeground(foreground);
			m_ListContent.setSelectionBackground(foreground);
			m_ListContent.setSelectionForeground(background);
			m_ListContent.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			splitListT.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitListT.setBottomComponent(new JScrollPane(m_ListContent));
			splitListT.setDividerSize(0);
			splitListT.setEnabled(false);	
		}
		{
			m_ListParody = new JList<Parody>();
			m_ListParody.setModel(new DefaultListModel());
			m_ListParody.setBackground(background);
			m_ListParody.setForeground(foreground);
			m_ListParody.setSelectionBackground(foreground);
			m_ListParody.setSelectionForeground(background);
			m_ListParody.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			splitListP.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitListP.setBottomComponent(new JScrollPane(m_ListParody));
			splitListP.setDividerSize(0);
			splitListP.setEnabled(false);	
		}
		new SwingWorker<Void,Record>()
		{
			private DefaultListModel mArtist = (DefaultListModel) m_ListArtist.getModel();
			private DefaultListModel mBook = (DefaultListModel) m_ListBook.getModel();
			private DefaultListModel mCircle = (DefaultListModel) m_ListCircle.getModel();
			private DefaultListModel mContent = (DefaultListModel) m_ListContent.getModel();
			private DefaultListModel mConvention = (DefaultListModel) m_ListConvention.getModel();
			private DefaultListModel mParody = (DefaultListModel) m_ListParody.getModel();
			
			@Override
			protected Void doInBackground() throws Exception
			{
				try
				{
					for(Record record : Core.Database.getRecycled())
					{
						if(record instanceof Artist)
						{
							publish(record);
							continue;
						}
						if(record instanceof Book)
						{
							publish(record);
							continue;
						}
						if(record instanceof Circle)
						{
							publish(record);
							continue;
						}
						if(record instanceof Convention)
						{
							publish(record);
							continue;
						}
						if(record instanceof Content)
						{
							publish(record);
							continue;
						}
						if(record instanceof Parody)
						{
							publish(record);
							continue;
						}
					}
				} catch(ArrayIndexOutOfBoundsException aioobe) {
					Logger.logError(aioobe.getMessage(), aioobe);
					aioobe.printStackTrace();
				} catch (DataBaseException dbe) {
					Logger.logError(dbe.getMessage(), dbe);
					dbe.printStackTrace();
				} catch (Exception e) {
					Logger.logError(e.getMessage(), e);
					e.printStackTrace();
				}
				return null;
			}
			@Override
			protected void process(java.util.List<Record> data)
			{
				for(final Record r : data)
				{
					if(r instanceof Artist)
						mArtist.addElement(r);
					if(r instanceof Book)
						mBook.addElement(r);
					if(r instanceof Circle)
						mCircle.addElement(r);
					if(r instanceof Content)
						mContent.addElement(r);
					if(r instanceof Convention)
						mConvention.addElement(r);
					if(r instanceof Parody)
						mParody.addElement(r);
				}
			}
			@Override
			protected void done()
			{
				loadData();
			}
		}.execute();
		m_PanelBase = new JPanel();
		m_PanelBase.setLayout(new LayoutManager()
		{
			@Override
			public void layoutContainer(Container parent)
			{
				int width = parent.getWidth();
				int posy = 0;
				m_PanelFrame[0].setBounds(0, posy, width, m_PanelFrame[0].getHeight());
				posy += m_PanelFrame[0].getHeight();
				m_PanelFrame[1].setBounds(0, posy, width, m_PanelFrame[1].getHeight());
				posy += m_PanelFrame[1].getHeight();
				m_PanelFrame[2].setBounds(0, posy, width, m_PanelFrame[2].getHeight());
				posy += m_PanelFrame[2].getHeight();
				m_PanelFrame[3].setBounds(0, posy, width, m_PanelFrame[3].getHeight());
				posy += m_PanelFrame[3].getHeight();
				m_PanelFrame[4].setBounds(0, posy, width, m_PanelFrame[4].getHeight());
				posy += m_PanelFrame[4].getHeight();
				m_PanelFrame[5].setBounds(0, posy, width, m_PanelFrame[5].getHeight());
				m_PanelBase.setPreferredSize(new Dimension(250,
					m_PanelFrame[0].getHeight() +
					m_PanelFrame[1].getHeight() +
					m_PanelFrame[2].getHeight() +
					m_PanelFrame[3].getHeight() +
					m_PanelFrame[4].getHeight() +
					m_PanelFrame[5].getHeight()));
				m_ScrollPaneBase.doLayout();
			}
			@Override
			public void addLayoutComponent(String key,Component c) {}
			@Override
			public void removeLayoutComponent(Component c) {}
			@Override
			public Dimension minimumLayoutSize(Container parent)
			{
			     return parent.getMinimumSize();
			}
			@Override
			public Dimension preferredLayoutSize(Container parent)
			{
			     return new Dimension(250,
			    	m_PanelFrame[0].getHeight() +
					m_PanelFrame[1].getHeight() +
					m_PanelFrame[2].getHeight() +
					m_PanelFrame[3].getHeight() +
					m_PanelFrame[4].getHeight() +
					m_PanelFrame[5].getHeight());
			}
		});
		m_LabelListArtist = new JLabel("Artists", Core.Resources.Icons.get("Desktop/Explorer/Artist"), JLabel.LEFT);
		m_PanelFrame[0] = new DynamicPanel(m_LabelListArtist, splitListA, m_PanelBase);
		m_LabelListBook = new JLabel("Books", Core.Resources.Icons.get("Desktop/Explorer/Book"), JLabel.LEFT);
		m_PanelFrame[1] = new DynamicPanel(m_LabelListBook, splitListB, m_PanelBase);
		m_LabelListCircle = new JLabel("Circles", Core.Resources.Icons.get("Desktop/Explorer/Circle"), JLabel.LEFT);
		m_PanelFrame[2] = new DynamicPanel(m_LabelListCircle, splitListC, m_PanelBase);
		m_LabelListConvention = new JLabel("Conventions", Core.Resources.Icons.get("Desktop/Explorer/Convention"), JLabel.LEFT);
		m_PanelFrame[3] = new DynamicPanel(m_LabelListConvention, splitListE, m_PanelBase);
		m_LabelListContent = new JLabel("Contents", Core.Resources.Icons.get("Desktop/Explorer/Content"), JLabel.LEFT);
		m_PanelFrame[4] = new DynamicPanel(m_LabelListContent, splitListT, m_PanelBase);
		m_LabelListParody = new JLabel("Parodies", Core.Resources.Icons.get("Desktop/Explorer/Parody"), JLabel.LEFT);
		m_PanelFrame[5] = new DynamicPanel(m_LabelListParody, splitListP, m_PanelBase);
		for(DynamicPanel panel : m_PanelFrame)
			m_PanelBase.add(panel);
		m_ScrollPaneBase = new JScrollPane(m_PanelBase);
		m_SplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel1, m_ScrollPaneBase);
		m_SplitPane.setDividerSize(1);
		m_SplitPane.setEnabled(false);
		super.add(m_SplitPane);
		;
		m_PanelBase.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent me)
			{
				checkPopup(me);
			}
			@Override
			public void mousePressed(MouseEvent me) {
				checkPopup(me);
			}

			@Override
			public void mouseReleased(MouseEvent me) {
				checkPopup(me);
			}

			@Override
			public void mouseEntered(MouseEvent me) {
				checkPopup(me);
			}

			@Override
			public void mouseExited(MouseEvent me) {
				checkPopup(me);
			}
			private void checkPopup(MouseEvent me)
			{
				if(!me.isPopupTrigger())
					return;
				
				JPopupMenu popupMenu = new JPopupMenu();
	    		JMenuItem menuItem;
	    		menuItem = new JMenuItem("Select All", Core.Resources.Icons.get("Frame/Trash/SelectAll"));
	    		menuItem.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae)
					{
						m_ListArtist.setSelectionInterval(0, m_ListArtist.getModel().getSize() - 1);
						m_ListBook.setSelectionInterval(0, m_ListBook.getModel().getSize() - 1);
						m_ListCircle.setSelectionInterval(0, m_ListCircle.getModel().getSize() - 1);
						m_ListContent.setSelectionInterval(0, m_ListContent.getModel().getSize() - 1);
						m_ListConvention.setSelectionInterval(0, m_ListConvention.getModel().getSize() - 1);
						m_ListParody.setSelectionInterval(0, m_ListParody.getModel().getSize() - 1);
						loadData();
					}
				});
	    		menuItem.setName("select-all");
				menuItem.setActionCommand("select-all");
				popupMenu.add(menuItem);
				menuItem = new JMenuItem("Deselect All", Core.Resources.Icons.get("Frame/Trash/DeselectAll"));
	    		menuItem.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae)
					{
						m_ListArtist.clearSelection();
						m_ListBook.clearSelection();
						m_ListCircle.clearSelection();
						m_ListContent.clearSelection();
						m_ListConvention.clearSelection();
						m_ListParody.clearSelection();
						loadData();
					}
				});
	    		menuItem.setName("deselect-all");
				menuItem.setActionCommand("deselect-all");
				popupMenu.add(menuItem);
				popupMenu.show(me.getComponent(), me.getX(), me.getY());
			}
		});
		;
		m_ListArtist.addListSelectionListener(this);
		m_ListBook.addListSelectionListener(this);
		m_ListCircle.addListSelectionListener(this);
		m_ListConvention.addListSelectionListener(this);
		m_ListContent.addListSelectionListener(this);
		m_ListParody.addListSelectionListener(this);
		;
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				m_SplitPane.revalidate();
				loadData();
			}
		});
	}
	
	@Override
	public void layoutContainer(Container parent)
	{
		int width = parent.getWidth(),
		height = parent.getHeight();
		m_LabelInfo.setBounds(0,0,130,20);
		m_LabelCount.setBounds(2,22,125,55);
		m_LabelTask.setBounds(0,75+5,130,20);
		m_ButtonRestore.setBounds(3,75+25+1,125,20);
		m_ButtonDelete.setBounds(3,75+45+2,125,20);
		m_ButtonEmpty.setBounds(3,75+65+2,125,20);
		m_SplitPane.setBounds(0, 0, width,  height);
	}
	
	@Override
	public void addLayoutComponent(String key,Component c) {}
	
	@Override
	public void removeLayoutComponent(Component c) {}
	
	@Override
	public Dimension minimumLayoutSize(Container parent)
	{
	     return parent.getMinimumSize();
	}
	
	@Override
	public Dimension preferredLayoutSize(Container parent)
	{
	     return parent.getPreferredSize();
	}
	
	private void loadData()
	{
		long count;
		try {
			count = Core.Database.getRecycled().size();
			m_LabelCount.setText((count==1)?("Item : 1"):("Items : "+count));
		} catch (DataBaseException dbe) {
			Logger.logError(dbe.getMessage(), dbe);
			dbe.printStackTrace();
		}
		m_LabelListArtist.setText("Artists (" + (m_ListArtist.getSelectedIndices().length) + "/" + m_ListArtist.getModel().getSize() + ")");
		m_LabelListBook.setText("Books (" + (m_ListBook.getSelectedIndices().length) + "/" + m_ListBook.getModel().getSize() + ")");
		m_LabelListCircle.setText("Circles (" + (m_ListCircle.getSelectedIndices().length) + "/" + m_ListCircle.getModel().getSize() + ")");
		m_LabelListConvention.setText("Conventions (" + (m_ListConvention.getSelectedIndices().length) + "/" + m_ListConvention.getModel().getSize() + ")");
		m_LabelListContent.setText("Contents (" + (m_ListContent.getSelectedIndices().length) + "/" + m_ListContent.getModel().getSize() + ")");
		m_LabelListParody.setText("Parodies (" + (m_ListParody.getSelectedIndices().length) + "/" + m_ListParody.getModel().getSize() + ")");
	}
	
	private final class DynamicPanel extends JPanel implements LayoutManager, ActionListener
	{
		private JLabel m_LabelTitle;
		private Component m_BodyComponent;
		private JButton m_ButtonToggle;
		private Component m_ParentComponent;
		
		private int STATUS;
		private final int STATUS_MINIMIZED = 0x1;
		private final int STATUS_MAXIMIZED = 0x2;
		
		private ImageIcon ICON_CHECKED = Core.Resources.Icons.get("JPanel/ToggleButton/Checked");
		private ImageIcon ICON_UNCHECKED = Core.Resources.Icons.get("JPanel/ToggleButton/Unchecked");
		
		public DynamicPanel(JLabel title, Component body, Component parent)
		{
			super();
			setLayout(this);
			STATUS = STATUS_MINIMIZED;
			setSize(100, 21);
			setMinimumSize(new Dimension(100, 21));
			setPreferredSize(new Dimension(250, 250));
			setMaximumSize(new Dimension(1280, 250));
			m_ParentComponent = parent;
			m_LabelTitle = title;
			add(m_LabelTitle);
			m_BodyComponent = body;
			add(m_BodyComponent);
			m_ButtonToggle = new JButton(ICON_CHECKED);
			m_ButtonToggle.setSelected(true);
			m_ButtonToggle.addActionListener(this);
			add(m_ButtonToggle);
		}

		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
			height = parent.getHeight();
			m_ButtonToggle.setBounds(width - 20, 0, 20, 20);
			m_LabelTitle.setBounds(0, 0, width - 20, 20);
			m_BodyComponent.setBounds(0, 20, width, height - 20);
		}
		
		@Override
		public void addLayoutComponent(String key,Component c) {}
		
		@Override
		public void removeLayoutComponent(Component c) {}
		
		@Override
		public Dimension minimumLayoutSize(Container parent)
		{
		    return new Dimension(0, 20);
		}
		
		@Override
		public Dimension preferredLayoutSize(Container parent)
		{
		    return new Dimension(250, 250);
		}
		
		@Override
		public void actionPerformed(ActionEvent ae)
		{
			if(STATUS == STATUS_MAXIMIZED)
			{
				STATUS = STATUS_MINIMIZED;
				m_ButtonToggle.setIcon(ICON_CHECKED);
				setSize(new Dimension(getWidth(), (int)getMinimumSize().getHeight()));
				m_ParentComponent.doLayout();
				m_ParentComponent.validate();
			} else {
				STATUS = STATUS_MAXIMIZED;
				m_ButtonToggle.setIcon(ICON_UNCHECKED);
				setSize(new Dimension(getWidth(), (int)getMaximumSize().getHeight()));
				m_ParentComponent.doLayout();
				m_ParentComponent.validate();
			}
		}
	}

	private final class DialogTrash extends JInternalFrame implements LayoutManager
	{
		private JComponent m_GlassPane = (JComponent) ((RootPaneContainer) PanelTrash.this.getRootPane().getParent()).getGlassPane();
		private JComponent m_Component;
		private JLabel m_LabelMessage;
		private JButton m_ButtonOk;
		private JButton m_ButtonCancel;
		private JProgressBar m_ProgressBar;
		
		private SwingWorker<?,?> m_Worker;
		
		public DialogTrash(String message, SwingWorker<?,?> worker)
		{
			super();
			super.setFrameIcon(Core.Resources.Icons.get("Desktop/Explorer/Trash"));
			super.setTitle("Trash");
			super.setMaximizable(false);
			super.setIconifiable(false);
			super.setResizable(false);
			super.setClosable(false);
			super.setPreferredSize(new Dimension(300, 150));
			super.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
			super.addInternalFrameListener(new InternalFrameAdapter()
			{
				@Override
				public void internalFrameClosed(InternalFrameEvent ife)
				{
					hideDialog();
				}

				@Override
				public void internalFrameClosing(InternalFrameEvent ife)
				{
					hideDialog();
				}
			});
			
			m_Worker = worker;
			m_Worker.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					if ("progress".equals(evt.getPropertyName())) {
						m_ProgressBar.setValue((Integer) evt.getNewValue());
						return;
					}
				}
			});
			
			m_Component = new JPanel();
			m_Component.setSize(250, 150);
			m_Component.setLayout(new GridLayout(3, 1));
			m_LabelMessage = new JLabel(message);
			m_LabelMessage.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			m_LabelMessage.setFont(Core.Resources.Font);
			m_LabelMessage.setVerticalAlignment(JLabel.CENTER);
			m_LabelMessage.setHorizontalAlignment(JLabel.CENTER);
			m_Component.add(m_LabelMessage);
			
			m_ProgressBar = new JProgressBar();
			m_ProgressBar.setValue(0);
			m_ProgressBar.setMinimum(0);
			m_ProgressBar.setMaximum(100);
			m_ProgressBar.setStringPainted(true);
			m_ProgressBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			m_Component.add(m_ProgressBar);
			
			JPanel bottomPanel = new JPanel();
			bottomPanel.setLayout(new GridLayout(1, 2));
			m_ButtonCancel = new JButton("Cancel");
			m_ButtonCancel.setFont(Core.Resources.Font);
			m_ButtonCancel.setMnemonic('C');
			m_ButtonCancel.setFocusable(false);
			m_ButtonCancel.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					m_Worker.cancel(true);
					dispose();
				}					
			});
			m_ButtonOk = new JButton("Ok");
			m_ButtonOk.setFont(Core.Resources.Font);
			m_ButtonOk.setMnemonic('O');
			m_ButtonOk.setFocusable(false);
			m_ButtonOk.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					m_ButtonOk.setEnabled(false);
					m_Worker.execute();
				}					
			});
			bottomPanel.add(m_ButtonOk);
			bottomPanel.add(m_ButtonCancel);
			m_Component.add(bottomPanel);
			super.add(m_Component);
			super.setVisible(true);
			
			showDialog();
		}
		
		
		private void showDialog()
		{
			m_GlassPane.add(this);
			m_GlassPane.setEnabled(true);
			m_GlassPane.setVisible(true);
			m_GlassPane.setEnabled(false);
			Dimension size = super.getPreferredSize();
			int x = (int) (m_GlassPane.getWidth() - size.getWidth()) / 2;
			int y = (int) (m_GlassPane.getHeight() - size.getHeight()) / 2;
			setBounds(x, y, (int) size.getWidth(), (int) size.getHeight());
			try {
				setSelected(true);
			} catch (PropertyVetoException pve) {
				pve.printStackTrace();
			}
		}
		
		private void hideDialog()
		{
			m_GlassPane.remove(this);
			m_GlassPane.setEnabled(true);
			m_GlassPane.setVisible(false);
			m_GlassPane.setEnabled(false);
		}

		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			m_Component.setBounds(0, 0, width, height);
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
	
	@Override
	public void recordAdded(Record rcd) {}
	
	@Override
	public void recordDeleted(Record rcd) { }
	
	@Override
	public void recordUpdated(Record rcd, UpdateData data) { }
	
	@SuppressWarnings({"unchecked","rawtypes"})
	@Override
	public void recordRecycled(Record rcd)
	{
		if(rcd instanceof Artist)
		{
			DefaultListModel model = (DefaultListModel)m_ListArtist.getModel();
			model.add(0, rcd);
			loadData();
			return;
		}
		if(rcd instanceof Book)
		{
			DefaultListModel model = (DefaultListModel)m_ListBook.getModel();
			model.add(0, rcd);
			loadData();
			return;
		}
		if(rcd instanceof Circle)
		{
			DefaultListModel model = (DefaultListModel)m_ListCircle.getModel();
			model.add(0, rcd);
			loadData();
			return;
		}
		if(rcd instanceof Content)
		{
			DefaultListModel model = (DefaultListModel)m_ListContent.getModel();
			model.add(0, rcd);
			loadData();
			return;
		}
		if(rcd instanceof Convention)
		{
			DefaultListModel model = (DefaultListModel)m_ListConvention.getModel();
			model.add(0, rcd);
			loadData();
			return;
		}
		if(rcd instanceof Parody)
		{
			DefaultListModel model = (DefaultListModel)m_ListParody.getModel();
			model.add(0, rcd);
			loadData();
			return;
		}
	}

	@Override
	public void recordRestored(Record rcd) { }
	
	@Override
	public void databaseConnected() { }
	
	@Override
	public void databaseDisconnected() { }
	
	@Override
	public void databaseCommit() { }
	
	@Override
	public void databaseRollback() { }

	@Override
	public void valueChanged(ListSelectionEvent lse) { }
}