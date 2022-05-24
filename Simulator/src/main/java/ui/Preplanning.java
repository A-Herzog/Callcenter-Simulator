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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mathtools.distribution.DataDistributionImpl;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;
import ui.model.CallcenterModelCaller;
import ui.model.CallcenterModelSkillLevel;
import ui.statistic.model.StatisticViewerErlangCTools;

/**
 * Ermöglicht die Agenten-Vorplanung auf Basis eines Erlang-C Modells
 * @author Alexander Herzog
 * @version 1.0
 */
public final class Preplanning {
	/** Kenngröße, für die ein Wert erreicht werden soll */
	private Mode mode;
	/** Soll eine Vereinfachung des Modells vorgenommen werden? */
	private Mode simplify;
	/** Vorgebene Auslastung */
	private double fixedLoadStatus;

	/**
	 * Auswahlmöglichkeiten für die Zielkenngröße und die Modellvereinfachung
	 * @see Preplanning#calc(Mode, Mode, double, boolean, double)
	 */
	public enum Mode {
		/** Vorplanung auf Basis eines vorgegebenen Erreichbarkeitsziels */
		MODE_SUCCESS,

		/** Vorplanung auf Basis eines vorgegebenen Wartezeitziels */
		MODE_WAITING_TIME,

		/** Vorplanung auf Basis eines vorgegebenen Service-Level-Ziels */
		MODE_SERVICE_LEVEL,

		/** Vorplanung auf Basis einer statischen Auslastung */
		MODE_FIXED_LOAD,

		/** Modell vor der Rechnung nicht vereinfachen. */
		SIMPLIFY_NO,

		/** Modell vor der Rechnung vereinfachen, dabei durchschittliche Bedienzeiten verwenden */
		SIMPLIFY_AVERAGE_AHT,

		/** Modell vor der Rechnung vereinfachen, dabei maximale Bedienzeitverteilung für alle Kundentypen verwenden */
		SIMPLIFY_MAX_AHT
	}

	/**
	 * Vorgabewert für den Parameter <code>multiSkillReduction</code>
	 * der Funktion <code>calc</code>.
	 * Die Vorplanung erfolgt für alle Agentengruppen einzeln. Multskill-Agenten
	 * werden anteilig den Kundengruppen, die sie bedienen können, zugeordnet.
	 * Über den Reduktionswert kann angegeben werden, ob weniger als eigentlich
	 * vorgesehen Multiskill-Agenten eingeplant werden sollen.
	 * @see Preplanning#calc(Mode, Mode, double, boolean, double)
	 */
	public static final double DEFAULT_MULTI_SKILL_REDUCTION=0.5;

	/**
	 * Ausgangs-Callcenter-Modell
	 */
	private final CallcenterModel model;

	/**
	 * Sind Multi-Skill-Agenten in den angegebenen Modell enthalten?
	 */
	public boolean isMultiSkillModel;

	/**
	 * Führt die Verarbeitung für einen Kundentyp aus.
	 * @see ClientTypeRunnable
	 */
	private ClientTypeRunnable[] worker=null;

	/**
	 * Führt die Gesamtplanung über alle Kundentypen aus.
	 * @see JoinedRunnable
	 */
	private JoinedRunnable joinedWorker=null;

	/**
	 * Konstruktor der Klasse <code>Preplanning</code>
	 * @param	model	Ausgangs-Callcenter-Modell
	 */
	public Preplanning(final CallcenterModel model) {
		this.model=model.clone();

		/* Modell vorbereiten */
		DataDistributionImpl dist=new DataDistributionImpl(48,48);
		dist.setToValue(0);
		for (CallcenterModelCallcenter callcenter : this.model.callcenter) for (CallcenterModelAgent agents : callcenter.agents) {
			if (!callcenter.active || !agents.active) continue;
			agents.count=-1;
			agents.setCountPerInterval(dist);
		}

		isMultiSkillModel=testIsMultiSkillModel();
	}

