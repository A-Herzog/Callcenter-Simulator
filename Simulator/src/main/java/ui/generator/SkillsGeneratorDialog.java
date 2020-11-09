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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import systemtools.MsgBox;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelSkillLevel;

/**
 * Dialog zum Laden der Bedienzeit- und Nachbearbeitungszeitverteilungen für ein ganzes Modell (Modell-Generator)
 * @author Alexander Herzog
 * @version 1.0
 */
public final class SkillsGeneratorDialog extends GeneratorBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4485497692649353582L;

	/** Nummern der Skill-Level */
	private final List<Integer> skillNr;
	/** Nummern der Skills innerhalb der Skill-Level */
	private final List<Integer> skillSubNr;
	/** Namen der Skill-Level */
	private final List<String> skillNames;
	/** Namen der Anrufertypen innerhalb der Skill-Level */
	private final List<String> skillCallerNames;
	/** Auswahl welche Spalten für das verwendet werden sollen */
	private List<JComboBox<String>> select;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param helpCallback	Hilfe-Callback
	 * @param model	Callcenter-Modell in das die Daten geladen werden sollen
	 */
	public SkillsGeneratorDialog(final Window owner, final Runnable helpCallback, final CallcenterModel model) {
		super(owner,Language.tr("Generator.LoadSkillData"),helpCallback,model);

		skillNr=new ArrayList<Integer>();
		skillSubNr=new ArrayList<Integer>();
		skillNames=new ArrayList<String>();
		skillCallerNames=new ArrayList<String>();
		for (int i=0;i<model.skills.size();i++) for (int j=0;j<model.skills.get(i).callerTypeName.size();j++) {
			skillNr.add(i);
			skillSubNr.add(j);
			skillNames.add(model.skills.get(i).name);
			skillCallerNames.add(model.skills.get(i).callerTypeName.get(j));
		}
	}

	/**
	 * Legt eine Combobox mit einem vorgegebenen Inhalt an und versucht einen sinnvollen Startwert einzustellen.
	 * @param label	Dieser Wert wird versucht in der Comboxbox zu finden und dann ggf. als Startwert einestellt.
	 * @param values1	Werte, die in die Combobox geschrieben werden sollen. Zusätzlich wird als erster Wert "Spalte nicht verwenden" geschrieben
	 * @param values2	Zusätzliche Angaben für die jeweils zweite Zeile
	 * @return	Liefert die neu erstellte Combobox zurück
	 */
	protected static JComboBox<String> createComboBox(String label, List<String> values1, List<String> values2) {
		JComboBox<String> comboBox=new JComboBox<String>();

		/* Combobox befüllen */
		comboBox.addItem(Language.tr("Generator.DoNotUseColumn"));
		for (int i=0;i<values1.size();i++) {
			comboBox.addItem("<html><body>"+values1.get(i)+"<br><b>"+values2.get(i)+" - "+Language.tr("Generator.HoldingTime")+"</b></body><html>");
			comboBox.addItem("<html><body>"+values1.get(i)+"<br><b>"+values2.get(i)+" - "+Language.tr("Generator.PostProcessingTime")+"</b></body><html>");
		}

		/* Passenden initialen Wert festlegen */
		if (comboBox.getItemCount()>0) comboBox.setSelectedIndex(0);

		return comboBox;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.generator.GeneratorBaseDialog#tableLoaded()
	 */
	@Override
	protected boolean tableLoaded() {
		data.setLayout(new BoxLayout(data,BoxLayout.Y_AXIS));
		JPanel tab=new JPanel(); data.add(tab);
		tab.setLayout(new GridLayout(colIndex.size()+1,2));

		select=new ArrayList<JComboBox<String>>();

		tab.add(createHeadingLabel(Language.tr("Generator.TableColumn")));
		tab.add(createHeadingLabel(Language.tr("Generator.Distribution")));

		for (int i=0;i<colIndex.size();i++) {
			int index=colIndex.get(i);

			tab.add(createLabel(heading[index]));

			JComboBox<String> box;
			JPanel p;
			tab.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p.add(box=createComboBox(heading[index],skillNames,skillCallerNames));
			select.add(box);
		}

		data.add(Box.createVerticalGlue());

		return true;
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
				index--;
				String s=skillCallerNames.get(index/2);
				s+=" - "+((index%2==0)?Language.tr("Generator.HoldingTime"):Language.tr("Generator.PostProcessingTime"));
				MsgBox.error(this,Language.tr("Generator.Error.MultipleColumnsAsSourceForOneSkillLevel.Title"),String.format(Language.tr("Generator.Error.MultipleColumnsAsSourceForOneSkillLevel.Info"),heading[colIndex.get(i)].replace('\n',' '),heading[colIndex.get(j)].replace('\n',' '),s));
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
			if (sel<=0) continue; /* ==0 heißt "nicht verwenden" */
			sel--;
			int nr1=skillNr.get(sel/2);
			int nr2=skillSubNr.get(sel/2);
			boolean time2=(sel%2!=0);
			int colNr=colIndex.get(i);

			CallcenterModelSkillLevel sl=model.skills.get(nr1);
			DataDistributionImpl dist=new DataDistributionImpl(3600,3600);

			for (int j=rows[0][colNr];j<=rows[1][colNr];j++) {
				Double d=NumberTools.getNotNegativeDouble(table.getValue(colNr,j));
				if (d==null) d=0.0;
				dist.densityData[j-rows[0][colNr]]=d;
			}
			dist.updateCumulativeDensity();
			if (time2) {
				sl.callerTypePostProcessingTime.set(nr2,dist);
				sl.callerTypeIntervalPostProcessingTime.set(nr2,new AbstractRealDistribution[48]);
			} else {
				sl.callerTypeWorkingTime.set(nr2,dist);
				sl.callerTypeIntervalWorkingTime.set(nr2,new AbstractRealDistribution[48]);
			}
		}
	}
}
