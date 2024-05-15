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

import java.awt.Window;
import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import net.calc.StartAnySimulator;
import simulator.CallcenterSimulatorInterface;
import simulator.Statistics;
import simulator.Statistics.AgentenDaten;
import simulator.Statistics.KundenDaten;
import systemtools.MsgBox;
import tools.SetupData;
import ui.connected.ConnectedSimulation;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;
import ui.model.CallcenterRunModel;

/**
 * Optimierer-Kernsystem
 * @author Alexander Herzog
 * @version 1.0
 */
public final class Optimizer {
	/** Initiales {@link CallcenterModel}, das die Basis der Optimierungsläufe darstellt */
	private final CallcenterModel initialEditModel;
	/** Objekt vom Typ {@link OptimizeSetup}, welches Informationen darüber enthält, was zu optimieren ist */
	private final OptimizeSetup setup;

	/** Übergeordnetes Fenster */
	private final Window owner;
	/** Ausgabestream */
	private final PrintStream out;

	/** Wie viele Zwischenschritte sollen bei der Statistik-Speicherung übersprungen werden (1=keine überspringen) */
	private int memorySavingLevel=1;
	/** Letztes abgeschlossenes Intervall in Bezug auf Veränderungen in die erste Richtung */
	private int lastOKInterval=-1;
	/** Letztes abgeschlossenes Intervall in Bezug auf Veränderungen in die zweite Richtung */
	private int lastOKInterval2=-1;
	/** Handelt es sich um den letzten Lauf? */
	private boolean thisIsLastRun=false;
	/** Wurde die Optimierung abgebrochen? ({@link #isCanceled()}) */
	private boolean canceled=false;

	/** Gesamtzahl der Simulationsläufe */
	private int runNr=0;
	/** Anzahl an Intervallen */
	private final int steps;
	/** Aktuelle Anzahl an Agenten pro Intervall */
	private DataDistributionImpl agents;
	/** Veränderung der Anzahl an Agenten im letzten Optimierungsschritt */
	private DataDistributionImpl agentsChangedLast;
	/** Veränderung der Anzahl an Agenten seit Start der Optimierung */
	private DataDistributionImpl agentsChanged;
	/** Notwendige Veränderung pro Intervall */
	private final DataDistributionImpl intervalNeedsChange;
	/** Grenzen für die Veränderungen pro Intervall */
	private final Map<String,DataDistributionImpl> intervalChangeAllowed;
	/** Prozentuale Veränderung pro Intervall */
	private final DataDistributionImpl intervalPercent;
	/** Absolute Veränderung pro Intervall */
	private final DataDistributionImpl intervalAbsoluteAdd;

	/** Startzeitpunkt der ersten Simulation */
	private long startTime;
	/** Simulator-Objekt über das die einzelnen Simulationen der Optimierung ausgeführt werden */
	private CallcenterSimulatorInterface simulator;

	/** Ergebnisse aus dem vorherigen Optimierungsschritt */
	private DataDistributionImpl resultLast=null;
	/** Ergebnisse aus dem zuletzt abgeschlossenen Optimierungsschritt */
	private DataDistributionImpl resultCurrent=null;

	/**
	 * Ergebnisse der Optimierung
	 * @see #getResults()
	 */
	private OptimizeData results;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param out	Ausgabestream
	 * @param initialEditModel	Initiales {@link CallcenterModel}, das die Basis der Optimierungsläufe darstellt.
	 * @param setup	Objekt vom Typ {@link OptimizeSetup}, welches Informationen darüber enthält, was zu optimieren ist.
	 */
	public Optimizer(Window owner, PrintStream out, CallcenterModel initialEditModel, OptimizeSetup setup) {
		this.initialEditModel=initialEditModel;
		this.setup=setup;
		this.owner=owner;
		this.out=out;

		int s=24;
		for (CallcenterModelCallcenter callcenter : initialEditModel.callcenter) if (callcenter.active) {
			for (CallcenterModelAgent agents : callcenter.agents) if (agents.active) {
				if (agents.count==-2 && s==24) s=48;
				if (agents.count==-1) {
					if (agents.countPerInterval48!=null && s==24) s=48;
					if (agents.countPerInterval96!=null && s<96) s=96;
				}
			}
		}
		steps=Math.max(s,48);

		for (CallcenterModelCallcenter callcenter : initialEditModel.callcenter) if (callcenter.active) {
			for (CallcenterModelAgent agents : callcenter.agents) if (agents.active && agents.count==-1) agents.stretchCountPerInterval(steps);
		}

		agents=new DataDistributionImpl(steps,steps);
		agentsChangedLast=new DataDistributionImpl(steps,steps);
		agentsChanged=new DataDistributionImpl(steps,steps);
		intervalNeedsChange=new DataDistributionImpl(steps,steps);
		intervalChangeAllowed=new HashMap<>();
		intervalPercent=new DataDistributionImpl(steps,steps);
		intervalAbsoluteAdd=new DataDistributionImpl(steps,steps);
	}

	/**
	 * Prüft die Liste der anzupassenden Agentengruppen.
	 * @param names	Liste der anzupassenden Agentengruppen
	 * @return	Liefert <code>true</code>, wenn die Liste der anzupassenden Agentengruppen gültig ist.
	 */
	private boolean checkAgentGroups(final String[] names) {
		String[] callcenterNames;
		int[] groupNumbers;
		Object[] obj=OptimizeSetup.splitCallcenterAgentGroupData(names);
		callcenterNames=(String[])obj[0];
		groupNumbers=(int[])obj[1];

		int count=0;
		for (int i=0;i<initialEditModel.callcenter.size();i++) {
			String s=initialEditModel.callcenter.get(i).name;
			int groupCount=initialEditModel.callcenter.get(i).agents.size();
			for (int j=0;j<callcenterNames.length;j++) if (callcenterNames[j].equalsIgnoreCase(s) && groupNumbers[j]<=groupCount) count++;
		}
		return count>0;
	}

	/**
	 * Prüft die Liste der auszuwertenden Anrufergruppen.
	 * @param names	Liste der auszuwertenden Anrufergruppen
	 * @return	Liefert <code>true</code>, wenn die Liste der auszuwertenden Anrufergruppen gültig ist.
	 */
	private boolean checkCallerGroups(final String[] names) {
		int count=0;
		for (int i=0;i<initialEditModel.caller.size();i++) {
			String s=initialEditModel.caller.get(i).name;
			for (int j=0;j<names.length;j++) if (names[j].equalsIgnoreCase(s)) {count++; break;}
		}
		return count>0;
	}