	/**
	 * Prüft, ob es sich bei dem betrachteten Modell um ein Modell mit Multiskill-Agenten handelt.
	 * @return	Gibt <code>true</code> zurück im Falle eines Modells mit Multiskill-Agenten.
	 */
	private boolean testIsMultiSkillModel() {
		for (CallcenterModelCallcenter callcenter : model.callcenter) if (callcenter.active) for (CallcenterModelAgent agent : callcenter.agents) if (agent.active) {
			String level=agent.skillLevel;
			for (CallcenterModelSkillLevel skillLevel : model.skills) if (skillLevel.name.equalsIgnoreCase(level)) {
				if (skillLevel.callerTypeName.size()>1) return true;
				break;
			}
		}
		return false;
	}

	/**
	 * Führt mehrere Callcenter-Modelle zusammen.
	 * @param workModel	Ausgangsmodell
	 * @param list	Weitere Modelle
	 * @return	Neues Modell
	 */
	private CallcenterModel joinModels(final CallcenterModel workModel, final CallcenterModel[] list) {
		if (list==null || list.length==0) return workModel;

		CallcenterModel model=workModel.clone();

		for (CallcenterModel m : list) {
			for (int j=0;j<m.callcenter.size();j++) {
				CallcenterModelCallcenter c=m.callcenter.get(j);
				if (!c.active) continue;
				for (int k=0;k<c.agents.size();k++) {
					CallcenterModelAgent a=c.agents.get(k);
					if (!a.active) continue;
					model.callcenter.get(j).agents.get(k).countPerInterval48=model.callcenter.get(j).agents.get(k).countPerInterval48.add(a.countPerInterval48);
				}
			}
		}

		return model;
	}

	/**
	 * Liefert die Namen der aktiven Kundentypen
	 * @param workModel	Modell dem die Daten entnommen werden sollen
	 * @return	Namen der aktiven Kundentypen
	 */
	private String[] getActiveClientTypes(final CallcenterModel workModel) {
		List<String> list=new ArrayList<>();
		for (CallcenterModelCaller caller : workModel.caller) if (caller.active) {
			String s=caller.name.trim();
			if (s.isEmpty() || list.indexOf(s)>=0) continue;
			list.add(s);
		}

		return list.toArray(new String[0]);
	}

	/**
	 * Erstellt ein Callcenter-Modell in dem nur eine bestimmte Anrufergruppe aktiv ist
	 * @param workModel	Modell dem die Daten entnommen werden sollen
	 * @param name	Name der Anrufergruppe, die aktiv sein soll
	 * @return	Neues Callcenter-Modell
	 */
	private CallcenterModel getModelForClientType(final CallcenterModel workModel, final String name) {
		CallcenterModel model=workModel.clone();
		for (CallcenterModelCaller caller : model.caller) if (caller.active) {
			String s=caller.name.trim();
			caller.active=(s.equals(name));
		}
		return model;
	}

	/**
	 * Erstellt ein Callcenter-Modell für einen Kundentyp
	 * @param baseModel	Ausgangsmodell
	 * @param callerTypeName	Kundentypname
	 * @param multiSkillReduction	Reduktion der geplanten Multiskiller gegenüber den Singleskillern (Wert &ge;0, siehe hier <code>DEFAULT_MULTI_SKILL_REDUCTION</code>)
	 * @param agentCount	Anzahl an Agenten pro Halbstundenintervall
	 * @return	Neues Callcenter-Modell
	 */
	private CallcenterModel buildModel(final CallcenterModel baseModel, final String callerTypeName, final double multiSkillReduction, final int[] agentCount) {
		CallcenterModel model=baseModel.clone();

		int matchingGroups=0;
		for (CallcenterModelCallcenter callcenter : model.callcenter) if (callcenter.active) for (CallcenterModelAgent agent : callcenter.agents) if (agent.active) {
			String s=agent.skillLevel.trim();
			for (CallcenterModelSkillLevel skill : model.skills) if (skill.name.trim().equalsIgnoreCase(s)) {matchingGroups++; break;}
		}
		if (matchingGroups==0) matchingGroups=1;

		for (CallcenterModelCallcenter callcenter : model.callcenter) if (callcenter.active) for (CallcenterModelAgent agent : callcenter.agents) if (agent.active) {
			String s=agent.skillLevel.trim();
			double factor=0;
			for (CallcenterModelSkillLevel skill : model.skills) if (skill.name.trim().equalsIgnoreCase(s)) {
				if (skill.callerTypeName.indexOf(callerTypeName)>=0) factor=1/Math.pow(skill.callerTypeName.size(),1+multiSkillReduction);
				break;
			}
			for (int i=0;i<48;i++) agent.countPerInterval48.densityData[i]=Math.max(0,Math.round(factor*agentCount[i]/matchingGroups));
		}

		return model;
	}

