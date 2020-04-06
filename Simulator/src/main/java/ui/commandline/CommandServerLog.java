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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import language.Language;

/**
 * Simulator als Rechenserver starten (Statusausgaben in Datei speichern).
 * @author Alexander Herzog
 * @see CommandLineSystem
 * @see CommandServer
 */
public final class CommandServerLog extends CommandServer {
	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<String>();
		list.add(Language.tr("CommandLine.LogFileServer.Name"));
		for (String s: Language.trOther("CommandLine.LogFileServer.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.LogFileServer.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.LogFileServer.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(1,4,additionalArguments); if (s!=null) return s;

		serverLogFile=new File(additionalArguments[0]);
		if (serverLogFile.isDirectory()) return String.format(Language.tr("CommandLine.Error.File.LogFileExistsAsDirectory"),serverLogFile.toString());
		if (serverLogFile.exists()) {
			if (!serverLogFile.canWrite()) return String.format(Language.tr("CommandLine.Error.File.CannotWriteLogFile"),serverLogFile.toString());
		} else {
			try {
				if (!serverLogFile.createNewFile()) return String.format(Language.tr("CommandLine.Error.File.CannotCreateLogFile"),serverLogFile.toString());
			} catch (IOException e) {
				return String.format(Language.tr("CommandLine.Error.File.CannotCreateLogFile"),serverLogFile.toString());
			}
		}

		return prepareServerParameter(additionalArguments,1,in,out);
	}
}
