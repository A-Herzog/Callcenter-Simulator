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
package simulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mathtools.distribution.DataDistributionImpl;
import simcore.Event;
import simcore.SimData;
import simulator.events.CallCancelEvent;
import simulator.events.CallEvent;
import simulator.events.Service1StartEvent;
import ui.model.CallcenterRunModel;
import ui.model.CallcenterRunModelAgent;
import ui.model.CallcenterRunModelCallcenter;
import ui.model.CallcenterRunModelCaller;
import ui.model.CallcenterRunModelSkillLevel;

/**
 * Diese Klasse hält die Daten, die sich dynamisch während der Simulation ändern vor.
 * @author Alexander Herzog
 * @version 1.0
 */
public final class RunData {
	/**
	 * Werden in dem Modell Mindestwartezeiten verwendet?
	 */
	private final boolean minWaitingTimeActive;

	/**
	 * Liste mit den im System verfügbaren Agenten
	 */
	public AgentRecord[] agentRecord;

	private CallerRecord[] callerRecordCache=null;
	private CallerRecord[] callerRecordTempCache=null;
	private int callerRecordCacheUsed=0;
	private int callerRecordCacheFill=0;
	private int callerRecordTempCacheCount=0;

	private final CallcenterRunModelCaller[] queueCallerTypes;

	/**
	 * Warteschlangen nach Kundentypen
	 */
	public final ArrayList<CallerRecord>[] queueByType;

	/**
	 * Außerhalb (=Übertrag vom Vortag) wartende Kunden nach Kundentypen
	 */
	public final ArrayList<CallerRecord>[] externalQueueByType;

	/**
	 * Anzahl an Kunden in der Warteschlange
	 */
	private int queueLength;

	private int phoneCallQueueLength;

	/**
	 * Liste der momentan im Leerlauf befindlichen Agenten
	 */
	public final List<AgentRecord> freeAgents;

	/**
	 * Anzahl der momentan arbeitenden Agenten
	 */
	public int workingAgentsCount;

	/**
	 * Cache. Wird in CallEvent.processCall verwendet.
	 */
	public final double[] workingAgentsCountArray=new double[1];

	/**
	 * Konstruktor der Klasse
	 * @param model	Laufzeitmodell zu dem in diesem Objekt zusätzliche Daten gespeichert werden sollen
	 */
	@SuppressWarnings("unchecked")
	public RunData(final CallcenterRunModel model) {
		minWaitingTimeActive=model.callerMinWaitingTimeUsed;

		int initCacheSize=0;
		for (CallcenterRunModelCaller caller: model.caller) initCacheSize+=caller.freshCallsCountMean;
		callerRecordCache=new CallerRecord[Math.max(1024,Math.min(8192,initCacheSize))];
		callerRecordTempCache=new CallerRecord[Math.max(1024,Math.min(8192,initCacheSize))];

		queueCallerTypes=model.caller;

		queueByType=new ArrayList[model.caller.length];
		for (int i=0;i<model.caller.length;i++) queueByType[i]=new ArrayList<CallerRecord>();

		externalQueueByType=new ArrayList[model.caller.length];
		for (int i=0;i<model.caller.length;i++) externalQueueByType[i]=new ArrayList<CallerRecord>();

		freeAgents=new ArrayList<AgentRecord>(); /* SplitList bringt leider nichts */
	}

	private long lastQueueLengthChangeTime;
	private int lastQueueLength;

	private void queueLengthChanged(final long now, final Statistics statistic) {
		/* Neuer Tag */
		if (now<lastQueueLengthChangeTime) {
			lastQueueLengthChangeTime=now;
			lastQueueLength=queueLength;
			return;
		}

		/* Maximallänge erfassen */
		statistic.maxQueueLength=Math.max(statistic.maxQueueLength,queueLength);

		if (lastQueueLength!=0) {
			/* Mittlere Warteschlangenlänge über alles */
			final long deltaLong=now-lastQueueLengthChangeTime;
			if (deltaLong>0) {
				final double delta=((double)deltaLong)/1000/86400;
				statistic.meanQueueLength+=lastQueueLength*delta;

				/* Mittlere Warteschlangenlänge pro Intervall */
				addIntervalParts(statistic.meanQueueLengthProIntervall,(int)lastQueueLengthChangeTime,(int)now,lastQueueLength);
			}
		}

		/* Neue Warteschlangenlänge speichern */
		lastQueueLength=queueLength;
		lastQueueLengthChangeTime=now;
	}

