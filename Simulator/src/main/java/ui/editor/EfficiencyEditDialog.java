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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.swing.JDataDistributionEditPanel;
import systemtools.MsgBox;
import ui.model.CallcenterModelAgent;

/**
 * Zeigt einen Dialog zur Konfiguration der Produktivität der Agenten oder
 * des krankheitsbedingten Zuschlags an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class EfficiencyEditDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2212952612583896614L;

	private final Mode mode;
	private DataDistributionImpl efficiency;
	private final boolean global;

	private JCheckBox useGlobal;
	private JRadioButton efficiencyMode1, efficiencyMode2, efficiencyMode3;
	private JTextField constantEfficiency;
	private JDataDistributionEditPanel intervalEfficiency;

	/**
	 * Betriebsart des Dialogs
	 * @author Alexander Herzog
	 */
	public enum Mode {
		/** Produktivität */
		MODE_EFFICIENCY,
		/** krankheitsbedingter Zuschlag */
		MODE_ADDITION
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können die Einstellungen nur angezeigt und nicht geändert werden.
	 * @param global	Gibt an, ob Werte für die Produktivität hinterlegt werden müssen (<code>true</code>) oder ob auf ein übergeordnetes Element verwiesen werden kann (<code>false</code>)
	 * @param efficiency	Verteilung aus 48 Produktivitätswerten (kann auch <code>null</code> sein, dann wird "1" für alle Intervalle angenommen)
	 * @param helpCallback	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird eine "Hilfe"-Schaltfläche angezeigt und die <code>Run</code>-Methode dieses Objekts beim Klicken auf diese Schaltfläche aufgerufen
	 * @param mode	Betriebsart (Produktivität oder krankheitsbedingter Zuschlag)
	 */
	public EfficiencyEditDialog(Window owner, boolean readOnly, boolean global, DataDistributionImpl efficiency, Runnable helpCallback, Mode mode) {
		super(owner,getTitle(mode),readOnly,helpCallback);
		this.mode=mode;
		this.global=global;
		if (efficiency==null) this.efficiency=null; else this.efficiency=efficiency.clone();
		createSimpleGUI(750,550,null,null);
	}

	private static String getTitle(Mode mode) {
		switch (mode) {
		case MODE_EFFICIENCY: return Language.tr("Editor.Productivity.Productivity");
		case MODE_ADDITION: return Language.tr("Editor.Productivity.DiseaseRelatedSurcharge");
		default: return "";
		}
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		JPanel top, p,p2;
		String s;

		/* BorderLayout-Aufteilung */
		content.setLayout(new BorderLayout());
		content.add(top=new JPanel(),BorderLayout.NORTH);
		top.setLayout(new BoxLayout(top,BoxLayout.Y_AXIS));

		switch (mode) {
		case MODE_ADDITION: s=Language.tr("Editor.Productivity.DiseaseRelatedSurcharge.Info"); break;
		default: s="";
		}
		if (!s.isEmpty()) {
			top.add(p=new JPanel()); p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS)); p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p2.add(new JLabel("<html><body>"+s+"</body></html>"));
		}

		/* Global an/aus initialisieren */
		useGlobal=null;
		if (!global) {
			top.add(p=new JPanel()); p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS)); p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p2.add(useGlobal=new JCheckBox(Language.tr("Editor.Productivity.UseGlobalSettings")));
			useGlobal.addActionListener(new ButtonListener());
			useGlobal.setEnabled(!readOnly);
			useGlobal.setSelected(efficiency==null);
		}

		/* Radiobuttons initialisieren */
		top.add(p=new JPanel()); p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS)); p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		switch (mode) {
		case MODE_EFFICIENCY: s=Language.tr("Editor.Productivity.Productivity.Off"); break;
		case MODE_ADDITION: s=Language.tr("Editor.Productivity.DiseaseRelatedSurcharge.Off"); break;
		default: s="";
		}
		p2.add(efficiencyMode1=new JRadioButton(s,true));
		top.add(p=new JPanel()); p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS)); p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		switch (mode) {
		case MODE_EFFICIENCY: s=Language.tr("Editor.Productivity.Productivity.Conatant"); break;
		case MODE_ADDITION: s=Language.tr("Editor.Productivity.DiseaseRelatedSurcharge.Conatant"); break;
		default: s="";
		}
		p2.add(efficiencyMode2=new JRadioButton(s));
		p2.add(constantEfficiency=new JTextField("100%",6));
		constantEfficiency.addActionListener(new ButtonListener());
		constantEfficiency.addKeyListener(new ButtonListener());
		top.add(p=new JPanel()); p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS)); p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		switch (mode) {
		case MODE_EFFICIENCY: s=Language.tr("Editor.Productivity.Productivity.PerInterval"); break;
		case MODE_ADDITION: s=Language.tr("Editor.Productivity.DiseaseRelatedSurcharge.PerInterval"); break;
		default: s="";
		}
		p2.add(efficiencyMode3=new JRadioButton(s));

		ButtonGroup group=new ButtonGroup();
		group.add(efficiencyMode1);
		group.add(efficiencyMode2);
		group.add(efficiencyMode3);

		/* Diagramm initialisieren */
		DataDistributionImpl dist=new DataDistributionImpl(CallcenterModelAgent.countPerIntervalMaxX,48);
		dist.setToValue(1);
		content.add(intervalEfficiency=new JDataDistributionEditPanel(dist,JDataDistributionEditPanel.PlotMode.PLOT_DENSITY,true,0.01),BorderLayout.CENTER);
		intervalEfficiency.setLabelFormat(JDataDistributionEditPanel.LabelMode.LABEL_PERCENT);

		/* Daten laden */
		int selectMode=1;
		double lastVal=1;
		if (efficiency!=null) {
			lastVal=efficiency.densityData[0];
			for (int i=1;i<efficiency.densityData.length;i++) if (efficiency.densityData[i]!=lastVal) {selectMode=3; break;}
			if (selectMode==1 && lastVal!=1) selectMode=2;
		} else {
			efficiency=new DataDistributionImpl(CallcenterModelAgent.countPerIntervalMaxX,48);
			efficiency.setToValue(1);
		}

		efficiencyMode1.setSelected(selectMode==1);
		efficiencyMode2.setSelected(selectMode==2);
		if (selectMode==2) constantEfficiency.setText(NumberTools.formatPercent(lastVal));
		efficiencyMode3.setSelected(selectMode==3);
		if (selectMode==2 || selectMode==3) intervalEfficiency.setDistribution(efficiency);

		enableGUI(!readOnly && (useGlobal==null || !useGlobal.isSelected()));
		intervalEfficiency.addChangeListener(new ButtonListener());
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#checkData()
	 */
	@Override
	protected boolean checkData() {
		if (useGlobal!=null && useGlobal.isSelected()) return true;

		if (efficiencyMode2.isSelected()) {
			Double D=NumberTools.getExtProbability(constantEfficiency,true);
			if (D==null) {
				String s,caption;
				switch (mode) {
				case MODE_EFFICIENCY: caption=Language.tr("Editor.Productivity.Productivity.InvalidTitle"); s=Language.tr("Editor.Productivity.Productivity.InvalidInfo"); break;
				case MODE_ADDITION: caption=Language.tr("Editor.Productivity.DiseaseRelatedSurcharge.InvalidTitle"); s=Language.tr("Editor.Productivity.DiseaseRelatedSurcharge.InvalidInfo"); break;
				default: caption=""; s="";
				}
				MsgBox.error(this,caption,s);
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
		if (useGlobal!=null && useGlobal.isSelected()) {efficiency=null; return;}

		efficiency=new DataDistributionImpl(CallcenterModelAgent.countPerIntervalMaxX,48);
		efficiency.setToValue(1);

		if (efficiencyMode2.isSelected()) {
			Double D=NumberTools.getExtProbability(constantEfficiency,true);
			efficiency.setToValue(D);
			return;
		}

		if (efficiencyMode3.isSelected()) {
			efficiency=intervalEfficiency.getDistribution();
			return;
		}
	}

	/**
	 * Liefert die vom Benutzer konfigurierte Produktivitätswerteverteilung zurück.
	 * @return	Verteilung aus 48 Produktivitätswerten oder <code>null</code>, wenn gewählt wurde, dass die übergeordnete Verteilung verwendet werden soll.
	 */
	public final DataDistributionImpl getEfficiency() {
		return efficiency;
	}

	private final void enableGUI(boolean enabled) {
		efficiencyMode1.setEnabled(enabled);
		efficiencyMode2.setEnabled(enabled);
		constantEfficiency.setEnabled(enabled);
		efficiencyMode3.setEnabled(enabled);
		intervalEfficiency.setEditable(enabled);
	}

	private final class ButtonListener implements ActionListener, KeyListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==useGlobal) {enableGUI(!useGlobal.isSelected()); return;}
			if (e.getSource()==constantEfficiency) {efficiencyMode2.setSelected(true); return;}
			if (e.getSource()==intervalEfficiency) {efficiencyMode3.setSelected(true); return;}
		}

		private void keyEvent(KeyEvent e) {
			NumberTools.getExtProbability(constantEfficiency,true);
			if (e.getSource()==constantEfficiency) efficiencyMode2.setSelected(true);
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