	/**
	 * Erstellt ein Callcenter-Modell
	 * @param baseModel	Ausgangsmodell
	 * @param agentCount	Anzahl an Agenten pro Halbstundenintervall
	 * @return	Neues Callcenter-Modell
	 */
	private CallcenterModel buildModel(final CallcenterModel baseModel, final int[] agentCount) {
		CallcenterModel model=baseModel.clone();

		DataDistributionImpl originalSumPerInterval=new DataDistributionImpl(48,48);

		int groups=0;
		for (CallcenterModelCallcenter callcenter : model.callcenter) if (callcenter.active) for (CallcenterModelAgent agent : callcenter.agents) if (agent.active) {
			groups++;
			originalSumPerInterval=originalSumPerInterval.add(agent.countPerInterval48);
		}

		for (CallcenterModelCallcenter callcenter : model.callcenter) if (callcenter.active) for (CallcenterModelAgent agent : callcenter.agents) if (agent.active) {
			DataDistributionImpl factorDist=agent.countPerInterval48.divide(originalSumPerInterval);
			for (int i=0;i<48;i++) {
				double factor=factorDist.densityData[i];
				if (Math.abs(originalSumPerInterval.densityData[i])<10E-10) factor=1.0/Math.max(1,groups);
				agent.countPerInterval48.densityData[i]=Math.max(0,Math.round(factor*agentCount[i]));
			}
		}

		return model;
	}

	/**
	 * Liefert die Mindestanzahl um eine bestimmte Anzahl an Kunden bedienen zu können
	 * @param baseModel	Basismodell
	 * @param callerTypeName	Zu betrachtender Kundentyp
	 * @param multiSkillReduction	Reduktion der geplanten Multiskiller gegenüber den Singleskillern (Wert &ge;0, siehe hier <code>DEFAULT_MULTI_SKILL_REDUCTION</code>)
	 * @return	Agentenanzahl pro Halbstundenintervall
	 */
	private int[] getMinAgentCount(final CallcenterModel baseModel, final String callerTypeName, final double multiSkillReduction) {
		final int[] dummyCount=new int[48];
		Arrays.fill(dummyCount,1000);

		final CallcenterModel newModel=buildModel(baseModel,callerTypeName,multiSkillReduction,dummyCount);

		final StatisticViewerErlangCTools erlangC=new StatisticViewerErlangCTools(newModel,false);
		final double[][] data=erlangC.getCallerAndMu();
		final double[] caller=data[0];
		final double[] mu=data[1];

		final int[] result=new int[48];
		for (int i=0;i<48;i++) {
			result[i]=(int)Math.round(caller[i]/(mu[i]*1800));
		}

		return result;
	}

	/**
	 * Liefert die Mindestanzahl um eine bestimmte Anzahl an Kunden bedienen zu können
	 * @param baseModel	Basismodell
	 * @param scale	Skalierungsfaktor
	 * @return	Agentenanzahl pro Halbstundenintervall
	 */
	private int[] getMinAgentCount(final CallcenterModel baseModel, final double scale) {
		final int[] dummyCount=new int[48];
		Arrays.fill(dummyCount,1000);

		final CallcenterModel newModel=buildModel(baseModel,dummyCount);

		final StatisticViewerErlangCTools erlangC=new StatisticViewerErlangCTools(newModel,false);
		final double[][] data=erlangC.getCallerAndMu();
		final double[] caller=data[0];
		final double[] mu=data[1];

		final int[] result=new int[48];
		for (int i=0;i<48;i++) {
			result[i]=(int)Math.round(caller[i]/(mu[i]*1800)*scale);
		}

		return result;
	}

