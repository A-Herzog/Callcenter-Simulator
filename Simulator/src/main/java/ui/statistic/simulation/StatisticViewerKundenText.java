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

import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;

import language.Language;
import simulator.Statistics;
import simulator.Statistics.KundenDaten;
import systemtools.statistics.StatisticViewerText;
import tools.SetupData;
import ui.editor.BaseEditDialog;
import ui.help.Help;
import ui.images.Images;
import ui.statistic.core.StatisticFilterDialog;

/**
 * Zeigt Informationen auf Kundentyp-Basis an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerKundenText extends StatisticViewerText {
	private final Statistics statistic;
	private final Mode dataType;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerKundenText#StatisticViewerKundenText(Statistics, Mode)
	 */
	public enum Mode {
		/**
		 * Liefert die Anzahl an Anrufern.
		 */
		DATA_TYPE_COUNT,

		/**
		 * Liefert die Warte- und Abbruchzeiten.
		 */
		DATA_TYPE_WAITINGTIME,

		/**
		 * Liefert die Erreichbarkeit.
		 */
		DATA_TYPE_SUCCESS,

		/**
		 * Liefert den Service-Level.
		 */
		DATA_TYPE_SERVICE_LEVEL
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerKundenText</code>
	 * @param statistic	Objekt vom Typ <code>ComplexStatisticSimData</code>, dem die Kundendaten entnommen werden sollen
	 * @param dataType	Darstellungsart
	 */
	public StatisticViewerKundenText(Statistics statistic, Mode dataType) {
		super();
		this.statistic=statistic;
		this.dataType=dataType;
	}

	/*
	private void addDescription(final String topic) {
		final URL url=StatisticViewerKundenText.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}
	 */

	@Override
	protected void buildText() {
		switch (dataType) {
		case DATA_TYPE_COUNT: addHeading(1,Language.tr("SimStatistic.NumberOfCallers")); break;
		case DATA_TYPE_WAITINGTIME: addHeading(1,Language.tr("SimStatistic.WaitingAndResidenceTimes")); break;
		case DATA_TYPE_SUCCESS: addHeading(1,Language.tr("SimStatistic.Accessibility")); break;
		case DATA_TYPE_SERVICE_LEVEL: addHeading(1,Language.tr("SimStatistic.ServiceLevel")); break;
		}

		int serviceLevel=statistic.editModel.serviceLevelSeconds;
		if (statistic.editModel.caller.get(0).serviceLevelSeconds>0) serviceLevel=statistic.editModel.caller.get(0).serviceLevelSeconds;
		for (int i=1;i<statistic.kundenProTyp.length;i++) {
			int sl=statistic.editModel.caller.get(i).serviceLevelSeconds;
			if (sl<=0) sl=statistic.editModel.serviceLevelSeconds;
			if (sl!=serviceLevel) {serviceLevel=-1; break;}
		}

		boolean needDetailedData=(statistic.kundenProTyp.length>1);
		if (!needDetailedData && dataType==Mode.DATA_TYPE_SERVICE_LEVEL && statistic.editModel.caller.get(0).serviceLevelSeconds>0) needDetailedData=true;

		buildClientData(statistic.kundenGlobal,(int)statistic.simulationData.runRepeatCount,serviceLevel);
		if (needDetailedData) for (int i=0;i<statistic.kundenProTyp.length;i++) {
			int sl=(statistic.editModel.caller.get(i).serviceLevelSeconds>0)?(statistic.editModel.caller.get(i).serviceLevelSeconds):(statistic.editModel.serviceLevelSeconds);
			buildClientData(statistic.kundenProTyp[i],(int)statistic.simulationData.runRepeatCount,sl);
		}

		/* Infotext  */
		/*
		Bringen keinen wirklich Mehrwert.
		switch (dataType) {
		case DATA_TYPE_COUNT: addDescription("ClientCount"); break;
		case DATA_TYPE_WAITINGTIME: addDescription("ClientWaitingAndResidenceTimes"); break;
		case DATA_TYPE_SUCCESS: addDescription("ClientAccessibility"); break;
		case DATA_TYPE_SERVICE_LEVEL: addDescription("ClientServiceLevel"); break;
		}
		 */
	}

	private void buildClientData(KundenDaten kunden, int days, int serviceLevel) {
		if (kunden.name.isEmpty()) addHeading(2,Language.tr("SimStatistic.AllClients")); else addHeading(2,kunden.name);

		long l;
		String hiddenSetup;

		switch (dataType) {
		case DATA_TYPE_COUNT:
			hiddenSetup=SetupData.getSetup().clientsCountFilter;
			beginParagraph();
			if (isIDVisible(hiddenSetup,FILTER_COUNT_FRESHCALLS)) addLineDiv(1,Language.tr("SimStatistic.FreshCalls"),kunden.kunden-kunden.kundenUebertrag,days);
			if (isIDVisible(hiddenSetup,FILTER_COUNT_RECALLINGCLIENTS) && kunden.kundenWiederanruf>0) addLineDiv(1,Language.tr("SimStatistic.RecallingClients.Info"),kunden.kundenWiederanruf,days);
			if (isIDVisible(hiddenSetup,FILTER_COUNT_CALLS)) addLineDiv(1,Language.tr("SimStatistic.Calls.Info"),kunden.anrufe-kunden.anrufeUebertrag,days);
			if (isIDVisible(hiddenSetup,FILTER_COUNT_CALLSBLOCKED)) addLine(1,Language.tr("SimStatistic.Canceled.CallBlocked"),kunden.anrufeBlocked,days,kunden.anrufe-kunden.anrufeUebertrag);
			if (isIDVisible(hiddenSetup,FILTER_COUNT_CALLSWAITINGCANCELED)) addLine(1,Language.tr("SimStatistic.Canceled.CallWaitingTime"),kunden.anrufeAbbruch,days,kunden.anrufe-kunden.anrufeUebertrag);
			if (isIDVisible(hiddenSetup,FILTER_COUNT_CALLSCARRIEDOVER)) addLine(1,Language.tr("SimStatistic.Canceled.CallCarriedOver"),kunden.anrufeUebertrag,days,kunden.anrufe-kunden.anrufeUebertrag);
			if (isIDVisible(hiddenSetup,FILTER_COUNT_CLIENTSCANCELED)) addLine(1,Language.tr("SimStatistic.Canceled.Client"),kunden.kundenBlocked+kunden.kundenAbbruch,days,kunden.kunden+kunden.kundenWiederanruf-kunden.kundenUebertrag);
			if (isIDVisible(hiddenSetup,FILTER_COUNT_FORWARDED_CALLS)) addLine(1,Language.tr("SimStatistic.ForwardedCalls"),kunden.anrufeWeiterleitungen,days,kunden.anrufe-kunden.anrufeUebertrag);
			if (isIDVisible(hiddenSetup,FILTER_COUNT_FORWARDED_CLIENTS)) addLine(1,Language.tr("SimStatistic.ForwardedClients"),kunden.kundenWeiterleitungen,days,kunden.kunden+kunden.kundenWiederanruf-kunden.kundenUebertrag);
			if (isIDVisible(hiddenSetup,FILTER_COUNT_RETRY_CALL)) addLine(1,Language.tr("SimStatistic.Retryer")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",kunden.anrufeWiederholungen,days,kunden.anrufe-kunden.anrufeUebertrag);
			if (isIDVisible(hiddenSetup,FILTER_COUNT_RETRY_CLIENT)) addLine(1,Language.tr("SimStatistic.Retryer")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",kunden.kundenWiederholungen,days,kunden.kunden+kunden.kundenWiederanruf-kunden.kundenUebertrag);
			if (isIDVisible(hiddenSetup,FILTER_COUNT_SUCCESSFUL_CALL)) addLine(1,Language.tr("SimStatistic.SuccessfulCalls"),kunden.anrufeErfolg,days,kunden.anrufe-kunden.anrufeUebertrag);
			if (isIDVisible(hiddenSetup,FILTER_COUNT_SUCCESSFUL_CLIENT)) addLine(1,Language.tr("SimStatistic.SuccessfulClients"),kunden.kundenErfolg,days,kunden.kunden+kunden.kundenWiederanruf-kunden.kundenUebertrag);
			endParagraph();
			break;
		case DATA_TYPE_WAITINGTIME:
			hiddenSetup=SetupData.getSetup().clientsWaitingTimeFilter;
			if (isIDVisible(hiddenSetup,FILTER_WAITINGTIME_WAITING_CALL)) {
				addHeading(3,Language.tr("SimStatistic.WaitingTimes")+" ("+Language.tr("SimStatistic.OnCallBasis")+")");
				beginParagraph();
				addShortTimeParts(2,Language.tr("Distribution.Mean"),kunden.anrufeWartezeitSum,kunden.anrufeErfolg);
				l=calcStd(kunden.anrufeWartezeitSum2,kunden.anrufeWartezeitSum,kunden.anrufeErfolg);
				addShortTimeParts(2,Language.tr("Distribution.StdDev"),l,1);
				addLine(2,Language.tr("Distribution.CV"),l/Math.max(1,(double)kunden.anrufeWartezeitSum/Math.max(1,kunden.anrufeErfolg)),1);
				endParagraph();
			}
			if (isIDVisible(hiddenSetup,FILTER_WAITINGTIME_RESIDENCE_CALL)) {
				addHeading(3,Language.tr("SimStatistic.ResidenceTimes")+" ("+Language.tr("SimStatistic.OnCallBasis")+")");
				beginParagraph();
				addShortTimeParts(2,Language.tr("Distribution.Mean"),kunden.anrufeVerweilzeitSum,kunden.anrufeErfolg);
				l=calcStd(kunden.anrufeVerweilzeitSum2,kunden.anrufeVerweilzeitSum,kunden.anrufeErfolg);
				addShortTimeParts(2,Language.tr("Distribution.StdDev"),l,1);
				addLine(2,Language.tr("Distribution.CV"),l/Math.max(1,(double)kunden.anrufeVerweilzeitSum/Math.max(1,kunden.anrufeErfolg)),1);
				endParagraph();
			}
			if (isIDVisible(hiddenSetup,FILTER_WAITINGTIME_CANCEL_CALL)) {
				addHeading(3,Language.tr("SimStatistic.CancelTimes")+" ("+Language.tr("SimStatistic.OnCallBasis")+")");
				beginParagraph();
				addShortTimeParts(2,Language.tr("Distribution.Mean"),kunden.anrufeAbbruchzeitSum,kunden.anrufeAbbruch);
				l=calcStd(kunden.anrufeAbbruchzeitSum2,kunden.anrufeAbbruchzeitSum,kunden.anrufeAbbruch);
				addShortTimeParts(2,Language.tr("Distribution.StdDev"),l,1);
				addLine(2,Language.tr("Distribution.CV"),l/Math.max(1,(double)kunden.anrufeAbbruchzeitSum/Math.max(1,kunden.anrufeAbbruch)),1);
				endParagraph();
			}
			if (isIDVisible(hiddenSetup,FILTER_WAITINGTIME_WAITING_CLIENT)) {
				addHeading(3,Language.tr("SimStatistic.WaitingTimes")+" ("+Language.tr("SimStatistic.OnClientBasis")+")");
				beginParagraph();
				addShortTimeParts(2,Language.tr("Distribution.Mean"),kunden.kundenWartezeitSum,kunden.kundenErfolg);
				l=calcStd(kunden.kundenWartezeitSum2,kunden.kundenWartezeitSum,kunden.kundenErfolg);
				addShortTimeParts(2,Language.tr("Distribution.StdDev"),l,1);
				addLine(2,Language.tr("Distribution.CV"),l/Math.max(1,(double)kunden.kundenWartezeitSum/Math.max(1,kunden.kundenErfolg)),1);
				endParagraph();
			}
			if (isIDVisible(hiddenSetup,FILTER_WAITINGTIME_RESIDENCE_CLIENT)) {
				addHeading(3,Language.tr("SimStatistic.ResidenceTimes")+" ("+Language.tr("SimStatistic.OnClientBasis")+")");
				beginParagraph();
				addShortTimeParts(2,Language.tr("Distribution.Mean"),kunden.kundenVerweilzeitSum,kunden.kundenErfolg);
				l=calcStd(kunden.kundenVerweilzeitSum2,kunden.kundenVerweilzeitSum,kunden.kundenErfolg);
				addShortTimeParts(2,Language.tr("Distribution.StdDev"),l,1);
				addLine(2,Language.tr("Distribution.CV"),l/Math.max(1,(double)kunden.kundenVerweilzeitSum/Math.max(1,kunden.kundenErfolg)),1);
				endParagraph();
			}
			if (isIDVisible(hiddenSetup,FILTER_WAITINGTIME_CANCEL_CLIENT)) {
				addHeading(3,Language.tr("SimStatistic.CancelTimes")+" ("+Language.tr("SimStatistic.OnClientBasis")+")");
				beginParagraph();
				addShortTimeParts(2,Language.tr("Distribution.Mean"),kunden.kundenAbbruchzeitSum,kunden.kundenAbbruch);
				l=calcStd(kunden.kundenAbbruchzeitSum2,kunden.kundenAbbruchzeitSum,kunden.kundenAbbruch);
				addShortTimeParts(2,Language.tr("Distribution.StdDev"),l,1);
				addLine(2,Language.tr("Distribution.CV"),l/Math.max(1,(double)kunden.kundenAbbruchzeitSum/Math.max(1,kunden.kundenAbbruch)),1);
				endParagraph();
			}
			break;
		case DATA_TYPE_SUCCESS:
			beginParagraph();
			addPercentLineParts(1,Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",kunden.anrufeErfolg,kunden.anrufe-kunden.anrufeUebertrag);
			addPercentLineParts(1,Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",kunden.kundenErfolg,kunden.kunden-kunden.kundenUebertrag);
			endParagraph();
			break;
		case DATA_TYPE_SERVICE_LEVEL:
			String serviceLevelText="P(W<="+serviceLevel+")";
			if (serviceLevel<0) serviceLevelText="("+Language.tr("SimStatistic.DifferentServiceLevelValuesPerClientType")+")";
			beginParagraph();
			addPercentLineParts(1,Language.tr("SimStatistic.ServiceLevel")+" "+Language.tr("SimStatistic.OnCallBasis")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+") "+serviceLevelText,kunden.anrufeServicelevel,kunden.anrufeErfolg);
			addPercentLineParts(1,Language.tr("SimStatistic.ServiceLevel")+" "+Language.tr("SimStatistic.OnClientBasis")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+") "+serviceLevelText,kunden.kundenServicelevel,kunden.kundenErfolg);
			addPercentLineParts(1,Language.tr("SimStatistic.ServiceLevel")+" "+Language.tr("SimStatistic.OnCallBasis")+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+") "+serviceLevelText,kunden.anrufeServicelevel,kunden.anrufe);
			addPercentLineParts(1,Language.tr("SimStatistic.ServiceLevel")+" "+Language.tr("SimStatistic.OnClientBasis")+" ("+Language.tr("SimStatistic.CalculatedOn.AllClients")+") "+serviceLevelText,kunden.kundenServicelevel,kunden.kunden);
			endParagraph();
			break;
		}
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.statistic.core.viewers.StatisticViewer#ownSettingsName()
	 */
	@Override
	public String ownSettingsName() {return (dataType==Mode.DATA_TYPE_COUNT || dataType==Mode.DATA_TYPE_WAITINGTIME)?Language.tr("Statistic.Filter.Title"):null;}

	/* (non-Javadoc)
	 * @see complexcallcenter.statistic.core.viewers.StatisticViewer#ownSettingsIcon()
	 */
	@Override
	public Icon ownSettingsIcon() {return (dataType==Mode.DATA_TYPE_COUNT || dataType==Mode.DATA_TYPE_WAITINGTIME)?Images.STATISTICS_FILTER.getIcon():null;}

	private final static int FILTER_COUNT_FRESHCALLS=0;
	private final static int FILTER_COUNT_RECALLINGCLIENTS=1;
	private final static int FILTER_COUNT_CALLS=2;
	private final static int FILTER_COUNT_CALLSBLOCKED=3;
	private final static int FILTER_COUNT_CALLSWAITINGCANCELED=4;
	private final static int FILTER_COUNT_CALLSCARRIEDOVER=5;
	private final static int FILTER_COUNT_CLIENTSCANCELED=6;
	private final static int FILTER_COUNT_FORWARDED_CALLS=7;
	private final static int FILTER_COUNT_FORWARDED_CLIENTS=8;
	private final static int FILTER_COUNT_RETRY_CALL=9;
	private final static int FILTER_COUNT_RETRY_CLIENT=10;
	private final static int FILTER_COUNT_SUCCESSFUL_CALL=11;
	private final static int FILTER_COUNT_SUCCESSFUL_CLIENT=12;

	private String[] getCountIDs() {
		String[] ids=new String[]{
				Language.tr("SimStatistic.FreshCalls"),
				Language.tr("SimStatistic.RecallingClients.Info"),
				Language.tr("SimStatistic.Calls.Info"),
				Language.tr("SimStatistic.Canceled.CallBlocked"),
				Language.tr("SimStatistic.Canceled.CallWaitingTime"),
				Language.tr("SimStatistic.Canceled.CallCarriedOver"),
				Language.tr("SimStatistic.Canceled.Client"),
				Language.tr("SimStatistic.ForwardedCalls"),
				Language.tr("SimStatistic.ForwardedClients"),
				Language.tr("SimStatistic.Retryer")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",
				Language.tr("SimStatistic.Retryer")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",
				Language.tr("SimStatistic.SuccessfulCalls"),
				Language.tr("SimStatistic.SuccessfulClients")
		};
		return ids;
	}

	private final static int FILTER_WAITINGTIME_WAITING_CALL=0;
	private final static int FILTER_WAITINGTIME_RESIDENCE_CALL=1;
	private final static int FILTER_WAITINGTIME_CANCEL_CALL=2;
	private final static int FILTER_WAITINGTIME_WAITING_CLIENT=3;
	private final static int FILTER_WAITINGTIME_RESIDENCE_CLIENT=4;
	private final static int FILTER_WAITINGTIME_CANCEL_CLIENT=5;

	private String[] getWaitingTimeIDs() {
		String[] ids=new String[]{
				Language.tr("SimStatistic.WaitingTimes")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",
				Language.tr("SimStatistic.ResidenceTimes")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",
				Language.tr("SimStatistic.CancelTimes")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",
				Language.tr("SimStatistic.WaitingTimes")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",
				Language.tr("SimStatistic.ResidenceTimes")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",
				Language.tr("SimStatistic.CancelTimes")+" ("+Language.tr("SimStatistic.OnClientBasis")+")"
		};
		return ids;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.statistic.core.viewers.StatisticViewer#ownSettings()
	 */
	@Override
	public boolean ownSettings(JPanel owner) {
		Container c=owner; while (!(c instanceof Window) && c!=null) c=c.getParent();

		SetupData setup;
		String[] ids;
		StatisticFilterDialog dialog;

		switch (dataType) {
		case DATA_TYPE_COUNT:
			setup=SetupData.getSetup();
			ids=getCountIDs();
			dialog=new StatisticFilterDialog((Window)c,Language.tr("SimStatistic.NumberOfCallers.FilterTitle"),ids,getHiddenIDs(ids,setup.clientsCountFilter),()->Help.topicModal(owner,"Statistik"));
			dialog.setVisible(true);
			if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return false;
			setup.clientsCountFilter=setHiddenIDs(ids,dialog.getHiddenIDs());
			setup.saveSetup();
			return true;
		case DATA_TYPE_WAITINGTIME:
			setup=SetupData.getSetup();
			ids=getWaitingTimeIDs();
			dialog=new StatisticFilterDialog((Window)c,Language.tr("SimStatistic.WaitingTimesPerCallerType.FilterTitle"),ids,getHiddenIDs(ids,setup.clientsWaitingTimeFilter),()->Help.topicModal(owner,"Statistik"));
			dialog.setVisible(true);
			if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return false;
			setup.clientsWaitingTimeFilter=setHiddenIDs(ids,dialog.getHiddenIDs());
			setup.saveSetup();
			return true;
		default:
			return false;
		}
	}

	private boolean isIDVisible(String setup, int id) {
		while (setup.length()<id+1) setup+="X";
		boolean b=setup.charAt(id)!='-';
		return b;
	}

	private String[] getHiddenIDs(String[] ids, String setup) {
		if (setup==null) setup="";
		while (setup.length()<ids.length) setup+="X";

		final List<String> hiddenIDs=new ArrayList<String>();
		for (int i=0;i<ids.length;i++) if (setup.charAt(i)=='-') hiddenIDs.add(ids[i]);
		return hiddenIDs.toArray(new String[0]);
	}

	private String setHiddenIDs(String[] ids, String[] hidden) {
		final List<String> hiddenIDs=Arrays.asList(hidden);
		StringBuilder setupString=new StringBuilder();
		for (int i=0;i<ids.length;i++) if (hiddenIDs.contains(ids[i])) setupString.append('-'); else setupString.append('X');
		return setupString.toString();
	}
}
