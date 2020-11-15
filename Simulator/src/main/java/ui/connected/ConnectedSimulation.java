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
package ui.connected;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import language.Language;
import mathtools.distribution.DataDistributionImpl;
import net.calc.StartAnySimulator;
import simulator.CallcenterSimulatorInterface;
import simulator.Statistics;
import simulator.Statistics.KundenDaten;
import tools.SetupData;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelCaller;
import ui.model.CallcenterRunModel;

/**
 * Diese Klasse ermöglicht es, eine verbundene Simulation durchzuführen.
 * @author Alexander Herzog
 * @version 1.0
 * @see ConnectedModel
 */
public final class ConnectedSimulation {
	/** Simulationsmodelle für die einzelnen Tage */
	private final List<File> models=new ArrayList<File>();
	/** Statistikergebnisse der einzelnen Tage */
	private final List<File> statistics=new ArrayList<File>();
	/** Übertrag gemäß Modell */
	private final List<HashMap<String,ConnectedModelUebertrag>> uebertrag=new ArrayList<HashMap<String,ConnectedModelUebertrag>>();
	/** Manueller Übertrag pro Tag: Liste der Kundengruppen */
	private final List<List<String>> uebertragCaller=new ArrayList<List<String>>();
	/** Manueller Übertrag pro Tag: Liste der Anrufer pro Gruppe */
	private final List<List<Integer>> uebertragCount=new ArrayList<List<Integer>>();
	/** Statistikergebnisse, die sich im letzten Simulationslauf ergeben haben */
	private Statistics lastStatistics;

	/** Nummer der laufenden Simulation (1-basierend) ({@link #getSimNr()}) */
	private int simNr=-1;
	/** Simulator der die eigentliche Arbeit ausführt */
	private CallcenterSimulatorInterface simulator;

	/**
	 * Lädt das angegeben verbundenen Simulationsmodell und prüft dabei, ob die in dem
	 * Modell angegebenen Dateien vorhanden sind.
	 * @param model	Verbundenes Simulationsmodell
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück; ansonsten eine Fehlermeldung als String.
	 */
	public String loadData(ConnectedModel model) {
		models.clear();
		statistics.clear();
		uebertrag.clear();
		uebertragCaller.clear();
		uebertragCount.clear();
		lastStatistics=null;

		simulator=null;
		simNr=-1;

		File folder=new File(model.defaultFolder);
		String s;

		lastStatistics=null;
		s=model.statisticsDay0;
		if (s!=null && !s.isEmpty()) {
			File statFile=(s.contains("/") || s.contains("\\"))?new File(s):new File(folder,s);
			if (!statFile.isFile()) return String.format(Language.tr("Connected.Error.Day0StatisticFileDoesNotExist"),statFile.toString());
			Statistics stat=new Statistics(null,null,0,0);
			s=stat.loadFromFile(statFile); if (s!=null) return s;
			lastStatistics=stat;
		}

		simNr=0;

		String lastModel="";
		for (int i=0;i<model.models.size();i++) {
			File f;

			/* Modelldatei */
			s=model.models.get(i);
			if (s.isEmpty()) s=lastModel;
			if (s.isEmpty()) return String.format(Language.tr("Connected.Error.NoModelForDay"),i+1);
			f=(s.contains("/") || s.contains("\\"))?new File(s):new File(folder,s);
			if (!f.exists()) return String.format(Language.tr("Connected.Error.ModelFileDoesNotExist"),i+1,f.toString());
			lastModel=f.toString();
			models.add(f);

			/* Statistikdatei */
			s=model.statistics.get(i);
			if (s.isEmpty()) statistics.add(null); else {
				statistics.add((s.contains("/") || s.contains("\\"))?new File(s):new File(folder,s));
			}

			/* Überträge */
			uebertrag.add(model.uebertrag.get(i));

			/* Manueller Übertrag */
			if (i==0) {
				uebertragCaller.add(model.additionalDay0CallerNames);
				uebertragCount.add(model.additionalDay0CallerCount);
			} else {
				uebertragCaller.add(new ArrayList<String>());
				uebertragCount.add(new ArrayList<Integer>());
			}
		}

		return null;
	}