	/*
	public int getQueueLength() {
		return queueLength;
	}
	 */

	/**
	 * Liefert die Anzahl an Kunden in der Warteschlange die eine Telefonleitung belegen.
	 * @return	Anzahl an Kunden in der Warteschlange die eine Telefonleitung belegen
	 */
	public int getPhoneCallQueueLength() {
		return phoneCallQueueLength;
	}

	/**
	 * Fügt einen Kunden an die (interne) Warteschlange an.
	 * @param callerRecord	Kundendatensatz
	 * @param time	Zeitpunkt
	 * @param statistic	Statistikobjekt
	 */
	public void addCallerToQueue(final CallerRecord callerRecord, final long time, final Statistics statistic) {
		final CallcenterRunModelCaller callerType=callerRecord.callerType;
		queueByType[callerType.index].add(callerRecord);
		if (callerType.blocksLine) phoneCallQueueLength++;
		queueLength++;
		queueLengthChanged(time,statistic);
	}

	/**
	 * Entfernt einen Kunden aus der (interne) Warteschlange.
	 * @param callerRecord	Kundendatensatz
	 * @param time	Zeitpunkt
	 * @param statistic	Statistikobjekt
	 */
	public void removeCallerFromQueue(final CallerRecord callerRecord, final long time, final Statistics statistic) {
		final CallcenterRunModelCaller callerType=callerRecord.callerType;
		if (queueByType[callerType.index].remove(callerRecord)) {
			if (callerType.blocksLine) phoneCallQueueLength--;
			queueLength--;
			queueLengthChanged(time,statistic);
		}
	}

	/**
	 * Fügt einen Kunden an die externe Warteschlange an.
	 * @param callerRecord	Kundendatensatz
	 */
	public void addCallerToExternalQueue(final CallerRecord callerRecord) {
		externalQueueByType[callerRecord.callerType.index].add(callerRecord);
	}

	/**
	 * Entfernt einen Kunden aus der externe Warteschlange.
	 * @param callerRecord	Kundendatensatz
	 */
	public void removeCallerFromExternalQueue(final CallerRecord callerRecord) {
		externalQueueByType[callerRecord.callerType.index].remove(callerRecord);
	}

	private void matchCallerAgent(final long now, final CallerRecord caller, final AgentRecord agent, final boolean removeAgentFromFreeAgentsList, final int freeAgentIndex, final SimData data, final boolean callerIsInQueue) {
		/* Agent ist nicht mehr frei */
		if (removeAgentFromFreeAgentsList) {
			if (freeAgentIndex>=0) {
				freeAgents.remove(freeAgentIndex);
			} else {
				freeAgents.remove(agent);
			}
		}

		/* Anrufer aus Warteschlange entfernen */
		assert (callerIsInQueue==(queueByType[caller.callerType.index].indexOf(caller)>=0));
		if (callerIsInQueue) {
			removeCallerFromQueue(caller,now,((SimulationData)data).statisticSimData);
		}

		/* Technische Bereitzeit beginnt */
		agent.logStatusChange(now,agent.status,AGENT_TECHNISCHER_LEERLAUF,caller);
		agent.status=AGENT_TECHNISCHER_LEERLAUF;

		/* Gesprächsbeginn-Ereignis anlegen */
		Service1StartEvent startEvent=((Service1StartEvent)data.getEvent(Service1StartEvent.class));
		startEvent.init(now+agent.callcenter.technicalFreeTime*1000);
		startEvent.caller=caller;
		startEvent.agent=agent;
		data.eventManager.addEvent(startEvent);

		if (!agent.callcenter.technicalFreeTimeIsWaitingTime) {
			if (caller.callCancelEvent!=null) data.eventManager.deleteEvent(caller.callCancelEvent,data);
			caller.callCancelEvent=null;
		}

		caller.agentAssignedStartEvent=startEvent;

		/* Recheck-Events löschen */
		if (minWaitingTimeActive && caller.reCheckEvents!=null) {
			for (int i=0;i<caller.reCheckEvents.length;i++) data.eventManager.deleteEvent(caller.reCheckEvents[i],data);
			caller.reCheckEvents=null;
		}
	}