	/**
	 * Sind in mindestens einem Callcenter des Modells Einstellungen zur minimalen Schichtlänge der Agenten aktiv?
	 * @return	Liefert <code>true</code>, wenn Einstellungen zur minimalen Schichtlänge der Agenten aktiv sind
	 */
	private boolean minimumShiftLengthsActive() {
		if (initialEditModel.minimumShiftLength>1) return true;
		for (CallcenterModelCallcenter callcenter: initialEditModel.callcenter) if (callcenter.active) for (CallcenterModelAgent agents: callcenter.agents) if (agents.active && agents.count<0 && agents.minimumShiftLength>1) return true;
		return false;
	}

	/**
	 * Prüft Modell und Konfiguration vor dem Start der Optimierung und bereitet die Daten für die Optimierung vor.
	 * @return	Gibt <code>null</code> zurück, wenn alles fehlerfrei ist, sonst wird ein Fehlermeldungs-String zurückgegeben.
	 */
	public String checkAndInit() {

		/* Mindestens eine Gruppe (Anrufer oder Agenten), über die optimiert wird? */
		if (setup.optimizeGroups==OptimizeSetup.OptimizeGroups.OPTIMIZE_GROUPS_SELECTION) {
			if (setup.optimizeProperty==OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_WORK_LOAD) {
				if (!checkAgentGroups(setup.optimizeGroupNames)) return Language.tr("Optimization.Error.NoAgentsGroupForTargetSelected");
			} else {
				if (!checkCallerGroups(setup.optimizeGroupNames)) return Language.tr("Optimization.Error.NoClientTypeForTargetSelected");
			}
		}

		/* Mindestens eine Agentengruppe, die angepasst wird? */
		if (!setup.changeAll) {
			if (!checkAgentGroups(setup.changeGroups)) return Language.tr("Optimization.Error.NoAgentsGroupForChangesSelected");
		}

		if (setup.optimizeMaxValue>=0) {
			double diff=setup.optimizeMaxValue-setup.optimizeValue;
			if (
					setup.optimizeProperty==OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_SUCCESS_BY_CALL ||
					setup.optimizeProperty==OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_SUCCESS_BY_CLIENT ||
					setup.optimizeProperty==OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL ||
					setup.optimizeProperty==OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL_ALL ||
					setup.optimizeProperty==OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT ||
					setup.optimizeProperty==OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT_ALL
					) {
				/* Verbesserung=größerer Wert */
				if (diff<0) return Language.tr("Optimization.Error.MaximumSmallerThanMinimum");
			} else {
				/* Verbesserung=kleinerer Wert */
				if (diff>0) return Language.tr("Optimization.Error.MinimumLargerThanMaximum");
			}
		}

		/* Keine Agentengruppen mit festen Arbeitszeiten, wenn optimizeMaxValue gesetzt ist. */
		if (setup.optimizeMaxValue>=0) {
			for (int i=0;i<initialEditModel.callcenter.size();i++) for (int j=0;j<initialEditModel.callcenter.get(i).agents.size();j++)
				if (initialEditModel.callcenter.get(i).agents.get(j).count>=0) return String.format(Language.tr("Optimizer.Error.CannotOptimizeDownOnFixedWorkingTimes"),j+1,initialEditModel.callcenter.get(i).name);
		}

		/* Mindestens ein Intervall gewählt? */
		if (setup.optimizeIntervals.getMax()<0.1) return Language.tr("Optimizer.Error.NoIntervalSelected");

		/* Einschränkungen in Bezug auf die Agentengruppen nur bei intervallweiser Optimierung */
		if (setup.groupRestrictionName.size()>0 && setup.optimizeByInterval==OptimizeSetup.OptimizeInterval.OPTIMIZE_BY_INTERVAL_NO) return Language.tr("Optimizer.Error.CannotOptimizeWithRestrictionsInAverage");

		/* Einschränkungen in Bezug auf die Agentengruppen korrekt? */
		String[] callcenterNames;
		int[] groupNumbers;
		Object[] obj=OptimizeSetup.splitCallcenterAgentGroupData(setup.groupRestrictionName.toArray(String[]::new));
		callcenterNames=(String[])obj[0];
		groupNumbers=(int[])obj[1];
		for (int i=0;i<callcenterNames.length;i++) {
			boolean ok=false;
			for (CallcenterModelCallcenter callcenter :	initialEditModel.callcenter) if (callcenter.name.equals(callcenterNames[i])) {
				if (groupNumbers[i]<1 || groupNumbers[i]>callcenter.agents.size()) return String.format(Language.tr("Optimizer.Error.Restrictions.NoInvalidGroupNumber"),callcenterNames[i]);
				CallcenterModelAgent agents=callcenter.agents.get(groupNumbers[i]-1);
				if (agents.active) {
					if (agents.count>=0) return String.format(Language.tr("Optimizer.Error.Restrictions.FixedNumberOfAgents"),callcenterNames[i]);
				}
				ok=true;
				break;
			}
			if (!ok) return String.format(Language.tr("Optimizer.Error.Restrictions.NoCallcenterName"),callcenterNames[i]);

			DataDistributionImpl min=setup.groupRestrictionMin.get(i);
			DataDistributionImpl max=setup.groupRestrictionMax.get(i);
			for (int j=0;j<min.densityData.length;j++) if (min.densityData[j]>max.densityData[j]) return String.format(Language.tr("Optimizer.Error.Restrictions.EmptyRange"),callcenterNames[i]);
		}

		/* Keine Optimierung nach unten, wenn Mindest-Schichtlängen definiert sind */
		if (minimumShiftLengthsActive() && setup.optimizeMaxValue>=0) return Language.tr("Optimizer.Error.NoMaximumValueIfMinimumShiftLengthsAreDefined");

		/* Modell ok? */
		CallcenterRunModel runModel=new CallcenterRunModel(initialEditModel);
		String s=runModel.checkAndInit(false,false,SetupData.getSetup().strictCheck);
		if (s!=null) return Language.tr("Optimizer.Error.Preparation")+":\n"+s;

		/* Am Anfang alle Agenten auf 100% der Ausgangswerte */
		intervalNeedsChange.setToValue(-1);
		intervalPercent.setToValue(1);

		results=new OptimizeData(setup.cloneOptimizeSetup());

		return null;
	}

	/**
	 * Wurden Intervalle für die Optimierung ausgewählt?
	 * @return	Liefert <code>true</code>, wenn Intervalle für die Optimierung ausgewählt wurden
	 * @see OptimizeSetup#optimizeIntervals
	 */
	private boolean allIntervals() {
		return (setup.optimizeIntervals.getMin()>0.1);
	}

