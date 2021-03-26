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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.tree.DefaultMutableTreeNode;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import systemtools.MsgBox;
import systemtools.statistics.JCheckboxTable;
import ui.HelpLink;
import ui.connected.AdditionalCallerSetupDialog;
import ui.connected.ConnectedModelUebertrag;
import ui.connected.ConnectedUebertragEditDialog;
import ui.editor.BaseEditDialog;
import ui.images.Images;
import ui.model.CallcenterModel;
import xml.XMLTools;

/**
 * Panel, welches Dialogelemente zur Konfiguration eines Optimierungslaufes kapselt
 * @author Alexander Herzog
 * @version 1.0
 */
public final class OptimizeEditPanel extends JTabbedPane {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1988525403055649357L;

	/** Elternfenster */
	private final Window owner;
	/** Verkn�pfung mit der Online-Hilfe */
	private final HelpLink helpLink;
	/** Datenmodell, dem Informationen zu Anrufer- und Agenten-Gruppen entnommen werden */
	private final CallcenterModel editModel;

	/* Dialogseite "Optimierungsgr��e" */

	/** Letzter in {@link #optProperty} gew�hlter Eintrag */
	private int lastProperty=-1;
	/** Optimierungseigenschaft */
	private final JComboBox<String> optProperty;
	/** Informationstext zu den Einstellungen 1 */
	private final JLabel optValueLabel;
	/** Informationstext zu den Einstellungen 2 */
	private final JLabel optValueLabel2;
	/** Informationstext zu den Einstellungen 3 */
	private final JLabel optValueLabel3;
	/** Eingabefeld f�r den Zielwert der Optimierung */
	private final JTextField optValue;
	/** Option: Auch Agentenanzahl reduzieren */
	private final JCheckBox optValueMaxActive;
	/** Eingabefeld f�r Maximum f�r Zielwert im Falle, dass {@link #optValueMaxActive} gew�hlt ist */
	private final JTextField optValueMax;
	/** Werte f�r {@link #optValue} f�r die verschiedenen Zielgr��en */
	private final String[] lastValue=new String[]{"95%","95%","00:15:00","00:15:00","00:05:00","00:05:00","80%","80%","90%","90%","80%"};
	/** Werte f�r {@link #optValueMax} f�r die verschiedenen Zielgr��en */
	private final String[] lastMaxValue=new String[]{"99%","99%","00:00:02","00:00:02","00:02:00","00:02:00","95%","95%","60%","60%","60%"};

	/** Auswahl wie der Zielwert im Tagesverlauf erreicht werden muss (im  Mittel, in jedem Intervall, ...) */
	private final JComboBox<String> optInterval;
	/** Auswahl f�r welche Kunden- oder Agentengruppen der Zielwert erreicht werden muss */
	private final JComboBox<String> optGroups;
	/** Auswahlm�glichkeiten f�r {@link #optGroups} wenn Kundentypen gew�hlt werden k�nnen */
	private final String[] groupsCaller=new String[]{
			Language.tr("Optimizer.CallerGroups.AverageOverAll"),
			Language.tr("Optimizer.CallerGroups.ForAllGroups"),
			Language.tr("Optimizer.CallerGroups.SelectedGroups")
	};
	/** Auswahlm�glichkeiten f�r {@link #optGroups} wenn Agentengruppen gew�hlt werden k�nnen */
	private final String[] groupsAgents=new String[]{
			Language.tr("Optimizer.AgentGroups.AverageOverAll"),
			Language.tr("Optimizer.AgentGroups.ForAllGroups"),
			Language.tr("Optimizer.AgentGroups.SelectedGroups")
	};
	/** Soll der Zielwert gem�� {@link #optGroups} f�r bestimmte Gruppen erreicht werden, so k�nnen hier die Gruppen gew�hlt werden */
	private final CheckBoxTree optGroupTree;

	/* Dialogseite "Stellgr��e" */

	/** Eingabefeld f�r den �nderungsfaktor */
	private final JTextField changeValue;
	/** Schaltfl�che "Einschr�nkungen" */
	private final JButton changeRestictionsButton;
	/** Erkl�rung f�r {@link #changeRestictionsButton} */
	private final JLabel changeRestrictionsLabel;
	/** Auswahl, ob alle oder nur bestimmte Gruppen (Kunden oder Agenten) ver�ndert werden sollen */
	private final JComboBox<String> changeGroups;
	/** Auswahl der Gruppen (Kunden oder Agenten) die ver�ndert werden sollen */
	private final CheckBoxTree changeGroupTree;

	/** Namen der Gruppen f�r gruppen-spezifische Einschr�nkungen */
	private final List<String> groupRestrictionName;
	/** Intervall-abh�ngige Minimalwerte f�r die gruppen-spezifische Einschr�nkungen */
	private final List<DataDistributionImpl> groupRestrictionMin;
	/** Intervall-abh�ngige Maximalwerte f�r die gruppen-spezifische Einschr�nkungen */
	private final List<DataDistributionImpl> groupRestrictionMax;

	/* Dialogseite "Intervalle" */

	/** Schaltfl�che "Alle ausw�hlen" */
	private JButton selectAllButton;
	/** Schaltfl�che "Nichts ausw�hlen" */
	private JButton selectNoneButton;
	/** Schaltfl�che "Bereich ausw�hlen" */
	private JButton selectRangeButton;
	/** Tabelle zur Darstellung der verf�gbaren Zeitslots */
	private JCheckboxTable intervalTable;

	/* Dialogseite "�bertrag" */

	/** Eingabefeld f�r "Statistik-Datei f�r �bertrag" */
	private final JTextField day0statisticsField;
	/** Schaltfl�che zur Auswahl der Datei f�r "Statistik-Datei f�r �bertrag" f�r {@link #day0statisticsField} */
	private final JButton day0statisticsButton;
	/** Schaltfl�che "�bertrag der Warteabbrecher" */
	private JButton day0configButton;
	/** Schaltfl�che "Zus�tzlicher manueller �bertrag in den Tag hinein" */
	private JButton additionalDay0Caller;

