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

import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;

/**
 * Zeigt einen Dialog an, der das Kopieren von Objekten unter Änderung des Names und der Stärke des Objekts ermöglicht
 * @author Alexander Herzog
 * @version 1.0
 */
public class CopyDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8727888932261919490L;

	/** Text im Label für den Namen des neuen Objektes (wird <code>null</code> übergeben, so wird kein Feld zu Änderung des Namens angezeigt) */
	private final String nameLabel;
	/** Initialer Wert für den Name des neuen Objektes */
	private final String nameValue;
	/** Text, der auf dem Info-Label angezeigt wird und angibt, welchem Wert die angegeben Prozentzahl entspricht. Dieser String wird angezeigt, wenn der Wert "1" angezeigt werden soll. (wird <code>null</code> übergeben, so wird kein Feld zu Änderung des Prozentwertes angezeigt) */
	private final String intensityLabelSingle;
	/** Text, der auf dem Info-Label angezeigt wird und angibt, welchem Wert die angegeben Prozentzahl entspricht. Dieser String wird angezeigt, wenn ein Wert ungleich "1" angezeigt werden soll; er muss "%d" enthalten. */
	private final String intensityLabelMulti;
	/** Gibt an, welcher Wert 100% entsprechen soll. */
	private final int count;
	/** Bezeichner der Werte */
	private final String[] countNames;
	/** Gibt an, welche Werte 100% entsprechen sollen. */
	private final int[] countValues;

	/** Eingabefeld für den Namen */
	private JTextField name=null;
	/** Eingabefeld für den Prozentwert */
	private JTextField percent=null;
	/** Infoausgabe zu der Umrechnung des Prozentwertes */
	private JLabel info;

	/**
	 * Konstruktor der Klasse <code>CopyDialog</code>
	 * @param owner Übergeordnetes Fenster
	 * @param title Fenstertitel
	 * @param nameLabel Text im Label für den Namen des neuen Objektes (wird <code>null</code> übergeben, so wird kein Feld zu Änderung des Namens angezeigt)
	 * @param nameValue Initialer Wert für den Name des neuen Objektes
	 * @param intensityLabelSingle Text, der auf dem Info-Label angezeigt wird und angibt, welchem Wert die angegeben Prozentzahl entspricht. Dieser String wird angezeigt, wenn der Wert "1" angezeigt werden soll. (wird <code>null</code> übergeben, so wird kein Feld zu Änderung des Prozentwertes angezeigt)
	 * @param intensityLabelMulti Text, der auf dem Info-Label angezeigt wird und angibt, welchem Wert die angegeben Prozentzahl entspricht. Dieser String wird angezeigt, wenn ein Wert ungleich "1" angezeigt werden soll; er muss "%d" enthalten.
	 * @param count	Gibt an, welcher Wert 100% entsprechen soll.
	 * @param helpCallback	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird eine "Hilfe"-Schaltfläche angezeigt und die <code>Run</code>-Methode dieses Objekts beim Klicken auf diese Schaltfläche aufgerufen
	 */
	public CopyDialog(Window owner, String title, String nameLabel, String nameValue, String intensityLabelSingle, String intensityLabelMulti, int count, Runnable helpCallback) {
		super(owner,title,false,helpCallback);
		this.nameLabel=nameLabel;
		this.nameValue=nameValue;
		this.intensityLabelSingle=intensityLabelSingle;
		this.intensityLabelMulti=intensityLabelMulti;
		this.count=count;
		this.countNames=new String[0];
		this.countValues=new int[0];
		createSimpleGUI(500,400,null,null);
		pack();
	}

	/**
	 * Konstruktor der Klasse <code>CopyDialog</code>
	 * @param owner Übergeordnetes Fenster
	 * @param title Fenstertitel
	 * @param intensityLabelSingle Text, der auf dem Info-Label angezeigt wird und angibt, welchem Wert die angegeben Prozentzahl entspricht. Dieser String wird angezeigt, wenn der Wert "1" angezeigt werden soll.
	 * @param intensityLabelMulti Text, der auf dem Info-Label angezeigt wird und angibt, welchem Wert die angegeben Prozentzahl entspricht. Dieser String wird angezeigt, wenn ein Wert ungleich "1" angezeigt werden soll; er muss "%d" enthalten.
	 * @param count	Gibt an, welcher Wert 100% entsprechen soll.
	 * @param helpCallback	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird eine "Hilfe"-Schaltfläche angezeigt und die <code>Run</code>-Methode dieses Objekts beim Klicken auf diese Schaltfläche aufgerufen
	 */
	public CopyDialog(Window owner, String title, String intensityLabelSingle, String intensityLabelMulti, int count, Runnable helpCallback) {
		this(owner,title,null,null,intensityLabelSingle,intensityLabelMulti,count,helpCallback);
	}

	/**
	 * Konstruktor der Klasse <code>CopyDialog</code>
	 * @param owner Übergeordnetes Fenster
	 * @param title Fenstertitel
	 * @param nameLabel Text im Label für den Namen des neuen Objektes (wird <code>null</code> übergeben, so wird kein Feld zu Änderung des Namens angezeigt)
	 * @param nameValue Initialer Wert für den Name des neuen Objektes
	 * @param intensityLabelSingle Text, der auf dem Info-Label angezeigt wird und angibt, welchem Wert die angegeben Prozentzahl entspricht. Dieser String wird angezeigt, wenn der Wert "1" angezeigt werden soll. (wird <code>null</code> übergeben, so wird kein Feld zu Änderung des Prozentwertes angezeigt)
	 * @param intensityLabelMulti Text, der auf dem Info-Label angezeigt wird und angibt, welchem Wert die angegeben Prozentzahl entspricht. Dieser String wird angezeigt, wenn ein Wert ungleich "1" angezeigt werden soll; er muss "%d" enthalten.
	 * @param countNames	Gibt die Bezeichner der Werte an.
	 * @param countValues	Gibt an, welche Werte 100% entsprechen sollen.
	 * @param helpCallback	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird eine "Hilfe"-Schaltfläche angezeigt und die <code>Run</code>-Methode dieses Objekts beim Klicken auf diese Schaltfläche aufgerufen
	 */
	public CopyDialog(Window owner, String title, String nameLabel, String nameValue, String intensityLabelSingle, String intensityLabelMulti, String[] countNames, int[] countValues, Runnable helpCallback) {
		super(owner,title,false,helpCallback);
		this.nameLabel=nameLabel;
		this.nameValue=nameValue;
		this.intensityLabelSingle=intensityLabelSingle;
		this.intensityLabelMulti=intensityLabelMulti;
		this.count=-1;
		this.countNames=countNames;
		this.countValues=countValues;
		createSimpleGUI(500,400,null,null);
		pack();
	}

	/**
	 * Konstruktor der Klasse <code>CopyDialog</code>
	 * @param owner Übergeordnetes Fenster
	 * @param title Fenstertitel
	 * @param intensityLabelSingle Text, der auf dem Info-Label angezeigt wird und angibt, welchem Wert die angegeben Prozentzahl entspricht. Dieser String wird angezeigt, wenn der Wert "1" angezeigt werden soll.
	 * @param intensityLabelMulti Text, der auf dem Info-Label angezeigt wird und angibt, welchem Wert die angegeben Prozentzahl entspricht. Dieser String wird angezeigt, wenn ein Wert ungleich "1" angezeigt werden soll; er muss "%d" enthalten.
	 * @param countNames	Gibt die Bezeichner der Werte an.
	 * @param countValues	Gibt an, welche Werte 100% entsprechen sollen.
	 * @param helpCallback	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird eine "Hilfe"-Schaltfläche angezeigt und die <code>Run</code>-Methode dieses Objekts beim Klicken auf diese Schaltfläche aufgerufen
	 */
	public CopyDialog(Window owner, String title, String intensityLabelSingle, String intensityLabelMulti, String[] countNames, int[] countValues, Runnable helpCallback) {
		this(owner,title,null,null,intensityLabelSingle,intensityLabelMulti,countNames,countValues,helpCallback);
	}

	/**
	 * Konstruktor der Klasse <code>CopyDialog</code>
	 * @param owner Übergeordnetes Fenster
	 * @param title Fenstertitel
	 * @param nameLabel Text im Label für den Namen des neuen Objektes
	 * @param nameValue Initialer Wert für den Name des neuen Objektes
	 * @param helpCallback	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird eine "Hilfe"-Schaltfläche angezeigt und die <code>Run</code>-Methode dieses Objekts beim Klicken auf diese Schaltfläche aufgerufen
	 */
	public CopyDialog(Window owner, String title, String nameLabel, String nameValue, Runnable helpCallback) {
		this(owner,title,nameLabel,nameValue,null,null,0,helpCallback);
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));

		JPanel p;

		if (nameLabel!=null) {
			content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p.add(new JLabel(nameLabel));
			content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p.add(name=new JTextField(nameValue,50));
		}

		if (intensityLabelSingle!=null) {
			content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p.add(new JLabel(Language.tr("Editor.CopyDialog.Intensity")));
			p.add(percent=new JTextField("100%",10));
			percent.addKeyListener(new PercentListener());

			content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p.add(info=new JLabel(""));

			updateCount();
		}
	}

	@Override
	public String getName() {
		return (name==null)?null:name.getText().trim();
	}

	/**
	 * Liefert die gewählte Wahrscheinlichkeit
	 * @return	Wahrscheinlichkeit
	 */
	public double getProbability() {
		return (percent==null)?1:NumberTools.getExtProbability(percent,false);
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#checkData()
	 */
	@Override
	protected boolean checkData() {
		if (name!=null && name.getText().trim().isEmpty()) {
			MsgBox.error(this,Language.tr("Editor.CopyDialog.ErrorNoNameTitle"),Language.tr("Editor.CopyDialog.ErrorNoNameInfo"));
			return false;
		}

		if (percent!=null) {
			Double d=NumberTools.getExtProbability(percent,true);
			if (d==null) {
				MsgBox.error(this,Language.tr("Editor.CopyDialog.ErrorInvalidIntensityTitle"),Language.tr("Editor.CopyDialog.ErrorInvalidIntensityInfo"));
				return false;
			}
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#storeData()
	 */
	@Override
	protected void storeData() {}

	/**
	 * Aktualisiert die Infoausgabe {@link #info},
	 * wenn sich die Eingaben in {@link #percent}
	 * geändert haben.
	 * @see #percent
	 * @see #info
	 */
	private void updateCount() {
		Double d=NumberTools.getExtProbability(percent,true);
		if (d==null) {info.setText(""); return;}

		StringBuilder sb=new StringBuilder();
		if (count>=0) {
			int c=(int)Math.round(d*count);
			if (c==1) sb.append(intensityLabelSingle); else sb.append(String.format(intensityLabelMulti,c));
		} else {
			sb.append("<html><body>");
			for (int i=0;i<Math.min(10,countNames.length);i++) {
				if (i>0) sb.append("<br><br>");
				if (countNames[i]!=null) sb.append("<b>"+countNames[i]+"</b><br>");
				int c=(int)Math.round(d*countValues[i]);
				if (c==1) sb.append(intensityLabelSingle); else sb.append(String.format(intensityLabelMulti,c));
			}
			if (countNames.length>10) sb.append("<br><br><b>...</b>");
			sb.append("</body></html>");
		}
		info.setText(sb.toString());
	}

	/**
	 * Reagiert auf Eingaben in {@link CopyDialog#percent}
	 * @see CopyDialog#percent
	 */
	private class PercentListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {if (e.getSource()==percent) updateCount();}
		@Override
		public void keyPressed(KeyEvent e) {if (e.getSource()==percent) updateCount();}
		@Override
		public void keyReleased(KeyEvent e) {if (e.getSource()==percent) updateCount();}
	}
}
