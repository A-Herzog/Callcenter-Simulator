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

import language.Language;
import mathtools.distribution.DataDistributionImpl;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;
import ui.model.CallcenterModelCaller;
import ui.model.CallcenterModelSkillLevel;
import ui.model.CallcenterRunModel;

/**
 * Diese Klasse erlaubt das Umverteilen der Anrufe und der Agenten um der jeweils
 * anderen Verteilung möglichst gut zu entsprechen
 * @author Alexander Herzog
 * @version 1.0
 */
public class Rearranger {
	/**
	 * Enthält das Callcenter-Model, welches dem Konstruktor übergeben wurde und
	 * welches die Basis für die Anpassungen darstellt.
	 */
	public final CallcenterModel editModel;

	/**
	 * Enthält einen Wert ungleich <code>null</code>, wenn das <code>editModel</code>
	 * fehlerhaft ist und nicht für die Simulation verwendet werden kann.
	 * In diesem Fall können über die Methoden <code>moveCalls</code> und <code>moveAgents</code>
	 * keine Veränderungen an dem Modell vorgenommen werden.
	 * @see #editModel
	 * @see #moveCalls(String[])
	 * @see #moveAgents(String[], boolean)
	 */
	public final String modelError;

	/**
	 * Konstruktor der Klasse <code>Rearranger</code>
	 * Das übergebene Callcenter-Model wird beim Aufruf des Konstruktors geprüft; das Ergebnis wird in <code>modelError</code> gespeichert.
	 * Nur wenn das Modell fehlerfrei ist, können Veränderungen daran vorgenommen werden.
	 * @param editModel	Ausgangs-Callcenter-Modell, welches die Basis für alle späteren Veränderungen darstellt. Auf dieses Modell kann über die Member-Variable <code>editModel</code> zugegriffen werden.
	 * @see #editModel
	 * @see #modelError
	 */
	public Rearranger(final CallcenterModel editModel) {
		this.editModel=editModel;
		CallcenterRunModel runModel=new CallcenterRunModel(editModel);
		modelError=runModel.checkAndInit(false,false,false);
	}

	/**
	 * Prüft, ob die Anrufer zum besseren Abgleich mit den anwesenden Agenten verschoben werden können.
	 * (Wenn die Agenten bereits gemäß den Kundenankünften verteilt werden, ist eine Verschiebung der Kundenankünfte nicht zielführend.)
	 * @return	Gibt <code>true</code> zurück, wenn eine Verschiebung der Kundenankünfte sinnvoll möglich ist.
	 */
	public String canMoveCalls() {
		if (modelError!=null) return modelError;

		for (CallcenterModelCallcenter callcenter : editModel.callcenter) if (callcenter.active) {
			for (CallcenterModelAgent agent : callcenter.agents) if (agent.active && agent.count==-2) return Language.tr("Rearranger.MoveCalls.ErrorAgentsByCalls");
		}

		return null;
	}

	/**
	 * Gibt an, welche Kundengruppen durch die Agenten eines bestimmten Skill-Levels bedient werden können
	 * @param skillLevel	Name des Skill-Levels
	 * @return	boolean-Array mit Angaben dazu, welche Kundengruppen durch Agenten des Skill-Levels bedient werden können
	 */
	private boolean[] getSkills(final String skillLevel) {
		boolean[] skills=new boolean[editModel.caller.size()];
		Arrays.fill(skills,false);

		for (CallcenterModelSkillLevel skillLevelObj : editModel.skills) if (skillLevelObj.name.equalsIgnoreCase(skillLevel)) {
			for (String skill: skillLevelObj.callerTypeName) {
				for (int i=0;i<editModel.caller.size();i++) if (editModel.caller.get(i).active && editModel.caller.get(i).name.equalsIgnoreCase(skill)) skills[i]=true;
			}
			break;
		}

		return skills;
	}

