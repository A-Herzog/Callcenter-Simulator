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

import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import systemtools.commandline.AbstractCommand;
import ui.Rearranger;
import ui.model.CallcenterModel;
import xml.XMLTools;

/**
 * Kunden oder Agenten verlagern, um Ressourcen besser ausnutzen zu können.
 * @author Alexander Herzog
 * @see CommandLineSystem
 * @see AbstractCommand
 */
public class CommandRearrange extends AbstractCommand {
	/** Eingabe-Modelldatei */
	private File inputFile;
	/** Ausgabe-Modelldatei */
	private File outputFile;
	/** Sollen Agenten (<code>true</code>) oder Anrufe (<code>false</code>) verlagert werden? */
	private boolean modeAgents;
	/** Muss eine Zahl zwischen 0 und 1 enthalten, die angibt, wie stark das Ausgangsmodell und das neue Modell berücksichtigt werden sollen (0=nur Ausgangsmodell, 1=nur neues Modell) */
	private double value;
	/**  Liste der Agenten- bzw. Anrufergruppen (bei Agentengruppen in der Form "Nr-CallcenterName" mit "Nr" 1-basierend), die verändert werden sollen. Wird <code>null</code> oder eine leere Liste übergeben, so werden alle Gruppen angepasst. */
	private String[] groups=null;

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.Rearrange.Name"));
		for (String s: Language.trOther("CommandLine.Rearrange.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.Rearrange.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.Rearrange.Description.Long").split("\n");
	}

	/**
	 * Lädt eine xml-Datei
	 * @param file	Zu ladende Datei
	 * @return Tritt ein Fehler auf, so wird <code>null</code> zurück gegeben, ansonsten das Root-Element der Daten
	 */
	private final Element loadXMLFile(final File file) {
		XMLTools xml=new XMLTools(file);
		return xml.load();
	}

	/**
	 * Prüft, ob die übergebene Datei eine Modell Datei ist
	 * @param file	Zu prüfende Datei
	 * @return	Gibt <code>true</code> zurück, wenn es sich um eine Modell Datei handelt
	 */
	private final boolean isModelFile(final File file) {
		Element root=loadXMLFile(file);
		if (root==null) return false;
		for (String s: CallcenterModel.XMLBaseElement) if (root.getNodeName().equalsIgnoreCase(s)) return true;
		return false;
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(4,9999,additionalArguments); if (s!=null) return s;

		inputFile=new File(additionalArguments[0]);
		outputFile=new File(additionalArguments[1]);
		String mode=additionalArguments[2];
		Double D=NumberTools.getExtProbability(additionalArguments[3]);

		if (additionalArguments.length>4) {
			groups=new String[additionalArguments.length-4];
			for (int i=4;i<additionalArguments.length;i++) groups[i-4]=additionalArguments[i];
		}

		if (!inputFile.exists()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),inputFile);
		if (!isModelFile(inputFile)) return String.format(Language.tr("CommandLine.Error.File.InputNoValidCallCenterModel"),inputFile);
		if (outputFile.exists()) return String.format(Language.tr("CommandLine.Error.File.OutputAlreadyExist"),outputFile);
		if (Language.trAll("CommandLine.Rearrange.ModeCalls",mode)) {
			modeAgents=false;
		} else {
			if (!Language.trAll("CommandLine.Rearrange.ModeAgents",mode)) return Language.tr("CommandLine.Rearrange.ModeError");
			modeAgents=true;
		}
		if (D==null || D>1) return Language.tr("CommandLine.Rearrange.ValueError");
		value=D;

		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		CallcenterModel model=new CallcenterModel();
		String s=model.loadFromFile(inputFile);
		if (s!=null) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Error.LoadingModel")+" "+s); return;}

		Rearranger rearranger=new Rearranger(model);
		if (rearranger.modelError!=null) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Error.PreparationOfModel")+": "+rearranger.modelError); return;}

		CallcenterModel newModel;

		if (modeAgents) {
			newModel=rearranger.mixModels(rearranger.moveAgents(groups,false),value);
		} else {
			s=rearranger.canMoveCalls();
			if (s!=null) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Error.PreparationOfModel")+": "+s); return;}
			newModel=rearranger.mixModels(rearranger.moveCalls(groups),value);
		}

		if (newModel==null) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Error.ChangingModel")+": "+s); return;}

		if (!newModel.saveToFile(outputFile)) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Error.SavingModel")); return;}

		out.println(Language.tr("CommandLine.Rearrange.Done"));
	}

}