	/**
	 * Liefert den gültigen Bereich pro Intervall für eine Agentengruppe
	 * @param agentsGroupName	Agentengruppe
	 * @return	Array aus den Verteilungen der minimalen und der maximalen Werte pro Intervall
	 */
	private DataDistributionImpl[] getRestrictions(final String agentsGroupName) {
		int index=setup.groupRestrictionName.indexOf(agentsGroupName);
		DataDistributionImpl min, max;
		if (index<0) {
			min=new DataDistributionImpl(86399,48); min.setToValue(0);
			max=new DataDistributionImpl(86399,48); max.setToValue(1000000);
		} else {
			min=setup.groupRestrictionMin.get(index).round();
			max=setup.groupRestrictionMax.get(index).round();
		}

		if (steps==96) {
			DataDistributionImpl min96=new DataDistributionImpl(86399,96);
			DataDistributionImpl max96=new DataDistributionImpl(86399,96);
			for (int i=0;i<48;i++) {
				min96.densityData[2*i]=min.densityData[i];
				min96.densityData[2*i+1]=min.densityData[i];
				max96.densityData[2*i]=max.densityData[i];
				max96.densityData[2*i+1]=max.densityData[i];
			}
			return new DataDistributionImpl[]{min96,max96};
		}

		return new DataDistributionImpl[]{min,max};
	}

	/**
	 * Dürfen in einem Intervall Veränderungen vorgenommen werden?
	 * @param interval	Intervall
	 * @return	Liefert <code>true</code>, wenn in dem Intervall Veränderungen vorgenommen werden dürfen
	 */
	private boolean intervalChangeAllowed(final int interval) {
		for (CallcenterModelCallcenter callcenter :	initialEditModel.callcenter) for (int i=0;i<callcenter.agents.size();i++) {
			DataDistributionImpl changeAllowed=intervalChangeAllowed.get(""+(i+1)+"-"+callcenter.name);
			if (changeAllowed==null) return true;
			if (changeAllowed.densityData[interval]>0) return true;
		}
		return false;
	}

	/**
	 * Ändert die Anzahl an Agenten in einer Gruppe.
	 * @param agent	Agentengruppe
	 * @param firstRun	Erster Optimierungslauf?
	 * @param agentsGroupName	Name der Agentengruppe
	 */
	private void changeAgents(final CallcenterModelAgent agent, final boolean firstRun, final String agentsGroupName) {
		if (agent.count==-2) {
			if ((setup.optimizeByInterval==OptimizeSetup.OptimizeInterval.OPTIMIZE_BY_INTERVAL_NO && allIntervals()) || firstRun) {
				/* Gesamte Verteilung hochsetzen */
				agents=agents.add(agent.calcAgentDistributionFromCallers(initialEditModel.caller)); /* wird durch "add" ggf. automatisch hochskaliert */
				if (!firstRun) {
					agent.byCallersAvailableHalfhours=(int)Math.round(agent.byCallersAvailableHalfhours*intervalPercent.densityData[0]);
					agent.byCallersAvailableHalfhours+=intervalAbsoluteAdd.densityData[0];
					agent.byCallersAvailableHalfhours=Math.max(agent.byCallersAvailableHalfhours,0);
				}
				agentsChanged=agentsChanged.add(agent.calcAgentDistributionFromCallers(initialEditModel.caller));
				return;
			}
			/* Agentengruppe in count=-1 Typ umwandeln */
			agent.setCountPerInterval(agent.calcAgentDistributionFromCallers(initialEditModel.caller));
			if (steps==96) agent.stretchCountPerInterval(96);
			agent.count=-1;
		}

		if (agent.count==-1) {
			/* Agenten pro Halbstundenintervall */
			agents=agents.add(agent.getCountPerInterval());
			DataDistributionImpl[] restrictions=getRestrictions(agentsGroupName);
			DataDistributionImpl dist=agent.getCountPerInterval();
			if (firstRun) {
				dist=dist.max(restrictions[0]).min(restrictions[1]);
			} else {
				dist=dist.multiply(intervalPercent).round().add(intervalAbsoluteAdd).max(0);
				for (int i=0;i<steps;i++) {
					if (dist.densityData[i]<restrictions[0].densityData[i]) {
						dist.densityData[i]=restrictions[0].densityData[i];
						DataDistributionImpl changeAllowed=intervalChangeAllowed.get(agentsGroupName);
						if (changeAllowed==null) {changeAllowed=new DataDistributionImpl(86399,steps); changeAllowed.setToValue(1); intervalChangeAllowed.put(agentsGroupName,changeAllowed);}
						changeAllowed.densityData[i]=0;
					}
					if (dist.densityData[i]>restrictions[1].densityData[i]) {
						dist.densityData[i]=restrictions[1].densityData[i];
						DataDistributionImpl changeAllowed=intervalChangeAllowed.get(agentsGroupName);
						if (changeAllowed==null) {changeAllowed=new DataDistributionImpl(86399,steps); changeAllowed.setToValue(1); intervalChangeAllowed.put(agentsGroupName,changeAllowed);}
						changeAllowed.densityData[i]=0;
					}
				}
			}
			agent.setCountPerInterval(dist);
			agentsChanged=agentsChanged.add(dist);
		} else {
			/* Agenten mit festen Arbeitszeiten */
			DataDistributionImpl temp;
			int start=(int)Math.min(47,Math.max(0,Math.round((double)agent.workingTimeStart/1800)));
			int end=agent.workingNoEndTime?47:(int)Math.min(47,Math.max(0,Math.round((double)agent.workingTimeEnd/1800)));
			temp=new DataDistributionImpl(48,48);
			for (int i=start;i<=end;i++) temp.densityData[i]=agent.count;
			temp.stretchToValueCount(steps);
			agents=agents.add(temp);
			if (!firstRun) {
				temp=new DataDistributionImpl(48,48);
				agent.count=(int)Math.round(agent.count*(1+setup.changeValue*runNr));
				for (int i=start;i<=end;i++) temp.densityData[i]=agent.count;
			}
			agentsChanged=agentsChanged.add(temp);
		}
	}

