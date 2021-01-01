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
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import language.Language;
import systemtools.images.SimToolsImages;
import systemtools.statistics.StatisticViewer;
import systemtools.statistics.StatisticViewer.CanDoAction;
import systemtools.statistics.StatisticViewerReport;
import systemtools.statistics.StatisticViewerSpecialBase;
import ui.images.Images;
import ui.statistic.core.viewers.StatisticViewerSpecialHTMLText;

/**
 * Dieses Panel stellt den Rahmen zur Darstellung
 * der {@link StatisticViewer}-Elemente dar.
 * @author Alexander Herzog
 * @see #setViewer(StatisticViewer, StatisticViewer, String, String, Image)
 */
public class StatisticDataPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -5311723790827549382L;

	/** Optionales Callback, welches aufgerufen wird, wenn alle Viewer aktualisiert werden sollen (dies passiert nach dem Schließen von {@link StatisticViewer#ownSettings(JPanel)}) */
	private final Runnable updateAllViewerCallback;

	/** Feld zur Anzeige des Titels */
	private final JLabel titleLabel;
	/** Feld zur Anzeige des Info-Icons */
	private final JLabel infoIcon;
	/** Feld zur Anzeige des Info-Textes */
	private final JLabel infoLabel;
	/** Schaltfläche Zoomfaktor zurücksetzen */
	private final JButton zoom;
	/** Schaltfläche Kopieren */
	private final JButton copy;
	/** Schaltfläche Drucken */
	private final JButton print;
	/** Schaltfläche Speichern */
	private final JButton save;
	/** Schaltfläche Einstellungen */
	private final JButton  settings;
	/** Schaltfläche "Alle auswählen" */
	private final JButton selectAll;
	/** Schaltfläche "Alle abwählen" */
	private final JButton selectNone;
	/** Schaltfläche "Tabellen speichern" */
	private final JButton saveTables;
	/** Schaltfläche "Vergleich mit Ergebnissen aus vorheriger Simulation */
	private final JButton last;
	/** Anzeigebereich für die Statistikausgabe */
	private final JPanel dataPanel;

	/** Popupmenü zur Konfiguration ({@link #settings}) */
	private final JPopupMenu settingsPopup;
	/** Menüpunkt für benutzerdefinierte Einstellungen im {@link #settingsPopup}-Menü */
	private final JMenuItem settingsCustomSettings;

	/** Symbolleiste */
	private JToolBar buttonPanel;
	/** Infopanel über den Viewern */
	private JPanel mainInfoPanel;
	/** Titel auf dem Infopanel */
	private JPanel titlePanel;
	/** Informationstext auf dem Infopanel */
	private JPanel infoPanel;

	/** Optionale zusätzliche benutzerdefinierte Schaltflächen in der Symbolleiste */
	private final List<JButton> userToolbarButtons;

	/** Aktueller Viewer */
	private StatisticViewer viewer=null;
	/** Viewer von vorheriger Simulation */
	private StatisticViewer lastViewer=null;
	/** Container der die eigentlich Inhalte des Viewers enthält */
	private Container viewerContainer=null;

	/**
	 * Konstruktor der Klasse
	 * @param updateAllViewerCallback	Optionales Callback, welches aufgerufen wird, wenn alle Viewer aktualisiert werden sollen (dies passiert nach dem Schließen von {@link StatisticViewer#ownSettings(JPanel)})
	 */
	public StatisticDataPanel(Runnable updateAllViewerCallback) {
		super(new BorderLayout());
		userToolbarButtons=new ArrayList<>();

		this.updateAllViewerCallback=updateAllViewerCallback;

		JPanel mainTopPanel;

		/* Infotext oben in der Mitte */
		add(mainTopPanel=new JPanel(new BorderLayout()),BorderLayout.NORTH);
		mainTopPanel.add(mainInfoPanel=new JPanel(),BorderLayout.NORTH);
		mainInfoPanel.setLayout(new BoxLayout(mainInfoPanel,BoxLayout.PAGE_AXIS));
		mainInfoPanel.setBackground(Color.GRAY);
		mainInfoPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		mainInfoPanel.add(titlePanel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		titlePanel.setOpaque(false);
		titlePanel.add(titleLabel=new JLabel(""));
		Font font=titleLabel.getFont();
		titleLabel.setFont(new java.awt.Font(font.getFontName(),java.awt.Font.BOLD,font.getSize()+1));
		titleLabel.setForeground(Color.WHITE);

		mainInfoPanel.add(infoPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		infoPanel.setOpaque(false);
		infoPanel.add(infoIcon=new JLabel());
		infoPanel.add(infoLabel=new JLabel(""));
		font=infoLabel.getFont();
		infoLabel.setFont(new java.awt.Font(font.getFontName(),java.awt.Font.BOLD,font.getSize()+3));
		infoLabel.setForeground(Color.WHITE);

		/* Buttons oben in der Mitte */
		mainTopPanel.add(buttonPanel=new JToolBar(),BorderLayout.CENTER);
		buttonPanel.setFloatable(false);

		buttonPanel.add(zoom=new JButton(Language.tr("Statistic.Toolbar.DefaultZoom")));
		zoom.addActionListener(new ButtonListener());
		zoom.setToolTipText(Language.tr("Statistic.Toolbar.DefaultZoom.Tooltip"));
		zoom.setIcon(Images.ZOOM.getIcon());

		buttonPanel.add(copy=new JButton(Language.tr("Dialog.Button.Copy")));
		copy.addActionListener(new ButtonListener());
		copy.setToolTipText(Language.tr("Statistic.Toolbar.Copy.Tooltip"));
		copy.setIcon(Images.EDIT_COPY.getIcon());

		buttonPanel.add(print=new JButton(Language.tr("Dialog.Button.Print")));
		print.addActionListener(new ButtonListener());
		print.setToolTipText(Language.tr("Statistic.Toolbar.Print.Tooltip"));
		print.setIcon(Images.GENERAL_PRINT.getIcon());

		buttonPanel.add(save=new JButton(Language.tr("Dialog.Button.Save")));
		save.addActionListener(new ButtonListener());
		save.setToolTipText(Language.tr("Statistic.Toolbar.Save.Tooltip"));
		save.setIcon(Images.GENERAL_SAVE.getIcon());

		buttonPanel.add(settings=new JButton(Language.tr("Statistic.Toolbar.Settings")));
		settings.addActionListener(new ButtonListener());
		settings.setToolTipText(Language.tr("Statistic.Toolbar.Settings.Tooltip"));
		settings.setIcon(Images.GENERAL_SETUP.getIcon());

		buttonPanel.add(selectAll=new JButton(Language.tr("Dialog.Select.All")));
		selectAll.addActionListener(new ButtonListener());
		selectAll.setToolTipText(Language.tr("Statistic.Toolbar.All.Tooltip"));
		selectAll.setIcon(Images.EDIT_ADD.getIcon());

		buttonPanel.add(selectNone=new JButton(Language.tr("Dialog.Select.Nothing")));
		selectNone.addActionListener(new ButtonListener());
		selectNone.setToolTipText(Language.tr("Statistic.Toolbar.Nothing.Tooltip"));
		selectNone.setIcon(Images.EDIT_DELETE.getIcon());

		buttonPanel.add(saveTables=new JButton(Language.tr("Statistic.Toolbar.SaveTables")));
		saveTables.addActionListener(new ButtonListener());
		saveTables.setToolTipText(Language.tr("Statistic.Toolbar.SaveTables.Tooltip"));
		saveTables.setIcon(Images.STATISTICS_SAVE_TABLE.getIcon());

		buttonPanel.add(last=new JButton(Language.tr("Statistic.Previous")));
		last.addActionListener(new ButtonListener());
		last.setToolTipText(Language.tr("Statistic.Previous.Hint"));
		last.setIcon(SimToolsImages.STATISTICS_COMPARE_LAST.getIcon());

		/* Datenpanel */
		add(dataPanel=new JPanel(new BorderLayout()));

		/* Popup-Menü vorbereiten */
		settingsPopup=new JPopupMenu();
		settingsPopup.add(settingsCustomSettings=new JMenuItem(""));
		settingsCustomSettings.addActionListener(new ButtonListener());
	}

	/**
	 * Stellt den in dem Panel darzustellenden Viewer ein
	 * @param viewer	Darzustellender Viewer
	 * @param lastViewer	Viewer mit Ergebnissen aus dem letzten Simulationslauf (optional, darf <code>null</code> sein)
	 * @param superTitle	Zusätzlicher Titel über dem Titel des Viewers
	 * @param info	Name des Viewers
	 * @param icon	Icon vor der Namensanzeige
	 * @return	Container der die eigentlich Inhalte des Viewers enthält
	 */
	public final Container setViewer(final StatisticViewer viewer, final StatisticViewer lastViewer, final String superTitle, String info, final Image icon) {
		if (this.viewer==viewer) return viewerContainer;
		this.viewer=viewer;
		this.lastViewer=lastViewer;
		dataPanel.removeAll();

		last.setVisible(false);
		if (viewer==null) {
			copy.setVisible(false);
			print.setVisible(false);
			save.setVisible(false);
			settings.setVisible(false);
			selectAll.setVisible(false);
			selectNone.setVisible(false);
			saveTables.setVisible(false);
			zoom.setVisible(false);
		} else {
			zoom.setVisible(viewer.getCanDo(CanDoAction.CAN_DO_UNZOOM));
			if (viewer instanceof StatisticViewerSpecialBase) {
				copy.setVisible(((StatisticViewerSpecialBase)viewer).getCanDo(StatisticViewerSpecialBase.CanDoAction.CAN_DO_COPY));
				print.setVisible(((StatisticViewerSpecialBase)viewer).getCanDo(StatisticViewerSpecialBase.CanDoAction.CAN_DO_PRINT));
				save.setVisible(((StatisticViewerSpecialBase)viewer).getCanDo(StatisticViewerSpecialBase.CanDoAction.CAN_DO_SAVE));
				settings.setVisible(false);
				selectAll.setVisible(viewer instanceof StatisticViewerReport);
				selectNone.setVisible(viewer instanceof StatisticViewerReport);
				saveTables.setVisible(viewer instanceof StatisticViewerReport);
				last.setVisible(false);
			} else {
				if (viewer instanceof StatisticViewerSpecialHTMLText) {
					copy.setVisible(false);
					print.setVisible(false);
					save.setVisible(false);
					settings.setVisible(false);
				} else {
					copy.setVisible(true);
					print.setVisible(true);
					save.setVisible(true);
					boolean b=(viewer.ownSettingsName()!=null);
					settings.setVisible(b);
					if (b) {
						settingsCustomSettings.setText(viewer.ownSettingsName());
						settingsCustomSettings.setIcon(viewer.ownSettingsIcon());
					}
				}
				selectAll.setVisible(false);
				selectNone.setVisible(false);
				saveTables.setVisible(false);
				if (lastViewer!=null) {
					last.setVisible(true);
					last.setText(Language.tr("Statistic.Previous"));
					last.setToolTipText(Language.tr("Statistic.Previous.Hint"));
				}
			}
		}

		infoIcon.setVisible(icon!=null);
		if (icon!=null)	infoIcon.setIcon(new ImageIcon(icon));
		infoLabel.setText(info);
		infoPanel.doLayout();
		mainInfoPanel.doLayout();
		infoPanel.doLayout();

		int delta=0;
		if (superTitle!=null && !superTitle.trim().isEmpty()) {
			titlePanel.setVisible(true);
			titleLabel.setText(superTitle);
			delta=titlePanel.getY();
		} else {
			titlePanel.setVisible(false);
			titleLabel.setText("");
		}

		while (infoLabel.getY()-delta>infoLabel.getSize().height && info.length()>20) {
			info=info.substring(0,info.length()-1);
			infoLabel.setText(info+"...");
			infoPanel.doLayout();
			mainInfoPanel.doLayout();
			infoPanel.doLayout();
		}

		updateViewerContainer(false);
		return viewerContainer;
	}

	/**
	 * Aktualisiert die Ausgabe
	 * @param needReInit	Muss der Viewer in Bezug auf die Daten zwingend neu initialisiert werden?
	 */
	private void updateViewerContainer(boolean needReInit) {
		if (viewerContainer!=null) dataPanel.remove(viewerContainer);

		viewerContainer=null;
		if (viewer!=null) viewerContainer=viewer.getViewer(needReInit);
		if (viewerContainer==null) viewerContainer=new JPanel();
		dataPanel.add(viewerContainer,BorderLayout.CENTER);

		for (JButton oldButton: userToolbarButtons) buttonPanel.remove(oldButton);
		userToolbarButtons.clear();
		final JButton[] newButtons=(viewer==null)?new JButton[0]:viewer.getAdditionalButton();
		if (newButtons!=null) for (JButton newButton: newButtons) {
			buttonPanel.add(newButton);
			userToolbarButtons.add(newButton);
		}

		dataPanel.revalidate();
		dataPanel.repaint();

		Container c=dataPanel.getParent();
		while (c!=null) {
			c.revalidate();
			c.repaint();
			c=c.getParent();
		}

		viewerContainer.setVisible(false);
		viewerContainer.setVisible(true);
	}

	/**
	 * Stellt den Standard-Zoomfaktor wieder her.
	 */
	private final void unZoom() {
		if (viewer==null) return;
		viewer.unZoom();
	}

	/**
	 * Kopiert die Daten in dem aktuellen Viewer in die Zwischenablage.
	 */
	public final void copyData() {
		if (viewer==null) return;
		viewer.copyToClipboard(getToolkit().getSystemClipboard());
	}

	/**
	 * Druckt die Daten in dem aktuellen Viewer.
	 */
	private final void printData() {
		if (viewer==null) return;
		viewer.print();
	}

	/**
	 * Speichert die Daten in dem aktuellen Viewer.
	 */
	private final void saveData() {
		if (viewer==null) return;
		Container c=getParent(); while ((c!=null) && (!(c instanceof Frame))) c=c.getParent();
		viewer.save(c);
	}

	/**
	 * Speichert, wenn es sich um den Report-Viewer handelt, die Tabellendaten in einer Tabellen-Arbeitsmappe.
	 */
	private final void saveTables() {
		if (viewer==null) return;
		if (!(viewer instanceof StatisticViewerReport)) return;
		((StatisticViewerReport)viewer).saveTablesToWorkbook(this);
	}

	/**
	 * Selektiert, wenn es sich um den Report-Viewer handelt, alle Einträge.
	 */
	private final void reportSelectAll() {
		if (viewer==null) return;
		if (!(viewer instanceof StatisticViewerReport)) return;
		((StatisticViewerReport)viewer).selectAll();
	}

	/**
	 * Wählt, wenn es sich um den Report-Viewer handelt, alle Einträge ab.
	 */
	private final void reportSelectNone() {
		if (viewer==null) return;
		if (!(viewer instanceof StatisticViewerReport)) return;
		((StatisticViewerReport)viewer).selectNone();
	}

	/**
	 * Zeigt den Dialog mit zusätzlichen Einstellungen zu dem Viewer an.
	 * @return	Liefert <code>true</code>, wenn der Dialog per "Ok" geschlossen wurde.
	 * @see StatisticViewer#ownSettings(JPanel)
	 */
	private boolean showCustomDialog() {
		final boolean b=viewer.ownSettings(this);
		if (b) {
			updateViewerContainer(true);
			if (updateAllViewerCallback!=null) updateAllViewerCallback.run();
		}
		return b;
	}

	/**
	 * Teilt dem Panel mit, dass es neu gezeichnet (und zu vor gelayouted) werden muss.
	 */
	public void updateViewer() {
		updateViewerContainer(true);
	}

	/**
	 * Fügt einen zusätzlichen Viewer hinzu
	 * @param currentViewer	Aktueller Viewer
	 * @param additionalViewer	Zusätzlicher Viewer
	 * @see #resetSubViewer(StatisticViewer)
	 */
	private void addSubViewer(final StatisticViewer currentViewer, final StatisticViewer additionalViewer) {
		final Container viewerComponent=currentViewer.getViewer(false);
		final Container parent=viewerComponent.getParent();
		if (parent==null) return;
		parent.remove(viewerComponent);

		final JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.setLeftComponent(viewerComponent);
		split.setRightComponent(additionalViewer.getViewer(true));
		split.setBorder(BorderFactory.createEmptyBorder());
		parent.add(split);
		split.setResizeWeight(0.66);
		split.setDividerLocation(0.5);
	}

	/**
	 * Entfernt einen möglichen zusätzlichen Viewer
	 * @param currentViewer	Aktueller Viewer
	 * @see #addSubViewer(StatisticViewer, StatisticViewer)
	 */
	private void resetSubViewer(final StatisticViewer currentViewer) {
		final Container viewerComponent=currentViewer.getViewer(false).getParent();
		final Container parent=viewerComponent.getParent();
		if (parent==null) return;
		parent.remove(viewerComponent);

		parent.add(currentViewer.getViewer(false));
	}

	/**
	 * Aktiviert oder deaktiviert die parallele Anzeige
	 * der Ergebnisse des vorherigen Simulationslaufs.
	 * @see #addSubViewer(StatisticViewer, StatisticViewer)
	 * @see #resetSubViewer(StatisticViewer)

	 */
	private void showLast() {
		if (last.getText().equals(Language.tr("Statistic.Previous"))) {
			last.setText(Language.tr("Statistic.PreviousRemove"));
			last.setToolTipText(Language.tr("Statistic.PreviousRemove.Hint"));
			addSubViewer(viewer,lastViewer);
		} else {
			last.setText(Language.tr("Statistic.Previous"));
			last.setToolTipText(Language.tr("Statistic.Previous.Hint"));
			resetSubViewer(viewer);
		}
	}

	/**
	 * Reagiert auf Klicks auf die Symbolleisten-Schaltflächen
	 */
	private final class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==zoom) {unZoom(); return;}
			if (e.getSource()==copy) {copyData(); return;}
			if (e.getSource()==print) {printData(); return;}
			if (e.getSource()==save) {saveData(); return;}
			if (e.getSource()==settings) {settingsPopup.show(settings,0,settings.getBounds().height); return;}
			if (e.getSource()==settingsCustomSettings) {showCustomDialog(); return;}
			if (e.getSource()==selectAll) {reportSelectAll(); return;}
			if (e.getSource()==selectNone) {reportSelectNone(); return;}
			if (e.getSource()==saveTables) {saveTables(); return;}
			if (e.getSource()==last) {showLast(); return;}
		}
	}
}
