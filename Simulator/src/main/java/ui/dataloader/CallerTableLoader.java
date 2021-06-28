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
package ui.dataloader;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;

/**
 * Diese abstrakte Basisklasse stellt Methoden bereit, um Daten zu Kundenankünften
 * aus einer Tabelle zu laden.
 * @author Alexander Herzog
 * @see TableLoader
 */
public abstract class CallerTableLoader extends TableLoader {
	/**
	 * Welche Daten sollen in der Ergebnistabelle ausgegeben werden?
	 * @author Alexander Herzog
	 * @see CallerTableLoader#setSaveMode(SaveMode)
	 */
	public enum SaveMode {
		/** Alle Daten zu den Anrufern ausgeben */
		SAVEMODE_FULL,
		/** Nur Verteilung der Erstanrufer ausgeben */
		SAVEMODE_FRESHCALLS
	}

	/**
	 * Zuordnung der Kundentypnamen zu den Daten für die Kundentypen
	 * @see #getCallerDataForTypeName(String)
	 */
	private final HashMap<String,CallerData> data;

	/**
	 * Anzahl der Intervalle pro Tag
	 */
	private final int intervalCount;

	/** Welche Daten sollen in der Ergebnistabelle ausgegeben werden? */
	private SaveMode saveMode=SaveMode.SAVEMODE_FULL;

	/**
	 * Konstruktor der Klasse
	 * @param file	Dateiname der Datei aus der die Daten geladen werden sollen (darf nicht <code>null</code> sein)
	 * @param intervalCount	Anzahl der Intervalle pro Tag
	 */
	protected CallerTableLoader(final File file, final int intervalCount) {
		super(file);
		this.intervalCount=intervalCount;
		data=new HashMap<>();
	}

	/**
	 * Erstellt oder liefert auf Basis eines Kundentypnamens ein {@link CallerData}-Objekt.
	 * @param name	Name des Kundentyps
	 * @return	Datenobjekt in dem alle Informationen zu diesem Kundentyp zusammengefasst werden
	 */
	protected final CallerData getCallerDataForTypeName(final String name) {
		CallerData caller=data.get(name);
		if (caller==null) {caller=new CallerData(name,intervalCount); data.put(name,caller);}
		return caller;
	}

	/**
	 * Stellt ein, welche Daten in der Ergebnistabelle ausgegeben werden sollen.
	 * @param newSaveMode	Auszugebende Daten
	 * @see SaveMode
	 */
	public final void setSaveMode(final SaveMode newSaveMode) {
		saveMode=newSaveMode;
	}

	/**
	 * Trägt die Ergebnisse für den Modus "Alle Daten zu den Anrufern ausgeben" in eine Tabelle ein.
	 * @param results	Tabelle in die die Ergebnisse eingetragen werden sollen
	 * @return	Gibt <code>true</code> zurück, wenn die Ergebnisse erfolgreich in die Tabelle eingetragen werden konnten
	 * @see #getResults(Table)
	 */
	private final boolean getResultsFull(final Table results) {
		if (data.size()==0) return false;
		String[] keys=data.keySet().toArray(new String[0]);
		String[] line=new String[2+keys.length*5];

		/* Zeile 1 */
		line[0]=Language.tr("Loader.Range.From");
		line[1]=Language.tr("Loader.Range.To");
		for (int i=0;i<keys.length;i++) for (int j=0;j<5;j++) line[2+i*5+j]=keys[i];
		results.addLine(line);

		/* Zeile 2 */
		line[0]="";
		line[1]="";
		for (int i=0;i<keys.length;i++) {
			line[5*i+2]=Language.tr("Loader.Column.Arrivals");
			line[5*i+3]=Language.tr("Loader.Column.NumberOfCancelations");
			line[5*i+4]=Language.tr("Loader.Column.AverageWaitingTime");
			line[5*i+5]=Language.tr("Loader.Column.AverageCancelTime");
			line[5*i+6]=Language.tr("Loader.Column.AverageHoldingTime");
		}
		results.addLine(line);

		/* Zeile für die Intervalle */
		for (int i=0;i<intervalCount;i++) {
			line[0]=TimeTools.formatTime(i*(86400/intervalCount));
			line[1]=TimeTools.formatTime((i+1)*(86400/intervalCount)-1);
			for (int j=0;j<keys.length;j++) {
				double d;
				CallerData caller=data.get(keys[j]);
				line[2+j*5]=NumberTools.formatNumberMax(caller.freshCallsCount.densityData[i]);
				line[3+j*5]=NumberTools.formatNumberMax(caller.cancelCount.densityData[i]);
				double erfolg=caller.freshCallsCount.densityData[i]-caller.cancelCount.densityData[i];
				if (erfolg>0) d=caller.waitingTime.densityData[i]/erfolg; else d=0;
				line[4+j*5]=NumberTools.formatNumberMax(d);
				if (caller.cancelCount.densityData[i]>0) d=caller.cancelTime.densityData[i]/caller.cancelCount.densityData[i]; else d=0;
				line[5+j*5]=NumberTools.formatNumberMax(d);
				if (erfolg>0) d=caller.serviceTime.densityData[i]/erfolg; else d=0;
				line[6+j*5]=NumberTools.formatNumberMax(d);
			}
			results.addLine(line);
		}

		return true;
	}

