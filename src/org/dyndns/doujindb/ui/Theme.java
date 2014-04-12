package org.dyndns.doujindb.ui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;

/**  
* Theme.java - DoujinDB theme/skin.
* @author  nozomu
* @version 1.0
*/
public final class Theme extends DefaultMetalTheme
{
	private final ColorUIResource prim1;
	private final ColorUIResource prim2;
	private final ColorUIResource prim3;
	private final ColorUIResource sec1;
	private final ColorUIResource sec2;
	private final ColorUIResource sec3;
	private final ColorUIResource win1;
	private final ColorUIResource win2;
	private final ColorUIResource win3;
	private final ColorUIResource win4;
	private final ColorUIResource win5;
	private final ColorUIResource win6;
	private final ColorUIResource win7;
	private final ColorUIResource win8;
	private final ColorUIResource win9;
	private final ColorUIResource win10;
	private final ColorUIResource win11;
	private final ColorUIResource win12;
	private FontUIResource controlFont;
	private FontUIResource captionFont;
	private FontUIResource systemFont;
	private FontUIResource userFont;
	private FontUIResource smallFont;

	public Theme(Color color1, Color color2, Font font)
	{
		ColorUIResource black = new ColorUIResource(0,0,0);
		ColorUIResource colorui1 = new ColorUIResource(color1);
		ColorUIResource colorui2 = new ColorUIResource(color2);
		prim1 = colorui1;
		prim2 = new ColorUIResource(color1.darker());
		prim3 = new ColorUIResource(color1.darker().darker());
		sec1 = new ColorUIResource(color2.brighter());
		sec2 = colorui2;
		sec3 = colorui2;
		win1 = colorui1;
		win2 = colorui2;
		win3 = colorui2;
		win4 = colorui1;
		win5 = colorui1;
		win6 = colorui2;
		win7 = black;
		win8 = colorui1;
		win9 = colorui1;
		win10 = colorui1;
		win11 = black;
		win12 = colorui2;
		controlFont = captionFont = systemFont = userFont = smallFont = new FontUIResource(font);
	}

	@Override
	public String getName()
	{
		return "org.dyndns.doujindb.ui.Theme";
	}

	@Override
	protected ColorUIResource getPrimary1()
	{
		return prim1;
	}

	@Override
	protected ColorUIResource getPrimary2()
	{
		return prim2;
	}

	@Override
	protected ColorUIResource getPrimary3()
	{
		return prim3;
	}

	@Override
	protected ColorUIResource getSecondary1()
	{
		return sec1;
	}

	@Override
	protected ColorUIResource getSecondary2()
	{
		return sec2;
	}

	@Override
	protected ColorUIResource getSecondary3()
	{
		return sec3;
	}

	@Override
	public ColorUIResource getSystemTextColor()
	{
		return win1;
	}

	@Override
	public ColorUIResource getControl()
	{
		return win2;
	}

	@Override
	public ColorUIResource getControlHighlight()
	{
		return win3;
	}

	@Override
	public ColorUIResource getControlTextColor()
	{
		return win4;
	}

	@Override
	public ColorUIResource getControlInfo()
	{
		return win5;
	}

	@Override
	public ColorUIResource getWindowBackground()
	{
		return win6;
	}

	public ColorUIResource getWindowTitleIn_ActiveForeground()
	{
		return win7;
	}

	@Override
	public ColorUIResource getUserTextColor()
	{
		return win8;
	}

	@Override
	public ColorUIResource getMenuForeground()
	{
		return win9;
	}

	@Override
	public ColorUIResource getMenuSelectedForeground()
	{
		return win10;
	}

	@Override
	public ColorUIResource getDesktopColor()
	{
		return win11;
	}

	@Override
	public ColorUIResource getMenuBackground()
	{
		return win12;
	}

	@Override
	public FontUIResource getControlTextFont()
	{
		return controlFont;
	}

	@Override
	public FontUIResource getSystemTextFont()
	{
		return systemFont;
	}

	@Override
	public FontUIResource getUserTextFont()
	{
		return userFont;
	}

	@Override
	public FontUIResource getMenuTextFont()
	{
		return controlFont;
	}

	public FontUIResource getEmphasisTextFont()
	{
		return captionFont;
	}

	@Override
	public FontUIResource getSubTextFont()
	{
		return smallFont;
	}

	@Override
	public FontUIResource getWindowTitleFont()
	{
		return captionFont;
	}
}