	/**
	 * Sucht einen passenden freien Agenten für einen Anrufer
	 * @param now	Aktuelle Simulationszeit
	 * @param callerRecord	Kundendatensatz für den ein Agent gefunden werden soll
	 * @param data	Simulationsdatenobjekt
	 * @param callerIsInQueue	Befindet sich der Kunde momentan in der Warteschlange? (Entfernt ihn dann ggf. bei Erfolg)
	 * @return	Passender Agenten-Datensatz oder <code>null</code>, wenn kein passender Agent gefunde wurde
	 */
	public AgentRecord findAgentForCaller(final long now, final CallerRecord callerRecord, final SimData data, final boolean callerIsInQueue) {
		if (freeAgents.isEmpty()) return null;

		AgentRecord bestAgent=null;
		double bestScore=-1;
		int bestIndex=-1;

		final CallcenterRunModelCaller callerType=callerRecord.callerType;
		final int callerTypeIndex=callerType.index;

		final int freeAgentsSize=freeAgents.size();
		for (int i=0;i<freeAgentsSize;i++) {
			final AgentRecord agent=freeAgents.get(i);

			/* Kann der Agent den Kunden bedienen ?*/
			final int skillLevelNr=agent.skillLevel.callerTypeByIndex[callerTypeIndex];
			if (skillLevelNr<0) continue;

			final CallcenterRunModelCallcenter callcenter=agent.callcenter;

			/* Mindestwartezeit erfüllt ? */
			if (minWaitingTimeActive) {
				final int j=callcenter.callerMinWaitingTimeClass.indexOf(callerType);
				if (j>=0) {
					if (callcenter.callerMinWaitingTimeMilliSecond.get(j)>now-callerRecord.startWaitingTime) continue;
				}
			}

			/* Agenten-Score berechnen */
			double score=callcenter.score+agent.skillLevel.callerTypeScore[skillLevelNr]; /* Callcenter Basisscore + Score des Skilllevels für diesen Kundentyp */
			double d=callcenter.agentScoreFreeTimePart; if (d!=0) score+=d*agent.getFreeTimePart(); /* Score für Leerlaufanteil */
			d=callcenter.agentScoreFreeTimeSinceLastCall; if (d!=0) score+=d*agent.getFreeTimeSinceLastCall(now); /* Score für Leerlauf seit letztem Anruf */

			if (score>bestScore) {bestAgent=agent; bestScore=score; bestIndex=i;}
		}

		/* Kein Agent gefunden ? */
		if (bestAgent==null) return null;

		matchCallerAgent(now,callerRecord,bestAgent,true,bestIndex,data,callerIsInQueue);
		return bestAgent;
	}

	/**
	 * Sucht einen passenden freien Agenten für einen Anrufer
	 * @param now	Aktuelle Simulationszeit
	 * @param callerRecord	Kundendatensatz für den ein Agent gefunden werden soll
	 * @param data	Simulationsdatenobjekt
	 * @param callerIsInQueue	Befindet sich der Kunde momentan in der Warteschlange? (Entfernt ihn dann ggf. bei Erfolg)
	 * @return	Passender Agenten-Datensatz oder <code>null</code>, wenn kein passender Agent gefunde wurde
	 */



	/**
	 * Sucht einen passenden wartenden Kunden für einen Agenten
	 * @param now	Aktuelle Simulationszeit
	 * @param agentRecord	Agentendatzsatz für den ein Kunde gefunden werden soll
	 * @param data	Simulationsdatenobjekt
	 * @param removeAgentFromFreeAgentsList	Soll der Agent (wenn ein Kunde gefunden wurde) auch gleich aus der Liste der verfügbaren Agenten ausgetragen werden?
	 * @return	Passender Kunden-Datensatz oder <code>null</code>, wenn kein passender Kunde gefunde wurde
	 */
	public CallerRecord findCallerForAgent(final long now, final AgentRecord agentRecord, final SimData data, final boolean removeAgentFromFreeAgentsList) {
		CallerRecord bestCaller=null;
		double bestScore=Double.NEGATIVE_INFINITY;

		final short[] skills=agentRecord.skillLevel.callerTypeByIndex;

		List<CallerRecord> list;
		int listSize;
		CallerRecord caller;
		CallcenterRunModelCaller callerType;
		int k;
		double score;
		for (int i=0;i<queueByType.length;i++) {
			list=queueByType[i];
			if (list.isEmpty()) continue;

			/* Kann der Agent den Kunden bedienen ?*/
			if (skills[queueCallerTypes[i].index]<0) continue;

			listSize=list.size();
			for (int j=0;j<listSize;j++) {
				caller=list.get(j);
				callerType=caller.callerType;

				/* Mindestwartezeit erfüllt ? */
				if (minWaitingTimeActive) {
					k=agentRecord.callcenter.callerMinWaitingTimeClass.indexOf(callerType);
					if (k>=0) {
						if (agentRecord.callcenter.callerMinWaitingTimeMilliSecond.get(k)>now-caller.startWaitingTime) continue;
					}
				}

				/* Caller-Score berechnen */
				score=callerType.scoreBase;
				if (caller.callContinued) score+=callerType.scoreContinued;
				if (Math.abs(callerType.scoreMilliSecond)>=0.0000001) score+=callerType.scoreMilliSecond*(now-caller.startWaitingTime);

				if (score>bestScore) {bestCaller=caller; bestScore=score;}
			}
		}

		/* Kein Kunde gefunden ? */
		if (bestCaller==null) return null;

		matchCallerAgent(now,bestCaller,agentRecord,removeAgentFromFreeAgentsList,-1,data,true);
		return bestCaller;
	}

