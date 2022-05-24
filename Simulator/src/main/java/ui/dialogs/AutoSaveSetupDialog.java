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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import tools.SetupData;
import ui.editor.BaseEditDialog;
import ui.images.Images;

/**
 * Dialog um Einstellungen zur automatischen Speicherung der Statistik-Ergebnisse vorzunehmen
 * @author Alexander Herzog
 * @version 1.0
 */
public class AutoSaveSetupDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -1382190901066407180L;

	/** Wählt aus, ob der Dialog im Batch-Modus oder im Einzel-Simulations-Modus geöffnet werden soll */
	private final boolean batchMode;

	/** Option "Statistik-Ergebnisse speichern" */
	private JCheckBox saveCheckBox;
	/** Eingabefeld für "Statistik-Ergebnisse speichern" */
	private JTextField saveFolder;
	/** Dateiauswahl-Schaltfläche für "Statistik-Ergebnisse speichern" */
	private JButton saveButton;
	/** Option "Gefilterte Statistik-Ergebnisse speichern" */
	private JCheckBox filterCheckBox;
	/** Auswahloption "Schnellzugriff-Skript 1 von der Statistikseite verwenden" */
	private JRadioButton filterInternal;
	/** Auswahloption "Externtes Skript verwenden" */
	private JRadioButton filterExternal;
	/** Eingabefeld "Externes Skript" */
	private JTextField filterExternalFile;
	/** Dateiauswahl-Schaltfläche für "Externes Skript" */
	private JButton filterExternalButton;
	/** Eingabefeld "Ausgabedatei für Filter-Ergebnisse" */
	private JTextField filterOutputFile;
	/** Dateiauswahl-Schaltfläche für "Ausgabedatei für Filter-Ergebnisse" */
	private JButton filterOutputButton;

	/** Datei-Dropper für "Statistik-Ergebnisse speichern"-Eingabefeld */
	private FileDropper drop1;
	/** Datei-Dropper für "Externes Skript"-Eingabefeld */
	private FileDropper drop2;
	/** Datei-Dropper für "Ausgabedatei für Filter-Ergebnisse"-Eingabefeld */
	private FileDropper drop3;

	/**
	 * Erzeugt den Dialog wahlweise im Batch-Modus oder im Einzel-Simulations-Modus.
	 * In den beiden Modi werden verschiedene Optionen angezeigt und die Einstellungen aus
	 * verschiedenen Setup-Feldern geladen bzw. in verschiedenen Setup-Feldern gespeichert.
	 * @param owner	Übergeordnetes Fenster
	 * @param title Titel des Fensters
	 * @param batchMode Wählt aus, ob der Dialog im Batch-Modus oder im Einzel-Simulations-Modus geöffnet werden soll
	 * @param helpCallback Wird hier ein Wert ungleich <code>null</code> übergeben, so wird eine "Hilfe"-Schaltfläche angezeigt und die <code>Run</code>-Methode dieses Objekts beim Klicken auf diese Schaltfläche aufgerufen
	 */
	public AutoSaveSetupDialog(Window owner, String title, boolean batchMode, Runnable helpCallback) {
		super(owner,title,false,helpCallback);
		this.batchMode=batchMode;
		createSimpleGUI(600,500,null,null);
		pack();
	}

	/**
	 * Erzeugt den Dialog im Modus für einzelne Simulationen
	 * @param owner	Übergeordnetes Fenster
	 * @param helpCallback Wird hier ein Wert ungleich <code>null</code> übergeben, so wird eine "Hilfe"-Schaltfläche angezeigt und die <code>Run</code>-Methode dieses Objekts beim Klicken auf diese Schaltfläche aufgerufen
	 */
	public AutoSaveSetupDialog(Window owner, Runnable helpCallback) {
		this(owner,Language.tr("AutoSave.Setup.Title"),false,helpCallback);
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
		JPanel panel1,panel2;

		if (!batchMode) {
			content.add(panel1=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			panel1.add(saveCheckBox=new JCheckBox("<html><body><b>"+Language.tr("AutoSave.Setup.SaveStatistics")+"</b></body></html>"));
			panel1.setMaximumSize(new Dimension(panel1.getMaximumSize().width,panel1.getMinimumSize().height));

			content.add(panel1=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			panel1.add(saveFolder=new JTextField(40),BorderLayout.CENTER);
			panel1.add(saveButton=new JButton(Language.tr("AutoSave.Setup.SelectFolder")),BorderLayout.EAST);
			saveButton.addActionListener(new ButtonListener());
			saveButton.setToolTipText(Language.tr("AutoSave.Setup.SelectFolder.Tooltip"));
			saveButton.setIcon(Images.GENERAL_SELECT_FOLDER.getIcon());
			panel1.setMaximumSize(new Dimension(panel1.getMaximumSize().width,panel1.getMinimumSize().height));

			drop1=new FileDropper(new Component[]{saveCheckBox,saveFolder},new ButtonListener());
		}

		content.add(panel1=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel1.add(filterCheckBox=new JCheckBox("<html><body><b>"+Language.tr("AutoSave.Setup.SaveFiltered")+"</b></body></html>"));
		panel1.setMaximumSize(new Dimension(panel1.getMaximumSize().width,panel1.getMinimumSize().height));

		content.add(panel1=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel1.add(panel2=new JPanel());
		panel2.setLayout(new BoxLayout(panel2,BoxLayout.Y_AXIS));
		ButtonGroup buttonGroup=new ButtonGroup();
		panel2.add(filterInternal=new JRadioButton(Language.tr("AutoSave.Setup.SaveFiltered.UseFirstScript")));
		buttonGroup.add(filterInternal);
		panel2.add(filterExternal=new JRadioButton(Language.tr("AutoSave.Setup.SaveFiltered.UseExternalScript")));
		buttonGroup.add(filterExternal);
		filterInternal.setSelected(true);
		panel1.setMaximumSize(new Dimension(panel1.getMaximumSize().width,panel1.getMinimumSize().height));

		content.add(panel1=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		JLabel filterExternalFileLabel;
		panel1.add(filterExternalFileLabel=new JLabel(Language.tr("AutoSave.Setup.SaveFiltered.UseExternalScript.Label")));
		panel1.setMaximumSize(new Dimension(panel1.getMaximumSize().width,panel1.getMinimumSize().height));

		content.add(panel1=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel1.add(filterExternalFile=new JTextField(40),BorderLayout.CENTER);
		panel1.add(filterExternalButton=new JButton(Language.tr("AutoSave.Setup.SaveFiltered.UseExternalScript.Select")),BorderLayout.EAST);
		filterExternalButton.addActionListener(new ButtonListener());
		filterExternalButton.setToolTipText(Language.tr("AutoSave.Setup.SaveFiltered.UseExternalScript.Select.Tooltip"));
		filterExternalButton.setIcon(Images.GENERAL_SELECT_FILE_SCRIPT.getIcon());
		panel1.setMaximumSize(new Dimension(panel1.getMaximumSize().width,panel1.getMinimumSize().height));

		content.add(panel1=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		JLabel filterOutputFileLabel;
		panel1.add(filterOutputFileLabel=new JLabel(Language.tr("AutoSave.Setup.SaveFiltered.Output.Label")));
		panel1.setMaximumSize(new Dimension(panel1.getMaximumSize().width,panel1.getMinimumSize().height));

		content.add(panel1=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel1.add(filterOutputFile=new JTextField(40),BorderLayout.CENTER);
		panel1.add(filterOutputButton=new JButton(Language.tr("AutoSave.Setup.SaveFiltered.Output.Select")),BorderLayout.EAST);
		filterOutputButton.addActionListener(new ButtonListener());
		filterOutputButton.setToolTipText(Language.tr("AutoSave.Setup.SaveFiltered.Output.Select.Tooltip"));
		filterOutputButton.setIcon(Images.GENERAL_SELECT_FILE_SCRIPT.getIcon());
		panel1.setMaximumSize(new Dimension(panel1.getMaximumSize().width,panel1.getMinimumSize().height));

		drop2=new FileDropper(new Component[]{filterExternalFileLabel,filterExternalFile},new ButtonListener());
		drop3=new FileDropper(new Component[]{filterOutputFileLabel,filterOutputFile},new ButtonListener());

		content.add(Box.createVerticalGlue());

		SetupData setup=SetupData.getSetup();
		if (batchMode) {
			filterCheckBox.setSelected(setup.batchSaveFilter);
			filterInternal.setSelected(setup.batchSaveFilterScript.equals(""));
			filterExternal.setSelected(!setup.batchSaveFilterScript.equals(""));
			filterExternalFile.setText(setup.batchSaveFilterScript);
			filterOutputFile.setText(setup.batchSaveFilterOutput);
		} else {
			saveCheckBox.setSelected(setup.autoSaveStatistic);
			saveFolder.setText(setup.autoSaveStatisticFolder);
			filterCheckBox.setSelected(setup.autoSaveFilter);
			filterInternal.setSelected(setup.autoSaveFilterScript.equals(""));
			filterExternal.setSelected(!setup.autoSaveFilterScript.equals(""));
			filterExternalFile.setText(setup.autoSaveFilterScript);
			filterOutputFile.setText(setup.autoSaveFilterOutput);
		}
	}


	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#checkData()
	 */
	@Override
	protected boolean checkData() {
		return true;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#storeData()
	 */
	@Override
	protected void storeData() {
		SetupData setup=SetupData.getSetup();
		if (batchMode) {
			setup.batchSaveFilter=filterCheckBox.isSelected();
			setup.batchSaveFilterScript=(filterInternal.isSelected())?"":filterExternalFile.getText();
			setup.batchSaveFilterOutput=filterOutputFile.getText();
		} else {
			setup.autoSaveStatistic=saveCheckBox.isSelected();
			setup.autoSaveStatisticFolder=saveFolder.getText();
			setup.autoSaveFilter=filterCheckBox.isSelected();
			setup.autoSaveFilterScript=(filterInternal.isSelected())?"":filterExternalFile.getText();
			setup.autoSaveFilterOutput=filterOutputFile.getText();
		}
		setup.saveSetupWithWarning(this);
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Textdatei an.
	 * @param dialogTitle	Dialogtitel
	 * @param oldFileName	Bisherige Datei (kann <code>null</code> oder leer sein)
	 * @return	Neue Datei oder <code>null</code>, wenn die Auswahl abgebrochen wurde
	 */
	private String selectTextFile(final String dialogTitle, final String oldFileName) {
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(dialogTitle);
		FileFilter txt=new FileNameExtensionFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
		fc.addChoosableFileFilter(txt);
		fc.setFileFilter(txt);
		if (oldFileName!=null && !oldFileName.isEmpty()) {
			File oldFile=new File(oldFileName);
			fc.setCurrentDirectory(oldFile.getParentFile());
		}
		if (fc.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0 && fc.getFileFilter()==txt) file=new File(file.getAbsoluteFile()+".txt");

		return file.getAbsolutePath();
	}

	/**
	 * Reagiert auf Klicks auf {@link AutoSaveSetupDialog#saveButton},
	 * {@link AutoSaveSetupDialog#filterExternalButton},
	 * {@link AutoSaveSetupDialog#filterOutputButton} und Drag&amp;drop-Operationen.
	 * @see AutoSaveSetupDialog#saveButton
	 * @see AutoSaveSetupDialog#filterExternalButton
	 * @see AutoSaveSetupDialog#filterOutputButton
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
			if (e.getSource()==saveButton) {
				JFileChooser fc=new JFileChooser();
				CommonVariables.initialDirectoryToJFileChooser(fc);
				fc.setDialogTitle(Language.tr("AutoSave.Setup.SelectFolder"));
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fc.showSaveDialog(AutoSaveSetupDialog.this)!=JFileChooser.APPROVE_OPTION) return;
				CommonVariables.initialDirectoryFromJFileChooser(fc);
				File file=fc.getSelectedFile();
				saveFolder.setText(file.toString());
				saveCheckBox.setSelected(true);
				return;
			}
			if (e.getSource()==filterExternalButton) {
				String s=selectTextFile(Language.tr("AutoSave.Setup.SaveFiltered.UseExternalScript.Select"),filterExternalFile.getText());
				if (s==null) return;
				filterExternalFile.setText(s);
				filterExternal.setSelected(true);
				filterCheckBox.setSelected(true);
			}
			if (e.getSource()==filterOutputButton) {
				String s=selectTextFile(Language.tr("AutoSave.Setup.SaveFiltered.Output.Select"),filterOutputFile.getText());
				if (s==null) return;
				filterOutputFile.setText(s);
				filterCheckBox.setSelected(true);
			}
			if (e.getSource() instanceof FileDropperData) {
				final FileDropperData data=(FileDropperData)e.getSource();
				final File file=data.getFile();

				if (data.getFileDropper()==drop1) {
					if (file.isFile()) {
						saveFolder.setText(file.toString());
						saveCheckBox.setSelected(true);
						data.dragDropConsumed();
					}
				}
				if (data.getFileDropper()==drop2) {
					if (file.isFile()) {
						filterExternalFile.setText(file.toString());
						filterExternal.setSelected(true);
						filterCheckBox.setSelected(true);
						data.dragDropConsumed();
					}
				}
				if (data.getFileDropper()==drop3) {
					if (file.isFile()) {
						filterOutputFile.setText(file.toString());
						filterCheckBox.setSelected(true);
						data.dragDropConsumed();
					}
				}
			}
		}
	}
}