	/**
	 * Liefert die Nummer der laufenden Simulation (1-basierend)
	 * @return Nummer der aktuell in Simulation befindlichen verknüpften Tages
	 */
	public int getSimNr() {
		return simNr;
	}

	/**
	 * Liefert die Anzahl der verbundenen Tage.
	 * @return Anzahl der verknüpften Tage.
	 */
	public int getSimCount() {
		return models.size();
	}

	/**
	 * Liefert ein Interface auf den Simulator.
	 * Nur gültig bis zum Aufruf von <code>doneSimulation</code>.
	 * @return Interface auf den aktuell laufenden Simulator
	 */
	public CallcenterSimulatorInterface getSimulator() {
		return simulator;
	}

	/**
	 * Übertrag der innerhalb und außerhalb wartenden Kunden (einzelner Kundentyp)
	 * @param days Simulierte Tage
	 * @param client	Kundentyp
	 * @param retry	Außerhalb wartende Kunden
	 * @param uebertragWaiting	Wartende Kunden
	 * @param uebertragTolerance	Restwartezeittoleranz der wartenden Kunden
	 */
	private static void getModifyCallerDataDirectConnect(int days, KundenDaten client, long[][] retry, long[][] uebertragWaiting, long[][] uebertragTolerance) {
		for (int k=0;k<days;k++) {
			List<Long> list;
			long[] array;

			list=client.kundenNextDayRetryProSimDay.get(k);
			array=new long[list.size()]; for (int i=0;i<list.size();i++) array[i]=list.get(i); retry[k]=array;

			list=client.kundenNextDayUebertragWaitingTimeProSimDay.get(k);
			array=new long[list.size()]; for (int i=0;i<list.size();i++) array[i]=list.get(i); uebertragWaiting[k]=array;

			list=client.kundenNextDayUebertragRestWaitingToleranceProSimDay.get(k);
			array=new long[list.size()]; for (int i=0;i<list.size();i++) array[i]=list.get(i); uebertragTolerance[k]=array;
		}
	}

	/**
	 * Übertrag der innerhalb und außerhalb wartenden Kunden (alle Kundentypen)
	 * @param lastStatistics	Statistik aus dem letzten Lauf
	 * @param model	Edítor-Modell
	 * @param connect	Überträge pro Kundentyp
	 * @param retry	Außerhalb wartende Kunden
	 * @param uebertragWaiting	Wartende Kunden
	 * @param uebertragTolerance	Restwartezeittoleranz der wartenden Kunden
	 */
	private static void getModifyDataDirectConnect(Statistics lastStatistics, CallcenterModel model, HashMap<String,ConnectedModelUebertrag> connect, List<long[][]> retry, List<long[][]> uebertragWaiting, List<long[][]> uebertragTolerance) {
		/* Innerhalb und außerhalb wartende Kunden übertragen */
		for (int i=0;i<model.caller.size();i++) {
			String name=model.caller.get(i).name;

			/* Datenstrukturen für diesen Kundentyp anlegen */
			long[][] retryByType=new long[model.days][]; Arrays.fill(retryByType,null);
			long[][] uebertragWaitingByType=new long[model.days][]; Arrays.fill(uebertragWaitingByType,null);
			long[][] uebertragToleranceByType=new long[model.days][]; Arrays.fill(uebertragToleranceByType,null);

			/* Daten für Kundentyp zusammenstellen */
			int statNr=-1;
			for (int j=0;j<lastStatistics.kundenProTyp.length;j++) if (lastStatistics.kundenProTyp[j].name.equals(name)) {statNr=j; break;}
			if (statNr>=0) {
				getModifyCallerDataDirectConnect(model.days,lastStatistics.kundenProTyp[statNr],retryByType,uebertragWaitingByType,uebertragToleranceByType);
			}

			retry.add(retryByType);
			uebertragWaiting.add(uebertragWaitingByType);
			uebertragTolerance.add(uebertragToleranceByType);
		}
	}