	/**
	 * Verändert das Modell.
	 * @param changeModelDirection	Richtung in die die Agentengruppe verändert werden soll.
	 * @return	Neues Modell
	 */
	private CallcenterModel changeModel(final int changeModelDirection) {
		CallcenterModel model=null;

		int changeSignum=(changeModelDirection>=0)?1:-1;

		boolean agentsNumberChanged=false;
		boolean firstRun=(runNr==1);
		agentsChangedLast=agentsChanged.clone();
		int changeIteration=0;

		DataDistributionImpl intervalPercentSave=intervalPercent.clone();

		while (!agentsNumberChanged) {
			changeIteration++;

			model=initialEditModel.clone();
			model.name=model.name+" ("+Language.tr("Optimizer.SimulationRun")+" "+runNr+")";
			agents.clearDensityData();
			agentsChanged.clearDensityData();

			/* Prozentwerte für Intervalle anpassen */
			if (firstRun) {
				intervalPercent.setToValue(1);
				intervalAbsoluteAdd.setToValue(0);
			} else {
				for (int i=0;i<steps;i++) {
					if (intervalNeedsChange.densityData[i]>0) intervalPercent.densityData[i]+=changeSignum*setup.changeValue;
				}
			}

			/* Modell ändern */
			if (setup.changeAll) {
				/* Alle Agentengruppen anpassen */
				for (int i=0;i<model.callcenter.size();i++) {
					CallcenterModelCallcenter c=model.callcenter.get(i);
					for (int j=0;j<c.agents.size();j++) changeAgents(c.agents.get(j),firstRun,""+(j+1)+"-"+c.name);
				}
			} else {
				/* Nur ausgewählte Gruppen anpassen */
				String[] callcenterNames;
				int[] groupNumbers;
				Object[] obj=OptimizeSetup.splitCallcenterAgentGroupData(setup.changeGroups);
				callcenterNames=(String[])obj[0];
				groupNumbers=(int[])obj[1];
				for (int i=0;i<model.callcenter.size();i++) {
					CallcenterModelCallcenter c=model.callcenter.get(i);
					for (int j=0;j<callcenterNames.length;j++) if (callcenterNames[j].equalsIgnoreCase(c.name)) {
						if (groupNumbers[j]-1>=c.agents.size()) continue;
						changeAgents(c.agents.get(groupNumbers[j]-1),firstRun,""+groupNumbers[j]+"-"+c.name);
					}
				}
			}

			/* Im ersten Lauf muss sich nichts geändert haben. */
			if (firstRun) break;

			/* Hat sich überhaupt etwas geändert? Wenn nein, noch eine Änder-Runde */
			for (int i=0;i<agents.densityData.length;i++) if (Math.abs(agentsChanged.densityData[i]-agentsChangedLast.densityData[i])>0.5) {agentsNumberChanged=true; break;}

			/* 20 Runden nichts passiert ? */
			if (changeIteration>=20) {
				/* dann Agenten als absolute Zahlenwerte hinzufügen */
				for (int i=0;i<steps;i++) {
					if (intervalNeedsChange.densityData[i]>0) intervalAbsoluteAdd.densityData[i]+=changeSignum*1;
				}
				changeIteration=0;
				System.arraycopy(intervalPercentSave.densityData,0,intervalPercent.densityData,0,steps);
			}

			boolean canChangeModel=false;
			for (int i=0;i<intervalNeedsChange.densityData.length;i++) if (intervalNeedsChange.densityData[i]>0 && intervalChangeAllowed(i)) {canChangeModel=true; break;}
			if (!canChangeModel) break;
		}

		StringBuilder s=new StringBuilder();
		s.append("\n\n"+Language.tr("Optimizer.ChangeOfNumberOfAgentsByOptimizer")+":\n");
		boolean showInfo=false;
		for (int i=0;i<steps;i++) {
			s.append(String.format("%s-%s: %s%%",TimeTools.formatShortTime(86400/steps*i),TimeTools.formatShortTime(86400/steps*(i+1)),NumberTools.formatNumber(intervalPercent.densityData[i]*100)));
			if (Math.abs(intervalAbsoluteAdd.densityData[i])>0.5) {
				showInfo=true;
				String t=(intervalAbsoluteAdd.densityData[i]>0)?"+":"";
				s.append(String.format(" %s%d",t,Math.round(intervalAbsoluteAdd.densityData[i])));
			}
			s.append('\n');
		}
		if (showInfo) {
			String info=Language.tr("Optimizer.InfoPlus");
			s.append("\n");
			while (!info.isEmpty()) {
				String next="";
				while (info.length()>=80) {int i=info.lastIndexOf(' '); next=info.substring(i)+next; info=info.substring(0,i);}
				s.append(info+"\n"); info=next.trim();
			}
		}

		if (setup.optimizeIntervals.getMin()<=0.1) {
			s.append("\n"+Language.tr("SimStatistic.OptimizeSetup.Intervals.List")+"\n");
			for (int i=0;i<results.setup.optimizeIntervals.densityData.length;i++) if (results.setup.optimizeIntervals.densityData[i]>0.1)
				s.append(TimeTools.formatShortTime(i*1800)+"-"+TimeTools.formatShortTime((i+1)*1800)+"\n");
		}

		if (model!=null) model.description=model.description+s.toString();

		return model;
	}

	/**
	 * Fügt einen Übertrag vom Vortag zu dem Callcenter-Modell hinzu.
	 * @param model	Callcenter-Modell
	 * @return	Liefert im Erfolgsfall ein {@link CallcenterRunModel}, sonst eine Fehlermeldung
	 */
	private Object addUebertrag(final CallcenterModel model) {
		Statistics statistic=null;

		if (setup.uebertragFile!=null && !setup.uebertragFile.isEmpty()) {
			File uebertragFileFile=new File(setup.uebertragFile);
			if (!uebertragFileFile.exists()) return String.format(Language.tr("Optimizer.Error.StatisticFilesDoesNotExist"),setup.uebertragFile);

			statistic=new Statistics(null,null,0,0);
			String s=statistic.loadFromFile(uebertragFileFile);
			if (s!=null) return String.format(Language.tr("Optimizer.Error.StatisticFileInvalid"),setup.uebertragFile)+": "+s;
		}

		Object o=ConnectedSimulation.buildConnectedRunModel(statistic,setup.uebertrag,setup.uebertragAdditionalCaller,setup.uebertragAdditionalCount,model);
		if (o instanceof String) return o;
		if (o instanceof CallcenterRunModel) return o;
		return Language.tr("Optimizer.Error.Internal");
	}

