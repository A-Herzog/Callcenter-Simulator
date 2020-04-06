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
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.swing.JDataDistributionEditPanel;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import tools.SetupData;
import ui.HelpLink;
import ui.editor.events.RenameEvent;
import ui.editor.events.RenameListener;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;
import ui.model.CallcenterModelSkillLevel;

/**
 * Diese Klasse kapselt einen kompletten Bearbeitungsdialog für Agentengruppen.
 * @author Alexander Herzog
 * @version 1.0
 * @see CallcenterModelAgent
 */
public class AgentEditDialog extends BaseEditDialog {
	private static final long serialVersionUID = 1514347570688540353L;

	private final CallcenterModelAgent agent;
	private final CallcenterModelCallcenter callcenter;
	private final List<CallcenterModelAgent> callcenterAgents;
	private final CallcenterModel model;

	private final List<RenameListener> listener;

	private JComboBox<String> comboBox;
	private JPanel main;

	private JTextField count;

	private JTextField workingTimeStart;
	private JTextField workingTimeEnd;
	private JCheckBox workingNoEndTime;

	private JDataDistributionEditPanel distributionPanel;
	private JComboBox<String> countComboBox;
	private JCheckBox workingNoEndTime2;
	private JComboBox<String> preferredShiftLength1, preferredShiftLength2;
	private JComboBox<String> minimumShiftLength1, minimumShiftLength2;

	private JTextField byCallerCount;

	private JButton tools1, tools2;
	private JMenuItem efficiency1, efficiency2;
	private JMenuItem addition1, addition2;
	private JPopupMenu tools1Popup, tools2Popup;
	private JMenuItem tools1item, tools2item, tools3item;
	private String[] byCaller;
	private JTextField[] byCallerRate;

	private JComboBox<String> skillLevelList;
	private JButton skillEditButton;
	private JLabel skillLevelInfo;

	private JTextField costPerWorkingHour;
	private JTextField[] costPerCall;
	private JTextField[] costPerCallMinute;

	private int indexOfGroup;
	private final String[] agentGroupNames;
	private final JPopupMenu popupMenu;
	private final JMenuItem[] applyThisPage;

	private final HelpLink helpLink;
	private final boolean showNumberOfAgentsTab;

	/**
	 * Konstruktor der Klasse <code>AgentEditDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param agent	Objekt vom Typ <code>CallcenterModelAgent</code> welches die Agentengruppen-Daten enthält (beim Klicken auf "Ok" wird auch dieses Objekt verändert)
	 * @param callcenter	Objekt vom Typ <code>CallcenterModelCallcenter</code>, welches das Callcenter angibt, in dem die Agentengruppe arbeiter
	 * @param callcenterAgents	Liste der Agentengruppen in dem Callcenter
	 * @param model	Objekt vom Typ <code>CallcenterModel</code>, welches das globale Modell enthält
	 * @param callerTypeNames	Liste mit allen Kundentypen-Namen (für die Agenten-Skills)
	 * @param groupName	Name der Agentengruppe (wird im Fenstertitel angezeigt)
	 * @param groupIndex	Index der Agentengruppe bezogen auf das aktuelle Callcenter (kann -1 sein, wenn es sich um eine neu angelegte Gruppe handelt)
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param previous Button "Vorherige Agentengruppe" anzeigen
	 * @param next Button "Nächste Agentengruppe" anzeigen
	 * @param helpLink Verknüpfung mit der Online-Hilfe
	 * @param showNumberOfAgentsTab	Gibt an, ob der Tab zur Auswahl, wie viele Agenten in der Gruppe sein sollen und wie diese verteilt sein sollen, angezeigt werden soll.
	 */
	public AgentEditDialog(Window owner, CallcenterModelAgent agent, CallcenterModelCallcenter callcenter, List<CallcenterModelAgent> callcenterAgents, CallcenterModel model, String[] callerTypeNames, String groupName, int groupIndex, boolean readOnly, boolean previous, boolean next, HelpLink helpLink, boolean showNumberOfAgentsTab) {
		super(owner,Language.tr("Editor.AgentsGroup.Title")+" ["+groupName+"]",callerTypeNames,readOnly,helpLink.dialogAgents);
		this.agent=agent;
		this.callcenter=callcenter;
		this.callcenterAgents=callcenterAgents;
		this.model=model;
		this.helpLink=helpLink;
		this.showNumberOfAgentsTab=showNumberOfAgentsTab;
		listener=new ArrayList<RenameListener>();

		indexOfGroup=-1;
		List<String> temp=new ArrayList<String>();
		for (CallcenterModelCallcenter c : model.callcenter) {
			if (c==callcenter) {
				for (int i=0;i<callcenterAgents.size();i++) {
					if (i==groupIndex) indexOfGroup=temp.size();
					temp.add(c.name+" - "+String.format(Language.tr("Editor.Callcenter.AgentGroupNr"),i+1));
				}
			} else {
				for (int i=0;i<c.agents.size();i++) {
					temp.add(c.name+" - "+String.format(Language.tr("Editor.Callcenter.AgentGroupNr"),i+1));
				}
			}
		}
		agentGroupNames=temp.toArray(new String[0]);

		popupMenu=new JPopupMenu();
		applyThisPage=new JMenuItem[agentGroupNames.length+1];
		if (!readOnly && agentGroupNames.length>1) {
			buildMenu();
			addUserButtons(new String[]{""}, new String[]{Language.tr("Editor.AgentsGroup.Apply.Info")}, new URL[]{Images.GENERAL_TOOLS.getURL()}, new Runnable[]{new ToolsButtonHandler()});
		}

		String previousText=null;
		if (previous) previousText=readOnly?Language.tr("Editor.AgentsGroup.Move.ViewPrevious"):Language.tr("Editor.AgentsGroup.Move.EditPrevious");
		String nextText=null;
		if (next) nextText=readOnly?Language.tr("Editor.AgentsGroup.Move.ViewNext"):Language.tr("Editor.AgentsGroup.Move.EditNext");
		createTabsGUI(groupName,null,Language.tr("Editor.AgentsGroup.Active"),agent.active,700,575,previousText,nextText);

		if (!readOnly && agentGroupNames.length>1) {
			getUserButton(0).setVisible(false);
			tabs.addChangeListener(new ChangeListener() {@Override public void stateChanged(ChangeEvent e) {getUserButton(0).setVisible(tabs.getSelectedIndex()==2);}});
		}
	}

