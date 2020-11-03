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
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import language.Language;
import mathtools.Table;
import simulator.Statistics;
import systemtools.statistics.StatisticNode;
import systemtools.statistics.StatisticTree;
import systemtools.statistics.StatisticTreeCellRenderer;
import systemtools.statistics.StatisticViewer;
import systemtools.statistics.StatisticViewerReport;
import systemtools.statistics.StatisticViewerSpecialBase;
import systemtools.statistics.StatisticsBasePanel;
import tools.SetupData;
import ui.HelpLink;
import ui.editor.BaseEditDialog;
import ui.help.Help;
import ui.images.Images;
import ui.statistic.core.viewers.StatisticViewerFastAccess;
import ui.statistic.core.viewers.StatisticViewerSpecialHTMLText;

/**
 * Dieses Panel enthält die Baumstruktur, die auf der linken Seite
 * einer Statistikansicht dargestellt wird.
 * @author Alexander Herzog
 * @see StatisticBasePanel
 */
public class StatisticTreePanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2558288206599547578L;

	/** Hilfe-Callback für den Aufruf der Hilfeseite für die Statistik */
	private final Runnable helpModal;
	/** Hilfe-Objekt, welches alle Hilfe-Links enthält */
	private final HelpLink helpLink;

	/** Callback zum Starten einer Simulation */
	private final Runnable startSimulation;
	/** Callback zum Laden von Statistikdaten */
	private final Runnable loadStatistics;

	/** Wurzel des Statistikdaten-Baumes */
	private StatisticNode statisticData;
	/** Statistik-Objekt, welches alle Daten für den Schnellfilter enthält (kann <code>null</code> sein, dann wird keine xslt-Transform-Option angeboten) */
	private Statistics statistic;
	/** Gibt an, für wie viele Ansichten Report-Nodes usw. erzeugt werden sollen */
	private int numberOfViewers;

	/**
	 * Ausgeblendete Einträge
	 * @see #getHiddenIDs()
	 * @see #setHiddenIDs(String[])
	 * @see #setHiddenIDsFromSetupString(String)
	 */
	private final Set<String> hiddenIDs=new LinkedHashSet <String>();

	/** Schaltfläche zum Filtern der Baumstruktur */
	private final JButton filter;
	/** Schaltfläche zum Anzeigen des Reportgenerators */
	private final JButton report;

	/** Baumstruktur */
	private final StatisticTree tree;
	/** Beumeintrag für die Report-Erstellung */
	private DefaultMutableTreeNode reportNode;

	/** Anzuzeigender Eintrag, wenn kein Bauminhalt vorhanden ist */
	private final DefaultMutableTreeNode noDataSelected=new DefaultMutableTreeNode("("+Language.tr("Statistic.NoDataSelected")+")");

	/**
	 * Konstruktor der Klasse <code>StatisticPanel</code>
	 * @param title	Titel, der über der Baumstruktur angezeigt wird
	 * @param icon	Icon, das neben dem Titel über der Baumstruktur angezeigt wird (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param commandLineCommand	Kommandozeilenbefehl, über den einzelne Statistikergebnisse abgerufen werden können (zur Anzeige eines Kontextmenüs, welche den jeweiligen Befehl benennt; wird hier <code>null</code> übergeben, so erhält die Baumansicht kein Kontextmenü)
	 * @param commandLineDataFileName	Beispieldateiname für Kommandozeilenbefehl, über den einzelne Statistikergebnisse abgerufen werden können (kann <code>null</code> sein)
	 * @param filterTree	Option zur Filterung der Daten im Statistikbaum anzeigen
	 * @param helpModal	Hilfe-Callback für den Aufruf der Hilfeseite für die Statistik
	 * @param helpLink	Hilfe-Objekt, welches alle Hilfe-Links enthält
	 * @param startSimulation	Callback zum Starten einer Simulation
	 * @param loadStatistics	Callback zum Laden von Statistikdaten
	 */
	public StatisticTreePanel(final String title, final URL icon, final String commandLineCommand, final String commandLineDataFileName, boolean filterTree, Runnable helpModal, final HelpLink helpLink, final Runnable startSimulation, final Runnable loadStatistics) {
		super(new BorderLayout());

		this.helpModal=helpModal;
		this.helpLink=helpLink;
		this.startSimulation=startSimulation;
		this.loadStatistics=loadStatistics;

		JPanel leftTopPanel;
		JToolBar buttonPanelLeft;
		JPanel infoPanel;

		/* Infotext oben links */
		add(leftTopPanel=new JPanel(new BorderLayout()),BorderLayout.NORTH);
		leftTopPanel.add(infoPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		infoPanel.setBackground(Color.GRAY);
		infoPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		if (icon!=null) infoPanel.add(new JLabel(new ImageIcon(icon)));
		JLabel infoLabel;
		infoPanel.add(infoLabel=new JLabel(title));
		Font font=infoLabel.getFont();
		infoLabel.setFont(new java.awt.Font(font.getFontName(),java.awt.Font.BOLD,font.getSize()+3));
		infoLabel.setForeground(Color.WHITE);

		/* Buttons oben links */
		leftTopPanel.add(buttonPanelLeft=new JToolBar(),BorderLayout.CENTER);
		buttonPanelLeft.setFloatable(false);

		if (filterTree) {
			buttonPanelLeft.add(filter=new JButton(Language.tr("Statistic.Filter.Title")));
			filter.addActionListener(new ButtonListener());
			filter.setToolTipText(Language.tr("Statistic.Filter.Tooltip"));
			filter.setIcon(Images.STATISTICS_FILTER.getIcon());
		} else {
			filter=null;
		}

		buttonPanelLeft.add(report=new JButton(Language.tr("Statistic.GenerateReport.Title")));
		report.addActionListener(new ButtonListener());
		report.setToolTipText(Language.tr("Statistic.GenerateReport.Tooltip"));
		report.setIcon(Images.STATISTICS_REPORT.getIcon());

		/* tree */
		JScrollPane sp=new JScrollPane(tree=new StatisticTree(commandLineCommand,commandLineDataFileName){
			private static final long serialVersionUID = 5013035517806204341L;
			@Override
			protected void nodeSelected(StatisticNode node, DefaultMutableTreeNode treeNode) {updateDataPanel(node,treeNode);}
		});
		sp.setBorder(BorderFactory.createLineBorder(Color.GRAY,1));

		add(sp,BorderLayout.CENTER);

		tree.setBackground(new Color(0xFF,0xFF,0xF8));
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
	 * Aktualisiert die Datenansicht
	 * @param node	Gewählter Statistikknoten
	 * @param treeNode	Zugehöriger Baumknoten
	 */
	private void updateDataPanel(StatisticNode node, DefaultMutableTreeNode treeNode) {
		if (node==null || node.viewer.length==0) {
			report.setVisible(reportNode!=null);
		} else {
			StatisticViewer viewer=node.viewer[0];
			if (viewer instanceof StatisticViewerSpecialBase) {
				report.setVisible(false);
			} else {
				report.setVisible(reportNode!=null);
			}
		}

		StatisticViewer[] viewer=new StatisticViewer[numberOfViewers];
		String info;
		if (node==null) {
			if (reportNode==null) {
				for (int i=0;i<viewer.length;i++) viewer[i]=new StatisticViewerSpecialHTMLText(StatisticViewerSpecialHTMLText.ViewerCategory.VIEWER_NODATA,startSimulation,loadStatistics);
			} else {
				for (int i=0;i<viewer.length;i++) viewer[i]=new StatisticViewerSpecialHTMLText(StatisticViewerSpecialHTMLText.ViewerCategory.VIEWER_CATEGORY);
			}
			info=Language.tr("Statistic.Information");
		} else {
			if (node.viewer.length==0) {
				for (int i=0;i<viewer.length;i++) viewer[i]=new StatisticViewerSpecialHTMLText(StatisticViewerSpecialHTMLText.ViewerCategory.VIEWER_SUBCATEGORY);
				info=Language.tr("Statistic.Information");
			} else {
				viewer=node.viewer;
				TreePath path=tree.getSelectionPath();
				String s="";
				while (path!=null) {
					DefaultMutableTreeNode n=(DefaultMutableTreeNode)path.getLastPathComponent();
					if (n.getUserObject()==null) break;
					if (!s.isEmpty()) s=" - "+s;
					s=((StatisticNode)(n.getUserObject())).name+s;
					path=path.getParentPath();
				}
				info=s;
			}
		}

		URL icon=null;
		if (treeNode!=null) {
			icon=new StatisticTreeCellRenderer().getIconURL(treeNode);
		}
		updateViewer(node,viewer,info,icon);
	}

	/**
	 * Wird aufgerufen, wenn ein neuer Viewer zur Ansicht ausgewählt wurde
	 * @param node	Zugehöriger Statistikknoten
	 * @param viewer	Viewer zum Anzeigen (bei Statistikvergleichen mehrere, sonst nur einer)
	 * @param info	Name des Viewers
	 * @param icon	Icon zur Anzeige links neben dem Namen
	 */
	protected void updateViewer(final StatisticNode node, final StatisticViewer[] viewer, final String info, final URL icon) {}

	/**
	 * Selektiert den "Zusammenfassung erstellen"-Eintrag in der Baumstruktur.
	 * @return	Gibt an, ob ein entsprechender Eintrag existiert und selektiert werden konnte.
	 */
	public final boolean selectReportNode() {
		return tree.selectNode(reportNode);
	}

	/**
	 * Gibt an, welche Statistikdaten angezeigt werden sollen.<br>
	 * Die Daten werden in Form einer Baumstruktur geordnet. Übergeben wird die
	 * Wurzel des Baumes, welche selbst keine Daten enthält.
	 * @param statisticData	Wurzel des Statistikdaten-Baumes
	 * @param statistic	Statistik-Objekt, welches alle Daten für den Schnellfilter enthält (kann <code>null</code> sein, dann wird keine xslt-Transform-Option angeboten)
	 * @param numberOfViewers	Gibt an, für wie viele Ansichten Report-Nodes usw. erzeugt werden sollen
	 * @see StatisticNode
	 */
	public final void setStatisticData(StatisticNode statisticData, Statistics statistic, int numberOfViewers) {
		this.statisticData=statisticData;
		this.statistic=statistic;
		this.numberOfViewers=numberOfViewers;

		if (statistic!=null) {
			if (settingHiddenIDs) {
				settingHiddenIDsDone=true;
			} else {
				settingHiddenIDsDone=false;
				loadFilterSettings();
				if (settingHiddenIDsDone) {settingHiddenIDsDone=false; return;}
			}
		}

		/* Bisher selektiertes Element speichern */
		int[] sel;
		if (tree.getRowCount()>1 && statisticData!=null && statisticData.getChildCount()>0) sel=tree.getSelectionRows(); else sel=null;

		/* Elemente laden */
		DefaultMutableTreeNode rootNode=new DefaultMutableTreeNode();
		if (statistic!=null) rootNode.add(new DefaultMutableTreeNode(new StatisticNode(Language.tr("Statistic.FastAccess"),new StatisticViewerFastAccess(this,statistic,helpLink.pageStatisticFastAccess,helpLink.pageStatisticFastAccessModal))));
		if (statisticData!=null) addToTree(statisticData,rootNode);
		reportNode=null;
		if (statisticData!=null && statisticData.getChildCount()>0) {
			List<StatisticViewer> list=new ArrayList<StatisticViewer>();
			for (int i=0;i<numberOfViewers;i++) list.add(new StatisticViewerReport(statisticData,null,"",i,()->Help.topic(StatisticTreePanel.this,"Statististik")){
				@Override
				protected String getSelectSettings() {return getReportSelectSettings();}
				@Override
				protected void setSelectSettings(String settings) {setReportSelectSettings(settings);}
				@Override
				protected boolean loadImagesInline() {return SetupData.getSetup().imagesInline;}
				@Override
				protected void saveImagesInline(final boolean imagesInline) {final SetupData setup=SetupData.getSetup(); setup.imagesInline=imagesInline; setup.saveSetup();}
			});
			rootNode.add(reportNode=new DefaultMutableTreeNode(new StatisticNode(StatisticsBasePanel.viewersReport,list)));
		} else {
			rootNode.add(noDataSelected);
		}
		((DefaultTreeModel)(tree.getModel())).setRoot(rootNode);

		/* Elemente ein- und ausklappen */
		int row=0; while (row<tree.getRowCount()) {
			DefaultMutableTreeNode node=(DefaultMutableTreeNode)(tree.getPathForRow(row).getLastPathComponent());
			if (!node.isLeaf() && !((StatisticNode)(node.getUserObject())).collapseChildren) tree.expandRow(row);
			row++;
		}

		/* Selektion wiederherstellen */
		if (sel!=null && sel.length>0) tree.setSelectionRow(sel[0]); else {
			if (statistic!=null && tree.getRowCount()>1) tree.setSelectionRow(1); else tree.setSelectionRow(0);
		}

		/* Filterbutton bei leerem Baum ausschalten */
		if (filter!=null) filter.setVisible(statisticData!=null && statisticData.getChildCount()>0);
	}

	/**
	 * Listet die IDs aller Elemente in der Reihenfolge, in der sie zum ersten Mal auftreten auf.
	 * @return	Array aus IDs
	 */
	protected final String[] getIDs() {
		if (statisticData==null) return new String[0];
		Set<String> ids=new LinkedHashSet<String>();
		statisticData.getIDs(ids);
		return ids.toArray(new String[0]);
	}

	/**
	 * Listet die IDs aller momentan ausgeblendeten Elemente auf.
	 * @return	Array aus den IDs der ausgeblendeten Elemente. (Ist kein Element ausgeblendet, so wird ein leeres Array zurückgegeben; nicht <code>null</code>.)
	 */
	protected final String[] getHiddenIDs() {
		return hiddenIDs.toArray(new String[0]);
	}

	/**
	 * Läuft gerade eine Verarbeitung der Einstellungen?
	 * @see #setHiddenIDs(String[])
	 */
	private boolean settingHiddenIDs=false;

	/**
	 * Wurde gerade eine entsprechende Verarbeitung abgeschlossen?
	 * @see #setHiddenIDs(String[])
	 * @see #setStatisticData(StatisticNode, Statistics, int)
	 */
	private boolean settingHiddenIDsDone=false;

	/**
	 * Stellt ein, welche Elemente ausgeblendet werden sollen.
	 * @param ids	Array der IDs, die ausgeblendet werden sollen. (<code>null</code> hat dieselbe Wirkung wie ein leeres Array.)
	 */
	protected final void setHiddenIDs(String[] ids) {
		hiddenIDs.clear();
		if (ids!=null) hiddenIDs.addAll(Arrays.asList(ids));
		settingHiddenIDs=true;
		try {setStatisticData(statisticData,statistic,numberOfViewers);} finally {settingHiddenIDs=false;}
	}

	/**
	 * Liefert die Einstellungen, welche Elemente versteckt sind und welche nicht als String zurück, der im Setup gespeichert werden kann
	 * @return	String mit Angaben darüber, welche Elemente versteckt werden sollen und welche nicht
	 */
	protected final String getHiddenIDsSetupString() {
		String[] ids=getIDs();
		StringBuilder setup=new StringBuilder();
		for (int i=0;i<ids.length;i++) if (hiddenIDs.contains(ids[i])) setup.append('-'); else setup.append('X');
		return setup.toString();
	}

	/**
	 * Stellt die Einstellungen, welche Elemente versteckt werden sollen und welche nicht, aus einem String wieder her.
	 * @param setup	String mit Angaben darüber enthält, welche Elemente versteckt werden sollen und welche nicht
	 */
	protected final void setHiddenIDsFromSetupString(String setup) {
		String[] ids=getIDs();

		if (setup==null) setup="";
		while (setup.length()<ids.length) setup+="X";

		hiddenIDs.clear();
		for (int i=0;i<ids.length;i++) if (setup.charAt(i)=='-') hiddenIDs.add(ids[i]);
		settingHiddenIDs=true;
		try {setStatisticData(statisticData,statistic,numberOfViewers);} finally {settingHiddenIDs=false;}
	}

	/**
	 * Fügt einen Eintrag zu der Baumstruktur hinzu.
	 * @param sNode	Statistikknoten
	 * @param tNode	Zugehöriger Baumknoten
	 */
	private final void addToTree(StatisticNode sNode, DefaultMutableTreeNode tNode) {
		for (int i=0;i<sNode.getChildCount();i++) {
			StatisticNode sChild=sNode.getChild(i);
			if (!sChild.id.isEmpty() && hiddenIDs.contains(sChild.id)) continue;
			DefaultMutableTreeNode tChild=new DefaultMutableTreeNode(sChild);
			tNode.add(tChild);
			if (sChild.getChildCount()>0) addToTree(sChild,tChild);
		}
	}

	/**
	 * Zeigt den Dialog zur Filterung der Baumstruktur an.
	 */
	private final void showFilterDialog() {
		if (statisticData==null) return;

		Container c=getParent(); while (c!=null) {if (c instanceof Window) break; c=c.getParent();}

		StatisticFilterDialog dialog=new StatisticFilterDialog((Window)c,Language.tr("Statistic.Filter.DialogTitle"),getIDs(),getHiddenIDs(),helpModal);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
		setHiddenIDs(dialog.getHiddenIDs());
		saveFilterSettings();
	}

	/**
	 * Ruft den HTML-Report-Generator auf und speichert den Report in der angegebenen Form in der angegebenen Datei.
	 * @param outputFile	Dateiname, in der der HTML-Report gespeichert werden soll.
	 * @param inline	Gibt an, ob die Grafiken direkt in die HTML-Datei eingebettet werden sollen.
	 * @param exportAllItems	Exportiert entweder alle Einträge (<code>true</code>) oder nur die im Viewer selektrierten (<code>false</code>).
	 * @return	Gibt an, ob der Report erfolgreich erstellt werden konnte.
	 */
	public boolean runReportGeneratorHTML(File outputFile, boolean inline, boolean exportAllItems) {
		if (reportNode==null || reportNode.getUserObject()==null || !(reportNode.getUserObject() instanceof StatisticNode)) return false;
		StatisticNode node=(StatisticNode)(reportNode.getUserObject());
		if (node.viewer.length==0 || node.viewer[0]==null || !(node.viewer[0] instanceof StatisticViewerReport)) return false;

		StatisticViewerReport reportViewer=(StatisticViewerReport)(node.viewer[0]);
		return reportViewer.save(this,outputFile,inline?StatisticViewerReport.FileFormat.FORMAT_HTML_INLINE:StatisticViewerReport.FileFormat.FORMAT_HTML,exportAllItems);
	}

	/**
	 * Ruft den DOCX-Report-Generator auf und speichert den Report in der angegebenen Form in der angegebenen Datei.
	 * @param outputFile	Dateiname, in der der DOCX-Report gespeichert werden soll.
	 * @param exportAllItems	Exportiert entweder alle Einträge (<code>true</code>) oder nur die im Viewer selektrierten (<code>false</code>).
	 * @return	Gibt an, ob der Report erfolgreich erstellt werden konnte.
	 */
	public boolean runReportGeneratorDOCX(File outputFile, boolean exportAllItems) {
		if (reportNode==null || reportNode.getUserObject()==null || !(reportNode.getUserObject() instanceof StatisticNode)) return false;
		StatisticNode node=(StatisticNode)(reportNode.getUserObject());
		if (node.viewer.length==0 || node.viewer[0]==null || !(node.viewer[0] instanceof StatisticViewerReport)) return false;

		StatisticViewerReport reportViewer=(StatisticViewerReport)(node.viewer[0]);
		return reportViewer.save(this,outputFile,StatisticViewerReport.FileFormat.FORMAT_DOCX,exportAllItems);
	}

	/**
	 * Ruft den PDF-Report-Generator auf und speichert den Report in der angegebenen Form in der angegebenen Datei.
	 * @param outputFile	Dateiname, in der der PDF-Report gespeichert werden soll.
	 * @param exportAllItems	Exportiert entweder alle Einträge (<code>true</code>) oder nur die im Viewer selektierten (<code>false</code>).
	 * @return	Gibt an, ob der Report erfolgreich erstellt werden konnte.
	 */
	public boolean runReportGeneratorPDF(File outputFile, boolean exportAllItems) {
		if (reportNode==null || reportNode.getUserObject()==null || !(reportNode.getUserObject() instanceof StatisticNode)) return false;
		StatisticNode node=(StatisticNode)(reportNode.getUserObject());
		if (node.viewer.length==0 || node.viewer[0]==null || !(node.viewer[0] instanceof StatisticViewerReport)) return false;

		StatisticViewerReport reportViewer=(StatisticViewerReport)(node.viewer[0]);
		return reportViewer.save(this,outputFile,StatisticViewerReport.FileFormat.FORMAT_PDF,exportAllItems);
	}

	/**
	 * Listet die Viewer auf einer Ebene auf
	 * @param parentName	Name des übergeordneten Eintrags
	 * @param parent	Baumeintrag des übergeordneten Eintrags
	 * @param viewers	Zu ergänzende Liste der Viewer auf der aktuellen Ebene
	 * @param types	Zu ergänzende Liste der Typen der Viewer auf der aktuellen Ebene
	 * @param names	Zu ergänzende Liste der Namen der Viewer auf der aktuellen Ebene
	 * @see #getReportList(File)
	 */
	private void getViewersAndNames(String parentName, DefaultMutableTreeNode parent, List<StatisticViewer> viewers, List<String> types, List<String> names) {
		if (parent==null) return;
		int count=parent.getChildCount();
		String s=parentName; if (!s.isEmpty()) s+=" - ";
		for (int i=0;i<count;i++) {
			if (!(parent.getChildAt(i) instanceof DefaultMutableTreeNode)) continue;
			DefaultMutableTreeNode node=(DefaultMutableTreeNode)(parent.getChildAt(i));
			if (!(node.getUserObject() instanceof StatisticNode)) continue;
			StatisticNode stat=(StatisticNode)(node.getUserObject());
			if (stat.viewer.length>0) {
				switch (stat.viewer[0].getType()) {
				case TYPE_TEXT : viewers.add(stat.viewer[0]); types.add(Language.tr("Statistic.Type.Text")); names.add(s+stat.name); break;
				case TYPE_TABLE : viewers.add(stat.viewer[0]); types.add(Language.tr("Statistic.Type.Table")); names.add(s+stat.name); break;
				case TYPE_IMAGE : viewers.add(stat.viewer[0]); types.add(Language.tr("Statistic.Type.Graphics")); names.add(s+stat.name); break;
				default: /* Andere Dinge nehmen wir nicht in die Liste auf. */ break;
				}
			}

			getViewersAndNames(s+stat.name,node,viewers,types,names);
		}
	}

	/**
	 * Speichert eine Liste aller Einträge im Statistikbaum als Datei
	 * @param output	Dateiname, in der die Liste gespeichert werden soll.
	 * @return	Gibt an, ob die Liste erfolgreich gespeichert werden konnte.
	 */
	public boolean getReportList(final File output) {
		if (!(tree.getModel().getRoot() instanceof DefaultMutableTreeNode)) return false;
		final DefaultMutableTreeNode node=(DefaultMutableTreeNode)(tree.getModel().getRoot());

		final List<StatisticViewer> viewers=new ArrayList<StatisticViewer>();
		final List<String> types=new ArrayList<String>();
		final List<String> names=new ArrayList<String>();
		getViewersAndNames("",node,viewers,types,names);

		final StringBuilder reportNames=new StringBuilder();
		for (int i=0;i<names.size();i++) reportNames.append(names.get(i)+" ("+types.get(i)+")\n");
		return Table.saveTextToFile(reportNames.toString(),output);
	}

	/**
	 * Speichert ein einzelnes Dokument aus dem Statisitikbaum als Datei
	 * @param output	Dateiname, in dem das Dokument gespeichert werden soll.
	 * @param entry	Gibt den Namen des Dokuments im Statistikbaum an.
	 * @return	Gibt an, ob das Dokument erfolgreich gespeichert werden konnte.
	 */
	public boolean getReportListEntry(File output, String entry) {
		if (!(tree.getModel().getRoot() instanceof DefaultMutableTreeNode)) return false;
		DefaultMutableTreeNode node=(DefaultMutableTreeNode)(tree.getModel().getRoot());

		List<StatisticViewer> viewers=new ArrayList<StatisticViewer>();
		List<String> types=new ArrayList<String>();
		List<String> names=new ArrayList<String>();
		getViewersAndNames("",node,viewers,types,names);

		int index=-1;
		for (int i=0;i<names.size();i++) {
			String s=names.get(i)+" ("+types.get(i)+")";
			if (s.equalsIgnoreCase(entry)) {index=i; break;}
		}
		if (index<0) return false;

		return viewers.get(index).save(this,output);
	}

	/**
	 * Diese Funktion wird nach dem Schließen des Filterdialogs mit "Ok" aufgerufen und ermöglicht
	 * es von dieser Klasse abgeleiteten Klassen, die neuen Filtereinstellungen zu speichern.
	 */
	protected void saveFilterSettings() {}

	/**
	 * Diese Funktion wird nach dem Laden der Statistikdaten aufgerufen und ermöglicht
	 * es von dieser Klasse abgeleiteten Klassen, die Filtereinstellungen für sichtbare/versteckte Elemente zu laden.
	 */
	protected void loadFilterSettings() {}

	/**
	 * Diese Funktion wird aufgerufen, wenn die Einstellungen, welche Report-Einträge ausgewählt sein sollen, abgefragt werden sollen.
	 * @return	Einstellungen, welche Report-Einträge selektiert sein sollen
	 */
	protected String getReportSelectSettings() {return "";}

	/**
	 * Diese Funktion wird aufgerufen, wenn die Einstellungen, welche Report-Einträge ausgewählt sind, gespeichert werden sollen.
	 * @param settings	Neue Einstellungen, welche Report-Einträge selektiert sind
	 */
	protected void setReportSelectSettings(String settings) {}

	/**
	 * Reagiert auf Klick auf die Schaltflächen in der Symbolleiste
	 * @see StatisticTreePanel#report
	 * @see StatisticTreePanel#filter
	 */
	private final class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==report) {selectReportNode(); return;}
			if (e.getSource()==filter) {showFilterDialog();	return;}
		}
	}
}
