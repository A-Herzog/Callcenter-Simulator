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
package net.calc;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import net.web.WebServerSystem;
import systemtools.MsgBox;
import tools.SetupData;
import ui.HelpLink;
import ui.images.Images;
import ui.specialpanels.JWorkPanel;

/**
 * Panel, welches den Betrieb des Simulationsservers per GUI ermöglicht
 * @author Alexander Herzog
 * @version 1.0
 */
public final class SimServerPanel extends JWorkPanel {
	private static final long serialVersionUID = -1719057517168514353L;

	private final Window owner;

	private final JCheckBox serverSimulation;
	private final JTextField portSimulation;
	private final JTextField passwordSimulation;
	private final JTextArea statusField;
	private final JCheckBox serverSaaS;
	private final JTextField portSaaS;
	private final JCheckBox serverSaaSStatistic;
	private final JTextField folderSaaSStatistic;
	private final JButton folderSaaSStatisticButton;
	private final JCheckBox serverSaaSApplet;
	private final JTextField fileSaaSApplet;
	private final JButton fileSaaSAppletButton;
	private final FileDropper drop1, drop2;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param doneNotify	Callback, das aufgerufen wird, wenn das Panel geschlossen werden soll
	 * @param helpLink	Help-Link
	 */
	public SimServerPanel(final Window owner, final Runnable doneNotify, final HelpLink helpLink) {
		super(doneNotify,helpLink.pageServer);

		this.owner=owner;

		JPanel main,p;

		/* Main area */
		add(main=new JPanel(new BorderLayout()),BorderLayout.CENTER);

		JPanel config=new JPanel();
		config.setLayout(new BoxLayout(config,BoxLayout.Y_AXIS));
		main.add(config,BorderLayout.NORTH);

		config.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(serverSimulation=new JCheckBox(Language.tr("Server.Type.Calc"),true));
		serverSimulation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileSaaSApplet.setEditable(serverSaaS.isSelected() && serverSimulation.isSelected());
				fileSaaSAppletButton.setEnabled(serverSaaS.isSelected() && serverSimulation.isSelected());
				serverSaaSApplet.setEnabled(serverSimulation.isSelected());
			}
		});
		p.add(new JLabel(Language.tr("Server.Port")+":"));
		p.add(portSimulation=new JTextField(5));
		portSimulation.setText(""+SetupData.getSetup().networkServerPort);
		p.add(new JLabel(Language.tr("Server.Password")+":"));
		p.add(passwordSimulation=new JTextField(20));
		passwordSimulation.setText(SetupData.getSetup().networkServerPassword);

		config.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(serverSaaS=new JCheckBox(Language.tr("Server.Type.Web"),true));
		serverSaaS.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				serverSaaSStatistic.setEnabled(serverSaaS.isSelected());
				folderSaaSStatistic.setEditable(serverSaaS.isSelected());
				folderSaaSStatisticButton.setEnabled(serverSaaS.isSelected());
				serverSaaSApplet.setEnabled(serverSaaS.isSelected());
				fileSaaSApplet.setEditable(serverSaaS.isSelected() && serverSimulation.isSelected());
				fileSaaSAppletButton.setEnabled(serverSaaS.isSelected() && serverSimulation.isSelected());
			}
		});
		p.add(new JLabel(Language.tr("Server.Port")+":"));
		p.add(portSaaS=new JTextField(5));
		portSaaS.setText(""+SetupData.getSetup().networkServerPortWeb);

		config.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(serverSaaSStatistic=new JCheckBox(Language.tr("Server.StatisticFolder"),true));
		p.add(folderSaaSStatistic=new JTextField(50));
		folderSaaSStatistic.setText(System.getProperty("user.home")+File.separator+"Desktop");
		drop1=new FileDropper(folderSaaSStatistic,new ButtonListener());

		p.add(folderSaaSStatisticButton=new JButton(Language.tr("Server.StatisticFolder.Button")));
		folderSaaSStatisticButton.addActionListener(new ButtonListener());
		folderSaaSStatisticButton.setToolTipText(Language.tr("Server.StatisticFolder.Button.Info"));
		folderSaaSStatisticButton.setIcon(Images.GENERAL_SELECT_FOLDER.getIcon());

		config.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(serverSaaSApplet=new JCheckBox(Language.tr("Server.OfferApplet"),true));
		p.add(fileSaaSApplet=new JTextField(50));
		fileSaaSApplet.setText("");
		drop2=new FileDropper(fileSaaSApplet,new ButtonListener());

		p.add(fileSaaSAppletButton=new JButton(Language.tr("Server.OfferApplet.Button")));
		fileSaaSAppletButton.addActionListener(new ButtonListener());
		fileSaaSAppletButton.setToolTipText(Language.tr("Server.OfferApplet.Button.Info"));
		fileSaaSAppletButton.setIcon(Images.GENERAL_SELECT_FILE.getIcon());

		main.add(p=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		p.add(new JScrollPane(statusField=new JTextArea(10,80)));
		statusField.setEditable(false);

		/* Bottom line */
		addFooter(Language.tr("Server.Button.StartServer"),Images.SERVER_CALC_RUN.getIcon(),Language.tr("Server.Button.StopServer"));

		addFooterButton(Language.tr("Server.Button.ShowInBrowser"),Images.STATISTICS_SHOW_WEBVIEWER.getURL());
		getFooterButton(0).setVisible(false);
		getFooterButton(0).setToolTipText(Language.tr("Server.Button.ShowInBrowser.Info"));
	}

	@Override
	protected void userButtonClick(int index, JButton button) {
		String port="";
		final SetupData setup=SetupData.getSetup();
		if (!setup.networkServerPortWeb.equals("80")) port=":"+setup.networkServerPortWeb;
		try {
			Desktop.getDesktop().browse(new URI("http://localhost"+port));
		} catch (IOException | URISyntaxException e) {}
	}

	private boolean check(boolean showErrorMessages) {
		boolean ok=true;
		SetupData setup=SetupData.getSetup();

		if (serverSimulation.isSelected()) {
			Integer I=NumberTools.getNotNegativeInteger(portSimulation.getText());
			if (I!=null && I!=0) {
				setup.networkServerPort=portSimulation.getText();
			} else {
				ok=false;
				if (showErrorMessages) MsgBox.error(this,Language.tr("Server.Error.Port.Title"),String.format(Language.tr("Server.Error.Port.Info"),portSimulation.getText()));
			}

			setup.networkServerPassword=passwordSimulation.getText().trim();
		}

		if (serverSaaS.isSelected()) {
			Integer I=NumberTools.getNotNegativeInteger(portSaaS.getText());
			if (I!=null && I!=0) {
				setup.networkServerPortWeb=portSaaS.getText();
			} else {
				ok=false;
				if (showErrorMessages) MsgBox.error(this,Language.tr("Server.Error.Port.Title"),String.format(Language.tr("Server.Error.Port.Info"),portSaaS.getText()));
			}

			if (serverSaaSStatistic.isSelected()) {
				File folder=new File(folderSaaSStatistic.getText());
				if (!folder.isDirectory()) {
					ok=false;
					if (showErrorMessages) MsgBox.error(this,Language.tr("Server.Error.StatisticFolder.Title"),String.format(Language.tr("Server.Error.StatisticFolder.Info"),folderSaaSStatistic.getText()));
				}
			}
		}

		if (showErrorMessages) {
			/* Start des Severs, hier keine Speicher-Fehlermeldungen */
			setup.saveSetup();
		} else {
			setup.saveSetupWithWarning(this);
		}

		return ok;
	}

	@Override
	protected void done() {
		check(false);
		super.done();
	}

	@Override
	protected void setWorkMode(boolean running) {
		super.setWorkMode(running);
		serverSimulation.setEnabled(!running);
		portSimulation.setEditable(!running);
		passwordSimulation.setEditable(!running);
		serverSaaS.setEnabled(!running);
		portSaaS.setEditable(!running);
		serverSaaSStatistic.setEnabled(!running && serverSaaS.isSelected());
		folderSaaSStatistic.setEditable(!running && serverSaaS.isSelected());
		folderSaaSStatisticButton.setEnabled(!running && serverSaaS.isSelected());
		serverSaaSApplet.setEnabled(!running && serverSaaS.isSelected() && serverSimulation.isSelected());
		fileSaaSApplet.setEditable(!running && serverSaaS.isSelected() && serverSimulation.isSelected());
		fileSaaSAppletButton.setEnabled(!running && serverSaaS.isSelected() && serverSimulation.isSelected());
		getFooterButton(0).setVisible(running && serverSaaS.isSelected());
	}

	private void addStatusLine(String text) {
		statusField.setText(statusField.getText()+text);
	}

	@Override
	protected void run() {
		if (!check(true)) return;

		statusField.setText("");
		cancelWork=false;
		setWorkMode(true);

		new Thread("SimServer") {
			@Override
			public void run() {
				boolean ok=true;
				final SetupData setup=SetupData.getSetup();
				WebServerSystem server=new WebServerSystemPanel();
				if (serverSimulation.isSelected()) {
					server.setupCalcServer(
							NumberTools.getNotNegativeInteger(setup.networkServerPort),
							setup.networkServerPassword,
							Math.max(0,setup.networkMaxThreads)
							);
				}
				if (serverSaaS.isSelected()) {
					ok=ok && server.setupWebServer(
							NumberTools.getNotNegativeInteger(setup.networkServerPortWeb),
							(serverSimulation.isSelected() && serverSaaSApplet.isSelected())?NumberTools.getNotNegativeInteger(setup.networkServerPort):0,
									new File(fileSaaSApplet.getText()),
									setup.networkServerPassword,
									(serverSaaSStatistic.isSelected())?new File(folderSaaSStatistic.getText()):null
							);
				}
				if (ok) server.run();
				setWorkMode(false);
			}
		}.start();
	}

	private final class WebServerSystemPanel extends WebServerSystem {
		public WebServerSystemPanel() {
			super(SetupData.getSetup().networkPermittedIPs,false,null);
		}

		@Override
		protected boolean isQuit() {
			return super.isQuit() || cancelWork;
		}

		@Override
		protected void writeConsoleOutput(String text) {
			addStatusLine(text);
		}
	}

	private final String selectFolder() {
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(Language.tr("Server.StatisticFolder.Button"));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		return file.toString();
	}

	private final String selectFile() {
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(Language.tr("Server.OfferApplet.Button"));
		FileFilter jar=new FileNameExtensionFilter(Language.tr("FileType.jar")+" (*.jar)","jar");
		fc.addChoosableFileFilter(jar);
		fc.setFileFilter(jar);
		if (fc.showOpenDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		return file.toString();
	}

	private final class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==folderSaaSStatisticButton) {String s=selectFolder(); if (s!=null) folderSaaSStatistic.setText(s); return;}
			if (e.getSource()==fileSaaSAppletButton) {String s=selectFile(); if (s!=null) fileSaaSApplet.setText(s); return;}
			if (e.getSource() instanceof FileDropperData) {
				final FileDropperData data=(FileDropperData)e.getSource();
				final File file=data.getFile();

				if (data.getFileDropper()==drop1) {
					if (file.isDirectory()) {folderSaaSStatistic.setText(file.toString()); data.dragDropConsumed();}
					return;
				}
				if (data.getFileDropper()==drop2) {
					if (file.isFile()) {fileSaaSApplet.setText(file.toString()); data.dragDropConsumed();}
					return;
				}
			}
		}
	}
}