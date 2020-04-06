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
package ui.statistic.core.filter;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.w3c.dom.Document;

import language.Language;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.DistributionTools;

/**
 * Diese Klasse stellt Methoden zur Verfügung, um Daten aus einem xml-Document auszulesen
 * @author Alexander Herzog
 * @version 1.0
 */
public class DataFilter extends DataFilterBase {
	private boolean systemNumbers;
	private boolean percent;
	private boolean time;
	private char separator;

	private String title;

	/**
	 * Konstruktor der Klasse <code>CallcenterSimulatorDataFilter</code>
	 * @param xmlDoc XML-Dokument, aus dem die Daten entnommen werden sollen
	 */
	public DataFilter(Document xmlDoc) {
		super(xmlDoc,true);
		registerCommand(new TextCommand());
		registerCommand(new PrintCommand());
		registerCommand(new FormatCommand());
		registerCommand(new SeparatorCommand());
		registerCommand(new SetCommand());
		registerCommand(new CalcCommand());
		registerCommand(new TitleCommand());
		registerCommand(new SumCommand());
		registerCommand(new MeanCommand());
		registerCommand(new SDCommand());
		registerCommand(new CVCommand());
	}

	private class TextCommand implements DataFilterCommand {
		@Override
		public String getName() {return "Text";}
		@Override
		public boolean allowEmptyParameters() {return false;}
		@Override
		public String run(String parameters) {
			if (parameters.length()>2 && (parameters.length() > 0 && parameters.charAt(0)=='"') && parameters.endsWith("\""))
				parameters=parameters.substring(1,parameters.length()-1);
			addResult(parameters.replaceAll("\\\\t","\t").replaceAll("\\\\n","\n"));
			return null;
		}
	}

	private class TitleCommand implements DataFilterCommand {
		@Override
		public String getName() {return "Title";}
		@Override
		public boolean allowEmptyParameters() {return false;}
		@Override
		public String run(String parameters) {
			if (parameters.length()>2 && (parameters.length() > 0 && parameters.charAt(0)=='"') && parameters.endsWith("\""))
				parameters=parameters.substring(1,parameters.length()-1);
			title=parameters.replaceAll("\\\\t","\t").replaceAll("\\\\n","\n");
			return null;
		}
	}

	private class PrintCommand implements DataFilterCommand {
		@Override
		public String getName() {return "Print";}
		@Override
		public boolean allowEmptyParameters() {return false;}
		@Override
		public String run(String parameters) {
			/* Variable */
			int index=-1;
			for (int i=0;i<varNames.size();i++) if (varNames.get(i).equalsIgnoreCase(parameters)) {index=i; break;}
			if (index>=0) {addResult(formatNumber(varValues.get(index),systemNumbers,percent,time,separator)); return null;}

			/* Rechnung */
			String[] value=calc(parameters);
			if (value[0]==null) {addResult(formatNumber(value[1],systemNumbers,percent,time,separator)); return null;}

			/* XML-Anweisung */
			String[] s=findElement(parameters,systemNumbers,percent,time,separator);
			if (s[1]!=null) addResult(s[1]);
			return s[0];
		}
	}

	private class FormatCommand implements DataFilterCommand {
		@Override
		public String getName() {return "Format";}
		@Override
		public boolean allowEmptyParameters() {return false;}
		@Override
		public String run(String parameters) {
			if (parameters.equalsIgnoreCase("lokal") || parameters.equalsIgnoreCase("local")) {systemNumbers=false; return null;}
			if (parameters.equalsIgnoreCase("system")) {systemNumbers=true; return null;}
			if (parameters.equalsIgnoreCase("fraction") || parameters.equalsIgnoreCase("bruch")) {time=false; percent=false; return null;}
			if (parameters.equalsIgnoreCase("percent") || parameters.equalsIgnoreCase("prozent")) {time=false; percent=true; return null;}
			if (parameters.equalsIgnoreCase("time") || parameters.equalsIgnoreCase("zeit")) {time=true; return null;}
			if (parameters.equalsIgnoreCase("number") || parameters.equalsIgnoreCase("zahl")) {time=false; return null;}
			return Language.tr("Statistic.Filter.InvalidParameters")+" ("+parameters+")";
		}
	}