	private final double factor=1.0/1800.0/1000.0;

	private void addIntervalParts(final DataDistributionImpl dist, final int timeFrom, final int timeTo, final int multiply) {
		if (timeFrom==timeTo) return;
		final double[] data=dist.densityData;
		final int x=Math.min(47,Math.max(0,(timeFrom/1800/1000-1)));
		final int y=Math.min(47,Math.max(0,(timeTo/1800/1000+1)));
		final double mul=multiply*factor;
		int j,start,end;
		for (int i=x;i<=y;i++) {
			j=i*1800*1000;
			start=Math.max(j,timeFrom);
			end=Math.min(j+1800*1000-1,timeTo);
			if (end>start) data[i]+=(end-start)*mul;
		}
	}

	private boolean getIntervalPartsWithoutDiv(final double[] data, final int[] intervals, final int timeFrom, final int timeTo) {
		if (timeFrom==timeTo) {
			intervals[0]=0; intervals[1]=-1;
			/* brauchen wir nicht mehr: Arrays.fill(data,0,47+1,0); /* for (int i=0;i<48;i++) data[i]=0; */ /* Achtung: Parameter "toIndex" bei Arrays.fill ist EXKLUSIV ! */
			return false;
		}

		final int x=Math.min(47,Math.max(0,(timeFrom/1800/1000-1)));
		final int y=Math.min(47,Math.max(0,(timeTo/1800/1000+1)));
		int j,start,end;
		/* brauchen wir nicht mehr:  if (x-1>=0) Arrays.fill(data,0,x-1+1,0); /* for (int i=0;i<x;i++) data[i]=0; */ /* Achtung: Parameter "toIndex" bei Arrays.fill ist EXKLUSIV ! */
		intervals[0]=x; intervals[1]=y;
		for (int i=x;i<=y;i++) {
			j=i*1800*1000;
			start=Math.max(j,timeFrom);
			end=Math.min(j+1800*1000-1,timeTo);
			if (end>start) data[i]=(end-start); else data[i]=0;
		}
		/* brauchen wir nicht mehr: if (y+1<=47) Arrays.fill(data,y+1,47+1,0); /* for (int i=y+1;i<48;i++) data[i]=0; */ /* Achtung: Parameter "toIndex" bei Arrays.fill ist EXKLUSIV ! */

		return true;
	}

	/**
	 * Markiert alle Kundendatensätze als unbenutzt.
	 */
	public void cacheAllCallerRecords() {
		callerRecordCacheUsed=0;
		callerRecordTempCacheCount=0;
	}

	/**
	 * Überträgt einen Kundendatensatz in den Cache
	 * @param record	Nicht mehr benötigter Datensatz für den Cache
	 */
	public void cacheSingleRecord(CallerRecord record) {
		if (callerRecordTempCacheCount==callerRecordTempCache.length) return;
		callerRecordTempCache[callerRecordTempCacheCount]=record;
		callerRecordTempCacheCount++;
	}

	/**
	 * Liefert einen neuen Kundendatensatz (entweder aus dem Cache oder neu angelegt)
	 * @return	Neuer Kundendatensatz
	 */
	public CallerRecord getNewCallerRecord() {
		CallerRecord record;

		if (callerRecordTempCacheCount>0) {
			callerRecordTempCacheCount--;
			record=callerRecordTempCache[callerRecordTempCacheCount];
			record.reinit();
			return record;
		}

		if (callerRecordCacheUsed==callerRecordCacheFill) {
			record=new CallerRecord();
			if (callerRecordCacheFill==callerRecordCache.length) callerRecordCache=Arrays.copyOf(callerRecordCache,callerRecordCache.length*2);
			callerRecordCache[callerRecordCacheFill]=record;
			callerRecordCacheFill++;
		} else {
			record=callerRecordCache[callerRecordCacheUsed];
			record.reinit();
		}

		callerRecordCacheUsed++;
		return record;
	}

