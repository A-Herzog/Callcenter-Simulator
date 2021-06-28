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
package ui.importer;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import language.Language;
import mathtools.MultiTable;
import systemtools.MsgBox;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;
import ui.model.CallcenterModel;

/**
 * Diese Klasse enthält die Daten für die in {@link ImporterPanel}
 * anzuzeigende Tabelle mit den Konfigurationsdatensätzen für den
 * Schablonen-Import.
 * @author Alexander Herzog
 * @see ImporterPanel
 * @see ImporterData
 */
public class ImporterJTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -1701976557478075869L;

	/** Übergeordnetes Fenster */
	private final Window owner;
	/** Callback das aufgerufen wird, wenn sich die zugehörige Tabelle aktualisieren soll */
	private final Runnable tableUpdateCallback;

	/** Callcenter-Modell in das Datenwerte geladen werden sollen */
	private CallcenterModel model;
	/** Arbeitskopie der Datenmodells */
	private final ImporterData data;
	/** Zuletzt geladenes oder gespeichertes Datenmodell (um prüfen zu können, ob die aktuellen Daten ohne Warnung verworfen werden dürfen) */
	private ImporterData dataSaved;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param model	Callcenter-Modell in das Datenwerte geladen werden sollen
	 * @param tableUpdateCallback	Callback das aufgerufen wird, wenn sich die zugehörige Tabelle aktualisieren soll
	 */
	public ImporterJTableModel(final Window owner, final CallcenterModel model, final Runnable tableUpdateCallback) {
		this.owner=owner;
		this.tableUpdateCallback=tableUpdateCallback;

		this.model=model;
		data=new ImporterData();
		dataSaved=data.clone();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return data.records.size()+1;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return 5;
	}

	/**
	 * Liefert das Panel mit Tools-Schaltflächen für eine Tabellenzeile
	 * @param rowIndex	Tabellenzeile
	 * @return	Panel mit Tools-Schaltflächen
	 */
	private final JPanel getToolsCellValue(final int rowIndex) {
		if (rowIndex==data.records.size()) {
			return makeButtonPanel(
					new String[]{Language.tr("Dialog.Button.Add")},
					new String[]{Language.tr("Importer.Buttons.Add.Info")},
					new Icon[]{Images.EDIT_ADD.getIcon()},
					new ActionListener[]{new TableButtonListener(rowIndex,3)}
					);
		}
		return makeButtonPanel(
				new String[]{"","",""},
				new String[]{Language.tr("Importer.Buttons.Up.Info"),Language.tr("Importer.Buttons.Down.Info"),Language.tr("Importer.Buttons.Del.Info")},
				new Icon[] {Images.ARROW_UP.getIcon(),Images.ARROW_DOWN.getIcon(),Images.EDIT_DELETE.getIcon()},
				new ActionListener[]{new TableButtonListener(rowIndex,0),new TableButtonListener(rowIndex,1),new TableButtonListener(rowIndex,2)}
				);
	}

	/**
	 * Liefert eine Auswahlbox zur Auswahl des Typs für eine Tabellenzeile
	 * @param rowIndex	Tabellenzeile
	 * @return	Auswahlbox zur Auswahl des Typs
	 */
	private final JComboBox<String> getTypeSelect(int rowIndex) {
		List<ImporterProcessor> processors=ImporterRecord.getImporterProcessors();
		String[] list=new String[processors.size()];

		int sel=0;
		ImporterProcessor selProcessor=data.records.get(rowIndex).getProcessor();
		for (int i=0;i<processors.size();i++) {
			list[i]=processors.get(i).getNames()[0];
			if (processors.get(i)==selProcessor) sel=i;
		}

		JComboBox<String> box=new JComboBox<>(list);
		box.setSelectedIndex(sel);
		box.addActionListener(new TableButtonListener(rowIndex,10,box));
		return box;
	}

	/**
	 * Liefert eine Auswahlbox zur Auswahl des Modus für eine Tabellenzeile
	 * @param rowIndex	Tabellenzeile
	 * @return	Auswahlbox zur Auswahl des Modus
	 */
	private final JComboBox<String> getParameterSelect(int rowIndex) {
		ImporterProcessor selProcessor=data.records.get(rowIndex).getProcessor();
		String parameter=data.records.get(rowIndex).parameter;
		Vector<String> list=new Vector<>();

		if (selProcessor.getParameterType()==ImporterProcessor.ParameterType.PARAMETER_TYPE_CALLER) for (int i=0;i<model.caller.size();i++) {
			list.add(model.caller.get(i).name);
		}

		if (selProcessor.getParameterType()==ImporterProcessor.ParameterType.PARAMETER_TYPE_AGENT_GROUP) for (int i=0;i<model.callcenter.size();i++) {
			String s=model.callcenter.get(i).name;
			for (int j=0;j<model.callcenter.get(i).agents.size();j++) list.add(s+" - "+Language.tr("Importer.AgentsGroup")+" "+(j+1));
		}

		if (selProcessor.getParameterType()==ImporterProcessor.ParameterType.PARAMETER_TYPE_SKILL_TIME) for (int i=0;i<model.skills.size();i++){
			String s=model.skills.get(i).name;
			for (int j=0;j<model.skills.get(i).callerTypeName.size();j++) {
				list.add(s+" - "+model.skills.get(i).callerTypeName.get(j));
			}
		}

		int sel=-1;
		for (int i=0;i<list.size();i++) if (list.get(i).equals(parameter)) {sel=i; break;}

		JComboBox<String> box=new JComboBox<>(list);
		box.setSelectedIndex(Math.max(0,sel));
		box.addActionListener(new TableButtonListener(rowIndex,11,box));
		if (sel<0) new TableButtonListener(rowIndex,11,box).actionPerformed(null);
		return box;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==data.records.size() && columnIndex<4) return "";
		ImporterRecord record=(rowIndex<data.records.size())?data.records.get(rowIndex):null;
		switch (columnIndex) {
		case 0: return getTypeSelect(rowIndex);
		case 1: return getParameterSelect(rowIndex);
		case 2: return (record==null)?"":record.cellStart;
		case 3: return (record==null)?"":record.cellEnd;
		case 4: return getToolsCellValue(rowIndex);
		default: return "";
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (rowIndex==data.records.size()) return;
		if (!(aValue instanceof String)) return;
		String s=(String)aValue;

		ImporterRecord record=data.records.get(rowIndex);
		switch (columnIndex) {
		case 2: if (!record.cellStart.equals(s)) {record.cellStart=s; updateTable();} break;
		case 3: if (!record.cellEnd.equals(s)) {record.cellEnd=s; updateTable();} break;
		}
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return rowIndex<data.records.size() || columnIndex==4;
	}

	@Override
	public final String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Importer.Type");
		case 1: return Language.tr("Importer.Parameter");
		case 2: return Language.tr("Importer.StartCell");
		case 3: return Language.tr("Importer.EndCell");
		case 4: return Language.tr("Dialog.Button.Tools");
		default: return "";
		}
	}

	/**
	 * Führt eine Verarbeitung gemäß der Import-Konfiguration durch.
	 * @param table	Tabelle der die Daten entnommen werdne sollen.
	 * @return	Neues Callcenter-Modell in das die Daten eingefügt wurden
	 */
	public final CallcenterModel process(final MultiTable table) {
		CallcenterModel newModel=data.process(model,table);
		if (newModel==null) {
			MsgBox.error(owner,Language.tr("Importer.Error.DataImport"),data.getProcessError());
			return null;
		}
		model=newModel;
		return newModel;
	}

	/**
	 * Aktualisiert die Tabellendarstellung.
	 */
	private final void updateTable() {
		fireTableDataChanged();
		if (tableUpdateCallback!=null) tableUpdateCallback.run();
	}

	/**
	 * Verschiebt einen Eintrag in der Liste um eine Position nach oben.
	 * @param index	Index des nach oben zu verschiebender Eintrags
	 */
	public final void moveUp(final int index) {
		data.moveUp(index);
		updateTable();
	}

	/**
	 * Verschiebt einen Eintrag in der Liste um eine Position nach unten.
	 * @param index	Index des nach unten zu verschiebender Eintrags
	 */
	public final void moveDown(final int index) {
		data.moveDown(index);
		updateTable();
	}

	/**
	 * Prüft, ob die Einstellungen verworfen werden dürfen (da keine ungespeicherten Änderungen vorliegen).
	 * @param silent	Soll im Fall von vorliegenden ungespeicherten Änderungen eine Speicher-Frage angezeigt werden?
	 * @return	Gibt <code>true</code> zurück, wenn die Daten verworfen werden dürfen
	 */
	public final boolean discardOk(final boolean silent) {
		if (data.equalsImporterData(dataSaved)) return true;
		if (silent) return false;

		int i=MsgBox.confirmSave(owner,Language.tr("Importer.UnsavedWarning.Title"),Language.tr("Importer.UnsavedWarning.Info"));
		if (i==JOptionPane.YES_OPTION) return saveToFile();
		if (i==JOptionPane.NO_OPTION) return true;
		return false;
	}

	/**
	 * Löscht alle Einträge und erstellt eine neue Import-Konfiguration.
	 * @return	Liefert <code>true</code>, wenn eine neue Import-Konfiguration angelegt werden konnte
	 */
	public final boolean newModel() {
		if (!discardOk(false)) return false;
		data.clear();
		dataSaved=data.clone();
		updateTable();
		return true;
	}

	/**
	 * Lädt eine Import-Konfiguration aus einer Datei.
	 * @param file	Dateiname der zu ladenden Import-Konfigurationsdatei
	 * @return	Liefert im Erfolgsfall <code>null</code> sonst eine Fehlermeldung
	 */
	public final String loadFromFile(final File file) {
		String error=data.loadFromFile(file);
		if (error==null) {
			dataSaved=data.clone();
			updateTable();
		}
		return error;
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer zu ladenden Import-Konfiguration an und lädt diese dann.
	 * @return	Liefert <code>false</code>, wenn die Dateiauswahl abgebrochen wurde oder das Laden fehlgeschlagen ist; sonst <code>true</code>.
	 */
	public final boolean loadFromFile() {
		if (!discardOk(false)) return false;
		boolean b=data.loadFromFile(owner);
		if (b) {
			dataSaved=data.clone();
			updateTable();
		}
		return b;
	}

	/**
	 * Speichert eine Import-Konfiguration in einer Datei.
	 * @param file	Dateiname unter dem die aktuelle Import-Konfiguration gespeichert werden soll
	 * @return	Liefert <code>true</code>, wenn die Daten erfolgreich gespeichert werden konnten
	 */
	public final boolean saveToFile(final File file) {
		boolean b=data.saveToFile(file);
		if (b) dataSaved=data.clone();
		return b;
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Datei für die Speicherung der Import-Konfiguration an und speichert die Daten dann in dieser Datei.
	 * @return	Liefert <code>false</code>, wenn die Dateiauswahl abgebrochen wurde oder das Speichern fehlgeschlagen ist; sonst <code>true</code>.
	 */
	public final boolean saveToFile() {
		boolean b=data.saveToFile(owner);
		if (b) dataSaved=data.clone();
		return b;
	}

	/**
	 * Reagiert auf Klicks auf die Schaltflächen in der Tabelle
	 */
	private final class TableButtonListener implements ActionListener {
		/** Tabellenzeile */
		private final int row;
		/** Auszuführende Aktion (0: nach oben, 1: nach unten, 2: löschen, 3: neu, ...) */
		private final int nr;
		/** Combobox auf dessen aktuellen Eintrag ist sich Aktion (im Modus 11) beziehen soll */
		private final JComboBox<String> object;

		/**
		 * Konstruktor der Klasse
		 * @param row	Tabellenzeile
		 * @param nr	Auszuführende Aktion (0: nach oben, 1: nach unten, 2: löschen, 3: neu, ...)
		 * @param object	Combobox auf dessen aktuellen Eintrag ist sich Aktion (im Modus 11) beziehen soll
		 */
		public TableButtonListener(final int row, final int nr, final JComboBox<String> object) {
			this.row=row;
			this.nr=nr;
			this.object=object;
		}

		/**
		 * Konstruktor der Klasse
		 * @param row	Tabellenzeile
		 * @param nr	Auszuführende Aktion (0: nach oben, 1: nach unten, 2: löschen, 3: neu, ...)
		 */
		public TableButtonListener(int row, int nr) {
			this(row,nr,null);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (nr) {
			case 0: moveUp(row); return;
			case 1: moveDown(row); return;
			case 2: data.records.remove(row); updateTable(); return;
			case 3: data.records.add(new ImporterRecord()); updateTable(); return;
			case 10: data.records.get(row).setProcessorFromName((String)(object.getSelectedItem())); updateTable(); return;
			case 11: data.records.get(row).parameter=(String)(object.getSelectedItem()); return;
			}
		}
	}
}