	private void buildMenu() {
		final Icon agents=Images.EDITOR_AGENTS.getIcon();

		for (int i=0;i<agentGroupNames.length;i++) {
			popupMenu.add(applyThisPage[i]=new JMenuItem(agentGroupNames[i]));
			applyThisPage[i].setIcon(agents);
			applyThisPage[i].setEnabled(i!=indexOfGroup);
			applyThisPage[i].addActionListener(new PopupActionListener());
		}
		if (agentGroupNames.length>2) {
			popupMenu.addSeparator();
			popupMenu.add(applyThisPage[agentGroupNames.length]=new JMenuItem(Language.tr("Editor.AgentsGroup.Apply.ForAllGroups")));
			applyThisPage[agentGroupNames.length].setIcon(agents);
			applyThisPage[agentGroupNames.length].addActionListener(new PopupActionListener());
		}
	}

	/**
	 * Die hier registrierten Listener werden benachrichtigt, wenn
	 * in dem Dialog ein Skill-Level umbenannt wird.
	 * @param listener	Listener für Skill-Level-Namensänderungen
	 * @see #removeSkillLevelRenameListener(RenameListener)
	 */
	public void addSkillLevelRenameListener(final RenameListener listener) {
		this.listener.add(listener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der Listener die über
	 * Skill-Level-Namensänderungen benachrichtigt werden sollen.
	 * @param listener	Listener für Skill-Level-Namensänderungen
	 * @return	Gibt an, ob der Listener erfolgreich deregistriert werden konnte
	 * @see #addSkillLevelRenameListener(RenameListener)
	 */
	public boolean removeSkillLevelRenameListener(final RenameListener listener) {
		return this.listener.remove(listener);
	}

	@Override
	protected void createTabs(JTabbedPane tabs) {
		JPanel content,p,p2,p3,p4;
		JButton b;
		String[] t;

		/* Dialogseite "Arbeitszeiten" */
		content=new JPanel(new BorderLayout());
		if (showNumberOfAgentsTab) tabs.addTab(Language.tr("Editor.AgentsGroup.Tabs.WorkingTimes"),content);
		content.setLayout(new BorderLayout());

		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		p.add(comboBox=new JComboBox<String>());
		comboBox.addItem(Language.tr("Editor.AgentsGroup.Mode.Fixed"));
		comboBox.addItem(Language.tr("Editor.AgentsGroup.Mode.Distribution"));
		comboBox.addItem(Language.tr("Editor.AgentsGroup.Mode.ByClients"));
		comboBox.addActionListener(new AgentTypeComboBoxActionListener());
		comboBox.setEnabled(!readOnly);
		comboBox.setRenderer(new IconListCellRenderer(new Images[] {
				Images.EDITOR_AGENTS_MODE_FIXED,
				Images.EDITOR_AGENTS_MODE_DISTRIBUTION,
				Images.EDITOR_AGENTS_MODE_BY_CLIENT
		}));

		content.add(main=new JPanel(new CardLayout()),BorderLayout.CENTER);

		/* Agenten mit festen Arbeiszeiten */
		main.add(p=new JPanel(),comboBox.getItemAt(0));
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
		p.add(p2=new JPanel(new GridLayout(10,2)));

		count=addInputLine(p2,Language.tr("Editor.AgentsGroup.NumberOfAgents"),Math.max(1,agent.count));
		count.setEnabled(!readOnly);
		count.addKeyListener(new DialogElementListener());
		workingTimeStart=addInputLine(p2,Language.tr("Editor.AgentsGroup.WorkingTimeStart"),TimeTools.formatTime(agent.workingTimeStart));
		workingTimeStart.setEnabled(!readOnly);
		workingTimeStart.addKeyListener(new DialogElementListener());
		workingTimeEnd=addInputLine(p2,Language.tr("Editor.AgentsGroup.WorkingTimeEnd"),TimeTools.formatTime(agent.workingTimeEnd));
		workingTimeEnd.setEnabled(!readOnly);
		p2.add(workingNoEndTime=new JCheckBox(Language.tr("Editor.AgentsGroup.WorkingTimeOpenEnd")));
		workingNoEndTime.setEnabled(!readOnly);
		workingNoEndTime.setSelected(agent.workingNoEndTime);
		workingTimeEnd.addKeyListener(new DialogElementListener());
		p.add(Box.createVerticalGlue());

		/* Vorgegebene Verteilung über den Tag */
		main.add(p=new JPanel(new BorderLayout()),comboBox.getItemAt(1));

		p.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		p3.setLayout(new BoxLayout(p3,BoxLayout.Y_AXIS));

		p3.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(countComboBox=new JComboBox<String>());
		countComboBox.addItem(Language.tr("Editor.AgentsGroup.Values24"));
		countComboBox.addItem(Language.tr("Editor.AgentsGroup.Values48"));
		countComboBox.addItem(Language.tr("Editor.AgentsGroup.Values96"));
		countComboBox.setEnabled(!readOnly);
		p2.add(workingNoEndTime2=new JCheckBox(Language.tr("Editor.AgentsGroup.Shift.LastIsOpenEnd")));
		workingNoEndTime2.setBorder(BorderFactory.createEmptyBorder());
		workingNoEndTime2.setEnabled(!readOnly);
		workingNoEndTime2.setSelected(agent.lastShiftIsOpenEnd);

		p3.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(new JLabel(Language.tr("Editor.AgentsGroup.Shift.MinimumShiftLength")+":"));
		t=new String[49];
		String u=TimeTools.formatTime(1800*model.minimumShiftLength);
		if (model.minimumShiftLength==1) u=Language.tr("Editor.GeneralData.GlobalParameters.MinimumShiftLength.NoRestrictions");
		t[0]=Language.tr("Editor.AgentsGroup.Shift.MinimumShiftLength.Global")+" ("+u+")";
		t[1]=Language.tr("Editor.GeneralData.GlobalParameters.MinimumShiftLength.NoRestrictions");
		for (int i=2;i<t.length;i++) t[i]=TimeTools.formatTime(1800*i);
		p2.add(minimumShiftLength1=new JComboBox<String>(t));
		minimumShiftLength1.setSelectedIndex(Math.max(0,agent.minimumShiftLength));
		minimumShiftLength1.setEnabled(!readOnly);
		p2.add(new JLabel(Language.tr("Editor.AgentsGroup.Shift.PreferredShiftLength")+":"));
		t=new String[49];
		t[0]=Language.tr("Editor.AgentsGroup.Shift.PreferredShiftLength.Global")+" ("+TimeTools.formatTime(1800*model.preferredShiftLength)+")";
		for (int i=1;i<t.length;i++) t[i]=TimeTools.formatTime(1800*i);
		p2.add(preferredShiftLength1=new JComboBox<String>(t));
		preferredShiftLength1.setSelectedIndex(Math.max(0,agent.preferredShiftLength));
		preferredShiftLength1.setEnabled(!readOnly);

		p3.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(b=new JButton(Language.tr("Editor.AgentsGroup.Shift.ShowPlan")));
		b.setIcon(Images.EDITOR_SHIFT_PLAN.getIcon());
		b.addActionListener(new ShiftPlanPreviewButtonActionListener(false));
		p2.add(tools1=new JButton(Language.tr("Dialog.Button.Tools")));
		tools1.addActionListener(new ToolPopupListener());
		tools1.setIcon(Images.GENERAL_SETUP.getIcon());
		DataDistributionImpl dist=null;
		if (agent.countPerInterval24!=null) {dist=agent.countPerInterval24; countComboBox.setSelectedIndex(0);}
		if (agent.countPerInterval48!=null) {dist=agent.countPerInterval48; countComboBox.setSelectedIndex(1);}
		if (agent.countPerInterval96!=null) {dist=agent.countPerInterval96; countComboBox.setSelectedIndex(2);}
		countComboBox.addActionListener(new CountComboBoxActionListener());
		p.add(distributionPanel=new JDataDistributionEditPanel(dist,JDataDistributionEditPanel.PlotMode.PLOT_DENSITY,!readOnly,readOnly?0:1,true));
		distributionPanel.setImageSaveSize(SetupData.getSetup().imageSize);

		tools1Popup=new JPopupMenu();
		tools1Popup.add(tools1item=new JMenuItem(Language.tr("Editor.AgentsGroup.Tools.ChangeAllIntervals")));
		tools1item.addActionListener(new ToolPopupListener());
		tools1item.setEnabled(!readOnly);
		tools1item.setIcon(Images.GENERAL_TOOLS.getIcon());
		tools1Popup.add(tools2item=new JMenuItem(Language.tr("Editor.AgentsGroup.Tools.DistribuionByClientArrivals")));
		tools2item.addActionListener(new ToolPopupListener());
		tools2item.setEnabled(!readOnly);
		tools2item.setIcon(Images.EDITOR_CALLER.getIcon());
		tools1Popup.addSeparator();
		tools1Popup.add(efficiency1=new JMenuItem(Language.tr("Editor.AgentsGroup.Tools.SetupEfficiency")));
		efficiency1.addActionListener(new ToolPopupListener());
		efficiency1.setIcon(Images.EDITOR_AGENTS_EFFICIENCY.getIcon());
		tools1Popup.add(addition1=new JMenuItem(Language.tr("Editor.AgentsGroup.Tools.SetupSurcharge")));
		addition1.addActionListener(new ToolPopupListener());
		addition1.setIcon(Images.EDITOR_AGENTS_ADDITION.getIcon());

		/* Verteilung über den Tag gemäß Kundenankünften */
		main.add(p=new JPanel(new BorderLayout()),comboBox.getItemAt(2));

		p.add(p3=new JPanel(),BorderLayout.NORTH);
		p3.setLayout(new BoxLayout(p3,BoxLayout.Y_AXIS));

		p3.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		byCallerCount=addInputLine(p2,Language.tr("Editor.AgentsGroup.AvailableAgentsHalfHours")+":",Math.max(1,agent.byCallersAvailableHalfhours));
		byCallerCount.setEnabled(!readOnly);
		byCallerCount.addKeyListener(new DialogElementListener());
		p2.add(tools2=new JButton(Language.tr("Dialog.Button.Tools")));
		tools2.addActionListener(new ToolPopupListener());
		tools2.setIcon(Images.GENERAL_SETUP.getIcon());
		p2.add(b=new JButton(Language.tr("Editor.AgentsGroup.Shift.ShowPlan")));
		b.setIcon(Images.EDITOR_SHIFT_PLAN.getIcon());
		b.addActionListener(new ShiftPlanPreviewButtonActionListener(true));

		p3.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(new JLabel(Language.tr("Editor.AgentsGroup.Shift.MinimumShiftLength")+":"));
		t=new String[49];
		u=TimeTools.formatTime(1800*model.minimumShiftLength);
		if (model.minimumShiftLength==1) u=Language.tr("Editor.GeneralData.GlobalParameters.MinimumShiftLength.NoRestrictions");
		t[0]=Language.tr("Editor.AgentsGroup.Shift.MinimumShiftLength.Global")+" ("+u+")";
		t[1]=Language.tr("Editor.GeneralData.GlobalParameters.MinimumShiftLength.NoRestrictions");
		for (int i=2;i<t.length;i++) t[i]=TimeTools.formatTime(1800*i);
		p2.add(minimumShiftLength2=new JComboBox<String>(t));
		minimumShiftLength2.setSelectedIndex(Math.max(0,agent.minimumShiftLength));
		minimumShiftLength2.setEnabled(!readOnly);
		p2.add(new JLabel(Language.tr("Editor.AgentsGroup.Shift.PreferredShiftLength")+":"));
		t=new String[49];
		t[0]=Language.tr("Editor.AgentsGroup.Shift.PreferredShiftLength.Global")+" ("+TimeTools.formatTime(1800*model.preferredShiftLength)+")";
		for (int i=1;i<t.length;i++) t[i]=TimeTools.formatTime(1800*i);
		p2.add(preferredShiftLength2=new JComboBox<String>(t));
		preferredShiftLength2.setSelectedIndex(Math.max(0,agent.preferredShiftLength));
		preferredShiftLength2.setEnabled(!readOnly);

		tools2Popup=new JPopupMenu();
		tools2Popup.add(tools3item=new JMenuItem(Language.tr("Editor.AgentsGroup.CountFromCalculatedLoad")));
		tools3item.addActionListener(new ToolPopupListener());
		tools3item.setEnabled(!readOnly);
		tools3item.setIcon(Images.EDITOR_AGENTS_CALCULATE.getIcon());
		tools2Popup.addSeparator();
		tools2Popup.add(efficiency2=new JMenuItem(Language.tr("Editor.AgentsGroup.Tools.SetupEfficiency")));
		efficiency2.addActionListener(new ToolPopupListener());
		efficiency2.setIcon(Images.EDITOR_AGENTS_EFFICIENCY.getIcon());
		tools2Popup.add(addition2=new JMenuItem(Language.tr("Editor.AgentsGroup.Tools.SetupSurcharge")));
		addition2.addActionListener(new ToolPopupListener());
		addition2.setIcon(Images.EDITOR_AGENTS_ADDITION.getIcon());

		p3=new JPanel(new GridLayout(model.caller.size(),2));
		byCallerRate=new JTextField[model.caller.size()];
		byCaller=new String[model.caller.size()];
		for (int i=0;i<model.caller.size();i++) {
			String name=model.caller.get(i).name;
			byCaller[i]=name;
			JLabel l=new JLabel(name);
			l.setIcon(Images.EDITOR_CALLER.getIcon());
			p4=new JPanel(new FlowLayout(FlowLayout.LEFT));
			p4.add(l);
			p3.add(p4);
			byCallerRate[i]=new JTextField(10);
			double d=0;
			for (int j=0;j<Math.min(agent.byCallers.size(),agent.byCallersRate.size());j++) if (agent.byCallers.get(j).equalsIgnoreCase(name)) {d=agent.byCallersRate.get(j); break;}
			byCallerRate[i].setText(NumberTools.formatNumberMax(d));
			byCallerRate[i].setEnabled(!readOnly);
			byCallerRate[i].addKeyListener(new DialogElementListener());
			p4=new JPanel(new FlowLayout(FlowLayout.LEFT));
			p4.add(byCallerRate[i]);
			p3.add(p4);
		}
		JScrollPane sp=new JScrollPane(p3);
		sp.setColumnHeaderView(new JLabel(Language.tr("Editor.AgentsGroup.RatesForUsingTheClientArrivals")));
		sp.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		p.add(p2=new JPanel(),BorderLayout.CENTER);
		p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));
		p2.add(sp);
		p2.add(Box.createVerticalGlue());

		/* Dialogseite "Skill-Level" */
		tabs.addTab(Language.tr("Editor.AgentsGroup.Tabs.SkillLevel"),content=new JPanel(new BorderLayout()));
		content.setLayout(new BorderLayout());

		sp=new JScrollPane(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sp.setBorder(BorderFactory.createEmptyBorder());
		content.add(sp,BorderLayout.CENTER);
		p.add(skillLevelInfo=new JLabel());

		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		String[] s=new String[model.skills.size()];
		for (int i=0;i<s.length;i++) s[i]=model.skills.get(i).name;
		skillLevelList=addComboBox(p,Language.tr("Editor.AgentsGroup.SkillLevel")+":",s);
		skillLevelList.addActionListener(new SkillLevelComboBoxActionListener());
		skillLevelList.setEnabled(!readOnly);
		p.add(skillEditButton=new JButton(Language.tr("Editor.AgentsGroup.SkillLevel.Edit")));
		skillEditButton.addActionListener(new ButtonActionListener());
		skillEditButton.setIcon(Images.EDITOR_SKILLLEVEL.getIcon());

		boolean ok=false;
		for (int i=0;i<model.skills.size();i++) if (s[i].equalsIgnoreCase(agent.skillLevel)) {
			skillLevelList.setSelectedIndex(i); ok=true; break;
		}
		new SkillLevelComboBoxActionListener().actionPerformed(null);
		if (!ok) {
			p.add(new JLabel("<html><body style=\"color:red;\">"+String.format(Language.tr("Editor.AgentsGroup.SkillLevel.Unknown"),agent.skillLevel)+"</body></html>"));
		}

		/* Dialogseite "Kosten" */
		tabs.addTab(Language.tr("Editor.AgentsGroup.Tabs.Costs"),content=new JPanel(new BorderLayout()));
		content.setLayout(new BorderLayout());

		content.add(p2=new JPanel(new GridLayout(2,1)),BorderLayout.NORTH);
		p2.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		costPerWorkingHour=addInputLine(p2,Language.tr("Editor.AgentsGroup.Costs.WorkingHour"),agent.costPerWorkingHour);
		costPerWorkingHour.setEnabled(!readOnly);
		costPerWorkingHour.addKeyListener(new DialogElementListener());

		p3=new JPanel(new GridLayout(callerTypeNames.length+1,3));
		costPerCall=new JTextField[callerTypeNames.length];
		costPerCallMinute=new JTextField[callerTypeNames.length];

		final String[] headings={Language.tr("Editor.AgentsGroup.Costs.ClientType"),Language.tr("Editor.AgentsGroup.Costs.PerConversation"),Language.tr("Editor.AgentsGroup.Costs.PerConversationMinute")};
		for (int i=0;i<headings.length;i++) {
			JLabel l=new JLabel(headings[i]);
			p4=new JPanel(new FlowLayout(FlowLayout.LEFT)); p4.add(l); p3.add(p4);
		}
		for (int i=0;i<callerTypeNames.length;i++) {
			JLabel l=new JLabel(callerTypeNames[i]);
			l.setIcon(Images.EDITOR_CALLER.getIcon());
			p4=new JPanel(new FlowLayout(FlowLayout.LEFT)); p4.add(l); p3.add(p4);

			double d=0, e=0;
			for (int j=0;j<agent.costCallerTypes.size();j++) if (agent.costCallerTypes.get(j).equalsIgnoreCase(callerTypeNames[i])) {
				d=agent.costPerCall.get(j); e=agent.costPerCallMinute.get(j); break;
			}

			costPerCall[i]=new JTextField(10);
			costPerCall[i].setText(NumberTools.formatNumberMax(d));
			costPerCall[i].setEnabled(!readOnly);
			costPerCall[i].addKeyListener(new DialogElementListener());
			p4=new JPanel(new FlowLayout(FlowLayout.LEFT)); p4.add(costPerCall[i]); p3.add(p4);

			costPerCallMinute[i]=new JTextField(10);
			costPerCallMinute[i].setText(NumberTools.formatNumberMax(e));
			costPerCallMinute[i].setEnabled(!readOnly);
			costPerCallMinute[i].addKeyListener(new DialogElementListener());
			p4=new JPanel(new FlowLayout(FlowLayout.LEFT)); p4.add(costPerCallMinute[i]); p3.add(p4);
		}
		JScrollPane sc=new JScrollPane(p3);
		sc.setColumnHeaderView(new JLabel(Language.tr("Editor.AgentsGroup.Costs.ForConversationsWithClients")));
		sc.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		content.add(p2=new JPanel(new GridLayout(2,1)),BorderLayout.CENTER);
		p2.add(sc);

		/* Daten laden */
		if (agent.count==-1) {
			comboBox.setSelectedIndex(1);
			((CardLayout)main.getLayout()).show(main,comboBox.getItemAt(1));
		} else {
			if (agent.count==-2) {
				comboBox.setSelectedIndex(2);
				((CardLayout)main.getLayout()).show(main,comboBox.getItemAt(2));
			} else {
				comboBox.setSelectedIndex(0);
				((CardLayout)main.getLayout()).show(main,comboBox.getItemAt(0));
			}
		}

		/* Icons auf Tabs */
		int c=0;
		if (showNumberOfAgentsTab) {
			tabs.setIconAt(0,Images.EDITOR_SHIFT_PLAN.getIcon());
			c=1;
		}
		tabs.setIconAt(c+0,Images.EDITOR_SKILLLEVEL.getIcon());
		tabs.setIconAt(c+1,Images.EDITOR_COSTS.getIcon());

	}

	@Override
	protected boolean checkData() {
		if (comboBox.getSelectedIndex()==0) {
			/* Anzahl */
			Integer I=NumberTools.getNotNegativeInteger(count,false);
			if (I==null || I<1) {
				MsgBox.error(this,Language.tr("Editor.AgentsGroup.Error.NumberOfAgents.Title"),String.format(Language.tr("Editor.AgentsGroup.Error.NumberOfAgents.Info"),count.getText()));
				return false;
			}
			/* Arbeitszeiten */
			if (TimeTools.getTime(workingTimeStart,false)==null) {
				MsgBox.error(this,Language.tr("Editor.AgentsGroup.Error.WorkingTimeStart.Title"),String.format(Language.tr("Editor.AgentsGroup.Error.WorkingTimeStart.Info"),workingTimeStart.getText()));
				return false;
			}
			if (!workingNoEndTime.isSelected() && TimeTools.getTime(workingTimeEnd,false)==null) {
				MsgBox.error(this,Language.tr("Editor.AgentsGroup.Error.WorkingTimeEnd.Title"),String.format(Language.tr("Editor.AgentsGroup.Error.WorkingTimeEnd.Info"),workingTimeEnd.getText()));
				return false;
			}
		}

		if (comboBox.getSelectedIndex()==2) {
			/* Verfügbare Halbstundenintervalle */
			Integer I=NumberTools.getNotNegativeInteger(byCallerCount,false);
			if (I==null || I<1) {
				MsgBox.error(this,Language.tr("Editor.AgentsGroup.Error.AgentsPerHalfHour.Title"),String.format(Language.tr("Editor.AgentsGroup.Error.AgentsPerHalfHour.Info"),byCallerCount.getText()));
				return false;
			}
			/* Raten pro Kundentyp */
			double sum=0;
			for (int i=0;i<byCallerRate.length;i++) {
				Double D=NumberTools.getNotNegativeDouble(byCallerRate[i],false);
				if (D==null) {
					MsgBox.error(this,Language.tr("Editor.AgentsGroup.Error.RateForClientType.Title"),String.format(Language.tr("Editor.AgentsGroup.Error.RateForClientType.Info"),byCaller[i],byCallerRate[i].getText()));
					return false;
				}
				sum+=D;
			}
			if (sum==0) {
				MsgBox.error(this,Language.tr("Editor.AgentsGroup.Error.NoClientTypeForAgentsDistribution.Title"),Language.tr("Editor.AgentsGroup.Error.NoClientTypeForAgentsDistribution.Info"));
				return false;
			}
		}

		/* Kosten */
		Double D=NumberTools.getNotNegativeDouble(costPerWorkingHour,false);
		if (D==null) {
			MsgBox.error(this,Language.tr("Editor.AgentsGroup.Error.WorkingHourCosts.Title"),String.format(Language.tr("Editor.AgentsGroup.Error.WorkingHourCosts.Info"),costPerWorkingHour.getText()));
			return false;
		}
		for (int i=0;i<costPerCall.length;i++) {
			D=NumberTools.getNotNegativeDouble(costPerCall[i],false);
			if (D==null) {
				MsgBox.error(this,Language.tr("Editor.AgentsGroup.Error.ConversationCosts.Title"),String.format(Language.tr("Editor.AgentsGroup.Error.ConversationCosts.Info"),callerTypeNames[i],costPerCall[i].getText()));
				return false;
			}
			D=NumberTools.getNotNegativeDouble(costPerCallMinute[i],false);
			if (D==null) {
				MsgBox.error(this,Language.tr("Editor.AgentsGroup.Error.ConversationMinuteCosts.Title"),String.format(Language.tr("Editor.AgentsGroup.Error.ConversationMinuteCosts.Info"),callerTypeNames[i],costPerCallMinute[i].getText()));
				return false;
			}
		}

		return true;
	}

	@Override
	protected void storeData() {
		agent.active=active.isSelected();

		switch (comboBox.getSelectedIndex()) {
		case 0:
			/* Anzahl */
			agent.count=NumberTools.getNotNegativeInteger(count,false);
			/* Arbeitszeiten */
			agent.workingTimeStart=TimeTools.getTime(workingTimeStart,false);
			Integer I=TimeTools.getTime(workingTimeEnd,false);
			if (I!=null) agent.workingTimeEnd=I;
			agent.workingNoEndTime=workingNoEndTime.isSelected();
			break;
		case 1:
			agent.count=-1;
			if (preferredShiftLength1.getSelectedIndex()==0) agent.preferredShiftLength=-1; else agent.preferredShiftLength=preferredShiftLength1.getSelectedIndex();
			if (minimumShiftLength1.getSelectedIndex()==0) agent.minimumShiftLength=-1; else agent.minimumShiftLength=minimumShiftLength1.getSelectedIndex();
			agent.setCountPerInterval(distributionPanel.getDistribution());
			agent.lastShiftIsOpenEnd=workingNoEndTime2.isSelected();
			break;
		case 2:
			agent.count=-2;
			if (preferredShiftLength2.getSelectedIndex()==0) agent.preferredShiftLength=-1; else agent.preferredShiftLength=preferredShiftLength2.getSelectedIndex();
			if (minimumShiftLength2.getSelectedIndex()==0) agent.minimumShiftLength=-1; else agent.minimumShiftLength=minimumShiftLength2.getSelectedIndex();
			/* Verfügbare Halbstundenintervalle */
			agent.byCallersAvailableHalfhours=NumberTools.getNotNegativeInteger(byCallerCount,false);
			/* Raten pro Kundentyp */
			agent.byCallers.clear();
			agent.byCallersRate.clear();
			for (int i=0;i<byCallerRate.length;i++) {
				Double D=NumberTools.getNotNegativeDouble(byCallerRate[i],false);
				if (D>0) {agent.byCallers.add(byCaller[i]); agent.byCallersRate.add(D);}
			}
			break;
		}

		/* Skills */
		if (skillLevelList.getSelectedIndex()<0) agent.skillLevel=""; else agent.skillLevel=(String)skillLevelList.getSelectedItem();

		/* Kosten */
		applyCostsToGroup(agent);
	}

	private void editSkillLevel() {
		if (skillLevelList.getSelectedIndex()==-1) return;
		String[] skillLevelNames=new String[model.skills.size()];
		for (int i=0;i<model.skills.size();i++) skillLevelNames[i]=model.skills.get(i).name;
		SkillLevelEditDialog dialog=new SkillLevelEditDialog(this,model.skills.get(skillLevelList.getSelectedIndex()),callerTypeNames,skillLevelNames,readOnly,false,false,helpLink);
		dialog.addSkillLevelRenameListener(new SkillLevelRenameListener());
		dialog.setVisible(true);
	}

	private void previewShiftPlan(boolean showDistribution) {
		CallcenterModelAgent a=new CallcenterModelAgent();
		if (comboBox.getSelectedIndex()==1) {
			a.count=-1;
			if (preferredShiftLength1.getSelectedIndex()==0) a.preferredShiftLength=-1; else a.preferredShiftLength=preferredShiftLength1.getSelectedIndex();
			if (minimumShiftLength1.getSelectedIndex()==0) a.minimumShiftLength=-1; else a.minimumShiftLength=minimumShiftLength1.getSelectedIndex();
			a.setCountPerInterval(distributionPanel.getDistribution());
		} else {
			if (!checkData()) return;
			a.count=-2;
			if (preferredShiftLength2.getSelectedIndex()==0) a.preferredShiftLength=-1; else a.preferredShiftLength=preferredShiftLength2.getSelectedIndex();
			if (minimumShiftLength2.getSelectedIndex()==0) a.minimumShiftLength=-1; else a.minimumShiftLength=minimumShiftLength2.getSelectedIndex();
			/* Verfügbare Halbstundenintervalle */
			a.byCallersAvailableHalfhours=NumberTools.getNotNegativeInteger(byCallerCount,false);
			/* Raten pro Kundentyp */
			a.byCallers.clear();
			a.byCallersRate.clear();
			for (int i=0;i<byCallerRate.length;i++) {
				Double D=NumberTools.getNotNegativeDouble(byCallerRate[i],false);
				if (D>0) {a.byCallers.add(byCaller[i]); a.byCallersRate.add(D);}
			}
		}
		if (skillLevelList.getSelectedIndex()<0) a.skillLevel=""; else a.skillLevel=(String)skillLevelList.getSelectedItem();
		new AgentShiftPlanPreviewDialog(this,showDistribution,a,callcenter,model);
	}

	private void changeAgentCount() {
		DataDistributionImpl dist=distributionPanel.getDistribution();
		double[] distData=dist.densityData;

		double scale=1;
		if (countComboBox.getSelectedIndex()==0) scale=2;
		if (countComboBox.getSelectedIndex()==2) scale=0.5;
		int count=(int)Math.round(dist.sum()*scale);

		String s1=Language.tr("Editor.Callcenter.Count.HalfHourIntervalSingleAbout");
		String s2=Language.tr("Editor.Callcenter.Count.HalfHourIntervalMultipleAbout");
		CopyDialog copy=new CopyDialog(this,Language.tr("Editor.AgentsGroup.Change"),s1,s2,count,helpCallback);
		copy.setVisible(true);
		if (copy.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;

		double d=copy.getProbability();
		for (int i=0;i<distData.length;i++) distData[i]=Math.round(distData[i]*d);

		distributionPanel.setDistribution(dist);
	}

	private class ButtonActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {editSkillLevel();}
	}

	private class ToolPopupListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==tools1) {tools1Popup.show(tools1,0,tools1.getBounds().height); return;}
			if (e.getSource()==tools2) {tools2Popup.show(tools2,0,tools2.getBounds().height); return;}

