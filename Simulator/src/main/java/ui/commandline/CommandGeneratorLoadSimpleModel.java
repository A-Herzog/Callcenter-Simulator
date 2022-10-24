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
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.dataloader.SimpleModelLoader;

/**
 * Erstellt aus einer Anrufer- und einer Agententabelle ein Callcenter-Modell.
 * @author Alexander Herzog
 * @see AbstractGeneratorCommand
 * @see CommandLineSystem
 */
public class CommandGeneratorLoadSimpleModel extends AbstractCommand {
	/** Eingabetabellendateien */
	private final List<File> tableFile;
	/** Arbeitsblattnamen in den Eingabetabellen */
	private final List<String> tableName;
	/** Spaltennamen in den Eingabetabellen */
	private final List<String> tableColumn;
	/** Ausgabemodelldatei */
	private File modelFile;

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandGeneratorLoadSimpleModel(final BaseCommandLineSystem system) {
		super(system);
		tableFile=new ArrayList<>();
		tableName=new ArrayList<>();
		tableColumn=new ArrayList<>();
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.GeneratorLoadSimpleModel.Name"));
		for (String s: Language.trOther("CommandLine.GeneratorLoadSimpleModel.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.GeneratorLoadSimpleModel.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.GeneratorLoadSimpleModel.Description.Long").split("\n");
	}

	/**
	 * Trennt einen Text an einem Trennzeichen
	 * @param text	Text
	 * @param separator	Trennzeichen
	 * @return	Teiltexte
	 */
	private List<String> split(String text, char separator) {
		List<String> results=new ArrayList<>();
		StringBuilder sb=new StringBuilder();
		String sub="";

		for (char c : text.toCharArray()) {
			if (c=='"' || c=='\'') {
				if (!sub.isEmpty() && sub.charAt(sub.length()-1)==c) {
					/* Eine Ebene rauf */
					if (sub.length()>1) sb.append(c);
					sub=sub.substring(0,sub.length()-1);
					continue;
				} else {
					/* Eine Ebene runter */
					if (sub.length()>0) sb.append(c);
					sub+=c;
				}
				continue;
			}
			if (c==separator && sub.length()==0) {
				results.add(sb.toString());
				sb=new StringBuilder();
				continue;
			}
			sb.append(c);
		}

		if (sb.length()>0) results.add(sb.toString());
		return results;
	}

	/**
	 * Führt die Interpretation der übergebenen Kommandozeilenparameter aus.
	 * @param parameter	Kommandozeilenparameter
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	private String processParameter(final String parameter) {
		List<String> parts=split(parameter,'!');
		while (parts.size()<3) parts.add("");

		File table=new File(parts.get(0));
		if (!table.isFile()) return String.format(Language.tr("CommandLine.Error.File.TableDoesNotExist"),table.toString());
		if (parts.get(2).trim().isEmpty()) parts.set(2,"A");

		tableFile.add(table);
		tableName.add(parts.get(1));
		tableColumn.add(parts.get(2));

		return null;
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(3,additionalArguments); if (s!=null) return s;

		for (int i=0;i<=1;i++) {
			s=processParameter(additionalArguments[i].trim());
			if (s!=null) return s;
		}
		modelFile=new File(additionalArguments[2]);
		if (modelFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.OutputAlreadyExist"),modelFile.toString());

		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		String s;
		SimpleModelLoader loader=new SimpleModelLoader();
		s=loader.setCallerTable(tableFile.get(0),tableName.get(0),tableColumn.get(0));
		if (s!=null) {out.println(s); return;}
		s=loader.setAgentsTable(tableFile.get(1),tableName.get(1),tableColumn.get(1));
		if (s!=null) {out.println(s); return;}
		s=loader.buildModel(modelFile);
		if (s!=null) {out.println(s); return;}
		out.println(String.format(Language.tr("CommandLine.GeneratorLoadSimpleModel.Success"),modelFile.toString()));
	}
}
