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
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import ui.HelpLink;
import ui.Preplanning;
import ui.editor.BaseEditDialog;
import ui.model.CallcenterModel;

/**
 * Über dieses Dialog ist es möglich, auf Basis von Erlang-C-Überlegungen
 * automatisiert neue Agentenzahlen festzulegen, um so eine Basis für
 * spätere Optimierungen zu erhalten.
 * @author Alexander Herzog
 */
public class PreplanningDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -3851051029525031101L;

	/**
	 * Ausgangs-Callcenter-Modell
	 */
	private final CallcenterModel baseModel;

	/**
	 * Neues Callcenter-Modell
	 * @see #getResultModel()
	 */
	private CallcenterModel resultModel;

	/** Vorschlagswerte für das Kenngrößen-Wert-Eingabefeld */
	private final String[] values=new String[]{"98%","30","80%"};
	/** Fortschrittsanzeige */
	private JProgressBar progress;
	/** Zuletzt in {@link #comboMode} gewählter Eintrag */
	private int lastSelectedIndex=0;
	/** Auswahlfeld für die Art der Vorplanung */
	private JComboBox<String> comboMode;
	/** Auswahlfeld für die Art der optionalen vorhergehenden Modellvereinfachung */
	private JComboBox<String> comboSimplify;
	/** Auswahlfeld für die Ziel-Kenngröße */
	private JComboBox<String> comboValueType;
	/** Eingabefeld für den Wert der Ziel-Kenngröße */
	private JTextField value;
	/** Zusätzliche Informationen für das Ziel-Kenngröße-Eingabefeld */
	private JLabel valueInfo;
	/** Eingabefeld für die Auslastung */
	private JTextField valueFixed;
	/** Zusätzliche Informationen für das Auslastung-Eingabefeld */
	private JLabel valueFixedInfo;

	/**
	 * Arbeitssthread in dem die Anpassungen durchgeführt werden.
	 * @see Preplanning
	 */
	private Thread planningThread;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param baseModel	Ausgangs-Callcenter-Modell
	 * @param helpLink	Hilfe-Link
	 */
	public PreplanningDialog(final Window owner, final CallcenterModel baseModel, final HelpLink helpLink) {
		super(owner,Language.tr("Editor.Preplanning.Title"),false,helpLink.dialogPreplanning);
		this.baseModel=baseModel;
		createSimpleGUI(500,400,null,null);
		pack();
		progress.setVisible(false);
		new ComboListener().actionPerformed(new ActionEvent(comboMode,0,null));
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		content.setLayout(new BorderLayout());
		JPanel p2;

		content.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		p2.add(new JLabel("<html>"+Language.tr("Editor.Preplanning.ModelChangeWarning")+"</html>"));

		JPanel p=new JPanel();
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
		content.add(p,BorderLayout.CENTER);

		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(comboMode=new JComboBox<>(new String[]{Language.tr("Editor.Preplanning.Mode.Fixed"),Language.tr("Editor.Preplanning.Mode.ErlangCSimple"),Language.tr("Editor.Preplanning.Mode.ErlangCComplex")}));
		comboMode.setSelectedIndex(2);
		comboMode.addActionListener(new ComboListener());

		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(comboSimplify=new JComboBox<>(new String[]{Language.tr("Editor.Preplanning.Simplify.No"),Language.tr("Editor.Preplanning.Simplify.MeanAHT"),Language.tr("Editor.Preplanning.Simplify.MaxAHT")}));
		comboSimplify.setSelectedIndex(0);

		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(comboValueType=new JComboBox<>(new String[]{Language.tr("SimStatistic.Accessibility"),Language.tr("SimStatistic.WaitingTime"),Language.tr("SimStatistic.ServiceLevel")}));
		comboValueType.setSelectedIndex(0);
		comboValueType.addActionListener(new ComboListener());
		p2.add(value=new JTextField("98%",5));
		value.addActionListener(new EditListener());
		p2.add(valueInfo=new JLabel(Language.tr("Statistic.Seconds")));
		valueInfo.setVisible(false);

		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(valueFixedInfo=new JLabel(Language.tr("SimStatistic.WorkLoad")));
		p2.add(valueFixed=new JTextField("90%",5));
		valueFixed.addActionListener(new EditListener());

		p.add(progress=new JProgressBar(SwingConstants.HORIZONTAL,0,100));
		progress.setStringPainted(true);
	}

	@Override
	protected boolean checkData() {
		if (planningThread!=null) return false;
		if (resultModel!=null) return true;

		if (comboMode.getSelectedIndex()==0) {
			if (NumberTools.getNotNegativeDouble(valueFixed,true)==null) return false;
		} else {
			if (comboValueType.getSelectedIndex()==1) {
				if (NumberTools.getNotNegativeInteger(value,true)==null) return false;
			} else {
				if (NumberTools.getNotNegativeDouble(value,true)==null) return false;
			}
		}

		setEnabled(false);
		progress.setVisible(true);
		doLayout();
		repaint();

		final Preplanning preplanning=new Preplanning(baseModel);

		Preplanning.Mode i=Preplanning.Mode.MODE_SUCCESS;
		if (comboMode.getSelectedIndex()==0) {
			i=Preplanning.Mode.MODE_FIXED_LOAD;
		} else {
			switch (comboValueType.getSelectedIndex()) {
			case 0: i=Preplanning.Mode.MODE_SUCCESS; break;
			case 1: i=Preplanning.Mode.MODE_WAITING_TIME; break;
			case 2: i=Preplanning.Mode.MODE_SERVICE_LEVEL; break;
			}
		}
		final Preplanning.Mode mode=i;

		i=Preplanning.Mode.MODE_SUCCESS;
		switch (comboSimplify.getSelectedIndex()) {
		case 0: i=Preplanning.Mode.SIMPLIFY_NO; break;
		case 1: i=Preplanning.Mode.SIMPLIFY_AVERAGE_AHT; break;
		case 2: i=Preplanning.Mode.SIMPLIFY_MAX_AHT; break;
		}
		final Preplanning.Mode simplify=i;

		double d=0;
		if (comboMode.getSelectedIndex()==0) {
			d=NumberTools.getNotNegativeDouble(this.valueFixed,true);
		} else {
			d=NumberTools.getNotNegativeDouble(this.value,true);
		}
		final double value=d;

		final boolean extended=(this.comboMode.getSelectedIndex()==2);

		planningThread=new Thread((Runnable)()->resultModel=preplanning.calc(mode,simplify,value,extended,Preplanning.DEFAULT_MULTI_SKILL_REDUCTION),"Erlang C global worker");
		planningThread.start();

		if (preplanning.isMultiSkillModel && comboSimplify.getSelectedIndex()==0  && this.comboMode.getSelectedIndex()>0) {
			MsgBox.warning(this,Language.tr("Editor.Preplanning.MultiSkillWarning.Title"),Language.tr("Editor.Preplanning.MultiSkillWarning.Info"));
			toFront();
		}

		final Timer timer=new Timer("ErlangCProgressTimer",false);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (!planningThread.isAlive()) {
					timer.cancel();
					planningThread=null;
					closeDialog(CLOSED_BY_OK);
					return;
				}
				progress.setValue((int)Math.round(100*preplanning.getStatus()));
			}
		},50,50);

		return false;
	}

	/**
	 * Liefert nach Abschluss des Dialogs das neue Modell mit den modifizierten Agentenzahlen.
	 * @return	Neues Callcenter-Modell
	 */
	public CallcenterModel getResultModel() {
		return resultModel;
	}

	/**
	 * Reagiert auf Änderungen an {@link PreplanningDialog#comboMode}
	 * und {@link PreplanningDialog#comboValueType}.
	 * @see PreplanningDialog#comboMode
	 * @see PreplanningDialog#comboValueType
	 */
	private class ComboListener implements ActionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public ComboListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==comboMode) {
				boolean fixedLoad=(comboMode.getSelectedIndex()==0);
				comboValueType.setVisible(!fixedLoad);
				value.setVisible(!fixedLoad);
				valueInfo.setVisible(!fixedLoad && comboValueType.getSelectedIndex()==1);
				valueFixed.setVisible(fixedLoad);
				valueFixedInfo.setVisible(fixedLoad);
				return;
			}
			if (e.getSource()==comboValueType) {
				if (comboValueType.getSelectedIndex()==lastSelectedIndex) return;
				values[lastSelectedIndex]=value.getText();
				lastSelectedIndex=comboValueType.getSelectedIndex();
				value.setText(values[lastSelectedIndex]);
				valueInfo.setVisible(comboMode.getSelectedIndex()!=0 && lastSelectedIndex==1);
				return;
			}
		}
	}

	/**
	 * Reagiert auf Eingaben in {@link PreplanningDialog#valueFixed}
	 * und {@link PreplanningDialog#value}.
	 * @see PreplanningDialog#valueFixed
	 * @see PreplanningDialog#value
	 */
	private class EditListener implements ActionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public EditListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (comboMode.getSelectedIndex()==0) {
				NumberTools.getNotNegativeDouble(valueFixed,true);
			} else {
				if (comboValueType.getSelectedIndex()==1) {
					NumberTools.getNotNegativeInteger(value,true);
				} else {
					NumberTools.getNotNegativeDouble(value,true);
				}
			}
		}
	}
}