	/**
	 * Startet den nächsten Simulationslauf des Optimierungsprozesses
	 * @param	changeModelDirection	Gibt an, ob Agenten hinzugefügt werden sollen (&gt;0) oder entfernt werden sollen (&lt;0); bei ==0 erfolgt keine Veränderung
	 * @return	Gibt <code>null</code> zurück, wenn der Simulationslauf gestartet werden konnte, sonst wird ein Fehlermeldungs-String zurückgegeben.
	 */
	public String simulationStart(int changeModelDirection) {
		runNr++;
		results.runCount=runNr;

		if (runNr==1) startTime=System.currentTimeMillis();

		CallcenterModel editModel=changeModel(changeModelDirection);

		if (runNr>1) {
			DataDistributionImpl change=agentsChanged.sub(agentsChangedLast);
			boolean changed=false;
			for (int i=0;i<change.densityData.length;i++) if (Math.abs(change.densityData[i])>0.5) {changed=true; break;}
			if (!changed) {
				if (setup.optimizeByInterval==OptimizeSetup.OptimizeInterval.OPTIMIZE_BY_INTERVAL_NO) thisIsLastRun=true; else lastOKInterval2++;
			}
		}

		Object o=addUebertrag(editModel);
		if (o instanceof String) return (String)o;
		CallcenterRunModel runModel=null;
		if (o instanceof CallcenterRunModel) runModel=(CallcenterRunModel)o;
		if (runModel==null) return Language.tr("Optimizer.Error.Internal");

		String s=runModel.checkAndInit(false,false,SetupData.getSetup().strictCheck);
		if (s!=null) return Language.tr("Optimizer.Error.Preparation")+":\n"+s;

		StartAnySimulator startAnySimulator=new StartAnySimulator(editModel);
		s=startAnySimulator.check();
		if (s!=null) return s;
		simulator=startAnySimulator.run();
		simulator.start(false);

		return null;
	}

	/**
	 * Berechnet die Ergebniswerte für eine Kundengruppe.
	 * @param kunden	Kundengruppe
	 * @return	Array mit Wert, Verteilung (für die Gruppe), Gesamt-Verteilung
	 */
	private Object[] calcClientResultValues(final KundenDaten kunden) {
		DataDistributionImpl dist=null;
		DataDistributionImpl distAll=null;
		double value=0;

		switch (setup.optimizeProperty) {
		case OPTIMIZE_PROPERTY_SUCCESS_BY_CALL:
			if (kunden.anrufe-kunden.anrufeUebertrag>0) value=(double)kunden.anrufeErfolg/(kunden.anrufe-kunden.anrufeUebertrag);
			dist=kunden.anrufeErfolgProIntervall.divide(kunden.anrufeProIntervall);
			distAll=kunden.anrufeProIntervall;
			break;
		case OPTIMIZE_PROPERTY_SUCCESS_BY_CLIENT:
			if (kunden.kunden+kunden.kundenWiederanruf-kunden.kundenUebertrag>0) value=(double)kunden.kundenErfolg/(kunden.kunden+kunden.kundenWiederanruf-kunden.kundenUebertrag);
			dist=kunden.kundenErfolgProIntervall.divide(kunden.kundenProIntervall);
			distAll=kunden.kundenProIntervall;
			break;
		case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CALL:
			if (kunden.anrufeErfolg>0) value=(double)kunden.anrufeWartezeitSum/kunden.anrufeErfolg;
			dist=kunden.anrufeWartezeitSumProIntervall.divide(kunden.anrufeErfolgProIntervall);
			distAll=kunden.anrufeErfolgProIntervall;
			break;
		case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CLIENT:
			if (kunden.kundenErfolg>0) value=(double)kunden.kundenWartezeitSum/kunden.kundenErfolg;
			dist=kunden.kundenWartezeitSumProIntervall.divide(kunden.kundenErfolgProIntervall);
			distAll=kunden.kundenErfolgProIntervall;
			break;
		case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CALL:
			if (kunden.anrufeErfolg>0) value=(double)kunden.anrufeVerweilzeitSum/kunden.anrufeErfolg;
			dist=kunden.anrufeVerweilzeitSumProIntervall.divide(kunden.anrufeErfolgProIntervall);
			distAll=kunden.anrufeErfolgProIntervall;
			break;
		case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CLIENT:
			if (kunden.kundenErfolg>0) value=(double)kunden.kundenVerweilzeitSum/kunden.kundenErfolg;
			dist=kunden.kundenVerweilzeitSumProIntervall.divide(kunden.kundenErfolgProIntervall);
			distAll=kunden.kundenErfolgProIntervall;
			break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL:
			if (kunden.anrufeErfolg>0) value=(double)kunden.anrufeServicelevel/kunden.anrufeErfolg;
			dist=kunden.anrufeServicelevelProIntervall.divide(kunden.anrufeErfolgProIntervall);
			distAll=kunden.anrufeErfolgProIntervall;
			break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL_ALL:
			if (kunden.anrufe>0) value=(double)kunden.anrufeServicelevel/kunden.anrufe;
			dist=kunden.anrufeServicelevelProIntervall.divide(kunden.anrufeProIntervall);
			distAll=kunden.anrufeProIntervall;
			break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT:
			if (kunden.kundenErfolg>0) value=(double)kunden.kundenServicelevel/kunden.kundenErfolg;
			dist=kunden.kundenServicelevelProIntervall.divide(kunden.kundenErfolgProIntervall);
			distAll=kunden.kundenErfolgProIntervall;
			break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT_ALL:
			if (kunden.kunden>0) value=(double)kunden.kundenServicelevel/kunden.kunden;
			dist=kunden.kundenServicelevelProIntervall.divide(kunden.kundenProIntervall);
			distAll=kunden.kundenProIntervall;
			break;
		case OPTIMIZE_PROPERTY_WORK_LOAD:
			/* Tritt in dieser Methode nicht auf. Hier geht es nur um die Daten der Kunden. */
			break;
		default:
			break;
		}

		Object[] obj=new Object[3]; obj[0]=value; obj[1]=dist; obj[2]=distAll;
		return obj;
	}