	/**
	 * Berechnet auf Basis eines Erlang-C-Modells eine Vorplanung für die Anzahl an Agenten
	 * @param mode	Kenngröße, für die ein Wert erreicht werden soll
	 * @param simplify	Soll eine Vereinfachung des Modells vorgenommen werden?
	 * @param value	Zielwert für die Kenngröße
	 * @param extended	Berücksichtigung von Wiederholern?
	 * @param multiSkillReduction	Reduktion der geplanten Multiskiller gegenüber den Singleskillern (Wert &ge;0, siehe hier <code>DEFAULT_MULTI_SKILL_REDUCTION</code>)
	 * @return	Neues Callcenter-Modell, in dem die Agentenzahlen angepasst wurden
	 */
	public CallcenterModel calc(final Mode mode, final Mode simplify, final double value, final boolean extended, final double multiSkillReduction) {
		this.mode=mode;
		this.simplify=simplify;

		CallcenterModel workModel;
		if (this.simplify==Mode.SIMPLIFY_NO) {
			workModel=model;
		} else {
			ModelShrinker shrinker=new ModelShrinker(model);
			workModel=shrinker.calc(this.simplify==Mode.SIMPLIFY_MAX_AHT);
			isMultiSkillModel=false;
		}

		if (mode==Mode.MODE_FIXED_LOAD) {
			return calcFixed(workModel,value,multiSkillReduction);
		} else {
			return calcErlangC(workModel,mode,value,extended,multiSkillReduction);
		}
	}

	/**
	 * Berechnet ein Modell mit vorgegebener Auslastung
	 * @param workModel	Ausgangsmodell
	 * @param load	Ziel-Auslastung
	 * @param multiSkillReduction	Reduktion der geplanten Multiskiller gegenüber den Singleskillern (Wert &ge;0, siehe hier <code>DEFAULT_MULTI_SKILL_REDUCTION</code>)
	 * @return	Neues Modell
	 * @see #calc(Mode, Mode, double, boolean, double)
	 */
	private CallcenterModel calcFixed(final CallcenterModel workModel, final double load, final double multiSkillReduction) {
		final String[] activeClientTypes=getActiveClientTypes(workModel);
		final CallcenterModel[] newModel=new CallcenterModel[activeClientTypes.length];

		for (int i=0;i<activeClientTypes.length;i++) {
			fixedLoadStatus=((double)i/activeClientTypes.length);
			CallcenterModel inputModel=getModelForClientType(workModel,activeClientTypes[i]);
			int[] agentCount=getMinAgentCount(inputModel,activeClientTypes[i],multiSkillReduction);
			for (int j=0;j<agentCount.length;j++) agentCount[j]=(int)Math.round((agentCount[j])/load);
			newModel[i]=buildModel(inputModel,activeClientTypes[i],multiSkillReduction,agentCount);
		}

		fixedLoadStatus=1;
		return joinModels(workModel,newModel);
	}