	/** �bertrag aus dem Vortag pro Anrufergruppe */
	private HashMap<String,ConnectedModelUebertrag> uebertrag;
	/** Namen der Anrufergruppen f�r zus�tzlichen, manuellen �bertrag */
	private final List<String> uebertragAdditionalCaller;
	/** Anzahlen der Anrufer in den Anrufergruppen f�r zus�tzlichen, manuellen �bertrag */
	private final List<Integer> uebertragAdditionalCount;

	/**
	 * Konstruktor der Klasse {@link OptimizeEditPanel}
	 * @param owner	Elternfenster
	 * @param editModel Datenmodell, dem Informationen zu Anrufer- und Agenten-Gruppen entnommen werden
	 * @param helpLink Verkn�pfung mit der Online-Hilfe
	 */
	public OptimizeEditPanel(Window owner, CallcenterModel editModel, HelpLink helpLink) {
		super();
		this.editModel=editModel;
		this.owner=owner;
		this.helpLink=helpLink;

		uebertrag=new HashMap<String,ConnectedModelUebertrag>();
		uebertragAdditionalCaller=new ArrayList<String>();
		uebertragAdditionalCount=new ArrayList<Integer>();

		groupRestrictionName=new ArrayList<String>();
		groupRestrictionMin=new ArrayList<DataDistributionImpl>();
		groupRestrictionMax=new ArrayList<DataDistributionImpl>();

		JPanel tab;
		JPanel p,p2,p3;

		/* Tab "Optimierungsgr��e" */
		add(tab=new JPanel(),Language.tr("Optimizer.Tabs.OptimizeProperty"));
		tab.setLayout(new BoxLayout(tab,BoxLayout.Y_AXIS));
		tab.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		tab.setAlignmentX(Component.LEFT_ALIGNMENT);
		tab.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body>"+Language.tr("Optimizer.Tabs.OptimizeProperty.Info")+"</body></html>"));
		tab.add(p=new JPanel(new BorderLayout()));
		p.add(p2=new JPanel(),BorderLayout.NORTH);
		p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p3.add(new JLabel(Language.tr("Optimizer.Tabs.OptimizeProperty.Label")+":"));
		p3.add(optProperty=new JComboBox<String>(new String[]{
				Language.tr("Optimizer.OptimizeProperty.Property.Accessibility")+" ("+Language.tr("Optimizer.OptimizeProperty.Property.OnCallBasis")+")",
				Language.tr("Optimizer.OptimizeProperty.Property.Accessibility")+" ("+Language.tr("Optimizer.OptimizeProperty.Property.OnClientBasis")+")",
				Language.tr("Optimizer.OptimizeProperty.Property.AverageWaitingTime")+" ("+Language.tr("Optimizer.OptimizeProperty.Property.OnCallBasis")+")",
				Language.tr("Optimizer.OptimizeProperty.Property.AverageWaitingTime")+" ("+Language.tr("Optimizer.OptimizeProperty.Property.OnClientBasis")+")",
				Language.tr("Optimizer.OptimizeProperty.Property.AverageResidenceTime")+" ("+Language.tr("Optimizer.OptimizeProperty.Property.OnCallBasis")+")",
				Language.tr("Optimizer.OptimizeProperty.Property.AverageResidenceTime")+" ("+Language.tr("Optimizer.OptimizeProperty.Property.OnClientBasis")+")",
				Language.tr("Optimizer.OptimizeProperty.Property.ServiceLevel")+" ("+Language.tr("Optimizer.OptimizeProperty.Property.OnCallBasis")+" "+Language.tr("Optimizer.OptimizeProperty.Property.CalculatedOn.SuccessfulCalls")+")",
				Language.tr("Optimizer.OptimizeProperty.Property.ServiceLevel")+" ("+Language.tr("Optimizer.OptimizeProperty.Property.OnCallBasis")+" "+Language.tr("Optimizer.OptimizeProperty.Property.CalculatedOn.AllCalls")+")",
				Language.tr("Optimizer.OptimizeProperty.Property.ServiceLevel")+" ("+Language.tr("Optimizer.OptimizeProperty.Property.OnClientBasis")+" "+Language.tr("Optimizer.OptimizeProperty.Property.CalculatedOn.SuccessfulClients")+")",
				Language.tr("Optimizer.OptimizeProperty.Property.ServiceLevel")+" ("+Language.tr("Optimizer.OptimizeProperty.Property.OnClientBasis")+" "+Language.tr("Optimizer.OptimizeProperty.Property.CalculatedOn.AllClients")+")",
				Language.tr("Optimizer.OptimizeProperty.Property.WorkLoad")
		}));
		optProperty.addActionListener(new DialogElementListener());
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p3.add(optValueLabel=new JLabel(Language.tr("Optimizer.OptimizeProperty.TargetForLabel.Accessibility")+":"));
		p3.add(optValue=new JTextField("95%",10));
		p3.add(optValueLabel2=new JLabel(""));
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p3.add(optValueMaxActive=new JCheckBox(Language.tr("Optimizer.OptimizeProperty.TargetMaxForLabel.Accessibility")+":"));
		p3.add(optValueMax=new JTextField("99%",10));
		p3.add(optValueLabel3=new JLabel(""));
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p3.add(new JLabel(Language.tr("Optimizer.OptimizeProperty.TargetValueFor")+":"));
		p3.add(optInterval=new JComboBox<String>(new String[]{
				Language.tr("Optimizer.OptimizeProperty.TargetValueFor.Day"),
				Language.tr("Optimizer.OptimizeProperty.TargetValueFor.Interval"),
				Language.tr("Optimizer.OptimizeProperty.TargetValueFor.IntervalInOrder")
		}));
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p3.add(new JLabel(Language.tr("Optimizer.OptimizeProperty.TargetValueFor")+":"));
		p3.add(optGroups=new JComboBox<String>(groupsCaller));
		optGroups.addActionListener(new DialogElementListener());

		optGroupTree=new CheckBoxTree();
		p.add(new JScrollPane(optGroupTree.tree),BorderLayout.CENTER);

		optProperty.setSelectedIndex(0);

		/* Tab "Stellgr��e" */
		add(tab=new JPanel(),Language.tr("Optimizer.Tabs.ControlVariable"));
		tab.setLayout(new BoxLayout(tab,BoxLayout.Y_AXIS));
		tab.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		tab.setAlignmentX(Component.LEFT_ALIGNMENT);
		tab.add(p=new JPanel(new BorderLayout()));

		p.add(p2=new JPanel(),BorderLayout.NORTH);
		p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p3.add(new JLabel(Language.tr("Optimizer.ControlVariable.ChangeNumberOfAgents")+":"));
		p3.add(changeValue=new JTextField("1%",10));
		p3.add(changeRestictionsButton=new JButton(Language.tr("Optimizer.ControlVariable.Restrictions")));
		changeRestictionsButton.setToolTipText(Language.tr("Optimizer.ControlVariable.Restrictions.Tooltip"));
		changeRestictionsButton.addActionListener(new ButtonListener());
		changeRestictionsButton.setIcon(Images.OPTIMIZER_PAGE_CONTROL_VARIABLE_RESTRICTION.getIcon());
		p3.add(changeRestrictionsLabel=new JLabel(Language.tr("Optimizer.ControlVariable.Restrictions.Info.No")));
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p3.add(new JLabel(Language.tr("Optimizer.ControlVariable.ChangeAgentGroups")+":"));
		p3.add(changeGroups=new JComboBox<String>(new String[]{
				Language.tr("Optimizer.ControlVariable.ChangeAgentGroups.All"),
				Language.tr("Optimizer.ControlVariable.ChangeAgentGroups.Selected")
		}));
		changeGroups.addActionListener(new DialogElementListener());

		changeGroupTree=new CheckBoxTree();
		p.add(new JScrollPane(changeGroupTree.tree),BorderLayout.CENTER);
		changeGroupTree.addActiveAgents(editModel);

		changeGroups.setSelectedIndex(0);

		/* Tab "Intervalle" */
		add(tab=new JPanel(new BorderLayout()),Language.tr("Optimizer.Tabs.UseIntervals"));
		tab.setBorder(BorderFactory.createEmptyBorder());

		JToolBar toolbar=new JToolBar();
		toolbar.setFloatable(false);
		tab.add(toolbar,BorderLayout.NORTH);

		toolbar.add(selectAllButton=new JButton(Language.tr("Dialog.Select.All")));
		selectAllButton.addActionListener(new ButtonListener());
		selectAllButton.setToolTipText(Language.tr("Optimizer.Tabs.UseIntervals.TooltipAll"));
		selectAllButton.setIcon(Images.EDIT_ADD.getIcon());

		toolbar.add(selectNoneButton=new JButton(Language.tr("Dialog.Select.Nothing")));
		selectNoneButton.addActionListener(new ButtonListener());
		selectNoneButton.setToolTipText(Language.tr("Optimizer.Tabs.UseIntervals.TooltipNothing"));
		selectNoneButton.setIcon(Images.EDIT_DELETE.getIcon());

		toolbar.add(selectRangeButton=new JButton(Language.tr("Dialog.Select.Range")));
		selectRangeButton.addActionListener(new ButtonListener());
		selectRangeButton.setToolTipText(Language.tr("Optimizer.Tabs.UseIntervals.TooltipRange"));
		selectRangeButton.setIcon(Images.OPTIMIZER_PAGE_INTERVAL_RANGE.getIcon());

		String[] intervalString=new String[48];
		for (int i=0;i<intervalString.length;i++) intervalString[i]=TimeTools.formatTime(i*1800)+"-"+TimeTools.formatTime((i+1)*1800-1);
		boolean[] intervalSelect=new boolean[48];
		Arrays.fill(intervalSelect,true);
		tab.add(new JScrollPane(intervalTable=new JCheckboxTable(intervalString,intervalSelect,Language.tr("Optimizer.Tabs.UseIntervals.TableCaption"))),BorderLayout.CENTER);

		/* Tab "�bertrag" */
		add(tab=new JPanel(new BorderLayout()),Language.tr("Optimizer.Tabs.CarryOver"));
		tab.setBorder(BorderFactory.createEmptyBorder());

		tab.add(p=new JPanel(),BorderLayout.NORTH);
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		p.setAlignmentX(Component.LEFT_ALIGNMENT);

		p.add(Box.createVerticalStrut(8));

		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.setAlignmentX(0);
		p2.add(new JLabel(Language.tr("Optimizer.CarryOver.Info")));

		p.add(Box.createVerticalStrut(10));

		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.setAlignmentX(0);
		p2.add(new JLabel(Language.tr("Optimizer.CarryOver.SelectStatisticTitle")));

		p.add(p2=new JPanel());
		p2.setLayout(new BoxLayout(p2,BoxLayout.X_AXIS));
		p2.setAlignmentX(0);
		p2.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		p2.add(day0statisticsField=new JTextField(60));
		Dimension d2=day0statisticsField.getPreferredSize();
		Dimension d3=day0statisticsField.getMaximumSize();
		d3.height=d2.height; day0statisticsField.setMaximumSize(d3);
		p2.add(day0statisticsButton=new JButton(Language.tr("Optimizer.CarryOver.SelectStatisticButton")));
		day0statisticsButton.addActionListener(new ButtonListener());
		day0statisticsButton.setToolTipText(Language.tr("Optimizer.CarryOver.SelectStatisticButton.Info"));
		day0statisticsButton.setIcon(Images.GENERAL_SELECT_FILE.getIcon());

		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.setAlignmentX(0);
		p2.add(day0configButton=new JButton(Language.tr("Optimizer.CarryOver.ButtonCanceledCaller")));
		day0configButton.addActionListener(new ButtonListener());
		day0configButton.setToolTipText(Language.tr("Optimizer.CarryOver.ButtonCanceledCaller.Info"));
		day0configButton.setIcon(Images.OPTIMIZER_PAGE_CARRY_OVER.getIcon());

		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.setAlignmentX(0);
		p2.add(additionalDay0Caller=new JButton(Language.tr("Optimizer.CarryOver.Additional")));
		additionalDay0Caller.addActionListener(new ButtonListener());
		additionalDay0Caller.setIcon(Images.OPTIMIZER_PAGE_CARRY_OVER.getIcon());

		tab.add(Box.createVerticalGlue());

		/* Allgemeine Einstellungen */
		setIconAt(0,Images.OPTIMIZER_PAGE_TARGET.getIcon());
		setIconAt(1,Images.OPTIMIZER_PAGE_CONTROL_VARIABLE.getIcon());
		setIconAt(2,Images.OPTIMIZER_PAGE_INTERVAL.getIcon());
		setIconAt(3,Images.OPTIMIZER_PAGE_CARRY_OVER.getIcon());

		setOptimizeSetup(new OptimizeSetup());
	}

