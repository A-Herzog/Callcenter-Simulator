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
package ui.specialpanels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.DistributionTools;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import net.calc.StartAnySimulator;
import simulator.CallcenterSimulatorInterface;
import simulator.Statistics;
import systemtools.MsgBox;
import tools.SetupData;
import tools.TrayNotify;
import ui.AutoSave;
import ui.HelpLink;
import ui.VersionConst;
import ui.dialogs.AutoSaveSetupDialog;
import ui.editor.BaseEditDialog;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.model.CallcenterRunModel;
import ui.statistic.core.viewers.StatisticViewerFastAccessDialog;
import xml.XMLTools;

/**
 * Panel, welches Dialogelemente zur Batch-Verarbeitung kapselt
 * @author Alexander Herzog
 * @version 1.0
 */
public class BatchPanel extends JWorkPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2238696032289072168L;

	/** Hilfe-Link */
	private final HelpLink helpLink;
	/** Übergeordnetes Fenster */
	private final Window owner;
	/** Registerreiter */
	private final JTabbedPane tabs;

	/* Dialogseite "Verzeichnis abarbeiten" */

	/** Eingabefeld "Verzeichnis" */
	private final JTextField folderField;
	/** Schaltfläche zur Auswahl eines Verzeichnisses für {@link #folderField} */
	private final JButton folderButton;

	/* Dialogseite "Parameterreihe erstellen" */

	/** Eingabefeld "Ausgabeverzeichnis" */
	private final JTextField xmlFolderField;
	/** Schaltfläche zur Auswahl eines Ausgabeverzeichnis für {@link #xmlFolderField} */
	private final JButton xmlFolderButton;
	/** Eingabefeld "XML-Element" */
	private final JTextField xmlField;
	/** Schaltfläche zur Auswahl eines XML-Elements für {@link #xmlField} */
	private final JButton xmlButton;
	/** Auswahlbox "Zu veränderndes Element" */
	private final JComboBox<String> xmlType;
	/** Eingabefeld "von" */
	private final JTextField xmlFrom;
	/** Eingabefeld "bis" */
	private final JTextField xmlTo;
	/** Eingabefeld "Schrittweite" */
	private final JTextField xmlStepSize;

	/** Ausgabefeld für Status-Informationen */
	private final JTextArea statusField;
	/** Text in der Statusleiste */
	private final JLabel statusLabel;
	/** Fortschrittsanzeige in der Statusleiste */
	private final JProgressBar statusProgress;

	/** Für die Batch-Verarbeitung zu verwendendes Callcenter-Modell */
	private final CallcenterModel model;
	/** Eingabedateien im Modus "Verzeichnis abarbeiten" */
	private List<File> inFiles;
	/** XML-Element im Modus "Parameterreihe erstellen" */
	private String inXMLKey;
	/** Typ des XML-Elements im Modus "Parameterreihe erstellen" */
	private int inXMLType;
	/** Werte für {@link #inXMLKey} */
	private List<Double> inValues;
	/** Ausgabe-Dateien für die verschiedenen Werte {@link #inValues} */
	private List<File> outFiles;
	/** Timer um regelmäßig den Fortschritt der Simulation in der GUI anzeigen zu können */
	private Timer timer;
	/** Startzeitpunkt der ersten Simulation */
	private long startTime;
	/** Zählt die Anzahl an Simulationen (gezählt wird beim Start) */
	private int simCount;
	/** Zählt die Aufrufe von {@link SimTimerTask} */
	private int count;

	/** Simulator, der die konkreten Simulationen ausführt */
	private CallcenterSimulatorInterface simulator;

	/** Reagiert auf Datei-Drag&amp;drop-Operationen auf {@link #folderField} */
	private final FileDropper drop1;
	/** Reagiert auf Datei-Drag&amp;drop-Operationen auf {@link #xmlField} */
	private final FileDropper drop2;

	/**
	 * Erzeugt eine Textzeile
	 * @param parent	Übergeordnetes Element
	 * @param text	Auszugebender Text
	 * @return	Panel das die Textzeile enthält (bereits in das übergeordnetes Element eingefügt)
	 */
	private JPanel createDialogLine(final JComponent parent, final String text) {
		JPanel content=new JPanel(new BorderLayout());
		JPanel textPanel=new JPanel(new FlowLayout());
		textPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
		textPanel.add(new JLabel(text));
		content.add(textPanel,BorderLayout.WEST);
		JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		line.add(content);
		parent.add(line);
		return content;
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param model	Für die Batch-Verarbeitung zu verwendendes Callcenter-Modell
	 * @param doneNotify	Callback wird aufgerufen, wenn das Batch-Panel geschlossen werden soll
	 * @param helpLink	Hilfe-Link
	 */
	public BatchPanel(final Window owner, final CallcenterModel model, final Runnable doneNotify, final HelpLink helpLink) {
		super(doneNotify,helpLink.pageBatch);
		this.helpLink=helpLink;
		this.owner=owner;
		this.model=model;

		JPanel main,p,p2,p3,tab;

		/* Main area */
		add(main=new JPanel(new BorderLayout()),BorderLayout.CENTER);

		main.add(p=new JPanel(),BorderLayout.NORTH);
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));

		/* Tabs */
		p.add(tabs=new JTabbedPane());

		/* Verzeichnis abarbeiten */
		tabs.addTab(Language.tr("Batch.Tabs.Folder"),tab=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2=createDialogLine(tab,Language.tr("Batch.Folder.Label"));
		p2.add(folderField=new JTextField(55),BorderLayout.CENTER);
		drop1=new FileDropper(folderField,new ButtonListener());
		p2.add(folderButton=new JButton(Language.tr("Batch.Folder.Button")),BorderLayout.EAST);
		folderButton.addActionListener(new ButtonListener());
		folderButton.setToolTipText(Language.tr("Batch.Folder.Info"));
		folderButton.setIcon(Images.GENERAL_SELECT_FOLDER.getIcon());

		/* Parameterreihe erstellen */
		tabs.addTab(Language.tr("Batch.Tabs.Parameter"),tab=new JPanel());
		tab.setLayout(new BoxLayout(tab,BoxLayout.Y_AXIS));
		p2=createDialogLine(tab,Language.tr("Batch.Parameter.OutpurFolder.Label"));
		p2.add(xmlFolderField=new JTextField(55),BorderLayout.CENTER);
		drop2=new FileDropper(xmlFolderField,new ButtonListener());
		p2.add(xmlFolderButton=new JButton(Language.tr("Batch.Folder.Button")),BorderLayout.EAST);
		xmlFolderButton.addActionListener(new ButtonListener());
		xmlFolderButton.setToolTipText(Language.tr("Batch.Parameter.OutpurFolder.Info"));
		xmlFolderButton.setIcon(Images.GENERAL_SELECT_FOLDER.getIcon());
		p2=createDialogLine(tab,Language.tr("Batch.Parameter.XMLTag.Label"));
		p2.add(xmlField=new JTextField(55),BorderLayout.CENTER);
		xmlField.setEditable(false);
		p2.add(xmlButton=new JButton(Language.tr("Batch.Parameter.XMLTag.Button")),BorderLayout.EAST);
		xmlButton.addActionListener(new ButtonListener());
		xmlButton.setToolTipText(Language.tr("Batch.Parameter.XMLTag.Info"));
		xmlButton.setIcon(Images.GENERAL_SELECT_XML.getIcon());
		p2=createDialogLine(tab,Language.tr("Batch.Parameter.ChangeType.Label")+":");
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.CENTER);
		p3.add(xmlType=new JComboBox<>());
		xmlType.addItem(Language.tr("Batch.Parameter.ChangeType.Number"));
		xmlType.addItem(Language.tr("Batch.Parameter.ChangeType.Mean"));
		xmlType.addItem(Language.tr("Batch.Parameter.ChangeType.StdDev"));
		xmlType.addItem(Language.tr("Batch.Parameter.ChangeType.DistributionParameter1"));
		xmlType.addItem(Language.tr("Batch.Parameter.ChangeType.DistributionParameter2"));
		xmlType.addItem(Language.tr("Batch.Parameter.ChangeType.DistributionParameter3"));
		xmlType.addItem(Language.tr("Batch.Parameter.ChangeType.DistributionParameter4"));
		xmlType.setSelectedIndex(0);
		p3.add(new JLabel(Language.tr("Batch.Parameter.ChangeType.From")));
		p3.add(xmlFrom=new JTextField(5));
		p3.add(new JLabel(Language.tr("Batch.Parameter.ChangeType.To")));
		p3.add(xmlTo=new JTextField(5));
		p3.add(new JLabel(Language.tr("Batch.Parameter.ChangeType.Step")));
		p3.add(xmlStepSize=new JTextField(5));
		xmlFrom.setText("0");
		xmlTo.setText("10");
		xmlStepSize.setText(NumberTools.formatNumber(0.1d));

		/* Icons für Tabs */
		tabs.setIconAt(0,Images.SIMULATION_BATCH_FOLDER.getIcon());
		tabs.setIconAt(1,Images.SIMULATION_BATCH_PARAMETERSERIES.getIcon());

		/* Statusfeld */
		main.add(p=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		p.add(new JScrollPane(statusField=new JTextArea(10,80)));
		statusField.setEditable(false);

		main.add(p=new JPanel(),BorderLayout.SOUTH);
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(statusLabel=new JLabel(""));

		p.add(p2=new JPanel(new BorderLayout()));
		p2.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		p2.add(statusProgress=new JProgressBar(),BorderLayout.CENTER);
		statusProgress.setStringPainted(true);

		/* Im Setup gespeicherte Daten wiederherstellen */
		SetupData setup=SetupData.getSetup();
		folderField.setText(setup.batchFolder);
		xmlFolderField.setText(setup.batchXMLFolder);
		if (testXMLFieldForModel(setup.batchXMLElement)) xmlField.setText(setup.batchXMLElement);
		xmlType.setSelectedIndex(Math.max(0,Math.min(xmlType.getItemCount()-1,setup.batchXMLElementType)));
		if (!setup.batchXMLElementFrom.trim().isEmpty()) xmlFrom.setText(setup.batchXMLElementFrom);
		if (!setup.batchXMLElementTo.trim().isEmpty()) xmlTo.setText(setup.batchXMLElementTo);
		if (!setup.batchXMLElementStepSize.trim().isEmpty()) xmlStepSize.setText(setup.batchXMLElementStepSize);

		/* Bottom line */
		addFooter(Language.tr("Batch.Simulation.Start"),Images.SIMULATION_BATCH.getIcon(),Language.tr("Batch.Simulation.Abort"));
		addFooterButton(Language.tr("Batch.AdditionalOutputSetting"));
	}

	/**
	 * Prüft, ob ein bestimmter XML-Pfad in dem Modell existiert
	 * @param xmlKey	XML-Pfad
	 * @return	Liefert <code>true</code>, wenn der Pfad auf eine änderbare Modelleigenschaft verweist
	 */
	private boolean testXMLFieldForModel(final String xmlKey) {
		Document xmlDoc=model.saveToXMLDocument();
		if (xmlDoc==null) return false;

		try (Scanner selectors=new Scanner(xmlKey)) {
			selectors.useDelimiter("->");
			if (!selectors.hasNext()) return false;
			String s=changeElement(selectors,xmlDoc.getDocumentElement(),new ArrayList<String>(),0);
			if (s!=null) return false;
		}
		return true;
	}

	/**
	 * Zeigt einen Dialog zur Auswahl eines Ein- oder Ausgabeverzeichnisses an.
	 * @return	Liefert im Erfolgsfall den Namen des Verzeichnisses sonst <code>null</code>
	 */
	private final String selectFolder() {
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(Language.tr("Batch.Folder.Button"));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		return file.toString();
	}

	/**
	 * Zeigt einen Dialog zur Auswahl eines XML-Elements an.
	 * @see #xmlButton
	 */
	private final void selectXML() {
		Document xmlDoc=model.saveToXMLDocument();
		if (xmlDoc==null) return;
		StatisticViewerFastAccessDialog dialog=new StatisticViewerFastAccessDialog(owner,xmlDoc,helpLink.pageBatchModal,true);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
		xmlField.setText(dialog.getXMLSelector());
	}

	/**
	 * Handelt es sich bei einer Datei um eine Modell-Datei?
	 * @param file	Zu prüfende Datei
	 * @return	Liefert <code>true</code>, wenn es sich um eine Modell-Datei handelt
	 */
	private boolean isModelFile(final File file) {
		if (file==null || !file.exists()) return false;

		XMLTools xml=new XMLTools(file);
		Element root=xml.load();
		if (root==null) return false;
		for (String s: CallcenterModel.XMLBaseElement) if (root.getNodeName().equalsIgnoreCase(s)) return true;
		return false;
	}

	/**
	 * Gibt eine Zeile in dem Status-Ausgaben-Bereich aus.
	 * @param line	Auszugebende Informationszeile
	 */
	private void addStatusLine(final String line) {
		if (statusField.getText().isEmpty()) statusField.setText(line); else statusField.setText(statusField.getText()+"\n"+line);
		statusField.setCaretPosition(statusField.getText().length());
	}

	/**
	 * Ändert den Wert in {@link #inXMLKey}
	 * @param oldValue	Bisherige Verteilung oder bisheriger Wert
	 * @param newValue	Einzutragender Wert
	 * @return	Neue Verteilung oder neuer Wert
	 */
	private String changeElementValue(String oldValue, double newValue) {
		if (inXMLType==0) {
			return NumberTools.formatNumber(newValue);
		} else {
			AbstractRealDistribution dist=DistributionTools.distributionFromString(oldValue,86400);
			if (dist==null) return null;
			AbstractRealDistribution newDist=null;
			switch (inXMLType) {
			case 1: newDist=DistributionTools.setMean(dist,newValue); break;
			case 2:	newDist=DistributionTools.setStandardDeviation(dist,newValue); break;
			case 3: newDist=DistributionTools.setParameter(dist,1,newValue); break;
			case 4: newDist=DistributionTools.setParameter(dist,2,newValue); break;
			case 5: newDist=DistributionTools.setParameter(dist,3,newValue); break;
			case 6: newDist=DistributionTools.setParameter(dist,4,newValue); break;
			}
			if (newDist==null) return null;
			return DistributionTools.distributionToString(newDist);
		}
	}

	/**
	 * Ändert den Wert eines XML-Objekt
	 * @param selectors	Zusammenstellung der Pfad-Komponenten
	 * @param parent	Übergeordnetes XML-Element
	 * @param parentTags	Namen der übergeordneten Elemente
	 * @param newValue	Neuer Wert für das XML-Objekt
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	private String changeElement(Scanner selectors, Element parent, List<String> parentTags, double newValue) {
		/* Selektor dekodieren */
		String sel=selectors.next();
		String tag=sel, attr="", attrValue="";
		int index=sel.indexOf('[');
		if (index>=0) {
			if (!sel.endsWith("]")) return String.format(Language.tr("Batch.Parameter.XMLTag.InvalidSelector"),sel);
			attr=sel.substring(index+1,sel.length()-1).trim();
			tag=sel.substring(0,index).trim();
			if (attr.isEmpty()) return String.format(Language.tr("Batch.Parameter.XMLTag.InvalidSelector"),sel);
			index=attr.indexOf('=');
			if (index>=0) {
				attrValue=attr.substring(index+1).trim();
				attr=attr.substring(0,index).trim();
				if (attrValue.length()>2 && (attrValue.length() > 0 && attrValue.charAt(0) == '"') && attrValue.endsWith("\""))
					attrValue=attrValue.substring(1,attrValue.length()-1);
			}
		}

		/* Attribut aus Parent zurückgeben */
		if (!selectors.hasNext() && tag.isEmpty()) {
			List<String> path=new ArrayList<>(parentTags);
			path.add(attr);
			String s=changeElementValue(parent.getAttribute(attr),newValue);
			if (s==null) return String.format(Language.tr("Batch.Parameter.XMLTag.InvalidValue"),parent.getAttribute(attr));
			parent.setAttribute(attr,s);
			return null;
		}

		/* Kindelement suchen */
		Element searchResult=null;
		NodeList list=parent.getChildNodes();
		for (int i=0; i<list.getLength();i++) {
			if (!(list.item(i) instanceof Element)) continue;
			Element node=(Element)list.item(i);
			if (node.getNodeName().equalsIgnoreCase(tag)) {
				if (attr.isEmpty()) {searchResult=node; break;}
				if (!selectors.hasNext() && attrValue.isEmpty()) {searchResult=node; break;}
				if (node.getAttribute(attr).equalsIgnoreCase(attrValue)) {searchResult=node; break;}
				if (node.getAttribute(attr).isEmpty() && attrValue.equals("\"\"")) {searchResult=node; break;}
			}
		}
		if (searchResult==null) return String.format(Language.tr("Batch.Parameter.XMLTag.NoElementFound"),sel);

		/* Elementinhalt zurückgeben */
		if (!selectors.hasNext()) {
			List<String> path=new ArrayList<>(parentTags);
			path.add(tag);
			if (attr.isEmpty() || !attrValue.isEmpty()) {
				String s=changeElementValue(searchResult.getTextContent(),newValue);
				if (s==null) return String.format(Language.tr("Batch.Parameter.XMLTag.InvalidValue"),searchResult.getTextContent());
				searchResult.setTextContent(s);
				return null;
			} else {
				path.add(attr);
				String s=changeElementValue(searchResult.getAttribute(attr),newValue);
				if (s==null) return String.format(Language.tr("Batch.Parameter.XMLTag.InvalidValue"),searchResult.getAttribute(attr));
				searchResult.setAttribute(attr,s);
				return null;
			}
		}

		/* Suche fortsetzen */
		List<String> tags=new ArrayList<>(parentTags);
		tags.add(tag);
		return changeElement(selectors,searchResult,tags,newValue);
	}

	/**
	 * Ändert einen XML-Eintrag in einem Callcenter-Modell
	 * @param model	Bisheriges Callcenter-Modell
	 * @param xmlKey	Zu änderndes XML-Element
	 * @param xmlType	Typ des zu ändernden XML-Elements
	 * @param value	Neuer Wert
	 * @return	Liefert im Erfolgsfall ein neues Callcenter-Modell, sonst eine Fehlermeldung
	 */
	private Object changeModel(CallcenterModel model, String xmlKey, int xmlType, double value) {
		Document xmlDoc=model.saveToXMLDocument();
		if (xmlDoc==null) return Language.tr("Batch.Parameter.XMLTag.NotAbleToSave");

		try (Scanner selectors=new Scanner(xmlKey)) {
			selectors.useDelimiter("->");
			if (!selectors.hasNext()) return null;
			String s=changeElement(selectors,xmlDoc.getDocumentElement(),new ArrayList<String>(),value);
			if (s!=null) return s;
		}

		CallcenterModel editModel=new CallcenterModel();
		if (editModel.loadFromXML(xmlDoc.getDocumentElement())!=null) return Language.tr("Batch.Parameter.XMLTag.NotAbleToLoad");
		editModel.description=String.format(Language.tr("Batch.Parameter.XMLTag.Changed"),xmlKey,NumberTools.formatNumber(value))+"\n\n"+editModel.description;
		return editModel;
	}

	/**
	 * Startet die nächste Simulation
	 * @return	Liefert <code>true</code>, wenn eine weitere Simulation gestartet werden konnte, und <code>false</code>, wenn die Simulation aller Modelle abgeschlossen ist
	 */
	private boolean initNextSimulation() {
		simulator=null;

		String s;
		while (simCount<outFiles.size()) {
			if (inFiles!=null) {
				addStatusLine(String.format(Language.tr("Batch.Simulation.BatchStatus1"),inFiles.get(simCount).getName()));
				statusLabel.setText(String.format(Language.tr("Batch.Simulation.BatchStatus2"),inFiles.get(simCount).getName(),simCount+1,outFiles.size()));
			} else {
				addStatusLine(String.format(Language.tr("Batch.Simulation.ParameterStatus1"),NumberTools.formatNumber(inValues.get(simCount))));
				statusLabel.setText(String.format(Language.tr("Batch.Simulation.ParameterStatus2"),NumberTools.formatNumber(inValues.get(simCount)),simCount+1,outFiles.size()));
			}

			/* Modell laden */
			CallcenterModel editModel=null;
			s=null;
			if (inFiles!=null) {
				editModel=new CallcenterModel();
				s=editModel.loadFromFile(inFiles.get(simCount));
			} else {
				Object obj=changeModel(model,inXMLKey,inXMLType,inValues.get(simCount));
				if (obj instanceof String) s=(String)obj; else editModel=(CallcenterModel)obj;
			}
			simCount++;
			if (s!=null) {addStatusLine(Language.tr("Dialog.Title.Error").toUpperCase()+":\n  "+Language.tr("Batch.LoadError")+":\n  "+s); continue;}
			if (editModel==null) continue;

			/* Modell vorbereiten */
			if (VersionConst.isNewerVersion(editModel.version)) addStatusLine(Language.tr("Dialog.Title.Warning").toUpperCase()+":\n"+Language.tr("Batch.LoadWarningNewerVersion"));
			CallcenterRunModel runModel=new CallcenterRunModel(editModel);
			s=runModel.checkAndInit(false,true,SetupData.getSetup().strictCheck);
			if (s!=null) {
				addStatusLine(Language.tr("Dialog.Title.Error").toUpperCase()+":\n  "+Language.tr("Batch.PreparationError")+":\n  "+s);
				continue;
			}

			/* Simulation starten */
			count=0;
			StartAnySimulator startAnySimulator=new StartAnySimulator(editModel);
			s=startAnySimulator.check(); if (s!=null) {addStatusLine(Language.tr("Dialog.Title.Error").toUpperCase()+":\n  "+Language.tr("Batch.PreparationError")+":\n  "+s); return false;}
			simulator=startAnySimulator.run();
			simulator.start(false);
			statusProgress.setMaximum((int)simulator.getSimDaysCount());
			break;
		}

		return simulator!=null;
	}

	/**
	 * Schließt die aktuelle Simulation ab.
	 */
	private void doneSimulation() {
		String errorMessage=simulator.finalizeRun();
		/* Vom Server gesandte Meldungen ausgeben */
		if (errorMessage!=null) MsgBox.error(this,Language.tr("Batch.Simulation.Error.Title"),errorMessage);

		Statistics statistics=simulator.collectStatistic();

		if (statistics!=null) {
			SetupData setup=SetupData.getSetup();
			File file=outFiles.get(simCount-1);
			if (file!=null) {
				if (!statistics.saveToFile(file)) addStatusLine(Language.tr("Dialog.Title.Error").toUpperCase()+":\n  "+Language.tr("Batch.Simulation.Error.SaveStatistic"));
			}
			if (setup.batchSaveFilter) {
				AutoSave autoSave=new AutoSave(statistics);
				String s=autoSave.saveFilter(setup.batchSaveFilterScript,setup.batchSaveFilterOutput);
				if (s!=null) addStatusLine(s);
			}
		} else {
			addStatusLine(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("Batch.Simulation.Canceled.Short"));
			requestClose();
		}
	}

	/**
	 * Wird nach dem Abschluss aller Simulationen
	 * (erfolgreich oder durch Abbruch) aufgerufen.
	 */
	private void everythingDone() {
		if (cancelWork)
			addStatusLine(String.format(Language.tr("Batch.Simulation.Canceled"),""+simCount));
		else {
			addStatusLine(String.format((simCount==1)?Language.tr("Batch.Simulation.FinishedSingle"):Language.tr("Batch.Simulation.FinishedMultiple"),""+simCount,NumberTools.formatLong((System.currentTimeMillis()-startTime)/1000)));
			new TrayNotify(this,Language.tr("Batch.Simulation.FinishedNofity.Title"),Language.tr("Batch.Simulation.FinishedNofity.Info"));
		}

		System.gc();

		/* GUI umschalten */
		setWorkMode(false);
		folderField.setEnabled(true);
		folderButton.setEnabled(true);
		xmlFolderField.setEnabled(true);
		xmlFolderButton.setEnabled(true);
		xmlField.setEnabled(true);
		xmlButton.setEnabled(true);
		xmlType.setEnabled(true);
		xmlFrom.setEnabled(true);
		xmlTo.setEnabled(true);
		xmlStepSize.setEnabled(true);
		statusLabel.setText("");
		statusProgress.setValue(0);
	}

	/**
	 * Prüft in Regelmäßigen Abständen, ob die
	 * laufende Simulation abgeschlossen wurde
	 * und die nächste Simulation gestartet
	 * werden kann.
	 * @see BatchPanel#initNextSimulation()
	 * @see BatchPanel#doneSimulation()
	 * @see BatchPanel#everythingDone()
	 */
	private class SimTimerTask extends TimerTask {
		/**
		 * Konstruktor der Klasse
		 */
		public SimTimerTask() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void run() {
			if (cancelWork) {timer.cancel(); simulator.cancel(); everythingDone(); return;}

			if (simulator.isRunning()) {
				count++; if (count%8==0) statusProgress.setValue((int)simulator.getSimDayCount());
				return;
			}

			doneSimulation();
			if (cancelWork) return;
			if (!initNextSimulation()) {timer.cancel(); everythingDone(); return;}
		}
	}

	/**
	 * Startet eine Mehrfach-Simulation im Modus
	 * "Verzeichnis abarbeiten".
	 * @return	Liefert <code>true</code>, wenn die Mehrfach-Simulation erfolgreich gestartet werden konnte
	 * @see #run()
	 */
	private final boolean runFolder() {
		/* Vorbereitung des Batch-Runs */
		if (folderField.getText().isEmpty()) {addStatusLine(Language.tr("Dialog.Title.Error").toUpperCase()+":\n  "+Language.tr("Batch.Folder.ErrorNoInputFolder")); return false;}
		File batchFolder=new File(folderField.getText());
		if (!batchFolder.isDirectory()) {addStatusLine(Language.tr("Dialog.Title.Error").toUpperCase()+":\n  "+String.format(Language.tr("Batch.Folder.ErrorInputFolderDoesNotExist"),batchFolder)); return false;}

		/* Iteration über die Dateien in dem angegebenen Verzeichnis */
		inFiles=new ArrayList<>();
		outFiles=new ArrayList<>();
		File[] list=batchFolder.listFiles();
		if (list!=null) for (int i=0;i<list.length;i++) if (list[i].isFile()) {
			File inFile=list[i];

			/* Ausgabedatei bestimmen */
			if (!isModelFile(inFile)) continue;
			int j=inFile.getName().lastIndexOf('.');
			String baseName=inFile.getName();
			if (j>=0) baseName=inFile.getName().substring(0,j);
			File statisticsFile=new File(inFile.getParent(),baseName+Language.tr("Batch.OutputFileNameAddOn")+".xml");
			if (statisticsFile.exists()) {
				addStatusLine(Language.tr("Dialog.Title.Warning").toUpperCase()+":\n"+String.format(Language.tr("Batch.Folder.WarningOutputFileExists"),statisticsFile.getName(),inFile.getName()));
				continue;
			}

			/* In Liste der Modelle speichern */
			inFiles.add(inFile);
			outFiles.add(statisticsFile);
		}
		if (inFiles.size()==0) {
			addStatusLine(String.format(Language.tr("Batch.Folder.NoModelsInFolder"),batchFolder.toString()));
			return false;
		}
		return true;
	}

	/**
	 * Startet eine Mehrfach-Simulation im Modus
	 * "Parameterreihe erstellen".
	 * @return	Liefert <code>true</code>, wenn die Mehrfach-Simulation erfolgreich gestartet werden konnte
	 * @see #run()
	 */
	private final boolean runParameter() {
		/* Vorbereitung des Batch-Runs */
		File batchFolder=null;
		if (xmlFolderField.getText().isEmpty()) {
			SetupData setup=SetupData.getSetup();
			if (!setup.batchSaveFilter || setup.batchSaveFilterOutput.isEmpty()) {
				addStatusLine(Language.tr("Dialog.Title.Error").toUpperCase()+":\n  "+Language.tr("Batch.Parameter.ErrorNoOutputFolder")); return false;
			}
		} else {
			batchFolder=new File(xmlFolderField.getText());
		}
		if (batchFolder!=null && !batchFolder.isDirectory()) {addStatusLine(Language.tr("Dialog.Title.Error").toUpperCase()+":\n  "+String.format(Language.tr("Batch.Folder.ErrorInputFolderDoesNotExist"),batchFolder)); return false;}
		if (xmlField.getText().isEmpty()) {addStatusLine(Language.tr("Dialog.Title.Error").toUpperCase()+":\n  "+Language.tr("Batch.Parameter.ErrorNoXMLTagToBeChanged")); return false;}
		Double D;
		D=NumberTools.getDouble(xmlFrom,true);
		if (D==null) {addStatusLine(Language.tr("Dialog.Title.Error").toUpperCase()+":\n  "+Language.tr("Batch.Parameter.ErrorInvalidFromValue")); return false;}
		double valueStart=D;
		D=NumberTools.getDouble(xmlTo,true);
		if (D==null) {addStatusLine(Language.tr("Dialog.Title.Error").toUpperCase()+":\n  "+Language.tr("Batch.Parameter.ErrorInvalidToValue")); return false;}
		double valueEnd=D;
		D=NumberTools.getDouble(xmlStepSize,true);
		if (D==null) {addStatusLine(Language.tr("Dialog.Title.Error").toUpperCase()+":\n  "+Language.tr("Batch.Parameter.ErrorInvalidStepWidth")); return false;}
		double valueStepSize=Math.abs(D);
		if (valueEnd<valueStart) valueStepSize=-valueStepSize;
		inXMLType=xmlType.getSelectedIndex();

		inXMLKey=xmlField.getText();
		inValues=new ArrayList<>();
		outFiles=new ArrayList<>();
		double value=valueStart;

		/* Liste an zu simulierenden Modellen zusammenstellen */
		int count=0;
		while ((valueStart<=valueEnd && value<=valueEnd) || (valueStart>valueEnd && value>=valueEnd)) {
			inValues.add(value);
			File outFile;
			count++;
			if (batchFolder==null) outFile=null; else {
				while (true) {
					outFile=new File(batchFolder,"Statistik_"+count+".xml");
					if (!outFile.exists()) break;
					count++;
				}
			}
			outFiles.add(outFile);
			if (valueStart<=valueEnd) value+=valueStepSize; else value-=valueStepSize;
		}
		return true;
	}

	@Override
	protected final void run() {
		statusField.setText("");
		cancelWork=false;

		boolean b=false;
		switch (tabs.getSelectedIndex()) {
		case 0:	b=runFolder(); break;
		case 1: b=runParameter(); break;
		}
		if (!b) return;

		/* GUI umschalten */
		setWorkMode(true);
		folderField.setEnabled(false);
		folderButton.setEnabled(false);
		xmlFolderField.setEnabled(false);
		xmlFolderButton.setEnabled(false);
		xmlField.setEnabled(false);
		xmlButton.setEnabled(false);
		xmlType.setEnabled(false);
		xmlFrom.setEnabled(false);
		xmlTo.setEnabled(false);
		xmlStepSize.setEnabled(false);

		/* Start der Simulation */
		startTime=System.currentTimeMillis();
		simCount=0;
		if (!initNextSimulation()) {
			everythingDone();
		} else {
			timer=new Timer();
			timer.schedule(new SimTimerTask(),50,50);
		}
	}

	@Override
	protected void done() {
		super.done();

		/* Setup speichern */
		SetupData setup=SetupData.getSetup();
		setup.batchFolder=folderField.getText();
		setup.batchXMLFolder=xmlFolderField.getText();
		setup.batchXMLElement=xmlField.getText();
		setup.batchXMLElementType=xmlType.getSelectedIndex();
		setup.batchXMLElementFrom=xmlFrom.getText();
		setup.batchXMLElementTo=xmlTo.getText();
		setup.batchXMLElementStepSize=xmlStepSize.getText();
	}

	@Override
	protected void userButtonClick(int index, JButton button) {
		if (index!=0) return;
		AutoSaveSetupDialog dialog=new AutoSaveSetupDialog(owner,Language.tr("Batch.AdditionalOutputSetting"),true,helpLink.pageBatchModal);
		dialog.setVisible(true);
	}

	/**
	 * Reagiert auf Klicks auf die verschiedenen Schaltflächen
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
			if (e.getSource()==folderButton) {String s=selectFolder(); if (s!=null) folderField.setText(s); return;}
			if (e.getSource()==xmlFolderButton) {String s=selectFolder(); if (s!=null) xmlFolderField.setText(s); return;}
			if (e.getSource()==xmlButton) {selectXML(); return;}
			if (e.getSource() instanceof FileDropperData) {
				final FileDropperData data=(FileDropperData)e.getSource();
				final File file=data.getFile();
				if (data.getFileDropper()==drop1) {
					if (file.isDirectory()) {
						folderField.setText(file.toString());
						data.dragDropConsumed();
					}
					return;
				}
				if (data.getFileDropper()==drop2) {
					if (file.isDirectory()) {
						xmlFolderField.setText(file.toString());
						data.dragDropConsumed();
					}
					return;
				}
			}
		}
	}
}
