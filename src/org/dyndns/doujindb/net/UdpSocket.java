package org.dyndns.doujindb.net;

/**  
* UdpSocket.java - Still unused
* @author  nozomu
* @version 1.0
*/
import java.net.*;

public class UdpSocket extends DatagramSocket
{
	public UdpSocket(int port) throws SocketException
	{
		super(port);
	}
	
	public UdpSocket(int port, String host) throws SocketException, UnknownHostException
	{
		super(port, InetAddress.getByName(host));
	}

}