	/**
	 * Soll der Knoten selektiert werden?
	 * @param node	Eltern-Name
	 * @param subIndex	Eigener Index
	 * @param parentData	M�gliche Eltern-Bezeichner
	 * @param nodeData	M�gliche eigene Bezeichner
	 * @return	Soll der Knoten selektiert werden?
	 */
	private boolean selectNode(String node, int subIndex, String[] parentData, int[] nodeData) {
		for (int i=0;i<parentData.length;i++) if (node.equalsIgnoreCase(parentData[i]) && subIndex==nodeData[i]-1) return true;
		return false;
	}

	/**
	 * W�hlt in einer {@link CheckBoxTree}-Struktur Eintr�ge gem�� ihren Namen aus
	 * @param root	Wurzelelement der Baumstruktur
	 * @param data	Namen der auszuw�hlenden Eintr�ge
	 */
	private void setTreeSelectFromArray(DefaultMutableTreeNode root, String[] data) {
		String[] parentData;
		int[] nodeData;
		Object[] obj=OptimizeSetup.splitCallcenterAgentGroupData(data);
		parentData=(String[])obj[0];
		nodeData=(int[])obj[1];

		for (int i=0;i<root.getChildCount();i++) {
			if (!(root.getChildAt(i) instanceof DefaultMutableTreeNode)) continue;
			DefaultMutableTreeNode node1=(DefaultMutableTreeNode)root.getChildAt(i);
			if (!(node1.getUserObject() instanceof CheckBoxTree.CheckBoxNode)) continue;
			CheckBoxTree.CheckBoxNode checkBox1=(CheckBoxTree.CheckBoxNode)node1.getUserObject();

			if (node1.getChildCount()==0) {
				checkBox1.setSelected(selectNode(checkBox1.getText(),-1,parentData,nodeData));
				continue;
			}

			int count=0;
			for (int j=0;j<node1.getChildCount();j++) {
				if (!(node1.getChildAt(j) instanceof DefaultMutableTreeNode)) continue;
				DefaultMutableTreeNode node2=(DefaultMutableTreeNode)node1.getChildAt(j);
				if (!(node2.getUserObject() instanceof CheckBoxTree.CheckBoxNode)) continue;
				CheckBoxTree.CheckBoxNode checkBox2=(CheckBoxTree.CheckBoxNode)node2.getUserObject();
				boolean b=selectNode(checkBox1.getText(),j,parentData,nodeData);
				if (b) count++;
				checkBox2.setSelected(b);
			}
			checkBox1.setSelected(count>0);
		}
	}

