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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.io.Serializable;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import ui.editor.BaseEditDialog;
import ui.model.CallcenterModel;
import ui.statistic.optimizer.StatisticViewerOptimizerBarChart;

/**
 * In diesem Dialog kann konfiguriert werden, welche Diagramme
 * auf der linken und auf der rechten Seite des Optimierungs-Panels
 * angezeigt werden sollen.
 * @author Alexander Herzog
 * @see OptimizePanel
 */
public class SetupOptimizeDiagrams extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2332940485811698342L;

	/** Zugehöriges Callcenter-Modell */
	private final CallcenterModel model;
	/** Bisheriger Datentyp für die linke Seite */
	private final int dataTypeLeft;
	/** Bisheriger Unter-Datentyp für die linke Seite */
	private final int dataNrLeft;
	/** Bisheriger Datentyp für die rechts Seite */
	private final int dataTypeRight;
	/** Bisheriger Unter-Datentyp für die rechte Seite */
	private final int dataNrRight;

	/** Listenansicht zur Auswahl der Anzeige im linken Diagramm */
	private JList<String> leftList;
	/** Listenansicht zur Auswahl der Anzeige im rechten Diagramm */
	private JList<String> rightList;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param helpCallback	Hilfe-Callback
	 * @param model	Zugehöriges Callcenter-Modell
	 * @param dataTypeLeft	Bisheriger Datentyp für die linke Seite
	 * @param dataNrLeft	Bisheriger Unter-Datentyp für die linke Seite
	 * @param dataTypeRight	Bisheriger Datentyp für die rechts Seite
	 * @param dataNrRight	Bisheriger Unter-Datentyp für die rechte Seite
	 * @see StatisticViewerOptimizerBarChart
	 */
	public SetupOptimizeDiagrams(final Window owner, final Runnable helpCallback, final CallcenterModel model, final int dataTypeLeft, final int dataNrLeft, final int dataTypeRight, final int dataNrRight) {
		super(owner,Language.tr("Optimizer.SetupDiagrams.Dialog.Title"),false,helpCallback);
		this.model=model;
		this.dataTypeLeft=dataTypeLeft;
		this.dataNrLeft=dataNrLeft;
		this.dataTypeRight=dataTypeRight;
		this.dataNrRight=dataNrRight;
		createSimpleGUI(600,700,null,null);
		setVisible(true);
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		content.setLayout(new GridLayout(1,2,5,0));
		JPanel left=new JPanel(new BorderLayout()); content.add(left);
		JPanel right=new JPanel(new BorderLayout()); content.add(right);

		left.add(new JLabel("<html><body><b>"+Language.tr("Optimizer.SetupDiagrams.Dialog.LeftDiagram")+"</b></body></html>"),BorderLayout.NORTH);
		right.add(new JLabel("<html><body><b>"+Language.tr("Optimizer.SetupDiagrams.Dialog.RightDiagram")+"</b></body></html>"),BorderLayout.NORTH);

		String[] listValues=StatisticViewerOptimizerBarChart.getDiagramTypesList(model);

		left.add(new JScrollPane(leftList=new JList<>(listValues)),BorderLayout.CENTER);
		right.add(new JScrollPane(rightList=new JList<>(listValues)),BorderLayout.CENTER);

		leftList.setSelectedIndex(StatisticViewerOptimizerBarChart.dataTypeToListIndex(model,dataTypeLeft,dataNrLeft));
		rightList.setSelectedIndex(StatisticViewerOptimizerBarChart.dataTypeToListIndex(model,dataTypeRight,dataNrRight));

		leftList.ensureIndexIsVisible(leftList.getSelectedIndex());
		rightList.ensureIndexIsVisible(rightList.getSelectedIndex());
	}

	/**
	 * Liefert die neuen in diesem Dialog eingestellten Daten
	 * @return	Neue Daten für die Diagramme in {@link OptimizePanel}: links, links(nr), rechts, rechts(nr)
	 */
	public int[] getData() {
		int[] l=StatisticViewerOptimizerBarChart.listIndexToDataType(model,leftList.getSelectedIndex());
		if (l==null || l.length!=2) l=new int[]{-1,-1};
		int[] r=StatisticViewerOptimizerBarChart.listIndexToDataType(model,rightList.getSelectedIndex());
		if (r==null || r.length!=2) r=new int[]{-2,-1};
		return new int[]{l[0],l[1],r[0],r[1]};
	}
}
