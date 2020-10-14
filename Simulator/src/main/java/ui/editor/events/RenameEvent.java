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
package ui.editor.events;

import java.awt.AWTEvent;
import java.io.Serializable;

/**
 * Dieses Ereignisse kapselt Informationen, die beim Umbenennen einer Callcenter-Modell-Komponente
 * anfallen.
 * @author Alexander Herzog
 * @version 1.0
 * @see RenameListener
 */
public class RenameEvent extends AWTEvent {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -3009382995603313568L;

	/**
	 * Alter Name des umzubenennende Objekt
	 */
	public final String oldName;

	/**
	 * Neuer Name für das umzubenennende Objekt (<code>null</code> bedeutet, dass das Objekt nicht umbenannt, sondern gelöscht wurde)
	 */
	public final String newName;

	/**
	 * Gibt an, dass das Objekt umbenannt wurde (und nicht gelöscht wurde)
	 */
	public final boolean renamed;

	/**
	 * Gibt an, das das Objekt gelöscht wurde (und nicht umbenannt wurde)
	 */
	public final boolean deleted;

	/**
	 * Konstruktor der Klasse <code>RenameEvent</code>
	 * @param source	Objekt das das Ereignis auslöst
	 * @param oldName	Alter Name für das umzubenennende Objekt
	 * @param newName	Neuer Name für das umzubenennende Objekt (<code>null</code> bedeutet, dass das Objekt nicht umbenannt, sondern gelöscht wurde)
	 */
	public RenameEvent(Object source, String oldName, String newName) {
		super(source,RESERVED_ID_MAX+1);
		this.oldName=oldName;
		this.newName=newName;
		renamed=(newName!=null);
		deleted=(newName==null);
	}
}