			if (e.getSource()==tools1item) {
				changeAgentCount();
				return;
			}

			if (e.getSource()==tools2item) {
				AgentEditCallersDialog dialog=new AgentEditCallersDialog(owner,model,helpCallback,distributionPanel.getDistribution().sum());
				dialog.setVisible(true);
				if (dialog.getClosedBy()==BaseEditDialog.CLOSED_BY_OK) {
					distributionPanel.setDistribution(dialog.getDistribution());
				}
				return;
			}

			if (e.getSource()==tools3item) {
				if (skillLevelList.getSelectedIndex()==-1) return;
				AgentSpecialEditDialog dialog=new AgentSpecialEditDialog(AgentEditDialog.this,model.caller,model.skills.get(skillLevelList.getSelectedIndex()),helpCallback);
				dialog.setVisible(true);
				int results=dialog.getResult();
				if (results>=0) byCallerCount.setText(""+results);
				return;
			}

			if (e.getSource()==efficiency1 || e.getSource()==efficiency2) {
				EfficiencyEditDialog dialog=new EfficiencyEditDialog(AgentEditDialog.this,readOnly,false,agent.efficiencyPerInterval,helpCallback,EfficiencyEditDialog.Mode.MODE_EFFICIENCY);
				dialog.setVisible(true);
				if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
				agent.efficiencyPerInterval=dialog.getEfficiency();
				return;
			}

