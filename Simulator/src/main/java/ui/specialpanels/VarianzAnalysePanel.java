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
package ui.specialpanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.CommonVariables;
import systemtools.MsgBox;
import tools.SetupData;
import ui.HelpLink;
import ui.images.Images;
import ui.model.CallcenterModel;

/**
 * Panel, über das eine Varianzanalyse des Modells durchgeführt werden kann,
 * d.h. verglichen werden kann, wie stark die Ergebnisse einzelner Simulationsläufe
 * von einander abweichen
 * @author Alexander Herzog
 * @version 1.0
 */
public class VarianzAnalysePanel extends JWorkPanel {
	private static final long serialVersionUID = -1992201900526618766L;

	private final CallcenterModel model;
	private int repeatCount;
	private int simDays;

	private final JTextField repeatNumber;
	private final JButton copyButton, saveButton;
	private final JTabbedPane tabs, tabs2;
	private final JProgressBar statusProgress;

	private final VarianzAnalyseExportableTable singleTable;
	private final VarianzAnalyseExportableTable globalTable;
	private ChartPanel chartPanel;
	private JFreeChart chart;
	private XYPlot plot;
	private XYSeriesCollection plotData;

	private VarianzAnalyseMultiSimulator multiSimulator;

	private Timer timer;
	private int count;

	private void initChart() {
		final String xLabel=Language.tr("VarianceAnalysis.SimulatedDays");
		final String yLabel=Language.tr("VarianceAnalysis.WaitingTimeCVBetweenSimulations");

		plotData=new XYSeriesCollection();
		chart=ChartFactory.createXYLineChart(Language.tr("VarianceAnalysis.WaitingTimeCV"),xLabel,yLabel,plotData,PlotOrientation.VERTICAL,true,true,false);
		plot=chart.getXYPlot();

		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.black);
		plot.setDomainGridlinePaint(Color.black);

		chartPanel=new ChartPanel(
				chart,
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
		chartPanel.setPopupMenu(null);
		chartPanel.setDomainZoomable(false);
		chartPanel.setRangeZoomable(false);
		chart.setBackgroundPaint(null);
		chart.getPlot().setBackgroundPaint(Color.WHITE);
		TextTitle t=chart.getTitle();
		if (t!=null) {Font f=t.getFont(); t.setFont(new Font(f.getFontName(),Font.PLAIN,f.getSize()-4));}

		NumberAxis axis;
		NumberFormat formater;

		axis=new NumberAxis();
		formater=NumberFormat.getNumberInstance();
		formater.setMinimumFractionDigits(0);
		formater.setMaximumFractionDigits(1);
		axis.setNumberFormatOverride(formater);
		axis.setLabel(xLabel);
		plot.setDomainAxis(axis);

		axis=new NumberAxis();
		formater=NumberFormat.getPercentInstance();
		formater.setMinimumFractionDigits(1);
		formater.setMaximumFractionDigits(1);
		axis.setNumberFormatOverride(formater);
		axis.setLabel(yLabel);
		plot.setRangeAxis(axis);
	}

