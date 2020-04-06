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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
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
 * @see #setViewer(StatisticViewer, StatisticViewer, String, String, URL)
 */
public class StatisticDataPanel extends JPanel {
	private static final long serialVersionUID = -5311723790827549382L;

	private final Runnable updateAllViewerCallback;

	private final JLabel titleLabel;
	private final JLabel infoIcon, infoLabel;
	private final JButton zoom, copy, print, save, settings, selectAll, selectNone, saveTables, last;
	private final JPanel dataPanel;

	private final JPopupMenu settingsPopup;
	private final JMenuItem settingsCustomSettings;

	private JToolBar buttonPanel;
	private JPanel mainInfoPanel;
	private JPanel titlePanel;
	private JPanel infoPanel;

	private final List<JButton> userToolbarButtons;

	private StatisticViewer viewer=null;
	private StatisticViewer lastViewer=null;
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
	public final Container setViewer(final StatisticViewer viewer, final StatisticViewer lastViewer, final String superTitle, String info, final URL icon) {
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

	private final void printData() {
		if (viewer==null) return;
		viewer.print();
	}

	private final void saveData() {
		if (viewer==null) return;
		Container c=getParent(); while ((c!=null) && (!(c instanceof Frame))) c=c.getParent();
		viewer.save(c);
	}

	private final void saveTables() {
		if (viewer==null) return;
		if (!(viewer instanceof StatisticViewerReport)) return;
		((StatisticViewerReport)viewer).saveTablesToWorkbook(this);
	}

	private final void reportSelectAll() {
		if (viewer==null) return;
		if (!(viewer instanceof StatisticViewerReport)) return;
		((StatisticViewerReport)viewer).selectAll();
	}

	private final void reportSelectNone() {
		if (viewer==null) return;
		if (!(viewer instanceof StatisticViewerReport)) return;
		((StatisticViewerReport)viewer).selectNone();
	}

	private boolean showCustomDialog() {
		boolean b=viewer.ownSettings(this);
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

	private void resetSubViewer(final StatisticViewer currentViewer) {
		final Container viewerComponent=currentViewer.getViewer(false).getParent();
		final Container parent=viewerComponent.getParent();
		if (parent==null) return;
		parent.remove(viewerComponent);

		parent.add(currentViewer.getViewer(false));
	}

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
