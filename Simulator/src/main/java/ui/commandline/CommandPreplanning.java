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
import mathtools.NumberTools;
import mathtools.TimeTools;
import systemtools.commandline.AbstractCommand;
import ui.Preplanning;
import ui.model.CallcenterModel;

/**
 * Führt eine Agenten-Vorplanung aus.
 * @author Alexander Herzog
 * @version 1.0
 * @see AbstractCommand
 * @see AbstractSimulationCommand
 * @see CommandLineSystem
 */
public class CommandPreplanning extends AbstractSimulationCommand {
	private File inputFile;
	private File outputFile;
	private Preplanning.Mode mode;
	private boolean simplify;
	private boolean extended;
	private double value;

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<String>();
		list.add(Language.tr("CommandLine.Preplanning.Name"));
		for (String s: Language.trOther("CommandLine.Preplanning.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.Preplanning.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.Preplanning.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(4,additionalArguments); if (s!=null) return s;

		inputFile=new File(additionalArguments[0]);
		outputFile=new File(additionalArguments[1]);
		if (!inputFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),inputFile);
		if (!isModelFile(inputFile)) return String.format(Language.tr("CommandLine.Error.File.InputNoValidCallCenterModel"),inputFile);
		if (outputFile.exists()) return String.format(Language.tr("CommandLine.Error.File.OutputAlreadyExist"),outputFile);

		mode=null;
		if (Language.trAll("CommandLine.Preplanning.Mode.Success.Simple",additionalArguments[2])) {mode=Preplanning.Mode.MODE_SUCCESS; simplify=false; extended=false;}
		if (Language.trAll("CommandLine.Preplanning.Mode.Success.Complex",additionalArguments[2])) {mode=Preplanning.Mode.MODE_SUCCESS; simplify=false; extended=true;}
		if (Language.trAll("CommandLine.Preplanning.Mode.WaitingTime.Simple",additionalArguments[2])) {mode=Preplanning.Mode.MODE_WAITING_TIME; simplify=false; extended=false;}
		if (Language.trAll("CommandLine.Preplanning.Mode.WaitingTime.Complex",additionalArguments[2])) {mode=Preplanning.Mode.MODE_WAITING_TIME; simplify=false; extended=true;}
		if (Language.trAll("CommandLine.Preplanning.Mode.ServiceLevel.Simple",additionalArguments[2])) {mode=Preplanning.Mode.MODE_SERVICE_LEVEL; simplify=false; extended=false;}
		if (Language.trAll("CommandLine.Preplanning.Mode.ServiceLevel.Complex",additionalArguments[2])) {mode=Preplanning.Mode.MODE_SERVICE_LEVEL; simplify=false; extended=true;}
		if (Language.trAll("CommandLine.Preplanning.Mode.Success.Simple.PreSimplify",additionalArguments[2])) {mode=Preplanning.Mode.MODE_SUCCESS; simplify=true; extended=false;}
		if (Language.trAll("CommandLine.Preplanning.Mode.Success.Complex.PreSimplify",additionalArguments[2])) {mode=Preplanning.Mode.MODE_SUCCESS; simplify=true; extended=true;}
		if (Language.trAll("CommandLine.Preplanning.Mode.WaitingTime.Simple.PreSimplify",additionalArguments[2])) {mode=Preplanning.Mode.MODE_WAITING_TIME; simplify=true; extended=false;}
		if (Language.trAll("CommandLine.Preplanning.Mode.WaitingTime.Complex.PreSimplify",additionalArguments[2])) {mode=Preplanning.Mode.MODE_WAITING_TIME; simplify=true; extended=true;}
		if (Language.trAll("CommandLine.Preplanning.Mode.ServiceLevel.Simple.PreSimplify",additionalArguments[2])) {mode=Preplanning.Mode.MODE_SERVICE_LEVEL; simplify=true; extended=false;}
		if (Language.trAll("CommandLine.Preplanning.Mode.ServiceLevel.Complex.PreSimplify",additionalArguments[2])) {mode=Preplanning.Mode.MODE_SERVICE_LEVEL; simplify=true; extended=true;}

		if (mode==null) return Language.tr("CommandLine.Preplanning.Mode.InvalidMode");

		if (mode==Preplanning.Mode.MODE_WAITING_TIME) {
			/* Zeitangabe */
			Double D=TimeTools.getExactTime(additionalArguments[3]);
			if (D==null || D<0) return Language.tr("CommandLine.Preplanning.Error.InvalidTargetValue");
			value=D;
		} else {
			/* Wahrscheinlichkeit */
			Double D=NumberTools.getExtProbability(additionalArguments[3]);
			if (D==null || D<0 || D>1) return Language.tr("CommandLine.Preplanning.Error.InvalidTargetValue");
			value=D;
		}

		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		CallcenterModel baseModel=new CallcenterModel();
		String s=baseModel.loadFromFile(inputFile);
		if (s!=null) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Error.LoadingModel")+" "+s); return;}

		Preplanning preplanning=new Preplanning(baseModel);
		CallcenterModel newModel=preplanning.calc(mode,simplify?Preplanning.Mode.SIMPLIFY_AVERAGE_AHT:Preplanning.Mode.SIMPLIFY_NO,value,extended,Preplanning.DEFAULT_MULTI_SKILL_REDUCTION);

		if (!newModel.saveToFile(outputFile)) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Error.SavingModel")); return;}
	}
}
