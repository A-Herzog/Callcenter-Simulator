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
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.Serializable;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import language.Language;
import net.calc.StartAnySimulator;
import simulator.CallcenterSimulatorInterface;
import simulator.Statistics;
import systemtools.MsgBox;
import ui.HelpLink;
import ui.Rearranger;
import ui.compare.ComparePanel;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.optimizer.CheckBoxTree;

/**
 * Mit Hilfe von den in diesem Panel angebotenen Funktionen
 * können Anrufe oder Agentenarbeitszeiten so verlagert werden,
 * dass eine bessere Korrespondenz zwischen beiden besteht.
 * @author Alexander Herzog
 */
public class RearrangePanel extends JWorkPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -6413752382384948291L;

	/** Übergeordnetes Fenster */
	private final Window owner;
	/** Für die Anrufe- oder Agenten-Verlagerung zu verwendenes Callcenter-Modell */
	private final CallcenterModel editModel;
	private final Rearranger rearranger;
	private CallcenterModel loadModelIntoEditor=null;

	private final JTabbedPane tabs;
	private final CheckBoxTree callsTree;
	private final JSlider callerSlider;
	private final CheckBoxTree agentsTree;
	private final JSlider agentsSlider;
	private final JCheckBox agentsAdditionalMove;

	private CallcenterSimulatorInterface simulator;
	private CallcenterRunPanel runPanel;
	private Statistics[] statistics=null;
	private ComparePanel comparePanel;

	/** Help-Link */
	private final HelpLink helpLink;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param model	Für die Anrufe- oder Agenten-Verlagerung zu verwendenes Callcenter-Modell
	 * @param doneNotify	Callback wird aufgerufen, wenn das Panel geschlossen werden soll
	 * @param helpLink	Help-Link
	 */
	public RearrangePanel(final Window owner, final Runnable doneNotify, final CallcenterModel model, final HelpLink helpLink) {
		super(doneNotify,helpLink.pageRearrange);
		this.owner=owner;
		editModel=model;
		rearranger=new Rearranger(editModel);

		this.helpLink=helpLink;

		JPanel tab,p,p2;

		/* Tabs */
		add(tabs=new JTabbedPane(),BorderLayout.CENTER);

		/* Kunden verlagern */
		tabs.addTab(Language.tr("Rearranger.MoveCalls"),tab=new JPanel(new BorderLayout()));
		callsTree=new CheckBoxTree();
		String s=rearranger.canMoveCalls();
		if (s==null) {
			tab.add(getLabel(Language.tr("Rearranger.MoveCalls.GroupInfo")),BorderLayout.NORTH);
			tab.add(new JScrollPane(callsTree.tree),BorderLayout.CENTER); callsTree.addActiveCaller(editModel);
			tab.add(callerSlider=getSlider(Language.tr("Rearranger.Slider.Min"),Language.tr("Rearranger.Slider.MaxClients")),BorderLayout.SOUTH);
		} else {
			tab.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
			p.add(p2=new JPanel()); p2.setLayout(new BoxLayout(p2,BoxLayout.PAGE_AXIS));
			p2.add(new JLabel("<html>"+lineWrap(s,125)+"</html>"));
			p.add(getLabel("<html>"+lineWrap(s,150)+"</html>"));
			callerSlider=null;
		}

		/* Agenten verlagern */
		tabs.addTab(Language.tr("Rearranger.MoveAgents"),tab=new JPanel(new BorderLayout()));
		agentsTree=new CheckBoxTree();
		tab.add(getLabel(Language.tr("Rearranger.MoveAgents.GroupInfo")),BorderLayout.NORTH);
		tab.add(new JScrollPane(agentsTree.tree),BorderLayout.CENTER); agentsTree.addActiveAgents(editModel);
		tab.add(p=new JPanel(),BorderLayout.SOUTH); p.setLayout(new BoxLayout(p,BoxLayout.PAGE_AXIS));
		p.add(agentsSlider=getSlider(Language.tr("Rearranger.Slider.Min"),Language.tr("Rearranger.Slider.MaxAgents")));
		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(agentsAdditionalMove=new JCheckBox(Language.tr("Rearranger.MoveAgents.Additional")));

		/* Icons für Tabs */
		tabs.setIconAt(0,Images.REARRANGE_PAGE_CALLER.getIcon());
		tabs.setIconAt(1,Images.REARRANGE_PAGE_AGENTS.getIcon());

		addFooter(null,null,null);
		addFooterButton(Language.tr("Rearranger.Button.Load"),Images.REARRANGE_LOAD.getURL());
		addFooterButton(Language.tr("Rearranger.Button.SimulateAndCompare"),Images.REARRANGE_RUN.getURL());
	}

	private String lineWrap(String text, int maxLength) {
		StringBuilder sb=new StringBuilder();
		while (!text.isEmpty()) {
			StringBuilder line=new StringBuilder();
			while (!text.isEmpty()) {
				int index=text.indexOf(' ');
				if (index<0) index=text.length();
				if (line.length()>0 && line.length()+1+index>maxLength) break;
				if (line.length()!=0) line.append(' ');
				line.append(text.substring(0,index));
				text=text.substring(index).trim();
			}
			if (line.length()>0) {
				if (sb.length()>0) sb.append("<br>");
				sb.append(line);
			}
		}

		return sb.toString();
	}

	private JSlider getSlider(String minLabel, String maxLabel) {
		JSlider slider=new JSlider(SwingConstants.HORIZONTAL,0,20,15);
		slider.setMajorTickSpacing(2);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		Hashtable<Integer,JLabel> callerLabelTable=new Hashtable<Integer,JLabel>();
		callerLabelTable.put(0,new JLabel("<html>"+minLabel+"</html>"));
		for (int i=1;i<=9;i++) callerLabelTable.put(i*2,new JLabel(""+(i*10)+"%"));
		callerLabelTable.put(20,new JLabel("<html>"+maxLabel+"</html>"));
		slider.setLabelTable(callerLabelTable);
		slider.setPaintLabels(true);
		return slider;
	}

	private JPanel getLabel(String info) {
		JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(new JLabel("<html>"+info+"</html>"));
		return p;
	}

	/**
	 * Wird hier ein Model geliefert, so soll dieses nach dem Schließen dieses Panels in den Editor geladen werden.
	 * @return	Modell, welches nach dem Schließen des Panels in den Editor geladen werden soll, oder <code>null</code>, wenn kein neues Modell geladen werden soll
	 */
	public CallcenterModel getModelForEditor() {
		return loadModelIntoEditor;
	}

	private CallcenterModel rearrange() {
		if (tabs.getSelectedIndex()==0) {
			String s=rearranger.canMoveCalls();
			if (s!=null) {
				MsgBox.error(this,Language.tr("Rearranger.MoveCalls.NotPossible"),s);
				return null;
			}
			String[] groups=callsTree.getSelected(false);
			if (groups==null || groups.length==0) {
				MsgBox.error(this,Language.tr("Rearranger.MoveCalls.NoGroups.Title"),Language.tr("Rearranger.MoveCalls.NoGroups.Info"));
				return null;
			}
			return rearranger.mixModels(rearranger.moveCalls(groups),(double)callerSlider.getValue()/20);
		} else {
			String[] groups=agentsTree.getSelected(true);
			if (groups==null || groups.length==0) {
				MsgBox.error(this,Language.tr("Rearranger.MoveAgents.NoGroups.Title"),Language.tr("Rearranger.MoveAgents.NoGroups.Info"));
				return null;
			}
			return rearranger.mixModels(rearranger.moveAgents(groups,agentsAdditionalMove.isSelected()),(double)agentsSlider.getValue()/20);
		}
	}

	private void rearrangeAndClose() {
		CallcenterModel newModel=rearrange();
		if (newModel==null) return;

		loadModelIntoEditor=newModel;
		done();
	}

	private void simulateAndCompare() {
		CallcenterModel newModel=rearrange();
		if (newModel==null) return;

		StartAnySimulator startAnySimulator;
		String s;

		startAnySimulator=new StartAnySimulator(newModel,null);
		s=startAnySimulator.check();
		if (s!=null) {MsgBox.error(this,Language.tr("Window.ErrorStartingSimulation.Title"),s); return;}

		startAnySimulator=new StartAnySimulator(editModel,null);
		s=startAnySimulator.check();
		if (s!=null) {MsgBox.error(this,Language.tr("Window.ErrorStartingSimulation.Title"),s); return;}

		remove(tabs);
		buttonPanel.setVisible(false);

		add(runPanel=new CallcenterRunPanel(simulator=startAnySimulator.run(),new SimDoneNotify(),false,-1),BorderLayout.CENTER);
	}

	@Override
	protected void userButtonClick(int index, JButton button) {
		if (index==0) {rearrangeAndClose(); return;}
		if (index==1) {simulateAndCompare(); return;}
	}

	private class SimDoneNotify implements Runnable {
		public void setDefaultGUIState() {
			simulator=null;
			statistics=null;
			runPanel=null;
			comparePanel=null;
			add(tabs,BorderLayout.CENTER);
			tabs.setVisible(false);
			tabs.setVisible(true);
			buttonPanel.setVisible(true);
		}

		@Override
		public void run() {
			if (runPanel!=null) {
				remove(runPanel);
				if (!runPanel.getRunComplete()) {
					MsgBox.warning(RearrangePanel.this,Language.tr("Window.Simulation.Canceled"),Language.tr("Window.Simulation.Canceled.Info"));
					setDefaultGUIState();
					return;
				}
				runPanel=null;
				if (statistics==null) {
					statistics=new Statistics[2];
					statistics[0]=simulator.collectStatistic();
					StartAnySimulator startAnySimulator=new StartAnySimulator(rearrange(),null);
					startAnySimulator.check();
					add(runPanel=new CallcenterRunPanel(simulator=startAnySimulator.run(),new SimDoneNotify(),false,-1),BorderLayout.CENTER);
					repaint();
				} else {
					statistics[1]=simulator.collectStatistic();
					add(comparePanel=new ComparePanel(
							owner,statistics,new String[]{Language.tr("Compare.Models.Base"),Language.tr("Compare.Models.Changed")},
							true,new SimDoneNotify(),false,
							helpLink,
							helpLink.pageRearrange,
							helpLink.pageRearrangeModal
							),BorderLayout.CENTER);
					comparePanel.setVisible(false);
					comparePanel.setVisible(true);
				}
				return;
			}

			if (comparePanel!=null) {
				remove(comparePanel);
				setDefaultGUIState();
				return;
			}
		}
	}
}