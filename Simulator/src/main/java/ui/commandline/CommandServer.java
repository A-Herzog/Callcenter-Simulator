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
package ui.commandline;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import language.Language;
import mathtools.NumberTools;
import net.web.WebServerSystem;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import tools.SetupData;

/**
 * Simulator als Rechenserver starten.
 * @author Alexander Herzog
 * @see CommandLineSystem
 * @see AbstractCommand
 */
public class CommandServer extends AbstractCommand {
	/** Server-Port */
	private int serverPort=6783;
	/** Server-Passwort */
	private String serverPassword="";
	/** Maximal Anzahl an Threads pro Anfrage */
	private int serverMaxThreads=0;

	/**
	 * Logfile für den Server
	 */
	protected File serverLogFile=null;

	/** Server-System */
	private WebServerSystem server;

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandServer(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.Server.Name"));
		for (String s: Language.trOther("CommandLine.Server.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(String[]::new);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.Server.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.Server.Description.Long").split("\n");
	}

	/**
	 * Wird von {@link #prepare(String[], InputStream, PrintStream)} zur Interpretation
	 * der Argumente verwendet.
	 * @param additionalArguments	An den Befehl übergebene Argumente
	 * @param shift	Ab welchem Eintrag sollen die Werte im <code>additionalArgumente</code>-Array verwendet werden?
	 * @param in	Ein {@link InputStream}-Objekt oder <code>null</code>, über das Zeichen von der Konsole gelesen werden können (<code>null</code>, wenn keine Konsole verfügbar ist)
	 * @param out	Ein {@link PrintStream}-Objekt, über das Texte ausgegeben werden können.
	 * @return	Gibt <code>null</code> zurück, wenn die Vorbereitung erfolgreich war, sonst eine Fehlermeldung.
	 */
	protected final String prepareServerParameter(String[] additionalArguments, int shift, InputStream in, PrintStream out) {
		Integer I;
		if (additionalArguments.length>=1+shift) {
			String portData=additionalArguments[0+shift];
			I=NumberTools.getNotNegativeInteger(portData);
			if (I==null) return String.format(Language.tr("CommandLine.Server.InvalidPort"),portData);
		} else {
			if (out!=System.out || in==null) {
				return String.format(Language.tr("CommandLine.Server.InvalidPort"),"");
			}
			out.println(Language.tr("CommandLine.Server.EnterPortNumber"));
			@SuppressWarnings("resource") /* Wir dürfen scanner.close(); nicht aufrufen, sonst später nicht mehr auf Eingaben (zum Beenden des Servers) reagiert werden. */
			Scanner scanner=new Scanner(in);
			I=NumberTools.getNotNegativeInteger(scanner.next());
			if (I==null || I==0) return Language.tr("CommandLine.Server.InvalidPort.Short");
		}
		serverPort=I;

		if (additionalArguments.length>=2+shift) {
			I=NumberTools.getNotNegativeInteger(additionalArguments[1+shift]);
			if (I==null) return String.format(Language.tr("CommandLine.SaaSServer.InvalidMaximumNumberOfThreads"),additionalArguments[1+shift]);
		}
		serverMaxThreads=I;

		if (additionalArguments.length>=3+shift) {
			serverPassword=additionalArguments[2+shift];
		}

		return null;
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(0,3,additionalArguments); if (s!=null) return s;
		return prepareServerParameter(additionalArguments,0,in,out);
	}

	@Override
	public final void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		final PrintStream outFinal=out;
		server=new WebServerSystem(SetupData.getSetup().networkPermittedIPs,true,in) {
			@Override
			protected void writeConsoleOutput(String text) {
				outFinal.println(text);
			}
		};
		server.setupLogFile(serverLogFile);
		server.setupCalcServer(serverPort,serverPassword,serverMaxThreads);
		server.run();
	}

	@Override
	public final void setQuit() {
		server.setQuit();
	}
}