	/**
	 * Übertrag der endgültigen Abbrecher (global für alle Kundentypen)
	 * @param lastStatistics	Statistik aus dem letzten Lauf
	 * @param model	Edítor-Modell
	 * @param retryFactor	Wiederholwahrscheinlichkeit
	 * @param add	Zusätzlich hinzuzufügende Anrufe
	 */
	private static void getModifyDataAddCanceledCallerGlobal(Statistics lastStatistics, CallcenterModel model, double retryFactor, List<int[]> add) {
		for (int i=0;i<model.caller.size();i++) {
			String name=model.caller.get(i).name;

			/* Datenstrukturen für diesen Kundentyp anlegen */
			int[] addByType=new int[model.days]; Arrays.fill(addByType,0);

			/* Zu dem zu model.call.get(i) gehörender lastStatistics.kundenProTyp[statNr] bestimmen */
			int statNr=-1;
			for (int j=0;j<lastStatistics.kundenProTyp.length;j++) if (lastStatistics.kundenProTyp[j].name.equals(name)) {statNr=j; break;}

			/* Für alle Tage die Wiederholer berechnen */
			if (statNr>=0 && retryFactor>0) for (int k=0;k<model.days;k++) {
				addByType[k]=(int)Math.round(retryFactor*lastStatistics.kundenProTyp[statNr].kundenAbbruchProSimDay.get(k));
			}

			add.add(addByType);
		}
	}

	/**
	 * Passt das Modell in Bezug auf einen Kundentyp gemäß dem Übertrag an.
	 * @param days	Anzahl an Wiederholungen der Simulation des Tags
	 * @param client	Kundengruppen
	 * @param connect	Übertrags-Modell
	 * @param name	Name der betrachteten Kundengruppe
	 * @param add	Zuschlag pro simuliertem Tag
	 * @see #getModifyData(Statistics, HashMap, CallcenterModel, List, List, List, List, List, List)
	 */
	private static void getModifyCallerDataAddCanceledCaller(int days, KundenDaten[] client, HashMap<String,ConnectedModelUebertrag> connect, String name, int[] add) {
		Iterator<String> it=connect.keySet().iterator();

		while (it.hasNext()) {
			String connectName=it.next();
			ConnectedModelUebertrag c=connect.get(connectName);
			if (c.probability==0) continue;

			double factor=c.probability;

			if (c.changeRates.size()==0) {
				/* Keine Typänderung => nur berücksichtigen, wenn dies der eigene Kundentyp ist */
				if (!connectName.equals(name)) factor=0;
			} else {
				/* Typenänderungen konfiguriert => nachsehen, ob's was für den aktuellen Typ gibt */
				Double d=c.changeRates.get(name);
				if (d==null || d==0) {
					factor=0;
				} else {
					double sum=0;
					Iterator<String> it2=c.changeRates.keySet().iterator();
					while (it2.hasNext()) {sum+=c.changeRates.get(it2.next());}
					factor*=d/sum;
				}
			}

			/* Laut connect nichts zum Hinzufügen */
			if (factor==0) continue;

			/* client[statNr] finden, von dem die Abbrecheranzahlen genommen werden sollen */
			int statNr=-1;
			for (int j=0;j<client.length;j++) if (client[j].name.equals(connectName)) {statNr=j; break;}

			/* Abbrecher gemäß factor übertragen */
			if (statNr>=0) for (int k=0;k<days;k++) {
				add[k]+=(int)Math.round(factor*client[statNr].kundenAbbruchProSimDay.get(k));
			}
		}
	}

