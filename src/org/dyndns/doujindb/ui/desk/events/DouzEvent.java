package org.dyndns.doujindb.ui.desk.events;

public class DouzEvent
{
	private Object param;
	private int type;
	
	public static final int DATABASE_REFRESH = 0x00;
	public static final int DATABASE_ITEMCHANGED = 0x01;
	public static final int DATABASE_ITEMADDED = 0x02;
	public static final int DATABASE_ITEMREMOVED = 0x03;
	public static final int DATABASE_RELOAD = 0x04;
	public static final int NETWORK_REFRESH = 0x10;
	public static final int NETWORK_CONNECTED = 0x11;
	public static final int NETWORK_CONNECTING = 0x12;
	public static final int NETWORK_DISCONNECTED = 0x13;
	public static final int NETWORK_DISCONNECTING = 0x14;
	public static final int SETTINGS_LOADED = 0x20;
	public static final int SETTINGS_CHANGED = 0x21;
	
	public DouzEvent(int type, Object param)
	{
		this.param = param;
		this.type = type;
	}

	public Object getParameter() {
		return param;
	}

	public int getType() {
		return type;
	}
}
