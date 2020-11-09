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
package ui.generator;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TimeTools;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import systemtools.MsgBox;
import ui.editor.BaseEditDialog;
import ui.images.Images;
import ui.model.CallcenterModel;

/**
 * Basisklasse für Modell-Generator-Dialoge
 * Die Klasse enthält bereits eine Eingabezeile für Tabellendateien, lädt gewählte Tabellen und benachrichtigt den Dialog
 * @author Alexander Herzog
 * @version 1.0
 */
public class GeneratorBaseDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4935517705615668761L;

	/**
	 * Speichert das bisherige Callcenter-Modell
	 */
	protected final CallcenterModel model;

	/**
	 * Geladene Datentabelle. Ist <code>null</code> wenn keine Tabelle geladen ist.
	 */
	protected Table table;

	/**
	 * Gibt die Zeilenbereiche in den Spalten an, in denen Zahlen stehen. (Array 1: Beginn der Bereiche, Array 2: Ende der Bereiche)
	 */
	protected Integer[][] rows;

	/**
	 * Gibt die Spaltenüberschriften an.
	 */
	protected String[] heading;

	/**
	 * Gibt an, welche nutzbare Spalte sich auf welche tatsächliche Spaltennummer bezieht.
	 */
	protected List<Integer> colIndex;

	/**
	 * Panel, in das die Spalten-Auswahl-Dialogelemente gezeichnet werden sollen.
	 * (Das Panel ist in ein JScrollPane eingebettet und wird nach dem Laden einer Tabelle eingeblendet.)
	 */
	protected JPanel data;

	/**
	 * Anzahl an Zeilen die für einen Datensatz dieser Art notwendig sind.
	 */
	protected final int neededRows;

	/**
	 * Eingabefeld für die Tabellendatei
	 */
	private JTextField tableField;

	/**
	 * Schaltfläche zur Auswahl einer Tabellendatei.
	 * @see #loadTable(File)
	 */
	private JButton tableButton;

	/**
	 * Zentraler Arbeitsbereich
	 */
	private JPanel cards;

	/**
	 * Konstruktor der Klasse <code>GeneratorBaseDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param title	Title des Fensters
	 * @param helpCallback Wird hier ein Wert ungleich <code>null</code> übergeben, so wird eine "Hilfe"-Schaltfläche angezeigt und die <code>Run</code>-Methode dieses Objekts beim Klicken auf diese Schaltfläche aufgerufen
	 * @param model	Callcenter-Modell, das die Basis für die neuen Daten bilden soll.
	 * @param neededRows Gibt an, wie viele zusammenhängende Zahlen-Zeilen in einer Spalte vorhanden sein müssen, damit diese als Quelldaten-Spalte verwendet werden kann.
	 */
	protected GeneratorBaseDialog(Window owner, String title, Runnable helpCallback, CallcenterModel model, int neededRows) {
		super(owner,title,false,helpCallback);
		this.model=model.clone();
		this.neededRows=neededRows;
		table=null;
		createSimpleGUI(700,500,null,null);
	}

	/**
	 * Konstruktor der Klasse <code>GeneratorBaseDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param title	Title des Fensters
	 * @param helpCallback Wird hier ein Wert ungleich <code>null</code> übergeben, so wird eine "Hilfe"-Schaltfläche angezeigt und die <code>Run</code>-Methode dieses Objekts beim Klicken auf diese Schaltfläche aufgerufen
	 * @param model	Callcenter-Modell, das die Basis für die neuen Daten bilden soll.
	 */
	protected GeneratorBaseDialog(Window owner, String title, Runnable helpCallback, CallcenterModel model) {
		super(owner,title,false,helpCallback);
		this.model=model.clone();
		neededRows=-1;
		table=null;
		createSimpleGUI(700,500,null,null);
	}

	@Override
	protected final void createSimpleContent(JPanel content) {
		JPanel p,p2;

		content.setLayout(new BorderLayout());

		/* Tabellenauswahl */
		content.add(p=new JPanel(),BorderLayout.NORTH);
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));

		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(new JLabel(Language.tr("Generator.TableFile")));

		p.add(p2=new JPanel(new BorderLayout()));
		p2.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		p2.add(tableField=new JTextField(60),BorderLayout.CENTER);
		tableField.addActionListener(new TableButtonListener());
		tableField.addKeyListener(new TableButtonListener());
		p2.add(tableButton=new JButton(Language.tr("Generator.SelectTableFile")),BorderLayout.EAST);
		tableButton.addActionListener(new TableButtonListener());
		tableButton.setToolTipText(Language.tr("Generator.SelectTableFile.Info"));
		tableButton.setIcon(Images.GENERAL_SELECT_FILE_TABLE.getIcon());

		/* Arbeitsbereich */
		content.add(cards=new JPanel(new CardLayout()),BorderLayout.CENTER);
		cards.add(p=new JPanel(),"empty");
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
		p.add(Box.createVerticalGlue());
		p.add(p2=new JPanel(new FlowLayout()));
		p2.add(new JLabel(Language.tr("Generator.NoTableSelected")));
		p.add(Box.createVerticalGlue());
		cards.add(p=new JPanel(),"data");
		p.setLayout(new BorderLayout());
		p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		JScrollPane sc;
		p.add(sc=new JScrollPane(data=new JPanel()));
		sc.setBorder(BorderFactory.createEmptyBorder());
		setCard(false);

		new FileDropper(new Component[]{this,tableField,tableButton},new TableButtonListener());
	}

	/**
	 * Wird aufgerufen, um neu geladene Tabellendaten in die GUI zu laden.
	 * @return	Gibt zurück, ob die Daten erfolgreich geladen werden konnten.
	 */
	protected boolean tableLoaded() {
		return true;
	}

	/**
	 * Liefert nach dem schließen des Dialogs das neue Callcenter-Modell zurück.
	 * @return	Liefert das Callcenter-Modell oder im Fall von einem Klick auf "Abbrechen" den Wert <code>null</code>
	 */
	public final CallcenterModel getModel() {
		return (getClosedBy()==BaseEditDialog.CLOSED_BY_OK)?model:null;
	}

	/**
	 * Aktiviert oder deaktiviert die Darstellung der GUI-Karte.
	 * @param dataCard	GUI-Elemente darstellen?
	 */
	private final void setCard(boolean dataCard) {
		if (!dataCard) table=null;
		((CardLayout)cards.getLayout()).show(cards,dataCard?"data":"empty");
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Tabellendatei an.
	 * @return	Liefert im Erfolgsfall den Namen der Tabellendatei, sonst <code>null</code>.
	 */
	private final File selectFile() {
		Container c=getParent(); while ((c!=null) && (!(c instanceof Frame))) c=c.getParent();
		File file=Table.showLoadDialog(c,Language.tr("Generator.SelectTableFile")); if (file==null) return null;
		return file;
	}

	/**
	 * Lädt die Daten aus {@link #table} in
	 * {@link #rows}, {@link #colIndex} und {@link #heading}.
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private final boolean loadTableData() {
		/* Gemeinsamen Datenbereich ermitteln */
		rows=getCommonNumberArea(table,neededRows);

		/* Gibt es überhaupt gültige Spalten? */
		colIndex=new ArrayList<Integer>();
		for (int i=0;i<rows[0].length;i++) if (rows[0][i]>=0) colIndex.add(i);
		if (colIndex.size()==0) return false;

		/* Überschriften ermitteln */
		heading=getHeadings(table,rows);

		return true;
	}

	/**
	 * Lädt und verarbeitet eine Tabellendatei
	 * @param file	Zu ladende Datei (kann <code>null</code> sein, dann wird die entsprechende GUI deaktiviert)
	 * @return	Liefert im Erfolgsfall <code>true</code> (im Fehlerfall werden außerdem Fehlermeldungen ausgegeben)
	 */
	private final boolean loadTable(File file) {
		if (file==null || !file.isFile()) {setCard(false); return false;}
		Table table=new Table(Table.IndexMode.COLS);
		if (!table.load(file)) {
			MsgBox.error(GeneratorBaseDialog.this,Language.tr("Generator.Error.CannotLoadTable.Title"),String.format(Language.tr("Generator.Error.CannotLoadTable.Info"),file.toString()));
			setCard(false);
			return false;
		}
		this.table=table;
		boolean b=loadTableData();
		if (b) {
			data.removeAll();
			b=tableLoaded();
			if (b) {
				if (!tableField.getText().trim().equals(file.toString())) tableField.setText(file.toString());
			}
		}
		if (!b) MsgBox.error(GeneratorBaseDialog.this,Language.tr("Generator.Error.NoDataInTable.Title"),String.format(Language.tr("Generator.Error.NoDataInTable.Info"),file.toString()));
		setCard(b);
		return b;
	}

	/**
	 * Reagiert auf Eingaben und auf Klicks auf die Schaltflächen
	 */
	private final class TableButtonListener implements ActionListener, KeyListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==tableField) {loadTable(new File(tableField.getText())); return;}
			if (e.getSource()==tableButton) {loadTable(selectFile()); return;}
			if (e.getSource() instanceof FileDropperData) {
				final FileDropperData data=(FileDropperData)e.getSource();
				if (loadTable(data.getFile())) data.dragDropConsumed();
				return;
			}
		}
		@Override
		public void keyTyped(KeyEvent e) {loadTable(new File(tableField.getText().trim())); return;}
		@Override
		public void keyPressed(KeyEvent e) {loadTable(new File(tableField.getText().trim())); return;}
		@Override
		public void keyReleased(KeyEvent e) {loadTable(new File(tableField.getText().trim())); return;}
	}

	/**
	 * Ermittelt den pro Spalte größten zusammenhängenden Bereich, in dem sich nicht negative Zahlen befinden
	 * @param table	Tabelle, aus der die Daten stammen
	 * @return	Array aus zwei Einträgen: Der erste Eintrag ist ein Array aus den Startzeilennummern und der zweite Eintrag ein Array aus den Endzeilnnummern ("-1" bedeutet, dass es in der Spalte keine Zahlen gab)
	 */
	private static final Integer[][] getNumberArea(Table table) {
		List<Integer> indexStart=new ArrayList<Integer>();
		List<Integer> indexEnd=new ArrayList<Integer>();

		for (int i=0;i<table.getSize(0);i++) {
			int lastStart=-1, lastEnd=-1, currentStart=-1;
			for (int j=0;j<table.getSize(1)+1;j++) {
				String s;
				if (j==table.getSize(1)) s=""; else s=table.getValue(i,j);
				Double d=null;
				if (!s.isEmpty()) {
					d=NumberTools.getNotNegativeDouble(s);
					if (d!=null && TimeTools.getTime(s)!=null) d=null;
				}

				if (d==null) {
					if (currentStart>=0) {
						int currentEnd=j-1;
						int currentCount=currentEnd-currentStart+1;
						int lastCount=0;
						if (lastStart>0) lastCount=lastEnd-lastStart+1;
						if (currentCount>lastCount) {lastStart=currentStart; lastEnd=currentEnd;}
					}
					currentStart=-1; continue;
				} else {
					if (currentStart<0) currentStart=j;
				}
			}

			indexStart.add(lastStart);
			indexEnd.add(lastEnd);
		}

		return new Integer[][]{indexStart.toArray(new Integer[0]),indexEnd.toArray(new Integer[0])};
	}

	/**
	 * Ermittelt einen gemeinsamen Zeilenbereich, in der sich in jeder Spalte (in der überhaupt Zahlen stehen) jeweils Zahlen stehen
	 * @param numberArea	Array, welches die jeweils größen Bereiche mit Zahlen pro Spalte angibt
	 * @param rows			Geforderte Anzahl der zusammenhängenden Zeilen mit Zahlen (-1 für variable Anzahl)
	 * @return				Array aus zwei Einträgen: Der erste Eintrag ist ein Array aus den Startzeilennummern und der zweite Eintrag ein Array aus den Endzeilnnummern ("-1" bedeutet, dass es in der Spalte keine Zahlen gab)
	 */
	private static final Integer[][] getCommonNumberArea(Integer[][] numberArea, int rows) {
		/* Minimalen gemeinsamen Bereich bestimmen */
		int maxStart=-1;
		int minEnd=-1;
		for (int i=0;i<numberArea[0].length;i++) if (numberArea[0][i]>=0) {
			if (maxStart==-1) {maxStart=numberArea[0][i]; minEnd=numberArea[1][i];} else {
				maxStart=Math.max(maxStart,numberArea[0][i]);
				minEnd=Math.min(minEnd,numberArea[1][i]);
			}
		}

		/* Prüfen der nötigen Größe */
		if (minEnd-maxStart+1<rows) {maxStart=-1; minEnd=-1;}
		if (minEnd-maxStart+1>rows) minEnd=maxStart+rows-1;

		/* Neue Bereichsvektoren mit minimalem gemeinsamem Bereich anlegen */
		List<Integer> indexStart=new ArrayList<Integer>();
		List<Integer> indexEnd=new ArrayList<Integer>();
		for (int i=0;i<numberArea[0].length;i++) {
			indexStart.add((numberArea[0][i]>=0)?maxStart:-1);
			indexEnd.add((numberArea[0][i]>=0)?minEnd:-1);
		}

		return new Integer[][]{indexStart.toArray(new Integer[0]),indexEnd.toArray(new Integer[0])};
	}

	/**
	 * Ermittelt einen gemeinsamen Zeilenbereich, in der sich in jeder Spalte (in der überhaupt Zahlen stehen) jeweils Zahlen stehen
	 * @param numberArea	Array, welches die jeweils größen Bereiche mit Zahlen pro Spalte angibt
	 * @return				Array aus zwei Einträgen: Der erste Eintrag ist ein Array aus den Startzeilennummern und der zweite Eintrag ein Array aus den Endzeilnnummern ("-1" bedeutet, dass es in der Spalte keine Zahlen gab)
	 */
	private static final Integer[][] getCommonNumberArea(Integer[][] numberArea) {
		/* Minimalen gemeinsamen Bereich bestimmen */
		int maxStart=-1;
		int minEnd=-1;
		for (int i=0;i<numberArea[0].length;i++) if (numberArea[0][i]>=0) {
			if (maxStart==-1) {maxStart=numberArea[0][i]; minEnd=numberArea[1][i];} else {
				maxStart=Math.max(maxStart,numberArea[0][i]);
				minEnd=Math.min(minEnd,numberArea[1][i]);
			}
		}

		/* Neue Bereichsvektoren mit minimalem gemeinsamem Bereich anlegen */
		List<Integer> indexStart=new ArrayList<Integer>();
		List<Integer> indexEnd=new ArrayList<Integer>();
		for (int i=0;i<numberArea[0].length;i++) {
			indexStart.add((numberArea[0][i]>=0)?maxStart:-1);
			indexEnd.add((numberArea[0][i]>=0)?minEnd:-1);
		}

		return new Integer[][]{indexStart.toArray(new Integer[0]),indexEnd.toArray(new Integer[0])};
	}

	/**
	 * Ermittelt einen gemeinsamen Zeilenbereich, in der sich in jeder Spalte (in der überhaupt Zahlen stehen) jeweils Zahlen stehen
	 * @param table	Tabelle, aus der die Daten stammen
	 * @param rows	Geforderte Anzahl der zusammenhängenden Zeilen mit Zahlen (-1 für variable Anzahl)
	 * @return		Array aus zwei Einträgen: Der erste Eintrag ist ein Array aus den Startzeilennummern und der zweite Eintrag ein Array aus den Endzeilnnummern ("-1" bedeutet, dass es in der Spalte keine Zahlen gab)
	 */
	private static final Integer[][] getCommonNumberArea(Table table, int rows) {
		if (rows==-1) return getCommonNumberArea(getNumberArea(table));
		return getCommonNumberArea(getNumberArea(table),rows);
	}

	/**
	 * Ermittelt die Überschriften der einzelnen Spalten
	 * @param table	Tabelle, aus der die Daten stammen
	 * @param commonNumberArea	Array, welches die jeweil verwendeten Bereiche mit Zahlen pro Spalte angibt
	 * @return	Array mit den Überschriften der Spalten
	 */
	private static final String[] getHeadings(Table table, Integer[][] commonNumberArea) {
		List<String> headings=new ArrayList<String>();

		/* Zeilennummern festlegen, in denen gesucht werden soll */
		int end=1;
		for (int i=0;i<commonNumberArea[0].length;i++) if (commonNumberArea[0][i]>=0) {end=Math.max(1,commonNumberArea[0][i]); break;}

		/* Daten aus Tabelle auslesen */
		for (int i=0;i<table.getSize(0);i++) {
			String s="";
			for (int j=0;j<end;j++) {
				String t=table.getValue(i,j);
				if (t.isEmpty()) continue;
				if (NumberTools.getExtProbability(t)!=null) continue;
				if (s.isEmpty()) s=t; else s=s+"\n"+t;
			}

			if (s.isEmpty()) {
				s=Language.tr("Generator.Column")+" "+Table.columnNameFromNumber(i);
			}

			headings.add(s);
		}

		return headings.toArray(new String[0]);
	}

	/**
	 * Erstellt ein Label mit HTML-Formatierung, welches eine Überschrift darstellen soll
	 * @param text	Anzuzeigender (Plain-)Text
	 * @return	Liefert das neu erstellte Label zurück.
	 */
	protected static final JLabel createHeadingLabel(String text) {
		return new JLabel("<html><body><b><u>"+text+"</u></b></body></html>");
	}

	/**
	 * Erstellt ein Label zur Anzeige eines Combobox-Labels. Enthält der Text Zeilenumbrüche, so wird auf HTM-Formatierung umgestellt und diese werden entsprechend umgesetzt.
	 * @param text Anzuzeigender Text, der Zeilenumbrüche enthalten kann
	 * @return Liefert das neu erstellte Label zurück.
	 */
	protected static final JLabel createLabel(String text) {
		if (text.indexOf('\n')>=0) text="<html><body>"+text.replace("\n","<br>")+"</body></html>";
		return new JLabel(text);
	}
}