	/**
	 * Berechnet die Ergebniswerte für eine Kundengruppe.
	 * @param statistics	Statistikdaten
	 * @return	Array mit Wert, Verteilung (für die Gruppe), Gesamt-Verteilung
	 */
	private Object[] calcClientResultValues(final Statistics statistics) {
		DataDistributionImpl dist=null;
		DataDistributionImpl distAll=null;
		double value=0;
		Object[] obj;

		boolean useMin=(
				setup.optimizeProperty==OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_SUCCESS_BY_CALL ||
				setup.optimizeProperty==OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_SUCCESS_BY_CLIENT ||
				setup.optimizeProperty==OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL ||
				setup.optimizeProperty==OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL_ALL ||
				setup.optimizeProperty==OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT ||
				setup.optimizeProperty==OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT_ALL
				);

		switch (setup.optimizeGroups) {
		case OPTIMIZE_GROUPS_AVERAGE:
			obj=calcClientResultValues(statistics.kundenGlobal);
			value=(Double)obj[0];
			dist=(DataDistributionImpl)obj[1];
			distAll=(DataDistributionImpl)obj[2];
			break;

		case OPTIMIZE_GROUPS_ALL:
			dist=new DataDistributionImpl(48,48);
			distAll=new DataDistributionImpl(48,48); distAll.setToValue(0);
			if (useMin) {value=1; dist.setToValue(1);} else {value=0; dist.setToValue(0);}
			for (int i=0;i<statistics.kundenProTyp.length;i++) {
				obj=calcClientResultValues(statistics.kundenProTyp[i]);
				double valueTemp=(Double)obj[0];
				DataDistributionImpl distTemp=(DataDistributionImpl)obj[1];
				DataDistributionImpl distAllTemp=(DataDistributionImpl)obj[2];
				if (useMin) {
					if (valueTemp>0) value=Math.min(value,valueTemp);
					for (int j=0;j<dist.densityData.length;j++) if (distAllTemp.densityData[j]>0) dist.densityData[j]=Math.min(dist.densityData[j],distTemp.densityData[j]);
				} else {
					value=Math.max(value,valueTemp);
					for (int j=0;j<dist.densityData.length;j++) if (distAllTemp.densityData[j]>0) dist.densityData[j]=Math.max(dist.densityData[j],distTemp.densityData[j]);
				}
				distAll=distAll.add(distAllTemp);
			}
			break;

		case OPTIMIZE_GROUPS_SELECTION:
			dist=new DataDistributionImpl(48,48);
			distAll=new DataDistributionImpl(48,48); distAll.setToValue(0);
			if (useMin) {value=1; dist.setToValue(1);} else {value=0; dist.setToValue(0);}
			for (int i=0;i<statistics.kundenProTyp.length;i++) {
				boolean ok=false;
				String s=statistics.kundenProTyp[i].name;
				for (int j=0;j<setup.optimizeGroupNames.length;j++) if (setup.optimizeGroupNames[j].equalsIgnoreCase(s)) {ok=true; break;}
				if (!ok) continue;
				obj=calcClientResultValues(statistics.kundenProTyp[i]);
				double valueTemp=(Double)obj[0];
				DataDistributionImpl distTemp=(DataDistributionImpl)obj[1];
				DataDistributionImpl distAllTemp=(DataDistributionImpl)obj[2];
				if (useMin) {
					if (valueTemp>0) value=Math.min(value,valueTemp);
					for (int j=0;j<dist.densityData.length;j++) if (distAllTemp.densityData[j]>0) dist.densityData[j]=Math.min(dist.densityData[j],distTemp.densityData[j]);
				} else {
					value=Math.max(value,valueTemp);
					for (int j=0;j<dist.densityData.length;j++) if (distAllTemp.densityData[j]>0) dist.densityData[j]=Math.max(dist.densityData[j],distTemp.densityData[j]);
				}
				distAll=distAll.add(distAllTemp);
			}
			break;
		}

		obj=new Object[3]; obj[0]=value; obj[1]=dist; obj[2]=distAll;
		return obj;
	}

	/**
	 * Berechnet die Ergebniswerte für eine Agentengruppe.
	 * @param agenten	Agentengruppe
	 * @return	Array mit Wert, Verteilung (für die Gruppe), Gesamt-Verteilung
	 */
	private Object[] calcAgentsResultValues(final AgentenDaten agenten) {
		DataDistributionImpl dist=null;
		DataDistributionImpl distAll=null;
		double value=0;

		long work=agenten.technischerLeerlaufGesamt+agenten.arbeitGesamt+agenten.postProcessingGesamt;
		long sum=agenten.leerlaufGesamt+work;
		value=(double)work/sum;

		DataDistributionImpl workDist=agenten.technischerLeerlaufProIntervall.add(agenten.arbeitProIntervall).add(agenten.postProcessingProIntervall);
		DataDistributionImpl sumDist=workDist.add(agenten.leerlaufProIntervall);
		dist=workDist.divide(sumDist);
		distAll=sumDist;

		Object[] obj=new Object[3]; obj[0]=value; obj[1]=dist; obj[2]=distAll;
		return obj;
	}

	/**
	 * Berechnet die Ergebniswerte für eine Agentengruppe.
	 * @param statistics	Statistikdaten
	 * @return	Array mit Wert, Verteilung (für die Gruppe), Gesamt-Verteilung
	 */
	private Object[] calcAgentResultValues(final Statistics statistics) {
		DataDistributionImpl dist=null;
		DataDistributionImpl distAll=null;
		double value=0;
		Object[] obj;

		switch (setup.optimizeGroups) {
		case OPTIMIZE_GROUPS_AVERAGE:
			obj=calcAgentsResultValues(statistics.agentenGlobal);
			value=(Double)obj[0];
			dist=(DataDistributionImpl)obj[1];
			distAll=(DataDistributionImpl)obj[2];
			break;

		case OPTIMIZE_GROUPS_ALL:
			dist=new DataDistributionImpl(48,48);
			value=0; dist.setToValue(0);
			for (int i=0;i<statistics.agentenProCallcenter.length;i++) {
				obj=calcAgentsResultValues(statistics.agentenProCallcenter[i]);
				double valueTemp=(Double)obj[0];
				DataDistributionImpl distTemp=(DataDistributionImpl)obj[1];
				DataDistributionImpl distAllTemp=(DataDistributionImpl)obj[2];
				value=Math.max(value,valueTemp);
				for (int j=0;j<dist.densityData.length;j++) if (distAllTemp.densityData[j]>0) dist.densityData[j]=Math.max(dist.densityData[j],distTemp.densityData[j]);
				if (distAll==null) distAll=distAllTemp; else distAll=distAll.add(distAllTemp);
			}
			break;

		case OPTIMIZE_GROUPS_SELECTION:
			dist=new DataDistributionImpl(48,48);
			value=0; dist.setToValue(0);
			for (int i=0;i<statistics.agentenProCallcenter.length;i++) {
				boolean ok=false;
				String s=statistics.agentenProCallcenter[i].name;
				for (int j=0;j<setup.optimizeGroupNames.length;j++) if (setup.optimizeGroupNames[j].equalsIgnoreCase(s)) {ok=true; break;}
				if (!ok) continue;
				obj=calcAgentsResultValues(statistics.agentenProCallcenter[i]);
				double valueTemp=(Double)obj[0];
				DataDistributionImpl distTemp=(DataDistributionImpl)obj[1];
				DataDistributionImpl distAllTemp=(DataDistributionImpl)obj[2];
				value=Math.max(value,valueTemp);
				for (int j=0;j<dist.densityData.length;j++) if (distAllTemp.densityData[j]>0) dist.densityData[j]=Math.max(dist.densityData[j],distTemp.densityData[j]);
				if (distAll==null) distAll=distAllTemp; else distAll=distAll.add(distAllTemp);
			}
			break;
		}

		obj=new Object[3]; obj[0]=value; obj[1]=dist; obj[2]=distAll;
		return obj;
	}

