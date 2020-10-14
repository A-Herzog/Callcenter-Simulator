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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.MultiTable;
import mathtools.Table;
import systemtools.MsgBox;
import tools.JTableExt;
import ui.HelpLink;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.specialpanels.JWorkPanel;

/**
 * Ermöglicht den Import von Tabellenspalten in diverse Datenfelder gleichzeitig
 * @author Alexander Herzog
 * @version 1.0
 */
public final class ImporterPanel extends JWorkPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2618492174899808992L;

	/** Übergeordnetes Fenster */
	private final Window owner;
	private CallcenterModel changedModel=null;

	private final JTextField tableField;
	private final JButton tableButton;

	private final ImporterJTableModel tableModel;
	private final JTableExt table;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param model	Callcenter-Modell das die Basis für den Import darstellen soll
	 * @param schablonenDatei	Zu ladende Schablonen-Import-Konfigurationsdatei (kann <code>null</code> sein)
	 * @param doneNotify	Callback wird aufgerufen, wenn das Batch-Panel geschlossen werden soll
	 * @param helpLink	Hilfe-Link
	 */
	public ImporterPanel(final Window owner, final CallcenterModel model, final File schablonenDatei, final Runnable doneNotify, final HelpLink helpLink) {
		super(doneNotify, helpLink.pageTableImport);
		this.owner=owner;

		JPanel main,p,p2,p3;

		/* Main area */
		add(main=new JPanel(new BorderLayout()),BorderLayout.CENTER);

		main.add(p=new JPanel(),BorderLayout.NORTH);
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(new JLabel(Language.tr("Importer.SelectTable.Label")));
		p.add(p2=new JPanel(new BorderLayout()));
		p2.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		p2.add(p3=new JPanel(),BorderLayout.CENTER);
		p3.setLayout(new BoxLayout(p3,BoxLayout.Y_AXIS));
		p3.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));
		p3.add(tableField=new JTextField(60));
		Dimension d2=tableField.getPreferredSize();
		Dimension d3=tableField.getMaximumSize();
		d3.height=d2.height; tableField.setMaximumSize(d3);
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.EAST);
		p3.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		p3.add(tableButton=new JButton(Language.tr("Importer.SelectTable")));
		tableButton.addActionListener(new ButtonListener());
		tableButton.setToolTipText(Language.tr("Importer.SelectTable.Info"));
		tableButton.setIcon(Images.GENERAL_SELECT_FILE_TABLE.getIcon());

		main.add(p=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		tableModel=new ImporterJTableModel(owner,model,()->new Runnable() {@Override public void run() {table.editingStopped(null);}});
		p.add(new JScrollPane(table=new JTableExt(tableModel)));
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		table.setIsPanelCellTable(4);
		table.getColumn(table.getColumnName(2)).setMaxWidth(125);
		table.getColumn(table.getColumnName(3)).setMaxWidth(125);

		/* Bottom line */
		addFooter(Language.tr("Importer.LoadData"),Images.DATA_LOAD_BY_TEMPLATE.getIcon(),"-");
		JButton button;
		button=addFooterButton(Language.tr("Importer.NewTemplate"));
		button.setIcon(Images.MODEL_NEW.getIcon());
		button=addFooterButton(Language.tr("Importer.LoadTemplate"));
		button.setIcon(Images.MODEL_LOAD.getIcon());
		button=addFooterButton(Language.tr("Importer.SaveTemplate"));
		button.setIcon(Images.MODEL_SAVE.getIcon());

		/* Datei laden */
		if (schablonenDatei!=null) {
			String error=tableModel.loadFromFile(schablonenDatei);
			if (error!=null) {
				MsgBox.error(owner,Language.tr("Importer.LoadTemplate.ErrorLoading.Title"),error);
			}
		}
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.specialpanels.JWorkPanel#run()
	 */
	@Override
	protected void run() {
		File tableFile=new File(tableField.getText());
		if (!tableFile.exists()) {
			MsgBox.error(this,Language.tr("Importer.Error.TableDoesNotExist.Title"),String.format(Language.tr("Importer.Error.TableDoesNotExist.Info"),tableFile.toString()));
			return;
		}

		MultiTable table=new MultiTable();
		if (!table.load(tableFile)) {
			MsgBox.error(this,Language.tr("Importer.Error.ErrorLoadingTable.Title"),String.format(Language.tr("Importer.Error.ErrorLoadingTable.Info"),tableFile.toString()));
			return;
		}

		changedModel=tableModel.process(table);
		if (changedModel==null) return;
		if (tableModel.discardOk(true)) requestClose(); else MsgBox.info(this,Language.tr("Importer.Success.Title"),String.format(Language.tr("Importer.Success.Info"),tableFile.toString()));
	}

	@Override
	public void done() {
		if (tableModel.discardOk(false)) super.done();
	}

	/**
	 * Liefert nach dem Abschluss des Importvorgangs das veränderte Modell.
	 * @return	Neues Callcenter-Modell mit eingetragenen neuen Daten
	 */
	public CallcenterModel getResults() {
		return changedModel;
	}

	@Override
	public boolean dragDropLoad(File file) {
		String error=tableModel.loadFromFile(file);
		if (error!=null) {
			MsgBox.error(owner,Language.tr("Importer.LoadTemplate.ErrorLoading.Title"),error);
			return true;
		}
		if (Table.isTableFileName(file)) {
			tableField.setText(file.toString());
			return true;
		}
		return false;
	}

	@Override
	protected void userButtonClick(int index, JButton button) {
		switch (index) {
		case 0: tableModel.newModel(); return;
		case 1: tableModel.loadFromFile(); return;
		case 2: tableModel.saveToFile(); return;
		}
	}

	private final class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			File tableFile=Table.showLoadDialog(owner,Language.tr("Importer.SelectTable"));
			if (tableFile!=null) tableField.setText(tableFile.toString());
		}
	}
}
