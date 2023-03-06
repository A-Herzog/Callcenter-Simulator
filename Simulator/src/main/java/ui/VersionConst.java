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
package ui;

/**
 * Diese Klasse stellt statische Hilfsroutinen für die
 * Verarbeitung der Programmversionsnummer zur Verfügung.
 * @author Alexander Herzog
 * @version 1.0
 */
public final class VersionConst {
	/**
	 * Programmversion
	 */
	public static final String version="5.9.232";

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse stellt nur statische Hilfsroutinen
	 * zur Verfügung und kann daher nicht instanziert werden.
	 */
	private VersionConst() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Vergleich zwei Versionsnummern der Form x.y.z in Bezug auf die Komponenten x und y miteinander
	 * @param currentVersion	Programmversion
	 * @param dataVersion	Datendateiversion
	 * @return	Liefert <code>true</code> zurück, wenn die Datendateiversion neuer als die Programmversion ist
	 */
	public static boolean isNewerVersion(String currentVersion, String dataVersion) {
		if (dataVersion==null || dataVersion.isEmpty()) return false;
		if (currentVersion==null || currentVersion.isEmpty()) return false;
		String[] newVer=dataVersion.split("\\.");
		String[] curVer=currentVersion.split("\\.");
		if (newVer.length<3 || curVer.length<3) return false;

		try {
			int new1=Integer.parseInt(newVer[0]);
			int new2=Integer.parseInt(newVer[1]);
			int cur1=Integer.parseInt(curVer[0]);
			int cur2=Integer.parseInt(curVer[1]);
			return (new1>cur1 || (new1==cur1 && new2>cur2));
		} catch (NumberFormatException e) {return false;}
	}

	/**
	 * Vergleich zwei Versionsnummern der Form x.y.z in Bezug auf die Komponenten x und y miteinander
	 * @param dataVersion	Datendateiversion
	 * @return	Liefert <code>true</code> zurück, wenn die Datendateiversion neuer als die Programmversion ist
	 */
	public static boolean isNewerVersion(String dataVersion) {
		return isNewerVersion(version,dataVersion);
	}

	/**
	 * Vergleich zwei Versionsnummern der Form x.y.z in Bezug auf die Komponenten x und y miteinander
	 * @param dataVersion	Datendateiversion
	 * @return	Liefert <code>true</code> zurück, wenn die Datendateiversion älter als die Programmversion ist
	 */
	public static boolean isOlderVersion(String dataVersion) {
		return isNewerVersion(dataVersion,version);
	}

	/**
	 * Vergleich zwei Versionsnummern der Form x.y.z in Bezug auf die Komponenten x, y und z miteinander
	 * @param currentVersion	Programmversion
	 * @param dataVersion	Datendateiversion
	 * @return	Liefert <code>true</code> zurück, wenn die Datendateiversion neuer als die Programmversion ist
	 */
	public static boolean isNewerVersionFull(String currentVersion, String dataVersion) {
		if (dataVersion==null || dataVersion.isEmpty()) return false;
		if (currentVersion==null || currentVersion.isEmpty()) return false;
		String[] newVer=dataVersion.trim().split("\\.");
		String[] curVer=currentVersion.trim().split("\\.");
		if (newVer.length<3 || curVer.length<3) return false;

		try {
			int new1=Integer.parseInt(newVer[0]);
			int new2=Integer.parseInt(newVer[1]);
			int new3=Integer.parseInt(newVer[2]);
			int cur1=Integer.parseInt(curVer[0]);
			int cur2=Integer.parseInt(curVer[1]);
			int cur3=Integer.parseInt(curVer[2]);
			return (new1>cur1 || (new1==cur1 && new2>cur2) || (new1==cur1 && new2==cur2 && new3>cur3));
		} catch (NumberFormatException e) {return false;}
	}

	/**
	 * Vergleich zwei Versionsnummern der Form x.y.z in Bezug auf die Komponenten x, y und z miteinander
	 * @param dataVersion	Datendateiversion
	 * @return	Liefert <code>true</code> zurück, wenn die Datendateiversion neuer als die Programmversion ist
	 */
	public static boolean isNewerVersionFull(String dataVersion) {
		return isNewerVersionFull(version,dataVersion);
	}
}