	/**
	 * Stellt ein, dass in einem Intervall Veränderung bzw. keine Veränderungen notwendig sind.
	 * @param interval48	Intervallnummer (bezogen auf Halbstundenintervalle)
	 * @param b	Veränderungen nötig?
	 */
	private void setNeedsChange(final int interval48, final boolean b) {
		int l=intervalNeedsChange.densityData.length;
		if (l==24) intervalNeedsChange.densityData[interval48/2]=b?1:-1;
		if (l==48) intervalNeedsChange.densityData[interval48]=b?1:-1;
		if (l==96) {intervalNeedsChange.densityData[interval48*2]=b?1:-1; intervalNeedsChange.densityData[interval48*2+1]=b?1:-1;}
	}

	/**
	 * Berechnet die Ergebniswerte eines Optimierungslaufs.
	 * @param statistics	Ergebnisse des	Optimierungslaufs
	 * @return	Gibt <code>0</code> zurück, wenn das Optimierungsergebnis erreicht wurde. Werte &gt;0 bedeuten, dass Agenten hinzugefügt werden sollen; Werte &lt;0 bedeuten, dass Agenten entfernt werden sollen.
	 */
	private int calcResultValues(final Statistics statistics) {
		/* Kenngröße berechnen */
		Object[] obj;
		if (setup.optimizeProperty==OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_WORK_LOAD) obj=calcAgentResultValues(statistics); else obj=calcClientResultValues(statistics);
		double value=(Double)obj[0];
		DataDistributionImpl dist=(DataDistributionImpl)obj[1];
		DataDistributionImpl distAll=(DataDistributionImpl)obj[2];

		/* Wenn nicht alle Intervalle gewählt sind, value entsprechend anpassen */
		if (!allIntervals()) {
			double sum=0, count=0;
			for (int i=0;i<setup.optimizeIntervals.densityData.length;i++) if (setup.optimizeIntervals.densityData[i]>0.1) {
				double d=dist.densityData[i];
				if (d>0) {
					sum+=dist.densityData[i];
					count+=1;
				}
			}
			if (count>0) value=sum/count;
		}

		int result=0;

		/* Und mit Min-Soll-Wert vergleichen */
		boolean done=true;
		if (setup.optimizeByInterval==OptimizeSetup.OptimizeInterval.OPTIMIZE_BY_INTERVAL_NO) {
			switch (setup.optimizeProperty) {
			case OPTIMIZE_PROPERTY_SUCCESS_BY_CALL:
			case OPTIMIZE_PROPERTY_SUCCESS_BY_CLIENT:
				done=(value==0 || value>setup.optimizeValue); break;
			case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CALL:
			case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CLIENT:
				done=(value<setup.optimizeValue); break;
			case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CALL:
			case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CLIENT:
				done=(value<setup.optimizeValue); break;
			case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL:
			case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL_ALL:
			case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT:
			case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT_ALL:
				done=(value==0 || value>setup.optimizeValue); break;
			case OPTIMIZE_PROPERTY_WORK_LOAD:
				done=(value<setup.optimizeValue); break;
			}

			if (allIntervals() || done) {
				intervalNeedsChange.setToValue(done?-1:1);
			} else {
				for (int i=0;i<setup.optimizeIntervals.densityData.length;i++) setNeedsChange(i,setup.optimizeIntervals.densityData[i]>0.1 && intervalChangeAllowed(i));
			}
		} else {
			for (int i=0;i<dist.densityData.length;i++) {

				boolean ok=true;
				if (distAll.densityData[i]>0) switch (setup.optimizeProperty) {
				case OPTIMIZE_PROPERTY_SUCCESS_BY_CALL:
				case OPTIMIZE_PROPERTY_SUCCESS_BY_CLIENT:
					ok=(dist.densityData[i]>setup.optimizeValue); break;
				case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CALL:
				case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CLIENT:
					ok=(dist.densityData[i]<setup.optimizeValue); break;
				case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CALL:
				case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CLIENT:
					ok=(dist.densityData[i]<setup.optimizeValue); break;
				case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL:
				case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL_ALL:
				case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT:
				case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT_ALL:
					ok=(dist.densityData[i]>setup.optimizeValue); break;
				case OPTIMIZE_PROPERTY_WORK_LOAD:
					ok=(dist.densityData[i]<setup.optimizeValue); break;
				}

				if (!allIntervals() && !ok) {
					if (setup.optimizeIntervals.densityData[i]<=0.1) ok=true;
				}

				if (setup.optimizeByInterval==OptimizeSetup.OptimizeInterval.OPTIMIZE_BY_INTERVAL_IN_ORDER) {
					if (i<=lastOKInterval) {
						setNeedsChange(i,false);
						continue;
					}
					if (i==lastOKInterval+1) {
						setNeedsChange(i,!ok);
						done=ok;
						if (ok || !intervalChangeAllowed(i)) lastOKInterval++;
						continue;
					}
					setNeedsChange(i,false);
				} else {
					if (!ok && !intervalChangeAllowed(i)) ok=true;
					setNeedsChange(i,!ok);
					done=done && ok;
				}
			}
		}

		/* Nun noch ggf. mit Max-Soll-Wert vergleichen */
		if (done && setup.optimizeMaxValue>=0) {
			if (setup.optimizeByInterval==OptimizeSetup.OptimizeInterval.OPTIMIZE_BY_INTERVAL_NO) {
				switch (setup.optimizeProperty) {
				case OPTIMIZE_PROPERTY_SUCCESS_BY_CALL: done=(value<setup.optimizeMaxValue); break;
				case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CALL: done=(value==0 || value>setup.optimizeMaxValue); break;
				case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CALL: done=(value==0 || value>setup.optimizeMaxValue); break;
				case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL: done=(value<setup.optimizeMaxValue); break;
				case OPTIMIZE_PROPERTY_WORK_LOAD: done=(value>setup.optimizeMaxValue); break;
				default: done=true; /* Tritt nicht auf. */ break;
				}

				if (allIntervals() || done) {
					intervalNeedsChange.setToValue(done?-1:1);
				} else {
					for (int i=0;i<setup.optimizeIntervals.densityData.length;i++) setNeedsChange(i,setup.optimizeIntervals.densityData[i]>0.1 && intervalChangeAllowed(i));
				}
			} else {
				for (int i=0;i<dist.densityData.length;i++) {

					boolean ok=true;
					if (distAll.densityData[i]>0) switch (setup.optimizeProperty) {
					case OPTIMIZE_PROPERTY_SUCCESS_BY_CALL: ok=(dist.densityData[i]<setup.optimizeMaxValue); break;
					case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CALL: ok=(dist.densityData[i]>setup.optimizeMaxValue); break;
					case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CALL: ok=(dist.densityData[i]>setup.optimizeMaxValue); break;
					case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL: ok=(dist.densityData[i]<setup.optimizeMaxValue); break;
					case OPTIMIZE_PROPERTY_WORK_LOAD: ok=(dist.densityData[i]>setup.optimizeMaxValue); break;
					default: ok=true; /* Tritt nicht auf. */ break;
					}

					if (!allIntervals() && !ok) {
						if (setup.optimizeIntervals.densityData[i]<=0.1) ok=true;
					}

					if (setup.optimizeByInterval==OptimizeSetup.OptimizeInterval.OPTIMIZE_BY_INTERVAL_IN_ORDER) {
						if (i<=lastOKInterval2) {
							setNeedsChange(i,false);
							continue;
						}
						if (i==lastOKInterval2+1) {
							if (agentsChanged.sub(agents).densityData[i]>0) ok=true;
							setNeedsChange(i,!ok);
							done=ok;
							if (ok || !intervalChangeAllowed(i)) lastOKInterval2++;
							continue;
						}
						setNeedsChange(i,false);
					} else {
						if (!ok && !intervalChangeAllowed(i)) ok=true;
						setNeedsChange(i,!ok);
						done=done && ok;
					}
				}
			}
			result=(done?0:-1);
		} else {
			result=(done?0:1);
		}

		/* Kenngrößen-Werte speichern */
		resultLast=resultCurrent;
		resultCurrent=dist;

		return result;
	}

