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
package ui.specialpanels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.NumberTools;
import simulator.CallcenterSimulatorInterface;
import systemtools.MsgBox;
import tools.TrayNotify;
import ui.images.Images;

/**
 * Die Klasse stellt ein <code>JPanel</code> dar, welches während der Simulation angezeigt werden kann
 * und Statusinformationen zum Simulationslauf ausgibt.
 * @author Alexander Herzog
 * @version 1.0
 */
public class CallcenterRunPanel extends JCloseablePanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 176710490651488333L;

	private final long startTime;
	private int lastGesamt;

	private boolean runComplete=false;
	private boolean abortRun=false;
	private JLabel info, info2;
	private JLabel statusbar;
	private JProgressBar progress;
	private JButton cancel;
	private final CallcenterSimulatorInterface simulator;
	private final Timer timer;
	private int countTimerIntervals;
	private final Runnable doneNotify;

	/**
	 * Handelt es sich um eine Simulation zum Vergleich von Modellen?<br>
	 * 1: Erstes von zwei Modellen<br>
	 * 2: Zweites von zwei Modellen<br>
	 * -1: Normale Simulation
	 */
	public final int compareModelsMode;

	/**
	 * Konstruktor der Klasse
	 * @param simulator	Simulator dessen Fortschritt in diesem Panel dargestellt werden soll
	 * @param doneNotify	Callback wird aufgerufen, wenn das Batch-Panel geschlossen werden soll
	 * @param autoSaveResults	Ergebnisse automatisch speichern
	 * @param compareModelsMode	Simulationsmodus (siehe {@link #compareModelsMode})
	 */
	public CallcenterRunPanel(CallcenterSimulatorInterface simulator, Runnable doneNotify, boolean autoSaveResults, int compareModelsMode) {
		super(new BorderLayout());

		this.compareModelsMode=compareModelsMode;

		this.simulator=simulator;
		this.doneNotify=doneNotify;

		JPanel mainarea, p1a, p1b, p2, p3=null;

		add(statusbar=new JLabel(),BorderLayout.SOUTH);
		statusbar.setPreferredSize(new Dimension(100,20));
		statusbar.setBorder(BorderFactory.createEtchedBorder());
		statusbar.setText(Language.tr("Simulation.Started"));

		add(mainarea=new JPanel(),BorderLayout.CENTER);
		mainarea.setLayout(new BoxLayout(mainarea,BoxLayout.Y_AXIS));
		mainarea.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		mainarea.add(Box.createVerticalGlue());
		mainarea.add(p1a=new JPanel()); p1a.setLayout(new BoxLayout(p1a,BoxLayout.X_AXIS));
		mainarea.add(p1b=new JPanel()); p1b.setLayout(new BoxLayout(p1b,BoxLayout.X_AXIS));
		if (autoSaveResults) {
			mainarea.add(Box.createVerticalStrut(10));
			mainarea.add(p3=new JPanel()); p3.setLayout(new BoxLayout(p3,BoxLayout.X_AXIS));
		}
		mainarea.add(Box.createVerticalStrut(10));
		mainarea.add(progress=new JProgressBar(0,(int)simulator.getSimDaysCount()));
		mainarea.add(Box.createVerticalStrut(10));
		mainarea.add(p2=new JPanel()); p2.setLayout(new BoxLayout(p2,BoxLayout.X_AXIS));
		mainarea.add(Box.createVerticalGlue());
		mainarea.add(Box.createVerticalGlue());

		progress.setStringPainted(true);

		p1a.add(Box.createHorizontalGlue());
		p1a.add(info=new JLabel(Language.tr("Simulation.WasStarted")));
		p1a.add(Box.createHorizontalGlue());

		p1b.add(Box.createHorizontalGlue());
		p1b.add(info2=new JLabel(""));
		p1b.add(Box.createHorizontalGlue());

		p2.add(Box.createHorizontalGlue());
		p2.add(cancel=new JButton(Language.tr("Simulation.CancelButton")));
		p2.add(Box.createHorizontalGlue());
		if (autoSaveResults && p3!=null) p3.add(new JLabel(Language.tr("Simulation.AutoSaveIsActive")));
		cancel.addActionListener(e->abortSimulation());
		cancel.setIcon(Images.GENERAL_CANCEL.getIcon());

		startTime=System.currentTimeMillis();
		simulator.start(false);

		countTimerIntervals=0;

		timer=new Timer("SimProgressBar",false);
		timer.schedule(new UpdateInfoTask(),50,50);

		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"),"ESCAPE");
		getActionMap().put("ESCAPE",new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = 190237083100271239L;
			@Override public void actionPerformed(ActionEvent e) {abortSimulation();}
		});
	}

	private void abortSimulation() {
		cancel.setEnabled(false);
		abortRun=true;
	}

	private class UpdateInfoTask extends TimerTask {
		@Override
		public void run() {
			if (abortRun) {
				timer.cancel();
				simulator.cancel();
				simulator.finalizeRun();
				System.gc();
				SwingUtilities.invokeLater(doneNotify);
				return;
			}

			lastGesamt=Integer.MAX_VALUE;
			if  (simulator.isRunning()) {
				countTimerIntervals++;
				final long day=simulator.getSimDayCount();
				final long days=simulator.getSimDaysCount();
				if (countTimerIntervals%10==0) {
					long time=System.currentTimeMillis();
					if (time-startTime>3000) {
						double gesamt=(time-startTime)/(((double)day)/days);
						gesamt-=(time-startTime);
						if (gesamt/1000<lastGesamt) lastGesamt=(int) Math.round(gesamt/1000);
						info.setText(Language.tr("Simulation.Running"));
						info2.setText(String.format(Language.tr("Simulation.RunInfo.Window"),NumberTools.formatLong((time-startTime)/1000),NumberTools.formatLong(lastGesamt)));
					}

					statusbar.setText(String.format(Language.tr("Simulation.RunInfo.Status"),NumberTools.formatLong(day),NumberTools.formatLong(days),NumberTools.formatLong(simulator.getEventCount()/1000000),NumberTools.formatLong(simulator.getEventsPerSecond()/1000)));
					progress.setValue((int)day);
				}
			} else {
				timer.cancel();
				String errorMessage=simulator.finalizeRun();
				/* Vom Server gesandte Meldungen ausgeben */
				if (errorMessage!=null) MsgBox.error(CallcenterRunPanel.this,Language.tr("Simulation.ErrorTitle"),errorMessage);
				runComplete=(errorMessage==null);
				System.gc();
				if (runComplete) new TrayNotify(CallcenterRunPanel.this,Language.tr("Simulation.TrayNotify.Title"),Language.tr("Simulation.TrayNotify.Info"));

				SwingUtilities.invokeLater(doneNotify);
			}
		}
	}

	/**
	 * Gibt an, ob der Simulationslauf erfolgreich beendet wurde.
	 * @return Gibt <code>true</code> zurück, wenn die Simulation erfolgreich beendet wurde.
	 */
	public boolean getRunComplete() {
		return runComplete;
	}

	/**
	 * Ermöglicht den Zugriff auf den laufenden Simulator.
	 * @return	Simulator
	 */
	public CallcenterSimulatorInterface getSimulator() {
		return simulator;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.specialpanels.JCloseablePanel#requestClose()
	 */
	@Override
	public void requestClose() {
		cancel.setEnabled(false); abortRun=true;
	}
}
