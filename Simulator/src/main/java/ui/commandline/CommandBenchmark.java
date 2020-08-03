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
import simulator.Statistics;
import systemtools.commandline.AbstractCommand;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelExamples;

/**
 * Führt einenen Benchmark-Test der Simulatorleistung durch.
 * @author Alexander Herzog
 * @version 1.0
 * @see AbstractCommand
 * @see AbstractSimulationCommand
 * @see CommandLineSystem
 */
public final class CommandBenchmark extends AbstractSimulationCommand {
	private int maxThreads=Integer.MAX_VALUE;
	private int speedTestMode=0;
	private File modelFile;

	@Override
	public String[] getKeys() {

		List<String> list=new ArrayList<String>();

		list.add(Language.tr("CommandLine.Benchmark.Name1"));
		for (String s: Language.trOther("CommandLine.Benchmark.Name1")) if (!list.contains(s)) list.add(s);

		if (!list.contains(Language.tr("CommandLine.Benchmark.Name2"))) list.add(Language.tr("CommandLine.Benchmark.Name2"));
		for (String s: Language.trOther("CommandLine.Benchmark.Name2")) if (!list.contains(s)) list.add(s);

		return list.toArray(new String[0]);
	}

	@Override
	public boolean isHidden() {
		return true;
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.Benchmark.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.Benchmark.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(0,1,additionalArguments); if (s!=null) return s;

		if (additionalArguments.length==0) return null;
		String arg=additionalArguments[0];

		Integer I=NumberTools.getNotNegativeInteger(additionalArguments[0]);
		if (I!=null && I!=0) {maxThreads=I; return null;}
		if (arg.equalsIgnoreCase(Language.tr("CommandLine.Benchmark.small"))) return null;
		for (String t: Language.trOther("CommandLine.Benchmark.small")) if (arg.equalsIgnoreCase(t)) return null;

		if (arg.equalsIgnoreCase(Language.tr("CommandLine.Benchmark.large1"))) {speedTestMode=1; return null;}
		for (String t: Language.trOther("CommandLine.Benchmark.large1")) if (arg.equalsIgnoreCase(t)) {speedTestMode=1; return null;}
		if (arg.equalsIgnoreCase(Language.tr("CommandLine.Benchmark.large2"))) {speedTestMode=1; return null;}
		for (String t: Language.trOther("CommandLine.Benchmark.large2")) if (arg.equalsIgnoreCase(t)) {speedTestMode=1; return null;}
		speedTestMode=2;
		modelFile=new File(arg);
		if (!modelFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),modelFile.toString());
		if (!isModelFile(modelFile)) return String.format(Language.tr("CommandLine.Error.File.InputNoValidCallCenterModel"),modelFile.toString());
		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		CallcenterModel editModel=null;

		switch (speedTestMode) {
		case 0:	editModel=CallcenterModelExamples.getExampleSmall(); editModel.days=2000; break;
		case 1:	editModel=CallcenterModelExamples.getExampleMedium(); break;
		case 2:
			editModel=new CallcenterModel(); String s=editModel.loadFromFile(modelFile);
			if (s!=null) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Error.LoadingModel")+" "+s); return;}
			break;
		}

		if (editModel==null) return;
		out.println(Language.tr("CommandLine.Benchmark.SimulatedDays")+": "+NumberTools.formatLong(editModel.days));

		for (int i=0;i<5;i++) {
			if (isCanceled()) break;
			if (i>0) out.println(Language.tr("CommandLine.Benchmark.SimulaionRun")+" "+(i+1));
			Statistics statistics=singleSimulation(editModel,true,maxThreads,out);
			if (statistics==null) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Benchmark.SimulaionFailed")); return;}
			if (i==0) out.println(Language.tr("CommandLine.Benchmark.Threads")+": "+NumberTools.formatLong(statistics.simulationData.runThreads));
			if (i==0) out.println(Language.tr("CommandLine.Benchmark.SimulaionRun")+" "+(i+1));
			out.println("  "+Language.tr("CommandLine.Benchmark.NeededCalculationTime")+": "+NumberTools.formatLong(statistics.simulationData.runTime)+" "+Language.tr("Statistic.Units.MilliSeconds"));
			out.println("  "+Language.tr("CommandLine.Benchmark.ClientsPerSecond")+": "+NumberTools.formatLong(1000*(long)(statistics.kundenGlobal.kunden+statistics.kundenGlobal.kundenWiederanruf)/statistics.simulationData.runRepeatCount/statistics.simulationData.runTime));
			out.println("  "+Language.tr("CommandLine.Benchmark.EventsPerSecond")+": "+NumberTools.formatLong(1000*statistics.simulationData.runEvents/statistics.simulationData.runTime));
		}
	}

}