	/**
	 * Laufzeitdaten eines Kunden
	 * @author Alexander Herzog
	 * @see RunData#queueByType
	 * @see RunData#externalQueueByType
	 */
	public final class CallerRecord {
		/** Start der Wartezeit */
		public long startWaitingTime;
		/** Zählung der Wiederholungen */
		public short retryCount;
		/** Zeitpunkt des ersten Anrufversuchs */
		public long firstCallTime;
		/** Handelt es sich um einen weitergeleiteten Anruf? */
		public boolean callContinued;
		/** Handelt es sich um einen Wiederanrufer */
		public boolean isRecall;
		/** Zugehöriger Kundentyp */
		public CallcenterRunModelCaller callerType;
		/** Statistikobjekt für diesen Kunden (gemäß Kundentyp) */
		public Statistics.KundenDaten statisticClient;
		/** Statistikobjekt für diesen Anruf (gemäß Kundentyp) */
		public Statistics.KundenDaten statisticCall;
		/** Zu einem wartenden Kunden gehöriges Warteabbruch-Event (um dieses ggf. vor der Ausführung zu löschen) */
		public CallCancelEvent callCancelEvent;
		/** Evtl. zusätzliche ReCheck-Events (nach dem Ende von Mindestwartezeiten) */
		public Event[] reCheckEvents;
		/** Aktuell gültiges Wiederhol-Event */
		public CallEvent retryEvent;
		/** Zuordnung Agent-Kunde (um Abbrüche während der technischen Bereitzeit verarbeiten zu können */
		public Service1StartEvent agentAssignedStartEvent;

		/** Wartezeitübertrag vom Vortag */
		public long initialStartWaitingTime;
		/** Rest-Wartezeittoleranzübertrag vom Vortag */
		public long initialRestWaitingTolerance;

		/** Wartezeitübertrag von vorherigen Gesprächen (vor einer Weiterleitung) **/
		public int callerWaitingTime;
		/** Verweilzeitübertrag von vorherigen Gesprächen (vor einer Weiterleitung) **/
		public int callerStayingTime;

		private final void reinit() {
			retryCount=0;
			callContinued=false;
			callCancelEvent=null;
			reCheckEvents=null;
			agentAssignedStartEvent=null;
			callerWaitingTime=0;
			callerStayingTime=0;
			initialStartWaitingTime=0;
			initialRestWaitingTolerance=0;
		}
	}

	/**
	 * Status eines Agenten: Dienst hat noch nicht begonnen
	 * @see RunData.AgentRecord#status
	 */
	public static final byte AGENT_VOR_DIENST=0;

	/**
	 * Status eines Agenten: Im Dienst und im Leerlauf
	 * @see RunData.AgentRecord#status
	 */
	public static final byte AGENT_LEERLAUF=1;

	/**
	 * Status eines Agenten: Im Dienst, in technischer Bereitzeit
	 * @see RunData.AgentRecord#status
	 */
	public static final byte AGENT_TECHNISCHER_LEERLAUF=2;

	/**
	 * Status eines Agenten: Im Dienst, bedient Kunden
	 * @see RunData.AgentRecord#status
	 */
	public static final byte AGENT_BEDIENUNG=3;

	/**
	 * Status eines Agenten: Im Dienst, in Nachbearbeitungszeit
	 * @see RunData.AgentRecord#status
	 */
	public static final byte AGENT_NACHBEARBEITUNG=4;

	/**
	 * Status eines Agenten: Dienst beendet
	 * @see RunData.AgentRecord#status
	 */
	public static final byte AGENT_NACH_DIENST=5;

	private final double[] tempData=new double[48];
	private final int[] tempDataIntervals=new int[2];

