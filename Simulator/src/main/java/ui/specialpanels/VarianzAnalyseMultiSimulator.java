
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
 */package ui.specialpanels;

 import javax.swing.JComponent;

import org.apache.commons.math3.distribution.NormalDistribution;

import language.Language;
import mathtools.NumberTools;
import net.calc.StartAnySimulator;
import simulator.CallcenterSimulatorInterface;
 import simulator.Statistics;
 import systemtools.MsgBox;
import ui.model.CallcenterModel;

 /**
  * Diese Klasse führt die eigentliche Varianzanalyse
  * eines Callcenter-Modells durch.
  * @author Alexander Herzog
  * @see VarianzAnalysePanel
  */
 class VarianzAnalyseMultiSimulator {
	 /** Beliebige Komponente zur optischen Ausrichtung von Meldungsfenster */
	 private final JComponent parent;
	 /** Callcenter-Modell welches die Basis der Varianzanalyse darstellt */
	 private final CallcenterModel model;
	 private final int simCountNumber;
	 private int simCount;
	 private final double[] waitingTimeSum;
	 private final double[] waitingTimeSum2;

	 private CallcenterSimulatorInterface simulator;

	 /**
	  * Konstruktor der Klasse
	  * @param parent	Beliebige Komponente zur optischen Ausrichtung von Meldungsfenster
	  * @param model	Callcenter-Modell welches die Basis der Varianzanalyse darstellt
	  * @param simDays	Anzahl an zu simulierenden Tagen (überschreibt den Wert aus dem Modell)
	  * @param simCountNumber	Anzahl an Wiederholungen des gesamten Modells
	  */
	 public VarianzAnalyseMultiSimulator(final JComponent parent, final CallcenterModel model, final int simDays, final int simCountNumber) {
		 this.parent=parent;
		 this.model=model.clone();
		 this.model.days=simDays;
		 this.simCountNumber=simCountNumber;
		 simCount=0;
		 waitingTimeSum=new double[model.caller.size()+1];
		 waitingTimeSum2=new double[model.caller.size()+1];
		 for (int i=0;i<model.caller.size()+1;i++) {waitingTimeSum[i]=0; waitingTimeSum2[i]=0;}
	 }

	 /**
	  * Liefer den Maximalwert für die Fortschrittsanzeige.
	  * @return	Maximalwert für die Fortschrittsanzeige
	  * @see #getProgress()
	  */
	 public int getProgressMax() {
		 return model.days*simCountNumber;
	 }

	 /**
	  * Liefert den aktuellen Fortschrittswert.
	  * @return	Aktueller Fortschrittswert
	  * @see #getProgressMax()
	  */
	 public long getProgress() {
		 if (simulator==null) return 0;
		 return model.days*(simCount-1)+simulator.getSimDayCount();
	 }

	 /**
	  * Liefert die Nummer der aktuellen Wiederholung der Simulation.
	  * @return	Nummer der aktuellen Wiederholung der Simulation (vor dem Start: 0, während und nach der ersten Simulation: 1, usw.)
	  */
	 public int getCurrentSimNumber() {
		 return simCount;
	 }

	 /**
	  * Gibt an, ob die Simulation läuft.
	  * @return	Liefert <code>true</code>, wenn die Simulation läuft.
	  */
	 public boolean isRunning() {
		 return simulator!=null && simulator.isRunning();
	 }

	 /**
	  * Bricht die Simulation ab.
	  */
	 public void cancel() {
		 if (simulator!=null) simulator.cancel();
	 }

	 /**
	  * Startet die nächste Simulation im Rahmen der Varianzanalyse
	  * @return	Liefert <code>true</code>, wenn die nächste Simulation gestartet wurde und <code>false</code>, wenn die Varianzanalyse vollständig und abgeschlossen ist.
	  */
	 public boolean initNextSimulation() {
		 simulator=null;

		 if (simCount==simCountNumber) return false;
		 simCount++;

		 /* Simulation starten */
		 StartAnySimulator startAnySimulator=new StartAnySimulator(model);
		 String s=startAnySimulator.check(); if (s!=null) {MsgBox.error(parent,Language.tr("VarianceAnalysis.CouldNotStartSimulation.Title"),s); return false;}
		 simulator=startAnySimulator.run();
		 simulator.start(false);

		 return true;
	 }

	 /**
	  * Finalisiert eine abgeschlossene Simulation
	  * @param detailTable	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird an diese Tabelle eine Zeile angehängt.
	  * @return	Liefert im Erfolgsfall <code>true</code>
	  */
	 public boolean doneSingleSimulation(final VarianzAnalyseExportableTable detailTable) {
		 String errorMessage=simulator.finalizeRun();
		 /* Vom Server gesandte Meldungen ausgeben */
		 if (errorMessage!=null) {
			 MsgBox.error(parent,Language.tr("Simulation.ErrorTitle"),errorMessage);
			 return false;
		 }

		 Statistics statistics=simulator.collectStatistic();
		 simulator=null;

		 if (statistics==null) {MsgBox.error(parent,Language.tr("VarianceAnalysis.Canceled.Title"),Language.tr("VarianceAnalysis.Canceled.Info")); return false;}

		 double d;
		 String[] line=new String[statistics.kundenProTyp.length+2];
		 line[0]=String.format(Language.tr("VarianceAnalysis.AverageWaitingTime.Run"),""+simCount);
		 for (int i=0;i<statistics.kundenProTyp.length;i++) {
			 if (statistics.kundenProTyp[i].anrufe==0) d=0; else d=(double)statistics.kundenProTyp[i].anrufeWartezeitSum/statistics.kundenProTyp[i].anrufeErfolg;
			 waitingTimeSum[i]+=d;
			 waitingTimeSum2[i]+=(d*d);
			 line[i+1]=NumberTools.formatNumber(d,1);
		 }
		 if (statistics.kundenGlobal.anrufe==0) d=0; else d=(double)statistics.kundenGlobal.anrufeWartezeitSum/statistics.kundenGlobal.anrufeErfolg;
		 waitingTimeSum[waitingTimeSum.length-1]+=d;
		 waitingTimeSum2[waitingTimeSum.length-1]+=(d*d);
		 line[line.length-1]=NumberTools.formatNumber(d,1);
		 if (detailTable!=null) detailTable.addLine(line);

		 return true;
	 }

	 private double calcStd(double x2, double x, double n) {
		 if (n>0) return Math.sqrt((x2)/n-x*x/n/n); else return 0;
	 }

	 private double[] calcConfidence(double x2, double x, double n, double p) {
		 double[] interval=new double[2];

		 double mean=x/n;
		 double sd=calcStd(x2,x,n);

		 /* x +- z(1-alpha/2)*sd/sqrt(n) */
		 NormalDistribution dist=new NormalDistribution(0,1);
		 double half;
		 half=dist.inverseCumulativeProbability(1-(1-p)/2)*sd/Math.sqrt(n);

		 interval[0]=mean-half;
		 interval[1]=mean+half;

		 return interval;
	 }

	 /**
	  * Beendet eine Mehrfachsimulation.
	  * @param detailTable	Wird hier ein Wert ungleich <code>null</code> übergeben, so werden in der Tabelle Abschluss-Informationen zu den Simulationen ausgegeben.
	  * @param globalTable	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird in der Tabelle pro Simulationslauf eine Zeile mit Übersichtsdaten ausgegeben.
	  */
	 public void doneAll(final VarianzAnalyseExportableTable detailTable, final VarianzAnalyseExportableTable globalTable) {
		 String[] line=new String[waitingTimeSum.length+1];

		 if (detailTable!=null) {
			 line[0]=Language.tr("VarianceAnalysis.AverageWaitingTime.All");
			 for (int i=0;i<waitingTimeSum.length;i++) line[i+1]=NumberTools.formatNumber(waitingTimeSum[i]/simCountNumber,1);
			 detailTable.addLine(line);
			 line[0]=Language.tr("Distribution.StdDevs");
			 for (int i=0;i<waitingTimeSum.length;i++) line[i+1]=NumberTools.formatNumber(calcStd(waitingTimeSum2[i],waitingTimeSum[i],simCountNumber),3);
			 detailTable.addLine(line);

			 line[0]=Language.tr("Distribution.CV");
			 for (int i=0;i<waitingTimeSum.length;i++) {
				 double mean=waitingTimeSum[i]/simCountNumber;
				 double std=calcStd(waitingTimeSum2[i],waitingTimeSum[i],simCountNumber);
				 double cv=(mean>0)?(std/mean):0;
				 line[i+1]=NumberTools.formatNumber(cv*100,3)+"%";
			 }
			 detailTable.addLine(line);


			 line[0]="90%"+Language.tr("VarianceAnalysis.ConfidenceIntervals");
			 for (int i=0;i<waitingTimeSum.length;i++) {
				 double[] c=calcConfidence(waitingTimeSum2[i],waitingTimeSum[i],simCountNumber,0.9);
				 line[i+1]="["+NumberTools.formatNumber(c[0],2)+" ; "+NumberTools.formatNumber(c[1],2)+"]";
			 }
			 detailTable.addLine(line);
			 line[0]="95%"+Language.tr("VarianceAnalysis.ConfidenceIntervals");
			 for (int i=0;i<waitingTimeSum.length;i++) {
				 double[] c=calcConfidence(waitingTimeSum2[i],waitingTimeSum[i],simCountNumber,0.95);
				 line[i+1]="["+NumberTools.formatNumber(c[0],2)+" ; "+NumberTools.formatNumber(c[1],2)+"]";
			 }
			 detailTable.addLine(line);
		 }

		 if (globalTable!=null) {
			 line[0]="CV ("+model.days+" "+Language.tr("VarianceAnalysis.SimulatedDays.short")+")";
			 for (int i=0;i<waitingTimeSum.length;i++) {
				 double std=calcStd(waitingTimeSum2[i],waitingTimeSum[i],simCountNumber);
				 double mean=(waitingTimeSum[i]/simCountNumber);
				 double cv=(mean>0)?(std/mean):0;
				 line[i+1]=NumberTools.formatNumber(cv*100,3)+"%";
			 }
			 globalTable.addLine(line);
		 }
	 }
 }