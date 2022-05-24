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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import language.Language;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import systemtools.statistics.JCheckboxTable;
import ui.images.Images;
import ui.model.CallcenterModelCaller;
import ui.optimizer.RangeSelectDialog;

/**
 * Wählt einzelne Zeitintervalle für die Callcenter-Schwellenwert-Konfiguration aus.
 * @author Alexander Herzog
 * @see CallcenterThresholdValueEditDialog
 */
public class CallcenterThresholdIntervalsDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1378188742640248672L;

	/** Schaltfläche "Alle auswählen" */
	private JButton selectAllButton;
	/** Schaltfläche "Nichts auswählen" */
	private JButton selectNoneButton;
	/** Schaltfläche "Bereich auswählen" */
	private JButton selectRangeButton;
	/** Tabelle zur Darstellung der verfügbaren Zeitslots */
	private JCheckboxTable intervalTable;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param readOnly	Nur-Lese-Status
	 * @param intervals	Bisherige Werte für die Intervalle
	 * @param helpCallback	Hilfe-Callback
	 */
	public CallcenterThresholdIntervalsDialog(final Window owner, final boolean readOnly, DataDistributionImpl intervals, final Runnable helpCallback) {
		super(owner,Language.tr("Editor.GeneralData.ThresholdValues.IntervalsEdit"),readOnly,helpCallback);
		if (intervals==null) intervals=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,48);

		createSimpleGUI(500,500,null,null);

		boolean[] data=new boolean[48];
		for (int i=0;i<Math.min(intervals.densityData.length,data.length);i++) data[i]=(intervals.densityData[i]>0.1);
		intervalTable.setSelected(data);
	}

	/**
	 * Liefert die neuen Werte für die Intervalle.
	 * @return	Neue Werte für die Intervalle
	 */
	public DataDistributionImpl getIntervals() {
		boolean[] intervals=intervalTable.getSelected();
		DataDistributionImpl dist=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,48);
		for (int i=0;i<Math.min(intervals.length,dist.densityData.length);i++) dist.densityData[i]=intervals[i]?1.0:0.0;
		return dist;
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		JToolBar toolbar;

		content.setLayout(new BorderLayout());

		content.add(toolbar=new JToolBar(),BorderLayout.NORTH);
		toolbar.setFloatable(false);

		toolbar.add(selectAllButton=new JButton(Language.tr("Dialog.Select.All")));
		selectAllButton.addActionListener(new ButtonListener());
		selectAllButton.setToolTipText(Language.tr("Editor.GeneralData.ThresholdValues.Intervals.TooltipAll"));
		selectAllButton.setIcon(Images.EDIT_ADD.getIcon());

		toolbar.add(selectNoneButton=new JButton(Language.tr("Dialog.Select.Nothing")));
		selectNoneButton.addActionListener(new ButtonListener());
		selectNoneButton.setToolTipText(Language.tr("Editor.GeneralData.ThresholdValues.Intervals.TooltipNothing"));
		selectNoneButton.setIcon(Images.EDIT_DELETE.getIcon());

		toolbar.add(selectRangeButton=new JButton(Language.tr("Dialog.Select.Range")));
		selectRangeButton.addActionListener(new ButtonListener());
		selectRangeButton.setToolTipText(Language.tr("Editor.GeneralData.ThresholdValues.Intervals.TooltipRange"));
		selectRangeButton.setIcon(Images.EDIT_RANGE.getIcon());

		String[] intervalString=new String[48];
		for (int i=0;i<intervalString.length;i++) intervalString[i]=TimeTools.formatTime(i*1800)+"-"+TimeTools.formatTime((i+1)*1800-1);
		boolean[] intervalSelect=new boolean[48];
		Arrays.fill(intervalSelect,true);
		content.add(new JScrollPane(intervalTable=new JCheckboxTable(intervalString,intervalSelect,Language.tr("Editor.GeneralData.ThresholdValues.Intervals.TimeCaption"))),BorderLayout.CENTER);
	}

	/**
	 * Reagiert auf Klicks auf die Schaltflächen
	 */
	private class ButtonListener implements ActionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public ButtonListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==selectAllButton) {
				intervalTable.selectAll();
				return;
			}

			if (e.getSource()==selectNoneButton) {
				intervalTable.selectNone();
				return;
			}

			if (e.getSource()==selectRangeButton) {
				RangeSelectDialog dialog=new RangeSelectDialog(CallcenterThresholdIntervalsDialog.this,helpCallback,RangeSelectDialog.Mode.MODE_THRESHOLD);
				dialog.setVisible(true);
				if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
				intervalTable.selectRange(dialog.getMin(),dialog.getMax());
				return;
			}
		}
	}
}
