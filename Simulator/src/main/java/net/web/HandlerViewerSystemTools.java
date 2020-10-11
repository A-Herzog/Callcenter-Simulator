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
package net.web;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.DistributionTools;
import simulator.Statistics;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;
import ui.model.CallcenterModelCaller;
import ui.statistic.simulation.StatisticViewerTextInformation;

/**
 * Diese Klasse stellt statische Methoden für
 * das Webserver-System zur Verfügung.
 * @author Alexander Herzog
 * @see WebServerSystem
 */
public class HandlerViewerSystemTools {
	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden. Sie stellt nur statische Hilfsroutinen zur Verfügung.
	 */
	private HandlerViewerSystemTools() {}

	private static String getServerListEntry(int index, final File xmlFile) {
		Statistics statistic=new Statistics(null,null,0,0);
		String error=statistic.loadFromFile(xmlFile);
		if (error!=null) return null;

		String s1,s2,s3,s4;
		String title=statistic.editModel.name;
		String date=""; if (statistic.editModel.date!=null && !statistic.editModel.date.trim().isEmpty()) date=CallcenterModel.dateToLocalString(CallcenterModel.stringToDate(statistic.editModel.date));
		s1=NumberTools.formatPercent((double)statistic.kundenGlobal.anrufeErfolg/Math.max(1,statistic.kundenGlobal.anrufe-statistic.kundenGlobal.anrufeUebertrag));
		s2=NumberTools.formatPercent((double)statistic.kundenGlobal.kundenErfolg/Math.max(1,statistic.kundenGlobal.kunden+statistic.kundenGlobal.kundenWiederanruf-statistic.kundenGlobal.kundenUebertrag));
		String success=s1+" <small>("+Language.tr("SimStatistic.OnCallBasis")+")</small><br>"+s2+" <small>("+Language.tr("SimStatistic.OnClientBasis")+")</small>";
		s1=TimeTools.formatExactTime((double)statistic.kundenGlobal.anrufeWartezeitSum/Math.max(1,statistic.kundenGlobal.anrufeErfolg));
		s2=TimeTools.formatExactTime((double)statistic.kundenGlobal.kundenWartezeitSum/Math.max(1,statistic.kundenGlobal.kundenErfolg));
		String waitingTime=s1+" <small>("+Language.tr("SimStatistic.OnCallBasis")+")</small><br>"+s2+" <small>("+Language.tr("SimStatistic.OnClientBasis")+")</small>";
		s1=NumberTools.formatPercent((double)statistic.kundenGlobal.anrufeServicelevel/Math.max(1,statistic.kundenGlobal.anrufeErfolg));
		s2=NumberTools.formatPercent((double)statistic.kundenGlobal.anrufeServicelevel/Math.max(1,statistic.kundenGlobal.anrufe));
		s3=NumberTools.formatPercent((double)statistic.kundenGlobal.kundenServicelevel/Math.max(1,statistic.kundenGlobal.kundenErfolg));
		s4=NumberTools.formatPercent((double)statistic.kundenGlobal.kundenServicelevel/Math.max(1,statistic.kundenGlobal.kunden));
		String serviceLevel=s1+" <small>("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")</small><br>"+s2+" <small>("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")</small><br>"+s3+" <small>("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")</small><br>"+s4+" <small>("+Language.tr("SimStatistic.CalculatedOn.AllClients")+")</small><br>";
		String actions="<a href=\"/viewer/"+(index+1)+"/data/"+xmlFile.getName()+"\" target=\"_blank\" class=\"action\">"+Language.tr("Server.WebMenu.ViewerList.Overview")+"</a> <a href=\"/viewer/"+(index+1)+"/"+xmlFile.getName()+"\" target=\"_blank\" class=\"action\">"+Language.tr("Server.WebMenu.ViewerList.Statistic")+"</a>";

		StringBuilder content=new StringBuilder();
		content.append("  <tr>\n");
		if (date!=null && !date.isEmpty()) date="<br><small>"+Language.tr("SimStatistic.ModelInformation.Date")+": <b>"+date+"</b></small>";
		content.append("    <td><b>"+title+"</b>"+date+"</td>\n");
		content.append("    <td>"+xmlFile.getName()+"</td>\n");
		content.append("    <td>"+success+"</td>\n");
		content.append("    <td>"+waitingTime+"</td>\n");
		content.append("    <td>"+serviceLevel+"</td>\n");
		content.append("    <td>"+actions+"</td>\n");
		content.append("  </tr>\n");
		return content.toString();
	}

