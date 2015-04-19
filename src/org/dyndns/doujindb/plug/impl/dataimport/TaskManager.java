package org.dyndns.doujindb.plug.impl.dataimport;

import java.awt.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import org.dyndns.doujindb.dat.*;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.*;
import org.dyndns.doujindb.db.record.*;
import org.dyndns.doujindb.db.record.Book.*;
import org.dyndns.doujindb.plug.impl.dataimport.Task.Duplicate.Option;
import org.dyndns.doujindb.plug.impl.dataimport.Task.State;
import org.dyndns.doujindb.plug.impl.imagesearch.ImageSearch;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.WindowEx;
import org.dyndns.doujindb.util.*;

final class TaskManager
{
	private java.util.List<Task> mTaskSet = new Vector<Task>();
	private final File mTaskFile;
	private final File mTmpDir;
	private Worker mWorker = new Worker();
	private static final CopyOnWriteArraySet<TaskListener> mListeners = new CopyOnWriteArraySet<TaskListener>();
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(TaskManager.class);
	
	TaskManager(final File homeDir) {
		mTaskFile = new File(homeDir, "tasks.xml");
		mTmpDir = new File(homeDir, "tmp");
		mTmpDir.mkdirs();
	}
	
	public void save() {
		LOG.debug("call save()");
		FileOutputStream out = null;
		try
		{
			TaskSet set = new TaskSet();
			set.tasks.addAll(mTaskSet);
			out = new FileOutputStream(mTaskFile);
			JAXBContext context = JAXBContext.newInstance(TaskSet.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
			m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");
			m.marshal(set, out);
			LOG.debug("Tasks saved to {}", mTaskFile);
		} catch (NullPointerException | JAXBException | FileNotFoundException e) {
			LOG.error("Error saving tasks to {}", mTaskFile, e);
		} finally {
			try { out.close(); } catch (Exception e) { }
		}
	}
	
	public void save(Task task, File file) {
		LOG.debug("call save({}, {})", task, file);
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(file);
			JAXBContext context = JAXBContext.newInstance(Task.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
			m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");
			m.marshal(task, out);
			LOG.debug("Task Id={} saved to {}", task.getId(), file);
		} catch (NullPointerException | JAXBException | FileNotFoundException e) {
			LOG.error("Error saving task Id={} to {}", new Object[]{task.getId(), file, e});
		} finally {
			try { out.close(); } catch (Exception e) { }
		}
	}
	
	public void load() {
		LOG.debug("call load()");
		synchronized(mTaskSet) {
			mTaskSet = new Vector<Task>();
			FileInputStream in = null;
			try
			{
				in = new FileInputStream(mTaskFile);
				JAXBContext context = JAXBContext.newInstance(TaskSet.class);
				Unmarshaller um = context.createUnmarshaller();
				TaskSet set = (TaskSet) um.unmarshal(in);
				mTaskSet.addAll(set.tasks);
				LOG.debug("Tasks loaded from {}", mTaskFile);
			} catch (NullPointerException | JAXBException | FileNotFoundException e) {
				LOG.error("Error loading tasks from {}", mTaskFile, e);
			} finally {
				try { in.close(); } catch (Exception e) { }
			}
		}
		fireTaskmanagerChanged();
	}
	
	public int size() {
		return mTaskSet.size();
	}
	
	public void add(File file) {
		LOG.debug("call add({})", file);
		synchronized(mTaskSet) {
			// Get unique ID
			String uuid = java.util.UUID.randomUUID().toString();
			while(mTaskSet.contains(uuid))
				uuid = java.util.UUID.randomUUID().toString();
			Task newTask = new Task(uuid, file, new File(mTmpDir, uuid + ".png"));
			mTaskSet.add(newTask);
		}
		fireTaskmanagerChanged();
	}
	
	public void remove(Task task) {
		LOG.debug("call remove({})", task);
		synchronized(mTaskSet) {
			mTaskSet.remove(task);
		}
		fireTaskmanagerChanged();
	}
	
	public void reset(Task task) {
		LOG.debug("call reset({})", task);
		synchronized(mTaskSet) {
			if(!contains(task))
				return;
			task.reset();
		}
	}
	
	public boolean contains(Task task) {
		return mTaskSet.contains(task);
	}
	
	public boolean contains(String taskid) {
		return mTaskSet.contains(taskid);
	}

	public Iterable<Task> tasks() {
		return mTaskSet;
	}
	
	public Task get(int index) {
		return mTaskSet.get(index);
	}
	
	public Task getRunningTask() {
		if(!isRunning())
			return null;
		return mWorker.mTask;
	}
	
	public void addTaskListener(TaskListener listener) {
		LOG.debug("call addTaskListener({})", listener);
		synchronized(mListeners) {
			if(!mListeners.contains(listener))
				mListeners.add(listener);
		}
	}
	
	public void removeTaskListener(TaskListener listener) {
		LOG.debug("call removeTaskListener({})", listener);
		synchronized(mListeners) {
			mListeners.remove(listener);
		}
	}
	
	public void start() {
		LOG.debug("call start()");
		Thread thread = new Thread(mWorker);
		thread.setName("plugin-dataimport-taskmanager");
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}
	
	public void stop() {
		LOG.debug("call stop()");
		//TODO
	}
	
	public void pause() {
		LOG.debug("call pause()");
		if(!mWorker.isPaused()) {
			mWorker.pause();
			fireTaskmanagerChanged();
			LOG.info("Worker paused");
		}
	}
	
	public void resume() {
		LOG.debug("call resume()");
		if(mWorker.isPaused()) {
			mWorker.resume();
			fireTaskmanagerChanged();
			LOG.info("Worker resumed");
		}
	}
	
	public boolean isRunning() {
		return !mWorker.isPaused();
	}
	
	private final class Worker implements Runnable
	{
		private Task mTask;
		private boolean mIsPaused = true;
		private boolean mPauseRequested = true;
		
		private Thread mCmdpollerThread;
		
		private Worker() {
			mCmdpollerThread = new Thread("plugin-dataimport-taskmanager-cmdpoller")
			{
				@Override
				public void run() {
					while(true) {
						mIsPaused = mPauseRequested;
						try { Thread.sleep(100); } catch (InterruptedException ie) { }
					}
				}
			};
			mCmdpollerThread.setDaemon(true);
			mCmdpollerThread.start();
		}
		
		public synchronized void pause() {
			synchronized(this) {
				if(isPaused())
					return;
				mPauseRequested = true;
				while(!isPaused())
					Thread.yield();
			}
		}
		
		public synchronized void resume() {
			synchronized(this) {
				if(!isPaused())
					return;
				mPauseRequested = false;
				while(isPaused())
					Thread.yield();
			}
		}
		
		public boolean isPaused() {
			return mIsPaused;
		}
		
		@Override
		public void run() {
			while(true)
			{
				// Don't hog the CPU
				try {
					Thread.sleep(100);
				} catch (InterruptedException ie) {
					LOG.warn("Worker interrupted", ie);
				}
				
				// Do nothing if paused
				if(isPaused())
					continue;
				
				// Get next queued Task
				Task nextTask = null;
				for(Task task : tasks()) {
					if(task.isLocked())
						continue;
					nextTask = task;
					break;
				}
				if(nextTask == null) {
					LOG.info("No more tasks to process");
					TaskManager.this.pause();
					continue;
				} else {
					mTask = nextTask;
				}
				
				LOG.info("Processing Task Id={}", mTask.getId());
				try {
					mTask.run();
				} catch (TaskException te) {
					mTask.error(te);
					LOG.error("Exception while processing Task Id={}", mTask.getId(), te);
				} catch (Exception e) {
					mTask.error(e);
					LOG.error("Exception while processing Task Id={}", mTask.getId(), e);
					// This error was not supposed to happen, pause TaskManager
					TaskManager.this.pause();
				}
			}
		}
	}

	static void fireTaskChanged(Task task) {
		for(TaskListener tl : mListeners)
			tl.taskChanged(task);
	}

	static void fireTaskmanagerChanged() {
		for(TaskListener tl : mListeners)
			tl.taskmanagerChanged();
	}
}
