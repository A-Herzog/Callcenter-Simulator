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
package ui.connected;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import tools.JTableExtAbstractTableModel;
import ui.editor.BaseEditDialog;
import ui.images.Images;
import xml.XMLTools;

/**
 * Diese Klasse enthält das Datenmodell für die Tabelle zur Bearbeitung
 * der einzelnen Simulationsschritte für eine verbundene Simulation.
 * @author Alexanderr Herzog
 * @see ConnectedPanel
 * @see ConnectedModel
 */
public final class ConnectedJTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4974472350493833926L;

	/** Übergeordnetes Fenster */
	private final Window owner;
	/** Hilfe-Callback */
	private final Runnable helpCallbackModel;
	/** Callback das aufgerufen wird, wenn die Tabelle aktualisiert werden soll */
	private final Runnable tableUpdateCallback;

	/**
	 * Modell für für einen verbundenen Simulationslauf
	 * @see #getModel()
	 */
	private final ConnectedModel model=new ConnectedModel();

	/**
	 * Kopie von {@link #model}, um prüfen zu können,
	 * ob das Modell seit dem letzten Speichern verändert wurde.
	 */
	private ConnectedModel modelSaved=new ConnectedModel();

	/**
	 * Popupmenü für die Modellauswahl
	 */
	private final JPopupMenu popupModel;

	/**
	 * Popupmenü zur Konfiguration der Statistik
	 */
	private final JPopupMenu popupStatistic;

	/**
	 * Zeile, auf die sich das jeweils aktuelle Popupmenü beziehen soll
	 */
	private int popupRow=-1;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param helpCallbackModel	Hilfe-Callback
	 * @param tableUpdateCallback	Callback das aufgerufen wird, wenn die Tabelle aktualisiert werden soll
	 */
	public ConnectedJTableModel(final Window owner, final Runnable helpCallbackModel, final Runnable tableUpdateCallback) {
		this.owner=owner;
		this.helpCallbackModel=helpCallbackModel;
		this.tableUpdateCallback=tableUpdateCallback;

		JMenuItem item;

		popupModel=new JPopupMenu();
		popupModel.add(item=new JMenuItem(Language.tr("Connected.SelectModel.LastDay"),Images.SIMULATION_CONNECTED_SELECT_MODEL_LAST.getIcon()));
		item.addActionListener(new TableButtonListener(-1,10));
		popupModel.add(item=new JMenuItem(Language.tr("Connected.SelectModel.SelectFile"),Images.SIMULATION_CONNECTED_SELECT_MODEL_FILE.getIcon()));
		item.addActionListener(new TableButtonListener(-1,11));

		popupStatistic=new JPopupMenu();
		popupStatistic.add(item=new JMenuItem(Language.tr("Connected.SaveStatistic.No"),Images.SIMULATION_CONNECTED_SAVE_MODE_NO.getIcon()));
		item.addActionListener(new TableButtonListener(-1,20));
		popupStatistic.add(item=new JMenuItem(Language.tr("Connected.SaveStatistic.SelectFile"),Images.SIMULATION_CONNECTED_SAVE_MODE_SELECT_FILE.getIcon()));
		item.addActionListener(new TableButtonListener(-1,21));
	}

	/**
	 * Liefert eine <b>Kopie</b> des in der Tabelle angezeigten Modell.
	 * @return	Modell für für einen verbundenen Simulationslauf
	 */
	public ConnectedModel getModel() {
		return model.clone();
	}

	/**
	 * Legt das Basisverzeichnis für Modelle und Statistiken fest.
	 * @param newDefaultFolder	Basisverzeichnis für Modelle und Statistiken
	 */
	public void setDefaultFolder(final String newDefaultFolder) {
		model.defaultFolder=newDefaultFolder;
	}

	/**
	 * Legt die Statistikdatei fest aus der der Übertrag in den ersten Simulationstag hinein bestimmt werden soll.
	 * @param newDay0Statistics	Statistikdatei für den Übertrag in den ersten Simulationstag hinein
	 */
	public void setDay0Statistics(final String newDay0Statistics) {
		model.statisticsDay0=newDay0Statistics;
	}

	/**
	 * Trägt zusätzliche Wiederholer für den ersten Simulationstag (aus dem Tag 0 heraus) in das Modell ein.
	 * @param names	Namen der Anrufergruppen
	 * @param count	Anzahlen an Wiederholern
	 */
	public void setAddditionalDay0Caller(final List<String> names, final List<Integer> count) {
		model.additionalDay0CallerNames.clear();
		model.additionalDay0CallerNames.addAll(names);
		model.additionalDay0CallerCount.clear();
		model.additionalDay0CallerCount.addAll(count);
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Connected.Columns.ModelFile");
		case 1: return Language.tr("Connected.Columns.StatisticFile");
		case 2: return Language.tr("Connected.Columns.CarryOver");
		case 3: return Language.tr("Connected.Columns.Tools");
		default: return "";
		}
	}

	@Override
	public int getRowCount() {
		return model.models.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	/**
	 * Liefert die Zelle zur Anzeige/Auswahl des Modells für eine Tabellenzeile.
	 * @param rowIndex	Tabellenzeile
	 * @return	Zelle zur Anzeige/Auswahl des Modells
	 */
	private JPanel getModelCellValue(final int rowIndex) {
		String title=model.models.get(rowIndex);
		if (title.isEmpty()) {
			if (rowIndex==0) title="<font color=\"red\">"+Language.tr("Connected.SelectModel.State.NoModel")+"</font>"; else title="("+Language.tr("Connected.SelectModel.State.SameAsLastDay")+")";
		} else {
			title="<tt>"+title+"</tt>";
		}
		title="<html><body><b>"+Language.tr("Connected.Day")+" "+(rowIndex+1)+":</b><br>"+title+"</body></html>";
		return makeEditPanel(title,Images.GENERAL_SELECT_FILE.getIcon(),new TableButtonListener(rowIndex,4));
	}

	/**
	 * Liefert die Zelle zur Anzeige/Auswahl der Statistikeinstellungen für eine Tabellenzeile.
	 * @param rowIndex	Tabellenzeile
	 * @return	Zelle zur Anzeige/Auswahl der Statistikeinstellungen
	 */
	private JPanel getStatisticCellValue(final int rowIndex) {
		String title=model.statistics.get(rowIndex);
		if (title.isEmpty()) {
			String c=(rowIndex==model.models.size()-1)?"red":"orange";
			title="<font color=\""+c+"\">("+Language.tr("Connected.SaveStatistic.State.DoNotSave")+")</font>";
		} else {
			title="<tt>"+title+"</tt>";
		}
		title="<html><body>"+title+"</body></html>";
		return makeEditPanel(title,Images.GENERAL_SELECT_FILE.getIcon(),new TableButtonListener(rowIndex,5));
	}

	/**
	 * Liefert die Zelle zur Anzeige/Auswahl des Übertrags für eine Tabellenzeile.
	 * @param rowIndex	Tabellenzeile
	 * @return	Zelle zur Anzeige/Auswahl des Übertrags
	 */
	private JPanel getUebertragCellValue(final int rowIndex) {
		StringBuilder title=new StringBuilder();

		Map<String,ConnectedModelUebertrag> m=model.uebertrag.get(rowIndex);
		String[] keys=m.keySet().toArray(new String[0]);
		if (keys.length>0) {
			if (keys.length==1 && keys[0].isEmpty()) {
				/* einheitlich für alle Kundentypen */
				title.append(Language.tr("Connected.AllClientTypes")+": "+NumberTools.formatPercent(m.get(keys[0]).probability));
			} else {
				/* individuell */
				for (int i=0;i<keys.length;i++) if (!keys[i].isEmpty()) {
					if (title.length()>0) title.append("<br>");
					title.append(keys[i]+": "+NumberTools.formatPercent(m.get(keys[i]).probability));
					if (m.get(keys[i]).changeRates.size()>0) title.append(" +"+Language.tr("Connected.ClientTypeChanges"));
				}
			}
		}
		if (title.length()==0) title.append("<font color=\"orange\">("+Language.tr("Connected.NoCarryOver")+")</font>"); else title=new StringBuilder("<font size=\"2\">"+title.toString()+"</font>");
		return makeEditPanel("<html><body>"+title.toString()+"</body></html>",Images.SIMULATION_CONNECTED_CARRY_OVER.getIcon(),new TableButtonListener(rowIndex,6));
	}

	/**
	 * Liefert die Zelle mit den Tools-Schaltflächen für eine Tabellenzeile.
	 * @param rowIndex	Tabellenzeile
	 * @return	Zelle mit den Tools-Schaltflächen
	 */
	private JPanel getToolsCellValue(final int rowIndex) {
		if (rowIndex==model.models.size()) {
			return makeButtonPanel(
					new String[]{Language.tr("Dialog.Button.Add")},
					new String[]{Language.tr("Connected.Button.Add.Info")},
					new Icon[]{Images.EDIT_ADD.getIcon()},
					new ActionListener[]{new TableButtonListener(rowIndex,3)}
					);
		}
		return makeButtonPanel(
				new String[]{"","",""},
				new String[]{
						Language.tr("Connected.Button.Up.Info"),
						Language.tr("Connected.Button.Down.Info"),
						Language.tr("Connected.Button.Delete.Info")
				},
				new Icon[]{Images.ARROW_UP.getIcon(),Images.ARROW_DOWN.getIcon(),Images.EDIT_DELETE.getIcon()},
				new ActionListener[]{new TableButtonListener(rowIndex,0),new TableButtonListener(rowIndex,1),new TableButtonListener(rowIndex,2)}
				);
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		switch (columnIndex) {
		case 0:	return (rowIndex==model.models.size())?makeEmptyPanel():getModelCellValue(rowIndex);
		case 1:	return (rowIndex==model.models.size())?makeEmptyPanel():getStatisticCellValue(rowIndex);
		case 2:	return (rowIndex==model.models.size())?makeEmptyPanel():getUebertragCellValue(rowIndex);
		case 3: return getToolsCellValue(rowIndex);
		default: return null;
		}
	}

	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		return rowIndex<model.models.size() || columnIndex==3;
	}

	/**
	 * Darf das aktuelle Modell verworfen werden? (Ggf. Nutzer fragen)
	 * @return	Liefert <code>true</code>, wenn das aktuelle Modell verworfen werden darf.
	 */
	private boolean discardOk() {
		if (model.equalsConnectedModel(modelSaved)) return true;

		int i=MsgBox.confirmSave(owner,Language.tr("Connected.SaveConfirm.Title"),Language.tr("Connected.SaveConfirm.Info"));
		if (i==JOptionPane.YES_OPTION) return saveToFile();
		if (i==JOptionPane.NO_OPTION) return true;
		return false;
	}


	/**
	 * Löscht das bestehende Modell
	 * @param withConfirmation	Wenn das Modell seit dem letzten Speichern verändert wurde, soll der Nutzer gefragt werden (<code>true</code>) oder soll das Modell ohne Rückfrage gelöscht werden (<code>false</code>)
	 * @return	Gibt <code>true</code> zurück, wenn das Modell gelöscht werden konnte
	 */
	public boolean clear(final boolean withConfirmation) {
		if (withConfirmation) {
			if (!discardOk()) return false;
		}
		model.clear();
		modelSaved=model.clone();
		updateTable();
		return true;
	}

	/**
	 * Zeigt ein Popupmenü für eine bestimmte Tabellenzeile an.
	 * @param parent	Übergeordnetes Element zum Ausrichten des Menüs
	 * @param popup	Anzuzeigendes Popupmenü
	 * @param row	Tabellenzeile auf die sich das Menü bezieht
	 * @see #popupRow
	 */
	private void showPopup(final JButton parent, final JPopupMenu popup, final int row) {
		popupRow=row;
		popup.show(parent,0,parent.getBounds().height);
	}

	/**
	 * Zeigt den Dialog zur Auswahl einer Statistikdatei für eine Tabellenzeile an.
	 * @param row	Tabellenzeile
	 */
	private void selectStatisticFile(final int row) {
		if (row<0 || row>=model.models.size()) return;

		String s=model.defaultFolder;
		String t=model.statistics.get(row);
		if (!t.isEmpty() && (t.contains("/") || t.contains("\\"))) {File file=new File(t); s=file.getParent();}

		File file=XMLTools.showSaveDialog(owner,Language.tr("Connected.SaveStatistic"),new File(s));
		if (file==null) return;

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return;
		}

		File folder=new File(model.defaultFolder);
		if (folder.equals(file.getParentFile())) model.statistics.set(row,file.getName()); else model.statistics.set(row,file.toString());
		updateTable();
	}

	/**
	 * Zeigt den Dialog zur Auswahl einer Modelldatei für eine Tabellenzeile an.
	 * @param row	Tabellenzeile
	 */
	private void selectModelFile(final int row) {
		if (row<0 || row>=model.models.size()) return;

		String s=model.defaultFolder;
		String t=model.statistics.get(row);
		if (!t.isEmpty() && (t.contains("/") || t.contains("\\"))) {File file=new File(t); s=file.getParent();}

		File file=XMLTools.showLoadDialog(owner,Language.tr("Connected.LoadModel"),new File(s));
		if (file==null) return;

		if (!file.exists()) {
			MsgBox.error(owner,Language.tr("Connected.LoadModelDoesNotExist.Title"),String.format(Language.tr("Connected.LoadModelDoesNotExist.Info"),file.toString()));
			return;
		}

		File folder=new File(model.defaultFolder);
		if (folder.equals(file.getParentFile())) model.models.set(row,file.getName()); else model.models.set(row,file.toString());
		updateTable();
	}

	/**
	 * Wandelt einen möglicher Weise relativen Pfad
	 * in eine absolute Dateiangabe um.
	 * @param name	Relativer oder absoluter Pfad zu Datei
	 * @return	Dateiobjekt
	 */
	private File getFileFromName(final String name) {
		if (name.isEmpty()) return null;
		File file=null;

		if (name.contains("/") || name.contains("\\")) {
			/* absoluter Pfad */
			file=new File(name);
		} else {
			/* relativer Pfad */
			File folder=new File(model.defaultFolder);
			file=new File(folder,name);
		}
		return file.exists()?file:null;
	}

	/**
	 * Bearbeitet die Übertrags-Einstellungen für eine bestimmte Tabellenzeile.
	 * @param row	Tabellenzeile
	 */
	private void editUebertrag(final int row) {
		if (row<0) return;

		File modelFile=null;
		File statisticFile=null;

		int useRow=row-1;
		while (useRow>=0) {
			modelFile=getFileFromName(model.models.get(useRow));
			if (modelFile!=null) break;
			useRow--;
		}

		useRow=row-1;
		if (modelFile==null) {
			while (useRow>=0) {
				statisticFile=getFileFromName(model.statistics.get(useRow));
				if (statisticFile!=null) break;
				useRow--;
			}
			if (statisticFile==null) statisticFile=getFileFromName(model.statisticsDay0);
		}

		ConnectedUebertragEditDialog editDialog=new ConnectedUebertragEditDialog(owner,helpCallbackModel,modelFile,statisticFile,model.uebertrag.get(row));
		editDialog.setVisible(true);
		if (editDialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
		updateTable();
	}

	/**
	 * Reagiert auf Klicks auf die Schaltfläche in der Tabelle
	 */
	private final class TableButtonListener implements ActionListener {
		/** Tabellenzeile */
		private final int row;
		/** Auszuführende Aktion (0: nach oben, 1: nach unten, 2: löschen, 3: hinzufügen, ...) */
		private final int nr;

		/**
		 * Konstruktor der Klasse
		 * @param row	Tabellenzeile
		 * @param nr	Auszuführende Aktion (0: nach oben, 1: nach unten, 2: löschen, 3: hinzufügen, ...)
		 */
		public TableButtonListener(final int row, final int nr) {
			this.row=row;
			this.nr=nr;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (nr) {
			case 0: if (model.moveUp(row)) updateTable(); return;
			case 1: if (model.moveDown(row)) updateTable(); return;
			case 2: if (model.delRecord(row)) updateTable(); return;
			case 3: model.addRecord(); updateTable(); return;
			case 4: if (row==0) selectModelFile(row); else showPopup((JButton)(e.getSource()),popupModel,row); return;
			case 5: if (row==model.models.size()-1) selectStatisticFile(row); else showPopup((JButton)(e.getSource()),popupStatistic,row); return;
			case 6: editUebertrag(row); return;
			case 10: model.models.set(popupRow,""); updateTable(); return;
			case 11: selectModelFile(popupRow); return;
			case 20: model.statistics.set(popupRow,""); updateTable(); return;
			case 21: selectStatisticFile(popupRow); return;
			}
		}
	}

	/**
	 * Aktualisiert die Tabellendarstellung.
	 */
	private void updateTable() {
		fireTableDataChanged();
		if (tableUpdateCallback!=null) tableUpdateCallback.run();
	}

	/**
	 * Speichert das verbundene Modell in der angegebenen Datei.
	 * @param file	Dateiname zum Speichern des verbundenen Modells
	 * @return	Gibt an, ob das Speichern erfolgreich war
	 */
	public boolean saveToFile(final File file) {
		return model.saveToFile(file);
	}

	/**
	 * Fragt nach einem Dateinamen für das verbundene Modell und speichert es dann in der angegegebenen Datei.
	 * @return	Wurde der Auswahldialog abgebrochen oder ist das Speichern fehlgeschlagen, so wird <code>false</code> geliefert, sonst <code>true</code>.
	 */
	public boolean saveToFile() {
		File file=XMLTools.showSaveDialog(owner,Language.tr("Connected.SaveConnectedModel"),new File(model.defaultFolder));
		if (file==null) return false;

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return false;
		}

		if (!saveToFile(file)) {
			MsgBox.error(owner,Language.tr("Connected.Error.SaveConnectedModel.Title"),String.format(Language.tr("Connected.Error.SaveConnectedModel.Info"),file.toString()));
			return false;
		}

		modelSaved=model.clone();
		return true;
	}

	/**
	 * Lädt ein verbundenes Modell aus der angegebenen Datei.
	 * @param file	Datei aus der dasverbundenen Modells geladen werden soll
	 * @return	Gibt an, ob das Laden erfolgreich war
	 */
	public String loadFromFile(final File file) {
		String s=model.loadFromFile(file);
		if (s==null) {
			modelSaved=model.clone();
			updateTable();
		}
		return s;
	}

	/**
	 * Fragt nach einer zu ladenden Datei und lädt dann das verbundene Modell aus der angegegebenen Datei.
	 * @return	Wurde der Auswahldialog abgebrochen oder ist das Laden fehlgeschlagen, so wird <code>false</code> geliefert, sonst <code>true</code>.
	 */
	public boolean loadFromFile() {
		if (!discardOk()) return false;

		File file=XMLTools.showLoadDialog(owner,Language.tr("Connected.LoadConnectedModel"),new File(model.defaultFolder));
		if (file==null) return false;

		if (!file.exists()) {
			MsgBox.error(owner,Language.tr("Connected.Error.LoadConnectedModel.Title"),String.format(Language.tr("Connected.Error.LoadConnectedModel.InfoNotExist"),file.toString()));
			return false;
		}

		String s=loadFromFile(file);
		if (s!=null) {
			MsgBox.error(owner,Language.tr("Connected.Error.LoadConnectedModel.Title"),String.format(Language.tr("Connected.Error.LoadConnectedModel.InfoLoadError"),file.toString())+"\n"+s);
			return false;
		}

		return true;
	}
}