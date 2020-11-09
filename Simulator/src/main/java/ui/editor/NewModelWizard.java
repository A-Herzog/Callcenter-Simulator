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
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.LogNormalDistributionImpl;
import mathtools.distribution.swing.JDataDistributionEditPanel;
import systemtools.MsgBox;
import tools.SetupData;
import ui.HelpLink;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;
import ui.model.CallcenterModelCaller;
import ui.model.CallcenterModelSkillLevel;

/**
 * Assistentendialog zum Anlegen neuer Modelle
 * @author Alexander Herzog
 * @version 1.0
 */
public class NewModelWizard extends JDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -6076142078109006053L;

	/** Verknüpfung mit der Online-Hilfe */
	private final HelpLink helpLink;

	/** Panel in dem die einzelnen Assistenten-Seiten angezeigt werden sollen */
	private final JPanel main;
	/** Index der aktuell aktiven Seite */
	private int cardNr=-1;
	/** Zeigt den Informationstext zu der jeweiligen Seite oberhalb des Konfigurationsbereichs an. */
	private JTextPane info=null;
	/** Schaltfläche "Ok" */
	private final JButton okButton;
	/** Schaltfläche "Abbrechen" */
	private final JButton cancelButton;
	/** Schaltfläche "Hilfe" */
	private final JButton helpButton;
	/** Schaltfläche "Zurück" */
	private final JButton previousButton;
	/** Schaltfläche "Weiter" */
	private final JButton nextButton;

	/* Seite 1 */

	/** Verteilung der Erstanrufer */
	private JDataDistributionEditPanel freshCalls;

	/* Seite 2 */

	/** Option: Kunden geben das Warten nie auf */
	private JRadioButton waitingTimeTypeInfinite;
	/** Option: Mittlere Wartezeittoleranz der Kunden */
	private JRadioButton waitingTimeTypeValue;
	/** Eingabefeld für die mittlere Wartezeittoleranz */
	private JTextField waitingTimeValue;
	/** Option: Anteil der Warteabbrecher, der später einen neuen Versuch tätigt */
	private JCheckBox retryTypeValue;
	/** Eingabefeld für die Wiederholwahrscheinlichkeit */
	private JTextField retryTimeValue;

	/* Seite 3 */

	/** Option: Anteil der Kunden, der nach dem Gespräch weitergeleitet wird */
	private JCheckBox continueTypeValue;
	/** Eingabefeld für die Weiterleitungswahrscheinlichkeit */
	private JTextField continueProbabilityValue;

	/* Seite 4 */

	/** Eingabefeld für die mittlere Bediendauer */
	private JTextField workingTime1;
	/** Eingabefeld für die mittlere Nachbearbeitungszeit */
	private JTextField workingTime2;
	/** Eingabefeld für die geplante Auslastung der Agenten */
	private JTextField workLoad;

	/**
	 * Neues Callcenter-Modell
	 * @see #getModel()
	 */
	private CallcenterModel model=null;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param helpLink	Verknüpfung mit der Online-Hilfe
	 */
	public NewModelWizard(final Window owner, final HelpLink helpLink) {
		super(owner,Language.tr("NewModelWizard.Title"),Dialog.ModalityType.APPLICATION_MODAL);
		this.helpLink=helpLink;

		JPanel p;
		getContentPane().setLayout(new BorderLayout());

		/* Kopfzeile */

		/* Inhalt */
		getContentPane().add(main=new JPanel(new CardLayout()),BorderLayout.CENTER);
		buildContent();

		/* Fußzeile */
		getContentPane().add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);

		p.add(okButton=new JButton(Language.tr("Dialog.Button.Ok")));
		okButton.addActionListener(new ActionEvents());
		okButton.setIcon(Images.MSGBOX_OK.getIcon());
		getRootPane().setDefaultButton(okButton);

		p.add(cancelButton=new JButton(Language.tr("Dialog.Button.Cancel")));
		cancelButton.addActionListener(new ActionEvents());
		cancelButton.setIcon(Images.GENERAL_CANCEL.getIcon());

		if (helpLink.dialogWizard!=null) {
			p.add(helpButton=new JButton(Language.tr("Dialog.Button.Help")));
			helpButton.addActionListener(new ActionEvents());
			helpButton.setIcon(Images.HELP.getIcon());
		} else {
			helpButton=null;
		}

		p.add(previousButton=new JButton(Language.tr("Dialog.Button.Back")));
		previousButton.addActionListener(new ActionEvents());
		previousButton.setIcon(Images.ARROW_LEFT.getIcon());

		p.add(nextButton=new JButton(Language.tr("Dialog.Button.Forward")));
		nextButton.addActionListener(new ActionEvents());
		nextButton.setIcon(Images.ARROW_RIGHT.getIcon());

		addWindowListener(new WindowAdapter() {@Override
			public void windowClosing(WindowEvent event) {setVisible(false); dispose();}});
		setResizable(false);
		SetupData setup=SetupData.getSetup();
		setSize((int)Math.round(650*setup.scaleGUI),(int)Math.round(450*setup.scaleGUI));
		setLocationRelativeTo(owner);
		setPage(0);
	}

	/**
	 * Erstellt die eigentliche GUI.
	 */
	private void buildContent() {
		JPanel page,p,p2;

		/* Ankunftsstrom */
		main.add(page=new JPanel(new BorderLayout()),"0");
		page.add(freshCalls=new JDataDistributionEditPanel(new DataDistributionImpl(86399,48),JDataDistributionEditPanel.PlotMode.PLOT_DENSITY,true,1),BorderLayout.CENTER);
		freshCalls.setImageSaveSize(SetupData.getSetup().imageSize);

		/* Wartezeittoleranz */
		main.add(page=new JPanel(new FlowLayout(FlowLayout.LEFT)),"1");
		page.add(p=new JPanel());
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));

		ButtonGroup group=new ButtonGroup();
		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(waitingTimeTypeInfinite=new JRadioButton(Language.tr("NewModelWizard.InfiniteWaitingTimeTolerance")));
		group.add(waitingTimeTypeInfinite);
		waitingTimeTypeInfinite.addActionListener(new ActionEvents());
		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(waitingTimeTypeValue=new JRadioButton(Language.tr("NewModelWizard.AverageWaitingTimeTolerance")+" ("+Language.tr("Statistic.Units.InSeconds")+"):"));
		group.add(waitingTimeTypeValue);
		waitingTimeTypeValue.setSelected(true);
		waitingTimeTypeValue.addActionListener(new ActionEvents());
		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(waitingTimeValue=new JTextField("90",4));

		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(retryTypeValue=new JCheckBox(Language.tr("NewModelWizard.RetryProbability")+":"));
		retryTypeValue.setSelected(true);
		retryTypeValue.addActionListener(new ActionEvents());
		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(retryTimeValue=new JTextField("90%",4));

		/* Weiterleitungen */
		main.add(page=new JPanel(new FlowLayout(FlowLayout.LEFT)),"2");
		page.add(p=new JPanel());
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));

		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(continueTypeValue=new JCheckBox(Language.tr("NewModelWizard.ForwardingProbability")+":"));
		continueTypeValue.setSelected(true);
		continueTypeValue.addActionListener(new ActionEvents());
		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(continueProbabilityValue=new JTextField("20%",4));

		/* Bedienzeiten und Auslastung */
		main.add(page=new JPanel(new FlowLayout(FlowLayout.LEFT)),"3");
		page.add(p=new JPanel());
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));

		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(new JLabel(Language.tr("NewModelWizard.AverageHoldingTime")+" ("+Language.tr("Statistic.Units.InSeconds")+"):"));
		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(workingTime1=new JTextField("210",4));

		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(new JLabel(Language.tr("NewModelWizard.AveragePostProcessingTime")+" ("+Language.tr("Statistic.Units.InSeconds")+"):"));
		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(workingTime2=new JTextField("90",4));

		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(new JLabel(Language.tr("NewModelWizard.AverageLoad")+":"));
		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(workLoad=new JTextField("85%",4));
	}

	/**
	 * Liefert das neue Callcenter-Modell
	 * @return	Neues Callcenter-Modell
	 */
	public CallcenterModel getModel() {
		return model;
	}

	@Override
	protected JRootPane createRootPane() {
		JRootPane rootPane=new JRootPane();
		InputMap inputMap=rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		KeyStroke stroke=KeyStroke.getKeyStroke("ESCAPE");
		inputMap.put(stroke,"ESCAPE");
		rootPane.getActionMap().put("ESCAPE",new SpecialKeyListener(0));

		stroke=KeyStroke.getKeyStroke("F1");
		inputMap.put(stroke,"F1");
		rootPane.getActionMap().put("F1",new SpecialKeyListener(1));

		return rootPane;
	}

	/**
	 * Reagiert auf F1- und Escape-Tastendrücke
	 * auf dem Dialog selber.
	 */
	private class SpecialKeyListener extends AbstractAction {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -485008309903554823L;

		/**
		 * Aktion (0: Abbruch, 1: Hilfe)
		 */
		private final int action;

		/**
		 * Konstruktor der Klasse
		 * @param action	Aktion (0: Abbruch, 1: Hilfe)
		 */
		public SpecialKeyListener(int action) {this.action=action;}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			switch (action) {
			case 0:	if (cancelButton!=null && cancelButton.isVisible()) {setVisible(false); dispose();} break;
			case 1: if (helpLink.dialogWizard!=null) helpLink.dialogWizard.run(); break;
			}
		}
	}

	/**
	 * Wechselt die aktuelle Seite in dem Assistenten-Dialog.
	 * @param nr	Anzuzeigende Seite (0..3)
	 * @return	Gibt <code>true</code> zurück, wenn die Einstellungen auf der bisherigen Seite in Ordnung sind und somit ein Seitenwechsel möglich war
	 */
	private boolean setPage(int nr) {
		Integer I;
		Double D;
		switch (cardNr) {
		case 0:
			double sum=freshCalls.getDistribution().sum();
			double max=freshCalls.getDistribution().getMax();
			if (sum<1) {
				MsgBox.error(this,Language.tr("NewModelWizard.Error.NoCaller.Title"),Language.tr("NewModelWizard.Error.NoCaller.Info"));
				return false;
			}
			if (sum<500 || max<25) {
				if (!MsgBox.confirm(this,Language.tr("NewModelWizard.Error.SmallNumberOfCallers.Title"),Language.tr("NewModelWizard.Error.SmallNumberOfCallers.Info"),Language.tr("NewModelWizard.Error.SmallNumberOfCallers.Continue.Info"),Language.tr("NewModelWizard.Error.SmallNumberOfCallers.Edit.Info"))) return false;
			}
			break;
		case 1:
			if (waitingTimeTypeValue.isSelected()) {
				I=NumberTools.getNotNegativeInteger(waitingTimeValue,false);
				if (I==null) {
					MsgBox.error(this,Language.tr("NewModelWizard.Error.WaitingTimeTolerance.Title"),String.format(Language.tr("NewModelWizard.Error.WaitingTimeTolerance.Info"),waitingTimeValue.getText()));
					return false;
				}
				if (retryTypeValue.isSelected()) {
					D=NumberTools.getProbability(retryTimeValue,false);
					if (D==null) {
						MsgBox.error(this,Language.tr("NewModelWizard.Error.RetryProbability.Title"),String.format(Language.tr("NewModelWizard.Error.RetryProbability.Info"),retryTimeValue.getText()));
						return false;
					}
				}
			}
			break;
		case 2:
			if (continueTypeValue.isSelected()) {
				D=NumberTools.getProbability(continueProbabilityValue,false);
				if (D==null) {
					MsgBox.error(this,Language.tr("NewModelWizard.Error.ForwardingProbability.Title"),String.format(Language.tr("NewModelWizard.Error.ForwardingProbability.Info"),continueProbabilityValue.getText()));
					return false;
				}
			}
			break;
		case 3:
			I=NumberTools.getNotNegativeInteger(workingTime1,false);
			if (I==null) {
				MsgBox.error(this,Language.tr("NewModelWizard.Error.HoldingTime.Title"),String.format(Language.tr("NewModelWizard.Error.HoldingTime.Info"),workingTime1.getText()));
				return false;
			}
			I=NumberTools.getNotNegativeInteger(workingTime2,false);
			if (I==null) {
				MsgBox.error(this,Language.tr("NewModelWizard.Error.PostProcessingTime.Title"),String.format(Language.tr("NewModelWizard.Error.PostProcessingTime.Info"),workingTime2.getText()));
				return false;
			}
			D=NumberTools.getProbability(workLoad,false);
			if (D==null) {
				MsgBox.error(this,Language.tr("NewModelWizard.Error.WorkLoad.Title"),String.format(Language.tr("NewModelWizard.Error.WorkLoad.Info"),workLoad.getText()));
				return false;
			}
			break;
		}

		cardNr=nr;
		((CardLayout)main.getLayout()).show(main,""+nr);
		previousButton.setEnabled(nr>0);
		nextButton.setEnabled(nr<3);

		String text="";
		switch(nr) {
		case 0:	text=Language.tr("NewModelWizard.Page1Info"); break;
		case 1: text=Language.tr("NewModelWizard.Page2Info"); break;
		case 2:	text=Language.tr("NewModelWizard.Page3Info"); break;
		case 3:	text=Language.tr("NewModelWizard.Page4Info"); break;
		}

		if (info!=null) getContentPane().remove(info);
		getContentPane().add(info=new JTextPane(),BorderLayout.NORTH);
		Color c=new Color(255,255,255,0);
		info.setBackground(c);
		info.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		info.setEditable(false);
		StyledDocument doc=info.getStyledDocument();
		Style defaultStyle=StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		Style docStyle=doc.addStyle("default",defaultStyle);
		try {doc.insertString(doc.getLength(),text,docStyle);} catch (BadLocationException e) {}

		return true;
	}

	/**
	 * Erstellt auf Basis der Dialogeinstellungen das Callcenter-Modell.
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 * @see #model
	 */
	private boolean buildModel() {
		if (!setPage(cardNr)) return false;

		model=new CallcenterModel(Language.tr("NewModelWizard.Model.Name"));
		model.description=Language.tr("NewModelWizard.Model.Description");
		model.maxQueueLength="200";
		model.days=200;
		model.preferredShiftLength=16;

		/* Anrufergruppe anlegen */
		CallcenterModelCaller caller=new CallcenterModelCaller(Language.tr("NewModelWizard.Model.ClientType"));
		caller.freshCallsDist48=freshCalls.getDistribution();
		caller.freshCallsCountMean=(int)Math.round(caller.freshCallsDist48.sum());
		caller.scoreBase=50;
		caller.scoreSecond=1;
		caller.scoreContinued=50;
		if (waitingTimeTypeValue.isSelected()) caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_SHORT; else caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_OFF;
		if (waitingTimeTypeValue.isSelected()) {
			Integer I=NumberTools.getNotNegativeInteger(waitingTimeValue,false);
			caller.waitingTimeDist=new LogNormalDistributionImpl(I,I*((double)2)/3);
		} else {
			caller.waitingTimeDist=new LogNormalDistributionImpl(300,200);
		}

		if (retryTypeValue.isSelected()) {
			caller.retryTimeDist=new ExponentialDistribution(900);
			Double D=NumberTools.getProbability(retryTimeValue,false);
			caller.retryProbabiltyAfterBlockedFirstRetry=D;
			caller.retryProbabiltyAfterBlocked=D;
			caller.retryProbabiltyAfterGiveUpFirstRetry=D;
			caller.retryProbabiltyAfterGiveUp=D;
		} else {
			caller.retryTimeDist=new ExponentialDistribution(900);
			caller.retryProbabiltyAfterBlockedFirstRetry=0;
			caller.retryProbabiltyAfterBlocked=0;
			caller.retryProbabiltyAfterGiveUpFirstRetry=0;
			caller.retryProbabiltyAfterGiveUp=0;
		}
		if (continueTypeValue.isSelected()) {
			caller.continueProbability=NumberTools.getProbability(continueProbabilityValue,false);
			caller.continueTypeName.add(Language.tr("NewModelWizard.Model.ClientType"));
			caller.continueTypeRate.add(1.0);
		} else {
			caller.continueProbability=0.0;
		}
		model.caller.add(caller);

		/* Anlegen eines Callcenters */
		CallcenterModelCallcenter callcenter=new CallcenterModelCallcenter(Language.tr("NewModelWizard.Model.Callcenter"));
		callcenter.technicalFreeTime=0;
		callcenter.score=1;
		callcenter.agentScoreFreeTimeSinceLastCall=0;
		callcenter.agentScoreFreeTimePart=1;
		model.callcenter.add(callcenter);

		/* Agenten anlegen und zu Callcenter hinzufügen */
		CallcenterModelAgent agent=new CallcenterModelAgent();
		agent.count=-2;
		Integer w1=NumberTools.getNotNegativeInteger(workingTime1,false);
		Integer w2=NumberTools.getNotNegativeInteger(workingTime2,false);
		Double load=NumberTools.getProbability(workLoad,false);
		agent.byCallersAvailableHalfhours=Math.max(1,(int)Math.round((double)caller.freshCallsCountMean*(w1+w2)/1800*(2-load)*(1+caller.continueProbability)));
		agent.byCallers.add(Language.tr("NewModelWizard.Model.ClientType"));
		agent.byCallersRate.add(1.0);
		agent.skillLevel=Language.tr("NewModelWizard.Model.SkillLevel");
		callcenter.agents.add(agent);

		/* Skill-Level A anlegen */
		CallcenterModelSkillLevel skill=new CallcenterModelSkillLevel(Language.tr("NewModelWizard.Model.SkillLevel"));
		skill.callerTypeName.add(Language.tr("NewModelWizard.Model.ClientType"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(w1,((double)w1)/3));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(w2,((double)w2)/3));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		model.skills.add(skill);

		model.prepareData();

		return true;
	}

	/**
	 * Reagiert auf Klicks auf die Schaltflächen in dem Dialog.
	 */
	private class ActionEvents implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==cancelButton) {setVisible(false); dispose();}
			if (e.getSource()==okButton) {if (buildModel()) {setVisible(false); dispose();}}
			if (e.getSource()==helpButton && helpLink.dialogWizard!=null) {helpLink.dialogWizard.run(); return;}
			if (e.getSource()==previousButton) setPage(cardNr-1);
			if (e.getSource()==nextButton) setPage(cardNr+1);

			if (e.getSource()==waitingTimeTypeInfinite || e.getSource()==waitingTimeTypeValue || e.getSource()==retryTypeValue) {
				waitingTimeValue.setEnabled(waitingTimeTypeValue.isSelected());
				retryTypeValue.setEnabled(waitingTimeTypeValue.isSelected());
				retryTimeValue.setEnabled(waitingTimeTypeValue.isSelected() && retryTypeValue.isSelected());
			}
			if (e.getSource()==continueTypeValue) {
				continueProbabilityValue.setEnabled(continueTypeValue.isSelected());
			}
		}
	}
}
