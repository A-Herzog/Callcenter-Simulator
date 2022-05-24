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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import language.Language;
import ui.HelpLink;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelExamples;

/**
 * Dialog um ein neues Modell anzulegen, in dem ein Template aufgerufen wird.
 * @author Alexander Herzog
 * @version 1.0
 */
public class NewModelDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -3959552630727959346L;

	/**
	 * Soll das Modell über den Assistenten angelegt werden?
	 * @see #getModel()
	 */
	private boolean openWizard=false;

	/** Registerkarten für die verschiedenen Arten, ein neues Modell anzulegen */
	private JTabbedPane tabs;
	/** Liste "Leeres Modell" */
	private JModelSelection empty;
	/** Liste "Beispielmodelle" */
	private JModelSelection examples;
	/** Liste "Vorlagen" */
	private JModelSelection templates;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param helpLink Verknüpfung mit der Online-Hilfe
	 */
	public NewModelDialog(final Window owner, final HelpLink helpLink) {
		super(owner,Language.tr("MainMenu.File.NewModel.Title"),null,false,helpLink.dialogNew);
		addUserButtons(
				new String[]{Language.tr("MainMenu.File.NewModel.CreateWithWizard")},
				new String[]{Language.tr("MainMenu.File.NewModel.CreateWithWizard.Tooltip")},
				new Icon[]{Images.MODEL_WIZARD.getIcon()},
				new Runnable[] {()-> {openWizard=true; closeDialog(CLOSED_BY_OK);}}
				);
		createSimpleGUI(750,550,null,null);
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		content.setLayout(new BorderLayout());
		content.add(tabs=new JTabbedPane(),BorderLayout.CENTER);

		List<CallcenterModel> models;

		models=new ArrayList<>();
		models.add(CallcenterModelExamples.getEmpty());
		if (models.size()>0) tabs.addTab(Language.tr("MainMenu.File.NewModel.Tab.Empty"),empty=new JModelSelection(models));

		models=new ArrayList<>();
		models.add(CallcenterModelExamples.getExampleSmall());
		models.add(CallcenterModelExamples.getExampleMedium());
		models.add(CallcenterModelExamples.getExampleLarge());
		models.add(CallcenterModelExamples.getExampleExtraLarge());
		models.add(CallcenterModelExamples.getExampleSmallErlang());
		models.add(CallcenterModelExamples.getExampleMediumErlang());
		models.add(CallcenterModelExamples.getExampleLargeErlang());
		if (models.size()>0) tabs.addTab(Language.tr("MainMenu.File.NewModel.Tab.Examples"),examples=new JModelSelection(models));
		models=getModelsFromFolder(new File(System.getProperty("user.dir")));
		if (models.size()>0) tabs.addTab(Language.tr("MainMenu.File.NewModel.Tab.Templates"),templates=new JModelSelection(models));
	}

	/**
	 * Liefert eine Liste aller in einem Verzeichnis vorhandenen Modelle
	 * @param folder	Zu durchsuchendes Verzeichnis
	 * @return	Liste der Modelle in dem Verzeichnis (Liste kann leer sein, ist aber nie <code>null</code>)
	 */
	private List<CallcenterModel> getModelsFromFolder(final File folder) {
		final List<CallcenterModel> models=new ArrayList<>();
		if (folder !=null && folder.isDirectory()) {
			File[] list=folder.listFiles();
			if (list!=null)	for (File file : list) {
				String extension="";
				int i=file.getName().lastIndexOf('.');
				if (i>0) extension=file.getName().substring(i+1);
				if (extension.equalsIgnoreCase("XML")) {
					CallcenterModel model=new CallcenterModel();
					if (model.loadFromFile(file)==null) models.add(model);
				}
			}
		}
		return models;
	}

	/**
	 * Wurde der Dialog mit "Ok" oder über "Modell mit Assistent anlegen" geschlossen, wird hier
	 * entweder ein Modell zurückgeliefert oder <code>null</code>, wenn das Modell über den Assistenten
	 * erstellt werden soll.
	 * @return	Entweder ein neues Modell als Vorlage oder aber <code>null</code>, wenn das Modell über den Assistenten angelegt werden soll.
	 */
	public CallcenterModel getModel() {
		if (openWizard) return null;

		switch (tabs.getSelectedIndex()) {
		case 0: return empty.getSelectedModel();
		case 1: return examples.getSelectedModel();
		case 2: return templates.getSelectedModel();
		default: return null;
		}
	}

	/**
	 * Bricht einen Text nach jeweils einer bestimmten Zeilenlänge um.
	 * @param text	Text
	 * @param lineLengt	Zeilenlänge
	 * @param wrapMarker	Am Ende jeder Zeile einzufügendes Symbol (z.B. "&lt;br&gt;")
	 * @return	Text mit Zeilenumbrüchen
	 */
	private String wrapString(final String text, final int lineLengt, final String wrapMarker) {
		final StringBuilder sb=new StringBuilder();
		StringBuilder part=new StringBuilder();

		for (int i=0;i<text.length();i++) {
			if (part.length()<lineLengt || text.charAt(i)!=' ') {part.append(text.charAt(i)); continue;}
			if (sb.length()>0) sb.append(wrapMarker);
			sb.append(part);
			part=new StringBuilder();
		}

		if (part.length()>0) {
			if (sb.length()>0) sb.append(wrapMarker);
			sb.append(part);
		}

		return sb.toString();
	}

	/**
	 * Darstellung einer Liste von Callcenter-Modellen inkl.
	 * der Anzeige einer Beschreibung zu dem jeweils gewählten Modell.
	 */
	private class JModelSelection extends JPanel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 4412860965612077368L;

		/**
		 * Liste der verfügbaren Callcenter-Modelle
		 */
		private final JList<CallcenterModel> list;

		/**
		 * Beschreibung des aktuell gewählten Modells
		 */
		private final JTextArea description;

		/**
		 * Konstruktor der Klasse
		 * @param models	In der Liste anzuzeigende Modelle
		 */
		public JModelSelection(final List<CallcenterModel> models) {
			super(new BorderLayout());

			JSplitPane splitPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			add(splitPane,BorderLayout.CENTER);
			splitPane.setContinuousLayout(true);

			DefaultListModel<CallcenterModel> listData=new DefaultListModel<>();
			for (CallcenterModel model: models) listData.addElement(model);
			JScrollPane listScroll;
			splitPane.setLeftComponent(listScroll=new JScrollPane(list=new JList<>(listData)));
			list.setCellRenderer(new ModelListRenderer());
			list.addListSelectionListener(new ModelSelectionListener());
			list.addMouseListener(new ModelSelectionListener());

			splitPane.setRightComponent(new JScrollPane(description=new JTextArea()));
			description.setEditable(false);
			description.setWrapStyleWord(true);

			Dimension d=listScroll.getMinimumSize();
			d.width=Math.max(d.width,250);
			listScroll.setMinimumSize(d);

			list.setSelectedIndex(0);
		}

		/**
		 * Liefert das aktuell in der Liste gewählte Callcenter-Modell zurück.
		 * @return	Aktuell in der Liste gewähltes Callcenter-Modell
		 */
		public CallcenterModel getSelectedModel() {
			return list.getSelectedValue();
		}

		/**
		 * Renderer für die Einträge in {@link JModelSelection#list}
		 */
		private class ModelListRenderer extends AdvancedListCellRenderer {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = 6499061601895830242L;

			/**
			 * Konstruktor der Klasse
			 */
			public ModelListRenderer() {
				/*
				 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
				 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
				 */
			}

			@Override
			protected void buildString(Object value, int index, StringBuilder s) {
				if (!(value instanceof CallcenterModel)) {
					s.append(index);
					return;
				}

				CallcenterModel model=(CallcenterModel)value;

				s.append("<b>"+wrapString(model.name,25,"<br>")+"</b>");
				s.append("<br>");
				if (model.caller.size()==1) {
					s.append(model.caller.size()+" "+Language.tr("Model.GenerateDescription.ClientType.Single"));
				} else {
					s.append(model.caller.size()+" "+Language.tr("Model.GenerateDescription.ClientType.Multiple"));
				}
				s.append("<br>");
				if (model.callcenter.size()==1) {
					s.append(model.callcenter.size()+" "+Language.tr("Model.GenerateDescription.Callcenter.Single"));
				} else {
					s.append(model.callcenter.size()+" "+Language.tr("Model.GenerateDescription.Callcenter.Multiple"));
				}
				s.append("<br>");
				if (model.skills.size()==1) {
					s.append(model.skills.size()+" "+Language.tr("Model.GenerateDescription.SkillLevel.Single"));
				} else {
					s.append(model.skills.size()+" "+Language.tr("Model.GenerateDescription.SkillLevel.Multiple"));
				}
			}

			@Override
			protected Icon getIcon(Object value) {
				return Images.GENERAL_SYMBOL_BIG.getIcon();
			}
		}

		/**
		 * Reagiert auf eine veränderte Auswahl in {@link JModelSelection#list}
		 * sowie auf Doppelklicks auf Einträge in der Liste.
		 * @see JModelSelection#list
		 */
		private class ModelSelectionListener implements ListSelectionListener, MouseListener {
			/**
			 * Konstruktor der Klasse
			 */
			public ModelSelectionListener() {
				/*
				 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
				 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
				 */
			}

			@Override
			public void valueChanged(ListSelectionEvent e) {
				CallcenterModel model=list.getSelectedValue();
				if (model==null) return;
				StringBuilder sb=new StringBuilder();
				for (String line : model.description.split("\n")) sb.append(wrapString(line,40,"\n")+"\n");
				description.setText(sb.toString());
				description.setSelectionStart(0);
				description.setSelectionEnd(0);
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()==2) closeDialog(CLOSED_BY_OK);
			}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}
		}
	}
}
