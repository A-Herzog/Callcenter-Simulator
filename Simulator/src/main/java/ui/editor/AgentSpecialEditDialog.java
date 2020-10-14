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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionTools;
import parser.CalcSystem;
import parser.MathCalcError;
import parser.MathParser;
import systemtools.MsgBox;
import ui.model.CallcenterModelCaller;
import ui.model.CallcenterModelSkillLevel;

/**
 * In diesem Dialog kann eingestellt werden, wie viele der Kunden
 * in den jeweiligen Gruppen durch Agenten dieser Gruppe bedient
 * werden sollen und auf dieser Basis kann die notwendige Anzahl
 * an Agenten-Intervall-Arbeitszeiten bestimmt werden.
 * @author Alexander Herzog
 * @see AgentEditDialog
 */
public class AgentSpecialEditDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -5377606879040445878L;

	private final CallcenterModelCaller[] caller;
	private final Double[] callerTime;
	private JTextField[] callerTextField;
	private int result=-1;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param caller	Liste mit allen Anrufergruppen
	 * @param skillLevel	Skill-Level der Agentengruppe (zur Bestimmung, welche Anrufergruppen überhaupt in Frage kommen)
	 * @param helpCallback	Hilfe-Callback
	 */
	public AgentSpecialEditDialog(final Window owner, final List<CallcenterModelCaller> caller, final CallcenterModelSkillLevel skillLevel, final Runnable helpCallback) {
		super(owner,Language.tr("Editor.AgentsGroup.CountFromCalculatedLoad.Title"),null,false,helpCallback);

		List<CallcenterModelCaller> callerTempRecords=new ArrayList<CallcenterModelCaller>();
		List<Double> callerTempTime=new ArrayList<Double>();
		for (int i=0;i<skillLevel.callerTypeName.size();i++) for (int j=0;j<caller.size();j++) if (caller.get(j).name.equalsIgnoreCase(skillLevel.callerTypeName.get(i))) {
			callerTempRecords.add(caller.get(j));
			double d=0;
			String s=skillLevel.callerTypeWorkingTimeAddOn.get(i);
			if (s!=null && !s.isEmpty() && !s.equals("0")) {
				MathParser calc=new CalcSystem(s,new String[]{"w"});
				if (calc.parse()==-1) try {
					d+=Math.max(0,calc.calc(new double[]{0.0}));
				} catch (MathCalcError e) {}
			}
			callerTempTime.add(d+DistributionTools.getMean(skillLevel.callerTypeWorkingTime.get(i))+DistributionTools.getMean(skillLevel.callerTypePostProcessingTime.get(i)));
			break;
		}
		this.caller=callerTempRecords.toArray(new CallcenterModelCaller[0]);
		callerTime=callerTempTime.toArray(new Double[0]);

		createSimpleGUI(500,400,null,null);
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		content.setLayout(new BorderLayout());

		JPanel p2,p3,p4;
		JTextPane info;
		content.add(info=new JTextPane(),BorderLayout.NORTH);
		Color c=new Color(255,255,255,0);
		info.setBackground(c);
		info.setEditable(false);
		StyledDocument doc=info.getStyledDocument();
		Style defaultStyle=StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		Style s=doc.addStyle("default",defaultStyle);
		try {doc.insertString(doc.getLength(),Language.tr("Editor.AgentsGroup.CountFromCalculatedLoad.Info")+"\n",s);} catch (BadLocationException e) {}

		p3=new JPanel(new GridLayout(caller.length,2));
		callerTextField=new JTextField[caller.length];
		for (int i=0;i<caller.length;i++) {
			String name=caller[i].name;
			JLabel l=new JLabel(name);
			p4=new JPanel(new FlowLayout(FlowLayout.LEFT));
			p4.add(l);
			p3.add(p4);
			callerTextField[i]=new JTextField(10);
			callerTextField[i].setText("100%");
			p4=new JPanel(new FlowLayout(FlowLayout.LEFT));
			p4.add(callerTextField[i]);
			p3.add(p4);
		}
		JScrollPane sp=new JScrollPane(p3);
		sp.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		content.add(p2=new JPanel(),BorderLayout.CENTER);
		p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));
		p2.add(sp);
		p2.add(Box.createVerticalGlue());
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#checkData()
	 */
	@Override
	protected boolean checkData() {
		for (int i=0;i<callerTextField.length;i++) {
			Double d=NumberTools.getProbability(callerTextField[i].getText());
			if (d==null) {
				MsgBox.error(this,Language.tr("Editor.AgentsGroup.CountFromCalculatedLoad.ErrorTitle"),String.format(Language.tr("Editor.AgentsGroup.CountFromCalculatedLoad.ErrorInfo"),caller[i].name,callerTextField[i].getText()));
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
		double seconds=0;
		for (int i=0;i<callerTextField.length;i++) {
			Double d=NumberTools.getProbability(callerTextField[i].getText());
			if (d!=null && d>0) seconds+=caller[i].freshCallsCountMean*callerTime[i]*d;
		}
		result=(int)Math.round(seconds/1800);
	}

	/**
	 * Anzahl an Agenten*Intervalle die für diese Gruppe vorgesehen werden sollen
	 * @return	Anzahl an Agenten die auf die Intervalle verteilt werden sollen um die angegebene Bedienleistung erbringen zu können
	 */
	public int getResult() {
		return result;
	}
}
