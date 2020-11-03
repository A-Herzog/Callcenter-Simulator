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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.Table;
import systemtools.MsgBox;
import ui.images.Images;
import ui.model.CallcenterModel;

/**
 * Abstrakte Basisklasse für Importfilter, Technion-Daten laden.
 * @author Alexander Herzog
 * @version 1.0
 * @see SpecialProcessingDialog
 * @see AbstractSpecialProcessing
 */
public abstract class AbstractTechnionProcessing extends AbstractSpecialProcessing {
	/** Eingabefeld für die Eingabedatei */
	private JTextField fileInput;
	/** Dateiauswahl-Schaltfläche für die Eingabedatei */
	private JButton fileInputButton;
	/** Auswahlbox für den Tag */
	private JComboBox<String> tagSelect;
	/** Verfügbare Tage */
	private final Vector<String> tagList=new Vector<String>();
	/** Eingabefeld für die Ausgabedatei */
	private JTextField fileOutput=null;
	/** Dateiauswahl-Schaltfläche für die Ausgabedatei */
	private JButton fileOutputButton=null;

	/**
	 * Wird dieses Feld vor dem Aufruf von {@link #getPanel()} auf
	 * <code>true</code> gesetzt, so wird eine Eingabezeile zur
	 * Definition einer Ausgabedatei angezeigt. Auf diese kann
	 * über den zweiten Parameter von {@link #processTechnion(TechnionLoader, File)}
	 * zugegriffen werden.
	 */
	protected boolean showOutputSelect=false;

	/**
	 * Wird dieses Feld vor dem Aufruf von {@link #getPanel()} mit
	 * einem nichtleeren String befüllt, so wird dieser im Hauptbereich
	 * des Panels angezeigt.
	 */
	protected String infoText="";

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 */
	protected AbstractTechnionProcessing(final Window owner) {
		super(owner);
	}

	@Override
	protected final JPanel createPanel() {
		final JPanel main=new JPanel(new BorderLayout());
		JPanel top,center;

		/* Konfigurationsbereich */
		main.add(top=new JPanel(),BorderLayout.NORTH);
		top.setLayout(new BoxLayout(top,BoxLayout.Y_AXIS));
		main.add(center=new JPanel(),BorderLayout.CENTER);

		fileInputButton=new JButton(Language.tr("Loader.InputFile.Select"));
		fileInputButton.setToolTipText(Language.tr("Loader.InputFile.Info"));
		fileInputButton.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		fileInputButton.addActionListener(new ButtonListener());
		fileInput=addFileInputLine(top,Language.tr("Loader.InputFile"),"",fileInputButton);
		fileInput.addKeyListener(new FileInputListener());

		tagSelect=new JComboBox<String>();
		addControlWithButton(top,Language.tr("Loader.InputFile.SelectDay"),tagSelect,null);

		if (showOutputSelect) {
			fileOutputButton=new JButton(Language.tr("Loader.OutputFile.Select"));
			fileOutputButton.setToolTipText(Language.tr("Loader.OutputFile.Info"));
			fileOutputButton.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
			fileOutputButton.addActionListener(new ButtonListener());
			fileOutput=addFileInputLine(top,Language.tr("Loader.OutputFile"),"",fileOutputButton);
		}

		/* Infobereich */
		addInfoText(center,infoText);

		return main;
	}

	@Override
	public final boolean checkData() {
		File file=new File(fileInput.getText().trim());
		if (!file.exists()) {
			MsgBox.error(owner,Language.tr("Loader.Error.InputFileDoesNotExist.Title"),String.format(Language.tr("Loader.Error.InputFileDoesNotExist.Info"),file.toString()));
			return false;
		}

		if (tagList.size()==0) {
			MsgBox.error(owner,Language.tr("Loader.Error.InputFileNoData.Title"),String.format(Language.tr("Loader.Error.InputFileNoData.Info"),file.toString()));
			return false;
		}

		if (tagSelect.getSelectedIndex()<0) {
			MsgBox.error(owner,Language.tr("Loader.Error.NoDaySelected.Title"),Language.tr("Loader.Error.NoDaySelected.Info"));
			return false;
		}

		if (showOutputSelect) {
			if (fileOutput.getText().trim().isEmpty()) {
				MsgBox.error(owner,Language.tr("Loader.Error.NoOutputFileSelected.Title"),Language.tr("Loader.Error.NoOutputFileSelected.Info"));
				return false;
			}

			file=new File(fileOutput.getText().trim());
			if (file.isDirectory()) {
				MsgBox.error(owner,Language.tr("Loader.Error.OutputFileIsDirectory.Title"),String.format(Language.tr("Loader.Error.OutputFileIsDirectory.Info"),file.toString()));
				return false;
			}

			if (file.exists()) {
				if (!MsgBox.confirmOverwrite(owner,file)) return false;
			}
		}

		return true;
	}

