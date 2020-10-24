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

import java.awt.Window;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import systemtools.statistics.JCheckboxTable;
import systemtools.statistics.StatisticNode;
import ui.editor.BaseEditDialog;
import ui.images.Images;
import ui.statistic.StatisticPanel;

/**
 * In diesem Dialog kann eingestellt werden, welche {@link StatisticNode}-Elemente in
 * dem {@link StatisticPanel} angezeigt werden sollen.
 * @author Alexander Herzog
 * @version 1.0
 */
public final class StatisticFilterDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -5055497416534668594L;

	/** Liste mit allen IDs */
	private final String[] ids;
	/** Welche IDs sidn ausgewählt? */
	private final boolean[] select;
	/** Liste mit den momentan ausgeblendeten IDs */
	private final List<String> hiddenIDs;

	/** Tabelle zur Auswahl der IDs */
	private JCheckboxTable table;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param title	Titel dieses Dialogs
	 * @param ids	Liste mit allen IDs
	 * @param hiddenIDs	Liste mit den momentan ausgeblendeten IDs
	 * @param helpModal	Hilfe-Callback
	 */
	public StatisticFilterDialog(Window owner, String title, String[] ids, String[] hiddenIDs, Runnable helpModal) {
		super(owner,title,false,helpModal);

		this.ids=ids;
		this.hiddenIDs=new ArrayList<String>(Arrays.asList(hiddenIDs));
		select=new boolean[ids.length];
		for (int i=0;i<ids.length;i++) select[i]=!this.hiddenIDs.contains(ids[i]);

		addUserButtons(new String[]{Language.tr("Dialog.Select.All"),Language.tr("Dialog.Select.Nothing")},new URL[]{Images.EDIT_ADD.getURL(),Images.EDIT_DELETE.getURL()},new Runnable[]{
				new Runnable() {@Override public void run() {table.selectAll();}},
				new Runnable() {@Override public void run() {table.selectNone();}}
		});

		createSimpleGUI(300,500,null,null);
		pack();
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		content.add(new JScrollPane(table=new JCheckboxTable(ids,select)));
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#checkData()
	 */
	@Override
	protected boolean checkData() {return true;}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#storeData()
	 */
	@Override
	protected void storeData() {
		hiddenIDs.clear();
		boolean[] newSelect=table.getSelected();
		for (int i=0;i<newSelect.length;i++) if (!newSelect[i]) hiddenIDs.add(ids[i]);
	}

	/**
	 * Wurde der Dialog mit "Ok" geschossen, so kann über diese Funktion die Liste der auszublendenden IDs abgerufen werden.
	 * @return	Liste der auszublendenden IDs
	 */
	public String[] getHiddenIDs() {
		return hiddenIDs.toArray(new String[0]);
	}
}