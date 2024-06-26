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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import language.Language;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.UpdateSystem;
import ui.UpdateSystemSignature;

/**
 * Pr�ft auf neue Versionen und l�dt ggf. eine neue jar-Datei herunter.
 * @author Alexander Herzog
 * @see CommandLineSystem
 * @see AbstractCommand
 */
public class CommandUpdate extends AbstractCommand {
	/** Ausgabe-jar-Datei */
	private File jarFile;

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandUpdate(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.Update.Name"));
		for (String s: Language.trOther("CommandLine.Update.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(String[]::new);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.Update.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.Update.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(1,additionalArguments); if (s!=null) return s;
		jarFile=new File(additionalArguments[0]);

		if (jarFile.exists()) return String.format(Language.tr("CommandLine.Error.File.OutputAlreadyExist"),jarFile.toString());
		return null;
	}

	/**
	 * Pr�ft, ob ein Update verf�gbar ist
	 * @param out	Ausgabe-Objekt
	 * @return	Liefert <code>true</code>, wenn ein Update verf�gbar ist
	 */
	private boolean checkNewVersionAvailable(PrintStream out) {
		String serverVersion=UpdateSystem.checkUpdateAvailable();
		if (!ui.VersionConst.isNewerVersionFull(serverVersion)) {
			out.println(Language.tr("CommandLine.Update.NoNewerVersion"));
			return false;
		}
		out.println(String.format(Language.tr("CommandLine.Update.DownloadingNewVersion"),serverVersion));
		return true;
	}

	/**
	 * L�dt ein Update herunter
	 * @param out	Ausgabe-Objekt
	 * @return	Liefert im Erfolgsfall <code>true</code>.
	 */
	private boolean downloadJarFiles(PrintStream out) {
		try {
			URL home1=new URI(UpdateSystem.defaultProtocollConnect+"://"+UpdateSystem.updateFullURL1).toURL();
			URL home2=new URI(UpdateSystem.defaultProtocollConnect+"://"+UpdateSystem.updateFullURL2).toURL();
			byte[] data=new byte[32768];

			/* Datei herunterladen */
			URLConnection connection=home1.openConnection();
			if (!(connection instanceof HttpURLConnection)) return false;
			if (connection instanceof HttpsURLConnection) {
				((HttpsURLConnection )connection).setHostnameVerifier((hostname, session)->hostname.equalsIgnoreCase(UpdateSystem.updateServer));
			}
			int bytes=0;
			try (BufferedInputStream in=new BufferedInputStream(connection.getInputStream())) {
				FileOutputStream fileOut=new FileOutputStream(jarFile);
				try (BufferedOutputStream buf=new BufferedOutputStream(fileOut,32768)) {
					int x=0, count=0;
					while((x=in.read(data,0,data.length))>=0) {
						buf.write(data,0,x);
						bytes+=x;
						count++;
						if (count%100==0) out.print(".");
					}
					if (count>50) out.println("");
				}
			}
			if (bytes<1024*1024*20) return false;

			/* Pr�fsumme laden */
			connection=home2.openConnection();
			if (!(connection instanceof HttpURLConnection)) {return false;}
			if (connection instanceof HttpsURLConnection) {
				((HttpsURLConnection )connection).setHostnameVerifier((hostname, session)->hostname.equalsIgnoreCase(UpdateSystem.homeURL));
			}
			try (BufferedInputStream in=new BufferedInputStream(connection.getInputStream())) {
				int size=in.read(data,0,data.length);
				if (size<=0) {
					/* Pr�fung fehlgeschlagen (keine Signatur verf�gbar) */
					out.println(Language.tr("CommandLine.Update.SignatureCheckFailed"));
					return false;
				}
				byte[] sign=Arrays.copyOf(data,size);
				UpdateSystemSignature tester=new UpdateSystemSignature(jarFile);
				if (!tester.verify(new String(sign))) {
					/* Pr�fung fehlgeschlagen */
					out.println(Language.tr("CommandLine.Update.SignatureCheckFailed"));
					return false;
				}
			}
		} catch (IOException | URISyntaxException e) {return false;}

		return true;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		if (checkNewVersionAvailable(out)) {
			if (!downloadJarFiles(out)) {
				if (jarFile.exists()) jarFile.delete();
				out.println(Language.tr("CommandLine.Update.DownloadFailed"));
			} else {
				out.println(Language.tr("CommandLine.Update.DownloadDoneAndSignatureCheckPassed"));
			}
		}
	}
}