	/**
	 * Liefert eine Liste mit allen Statistik-Dateien in einem Verzeichnis als html-Datei
	 * @param fileNames	Liste der Dateien, die aufgelistet werden sollen
	 * @param index	Index des Verzeichnisses in der Zählung des Webservers (zur Generierung der URLs zu den einzelnen Modellen)
	 * @param returnNullOnEmpryList	Gibt an, ob <code>null</code> (<code>true</code>) oder eine Meldung (<code>false</code>) im Falle einer leeren Liste zurückgeliefert werden soll.
	 * @return	html-Code, der die List enthält
	 */
	public static String getServerFileList(File[] fileNames, int index, boolean returnNullOnEmpryList) {
		StringBuilder content=new StringBuilder();

		content.append("<style type=\"text/css\">\n");
		content.append("#overviewTable {border-collapse:collapse; margin: 20px 2px;}\n");
		content.append("#overviewTable th {border: 1px solid #333; background-color: #E7E7E7; padding: 3px; 6px;}\n");
		content.append("#overviewTable td {border: 1px solid #333; padding: 3px; 6px;}\n");
		content.append("a.action {display: block; float: left; border: 1px solid #555; border-radius: 1px; padding: 4px; margin: 2px; background-color: #CCC;}\n");
		content.append("</style>\n");

		StringBuilder innercontent=new StringBuilder();
		for (File file: fileNames) {
			String s=getServerListEntry(index,file);
			if (s!=null && !s.isEmpty()) innercontent.append(s);
		}

		if (innercontent.length()==0) {
			if (returnNullOnEmpryList) return null;
			content.append("<p>"+Language.tr("Server.WebMenu.ViewerList.Empty")+"</p>");
		} else {
			content.append("<table id=\"overviewTable\">\n");
			content.append("  <tr>\n");
			content.append("    <th>"+Language.tr("Editor.GeneralData.Name")+"</th>\n");
			content.append("    <th>"+Language.tr("Editor.GeneralData.FileName")+"</th>\n");
			content.append("    <th>"+Language.tr("SimStatistic.Accessibility")+"</th>\n");
			content.append("    <th>"+Language.tr("SimStatistic.AverageWaitingTime")+"</th>\n");
			content.append("    <th>"+Language.tr("SimStatistic.ServiceLevel")+"</th>\n");
			content.append("    <th>"+Language.tr("Editor.GeneralData.Actions")+"</th>\n");
			content.append("  </tr>\n");
			content.append(innercontent);
			content.append("</table>\n");
		}

		return content.toString();
	}

	private static File getFileForDate(int month, File[] files, Date[] dates, int day) {
		Calendar cal=new GregorianCalendar();

		for (int i=0;i<dates.length;i++) if (dates[i]!=null) {
			cal.setTime(dates[i]);
			if (cal.get(Calendar.YEAR)==month/12 && cal.get(Calendar.MONTH)==month%12 && cal.get(Calendar.DATE)==day) return files[i];
		}

		return null;
	}

