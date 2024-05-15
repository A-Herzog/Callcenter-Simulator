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
package ui.optimizer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

import language.Language;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import simulator.CallcenterSimulatorInterface;
import simulator.Statistics;
import systemtools.MsgBox;
import tools.TrayNotify;
import ui.HelpLink;
import ui.editor.BaseEditDialog;
import ui.editor.CallcenterModelEditorPanelDialog;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.specialpanels.JWorkPanel;
import ui.statistic.optimizer.StatisticViewerOptimizerBarChart;

/**
 * Panel, welches den Optimierer kapselt
 * @author Alexander Herzog
 * @version 1.0
 */
public final class OptimizePanel extends JWorkPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 7826354435699022765L;

	/**
	 * Soll ein Meldungsdialog zum Abschluss einer
	 * erfolgreichen Optimierung angezeigt werden?
	 */
	public static boolean SHOW_OPTIMIZER_SUCCESS_DIALOG=false;

	/** Editor-Callcenter-Modell, das als Basis für die Optimierungen verwendet werden soll */
	private final CallcenterModel editModel;
	/** System, das die eigentliche Optimierung durchführt */
	private Optimizer optimizer;
	/** Hält die Optimierung an ({@link #pauseClick()}) */
	private boolean pauseMode;

	/** Übergeordnetes Fenster */
	private final Window owner;

	/** Ergebnisse (in Bezug auf die Zielgröße) aus dem initialen Optimierungsschritt */
	private DataDistributionImpl initialRunResults=null;

	/** Haupt-Panel (kann Eingabebereich aber auch Simulationsfortschrittsanzeige beinhalten) */
	private final JPanel main;
	/** Eingabebereich zur Konfiguration der Optimierung */
	private OptimizeEditPanel editPanel;
	/** Anzeige der Veränderung der Zielgröße während der Optimierung */
	private JFreeChart optimizeChart;
	/** Anzeige der Veränderung der Stellgröße während der Optimierung */
	private JFreeChart changeChart;
	/** Anzeige des Simulationsfortschritts */
	private JProgressBar simProgress;
	/** Timer zur Überwachung des Simulationsfortschritts ({@link SimTimerTask}) */
	private Timer timer;
	/** Zählt die Aufrufe von {@link SimTimerTask} */
	private int timerCount;

	/** Darzustellende Größe im linken Diagramm */
	private int dataTypeLeft=-1;
	/** Untereintrag für die darzustellende Größe im linken Diagramm */
	private int dataNrLeft=-1;
	/** Darzustellende Größe im rechten Diagramm */
	private int dataTypeRight=-2;
	/** Untereintrag für die darzustellende Größe im rechten Diagramm */
	private int dataNrRight=-1;

	/** Hilfe-Link */
	private final HelpLink helpLink;

	/**
	 * Ergebnisse der Optimierung
	 * @see #getResults()
	 */
	private OptimizeData results=null;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param doneNotify	Callback wird aufgerufen, wenn das Optimierungs-Panel geschlossen werden soll
	 * @param editModel	Editor-Callcenter-Modell, das als Basis für die Optimierungen verwendet werden soll
	 * @param optimizeSetup	Initial zu ladende Optimierungseinstellungendatei (kann <code>null</code> sein)
	 * @param helpLink	Hilfe-Link
	 */
	public OptimizePanel(final Window owner, final Runnable doneNotify, final CallcenterModel editModel, final File optimizeSetup, final HelpLink helpLink) {
		super(doneNotify,helpLink.pageOptimize);
		this.editModel=editModel;
		this.owner=owner;

		this.helpLink=helpLink;

		/* Main area */
		add(main=new JPanel(new CardLayout()),BorderLayout.CENTER);
		main.add(editPanel=new OptimizeEditPanel(owner,editModel,helpLink),"edit");
		main.add(createRunPanel(),"run");

		/* Bottom line */
		createFooterButtons();

		setWorkMode(false);

		if (optimizeSetup!=null && optimizeSetup.exists()) {
			editPanel.loadOptimizeSetup(optimizeSetup);
		}
	}

	@Override
	public boolean dragDropLoad(File file) {
		return editPanel.loadOptimizeSetup(file);
	}

	/**
	 * Gibt, sofern eine Optimierung durchgeführt wurde, die Ergebnisse zurück.
	 * @return	Wurde keine Optimierung durchgeführt, so liefert die Funktion <code>null</code>, sonst ein Objekt vom Typ <code>OptimizeData</code>
	 */
	public OptimizeData getResults() {
		return results;
	}

	/**
	 * Legt die Schaltflächen fest, die während der Bearbeitung
	 * der Einstellungen und während der Optimierung in der
	 * Symbolleiste angezeigt werden sollen.
	 */
	private void createFooterButtons() {
		addFooter(Language.tr("Optimizer.StartOptimization"),Images.OPTIMIZER.getIcon(),Language.tr("Optimizer.StopOptimization"));
		JButton button;

		button=addFooterButton(Language.tr("Optimizer.LoadSettings"));
		button.setToolTipText(Language.tr("Optimizer.LoadSettings.Info"));
		button.setIcon(Images.OPTIMIZER_SETTINGS_LOAD.getIcon());

		button=addFooterButton(Language.tr("Optimizer.SaveSettings"));
		button.setToolTipText(Language.tr("Optimizer.SaveSettings.Info"));
		button.setIcon(Images.OPTIMIZER_SETTINGS_SAVE.getIcon());

		button=addFooterButton(Language.tr("Optimizer.Pause"));
		button.setToolTipText(Language.tr("Optimizer.Pause.Info"));
		button.setIcon(Images.OPTIMIZER_PAUSE.getIcon());

		button=addFooterButton(Language.tr("Optimizer.Results"));
		button.setToolTipText(Language.tr("Optimizer.Results.Info"));
		button.setIcon(Images.OPTIMIZER_RESULTS.getIcon());

		button=addFooterButton(Language.tr("Optimizer.SetupDiagrams"));
		button.setToolTipText(Language.tr("Optimizer.SetupDiagrams.Info"));
		button.setIcon(Images.OPTIMIZER_RESULTS_DIAGRAMS.getIcon());

		getFooterButton(2).setVisible(false);
		getFooterButton(3).setVisible(false);
		getFooterButton(4).setVisible(false);
	}

	/**
	 * Führt die allgemeine Initialisierung eines Diagramms durch.
	 * @param chart	Diagrammobjekt
	 * @return	Panel in das das Diagrammobjekt eingebettet wird
	 * @see #createRunPanel()
	 */
	private ChartPanel initChart(final JFreeChart chart) {
		final CategoryPlot plot=chart.getCategoryPlot();

		plot.setRenderer(new StackedBarRenderer());
		plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		((BarRenderer)(plot.getRenderer())).setDrawBarOutline(true);
		((BarRenderer)(plot.getRenderer())).setShadowVisible(false);
		((BarRenderer)(plot.getRenderer())).setBarPainter(new StandardBarPainter());

		final ChartPanel chartPanel=new ChartPanel(
				chart,
				ChartPanel.DEFAULT_WIDTH,
				ChartPanel.DEFAULT_HEIGHT,
				ChartPanel.DEFAULT_MINIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_BUFFER_USED,
				true,  /* properties */
				false,  /* save */
				true,  /* print */
				true,  /* zoom */
				true   /* tooltips */
				);
		chartPanel.setPopupMenu(null);
		chart.setBackgroundPaint(null);
		chart.getPlot().setBackgroundPaint(Color.WHITE);
		TextTitle t=chart.getTitle();
		if (t!=null) {Font f=t.getFont(); t.setFont(new Font(f.getFontName(),Font.PLAIN,f.getSize()-4));}
		return chartPanel;
	}

	/**
	 * Legt das Panel an, das während der Optimierung
	 * den jeweiligen Fortschritt anzeigt.
	 * @return	Panel zur Anzeige während der Optimierung
	 */
	private JPanel createRunPanel() {
		JPanel status=new JPanel(new BorderLayout());
		JPanel p;

		status.add(p=new JPanel(new GridLayout(1,2)),BorderLayout.CENTER);
		p.add(initChart(optimizeChart=ChartFactory.createBarChart("","","",new DefaultCategoryDataset(),PlotOrientation.VERTICAL,true,true,false)));
		p.add(initChart(changeChart=ChartFactory.createBarChart("","","",new DefaultCategoryDataset(),PlotOrientation.VERTICAL,true,true,false)));

		status.add(p=new JPanel(new GridLayout(1,1)),BorderLayout.SOUTH);
		p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		p.add(simProgress=new JProgressBar(SwingConstants.HORIZONTAL),BorderLayout.SOUTH);
		simProgress.setStringPainted(true);

		return status;
	}

	/**
	 * Reagiert auf Klicks auf die Schaltfläche zur Unterbrechung
	 * bzw. zur Fortsetzung einer angehaltenen Optimierung.
	 * @see #pauseMode
	 */
	private void pauseClick() {
		pauseMode=!pauseMode;
		getFooterButton(2).setText(pauseMode?Language.tr("Optimizer.Resume"):Language.tr("Optimizer.Pause"));
		getFooterButton(3).setVisible(pauseMode);
	}

	/**
	 * Anzeige der bisherigen Teilergebnisse einer
	 * momentan pausierten Optimierung.
	 */
	private void temporaryResultsClick() {
		final OptimizeData data=optimizer.getResults();
		if (data==null) {
			MsgBox.error(this,Language.tr("Optimizer.Results.NoData.Title"),Language.tr("Optimizer.Results.NoData.Info"));
			return;
		}
		final Statistics[] results=data.data.toArray(Statistics[]::new);

		OptimizeSelectResult dialog=new OptimizeSelectResult(owner,results,helpLink.pageOptimizeModal);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;

		final Statistics statistic=results[dialog.getSelectedResult()];

		final CallcenterModelEditorPanelDialog viewer=new CallcenterModelEditorPanelDialog(owner,statistic.editModel,statistic,false,helpLink);
		viewer.setCloseNotify(()->setEnableGUI(owner,true));
		setEnableGUI(owner,false);
		viewer.setVisible(true);
	}

	@Override
	protected void userButtonClick(int index, JButton button) {
		switch (index) {
		case 0: editPanel.loadOptimizeSetup(); break;
		case 1: editPanel.saveOptimizeSetup(); break;
		case 2: pauseClick(); break;
		case 3: temporaryResultsClick(); break;
		case 4: setupDiagrams(); break;
		}
	}

	/**
	 * Stellt ein, welche Diagramme während der Optimierung angezeigt werden sollen.
	 * @see SetupOptimizeDiagrams
	 */
	private void setupDiagrams() {
		final boolean oldPauseState=pauseMode;
		try {
			pauseMode=true;
			SetupOptimizeDiagrams dialog=new SetupOptimizeDiagrams(owner,helpLink.pageOptimizeModal,editModel,dataTypeLeft,dataNrLeft,dataTypeRight,dataNrRight);
			if (dialog.getClosedBy()==BaseEditDialog.CLOSED_BY_OK) {
				int[] newData=dialog.getData();
				if (newData!=null && newData.length==4) {
					dataTypeLeft=newData[0];
					dataNrLeft=newData[1];
					dataTypeRight=newData[2];
					dataNrRight=newData[3];
				}
				updateCharts();
			}
		} finally {
			pauseMode=oldPauseState;
		}
	}

	@Override
	protected void setWorkMode(boolean running) {
		super.setWorkMode(running);
		getFooterButton(2).setVisible(running);
		getFooterButton(3).setVisible(false);
		getFooterButton(4).setVisible(running);
		((CardLayout)main.getLayout()).show(main,running?"run":"edit");
	}

	/**
	 * Initialisiert eine Optimierung.
	 * @return	Liefert im Erfolgsfall das {@link Optimizer}-Objekt, sonst <code>null</code> (eine Fehlermeldung wurde dann bereits ausgegeben)
	 * @see Optimizer
	 */
	private Optimizer initOptimizer() {
		OptimizeSetup optimizeSetup=editPanel.getOptimizeSetup();
		if (optimizeSetup==null) return null;

		Optimizer opt=new Optimizer(owner,null,editModel,optimizeSetup);

		String s=opt.checkAndInit();
		if (s!=null) {
			MsgBox.error(this,Language.tr("Optimizer.Error.UnableToStart"),s);
			return null;
		}

		if (optimizeSetup.optimizeIntervals.getMin()<=0.1 && optimizeSetup.optimizeByInterval==OptimizeSetup.OptimizeInterval.OPTIMIZE_BY_INTERVAL_NO) MsgBox.warning(this,Language.tr("Optimizer.IntervalMeanWarning.Title"),Language.tr("Optimizer.IntervalMeanWarning.Info"));

		if (optimizeSetup.optimizeMaxValue>=0) MsgBox.warning(this,Language.tr("Optimizer.UpDownWarning.Title"),Language.tr("Optimizer.UpDownWarning.Info"));

		return opt;
	}

	/**
	 * Wird nach dem Abschluss aller Simulationen
	 * (erfolgreich oder durch Abbruch) aufgerufen.
	 */
	private void everythingDone() {
		setWorkMode(false);
		results=optimizer.getResults();

		String s;
		if (cancelWork) {
			s=String.format(Language.tr("Optimizer.Message.Canceled"),optimizer.getCurrentRunNr());
		} else {
			s=String.format(Language.tr("Optimizer.Message.Finished"),optimizer.getCurrentRunNr());
			new TrayNotify(this,Language.tr("Optimizer.Message.FinishedTray.Title"),Language.tr("Optimizer.Message.FinishedTray.Info"));
		}
		if (SHOW_OPTIMIZER_SUCCESS_DIALOG || cancelWork)
			MsgBox.info(this,Language.tr("Optimizer.Message.DoneTitle"),s);

		if (results!=null) done();
	}

	/**
	 * Konfiguriert die Diagrammanzeige
	 * @param chart	Diagramm
	 * @param title	Diagrammtitel
	 * @param yLabel	y-Achsen-Beschriftung
	 * @param percent	Soll die y-Achse Zahlenwerte (<code>false</code>) oder Prozentwerte (<code>true</code>) anzeigen?
	 */
	private void initOptimizeChartCustom(JFreeChart chart, String title, String yLabel, boolean percent) {
		chart.setTitle(title);

		NumberAxis axis=(NumberAxis)(chart.getCategoryPlot().getRangeAxis());
		axis.setLabel(yLabel);
		if (percent) {
			NumberFormat formater=NumberFormat.getPercentInstance();
			formater.setMinimumFractionDigits(1);
			formater.setMaximumFractionDigits(1);
			axis.setNumberFormatOverride(formater);
		}

		chart.getCategoryPlot().getDomainAxis().setLabel(Language.tr("Statistic.Units.IntervalHalfHour"));
	}

	/**
	 * Aktualisiert ein Ausgabediagramm.
	 * @param chart	Ausgabediagramm
	 * @param dataType	Darzustellende Größe im Diagramm
	 * @param dataNr	Untereintrag für die darzustellende Größe im Diagramm
	 */
	private void updateOptimizeChartCustom(final JFreeChart chart, final int dataType, final int dataNr) {
		OptimizeData results=optimizer.getResults();
		if (results==null || results.data==null || results.data.size()==0) return;
		Statistics statistic1=results.data.get(0);
		Statistics statistic2=results.data.get(results.data.size()-1);

		Object[] obj=StatisticViewerOptimizerBarChart.getChartInitData(dataType);
		if (obj!=null) {
			initOptimizeChartCustom(chart,(String)obj[0],(String)obj[1],(Boolean)obj[2]);
			StatisticViewerOptimizerBarChart.setChartData(chart,statistic1,statistic2,dataType,dataNr);
		}
	}

	/**
	 * Aktualisiert das Zielgrößen-Diagramm (links).
	 * @param chart	Zielgrößen-Diagramm (links)
	 */
	private void updateOptimizeChart(final JFreeChart chart) {
		String s;
		boolean up;
		switch (optimizer.getOptimizeSetup().optimizeProperty) {
		case OPTIMIZE_PROPERTY_SUCCESS_BY_CALL:
		case OPTIMIZE_PROPERTY_SUCCESS_BY_CLIENT:
			s=Language.tr("SimStatistic.Accessibility"); up=true; break;
		case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CALL:
		case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CLIENT:
			s=Language.tr("SimStatistic.WaitingTime"); up=false; break;
		case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CALL:
		case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CLIENT:
			s=Language.tr("SimStatistic.ResidenceTime"); up=false; break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL:
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL_ALL:
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT:
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT_ALL:
			s=Language.tr("SimStatistic.ServiceLevel"); up=true; break;
		case OPTIMIZE_PROPERTY_WORK_LOAD:
			s=Language.tr("SimStatistic.WorkLoad"); up=false; break;
		default:
			s=""; up=true;
		}

		chart.setTitle(s+" - "+Language.tr("Optimizer.SimulationRun")+" "+optimizer.getCurrentRunNr());
		chart.getCategoryPlot().getDomainAxis().setLabel(Language.tr("Statistic.Units.IntervalHalfHour"));
		NumberAxis axis=(NumberAxis)(chart.getCategoryPlot().getRangeAxis());
		axis.setLabel(Language.tr("Optimizer.TargetValue"));
		axis.setNumberFormatOverride(null);

		DefaultCategoryDataset optimizeDataSet=new DefaultCategoryDataset();
		DataDistributionImpl[] values=optimizer.getOptimizeValue();

		if (initialRunResults==null && values[1]!=null) initialRunResults=values[1].clone();

		if (values[1]!=null) for (int i=0;i<values[1].densityData.length;i++) {
			s=TimeTools.formatShortTime(i*1800);

			double original=0;
			if (optimizer.getCurrentRunNr()>1) original=Math.max(0,initialRunResults.densityData[i]);

			switch (optimizer.getCurrentRunNr()) {
			case 1:
				/* Keine Balken */
				break;

			case 2:
				optimizeDataSet.addValue(original,Language.tr("Optimizer.InitialValues"),s);
				break;

			case 3:
				if (up) {
					optimizeDataSet.addValue(original,Language.tr("Optimizer.InitialValues"),s);
					optimizeDataSet.addValue(Math.max(0,values[1].densityData[i]-original),Language.tr("Optimizer.CurrentValues"),s);
				} else {
					optimizeDataSet.addValue(values[1].densityData[i],Language.tr("Optimizer.InitialValues"),s);
					optimizeDataSet.addValue(Math.max(0,original-values[1].densityData[i]),Language.tr("Optimizer.CurrentValues"),s);
				}
				break;

			default:
				if (up) {
					optimizeDataSet.addValue(original,Language.tr("Optimizer.InitialValues"),s);
					optimizeDataSet.addValue(Math.max(0,values[0].densityData[i]-original),Language.tr("Optimizer.ChangedValues"),s);
					optimizeDataSet.addValue(Math.max(0,values[1].densityData[i]-values[0].densityData[i]),Language.tr("Optimizer.ChangedValuesLastStep"),s);
				} else {
					optimizeDataSet.addValue(Math.max(0,values[1].densityData[i]),Language.tr("Optimizer.InitialValues"),s);
					optimizeDataSet.addValue(Math.max(0,original-values[0].densityData[i]),Language.tr("Optimizer.ChangedValuesLastStep"),s);
					optimizeDataSet.addValue(Math.max(0,values[0].densityData[i]-values[1].densityData[i]),Language.tr("Optimizer.ChangedValues"),s);
				}
				break;
			}
		}

		chart.getCategoryPlot().setDataset(optimizeDataSet);

		if (optimizer.getCurrentRunNr()>1) chart.getCategoryPlot().getRendererForDataset(optimizeDataSet).setSeriesPaint(0,Color.BLUE);
		if (optimizer.getCurrentRunNr()>2) chart.getCategoryPlot().getRendererForDataset(optimizeDataSet).setSeriesPaint(1,Color.RED);
		if (optimizer.getCurrentRunNr()>3) chart.getCategoryPlot().getRendererForDataset(optimizeDataSet).setSeriesPaint(2,Color.YELLOW);
	}

	/**
	 * Aktualisiert das Stellgrößen-Diagramm (rechts).
	 * @param chart	Stellgrößen-Diagramm (rechts)
	 */
	private void updateAgentChart(final JFreeChart chart) {
		chart.setTitle(Language.tr("Optimizer.NumberOfAgents"));
		chart.getCategoryPlot().getDomainAxis().setLabel(Language.tr("Statistic.Units.IntervalHalfHour"));
		NumberAxis axis=(NumberAxis)(chart.getCategoryPlot().getRangeAxis());
		axis.setLabel(Language.tr("Optimizer.AgentsPerHalfHourInterval"));
		axis.setNumberFormatOverride(new DecimalFormat("###"));

		DefaultCategoryDataset changeDataSet=new DefaultCategoryDataset();
		DataDistributionImpl[] agents=optimizer.getAgentsCounts();

		for (int i=0;i<agents[0].densityData.length;i++) {
			String s=TimeTools.formatShortTime(i*86400/agents[0].densityData.length);

			double original=agents[0].densityData[i];

			switch (optimizer.getCurrentRunNr()) {
			case 1:
				changeDataSet.addValue(original,Language.tr("Optimizer.InitialNumberOfAgents"),s);
				/* Keine weiteren Balken */
				break;
			case 2:
				double changedLastStep=agents[1].densityData[i]-agents[0].densityData[i];
				if (changedLastStep<0) {
					original+=changedLastStep; changedLastStep=-changedLastStep;
					changeDataSet.addValue(0,Language.tr("Optimizer.InitialNumberOfAgents"),s);
					changeDataSet.addValue(original,Language.tr("Optimizer.ChangedNumberOfAgents"),s);
				} else {
					changeDataSet.addValue(original,Language.tr("Optimizer.InitialNumberOfAgents"),s);
					changeDataSet.addValue(changedLastStep,Language.tr("Optimizer.ChangedNumberOfAgents"),s);
				}
				break;
			default:
				double changed=agents[2].densityData[i]-agents[0].densityData[i];
				changedLastStep=agents[1].densityData[i]-agents[2].densityData[i];
				if (changed<0 || changedLastStep<0) {
					original+=(changed+changedLastStep); changed=-changed; changedLastStep=-changedLastStep;
					changeDataSet.addValue(0,Language.tr("Optimizer.InitialNumberOfAgents"),s);
					if (changedLastStep>0) {
						changeDataSet.addValue(0,"Angepasste Agentenanzahl",s);
						changeDataSet.addValue(original,Language.tr("Optimizer.ChangedValuesLastStep"),s);
					} else {
						changeDataSet.addValue(original,Language.tr("Optimizer.ChangedNumberOfAgents"),s);
						changeDataSet.addValue(0,Language.tr("Optimizer.ChangedValuesLastStep"),s);
					}
				} else {
					changeDataSet.addValue(original,Language.tr("Optimizer.InitialNumberOfAgents"),s);
					changeDataSet.addValue(changed,Language.tr("Optimizer.ChangedNumberOfAgents"),s);
					changeDataSet.addValue(changedLastStep,Language.tr("Optimizer.ChangedValuesLastStep"),s);
				}
				break;
			}
		}

		chart.getCategoryPlot().setDataset(changeDataSet);

		chart.getCategoryPlot().getRendererForDataset(changeDataSet).setSeriesPaint(0,Color.BLUE);
		if (optimizer.getCurrentRunNr()>1) chart.getCategoryPlot().getRendererForDataset(changeDataSet).setSeriesPaint(1,Color.RED);
		if (optimizer.getCurrentRunNr()>2) chart.getCategoryPlot().getRendererForDataset(changeDataSet).setSeriesPaint(2,Color.YELLOW);
	}

	/**
	 * Aktualisiert während einer laufenden Optimierung
	 * die Diagrammdarstellungen, um die jeweils neusten
	 * Ergebnisse darzustellen.
	 */
	private void updateCharts() {
		switch (dataTypeLeft) {
		case -1: updateOptimizeChart(optimizeChart); break;
		case -2: updateAgentChart(optimizeChart); break;
		default: updateOptimizeChartCustom(optimizeChart,dataTypeLeft,dataNrLeft); break;
		}
		switch (dataTypeRight) {
		case -1: updateOptimizeChart(changeChart); break;
		case -2: updateAgentChart(changeChart); break;
		default: updateOptimizeChartCustom(changeChart,dataTypeRight,dataNrRight); break;
		}
	}

	/**
	 * Prüft in regelmäßigen Abständen, ob die
	 * laufende Simulation abgeschlossen wurde
	 * und die nächste Simulation gestartet
	 * werden kann.
	 */
	private final class SimTimerTask extends TimerTask {
		/**
		 * Konstruktor der Klasse
		 */
		public SimTimerTask() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void run() {
			if (pauseMode && !cancelWork) return;

			CallcenterSimulatorInterface simulator=optimizer.getSimulator();

			if (cancelWork) {
				timer.cancel();
				if (simulator!=null) simulator.cancel();
				everythingDone();
				return;
			}

			if (simulator==null || simulator.isRunning()) {
				timerCount++;
				if (timerCount%8==0) simProgress.setValue((int)((simulator==null)?0:simulator.getSimDayCount()));
				return;
			}

			int changeNeeded=optimizer.simulationDone();
			if (changeNeeded==0) {
				/* Ende */
				timer.cancel();
				everythingDone();
			} else {
				if (optimizer.isCanceled()) {requestClose(); return;}
				/* Nächsten Lauf starten */
				optimizer.simulationStart(changeNeeded);
				updateCharts();
			}
		}
	}

	@Override
	protected void run() {
		optimizer=initOptimizer();
		if (optimizer==null) return;

		cancelWork=false;
		setWorkMode(true);
		simProgress.setMaximum(editModel.days);

		String s=optimizer.simulationStart(0);
		if (s!=null) {
			MsgBox.error(owner,Language.tr("Window.ErrorStartingSimulation.Title"),s);
			everythingDone();
			return;
		}
		updateCharts();

		timer=new Timer();
		timer.schedule(new SimTimerTask(),50,50);
	}
}