	/**
	 * Trägt die Ergebnisse für den Modus "Nur Verteilung der Erstanrufer ausgeben " in eine Tabelle ein.
	 * @param results	Tabelle in die die Ergebnisse eingetragen werden sollen
	 * @return	Gibt <code>true</code> zurück, wenn die Ergebnisse erfolgreich in die Tabelle eingetragen werden konnten
	 * @see #getResults(Table)
	 */
	private final boolean getResultsFreshCallsOnly(final Table results) {
		if (data.size()==0) return false;
		String[] keys=data.keySet().toArray(new String[0]);
		String[] line=new String[2+keys.length];

		/* Zeile 1 */
		line[0]=Language.tr("Loader.Range.From");
		line[1]=Language.tr("Loader.Range.To");
		for (int i=0;i<keys.length;i++) line[2+i]=keys[i];
		results.addLine(line);

		/* Zeile für die Intervalle */
		for (int i=0;i<48;i++) {
			line[0]=TimeTools.formatTime(i*(86400/intervalCount));
			line[1]=TimeTools.formatTime((i+1)*(86400/intervalCount)-1);
			for (int j=0;j<keys.length;j++) line[2+j]=NumberTools.formatNumberMax(data.get(keys[j]).freshCallsCount.densityData[i]);
			results.addLine(line);
		}

		return true;
	}

	@Override
	protected final boolean getResults(final Table results) {
		if (saveMode==SaveMode.SAVEMODE_FULL) return getResultsFull(results);
		if (saveMode==SaveMode.SAVEMODE_FRESHCALLS) return getResultsFreshCallsOnly(results);
		return false;
	}

	/**
	 * Liefert auf Basis der geladenen Daten die Verteilung der Erstanrufer pro Intervall pro Kundentyp.
	 * @return	Verteilung der Erstanrufer pro Intervall pro Kundentyp
	 */
	protected final HashMap<String,DataDistributionImpl> getFreshCallsDistributions() {
		HashMap<String,DataDistributionImpl> map=new HashMap<>();

		Iterator<String> it=data.keySet().iterator();
		while (it.hasNext()) {
			String name=it.next();
			map.put(name,data.get(name).freshCallsCount.clone());
		}

		return map;
	}

	/**
	 * Liefert auf Basis der geladenen Daten die Verteilung der Anzahl der Warteabbrecher pro Intervall pro Kundentyp.
	 * @return	Verteilung der Anzahl der Warteabbrecher pro Intervall pro Kundentyp
	 */
	protected final HashMap<String,DataDistributionImpl> getCancelCountDistributions() {
		HashMap<String,DataDistributionImpl> map=new HashMap<>();

		Iterator<String> it=data.keySet().iterator();
		while (it.hasNext()) {
			String name=it.next();
			map.put(name,data.get(name).cancelCount.clone());
		}

		return map;
	}

	/**
	 * Liefert auf Basis der geladenen Daten die Verteilung der mittleren Wartezeiten pro Intervall pro Kundentyp.
	 * @return	Verteilung der mittleren Wartezeiten pro Intervall pro Kundentyp
	 */
	protected final HashMap<String,DataDistributionImpl> getWaitingTimeDistributions() {
		HashMap<String,DataDistributionImpl> map=new HashMap<>();

		Iterator<String> it=data.keySet().iterator();
		while (it.hasNext()) {
			String name=it.next();
			map.put(name,data.get(name).waitingTime.clone());
		}

		return map;
	}

	/**
	 * Liefert auf Basis der geladenen Daten die Verteilung der mittleren Bediendauern pro Intervall pro Kundentyp.
	 * @return	Verteilung der mittleren Bediendauern pro Intervall pro Kundentyp
	 */
	protected final HashMap<String,DataDistributionImpl> getServiceTimeDistributions() {
		HashMap<String,DataDistributionImpl> map=new HashMap<>();

		Iterator<String> it=data.keySet().iterator();
		while (it.hasNext()) {
			String name=it.next();
			map.put(name,data.get(name).serviceTime.clone());
		}

		return map;
	}

	/**
	 * Repräsentiert einen Kundentyp, der aus der Tabelle geladen wurde.
	 * @author Alexander Herzog
	 * @see CallerTableLoader#getCallerDataForTypeName(String)
	 */
	protected static final class CallerData {
		/** Name des Kundentyps */
		public final String name;

		/** Verteilung der Anzahl an Erstanrufern pro Intervall */
		public final DataDistributionImpl freshCallsCount;
		/** Verteilung der Anzahl an Warteabbrüchen pro Intervall */
		public final DataDistributionImpl cancelCount;
		/** Verteilung der mittleren Wartezeiten pro Intervall */
		public final DataDistributionImpl waitingTime;
		/** Verteilung der mittleren Abbruchzeiten pro Intervall */
		public final DataDistributionImpl cancelTime;
		/** Verteilung der mitteren Bedienzeiten pro Intervall */
		public final DataDistributionImpl serviceTime;

		/**
		 * Konstruktor der Klasse
		 * @param name	Name des Kundentyps
		 * @param intervalCount	Anzahl an Intervallen
		 */
		private CallerData(final String name, final int intervalCount) {
			this.name=name;
			freshCallsCount=new DataDistributionImpl(86399,intervalCount);
			cancelCount=new DataDistributionImpl(86399,intervalCount);
			waitingTime=new DataDistributionImpl(86399,intervalCount);
			cancelTime=new DataDistributionImpl(86399,intervalCount);
			serviceTime=new DataDistributionImpl(86399,intervalCount);
		}
	}
}
