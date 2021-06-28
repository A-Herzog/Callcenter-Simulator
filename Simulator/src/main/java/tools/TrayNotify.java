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
package tools;

import java.awt.AWTException;
import java.awt.Frame;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import ui.MainFrame;

/**
 * Ermöglicht das Anzeigen von Meldungen als Tooltips von Trayicons
 * Die Meldung wird nur angezeigt, wenn das Programmfenster momentan minimiert ist.
 * Wird die Meldung oder das Icon angeklickt, so wird das Programmfenster wiederhergestellt und Meldung und Icon werden wieder ausgeblendet.
 * Werden Meldung oder Icon 10 Sekunden lang nicht angeklickt, so werden diese ohne weitere Aktionen ausgeblendet.
 * @author Alexander Herzog
 * @version 1.0
 */
public class TrayNotify {
	/**
	 * Konstruktor der Klasse <code>TrayNotify</code>
	 * @param component	Übergeorndete Komponente (wird benötigt, um das Elternfenster zu finden)
	 * @param caption	Titel der anzuzeigenden Meldung
	 * @param message	Meldung, die angezeigt werden soll
	 */
	public TrayNotify(final JComponent component, final String caption, final String message) {
		/* Allgemeine Voraussetzungen prüfen */
		if (!SystemTray.isSupported()) return;

		/* Übergeordnetes Fenster finden und prüfen, ob es minimiert ist */
		final Window window=SwingUtilities.windowForComponent(component);
		if (window==null) return;
		if (!(window instanceof Frame)) return;
		final Frame frame=(Frame)window;
		if (frame.getExtendedState()!=Frame.ICONIFIED) return;

		/* Icon laden, Trayicon anzeigen, Meldung anzeigen */
		Image image=Toolkit.getDefaultToolkit().getImage(MainFrame.ICON_URL);
		final TrayIcon icon=new TrayIcon(image,caption);
		icon.addMouseListener(new MouseInputAdapter() {@Override public void mouseClicked(MouseEvent e) {frame.setExtendedState(Frame.NORMAL);}});
		icon.addActionListener(e->frame.setExtendedState(Frame.NORMAL));
		try {SystemTray.getSystemTray().add(icon);} catch (AWTException e1) {}
		icon.displayMessage(caption,message,MessageType.INFO);

		/* Timeout für Entfernen des Icons */
		final Timer timer=new Timer();
		timer.schedule(new TimerTask() {@Override public void run() {SystemTray.getSystemTray().remove(icon);}},10000);
	}
}
