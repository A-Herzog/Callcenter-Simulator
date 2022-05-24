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
package ui.compare;

import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import simulator.Statistics;
import systemtools.MsgBox;
import ui.editor.BaseEditDialog;
import ui.images.Images;
import xml.XMLTools;

/**
 * Ermöglicht die Auswahl von zwei Statistik-Dateien, die im Folgenden verglichen werden sollen
 * @author Alexander Herzog
 * @version 1.0
 */
public class CompareSelectDialog extends BaseEditDialog  {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -5204836029062935247L;

	/**
	 * Textfelder zur Eingabe der Dateinamen
	 */
	private final JTextField[] statisticTextFields;

	/**
	 * Schaltflächen zur Auswahl der Dateien über Dialoge
	 */
	private final JButton[] statisticButton;

	/**
	 * Auswahl: Statistikdaten nebeneinander anzeigen
	 */
	private JRadioButton selectNebeneinander;

	/**
	 * Auswahl: Statistikdaten untereinander anzeigen
	 */
	private JRadioButton selectUntereinander;

	/**
	 * Gewählte Dateien
	 * @see #storeData()
	 */
	public File[] statisticFiles;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param helpCallback	Hilfe-Callback
	 * @param numberOfFiles	Anzahl an Eingabefelder die zum Auswählen von Statistikdateien angeboten werden sollen
	 */
	public CompareSelectDialog(Window owner, Runnable helpCallback, int numberOfFiles) {
		super(owner,Language.tr("Compare.Title"),false,helpCallback);

		statisticTextFields=new JTextField[numberOfFiles];
		statisticButton=new JButton[numberOfFiles];

		new FileDropper(this,new ButtonListener());

		createSimpleGUI(500,400,null,null);
		pack();
	}

