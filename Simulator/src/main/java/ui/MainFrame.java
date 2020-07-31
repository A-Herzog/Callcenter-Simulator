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
package ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;

import language.Language;
import language.LanguageStaticLoader;
import language.Messages_Java11;
import systemtools.MainFrameBase;
import systemtools.MainPanelBase;
import systemtools.MsgBox;
import tools.SetupData;
import tools.SetupData.StartMode;

/**
 * Hauptfenster des gesamten Callcenter-Simulator-Systems
 * @author Alexander Herzog
 * @version 1.0
 */
public class MainFrame extends MainFrameBase {
	private static final long serialVersionUID = -705771374983903700L;

	/**
	 * Programmname
	 */
	public static final String PROGRAM_NAME="Callcenter Simulator";

	/**
	 * Ressourcen-URL für das Taskleisten-Symbol für das Programm
	 */
	public static final URL ICON_URL=MainFrame.class.getResource("res/Symbol.png");

	/**
	 * Konstruktor der Klasse <code>CallcenterSimulatorFrame</code>
	 * @param version	Im Info-Dialog anzuzeigende Versionsnummer
	 * @param loadFile	Modell oder Statistik, die beim Start geladen werden soll. Wird <code>null</code> übergeben, so wird kein Modell geladen.
	 */
	public MainFrame(final String version, final File loadFile) {
		super(PROGRAM_NAME,loadFile);

		final MainPanelBase panel=new MainPanel(this,PROGRAM_NAME,false);
		setMainPanel(panel);
		if (panel instanceof MainPanel) ((MainPanel)getMainPanel()).setReloadWindow(new ReloadWindow());
		setIcon(ICON_URL);
		setVisible(true);
	}

	private static Dimension minMainWindowSize=new Dimension(1025,700);

	private Dimension getScaledDefaultSize(double scale) {
		final Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		return new Dimension(Math.min(screenSize.width-50,(int)Math.round(minMainWindowSize.width*scale)),Math.min(screenSize.height-50,(int)Math.round(minMainWindowSize.height*scale)));
	}

	@Override
	protected void loadWindowSize() {
		setSize(getScaledDefaultSize(SetupData.getSetup().scaleGUI));
		setMinimumSize(getSize());
		setLocationRelativeTo(null);

		final Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		final SetupData setup=SetupData.getSetup();
		switch (setup.startSizeMode) {
		case START_MODE_FULLSCREEN:
			setExtendedState(Frame.MAXIMIZED_BOTH);
			break;
		case START_MODE_LASTSIZE:
			setExtendedState(setup.lastSizeMode);
			if (setup.lastSizeMode==Frame.NORMAL) {
				final Dimension minSize=getMinimumSize();
				Dimension d=setup.lastSize;
				if (d.width<minSize.width) d.width=minSize.width;
				if (d.width>screenSize.width) d.width=screenSize.width;
				if (d.height<minSize.height) d.height=minSize.height;
				if (d.height>screenSize.height) d.height=screenSize.height;
				setSize(d);

				Point point=setup.lastPosition;
				if (point.x<0) point.x=0;
				if (point.x>=screenSize.width-50) point.x=screenSize.width-50;
				if (point.y<0) point.y=0;
				if (point.y>=screenSize.height-50) point.y=screenSize.height-50;
				setLocation(point);
			}
			break;
		case START_MODE_DEFAULT:
			/* Keine Verarbeitung notwendig. */
			break;
		}
	}

	@Override
	protected void saveWindowSize() {
		SetupData setup=SetupData.getSetup();
		if (setup.startSizeMode==StartMode.START_MODE_LASTSIZE) {
			setup.lastSizeMode=getExtendedState();
			setup.lastPosition=getLocation();
			setup.lastSize=getSize();
			setup.saveSetupWithWarning(this);
		}
	}

	@Override
	protected void logException(final String info) {
		SetupData setup=SetupData.getSetup();
		setup.lastError=info;
		setup.saveSetup();
	}

	private class ReloadWindow implements Runnable {
		@Override
		public void run() {
			if (!(getMainPanel() instanceof MainPanel)) return;

			final Object[] store=((MainPanel)getMainPanel()).getAllData();

			Language.init(SetupData.getSetup().language);
			LanguageStaticLoader.setLanguage();
			if (Messages_Java11.isFixNeeded()) Messages_Java11.setupMissingSwingMessages();

			final MainPanel newMainPanel=new MainPanel(MainFrame.this,PROGRAM_NAME,true);
			setMainPanel(newMainPanel);
			newMainPanel.setReloadWindow(new ReloadWindow());
			newMainPanel.setAllData(store);
		}
	}

	@Override
	protected boolean exitProgramOnCloseWindow() {
		final SetupData setup=SetupData.getSetup();
		final File setupFile=new File(SetupData.getSetupFolder(),SetupData.SETUP_FILE_NAME);

		boolean b=setup.isLastFileSaveSuccessful();
		while (!b) {
			b=setup.saveSetup();
			if (!b) {
				if (!MsgBox.confirm(this,Language.tr("SetupFailure.Title"),String.format(Language.tr("SetupFailure.Info"),setupFile.toString()),Language.tr("SetupFailure.Retry"),Language.tr("SetupFailure.Discard"))) break;
			}
		}

		return true;
	}
}
