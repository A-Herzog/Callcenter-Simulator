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
package ui.statistic.core.viewers;

import java.util.ArrayList;
import java.util.List;

import language.Language;

/**
 * Gibt bestimmte vordefinierte Texte in Form eines HTML-Panels aus.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerSpecialHTMLText extends	StatisticViewerSimpleHTMLText {
	/**
	 * Anzeigemodus
	 * @author Alexander Herzog
	 */
	public enum ViewerCategory {
		/**
		 * Anzeige: Bitte Kategorie auswählen.
		 */
		VIEWER_CATEGORY,

		/**
		 * Anzeige: Bitte Unterkategorie auswählen.
		 */
		VIEWER_SUBCATEGORY,

		/**
		 * Anzeige: Bitte Simulation starten.
		 */
		VIEWER_NODATA
	}

	/**
	 * Erzeugt den auszugebenden Text
	 * @param type Gibt an, was angezeigt werden soll (siehe <code>VIEWER_*</code>-Konstanten)	 *
	 * @param startSimulation <code>Runnable</code>-Objekt, das beim Klick auf "Simulation jetzt starten" ausgeführt werden soll.
	 * @param loadStatistics <code>Runnable</code>-Objekt, das beim Klick auf "Statistikdaten laden" ausgeführt werden soll.
	 * @return	Auszugebender Text
	 */
	private static final String buildInfoText(ViewerCategory type, Runnable startSimulation, Runnable loadStatistics) {
		switch (type) {
		case VIEWER_CATEGORY: return Language.tr("Statistic.GeneralPage.SelectCategoryHTML");
		case VIEWER_SUBCATEGORY: return Language.tr("Statistic.GeneralPage.SelectSubCategoryHTML");
		case VIEWER_NODATA:
			String info=Language.tr("Statistic.GeneralPage.NoDataHTML");
			int nr=0;
			if (startSimulation!=null) {
				nr++;
				info+="<p><a href=\"special:"+nr+"\">"+Language.tr("Statistic.GeneralPage.NoDataHTML.RunSimulation")+"</a></p>";
			}
			if (loadStatistics!=null) {
				nr++;
				info+="<p><a href=\"special:"+nr+"\">"+Language.tr("Statistic.GeneralPage.NoDataHTML.LoadStatistic")+"</a></p>";
			}
			return info;
		}
		return "";
	}

	/**
	 * Generiert die Callbacks zum Start einer Simulation und zum Laden von Statistikdaten
	 * @param startSimulation <code>Runnable</code>-Objekt, das beim Klick auf "Simulation jetzt starten" ausgeführt werden soll.
	 * @param loadStatistics <code>Runnable</code>-Objekt, das beim Klick auf "Statistikdaten laden" ausgeführt werden soll.
	 * @return	Callbacks zum Start einer Simulation und zum Laden von Statistikdaten
	 */
	private static final Runnable[] buildSpecialLinkListener(Runnable startSimulation, Runnable loadStatistics) {
		List<Runnable> runner=new ArrayList<>();
		if (startSimulation!=null) runner.add(startSimulation);
		if (loadStatistics!=null) runner.add(loadStatistics);
		return runner.toArray(new Runnable[0]);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerSpecialHTMLText</code>
	 * @param type Gibt an, was angezeigt werden soll (siehe <code>VIEWER_*</code>-Konstanten)	 *
	 * @param startSimulation <code>Runnable</code>-Objekt, das beim Klick auf "Simulation jetzt starten" ausgeführt werden soll.
	 * @param loadStatistics <code>Runnable</code>-Objekt, das beim Klick auf "Statistikdaten laden" ausgeführt werden soll.
	 */
	public StatisticViewerSpecialHTMLText(ViewerCategory type, Runnable startSimulation, Runnable loadStatistics) {
		super(buildInfoText(type,startSimulation,loadStatistics),buildSpecialLinkListener(startSimulation,loadStatistics));
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerSpecialHTMLText</code>
	 * @param type Gibt an, was angezeigt werden soll (siehe <code>VIEWER_*</code>-Konstanten)
	 */
	public StatisticViewerSpecialHTMLText(ViewerCategory type) {
		this(type,null,null);
	}
}