	/**
	 * Konstruktor der Klasse
	 * @param doneNotify	Callback wird aufgerufen, wenn das Panel geschlossen werden soll
	 * @param model	Für die Varianzanalyse zu verwendenen Callcenter-Modell
	 * @param helpLink	Help-Link
	 */
	public VarianzAnalysePanel(final Runnable doneNotify, final CallcenterModel model, final HelpLink helpLink) {
		super(doneNotify,helpLink.pageVarianceAnalysis);
		this.model=model;

		singleTable=new VarianzAnalyseExportableTable(model);
		globalTable=new VarianzAnalyseExportableTable(model);

		JPanel main,p;

		/* Main area */
		add(main=new JPanel(new BorderLayout()),BorderLayout.CENTER);

		main.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		p.add(new JLabel(Language.tr("VarianceAnalysis.NumberOfRepetitions")+":"));
		p.add(repeatNumber=new JTextField(5));
		repeatNumber.setText(""+SetupData.getSetup().varianzAnalyseNumber);

		p.add(copyButton=new JButton(Language.tr("Dialog.Button.Copy")));
		copyButton.addActionListener(new ButtonListener());
		copyButton.setToolTipText(Language.tr("VarianceAnalysis.Copy.Info"));
		copyButton.setEnabled(false);
		copyButton.setIcon(Images.EDIT_COPY.getIcon());

		p.add(saveButton=new JButton(Language.tr("Dialog.Button.Save")));
		saveButton.addActionListener(new ButtonListener());
		saveButton.setToolTipText(Language.tr("VarianceAnalysis.Save.Info"));
		saveButton.setEnabled(false);
		saveButton.setIcon(Images.GENERAL_SAVE.getIcon());

		main.add(tabs=new JTabbedPane(),BorderLayout.CENTER);

		tabs.addTab(model.days+" "+(model.days==1?Language.tr("VarianceAnalysis.SimulationDays.Single"):Language.tr("VarianceAnalysis.SimulationDays.Multiple")),Images.VARIANCE_ANALYSIS_PAGE_SINGLE.getIcon(),singleTable.getTableInScrollPage());
		tabs.addTab(Language.tr("VarianceAnalysis.VariableNumberOfSimulationDays"),Images.VARIANCE_ANALYSIS_PAGE_MULTI.getIcon(),tabs2=new JTabbedPane());

		tabs2.addTab(Language.tr("VarianceAnalysis.Table"),Images.VARIANCE_ANALYSIS_PAGE_MULTI_TABLE.getIcon(),globalTable.getTableInScrollPage());
		initChart();
		tabs2.addTab(Language.tr("VarianceAnalysis.Diagram"),Images.VARIANCE_ANALYSIS_PAGE_MULTI_CHART.getIcon(),chartPanel);

		main.add(p=new JPanel(new BorderLayout()),BorderLayout.SOUTH);
		p.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		p.add(statusProgress=new JProgressBar(),BorderLayout.CENTER);
		statusProgress.setStringPainted(true);

		/* Bottom line */
		addFooter(Language.tr("VarianceAnalysis.Start"),Images.VARIANCE_ANALYSIS_RUN.getIcon(),Language.tr("VarianceAnalysis.Stop"));
	}

	@Override
	protected void setWorkMode(boolean running) {
		super.setWorkMode(running);
		if (!running) statusProgress.setValue(0);
		repeatNumber.setEnabled(!running);
		copyButton.setEnabled(!running);
		saveButton.setEnabled(!running);
		tabs.setEnabled(!running);
	}

	private boolean readRepeatCount(boolean showErrorMessage) {
		Integer I=NumberTools.getNotNegativeInteger(repeatNumber.getText());
		if (I==null || I==0) {
			if (showErrorMessage) MsgBox.error(this,Language.tr("VarianceAnalysis.InavlidRepetitionsNumber.Title"),Language.tr("VarianceAnalysis.InavlidRepetitionsNumber.Info"));
			return false;
		}
		repeatCount=I;
		return true;
	}