	/**
	 * Setzen der Optimierer-Konfiguration
	 * @param setup Neue Optimierer Konfiguration
	 */
	public void setOptimizeSetup(OptimizeSetup setup) {
		switch (setup.optimizeProperty) {
		case OPTIMIZE_PROPERTY_SUCCESS_BY_CALL: optProperty.setSelectedIndex(0); break;
		case OPTIMIZE_PROPERTY_SUCCESS_BY_CLIENT: optProperty.setSelectedIndex(1); break;
		case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CALL: optProperty.setSelectedIndex(2); break;
		case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CLIENT: optProperty.setSelectedIndex(3); break;
		case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CALL: optProperty.setSelectedIndex(4); break;
		case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CLIENT: optProperty.setSelectedIndex(5); break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL: optProperty.setSelectedIndex(6); break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL_ALL: optProperty.setSelectedIndex(7); break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT: optProperty.setSelectedIndex(8); break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT_ALL: optProperty.setSelectedIndex(9); break;
		case OPTIMIZE_PROPERTY_WORK_LOAD: optProperty.setSelectedIndex(10); break;
		}
		optPropertyChanged();

		switch (setup.optimizeProperty) {
		case OPTIMIZE_PROPERTY_SUCCESS_BY_CALL:
		case OPTIMIZE_PROPERTY_SUCCESS_BY_CLIENT:
			optValue.setText(NumberTools.formatPercent(setup.optimizeValue)); break;
		case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CALL:
		case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CLIENT:
			optValue.setText(TimeTools.formatTime((int)Math.round(setup.optimizeValue))); break;
		case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CALL:
		case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CLIENT:
			optValue.setText(TimeTools.formatTime((int)Math.round(setup.optimizeValue))); break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL:
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT:
			optValue.setText(NumberTools.formatPercent(setup.optimizeValue)); break;
		case OPTIMIZE_PROPERTY_WORK_LOAD:
			optValue.setText(NumberTools.formatPercent(setup.optimizeValue)); break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL_ALL:
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT_ALL:
			optValue.setText(NumberTools.formatPercent(setup.optimizeValue)); break;
		}

		if (setup.optimizeMaxValue>=0) {
			optValueMaxActive.setSelected(true);
			switch (setup.optimizeProperty) {
			case OPTIMIZE_PROPERTY_SUCCESS_BY_CALL:
			case OPTIMIZE_PROPERTY_SUCCESS_BY_CLIENT:
				optValueMax.setText(NumberTools.formatPercent(setup.optimizeMaxValue)); break;
			case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CALL:
			case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CLIENT:
				optValueMax.setText(TimeTools.formatTime((int)Math.round(setup.optimizeMaxValue))); break;
			case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CALL:
			case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CLIENT:
				optValueMax.setText(TimeTools.formatTime((int)Math.round(setup.optimizeMaxValue))); break;
			case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL:
			case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT:
				optValueMax.setText(NumberTools.formatPercent(setup.optimizeMaxValue)); break;
			case OPTIMIZE_PROPERTY_WORK_LOAD:
				optValueMax.setText(NumberTools.formatPercent(setup.optimizeMaxValue)); break;
			case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL_ALL:
			case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT_ALL:
				optValueMax.setText(NumberTools.formatPercent(setup.optimizeMaxValue)); break;
			}
		} else {
			optValueMaxActive.setSelected(false);
		}

		switch (setup.optimizeByInterval) {
		case OPTIMIZE_BY_INTERVAL_NO: optInterval.setSelectedIndex(0); break;
		case OPTIMIZE_BY_INTERVAL_YES: optInterval.setSelectedIndex(1); break;
		case OPTIMIZE_BY_INTERVAL_IN_ORDER: optInterval.setSelectedIndex(2); break;
		}

		switch (setup.optimizeGroups) {
		case OPTIMIZE_GROUPS_AVERAGE: optGroups.setSelectedIndex(0); break;
		case OPTIMIZE_GROUPS_ALL: optGroups.setSelectedIndex(1); break;
		case OPTIMIZE_GROUPS_SELECTION: optGroups.setSelectedIndex(2); break;
		}

		if (setup.optimizeGroups==OptimizeSetup.OptimizeGroups.OPTIMIZE_GROUPS_SELECTION)
			setTreeSelectFromArray(optGroupTree.root,setup.optimizeGroupNames);

		changeValue.setText(NumberTools.formatPercent(setup.changeValue));
		changeGroups.setSelectedIndex(setup.changeAll?0:1);

		day0statisticsField.setText(setup.uebertragFile);
		uebertrag=OptimizeSetup.cloneUebertrag(setup.uebertrag);
		uebertragAdditionalCaller.clear();
		uebertragAdditionalCaller.addAll(setup.uebertragAdditionalCaller);
		uebertragAdditionalCount.clear();
		uebertragAdditionalCount.addAll(setup.uebertragAdditionalCount);

		groupRestrictionName.clear();
		groupRestrictionMin.clear();
		groupRestrictionMax.clear();
		groupRestrictionName.addAll(setup.groupRestrictionName);
		for (int i=0;i<setup.groupRestrictionMin.size();i++) groupRestrictionMin.add(setup.groupRestrictionMin.get(i).clone());
		for (int i=0;i<setup.groupRestrictionMax.size();i++) groupRestrictionMax.add(setup.groupRestrictionMax.get(i).clone());
		changeRestrictionsLabel.setText((groupRestrictionName.size()>0)?Language.tr("Optimizer.ControlVariable.Restrictions.Info.Yes"):Language.tr("Optimizer.ControlVariable.Restrictions.Info.No"));

		boolean[] newSetup=new boolean[48];
		Arrays.fill(newSetup,true);
		for (int i=0;i<Math.min(setup.optimizeIntervals.densityData.length,newSetup.length);i++) newSetup[i]=(Math.abs(setup.optimizeIntervals.densityData[i])>0.1);
		intervalTable.setSelected(newSetup);

		if (!setup.changeAll) setTreeSelectFromArray(changeGroupTree.root,setup.changeGroups);
	}

