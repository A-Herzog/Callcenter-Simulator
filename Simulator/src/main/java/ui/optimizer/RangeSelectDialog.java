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

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import mathtools.TimeTools;
import systemtools.MsgBox;
import ui.editor.BaseEditDialog;
import ui.editor.CallcenterThresholdIntervalsDialog;

/**
 * Dialog zur Auswahl eines Zeitbereichs, über den optimiert werden soll
 * @author Alexander Herzog
 * @version 1.0
 * @see CallcenterThresholdIntervalsDialog
 * @see OptimizeEditPanel
 */
public class RangeSelectDialog extends BaseEditDialog {
	private static final long serialVersionUID = 3073283623605767866L;

	private JComboBox<String> comboMin;
	private JComboBox<String> comboMax;

	/**
	 * Wofür soll ein Zeitbereich ausgewählt werden=
	 * @author Alexander Herzog
	 * @see RangeSelectDialog#RangeSelectDialog(Window, Runnable, Mode)
	 */
	public enum Mode {
		/** Zeitbereich für Optimierungen anzeigen */
		MODE_OPTIMIZE,
		/** Zeitbereich für die Schwellenwert-Konfiguration anzeigen */
		MODE_THRESHOLD
	}

	private final Mode mode;

	private static String getTitle(Mode mode) {
		switch (mode) {
		case MODE_OPTIMIZE: return Language.tr("Optimizer.Tabs.UseIntervals.RangeDialog.Title");
		case MODE_THRESHOLD: return Language.tr("Editor.GeneralData.ThresholdValues.Intervals.RangeDialog.Title");
		default: return "";
		}
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param helpCallback	Hilfe-Callback
	 * @param mode	Wofür soll der Zeitbereich ausgewählt werden?
	 */
	public RangeSelectDialog(final Window owner, final Runnable helpCallback, final Mode mode) {
		super(owner,getTitle(mode),false,helpCallback);
		this.mode=mode;
		createSimpleGUI(300,300,null,null);
		pack();
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		String[] list=new String[48];
		for (int i=0;i<list.length;i++) list[i]=TimeTools.formatTime(i*1800)+"-"+TimeTools.formatTime((i+1)*1800-1);
		JPanel p;
		String s;
		content.setLayout(new GridLayout(2,2));
		switch (mode) {
		case MODE_OPTIMIZE: s=Language.tr("Optimizer.Tabs.UseIntervals.RangeDialog.Min"); break;
		case MODE_THRESHOLD: s=Language.tr("Editor.GeneralData.ThresholdValues.Intervals.RangeDialog.Min"); break;
		default: s="";
		}
		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT))); p.add(new JLabel(s));
		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT))); p.add(comboMin=new JComboBox<String>(list));
		switch (mode) {
		case MODE_OPTIMIZE: s=Language.tr("Optimizer.Tabs.UseIntervals.RangeDialog.Max"); break;
		case MODE_THRESHOLD: s=Language.tr("Editor.GeneralData.ThresholdValues.Intervals.RangeDialog.Max"); break;
		default: s="";
		}
		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT))); p.add(new JLabel(s));
		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT))); p.add(comboMax=new JComboBox<String>(list));
		comboMin.setSelectedIndex(16);
		comboMax.setSelectedIndex(35);
	}

	@Override
	protected boolean checkData() {
		String s,t;
		switch (mode) {
		case MODE_OPTIMIZE:
			s=Language.tr("Optimizer.Tabs.UseIntervals.RangeDialog.ErrorTitle");
			t=Language.tr("Optimizer.Tabs.UseIntervals.RangeDialog.ErrorSubTitle");
			break;
		case MODE_THRESHOLD:
			s=Language.tr("Editor.GeneralData.ThresholdValues.Intervals.RangeDialog.ErrorTitle");
			t=Language.tr("Editor.GeneralData.ThresholdValues.Intervals.RangeDialog.ErrorSubTitle");
			break;
		default:
			s="";
			t="";
		}
		if (comboMin.getSelectedIndex()>comboMax.getSelectedIndex()) {
			MsgBox.error(this,s,t);
			return false;
		}
		return true;
	}

	/**
	 * Liefert den Index des ersten Intervalls.
	 * @return	0-basierter Index des ersten Intervalls
	 */
	public int getMin() {
		return comboMin.getSelectedIndex();
	}

	/**
	 * Liefert den Index des letzten Intervalls.
	 * @return	0-basierter Index des letzten Intervalls
	 */
	public int getMax() {
		return comboMax.getSelectedIndex();
	}
}
