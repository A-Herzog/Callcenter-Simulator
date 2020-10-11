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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import tools.SetupData;
import ui.MainFrame;
import ui.MainPanel;
import ui.UpdateSystem;
import ui.images.Images;

/**
 * Versionsinfo-Dialog
 * @author Alexander Herzog
 * @version 1.0
 */
public class InfoDialog extends JDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4544783238672067726L;

	/**
	 * "Ok"-Schaltfläche
	 */
	private final JButton okButton;
	/**
	 * "Versionsgeschichte"-Schaltfläche
	 * @see #showVersionHistory
	 */
	private final JButton versionHistoryButton;

	/**
	 * "Lizenzen"-Schaltfläche
	 * @see #showLicenses
	 */
	private final JButton licenseButton;

	/**
	 * Auswahlfeld zum Ändern der Programmsprache
	 */
	private final JComboBox<String> languages;

	/**
	 * Dieses Feld wird auf <code>true</code> gesetzt, wenn der Dialog nicht über "Ok"
	 * oder das Schließen-Feld geschlossen wurde, sondern der Nutzer angeklickt hat,
	 * dass die Versionsgeschichte aufgerufen werden soll. Der Aufrufer der Dialogs
	 * muss auf dieses Feld achten.
	 */
	public boolean showVersionHistory;

	/**
	 * Dieses Feld wird auf <code>true</code> gesetzt, wenn der Dialog nicht über "Ok"
	 * oder das Schließen-Feld geschlossen wurde, sondern der Nutzer angeklickt hat,
	 * dass die Lizenzinformationen aufgerufen werden soll. Der Aufrufer der Dialogs
	 * muss auf dieses Feld achten.
	 */
	public boolean showLicenses;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param version	Anzuzeigende Versionsnummer
	 */
	public InfoDialog(final Window owner, final String version) {
		super(owner,Language.tr("InfoDialog.Title"),Dialog.ModalityType.APPLICATION_MODAL);
		setLayout(new BorderLayout());

		JPanel mainarea,p,p2,p3;
		JLabel image;

		add(mainarea=new JPanel(),BorderLayout.CENTER);
		mainarea.setLayout(new BoxLayout(mainarea,BoxLayout.Y_AXIS));

		/* Bild anzeigen */
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.CENTER))); p.add(image=new JLabel());
		image.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		final ExecutorService executor=new ThreadPoolExecutor(0,1,1,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(1));
		executor.execute(new FutureTask<Integer>(()->{
			final URL url=MainPanel.class.getResource("res/CCS.png");
			if (url!=null) image.setIcon(new ImageIcon(url)); else image.setVisible(false);
			return null;
		}));

		/* Text anzeigen */
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT))); p.add(p2=new JPanel());
		p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));

		/* Programm und Autor */
		List<String> text=new ArrayList<String>();
		text.add(MainFrame.PROGRAM_NAME+" "+version);
		text.add("&copy; "+MainPanel.AUTHOR+" (<a href=\"mailto:"+UpdateSystem.mailURL+"\">"+UpdateSystem.mailURL+"</a>)");

		text.add("");

		/* Java-Version */
		text.add(Language.tr("InfoDialog.JavaVersion")+": "+System.getProperty("java.version")+" ("+System.getProperty("java.vm.name")+")");
		text.add(Language.tr("InfoDialog.Is64Bit")+": "+(System.getProperty("os.arch").contains("64")?Language.tr("InfoDialog.Is64Bit.Yes"):Language.tr("InfoDialog.Is64Bit.No")));

		/* Speicherverbrauch */
		final long l1=ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		final long l2=ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
		text.add(Language.tr("InfoDialog.MemoryUsage")+": "+NumberTools.formatLong((l1+l2)/1024/1024)+" MB");
		text.add(Language.tr("InfoDialog.MemoryAvailable")+": "+NumberTools.formatLong(Runtime.getRuntime().maxMemory()/1024/1024)+" MB");

		/* Ausgabe */
		StringBuilder s=new StringBuilder();
		s.append("<html><body style=\"margin: 0px; padding: 0px; font-family: sans;  background-color: transparent;\"><p style=\"margin-top: 0px; font-weight: bold; font-size: larger;\">"+text.get(0)+"</p><p style=\"margin-top: 5px; margin-bottom: 5px;\">"+text.get(1)+"</p><p style=\"margin-top: 0px; font-size: 9pt;\">");
		for (int i=2;i<text.size();i++) {if (i>2) s.append("<br>");	s.append(text.get(i));}
		s.append("</p></body></html>");
		final String htmlInfoText=s.toString();

		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JTextPane label=new JTextPane();

		label.setContentType("text/html");
		label.setText(htmlInfoText);
		label.setEditable(false);
		label.setOpaque(false);
		p3.add(label);
		label.addHyperlinkListener(new LinkListener());

		mainarea.add(Box.createVerticalGlue());

		add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		/* Ok-Button */
		p.add(okButton=new JButton("Ok"));
		okButton.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {
			if (languages!=null) {
				String lang=(languages.getSelectedIndex()==1)?"de":"en";
				SetupData setup=SetupData.getSetup();
				if (!setup.language.equals(lang)) {
					setup.language=lang;
					setup.saveSetupWithWarning(InfoDialog.this);
				}
			}
			setVisible(false);
			dispose();
		}});
		okButton.setIcon(Images.MSGBOX_OK.getIcon());
		getRootPane().setDefaultButton(okButton);

		/* Version history */
		p.add(versionHistoryButton=new JButton(Language.tr("InfoDialog.ShowVersionHistory")));
		versionHistoryButton.setToolTipText(Language.tr("InfoDialog.ShowVersionHistory.Tooltip"));
		versionHistoryButton.addActionListener((e)->{showVersionHistory=true; setVisible(false); dispose();});
		versionHistoryButton.setIcon(Images.GENERAL_CHANGELOG.getIcon());

		/* Lizenzen */
		p.add(licenseButton=new JButton(Language.tr("InfoDialog.ShowLicenses")));
		licenseButton.setToolTipText(Language.tr("InfoDialog.ShowLicenses.Tooltip"));
		licenseButton.addActionListener((e)->{showLicenses=true; setVisible(false); dispose();});
		licenseButton.setIcon(Images.GENERAL_LICENSE.getIcon());

		/* Sprachschalter */
		p.add(Box.createHorizontalStrut(5));
		p.add(new JLabel(Language.tr("SettingsDialog.Languages")+":"));
		p.add(languages=new JComboBox<String>(new String[]{Language.tr("SettingsDialog.Languages.English"),Language.tr("SettingsDialog.Languages.German")}));
		languages.setRenderer(new IconListCellRenderer(new Images[]{Images.LANGUAGE_EN,Images.LANGUAGE_DE}));
		languages.setToolTipText(Language.tr("SettingsDialog.Languages.Info"));
		SetupData setup=SetupData.getSetup();
		if (setup.language==null || setup.language.isEmpty() || setup.language.equalsIgnoreCase("de")) languages.setSelectedIndex(1); else languages.setSelectedIndex(0);

		addWindowListener(new WindowAdapter() {@Override public void windowClosing(WindowEvent event) {setVisible(false); dispose();}});
		setResizable(false);
		setSize(550,650);
		SwingUtilities.invokeLater(()->pack());
	}

	@Override
	protected JRootPane createRootPane() {
		JRootPane rootPane=new JRootPane();
		KeyStroke stroke=KeyStroke.getKeyStroke("ESCAPE");
		InputMap inputMap=rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(stroke,"ESCAPE");
		rootPane.getActionMap().put("ESCAPE",new CloseListener());
		return rootPane;
	}

	/**
	 * Wird ausgelöst, wenn der Dialog geschlossen werden soll.
	 * @see InfoDialog#createRootPane()
	 */
	private class CloseListener extends AbstractAction {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -485008309903554823L;
		@Override
		public void actionPerformed(ActionEvent actionEvent) {setVisible(false); dispose();}
	}

	/**
	 * Wird ausgelöst, wenn in dem Infotext auf die E-Mail-Adresse geklickt wird.
	 */
	private class LinkListener implements HyperlinkListener {
		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED && e.getURL().toString().toLowerCase().startsWith("mailto:")) {
				try {Desktop.getDesktop().mail(e.getURL().toURI());} catch (IOException | URISyntaxException e1) {
					MsgBox.error(InfoDialog.this,Language.tr("Window.Info.NoEMailProgram.Title"),String.format(Language.tr("Window.Info.NoEMailProgram.Info"),e.getURL().toString()));
				}
			}
		}
	}
}
