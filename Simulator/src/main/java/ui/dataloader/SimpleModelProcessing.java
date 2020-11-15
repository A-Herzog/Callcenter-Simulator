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
 * Kapselt die Funktionen, um ein vollständiges Callcenter-Modell aus zwei Tabellen (Anrufer und Agenten) zu laden.
 * @author Alexander Herzog
 * @see AbstractSpecialProcessing
 * @see SimpleModelLoader
 */
public class SimpleModelProcessing extends AbstractSpecialProcessing {
	/** Eingabefeld für die Anrufer-Datei */
	private JTextField fileCallerInput;
	/** Schaltfläche zur Auswahl einer Anrufer-Datei für {@link #fileCallerInput} */
	private JButton fileCallerInputButton;
	/** Eingabefeld für den optionalen Tabellennamen in der Anrufer-Arbeitsmappe */
	private JTextField fileCallerTableNameInput;
	/** Eingabefeld für die erste zu verwendende Spalte in der Anrufer-Tabelle */
	private JTextField fileCallerColumnInput;
	/** Eingabefeld für die Agenten-Datei */
	private JTextField fileAgentsInput;
	/** Schaltfläche zur Auswahl einer Agenten-Datei für {@link #fileAgentsInput} */
	private JButton fileAgentsInputButton;
	/** Eingabefeld für den optionalen Tabellennamen in der Agenten-Arbeitsmappe */
	private JTextField fileAgentsTableNameInput;
	/** Eingabefeld für die erste zu verwendende Spalte in der Agenten-Tabelle */
	private JTextField fileAgentsColumnInput;
	/** Eingabefeld für die Ausgabedatei */
	private JTextField fileOutput;
	/** Schaltfläche zur Auswahl einer Ausgabe-Datei für {@link #fileOutput} */
	private JButton fileOutputButton;
	/** Objekt, welches den eigentlichen Lade-Vorgang durchführt */
	private SimpleModelLoader loader;
	/** Neues Callcenter-Modell */
	private CallcenterModel model;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 */
	public SimpleModelProcessing(final Window owner) {
		super(owner);
	}

	@Override
	public String getName() {
		return Language.tr("Loader.Info.SimpleModel.Title");
	}

	@Override
	protected JPanel createPanel() {
		final JPanel main=new JPanel(new BorderLayout());
		JPanel top,center;

		/* Konfigurationsbereich */
		main.add(top=new JPanel(),BorderLayout.NORTH);
		top.setLayout(new BoxLayout(top,BoxLayout.Y_AXIS));
		main.add(center=new JPanel(),BorderLayout.CENTER);

		fileCallerInputButton=new JButton(Language.tr("Loader.InputFile.Select"));
		fileCallerInputButton.setToolTipText(Language.tr("Loader.InputFile.Info"));
		fileCallerInputButton.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		fileCallerInputButton.addActionListener(new ButtonListener());
		fileCallerInput=addFileInputLine(top,Language.tr("Loader.Info.SimpleModel.InputFileCaller"),"",fileCallerInputButton);
		fileCallerTableNameInput=addInputLine(top,Language.tr("Loader.Info.SimpleModel.InputFileCallerTableName"),"",null);
		fileCallerColumnInput=addInputLine(top,Language.tr("Loader.Info.SimpleModel.InputFileCallerStartColumn"),"A",null);

		fileAgentsInputButton=new JButton(Language.tr("Loader.InputFile.Select"));
		fileAgentsInputButton.setToolTipText(Language.tr("Loader.InputFile.Info"));
		fileAgentsInputButton.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		fileAgentsInputButton.addActionListener(new ButtonListener());
		fileAgentsInput=addFileInputLine(top,Language.tr("Loader.Info.SimpleModel.InputFileAgents"),"",fileAgentsInputButton);
		fileAgentsTableNameInput=addInputLine(top,Language.tr("Loader.Info.SimpleModel.InputFileAgentsTableName"),"",null);
		fileAgentsColumnInput=addInputLine(top,Language.tr("Loader.Info.SimpleModel.InputFileAgentsStartColumn"),"A",null);

		fileOutputButton=new JButton(Language.tr("Loader.OutputFile.Select"));
		fileOutputButton.setToolTipText(Language.tr("Loader.OutputFile.Info"));
		fileOutputButton.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		fileOutputButton.addActionListener(new ButtonListener());
		fileOutput=addFileInputLine(top,Language.tr("Loader.OutputFile"),"",fileOutputButton);

		/* Infobereich */
		addInfoText(center,Language.tr("Loader.Info.SimpleModel.Description"));

		return main;
	}

	@Override
	public boolean checkData() {
		String error;
		loader=new SimpleModelLoader();
		error=loader.setCallerTable(new File(fileCallerInput.getText().trim()),fileCallerTableNameInput.getText().trim(),fileCallerColumnInput.getText().trim());
		if (error!=null) {
			MsgBox.error(owner,Language.tr("Loader.Info.SimpleModel.ErrorProcessingCaller"),error);
			return false;
		}
		error=loader.setAgentsTable(new File(fileAgentsInput.getText().trim()),fileAgentsTableNameInput.getText().trim(),fileAgentsColumnInput.getText().trim());
		if (error!=null) {
			MsgBox.error(owner,Language.tr("Loader.Info.SimpleModel.ErrorProcessingAgents"),error);
			return false;
		}

		Object O=loader.buildModel();
		if (O instanceof String) {
			MsgBox.error(owner,Language.tr("Loader.Info.SimpleModel.ErrorProcessing"),(String)O);
			return false;
		}
		model=(CallcenterModel)O;
		return true;
	}

	@Override
	public CallcenterModel process() {
		String s=fileOutput.getText().trim();
		if (!s.isEmpty()) {
			s=SimpleModelBaseLoader.saveModel(model,new File(s));
			if (s!=null) MsgBox.error(owner,Language.tr("Loader.Info.SimpleModel.ErrorProcessing"),s);
		}
		return model;
	}

	/**
	 * Reagiert auf Klicks auf die Schaltflächen in dem Panel.
	 */
	private final class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==fileCallerInputButton) {
				File folder=null;
				if (!fileCallerInput.getText().trim().isEmpty()) {folder=new File(fileCallerInput.getText()); folder=folder.getParentFile();}
				File file=Table.showLoadDialog(owner,Language.tr("Loader.LoadData"),folder);
				if (file!=null)	fileCallerInput.setText(file.toString());
				return;
			}
			if (e.getSource()==fileAgentsInputButton) {
				File folder=null;
				if (!fileAgentsInput.getText().trim().isEmpty()) {folder=new File(fileAgentsInput.getText()); folder=folder.getParentFile();}
				File file=Table.showLoadDialog(owner,Language.tr("Loader.LoadData"),folder);
				if (file!=null)	fileAgentsInput.setText(file.toString());
				return;
			}
			if (e.getSource()==fileOutputButton) {
				File folder=null;
				if (!fileOutput.getText().trim().isEmpty()) {folder=new File(fileOutput.getText()); folder=folder.getParentFile();}
				File file=TableLoader.getModelFileToCreate(owner,folder);
				if (file!=null)	fileOutput.setText(file.toString());
				return;
			}
		}
	}
}
