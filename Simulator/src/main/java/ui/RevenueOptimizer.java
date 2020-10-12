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
package ui;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import language.Language;
import mathtools.NumberTools;
import net.calc.StartAnySimulator;
import simulator.CallcenterSimulatorInterface;
import simulator.Statistics;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;
import ui.model.CallcenterModelCaller;

/**
 * Versucht den Gesamtertrag des Callcenter-Systems durch eine
 * Veränderung der Agentenanzahl zu maximieren.
 * @author Alexander Herzog
 * @version 1.0
 */
public class RevenueOptimizer {
	private static final double DEFAULT_SIGNIFICANCE_LEVEL=100;

	private final CallcenterModel baseModel;
	/** Stellt ein, ab welchem Unterschied (relativ zur Standardabweichung des Ertrags des Basismodells und der Agentenanzahl) zwei Modelle als unterschiedlch angesehen werden. */
	private final double signficanceLevel;
	private int workForce;
	private CallcenterModel bestModel;
	private double baseRevenue;
	private double baseRevenueSD;
	private double bestRevenue;
	private int steps;
	private int agentsAdded;
	private int agentsRemoved;

	private String error=null;
	private boolean abortOptimization=false;

	/**
	 * Konstruktor der Klasse <code>RevenueOptimizer</code>
	 * @param baseModel	Zu optimierendes Ausgangsmodell (das Ausgangsmodell wird nicht verändert, es wird mit einer Kopie gearbeitet)
	 * @param signficanceLevel	Stellt ein, ab welchem Unterschied (relativ zur Standardabweichung des Ertrags des Basismodells und der Agentenanzahl) zwei Modelle als unterschiedlch angesehen werden.
	 */
	public RevenueOptimizer(final CallcenterModel baseModel, double signficanceLevel) {
		this.baseModel=prepareModel(baseModel);
		this.signficanceLevel=signficanceLevel;
	}

	/**
	 * Konstruktor der Klasse <code>RevenueOptimizer</code>
	 * @param baseModel	Zu optimierendes Ausgangsmodell (das Ausgangsmodell wird nicht verändert, es wird mit einer Kopie gearbeitet)
	 */
	public RevenueOptimizer(final CallcenterModel baseModel) {
		this(baseModel,DEFAULT_SIGNIFICANCE_LEVEL);
	}

	/**
	 * Schälgt der Aufruf einer Funktion fehlt, so wird hier die Fehlermeldung geliefert.
	 * @return	Fehlermeldung zu dem fehlgeschlagenen Funktionsaufruf.
	 */
	public String getError() {
		return error;
	}

	private CallcenterModel prepareModel(final CallcenterModel model) {
		CallcenterModel model2=model.clone();

		int s=24;
		for (CallcenterModelCallcenter callcenter : model2.callcenter) if (callcenter.active) {
			for (CallcenterModelAgent agents : callcenter.agents) if (agents.active) {
				if (agents.count==-2 && s==24) s=48;
				if (agents.count==-1) {
					if (agents.countPerInterval48!=null && s==24) s=48;
					if (agents.countPerInterval96!=null && s<96) s=96;
				}
			}
		}
		steps=Math.max(s,48);

		for (CallcenterModelCallcenter callcenter : model2.callcenter) if (callcenter.active) {
			for (CallcenterModelAgent agents : callcenter.agents) if (agents.active && agents.count==-1) agents.stretchCountPerInterval(steps);
		}

		return model2;
	}

	/**
	 * Prüft das im Konstruktor übergebene Modell ohne weitere (Zeit verbrauchende) Schritte durchzuführen.
	 * @return	Gibt <code>true</code> zurück, wenn das Modell verwendet werden kann. Im Fehlerfalle kann über <code>getError</code> die Fehlermeldung abgerufen werden.
	 * @see #getError()
	 */
	public boolean check() {
		StartAnySimulator start=new StartAnySimulator(baseModel);
		error=start.check();
		if (error!=null) return false;
		return true;
	}

	/**
	 * Prüft, ob überhaupt Kosten und/oder Erträge definiert sind.
	 * Wenn nicht braucht keine Optimierung vorgenommen werden.
	 * @return	Liefert <code>true</code> zurück, wenn Kosten und/oder Erträge definiert sind.
	 */
	public boolean checkCostsAvailable() {
		for (CallcenterModelCaller caller : baseModel.caller) if (caller.active) {
			if (Math.abs(caller.costPerCancel)>1E-8) return true;
			if (Math.abs(caller.costPerWaitingSec)>1E-8) return true;
			if (Math.abs(caller.revenuePerClient)>1E-8) return true;
		}

		for (CallcenterModelCallcenter callcenter : baseModel.callcenter) if (callcenter.active) for (CallcenterModelAgent agents : callcenter.agents) if (agents.active) {
			if (Math.abs(agents.costPerWorkingHour)>1E-8) return true;

			if (agents.costPerCall!=null) for (Double D: agents.costPerCall) if (D!=null && Math.abs(D)>1E-8) return true;
			if (agents.costPerCallMinute!=null) for (Double D: agents.costPerCallMinute) if (D!=null && Math.abs(D)>1E-8) return true;
		}

		return false;
	}

