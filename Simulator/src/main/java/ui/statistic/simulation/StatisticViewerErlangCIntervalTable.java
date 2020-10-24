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
import simulator.Statistics;
import ui.statistic.model.StatisticViewerErlangCTools;

/**
 * Klasse zur Anzeige von Vergleichsdaten zwischen Simulation und Erlang-C-Daten
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerErlangCIntervalTable extends StatisticViewerIntervalTable {
	/** Darstellungsart, siehe <code>DATA_*</code> Konstanten. */
	private final Mode dataType;
	/** Objekt vom Typ {@link Statistics}, dem die Daten entnommen werden sollen */
	private final Statistics statistic;
	private final StatisticViewerErlangCTools erlangC1;
	private final StatisticViewerErlangCTools erlangC2;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerErlangCIntervalTable#StatisticViewerErlangCIntervalTable(Statistics, Mode)
	 */
	public enum Mode {
		/**
		 * Anzeige der Erreichbarkeit
		 */
		DATA_SUCCESS,

		/**
		 * Anzeige der mittleren Wartezeit
		 */
		DATA_WAITING_TIME,

		/**
		 * Anzeige des Service-Levels
		 */
		DATA_SERVICE_LEVEL
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerErlangCIntervalTable</code>
	 * @param statistic	Objekt vom Typ {@link Statistics}, dem die Daten entnommen werden sollen
	 * @param dataType	Darstellungsart, siehe <code>DATA_*</code> Konstanten.
	 */
	public StatisticViewerErlangCIntervalTable(Statistics statistic, Mode dataType) {
		super();
		this.dataType=dataType;
		this.statistic=statistic;
		erlangC1=new StatisticViewerErlangCTools(statistic.editModel,false);
		erlangC2=new StatisticViewerErlangCTools(statistic.editModel,true);
	}

	private final String[] getTitle() {
		String value="";
		switch (dataType) {
		case DATA_SUCCESS : value=Language.tr("SimStatistic.Accessibility"); break;
		case DATA_WAITING_TIME : value=Language.tr("SimStatistic.AverageWaitingTime"); break;
		case DATA_SERVICE_LEVEL : value=Language.tr("SimStatistic.ServiceLevel"); break;
		}

		return new String[]{
				value+" ("+Language.tr("SimStatistic.Type.ErlangCSimple")+")",
				value+" ("+Language.tr("SimStatistic.Type.ErlangCComplex")+")",
				value+" ("+Language.tr("SimStatistic.Type.Simulation")+")",
				Language.tr("SimStatistic.Error")+" "+Language.tr("SimStatistic.Type.ErlangCSimple")+" - "+Language.tr("SimStatistic.Type.Simulation"),
				Language.tr("SimStatistic.Error")+" "+Language.tr("SimStatistic.Type.ErlangCComplex")+" - "+Language.tr("SimStatistic.Type.Simulation")
		};
	}

	@Override
	protected void buildTable() {
		buildIntervalTable(statistic,getTitle(),null);
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.statistic.simulation.StatisticViewerIntervalTable#getUserCol(complexcallcenter.simulator.ComplexStatisticSimData, int)
	 */
	@Override
	protected String[] getUserCol(Statistics statistic, int colNr) {
		double[] erlang1, erlang2, sim;
		switch (dataType) {
		case DATA_SUCCESS:
			erlang1=erlangC1.getSuccessProbability();
			erlang2=erlangC1.getSuccessProbability();
			sim=statistic.kundenGlobal.anrufeErfolgProIntervall.divide(statistic.kundenGlobal.anrufeProIntervall).densityData;
			break;
		case DATA_WAITING_TIME:
			erlang1=erlangC1.getMeanWaitingTime();
			erlang2=erlangC2.getMeanWaitingTime();
			sim=statistic.kundenGlobal.anrufeWartezeitSumProIntervall.divide(statistic.kundenGlobal.anrufeErfolgProIntervall).densityData;
			break;
		case DATA_SERVICE_LEVEL:
			erlang1=erlangC1.getServiceLevel();
			erlang2=erlangC2.getServiceLevel();
			sim=statistic.kundenGlobal.anrufeServicelevelProIntervall.divide(statistic.kundenGlobal.anrufeErfolgProIntervall).densityData;
			break;
		default:
			return null;
		}

		double[] data;
		switch (colNr) {
		case 0: data=erlang1; break;
		case 1: data=erlang2; break;
		case 2: data=sim; break;
		case 3: data=new double[48]; for (int i=0;i<48;i++) data[i]=erlang1[i]-sim[i]; break;
		case 4: data=new double[48]; for (int i=0;i<48;i++) data[i]=erlang2[i]-sim[i]; break;
		default: return null;
		}

		String[] result=new String[48];
		for (int i=0;i<48;i++) if (dataType==Mode.DATA_WAITING_TIME) {
			result[i]=TimeTools.formatExactTime(data[i]);
		} else {
			result[i]=NumberTools.formatPercent(data[i],1);
		}

		return result;
	}
}