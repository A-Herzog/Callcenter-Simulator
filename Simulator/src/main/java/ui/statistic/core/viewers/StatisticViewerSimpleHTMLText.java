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
package ui.statistic.core.viewers;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.print.PrinterException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.swing.CommonVariables;
import systemtools.MsgBox;
import systemtools.statistics.PDFWriter;
import systemtools.statistics.StatisticViewer;

/**
 * Gibt Texte in Form eines HTML-Panels aus.
 * Formatierungen erfolgen direkt über HTML-Befehle, eine Ausgabe als Word- und pdf-Dateien ist somit nicht möglich.
 * @author Alexander Herzog
 */
public class StatisticViewerSimpleHTMLText implements StatisticViewer {
	private JTextPane textPane=null;
	private final String infoText;
	private final Runnable[] specialLinkListener;
	private String specialLink;

	private static final String head=
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"+
					"<html>\n"+
					"<head>\n"+
					"  <style type=\"text/css\">\n"+
					"  body {font-family: Verdana, Lucida, sans-serif; background-color: #FFFFF3; margin: 2px;}\n"+
					"  ul.big li {margin-bottom: 5px;}\n"+
					"  ol.big li {margin-bottom: 5px;}\n"+
					"  a {text-decoration: none;}\n"+
					"  a.box {margin-top: 10px; margin-botton: 10px; border: 1px solid black; background-color: #DDDDDD; padding: 5px;}\n"+
					"  h2 {margin-bottom: 0px;}\n"+
					"  p.red {color: red;}\n"+
					"  </style>\n"+
					"</head>\n"+
					"<body>\n";
	private static final String foot="</body></html>";

	/**
	 * Konstruktor der Klasse
	 * @param infoText	Auszugebender Text
	 * @param specialLinkListener	Die hier optional angegeben {@link Runnable}-Objekte werden aufgerufen, wenn der Nutzer auf einen Link mit dem Ziel "special:nr" klickt; dabei ist nr-1 der Index der {@link Runnable}-Objektes in dem Array
	 */
	public StatisticViewerSimpleHTMLText(String infoText, Runnable[] specialLinkListener) {
		this.infoText=infoText;
		this.specialLinkListener=specialLinkListener;
	}

	/**
	 * Konstruktor der Klasse
	 * @param infoText	Auszugebender Text
	 */
	public StatisticViewerSimpleHTMLText(String infoText) {
		this(infoText,null);
	}

	/**
	 * Initialisiert das {@link JTextPane}-Element mit dem im Konstruktor übergebenen Text
	 */
	protected void initTextPane() {
		if (textPane!=null) return;
		textPane=new JTextPane();
		textPane.setEditable(false);
		textPane.addHyperlinkListener(new LinkListener());
		textPane.setContentType("text/html");
		textPane.setText(head+infoText+foot);
	}

	@Override
	public ViewerType getType() {return ViewerType.TYPE_TEXT;}

	@Override
	public ViewerImageType getImageType() {return ViewerImageType.IMAGE_TYPE_NOIMAGE;}

	@Override
	public Container getViewer(boolean needReInit) {
		if (textPane==null || needReInit) initTextPane();
		Container c=new JScrollPane(textPane);
		textPane.setSelectionStart(0);
		textPane.setSelectionEnd(0);
		return c;
	}

	@Override
	public void copyToClipboard(Clipboard clipboard) {
		clipboard.setContents(new StringSelection(head+infoText+foot),null);
	}

	@Override
	public boolean print() {
		if (textPane==null) initTextPane();
		try {textPane.print();} catch (PrinterException e) {return false;}
		return true;
	}

	@Override
	public void save(Component parentFrame) {
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(Language.tr("FileType.Save.Text"));
		FileFilter html=new FileNameExtensionFilter(Language.tr("FileType.HTML")+" (*.html, *.htm)","html","htm");
		fc.addChoosableFileFilter(html);
		fc.setFileFilter(html);

		if (fc.showSaveDialog(parentFrame)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==html) file=new File(file.getAbsoluteFile()+".html");
		}

		save(parentFrame,file);
	}

	@Override
	public boolean save(Component owner, File file) {
		if (textPane==null) initTextPane();

		return Table.saveTextToFile(head+infoText+foot,file);
	}

	@Override
	public int saveHtml(BufferedWriter bw, File mainFile, int nextImageNr, boolean imagesInline) throws IOException {
		if (textPane==null) initTextPane();

		bw.write(head+infoText+foot);
		return nextImageNr;
	}

	@Override
	public boolean saveDOCX(XWPFDocument doc) {
		doc.createParagraph().createRun().setText(infoText);
		return true;
	}

	@Override
	public boolean savePDF(PDFWriter pdf) {
		return pdf.writeText(infoText,11,false,25);
	}

	@Override
	public void unZoom() {}

	@Override
	public JButton[] getAdditionalButton() {
		return null;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.statistic.core.viewers.StatisticViewer#ownSettingsName()
	 */
	@Override
	public String ownSettingsName() {return null;}

	/* (non-Javadoc)
	 * @see complexcallcenter.statistic.core.viewers.StatisticViewer#ownSettingsIcon()
	 */
	@Override
	public Icon ownSettingsIcon() {return null;}

	/* (non-Javadoc)
	 * @see complexcallcenter.statistic.core.viewers.StatisticViewer#ownSettings()
	 */
	@Override
	public boolean ownSettings(JPanel owner) {return false;}

	private class LinkListener implements HyperlinkListener {
		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType()==HyperlinkEvent.EventType.ENTERED) {
				textPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				return;
			}

			if (e.getEventType()==HyperlinkEvent.EventType.EXITED) {
				textPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				return;
			}

			if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
				if (e instanceof HTMLFrameHyperlinkEvent) {
					HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent)e;
					HTMLDocument doc = (HTMLDocument)textPane.getDocument();
					doc.processHTMLFrameHyperlinkEvent(evt);
				} else {
					URL url=e.getURL();
					if (url==null) {
						specialLink=e.getDescription();
						if (specialLink.startsWith("special:")) {
							Integer i=NumberTools.getInteger(specialLink.substring(8));
							if (i!=null && i>=1 && specialLinkListener!=null && i<=specialLinkListener.length && specialLinkListener[i-1]!=null) SwingUtilities.invokeLater(specialLinkListener[i-1]);
						}
					} else {
						String s=e.getURL().toString();
						if (s.toLowerCase().startsWith("mailto:")) {
							try {Desktop.getDesktop().mail(e.getURL().toURI());} catch (IOException | URISyntaxException e1) {
								MsgBox.error(textPane,Language.tr("Window.Info.NoEMailProgram.Title"),String.format(Language.tr("Window.Info.NoEMailProgram.Info"),e.getURL().toString()));
							}
						} else {
							try {Desktop.getDesktop().browse(e.getURL().toURI());} catch (IOException | URISyntaxException e1) {
								MsgBox.error(textPane,Language.tr("Window.Info.NoInternetConnection"),String.format(Language.tr("Window.Info.NoInternetConnection.Address"),e.getURL().toString()));
							}
						}
					}
				}
			}
		}
	}

	@Override
	public boolean getCanDo(CanDoAction canDoType) {
		switch (canDoType) {
		case CAN_DO_UNZOOM: return false;
		case CAN_DO_COPY: return true;
		case CAN_DO_PRINT: return true;
		case CAN_DO_SAVE: return true;
		default: return false;
		}
	}

	@Override
	public void setRequestImageSize(IntSupplier getImageSize) {
	}

	@Override
	public void setUpdateImageSize(IntConsumer setImageSize) {
	}
}
