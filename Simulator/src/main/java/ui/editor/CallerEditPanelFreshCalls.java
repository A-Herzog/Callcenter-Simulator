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
package ui.editor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.apache.commons.math3.util.ArithmeticUtils;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.swing.JDataDistributionEditPanel;
import systemtools.MsgBox;
import tools.SetupData;
import ui.images.Images;
import ui.model.CallcenterModelCaller;

/**
 * In diesem Panel kann die Verteilung der Erstanrufer für eine Kundengruppe eingestellt werden.
 * @author Alexander Herzog
 * @see CallerEditDialog
 */
public class CallerEditPanelFreshCalls extends CallerEditPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2300068644945818994L;

	private final JTextField freshCallsCountMean;
	private final JTextField freshCallsCountSD;
	private JButton tools;
	private final JPopupMenu toolsPopup;
	private JMenuItem tools1, tools2, tools3, tools4, tools5, tools6, tools7;
	private JDataDistributionEditPanel freshCalls;
	private JComboBox<String> freshCallsType;

	/**
	 * Konstruktor der Klasse
	 * @param initData	Informationen über den aktuellen Kundentyp zur Initialisierung des Panels
	 * @see CallerEditPanel.InitData
	 */
	public CallerEditPanelFreshCalls(final InitData initData) {
		super(initData);

		JPanel p2;

		setLayout(new BorderLayout());

		add(p2=new JPanel(),BorderLayout.NORTH);
		p2.setLayout(new FlowLayout(FlowLayout.LEFT,5,0));
		p2.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));
		p2.add(new JLabel(Language.tr("Editor.Caller.FreshCalls.Number")+":"));
		p2.add(freshCallsCountMean=new JTextField(((Integer)caller.freshCallsCountMean).toString(),7));
		freshCallsCountMean.setEnabled(!readOnly);
		freshCallsCountMean.addKeyListener(dialogElementListener);
		p2.add(new JLabel(Language.tr("Distribution.StdDev")+":"));
		p2.add(freshCallsCountSD=new JTextField(NumberTools.formatNumber(caller.freshCallsCountSD,1),7));
		freshCallsCountSD.setEnabled(!readOnly);
		freshCallsCountSD.addKeyListener(dialogElementListener);
		p2.add(tools=new JButton(Language.tr("Dialog.Button.Tools")));
		tools.setEnabled(!readOnly);
		tools.addActionListener(dialogElementListener);
		tools.setIcon(Images.GENERAL_SETUP.getIcon());
		p2.add(freshCallsType=new JComboBox<String>(new String[]{
				Language.tr("Editor.Caller.FreshCalls.Distribution.60Minutes"),
				Language.tr("Editor.Caller.FreshCalls.Distribution.30Minutes"),
				Language.tr("Editor.Caller.FreshCalls.Distribution.15Minutes")
		}));
		freshCallsType.setEnabled(!readOnly);
		DataDistributionImpl dist=null;
		if (caller.freshCallsDist24!=null) {freshCallsType.setSelectedIndex(0); dist=caller.freshCallsDist24;}
		if (caller.freshCallsDist48!=null) {freshCallsType.setSelectedIndex(1); dist=caller.freshCallsDist48;}
		if (caller.freshCallsDist96!=null) {freshCallsType.setSelectedIndex(2); dist=caller.freshCallsDist96;}
		freshCallsType.addActionListener(dialogElementListener);

		add(freshCalls=new JDataDistributionEditPanel(dist,JDataDistributionEditPanel.PlotMode.PLOT_BOTH,!readOnly,readOnly?0:1,true),BorderLayout.CENTER);
		freshCalls.setImageSaveSize(SetupData.getSetup().imageSize);

		toolsPopup=new JPopupMenu();
		toolsPopup.add(tools1=new JMenuItem(Language.tr("Editor.Caller.FreshCalls.Tools.TotalNumberFromDensity")));
		tools1.addActionListener(dialogElementListener);
		tools1.setIcon(Images.ARROW_UP.getIcon());
		toolsPopup.addSeparator();
		toolsPopup.add(tools2=new JMenuItem(Language.tr("Editor.Caller.FreshCalls.Tools.TotalNumberToDensity")));
		tools2.addActionListener(dialogElementListener);
		tools2.setIcon(Images.ARROW_DOWN.getIcon());
		toolsPopup.add(tools3=new JMenuItem(Language.tr("Editor.Caller.FreshCalls.Tools.NormalizeDensity")));
		tools3.addActionListener(dialogElementListener);
		tools3.setIcon(Images.EDITOR_CALLER_DENSITY_NORMALIZE.getIcon());
		toolsPopup.add(tools4=new JMenuItem(Language.tr("Editor.Caller.FreshCalls.Tools.ExpandDensityToIntegerNumbers")));
		tools4.addActionListener(dialogElementListener);
		tools4.setIcon(Images.EDITOR_CALLER_DENSITY_INTEGER.getIcon());
		toolsPopup.addSeparator();
		toolsPopup.add(tools5=new JMenuItem(Language.tr("Editor.Caller.FreshCalls.Tools.LoadArrivalsOn60MinutesBasis")));
		tools5.addActionListener(dialogElementListener);
		tools5.setIcon(Images.EDITOR_CALLER.getIcon());
		toolsPopup.add(tools6=new JMenuItem(Language.tr("Editor.Caller.FreshCalls.Tools.LoadArrivalsOn30MinutesBasis")));
		tools6.addActionListener(dialogElementListener);
		tools6.setIcon(Images.EDITOR_CALLER.getIcon());
		toolsPopup.add(tools7=new JMenuItem(Language.tr("Editor.Caller.FreshCalls.Tools.LoadArrivalsOn15MinutesBasis")));
		tools7.addActionListener(dialogElementListener);
		tools7.setIcon(Images.EDITOR_CALLER.getIcon());
	}

	@Override
	public String[] check(KeyEvent e) {
		String[] result=null;

		if (NumberTools.getNotNegativeInteger(freshCallsCountMean,true)==null) {
			if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.InvalidFreshCalls.Title"),String.format(Language.tr("Editor.Caller.Error.InvalidFreshCalls.Info"),freshCallsCountMean.getText())};
		}

		if (NumberTools.getNotNegativeDouble(freshCallsCountSD,true)==null) {
			if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.InvalidFreshCallsStdDev.Title"),String.format(Language.tr("Editor.Caller.Error.InvalidFreshCallsStdDev.Info"),freshCallsCountSD.getText())};
		}

		return result;
	}

	@Override
	public void writeToCaller(CallcenterModelCaller newCaller) {
		newCaller.freshCallsCountMean=NumberTools.getNotNegativeInteger(freshCallsCountMean,false);
		newCaller.freshCallsCountSD=NumberTools.getNotNegativeDouble(freshCallsCountSD,false);
		newCaller.freshCallsDist24=null;
		newCaller.freshCallsDist48=null;
		newCaller.freshCallsDist96=null;
		switch (freshCallsType.getSelectedIndex()) {
		case 0: newCaller.freshCallsDist24=(freshCalls.getDistribution()); break;
		case 1: newCaller.freshCallsDist48=(freshCalls.getDistribution()); break;
		case 2: newCaller.freshCallsDist96=(freshCalls.getDistribution()); break;
		}
	}

	@Override
	public String getTabName() {
		return Language.tr("Editor.Caller.Tabs.FreshCalls");
	}

	@Override
	public Icon getTabIconObject() {
		return Images.EDITOR_CALLER_PAGE_FRESH.getIcon();
	}

	private double isInteger(double[] d) {
		double max=0;
		for (int i=0;i<d.length;i++) max=Math.max(max,Math.abs(d[i]-Math.round(d[i])));
		return max;
	}

	private void round(double[] d) {
		for (int i=0;i<d.length;i++) d[i]=Math.round(d[i]);
	}

	private boolean makeDensityInteger(double[] d) {
		final double eps=0.0000001;

		if (d.length==0) return true;
		if (d.length==1) {d[0]=1; return true;}

		/* Ist bereits ganzzahlig? */
		if (isInteger(d)<eps) {round(d); return true;}

		/* Vergrößern bis ganzzahlig */
		boolean ok=false;
		for (int count=1;count<=6;count++) {
			for (int i=0;i<d.length;i++) d[i]*=10;
			if (isInteger(d)<eps) {ok=true; break;}
		}
		if (!ok) return false;
		round(d);

		/* Ggf. wieder schrumpfen */
		long gcd;
		if (Math.round(d[0])==0) {
			if (Math.round(d[1])==0) gcd=0; else gcd=Math.round(d[1]);
		} else {
			gcd=ArithmeticUtils.gcd(Math.round(d[0]),Math.round(d[1]));
		}
		for (int i=2;i<d.length;i++) {
			if (gcd==1) break;
			if (gcd==0) {
				gcd=Math.round(d[i]);
			} else {
				if (Math.round(d[i])!=0) gcd=ArithmeticUtils.gcd(gcd,Math.round(d[i]));
			}
		}
		if (gcd>1) for (int i=0;i<d.length;i++) d[i]/=gcd;
		round(d);
		return true;
	}

	@Override
	protected void processDialogEvents(ActionEvent e) {
		if (e.getSource()==freshCallsType) {
			DataDistributionImpl dist=freshCalls.getDistribution().clone();
			switch (freshCallsType.getSelectedIndex()) {
			case 0: dist.stretchToValueCount(24); break;
			case 1: dist.stretchToValueCount(48); break;
			case 2: dist.stretchToValueCount(96); break;
			}
			freshCalls.setDistribution(dist);
			return;
		}

		if (e.getSource()==tools) {toolsPopup.show(tools,0,tools.getBounds().height); return;}

		if (e.getSource()==tools1) {
			double[] d=freshCalls.getDistribution().densityData;
			double sum=0; for (int i=0;i<d.length;i++) sum+=d[i];
			freshCallsCountMean.setText(""+Math.round(sum));
			return;
		}

		if (e.getSource()==tools2) {
			DataDistributionImpl dist=freshCalls.getDistribution();
			double sum=0; for (int i=0;i<dist.densityData.length;i++) sum+=dist.densityData[i];
			Integer I=NumberTools.getInteger(freshCallsCountMean.getText());
			if (I==null || I<1) {
				MsgBox.error(CallerEditPanelFreshCalls.this,Language.tr("Editor.Caller.Error.InvalidFreshCalls.Title"),String.format(Language.tr("Editor.Caller.Error.InvalidFreshCalls.Info"),freshCallsCountMean.getText()));
				return;
			}
			for (int i=0;i<dist.densityData.length;i++) dist.densityData[i]=(double)Math.round(dist.densityData[i]/sum*I*100)/100;
			freshCalls.setDistribution(dist);
			return;
		}

		if (e.getSource()==tools3) {
			DataDistributionImpl dist=freshCalls.getDistribution();
			double sum=0; for (int i=0;i<dist.densityData.length;i++) sum+=dist.densityData[i];
			for (int i=0;i<dist.densityData.length;i++) dist.densityData[i]=(double)Math.round(dist.densityData[i]/sum*1000)/1000;
			freshCalls.setDistribution(dist);
			return;
		}

		if (e.getSource()==tools4) {
			DataDistributionImpl dist=freshCalls.getDistribution();
			if (!makeDensityInteger(dist.densityData)) return;
			freshCalls.setDistribution(dist);
			return;
		}

		if (e.getSource()==tools5) {
			requestOpenGenerator(24);
			return;
		}

		if (e.getSource()==tools6) {
			requestOpenGenerator(48);
			return;
		}

		if (e.getSource()==tools7) {
			requestOpenGenerator(96);
			return;
		}
	}
}
