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
		int r1 = color1.getRed();
		int g1 = color1.getGreen();
		int b1 = color1.getBlue();
		int r2 = color2.getRed();
		int g2 = color2.getGreen();
		int b2 = color2.getBlue();
		prim1 = new ColorUIResource(new ColorUIResource(r1,g1,b1));
		prim2 = new ColorUIResource(new ColorUIResource((int)(r1/1.1),(int)(g1/1.1),(int)(b1/1.1)));
		prim3 = new ColorUIResource(new ColorUIResource((int)(r1/1.2),(int)(g1/1.2),(int)(b1/1.2)));
		sec1 = new ColorUIResource(new ColorUIResource((int)(r2/1.1),(int)(g2/1.1),(int)(b2/1.1)));
		sec2 = new ColorUIResource(new ColorUIResource(r2,g2,b2));
		sec3 = new ColorUIResource(new ColorUIResource((int)(r2/1.3),(int)(g2/1.3),(int)(b2/1.3)));
		win1 = new ColorUIResource(new ColorUIResource((int)(r1/1.1),(int)(g1/1.1),(int)(b1/1.1)));
		win2 = new ColorUIResource(new ColorUIResource((int)(r2/1.4),(int)(g2/1.4),(int)(b2/1.4)));
		win3 = new ColorUIResource(new ColorUIResource((int)(r2/1.4),(int)(g2/1.4),(int)(b2/1.4)));
		win4 = new ColorUIResource(new ColorUIResource((int)(r1/1.1),(int)(g1/1.1),(int)(b1/1.1)));
		win5 = new ColorUIResource(new ColorUIResource(r1,g1,b1));
		win6 = new ColorUIResource(new ColorUIResource((int)(r2/1.4),(int)(g2/1.4),(int)(b2/1.4)));
		win7 = new ColorUIResource(new ColorUIResource(0,0,0));
		win8 = new ColorUIResource(new ColorUIResource(r1,g1,b1));
		win9 = new ColorUIResource(new ColorUIResource(r1,g1,b1));
		win10 = new ColorUIResource(new ColorUIResource((int)(r2/1.4),(int)(g2/1.4),(int)(b2/1.4)));
		win11 = new ColorUIResource(new ColorUIResource(0,0,0));
		win12 = new ColorUIResource(new ColorUIResource((int)(r2/1.4),(int)(g2/1.4),(int)(b2/1.4)));
		FontUIResource fontUI = new FontUIResource(font);
		controlFont = fontUI;
		captionFont = fontUI;
		systemFont = fontUI;
		userFont = fontUI;
		smallFont = fontUI;
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

//	@Override
//	public ColorUIResource getWindowTitleBackground() {
//		return sec2;
//	}
//
//	@Override
//	public ColorUIResource getWindowTitleForeground() {
//		return sec3;
//	}
//
//	@Override
//	public ColorUIResource getWindowTitleInactiveBackground() {
//		return sec1;
//	}
//
//	@Override
//	public ColorUIResource getWindowTitleInactiveForeground() {
//		return prim1;
//	}
}