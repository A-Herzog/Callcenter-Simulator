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

import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import language.Language;
import mathtools.NumberTools;

/**
 * Zeigt ein Eingabe-Panel für die Eingabeparameter der Einfach-Simulation an
 * @author Alexander Herzog
 * @version 1.0
 * @see SimpleSimulation
 */
public class SimpleSimulationInputPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -394335481000773071L;

	/** Eingabefeld für lambda */
	private final JTextField editLambda;
	/** Auswahlfeld: Begrenzte Wartezeittoleranz? */
	private final JCheckBox checkboxWT;
	/** Eingabefeld für E[WT] */
	private final JTextField editEWT;
	/** Eingabefeld für Std[WT] */
	private final JTextField  editStdWT;
	/** Eingabefeld für die Wiederholwahrscheinlichkeit */
	private final JTextField editRetryProbability;
	/** Eingabefeld für den mittleren Wiederholabstand */
	private final JTextField editERetry;
	/** Eingabefeld für c */
	private final JTextField editC;
	/** Eingabefeld für E[S] */
	private final JTextField editES;
	/** Eingabefeld für Std[S] */
	private final JTextField editStdS;
	/** Auswahlfeld: Nachbearbeitungszeiten verwenden? */
	private final JCheckBox checkboxS2;
	/** Eingabefeld für E[Nachbearbeitungszeit] */
	private final JTextField editES2;
	/** Eingabefeld für Std[Nachbearbeitungszeit] */
	private final JTextField editStdS2;
	/** Eingabefeld für die Weiterleitungswahrscheinlichkeit */
	private final JTextField editContinueProbability;

	/** Runnable zur Aktualisierung der Ausgabe */
	private final Runnable updateSimulation;

	/**
	 * Konstruktor der Klasse <code>SimpleSimulationInputPanel</code>
	 * @param updateSimulation	Objekt vom Typ <code>Runnable</code> das aufgerufen wird, wenn sich die Eingabeparameter verändert haben
	 */
	public SimpleSimulationInputPanel(final Runnable updateSimulation) {
		super();
		this.updateSimulation=updateSimulation;

		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		addTopInfo(this,Language.tr("LoadCalculator.Section.Arrivals"));
		editLambda=addInputLine(this,Language.tr("LoadCalculator.ArrivalRate")+" ("+Language.tr("LoadCalculator.Units.ClientsPerMinute")+"):","<html><body>&lambda;=</body></html>",NumberTools.formatNumber(3.5));
		addTopInfo(this,Language.tr("LoadCalculator.Section.WaitingTimeTolerance"));
		checkboxWT=addCheckBox(this,Language.tr("LoadCalculator.LimitedWaitingTimeTolerance"),true);
		editEWT=addInputLine(this,Language.tr("LoadCalculator.AverageWaitingTimeTolerance")+" ("+Language.tr("LoadCalculator.Units.InMinutes")+"):","<html><body>E[WT]=1/&nu;=</body></html>",NumberTools.formatNumber(3));
		editStdWT=addInputLine(this,Language.tr("LoadCalculator.StandardDeviationWaitingTimeTolerance")+" ("+Language.tr("LoadCalculator.Units.InMinutes")+"):","<html><body>Std[WT]=</body></html>",NumberTools.formatNumber(3));
		addTopInfo(this,Language.tr("LoadCalculator.Section.Retry"));
		editRetryProbability=addInputLine(this,Language.tr("LoadCalculator.RetryProbability")+":","<html><body>P("+Language.tr("LoadCalculator.Retry")+")=</body></html>",NumberTools.formatPercent(0.75));
		editERetry=addInputLine(this,Language.tr("LoadCalculator.AverageRetryTime")+" ("+Language.tr("LoadCalculator.Units.InMinutes")+"):","<html><body>E["+Language.tr("LoadCalculator.RetryTime")+"]=</body></html>",NumberTools.formatNumber(15));
		addTopInfo(this,Language.tr("LoadCalculator.Section.Agents"));
		editC=addInputLine(this,Language.tr("LoadCalculator.Agents")+":","c=","13");
		addTopInfo(this,Language.tr("LoadCalculator.Section.ServiceProcess"));
		editES=addInputLine(this,Language.tr("LoadCalculator.AverageHoldingTime")+" ("+Language.tr("LoadCalculator.Units.InMinutes")+"):","<html><body>E[S]=1/&mu;=</body></html>",NumberTools.formatNumber(3));
		editStdS=addInputLine(this,Language.tr("LoadCalculator.StandardDeviationHoldingTime")+" ("+Language.tr("LoadCalculator.Units.InMinutes")+"):","<html><body>Std[S]=</body></html>",NumberTools.formatNumber(1));
		checkboxS2=addCheckBox(this,Language.tr("LoadCalculator.PostProcessingTime"),false);
		editES2=addInputLine(this,Language.tr("LoadCalculator.AveragePostProcessingTime")+" ("+Language.tr("LoadCalculator.Units.InMinutes")+"):","<html><body>E[S<sub>2</sub>]=1/&mu;=</body></html>",NumberTools.formatNumber(2));
		editStdS2=addInputLine(this,Language.tr("LoadCalculator.StandardDeviationPostProcessingTime")+" ("+Language.tr("LoadCalculator.Units.InMinutes")+"):","<html><body>Std[S<sub>2</sub>]=</body></html>",NumberTools.formatNumber(1));
		addTopInfo(this,Language.tr("LoadCalculator.Section.CallContinue"));
		editContinueProbability=addInputLine(this,Language.tr("LoadCalculator.ContinueProbability")+":","<html><body>P("+Language.tr("LoadCalculator.CallContinue")+")=</body></html>",NumberTools.formatPercent(0.2));
	}

	/**
	 * Fügt eine Überschriftszeile zu einem Panel hinzu.
	 * @param parent	Panel, in das die Überschriftszeile eingefügt werden soll
	 * @param text	Anzuzeigender Text
	 */
	private void addTopInfo(JPanel parent, String text) {
		parent.add(new JLabel("<html><body><b>"+text+"</b></body></html>"));
		parent.add(Box.createVerticalStrut(10));
	}

	/**
	 * Diese Funktion fügt ein Textfeld inkl. Beschreibung zu einem Panel hinzu.
	 * @param parent	Panel, in das das Eingabefeld eingefügt werden soll
	 * @param title	Optionaler Titel über dem Textfeld (kann <code>null</code> sein)
	 * @param label	Beschriftung für das Textfeld
	 * @param defaultValue	Anfänglicher Wert für das Textfeld
	 * @return	Referenz auf das neu erzeugte Textfeld
	 */
	private JTextField addInputLine(final JPanel parent, final String title, final String label, final String defaultValue) {
		JPanel panel;
		JTextField field;

		if (title!=null && !title.isEmpty()) parent.add(new JLabel("<html><body>"+title+"</body></html>"));
		parent.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(new JLabel(label));
		panel.add(field=new JTextField(defaultValue,7));
		panel.setAlignmentX(0);
		field.addKeyListener(new UpdateSimulation());

		return field;
	}

	/**
	 * Erzeugt eine Checkbox
	 * @param parent	Übergeordnetes Element in das die Checkbox eingefügt werden soll
	 * @param title	Beschriftung der Checkbox
	 * @param defaultValue	Initialer Wert der Checkbox
	 * @return	Neue Checkbox (bereits in das übergeordnete Element eingefügt)
	 */
	private JCheckBox addCheckBox(final JPanel parent, final String title, final boolean defaultValue) {
		JPanel panel;
		JCheckBox checkbox;

		parent.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(checkbox=new JCheckBox(title,defaultValue));
		panel.setAlignmentX(0);
		checkbox.addKeyListener(new UpdateSimulation());
		checkbox.addChangeListener(new UpdateSimulation());

		return checkbox;
	}

	/**
	 * Liefert die Eingabeparameter in Form eines <code>SimpleSimulationInput</code>-Objektes zurück
	 * @return	Eingabeparameter oder <code>null</code>, wenn die Nutzereingaben ungültig sind
	 */
	public SimpleSimulationInput getInputData() {
		double lambda;
		double EWT, StdWT;
		double retryProbability, ERetry;
		int c;
		double ES, StdS;
		double ES2, StdS2;
		double continueProbability;

		Double D;
		Long L;

		D=NumberTools.getNotNegativeDouble(editLambda,true); if (D==null) return null; lambda=D;
		if (checkboxWT.isSelected()) {
			D=NumberTools.getNotNegativeDouble(editEWT,true); if (D==null) return null; EWT=D;
			D=NumberTools.getNotNegativeDouble(editStdWT,true); if (D==null) return null; StdWT=D;
		} else {
			EWT=-1;
			StdWT=1;
		}
		D=NumberTools.getProbability(editRetryProbability,true); if (D==null) return null; retryProbability=D;
		D=NumberTools.getNotNegativeDouble(editERetry,true); if (D==null) return null; ERetry=D;
		L=NumberTools.getPositiveLong(editC,true); if (L==null) return null; c=(int)((long)L);
		D=NumberTools.getNotNegativeDouble(editES,true); if (D==null) return null; ES=D;
		D=NumberTools.getNotNegativeDouble(editStdS,true); if (D==null) return null; StdS=D;
		if (checkboxS2.isSelected()) {
			D=NumberTools.getNotNegativeDouble(editES2,true); if (D==null) return null; ES2=D;
			D=NumberTools.getNotNegativeDouble(editStdS2,true); if (D==null) return null; StdS2=D;
		} else {
			ES2=-1;
			StdS2=1;
		}
		D=NumberTools.getProbability(editContinueProbability,true); if (D==null) return null; continueProbability=D;

		return new SimpleSimulationInput(lambda,EWT,StdWT,retryProbability,ERetry,c,ES,StdS,ES2,StdS2,continueProbability);
	}

	/**
	 * Reagiert auf Änderungen in der GUI und aktualisiert die Ausgabe.
	 */
	private class UpdateSimulation implements ChangeListener, KeyListener {
		@Override public void stateChanged(ChangeEvent e) {updateSimulation.run();}
		@Override public void keyTyped(KeyEvent e) {updateSimulation.run();}
		@Override public void keyPressed(KeyEvent e) {updateSimulation.run();}
		@Override public void keyReleased(KeyEvent e) {updateSimulation.run();}
	}
}