	private void addSeries(String title) {
		XYSeries series=new XYSeries(title);
		plotData.addSeries(series);
		int nr=plotData.getSeriesCount()-1;
		Color color=Color.BLACK;
		switch (nr%8) {
		case 0: color=Color.RED; break;
		case 1: color=Color.BLUE; break;
		case 2: color=Color.GREEN; break;
		case 3: color=Color.BLACK; break;
		case 4: color=Color.CYAN; break;
		case 5: color=Color.MAGENTA; break;
		case 6: color=Color.ORANGE; break;
		case 7: color=Color.PINK; break;
		}
		plot.getRenderer().setSeriesPaint(nr,color);
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.specialpanels.JWorkPanel#run()
	 */
	@Override
	protected void run() {
		if (!readRepeatCount(true)) return;

		simDays=0;
		singleTable.setBoldAfterRow(repeatCount);
		singleTable.clear();
		globalTable.clear();
		plotData.removeAllSeries();
		for (int i=0;i<model.caller.size();i++) addSeries(model.caller.get(i).name);
		addSeries(Language.tr("VarianceAnalysis.AllCaller"));

		cancelWork=false;
		setWorkMode(true);
		multiSimulatorStart();
	}

	private void multiSimulatorStart() {
		simDays+=5;
		multiSimulator=new VarianzAnalyseMultiSimulator(this,model,(tabs.getSelectedIndex()==0)?(model.days):simDays,repeatCount);
		count=0;
		if (!multiSimulator.initNextSimulation()) {
			multiSimulatorDone();
		} else {
			statusProgress.setMaximum(multiSimulator.getProgressMax());
			timer=new Timer();
			timer.schedule(new SimTimerTask(),50,50);
		}
	}

	private class SimTimerTask extends TimerTask {
		@Override
		public void run() {
			if (cancelWork) {timer.cancel(); multiSimulator.cancel(); multiSimulatorDone(); return;}

			if (multiSimulator.isRunning()) {
				if (count%8==0)	statusProgress.setValue((int)(multiSimulator.getProgress()));
				count++;
				return;
			}

			multiSimulator.doneSingleSimulation((tabs.getSelectedIndex()==0)?singleTable:null);
			count=0;
			if (!multiSimulator.initNextSimulation()) {timer.cancel(); multiSimulatorDone(); return;}
		}
	}

	private void multiSimulatorDone() {
		if (cancelWork) {
			if (tabs.getSelectedIndex()==0)	MsgBox.error(this,Language.tr("VarianceAnalysis.Stop.Title"),String.format(Language.tr("VarianceAnalysis.Stop.Info"),""+multiSimulator.getCurrentSimNumber()));
		} else {
			if (tabs.getSelectedIndex()==0)	{
				multiSimulator.doneAll(singleTable,null);
			} else {
				multiSimulator.doneAll(null,globalTable);
				for (int i=0;i<plot.getSeriesCount();i++) {
					plotData.getSeries(i).clear();
					for (int j=0;j<globalTable.getTable().getSize(0);j++) {
						plotData.getSeries(i).add((j+1)*5,NumberTools.getExtProbability(globalTable.getTable().getValue(j,i+1)));
					}
				}
			}
		}

		if (tabs.getSelectedIndex()==0 || simDays==250 || cancelWork) {
			multiSimulator=null;
			System.gc();
			setWorkMode(false);
		} else {
			multiSimulatorStart();
		}
	}

	@Override
	protected void done() {
		if (readRepeatCount(false)) SetupData.getSetup().varianzAnalyseNumber=repeatCount;
		super.done();
	}

	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==copyButton) {
				if (tabs.getSelectedIndex()==0) singleTable.copyTable(); else {
					if (tabs2.getSelectedIndex()==0) globalTable.copyTable(); else {
						SetupData setup=SetupData.getSetup();
						BufferedImage image=chart.createBufferedImage(setup.imageSize,setup.imageSize);
						getToolkit().getSystemClipboard().setContents(new TransferableImage(image),null);
					}
				}
				return;
			}
			if (e.getSource()==saveButton) {
				if (tabs.getSelectedIndex()==0) singleTable.saveTable(VarianzAnalysePanel.this); else {
					if (tabs2.getSelectedIndex()==0) globalTable.saveTable(VarianzAnalysePanel.this); else {
						JFileChooser fc=new JFileChooser();
						CommonVariables.initialDirectoryToJFileChooser(fc);
						fc.setDialogTitle(Language.tr("FileType.Save.Image"));
						FileFilter jpg=new FileNameExtensionFilter(Language.tr("FileType.jpeg")+" (*.jpg, *.jpeg)","jpg","jpeg");
						FileFilter gif=new FileNameExtensionFilter(Language.tr("FileType.gif")+" (*.gif)","gif");
						FileFilter png=new FileNameExtensionFilter(Language.tr("FileType.png")+" (*.png)","png");
						fc.addChoosableFileFilter(jpg);
						fc.addChoosableFileFilter(gif);
						fc.addChoosableFileFilter(png);
						fc.setFileFilter(png);
						fc.setAcceptAllFileFilterUsed(false);

						if (fc.showSaveDialog(chartPanel)!=JFileChooser.APPROVE_OPTION) return;
						CommonVariables.initialDirectoryFromJFileChooser(fc);
						File file=fc.getSelectedFile();

						if (file.getName().indexOf('.')<0) {
							if (fc.getFileFilter()==jpg) file=new File(file.getAbsoluteFile()+".jpg");
							if (fc.getFileFilter()==gif) file=new File(file.getAbsoluteFile()+".gif");
							if (fc.getFileFilter()==png) file=new File(file.getAbsoluteFile()+".png");
						}

						String s="png";
						if (file.getName().toLowerCase().endsWith(".jpg")) s="jpg";
						if (file.getName().toLowerCase().endsWith(".gif")) s="gif";

						SetupData setup=SetupData.getSetup();
						BufferedImage image=chart.createBufferedImage(setup.imageSize,setup.imageSize);
						try {ImageIO.write(image,s,file);} catch (IOException e1) {}
					}
				}
				return;
			}
		}

		private class TransferableImage implements Transferable{
			public TransferableImage(Image image) {theImage=image;}
			@Override
			public DataFlavor[] getTransferDataFlavors(){return new DataFlavor[]{DataFlavor.imageFlavor};}
			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor){return flavor.equals(DataFlavor.imageFlavor);}
			@Override
			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException{if (flavor.equals(DataFlavor.imageFlavor)) return theImage; else throw new UnsupportedFlavorException(flavor);}
			private final Image theImage;
		}
	}
}