	/**
	 * Laufzeitdaten eines Agenten
	 * @author Alexander Herzog
	 * @see RunData#freeAgents
	 */
	public final class AgentRecord {
		/** Status des Agenten */
		public byte status=AGENT_VOR_DIENST;
		/** Typ des Kunden der gerade durch diesen Agent bedient wird */
		public CallcenterRunModelCaller callerType;
		/** Zeitpunkt der letzten Statusänderung des Agenten (für die Statistikerfassung) */
		public long lastStatusChange;
		/** Zugehöriges statisches Modell-Agenten-Objekt */
		public CallcenterRunModelAgent agent;
		/** Skill-Level des Agenten */
		public CallcenterRunModelSkillLevel skillLevel;
		/** Callcenter in dem der Agent arbeitet */
		public CallcenterRunModelCallcenter callcenter;
		/** Globales Agentenstatistikobjekt */
		public Statistics.AgentenDaten statisticGlobal;
		/** Callcenter-spezifisches Agentenstatistikobjekt */
		public Statistics.AgentenDaten statisticProCallcenter;
		/** Skill-Level-spezifisches Agentenstatistikobjekt */
		public Statistics.AgentenDaten statisticProSkilllevel;

		private int leerlaufGesamt; /* Daten pro Tag, daher sollte ein Int reichen */
		private int technischerLeerlaufGesamt;
		private int arbeitGesamt;
		private int postProcessingGesamt;
		private short anzahlAnrufeGesamt;

		private final int[] dataByCallerTechnial; /* Daten pro Tag, daher sollte ein Int reichen */
		private final int[] dataByCallerService;
		private final int[] dataByCallerPostProcessing;

		/**
		 * Konstruktor der Klasse
		 * @param caller	Liste aller Kundentypen (um kundentyp-indizierte Zähler-Arrays zu initialisieren)
		 */
		public AgentRecord(final CallcenterRunModelCaller[] caller) {
			dataByCallerTechnial=new int[caller.length];
			dataByCallerService=new int[caller.length];
			dataByCallerPostProcessing=new int[caller.length];
		}

		/**
		 * Reinitialisiert den Agentendatensatz.
		 */
		public void reinit() {
			status=AGENT_VOR_DIENST;
			leerlaufGesamt=0;
			technischerLeerlaufGesamt=0;
			arbeitGesamt=0;
			postProcessingGesamt=0;
			anzahlAnrufeGesamt=0;
			Arrays.fill(dataByCallerTechnial,0);
			Arrays.fill(dataByCallerService,0);
			Arrays.fill(dataByCallerPostProcessing,0);
			lastStatusChange=0;
		}

		/**
		 * Muss zum Ende eines Simulationstages aufgerufen werden.
		 * @param now	Zeitpunkt
		 * @param agentCostsUsed	Werden in dem Modell agentenspezifische Kosten verwendet, siehe {@link CallcenterRunModel#agentCostsUsed}
		 * @see SimulationData#terminateCleanUp(long)
		 */
		public void doneDay(final long now, final boolean agentCostsUsed) {
			if (status!=AGENT_NACH_DIENST) logStatusChange(Math.min(now,86400*1000-1),status,AGENT_NACH_DIENST,null);

			statisticGlobal.leerlaufGesamt+=leerlaufGesamt;
			statisticProCallcenter.leerlaufGesamt+=leerlaufGesamt;
			statisticProSkilllevel.leerlaufGesamt+=leerlaufGesamt;
			statisticGlobal.technischerLeerlaufGesamt+=technischerLeerlaufGesamt;
			statisticProCallcenter.technischerLeerlaufGesamt+=technischerLeerlaufGesamt;
			statisticProSkilllevel.technischerLeerlaufGesamt+=technischerLeerlaufGesamt;
			statisticGlobal.arbeitGesamt+=arbeitGesamt;
			statisticProCallcenter.arbeitGesamt+=arbeitGesamt;
			statisticProSkilllevel.arbeitGesamt+=arbeitGesamt;
			statisticGlobal.postProcessingGesamt+=postProcessingGesamt;
			statisticProCallcenter.postProcessingGesamt+=postProcessingGesamt;
			statisticProSkilllevel.postProcessingGesamt+=postProcessingGesamt;
			statisticGlobal.anzahlAnrufeGesamt+=anzahlAnrufeGesamt;
			statisticProCallcenter.anzahlAnrufeGesamt+=anzahlAnrufeGesamt;
			statisticProSkilllevel.anzahlAnrufeGesamt+=anzahlAnrufeGesamt;

			for (int i=0;i<dataByCallerTechnial.length;i++) {
				long l;

				l=dataByCallerTechnial[i];
				if (l>0) {
					statisticGlobal.dataByCallerTechnial[i]+=l;
					statisticProCallcenter.dataByCallerTechnial[i]+=l;
					statisticProSkilllevel.dataByCallerTechnial[i]+=l;
				}

				l=dataByCallerService[i];
				if (l>0) {
					statisticGlobal.dataByCallerService[i]+=l;
					statisticProCallcenter.dataByCallerService[i]+=l;
					statisticProSkilllevel.dataByCallerService[i]+=l;
				}

				l=dataByCallerPostProcessing[i];
				if (l>0) {
					statisticGlobal.dataByCallerPostProcessing[i]+=l;
					statisticProCallcenter.dataByCallerPostProcessing[i]+=l;
					statisticProSkilllevel.dataByCallerPostProcessing[i]+=l;
				}
			}

			if (agentCostsUsed) {
				/* Stundenlohn */
				if (agent.costPerWorkingHour>0) {
					final double costOfficeTime=((double)leerlaufGesamt+technischerLeerlaufGesamt+arbeitGesamt+postProcessingGesamt)/1000/3600*agent.costPerWorkingHour;
					statisticGlobal.costOfficeTime+=costOfficeTime;
					statisticProCallcenter.costOfficeTime+=costOfficeTime;
					statisticProSkilllevel.costOfficeTime+=costOfficeTime;
				}

				/* Kosten pro Gesprächsminute */
				for (int i=0;i<dataByCallerTechnial.length;i++) {
					double d=(agent.costPerCallMinute==null)?0.0:agent.costPerCallMinute[i]; if (d==0.0) continue;
					final double d2=((double)(dataByCallerTechnial[i]+dataByCallerService[i]+dataByCallerPostProcessing[i]))/1000/60; if (d2==0) continue;
					d=d*d2;
					statisticGlobal.costProcessTime+=d;
					statisticProCallcenter.costProcessTime+=d;
					statisticProSkilllevel.costProcessTime+=d;
				}
			}
		}