	/**
	 * Abfrage der Optimierer-Konfiguration
	 * @return	Liefert im Erfolgsfall ein Objekt vom Typ <code>OptimizeSetup</code>; andernfalls liefert die Funktion <code>null</code>
	 */
	public OptimizeSetup getOptimizeSetup() {
		OptimizeSetup setup=new OptimizeSetup();

		switch (optProperty.getSelectedIndex()) {
		case 0: setup.optimizeProperty=OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_SUCCESS_BY_CALL; break;
		case 1: setup.optimizeProperty=OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_SUCCESS_BY_CLIENT; break;
		case 2: setup.optimizeProperty=OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_WAITING_TIME_BY_CALL; break;
		case 3: setup.optimizeProperty=OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_WAITING_TIME_BY_CLIENT; break;
		case 4: setup.optimizeProperty=OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_STAYING_TIME_BY_CALL; break;
		case 5: setup.optimizeProperty=OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_STAYING_TIME_BY_CLIENT; break;
		case 6: setup.optimizeProperty=OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL; break;
		case 7: setup.optimizeProperty=OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL_ALL; break;
		case 8: setup.optimizeProperty=OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT; break;
		case 9: setup.optimizeProperty=OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT_ALL; break;
		case 10: setup.optimizeProperty=OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_WORK_LOAD; break;
		}

		String s=optValue.getText();
		Double d=0.0;
		Integer in;
		switch (optProperty.getSelectedIndex()) {
		case 0:
		case 1:
			d=NumberTools.getProbability(s);
			if (d==0) {
				MsgBox.error(this,Language.tr("Optimizer.Error.TargetAccessibility.Title"),String.format(Language.tr("Optimizer.Error.TargetAccessibility.Info"),s));
				return null;
			}
			break;
		case 2:
		case 3:
			in=TimeTools.getTime(s);
			if (in==0) {
				MsgBox.error(this,Language.tr("Optimizer.Error.TargetWaitingTime.Title"),String.format(Language.tr("Optimizer.Error.TargetWaitingTime.Info"),s));
				return null;
			}
			d=(double)in;
			break;
		case 4:
		case 5:
			in=TimeTools.getTime(s);
			if (in==0) {
				MsgBox.error(this,Language.tr("Optimizer.Error.TargetResidenceTime.Title"),String.format(Language.tr("Optimizer.Error.TargetResidenceTime.Info"),s));
				return null;
			}
			d=(double)in;
			break;
		case 6:
		case 7:
		case 8:
		case 9:
			d=NumberTools.getProbability(s);
			if (d==0) {
				MsgBox.info(this,Language.tr("Optimizer.Error.TargetServiceLevel.Title"),String.format(Language.tr("Optimizer.Error.TargetServiceLevel.Info"),s));
				return null;
			}
			break;
		case 10:
			d=NumberTools.getProbability(s);
			if (d==0) {
				MsgBox.info(this,Language.tr("Optimizer.Error.TargetWorkLoad.Title"),String.format(Language.tr("Optimizer.Error.TargetWorkLoad.Info"),s));
				return null;
			}
			break;
		}
		setup.optimizeValue=d;

		if (optValueMaxActive.isSelected()) {

			s=optValueMax.getText();
			d=0.0;
			switch (optProperty.getSelectedIndex()) {
			case 0:
			case 1:
				d=NumberTools.getProbability(s);
				if (d==0) {
					MsgBox.info(this,Language.tr("Optimizer.Error.MaxTargetAccessibility.Title"),String.format(Language.tr("Optimizer.Error.MaxTargetAccessibility.Info"),s));
					return null;
				}
				break;
			case 2:
			case 3:
				in=TimeTools.getTime(s);
				if (in==0) {
					MsgBox.info(this,Language.tr("Optimizer.Error.MinTargetWaitingTime.Title"),String.format(Language.tr("Optimizer.Error.MinTargetWaitingTime.Info"),s));
					return null;
				}
				d=(double)in;
				break;
			case 4:
			case 5:
				in=TimeTools.getTime(s);
				if (in==0) {
					MsgBox.info(this,Language.tr("Optimizer.Error.MinTargetResidenceTime.Title"),String.format(Language.tr("Optimizer.Error.MinTargetResidenceTime.Info"),s));
					return null;
				}
				d=(double)in;
				break;
			case 6:
			case 7:
			case 8:
			case 9:
				d=NumberTools.getProbability(s);
				if (d==0) {
					MsgBox.info(this,Language.tr("Optimizer.Error.MaxTargetServiceLevel.Title"),String.format(Language.tr("Optimizer.Error.MaxTargetServiceLevel.Info"),s));
					return null;
				}
				break;
			case 10:
				d=NumberTools.getProbability(s);
				if (d==0) {
					MsgBox.info(this,Language.tr("Optimizer.Error.MinTargetWorkLoad.Title"),String.format(Language.tr("Optimizer.Error.MinTargetWorkLoad.Info"),s));
					return null;
				}
				break;
			}
			setup.optimizeMaxValue=d;
		} else {
			setup.optimizeMaxValue=-1;
		}

		switch (optInterval.getSelectedIndex()) {
		case 0: setup.optimizeByInterval=OptimizeSetup.OptimizeInterval.OPTIMIZE_BY_INTERVAL_NO; break;
		case 1: setup.optimizeByInterval=OptimizeSetup.OptimizeInterval.OPTIMIZE_BY_INTERVAL_YES; break;
		case 2: setup.optimizeByInterval=OptimizeSetup.OptimizeInterval.OPTIMIZE_BY_INTERVAL_IN_ORDER; break;
		}

		switch (optGroups.getSelectedIndex()) {
		case 0: setup.optimizeGroups=OptimizeSetup.OptimizeGroups.OPTIMIZE_GROUPS_AVERAGE; break;
		case 1: setup.optimizeGroups=OptimizeSetup.OptimizeGroups.OPTIMIZE_GROUPS_ALL; break;
		case 2: setup.optimizeGroups=OptimizeSetup.OptimizeGroups.OPTIMIZE_GROUPS_SELECTION; break;
		}

		if (optGroups.getSelectedIndex()==2) {
			String[] l=optGroupTree.getSelected(false);
			if (l.length==0) {
				MsgBox.info(this,Language.tr("Optimizer.Error.NoGroupsSelected.Title"),Language.tr("Optimizer.Error.NoGroupsSelected.Info"));
				return null;
			}
			setup.optimizeGroupNames=l;
		}

		d=NumberTools.getExtProbability(changeValue.getText());
		if (d==0) {
			MsgBox.info(this,Language.tr("Optimizer.Error.ChangeRate.Title"),String.format(Language.tr("Optimizer.Error.ChangeRate.Info"),changeValue.getText()));
			return null;
		}
		setup.changeValue=d;

		setup.changeAll=(changeGroups.getSelectedIndex()==0);

		if (changeGroups.getSelectedIndex()==1) {
			String[] l=changeGroupTree.getSelected(true);
			if (l.length==0) {
				MsgBox.info(this,Language.tr("Optimizer.Error.NoOptimizationAgentsGroupSelected.Title"),Language.tr("Optimizer.Error.NoOptimizationAgentsGroupSelected.Info"));
				return null;
			}
			setup.changeGroups=l;
		}

		setup.uebertragFile=day0statisticsField.getText();
		setup.uebertrag=OptimizeSetup.cloneUebertrag(uebertrag);

		setup.uebertragAdditionalCaller.clear();
		setup.uebertragAdditionalCaller.addAll(uebertragAdditionalCaller);
		setup.uebertragAdditionalCount.clear();
		setup.uebertragAdditionalCount.addAll(uebertragAdditionalCount);

		setup.groupRestrictionName.clear();
		setup.groupRestrictionMin.clear();
		setup.groupRestrictionMax.clear();
		setup.groupRestrictionName.addAll(groupRestrictionName);
		for (int i=0;i<groupRestrictionMin.size();i++) setup.groupRestrictionMin.add(groupRestrictionMin.get(i).clone());
		for (int i=0;i<groupRestrictionMax.size();i++) setup.groupRestrictionMax.add(groupRestrictionMax.get(i).clone());

		boolean[] intervals=intervalTable.getSelected();
		for (int i=0;i<Math.min(intervals.length,setup.optimizeIntervals.densityData.length);i++)
			setup.optimizeIntervals.densityData[i]=intervals[i]?1.0:0.0;

		return setup;
	}