	/**
	 * Berechnet ein Modell mit einem vorgegebenen Zielwert für eine bestimmte Eigenschaft
	 * @param workModel	Ausgangsmodell
	 * @param mode	Kenngröße, für die ein Wert erreicht werden soll
	 * @param value	Zielwert für die Kenngröße
	 * @param extended	Berücksichtigung von Wiederholern?
	 * @param multiSkillReduction	Reduktion der geplanten Multiskiller gegenüber den Singleskillern (Wert &ge;0, siehe hier <code>DEFAULT_MULTI_SKILL_REDUCTION</code>)
	 * @return	Neues Modell
	 * @see #calc(Mode, Mode, double, boolean, double)
	 */
	private CallcenterModel calcErlangC(final CallcenterModel workModel, final Mode mode, final double value, final boolean extended, final double multiSkillReduction) {
		final String[] activeClientTypes=getActiveClientTypes(workModel);

		/* Einzelne Kundentypen unabhängig planen */
		worker=new ClientTypeRunnable[activeClientTypes.length];
		Thread[] thread=new Thread[activeClientTypes.length];

		for (int i=0;i<activeClientTypes.length;i++) {
			CallcenterModel inputModel=getModelForClientType(workModel,activeClientTypes[i]);
			worker[i]=new ClientTypeRunnable(mode,value,extended,multiSkillReduction,inputModel,activeClientTypes[i]);
			thread[i]=new Thread(worker[i],"Erlang C worker for Client type "+activeClientTypes[i]);
			thread[i].setPriority(Thread.MIN_PRIORITY);
			thread[i].start();
		}

		final CallcenterModel[] modelByClientType=new CallcenterModel[activeClientTypes.length];
		for (int i=0;i<activeClientTypes.length;i++) {
			try {
				thread[i].join();
			} catch (InterruptedException e) {}
			modelByClientType[i]=worker[i].result;
		}

		CallcenterModel intermediateModel=joinModels(workModel,modelByClientType);

		if (!isMultiSkillModel) return intermediateModel;

		/* Gesamtplanung über alle Kundentypen */
		joinedWorker=new JoinedRunnable(mode,value,extended,intermediateModel);
		Thread joinedThread=new Thread(joinedWorker,"Final Erlang C worker");
		joinedThread.setPriority(Thread.MIN_PRIORITY);
		joinedThread.start();
		try {
			joinedThread.join();
		} catch (InterruptedException e) {}
		CallcenterModel finalModel=joinedWorker.result;

		return finalModel;
	}

	/**
	 * Aktueller Fortschritt der Berechnung während des Laufs der Funktion <code>calc</code>
	 * @return	Aktueller Fortschritt, Zahlenwert zwischen 0 und 1
	 * @see Preplanning#calc(Mode, Mode, double, boolean, double)
	 */
	public double getStatus() {
		if (mode==Mode.MODE_FIXED_LOAD) return fixedLoadStatus;

		if (worker==null || worker.length==0) return 0;
		double sum=0;
		for (ClientTypeRunnable w : worker) if (w!=null) sum+=w.status;

		if (isMultiSkillModel) {
			if (joinedWorker!=null) sum+=joinedWorker.status;
			return sum/(worker.length+1);
		} else {
			return sum/worker.length;
		}
	}

	/**
	 * Für die Verarbeitung für einen Kundentyp durch.
	 * @see Preplanning#worker
	 */
	private class ClientTypeRunnable implements Runnable {
		/** Kenngröße, für die ein Wert erreicht werden soll */
		private final Mode mode;
		/** Zielwert für die Kenngröße */
		private final double value;
		/** Berücksichtigung von Wiederholern? */
		private final boolean extended;
		/** Reduktion der geplanten Multiskiller gegenüber den Singleskillern (Wert &ge;0, siehe hier <code>DEFAULT_MULTI_SKILL_REDUCTION</code>) */
		private final double multiSkillReduction;
		/** Ausgangsmodell */
		private final CallcenterModel inputModel;
		/** Name der aktuell zu betrachtenden Kundengruppe */
		private final String callerTypeName;

		/** Ergebnismodell */
		public CallcenterModel result;
		/** Fortschritt bei der Verarbeitung (0..1) */
		public double status;

		/**
		 * Konstruktor der Klasse
		 * @param mode	Kenngröße, für die ein Wert erreicht werden soll
		 * @param value	Zielwert für die Kenngröße
		 * @param extended	Berücksichtigung von Wiederholern?
		 * @param multiSkillReduction	Reduktion der geplanten Multiskiller gegenüber den Singleskillern (Wert &ge;0, siehe hier <code>DEFAULT_MULTI_SKILL_REDUCTION</code>)
		 * @param inputModel	Ausgangsmodell
		 * @param callerTypeName	 Name der aktuell zu betrachtenden Kundengruppe
		 */
		public ClientTypeRunnable(final Mode mode, final double value, final boolean extended, final double multiSkillReduction, final CallcenterModel inputModel, final String callerTypeName) {
			this.mode=mode;
			this.value=value;
			this.extended=extended;
			this.multiSkillReduction=multiSkillReduction;
			this.inputModel=inputModel;
			this.callerTypeName=callerTypeName;
		}

