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
import java.awt.GridLayout;
import java.awt.Window;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import systemtools.MsgBox;
import ui.model.CallcenterModel;

/**
 * In diesem Dialog kann eingestellt werden wie hoch der Anteil
 * der Kundenankünfte in den jeweiligen Gruppen ist, der durch
 * diese Agentengruppe bedient werden soll. Auf dieser Basis
 * können neue Agenten über die Intervalle neu verteilt werden,
 * so dass die jeweils verfügbare Bedienleistung am besten zu
 * dem Bedarf passt.
 * @author Alexander Herzog
 * @version 1.0
 * @see AgentEditDialog
 */
public class AgentEditCallersDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8756194008858357281L;

	/** Gesamtes Callcenter-Modell */
	private final CallcenterModel model;
	/** Über alle Intervalle verteilbare Anzahl an Agenten (Agenten*Intervalle) */
	private final double agentCount;

	/**
	 * Verteilung der verfügbaren Agenten über die Intervalle
	 * @see #getDistribution()
	 */
	private DataDistributionImpl distribution;

	/**
	 * Namen der Kundengruppen
	 */
	private String[] byCaller;

	/**
	 * Eingabefelder für die Raten denen die Kundenankünfte die in Agetenverteilung eingehen sollen
	 */
	private JTextField[] byCallerRate;

	/**
	 * Konstuktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param model	Gesamtes Callcenter-Modell
	 * @param helpCallback	Hilfe-Callback
	 * @param agentCount	Über alle Intervalle verteilbare Anzahl an Agenten (Agenten*Intervalle)
	 */
	public AgentEditCallersDialog(final Window owner, final CallcenterModel model, final Runnable helpCallback, final double agentCount) {
		super(owner,Language.tr("Editor.AgentsGroup.Tools.DistribuionByClientArrivals.Title"),null,false,helpCallback);
		this.model=model;
		this.agentCount=agentCount;
		distribution=new DataDistributionImpl(48,48);
		createSimpleGUI(500,500,null,null);
		pack();
	}

	@Override
	protected void createSimpleContent(JPanel content) {

		JPanel p3=new JPanel(new GridLayout(model.caller.size(),2));
		byCallerRate=new JTextField[model.caller.size()];
		byCaller=new String[model.caller.size()];
		for (int i=0;i<model.caller.size();i++) {
			String name=model.caller.get(i).name;
			byCaller[i]=name;
			JLabel l=new JLabel(name);
			JPanel p4=new JPanel(new FlowLayout(FlowLayout.LEFT));
			p4.add(l);
			p3.add(p4);
			byCallerRate[i]=new JTextField(10);
			byCallerRate[i].setText("0");
			p4=new JPanel(new FlowLayout(FlowLayout.LEFT));
			p4.add(byCallerRate[i]);
			p3.add(p4);
		}
		JScrollPane sp=new JScrollPane(p3);
		sp.setColumnHeaderView(new JLabel("<html><body>"+Language.tr("Editor.AgentsGroup.Tools.DistribuionByClientArrivals.Info")+"</body></html>"));
		sp.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		JPanel p2;
		content.add(p2=new JPanel(),BorderLayout.CENTER);
		p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));
		p2.add(sp);
	}

	@Override
	protected boolean checkData() {
		for (int i=0;i<byCallerRate.length;i++) {
			Double D=NumberTools.getDouble(byCallerRate[i],true);
			if (D==null) {
				MsgBox.error(owner,Language.tr("Editor.AgentsGroup.Tools.DistribuionByClientArrivals.ErrorTitle"),String.format(Language.tr("Editor.AgentsGroup.Tools.DistribuionByClientArrivals.ErrorInfo"),byCaller[i],byCallerRate[i].getText()));
				return false;
			}
		}
		return true;
	}

	@Override
	protected void storeData() {
		distribution.setToValue(0);

		for (int i=0;i<byCallerRate.length;i++) {
			Double D=NumberTools.getDouble(byCallerRate[i],true);
			if (D>0) distribution=distribution.add(model.caller.get(i).getFreshCallsDistOn48Base().multiply(D));
		}
		distribution.normalizeDensity();
		distribution=distribution.multiply(agentCount).round();
	}

	/**
	 * Liefert die neue Verteilung der verfügbaren Agenten über die Intervalle
	 * @return	Verteilung der verfügbaren Agenten über die Intervalle
	 */
	public final DataDistributionImpl getDistribution() {
		return distribution;
	}
}