	@Override
	public final CallcenterModel process() {
		File inFile=new File(fileInput.getText().trim());
		String tag=tagList.get(tagSelect.getSelectedIndex());
		File outFile=(showOutputSelect)?(new File(fileOutput.getText().trim())):null;

		TechnionLoader loader=new TechnionLoader(inFile,tag);
		if (loader.getLastError()!=null) {
			MsgBox.error(owner,Language.tr("Loader.Error.ProcessingData.Title"),loader.getLastError());
			return null;
		}
		if (!loader.process()) {
			MsgBox.error(owner,Language.tr("Loader.Error.ProcessingData.Title"),loader.getLastError());
			return null;
		}

		return processTechnion(loader,outFile);
	}

	/**
	 * Führt die eigentliche Verarbeitung durch
	 * @param loader	Eigentlicher Loader, der die Verarbeitung durchführt
	 * @param outFile	Zusätzlich ausgewählte Ausgabedatei, siehe {@link #showOutputSelect}
	 * @return	Gibt optional ein Callcenter Modell zurück, in den Editor geladen werden soll.
	 * @see TechnionLoader
	 */
	protected abstract CallcenterModel processTechnion(final TechnionLoader loader, final File outFile);

	/**
	 * Liefert eine Liste der in einer Datei verfügbaren Tage.
	 * @param fileName	Eingabedatei
	 * @return	Verfügbare Tage
	 */
	private final List<String> getDays(String fileName) {
		if (fileName==null || fileName.isEmpty()) return null;
		File file=new File(fileName);
		if (!file.exists()) return null;

		Table table=new Table();
		if (!table.load(file)) return null;
		if (table.getSize(0)<2) return null;

		List<String> row=table.getLine(0);
		int nr=-1;
		for (int i=0;i<row.size();i++) if (row.get(i).equalsIgnoreCase("date")) {nr=i; break;}
		if (nr<0) return null;

		List<String> col=new Vector<String>();
		for (int i=1;i<table.getSize(0);i++) {
			String value=table.getValue(i,nr);
			if (!col.contains(value)) col.add(value);
		}

		return col;
	}

	/**
	 * Übernimmt die neuen Eingaben in {@link #fileInput}.
	 * @see #fileInput
	 */
	private final void inputFileChanged() {
		String selectedTag=(tagSelect.getModel().getSelectedItem()==null)?null:tagSelect.getModel().getSelectedItem().toString();
		tagList.clear();
		List<String> days=getDays(fileInput.getText().trim()); if (days!=null) tagList.addAll(days);
		tagSelect.setModel(new DefaultComboBoxModel<String>(tagList));
		if (tagList.size()>0) tagSelect.setSelectedIndex(Math.max(0,tagList.indexOf(selectedTag)));
	}

	/**
	 * Reagiert auf Klicks auf {@link AbstractTechnionProcessing#fileInputButton}
	 * und auf {@link AbstractTechnionProcessing#fileOutputButton}.
	 * @see AbstractTechnionProcessing#fileInputButton
	 * @see AbstractTechnionProcessing#fileOutputButton
	 */
	private final class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==fileInputButton) {
				File folder=null;
				if (!fileInput.getText().trim().isEmpty()) {folder=new File(fileInput.getText()); folder=folder.getParentFile();}
				File file=TableLoader.getTextFileToLoad(owner,folder); if (file==null) return;
				fileInput.setText(file.toString());
				inputFileChanged();
			}
			if (e.getSource()==fileOutputButton) {
				File folder=null;
				if (!fileOutput.getText().trim().isEmpty()) {folder=new File(fileOutput.getText()); folder=folder.getParentFile();}
				File file=Table.showSaveDialog(owner,Language.tr("FileType.Save.Table"),folder,null,null); if (file==null) return;
				fileOutput.setText(file.toString());
			}
		}
	}

	/**
	 * Reagiert auf Eingabe in
	 * {@link AbstractTechnionProcessing#fileInput}.
	 * @see AbstractTechnionProcessing#fileInput
	 */
	private final class FileInputListener implements KeyListener {
		@Override public void keyPressed(KeyEvent arg0) {inputFileChanged();}
		@Override public void keyReleased(KeyEvent arg0) {inputFileChanged();}
		@Override public void keyTyped(KeyEvent arg0) {inputFileChanged();}
	}
}
