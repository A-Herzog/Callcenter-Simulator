/**
 * Copyright 2020 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ui.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import ui.model.CallcenterModelAgent;

/**
 * Zeigt ein Schichtplan-Diagramm als JPanel an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class AgentShiftPlanDiagram extends JPanel implements mathtools.distribution.swing.JGetImage {
	private static final long serialVersionUID = -1735941999019425028L;

	private final List<CallcenterModelAgent> agents;
	private int agentCount=0;

	/**
	 * Konstruktor der Klasse <code>AgentShiftPlanDiagram</code>
	 * @param translatedAgents	Liste der Agenten, die in dem Plan berücksichtigt werden sollen. (Es muss sich um Agentengruppen mit festen Arbeitszeiten, d.h. count&gt;0, handeln.)
	 */
	public AgentShiftPlanDiagram(List<CallcenterModelAgent> translatedAgents) {
		super();
		agents=translatedAgents;
		if (translatedAgents!=null) for (int i=0;i<translatedAgents.size();i++) agentCount+=translatedAgents.get(i).count;
	}

	private Rectangle paintDiagrammRect(Graphics g, Rectangle r) {
		g.setColor(Color.white);
		g.fillRect(0,0,r.width,r.height);

		int leftSpace=g.getFontMetrics().stringWidth(""+agentCount)+2;
		int bottomSpace=g.getFontMetrics().getAscent()+2;

		Rectangle dataRect=new Rectangle(leftSpace,1,r.width-leftSpace-1,r.height-bottomSpace-1);

		g.setColor(Color.black);
		g.drawLine(dataRect.x,dataRect.y,dataRect.x,dataRect.y+dataRect.height);
		g.drawLine(dataRect.x,dataRect.y+dataRect.height,dataRect.x+dataRect.width,dataRect.y+dataRect.height);

		g.drawString(""+agentCount,1,g.getFontMetrics().getAscent()+1);
		g.drawString("0",1+g.getFontMetrics().stringWidth(""+agentCount)-g.getFontMetrics().stringWidth("0"),dataRect.y+dataRect.height);

		int y=dataRect.y+dataRect.height+bottomSpace;
		for (int i=0;i<=24;i+=2) {
			String s=String.format("%02d:00",i);
			int x=dataRect.x+i*dataRect.width/24;
			int delta=-g.getFontMetrics().stringWidth(s)/2;
			if (i==0) delta=0;
			if (i==24) delta=-g.getFontMetrics().stringWidth(s);
			g.drawString(s,x+delta,y);
		}

		return dataRect;
	}

	private void paintDiagramm(Graphics g, Rectangle dataRect) {
		if (agents==null || agents.size()==0) return;

		dataRect.x+=1;
		dataRect.y+=1;
		dataRect.width-=2;
		dataRect.height-=2;

		int y=dataRect.y+dataRect.height;
		for (int i=0;i<agents.size();i++) {
			CallcenterModelAgent a=agents.get(i);
			int h=dataRect.height*a.count/agentCount;
			Rectangle r=new Rectangle(
					dataRect.x+dataRect.width*a.workingTimeStart/86400,
					y-h,
					dataRect.width*(a.workingTimeEnd-a.workingTimeStart)/86400,
					h
					);
			g.setColor(Color.RED);
			g.fillRect(r.x,r.y,r.width,r.height);
			g.setColor(Color.BLACK);
			g.drawRect(r.x,r.y,r.width,r.height);
			y-=h;
		}
	}

	@Override
	public void paint(Graphics g) {
		Rectangle dataRect=paintDiagrammRect(g,getBounds());
		paintDiagramm(g,dataRect);
	}

	@Override
	public void paintToGraphics(Graphics g) {
		Rectangle dataRect=paintDiagrammRect(g,g.getClipBounds());
		paintDiagramm(g,dataRect);
	}

	private class TransferableImage implements Transferable{
		public TransferableImage(Image image) {theImage=image;}
		@Override
		public DataFlavor[] getTransferDataFlavors(){return new DataFlavor[]{DataFlavor.imageFlavor};}
		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor){return flavor.equals(DataFlavor.imageFlavor);}
		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException{if (flavor.equals(DataFlavor.imageFlavor)) return theImage; else throw new UnsupportedFlavorException(flavor);}
		private final Image theImage;
	}

	/**
	 * Kopiert den Schichtplan als Grafik in die Zwischenablage
	 * @param imageSize	Grafikgröße in x- und in y-Richtung
	 */
	public void copyToClipboard(final int imageSize) {
		Image image=createImage(imageSize,imageSize);
		Graphics g=image.getGraphics();
		g.setClip(0,0,imageSize,imageSize);
		Dimension d=getSize();
		setSize(imageSize,imageSize);
		paint(g);
		setSize(d);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new TransferableImage(image),null);
	}

	/**
	 * Speichert den Schichtplan als Grafik in einer Datei
	 * @param file	Ausgabedatei
	 * @param format	Dateiformat
	 * @param imageSize	Grafikgröße in x- und in y-Richtung
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	public boolean saveToFile(final File file, final String format, final int imageSize) {
		BufferedImage image=new BufferedImage(imageSize,imageSize,BufferedImage.TYPE_INT_RGB);
		Graphics g=image.getGraphics();
		g.setClip(0,0,imageSize,imageSize);
		Dimension d=getSize();
		setSize(imageSize,imageSize);
		paint(g);
		setSize(d);

		try {ImageIO.write(image,format,file);} catch (IOException e) {return false;}
		return true;
	}
}
