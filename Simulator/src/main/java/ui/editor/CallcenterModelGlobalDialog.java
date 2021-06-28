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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import parser.CalcSystem;
import parser.MathCalcError;
import parser.MathParser;
import systemtools.MsgBox;
import ui.HelpLink;
import ui.images.Images;
import ui.model.CallcenterModel;

/**
 * In diesem Dialog können globale Einstellungen (z.B. Anzahl an
 * zu simulierenden Tagen) für das Callcenter-Modell vorgenommen werden.
 * @author Alexander Herzog
 * @see CallcenterModel
 * @see CallcenterModelEditorPanel
 */
public final class CallcenterModelGlobalDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 697889756470834433L;

	/** Eingabefeld für die maximale Warteschlangenlänge */
	private JTextField maxQueueLength;
	/** Eingabefeld für die Anzahl an zu simulierenden Tagen */
	private JTextField days;
	/** Auswahlbox für die gewünschte Schichtlänge */
	private JComboBox<String> preferredShiftLength;
	/** Auswahlbox für die minimale Schichtlänge */
	private JComboBox<String> minimumShiftLength;
	/** Eingabefeld für den Service-Level */
	private JTextField serviceLevel;
	/** Schaltfläche "Produktivität der Agentengruppen" */
	private JButton efficiency;
	/** Schaltfläche "Krankheitsbedingter Zuschlag" */
	private JButton addition;

	/** Callcenter-Modell für das die Einstellungen bearbeitet werden sollen */
	private final CallcenterModel model;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param model	Callcenter-Modell für das die Einstellungen bearbeitet werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param helpLink	Hilfe-Link
	 * @see #getModel()
	 */
	public CallcenterModelGlobalDialog(final Window owner, final CallcenterModel model, final boolean readOnly, final HelpLink helpLink) {
		super(owner,Language.tr("Editor.GeneralData.GlobalParameters"),readOnly,helpLink.dialogGlobalParameters);
		this.model=model;
		createSimpleGUI(500,400,null,null);
		pack();

		maxQueueLength.setText(model.maxQueueLength);
		days.setText(""+model.days);
		preferredShiftLength.setSelectedIndex(Math.max(0,model.preferredShiftLength-1));
		minimumShiftLength.setSelectedIndex(Math.max(0,model.minimumShiftLength-1));
		serviceLevel.setText(""+model.serviceLevelSeconds);
	}

	/**
	 * Erzeugt eine Zeile mit einem Text
	 * @param p	Übergeordnetes Element
	 * @param name	Text der in der neuen Zeile angezeigt werden soll
	 */
	private void addLabel(final JPanel p, final String name) {
		JPanel subPanel;
		p.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)));
		subPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		subPanel.add(new JLabel(name));
	}

	/**
	 * Erzeugt eine Eingabezeile
	 * @param p	Übergeordnetes Element
	 * @param name	Beschriftung des Eingabefeldes
	 * @param initialValue Anfänglich anzuzeigender Wert
	 * @param width	Breite des Eingabefeldes
	 * @param additionalInfo	Optionaler hinter dem Eingabefeld anzuzeigender Text (kann <code>null</code> sein)
	 * @return	Liefert das neue Eingabefeld zurück (welches bereits in das übergeordnete Panel eingefügt ist)
	 */
	private JTextField addInputLine(JPanel p, String name, String initialValue, int width, String additionalInfo) {
		JPanel subPanel,p2;

		p.add(subPanel=new JPanel());
		subPanel.setLayout(new BoxLayout(subPanel,BoxLayout.X_AXIS));
		subPanel.add(new JLabel(name));
		subPanel.add(Box.createHorizontalGlue());

		JTextField text;
		p.add(subPanel=new JPanel());
		subPanel.setLayout(new BoxLayout(subPanel,BoxLayout.X_AXIS));
		subPanel.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)));
		p2.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		p2.add(text=new JTextField(initialValue,width));

		if (additionalInfo!=null && !additionalInfo.isEmpty()) {
			p2.add(Box.createHorizontalStrut(10));
			p2.add(new JLabel(additionalInfo));
		}
		subPanel.add(Box.createHorizontalGlue());

		p.add(Box.createVerticalStrut(5));

		return text;
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		JPanel p,p2;

		content.setLayout(new BorderLayout());
		content.add(p=new JPanel(),BorderLayout.CENTER);

		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));

		maxQueueLength=addInputLine(p,Language.tr("Editor.GeneralData.GlobalParameters.MaximumQueueLength"),model.maxQueueLength,5,"("+Language.tr("Editor.GeneralData.GlobalParameters.MaximumQueueLength.Info")+")");
		maxQueueLength.addKeyListener(new DialogElementListener());

		days=addInputLine(p,Language.tr("Editor.GeneralData.GlobalParameters.NumberOfDaysToBeSimulated"),""+model.days,5,"");
		days.addKeyListener(new DialogElementListener());

		addLabel(p,Language.tr("Editor.GeneralData.GlobalParameters.MinimumShiftLength"));
		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)));
		p2.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		String[] s=new String[48];
		s[0]=Language.tr("Editor.GeneralData.GlobalParameters.MinimumShiftLength.NoRestrictions");
		for (int i=1;i<s.length;i++) s[i]=TimeTools.formatTime(1800*(i+1));
		p2.add(minimumShiftLength=new JComboBox<>(s));
		p2.add(Box.createHorizontalStrut(10));
		p2.add(new JLabel("("+Language.tr("Editor.GeneralData.GlobalParameters.PreferedShiftLength.Info")+")"));
		minimumShiftLength.setSelectedIndex(Math.max(0,model.preferredShiftLength-1));
		p.add(Box.createVerticalStrut(5));

		addLabel(p,Language.tr("Editor.GeneralData.GlobalParameters.PreferedShiftLength"));
		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)));
		p2.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		s[0]=TimeTools.formatTime(1800);
		p2.add(preferredShiftLength=new JComboBox<>(s));
		p2.add(Box.createHorizontalStrut(10));
		p2.add(new JLabel("("+Language.tr("Editor.GeneralData.GlobalParameters.PreferedShiftLength.Info")+")"));
		preferredShiftLength.setSelectedIndex(Math.max(0,model.preferredShiftLength-1));
		p.add(Box.createVerticalStrut(5));

		serviceLevel=addInputLine(p,Language.tr("Editor.GeneralData.GlobalParameters.ServiceLevel"),""+model.serviceLevelSeconds,3,"("+Language.tr("Editor.GeneralData.GlobalParameters.ServiceLevel.Info")+")");
		serviceLevel.addKeyListener(new DialogElementListener());
		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(efficiency=new JButton(Language.tr("Editor.GeneralData.GlobalParameters.AgentGroupsProductivity")));
		efficiency.addActionListener(new ButtonActionListener());
		efficiency.setIcon(Images.EDITOR_AGENTS_EFFICIENCY.getIcon());
		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(addition=new JButton(Language.tr("Editor.GeneralData.GlobalParameters.DiseaseRelatedSurcharge")));
		addition.addActionListener(new ButtonActionListener());
		addition.setIcon(Images.EDITOR_AGENTS_ADDITION.getIcon());

		maxQueueLength.setEditable(!readOnly);
		days.setEditable(!readOnly);
		preferredShiftLength.setEnabled(!readOnly);
		minimumShiftLength.setEnabled(!readOnly);
		serviceLevel.setEnabled(!readOnly);
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#checkData()
	 */
	@Override
	protected boolean checkData() {
		String s=null, caption=null;
		Integer i;

		boolean ok=true;
		MathParser parser=new CalcSystem(maxQueueLength.getText(),new String[]{"a"});
		ok=(parser.parse()==-1);
		if (ok) try {parser.calc(new double[]{1.0});} catch (MathCalcError e) {ok=false;}
		if (!ok) {
			caption=Language.tr("Editor.GeneralData.GlobalParameters.MaximumQueueLength.InvalidIitle");
			s=String.format(Language.tr("Editor.GeneralData.GlobalParameters.MaximumQueueLength.InvalidInfo"),maxQueueLength.getText());
		}
		if (s==null) {
			i=NumberTools.getNotNegativeInteger(days,false);
			if (i==null || i<1) {caption=Language.tr("Editor.GeneralData.GlobalParameters.NumberOfDaysToBeSimulated.InvalidTitle"); s=String.format(Language.tr("Editor.GeneralData.GlobalParameters.NumberOfDaysToBeSimulated.InvalidInfo"),days.getText());}
		}
		if (s==null) {
			i=NumberTools.getNotNegativeInteger(serviceLevel,false);
			if (i==null || i<1) {caption=Language.tr("Editor.GeneralData.GlobalParameters.ServiceLevel.InvalidTitle"); s=String.format(Language.tr("Editor.GeneralData.GlobalParameters.ServiceLevel.InvalidInfo"),serviceLevel.toString());}
		}

		if (s!=null) {
			MsgBox.error(this,caption,s);
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#storeData()
	 */
	@Override
	protected void storeData() {
		model.maxQueueLength=maxQueueLength.getText();
		model.days=NumberTools.getNotNegativeInteger(days,false);
		model.preferredShiftLength=preferredShiftLength.getSelectedIndex()+1;
		model.minimumShiftLength=minimumShiftLength.getSelectedIndex()+1;
		model.serviceLevelSeconds=NumberTools.getNotNegativeShort(serviceLevel,false);
	}

	/**
	 * Liefert das veränderte Callcenter-Modell nach dem Schließen des Dialogs zurück.
	 * @return	Neues Callcenter-Modell
	 */
	public CallcenterModel getModel() {
		return model;
	}

	/**
	 * Reagiert auf Klicks auf die verschiedenen Schaltflächen
	 * @see CallcenterModelGlobalDialog#efficiency
	 * @see CallcenterModelGlobalDialog#addition
	 */
	private final class ButtonActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==efficiency) {
				EfficiencyEditDialog dialog=new EfficiencyEditDialog(owner,readOnly,true,model.efficiencyPerInterval,helpCallback,EfficiencyEditDialog.Mode.MODE_EFFICIENCY);
				dialog.setVisible(true);
				if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
				model.efficiencyPerInterval=dialog.getEfficiency();
				return;
			}
			if (e.getSource()==addition) {
				EfficiencyEditDialog dialog=new EfficiencyEditDialog(owner,readOnly,true,model.additionPerInterval,helpCallback,EfficiencyEditDialog.Mode.MODE_ADDITION);
				dialog.setVisible(true);
				if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
				model.additionPerInterval=dialog.getEfficiency();
				return;
			}
		}
	}

	/**
	 * Reagiert auf Tastendrücke in den verschiedenen
	 * Eingabefeldern des Dialogs
	 */
	private class DialogElementListener implements KeyListener {
		/**
		 * Reagiert auf ein Tastenereignis
		 * @param e	Tastenereignis
		 * @see #keyTyped(KeyEvent)
		 * @see #keyPressed(KeyEvent)
		 * @see #keyReleased(KeyEvent)
		 */
		private void keyEvent(KeyEvent e) {
			MathParser parser=new CalcSystem(maxQueueLength.getText(),new String[]{"a"});
			boolean ok=(parser.parse()==-1);
			if (ok) try {parser.calc(new double[]{1.0});} catch (MathCalcError ex) {ok=false;}
			if (ok) maxQueueLength.setBackground(NumberTools.getTextFieldDefaultBackground()); else maxQueueLength.setBackground(Color.red);
			NumberTools.getNotNegativeInteger(days,true);
			NumberTools.getNotNegativeInteger(serviceLevel,true);
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
}