	private Double calcRevenue(CallcenterModel model) {
		StartAnySimulator start=new StartAnySimulator(model);
		error=start.check();
		if (error!=null) return null;

		CallcenterSimulatorInterface simulator=start.run();

		simulator.start(false);
		error=simulator.finalizeRun(); if (error!=null) return null;

		Statistics statistic=simulator.collectStatistic();

		double sum=statistic.kundenGlobal.revenue-(statistic.kundenGlobal.costCancel+statistic.kundenGlobal.costWaiting+statistic.agentenGlobal.costOfficeTime+statistic.agentenGlobal.costCalls+statistic.agentenGlobal.costProcessTime);
		return sum/statistic.simulationData.runRepeatCount;
	}

	/**
	 * Gibt den übergebenen Statustext auf der Konsole aus.
	 * Diese Funktion kann von abgeleiteten Klassen überschrieben werden,
	 * um andere Ausgabemodi zu erlauben.
	 * @param isHeading Gibt an, ob es sich bei der Meldung um eine Überschrift (die ggf. anders dargestellt werden könnte) handelt.
	 * @param text	Text, der ausgegeben werden soll.
	 */
	protected void statusOutput(boolean isHeading, String text) {
		System.out.println(text);
	}

	/**
	 * Liefert den Ertrag des Basismodells zurück.
	 * @return	Ertrag des Basismodells.
	 */
	public double getBaseRevenue() {
		return baseRevenue;
	}

	/**
	 * Liefert den Ertrag des optimierten Modells zurück.
	 * @return	Ertrag des optimierten Modells.
	 */
	public double getBestRevenue() {
		return bestRevenue;
	}

	/**
	 * Liefert das Basismodell zurück.
	 * @return	Basismodell
	 */
	public CallcenterModel getBaseModel() {
		return baseModel;
	}

	/**
	 * Liefert das optimierte Modell zurück.
	 * @return	Optimiertes Modell
	 */
	public CallcenterModel getBestModel() {
		return bestModel;
	}

	/**
	 * Liefert zurück, wie viele Agenten bisher hinzugefügt und entfernt wurden.
	 * @return	Array aus 2 Elementen: hinzugefügte Agentenintervalle, entfernte Agentenintervalle.
	 */
	public int[] getAgentsNumberChange() {
		return new int[]{agentsAdded, agentsRemoved};
	}

	private int calcWorkForce() {
		int sum=0;
		for (CallcenterModelCallcenter callcenter : baseModel.callcenter) if (callcenter.active) for (CallcenterModelAgent agents : callcenter.agents) if (agents.active) {
			if (agents.count>0) {
				sum+=agents.count*(agents.workingNoEndTime?86400:agents.workingTimeEnd-agents.workingTimeStart)/1800;
			} else {
				if (agents.count==-1) {
					if (agents.countPerInterval24!=null) sum+=Math.round(agents.countPerInterval24.sum())*2;
					if (agents.countPerInterval48!=null) sum+=Math.round(agents.countPerInterval48.sum());
					if (agents.countPerInterval96!=null) sum+=Math.round(agents.countPerInterval96.sum())/2;
				}
				if (agents.count==-2) sum+=agents.byCallersAvailableHalfhours;
			}
		}
		return sum;
	}

	private boolean runGroupAddAgents(int iNr, int cNr, int aNr, double signficanceLevelInt, int changeQuantity) {
		CallcenterModel workingModel=bestModel.clone();
		while (!abortOptimization) {
			CallcenterModelAgent workingAgents=workingModel.callcenter.get(cNr).agents.get(aNr);
			if (steps==24) workingAgents.countPerInterval24.densityData[iNr]+=changeQuantity;
			if (steps==48) workingAgents.countPerInterval48.densityData[iNr]+=changeQuantity;
			if (steps==96) workingAgents.countPerInterval96.densityData[iNr]+=changeQuantity;
			Double D=calcRevenue(workingModel); if (D==null) return false;
			if (D<bestRevenue+signficanceLevelInt) break;
			if (changeQuantity>1) {
				statusOutput(false,String.format(Language.tr("RevenueOptimizer.Working.Add.Multiple"),changeQuantity,aNr+1,NumberTools.formatNumber(D),NumberTools.formatNumber(D-bestRevenue)));
			} else {
				statusOutput(false,String.format(Language.tr("RevenueOptimizer.Working.Add.Single"),aNr+1,NumberTools.formatNumber(D),NumberTools.formatNumber(D-bestRevenue)));
			}
			bestModel=workingModel.clone();
			bestRevenue=D;
			agentsAdded+=changeQuantity;
		}
		return true;
	}