	private class SeparatorCommand implements DataFilterCommand {
		@Override
		public String getName() {return "Separator";}
		@Override
		public boolean allowEmptyParameters() {return false;}
		@Override
		public String run(String parameters) {
			if (parameters.equalsIgnoreCase("semikolon") || parameters.equalsIgnoreCase("semicolon")) {separator=';'; return null;}
			if (parameters.equalsIgnoreCase("line") || parameters.equalsIgnoreCase("lines") || parameters.equalsIgnoreCase("newline") || parameters.equalsIgnoreCase("zeilen") || parameters.equalsIgnoreCase("zeile")) {separator='\n'; return null;}
			if (parameters.equalsIgnoreCase("tab") || parameters.equalsIgnoreCase("tabs") || parameters.equalsIgnoreCase("tabulator")) {separator='\t'; return null;}
			return Language.tr("Statistic.Filter.InvalidParameters")+" ("+parameters+")";
		}
	}

	private class SetCommand implements DataFilterCommand {
		@Override
		public String getName() {return "Set";}
		@Override
		public boolean allowEmptyParameters() {return false;}
		@Override
		public String run(String parameters) {
			/* Variable und XML-Anweisung trennen */
			int index=parameters.indexOf('=');
			if (index<1) return Language.tr("Statistic.Filter.InvalidParameters")+" ("+parameters+")";
			String name=parameters.substring(0,index).trim();
			parameters=parameters.substring(index+1).trim();

			/* XML-Anweisung */
			String[] value=findElement(parameters,true,false,false,';');
			if (value[0]!=null) return value[0];

			/* Ergebnis in Variable speichern */
			index=-1;
			for (int i=0;i<varNames.size();i++) if (varNames.get(i).equalsIgnoreCase(name)) {index=i; break;}
			if (index>=0) varValues.set(index,value[1]); else {varNames.add(name); varValues.add(value[1]);}
			return null;
		}
	}

	private class CalcCommand implements DataFilterCommand {
		@Override
		public String getName() {return "Calc";}
		@Override
		public boolean allowEmptyParameters() {return false;}
		@Override
		public String run(String parameters) {
			/* Variable und Anweisung trennen */
			int index=parameters.indexOf('=');
			if (index<1) return Language.tr("Statistic.Filter.InvalidParameters")+" ("+parameters+")";
			String name=parameters.substring(0,index).trim();
			parameters=parameters.substring(index+1).trim();

			String[] value=calc(parameters);
			if (value[0]!=null) return value[0];

			/* Ergebnis in Variable speichern */
			index=-1;
			for (int i=0;i<varNames.size();i++) if (varNames.get(i).equalsIgnoreCase(name)) {index=i; break;}
			if (index>=0) varValues.set(index,value[1]); else {varNames.add(name); varValues.add(value[1]);}
			return null;
		}
	}

	private class SumCommand implements DataFilterCommand {
		@Override
		public String getName() {return "Sum";}
		@Override
		public boolean allowEmptyParameters() {return false;}
		@Override
		public String run(String parameters) {
			String value=null;

			/* Variable */
			for (int i=0;i<varNames.size();i++) if (varNames.get(i).equalsIgnoreCase(parameters)) {value=varValues.get(i); break;}

			if (value==null) {
				/* XML-Anweisung */
				String[] result=findElement(parameters,true,false,false,';');
				if (result[0]==null) value=result[1]; else return result[0];
			}

			AbstractRealDistribution dist=DistributionTools.distributionFromString(value,86400);
			if (dist==null) return String.format(Language.tr("Statistic.Filter.InvalidDistribution"),value);
			if (!(dist instanceof DataDistributionImpl)) return String.format(Language.tr("Statistic.Filter.InvalidDataDistribution"),value);

			addResult(formatNumber(((DataDistributionImpl)dist).sum(),systemNumbers,percent,time,separator));
			return null;
		}
	}

