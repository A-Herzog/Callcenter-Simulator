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

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import simulator.Statistics;
import simulator.Statistics.KundenDaten;

/**
 * Klasse zur Anzeige einer Tabelle mit Kunden-spezifischen Statistikdaten auf Intervallbasis
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerKundenIntervalTable extends StatisticViewerIntervalTable {
	/** Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten. */
	private final Mode dataType;
	private final long days;
	/** Objekt vom Typ {@link Statistics}, dem die Kundendaten entnommen werden sollen */
	private final Statistics statistic;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerKundenIntervalTable#StatisticViewerKundenIntervalTable(Statistics, Mode)
	 */
	public enum Mode {

		/**
		 * Anrufer auflisten.
		 */
		DATA_TYPE_CALLS,

		/**
		 * Weitergeleitete Anrufe auflisten.
		 */
		DATA_TYPE_CALLS_CONTINUE,

		/**
		 * Anrufwiederholungen auflisten.
		 */
		DATA_TYPE_CALLS_RETRIED,

		/**
		 * Anruf-Abbrüche auflisten.
		 */
		DATA_TYPE_CALLS_CANCEL,

		/**
		 * Erfolgreiche Anrufe auflisten.
		 */
		DATA_TYPE_CALLS_SUCCESS,

		/**
		 * Anteil der weitergeleiteten Anrufe auflisten.
		 */
		DATA_TYPE_CALLS_CONTINUE_PART,

		/**
		 * Anteil der Anruf-Abbrüche auflisten.
		 */
		DATA_TYPE_CALLS_CANCEL_PART,

		/**
		 * Anteil der erfolgreichen Anrufe auflisten.
		 */
		DATA_TYPE_CALLS_SUCCESS_PART,

		/**
		 * Service-Level auf Anrufbasis (bezogen auf erfolgreiche Anrufe) anzeigen.
		 */
		DATA_TYPE_CALLS_SERVICE_LEVEL,

		/**
		 * Service-Level auf Anrufbasis (bezogen auf alle Anrufe) anzeigen.
		 */
		DATA_TYPE_CALLS_SERVICE_LEVEL_ALL,

		/**
		 * Wartezeitverteilung auf Anrufbasis anzeigen.
		 */
		DATA_TYPE_CALLS_WAITINGTIME,

		/**
		 * Verteilzeitverteilung auf Anrufbasis anzeigen.
		 */
		DATA_TYPE_CALLS_STAYINGTIME,

		/**
		 * Abbruchzeitverteilung auf Anrufbasis anzeigen.
		 */
		DATA_TYPE_CALLS_CANCELTIME,

		/**
		 * Kunden auflisten.
		 */
		DATA_TYPE_CLIENTS,

		/**
		 * Weitergeleitete Kunden auflisten.
		 */
		DATA_TYPE_CLIENTS_CONTINUE,

		/**
		 * Wiederholungen auf Kundenbasis auflisten.
		 */
		DATA_TYPE_CLIENTS_RETRIED,

		/**
		 * Aufgegebene Kunden auflisten.
		 */
		DATA_TYPE_CLIENTS_CANCEL,

		/**
		 * Erfolgreiche Kunden auflisten.
		 */
		DATA_TYPE_CLIENTS_SUCCESS,

		/**
		 * Anteil der weitergeleiteten Kunden auflisten.
		 */
		DATA_TYPE_CLIENTS_CONTINUE_PART,

		/**
		 * Anteil der aufgegebenen Kunden auflisten.
		 */
		DATA_TYPE_CLIENTS_CANCEL_PART,

		/**
		 * Anteil der erfolgreichen Kunden auflisten.
		 */
		DATA_TYPE_CLIENTS_SUCCESS_PART,

		/**
		 * Service-Level auf Kundenbasis (bezogen auf erfolgreiche Kunden) anzeigen.
		 */
		DATA_TYPE_CLIENTS_SERVICE_LEVEL,

		/**
		 * Service-Level auf Kundenbasis (bezogen auf alle Kunden) anzeigen.
		 */
		DATA_TYPE_CLIENTS_SERVICE_LEVEL_ALL,

		/**
		 * Wartezeitverteilung auf Kundenbasis anzeigen.
		 */
		DATA_TYPE_CLIENTS_WAITINGTIME,

		/**
		 * Verweilzeitverteilung auf Kundenbasis anzeigen.
		 */
		DATA_TYPE_CLIENTS_STAYINGTIME,

		/**
		 * Abbruchzeitverteilung auf Kundenbasis anzeigen.
		 */
		DATA_TYPE_CLIENTS_CANCELTIME,

		/**
		 * Wiederanrufer auflisten.
		 */
		DATA_TYPE_RECALLS
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerKundenIntervalTable</code>
	 * @param statistic	Objekt vom Typ {@link Statistics}, dem die Kundendaten entnommen werden sollen
	 * @param dataType	Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten.
	 */
	public StatisticViewerKundenIntervalTable(Statistics statistic, Mode dataType) {
		super();
		this.dataType=dataType;
		this.statistic=statistic;
		days=statistic.simulationData.runRepeatCount;
	}

	@Override
	protected void buildTable() {
		String sumRow;
		switch (dataType) {
		case DATA_TYPE_CALLS: sumRow=Language.tr("Statistic.Sum"); break;
		case DATA_TYPE_CALLS_CONTINUE: sumRow=Language.tr("Statistic.Sum"); break;
		case DATA_TYPE_CALLS_RETRIED: sumRow=Language.tr("Statistic.Sum"); break;
		case DATA_TYPE_CALLS_CANCEL: sumRow=Language.tr("Statistic.Sum"); break;
		case DATA_TYPE_CALLS_SUCCESS: sumRow=Language.tr("Statistic.Sum"); break;
		case DATA_TYPE_CALLS_CONTINUE_PART: sumRow=Language.tr("Statistic.Average"); break;
		case DATA_TYPE_CALLS_CANCEL_PART: sumRow=Language.tr("Statistic.Average"); break;
		case DATA_TYPE_CALLS_SUCCESS_PART: sumRow=Language.tr("Statistic.Average"); break;
		case DATA_TYPE_CALLS_SERVICE_LEVEL: sumRow=Language.tr("Statistic.Average"); break;
		case DATA_TYPE_CALLS_SERVICE_LEVEL_ALL: sumRow=Language.tr("Statistic.Average"); break;
		case DATA_TYPE_CALLS_WAITINGTIME: sumRow=Language.tr("Statistic.Average"); break;
		case DATA_TYPE_CALLS_STAYINGTIME: sumRow=Language.tr("Statistic.Average"); break;
		case DATA_TYPE_CALLS_CANCELTIME: sumRow=Language.tr("Statistic.Average"); break;
		case DATA_TYPE_CLIENTS: sumRow=Language.tr("Statistic.Sum"); break;
		case DATA_TYPE_CLIENTS_CONTINUE: sumRow=Language.tr("Statistic.Sum"); break;
		case DATA_TYPE_CLIENTS_RETRIED: sumRow=Language.tr("Statistic.Sum"); break;
		case DATA_TYPE_CLIENTS_CANCEL: sumRow=Language.tr("Statistic.Sum"); break;
		case DATA_TYPE_CLIENTS_SUCCESS: sumRow=Language.tr("Statistic.Sum"); break;
		case DATA_TYPE_CLIENTS_CONTINUE_PART: sumRow=Language.tr("Statistic.Average"); break;
		case DATA_TYPE_CLIENTS_CANCEL_PART: sumRow=Language.tr("Statistic.Average"); break;
		case DATA_TYPE_CLIENTS_SUCCESS_PART: sumRow=Language.tr("Statistic.Average"); break;
		case DATA_TYPE_CLIENTS_SERVICE_LEVEL: sumRow=Language.tr("Statistic.Average"); break;
		case DATA_TYPE_CLIENTS_SERVICE_LEVEL_ALL: sumRow=Language.tr("Statistic.Average"); break;
		case DATA_TYPE_CLIENTS_WAITINGTIME: sumRow=Language.tr("Statistic.Average"); break;
		case DATA_TYPE_CLIENTS_STAYINGTIME: sumRow=Language.tr("Statistic.Average"); break;
		case DATA_TYPE_CLIENTS_CANCELTIME: sumRow=Language.tr("Statistic.Average"); break;
		case DATA_TYPE_RECALLS: sumRow=Language.tr("Statistic.Sum"); break;
		default: sumRow=null;
		}

		buildIntervalTable(statistic,getKundenCols(statistic),sumRow);
	}

	private String[] getKundenCols(Statistics statistic) {
		String[] cols=new String[statistic.kundenProTyp.length+1];
		for (int i=0;i<cols.length-1;i++) cols[i]=statistic.kundenProTyp[i].name;
		switch (dataType) {
		case DATA_TYPE_CLIENTS_WAITINGTIME:
		case DATA_TYPE_CALLS_WAITINGTIME:
		case DATA_TYPE_CLIENTS_STAYINGTIME:
		case DATA_TYPE_CALLS_STAYINGTIME:
		case DATA_TYPE_CLIENTS_CANCELTIME:
		case DATA_TYPE_CALLS_CANCELTIME:
		case DATA_TYPE_CALLS_SERVICE_LEVEL:
		case DATA_TYPE_CALLS_SERVICE_LEVEL_ALL:
		case DATA_TYPE_CLIENTS_SERVICE_LEVEL:
		case DATA_TYPE_CLIENTS_SERVICE_LEVEL_ALL:
			cols[cols.length-1]=Language.tr("Statistic.Average");
			break;
		default:
			cols[cols.length-1]=Language.tr("Statistic.Total");
			break;
		}
		return cols;
	}

	private String[] getKundenCol(KundenDaten kunden) {
		DataDistributionImpl dist, dist2=null;
		String sum;
		switch (dataType) {
		case DATA_TYPE_CALLS:
			dist=kunden.anrufeProIntervall.divide(days);
			sum=NumberTools.formatNumber((double)kunden.anrufe/days);
			break;
		case DATA_TYPE_CALLS_CONTINUE:
			dist=kunden.anrufeWeiterleitungenProIntervall.divide(days);
			sum=NumberTools.formatNumber((double)kunden.anrufeWeiterleitungen/days);
			break;
		case DATA_TYPE_CALLS_RETRIED:
			dist=kunden.anrufeWiederholungenProIntervall.divide(days);
			sum=NumberTools.formatNumber((double)kunden.anrufeWiederholungen/days);
			break;
		case DATA_TYPE_CALLS_CANCEL:
			dist=kunden.anrufeAbbruchProIntervall.divide(days);
			sum=NumberTools.formatNumber((double)kunden.anrufeAbbruch/days);
			break;
		case DATA_TYPE_CALLS_SUCCESS:
			dist=kunden.anrufeErfolgProIntervall.divide(days);
			sum=NumberTools.formatNumber((double)kunden.anrufeErfolg/days);
			break;
		case DATA_TYPE_CALLS_CONTINUE_PART:
			dist=kunden.anrufeWeiterleitungenProIntervall;
			dist2=kunden.anrufeErfolgProIntervall;
			sum=NumberTools.formatPercent((double)kunden.anrufeWeiterleitungen/kunden.anrufeErfolg);
			break;
		case DATA_TYPE_CALLS_CANCEL_PART:
			dist=kunden.anrufeAbbruchProIntervall;
			dist2=kunden.anrufeProIntervall;
			sum=NumberTools.formatPercent((double)kunden.anrufeAbbruch/kunden.anrufe);
			break;
		case DATA_TYPE_CALLS_SUCCESS_PART:
			dist=kunden.anrufeErfolgProIntervall;
			dist2=kunden.anrufeProIntervall;
			sum=NumberTools.formatPercent((double)kunden.anrufeErfolg/kunden.anrufe);
			break;
		case DATA_TYPE_CALLS_SERVICE_LEVEL:
			dist=kunden.anrufeServicelevelProIntervall;
			dist2=kunden.anrufeErfolgProIntervall;
			sum=NumberTools.formatPercent((double)kunden.anrufeServicelevel/Math.max(1,kunden.anrufeErfolg));
			break;
		case DATA_TYPE_CALLS_SERVICE_LEVEL_ALL:
			dist=kunden.anrufeServicelevelProIntervall;
			dist2=kunden.anrufeProIntervall;
			sum=NumberTools.formatPercent((double)kunden.anrufeServicelevel/Math.max(1,kunden.anrufe));
			break;
		case DATA_TYPE_CALLS_WAITINGTIME:
			dist=kunden.anrufeWartezeitSumProIntervall;
			dist2=kunden.anrufeErfolgProIntervall;
			sum=TimeTools.formatTime((int)Math.round((double)kunden.anrufeWartezeitSum/kunden.anrufeErfolg));
			break;
		case DATA_TYPE_CALLS_STAYINGTIME:
			dist=kunden.anrufeVerweilzeitSumProIntervall;
			dist2=kunden.anrufeErfolgProIntervall;
			sum=TimeTools.formatTime((int)Math.round((double)kunden.anrufeVerweilzeitSum/kunden.anrufeErfolg));
			break;
		case DATA_TYPE_CALLS_CANCELTIME:
			dist=kunden.anrufeAbbruchzeitSumProIntervall;
			dist2=kunden.anrufeAbbruchProIntervall;
			sum=TimeTools.formatTime((int)Math.round((double)kunden.anrufeAbbruchzeitSum/kunden.anrufeAbbruch));
			break;
		case DATA_TYPE_CLIENTS:
			dist=kunden.kundenProIntervall.divide(days);
			sum=NumberTools.formatNumber((double)kunden.kunden/days);
			break;
		case DATA_TYPE_CLIENTS_CONTINUE:
			dist=kunden.kundenWeiterleitungenProIntervall.divide(days);
			sum=NumberTools.formatNumber((double)kunden.kundenWeiterleitungen/days);
			break;
		case DATA_TYPE_CLIENTS_RETRIED:
			dist=kunden.kundenWiederholungenProIntervall.divide(days);
			sum=NumberTools.formatNumber((double)kunden.kundenWiederholungen/days);
			break;
		case DATA_TYPE_CLIENTS_CANCEL:
			dist=kunden.kundenAbbruchProIntervall.divide(days);
			sum=NumberTools.formatNumber((double)kunden.kundenAbbruch/days);
			break;
		case DATA_TYPE_CLIENTS_CONTINUE_PART:
			dist=kunden.kundenWeiterleitungenProIntervall;
			dist2=kunden.kundenErfolgProIntervall;
			sum=NumberTools.formatPercent((double)kunden.kundenWeiterleitungen/kunden.kundenErfolg);
			break;
		case DATA_TYPE_CLIENTS_CANCEL_PART:
			dist=kunden.kundenAbbruchProIntervall;
			dist2=kunden.kundenProIntervall;
			sum=NumberTools.formatPercent((double)kunden.kundenAbbruch/kunden.kunden);
			break;
		case DATA_TYPE_CLIENTS_SUCCESS_PART:
			dist=kunden.kundenErfolgProIntervall;
			dist2=kunden.kundenProIntervall;
			sum=NumberTools.formatPercent((double)kunden.kundenErfolg/kunden.kunden);
			break;
		case DATA_TYPE_CLIENTS_SUCCESS:
			dist=kunden.kundenErfolgProIntervall.divide(days);
			sum=NumberTools.formatNumber((double)kunden.kundenErfolg/days);
			break;
		case DATA_TYPE_CLIENTS_SERVICE_LEVEL:
			dist=kunden.kundenServicelevelProIntervall;
			dist2=kunden.kundenErfolgProIntervall;
			sum=NumberTools.formatPercent((double)kunden.kundenServicelevel/Math.max(1,kunden.kundenErfolg));
			break;
		case DATA_TYPE_CLIENTS_SERVICE_LEVEL_ALL:
			dist=kunden.kundenServicelevelProIntervall;
			dist2=kunden.kundenProIntervall;
			sum=NumberTools.formatPercent((double)kunden.kundenServicelevel/Math.max(1,kunden.kunden));
			break;
		case DATA_TYPE_CLIENTS_WAITINGTIME:
			dist=kunden.kundenWartezeitSumProIntervall;
			dist2=kunden.kundenErfolgProIntervall;
			sum=TimeTools.formatExactTime((int)Math.round((double)kunden.kundenWartezeitSum/kunden.kundenErfolg));
			break;
		case DATA_TYPE_CLIENTS_STAYINGTIME:
			dist=kunden.kundenVerweilzeitSumProIntervall;
			dist2=kunden.kundenErfolgProIntervall;
			sum=TimeTools.formatExactTime((int)Math.round((double)kunden.kundenVerweilzeitSum/kunden.kundenErfolg));
			break;
		case DATA_TYPE_CLIENTS_CANCELTIME:
			dist=kunden.kundenAbbruchzeitSumProIntervall;
			dist2=kunden.kundenAbbruchProIntervall;
			sum=TimeTools.formatExactTime((int)Math.round((double)kunden.kundenAbbruchzeitSum/kunden.kundenAbbruch));
			break;
		case DATA_TYPE_RECALLS:
			dist=kunden.kundenWiederanrufProIntervall.divide(days);
			sum=NumberTools.formatNumber((double)kunden.kundenWiederanruf/days);
			break;
		default:
			dist=null;
			sum="";
		}
		String[] col=new String[49];
		for (int i=0;i<48;i++) {
			double value=(dist==null)?0:dist.densityData[i];
			if (dist2!=null && dist2.densityData[i]!=0) value/=dist2.densityData[i];
			String s;
			switch (dataType) {
			case DATA_TYPE_CALLS_CONTINUE_PART:
			case DATA_TYPE_CALLS_CANCEL_PART:
			case DATA_TYPE_CALLS_SUCCESS_PART:
			case DATA_TYPE_CALLS_SERVICE_LEVEL:
			case DATA_TYPE_CALLS_SERVICE_LEVEL_ALL:
			case DATA_TYPE_CLIENTS_CONTINUE_PART:
			case DATA_TYPE_CLIENTS_CANCEL_PART:
			case DATA_TYPE_CLIENTS_SUCCESS_PART:
			case DATA_TYPE_CLIENTS_SERVICE_LEVEL:
			case DATA_TYPE_CLIENTS_SERVICE_LEVEL_ALL:
				s=NumberTools.formatPercent(value);
				break;
			case DATA_TYPE_CALLS_WAITINGTIME:
			case DATA_TYPE_CLIENTS_WAITINGTIME:
			case DATA_TYPE_CALLS_STAYINGTIME:
			case DATA_TYPE_CLIENTS_STAYINGTIME:
			case DATA_TYPE_CALLS_CANCELTIME:
			case DATA_TYPE_CLIENTS_CANCELTIME:
				s=TimeTools.formatTime((int)Math.round(value));
				break;
			default:
				s=NumberTools.formatNumber(value);
			}
			col[i]=s;
		}
		col[48]=sum;
		return col;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.statistic.simulation.StatisticViewerIntervalTable#getUserCol(complexcallcenter.simulator.ComplexStatisticSimData, int)
	 */
	@Override
	protected String[] getUserCol(Statistics statistic, int colNr) {
		return (colNr<statistic.kundenProTyp.length)?getKundenCol(statistic.kundenProTyp[colNr]):getKundenCol(statistic.kundenGlobal);
	}

}
