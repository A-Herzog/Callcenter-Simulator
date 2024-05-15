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
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.Table;
import systemtools.MsgBox;
import ui.images.Images;
import ui.model.CallcenterModel;

/**
 * Kombiniert ein bestehendes Callcenter-Modell mit weiteren Daten,
 * die aus einer Tabelle geladen werden.
 * @author Alexander Herzog
 * @see AbstractSpecialProcessing
 */
public class XMLPreprocessing extends AbstractSpecialProcessing {
	/** Eingabefeld für den Dateinamen des Eingabemodells */
	private JTextField xmlFileInput;
	/** Schaltfläche zur Auswahl des Dateinamens des Eingabemodells für {@link #xmlFileInput} */
	private JButton xmlFileInputButton;
	/** Eingabefeld für den Dateinamen der Eingabetabelle */
	private JTextField tableFileInput;
	/** Schaltfläche zur Auswahl des Dateinamens der Eingabetabelle für {@link #tableFileInput} */
	private JButton tableFileInputButton;
	/** Eingabefeld für den Dateinamen der Ausgabedatei */
	private JTextField xmlFileOutput;
	/** Schaltfläche zur Auswahl des Dateinamens der Ausgabedatei für {@link #xmlFileOutput} */
	private JButton xmlFileOutputButton;
	/** In den Editor zu ladendes Callcenter-Modell */
	private CallcenterModel model;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 */
	public XMLPreprocessing(final Window owner) {
		super(owner);
	}

	@Override
	public String getName() {
		return Language.tr("Loader.Info.XMLPreprocessing.Title");
	}

	@Override
	protected JPanel createPanel() {
		final JPanel main=new JPanel(new BorderLayout());
		JPanel top,center;

		/* Konfigurationsbereich */
		main.add(top=new JPanel(),BorderLayout.NORTH);
		top.setLayout(new BoxLayout(top,BoxLayout.Y_AXIS));
		main.add(center=new JPanel(),BorderLayout.CENTER);

		xmlFileInputButton=new JButton(Language.tr("Loader.InputFile.Select"));
		xmlFileInputButton.setToolTipText(Language.tr("Loader.InputFile.Info"));
		xmlFileInputButton.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		xmlFileInputButton.addActionListener(new ButtonListener());
		xmlFileInput=addFileInputLine(top,Language.tr("Loader.Info.XMLPreprocessing.InputXMLFile"),"",xmlFileInputButton);

		tableFileInputButton=new JButton(Language.tr("Loader.InputFile.Select"));
		tableFileInputButton.setToolTipText(Language.tr("Loader.InputFile.Info"));
		tableFileInputButton.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		tableFileInputButton.addActionListener(new ButtonListener());
		tableFileInput=addFileInputLine(top,Language.tr("Loader.Info.XMLPreprocessing.InputTableFile"),"",tableFileInputButton);

		xmlFileOutputButton=new JButton(Language.tr("Loader.OutputFile.Select"));
		xmlFileOutputButton.setToolTipText(Language.tr("Loader.OutputFile.Info"));
		xmlFileOutputButton.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		xmlFileOutputButton.addActionListener(new ButtonListener());
		xmlFileOutput=addFileInputLine(top,Language.tr("Loader.OutputFile"),"",xmlFileOutputButton);

		/* Infobereich */
		addInfoText(center,Language.tr("Loader.Info.XMLPreprocessing.Description"));

		return main;
	}

	@Override
	public boolean checkData() {
		final XMLPreprocessor preprocessor=new XMLPreprocessor(new File(xmlFileInput.getText()),new File(tableFileInput.getText()));
		String error;

		error=preprocessor.prepare();
		if (error!=null) {
			MsgBox.error(owner,Language.tr("Loader.Info.XMLPreprocessing.ErrorProcessing"),error);
			return false;
		}
		error=preprocessor.process();
		if (error!=null) {
			MsgBox.error(owner,Language.tr("Loader.Info.XMLPreprocessing.ErrorProcessing"),error);
			return false;
		}

		model=new CallcenterModel();
		error=model.loadFromString(preprocessor.getResult());
		if (error!=null) {
			model=null;
			MsgBox.error(owner,Language.tr("Loader.Info.XMLPreprocessing.ErrorProcessing"),error);
			return false;
		}

		return true;
	}

	@Override
	public CallcenterModel process() {
		return model;
	}

	/**
	 * Reagiert auf Klicks auf die Schaltflächen in dem Panel.
	 */
	private class ButtonListener implements ActionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public ButtonListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==xmlFileInputButton) {
				File folder=null;
				if (!xmlFileInput.getText().isBlank()) {folder=new File(xmlFileInput.getText()); folder=folder.getParentFile();}
				File file=TableLoader.getModelFileToLoad(owner,folder);
				if (file!=null)	xmlFileInput.setText(file.toString());
				return;
			}
			if (e.getSource()==tableFileInputButton) {
				File folder=null;
				if (!tableFileInput.getText().isBlank()) {folder=new File(tableFileInput.getText()); folder=folder.getParentFile();}
				File file=Table.showLoadDialog(owner,Language.tr("Loader.LoadData"),folder);
				if (file!=null)	tableFileInput.setText(file.toString());
				return;
			}
			if (e.getSource()==xmlFileOutputButton) {
				File folder=null;
				if (!xmlFileOutput.getText().isBlank()) {folder=new File(xmlFileOutput.getText()); folder=folder.getParentFile();}
				File file=TableLoader.getModelFileToCreate(owner,folder);
				if (file!=null)	xmlFileOutput.setText(file.toString());
				return;
			}
		}
	}
}