	/**
	 * Zeigt einen Dateiauswahldialog an und l�dt dann die gew�hlte Optimierereinstellungendatei.
	 * @return	Wurde der Auswahldialog abgebrochen oder ist das Laden fehlgeschlagen, so wird <code>false</code> geliefert, sonst <code>true</code>
	 */
	public boolean loadOptimizeSetup() {
		final File file=XMLTools.showLoadDialog(getParent(),Language.tr("Optimizer.LoadSetup.Title"));
		if (file==null) return false;
		return loadOptimizeSetup(file);
	}

	/**
	 * L�dt eine Optimierereinstellungendatei.
	 * @param file	Zu ladende Datei
	 * @return	Gibt an, ob das Laden erfolgreich war.
	 */
	public boolean loadOptimizeSetup(File file) {
		final OptimizeSetup setup=new OptimizeSetup();
		String s=setup.loadFromFile(file);
		if (s!=null) {
			MsgBox.error(this,Language.tr("Optimizer.LoadSetup.ErrorTitle"),s);
			return false;
		}

		setOptimizeSetup(setup);
		return true;
	}

	/**
	 * Zeigt einen Dateiauswahldialog an und speichert dann die aktuellen Optimierereinstellungen in der gew�hlten Datei.
	 * @return	Wurde der Auswahldialog abgebrochen oder ist das Speichern fehlgeschlagen, so wird <code>false</code> geliefert, sonst <code>true</code>
	 */
	public boolean saveOptimizeSetup() {
		final OptimizeSetup setup=getOptimizeSetup();
		if (setup==null) return false;

		final File file=XMLTools.showSaveDialog(getParent(),Language.tr("Optimizer.SaveSetup.Title"));
		if (file==null) return false;

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(this,file)) return false;
		}

		return setup.saveToFile(file);
	}

	/**
	 * Reagiert auf eine ver�nderte Auswahl in
	 * {@link OptimizeEditPanel#optProperty},
	 * {@link OptimizeEditPanel#optGroups} und
	 * {@link OptimizeEditPanel#changeGroups}.
	 * @see OptimizeEditPanel#optProperty
	 * @see OptimizeEditPanel#optGroups
	 * @see OptimizeEditPanel#changeGroups
	 */
	private final class DialogElementListener implements ActionListener {
		/**
		 * Ver�nderte Auswahl f�r welche Kunden- oder Agentengruppen der Zielwert erreicht werden muss
		 * @see OptimizeEditPanel#optGroups
		 */
		private void optGroupsChanged() {
			optGroupTree.tree.setEnabled(optGroups.getSelectedIndex()==2);

			if (optProperty.getSelectedIndex()==lastValue.length-1) {
				/* Agenten */
				optGroupTree.addActiveCallcenter(editModel);
			} else {
				/* Anrufer */
				optGroupTree.addActiveCaller(editModel);
			}
		}

		/**
		 * Ver�nderte Auswahl, ob alle oder nur bestimmte Gruppen (Kunden oder Agenten) ver�ndert werden sollen
		 * @see OptimizeEditPanel#changeGroups
		 */
		private void changeGroupsChanged() {
			changeGroupTree.tree.setEnabled(changeGroups.getSelectedIndex()==1);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==optProperty) {optPropertyChanged(); return;}
			if (e.getSource()==optGroups) {optGroupsChanged(); return;}
			if (e.getSource()==changeGroups) {changeGroupsChanged(); return;}
		}
	}

	/**
	 * Zeigt einen Auswahldialog zur Auswahl einer �bertrags-Statistik-Datei an.
	 * @see #day0statisticsField
	 * @see #day0statisticsButton
	 */
	private void selectDay0Statistics() {
		File file=XMLTools.showLoadDialog(this,Language.tr("Optimizer.LoadStatistic"));
		if (file==null) return;

		if (!file.exists()) {
			MsgBox.warning(this,Language.tr("Optimizer.Error.StatisticFileDoesNotExist.Title"),String.format(Language.tr("Optimizer.Error.StatisticFileDoesNotExist.Info"),file.toString()));
			return;
		}

		day0statisticsField.setText(file.toString());
	}

	/**
	 * Ver�nderte Optimierungseigenschaft
	 * @see #optProperty
	 */
	private void optPropertyChanged() {
		if (lastProperty>=0) {
			lastValue[lastProperty]=optValue.getText();
			lastMaxValue[lastProperty]=optValueMax.getText();
		}
		boolean needTreeUpdate=(lastProperty==-1 ||  (optProperty.getSelectedIndex()==lastValue.length-1 && lastProperty!=lastValue.length-1) || (lastProperty==lastValue.length-1 && optProperty.getSelectedIndex()!=lastValue.length-1));
		lastProperty=optProperty.getSelectedIndex();
		if (lastProperty>=0) {
			optValue.setText(lastValue[Math.max(0,Math.min(lastProperty,lastValue.length-1))]);
			optValueMax.setText(lastMaxValue[Math.max(0,Math.min(lastProperty,lastMaxValue.length-1))]);
		}

		String info2="", info3="";
		switch (lastProperty) {
		case 0:
		case 1:
			optValueLabel.setText(Language.tr("Optimizer.OptimizeProperty.TargetForLabel.Accessibility")+":");
			optValue.setToolTipText(Language.tr("Optimizer.OptimizeProperty.TargetForLabel.Accessibility.Tooltip"));
			info2=Language.tr("Optimizer.OptimizeProperty.TargetForLabel.Accessibility.Info");
			optValueMaxActive.setText(Language.tr("Optimizer.OptimizeProperty.TargetMaxForLabel.Accessibility")+":");
			optValueMax.setToolTipText(Language.tr("Optimizer.OptimizeProperty.TargetMaxForLabel.Accessibility.Tooltip"));
			info3=Language.tr("Optimizer.OptimizeProperty.TargetMaxForLabel.Accessibility.Info");
			break;
		case 2:
		case 3:
			optValueLabel.setText(Language.tr("Optimizer.OptimizeProperty.TargetForLabel.AverageWaitingTime")+":");
			optValue.setToolTipText(Language.tr("Optimizer.OptimizeProperty.TargetForLabel.AverageWaitingTime.Tooltip"));
			info2=Language.tr("Optimizer.OptimizeProperty.TargetForLabel.AverageWaitingTime.Info");
			optValueMaxActive.setText(Language.tr("Optimizer.OptimizeProperty.TargetMaxForLabel.AverageWaitingTime")+":");
			optValueMax.setToolTipText(Language.tr("Optimizer.OptimizeProperty.TargetMaxForLabel.AverageWaitingTime.Tooltip"));
			info3=Language.tr("Optimizer.OptimizeProperty.TargetMaxForLabel.AverageWaitingTime.Info");
			break;
		case 4:
		case 5:
			optValueLabel.setText(Language.tr("Optimizer.OptimizeProperty.TargetForLabel.AverageResidenceTime")+":");
			optValue.setToolTipText(Language.tr("Optimizer.OptimizeProperty.TargetForLabel.AverageResidenceTime.Tooltip"));
			info2=Language.tr("Optimizer.OptimizeProperty.TargetForLabel.AverageResidenceTime.Info");
			optValueMaxActive.setText(Language.tr("Optimizer.OptimizeProperty.TargetMaxForLabel.AverageResidenceTime")+":");
			optValue.setToolTipText(Language.tr("Optimizer.OptimizeProperty.TargetMaxForLabel.AverageResidenceTime.Tooltip"));
			info3=Language.tr("Optimizer.OptimizeProperty.TargetMaxForLabel.AverageResidenceTime.Info");
			break;
		case 6:
		case 7:
		case 8:
		case 9:
			optValueLabel.setText(Language.tr("Optimizer.OptimizeProperty.TargetForLabel.ServiceLevel")+":");
			optValue.setToolTipText(Language.tr("Optimizer.OptimizeProperty.TargetForLabel.ServiceLevel.Tooltip"));
			info2=Language.tr("Optimizer.OptimizeProperty.TargetForLabel.ServiceLevel.Info");
			optValueMaxActive.setText(Language.tr("Optimizer.OptimizeProperty.TargetMaxForLabel.ServiceLevel")+":");
			optValueMax.setToolTipText(Language.tr("Optimizer.OptimizeProperty.TargetMaxForLabel.ServiceLevel.Tooltip"));
			info3=Language.tr("Optimizer.OptimizeProperty.TargetMaxForLabel.ServiceLevel.Info");
			break;
		case 10:
			optValueLabel.setText(Language.tr("Optimizer.OptimizeProperty.TargetForLabel.WorkLoad")+":");
			optValue.setToolTipText(Language.tr("Optimizer.OptimizeProperty.TargetForLabel.WorkLoad.Tooltip"));
			info2=Language.tr("Optimizer.OptimizeProperty.TargetForLabel.WorkLoad.Info");
			optValueMaxActive.setText(Language.tr("Optimizer.OptimizeProperty.TargetMaxForLabel.WorkLoad")+":");
			optValueMax.setToolTipText(Language.tr("Optimizer.OptimizeProperty.TargetMaxForLabel.WorkLoad.Tooltip"));
			info3=Language.tr("Optimizer.OptimizeProperty.TargetMaxForLabel.WorkLoad.Info");
			break;
		}
		if (!info2.isEmpty()) info2="("+info2+")";
		optValueLabel2.setText("<html><body style='font-size: 95%;'>"+info2+"</body></html>");
		if (!info3.isEmpty()) info3="("+info3+")";
		optValueLabel3.setText("<html><body style='font-size: 95%;'>"+info3+"</body></html>");

		if (needTreeUpdate) {
			int index=optGroups.getSelectedIndex();
			String[] s=(lastProperty==lastValue.length-1)?groupsAgents:groupsCaller;
			optGroups.removeAllItems();
			for (int i=0;i<s.length;i++) optGroups.addItem(s[i]);
			optGroups.setSelectedIndex(index);
		}
	}

	/**
	 * Reagiert auf Klicks auf die verschiedenen Schaltfl�chen
	 */
	private final class ButtonListener implements ActionListener {
		/**
		 * Zeigt den Dialog zur Konfiguration des �bertrag der Warteabbrecher an.
		 * @see OptimizeEditPanel#day0configButton
		 * @see ConnectedUebertragEditDialog
		 */
		private void editDay0Statistics() {
			File statisticFile=null;
			if (!day0statisticsField.getText().trim().isEmpty()) {
				statisticFile=new File(day0statisticsField.getText());
				if (!statisticFile.exists()) statisticFile=null;
			}
			ConnectedUebertragEditDialog editDialog=new ConnectedUebertragEditDialog(owner,helpLink.pageConnectedModal,editModel,statisticFile,uebertrag);
			editDialog.setVisible(true);
		}

		/**
		 * Zeigt einen Dialog zur Konfiguration des zus�tzlichen �bertrags an.
		 * @see OptimizeEditPanel#additionalDay0Caller
		 * @see AdditionalCallerSetupDialog
		 */
		private void editAdditionalCaller() {
			AdditionalCallerSetupDialog dialog=new AdditionalCallerSetupDialog(owner,helpLink.pageConnectedModal,editModel,uebertragAdditionalCaller,uebertragAdditionalCount);
			dialog.setVisible(true);
			if (dialog.getClosedBy()==BaseEditDialog.CLOSED_BY_OK) {
				uebertragAdditionalCaller.clear();
				uebertragAdditionalCaller.addAll(dialog.getCallerNames());
				uebertragAdditionalCount.clear();
				uebertragAdditionalCount.addAll(dialog.getCallerCount());
			}
		}

		/**
		 * Zeigt einen Dialog zur Auswahl des Bereichs, der bei der Optimierung
		 * ber�cksichtigt werden soll, an.
		 * @see OptimizeEditPanel#selectRangeButton
		 * @see RangeSelectDialog
		 */
		private void setupRange() {
			RangeSelectDialog dialog=new RangeSelectDialog(owner,helpLink.pageOptimizeModal,RangeSelectDialog.Mode.MODE_OPTIMIZE);
			dialog.setVisible(true);
			if (dialog.getClosedBy()==BaseEditDialog.CLOSED_BY_OK) intervalTable.selectRange(dialog.getMin(),dialog.getMax());
		}

		/**
		 * Zeigt einen Dialog zur Konfiguration der gruppen-abh�ngigen Einschr�nkungen f�r die Stellgr��e an.
		 * @see OptimizeEditPanel#changeRestictionsButton
		 * @see OptimizerRestrictionsDialog
		 */
		private void editRestrictions() {
			OptimizerRestrictionsDialog dialog=new OptimizerRestrictionsDialog(owner,helpLink.pageOptimizeModal,editModel,groupRestrictionName,groupRestrictionMin,groupRestrictionMax);
			dialog.setVisible(true);
			if (dialog.getClosedBy()==BaseEditDialog.CLOSED_BY_OK) {
				groupRestrictionName.clear();
				groupRestrictionMin.clear();
				groupRestrictionMax.clear();
				groupRestrictionName.addAll(dialog.names);
				for (int i=0;i<dialog.min.size();i++) groupRestrictionMin.add(dialog.min.get(i).clone());
				for (int i=0;i<dialog.max.size();i++) groupRestrictionMax.add(dialog.max.get(i).clone());
				changeRestrictionsLabel.setText((groupRestrictionName.size()>0)?Language.tr("Optimizer.ControlVariable.Restrictions.Info.Yes"):Language.tr("Optimizer.ControlVariable.Restrictions.Info.No"));
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==day0statisticsButton) {selectDay0Statistics(); return;}
			if (e.getSource()==day0configButton) {editDay0Statistics(); return;}
			if (e.getSource()==additionalDay0Caller) {editAdditionalCaller(); return;}
			if (e.getSource()==selectAllButton) {intervalTable.selectAll(); return;}
			if (e.getSource()==selectNoneButton) {intervalTable.selectNone(); return;}
			if (e.getSource()==selectRangeButton) {setupRange(); return;}
			if (e.getSource()==changeRestictionsButton) {editRestrictions(); return;}
		}
	}
}