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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.LogNormalDistributionImpl;
import mathtools.distribution.tools.DistributionTools;
import net.calc.StartAnySimulator;
import simulator.CallcenterSimulatorInterface;
import simulator.Statistics;
import systemtools.MsgBox;
import tools.TrayNotify;
import ui.HelpLink;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelCaller;
import xml.XMLTools;

/**
 * Über die in diesem Panel angebotenen Funktionen kann
 * einer Kalibrierung der prinzipbedingt nicht messbaren
 * Wartezeittoleranzen der Kunden erfolgen.
 * @author Alexander Herzog
 */
public class CalibratePanel extends JWorkPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4042434827597615142L;

	/** Gesamter Darstellungsbereich */
	private final JPanel scrollArea;
	/** Scroll-Bereich in {@link #scrollArea} für Eingaben */
	private final JScrollPane scroll1;
	/** Scroll-Bereich in {@link #scrollArea} für Ausgaben */
	private final JScrollPane scroll2;
	/** Eingabefelder für die Ziel-Erreichbarkeiten pro Kundentyp */
	private final JTextField[] successInput;
	/** Eingabefeld "Variationskoeffizient der Wartezeittoleranzen" */
	private final JTextField waitingTimeToleranceCVInput;
	/** Eingabefeld "Maximale Zielabweichung" */
	private final JTextField successTargetWindowSizeInput;
	/** Ausgabefeld für Status-Informationen */
	private final JTextArea statusField;
	/** Text in der Statusleiste */
	private final JLabel statusLabel;
	/** Fortschrittsanzeige in der Statusleiste */
	private final JProgressBar statusProgress;

	/** Für die Kalibrierung zu verwendendes Callcenter-Modell */
	private final CallcenterModel model;
	/** Von {@link #model} abgeleitetes, verändertes Callcenter-Modell für die Simulation */
	private CallcenterModel editModel;
	/** Timer um regelmäßig den Fortschritt der Simulation in der GUI anzeigen zu können */
	private Timer timer;
	/** Startzeitpunkt der ersten Simulation */
	private long startTime;
	/** Zählt die Anzahl an Simulationen (gezählt wird beim Start) */
	private int simCount;
	/** Zählt die Aufrufe von {@link SimTimerTask} */
	private int count;
	/** Wurden alle Simulationen abgeschlossen? */
	private boolean allDone;
	/** Aktuelle Wartezeittoleranzen */
	private double[] waitingTimeTolerance;
	/** Aktuelle Änderungen der Wartezeittoleranzen */
	private double[] waitingTimeToleranceStepSize;
	/** Änderungen im letzten Schritt */
	private List<Double> lastChanges;
	/** Erreichbarkeit pro Kundentyp im letzten Schritt */
	private double[] successTarget;
	/** Variationskoeffizient der Wartezeittoleranzen */
	private double waitingTimeToleranceCV;
	/** Maximale Zielabweichung */
	private double successTargetWindowSize;

	/** Simulator, der die konkreten Simulationen ausführt */
	private CallcenterSimulatorInterface simulator;

	/**
	 * Konstruktor der Klasse
	 * @param model	Für die Kalibrierung zu verwendendes Callcenter-Modell
	 * @param doneNotify	Callback wird aufgerufen, wenn das Panel geschlossen werden soll
	 * @param helpLink	Help-Link
	 */
	public CalibratePanel(final CallcenterModel model, final Runnable doneNotify, final HelpLink helpLink) {
		super(doneNotify,helpLink.pageCalibrate);
		this.model=model;

		JPanel main,p,p2,p3,p3a;

		/* Main area */
		add(main=new JPanel(new BorderLayout()),BorderLayout.CENTER);

		main.add(scrollArea=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		scrollArea.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		/* Eingabefelder */
		p2=new JPanel(new BorderLayout());
		p2.add(p3=new JPanel(),BorderLayout.NORTH);
		p3.setLayout(new BoxLayout(p3,BoxLayout.PAGE_AXIS));

		p3.add(p3a=new JPanel(new GridLayout(model.caller.size()+4,2)));
		p2.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		p3a.add(new JLabel("<html><b>"+Language.tr("Calibrate.ClientType")+"</b></html>"));
		p3a.add(new JLabel("<html><b>"+Language.tr("Calibrate.TargetAccessibility")+"</b></html>"));
		successInput=new JTextField[model.caller.size()];
		for (int i=0;i<model.caller.size();i++) {
			p3a.add(new JLabel(model.caller.get(i).name));
			p3a.add(successInput[i]=new JTextField("80%"));
		}
		p3a.add(new JLabel("<html><b>"+Language.tr("Calibrate.AdditionalSettings")+"</b></html>"));
		p3a.add(new JLabel(""));
		p3a.add(new JLabel(Language.tr("Calibrate.WaitingTimeToleranceCV")));
		p3a.add(waitingTimeToleranceCVInput=new JTextField(NumberTools.formatNumber(1.5d)));
		p3a.add(new JLabel(Language.tr("Calibrate.MaximumTargetError")));
		p3a.add(successTargetWindowSizeInput=new JTextField("1%"));
		p3.add(Box.createVerticalStrut(20));
		p3.add(p3a=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p3a.add(new JLabel("<html>"+Language.tr("Calibrate.TargetAccessibility.Info")+"</html>"));

		scrollArea.add(scroll1=new JScrollPane(p2));

		/* Statusfeld */
		scroll2=new JScrollPane(statusField=new JTextArea(10,80));
		statusField.setEditable(false);

		main.add(p=new JPanel(),BorderLayout.SOUTH);
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(statusLabel=new JLabel(""));

		p.add(p2=new JPanel(new BorderLayout()));
		p2.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		p2.add(statusProgress=new JProgressBar(),BorderLayout.CENTER);
		statusProgress.setStringPainted(true);

		/* Bottom line */
		addFooter(Language.tr("Calibrate.Simulation.Start"),Images.SIMULATION_CALIBRATE.getIcon(),Language.tr("Calibrate.Simulation.Abort"));
	}

	/**
	 * Gibt eine Zeile in dem Status-Ausgaben-Bereich aus.
	 * @param line	Auszugebende Informationszeile
	 */
	private void addStatusLine(final String line) {
		if (statusField.getText().isEmpty()) statusField.setText(line); else statusField.setText(statusField.getText()+"\n"+line);
		statusField.setCaretPosition(statusField.getText().length());
	}

	/**
	 * Startet die nächste Simulation
	 * @return	Liefert <code>true</code>, wenn eine weitere Simulation gestartet werden konnte, und <code>false</code>, wenn die Simulation aller Modelle abgeschlossen ist
	 */
	private boolean initNextSimulation() {
		simulator=null;

		if (allDone) return false;

		simCount++;
		for (int i=0;i<waitingTimeTolerance.length;i++) editModel.caller.get(i).waitingTimeDist=new LogNormalDistributionImpl(waitingTimeTolerance[i],waitingTimeTolerance[i]*waitingTimeToleranceCV);

		StartAnySimulator startAnySimulator=new StartAnySimulator(editModel);
		String s=startAnySimulator.check(); if (s!=null) {
			addStatusLine(Language.tr("Dialog.Title.Error").toUpperCase()+":\n  "+Language.tr("Calibrate.ModelPreparationError")+":\n  "+s);
			return false;
		}
		simulator=startAnySimulator.run();
		simulator.start(false);
		statusProgress.setMaximum((int)simulator.getSimDaysCount());

		return simulator!=null;
	}

	/**
	 * Schließt die aktuelle Simulation ab.
	 */
	private void doneSimulation() {
		String errorMessage=simulator.finalizeRun();
		/* Vom Server gesandte Meldungen ausgeben */
		if (errorMessage!=null) addStatusLine(Language.tr("Dialog.Title.Error").toUpperCase()+":\n  "+errorMessage);

		Statistics statistics=simulator.collectStatistic();
		double[] results=new double[successTarget.length];
		for (int i=0;i<results.length;i++) results[i]=(double)statistics.kundenProTyp[i].anrufeErfolg/Math.max(1,statistics.kundenProTyp[i].anrufe);

		StringBuilder sb;

		sb=new StringBuilder();
		sb.append(String.format(Language.tr("Calibrate.AverageWaitingTimeToleranceInSimulation"),""+simCount)+": ");
		for (int i=0;i<results.length;i++) {if (i>0) sb.append("  "); sb.append(NumberTools.formatNumber(waitingTimeTolerance[i]));}
		addStatusLine(sb.toString());

		sb=new StringBuilder();
		sb.append(String.format(Language.tr("Calibrate.AccessibilityInSimulation"),""+simCount)+": ");
		for (int i=0;i<results.length;i++) {if (i>0) sb.append("  "); sb.append(NumberTools.formatPercent(results[i]));}
		addStatusLine(sb.toString());

		boolean ok=true;
		for (int i=0;i<results.length;i++) if (Math.abs(results[i]-successTarget[i])>successTargetWindowSize) {ok=false; break;}
		if (ok) {allDone=true; return;}

		double[] realChange=new double[results.length];
		double realChangeSum=0;
		int notReachable=0;
		for (int i=0;i<results.length;i++) {
			realChange[i]=0;
			if (Math.abs(results[i]-successTarget[i])<=successTargetWindowSize) continue;

			boolean upNext=(results[i]-successTarget[i]<0);
			boolean upLast=(waitingTimeToleranceStepSize[i]>0);

			if (waitingTimeToleranceStepSize[i]==0) {
				/* Erster Lauf */
				waitingTimeToleranceStepSize[i]=(upNext?1:-1)*waitingTimeTolerance[i]*0.1;
			} else {
				/* Weitere Läufe */
				if (upNext!=upLast) waitingTimeToleranceStepSize[i]=-waitingTimeToleranceStepSize[i]*0.2;
			}

			double d=waitingTimeTolerance[i]+waitingTimeToleranceStepSize[i];
			if (waitingTimeTolerance[i]==0 && d<0) notReachable++;
			waitingTimeTolerance[i]=Math.max(0,d);
			realChange[i]=waitingTimeToleranceStepSize[i];
			realChangeSum+=Math.abs(waitingTimeToleranceStepSize[i]);
		}
		if (notReachable==results.length) {
			addStatusLine(Language.tr("Calibrate.TargetNotReachable1"));
			addStatusLine(Language.tr("Calibrate.TargetNotReachable2"));
			allDone=true;
			return;
		}
		if (lastChanges.size()>20) {
			double d=0; for (int i=lastChanges.size()-10;i<lastChanges.size();i++) d=Math.max(d,lastChanges.get(i));
			if (realChangeSum>d*0.9999) {
				addStatusLine(Language.tr("Calibrate.NoChangeAnymore1"));
				addStatusLine(Language.tr("Calibrate.NoChangeAnymore2"));
				allDone=true;
				return;
			}
		}
		lastChanges.add(realChangeSum);

		sb=new StringBuilder();
		sb.append(Language.tr("Calibrate.ChangeOfAverageWaitingTimeTolerance")+":");
		for (int i=0;i<results.length;i++) {sb.append("  "); if (realChange[i]>=0) sb.append("+"); sb.append(NumberTools.formatNumber(realChange[i]));}
		addStatusLine(sb.toString());
	}

	/**
	 * Wird nach dem Abschluss aller Simulationen
	 * (erfolgreich oder durch Abbruch) aufgerufen.
	 */
	private void everythingDone() {
		if (cancelWork) {
			addStatusLine(String.format(Language.tr("Calibrate.Simulation.Aborted"),""+simCount));
		} else {
			addStatusLine(String.format((simCount==1)?Language.tr("Calibrate.Simulation.FinishedSingle"):Language.tr("Calibrate.Simulation.FinishedMultiple"),""+simCount,NumberTools.formatLong((System.currentTimeMillis()-startTime)/1000)));
			new TrayNotify(this,Language.tr("Calibrate.Simulation.FinishedNofity.Title"),Language.tr("Calibrate.Simulation.FinishedNofity.Info"));

			File file;
			while (true) {
				file=XMLTools.showSaveDialog(getParent(),Language.tr("Calibrate.SaveModel"));
				if (file==null) break;
				if (file.exists()) {
					if (!MsgBox.confirmOverwrite(this,file)) continue;
				}
				if (editModel.saveToFile(file)) break;
				MsgBox.error(this,Language.tr("Calibrate.SaveModel.ErrorTitle"),String.format(Language.tr("Calibrate.SaveModel.ErrorInfo"),file.toString()));
			}
		}

		System.gc();

		/* GUI umschalten */
		setWorkMode(false);
		scrollArea.remove(scroll2);
		scrollArea.add(scroll1);
		scrollArea.repaint();
		statusLabel.setText("");
		statusProgress.setValue(0);
	}

	/**
	 * Prüft in Regelmäßigen Abständen, ob die
	 * laufende Simulation abgeschlossen wurde
	 * und die nächste Simulation gestartet
	 * werden kann.
	 * @see CalibratePanel#initNextSimulation()
	 * @see CalibratePanel#doneSimulation()
	 * @see CalibratePanel#everythingDone()
	 */
	private class SimTimerTask extends TimerTask {
		/**
		 * Konstruktor der Klasse
		 */
		public SimTimerTask() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void run() {
			if (cancelWork) {timer.cancel(); simulator.cancel(); everythingDone(); return;}

			if (simulator.isRunning()) {
				count++; if (count%8==0) statusProgress.setValue((int)simulator.getSimDayCount());
				return;
			}

			doneSimulation();
			if (cancelWork) return;
			if (!initNextSimulation()) {timer.cancel(); everythingDone(); return;}
		}
	}

	/**
	 * Initialisiert das Modell für die Kalibrierung.
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 * @see CalibratePanel#model
	 * @see CalibratePanel#editModel
	 */
	private String initModel() {
		/* Initialdaten aus Modell auslesen */
		editModel=model.clone();
		if (editModel.caller.size()==0) return Language.tr("Calibrate.ErrorNoClientTypes");
		waitingTimeTolerance=new double[editModel.caller.size()];
		waitingTimeToleranceStepSize=new double[editModel.caller.size()];
		for (int i=0;i<waitingTimeTolerance.length;i++) {
			if (editModel.caller.get(i).waitingTimeMode!=CallcenterModelCaller.WAITING_TIME_MODE_SHORT) return Language.tr("Calibrate.ErrorWaitingTimeToleranceEstimationActive");
			waitingTimeTolerance[i]=DistributionTools.getMean(editModel.caller.get(i).waitingTimeDist);
			waitingTimeToleranceStepSize[i]=0;
		}

		/* GUI Einstellungen für Zielwerte auslesen */
		successTarget=new double[editModel.caller.size()];
		for (int i=0;i<successTarget.length;i++) {
			Double D=NumberTools.getExtProbability(successInput[i],true);
			if (D==null) return String.format(Language.tr("Calibrate.ErrorInvalidTargetAccessibility"),editModel.caller.get(i).name);
			successTarget[i]=D;
		}

		Double D;
		D=NumberTools.getExtProbability(waitingTimeToleranceCVInput,true);
		if (D==null) return Language.tr("Calibrate.ErrorInvalidWaitingTimeToleranceCV");
		waitingTimeToleranceCV=D;
		D=NumberTools.getExtProbability(successTargetWindowSizeInput,true);
		if (D==null) return Language.tr("Calibrate.ErrorInvalidMaximumAccessibilityError");
		successTargetWindowSize=D;

		return null;
	}

	@Override
	protected final void run() {
		cancelWork=false;
		allDone=false;
		lastChanges=new ArrayList<>();

		String s=initModel();
		if (s!=null) {MsgBox.error(this,Language.tr("Calibrate.ErrorTitle"),s); return;}

		/* GUI umschalten */
		setWorkMode(true);
		statusField.setText("");
		scrollArea.remove(scroll1);
		scrollArea.add(scroll2);
		scrollArea.repaint();

		/* Start der Simulation */
		startTime=System.currentTimeMillis();
		simCount=0;
		if (!initNextSimulation()) {
			everythingDone();
		} else {
			timer=new Timer();
			timer.schedule(new SimTimerTask(),50,50);
		}
	}
}
