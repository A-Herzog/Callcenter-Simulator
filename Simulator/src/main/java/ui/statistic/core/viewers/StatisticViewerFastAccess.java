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
package ui.statistic.core.viewers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.distribution.swing.CommonVariables;
import simulator.Statistics;
import systemtools.MsgBox;
import systemtools.statistics.StatisticViewerSpecialBase;
import systemtools.statistics.StatisticsBasePanel;
import tools.SetupData;
import ui.editor.BaseEditDialog;
import ui.images.Images;
import ui.statistic.core.filter.DataFilter;
import ui.statistic.core.filter.DataFilterBase;

/**
 * Diese Klasse kapselt einen xslt-Übersetzer, der innerhalb von <code>StatisticPanel</code> verwendet wird.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerFastAccess extends StatisticViewerSpecialBase {
	/** Übergeordnetes Element */
	private final Component owner;
	/** Hilfe für Schnellzugriff-Seite */
	private final Runnable helpFastAccess;
	/** Hilfe für Schnellzugriff-Dialog */
	private final Runnable helpFastAccessModal;
	/** System zur Filterung der Daten */
	private DataFilter dataFilter=null;
	/** Statistik-Objekt, dem die Daten entnommen werden sollen */
	private final Statistics statistic;
	/** Eingabebereich für das Filter-Skript */
	private JTextArea filter;
	/** Ausgabebereich für die Ergebnisse */
	private JTextArea results;
	/** Symbolleiste */
	private JToolBar toolbar;
	/** Auswahlbox zur Auswahl des aktuellen Skriptes */
	private JComboBox<ComboBoxItem> toolbarFilter;
	/** Schaltfläche "Neu" */
	private JButton toolbarNew;
	/** Schaltfläche "Laden" */
	private JButton toolbarLoad;
	/** Schaltfläche "Speichern" */
	private JButton toolbarSave;
	/** Schaltfläche "Tools" */
	private JButton toolbarTools;
	/** Schaltfläche "Hilfe" */
	private JButton toolbarHelp;
	/** Popupmenü zur Anzeige beim Anklicken von {@link #toolbarTools} */
	private JPopupMenu toolbarToolsMenu;
	/** Menüpunkt zur Auswahl eines XML-Elements */
	private JMenuItem toolbarSelect;
	/** Menü zur Anzeige der Vorlagen */
	private JMenu toolbarTemplate;
	/** Menüpunkte für die Skript-Vorlagen */
	private JMenuItem[] toolbarTemplateItem;
	/** Zuletzt verwendetes Filter-Skript */
	private String lastFilterText;

	/**
	 * Konstruktor der Klasse <code>StatisticViewerFastAccess</code>
	 * @param owner	Übergeordnetes Element
	 * @param statistic	Statistik-Objekt, dem die Daten entnommen werden sollen
	 * @param helpFastAccess	Hilfe für Schnellzugriff-Seite
	 * @param helpFastAccessModal	Hilfe für Schnellzugriff-Dialog
	 */
	public StatisticViewerFastAccess(Component owner, Statistics statistic, Runnable helpFastAccess, Runnable helpFastAccessModal) {
		this.owner=owner;
		this.statistic=statistic;
		this.helpFastAccess=helpFastAccess;
		this.helpFastAccessModal=helpFastAccessModal;
	}

	@Override
	public ViewerType getType() {
		return ViewerType.TYPE_SPECIAL;
	}

	/** Namen für Skript-Vorlagen */
	private static final String[] templateNames={
			Language.tr("Statistic.FastAccess.Template.DisplayAccessibility"),
			Language.tr("Statistic.FastAccess.Template.DisplayServiceLevel"),
			Language.tr("Statistic.FastAccess.Template.DisplayAverageWaitingTime")
	};

	/** Skript-Vorlagen */
	private static final String[] templateContent={
			"Title "+Language.tr("Statistic.FastAccess.Template.DisplayAccessibility")+"\n"+
					"Format percent\n"+
					"Print "+Language.trPrimary("XML.Statistic.Clients")+"["+Language.trPrimary("XML.Statistic.GeneralAttributes.Name")+"=\"\"]->"+Language.trPrimary("XML.Statistic.Clients.Summary")+"["+Language.trPrimary("XML.Statistic.Clients.Summary.CallsAccessibility")+"]\n"+
					"Text \"\\t\"",
					"Title "+Language.tr("Statistic.FastAccess.Template.DisplayServiceLevel")+"\n"+
							"Format percent\n"+
							"Print "+Language.trPrimary("XML.Statistic.Clients")+"["+Language.trPrimary("XML.Statistic.GeneralAttributes.Name")+"=\"\"]->"+Language.trPrimary("XML.Statistic.Clients.Summary")+"["+Language.trPrimary("XML.Statistic.Clients.Summary.CallsServiceLevel")+"]\n"+
							"Text \"\\t\"",
							"Title "+Language.tr("Statistic.FastAccess.Template.DisplayAverageWaitingTime")+"\n"+
									"Format Time\n"+
									"Print "+Language.trPrimary("XML.Statistic.Clients")+"["+Language.trPrimary("XML.Statistic.GeneralAttributes.Name")+"=\"\"]->"+Language.trPrimary("XML.Statistic.Clients.Summary")+"["+Language.trPrimary("XML.Statistic.Clients.Summary.CallsAverageWaitingTime")+"]\n"+
									"Text \"\\t\""
	};

	@Override
	public Container getViewer(boolean needReInit) {
		if (dataFilter==null || needReInit) dataFilter=new DataFilter(statistic.saveToXMLDocument());
		SetupData setup=SetupData.getSetup();

		JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		/* Ausgabe */
		split.add(new JScrollPane(results=new JTextArea()));
		results.setEditable(false);

		/* Filter */
		JTabbedPane tabs=new JTabbedPane();
		split.add(tabs);
		tabs.setPreferredSize(new Dimension(1,250));
		JPanel p2=new JPanel(new BorderLayout());
		tabs.add(p2,Language.tr("Statistic.FastAccess.Filter"));
		p2.add(toolbar=new JToolBar(),BorderLayout.NORTH);
		p2.add(new JScrollPane(filter=new JTextArea()),BorderLayout.CENTER);
		filter.addKeyListener(new FilterKeyListener());

		tabs.setIconAt(0,Images.STATISTICS_FILTER.getIcon());

		/* Toolbar */
		toolbar.setFloatable(false);

		toolbar.add(toolbarFilter=new JComboBox<>());
		for (int i=0;i<setup.filter.length;i++)  {
			String s=DataFilter.getTitleFromCommand(setup.filter[i]);
			if (!s.isEmpty()) s=": "+s;
			toolbarFilter.addItem(new ComboBoxItem(Language.tr("Statistic.FastAccess.Filter")+" "+(i+1)+s));
		}
		toolbarFilter.setSelectedIndex(Math.min(toolbarFilter.getItemCount()-1,Math.max(0,setup.lastFilterIndex)));
		toolbarFilter.addActionListener(new ToolBarActionListener());
		Dimension d=toolbarFilter.getMaximumSize(); d.width=250;
		toolbarFilter.setMaximumSize(d);

		toolbar.addSeparator();

		toolbar.add(toolbarNew=new JButton(Language.tr("Dialog.Button.New")));
		toolbarNew.addActionListener(new ToolBarActionListener());
		toolbarNew.setToolTipText(Language.tr("Statistic.FastAccess.New.Tooltip"));
		toolbarNew.setIcon(Images.STATISTICS_FILTER_NEW.getIcon());
		toolbar.add(toolbarLoad=new JButton(Language.tr("Dialog.Button.Load")));
		toolbarLoad.addActionListener(new ToolBarActionListener());
		toolbarLoad.setToolTipText(Language.tr("Statistic.FastAccess.Load.Tooltip"));
		toolbarLoad.setIcon(Images.STATISTICS_FILTER_LOAD.getIcon());
		toolbar.add(toolbarSave=new JButton(Language.tr("Dialog.Button.Save")));
		toolbarSave.addActionListener(new ToolBarActionListener());
		toolbarSave.setToolTipText(Language.tr("Statistic.FastAccess.Save.Tooltip"));
		toolbarSave.setIcon(Images.GENERAL_SAVE.getIcon());

		toolbar.add(toolbarTools=new JButton(Language.tr("Dialog.Button.Tools")));
		toolbarTools.addActionListener(new ToolBarActionListener());
		toolbarTools.setIcon(Images.GENERAL_SETUP.getIcon());

		toolbarToolsMenu=new JPopupMenu();
		toolbarToolsMenu.add(toolbarSelect=new JMenuItem(Language.tr("Statistic.FastAccess.SelectXMLTag")));
		toolbarSelect.addActionListener(new ToolBarActionListener());
		toolbarSelect.setToolTipText(Language.tr("Statistic.FastAccess.SelectXMLTag.Tooltip"));
		toolbarSelect.setIcon(Images.GENERAL_SELECT_XML.getIcon());
		toolbarToolsMenu.add(toolbarTemplate=new JMenu(Language.tr("Statistic.FastAccess.Template")));
		toolbarTemplateItem=new JMenuItem[templateNames.length];
		for (int i=0;i<templateNames.length;i++) {
			toolbarTemplate.add(toolbarTemplateItem[i]=new JMenuItem(templateNames[i]));
			toolbarTemplateItem[i].addActionListener(new ToolBarActionListener());
		}
		if (helpFastAccess!=null) {
			toolbar.addSeparator();
			toolbar.add(toolbarHelp=new JButton(Language.tr("Dialog.Button.Help")));
			toolbarHelp.addActionListener(new ToolBarActionListener());
			toolbarHelp.setToolTipText(Language.tr("Statistic.FastAccess.Help.Tooltip"));
			toolbarHelp.setIcon(Images.HELP.getIcon());
		}

		/* Filtertext laden */
		filter.setText(SetupData.getSetup().filter[toolbarFilter.getSelectedIndex()]);
		filterUpdated();
		lastFilterText="";

		split.setResizeWeight(1);
		split.setDividerLocation(split.getSize().height-200);
		return split;
	}

	@Override
	public Transferable getTransferable() {
		return new StringSelection(results.getText());
	}

	@Override
	public void copyToClipboard(Clipboard clipboard) {
		filterUpdated();
		clipboard.setContents(new StringSelection(results.getText()),null);
	}

	@Override
	public boolean print() {
		return false;
	}

	@Override
	public void save(Component parentFrame) {
		filterUpdated();
		saveTextToFile(parentFrame,results.getText());
	}

	@Override
	public void search(Component owner) {
	}

	@Override
	public boolean save(Component owner, File file) {
		filterUpdated();
		return DataFilterBase.saveText(results.getText(),file,false);
	}

	@Override
	public String[] ownSettingsName() {return null;}

	/**
	 * Speichert ein Skript in einer Datei
	 * @param parentFrame	Übergeordnetes Fenster (zur Ausrichtung von Dialogen)
	 * @param text	Skripttext
	 * @return	Liefert <code>true</code>, wenn der Text gespeichert werden konnte
	 */
	private boolean saveTextToFile(final Component parentFrame, final String text) {
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(Language.tr("FileType.Save.Text"));
		FileFilter txt=new FileNameExtensionFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
		fc.addChoosableFileFilter(txt);
		fc.setFileFilter(txt);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(parentFrame)!=JFileChooser.APPROVE_OPTION) return false;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==txt) file=new File(file.getAbsoluteFile()+".txt");
		}

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(parentFrame,file)) return false;
		}

		return DataFilterBase.saveText(text,file,false);
	}

	/**
	 * Versucht ein Skript aus einer Datei zu laden.
	 * @param parentFrame	Übergeordnetes Fenster (zur Ausrichtung von Dialogen)
	 * @return	Liefert im Erfolgsfall den geladenen Text, sonst <code>null</code>
	 */
	private String loadTextFromFile(final Container parentFrame) {
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(Language.tr("FileType.Load.Text"));
		FileFilter txt=new FileNameExtensionFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
		fc.addChoosableFileFilter(txt);
		fc.setFileFilter(txt);

		if (fc.showOpenDialog(parentFrame)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0 && fc.getFileFilter()==txt) file=new File(file.getAbsoluteFile()+".txt");

		return DataFilterBase.loadText(file);
	}

	@Override
	public boolean getCanDo(CanDoAction canDoType) {
		switch (canDoType) {
		case CAN_DO_COPY : return true;
		case CAN_DO_PRINT : return false;
		case CAN_DO_SAVE : return true;
		case CAN_DO_UNZOOM: return false;
		default: return false;
		}
	}

	/**
	 * Läuft gerade ein Aktualisierungsvorgang?
	 * @see #filterUpdated()
	 */
	private boolean filterIsUpdating=false;

	/**
	 * Aktualisiert die Ausgabe basierend auf dem geänderten Filterskirpt.
	 */
	private void filterUpdated() {
		if (dataFilter==null) dataFilter=new DataFilter(statistic.saveToXMLDocument());
		filterIsUpdating=true;
		try {
			dataFilter.run(filter.getText(),false);
			results.setText(dataFilter.getResults());
			SetupData setup=SetupData.getSetup();
			setup.filter[toolbarFilter.getSelectedIndex()]=filter.getText();
			setup.saveSetupWithWarning(null);

			String s=dataFilter.getTitle();
			if (!s.isEmpty()) s=": "+s;
			s=Language.tr("Statistic.FastAccess.Filter")+" "+(toolbarFilter.getSelectedIndex()+1)+s;
			ComboBoxItem c;
			c=(ComboBoxItem)(toolbarFilter.getSelectedItem());
			c.setValue(s);
			toolbarFilter.repaint();
		} finally {
			filterIsUpdating=false;
		}
	}

	/**
	 * Prüft, ob das aktuelle Skript verworfen werden darf (und fragt ggf. den Nutzer dazu).
	 * @param parentFrame	Übergeordnetes Fenster (zur Ausrichtung von Dialogen)
	 * @return	Liefert <code>true</code>, wenn das Skript verworfen werden darf
	 */
	private boolean discardFilterOk(final Container parentFrame) {
		if (filter.getText().equals(lastFilterText)) return true;

		int i=MsgBox.confirmSave(owner,Language.tr("Statistic.FastAccess.DiscardConfirm.Title"),Language.tr("Statistic.FastAccess.DiscardConfirm.Info"));
		if (i==JOptionPane.YES_OPTION) {
			if (!saveTextToFile(parentFrame,filter.getText())) return false;
			return true;
		}
		if (i==JOptionPane.NO_OPTION) return true;
		return false;
	}

	/**
	 * Reagiert auf Tastendrücke in {@link StatisticViewerFastAccess#filter}
	 * @see StatisticViewerFastAccess#filter
	 */
	private class FilterKeyListener extends KeyAdapter {
		@Override
		public void keyReleased(KeyEvent e) {filterUpdated();}
	}

	/**
	 * Reagiert auf Klicks auf die Schaltflächen in der Symbolleiste
	 */
	private class ToolBarActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Container c=null;
			if (e.getSource() instanceof Component) {
				Object o=((Component) (e.getSource())).getParent();
				while (true) {
					if (o==null) break;
					if (!(o instanceof Component)) break;
					o=((Component) (e.getSource())).getParent();
					if (o instanceof Container) {c=(Container)o; break;}
				}
			}

			if (e.getSource()==toolbarFilter) {
				if (filterIsUpdating) return;
				SetupData setup=SetupData.getSetup();
				filter.setText(setup.filter[toolbarFilter.getSelectedIndex()]);
				filterUpdated();
				lastFilterText=filter.getText();
				setup.lastFilterIndex=toolbarFilter.getSelectedIndex();
				setup.saveSetup();
				return;
			}

			if (e.getSource()==toolbarNew) {
				if (!discardFilterOk(c)) return;
				if (!filter.getText().equals(lastFilterText)) lastFilterText=filter.getText();
				filter.setText(""); filterUpdated();
				return;
			}
			if (e.getSource()==toolbarLoad) {
				if (!discardFilterOk(c)) return;
				String s=loadTextFromFile(c);
				if (s!=null) {filter.setText(s); filterUpdated(); lastFilterText=s;}
				return;
			}
			if (e.getSource()==toolbarSave) {
				if (saveTextToFile(c,filter.getText())) lastFilterText=filter.getText();
				return;
			}
			if (e.getSource()==toolbarTools) {
				toolbarToolsMenu.show(toolbarTools,0,toolbarTools.getBounds().height);
			}
			if (e.getSource()==toolbarSelect) {
				if (dataFilter==null) dataFilter=new DataFilter(statistic.saveToXMLDocument());
				StatisticViewerFastAccessDialog dialog=new StatisticViewerFastAccessDialog(null,dataFilter.getXMLDocument(),helpFastAccessModal,false);
				dialog.setVisible(true);
				if (dialog.getClosedBy()==BaseEditDialog.CLOSED_BY_OK) {
					String xmlSelector=dialog.getXMLSelector();
					int insertType=dialog.getInsertType();
					if (insertType==0 || insertType==1) xmlSelector="Print "+xmlSelector;
					String s=filter.getText();
					if (insertType==1 || insertType==3) {
						s=s.substring(0,filter.getCaretPosition())+xmlSelector+s.substring(filter.getCaretPosition());
					} else {
						if (!s.endsWith("\n")) s+="\n";
						s+=xmlSelector;
					}
					filter.setText(s);
					filterUpdated();
				}
				return;
			}
			for (int i=0;i<toolbarTemplateItem.length;i++) if (e.getSource()==toolbarTemplateItem[i]) {
				String s=filter.getText();
				s=s.substring(0,filter.getCaretPosition())+"\n"+templateContent[i]+"\n"+s.substring(filter.getCaretPosition());
				filter.setText(s);
				filterUpdated();
				return;
			}
			if (e.getSource()==toolbarHelp) {
				if (helpFastAccess!=null) helpFastAccess.run();
				return;
			}
		}
	}

	/**
	 * Einträge für {@link StatisticViewerFastAccess#toolbarFilter}
	 * @see StatisticViewerFastAccess#toolbarFilter
	 */
	private static class ComboBoxItem {
		/** Anzuzeigender Text */
		private String value;

		/**
		 * Konstruktor der Klasse
		 * @param value	Anzuzeigender Text
		 */
		public ComboBoxItem(final String value) {
			this.value=value;
		}

		/**
		 * Stellt einen neuen anzuzeigenden Text ein.
		 * @param newValue	Anzuzeigender Text
		 */
		public void setValue(final String newValue) {
			value=newValue;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	@Override
	public void setRequestImageSize(IntSupplier getImageSize) {
	}

	@Override
	public void setUpdateImageSize(IntConsumer setImageSize) {
	}

	/**
	 * Soll für diese Komponente der Standard-FileDrop-Listener des {@link StatisticsBasePanel} verwendet werden?
	 * @return	Übergeordneten FileDrop-Listener verwenden (<code>false</code>) oder eigenen (<code>true</code>)
	 */
	@Override
	public boolean hasOwnFileDropListener() {
		return false;
	}

	@Override
	public boolean isViewerGenerated() {
		return false;
	}
}
