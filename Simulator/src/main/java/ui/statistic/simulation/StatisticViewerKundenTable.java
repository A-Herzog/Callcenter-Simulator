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
package ui.statistic.simulation;

import java.util.ArrayList;
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TimeTools;
import simulator.Statistics;
import simulator.Statistics.KundenDaten;
import systemtools.statistics.StatisticViewerTable;

/**
 * Zeigt Kunden-spezifischen Statistikdaten in einer Tabelle an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerKundenTable extends StatisticViewerTable {
	private final Mode dataType;
	private final Statistics statistic;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerKundenTable#StatisticViewerKundenTable(Statistics, Mode)
	 */
	public enum Mode {
		/**
		 * Liefert die Anzahl an Anrufern.
		 */
		DATA_TYPE_COUNT,

		/**
		 * Liefert die Anzahl an erfolgreichen Anrufern.
		 */
		DATA_TYPE_SUCCESS,

		/**
		 * Liefert den Service-Level.
		 */
		DATA_TYPE_SERVICE_LEVEL,

		/**
		 * Liefert Warte- und Abbruchzeiten.
		 */
		DATA_TYPE_WAITINGTIME,

		/**
		 * Liefert die Wartezeitverteilung auf Anrufbasis.
		 */
		DATA_TYPE_CALLER_WAITINGTIME_DIST,

		/**
		 * Liefert die Verweilzeitverteilung auf Anrufbasis.
		 */
		DATA_TYPE_CALLER_STAYINGTIME_DIST,

		/**
		 * Liefert die Abbruchzeitverteilung auf Anrufbasis.
		 */
		DATA_TYPE_CALLER_CANCELTIME_DIST,

		/**
		 * Liefert die Wartezeitverteilung auf Kundenbasis.
		 */
		DATA_TYPE_CLIENT_WAITINGTIME_DIST,

		/**
		 * Liefert die Verweilzeitverteilung auf Kundenbasis.
		 */
		DATA_TYPE_CLIENT_STAYINGTIME_DIST,

		/**
		 * Liefert die Abbruchzeitverteilung auf Kundenbasis.
		 */
		DATA_TYPE_CLIENT_CANCELTIME_DIST,

		/**
		 * Liefert die lange Wartezeitverteilung auf Anrufbasis.
		 */
		DATA_TYPE_CALLER_WAITINGTIME_DIST_LONG,

		/**
		 * Liefert die lange Verweilzeitverteilung auf Anrufbasis.
		 */
		DATA_TYPE_CALLER_STAYINGTIME_DIST_LONG,

		/**
		 * Liefert die lange Abbruchzeitverteilung auf Anrufbasis.
		 */
		DATA_TYPE_CALLER_CANCELTIME_DIST_LONG,

		/**
		 * Liefert die lange Wartezeitverteilung auf Kundenbasis.
		 */
		DATA_TYPE_CLIENT_WAITINGTIME_DIST_LONG,

		/**
		 * Liefert die lange Verweilzeitverteilung auf Kundenbasis.
		 */
		DATA_TYPE_CLIENT_STAYINGTIME_DIST_LONG,

		/**
		 * Liefert die lange Abbruchzeitverteilung auf Kundenbasis.
		 */
		DATA_TYPE_CLIENT_CANCELTIME_DIST_LONG
	}


	/**
	 * Konstruktor der Klasse <code>StatisticViewerKundenTable</code>
	 * @param statistic	Objekt vom Typ <code>ComplexStatisticSimData</code>, dem die Kundendaten entnommen werden sollen
	 * @param dataType	Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten.
	 */
	public StatisticViewerKundenTable(Statistics statistic, Mode dataType) {
		super();
		this.dataType=dataType;
		this.statistic=statistic;
	}

	@Override
	protected void buildTable() {
		buildClientTable(statistic,getRowNames());
	}

	private String[] getTimeArray() {
		String[] row=new String[KundenDaten.DistMax];
		for (int i=0;i<KundenDaten.DistMax;i++) row[i]=i+" "+Language.tr("Statistic.Seconds");
		return row;
	}

	private String[] getLongTimeArray() {
		String[] row=new String[KundenDaten.DistMax];
		for (int i=0;i<KundenDaten.DistMax;i++) row[i]=TimeTools.formatTime(i*1800)+"-"+TimeTools.formatTime((i+1)*1800-1);
		return row;
	}

	private String[] getRowNames() {
		switch (dataType) {
		case DATA_TYPE_COUNT:
			return new String[]{
					Language.tr("SimStatistic.FreshCalls"),
					Language.tr("SimStatistic.RecallingClients.Info"),
					Language.tr("SimStatistic.Retryer")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",
					Language.tr("SimStatistic.Call.Total"),
					Language.tr("SimStatistic.Call.Blocked"),
					Language.tr("SimStatistic.Call.Blocked"),
					Language.tr("SimStatistic.Call.Canceled"),
					Language.tr("SimStatistic.Call.Canceled"),
					Language.tr("SimStatistic.Call.CarriedOver"),
					Language.tr("SimStatistic.Call.CarriedOver"),
					Language.tr("SimStatistic.Client.Canceled"),
					Language.tr("SimStatistic.Client.Canceled"),
					Language.tr("SimStatistic.ForwardedCalls"),
					Language.tr("SimStatistic.ForwardedCalls"),
					Language.tr("SimStatistic.ForwardedClients"),
					Language.tr("SimStatistic.ForwardedClients"),
					Language.tr("SimStatistic.SuccessfulCalls"),
					Language.tr("SimStatistic.SuccessfulCalls"),
					Language.tr("SimStatistic.SuccessfulClients"),
					Language.tr("SimStatistic.SuccessfulClients")
			};
		case DATA_TYPE_SUCCESS:
			return new String[]{
					Language.tr("SimStatistic.Accessibility")+" "+Language.tr("SimStatistic.OnCallBasis"),
					Language.tr("SimStatistic.Accessibility")+" "+Language.tr("SimStatistic.OnClientBasis")
			};
		case DATA_TYPE_SERVICE_LEVEL:
			return new String[]{
					Language.tr("SimStatistic.ServiceLevel")+" "+Language.tr("SimStatistic.OnCallBasis")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")",
					Language.tr("SimStatistic.ServiceLevel")+" "+Language.tr("SimStatistic.OnCallBasis")+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")",
					Language.tr("SimStatistic.ServiceLevel")+" "+Language.tr("SimStatistic.OnClientBasis")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")",
					Language.tr("SimStatistic.ServiceLevel")+" "+Language.tr("SimStatistic.OnClientBasis")+" ("+Language.tr("SimStatistic.CalculatedOn.AllClients")+")"
			};
		case DATA_TYPE_WAITINGTIME:
			return new String[]{
					Language.tr("SimStatistic.WaitingTime.Mean")+" "+Language.tr("SimStatistic.OnCallBasis"),
					Language.tr("SimStatistic.WaitingTime.StdDev")+" "+Language.tr("SimStatistic.OnCallBasis"),
					Language.tr("SimStatistic.WaitingTime.CV")+" "+Language.tr("SimStatistic.OnCallBasis"),
					Language.tr("SimStatistic.ResidenceTime.Mean")+" "+Language.tr("SimStatistic.OnCallBasis"),
					Language.tr("SimStatistic.ResidenceTime.StdDev")+" "+Language.tr("SimStatistic.OnCallBasis"),
					Language.tr("SimStatistic.ResidenceTime.CV")+" "+Language.tr("SimStatistic.OnCallBasis"),
					Language.tr("SimStatistic.CancelTime.Mean")+" "+Language.tr("SimStatistic.OnCallBasis"),
					Language.tr("SimStatistic.CancelTime.StdDev")+" "+Language.tr("SimStatistic.OnCallBasis"),
					Language.tr("SimStatistic.CancelTime.CV")+" "+Language.tr("SimStatistic.OnCallBasis"),
					Language.tr("SimStatistic.WaitingTime.Mean")+" "+Language.tr("SimStatistic.OnClientBasis"),
					Language.tr("SimStatistic.WaitingTime.StdDev")+" "+Language.tr("SimStatistic.OnClientBasis"),
					Language.tr("SimStatistic.WaitingTime.CV")+" "+Language.tr("SimStatistic.OnClientBasis"),
					Language.tr("SimStatistic.ResidenceTime.Mean")+" "+Language.tr("SimStatistic.OnClientBasis"),
					Language.tr("SimStatistic.ResidenceTime.StdDev")+" "+Language.tr("SimStatistic.OnClientBasis"),
					Language.tr("SimStatistic.ResidenceTime.CV")+" "+Language.tr("SimStatistic.OnClientBasis"),
					Language.tr("SimStatistic.CancelTime.Mean")+" "+Language.tr("SimStatistic.OnClientBasis"),
					Language.tr("SimStatistic.CancelTime.StdDev")+" "+Language.tr("SimStatistic.OnClientBasis"),
					Language.tr("SimStatistic.CancelTime.CV")+" "+Language.tr("SimStatistic.OnClientBasis")
			};
		case DATA_TYPE_CALLER_WAITINGTIME_DIST:
		case DATA_TYPE_CALLER_STAYINGTIME_DIST:
		case DATA_TYPE_CALLER_CANCELTIME_DIST:
		case DATA_TYPE_CLIENT_WAITINGTIME_DIST:
		case DATA_TYPE_CLIENT_STAYINGTIME_DIST:
		case DATA_TYPE_CLIENT_CANCELTIME_DIST:
			return getTimeArray();
		case DATA_TYPE_CALLER_WAITINGTIME_DIST_LONG:
		case DATA_TYPE_CALLER_STAYINGTIME_DIST_LONG:
		case DATA_TYPE_CALLER_CANCELTIME_DIST_LONG:
		case DATA_TYPE_CLIENT_WAITINGTIME_DIST_LONG:
		case DATA_TYPE_CLIENT_STAYINGTIME_DIST_LONG:
		case DATA_TYPE_CLIENT_CANCELTIME_DIST_LONG:
			return getLongTimeArray();

		default:
			return new String[0];
		}
	}

	/**
	 * Erzeugt die Tabelle
	 * @param statistic	<code>ComplexStatisticSimData</code>-Objekt, dem die Kundendaten entnommen werden sollen
	 * @param rowNames	Namen der einzelnen Zeilen (stehen in der ersten Spalte)
	 */
	private void buildClientTable(Statistics statistic, String[] rowNames) {
		setData(getTableData(statistic,rowNames),getTableHeading(statistic));
	}

	/**
	 * Liefert eine Spalte der Tabelle
	 * @param kunden	<code>KundenDaten</code>-Element, dessen Daten verwendet werden sollen
	 * @param days	Anzahl der simulierten Tage
	 * @return	Gibt die Tabellenspalte (ohne Überschrift) zurück
	 */
	private List<String> getLine(KundenDaten kunden, long days) {
		List<String> line=new ArrayList<String>();

		double[] d;
		long l;
		double m;

		switch (dataType) {
		case DATA_TYPE_COUNT:
			line.add(addCell(kunden.kunden-kunden.kundenUebertrag,days));
			line.add(addCell(kunden.kundenWiederanruf,days));
			line.add(addCell(kunden.anrufeWiederholungen,days));
			line.add(addCell(kunden.anrufe-kunden.anrufeUebertrag,days));

			line.add(addCell(kunden.anrufeBlocked,days));
			line.add(addPercentCellParts(kunden.anrufeBlocked,kunden.anrufe-kunden.anrufeUebertrag));
			line.add(addCell(kunden.anrufeAbbruch,days));
			line.add(addPercentCellParts(kunden.anrufeAbbruch,kunden.anrufe-kunden.anrufeUebertrag));

			line.add(addCell(kunden.anrufeUebertrag,days));
			line.add(addPercentCellParts(kunden.anrufeUebertrag,kunden.anrufe-kunden.anrufeUebertrag));

			line.add(addCell(kunden.kundenBlocked+kunden.kundenAbbruch,days));
			line.add(addPercentCellParts(kunden.kundenBlocked+kunden.kundenAbbruch,kunden.kunden+kunden.kundenWiederanruf-kunden.kundenUebertrag));

			line.add(addCell(kunden.anrufeWeiterleitungen,days));
			line.add(addPercentCellParts(kunden.anrufeWeiterleitungen,kunden.anrufe-kunden.anrufeUebertrag));
			line.add(addCell(kunden.kundenWeiterleitungen,days));
			line.add(addPercentCellParts(kunden.kundenWeiterleitungen,kunden.kunden+kunden.kundenWiederanruf-kunden.kundenUebertrag));

			line.add(addCell(kunden.anrufeErfolg,days));
			line.add(addPercentCellParts(kunden.anrufeErfolg,kunden.anrufe-kunden.anrufeUebertrag));
			line.add(addCell(kunden.kundenErfolg,days));
			line.add(addPercentCellParts(kunden.kundenErfolg,kunden.kunden+kunden.kundenWiederanruf-kunden.kundenUebertrag));
			break;

		case DATA_TYPE_SUCCESS:
			line.add(addPercentCellParts(kunden.anrufeErfolg,kunden.anrufe-kunden.anrufeUebertrag));
			line.add(addPercentCellParts(kunden.kundenErfolg,kunden.kunden+kunden.kundenWiederanruf-kunden.kundenUebertrag));
			break;

		case DATA_TYPE_SERVICE_LEVEL:
			line.add(addPercentCellParts(kunden.anrufeServicelevel,kunden.anrufeErfolg));
			line.add(addPercentCellParts(kunden.anrufeServicelevel,kunden.anrufe));
			line.add(addPercentCellParts(kunden.kundenServicelevel,kunden.kundenErfolg));
			line.add(addPercentCellParts(kunden.kundenServicelevel,kunden.kunden));
			break;

		case DATA_TYPE_WAITINGTIME:
			m=(double)kunden.anrufeWartezeitSum/Math.max(1,kunden.anrufeErfolg);
			l=calcStd(kunden.anrufeWartezeitSum2,kunden.anrufeWartezeitSum,kunden.anrufeErfolg);
			line.add(TimeTools.formatTime((int)Math.round(m)));
			line.add(TimeTools.formatTime((int)l));
			line.add(NumberTools.formatNumber(l/Math.max(1,m)));

			m=(double)kunden.anrufeVerweilzeitSum/Math.max(1,kunden.anrufeErfolg);
			l=calcStd(kunden.anrufeVerweilzeitSum2,kunden.anrufeVerweilzeitSum,kunden.anrufeErfolg);
			line.add(TimeTools.formatTime((int)Math.round(m)));
			line.add(TimeTools.formatTime((int)l));
			line.add(NumberTools.formatNumber(l/Math.max(1,m)));

			m=(double)kunden.anrufeAbbruchzeitSum/Math.max(1,kunden.anrufeAbbruch);
			l=calcStd(kunden.anrufeAbbruchzeitSum2,kunden.anrufeAbbruchzeitSum,kunden.anrufeAbbruch);
			line.add(TimeTools.formatTime((int)Math.round(m)));
			line.add(TimeTools.formatTime((int)l));
			line.add(NumberTools.formatNumber(l/Math.max(1,m)));

			m=(double)kunden.kundenWartezeitSum/Math.max(1,kunden.kundenErfolg);
			l=calcStd(kunden.kundenWartezeitSum2,kunden.kundenWartezeitSum,kunden.kundenErfolg);
			line.add(TimeTools.formatTime((int)Math.round(m)));
			line.add(TimeTools.formatTime((int)l));
			line.add(NumberTools.formatNumber(l/Math.max(1,m)));

			m=(double)kunden.kundenVerweilzeitSum/Math.max(1,kunden.kundenErfolg);
			l=calcStd(kunden.kundenVerweilzeitSum2,kunden.kundenVerweilzeitSum,kunden.kundenErfolg);
			line.add(TimeTools.formatTime((int)Math.round(m)));
			line.add(TimeTools.formatTime((int)l));
			line.add(NumberTools.formatNumber(l/Math.max(1,m)));

			m=(double)kunden.kundenAbbruchzeitSum/Math.max(1,kunden.kundenAbbruch);
			l=calcStd(kunden.kundenAbbruchzeitSum2,kunden.kundenAbbruchzeitSum,kunden.kundenAbbruch);
			line.add(TimeTools.formatTime((int)Math.round(m)));
			line.add(TimeTools.formatTime((int)l));
			line.add(NumberTools.formatNumber(l/Math.max(1,m)));
			break;

		case DATA_TYPE_CALLER_WAITINGTIME_DIST:
			d=kunden.anrufeWartezeitVerteilung.densityData;
			for (int i=0;i<KundenDaten.DistMax;i++) line.add(NumberTools.formatNumber(d[i]/days,3));
			break;

		case DATA_TYPE_CALLER_STAYINGTIME_DIST:
			d=kunden.anrufeVerweilzeitVerteilung.densityData;
			for (int i=0;i<KundenDaten.DistMax;i++) line.add(NumberTools.formatNumber(d[i]/days,3));
			break;

		case DATA_TYPE_CALLER_CANCELTIME_DIST:
			d=kunden.anrufeAbbruchzeitVerteilung.densityData;
			for (int i=0;i<KundenDaten.DistMax;i++) line.add(NumberTools.formatNumber(d[i]/days,3));
			break;

		case DATA_TYPE_CLIENT_WAITINGTIME_DIST:
			d=kunden.kundenWartezeitVerteilung.densityData;
			for (int i=0;i<KundenDaten.DistMax;i++) line.add(NumberTools.formatNumber(d[i]/days,3));
			break;

		case DATA_TYPE_CLIENT_STAYINGTIME_DIST:
			d=kunden.kundenVerweilzeitVerteilung.densityData;
			for (int i=0;i<KundenDaten.DistMax;i++) line.add(NumberTools.formatNumber(d[i]/days,3));
			break;

		case DATA_TYPE_CLIENT_CANCELTIME_DIST:
			d=kunden.kundenAbbruchzeitVerteilung.densityData;
			for (int i=0;i<KundenDaten.DistMax;i++) line.add(NumberTools.formatNumber(d[i]/days,3));
			break;

		case DATA_TYPE_CALLER_WAITINGTIME_DIST_LONG:
			d=kunden.anrufeWartezeitVerteilungLang.densityData;
			for (int i=0;i<KundenDaten.DistMax;i++) line.add(NumberTools.formatNumber(d[i]/days,3));
			break;

		case DATA_TYPE_CALLER_STAYINGTIME_DIST_LONG:
			d=kunden.anrufeVerweilzeitVerteilungLang.densityData;
			for (int i=0;i<KundenDaten.DistMax;i++) line.add(NumberTools.formatNumber(d[i]/days,3));
			break;

		case DATA_TYPE_CALLER_CANCELTIME_DIST_LONG:
			d=kunden.anrufeAbbruchzeitVerteilungLang.densityData;
			for (int i=0;i<KundenDaten.DistMax;i++) line.add(NumberTools.formatNumber(d[i]/days,3));
			break;

		case DATA_TYPE_CLIENT_WAITINGTIME_DIST_LONG:
			d=kunden.kundenWartezeitVerteilungLang.densityData;
			for (int i=0;i<KundenDaten.DistMax;i++) line.add(NumberTools.formatNumber(d[i]/days,3));
			break;

		case DATA_TYPE_CLIENT_STAYINGTIME_DIST_LONG:
			d=kunden.kundenVerweilzeitVerteilungLang.densityData;
			for (int i=0;i<KundenDaten.DistMax;i++) line.add(NumberTools.formatNumber(d[i]/days,3));
			break;

		case DATA_TYPE_CLIENT_CANCELTIME_DIST_LONG:
			d=kunden.kundenAbbruchzeitVerteilungLang.densityData;
			for (int i=0;i<KundenDaten.DistMax;i++) line.add(NumberTools.formatNumber(d[i]/days,3));
			break;
		}
		return line;
	}

	private Table getTableData(Statistics statistic,String[] rowNames) {
		Table table=new Table();

		table.addLine(rowNames);
		for (int i=0;i<statistic.kundenProTyp.length;i++) table.addLine(getLine(statistic.kundenProTyp[i],statistic.simDays));
		table.addLine(getLine(statistic.kundenGlobal,statistic.simDays));

		return table.transpose();
	}

	private String[] getTableHeading(Statistics statistic) {
		String[] head=new String[statistic.kundenProTyp.length+2];
		head[0]="";
		for (int i=0;i<statistic.kundenProTyp.length;i++) head[i+1]=statistic.kundenProTyp[i].name;
		switch (dataType) {
		case DATA_TYPE_WAITINGTIME:
		case DATA_TYPE_SUCCESS:
		case DATA_TYPE_SERVICE_LEVEL:
			head[head.length-1]=Language.tr("Statistic.Average");
			break;
		default:
			head[head.length-1]=Language.tr("Statistic.Total");
			break;
		}

		return head;
	}
}