		private final void logClientSpecificData(final long time1, final long time2, final int[] data, final DataDistributionImpl[] dataProIntervall1a, final DataDistributionImpl[] dataProIntervall1b, final DataDistributionImpl[] dataProIntervall1c, final DataDistributionImpl dataProIntervall2a, final DataDistributionImpl dataProIntervall2b, final DataDistributionImpl dataProIntervall2c, boolean isService) {
			final int i=callerType.index;
			if (i<0) {
				assert(i>=0);
				return;
			}

			if (isService) {
				/* Kosten pro Anruf */
				final double d=(agent.costPerCall==null)?0.0:agent.costPerCall[i];
				if (d>0) {
					statisticGlobal.costCalls+=d;
					statisticProCallcenter.costCalls+=d;
					statisticProSkilllevel.costCalls+=d;
				}
			}

			final long delta=time2-time1;
			if (delta>0) {
				data[i]+=delta;
				final double[] dist1a=dataProIntervall1a[i].densityData;
				final double[] dist1b=dataProIntervall1b[i].densityData;
				final double[] dist1c=dataProIntervall1c[i].densityData;
				final double[] dist2a=dataProIntervall2a.densityData;
				final double[] dist2b=dataProIntervall2b.densityData;
				final double[] dist2c=dataProIntervall2c.densityData;

				boolean b=getIntervalPartsWithoutDiv(tempData,tempDataIntervals,(int)time1,(int)time2);
				if (b) for (int j=tempDataIntervals[0];j<=tempDataIntervals[1];j++) if (tempData[j]!=0) {
					final double d=tempData[j];
					dist1a[j]+=d;
					dist1b[j]+=d;
					dist1c[j]+=d;
					dist2a[j]+=d;
					dist2b[j]+=d;
					dist2c[j]+=d;
				}
			}
		}