		@Override
		public void run() {
			int[] agentCount=getMinAgentCount(inputModel,callerTypeName,multiSkillReduction);
			CallcenterModel newModel=null;
			StatisticViewerErlangCTools erlangC=null;

			for (int i=0;i<48;i++) {
				status=((double)i)/48;
				while (true) {
					newModel=buildModel(inputModel,callerTypeName,multiSkillReduction,agentCount);

					if (erlangC==null) erlangC=new StatisticViewerErlangCTools(newModel,extended);
					erlangC.calcInterval(newModel,i);
					double[] results;
					switch(mode) {
					case MODE_SUCCESS: results=erlangC.getSuccessProbability(); break;
					case MODE_WAITING_TIME: results=erlangC.getMeanWaitingTime(); break;
					case MODE_SERVICE_LEVEL: results=erlangC.getServiceLevel(); break;
					default: results=new double[48];
					}
					double resultValue=results[i];
					if (Double.isNaN(resultValue) || Double.isInfinite(resultValue)) break;
					if (mode==Mode.MODE_WAITING_TIME) {
						if (resultValue<=value) break;
					} else {
						if (resultValue>=value) break;
					}

					agentCount[i]++;
					if (agentCount[i]>20000) break;
				}
			}

			status=1;
			result=newModel;
		}
	}

	/**
	 * Führt die Gesamtplanung über alle Kundentypen aus.
	 * @see Preplanning#joinedWorker
	 */
	private class JoinedRunnable implements Runnable {
		/** Kenngröße, für die ein Wert erreicht werden soll */
		private final Mode mode;
		/** Zielwert für die Kenngröße */
		private final double value;
		/** Berücksichtigung von Wiederholern? */
		private final boolean extended;
		/** Ausgangsmodell */
		private final CallcenterModel inputModel;

		/** Ergebnismodell */
		public CallcenterModel result;
		/** Fortschritt bei der Verarbeitung (0..1) */
		public double status;

		/**
		 * Konstruktor der Klasse
		 * @param mode	Kenngröße, für die ein Wert erreicht werden soll
		 * @param value	Zielwert für die Kenngröße
		 * @param extended	Berücksichtigung von Wiederholern?
		 * @param inputModel	Ausgangsmodell
		 */
		public JoinedRunnable(final Mode mode, final double value, final boolean extended, final CallcenterModel inputModel) {
			this.mode=mode;
			this.value=value;
			this.extended=extended;
			this.inputModel=inputModel;
		}

		@Override
		public void run() {
			int[] agentCount=getMinAgentCount(inputModel,0.5);
			CallcenterModel newModel=null;
			StatisticViewerErlangCTools erlangC=null;

			for (int i=0;i<48;i++) {
				status=((double)i)/48;
				while (true) {
					newModel=buildModel(inputModel,agentCount);

					if (erlangC==null) erlangC=new StatisticViewerErlangCTools(newModel,extended);
					erlangC.calcInterval(newModel,i);

					double[] results;
					switch(mode) {
					case MODE_SUCCESS: results=erlangC.getSuccessProbability(); break;
					case MODE_WAITING_TIME: results=erlangC.getMeanWaitingTime(); break;
					case MODE_SERVICE_LEVEL: results=erlangC.getServiceLevel(); break;
					default: results=new double[48];
					}
					double resultValue=results[i];
					if (Double.isNaN(resultValue) || Double.isInfinite(resultValue)) break;
					if (mode==Mode.MODE_WAITING_TIME) {
						if (resultValue<=value) break;
					} else {
						if (resultValue>=value) break;
					}

					agentCount[i]++;
					if (agentCount[i]>20000) break;
				}
			}

			status=1;
			result=newModel;
		}
	}
}