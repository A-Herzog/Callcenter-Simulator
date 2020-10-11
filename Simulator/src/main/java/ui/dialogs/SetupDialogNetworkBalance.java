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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.NumberTools;
import net.calc.RemoteComplexCallcenterSimulator;
import simulator.CallcenterSimulatorInterface;
import simulator.Simulator;
import simulator.Statistics;
import systemtools.MsgBox;
import tools.SetupData;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelExamples;
import ui.model.CallcenterRunModel;

/**
 * Über diesen Dialog kann die optimale Lastverteilung
 * zwischen einem oder mehreren entfernen Simulationsservern
 * und dem lokalen Rechner bestimmt werden.
 * @author Alexander Herzog
 */
public class SetupDialogNetworkBalance extends JDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -9046881414191682213L;

	private final String[] server;
	private final int[] port;
	private final String[] password;
	private final JProgressBar progress;
	private double[] serverParts=null;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param server	Liste der Server entsprechend {@link SetupData#networkServer}
	 * @param port	Liste der Ports der Server entsprechend {@link SetupData#networkPort}
	 * @param password	Liste der Passwörter für die Server entsprechend {@link SetupData#networkPassword}
	 */
	public SetupDialogNetworkBalance(final Window owner, final String server, final String port, final String password) {
		super(owner,Language.tr("SettingsDialog.NetworkSimulation.ServerPart.SetupTitle"));

		this.server=server.split(";");

		String[] s=port.split(";");
		if (s.length<this.server.length) {
			List<String> list=new ArrayList<String>(Arrays.asList(s));
			while (list.size()<this.server.length) list.add("6783");
			s=list.toArray(new String[0]);
		}
		this.port=new int[s.length];
		for (int i=0;i<s.length;i++) {
			Integer I=NumberTools.getNotNegativeInteger(s[i]);
			this.port[i]=(I!=null && I>0)?I:6783;
		}

		s=password.split(";");
		if (s.length<this.server.length) {
			List<String> list=new ArrayList<String>(Arrays.asList(s));
			while (list.size()<this.server.length) list.add("");
			s=list.toArray(new String[0]);
		}
		this.password=s;

		getContentPane().setLayout(new BorderLayout());
		JPanel p;
		getContentPane().add(p=new JPanel());
		p.add(progress=new JProgressBar(SwingConstants.HORIZONTAL,0,100));

		Dimension d=progress.getPreferredSize();
		d.width=350;
		progress.setPreferredSize(d);

		pack();
		setResizable(false);
		setModal(true);
		setLocationRelativeTo(owner);

		SwingUtilities.invokeLater(new Runnable(){@Override public void run() {new WorkThread().start();}});
		setVisible(true);
	}

	/**
	 * Liefert nach dem Abschluss der Tests und dem Schließen dieses Dialogs die
	 * optimale Lastverteilung, die dann in {@link SetupData#networkPart} gespeichert
	 * werden kann.
	 * @return	Optimale Lastverteilung
	 */
	public double[] getServerParts() {
		return serverParts;
	}

	private synchronized void setProgress(int percent) {
		progress.setValue(percent);
		repaint();
	}

	private class WorkThread extends Thread {
		private void waitForSimulationDone(CallcenterSimulatorInterface[] simulator, double percentAdd, double percentFactor) {
			int count=0;
			while (true) {
				boolean running=false;
				for (int i=0;i<simulator.length;i++) if (simulator[i].isRunning()) {running=true; break;}
				if (!running) break;

				try {Thread.sleep(25);} catch (InterruptedException e) {}
				count++;
				if (count%10==0) {
					double percent=((double)(simulator[0].getSimDayCount()))/simulator[0].getSimDaysCount();
					for (int i=1;i<simulator.length;i++) percent=Math.min(percent,((double)(simulator[i].getSimDayCount()))/simulator[i].getSimDaysCount());
					setProgress((int)Math.round((percent*percentFactor+percentAdd)*100));
				}
			}
		}

		private Statistics[] getStatistics(int days, double percentAdd, double percentFactor) {
			Statistics[] statistics=new Statistics[server.length+1];

			CallcenterModel editModel=CallcenterModelExamples.getExampleMedium();
			editModel.days=days;

			CallcenterSimulatorInterface[] simulator=new CallcenterSimulatorInterface[server.length+1];

			CallcenterRunModel runModel=new CallcenterRunModel(editModel);
			runModel.checkAndInit(false,false,false);
			simulator[0]=new Simulator(SetupData.getSetup().getRealMaxThreadNumber(),runModel);
			simulator[0].start(false);

			for (int i=0;i<server.length;i++) {
				simulator[1+i]=new RemoteComplexCallcenterSimulator(server[i],port[i],password[i],editModel);
				simulator[1+i].start(false);
			}

			waitForSimulationDone(simulator,percentAdd,percentFactor);

			for (int i=0;i<simulator.length;i++) {
				String s=simulator[i].finalizeRun();
				if (s!=null) {
					MsgBox.error(SetupDialogNetworkBalance.this,"Fehler bei der Netzwerksimulation",s);
					return null;
				}
				statistics[i]=simulator[i].collectStatistic();
			}

			return statistics;
		}

		@Override
		public void run() {
			Statistics[] statistics;

			statistics=getStatistics(20,0,0.1);
			if (statistics==null) {setVisible(false); return;}

			statistics=getStatistics(200,0.1,0.9);
			if (statistics==null) {setVisible(false); return;}

			for (int i=0;i<statistics.length;i++) if (statistics[i].simulationData.runTime<=0) {setVisible(false); return;}

			double[] parts=new double[statistics.length];
			double sum=0;
			for (int i=0;i<parts.length;i++) {double d=1/(double)(statistics[i].simulationData.runTime); parts[i]=d; sum+=d;}
			for (int i=0;i<parts.length;i++) {parts[i]/=sum;}

			serverParts=new double[server.length];
			for (int i=1;i<parts.length;i++) serverParts[i-1]=parts[i];
			setVisible(false);
		}
	}
}
