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
package ui.generator;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import systemtools.MsgBox;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;

/**
 * Dialog zum Laden der Agentenverteilungen für ein ganzes Callcenter (Modell-Generator)
 * @author Alexander Herzog
 * @version 1.0
 */
public final class AgentsGeneratorDialog extends GeneratorBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 730077141756069208L;

	/**
	 * Welche Daten sollen geladen werden?
	 * @author Alexander Herzog
	 * @see AgentsGeneratorDialog#AgentsGeneratorDialog(Window, Runnable, CallcenterModel, int, AgentsGeneratorMode, int)
	 */
	public enum AgentsGeneratorMode {
		/** Anzahl an alktiven Agenten pro Intervall */
		AGENTS_GENERATOR_MODE_WORKING,
		/** Produktivität pro Intervall */
		AGENTS_GENERATOR_MODE_EFFICIENCY,
		/** Krankheitsbedingter Aufschlag pro Intervall */
		AGENTS_GENERATOR_MODE_ADDITION
	}

	private List<CallcenterModelAgent> agents;
	private int selectCallcenterNr;
	private final AgentsGeneratorMode mode;

	private JComboBox<String> callcenterSelect=null;
	private JPanel tab;
	private List<JComboBox<String>> select;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param helpCallback	Hilfe-Callback
	 * @param model	Callcenter-Modell in das die Daten geladen werden sollen
	 * @param selectCallcenterNr	Welches Callcenter soll als Empfänger für die Daten vorgeschlagen werden? (kann -1 sein)
	 * @param mode	Welche Daten sollen geladen werden?
	 * @param intervals	Anzahl der Intervalle über die die Agenten pro Tag verteilt werden sollen (muss 24, 48 oder 96 sein)
	 */
	public AgentsGeneratorDialog(final Window owner, final Runnable helpCallback, final CallcenterModel model, final int selectCallcenterNr, final AgentsGeneratorMode mode, final int intervals) {
		super(owner,getTitle(mode),helpCallback,model,(mode==AgentsGeneratorMode.AGENTS_GENERATOR_MODE_WORKING)?intervals:48);
		this.selectCallcenterNr=selectCallcenterNr;
		this.mode=mode;
	}

	private static String getTitle(AgentsGeneratorMode mode) {
		switch (mode) {
		case AGENTS_GENERATOR_MODE_WORKING: return Language.tr("Generator.LoadAgentsData");
		case AGENTS_GENERATOR_MODE_EFFICIENCY: return Language.tr("Generator.LoadAgentsProductivity");
		case AGENTS_GENERATOR_MODE_ADDITION: return Language.tr("Generator.LoadAgentsSurcharge");
		default: return "";
		}
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.generator.GeneratorBaseDialog#tableLoaded()
	 */
	@Override
	protected boolean tableLoaded() {
		agents=null;
		JPanel p;

		data.setLayout(new BoxLayout(data,BoxLayout.Y_AXIS));

		data.add(p=new JPanel());
		p.setLayout(new GridLayout(2,1));
		p.add(createHeadingLabel(Language.tr("Generator.Callcenter")));
		int last=(callcenterSelect==null)?0:callcenterSelect.getSelectedIndex();
		p.add(callcenterSelect=new JComboBox<String>());
		for (int i=0;i<model.callcenter.size();i++) callcenterSelect.addItem(model.callcenter.get(i).name);

		data.add(tab=new JPanel());

		data.add(Box.createVerticalGlue());

		if (model.callcenter.size()>0) {
			if (selectCallcenterNr>=0) {
				callcenterSelect.setSelectedIndex(Math.min(callcenterSelect.getItemCount()-1,selectCallcenterNr));
				selectCallcenterNr=-1;
			} else {
				callcenterSelect.setSelectedIndex(Math.min(callcenterSelect.getItemCount()-1,last));
			}
			callcenterSelected();
		} else {
			MsgBox.error(this,"Kein Callcenter vorhanden","Es wurden keine Callcenter definiert, für die Agentendaten geladen werden könnten.");
			return false;
		}
		callcenterSelect.addActionListener(new CallcenterSelectListener());

		return true;
	}

	private static boolean arraysMatch(String[] list1, String[] list2) {
		for (int i=0;i<list1.length;i++) {
			String s=list1[i];
			if (s.isEmpty()) continue;
			boolean ok=false;
			for (int j=0;j<list2.length;j++) if (list2[j].equalsIgnoreCase(s)) {ok=true; break;}
			if (!ok) return false;
		}
		for (int i=0;i<list2.length;i++) {
			String s=list2[i];
			if (s.isEmpty()) continue;
			boolean ok=false;
			for (int j=0;j<list1.length;j++) if (list1[j].equalsIgnoreCase(s)) {ok=true; break;}
			if (!ok) return false;
		}
		return true;
	}

	/**
	 * Legt eine Combobox mit einem vorgegebenen Inhalt an und versucht einen sinnvollen Startwert einzustellen.
	 * @param label	Dieser Wert wird versucht in der Comboxbox zu finden und dann ggf. als Startwert einestellt.
	 * @param valuesDisplay	Werte, die in die Combobox geschrieben werden sollen. Zusätzlich wird als erster Wert "Spalte nicht verwenden" geschrieben
	 * @param valuesSelect	Initial auszuwählende Werte
	 * @return	Liefert die neu erstellte Combobox zurück
	 */
	protected static JComboBox<String> createComboBox(String label, List<String> valuesDisplay, List<String> valuesSelect) {
		JComboBox<String> comboBox=new JComboBox<String>();

		/* Combobox befüllen */
		comboBox.addItem(Language.tr("Generator.DoNotUseColumn"));
		for (int i=0;i<valuesDisplay.size();i++) comboBox.addItem(valuesDisplay.get(i));

		/* Passenden initialen Wert festlegen */
		if (comboBox.getItemCount()>0) comboBox.setSelectedIndex(0);

		String[] labels=label.split("\n");
		for (int i=0;i<valuesDisplay.size();i++) {
			String[] values=valuesSelect.get(i).split("\n");
			if (arraysMatch(labels,values)) {comboBox.setSelectedIndex(i+1); return comboBox;}
		}

		return comboBox;
	}

	private void callcenterSelected() {
		tab.removeAll();
		agents=null;
		if (callcenterSelect.getSelectedIndex()<0) return;
		agents=model.callcenter.get(callcenterSelect.getSelectedIndex()).agents;
		String callcenterName=model.callcenter.get(callcenterSelect.getSelectedIndex()).name;

		/* Agentengruppenliste erstellen */
		List<String> agentGroupsDisplay=new ArrayList<String>();
		List<String> agentGroupsSelect=new ArrayList<String>();
		for (int i=0;i<agents.size();i++) {
			StringBuilder s=new StringBuilder("<html><body><b>"+callcenterName+" - "+Language.tr("Generator.AgentGroup")+" "+(i+1)+"</b><br>");
			StringBuilder t=new StringBuilder();
			int nr=-1;
			for (int j=0;j<model.skills.size();j++) if (agents.get(i).skillLevel.equalsIgnoreCase(model.skills.get(j).name)) {nr=j; break;}
			if (nr<0) {t=new StringBuilder(); s.append("("+Language.tr("Generator.SkillLevel.Unknown")+")");} else {
				List<String> caller=model.skills.get(nr).callerTypeName;
				for (int j=0;j<caller.size();j++) {
					s.append("  "+caller.get(j));
					t.append(caller.get(j));
					if (j<caller.size()-1) {s.append("<br>"); t.append('\n');}
				}
			}
			s.append("</body></html>");
			agentGroupsDisplay.add(s.toString());
			agentGroupsSelect.add(t.toString());
		}

		/* GUI erzeugen */
		tab.setLayout(new GridLayout(colIndex.size()+1,2));

		select=new ArrayList<JComboBox<String>>();

		tab.add(createHeadingLabel(Language.tr("Generator.TableColumn")));
		tab.add(createHeadingLabel(Language.tr("Generator.AgentGroup")));

		for (int i=0;i<colIndex.size();i++) {
			int index=colIndex.get(i);

			tab.add(createLabel(heading[index]));

			JComboBox<String> box;
			JPanel p;
			tab.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p.add(box=createComboBox(heading[index],agentGroupsDisplay,agentGroupsSelect));
			select.add(box);
		}

		tab.validate(); /* Muss aufgerufen werden, um die Elemente nach einem Ändern der Callcenterauswahl zu aktualisieren. */
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#checkData()
	 */
	@Override
	protected boolean checkData() {
		if (table==null) return false;
		for (int i=1;i<select.size();i++) {
			int index=select.get(i).getSelectedIndex();
			if (index==0) continue;
			for (int j=0;j<i;j++) if (select.get(j).getSelectedIndex()==index) {
				MsgBox.error(this,Language.tr("Generator.Error.MultipleColumnsAsSourceForOneAgentsGroup.Title"),String.format(Language.tr("Generator.Error.MultipleColumnsAsSourceForOneAgentsGroup.Info"),heading[colIndex.get(i)].replace('\n',' '),heading[colIndex.get(j)].replace('\n',' '),""+index));
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#storeData()
	 */
	@Override
	protected void storeData() {
		for (int i=0;i<select.size();i++) {
			int sel=select.get(i).getSelectedIndex();
			if (sel==0) continue;

			int colNr=colIndex.get(i);
			CallcenterModelAgent a=agents.get(sel-1);

			switch (mode) {
			case AGENTS_GENERATOR_MODE_WORKING:
				a.count=-1;
				a.countPerInterval24=null;
				a.countPerInterval48=null;
				a.countPerInterval96=null;
				switch (neededRows) {
				case 24: a.countPerInterval24=new DataDistributionImpl(CallcenterModelAgent.countPerIntervalMaxX,24); break;
				case 48: a.countPerInterval48=new DataDistributionImpl(CallcenterModelAgent.countPerIntervalMaxX,48); break;
				case 96: a.countPerInterval96=new DataDistributionImpl(CallcenterModelAgent.countPerIntervalMaxX,96); break;
				}
				for (int j=rows[0][colNr];j<=rows[1][colNr];j++) {
					Double d=NumberTools.getNotNegativeDouble(table.getValue(colNr,j));
					if (d==null) d=0.0;
					switch (neededRows) {
					case 24: a.countPerInterval24.densityData[j-rows[0][colNr]]=Math.round(d); break;
					case 48: a.countPerInterval48.densityData[j-rows[0][colNr]]=Math.round(d); break;
					case 96: a.countPerInterval96.densityData[j-rows[0][colNr]]=Math.round(d); break;
					}
				}
				break;
			case AGENTS_GENERATOR_MODE_EFFICIENCY:
				DataDistributionImpl efficiency=model.efficiencyPerInterval.clone();
				for (int j=rows[0][colNr];j<=rows[1][colNr];j++) {
					Double d=NumberTools.getNotNegativeDouble(table.getValue(colNr,j));
					if (d!=null) efficiency.densityData[j-rows[0][colNr]]=d;
				}
				a.efficiencyPerInterval=efficiency;
				break;
			case AGENTS_GENERATOR_MODE_ADDITION:
				DataDistributionImpl addition=model.additionPerInterval.clone();
				for (int j=rows[0][colNr];j<=rows[1][colNr];j++) {
					Double d=NumberTools.getNotNegativeDouble(table.getValue(colNr,j));
					if (d!=null) addition.densityData[j-rows[0][colNr]]=d;
				}
				a.additionPerInterval=addition;
				break;
			}
		}
	}

	private final class CallcenterSelectListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {callcenterSelected();}
	}
}