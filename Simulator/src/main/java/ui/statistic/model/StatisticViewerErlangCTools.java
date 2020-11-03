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
package ui.statistic.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.special.Gamma;

import mathtools.ErlangC;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.DistributionTools;
import parser.CalcSystem;
import parser.MathCalcError;
import parser.MathParser;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;
import ui.model.CallcenterModelCaller;
import ui.model.CallcenterModelSkillLevel;

/**
 * Bestimmt Wartezeit und Service-Level mit Hilfe der Erlang-C-Formel basierend auf einem {@link CallcenterModel}-Objekt.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerErlangCTools {
	/** Callcenter-Modell, von dem Wartezeit und Service-Level bestimmt werden sollen */
	private CallcenterModel model;
	/** Einfaches (<code>false</code>) oder erweitertes (<code>true</code>) Erlang-C-Modell */
	private final boolean extended;
	/** Wurde die Berechnung abgeschlossen ({@link #calc()})? */
	private boolean calcDone=false;

	/** Wiederholwahrscheinlichkeiten pro Intervall */
	private final double[] retryProbability=new double[48];
	/** Bedienrate pro Intervall */
	private final double[] mu=new double[48];
	/** Abbruchrate pro Intervall */
	private final double[] nu=new double[48];
	/** Anzahl an verfügbaren Ageten pro Intervall */
	private final double[] agents=new double[48];

	/** Anzahl an Anrufern pro Intervall */
	private final double[] caller=new double[48];
	/** Anzahl an Wiederholern pro Intervall */
	private final double[] retryCaller=new double[48];
	/** Mittlere Wartezeit pro Intervall */
	private final double[] meanWaitingTime=new double[48];
	/** Anteil erfolgreicher Anrufe pro Intervall */
	private final double[] successProbability=new double[48];
	/** Service-Level pro Intervall */
	private final double[] serviceLevel=new double[48];

	/** Wiederholer aus dem jeweils vorherigen Intervall (75%) */
	private final double[] retryCallerFromLastInterval1=new double[48];
	/** Wiederholer aus dem jeweils vorherigen Intervall (25%) */
	private final double[] retryCallerFromLastInterval2=new double[48];

	/** Parser-Objekt für Formeln für die wartezeitanhängige Bediendauer */
	private MathParser parser;

	/**
	 * Konstruktor der Klasse <code>StatisticViewerErlangCTools</code>.
	 * @param model	Callcenter-Modell, von dem Wartezeit und Service-Level bestimmt werden sollen
	 * @param	extended	Einfaches (<code>false</code>) oder erweitertes (<code>true</code>) Erlang-C-Modell
	 */
	public StatisticViewerErlangCTools(final CallcenterModel model, final boolean extended) {
		this.model=model;
		this.extended=extended;

		parser=new CalcSystem(model.maxQueueLength,new String[]{"a"});
		if (parser.parse()!=-1) parser=null;
	}

	/**
	 * Berechnet die Anruferanzahl und die Bedienrate pro Intervall (über alle Anrufergruppen bzw. Agentengruppen) und liefert diese Werte zurück.
	 * @return	Array der Länge zwei aus Arrays der Länge 48 mit den Anruferanzahlen und den Bedienraten pro Intervall
	 */
	public double[][] getCallerAndMu() {
		getCallerAndAgents(model);
		return new double[][]{caller,mu};
	}

	/**
	 * Berechnet die Werte für ein einzelnes Intervall.
	 * Die Funktion verlässt sich dabei darauf, dass die Werte für die vorherigen Intervalle bereits berechnet wurden.
	 * @param model	Modell, dem die Agentenzahlen entnommen werden sollen
	 * @param interval	Zu berechnendes Intervall (0..47).
	 */
	public void calcInterval(CallcenterModel model, int interval) {
		this.model=model;

		/* Bestimmung von caller, retryProbability und nu[j] aus dem Modell */
		getCallerAndAgents(model);

		calcSingleInterval(interval);
	}

	/**
	 * Berechnet die Daten für ein Intervall.
	 * @param interval	Zeitintervall für das die Berechnung erfolgen soll
	 */
	private void calcSingleInterval(int interval) {
		double lambda=(caller[interval]+((interval==0)?0.0:retryCallerFromLastInterval1[interval-1]))/1800;
		int c=(int)Math.round(agents[interval]);
		int w=1000000;
		if (parser!=null) try {
			final double d=parser.calc(new double[]{agents[interval]});
			w=(int)Math.round(Math.max(0,d));
		} catch (MathCalcError e) {}
		int K=c+w;

		double[] Cn=ErlangC.extErlangCCn(lambda,mu[interval],nu[interval],c,K);
		K=Cn.length-1;

		if (c==0) {
			meanWaitingTime[interval]=1800;
			successProbability[interval]=0;
			serviceLevel[interval]=0;
		} else {
			double pi0=0; for (int n=0;n<=K;n++) pi0+=Cn[n]; pi0=1/pi0;
			double s=0; if (pi0==0) s=0; else for (int n=c+1;n<=K;n++) s+=((n-c))*Cn[n]*pi0;
			if (lambda==0) meanWaitingTime[interval]=0; else meanWaitingTime[interval]=s/(lambda*(1-Cn[K]*pi0));

			/* System.out.println("i="+i+" lambda="+(lambda*60)+" 1/mu="+(1/mu[i]/60)+" 1/nu="+(1/nu[i]/60)+" c="+c); */

			double piK; if (pi0==0) piK=0; else piK=pi0*Cn[K];
			if (lambda==0) successProbability[interval]=1; else successProbability[interval]=1-(piK+nu[interval]/lambda*s);
			serviceLevel[interval]=getWaitingTimeProbability(Cn,pi0,K,c,mu[interval],nu[interval],model.serviceLevelSeconds); /* Hier verwenden wir nur den globalen Service-Level - wir rechnen ja auch nur global */
		}

		retryCaller[interval]=caller[interval]*(1-successProbability[interval])*retryProbability[interval];
		retryCallerFromLastInterval1[interval]=retryCaller[interval]*0.75+((interval==0)?0.0:retryCallerFromLastInterval2[interval-1]);
		retryCallerFromLastInterval2[interval]=retryCaller[interval]*0.25;
	}

	/**
	 * Führt die Gesamtberechnung wenn nötig durch.
	 */
	private void calc() {
		if (calcDone) return;
		calcDone=true;

		/* Bestimmung von caller, retryProbability und nu[j] aus dem Modell */
		getCallerAndAgents(model);

		for (int i=0;i<48;i++) calcSingleInterval(i);
	}

	/**
	 * Liefert ein Array der Länge 48 mit der jeweiligen Anzahl an Erstanrufern pro Intervall
	 * @return	Erstanrufer pro Intervall
	 */
	public double[] getFreshCalls() {
		calc();
		return caller;
	}

	/**
	 * Liefert ein Array der Länge 48 mit der jeweiligen Anzahl an Wiederholern pro Intervall
	 * @return	Wiederholer pro Intervall
	 */
	public double[] getRetryCalls() {
		calc();
		return retryCaller;
	}

	/**
	 * Liefert ein Array der Länge 48 mit mittleren Wartezeiten (auf Sekundenbasis)
	 * @return	Mittlere Wartezeiten Array
	 */
	public double[] getMeanWaitingTime() {
		calc();
		return meanWaitingTime;
	}

	/**
	 * Liefert ein Array der Länge 48 mit Erreichbarkeitswahrscheinlichkeiten
	 * @return	Erreichbarkeitswahrscheinlichkeiten Array
	 */
	public double[] getSuccessProbability() {
		calc();
		return successProbability;
	}

	/**
	 * Liefert ein Array der Länge 48 mit Service-Level-Werten
	 * @return	Service-Level-Werte Array
	 */
	public double[] getServiceLevel() {
		calc();
		return serviceLevel;
	}

	/**
	 * Liefert ein Array der Länge 48 mit Werten, wie viele Agenten pro Intervall eingeplant sind
	 * @return	Agentenanzahl-Array
	 */
	public double[] getAgents() {
		calc();
		return agents;
	}

	/**
	 * Berechnet die Wahrscheinlichkeit für eine bestimmte Wartezeit
	 * @param Cn	C[n] aus der Erlang-C-Formel
	 * @param pi0	pi0 aus der Erlang-C-Formel
	 * @param K	Gesamtanzahl an Telefonleitungen
	 * @param c	Anzahl an verfügbaren Agenten
	 * @param mu	Bedienrate
	 * @param nu	Abbruchrate
	 * @param t	Zeitdauer für die die Wartezeitwahrscheinlichkeit berechnet werden soll
	 * @return	Wahrscheinlichkeit dafür, dass Kunden höcgstens so lange wie angegeben warten müssen
	 */
	private double getWaitingTimeProbability(double[] Cn, double pi0, int K, int c, double mu, double nu, double t) {
		/* 1-Cn[K]*pi0-pi0*sum(n=c...K-1)Cn[n]*gamma(n-c+1,(c*mu+nu)*t)/(n-c)! */

		double m=c*mu+nu;
		/* 1-Cn[K]*pi0-pi0*sum(n=c...K-1)Cn[n]*gamma(n-c+1,m*t)/(n-c)! */

		/* gamma(n-c+1,m*t)/(n-c)! = gamma(n-c1+,m*t)/gamma(n-c+1) = regularizedGammaQ(n-c+1,m*t) */
		/* => 1-Cn[K]*pi0-pi0*sum(n=c...K-1)Cn[n]*regularizedGammaQ(n-c+1,m*t) */
		double summe=0;
		for (int n=c;n<=K-1;n++) {
			if (Cn[n]<=0) break;
			summe+=Cn[n]*Gamma.regularizedGammaQ(n-c+1,m*t);
		}

		double ergebnis=1-Cn[K]*pi0-pi0*summe;
		return ergebnis;
	}

	/**
	 * Berechnet die Anzahl an Anrufern und verfügbaren Agenten
	 * @param model	Gesamtes Modell
	 */
	private void getCallerAndAgents(CallcenterModel model) {
		/* Bestimmung der Anzahl an Anrufern (Weiterleitungen werden approximativ berücksichtigt), der Weiterleitungswahrscheinlichkeit und der Wiederholwahrscheinlichkeit pro Halbstundenintervall */
		for (int j=0;j<48;j++) {caller[j]=0; retryProbability[j]=0; nu[j]=0;}

		for (int i=0;i<model.caller.size();i++) if (model.caller.get(i).active) {
			CallcenterModelCaller c=model.caller.get(i);
			DataDistributionImpl freshCalls=c.getFreshCallsDistOn48Base();
			freshCalls.normalizeDensity();
			for (int j=0;j<48;j++) {
				double numberCaller=c.freshCallsCountMean*freshCalls.densityData[j]*(1+c.continueProbability);
				caller[j]+=numberCaller;
				if (extended) retryProbability[j]+=c.retryProbabiltyAfterGiveUp*numberCaller;
				switch (c.waitingTimeMode) {
				case CallcenterModelCaller.WAITING_TIME_MODE_SHORT:
					nu[j]+=1/DistributionTools.getMean(c.waitingTimeDist)*numberCaller;
					break;
				case CallcenterModelCaller.WAITING_TIME_MODE_LONG:
					nu[j]+=1/DistributionTools.getMean(c.waitingTimeDistLong)*numberCaller;
					break;
				case CallcenterModelCaller.WAITING_TIME_MODE_CALC:
					nu[j]+=1/Math.max(1,c.waitingTimeCalcMeanWaitingTime/c.waitingTimeCalcCancelProbability+c.waitingTimeCalcAdd)*numberCaller;
					break;
				}
			}
		}
		for (int j=0;j<48;j++) {
			if (caller[j]>0) retryProbability[j]=retryProbability[j]/caller[j]; else retryProbability[j]=0;
			if (caller[j]>0) nu[j]=nu[j]/caller[j]; else nu[j]=0;
		}

		/* Liste aller Agenten aufstellen */
		List<CallcenterModelAgent> translatedAgents=new ArrayList<CallcenterModelAgent>();
		for (int i=0;i<model.callcenter.size();i++) if (model.callcenter.get(i).active) {
			CallcenterModelCallcenter c=model.callcenter.get(i);
			for (int j=0;j<c.agents.size();j++) if (c.agents.get(j).active) {
				CallcenterModelAgent a=c.agents.get(j);
				if (a.count>=0) translatedAgents.add(a); else translatedAgents.addAll(a.calcAgentShifts(false,c,model,true));
			}
		}

		/* Bestimmung der Agentenanzahl und der Bedienrate pro Halbstundenintervall */
		for (int j=0;j<48;j++) {agents[j]=0; mu[j]=0;}
		for (int i=0;i<translatedAgents.size();i++) {
			CallcenterModelAgent a=translatedAgents.get(i);
			int index1=(int)Math.round(((double)a.workingTimeStart)/(60*30));
			int index2; if (a.workingNoEndTime) index2=47; else index2=(int)Math.round(((double)a.workingTimeEnd)/(60*30))-1;
			for (int j=index1;j<=index2;j++) {
				double mean=getMeanWorkingTime(model,a,j);
				agents[j]+=(a.count);
				mu[j]+=(a.count)*mean;
			}
		}
		for (int i=0;i<48;i++) if (mu[i]!=0) mu[i]=agents[i]/mu[i];
	}

	/**
	 * Berechnet die mittlere Bediendauer für eine Agentengruppe.
	 * @param model	Gesamtes Modell
	 * @param a	Agentengruppe
	 * @param interval	Zeitintervall
	 * @return	Mittlere Bediendauer
	 */
	private double getMeanWorkingTime(CallcenterModel model, CallcenterModelAgent a, int interval) {
		CallcenterModelSkillLevel s=null;
		for (int i=0;i<model.skills.size();i++) if (model.skills.get(i).name.equalsIgnoreCase(a.skillLevel)) {s=model.skills.get(i); break;}
		if (s==null) return 0;

		double d=0;
		for (int i=0;i<s.callerTypeWorkingTime.size();i++) {
			String dist0=s.callerTypeIntervalWorkingTimeAddOn.get(i)[interval];
			if (dist0==null) dist0=s.callerTypeWorkingTimeAddOn.get(i);
			AbstractRealDistribution dist1=s.callerTypeIntervalWorkingTime.get(i)[interval];
			if (dist1==null) dist1=s.callerTypeWorkingTime.get(i);
			AbstractRealDistribution dist2=s.callerTypeIntervalPostProcessingTime.get(i)[interval];
			if (dist2==null) dist2=s.callerTypePostProcessingTime.get(i);

			if (dist0!=null && !dist0.isEmpty() && !dist0.equals("0")) {
				MathParser calc=new CalcSystem(dist0,new String[]{"w"});
				if (calc.parse()==-1) {
					try {
						d+=calc.calc(new double[]{0});
					} catch (MathCalcError e) {}
				}
			}
			d+=DistributionTools.getMean(dist1)+DistributionTools.getMean(dist2);
		}
		return d/(s.callerTypeWorkingTime.size());
	}
}
