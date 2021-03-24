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
package ui.statistic.core;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Lädt die JFreeChart-Klasse im Hintergrund, so dass sie nicht erst bei der ersten
 * Verwendung gesucht werden muss.
 * @author Alexander Herzog
 * @version 1.0
 */
public class SpeedUpJFreeChart extends Thread {
	/** xlsx-Arbeitsmappe */
	private static XSSFWorkbook wbX;
	/** xls-Arbeitsmappe */
	private static HSSFWorkbook wbH;
	/* public static SpreadSheet sheet; */

	/**
	 * Konstruktor der Klasse
	 */
	public SpeedUpJFreeChart() {
		super();
		start();
	}

	@Override
	public void run() {
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		try {Thread.sleep(2500);} catch (InterruptedException e) {}

		DefaultPieDataset<String> pieData=new DefaultPieDataset<>();
		ChartFactory.createPieChart("",pieData,true,true,false);

		XYSeriesCollection lineData=new XYSeriesCollection();
		ChartFactory.createXYLineChart("","x","y",lineData,PlotOrientation.VERTICAL,true,true,false);

		DefaultCategoryDataset barData=new DefaultCategoryDataset();
		new ChartPanel(
				ChartFactory.createBarChart("","x","y",barData,PlotOrientation.VERTICAL,true,true,false),
				ChartPanel.DEFAULT_WIDTH,
				ChartPanel.DEFAULT_HEIGHT,
				ChartPanel.DEFAULT_MINIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_BUFFER_USED,
				true,  // properties
				false,  // save
				true,  // print
				true,  // zoom
				true   // tooltips
				);

		wbX=new XSSFWorkbook();
		wbX.toString();

		wbH=new HSSFWorkbook();
		wbH.toString();

		/*
		sheet=SpreadSheet.create(0,0,0);
		sheet.toString();
		 */
	}
}