	private class MeanCommand implements DataFilterCommand {
		@Override
		public String getName() {return "Mean";}
		@Override
		public boolean allowEmptyParameters() {return false;}
		@Override
		public String run(String parameters) {
			String value=null;

			/* Variable */
			for (int i=0;i<varNames.size();i++) if (varNames.get(i).equalsIgnoreCase(parameters)) {value=varValues.get(i); break;}

			if (value==null) {
				/* XML-Anweisung */
				String[] result=findElement(parameters,true,false,false,';');
				if (result[0]==null) value=result[1]; else return result[0];
			}

			AbstractRealDistribution dist=DistributionTools.distributionFromString(value,86400);
			if (dist==null) return String.format(Language.tr("Statistic.Filter.InvalidDistribution"),value);

			addResult(formatNumber(DistributionTools.getMean(dist),systemNumbers,percent,time,separator));
			return null;
		}
	}

	private class SDCommand implements DataFilterCommand {
		@Override
		public String getName() {return "SD";}
		@Override
		public boolean allowEmptyParameters() {return false;}
		@Override
		public String run(String parameters) {
			String value=null;

			/* Variable */
			for (int i=0;i<varNames.size();i++) if (varNames.get(i).equalsIgnoreCase(parameters)) {value=varValues.get(i); break;}

			if (value==null) {
				/* XML-Anweisung */
				String[] result=findElement(parameters,true,false,false,';');
				if (result[0]==null) value=result[1]; else return result[0];
			}

			AbstractRealDistribution dist=DistributionTools.distributionFromString(value,86400);
			if (dist==null) return String.format(Language.tr("Statistic.Filter.InvalidDistribution"),value);

			addResult(formatNumber(DistributionTools.getStandardDeviation(dist),systemNumbers,percent,time,separator));
			return null;
		}
	}

	private class CVCommand implements DataFilterCommand {
		@Override
		public String getName() {return "CV";}
		@Override
		public boolean allowEmptyParameters() {return false;}
		@Override
		public String run(String parameters) {
			String value=null;

			/* Variable */
			for (int i=0;i<varNames.size();i++) if (varNames.get(i).equalsIgnoreCase(parameters)) {value=varValues.get(i); break;}

			if (value==null) {
				/* XML-Anweisung */
				String[] result=findElement(parameters,true,false,false,';');
				if (result[0]==null) value=result[1]; else return result[0];
			}

			AbstractRealDistribution dist=DistributionTools.distributionFromString(value,86400);
			if (dist==null) return String.format(Language.tr("Statistic.Filter.InvalidDistribution"),value);

			addResult(formatNumber(DistributionTools.getCV(dist),systemNumbers,percent,time,separator));
			return null;
		}
	}

	@Override
	public boolean run(String commands, boolean ignoreErrors) {
		systemNumbers=false;
		percent=false;
		time=false;
		separator=';';
		varNames.clear();
		varValues.clear();
		title="";
		return super.run(commands,ignoreErrors);
	}

	/**
	 * Liefert nach der Ausführungen von <code>run</code> den Titel des Skripts
	 * zurück (sofern ein solcher gesetzt war, sonst "").
	 * @return	Titel des Skripts.
	 * @see #getTitleFromCommand(String)
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Liefert ohne vorherige Ausführung von <code>run</code> den Titel des Skriptes
	 * zurück (sofern ein solcher gesetzt war, sonst "").
	 * @param commands Auszuführende Filterbefehle
	 * @return	Titel des Skripts.
	 * @see #getTitle()
	 */
	public static String getTitleFromCommand(String commands) {
		DataFilter temp=new DataFilter(null);
		temp.run(commands,true);
		return temp.getTitle();
	}
}
