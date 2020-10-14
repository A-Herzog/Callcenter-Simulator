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
package ui.optimizer;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import language.Language;
import simulator.Statistics;
import ui.editor.BaseEditDialog;

/**
 * Dialog zur Auswahl, von welchem Simulationslauf die Statistik-Ergebnisse angezeigt werden sollen
 * @author Alexander Herzog
 * @version 1.0
 */
public final class OptimizeSelectResult extends BaseEditDialog  {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -8093197712337120384L;

	private final OptimizeData results;
	private final Statistics[] resultsDirect;
	private JComboBox<String> comboBox;
	private JTextPane text;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param results	Statistikdaten die zur Auswahl angeboten werden sollen
	 * @param helpCallback	Hilfe-Callback
	 */
	public OptimizeSelectResult(final Window owner, final Statistics[] results, final Runnable helpCallback) {
		super(owner,Language.tr("Optimizer.ShowResultsOfASimulationRun"),false,helpCallback);
		this.results=null;
		resultsDirect=results;
		createSimpleGUI(600,600,null,null);
		if (comboBox.getItemCount()>0) comboBox.setSelectedIndex(0);
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param results	Optimierungsergebnisse aus denen die enthaltenen Statistikdaten die zur Auswahl angeboten werden sollen
	 * @param helpCallback	Hilfe-Callback
	 */
	public OptimizeSelectResult(final Window owner, final OptimizeData results, final Runnable helpCallback) {
		super(owner,Language.tr("Optimizer.ShowResultsOfASimulationRun"),false,helpCallback);
		this.results=results;
		resultsDirect=null;
		createSimpleGUI(600,600,null,null);
		if (comboBox.getItemCount()>0) comboBox.setSelectedIndex(comboBox.getItemCount()-1);
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		content.setLayout(new BorderLayout());

		final Vector<String> list=new Vector<String>();
		if (results==null) {
			for (int i=0;i<resultsDirect.length;i++) list.add(String.format(Language.tr("Optimizer.SimulatedDay"),i+1));
		} else {
			list.add(Language.tr("Optimizer.InitialModel"));
			if (results.runTime>0) {
				for (int i=1;i<results.data.size()-1;i++) list.add(Language.tr("Optimizer.SimulationRun")+" "+(i+1));
				if (results.data.size()>1) list.add(Language.tr("Optimizer.SimulationRun.Final"));
			} else {
				for (int i=1;i<results.data.size();i++) list.add(Language.tr("Optimizer.SimulationRun")+" "+(i+1));
			}
		}
		content.add(comboBox=new JComboBox<String>(list),BorderLayout.NORTH);
		comboBox.addActionListener(new ComboBoxListener());

		content.add(new JScrollPane(text=new JTextPane()),BorderLayout.CENTER);
		text.setEditable(false);
	}

	@Override
	protected boolean checkData() {return true;}

	@Override
	protected void storeData() {}

	/**
	 * Liefert die Nummer der ausgewählten Statistikdatei.
	 * @return	0-basierte Nummer der ausgewählten Statistikdatei
	 */
	public int getSelectedResult() {
		return comboBox.getSelectedIndex();
	}

	private final class ComboBoxListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Statistics statistics=(results==null)?(resultsDirect[comboBox.getSelectedIndex()]):(results.data.get(comboBox.getSelectedIndex()));
			if (statistics==null) text.setText(Language.tr("Optimizer.Error.MissingData")); else text.setText(statistics.editModel.description);
			text.setSelectionStart(0);
			text.setSelectionEnd(0);
		}
	}
}
