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
package ui.simplesimulation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.io.Serializable;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import language.Language;
import ui.HelpLink;
import ui.editor.BaseEditDialog;

/**
 * Zeigt einen Dialog an, in dem über die Simulation eines Einfach-Modells direkt die Auswirkungen von
 * Änderungen von Parametern untersucht werden können.
 * @author Alexander Herzog
 * @version 1.0
 */
public class SimpleSimulationDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 3257255566302733981L;

	/** Eingabebereich */
	private SimpleSimulationInputPanel inputPanel;
	/** Ausgabebereich */
	private SimpleSimulationResultsPanel resultsPanel;
	/** Letzte Eingaben */
	private SimpleSimulationInput lastInput=null;

	/**
	 * Konstruktor der Klasse <code>SimpleSimulationDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param helpLink Verknüpfung mit der Online-Hilfe
	 */
	public SimpleSimulationDialog(Window owner, HelpLink helpLink) {
		super(owner,Language.tr("SimpleSimulation.Title"),null,false,helpLink.dialogSimpleSimulation);
		showCloseButton=true;
		createSimpleGUI(1000,800,null,null);
		new UpdateSimulation().run();
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		content.setLayout(new BorderLayout());

		final JSplitPane split=new JSplitPane();
		content.add(split,BorderLayout.CENTER);

		split.setLeftComponent(inputPanel=new SimpleSimulationInputPanel(new UpdateSimulation()));
		split.setRightComponent(resultsPanel=new SimpleSimulationResultsPanel());

		inputPanel.setMinimumSize(new Dimension(300,100));
		resultsPanel.setMinimumSize(new Dimension(300,100));

		SwingUtilities.invokeLater(()->split.setDividerLocation(0.35));
	}

	/**
	 * Aktualisiert die Ausgaben
	 */
	private class UpdateSimulation implements Runnable {
		/**
		 * Konstruktor der Klasse
		 */
		public UpdateSimulation() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void run() {
			SimpleSimulationInput input=inputPanel.getInputData();
			if (lastInput!=null) {
				if (input!=null && input.equals(lastInput)) return;
			}
			lastInput=input;
			final SimpleSimulation simulator=new SimpleSimulation(inputPanel.getInputData());
			simulator.start(()->resultsPanel.setResults(simulator));
		}
	}
}