	/**
	 * Nimmt die Ergebnisse eines Optimierungslaufes in {@link #results} auf.
	 * @param statistics	Neue Statistikergebnisse
	 * @param isFinalRun	Ist dies der letzten bzw. finale Lauf gewesen?
	 */
	private void storeResults(final Statistics statistics, final boolean isFinalRun) {
		/* Neue Ergebnisse in Liste aufnehmen */
		if (isFinalRun) {results.data.add(statistics); return;}
		if (memorySavingLevel==1 || runNr%memorySavingLevel==1) results.data.add(statistics);

		final int maxResults=50;

		/* Weniger als 50 Ergebnisse? Ok, Ende */
		if (results.data.size()<maxResults) return;

		/* Ergebnisse löschen */
		for (int i=maxResults-1;i>0;i-=2) results.data.remove(i);
		memorySavingLevel*=2;
	}

	/**
	 * Führt die Abschluss-Berechnungen nach Ende eines Simulationslaufes durch.
	 * In diesem Zusammenhang werden auch die Ergebnisse gespeichert und es wird geprüft,
	 * ob weitere Läufe zur Erreichung des Ziels notwendig sind.
	 * @return	Gibt <code>0</code> zurück, wenn das Optimierungsergebnis erreicht wurde. Werte &gt;0 bedeuten, dass Agenten hinzugefügt werden sollen; Werte &lt;0 bedeuten, dass Agenten entfernt werden sollen.
	 */
	public int simulationDone() {
		/* Ergebnisse aus Simulator auslesen */
		String errorMessage=simulator.finalizeRun();
		/* Vom Server gesandte Meldungen ausgeben */
		if (errorMessage!=null) {
			if (owner==null) {
				if (out!=null) out.println("Dialog.Title.Error"+": "+errorMessage);
			} else {
				MsgBox.error(owner,Language.tr("Optimization.Error.GeneralTitle"),errorMessage);
			}
			canceled=true;
			return 0;
		}

		Statistics statistics=simulator.collectStatistic();
		if (statistics==null) {canceled=true; return 0;}

		/* Berechnen, ob weitere Läufe notwendig sind */
		int changeNeeded=calcResultValues(statistics);
		if (thisIsLastRun) changeNeeded=0;

		/* Ergebnisse speichern */
		storeResults(statistics,changeNeeded==0);

		if (changeNeeded==0) {
			results.runTime=(int)((System.currentTimeMillis()-startTime)/1000);
			System.gc();
		}

		return changeNeeded;
	}

	/**
	 * Liefert das aktuelle Setup für den Optimierer zurück.
	 * @return	Aktuelle Optimierer-Setup.
	 */
	public OptimizeSetup getOptimizeSetup() {
		return setup;
	}

	/**
	 * Liefert die Nummer des aktuellen Simulationslaufes
	 * @return	1-basierende Nummer des aktuellen Simulationslaufes
	 */
	public int getCurrentRunNr() {
		return runNr;
	}

	/**
	 * Gibt zurück, ob die Optimierung (erfolglos) abgebrochen wurde.
	 * @return	true, wenn die Optimierung abgebrochen wurde.
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * Liefert das für den aktuellen Simulationslauf zuständige <code>ComplexCallcenterSimulator</code>-Objekt
	 * @return	Aktuelles Simulator-Objekt
	 */
	public CallcenterSimulatorInterface getSimulator() {
		return simulator;
	}

	/**
	 * Liefert die Original-Agentenzahlen und die für die Optimierung angepassten Agentenzahlen zurück (24, 48 oder 96 Werte).
	 * Es werden dabei nur die Agenten betrachtet, die laut Optimierungs-Setup angepasst werden sollen.
	 * @return	Array aus drei Einträgen: Erste Eintrag Original-Agentenzahlen, zweiter Eintrag angepasste Agentenzahlen, dritter Eintrag angepasste Agentenzahlen vom letzten Lauf
	 */
	public DataDistributionImpl[] getAgentsCounts() {
		DataDistributionImpl[] data=new DataDistributionImpl[3];
		data[0]=agents;
		data[1]=agentsChanged;
		data[2]=agentsChangedLast;
		return data;
	}

	/**
	 * Liefert die Werte der Kenngröße vom aktuellen und vom letzten Lauf zurück (48 Werte).
	 * @return	Array aus zwei Einträgen: Erste Eintrag alte Werte, zweiter Eintrag aktuelle Werte
	 */
	public DataDistributionImpl[] getOptimizeValue() {
		DataDistributionImpl[] data=new DataDistributionImpl[2];
		data[0]=resultLast;
		data[1]=resultCurrent;
		return data;
	}

	/**
	 * Liefert die Ergebnisse der Optimierung
	 * @return	Objekt vom Typ <code>OptimizeData</code>
	 */
	public OptimizeData getResults() {
		return (results.data.size()>0)?results:null;
	}
}