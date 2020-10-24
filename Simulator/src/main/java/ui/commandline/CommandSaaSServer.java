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

import language.Language;
import net.web.WebServerSystem;
import systemtools.commandline.AbstractCommand;
import tools.SetupData;

/**
 * Simulator als SaaS-HTML- und als Rechenserver starten.
 * @author Alexander Herzog
 * @see CommandLineSystem
 * @see AbstractCommand
 */
public class CommandSaaSServer extends AbstractCommand {
	/** Server-System */
	private WebServerSystem server;

	/** Sollen nur Initialisierung durchgeführt werden und kein eigentlicher Server gestartet werden? */
	private boolean isInitMode=false;

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<String>();
		list.add(Language.tr("CommandLine.SaaSServer.Name"));
		for (String s: Language.trOther("CommandLine.SaaSServer.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.SaaSServer.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.SaaSServer.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(1,1,additionalArguments); if (s!=null) return s;

		final PrintStream outFinal=out;

		server=new WebServerSystem(SetupData.getSetup().networkPermittedIPs,true,in) {
			@Override
			protected void writeConsoleOutput(String text) {
				outFinal.println(text);
			}
		};
		String param=additionalArguments[0].trim();
		if (param.equalsIgnoreCase("test") || param.equalsIgnoreCase("demo")) {
			return server.setupDemo();
		}

		if (param.equalsIgnoreCase("init")) {
			isInitMode=true;
			return server.initData();
		}

		return server.setupFromConfigFile(new File(additionalArguments[0]));
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		if (isInitMode) return; /* Nur Initialisierung durchführen, kein eigentlicher Server-Start. */
		server.run();
	}

	@Override
	public final void setQuit() {
		server.setQuit();
	}
}
