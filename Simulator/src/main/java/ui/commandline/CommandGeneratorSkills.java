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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.Table;
import mathtools.distribution.DataDistributionImpl;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelSkillLevel;

/**
 * Lädt per Kommandozeilenbefehl Bedien- und Nachbearbeitungszeiten aus einer Tabelle und fügt diese in ein Callcenter-Modell ein.
 * @author Alexander Herzog
 * @see AbstractGeneratorCommand
 * @see CommandLineSystem
 */
public final class CommandGeneratorSkills extends AbstractGeneratorCommand {
	/**
	 * Konstruktor der Klasse
	 */
	public CommandGeneratorSkills() {
		nameArgumentCount=1;
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<String>();
		list.add(Language.tr("CommandLine.GeneratorSkillLevel.Name"));
		for (String s: Language.trOther("CommandLine.GeneratorSkillLevel.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.GeneratorSkillLevel.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.GeneratorSkillLevel.Description.Long").split("\n");
	}

	private String generatorSkills(int row, int col, CallcenterModel editModel, Table table, String type) {
		/* Skill-Level wählen */
		CallcenterModelSkillLevel skill=null;
		for (int i=0;i<editModel.skills.size();i++) if (editModel.skills.get(i).name.equalsIgnoreCase(name[0])) {skill=editModel.skills.get(i); break;}
		if (skill==null) return String.format(Language.tr("CommandLine.Error.NoSkillLevelWithName"),name[0]);

		/* Eintrag wählen wählen */
		if (type.isEmpty()) return Language.tr("CommandLine.Error.NoClientType");
		String type1=type.substring(0,1).toUpperCase();
		String type2=type.substring(1);
		if (!type1.equals(Language.tr("CommandLine.GeneratorSkillLevel.Type.LetterHolding")) && !type1.equals(Language.tr("CommandLine.GeneratorSkillLevel.Type.LetterPostProcessing"))) return Language.tr("CommandLine.GeneratorSkillLevel.ErrorNoType");

		int nr=-1;
		for (int i=0;i<skill.callerTypeName.size();i++) if (skill.callerTypeName.get(i).equalsIgnoreCase(type2)) {nr=i; break;}
		if (nr<0) return String.format(Language.tr("CommandLine.GeneratorSkillLevel.ErrorNoClientType"),type,type2);

		/* Daten laden und eintragen */
		DataDistributionImpl data=new DataDistributionImpl(3600,3600);
		String s=loadData(row,col,table,data,3600);
		if (s!=null) return s;
		if (type1.equals(Language.tr("CommandLine.GeneratorSkillLevel.Type.LetterHolding"))) {
			skill.callerTypeWorkingTime.set(nr,data);
			skill.callerTypeIntervalWorkingTime.set(nr,new AbstractRealDistribution[48]);
		} else {
			skill.callerTypePostProcessingTime.set(nr,data);
			skill.callerTypeIntervalPostProcessingTime.set(nr,new AbstractRealDistribution[48]);
		}

		return null;
	}

	@Override
	protected boolean runIntern(CallcenterModel editModel, Table table, PrintStream out) {
		for (int i=0;i<generatorColumn.size();i++) {
			int[] cell=getStartCell(generatorColumn.get(i));
			if (cell==null) {out.print(Language.tr("Dialog.Title.Error").toUpperCase()+": "+String.format(Language.tr("CommandLine.Error.InvalidStartCell"),generatorColumn.get(i))); return true;}
			String t=generatorSkills(cell[0],cell[1],editModel,table,generatorType.get(i));
			if (t!=null) {out.println(t); return false;}
			out.println(String.format(Language.tr("CommandLine.GeneratorSkillLevel.Done"),generatorType.get(i).substring(1),name[0]));
		}

		return true;
	}
}
