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
package start;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import language.Language;
import language.LanguageStaticLoader;
import language.Messages_Java11;
import mathtools.Table;
import systemtools.GUITools;
import systemtools.MsgBox;
import systemtools.MsgBoxBackendTaskDialog;
import systemtools.statistics.PDFWriter;
import tools.SetupData;
import ui.MainFrame;
import ui.UpdateSystem;
import ui.VersionConst;
import ui.commandline.CommandLineSystem;
import ui.statistic.core.SpeedUpJFreeChart;
import xml.XMLTools;

/**
 * Main-Klasse des Simulators
 * Der Simulator kann über diese Klasse sowohl im GUI- als auch im Kommandozeilen-Modus gestartet werden.
 * @author Alexander Herzog
 */
public final class Main {
	private static File loadFile;

	private static String[] processConfigFileParameter(String[] args) {
		if (args.length==0 || !args[0].toLowerCase().startsWith("cfg=")) return args;

		String cfg=args[0].substring(4);
		if (cfg.startsWith("\"") && cfg.endsWith("\"")) cfg=cfg.substring(1,cfg.length()-1);

		File cfgFile=new File(cfg);
		if (!cfgFile.getParentFile().exists()) return args;
		if (cfgFile.isDirectory()) return args;

		SetupData.userConfigFile=cfgFile;
		List<String> argsList=new ArrayList<String>(Arrays.asList(args));
		argsList.remove(0);
		args=argsList.toArray(new String[0]);
		return args;
	}

	/**
	 * Hauptroutine des gesamten Programms
	 * @param args	Kommandozeilen-Parameter
	 */
	public static void main(String[] args) {
		args=processConfigFileParameter(args);

		/* Cache-Ordner für PDFWriter einstellen */
		PDFWriter.cacheFolder=SetupData.getSetupFolder();

		/* Programmname für Tabellenexport */
		Table.ExportTitle=MainFrame.PROGRAM_NAME;

		/* Sprache */
		Language.init(SetupData.getSetup().language);
		LanguageStaticLoader.setLanguage();
		if (Messages_Java11.isFixNeeded()) Messages_Java11.setupMissingSwingMessages();

		/* Basiseinstellungen zu den xml-Dateiformaten */
		XMLTools.homeURL=UpdateSystem.homeURL;
		XMLTools.mediaURL="https://"+XMLTools.homeURL+"/Media/";
		XMLTools.dtd=Language.tr("XML.DTD");
		XMLTools.xsd=Language.tr("XML.XSD");

		/* Parameter */
		if (args.length>0) {
			CommandLineSystem commandLineSimulator=new CommandLineSystem();
			loadFile=commandLineSimulator.checkLoadFile(args);
			if (loadFile==null) {if (commandLineSimulator.run(args)) return;}
		}

		/* Updatesystem */
		if (loadFile==null) {
			final UpdateSystem update=UpdateSystem.getUpdateSystem();
			if (update.runUpdate()) return;
			new Thread() {@Override
				public void run() {
				try {Thread.sleep(1500);} catch (InterruptedException e) {}
				update.checkUpdate(false);
			}}.start();
		}

		/* Grafische Oberfläche verfügbar? */
		if (!GUITools.isGraphicsAvailable()) return;

		/* Grafische Oberfläche starten */
		SwingUtilities.invokeLater(new RunSimulator());
	}

	private static final class RunSimulator implements Runnable {
		@Override
		public void run() {
			SetupData setup=SetupData.getSetup();
			GUITools.setupUI();
			GUITools.setupFontSize(setup.scaleGUI);
			MsgBox.setBackend(new MsgBoxBackendTaskDialog());
			new MainFrame(VersionConst.version,loadFile);
			new SpeedUpJFreeChart();
		}
	}
}