	/**
	 * Verschiebt die Anrufe in den angegebenen Kundengruppen, so dass diese möglichst gut zu der Agentenverteilung passen.
	 * @param groups	Liste der Kundengruppen, die verändert werden sollen. Wird <code>null</code> oder eine leere Liste übergeben, so werden alle Gruppen angepasst.
	 * @return	Angepasstes Modell. (Im Fall <code>modelError!=null</code> oder <code>canMoveCalls()==null</code>  wird hier nur das Ausgangsmodell zurückgegeben.)
	 */
	public CallcenterModel moveCalls(String[] groups) {
		if (modelError!=null) return editModel;

		/* Welche Anrufergruppen sollen geändert werden? */
		boolean[] changeGroup=new boolean[editModel.caller.size()];
		boolean ok=false;
		for (int i=0;i<changeGroup.length;i++) {
			CallcenterModelCaller caller=editModel.caller.get(i);
			if (!caller.active) {changeGroup[i]=false; continue;}
			if (groups==null || groups.length==0) {changeGroup[i]=true; ok=true; continue;}
			boolean b=false;
			for (String s: groups) if (s.equalsIgnoreCase(caller.name)) {b=true; break;}
			changeGroup[i]=b; ok=ok || b;
		}
		if (!ok) return editModel;

		/* Bedienleistung pro Gruppe und Intervall bestimmen */
		DataDistributionImpl[] workForce=new DataDistributionImpl[editModel.caller.size()];
		List<Integer> workForceDiv=new ArrayList<>();
		for (int i=0;i<workForce.length;i++) workForce[i]=new DataDistributionImpl(86399,96);
		for (int i=0;i<96;i++) {
			for (CallcenterModelCallcenter callcenter : editModel.callcenter) if (callcenter.active) {
				for (CallcenterModelAgent agent : callcenter.agents) if (agent.active) {
					if (agent.count==-2) return editModel; /* Verteilung der Agenten nach Kunden */
					/* Anzahl an Agenten in dem Intervall bestimmen */
					int count=0;
					if (agent.count==-1) {
						/* Arbeitszeiten der Agenten gemäß fixer Verteilungskurve */
						if (agent.countPerInterval24!=null) count=(int)Math.round(agent.countPerInterval24.densityData[i/4]);
						if (agent.countPerInterval48!=null) count=(int)Math.round(agent.countPerInterval48.densityData[i/2]);
						if (agent.countPerInterval96!=null) count=(int)Math.round(agent.countPerInterval96.densityData[i]);
					} else {
						/* Agenten mit festen Arbeitszeiten */
						if (agent.workingTimeStart<=i*900 && agent.workingTimeEnd>(i+1)*900) count=agent.count;
					}
					/* Anzahl auf Kundentypen aufteilen */
					boolean[] skills=getSkills(agent.skillLevel);
					int skillCount=0; for (boolean skill : skills) if (skill) skillCount++;
					if (workForceDiv.indexOf(skillCount)<0) workForceDiv.add(skillCount);
					for (int j=0;j<workForce.length;j++) if (skills[j]) workForce[j].densityData[i]+=(double)count/skillCount;
				}
			}
		}

		/* Bedienleistungen sind nur relativ zu sehen, daher können wir die Werte zu Ganzzahlen erweitern */
		int mul=1; for (int i: workForceDiv) mul*=i;
		for (int i=0;i<workForce.length;i++) workForce[i]=workForce[i].multiply(mul).round();

		CallcenterModel newModel=editModel.clone();
		newModel.name+=" ("+Language.tr("Rearranger.MoveCalls.Changed")+")";

		/* Kunden umverteilen */
		for (int i=0;i<changeGroup.length;i++) if (changeGroup[i]) {
			CallcenterModelCaller caller=newModel.caller.get(i);
			if (caller.freshCallsDist24!=null) for (int j=0;j<24;j++) caller.freshCallsDist24.densityData[j]=workForce[i].densityData[j*2]+workForce[i].densityData[j*2+1]+workForce[i].densityData[j*2+2]+workForce[i].densityData[j*2+3];
			if (caller.freshCallsDist48!=null) for (int j=0;j<48;j++) caller.freshCallsDist48.densityData[j]=workForce[i].densityData[j*2]+workForce[i].densityData[j*2+1];
			if (caller.freshCallsDist96!=null) caller.freshCallsDist96=workForce[i].clone();
		}

		return newModel;
	}

