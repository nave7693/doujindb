package org.dyndns.doujindb.conf;

/**  
* Property.java - A setting property.
* @author  nozomu
* @version 1.0
*/
public interface Property
{
	public String getDescription();
	
	public void setDescription(String description);
	
	public Object getValue();
	
	public void setValue(Object value) throws PropertyException;
	
	public Boolean asBoolean() throws PropertyException;
	
	public String asString() throws PropertyException;
	
	public Integer asNumber() throws PropertyException;
	
	public java.awt.Font asFont() throws PropertyException;
	
	public java.awt.Color asColor() throws PropertyException;
}
