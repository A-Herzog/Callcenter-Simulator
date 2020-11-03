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
package ui.editor;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import language.Language;
import systemtools.MsgBox;
import ui.editor.CallerEditPanel.InitData;
import ui.editor.events.RenameEvent;
import ui.editor.events.RenameListener;
import ui.images.Images;
import ui.model.CallcenterModelCaller;

/**
 * Diese Klasse kapselt einen kompletten Bearbeitungsdialog für Kundentypen.
 * @author Alexander Herzog
 * @version 1.0
 * @see CallcenterModelCaller
 */
public class CallerEditDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2913991307659871097L;

	/** Objekt vom Typ <code>CallcenterModelCaller</code> welches die Kundentyp-Daten enthält (beim Klicken auf "Ok" wird auch dieses Objekt verändert) */
	private final CallcenterModelCaller caller;
	/** Array mit allen <code>CallcenterModelCaller</code>-Objekten (um Einstellungen auf andere Kundentypen anwenden zu können) */
	private final CallcenterModelCaller[] callers;
	private final int callerTypeIndexForThisType;
	private final CallerEditPanel.InitData initData;

	private final List<CallerEditPanel> panels=new ArrayList<CallerEditPanel>();
	private final List<RenameListener> listener=new ArrayList<RenameListener>();

	private final JPopupMenu popupMenu;
	private final JMenuItem[] applyThisPage, applyAllPages;

	private int openGenerator=0;

	/**
	 * Konstruktor der Klasse <code>CallerEditDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param serviceLevelModel Globaler Service-Level Wert in dem Modell
	 * @param caller	Objekt vom Typ <code>CallcenterModelCaller</code> welches die Kundentyp-Daten enthält (beim Klicken auf "Ok" wird auch dieses Objekt verändert)
	 * @param callers	Array mit allen <code>CallcenterModelCaller</code>-Objekten (um Einstellungen auf andere Kundentypen anwenden zu können)
	 * @param callerTypeNames	Liste mit allen Kundentypen-Namen (für die Auflistung der Weiterleitungsraten)
	 * @param skills	Liste mit allen Skill-Level-Namen (für die Auflistung der Skill-Level-abhängigen Weiterleitungsraten)
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param previous Button "Vorherige Kundengruppe" anzeigen
	 * @param next Button "Nächste Kundengruppe" anzeigen
	 * @param helpCallback	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird eine "Hilfe"-Schaltfläche angezeigt und die <code>Run</code>-Methode dieses Objekts beim Klicken auf diese Schaltfläche aufgerufen
	 */
	public CallerEditDialog(Window owner, short serviceLevelModel, CallcenterModelCaller caller, CallcenterModelCaller[] callers, String[] callerTypeNames, String[] skills, boolean readOnly, boolean previous, boolean next, Runnable helpCallback) {
		super(owner,Language.tr("Editor.Caller.Title"),callerTypeNames,readOnly,helpCallback);

		this.caller=caller;
		this.callers=callers;
		callerTypeIndexForThisType=Arrays.asList(callerTypeNames).indexOf(this.caller.name);
		initData=new InitData(this,caller,callerTypeIndexForThisType,serviceLevelModel,callerTypeNames,skills,readOnly,helpCallback);

		popupMenu=new JPopupMenu();
		applyThisPage=new JMenuItem[callerTypeNames.length+1];
		applyAllPages=new JMenuItem[callerTypeNames.length+1];
		if (!readOnly && callerTypeNames.length>1) {
			buildMenu();
			addUserButtons(new String[]{""}, new String[]{Language.tr("Editor.Caller.Apply.Info")}, new URL[]{Images.GENERAL_TOOLS.getURL()}, new Runnable[]{new ToolsButtonHandler()});
		}
		String previousText=null;
		if (previous) previousText=readOnly?Language.tr("Editor.Caller.Move.ViewPrevious"):Language.tr("Editor.Caller.Move.EditPrevious");
		String nextText=null;
		if (next) nextText=readOnly?Language.tr("Editor.Caller.Move.ViewNext"):Language.tr("Editor.Caller.Move.EditNext");
		createTabsGUI(Language.tr("Editor.Caller.Name")+":",caller.name,Language.tr("Editor.Caller.Active"),caller.active,925,500,previousText,nextText);
		tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
	}

	private void buildMenu() {
		final Icon user=Images.EDITOR_CALLER.getIcon();
		final Icon userRed=Images.EDITOR_CALLER_RED.getIcon();
		final Icon group=Images.EDITOR_AGENTS.getIcon();

		JMenu m;
		popupMenu.add(m=new JMenu(Language.tr("Editor.Caller.Apply.ThisPage")));
		m.setIcon(Images.EDITOR_APPLY_SINGLE.getIcon());

		for (int i=0;i<callerTypeNames.length;i++) {
			m.add(applyThisPage[i]=new JMenuItem(callerTypeNames[i]));
			applyThisPage[i].setIcon((i==callerTypeIndexForThisType)?userRed:user);
			applyThisPage[i].setEnabled(i!=callerTypeIndexForThisType);
			applyThisPage[i].addActionListener(new PopupActionListener());
		}
		if (callerTypeNames.length>2) {
			m.addSeparator();
			m.add(applyThisPage[callerTypeNames.length]=new JMenuItem(Language.tr("Editor.Caller.Apply.ForAllClientTypes")));
			applyThisPage[callerTypeNames.length].setIcon(group);
			applyThisPage[callerTypeNames.length].addActionListener(new PopupActionListener());
		}

		popupMenu.add(m=new JMenu(Language.tr("Editor.Caller.Apply.AllPages")));
		m.setIcon(Images.EDITOR_APPLY_ALL.getIcon());

		for (int i=0;i<callerTypeNames.length;i++) {
			m.add(applyAllPages[i]=new JMenuItem(callerTypeNames[i]));
			applyAllPages[i].setIcon((i==callerTypeIndexForThisType)?userRed:user);
			applyAllPages[i].setEnabled(i!=callerTypeIndexForThisType);
			applyAllPages[i].addActionListener(new PopupActionListener());
		}
		if (callerTypeNames.length>2) {
			m.addSeparator();
			m.add(applyAllPages[callerTypeNames.length]=new JMenuItem(Language.tr("Editor.Caller.Apply.ForAllClientTypes")));
			applyAllPages[callerTypeNames.length].setIcon(group);
			applyAllPages[callerTypeNames.length].addActionListener(new PopupActionListener());
		}
	}

	/**
	 * Die hier registrierten Listener werden benachrichtigt, wenn
	 * in dem Dialog ein Kundentyp umbenannt wird.
	 * @param listener	Listener für Kundentyp-Namensänderungen
	 * @see #removeCallerTypeRenameListener(RenameListener)
	 */
	public void addCallerTypeRenameListener(final RenameListener listener) {
		this.listener.add(listener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der Listener die über
	 * Kundentyp-Namensänderungen benachrichtigt werden sollen.
	 * @param listener	Listener für Kundentyp-Namensänderungen
	 * @return	Gibt an, ob der Listener erfolgreich deregistriert werden konnte
	 * @see #addCallerTypeRenameListener(RenameListener)
	 */
	public boolean removeCallerTypeRenameListener(final RenameListener listener) {
		return this.listener.remove(listener);
	}

	/**
	 * Wurde in dem Dialog ausgewählt, dass die Funktion zum Laden
	 * von Anruferzahlen pro Intervall werden soll?
	 * @return	Dialog zum Laden von Anruferzahlen pro Intervall öffnen?
	 */
	public int isOpenGenerator() {
		return openGenerator;
	}

	@Override
	protected void createTabs(JTabbedPane tabs) {
		panels.add(new CallerEditPanelFreshCalls(initData));
		panels.add(new CallerEditPanelWaitingTimeTolerance(initData));
		panels.add(new CallerEditPanelRetryProbability(initData));
		panels.add(new CallerEditPanelRetryDistribution(initData));
		panels.add(new CallerEditPanelForwarding(initData));
		panels.add(new CallerEditPanelRecaller(initData));
		panels.add(new CallerEditPanelRecallerDistribution(initData));
		panels.add(new CallerEditPanelScore(initData));
		panels.add(new CallerEditPanelCosts(initData));
		panels.add(new CallerEditPanelServiceLevel(initData));

		for (CallerEditPanel panel : panels) tabs.addTab(panel.getTabName(),panel.getTabIconObject(),panel);
	}

	@Override
	protected boolean checkData() {
		/* Name */
		if (name.getText().trim().isEmpty()) {
			MsgBox.error(this,Language.tr("Editor.Caller.Error.NoName.Title"),Language.tr("Editor.Caller.Error.NoName.Info"));
			return false;
		}

		/* Doppelte Namen */
		String s=name.getText();
		for (int i=0;i<callerTypeNames.length;i++) {
			if (i==callerTypeIndexForThisType) continue;
			if (callerTypeNames[i].equalsIgnoreCase(s)) {
				MsgBox.error(this,String.format(Language.tr("Editor.Caller.Error.NameInUse.Title"),s),String.format(Language.tr("Editor.Caller.Error.NameInUse.Info"),s));
				return false;
			}
		}

		/* Dialogseiten */
		for (CallerEditPanel p : panels) {
			String[] error=p.check(null);
			if (error!=null) {MsgBox.error(this,error[0],error[1]); return false;}
		}

		return true;
	}

	@Override
	protected void storeData() {
		caller.active=active.isSelected();

		if (!caller.name.equalsIgnoreCase(name.getText())) {
			final RenameEvent event=new RenameEvent(this,caller.name,name.getText());
			for (RenameListener renameListener : listener) renameListener.renamed(event);
		}

		caller.name=name.getText();

		for (CallerEditPanel p : panels) {
			p.writeToCaller(caller);
			if (p.getOpenGenerator()!=0) openGenerator=p.getOpenGenerator();
		}
	}

	@Override
	protected void nameChange(String newName) {
		for (CallerEditPanel p : panels) p.nameChange(newName);
	}

	private class ToolsButtonHandler implements Runnable{
		@Override
		public void run() {
			final JButton b=getUserButton(0);
			popupMenu.show(b,0,b.getHeight());
		}
	}

	private boolean checkPagesForApply(boolean allPages) {
		for (int i=0;i<panels.size();i++) if (allPages || i==tabs.getSelectedIndex()) {
			CallerEditPanel p=panels.get(i);
			String[] error=p.check(null);
			if (error!=null) {
				final String largeStart="<b><span style=\"font-size: 115%\">";
				final String largeEnd="</span></b>";
				StringBuilder sb=new StringBuilder();
				sb.append("<html><body>\n");
				sb.append(Language.tr("Editor.Caller.Apply.Error.Info")+"<br>\n");
				sb.append(largeStart+Language.tr("Editor.Caller.Apply.Error.AdditionalInformation")+":"+largeEnd+"<br>\n");
				sb.append("<div style=\"border: 1px solid red; background-color: #FFBBBB; padding: 5px;\">");
				sb.append(largeStart+error[0]+largeEnd+"\n");
				sb.append(error[1]+"</div>\n");
				sb.append("</body></html>");
				MsgBox.error(owner,Language.tr("Editor.Caller.Apply.Error.Title"),sb.toString());
				return false;
			}
		}
		return true;
	}

	private void applyPages(int page, int clientType) {
		for (int i=0;i<callers.length;i++) if (clientType==i || (clientType==-1 && i!=callerTypeIndexForThisType)) {
			for (int j=0;j<panels.size();j++) if (page==j || page==-1) {
				panels.get(j).writeToCaller(callers[i]);
			}
		}
	}

	private class PopupActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (int i=0;i<applyThisPage.length;i++) if (applyThisPage[i]==e.getSource()) {
				if (!checkPagesForApply(false)) return;
				applyPages(tabs.getSelectedIndex(),(i==applyThisPage.length-1)?-1:i);
				return;
			}
			for (int i=0;i<applyAllPages.length;i++) if (applyAllPages[i]==e.getSource()) {
				if (!checkPagesForApply(true)) return;
				applyPages(-1,(i==applyAllPages.length-1)?-1:i);
				return;
			}
		}
	}
}