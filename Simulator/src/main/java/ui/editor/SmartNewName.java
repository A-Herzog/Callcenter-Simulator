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
package ui.editor;

import java.util.ArrayList;
import java.util.List;

/**
 * Erzeugt einen neuen Namen für ein kopiertes Objekt durch anfügen von " (2)" usw. an den Namen
 * @author Alexander Herzog
 * @version 1.0
 */
public class SmartNewName {
	/**
	 * Liste der bereits reservierten Namen
	 * @see #addReservedName(String)
	 * @see #getUniqueNewName(String)
	 */
	private final List<String> reservedNames;

	/**
	 * Konstruktor der Klasse
	 */
	public SmartNewName() {
		reservedNames=new ArrayList<>();
	}

	/**
	 * Konstruktor der Klasse
	 * @param reservedNames	Liste mit bereits reservierten Namen
	 */
	public SmartNewName(final List<String> reservedNames) {
		this.reservedNames=(reservedNames==null)?new ArrayList<>():reservedNames;
	}

	/**
	 * Fügt einen Namen zu der Liste der bereits reservierten Namen hinzu.
	 * @param name	Zusätzlicher als reserviert zu betrachtender Name
	 */
	public void addReservedName(final String name) {
		reservedNames.add(name);
	}

	/**
	 * Liefert auf Basis eines Ausgangsnamens einen eindeutigen Namen,
	 * der mit keinem bereits reservierten Namen kollidiert.
	 * @param baseName	Ausgangsname
	 * @return	Eindeutiger Name
	 */
	public String getUniqueNewName(String baseName) {
		if (reservedNames==null || reservedNames.size()==0) return baseName;

		/* Splitname */
		int nr=1;
		if (baseName.charAt(baseName.length()-1)==')') {
			String s=baseName.substring(0,baseName.length()-1);
			int i=s.lastIndexOf('(');
			if (i>=0) {
				try {nr=Integer.parseInt(s.substring(i+1));} catch(NumberFormatException e) {}
				if (nr>1) {
					baseName=s.substring(0,i);
					while (!baseName.isEmpty() && baseName.charAt(baseName.length()-1)==' ') baseName=baseName.substring(0,baseName.length()-1);
				}
			}
		}

		/* Freien Namen finden */
		boolean ok=false;
		while (!ok) {
			nr++;
			String s=baseName+" ("+nr+")";
			ok=true;
			for (int i=0;i<reservedNames.size();i++) if (reservedNames.get(i).equalsIgnoreCase(s)) {ok=false; break;}
		}

		return baseName+" ("+nr+")";
	}
}
