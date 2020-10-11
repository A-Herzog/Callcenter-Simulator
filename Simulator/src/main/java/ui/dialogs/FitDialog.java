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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.swing.JDataDistributionEditPanel;
import mathtools.distribution.swing.JDataLoader;
import mathtools.distribution.swing.JDistributionPanel;
import mathtools.distribution.tools.DistributionFitter;
import mathtools.distribution.tools.DistributionTools;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import systemtools.MsgBox;
import tools.SelectableTreeSystem;
import tools.SelectableTreeSystem.SelectableTreeNode;
import ui.HelpLink;
import ui.editor.BaseEditDialog;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelCaller;
import ui.model.CallcenterModelSkillLevel;

/**
 * @author Alexander Herzog
 * @version 1.0
 */
public class FitDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8263152374892311273L;

	/**
	 * HTML-Kopf f�r die Ausgabe in {@link #inputValues} und {@link #outputText}
	 * @see #inputValues
	 * @see #outputText
	 * @see #htmlFoot
	 */
	private static final String htmlHead=
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"+
					"<html>\n"+
					"<head>\n"+
					"  <style type=\"text/css\">\n"+
					"  body {font-family: Verdana, Lucida, sans-serif; background-color: #FFFFF3; margin: 2px;}\n"+
					"  ul.big li {margin-bottom: 5px;}\n"+
					"  ol.big li {margin-bottom: 5px;}\n"+
					"  a {text-decoration: none;}\n"+
					"  a.box {margin-top: 10px; margin-botton: 10px; border: 1px solid black; background-color: #DDDDDD; padding: 5px;}\n"+
					"  h2 {margin-bottom: 0px;}\n"+
					"  p.red {color: red;}\n"+
					"  </style>\n"+
					"</head>\n"+
					"<body>\n";

	/**
	 * HTML-Fu�bereich f�r die Ausgabe in {@link #inputValues} und {@link #outputText}
	 * @see #inputValues
	 * @see #outputText
	 * @see #htmlHead
	 */
	private static final String htmlFoot="</body></html>";

	/** Eingabefeld f�r die Messwerte */
	private JTextPane inputValues;
	/** Maximalwert innerhalb der Messwerte */
	private double inputValuesMax=0;
	/** Darstellung der Messwerte als empirische Verteilung */
	private JDataDistributionEditPanel inputDistribution;
	/** Mussten Werte gerundet werden */
	private boolean hasFloat;
	/** Ergebnis der Verteilungsanpassung als Text */
	private String outputReportPlain;
	/** Ergebnis der Verteilungsanpassung als HTML */
	private String outputReportHTML;
	/** Ausgabefeld f�r die Ergebnisse der Verteilungsanpassung */
	private JTextPane outputText;
	/** Am besten passende Verteilung */
	private JDistributionPanel outputDistribution;

	private JComboBox<String> ouputSelectInsert;
	private List<AbstractRealDistribution> outputSelectDist;

	private SelectableTreeSystem tree;
	private JButton ouputSelectButton;

	/**
	 * Im Konstruktor angegebenes Callcenter-Modell in das ggf. Verteilungsdaten eingetragen werden
	 */
	public final CallcenterModel model;

	/**
	 * Wurde das Modell durch den Dialog ver�ndert?
	 * Dann sollte es nach dem Schlie�en des Dialogs wieder in den Editor geladen werden.
	 */
	public boolean modelChanged=false;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Fenster
	 * @param model	Callcenter-Modell in das ggf. Verteilungen eingetragen werden
	 * @param helpLink	Hilfe-Link
	 */
	public FitDialog(final Window owner, final CallcenterModel model, final HelpLink helpLink) {
		super(owner,Language.tr("FitDialog.Title"),null,false,helpLink.dialogFitDistribution);
		showCloseButton=true;
		this.model=model;
		createTabsGUI(null,null,null,false,725,500,null,null);
		setResizable(true);
		setMinimumSize(new Dimension(725,500));
	}

	@Override
	protected void createTabs(JTabbedPane tabs) {
		JPanel p,p2,p3;
		JToolBar toolbar;
		JButton b;
		JScrollPane sp;

		/* Dialogseite "Messwerte" */
		tabs.addTab(Language.tr("FitDialog.Tab.Values"),p=new JPanel(new BorderLayout()));
		p.add(toolbar=new JToolBar(),BorderLayout.NORTH);
		toolbar.setFloatable(false);
		toolbar.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
		toolbar.add(b=new JButton(Language.tr("FitDialog.PasteValues")));
		b.setToolTipText(Language.tr("FitDialog.PasteValues.Tooltip"));
		b.addActionListener(new ButtonListener(0));
		b.setIcon(Images.EDIT_PASTE.getIcon());
		toolbar.add(b=new JButton(Language.tr("FitDialog.LoadValues")));
		b.setToolTipText(Language.tr("FitDialog.LoadValues.Tooltip"));
		b.addActionListener(new ButtonListener(1));
		b.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		p.add(p2=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		p2.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		p2.add(sp=new JScrollPane(inputValues=new JTextPane()),BorderLayout.CENTER);
		sp.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		inputValues.setEditable(false);
		inputValues.setContentType("text/html");
		inputValues.setText(htmlHead+Language.tr("FitDialog.PasteOrLoadValues")+htmlFoot);

		/* Dialogseite "Empirische Verteilung" */
		tabs.addTab(Language.tr("FitDialog.Tab.EmpiricalDistribution"),p=new JPanel(new BorderLayout()));
		p.add(inputDistribution=new JDataDistributionEditPanel(new DataDistributionImpl(10,10),JDataDistributionEditPanel.PlotMode.PLOT_DENSITY),BorderLayout.CENTER);
		inputDistribution.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));

		/* Dialogseite "Anpassung" */
		tabs.addTab(Language.tr("FitDialog.Tab.Fit"),p=new JPanel(new BorderLayout()));
		p.add(toolbar=new JToolBar(),BorderLayout.NORTH);
		toolbar.setFloatable(false);
		toolbar.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
		toolbar.add(b=new JButton(Language.tr("FitDialog.CopyResults")));
		b.addActionListener(new ButtonListener(2));
		b.setToolTipText(Language.tr("FitDialog.CopyResults.Tooltip"));
		b.setIcon(Images.EDIT_COPY.getIcon());
		p.add(p2=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		p2.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		p2.add(sp=new JScrollPane(outputText=new JTextPane()),BorderLayout.CENTER);
		sp.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		outputText.setEditable(false);
		outputText.setContentType("text/html");
		outputText.setText(htmlHead+htmlFoot);

		/* Dialogseite "Angepasste Verteilung" */
		tabs.addTab(Language.tr("FitDalog.Tab.FittedDistribution"),p=new JPanel(new BorderLayout()));
		p.add(outputDistribution=new JDistributionPanel(new DataDistributionImpl(10,10),10,false),BorderLayout.CENTER);
		outputDistribution.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));

		/* Dialogseite "Verteilung in Modell �bernehmen" */
		tabs.addTab(Language.tr("FitDalog.Tab.UseResults"),p=new JPanel(new BorderLayout()));
		p.add(p2=new JPanel(),BorderLayout.NORTH);
		p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p3.add(new JLabel(Language.tr("FitDalog.Distribution")+":"));
		p3.add(ouputSelectInsert=new JComboBox<String>());
		ouputSelectInsert.setEnabled(false);
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p3.add(new JLabel(Language.tr("FitDalog.UseFor")+":"));

		tree=new SelectableTreeSystem();
		p.add(new JScrollPane(tree.getTree()));
		addItemsToOutputTree();

		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		p2.add(ouputSelectButton=new JButton(Language.tr("Dialog.Button.Paste")));
		ouputSelectButton.setIcon(Images.EDIT_PASTE.getIcon());
		ouputSelectButton.addActionListener(new ButtonListener(4));
		ouputSelectButton.setEnabled(false);

		new FileDropper(this,new ButtonListener(3));
		new FileDropper(inputValues,new ButtonListener(3));
		new FileDropper(outputText,new ButtonListener(3));

		tabs.setIconAt(0,Images.FIT_PAGE_VALUES.getIcon());
		tabs.setIconAt(1,Images.FIT_PAGE_EMPIRICAL_DISTRIBUTION.getIcon());
		tabs.setIconAt(2,Images.FIT_PAGE_FIT.getIcon());
		tabs.setIconAt(3,Images.FIT_PAGE_RESULT.getIcon());
		tabs.setIconAt(4,Images.MODEL.getIcon());
	}


	@Override protected boolean checkData() {return true;}
	@Override protected void storeData() {}

	private void addItemsToOutputTree() {
		DefaultMutableTreeNode root=tree.getRoot(), node, node2;

		for (int i=0;i<model.caller.size();i++) {
			CallcenterModelCaller caller=model.caller.get(i);
			root.add(node=tree.createNode(Language.tr("FitDalog.UseFor.CallerType")+" \""+caller.name+"\""));
			node.add(tree.createCheckBox(Language.tr("FitDalog.UseFor.WaitingTimeToleranceDistribution"),i*10+0));
			node.add(tree.createCheckBox(Language.tr("FitDalog.UseFor.WaitingTimeToleranceDistributionLong"),i*10+1));
			node.add(tree.createCheckBox(Language.tr("FitDalog.UseFor.RetryTimeDistribution"),i*10+2));
			node.add(tree.createCheckBox(Language.tr("FitDalog.UseFor.RecallTimeDistribution"),i*10+3));
		}

		for (int i=0;i<model.skills.size();i++) {
			CallcenterModelSkillLevel skill=model.skills.get(i);
			root.add(node=tree.createNode(Language.tr("FitDalog.UseFor.SkillLevel")+" \""+skill.name+"\""));
			for (int j=0;j<skill.callerTypeName.size();j++) {
				node.add(node2=tree.createNode(Language.tr("FitDalog.UseFor.CallerType")+" \""+skill.callerTypeName.get(j)+"\""));
				node2.add(tree.createCheckBox(Language.tr("FitDalog.HoldingTimeDistribution")+" ("+Language.tr("FitDalog.Global")+")",10000000+i*10000+j*100+0));
				for (int k=0;k<48;k++) node2.add(tree.createCheckBox(Language.tr("FitDalog.HoldingTimeDistribution")+" ("+TimeTools.formatTime(k*1800)+" - "+TimeTools.formatTime((k+1)*1800-1)+")",10000000+i*10000+j*100+(k+1)));
				node2.add(tree.createCheckBox(Language.tr("FitDalog.PostProcessingTimeDistribution")+" ("+Language.tr("FitDalog.Global")+")",10000000+i*10000+j*100+49));
				for (int k=0;k<48;k++) node2.add(tree.createCheckBox(Language.tr("FitDalog.PostProcessingTimeDistribution")+" ("+TimeTools.formatTime(k*1800)+" - "+TimeTools.formatTime((k+1)*1800-1)+")",10000000+i*10000+j*100+(k+50)));
			}
		}

		tree.reload(true);
	}

	private class ButtonListener implements ActionListener {
		private final int buttonNr;

		public ButtonListener(final int buttonNr) {
			this.buttonNr=buttonNr;
		}

		private boolean loadValuesFromArray(double newValues[][]) {
			if (newValues==null || newValues.length==0 || newValues[0]==null || newValues[0].length==0) return false;

			/* Messwerte-Liste f�llen */
			StringBuilder sb=new StringBuilder();
			sb.append("<h2>"+Language.tr("FitDalog.Loaded.Title")+"</h2>");
			sb.append("<p>"+String.format(Language.tr("FitDalog.Loaded.Info"),newValues[0].length)+"</p>");
			sb.append("<h2>"+Language.tr("FitDalog.Loaded.List")+"</h2>");
			sb.append("<p>");
			for (int i=0;i<newValues[0].length;i++) {
				if (newValues.length==1) sb.append(NumberTools.formatNumber(newValues[0][i],2)+"<br>"); else sb.append(NumberTools.formatNumber(newValues[0][i],0)+": "+NumberTools.formatNumber(newValues[1][i],2)+"<br>");
			}
			sb.append("</p>");

			/* Messwerte-Diagramm f�llen */
			final Object[] obj=DistributionFitter.dataDistributionFromValues(newValues);
			if (obj==null) return false;
			inputDistribution.setDistribution((DataDistributionImpl)obj[0]);
			inputValuesMax=((DataDistributionImpl)obj[0]).densityData.length;
			hasFloat=(Boolean)obj[1];

			inputValues.setText(htmlHead+sb.toString()+htmlFoot);
			inputValues.setSelectionStart(0);
			inputValues.setSelectionEnd(0);

			return true;
		}

		private boolean loadValuesFromClipboard() {
			Transferable cont=getToolkit().getSystemClipboard().getContents(this);
			if (cont==null) return false;
			String s=null;
			try {s=(String)cont.getTransferData(DataFlavor.stringFlavor);} catch (Exception ex) {return false;}
			if (s==null) return false;

			return loadValuesFromArray(JDataLoader.loadNumbersTwoRowsFromString(FitDialog.this,s,1,Integer.MAX_VALUE));
		}

		private boolean loadValuesFromFile() {
			return loadValuesFromArray(JDataLoader.loadNumbersTwoRows(FitDialog.this,Language.tr("FitDialog.LoadValues"),1,Integer.MAX_VALUE));
		}

		private void calcFit() {
			DistributionFitter fitter=new DistributionFitter();
			fitter.process(inputDistribution.getDistribution());
			outputReportPlain=fitter.getResult(false);
			outputReportHTML=fitter.getResult(true);
			String info="";
			if (hasFloat) info="<h2>"+Language.tr("Dialog.Title.Information")+"</h2><p>"+Language.tr("FitDialog.InfoValuesRounded")+"</p>";
			outputText.setText(htmlHead+"<h2>"+Language.tr("FitDalog.FittedDistribution")+"</h2>"+outputReportHTML+info+htmlFoot);
			outputDistribution.setDistribution(fitter.getFitDistribution());
			outputDistribution.setMaxXValue(inputValuesMax);
			ouputSelectInsert.setEnabled(true);
			List<String> list=fitter.getResultList();
			for (int i=0;i<list.size();i++)	ouputSelectInsert.addItem(list.get(i));
			outputSelectDist=fitter.getResultListDist();
			ouputSelectButton.setEnabled(true);
		}

		private void copyResults() {
			getToolkit().getSystemClipboard().setContents(new StringSelection(outputReportPlain),null);
		}

		private boolean fileDrop(FileDropperData dropper) {
			final boolean ok=loadValuesFromArray(JDataLoader.loadNumbersTwoRowsFromFile(FitDialog.this,dropper.getFile(),1,Integer.MAX_VALUE));
			if (ok) dropper.dragDropConsumed();
			return ok;
		}

		private void insertInfoProperty(int id, AbstractRealDistribution dist) {
			if (id<10000000) {
				/* Kundentyp */
				CallcenterModelCaller caller=model.caller.get(id/10);
				switch (id%10) {
				case 0: caller.waitingTimeDist=DistributionTools.cloneDistribution(dist); caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_SHORT; break;
				case 1: caller.waitingTimeDistLong=DistributionTools.cloneDistribution(dist); caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_LONG; break;
				case 2: caller.retryTimeDist=DistributionTools.cloneDistribution(dist); break;
				case 3: caller.recallTimeDist=DistributionTools.cloneDistribution(dist); break;
				}
			} else {
				/* Skill-Level */
				id=id%10000000;
				CallcenterModelSkillLevel skill=model.skills.get(id/10000);
				id=id%10000;
				int id2=id%100;
				id=id/100;
				if (id2==0) skill.callerTypeWorkingTime.set(id,DistributionTools.cloneDistribution(dist));
				if (id2>=1 && id2<=48) skill.callerTypeIntervalWorkingTime.get(id)[id2-1]=DistributionTools.cloneDistribution(dist);
				if (id2==49) skill.callerTypePostProcessingTime.set(id,DistributionTools.cloneDistribution(dist));
				if (id2>=50 && id2<=97) skill.callerTypeIntervalPostProcessingTime.get(id)[id2-50]=DistributionTools.cloneDistribution(dist);
			}
		}

		private void insertInfoModel() {
			AbstractRealDistribution dist=outputSelectDist.get(ouputSelectInsert.getSelectedIndex());

			List<SelectableTreeNode> nodes=tree.getNodes();
			int count=0;
			for (int i=0;i<nodes.size();i++) if (nodes.get(i).toggle!=null && nodes.get(i).toggle.isSelected()) {
				count++;
				int id=nodes.get(i).id;
				insertInfoProperty(id,dist);
				modelChanged=true;
			}
			if (count==0) {
				MsgBox.error(FitDialog.this,Language.tr("FitDalog.UseDistribution.NoTargetErrorTitle"),Language.tr("FitDalog.UseDistribution.NoTargetErrorInfo"));
			} else {
				MsgBox.info(FitDialog.this,Language.tr("FitDalog.UseDistribution.Title"), (count==1)?Language.tr("FitDalog.UseDistribution.InfoSingle"):String.format(Language.tr("FitDalog.UseDistribution.InfoMultiple"),count));
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (buttonNr) {
			case 0:
				if (loadValuesFromClipboard()) {tabs.setSelectedIndex(2); calcFit();} else MsgBox.error(FitDialog.this,Language.tr("FitDalog.InvalidDataTitle"),Language.tr("FitDalog.InvalidDataClipboard"));
				break;
			case 1:
				if (loadValuesFromFile()) {tabs.setSelectedIndex(2); calcFit();} else MsgBox.error(FitDialog.this,Language.tr("FitDalog.InvalidDataTitle"),Language.tr("FitDalog.InvalidDataFile"));
				break;
			case 2: copyResults(); break;
			case 3: if (fileDrop((FileDropperData)(e.getSource()))) {tabs.setSelectedIndex(2); calcFit();} break;
			case 4: insertInfoModel(); break;
			}
		}
	}
}
