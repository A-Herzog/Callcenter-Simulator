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
package ui.dataloader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import ui.model.CallcenterModel;

/**
 * Abstrakte Basisklasse für die speziellen Importfilter, die im <code>SpecialProcessingDialog</code>-Dialog
 * registriert werden können.
 * @author Alexander Herzog
 * @version 1.0
 * @see SpecialProcessingDialog
 */
public abstract class AbstractSpecialProcessing {
	private JPanel panel;
	private JTextPane viewer;
	private final List<FileDropper> drop;

	/**
	 * Übergeordnetes Fenster
	 */
	protected final Window owner;

	/**
	 * Konstructor der Klasse <code>AbstractSpecialProcessing</code>
	 * @param owner	Übergeordnetes Fenster (wird benötigt, um Dialoge anzeigen zu können).
	 */
	protected AbstractSpecialProcessing(Window owner) {
		this.owner=owner;
		drop=new ArrayList<FileDropper>();
	}

	/**
	 * Liefert den Namen des Importfilters, so wie er im Dialog angezeigt werden soll, zurück.
	 * @return	Name des Importfilters
	 */
	public abstract String getName();

	/**
	 * Liefert das in der rechten Dialoghälfte anzuzeigende Control, über das der Importfilter konfiguriert werden kann, zurück
	 * @return	Panel, welches auf der rechten Seite eingebettet werden kann.
	 */
	public final JPanel getPanel() {
		if (panel==null) panel=createPanel();
		return panel;
	}

	/**
	 * Erzeugt das Panel, welches später auf der rechten Dialogseite angezeigt werden soll.
	 * @return	Neu erzeugtes Panel
	 */
	protected abstract JPanel createPanel();

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind
	 * @return	 Liefert <code>true</code> zurück, wenn die Daten eine korrekte Verarbeitung zulassen.
	 */
	public abstract boolean checkData();

	/**
	 * Führt die Verarbeitung durch.
	 * @return	Gibt optional ein Callcenter Modell zurück, in den Editor geladen werden soll.
	 */
	public abstract CallcenterModel process();

	/**
	 * Fügt ein beschriftetes Eingabelement mit optionalem Button in ein Panel ein
	 * @param parent	Übergeordnetes Element
	 * @param name	Beschriftungstext
	 * @param control	Dialogelement, dass eingefügt werden soll
	 * @param button	Optionales Button (kann <code>null</code> sein)
	 * @return	Liefert das Panel, welches das Control und die Beschriftung enthält, zurück.
	 */
	protected final JPanel addControlWithButton(JPanel parent, String name, JComponent control, JButton button) {
		JPanel sub,p,p2;

		parent.add(sub=new JPanel());
		sub.setAlignmentX(0);
		sub.setLayout(new BoxLayout(sub,BoxLayout.Y_AXIS));

		sub.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(name));

		sub.add(p=new JPanel(new BorderLayout()));
		p.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		p.add(p2=new JPanel(),BorderLayout.CENTER);
		p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));
		if (button!=null) p2.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
		p2.add(control,BorderLayout.CENTER);
		Dimension d1=control.getPreferredSize();
		Dimension d2=control.getMaximumSize();
		d2.height=d1.height;
		control.setMaximumSize(d2);
		if (button!=null) p.add(button,BorderLayout.EAST);

		return sub;
	}

	/**
	 * Fügt eine beschriftete Eingabezeile mit optionalem Button in ein Panel ein
	 * @param parent	Übergeordnetes Element
	 * @param name	Beschriftungstext
	 * @param initialValue	Vorgabetext für die Eingabezeile (kann <code>null</code> sein)
	 * @param button	Optionales Button (kann <code>null</code> sein)
	 * @return	Liefert das neu erstellte und eingefügte <code>JTextField</code>-Objekt zurück
	 */
	protected final JTextField addInputLine(JPanel parent, String name, String initialValue, JButton button) {
		JTextField text=new JTextField(50);
		addControlWithButton(parent,name,text,button);
		if (initialValue!=null && !initialValue.trim().isEmpty()) text.setText(initialValue.trim());
		return text;
	}

	/**
	 * Fügt eine beschriftete Eingabezeile mit optionalem Button in ein Panel ein und
	 * sorgt dafür, dass Dateien auf der Eingabezeile per Drag&Drop abgelegt werden können.
	 * @param parent	Übergeordnetes Element
	 * @param name	Beschriftungstext
	 * @param initialValue	Vorgabetext für die Eingabezeile (kann <code>null</code> sein)
	 * @param button	Optionales Button (kann <code>null</code> sein)
	 * @return	Liefert das neu erstellte und eingefügte <code>JTextField</code>-Objekt zurück
	 */
	protected final JTextField addFileInputLine(JPanel parent, String name, String initialValue, JButton button) {
		JTextField text=new JTextField(50);
		JPanel sub=addControlWithButton(parent,name,text,button);
		if (initialValue!=null && !initialValue.trim().isEmpty()) text.setText(initialValue.trim());

		drop.add(new FileDropper(new Component[]{sub,text},new FileDropActionListener()));

		return text;
	}

	/**
	 * Fügt ein Infotext-Panel in ein übergeordnetes Panel ein.
	 * @param parent	Übergeordnetes Panel
	 * @param info	Anzuzeigender Infotext (mit optionalen html-Formatierungen)
	 */
	protected final void addInfoText(JPanel parent, String info) {
		final String head=
				"<!DOCTYPE html>\n"+
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

		parent.setLayout(new BorderLayout());
		parent.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		viewer=new JTextPane();
		viewer.setEditable(false);
		viewer.addHyperlinkListener(new LinkListener());
		viewer.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		viewer.setContentType("text/html");
		viewer.setText(head+info+"</body></html>");

		JScrollPane sp=new JScrollPane(viewer);
		parent.add(sp,BorderLayout.CENTER);
		sp.setBorder(BorderFactory.createEmptyBorder());
	}

	private final class LinkListener implements HyperlinkListener {
		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType()==HyperlinkEvent.EventType.ENTERED) {
				viewer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				return;
			}

			if (e.getEventType()==HyperlinkEvent.EventType.EXITED) {
				viewer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				return;
			}

			if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
				if (e instanceof HTMLFrameHyperlinkEvent) {
					HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent)e;
					HTMLDocument doc = (HTMLDocument)viewer.getDocument();
					doc.processHTMLFrameHyperlinkEvent(evt);
					return;
				}

				URL url=e.getURL();
				if (url!=null) try {
					Desktop.getDesktop().browse(url.toURI());
				} catch (IOException e1) {} catch (URISyntaxException e1) {}

			}
		}
	}

	private final class FileDropActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			final FileDropperData data=(FileDropperData)e.getSource();
			int i=drop.indexOf(data.getFileDropper());
			if (i<0) return;
			final FileDropper dropper=drop.get(i);
			for (Component c: dropper.getComponents()) if (c instanceof JTextField && dropFile(data.getFile(),(JTextField)c)) {data.dragDropConsumed(); break;}
		}

		private final boolean dropFile(File file, JTextField field) {
			if (!file.exists()) return false;
			field.setText(file.toString());
			return true;
		}
	}
}