	/**
	 * Passt das Modell gemäß dem Übertrag an.
	 * @param lastStatistics	Statistikergebnisse der letzten Simulation
	 * @param connect	Übertrags-Modell
	 * @param model	Ausgangsmodell
	 * @param add	Zusätzliche Anrufer
	 * @param retry	Übertrag von Warteabbrechern
	 * @param uebertragWaiting	Wartezeiten der Kunden im System
	 * @param uebertragTolerance	Wartezeittoleranzen der Kunden im System
	 * @param uebertragCaller	Namen der Anrufergruppen
	 * @param uebertragCount	Anzahlen pro Anrufergruppe
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 * @see #buildConnectedRunModel(Statistics, HashMap, List, List, CallcenterModel)
	 */
	private static String getModifyData(Statistics lastStatistics, HashMap<String,ConnectedModelUebertrag> connect, CallcenterModel model, List<int[]> add, List<long[][]> retry, List<long[][]> uebertragWaiting, List<long[][]> uebertragTolerance, List<String> uebertragCaller, List<Integer> uebertragCount) {
		add.clear();
		retry.clear();
		uebertragWaiting.clear();
		uebertragTolerance.clear();

		/* Manuellen Übertrag hinzufügen */
		if (uebertragCaller!=null) for (int i=0;i<uebertragCaller.size();i++) {
			String s=uebertragCaller.get(i);
			for (int j=0;j<model.caller.size();j++) if (model.caller.get(j).name.equalsIgnoreCase(s)) {
				CallcenterModelCaller caller=model.caller.get(j);
				double addCount=uebertragCount.get(i);
				DataDistributionImpl dist=null;
				if (caller.freshCallsDist24!=null) dist=caller.freshCallsDist24;
				if (caller.freshCallsDist48!=null) dist=caller.freshCallsDist48;
				if (caller.freshCallsDist96!=null) dist=caller.freshCallsDist96;
				if (dist==null) continue;
				double count=caller.freshCallsCountMean;
				double sum=dist.sum();
				dist.densityData[0]+=addCount/count*sum;
				caller.freshCallsCountMean+=addCount;
			}
		}

		/* Übertrag auf Basis einer Statistik-Datei */
		if (lastStatistics==null) return null;
		if (lastStatistics.kundenGlobal.kundenAbbruchProSimDay.size()!=model.days) return Language.tr("Connected.Error.NotSameNumberOfDays");

		/* Innerhalb und außerhalb wartende Kunden übertragen */
		getModifyDataDirectConnect(lastStatistics,model,connect,retry,uebertragWaiting,uebertragTolerance);

		/* Endgültige Abbrecher teilweise zu den Erstanrufern addieren */
		if (connect.size()==1 && connect.get("")!=null) {
			/* Pauschaler Übertrag für alle Kundentypen */
			Double d=connect.get("").probability;
			double retryFactor=(d!=null && d>0)?d:0;
			getModifyDataAddCanceledCallerGlobal(lastStatistics,model,retryFactor,add);
		} else {
			/* Übertrag pro Kundentyp */
			for (int i=0;i<model.caller.size();i++) {
				/* Datenstrukturen für diesen Kundentyp anlegen */
				int[] addByType=new int[model.days]; Arrays.fill(addByType,0);
				/* Anpassung des Datentyps */
				getModifyCallerDataAddCanceledCaller(model.days,lastStatistics.kundenProTyp,connect,model.caller.get(i).name,addByType);
				/* Speichern */
				add.add(addByType);
			}
		}

		return null;
	}