	/**
	 * Verschiebt Agenten innerhalb der angegebenen Gruppen, so dass diese möglichst gut zu der Kunden-Erstanrufverteilung passen.
	 * @param groups	Liste der Agentengruppen (in der Form "Nr-CallcenterName" mit "Nr" 1-basierend) , die verändert werden sollen. Wird <code>null</code> oder eine leere Liste übergeben, so werden alle Gruppen angepasst.
	 * @param additionalMove	Zusätzliche Verschiebung hin zu späteren Uhrzeiten?
	 * @return	Angepasstes Modell. (Im Fall <code>modelError!=null</code> wird hier nur das Ausgangsmodell zurückgegeben.)
	 */
	public CallcenterModel moveAgents(String[] groups, boolean additionalMove) {
		if (modelError!=null) return editModel;

		/* Welche Agentengruppen sollen geändert werden? */
		boolean[][] changeGroup=new boolean[editModel.callcenter.size()][];
		boolean ok=false;
		for (int i=0;i<changeGroup.length;i++) {
			CallcenterModelCallcenter callcenter=editModel.callcenter.get(i);
			changeGroup[i]=new boolean[callcenter.agents.size()];
			if (!callcenter.active) {Arrays.fill(changeGroup[i],false); continue;}
			for (int j=0;j<changeGroup[i].length;j++) {
				CallcenterModelAgent agent=callcenter.agents.get(j);
				if (!agent.active || agent.count>=0 || agent.count==-2) {changeGroup[i][j]=false; continue;}
				boolean b=false;
				if (groups==null || groups.length==0) {changeGroup[i][j]=true; ok=true; continue;}
				for (String s: groups) if (s.equalsIgnoreCase(""+(j+1)+"-"+callcenter.name)) {b=true; break;}
				changeGroup[i][j]=b; ok=ok || b;
			}
		}
		if (!ok) return editModel;

		/* Anzahl an Anrufen pro Kundengruppe und Intervall bestimmen */
		DataDistributionImpl[] calls=new DataDistributionImpl[editModel.caller.size()];
		for (int i=0;i<calls.length;i++) {
			calls[i]=new DataDistributionImpl(86399,96);
			CallcenterModelCaller caller=editModel.caller.get(i);
			if (!caller.active) continue;
			if (caller.freshCallsDist24!=null) for (int j=0;j<24;j++) {double d=caller.freshCallsDist24.densityData[j]/4; calls[i].densityData[4*j]=d; calls[i].densityData[4*j+1]=d; calls[i].densityData[4*j+2]=d; calls[i].densityData[4*j+3]=d;}
			if (caller.freshCallsDist48!=null) for (int j=0;j<48;j++) {double d=caller.freshCallsDist48.densityData[j]/2; calls[i].densityData[2*j]=d; calls[i].densityData[2*j+1]=d;}
			if (caller.freshCallsDist96!=null) calls[i]=caller.freshCallsDist96.clone();
		}

		/* Zusätzliche Verschiebung hin zu späteren Uhrzeiten */
		if (additionalMove) {
			DataDistributionImpl mul=new DataDistributionImpl(86399,96);
			for (int i=0;i<96;i++) mul.densityData[i]=0.9+0.2*i/96;
			for (int i=0;i<calls.length;i++) calls[i]=calls[i].multiply(mul);
		}

		CallcenterModel newModel=editModel.clone();
		newModel.name+=" ("+Language.tr("Rearranger.MoveAgents.Changed")+")";

		/* Agenten umverteilen */
		for (int i=0;i<newModel.callcenter.size();i++) {
			CallcenterModelCallcenter callcenter=newModel.callcenter.get(i);
			if (!callcenter.active) continue;
			for (int j=0;j<callcenter.agents.size();j++) {
				CallcenterModelAgent agent=callcenter.agents.get(j);
				if (!agent.active || !changeGroup[i][j]) continue;
				/* Nur Gruppen mit count==-1, alle anderen wurden über changeGroup bereits ausgesondert */
				boolean[] skills=getSkills(agent.skillLevel);
				DataDistributionImpl dist=null;
				for (int k=0;k<skills.length;k++) if (skills[k]) {
					if (dist==null) dist=calls[k].clone(); else dist=dist.add(calls[k]);
				}
				if (agent.countPerInterval24!=null) {
					double sum=agent.countPerInterval24.sum();
					if (dist!=null) for (int k=0;k<24;k++) agent.countPerInterval24.densityData[k]=(dist.densityData[4*k]+dist.densityData[4*k+1]+dist.densityData[4*k+2]+dist.densityData[4*k+3]);
					double sum2=agent.countPerInterval24.sum(); if (sum2==0) sum2=1;
					agent.countPerInterval24=agent.countPerInterval24.multiply(sum/sum2).round();
				}
				if (agent.countPerInterval48!=null) {
					double sum=agent.countPerInterval48.sum();
					if (dist!=null) for (int k=0;k<48;k++) agent.countPerInterval48.densityData[k]=(dist.densityData[2*k]+dist.densityData[2*k+1]);
					double sum2=agent.countPerInterval48.sum(); if (sum2==0) sum2=1;
					agent.countPerInterval48=agent.countPerInterval48.multiply(sum/sum2).round();
				}
				if (agent.countPerInterval96!=null) {
					double sum=agent.countPerInterval96.sum();
					if (dist!=null) agent.countPerInterval96=dist.clone();
					double sum2=agent.countPerInterval96.sum(); if (sum2==0) sum2=1;
					agent.countPerInterval96=agent.countPerInterval96.multiply(sum/sum2).round();
				}
			}
		}

		return newModel;
	}