			if (e.getSource()==addition1 || e.getSource()==addition2) {
				EfficiencyEditDialog dialog=new EfficiencyEditDialog(AgentEditDialog.this,readOnly,false,agent.additionPerInterval,helpCallback,EfficiencyEditDialog.Mode.MODE_ADDITION);
				dialog.setVisible(true);
				if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
				agent.additionPerInterval=dialog.getEfficiency();
				return;
			}
		}
	}

	private class SkillLevelComboBoxActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int index=skillLevelList.getSelectedIndex();
			skillEditButton.setEnabled(index!=-1);
			StringBuilder s=new StringBuilder();
			if (index!=-1) {
				CallcenterModelSkillLevel skill=model.skills.get(index);
				s.append("<html><body>");
				s.append("<b>"+skill.name+"</b><br><br>");
				s.append(Language.tr("Editor.AgentsGroup.SkillLevel.ClientTypes")+":<br><ul>");
				for (int i=0;i<skill.callerTypeName.size();i++) {
					s.append("<li><b>"+skill.callerTypeName.get(i)+"</b>");
					s.append("<br>("+Language.tr("Editor.AgentsGroup.SkillLevel.Score")+": "+skill.callerTypeScore.get(i)+")<br><br></li>");
				}
				s.append("</ul></body></html>");
			}
			skillLevelInfo.setText(s.toString());
		}
	}

	private class AgentTypeComboBoxActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			((CardLayout)main.getLayout()).show(main,(String)comboBox.getSelectedItem());
		}
	}

	private class CountComboBoxActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			DataDistributionImpl dist=distributionPanel.getDistribution();
			int values;
			switch (countComboBox.getSelectedIndex()) {
			case 0: values=24; break;
			case 1: values=48; break;
			case 2: values=96; break;
			default: values=48;
			}
			dist.stretchToValueCount(values);
			distributionPanel.setDistribution(dist);
		}
	}

	private class ShiftPlanPreviewButtonActionListener implements ActionListener {
		private final boolean showDistribution;

		public ShiftPlanPreviewButtonActionListener(boolean showDistribution) {
			this.showDistribution=showDistribution;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			previewShiftPlan(showDistribution);
		}
	}

	private class SkillLevelRenameListener implements RenameListener {
		@Override
		public void renamed(RenameEvent e) {
			/* ComboBox aktualisieren */
			int sel=skillLevelList.getSelectedIndex();
			int index=-1;
			for (int i=0;i<skillLevelList.getItemCount();i++) if (skillLevelList.getItemAt(i).toString().equalsIgnoreCase(e.oldName)) {index=i; break;}
			if (index>=0) {
				skillLevelList.removeItemAt(index);
				if (e.renamed) skillLevelList.insertItemAt(e.newName,index);
			}
			skillLevelList.setSelectedIndex(sel);

			/* Skill bei Agent auch ohne "Ok"-Klick umbenennen */
			if (e.renamed && agent.skillLevel.equalsIgnoreCase(e.oldName)) agent.skillLevel=e.newName;

			/* Event weiterreichen */
			for (int i=0;i<listener.size();i++) listener.get(i).renamed(e);
		}
	}

	private class DialogElementListener implements KeyListener {
		private void keyEvent(KeyEvent e) {
			if (comboBox.getSelectedIndex()==0) {
				NumberTools.getNotNegativeInteger(count,true);
				TimeTools.getTime(workingTimeStart,true);
				TimeTools.getTime(workingTimeEnd,true);
			}
			if (comboBox.getSelectedIndex()==2) {
				NumberTools.getNotNegativeInteger(byCallerCount,true);
				for (int i=0;i<byCallerRate.length;i++) NumberTools.getNotNegativeDouble(byCallerRate[i],true);
			}
			NumberTools.getNotNegativeDouble(costPerWorkingHour,true);
			for (int i=0;i<costPerCall.length;i++) {
				NumberTools.getNotNegativeDouble(costPerCall[i],true);
				NumberTools.getNotNegativeDouble(costPerCallMinute[i],true);
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
			keyEvent(e);
		}

		@Override
		public void keyPressed(KeyEvent e) {
			keyEvent(e);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			keyEvent(e);
		}
	}

	private class ToolsButtonHandler implements Runnable{
		@Override
		public void run() {
			final JButton b=getUserButton(0);
			popupMenu.show(b,0,b.getHeight());
		}
	}

	private boolean checkCostsForApply() {
		String[] error=null;

		Double D=NumberTools.getNotNegativeDouble(costPerWorkingHour,true);
		if (D==null) {
			if (error==null) error=new String[]{Language.tr("Editor.AgentsGroup.Error.WorkingHourCosts.Title"),String.format(Language.tr("Editor.AgentsGroup.Error.WorkingHourCosts.Info"),costPerWorkingHour.getText())};
		}
		for (int i=0;i<costPerCall.length;i++) {
			D=NumberTools.getNotNegativeDouble(costPerCall[i],true);
			if (D==null) {
				if (error==null) error=new String[]{Language.tr("Editor.AgentsGroup.Error.ConversationCosts.Title"),String.format(Language.tr("Editor.AgentsGroup.Error.ConversationCosts.Info"),callerTypeNames[i],costPerCall[i].getText())};
			}
			D=NumberTools.getNotNegativeDouble(costPerCallMinute[i],false);
			if (D==null) {
				if (error==null) error=new String[]{Language.tr("Editor.AgentsGroup.Error.ConversationMinuteCosts.Title"),String.format(Language.tr("Editor.AgentsGroup.Error.ConversationMinuteCosts.Info"),callerTypeNames[i],costPerCallMinute[i].getText())};
			}
		}

		if (error!=null) {
			final String largeStart="<b><span style=\"font-size: 115%\">";
			final String largeEnd="</span></b>";
			StringBuilder sb=new StringBuilder();
			sb.append("<html><body>\n");
			sb.append(Language.tr("Editor.AgentsGroup.Apply.Error.Info")+"<br>\n");
			sb.append(largeStart+Language.tr("Editor.AgentsGroup.Apply.Error.AdditionalInformation")+":"+largeEnd+"<br>\n");
			sb.append("<div style=\"border: 1px solid red; background-color: #FFBBBB; padding: 5px;\">");
			sb.append(largeStart+error[0]+largeEnd+"\n");
			sb.append(error[1]+"</div>\n");
			sb.append("</body></html>");
			MsgBox.error(owner,Language.tr("Editor.AgentsGroup.Apply.Error.Title"),sb.toString());
			return false;
		}
		return true;
	}

	private void applyCostsToGroup(CallcenterModelAgent agent) {
		agent.costPerWorkingHour=NumberTools.getNotNegativeDouble(costPerWorkingHour,false);

		agent.costCallerTypes.clear();
		agent.costPerCall.clear();
		agent.costPerCallMinute.clear();
		for (int i=0;i<costPerCall.length;i++) {
			double d1=NumberTools.getNotNegativeDouble(costPerCall[i],false);
			double d2=NumberTools.getNotNegativeDouble(costPerCallMinute[i],false);
			if (d1>0 || d2>0) {
				agent.costCallerTypes.add(callerTypeNames[i]);
				agent.costPerCall.add(d1);
				agent.costPerCallMinute.add(d2);
			}
		}
	}

	private void applyCosts(int group) {
		int count=0;
		for (CallcenterModelCallcenter c : model.callcenter) {
			if (c==callcenter) {
				for (int i=0;i<callcenterAgents.size();i++) {
					if (count!=indexOfGroup && (group==-1 || group==count)) applyCostsToGroup(callcenterAgents.get(i));
					count++;
				}
			} else {
				for (int i=0;i<c.agents.size();i++) {
					if (count!=indexOfGroup && (group==-1 || group==count)) applyCostsToGroup(c.agents.get(i));
					count++;
				}
			}
		}
	}

	private class PopupActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (int i=0;i<applyThisPage.length;i++) if (applyThisPage[i]==e.getSource()) {
				if (!checkCostsForApply()) return;
				applyCosts((i==applyThisPage.length-1)?-1:i);
				return;
			}
		}
	}
}