	/**
	 * Erstellt ein Laufzeitmodell für einen Schritt innerhalb einer verbundenen Simulation.
	 * @param lastStatistics	Statistik vom Vortrag
	 * @param connect	Zusätzlicher Übertrag direkt vom Vortag
	 * @param uebertragCaller	Liste mit Kundentypnamen mit zusätzlichen Wiederholern vom Vortag
	 * @param uebertragCount	Liste mit Anzahl von Wiederholern pro Kundentyp	vom Vortag
	 * @param editModel	Editor-Callcenter-Modell welches die Basis für das Laufzeitmodell darstellen soll
	 * @return	Liefert im Erfolgsfall ein Laufzeitmodell ({@link CallcenterRunModel}) sonst einen String mit einer Fehlermeldung
	 */
	public static Object buildConnectedRunModel(final Statistics lastStatistics, final HashMap<String,ConnectedModelUebertrag> connect, final List<String> uebertragCaller, final List<Integer> uebertragCount, final CallcenterModel editModel) {
		List<int[]> add=new ArrayList<int[]>();
		List<long[][]> retry=new ArrayList<long[][]>();
		List<long[][]> uebertragWaiting=new ArrayList<long[][]>();
		List<long[][]> uebertragTolerance=new ArrayList<long[][]>();
		String s=getModifyData(lastStatistics,connect,editModel,add,retry,uebertragWaiting,uebertragTolerance,uebertragCaller,uebertragCount);
		if (s!=null) return Language.tr("Connected.Error.Initialization.Info")+"\n"+s;
		CallcenterRunModel runModel=new CallcenterRunModel(editModel,add,retry,uebertragWaiting,uebertragTolerance);
		return runModel;
	}

	/**
	 * Startet die Simulation des nächsten verknüpften Tages.
	 * @param logFile	Zu verwendende Logdatei (oder <code>null</code> wenn nicht geloggt werden soll)
	 * @return Im Erfolgsfall wird <code>null</code> zurück gegeben. Sind bereits alle Tage simuliert, so wird "" zurückgegeben. Im Fehlerfall wird eine Fehlermeldung als String zurückgegeben.
	 */
	public String initNextSimulation(File logFile) {
		if (simNr<0) return "";

		if (simNr>=models.size()) return "";
		simNr++;

		/* Modell laden */
		CallcenterModel editModel=new CallcenterModel();
		String s=editModel.loadFromFile(models.get(simNr-1));
		if (s!=null) return Language.tr("Connected.Error.LoadingModel.Info")+"\n"+s;

		/* Info über Connected-Modell Nummer in Logdatei schreiben */
		if (logFile!=null) {
			editModel.days=1;
			try {
				try (FileWriter logFileWriter=new FileWriter(logFile,true)) {
					if (simNr>1) logFileWriter.write("\n\n");
					logFileWriter.write(String.format(Language.tr("Connected.ProgressLog"),simNr)+"\n\n");
					logFileWriter.flush();
				}
			} catch (IOException e) {}
		}

		/* Modell vorbereiten */
		if (ui.VersionConst.isNewerVersion(editModel.version)) return Language.tr("Connected.Error.NewerVersion");

		Object o=buildConnectedRunModel(lastStatistics,uebertrag.get(simNr-1),uebertragCaller.get(simNr-1),uebertragCount.get(simNr-1),editModel);
		CallcenterRunModel runModel=null;
		if (o instanceof String) return (String)o;
		if (o instanceof CallcenterRunModel) runModel=(CallcenterRunModel)o;
		if (runModel==null) return Language.tr("Connected.Error.Internal");
		s=runModel.checkAndInit(false,false,SetupData.getSetup().strictCheck);
		if (s!=null) {
			return Language.tr("Connected.Error.Initialization.Info")+"\n"+s;
		}

		/* Simulation starten */
		StartAnySimulator startAnySimulator=new StartAnySimulator(editModel);
		s=startAnySimulator.check(); if (s!=null) return s;
		simulator=startAnySimulator.run();
		simulator.start(false);

		return null;
	}

	/**
	 * Muss nach Abschluss der Simulation eines verbundenen Tages aufgerufen werden. Speichert die Statistik usw.
	 * @return Gibt im Erfolgsfall <code>null</code> zurück; ansonsten eine Fehlermeldung als String.
	 */
	public String doneSimulation() {
		simulator.finalizeRun();
		Statistics statistic=simulator.collectStatistic();
		lastStatistics=statistic;

		try {
			if (statistic==null) return Language.tr("Connected.Simulation.Canceled");
			if (statistics.get(simNr-1)!=null) {
				if (!statistic.saveToFile(statistics.get(simNr-1))) return String.format(Language.tr("Connected.Error.SaveStatistic.Info"),statistics.get(simNr-1).toString());
			}
		} finally {simulator=null;}

		return null;
	}
}