	/**
	 * Mischt die beiden übergebenen Modelle in Bezug auf Anrufer und Agenten
	 * @param model1	Erstes Modell
	 * @param model2	Zweites Modell
	 * @param value	Muss eine Zahl zwischen 0 und 1 enthalten, die angibt, wie stark Modell 1 und Modell 2 berücksichtigt werden sollen.
	 * @return	Neues, kombiniertes Modell; im Falle, dass die Modelle zu verschieden sind, um sie zu kombinieren, wird <code>model1</code> zurückgegeben.
	 */
	public CallcenterModel mixModels(CallcenterModel model1, CallcenterModel model2, double value) {
		/* Modell 1 zurückgeben, wenn die Modelle zu verschieden sind, um sie zu mischen */
		if (model1.caller.size()!=model2.caller.size()) return model1;
		if (model1.callcenter.size()!=model2.callcenter.size()) return model1;
		for (int i=0;i<model1.callcenter.size();i++) if (model1.callcenter.get(i).agents.size()!=model2.callcenter.get(i).agents.size()) return model1;

		value=Math.max(0,Math.min(1,value));

		CallcenterModel newModel=model1.clone();
		newModel.name=model2.name;

		for (int i=0;i<newModel.caller.size();i++) {
			CallcenterModelCaller caller=newModel.caller.get(i);
			CallcenterModelCaller caller1=model1.caller.get(i);
			CallcenterModelCaller caller2=model2.caller.get(i);

			caller.freshCallsCountMean=(int)Math.round(caller1.freshCallsCountMean*(1-value)+caller2.freshCallsCountMean*value);
			caller.freshCallsCountSD=caller1.freshCallsCountSD*(1-value)+caller2.freshCallsCountSD*value;
			if (caller.freshCallsDist24!=null) caller.freshCallsDist24=caller1.freshCallsDist24.multiply(1-value).add(caller2.freshCallsDist24.multiply(value));
			if (caller.freshCallsDist48!=null) caller.freshCallsDist48=caller1.freshCallsDist48.multiply(1-value).add(caller2.freshCallsDist48.multiply(value));
			if (caller.freshCallsDist96!=null) caller.freshCallsDist96=caller1.freshCallsDist96.multiply(1-value).add(caller2.freshCallsDist96.multiply(value));
		}

		for (int i=0;i<newModel.callcenter.size();i++) {
			CallcenterModelCallcenter callcenter=newModel.callcenter.get(i);
			CallcenterModelCallcenter callcenter1=model1.callcenter.get(i);
			CallcenterModelCallcenter callcenter2=model2.callcenter.get(i);
			for (int j=0;j<callcenter.agents.size();j++) {
				CallcenterModelAgent agents=callcenter.agents.get(j);
				CallcenterModelAgent agents1=callcenter1.agents.get(j);
				CallcenterModelAgent agents2=callcenter2.agents.get(j);

				if (agents.count>=0) {
					agents.count=(int)Math.round(agents1.count*(1-value)+agents2.count*value);
					agents.workingTimeStart=(int)Math.round(agents1.workingTimeStart*(1-value)+agents2.workingTimeStart*value);
					agents.workingTimeEnd=(int)Math.round(agents1.workingTimeEnd*(1-value)+agents2.workingTimeEnd*value);
				}
				if (agents.count==-1) {
					if (agents.countPerInterval24!=null) agents.countPerInterval24=agents1.countPerInterval24.multiply(1-value).add(agents2.countPerInterval24.multiply(value)).round();
					if (agents.countPerInterval48!=null) agents.countPerInterval48=agents1.countPerInterval48.multiply(1-value).add(agents2.countPerInterval48.multiply(value)).round();
					if (agents.countPerInterval96!=null) agents.countPerInterval96=agents1.countPerInterval96.multiply(1-value).add(agents2.countPerInterval96.multiply(value)).round();
				}
				if (agents.count==-2) {
					agents.byCallersAvailableHalfhours=(int)Math.round(agents1.byCallersAvailableHalfhours*(1-value)+agents2.byCallersAvailableHalfhours*value);
				}
			}
		}

		return newModel;
	}

	/**
	 * Mischt ein neues Modell mit dem Ausgangsmodell (<code>editModel</code>)
	 * @param model2	Zweites Modell
	 * @param value	Muss eine Zahl zwischen 0 und 1 enthalten, die angibt, wie stark das Ausgangsmodell und das neue Modell berücksichtigt werden sollen (0=nur Ausgangsmodell, 1=nur neues Modell)
	 * @return	Neues, kombiniertes Modell
	 */
	public CallcenterModel mixModels(CallcenterModel model2, double value) {
		return mixModels(editModel,model2,value);
	}
}