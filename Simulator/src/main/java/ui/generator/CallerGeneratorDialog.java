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

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import systemtools.MsgBox;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelCaller;

/**
 * Dialog zum Laden der Kundenankunftsverteilungen für ein ganzes Modell (Modell-Generator)
 * @author Alexander Herzog
 * @version 1.0
 */
public final class CallerGeneratorDialog extends GeneratorBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 5863544128633698724L;

	/** Liste der Namen der verfügbaren Kundengruppen */
	private final List<String> callerNames;
	private List<JComboBox<String>> select;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param helpCallback	Hilfe-Callback
	 * @param model	Callcenter-Modell in das die Daten geladen werden sollen
	 * @param intervals	Anzahl der Intervalle über die die Kunden pro Tag verteilt werden sollen (muss 24, 48 oder 96 sein)
	 */
	public CallerGeneratorDialog(final Window owner, final Runnable helpCallback, final CallcenterModel model, final int intervals) {
		super(owner,Language.tr("Generator.LoadClients"),helpCallback,model,intervals);

		callerNames=new ArrayList<String>();
		for (int i=0;i<model.caller.size();i++) callerNames.add(model.caller.get(i).name);
	}

	/**
	 * Legt eine Combobox mit einem vorgegebenen Inhalt an und versucht einen sinnvollen Startwert einzustellen.
	 * @param label	Dieser Wert wird versucht in der Comboxbox zu finden und dann ggf. als Startwert einestellt.
	 * @param values	Werte, die in die Combobox geschrieben werden sollen. Zusätzlich wird als erster Wert "Spalte nicht verwenden" geschrieben
	 * @return	Liefert die neu erstellte Combobox zurück
	 */
	protected static JComboBox<String> createComboBox(String label, List<String> values) {
		JComboBox<String> comboBox=new JComboBox<String>();

		/* Combobox befüllen */
		comboBox.addItem(Language.tr("Generator.DoNotUseColumn"));
		for (int i=0;i<values.size();i++) comboBox.addItem(values.get(i));

		/* Passenden initialen Wert festlegen */
		if (comboBox.getItemCount()>0) comboBox.setSelectedIndex(0);

		String[] labels=label.split("\n");

		for (int i=0;i<values.size();i++) for (int j=0;j<labels.length;j++) if (values.get(i).equalsIgnoreCase(labels[j])) {
			comboBox.setSelectedIndex(i+1); return comboBox;
		}

		for (int i=0;i<labels.length;i++) {
			List<Integer> match=new ArrayList<Integer>();
			String s=labels[i].toUpperCase();

			for (int j=0;j<values.size();j++) {
				String t=values.get(j).toUpperCase();
				if (t.contains(s) || s.contains(t)) match.add(j);
			}
			if (match.size()==1) {comboBox.setSelectedIndex(match.get(0)+1); return comboBox;}
		}

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
		tab.add(createHeadingLabel(Language.tr("Generator.ClientType")));


		for (int i=0;i<colIndex.size();i++) {
			int index=colIndex.get(i);

			tab.add(createLabel(heading[index]));

			JComboBox<String> box;
			JPanel p;
			tab.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p.add(box=createComboBox(heading[index],callerNames));
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
				MsgBox.error(this,Language.tr("Generator.Error.MultipleColumnsAsSourceForOneClientType.Title"),String.format(Language.tr("Generator.Error.MultipleColumnsAsSourceForOneClientType.Info"),heading[colIndex.get(i)].replace('\n',' '),heading[colIndex.get(j)].replace('\n',' '),model.caller.get(index-1).name));
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
			CallcenterModelCaller c=model.caller.get(sel-1);
			c.freshCallsDist24=null;
			c.freshCallsDist48=null;
			c.freshCallsDist96=null;
			switch (neededRows) {
			case 24 : c.freshCallsDist24=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,24); break;
			case 48 : c.freshCallsDist48=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,48); break;
			case 96 : c.freshCallsDist96=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,96); break;
			}
			double sum=0;
			for (int j=rows[0][colNr];j<=rows[1][colNr];j++) {
				Double d=NumberTools.getNotNegativeDouble(table.getValue(colNr,j));
				if (d==null) d=0.0;
				switch (neededRows) {
				case 24: c.freshCallsDist24.densityData[j-rows[0][colNr]]=d; break;
				case 48: c.freshCallsDist48.densityData[j-rows[0][colNr]]=d; break;
				case 96: c.freshCallsDist96.densityData[j-rows[0][colNr]]=d; break;
				}
				sum+=d;
			}
			if (c.freshCallsDist24!=null) c.freshCallsDist24.updateCumulativeDensity();
			if (c.freshCallsDist48!=null) c.freshCallsDist48.updateCumulativeDensity();
			if (c.freshCallsDist96!=null) c.freshCallsDist96.updateCumulativeDensity();
			c.freshCallsCountMean=(int) Math.round(sum);
		}
	}
}
