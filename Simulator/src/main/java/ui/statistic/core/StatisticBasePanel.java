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
package ui.statistic.core;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.Range;

import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import simulator.Statistics;
import systemtools.statistics.StatisticNode;
import systemtools.statistics.StatisticViewer;
import systemtools.statistics.StatisticViewerBarChart;
import systemtools.statistics.StatisticViewerJFreeChart;
import systemtools.statistics.StatisticViewerLineChart;
import ui.HelpLink;
import ui.commandline.AbstractReportCommandConnect;
import ui.statistic.StatisticPanel;

/**
 * Diese Klasse stellt ein <code>JPanel</code> zur Verfügung, welche Statistikdaten gemäß einer
 * Baumstruktur aus <code>StatisticNode</code>-Objekten anzeigt.
 * @author Alexander Herzog
 * @version 1.0
 * @see StatisticNode
 */
public class StatisticBasePanel extends JPanel implements AbstractReportCommandConnect {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -7826682745490191755L;

	/** Soll ein Vergleich zu den jeweils vorherigen Ergebnissen angeboten werden? */
	private final boolean storeLastRoot;

	/** Früheres Wurzelelement des Statistikbaums */
	private StatisticNode lastRoot;

	/** Wurzelelement des Statistikbaums */
	private StatisticNode currentRoot;

	/** Splitter zwischen Baumstruktur und Viewern */
	private final JSplitPane splitPane;

	/** Statistikbaum */
	private final StatisticTreePanel tree;

	/** Parallele Statistikdaten-Viewer */
	private final StatisticDataPanel[] data;

	/** Listener die bei Drag&amp;Drop-Operationen auf den Viewern benachrichtigt werden */
	private final List<ActionListener> fileDropListeners;

	/**
	 * Werden mehrere Statistikdokumente gleichzeitig angezeigt, so kann über dieses
	 * Array jeweils ein zusätzlicher Titel für die einzelnen Spalten festgelegt werden.
	 */
	protected String[] additionalTitle;

	/**
	 * Konstruktor der Klasse <code>StatisticPanel</code>
	 * @param title	Titel, der über der Baumstruktur angezeigt wird
	 * @param icon	Icon, das neben dem Titel über der Baumstruktur angezeigt wird (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param commandLineCommand	Kommandozeilenbefehl, über den einzelne Statistikergebnisse abgerufen werden können (zur Anzeige eines Kontextmenüs, welche den jeweiligen Befehl benennt; wird hier <code>null</code> übergeben, so erhält die Baumansicht kein Kontextmenü)
	 * @param commandLineDataFileName	Beispieldateiname für Kommandozeilenbefehl, über den einzelne Statistikergebnisse abgerufen werden können (kann <code>null</code> sein)
	 * @param nebeneinander	Bei mehreren Statistikdaten: nebeneinander (<code>true</code>) oder untereinander (<code>false</code>)
	 * @param filterTree	Option zur Filterung der Daten im Statistikbaum anzeigen
	 * @param helpModal	Hilfe-Callback für den Aufruf der Hilfeseite für die Statistik
	 * @param helpLink	Hilfe-Objekt, welches alle Hilfe-Links enthält
	 * @param startSimulation	Callback zum Starten einer Simulation
	 * @param loadStatistics	Callback zum Laden von Statistikdaten
	 * @param numberOfViewers	Anzahl der nebeneinander anzuzeigenden Ergebnisse
	 * @param storeLastRoot	Soll ein Vergleich zu den jeweils vorherigen Ergebnissen angeboten werden?
	 */
	public StatisticBasePanel(String title, URL icon, String commandLineCommand, String commandLineDataFileName, boolean nebeneinander, boolean filterTree, Runnable helpModal, HelpLink helpLink, Runnable startSimulation, Runnable loadStatistics, int numberOfViewers, final boolean storeLastRoot) {
		super(new BorderLayout());

		this.storeLastRoot=storeLastRoot;

		fileDropListeners=new ArrayList<>();

		add(splitPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT),BorderLayout.CENTER);
		splitPane.setContinuousLayout(true);

		splitPane.setLeftComponent(tree=new StatisticTreePanel(title, icon, commandLineCommand, commandLineDataFileName, filterTree, helpModal, helpLink, startSimulation, loadStatistics){
			private static final long serialVersionUID = 5439163871614142476L;
			@Override protected void updateViewer(final StatisticNode node, final StatisticViewer viewer[], final String info, final URL icon) {setViewer(node,viewer,info,icon);}
			@Override protected void saveFilterSettings() {saveFilterDialogSettings();}
			@Override protected void loadFilterSettings() {loadFilterDialogSettings();}
			@Override protected String getReportSelectSettings() {return getReportSettings();}
			@Override protected void setReportSelectSettings(String settings) {setReportSettings(settings);}
		});