	@Override
	protected final void createSimpleContent(JPanel content) {
		content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));

		JPanel p;

		if (statisticTextFields.length>2) {
			content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p.add(new JLabel(Language.tr("Compare.ErrorAtLeastTwoModels")));
		}

		for (int i=0;i<statisticTextFields.length;i++) {
			content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p.add(new JLabel(Language.tr("Compare.StatisticFile")+" "+(i+1)));
			content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p.add(statisticTextFields[i]=new JTextField(50));
			p.add(statisticButton[i]=new JButton(Language.tr("Compare.SelectStatisticFile"),Images.STATISTICS_LOAD.getIcon()));
			statisticButton[i].setToolTipText(Language.tr("Compare.SelectStatisticFile.Info"));
			statisticButton[i].addActionListener(new ButtonListener());
		}

		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		JPanel p2;
		p.add(p2=new JPanel());
		p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));
		p2.add(selectNebeneinander=new JRadioButton(Language.tr("Compare.Mode.Horizontal"),true));
		p2.add(selectUntereinander=new JRadioButton(Language.tr("Compare.Mode.Vertical")));

		ButtonGroup bg=new ButtonGroup();
		bg.add(selectNebeneinander);
		bg.add(selectUntereinander);
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#checkData()
	 */
	@Override
	protected boolean checkData() {
		List<File> files=new ArrayList<>();
		List<Integer> nr=new ArrayList<>();

		for (int i=0;i<statisticTextFields.length;i++) {
			String s=statisticTextFields[i].getText().trim();
			if (s.isEmpty()) continue;
			File file=new File(s);
			if (!file.exists()) {
				MsgBox.error(this,String.format(Language.tr("Compare.FileDoesNotExist.Title"),""+(i+1)),String.format(Language.tr("Compare.FileDoesNotExist.Info"),""+(i+1),file.toString()));
				return false;
			}
			for (int j=0;j<files.size();j++) if (file.equals(files.get(j))) {
				MsgBox.error(this,Language.tr("Compare.DoubleFile.Title"),String.format(Language.tr("Compare.DoubleFile.Info"),""+(i+1),""+(nr.get(j)+1),file.toString()));
				return false;
			}
			files.add(file);
			nr.add(i);
		}

		if (files.size()<2) {
			String s=(statisticTextFields.length==2)?Language.tr("Compare.TooFewModels.InfoTwo"):Language.tr("Compare.TooFewModels.InfoAtLeastTwo");
			MsgBox.error(this,Language.tr("Compare.TooFewModels.Title"),s);
			return false;
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#storeData()
	 */
	@Override
	protected void storeData() {
		List<File> files=new ArrayList<>();
		for (int i=0;i<statisticTextFields.length;i++) {
			String s=statisticTextFields[i].getText().trim();
			if (s.isEmpty()) continue;
			files.add(new File(s));
		}
		statisticFiles=files.toArray(new File[0]);
	}

	/**
	 * Ruft den Dialog zur Auswahl einer Statistikdatei auf
	 * @param initialFolder	Anfänglich zu selektierender Ordner
	 * @return	Dateiname oder <code>null</code>, wenn die Auswahl abgebrochen wurde
	 */
	private final File selectFile(File initialFolder) {
		File file=XMLTools.showLoadDialog(getParent(),Language.tr("Compare.LoadStatisticData"),initialFolder);
		if (file==null) return null;

		Statistics newData=new Statistics(null,null,0,0);
		String s=newData.loadFromFile(file); if (s!=null) {
			MsgBox.error(this,Language.tr("Compare.InvalidStatisticFile.Title"),String.format(Language.tr("Compare.InvalidStatisticFile.Info"),file.toString()));
			return null;
		}

		return file;
	}

	/**
	 * Sollen die Statistikdaten nebeneinander oder untereinander angezeigt werden
	 * @return	Statistikdaten nebeneinander (<code>true</code>) oder untereinander (<code>false</code>)
	 */
	public final boolean isNebeneinander() {
		return selectNebeneinander.isSelected();
	}

	/**
	 * Reagiert auf Auswahl-Schaltflächen-Klicks und auf Drag&amp;drop-Operationen
	 */
	private final class ButtonListener implements ActionListener {
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
			for (int i=0;i<statisticButton.length;i++) if (e.getSource()==statisticButton[i]) {
				File initialFile=null;
				for (int j=i;j>=0;j--) {
					String s=statisticTextFields[j].getText().trim();
					if (s!=null && !s.isEmpty()) {File f=new File(s); if (f.exists()) initialFile=f; break;}
				}
				File newFile=selectFile((initialFile==null)?null:initialFile.getParentFile());
				if (newFile!=null) statisticTextFields[i].setText(newFile.toString());
				return;
			}

			if (e.getSource() instanceof FileDropperData) {
				final FileDropperData data=(FileDropperData)e.getSource();
				if (dropFile(data.getFile())) data.dragDropConsumed();
				return;
			}
		}
	}

	/**
	 * Index der Eingabezeile in die die letzte Drag&amp;Drop-Operation erfolgte
	 * @see #dropFile(File)
	 */
	private int lastDrop=-1;

	/**
	 * Reagiert auf Drag&amp;Drop einer Datei auf den Dialog
	 * @param file	Datei, die übermittelt wirde
	 * @return	Gibt an, ob die Datei erfolgreich in die Liste der zu vergleichenden Statistikdateien aufgenommen werden konnte
	 */
	private final boolean dropFile(File file) {
		if (!file.exists()) return false;

		Statistics newData=new Statistics(null,null,0,0);
		String s=newData.loadFromFile(file); if (s!=null) {
			MsgBox.error(this,Language.tr("Compare.InvalidStatisticFile.Title"),String.format(Language.tr("Compare.InvalidStatisticFile.Info"),file.toString()));
			return false;
		}

		int nextFree=-1;
		for (int i=0;i<statisticTextFields.length;i++) if (statisticTextFields[i].getText().trim().isEmpty()) {nextFree=i; break;}
		if (nextFree==-1) {
			if (lastDrop==-1) nextFree=0; else {
				if (lastDrop==statisticTextFields.length-1) nextFree=0; else nextFree=lastDrop+1;
			}
		}
		lastDrop=nextFree;

		statisticTextFields[nextFree].setText(file.toString());
		return true;
	}
}
