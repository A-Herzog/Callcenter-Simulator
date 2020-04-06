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
package ui.simplesimulation;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;

/**
 * Zeigt die Ergebnisse der Einfach-Simulation an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class SimpleSimulationResultsPanel extends JPanel {
	private static final long serialVersionUID = -5694519365920895160L;

	private final JTextPane text;

	private static final String head=
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"+
					"<html>\n"+
					"<head>\n"+
					"  <style type=\"text/css\">\n"+
					"  body {font-family: Verdana, Lucida, sans-serif; font-size: 100%; background-color: #FFFFF3; margin: 2px;}\n"+
					"  </style>\n"+
					"</head>\n"+
					"<body>\n";
	private static final String foot="</body></html>";

	/**
	 * Konstruktor der Klasse <code>SimpleSimulationResultsPanel</code>
	 */
	public SimpleSimulationResultsPanel() {
		setLayout(new BorderLayout());
		text=new JTextPane();
		text.setEditable(false);
		text.setBackground(new Color(0xFF,0xFF,0xF8));
		text.setContentType("text/html");
		add(new JScrollPane(text),BorderLayout.CENTER);
	}

	private String getResultsText(final SimpleSimulation simulator) {
		if (simulator.error!=null) return "<b>"+Language.tr("Dialog.Title.Error")+"</b><br>"+simulator.error;

		StringBuilder sb=new StringBuilder();

		sb.append("<b>"+Language.tr("SimStatistic.Accessibility")+"</b><br>");
		sb.append(Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.OnCallBasis")+"): "+NumberTools.formatPercent(simulator.anrufeErfolg)+"<br>");
		sb.append(Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.OnClientBasis")+"): "+NumberTools.formatPercent(simulator.kundenErfolg)+"<br>");
		sb.append("<br>");

		sb.append("<b>"+Language.tr("SimStatistic.WaitingTime")+"</b><br>");
		sb.append(Language.tr("SimStatistic.WaitingTime.Mean")+" ("+Language.tr("SimStatistic.OnCallBasis")+", "+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+"): "+TimeTools.formatExactTime(simulator.anrufeWartezeit,1)+"<br>");
		sb.append(Language.tr("SimStatistic.CancelTime.Mean")+" ("+Language.tr("SimStatistic.OnCallBasis")+"): "+TimeTools.formatExactTime(simulator.anrufeAbbruchzeit,1)+"<br>");
		sb.append(Language.tr("SimStatistic.WaitingTime.Mean")+" ("+Language.tr("SimStatistic.OnCallBasis")+", "+Language.tr("SimStatistic.CalculatedOn.AllCalls")+"): "+TimeTools.formatExactTime(simulator.anrufeWartezeitAlle,1)+"<br>");
		sb.append(Language.tr("SimStatistic.WaitingTime.Mean")+" ("+Language.tr("SimStatistic.OnClientBasis")+", "+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+"): "+TimeTools.formatExactTime(simulator.kundenWartezeit,1)+"<br>");
		sb.append(Language.tr("SimStatistic.CancelTime.Mean")+" ("+Language.tr("SimStatistic.OnClientBasis")+"): "+TimeTools.formatExactTime(simulator.kundenAbbruchzeit,1)+"<br>");
		sb.append(Language.tr("SimStatistic.WaitingTime.Mean")+" ("+Language.tr("SimStatistic.OnClientBasis")+", "+Language.tr("SimStatistic.CalculatedOn.AllClients")+"): "+TimeTools.formatExactTime(simulator.kundenWartezeitAlle,1)+"<br>");
		sb.append("<br>");

		sb.append("<b>"+Language.tr("SimStatistic.ResidenceTime")+"</b><br>");
		sb.append(Language.tr("SimStatistic.ResidenceTime.Mean")+" ("+Language.tr("SimStatistic.OnCallBasis")+", "+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+"): "+TimeTools.formatExactTime(simulator.anrufeVerweilzeit,1)+"<br>");
		sb.append(Language.tr("SimStatistic.CancelTime.Mean")+" ("+Language.tr("SimStatistic.OnCallBasis")+"): "+TimeTools.formatExactTime(simulator.anrufeAbbruchzeit,1)+"<br>");
		sb.append(Language.tr("SimStatistic.ResidenceTime.Mean")+" ("+Language.tr("SimStatistic.OnCallBasis")+", "+Language.tr("SimStatistic.CalculatedOn.AllCalls")+"): "+TimeTools.formatExactTime(simulator.anrufeVerweilzeitAlle,1)+"<br>");
		sb.append(Language.tr("SimStatistic.ResidenceTime.Mean")+" ("+Language.tr("SimStatistic.OnClientBasis")+", "+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+"): "+TimeTools.formatExactTime(simulator.kundenVerweilzeit,1)+"<br>");
		sb.append(Language.tr("SimStatistic.CancelTime.Mean")+" ("+Language.tr("SimStatistic.OnClientBasis")+"): "+TimeTools.formatExactTime(simulator.kundenAbbruchzeit,1)+"<br>");
		sb.append(Language.tr("SimStatistic.ResidenceTime.Mean")+" ("+Language.tr("SimStatistic.OnClientBasis")+", "+Language.tr("SimStatistic.CalculatedOn.AllClients")+"): "+TimeTools.formatExactTime(simulator.kundenVerweilzeitAlle,1)+"<br>");
		sb.append("<br>");

		sb.append("<b>"+Language.tr("SimStatistic.ServiceLevel")+"</b><br>");
		sb.append(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.OnCallBasis")+", "+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+"): "+NumberTools.formatPercent(simulator.anrufeServiceLevelErfolg)+"<br>");
		sb.append(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.OnCallBasis")+", "+Language.tr("SimStatistic.CalculatedOn.AllCalls")+"): "+NumberTools.formatPercent(simulator.anrufeServiceLevelAlle)+"<br>");
		sb.append(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.OnClientBasis")+", "+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+"): "+NumberTools.formatPercent(simulator.kundenServiceLevelErfolg)+"<br>");
		sb.append(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.OnClientBasis")+", "+Language.tr("SimStatistic.CalculatedOn.AllClients")+"): "+NumberTools.formatPercent(simulator.kundenServiceLevelAlle)+"<br>");
		sb.append("<br>");

		sb.append("<b>"+Language.tr("SimStatistic.WorkLoad")+"</b><br>");
		sb.append(Language.tr("SimStatistic.WorkLoad")+": "+NumberTools.formatPercent(simulator.auslastung)+"<br>");
		sb.append("<br>");

		sb.append("<b>"+Language.tr("SimStatistic.SystemData")+"</b><br>");
		sb.append(Language.tr("SimStatistic.SystemData.SimulationTime")+": "+NumberTools.formatLong(simulator.runTime)+" "+Language.tr("Statistic.Units.MilliSeconds")+"<br>");
		sb.append("<br>");

		return sb.toString();
	}

	/**
	 * Übergibt ein <code>SimpleSimulation</code>-Objekt, dessen Statistikdaten angezeigt werden sollen
	 * @param simulator	<code>SimpleSimulation</code>-Objekt, dessen Ergebnisse hier angezeigt werden sollen
	 */
	public void setResults(final SimpleSimulation simulator) {
		text.setText(head+getResultsText(simulator)+foot);
	}
}