		/**
		 * Erfasst eine Statusänderung für den Agenten
		 * @param time	Zeitpunkt
		 * @param statusOld	Alter Status
		 * @param statusNew	Neuer Status
		 * @param caller	Zugehöriger Kunde (wird verwendet, wenn der neue Status {@link RunData#AGENT_TECHNISCHER_LEERLAUF} oder {@link RunData#AGENT_BEDIENUNG} ist
		 */
		public final void logStatusChange(long time, final int statusOld, final int statusNew, final CallerRecord caller) {
			if (statusNew==AGENT_TECHNISCHER_LEERLAUF || statusNew==AGENT_BEDIENUNG) callerType=caller.callerType;

			switch (statusOld) {
			case AGENT_VOR_DIENST:
				workingAgentsCount++;
				/* nichts zu erfassen */
				break;
			case AGENT_LEERLAUF:
				if (time>86400*1000-1) time=86400*1000-1; /* Bei Agenten mit festen Arbeitszeiten und Open-End potentiellen Leerlauf nur bis Mitternacht zählen. */
				final long delta=time-lastStatusChange;
				if (delta>0) {
					leerlaufGesamt+=delta;
					final boolean b=getIntervalPartsWithoutDiv(tempData,tempDataIntervals,(int)lastStatusChange,(int)time);
					final double[] distA=statisticGlobal.leerlaufProIntervall.densityData;
					final double[] distB=statisticProCallcenter.leerlaufProIntervall.densityData;
					final double[] distC=statisticProSkilllevel.leerlaufProIntervall.densityData;
					if (b) for (int j=tempDataIntervals[0];j<=tempDataIntervals[1];j++) {
						distA[j]+=tempData[j];
						distB[j]+=tempData[j];
						distC[j]+=tempData[j];
					}
				}
				break;
			case AGENT_TECHNISCHER_LEERLAUF:
				technischerLeerlaufGesamt+=(time-lastStatusChange);
				logClientSpecificData(
						lastStatusChange,
						time,
						dataByCallerTechnial,
						statisticGlobal.dataByCallerTechnialProIntervall,
						statisticProCallcenter.dataByCallerTechnialProIntervall,
						statisticProSkilllevel.dataByCallerTechnialProIntervall,
						statisticGlobal.technischerLeerlaufProIntervall,
						statisticProCallcenter.technischerLeerlaufProIntervall,
						statisticProSkilllevel.technischerLeerlaufProIntervall,
						false);
				break;
			case AGENT_BEDIENUNG:
				final int interval=(int)Math.max(0,Math.min(47,time/1000/1800));
				arbeitGesamt+=(time-lastStatusChange);
				anzahlAnrufeGesamt++;
				statisticGlobal.anzahlAnrufeProIntervall.densityData[interval]++;
				statisticProCallcenter.anzahlAnrufeProIntervall.densityData[interval]++;
				statisticProSkilllevel.anzahlAnrufeProIntervall.densityData[interval]++;
				final int i=callerType.index;
				statisticGlobal.dataByCallerAnzahlAnrufe[i]++;
				statisticProCallcenter.dataByCallerAnzahlAnrufe[i]++;
				statisticProSkilllevel.dataByCallerAnzahlAnrufe[i]++;

				statisticGlobal.dataByCallerAnzahlAnrufeProIntervall[i].densityData[interval]++;
				statisticProCallcenter.dataByCallerAnzahlAnrufeProIntervall[i].densityData[interval]++;
				statisticProSkilllevel.dataByCallerAnzahlAnrufeProIntervall[i].densityData[interval]++;
				logClientSpecificData(
						lastStatusChange,
						time,
						dataByCallerService,
						statisticGlobal.dataByCallerServiceProIntervall,
						statisticProCallcenter.dataByCallerServiceProIntervall,
						statisticProSkilllevel.dataByCallerServiceProIntervall,
						statisticGlobal.arbeitProIntervall,
						statisticProCallcenter.arbeitProIntervall,
						statisticProSkilllevel.arbeitProIntervall,
						true);
				break;
			case AGENT_NACHBEARBEITUNG:
				postProcessingGesamt+=(time-lastStatusChange);
				logClientSpecificData(
						lastStatusChange,
						time,
						dataByCallerPostProcessing,
						statisticGlobal.dataByCallerPostProcessingProIntervall,
						statisticProCallcenter.dataByCallerPostProcessingProIntervall,
						statisticProSkilllevel.dataByCallerPostProcessingProIntervall,
						statisticGlobal.postProcessingProIntervall,
						statisticProCallcenter.postProcessingProIntervall,
						statisticProSkilllevel.postProcessingProIntervall,
						false);
				break;
			}
			lastStatusChange=time;
			if (statusNew==AGENT_NACH_DIENST) workingAgentsCount--;
		}

		private final double getFreeTimePart() {
			final long free=leerlaufGesamt+technischerLeerlaufGesamt;
			if (free==0) return 0.0;
			final long work=arbeitGesamt+postProcessingGesamt;
			final long sum=free+work;
			return (sum>0)?(((double)free)/sum):0.0;
		}

		private final long getFreeTimeSinceLastCall(final long now) {
			if (status!=AGENT_LEERLAUF) return 0; else return now-lastStatusChange;
		}
	}
}