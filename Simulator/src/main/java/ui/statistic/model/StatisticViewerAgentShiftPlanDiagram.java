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

import java.util.List;

import systemtools.statistics.StatisticViewerImage;
import ui.editor.AgentShiftPlanDiagram;
import ui.model.CallcenterModelAgent;

/**
 * Zeigt ein Schichtplandiagramm an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerAgentShiftPlanDiagram extends StatisticViewerImage {
	private final List<CallcenterModelAgent> translatedAgents;

	/**
	 * Konstruktor der Klasse
	 * @param translatedAgents	Konkrete Agenten deren Arbeitszeiten als Schichtplantabelle dargestellt werden sollen
	 */
	public StatisticViewerAgentShiftPlanDiagram(List<CallcenterModelAgent> translatedAgents) {
		super(null);
		this.translatedAgents=translatedAgents;
	}

	@Override
	public ViewerImageType getImageType() {return ViewerImageType.IMAGE_TYPE_SHIFTPLAN;}

	@Override
	protected void panelNeeded() {
		setPanel(new AgentShiftPlanDiagram(translatedAgents));
	}
}
