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
package ui.specialpanels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import ui.HelpLink;
import ui.RevenueOptimizer;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelCaller;

/**
 * In diesem Panel kann eine automatische Ertragsoptimierung
 * durch die Variation der Agenten-Anzahl vorgenommen werden.
 * @author Alexander Herzog
 * @see RevenueOptimizer
 */
public class RevenueOptimizerPanel extends JWorkPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -1507079890494303036L;

	/** �bergeordnetes Fenster */
	private final Window owner;
	/** F�r die Ertragsoptimierung zu verwendendes Callcenter-Modell */
	private final CallcenterModel baseModel;
	/** Bisheriges bestes Modell */
	private CallcenterModel bestModel;
	/** Modell f�r die R�ckgabe an den Editor */
	private CallcenterModel returnModel=null;

	/** Fu�zeile mit Informationen */
	private final JPanel bottomInfoPanel;
	/** Text innerhalb der Fu�zeile */
	private final JLabel bottomInfoLabel;
	/** Bereich f�r Statusausgaben */
	private final JTextPane statusField;

	/** Ertrags-Optimierer-System */
	private RevenueOptimizer revenueOptimizer;
	/** Thread in dem die eigentliche Optimierung erfolgt */
	private OptimizeThread thread;
	/** Timer zum regelm��igen Aufruf von {@link UpdateTimer} w�hrend einer laufenden Optimierung */
	private Timer timer;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Fenster
	 * @param doneNotify	Callback wird aufgerufen, wenn das Panel geschlossen werden soll
	 * @param helpLink	Help-Link
	 * @param model	F�r die Ertragsoptimierung zu verwendendes Callcenter-Modell
	 */
	public RevenueOptimizerPanel(final Window owner, final Runnable doneNotify, final HelpLink helpLink, final CallcenterModel model) {
		super(doneNotify,helpLink.pageRevenueOptimizer);
		this.owner=owner;
		this.baseModel=model;
		this.bestModel=model;

		addFooter(Language.tr("RevenueOptimizer.Button.Opzimize"),Images.REVENUSE_OPTIMIZER_RUN.getIcon(),Language.tr("RevenueOptimizer.Button.Cancel"));

		add(bottomInfoPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		bottomInfoPanel.add(bottomInfoLabel=new JLabel());

		add(new JScrollPane(statusField=new JTextPane()),BorderLayout.CENTER);
		statusField.setEditable(false);
		clearText();

		addStatusLine(2,Language.tr("RevenueOptimizer.Warning.MainTitle"));
		addStatusLine(1,Language.tr("RevenueOptimizer.Warning.Heuristic.Title"));
		addStatusLine(0,Language.tr("RevenueOptimizer.Warning.Heuristic.Info"));
		addStatusLine(1,Language.tr("RevenueOptimizer.Warning.SearchDirection.Title"));
		addStatusLine(0,Language.tr("RevenueOptimizer.Warning.SearchDirection.Info"));
		int count=0;
		for (CallcenterModelCaller caller : model.caller) if (caller.active) count+=caller.freshCallsCountMean;
		if (count>2000) {
			addStatusLine(1,Language.tr("RevenueOptimizer.Warning.LongRunTime.Title"));
			addStatusLine(0,Language.tr("RevenueOptimizer.Warning.LongRunTime.Info"));
		}
		if (model.days<1000) {
			addStatusLine(1,Language.tr("RevenueOptimizer.Warning.MoreDaysNeeded.Title"));
			addStatusLine(0,Language.tr("RevenueOptimizer.Warning.MoreDaysNeeded.Info"));
		}
	}

	/**
	 * L�scht die bisherigen Ausgaben in {@link #statusField}
	 * und initialisiert die Stile f�r die Ausgabe.
	 * @see #statusField
	 */
	private void clearText() {
		StyledDocument doc=new DefaultStyledDocument();

		Style defaultStyle=StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		Style s;

		s=doc.addStyle("text",defaultStyle);
		StyleConstants.setFontSize(s,StyleConstants.getFontSize(s)+1);

		s=doc.addStyle("heading",defaultStyle);
		StyleConstants.setBold(s,true);
		StyleConstants.setFontSize(s,StyleConstants.getFontSize(s)+4);

		s=doc.addStyle("subheading",defaultStyle);
		StyleConstants.setBold(s,true);
		StyleConstants.setFontSize(s,StyleConstants.getFontSize(s)+2);

		statusField.setDocument(doc);
	}

	/**
	 * Liefert im Anschluss an die Optimierung das in Bezug auf den Ertrag beste Modell.
	 * @return	Bezug auf den Ertrag bestes Callcenter-Modell
	 */
	public CallcenterModel getBestModel() {
		return returnModel;
	}

	@Override
	public void done() {
		if (bestModel!=null && !bestModel.equalsCallcenterModel(baseModel)) {
			if (MsgBox.confirm(owner,Language.tr("RevenueOptimizer.UseResult.Title"),Language.tr("RevenueOptimizer.UseResult.Info"),Language.tr("RevenueOptimizer.UseResult.Yes.Info"),Language.tr("RevenueOptimizer.UseResult.No.Info")))
				returnModel=bestModel;
		}

		super.done();
	}

	/**
	 * Gibt eine Meldung in {@link #statusField} aus.
	 * @param heading	�berschrift
	 * @param line	Meldung
	 */
	private synchronized void addStatusLine(final int heading, final String line) {
		StyledDocument doc=statusField.getStyledDocument();
		switch (heading) {
		case 2:
			try {doc.insertString(doc.getLength(),line+"\n",doc.getStyle("heading"));} catch (BadLocationException e) {}
			break;
		case 1:
			try {doc.insertString(doc.getLength(),"\n",doc.getStyle("text"));} catch (BadLocationException e) {}
			try {doc.insertString(doc.getLength(),line+"\n",doc.getStyle("subheading"));} catch (BadLocationException e) {}
			break;
		default:
			try {doc.insertString(doc.getLength(),line+"\n",doc.getStyle("text"));} catch (BadLocationException e) {}
			break;
		}
		try {statusField.setCaretPosition(doc.getLength());} catch (IllegalArgumentException e) {}
	}

	@Override
	protected void run() {
		clearText();

		revenueOptimizer=new RevenueOptimizer(bestModel) {
			@Override
			protected void statusOutput(boolean isHeading,String text) {addStatusLine(isHeading?1:0,text);}
		};
		if (!revenueOptimizer.check()) {
			MsgBox.error(this,Language.tr("Window.ErrorStartingSimulation.Title"),revenueOptimizer.getError());
			return;
		}

		thread=new OptimizeThread();
		thread.start();

		timer=new Timer("RevenueOptimizerGUIUpdateTimer");
		timer.schedule(new UpdateTimer(),50,50);

		setWorkMode(true);
	}

	/**
	 * Aktualisiert die Anzeige gem�� des Simulationsfortschritts
	 */
	private class UpdateTimer extends TimerTask {
		/**
		 * Konstruktor der Klasse
		 */
		public UpdateTimer() {
			/*
			 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
			 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void run() {
			if (cancelWork) {
				timer.cancel();
				revenueOptimizer.setAbort();
				try {thread.join();} catch (InterruptedException e) {}
				return;
			}
			double d1=revenueOptimizer.getBaseRevenue();
			double d2=revenueOptimizer.getBestRevenue();
			double delta=(Math.abs(d1)>0.01)?((d2-d1)/Math.abs(d1)):0.0;

			final String greenStart="<span color=\"green\">";
			final String redStart="<span color=\"red\">";
			final String colorStop="</span>";
			String c1, c2;

			StringBuilder sb=new StringBuilder();
			sb.append("<html><body style=\"font-size: 105%\">");
			if (delta>0.005) {c1=greenStart; c2=colorStop;} else {c1=""; c2="";}
			sb.append("Ertrag: <b>"+c1+NumberTools.formatNumber(d1)+" &rarr; "+NumberTools.formatNumber(d2)+" (+"+NumberTools.formatPercent(delta)+")"+c2+"</b>,   ");
			int[] agents=revenueOptimizer.getAgentsNumberChange();
			if (agents[0]>0) {c1=redStart; c2=colorStop;} else {c1=""; c2="";}
			sb.append("hinzugef�gte Agentenintervalle: <b>"+c1+agents[0]+c2+"</b>,   ");
			if (agents[1]>0) {c1=greenStart; c2=colorStop;} else {c1=""; c2="";}
			sb.append("entfernte Agentenintervalle: <b>"+c1+agents[1]+c2+"</b>");
			sb.append("</body></html>");

			bottomInfoLabel.setText(sb.toString());
		}
	}

	/**
	 * Thread der die eigentliche Optimierung durchf�hrt.
	 * @see RevenueOptimizerPanel#revenueOptimizer
	 * @see RevenueOptimizer
	 */
	private class OptimizeThread extends Thread {
		/**
		 * Konstruktor der Klasse
		 */
		public OptimizeThread() {
			super("RevenueOptimizerThread");
		}
		@Override
		public void run() {
			if (!revenueOptimizer.run()) {
				addStatusLine(0,revenueOptimizer.getError());
			} else {
				CallcenterModel model=revenueOptimizer.getBestModel();
				if (model!=null) bestModel=model;
			}
			timer.cancel();
			setWorkMode(false);
		}
	}
}