	private static int firstWeekDayOfMonth(int month) {
		Calendar cal=new GregorianCalendar();
		cal.set(month/12,month%12,1);
		int week=0;
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.MONDAY : week=0; break;
		case Calendar.TUESDAY : week=1; break;
		case Calendar.WEDNESDAY : week=2; break;
		case Calendar.THURSDAY : week=3; break;
		case Calendar.FRIDAY : week=4; break;
		case Calendar.SATURDAY : week=5; break;
		case Calendar.SUNDAY: week=6; break;
		}
		return week;
	}

	private static int getDaysInMonth(int month) {
		Calendar cal=new GregorianCalendar();
		cal.set(month/12,month%12,1);
		return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	private static String getServerDateFileListMonthEntry(int month, File[] files, Date[] dates, int index) {
		StringBuilder content=new StringBuilder();

		content.append("<style type=\"text/css\">\n");
		content.append("#monthTable"+month+" {float: left; width: 250px; border: 1px solid black; 10px; margin: 2px 10px 10px 2px; border-collapse: collapse;}\n");
		content.append("#monthTable"+month+" td {border: 1px solid black; text-align: right;}\n");
		content.append("#monthTable"+month+" th {border: 1px solid black;}\n");
		content.append("#monthTable"+month+" th.monthName {background-color: gray; font-weight: bold;}\n");
		content.append("#monthTable"+month+" th.week {background-color: lightgray;}\n");
		content.append("#monthTable"+month+" th.weekend {background-color: lightgray; color: red;}\n");
		content.append("#monthTable"+month+" td.week {}\n");
		content.append("#monthTable"+month+" td.weekend {color: red;}\n");
		content.append("#monthTable"+month+" td.weekMenu {background-color: green; color: white;}\n");
		content.append("#monthTable"+month+" td.weekendMenu {background-color: green; color: red;}\n");
		for (int i=0;i<getDaysInMonth(month);i++) {
			content.append("#monthTable"+month+" td#nummer"+i+" span#nummerMenu"+i+" {display: none;}\n");
			content.append("#monthTable"+month+" td#nummer"+i+":hover span#nummerMenu"+i+" {display: block; position: absolute; background-color: lightgray; color: black; border: 1px solid black; padding: 2px;}\n");
		}
		content.append("#monthTable"+month+" a.action {display: block; float: left; border: 1px solid #555; border-radius: 1px; padding: 4px; margin: 2px; background-color: white;}\n");
		content.append("</style>\n");

		/* Titelzeile*/
		content.append("<table id=\"monthTable"+month+"\">\n");
		content.append("  <tr>\n");
		String[] monthNames=Language.tr("Server.WebMenu.ViewerList.MonthNames").split(";");
		content.append("    <th colspan=\"7\" class=\"monthName\">"+monthNames[month%12]+" "+(month/12)+"</td>\n");
		content.append("  </tr>\n");
		content.append("  <tr>\n");
		for (String s: Language.tr("Server.WebMenu.ViewerList.DayNames.Week").split(";")) content.append("    <th width=\"14%\" class=\"week\">"+s+"</th>\n");
		for (String s: Language.tr("Server.WebMenu.ViewerList.DayNames.Weekend").split(";")) content.append("    <th width=\"14%\" class=\"weekend\">"+s+"</th>\n");
		content.append("  </tr>\n");

		/* Kalenderzeilen */
		int nr=1-firstWeekDayOfMonth(month);
		int daysInMonth=getDaysInMonth(month);
		for (int row=0;row<6;row++) {
			content.append("  <tr>\n");
			for (int col=0;col<7;col++) {
				if (nr<=0 || nr>daysInMonth) {
					content.append("    <td class=\"week\">&nbsp;</td>\n");
				} else {
					File file=getFileForDate(month,files,dates,nr);
					String style=(col<=4)?"week":"weekend";
					if (file==null) {
						content.append("    <td class=\""+style+"\">"+nr+"</td>\n");
					} else {
						String actions="<a href=\"/viewer/"+(index+1)+"/data/"+file.getName()+"\" target=\"_blank\" class=\"action\">"+Language.tr("Server.WebMenu.ViewerList.Overview")+"</a><br><a href=\"/viewer/"+(index+1)+"/"+file.getName()+"\" target=\"_blank\" class=\"action\">"+Language.tr("Server.WebMenu.ViewerList.Statistic")+"</a>";
						String menu="<span id=\"nummerMenu"+nr+"\">"+actions+"</span>";
						content.append("    <td id=\"nummer"+nr+"\" class=\""+style+"Menu\">"+nr+menu+"</td>\n");
					}
				}
				nr++;
			}
			content.append("  </tr>\n");
		}

		content.append("</table>\n");
		return content.toString();
	}

	/**
	 * Liefert eine Liste mit allen Statistik-Dateien nach Datum sortiert in einem Verzeichnis als html-Datei
	 * @param folder Statistikverzeichnis
	 * @param index	Index des Verzeichnisses in der Zählung des Webservers (zur Generierung der URLs zu den einzelnen Modellen)
	 * @return	html-Code, der die List enthält
	 */
	public static String getServerDateFileList(final StatisticFolder folder, final int index) {
		final File[] fileNames=folder.listFiles();
		final Date[] fileDates=folder.getFileDates();
		Date minDate=null, maxDate=null;
		for (Date date : fileDates) if (date!=null) {
			if (minDate==null || date.before(minDate)) minDate=date;
			if (maxDate==null || date.after(maxDate)) maxDate=date;
		}

		StringBuilder content=new StringBuilder();

		if (minDate==null || maxDate==null) {
			content.append("<p>"+Language.tr("Server.WebMenu.ViewerList.Empty")+"</p>");
		} else {
			Calendar cal=new GregorianCalendar();
			cal.setTime(minDate);
			int start=cal.get(Calendar.YEAR)*12+cal.get(Calendar.MONTH);
			cal.setTime(maxDate);
			int stop=cal.get(Calendar.YEAR)*12+cal.get(Calendar.MONTH);

			for (int month=start;month<=stop;month++) content.append(getServerDateFileListMonthEntry(month,fileNames,fileDates,index));

			List<File> filesWithoutDate=new ArrayList<File>();
			for (int i=0;i<fileDates.length;i++) if (fileDates[i]==null) filesWithoutDate.add(fileNames[i]);
			if (filesWithoutDate.size()>0) {
				String innercontent=getServerFileList(filesWithoutDate.toArray(new File[0]),index,true);
				if (innercontent!=null) {
					content.append("<h1 style=\"clear: both;\">"+Language.tr("Server.WebMenu.ViewerList.FilesWithoutDateInformation")+"</h1>\n");
					content.append(innercontent);
				}
			}
		}

		return content.toString();
	}

	private static boolean callerTypeMayChangeOnRetry(CallcenterModelCaller caller) {
		for (int i=0;i<caller.retryCallerTypeRateAfterBlockedFirstRetry.size();i++) if (caller.retryCallerTypeRateAfterBlockedFirstRetry.get(i)>0) return true;
		for (int i=0;i<caller.retryCallerTypeRateAfterBlocked.size();i++) if (caller.retryCallerTypeRateAfterBlocked.get(i)>0) return true;
		for (int i=0;i<caller.retryCallerTypeRateAfterGiveUpFirstRetry.size();i++) if (caller.retryCallerTypeRateAfterGiveUpFirstRetry.get(i)>0) return true;
		for (int i=0;i<caller.retryCallerTypeRateAfterGiveUp.size();i++) if (caller.retryCallerTypeRateAfterGiveUp.get(i)>0) return true;
		return false;
	}

	private static String getModelAgentsData(Statistics statistic, String callcenter, int group) {
		StringBuilder sb=new StringBuilder();
		String groupName=Statistics.getAgentModelDataGroupName(callcenter,group);

		for (Statistics.AgentModelData agents : statistic.agentenModellProGruppe) if (agents.name.equals(groupName)) {
			sb.append(Language.tr("SimStatistic.AgentsOnModelBasis.AgentsInSimulation")+": <b>"+agents.simAgents.storeToLocalString()+"</b><br>\n");
			sb.append(Language.tr("SimStatistic.AgentsOnModelBasis.AgentsInModel")+": <b>"+agents.modelAgents.storeToLocalString()+"</b><br>\n");
			sb.append(Language.tr("SimStatistic.AgentsOnModelBasis.AgentsWithAddition")+": <b>"+agents.fullAgents.storeToLocalString()+"</b>\n");
			break;
		}

		return sb.toString();
	}

	/**
	 * Liefert eine Inhaltsübersicht über eine Statistik-Datei als html-Seite
	 * @param index	Index des Verzeichnisses in der Zählung des Webservers (zur Generierung von URLs)
	 * @param statistic	Statistik-Daten, die aufbereitet werden sollen
	 * @param xmlFileName	Dateiname (ohne Pfad) der Statistik-xml-Datei (zur Generierung von URLs)
	 * @param filterFolder	Verzeichnis mit Filter-Skripten
	 * @return	Inhaltsübersicht als html-Code (ohne html-Rumpf)
	 */
	public static String getModelAndStatisticData(int index, Statistics statistic, String xmlFileName, String filterFolder) {
		StringBuilder content=new StringBuilder();

		String disabled=" <span style=\"color:red;\">"+Language.tr("Dialog.deactivated.lower")+"</span>";

		/* Modellname & Beschreibung */
		content.append("<h1>"+Language.tr("Editor.GeneralData.Model")+" \""+statistic.editModel.name+"\"</h1>\n");
		content.append("<p>"+statistic.editModel.description.replace("\n","<br>")+"</p>\n");
		if (statistic.editModel.date!=null && !statistic.editModel.date.trim().isEmpty()) {
			content.append("<p>"+Language.tr("SimStatistic.ModelInformation.Date")+": "+CallcenterModel.dateToLocalString(CallcenterModel.stringToDate(statistic.editModel.date))+"</p>\n");
		}

		/* Anrufer */
		content.append("<h1>"+Language.tr("Editor.Overview.Category.Caller")+"</h1>\n");
		content.append("<ol>");
		for (CallcenterModelCaller caller: statistic.editModel.caller) {
			content.append("<li style=\"margin-bottom: 10px;\"><b>"+caller.name+(caller.active?"":disabled)+"</b>:<br>\n");
			String t="";
			if (caller.freshCallsCountSD>0) t="; "+Language.tr("Distribution.StdDev")+" "+NumberTools.formatNumber(caller.freshCallsCountSD,1);
			if (caller.freshCallsCountMean==1) content.append(Language.tr("Editor.Caller.List.CallsPerDaySingle")+t+"\n"); else content.append(String.format(Language.tr("Editor.Caller.List.CallsPerDayMultiple"),caller.freshCallsCountMean)+t+"\n");
			content.append("<br>\n");
			content.append(String.format(Language.tr("Editor.Caller.List.Score"),NumberTools.formatNumberMax(caller.scoreBase),NumberTools.formatNumberMax(caller.scoreContinued),NumberTools.formatNumberMax(caller.scoreSecond)));
			content.append("<br>\n");

			switch (caller.waitingTimeMode) {
			case CallcenterModelCaller.WAITING_TIME_MODE_SHORT:
				content.append(Language.tr("Editor.Caller.List.WaitingTimeTolerance")+": ");
				if (caller.waitingTimeDist instanceof DataDistributionImpl)
					content.append(Language.tr("Editor.Caller.List.WaitingTimeTolerance.EmpiricalDistribution")); else content.append(DistributionTools.getDistributionName(caller.waitingTimeDist)+", "+DistributionTools.getDistributionInfo(caller.waitingTimeDist));
				content.append("<br>\n");
				break;
			case CallcenterModelCaller.WAITING_TIME_MODE_LONG:
				content.append(Language.tr("Editor.Caller.List.WaitingTimeTolerance")+": ");
				if (caller.waitingTimeDistLong instanceof DataDistributionImpl)
					content.append(Language.tr("Editor.Caller.List.WaitingTimeTolerance.EmpiricalDistribution")); else content.append(DistributionTools.getDistributionName(caller.waitingTimeDistLong)+", "+DistributionTools.getDistributionInfo(caller.waitingTimeDistLong));
				content.append("<br>\n");
				break;
			case CallcenterModelCaller.WAITING_TIME_MODE_CALC:
				content.append(Language.tr("Editor.Caller.List.WaitingTimeTolerance")+": ");
				content.append(String.format(Language.tr("Editor.Caller.List.WaitingTimeTolerance.Extrapolation"),TimeTools.formatExactTime(caller.waitingTimeCalcMeanWaitingTime),NumberTools.formatPercent(caller.waitingTimeCalcCancelProbability),TimeTools.formatExactTime(caller.waitingTimeCalcAdd)));
				content.append("<br>\n");
				break;
			}

			if (caller.retryProbabiltyAfterBlocked>0 || caller.retryProbabiltyAfterBlockedFirstRetry>0 || caller.retryProbabiltyAfterGiveUp>0 || caller.retryProbabiltyAfterGiveUpFirstRetry>0) {
				double min=caller.retryProbabiltyAfterBlocked;
				double max=caller.retryProbabiltyAfterBlocked;
				min=Math.min(min,caller.retryProbabiltyAfterBlockedFirstRetry);
				max=Math.max(max,caller.retryProbabiltyAfterBlockedFirstRetry);
				min=Math.min(min,caller.retryProbabiltyAfterGiveUp);
				max=Math.max(max,caller.retryProbabiltyAfterGiveUp);
				min=Math.min(min,caller.retryProbabiltyAfterGiveUpFirstRetry);
				max=Math.max(max,caller.retryProbabiltyAfterGiveUpFirstRetry);
				if (min==max) {
					content.append("P("+Language.tr("Editor.Caller.List.Retry")+")="+NumberTools.formatNumberMax(max*100)+"%");
				} else {
					content.append("P("+Language.tr("Editor.Caller.List.Retry")+")="+NumberTools.formatNumberMax(min*100)+"%..."+NumberTools.formatNumberMax(max*100)+"%");

				}
				if (callerTypeMayChangeOnRetry(caller)) content.append(", "+Language.tr("Editor.Caller.List.Retry.CallerTypeCanChange"));
				content.append(", ");
			}

			content.append("P("+Language.tr("Editor.Caller.List.Forwarding")+")="+NumberTools.formatNumberMax(caller.continueProbability*100)+"%");

			if (caller.recallProbability>0) {
				content.append(", P("+Language.tr("Editor.Caller.List.Recall")+")="+NumberTools.formatNumberMax(caller.recallProbability*100)+"%");
			}
			content.append("</li>\n");
		}
		content.append("</ol>");

		/* Callcenter & Agenten */
		for (CallcenterModelCallcenter callcenter : statistic.editModel.callcenter) {
			content.append("<h1>"+Language.tr("Editor.Callcenter")+" \""+callcenter.name+"\""+(callcenter.active?"":disabled)+" </h1>\n");
			content.append("<ol>\n");
			int indexCallcenter=0;
			for (CallcenterModelAgent agent : callcenter.agents) {
				content.append("<li style=\"margin-bottom: 10px;\"><b>"+String.format(Language.tr("Editor.Callcenter.AgentGroupNr"),indexCallcenter+1)+"</b>"+(agent.active?"":disabled)+"<br>\n");

				if (agent.count==-1) {
					content.append(Language.tr("Editor.Callcenter.AgentsMode.Distribution"));
					content.append("<br>\n");
					double count=0;
					DataDistributionImpl d=null;
					if (agent.countPerInterval24!=null) {
						d=agent.countPerInterval24;
						double[] dist=d.densityData;
						for (int i=0;i<dist.length;i++) count+=dist[i]*2;
					}
					if (agent.countPerInterval48!=null) {
						d=agent.countPerInterval48;
						double[] dist=d.densityData;
						for (int i=0;i<dist.length;i++) count+=dist[i];
					}
					if (agent.countPerInterval96!=null) {
						d=agent.countPerInterval96;
						double[] dist=d.densityData;
						for (int i=0;i<dist.length;i++) count+=dist[i]/2;
					}
					if (count==1) content.append(Language.tr("Editor.Callcenter.Count.HalfHourIntervalSingle")); else content.append(String.format(Language.tr("Editor.Callcenter.Count.HalfHourIntervalMultiple"),Math.round(count)));
					content.append("<br>\n");
					content.append(getModelAgentsData(statistic,callcenter.name,indexCallcenter));
				} else {
					if (agent.count==-2) {
						content.append(Language.tr("Editor.Callcenter.AgentsMode.ClientArrivals"));
						content.append("<br>\n");
						if (agent.byCallersAvailableHalfhours==1) content.append(Language.tr("Editor.Callcenter.Count.HalfHourIntervalSingle")); else content.append(String.format(Language.tr("Editor.Callcenter.Count.HalfHourIntervalMultiple"),agent.byCallersAvailableHalfhours));
						content.append("<br>\n");
						content.append(Language.tr("Editor.Callcenter.AgentsMode.ClientArrivals")+": ");
						for (int i=0;i<agent.byCallers.size();i++) {
							if (i>0) content.append(", ");
							content.append(agent.byCallers.get(i));
						}
					} else {
						if (agent.count!=1) content.append(String.format(Language.tr("Editor.Callcenter.Count.AgentMultiple"),agent.count)); else content.append(Language.tr("Editor.Callcenter.Count.AgentSingle"));
						content.append("<br>\n");
						content.append(Language.tr("Editor.Callcenter.WorkingTime")+": "+TimeTools.formatTime(agent.workingTimeStart)+"-");
						if (agent.workingNoEndTime) content.append(Language.tr("Distribution.Infinite")); else content.append(TimeTools.formatTime(agent.workingTimeEnd));
					}
				}
				content.append("<br>\n");
				content.append(Language.tr("Editor.Callcenter.SkillLevel")+": "+agent.skillLevel);
				content.append("</li>\n");
				indexCallcenter++;
			}
			content.append("</ol>\n");
		}

		/* Simulationsergebnisse */
		StatisticViewerTextInformation viewer=new StatisticViewerTextInformation(statistic,StatisticViewerTextInformation.Mode.MODE_BASE_INFORMATION);
		StringWriter sw=new StringWriter();
		BufferedWriter bw=new BufferedWriter(sw);
		try {
			viewer.saveHtml(bw,null,0,true);
			bw.flush();
		} catch (IOException e) {}
		content.append(sw.toString());

		/* Filter-Skripte */
		FastAccessFolder fastAccessFolder=new FastAccessFolder(filterFolder);
		String[][] scripts=fastAccessFolder.getScriptsList();
		if (scripts[0].length>0) {
			content.append("<h1>"+Language.tr("Statistic.FastAccess")+"</h1>\n");
			content.append("<ul>\n");
			for (int i=0;i<scripts[0].length;i++) {
				content.append("<li><a href=\"/viewer/"+(index+1)+"/filter/"+xmlFileName+"?"+scripts[0][i]+"\">"+scripts[1][i]+"</a></li>\n");
			}
			content.append("</ul>\n");
		}

		/* Aktionen */
		final String aStyle=" style=\"display: block; float: left; border: 1px solid #555; border-radius: 1px; padding: 4px; margin: 2px; background-color: #CCC;\"";
		content.append("<h1>"+Language.tr("Server.WebMenu.ViewerList.Actions")+"</h1>\n");
		content.append("<p style=\"margin: 0px 10px\">\n");
		content.append("<a href=\"/viewer/"+(index+1)+"/downloadmodel/"+xmlFileName+"\""+aStyle+">"+Language.tr("Server.WebMenu.Download.Model")+"</a> <a href=\"/viewer/"+(index+1)+"/downloadstatistic/"+xmlFileName+"\""+aStyle+">"+Language.tr("Server.WebMenu.Download.Statistic")+"</a>\n");
		content.append("</p>\n");

		return content.toString();
	}
}
