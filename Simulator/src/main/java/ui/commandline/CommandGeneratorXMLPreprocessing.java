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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import language.Language;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.dataloader.XMLPreprocessor;

/**
 * Fügt Zellen aus einer Tabelle in eine XML-Datei ein.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandGeneratorXMLPreprocessing extends AbstractCommand {
	/** Eingabe-xml-Datei */
	private File xmlFile;
	/** Eingabetabelle */
	private File tableFile;
	/** Ausgabe-xml-Datei */
	private File outputFile;

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandGeneratorXMLPreprocessing(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.GeneratorXMLPreprocessing.Name"));
		for (String s: Language.trOther("CommandLine.GeneratorXMLPreprocessing.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.GeneratorXMLPreprocessing.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.GeneratorXMLPreprocessing.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(3,additionalArguments); if (s!=null) return s;

		xmlFile=new File(additionalArguments[0]);
		tableFile=new File(additionalArguments[1]);
		outputFile=new File(additionalArguments[2]);

		if (!xmlFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),xmlFile.toString());
		if (!tableFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.TableDoesNotExist"),tableFile.toString());
		if (outputFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.OutputAlreadyExist"),outputFile.toString());

		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		XMLPreprocessor preprocessor=new XMLPreprocessor(xmlFile,tableFile);
		String error;

		error=preprocessor.prepare();
		if (error!=null) {out.println(error); return;}
		error=preprocessor.process();
		if (error!=null) {out.println(error); return;}

		try(PrintWriter fileOut=new PrintWriter(outputFile)){
			fileOut.println(preprocessor.getResult());
		} catch (FileNotFoundException e) {return;}

		out.println(String.format(Language.tr("CommandLine.GeneratorXMLPreprocessing.Success"),outputFile.toString()));
	}
}