	private boolean runGroupRemoveAgents(int iNr, int cNr, int aNr, double signficanceLevelInt, int changeQuantity) {
		CallcenterModel workingModel=bestModel.clone();
		while (!abortOptimization) {
			CallcenterModelAgent workingAgents=workingModel.callcenter.get(cNr).agents.get(aNr);
			if (steps==24) {
				if (workingAgents.countPerInterval24.densityData[iNr]<changeQuantity) break;
				workingAgents.countPerInterval24.densityData[iNr]-=changeQuantity;
			}
			if (steps==48) {
				if (workingAgents.countPerInterval48.densityData[iNr]<changeQuantity) break;
				workingAgents.countPerInterval48.densityData[iNr]-=changeQuantity;
			}
			if (steps==96) {
				if (workingAgents.countPerInterval96.densityData[iNr]<changeQuantity) break;
				workingAgents.countPerInterval96.densityData[iNr]-=changeQuantity;
			}
			Double D=calcRevenue(workingModel); if (D==null) return false;
			if (D<bestRevenue+signficanceLevelInt) break;

			if (changeQuantity>1) {
				statusOutput(false,String.format(Language.tr("RevenueOptimizer.Working.Remove.Multiple"),changeQuantity,aNr+1,NumberTools.formatNumber(D),NumberTools.formatNumber(D-bestRevenue)));
			} else {
				statusOutput(false,String.format(Language.tr("RevenueOptimizer.Working.Remove.Single"),aNr+1,NumberTools.formatNumber(D),NumberTools.formatNumber(D-bestRevenue)));
			}
			bestModel=workingModel.clone();
			bestRevenue=D;
			agentsRemoved+=changeQuantity;
		}
		return true;
	}

	private boolean runGroup(int iNr, int cNr, int aNr) {
		double signficanceLevelInt=signficanceLevel*baseRevenueSD/workForce;

		for (int quantity : new int[]{8,4,2,1}) {
			if (!runGroupAddAgents(iNr,cNr,aNr,signficanceLevelInt,quantity)) return false;
			if (abortOptimization) return true;
		}
		for (int quantity : new int[]{8,4,2,1}) {
			if (!runGroupRemoveAgents(iNr,cNr,aNr,signficanceLevelInt,quantity)) return false;
			if (abortOptimization) return true;
		}

		return true;
	}

	private boolean runInterval(int nr) {
		for (int i=0;i<baseModel.callcenter.size();i++) {
			CallcenterModelCallcenter callcenter=baseModel.callcenter.get(i);
			if (callcenter.active) {
				statusOutput(true,String.format(Language.tr("RevenueOptimizer.Working.IntervalCallcenter"),nr+1,steps,callcenter.name));
				for (int j=0;j<callcenter.agents.size();j++) {
					CallcenterModelAgent agents=callcenter.agents.get(j);
					if (agents.active && agents.count==-1) {
						if (!runGroup(nr,i,j)) return false;
						if (abortOptimization) return true;
					}
				}
			}
		}

		return true;
	}

	/**
	 * Führt die eigentliche Optimierung aus
	 * @return	Gibt <code>true</code> zurück, wenn die Optimierung erfolgreich abgeschlossen werden konnte.
	 */
	public boolean run() {
		if (!checkCostsAvailable()) {
			statusOutput(false,Language.tr("RevenueOptimizer.NoCostsDefined"));
			baseRevenue=0; baseRevenueSD=0;
			bestModel=baseModel.clone();
			return true;
		}

		long startTime=System.currentTimeMillis();

		workForce=calcWorkForce();

		statusOutput(false,Language.tr("RevenueOptimizer.RevenueForInitialModel.Calculating"));
		double revenue[]=new double[10];
		for (int i=0;i<revenue.length;i++) {
			Double D=calcRevenue(baseModel); if (D==null) return false;
			revenue[i]=D;
			if (abortOptimization) return true;
		}
		baseRevenue=new Mean().evaluate(revenue);
		baseRevenueSD=new StandardDeviation().evaluate(revenue);

		statusOutput(false,Language.tr("RevenueOptimizer.RevenueForInitialModel")+": "+NumberTools.formatNumber(baseRevenue)+ " ("+Language.tr("Distribution.StdDev")+": "+NumberTools.formatNumber(baseRevenueSD)+")");

		bestRevenue=baseRevenue;
		bestModel=baseModel;

		for (int i=0;i<steps;i++) {
			if (!runInterval(i)) return false;
			if (abortOptimization) break;
		}

		String s="";
		if (baseRevenue>0.1 || baseRevenue<-0.1) s=" (+"+NumberTools.formatPercent((bestRevenue-baseRevenue)/Math.abs(baseRevenue))+")";
		statusOutput(true,Language.tr("RevenueOptimizer.OptimizationResults"));
		statusOutput(false,Language.tr("RevenueOptimizer.Revenue")+": "+NumberTools.formatNumber(baseRevenue)+" -> "+NumberTools.formatNumber(bestRevenue)+s);
		statusOutput(false,Language.tr("RevenueOptimizer.Result.AgentsAdded")+": "+agentsAdded);
		statusOutput(false,Language.tr("RevenueOptimizer.Result.AgentsRemoved")+": "+agentsRemoved);
		statusOutput(false,Language.tr("RevenueOptimizer.Result.NeededTime")+": "+((System.currentTimeMillis()-startTime)/1000)+" "+Language.tr("Statistic.Seconds"));

		return true;
	}

	/**
	 * Bricht den Optimierungsprozess ab.
	 */
	public void setAbort() {
		abortOptimization=true;
	}
}
