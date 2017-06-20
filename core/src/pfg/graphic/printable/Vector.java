/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.graphic.printable;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.io.Serializable;

import pfg.graphic.Fenetre;
import pfg.graphic.Position;
import pfg.graphic.Vec2RO;
import pfg.graphic.Vec2RW;

/**
 * Un vecteur affichable
 * 
 * @author pf
 *
 */

public class Vector implements Printable, Serializable
{
	private static final long serialVersionUID = 3887897521575363643L;
	private Vec2RO a, b;
	private double orientation;
	private Layer l;
	private Color c;
	private AffineTransform tx = new AffineTransform();
	private Polygon arrowHead = new Polygon();  
	
	public Vector(Position pos, double orientation, Layer l, Color c)
	{
		a = new Vec2RO(pos.getX(), pos.getY());
		b = new Vec2RW(50, orientation, true).plus(a);
		this.orientation = orientation;
		this.l = l;
		this.c = c;
		arrowHead.addPoint(0,5);
		arrowHead.addPoint(-5,-5);
		arrowHead.addPoint(5,-5);
	}
	
	public void update(Position pos, double orientation)
	{
		a = new Vec2RO(pos.getX(), pos.getY());
		b = new Vec2RW(50, orientation, true).plus(a);
		this.orientation = orientation;
	}

	@Override
	public void print(Graphics g, Fenetre f)
	{
		g.setColor(c);
		g.drawLine(f.XtoWindow(a.getX()), f.YtoWindow(a.getY()), f.XtoWindow(b.getX()), f.YtoWindow(b.getY()));
	    tx.setToIdentity();
	    tx.translate(f.XtoWindow(b.getX()), f.YtoWindow(b.getY()));
	    tx.rotate((-orientation-Math.PI/2d));  

	    Graphics2D g2d = (Graphics2D) g.create();
	    g2d.setTransform(tx);   
	    g2d.fill(arrowHead);
	    g2d.dispose();
	}

	@Override
	public Vector clone()
	{
		return new Vector(a.clone(), orientation, l, c);
	}
	
	@Override
	public int getLayer()
	{
		return l.ordinal();
	}

	@Override
	public String toString()
	{
		return "Vecteur entre " + a + " et " + b;
	}

}