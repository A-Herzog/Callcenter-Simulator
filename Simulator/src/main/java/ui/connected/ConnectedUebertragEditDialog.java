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
package ui.connected;

import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.Statistics;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.editor.BaseEditDialog;
import ui.images.Images;
import ui.model.CallcenterModel;

/**
 * In diesem Dialog kann eingestellt werden, ein wie hoher Anteil der Warteabbrecher
 * von einem Tag in einer verbundenen Simulation am nächsten Simulationstag einen
 * erneuten Anrufversuch starten soll.
 * @author Alexander Herzog
 * @see ConnectedJTableModel
 */
public final class ConnectedUebertragEditDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1382291382514142930L;

	/** Konfiguration der Überträge von Warteabbrechern von einem Tag zum nächsten */
	private final HashMap<String,ConnectedModelUebertrag> uebertrag;
	/** Liste aller vorhandenen Kundengruppen */
	private final String[] caller;
	/** Konfiguration der Überträge von Warteabbrechern von einem Tag zum nächsten */
	private final ConnectedModelUebertrag[] callerRetry;

	/** Option: Kein Übertrag der Abbrecher */
	private JRadioButton radioNo;
	/** Option: Warteabbrecher pauschal übertragen */
	private JRadioButton radioGlobal;
	/** Option: Warteabbrecher gemäß Kundentyp übertragen */
	private JRadioButton radioByType;

	/** Eingabefeld für den Anteil der zu übertragenden Warteabbrecher im Fall {@link #radioGlobal} */
	private JTextField retryGlobal;
	/** Datenmodell für die Wiederholwahrscheinlichkeitentabelle {@link #retryTable} */
	private UebertragTableModel retryData;
	/** Wiederholwahrscheinlichkeitentabelle */
	private JTableExt retryTable;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param helpCallback	Hilfe-Callback
	 * @param modelFile	Simulationsmodelldatei zur Bestimmung der verfügbaren Kundentypnamen
	 * @param statisticFile	Statistikdatei zur Bestimmung der verfügbaren Kundentypnamen
	 * @param uebertrag	Konfiguration der Überträge von Warteabbrechern von einem Tag zum nächsten
	 */
	public ConnectedUebertragEditDialog(final Window owner, final Runnable helpCallback, final File modelFile, final File statisticFile, final HashMap<String,ConnectedModelUebertrag> uebertrag) {
		super(owner,Language.tr("Connected.EditCarryOver.Title"),false,helpCallback);
		this.uebertrag=uebertrag;

		/* Namen der Kundentypen laden */
		caller=getCallerNames(modelFile,statisticFile);

		/* Übertragsdaten laden */
		if (caller==null) {callerRetry=null;} else {
			callerRetry=new ConnectedModelUebertrag[caller.length];
			for (int i=0;i<caller.length;i++) {
				final ConnectedModelUebertrag u=uebertrag.get(caller[i]);
				callerRetry[i]=(u==null)?(new ConnectedModelUebertrag(0)):(u.cloneUebertrag());
			}
		}

		createSimpleGUI(500,450,null,null);
		pack();
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param helpCallback	Hilfe-Callback
	 * @param model	Simulationsmodell zur Bestimmung der verfügbaren Kundentypnamen
	 * @param statisticFile	Statistikdatei zur Bestimmung der verfügbaren Kundentypnamen
	 * @param uebertrag	Konfiguration der Überträge von Warteabbrechern von einem Tag zum nächsten
	 */
	public ConnectedUebertragEditDialog(final Window owner, final Runnable helpCallback, final CallcenterModel model, final File statisticFile, final HashMap<String,ConnectedModelUebertrag> uebertrag) {
		super(owner,Language.tr("Connected.EditCarryOver.Title"),false,helpCallback);
		this.uebertrag=uebertrag;

		/* Namen der Kundentypen laden */
		caller=getCallerNames(model,statisticFile);

		/* Übertragsdaten laden */
		if (caller==null) {callerRetry=null;} else {
			callerRetry=new ConnectedModelUebertrag[caller.length];
			for (int i=0;i<caller.length;i++) {
				ConnectedModelUebertrag u=uebertrag.get(caller[i]);
				callerRetry[i]=(u==null)?(new ConnectedModelUebertrag(0)):(u.cloneUebertrag());
			}
		}

		createSimpleGUI(500,450,null,null);
		pack();
	}

	/**
	 * Liefert eine Liste aller vorhandenen Kundengruppen.
	 * @param model	Modell (darf <code>null</code> sein)
	 * @param statisticFile	Statistikdatei (wird nur verwendet, wenn als Modelldatei <code>null</code> übergeben wurde)
	 * @return	Liste aller vorhandenen Kundengruppen
	 * @see #caller
	 */
	private String[] getCallerNames(final CallcenterModel model, final File statisticFile) {
		String[] names=getCallerNamesFromModel(model);
		if (names!=null) return names;

		return getCallerNamesFromStatistic(statisticFile);
	}

	/**
	 * Liefert eine Liste aller vorhandenen Kundengruppen.
	 * @param modelFile	Modelldatei	(darf <code>null</code> sein)
	 * @param statisticFile	Statistikdatei (wird nur verwendet, wenn als Modelldatei <code>null</code> übergeben wurde)
	 * @return	Liste aller vorhandenen Kundengruppen
	 * @see #caller
	 */
	private String[] getCallerNames(final File modelFile, final File statisticFile) {
		if (modelFile!=null) {
			CallcenterModel model=new CallcenterModel();
			if (model.loadFromFile(modelFile)!=null) model=null;
			String[] names=getCallerNamesFromModel(model);
			if (names!=null) return names;
		}

		return getCallerNamesFromStatistic(statisticFile);
	}

	/**
	 * Liefert eine Liste aller vorhandenen Kundengruppen.
	 * @param model	Modell
	 * @return	Liste aller vorhandenen Kundengruppen
	 * @see #caller
	 */
	private String[] getCallerNamesFromModel(CallcenterModel model) {
		if (model!=null && model.caller.size()==0) model=null;
		if (model!=null) {
			String[] names=new String[model.caller.size()];
			for (int i=0;i<model.caller.size();i++) names[i]=model.caller.get(i).name;
			return names;
		}

		return null;
	}

	/**
	 * Liefert eine Liste aller vorhandenen Kundengruppen.
	 * @param statisticFile	Statistikdatei
	 * @return	Liste aller vorhandenen Kundengruppen
	 * @see #caller
	 */
	private String[] getCallerNamesFromStatistic(File statisticFile) {
		if (caller==null && statisticFile!=null) {
			Statistics statistic=new Statistics(null,null,0,0);
			if (statistic.loadFromFile(statisticFile)!=null) statistic=null;
			if (statistic!=null && statistic.kundenProTyp.length==0) statistic=null;
			if (statistic!=null) {
				String[] names=new String[statistic.kundenProTyp.length];
				for (int i=0;i<statistic.kundenProTyp.length;i++) names[i]=statistic.kundenProTyp[i].name;
				return names;
			}
		}

		return null;
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		/* Init GUI */
		JPanel p;

		content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
		content.add(radioNo=new JRadioButton(Language.tr("Connected.EditCarryOver.NoCarryOver")));
		radioNo.setAlignmentX(0);

		content.add(radioGlobal=new JRadioButton(Language.tr("Connected.EditCarryOver.FixedCarryOver")));
		radioGlobal.setAlignmentX(0);
		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.setAlignmentX(0);
		p.add(new JLabel(Language.tr("Connected.EditCarryOver.FixedCarryOver.Part")+":"));
		p.add(retryGlobal=new JTextField("70%",5));
		retryGlobal.addKeyListener(new RetryGlobalKeyListener());
		p.setMaximumSize(p.getPreferredSize());

		content.add(radioByType=new JRadioButton(Language.tr("Connected.EditCarryOver.CarryOverByClientType")));
		radioByType.setAlignmentX(0);
		radioByType.setEnabled(caller!=null);
		if (caller==null) {
			content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p.setAlignmentX(0);
			p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
			p.add(new JLabel(Language.tr("Connected.EditCarryOver.CarryOverByClientType.NoModelLine1")));
			p.add(new JLabel(Language.tr("Connected.EditCarryOver.CarryOverByClientType.NoModelLine2")));
		} else {
			retryData=new UebertragTableModel();
			JScrollPane sc;
			content.add(sc=new JScrollPane(retryTable=new JTableExt(retryData)));
			sc.setAlignmentX(0);
			retryTable.setIsPanelCellTable(2);
		}

		content.add(Box.createVerticalGlue());

		ButtonGroup group=new ButtonGroup();
		group.add(radioNo);
		group.add(radioGlobal);
		group.add(radioByType);

		/* Load data */
		if (uebertrag.size()==0) {
			radioNo.setSelected(true);
		} else {
			if (uebertrag.size()==1 && uebertrag.keySet().toArray(String[]::new)[0].isEmpty()) {
				radioGlobal.setSelected(true);
				retryGlobal.setText(NumberTools.formatNumberMax(uebertrag.get("").probability*100)+"%");
			} else {
				radioByType.setSelected(true);
			}
		}
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#checkData()
	 */
	@Override
	protected boolean checkData() {
		if (radioGlobal.isSelected()) {
			Double D=NumberTools.getProbability(retryGlobal.getText());
			if (D==null) {
				MsgBox.error(this,Language.tr("Connected.Error.InvalidRetryProbability.Title"),String.format(Language.tr("Connected.Error.InvalidRetryProbability.Info"),retryGlobal.getText()));
				return false;
			}
			return true;
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#storeData()
	 */
	@Override
	protected void storeData() {
		uebertrag.clear();

		if (radioNo.isSelected()) return;

		if (radioGlobal.isSelected()) {
			Double D=NumberTools.getProbability(retryGlobal.getText());
			uebertrag.put("",new ConnectedModelUebertrag(D));
			return;
		}

		if (radioByType.isSelected()) {
			for (int i=0;i<callerRetry.length;i++) {
				uebertrag.put(caller[i],callerRetry[i].cloneUebertrag());
			}
			return;
		}
	}

	/**
	 * Datenmodell für die Wiederholwahrscheinlichkeitentabelle
	 * @see ConnectedUebertragEditDialog#retryData
	 * @see ConnectedUebertragEditDialog#retryTable
	 */
	private final class UebertragTableModel extends JTableExtAbstractTableModel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -3347645492735924255L;

		/**
		 * Konstruktor der Klasse
		 */
		public UebertragTableModel() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex>0;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0: return Language.tr("Connected.ClientType");
			case 1: return Language.tr("Connected.RetryProbability");
			case 2: return Language.tr("Connected.ClientTypeChange");
			default: return "";
			}
		}

		@Override
		public int getRowCount() {
			return caller.length;
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0: return caller[rowIndex];
			case 1: return NumberTools.formatNumberMax(callerRetry[rowIndex].probability*100)+"%";
			case 2: return makeButtonPanel(
					new String[]{Language.tr("Connected.ClientTypes")},
					new Icon[]{Images.EDITOR_CALLER.getIcon()},
					new ActionListener[]{new ButtonListener(rowIndex)}
					);
			default: return null;
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex==1) {
				Double D=NumberTools.getProbability(aValue.toString());
				if (D!=null) callerRetry[rowIndex].probability=D;
			}
			radioByType.setSelected(true);
		}
	}

	/**
	 * Reagiert auf Tastendrücke auf {@link ConnectedUebertragEditDialog#retryGlobal}
	 * @see ConnectedUebertragEditDialog#retryGlobal
	 */
	private final class RetryGlobalKeyListener implements KeyListener {
		/**
		 * Konstruktor der Klasse
		 */
		public RetryGlobalKeyListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void keyTyped(KeyEvent e) {
			radioGlobal.setSelected(true);
		}

		@Override
		public void keyPressed(KeyEvent e) {
			radioGlobal.setSelected(true);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			radioGlobal.setSelected(true);
		}
	}

	/**
	 * Reagiert auf Klicks auf die Schaltfläche in der Tabelle
	 * @see ConnectedUebertragEditDialog#retryTable
	 */
	private final class ButtonListener implements ActionListener {
		/** Tabellenzeile */
		private final int index;

		/**
		 * Konstruktor der Klasse
		 * @param index	Tabellenzeile
		 */
		public ButtonListener(int index) {
			this.index=index;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			ConnectedUebertragTypeEditDialog dialog=new ConnectedUebertragTypeEditDialog(ConnectedUebertragEditDialog.this,caller[index],caller,callerRetry[index],helpCallback);
			dialog.setVisible(true);
		}
	}
}
