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
package ui.dataloader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import language.Language;
import ui.HelpLink;
import ui.editor.BaseEditDialog;
import ui.model.CallcenterModel;

/**
 * Dialog um speziell formatierte Daten zu importieren
 * @author Alexander Herzog
 * @version 1.0
 * @see AbstractSpecialProcessing
 */
public final class SpecialProcessingDialog extends BaseEditDialog {
	private static final long serialVersionUID = 3919074252992633185L;

	private final List<AbstractSpecialProcessing> handlers=new ArrayList<AbstractSpecialProcessing>();
	private JList<String> list;
	private JLabel toplabel;
	private JPanel main;
	private CallcenterModel model=null;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param helpLink	Hilfe-Link
	 */
	public SpecialProcessingDialog(final Window owner, final HelpLink helpLink) {
		super(owner,Language.tr("Loader.ProcessData.Title"),false,helpLink.dialogSpecialLoader);
		registerProcessingClasses();
		createSimpleGUI(850,700,null,null);
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		content.setLayout(new BorderLayout());
		content.setBorder(BorderFactory.createEmptyBorder());

		/* Liste */
		String[] names=new String[handlers.size()];
		for (int i=0;i<names.length;i++) names[i]=handlers.get(i).getName();
		JScrollPane sp=new JScrollPane(list=new JList<String>(names));
		sp.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));
		content.add(sp,BorderLayout.WEST);
		list.addListSelectionListener(new ListSelection());

		/* Content */
		JPanel right, topinfo;
		content.add(right=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		right.setBorder(BorderFactory.createEmptyBorder());
		right.add(topinfo=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);

		topinfo.setBackground(Color.GRAY);
		topinfo.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		topinfo.add(toplabel=new JLabel(""));
		Font font=toplabel.getFont();
		toplabel.setFont(new java.awt.Font(font.getFontName(),java.awt.Font.BOLD,font.getSize()+4));
		toplabel.setForeground(Color.WHITE);

		right.add(main=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		main.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		list.setSelectedIndex(0);
	}

	private void registerProcessingClasses() {
		handlers.add(new SimpleModelProcessing(owner));
		handlers.add(new XMLPreprocessing(owner));
		handlers.add(new TechnionFullProcessing(owner));
		handlers.add(new TechnionCallerProcessing(owner));
		handlers.add(new TechnionModelProcessing(owner));
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#checkData()
	 */
	@Override
	protected boolean checkData() {
		int i=list.getSelectedIndex(); if (i<0) return true;
		return handlers.get(i).checkData();
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#storeData()
	 */
	@Override
	protected void storeData() {
		int i=list.getSelectedIndex(); if (i<0) return;
		model=handlers.get(i).process();
		if (model!=null) model.prepareData();
	}

	/**
	 * Liefert das auf Basis der geladenen Daten erstellte Callcenter-Modell.
	 * @return	Neues Callcenter-Modell
	 */
	public CallcenterModel getModel() {
		return model;
	}

	private final class ListSelection implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			int i=list.getSelectedIndex(); if (i<0) return;
			main.removeAll();
			toplabel.setText(handlers.get(i).getName());
			main.add(handlers.get(i).getPanel(),BorderLayout.CENTER);

			main.revalidate();
			main.repaint();
		}
	}
}