		JPanel p=new JPanel();
		if (nebeneinander) {
			p.setLayout(new GridLayout(0,numberOfViewers));
		} else {
			p.setLayout(new GridLayout(numberOfViewers,0));
		}
		data=new StatisticDataPanel[numberOfViewers];
		for (int i=0;i<data.length;i++) p.add(data[i]=new StatisticDataPanel(()->{
			for (int j=0;j<data.length;j++) data[j].updateViewer();
		}),i);
		splitPane.setRightComponent(p);

		/* Copy-Hotkey erkennen */

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_DOWN_MASK),"CopyViewer");
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,InputEvent.CTRL_DOWN_MASK),"CopyViewer");
		getActionMap().put("CopyViewer",new AbstractAction() {
			private static final long serialVersionUID = 6834309003536671412L;
			@Override
			public void actionPerformed(ActionEvent e) {
				if (data==null  || data.length!=1 || data[0]==null) return;
				if (!(data[0] instanceof StatisticDataPanel)) return;
				final StatisticDataPanel viewer=data[0];
				viewer.copyData();
			}
		});
	}

	/**
	 * Konstruktor der Klasse {@link StatisticPanel}
	 * @param title	Titel, der über der Baumstruktur angezeigt wird
	 * @param icon	Icon, das neben dem Titel über der Baumstruktur angezeigt wird (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param commandLineCommand	Kommandozeilenbefehl, über den einzelne Statistikergebnisse abgerufen werden können (zur Anzeige eines Kontextmenüs, welche den jeweiligen Befehl benennt; wird hier <code>null</code> übergeben, so erhält die Baumansicht kein Kontextmenü)
	 * @param commandLineDataFileName	Beispieldateiname für Kommandozeilenbefehl, über den einzelne Statistikergebnisse abgerufen werden können (kann <code>null</code> sein)
	 * @param nebeneinander	Bei mehreren Statistikdaten: nebeneinander (<code>true</code>) oder untereinander (<code>false</code>)
	 * @param filterTree	Option zur Filterung der Daten im Statistikbaum anzeigen
	 * @param helpModal	Hilfe-Callback für den Aufruf der Hilfeseite für die Statistik
	 * @param helpLink	Hilfe-Objekt, welches alle Hilfe-Links enthält
	 * @param startSimulation	Callback zum Starten einer Simulation
	 * @param loadStatistics	Callback zum Laden von Statistikdaten
	 * @param storeLastRoot	Soll ein Vergleich zu den jeweils vorherigen Ergebnissen angeboten werden?
	 */
	public StatisticBasePanel(String title, URL icon, String commandLineCommand, String commandLineDataFileName, boolean nebeneinander, boolean filterTree, Runnable helpModal, HelpLink helpLink, Runnable startSimulation, Runnable loadStatistics, final boolean storeLastRoot) {
		this(title,icon,commandLineCommand,commandLineDataFileName,nebeneinander,filterTree,helpModal,helpLink,startSimulation,loadStatistics,1,storeLastRoot);
	}

	/**
	 * Wenn im Kontextmenü der Baumstruktur Befehle für die Kommandozeile angeboten werden
	 * sollen, über die die jeweilige Information über die Kommandozeile abgerufen werden
	 * kann, so muss hier ein Beispieldateiname für die zu verwendende Statistikdatei
	 * angegeben werden.
	 * @param commandLineDataFileName	Dateiname für die Statistikdatei der in Beispiel-Kommandozeilen-Befehlen angezeigt werden soll
	 */
	public void setDataFileName(final String commandLineDataFileName) {
		tree.setDataFileName(commandLineDataFileName);
	}

	/**
	 * Liefert {@link JFreeChart}-Komponenten aus einer Reihe von Viewern zurück.
	 * @param viewer	Viewer in denen nach {@link JFreeChart}-Komponenten gesucht werden soll
	 * @param chartClass	Viewer-Klasse die berücksichtigt werden soll
	 * @return	Array mit allen {@link JFreeChart}-Komponenten; kann auch <code>null</code> sein, wenn die Viewer nicht vom passenden Klassentyp sind.
	 * @see #adjustLineCharts(JFreeChart[])
	 * @see #adjustBarCharts(JFreeChart[])
	 */
	private final JFreeChart[] getCharts(StatisticViewer viewer[], Class<? extends StatisticViewer> chartClass) {
		JFreeChart[] chart=new JFreeChart[viewer.length];
		for (int i=0;i<viewer.length;i++) {
			if (!chartClass.isInstance(viewer[i])) return null;
			Container c=((StatisticViewerJFreeChart)viewer[i]).getViewer(false);
			if (!(c instanceof ChartPanel)) return null;
			chart[i]=((ChartPanel)c).getChart();
		}
		return chart;
	}

	/**
	 * Liefert zu einem Statistikknoten die Viewer vom letzten Simulationslauf (als Vergleichswerte)
	 * @param currentNode	Aktueller Statistikknoten
	 * @return	Viewer vom letzten Lauf (sofern verfügbar, sonst <code>null</code>)
	 * @see #lastRoot
	 * 	 */
	private StatisticViewer[] getLastViewer(final StatisticNode currentNode) {
		if (lastRoot==null || currentNode==null) return null;

		final List<Integer> path=currentNode.getPath();
		if (path==null) return null;

		final StatisticNode lastNode=lastRoot.getChildByPath(path);
		if (lastNode==null) return null;

		return lastNode.viewer;
	}

	/**
	 * Stellt neue Viewer für den Datenbereich ein.
	 * @param node	Ausgewählter Statistik-Knoten in der Baumdarstellung
	 * @param viewer	Zugehörige Viewer
	 * @param info	Infotext übern den Viewern
	 * @param icon	Icon über den Viewern
	 */
	private final void setViewer(final StatisticNode node, final StatisticViewer viewer[], final String info, final URL icon) {
		final StatisticViewer[] lastViewer=getLastViewer(node);

		for (int i=0;i<data.length;i++) {
			final Container viewerContainer=data[i].setViewer((viewer.length>i)?viewer[i]:null,(lastViewer!=null && lastViewer.length>i)?lastViewer[i]:null,(additionalTitle==null || additionalTitle.length<=i)?null:additionalTitle[i],info,icon);
			registerComponentAndChildsForFileDrop(viewerContainer);
		}

		if (viewer.length<2) return;

		for (int i=0;i<viewer.length;i++) if (viewer[i]==null || viewer[i].getType()!=StatisticViewer.ViewerType.TYPE_IMAGE) return;

		if (viewer[0].getImageType()==StatisticViewer.ViewerImageType.IMAGE_TYPE_LINE) {
			JFreeChart[] charts=getCharts(viewer,StatisticViewerLineChart.class);
			if (charts!=null) adjustLineCharts(charts);
		}
		if (viewer[0].getImageType()==StatisticViewer.ViewerImageType.IMAGE_TYPE_BAR) {
			JFreeChart[] charts=getCharts(viewer,StatisticViewerBarChart.class);
			if (charts!=null) adjustBarCharts(charts);
		}
	}

	/**
	 * Passt Viewer-übergreifend bei mehreren Liniendiagrammen den
	 * y-Achsenbereich an, so dass alle Diagramme denselben Bereich verwenden.
	 * @param chart	Anzupassende Diagramme
	 * @see #getCharts(StatisticViewer[], Class)
	 */
	private final void adjustLineCharts(JFreeChart[] chart) {
		for (int nr=0;nr<chart[0].getXYPlot().getRangeAxisCount();nr++) {
			Range r=chart[0].getXYPlot().getRangeAxis(nr).getRange();
			double min=r.getLowerBound();
			double max=r.getUpperBound();
			for (int i=1;i<chart.length;i++) {
				r=chart[i].getXYPlot().getRangeAxis(nr).getRange();
				min=Math.min(min,r.getLowerBound());
				max=Math.max(max,r.getUpperBound());
			}

			r=new Range(min,max);
			for (int i=0;i<chart.length;i++) chart[i].getXYPlot().getRangeAxis(nr).setRange(r);
		}
	}

	/**
	 * Passt Viewer-übergreifend bei mehreren Balkendiagrammen den
	 * y-Achsenbereich an, so dass alle Diagramme denselben Bereich verwenden.
	 * @param chart	Anzupassende Diagramme
	 * @see #getCharts(StatisticViewer[], Class)
	 */
	private final void adjustBarCharts(JFreeChart[] chart) {
		for (int nr=0;nr<chart[0].getCategoryPlot().getRangeAxisCount();nr++) {
			Range r=chart[0].getCategoryPlot().getRangeAxis(nr).getRange();
			double min=r.getLowerBound();
			double max=r.getUpperBound();
			for (int i=1;i<chart.length;i++) {
				r=chart[i].getCategoryPlot().getRangeAxis(nr).getRange();
				min=Math.min(min,r.getLowerBound());
				max=Math.max(max,r.getUpperBound());
			}

			r=new Range(min,max);
			for (int i=0;i<chart.length;i++) chart[i].getCategoryPlot().getRangeAxis(nr).setRange(r);
		}
	}

	/**
	 * Ruft den HTML-Report-Generator auf und speichert den Report in der angegebenen Form in der angegebenen Datei.
	 * @param outputFile	Dateiname, in der der HTML-Report gespeichert werden soll.
	 * @param inline	Gibt an, ob die Grafiken direkt in die HTML-Datei eingebettet werden sollen.
	 * @return	Gibt an, ob der Report erfolgreich erstellt werden konnte.
	 */
	@Override
	public boolean runReportGeneratorHTML(File outputFile, boolean inline, boolean exportAllItems) {
		return tree.runReportGeneratorHTML(outputFile,inline,exportAllItems);
	}

	/**
	 * Ruft den DOCX-Report-Generator auf und speichert den Report in der angegebenen Form in der angegebenen Datei.
	 * @param output	Dateiname, in der der DOCX-Report gespeichert werden soll.
	 * @param	exportAllItems	Alle Einträge exportieren oder nur gewählte
	 * @return	Gibt an, ob der Report erfolgreich erstellt werden konnte.
	 */
	@Override
	public boolean runReportGeneratorDOCX(File output, boolean exportAllItems) {
		return tree.runReportGeneratorDOCX(output,exportAllItems);
	}

	/**
	 * Ruft den PDF-Report-Generator auf und speichert den Report in der angegebenen Form in der angegebenen Datei.
	 * @param output	Dateiname, in der der PDF-Report gespeichert werden soll.
	 * @param	exportAllItems	Alle Einträge exportieren oder nur gewählte
	 * @return	Gibt an, ob der Report erfolgreich erstellt werden konnte.
	 */
	@Override
	public boolean runReportGeneratorPDF(File output, boolean exportAllItems) {
		return tree.runReportGeneratorPDF(output,exportAllItems);
	}

	/**
	 * Speichert eine Liste aller Einträge im Statistikbaum als Datei
	 * @param output	Dateiname, in der die Liste gespeichert werden soll.
	 * @return	Gibt an, ob die Liste erfolgreich gespeichert werden konnte.
	 */
	@Override
	public boolean getReportList(File output) {
		return tree.getReportList(output);
	}

	/**
	 * Speichert ein einzelnes Dokument aus dem Statisitikbaum als Datei
	 * @param output	Dateiname, in dem das Dokument gespeichert werden soll.
	 * @param entry	Gibt den Namen des Dokuments im Statistikbaum an.
	 * @return	Gibt an, ob das Dokument erfolgreich gespeichert werden konnte.
	 */
	@Override
	public boolean getReportListEntry(File output, String entry) {
		return tree.getReportListEntry(output,entry);
	}

	/**
	 * Gibt an, welche Statistikdaten angezeigt werden sollen.<br>
	 * Die Daten werden in Form einer Baumstruktur geordnet. Übergeben wird die
	 * Wurzel des Baumes, welche selbst keine Daten enthält.
	 * @param statisticData	Wurzel des Statistikdaten-Baumes
	 * @see StatisticNode
	 */
	public final void setStatisticData(StatisticNode statisticData) {
		setStatisticData(statisticData,null);
	}

	/**
	 * Gibt an, welche Statistikdaten angezeigt werden sollen.<br>
	 * Die Daten werden in Form einer Baumstruktur geordnet. Übergeben wird die
	 * Wurzel des Baumes, welche selbst keine Daten enthält.
	 * @param statisticData	Wurzel des Statistikdaten-Baumes
	 * @param statistic	Statistik-Objekt, welches alle Daten für den Schnellfilter enthält (kann <code>null</code> sein, dann wird keine xslt-Transform-Option angeboten)
	 * @see StatisticNode
	 */
	public final void setStatisticData(StatisticNode statisticData, Statistics statistic) {
		if (storeLastRoot) {
			lastRoot=currentRoot;
			currentRoot=statisticData;
		}

		tree.setStatisticData(statisticData,statistic,data.length);

		/* Breite der linken Spalte anpassen */
		Dimension d=tree.getPreferredSize();
		d.width=Math.min(d.width,Math.max(250,getBounds().width/5));

		/* Minimale Breite der Baumstruktur */
		d=tree.getMinimumSize();
		d.width=Math.max(d.width,250);
		tree.setMinimumSize(d);

		if (d.width!=splitPane.getDividerLocation()) splitPane.setDividerLocation(d.width);
	}

	/**
	 * Diese Funktion wird nach dem Schließen des Filterdialogs mit "Ok" aufgerufen und ermöglicht
	 * es von dieser Klasse abgeleiteten Klassen, die neuen Filtereinstellungen zu speichern.
	 */
	protected void saveFilterDialogSettings() {}

	/**
	 * Diese Funktion wird nach dem Laden der Statistikdaten aufgerufen und ermöglicht
	 * es von dieser Klasse abgeleiteten Klassen, die Filtereinstellungen für sichtbare/versteckte Elemente zu laden.
	 */
	protected void loadFilterDialogSettings() {}

	/**
	 * Diese Funktion wird aufgerufen, wenn die Einstellungen, welche Report-Einträge ausgewählt sein sollen, abgefragt werden sollen.
	 * @return	Einstellungen, welche Report-Einträge selektiert sein sollen
	 */
	protected String getReportSettings() {return "";}

	/**
	 * Diese Funktion wird aufgerufen, wenn die Einstellungen, welche Report-Einträge ausgewählt sind, gespeichert werden sollen.
	 * @param settings	Neue Einstellungen, welche Report-Einträge selektiert sind
	 */
	protected void setReportSettings(String settings) {}

	/**
	 * Liefert die Einstellungen, welche Elemente versteckt sind und welche nicht als String zurück, der im Setup gespeichert werden kann
	 * @return	String mit Angaben darüber, welche Elemente versteckt werden sollen und welche nicht
	 */
	protected final String getHiddenIDsSetupString() {
		return tree.getHiddenIDsSetupString();
	}

	/**
	 * Stellt die Einstellungen, welche Elemente versteckt werden sollen und welche nicht, aus einem String wieder her.
	 * @param setup	String mit Angaben darüber enthält, welche Elemente versteckt werden sollen und welche nicht
	 */
	protected final void setHiddenIDsFromSetupString(String setup) {
		tree.setHiddenIDsFromSetupString(setup);
	}

	/**
	 * Selektiert den Report-Knoten
	 * @return	Gibt <code>true</code> zurück, wenn ein Report-Knoten vorhanden ist.
	 */
	public final boolean selectReportNode() {
		return tree.selectReportNode();
	}

	/**
	 * Fügt einen Listener hinzu, der benachrichtigt wird, wenn eine Datei auf der Komponente abgelegt wird
	 * @param fileDropListener	Zu benachrichtigender Listener (der Dateiname ist über die <code>getActionCommand()</code>-Methode des übergebenen <code>ActionEvent</code>-Objekts abrufbar)
	 */
	public void addFileDropListener(final ActionListener fileDropListener) {
		if (fileDropListeners.indexOf(fileDropListener)<0) fileDropListeners.add(fileDropListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der im Falle einer auf dieser Komponente abgelegten Datei zu benachrichtigenden Listener
	 * @param fileDropListener	In Zukunft nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener erfolgreich aus der Liste entfernt werden konnte
	 */
	public boolean removeFileDropListener(final ActionListener fileDropListener) {
		return fileDropListeners.remove(fileDropListener);
	}

	/**
	 * Muss aufgerufen werden, wenn eine Datei per Drag&amp;drop auf dem Statistik-Panel
	 * abgelegt wird. Es werden dann die registrierten {@link #fileDropListeners} benachrichtigt.
	 * @param data	Drag&amp;drop-Daten
	 */
	private void dropFile(final FileDropperData data) {
		final ActionEvent event=FileDropperData.getActionEvent(data);
		for (ActionListener listener: fileDropListeners) listener.actionPerformed(event);
	}

	/**
	 * Registriert eine Komponente, die bei Drag&amp;drop-Operationen
	 * {@link #dropFile(FileDropperData)} aufrufen soll.
	 * @param component	Zu registrierende Komponente
	 * @see #dropFile(FileDropperData)
	 */
	private void registerComponentForFileDrop(final Component component) {
		new FileDropper(component,e->{
			final FileDropperData dropper=(FileDropperData)e.getSource();
			dropFile(dropper);
		});
	}

	/**
	 * Registriert eine Komponente, die bei Drag&amp;drop-Operationen
	 * auf sich und auf ihre Kindkomponenten
	 * {@link #dropFile(FileDropperData)} aufrufen soll.
	 * @param component	Zu registrierende Komponente
	 * @see #dropFile(FileDropperData)
	 */
	private void registerComponentAndChildsForFileDrop(final Component component) {
		if (component==null) return;
		registerComponentForFileDrop(component);

		if (component instanceof Container) {
			final Container container=(Container)component;
			for (int i=0;i<container.getComponentCount();i++) {
				registerComponentAndChildsForFileDrop(container.getComponent(i));
			}
		}
	}
}
