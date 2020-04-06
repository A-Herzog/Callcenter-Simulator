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
package ui.calculator;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.math3.special.Gamma;

import language.Language;
import mathtools.ErlangC;
import mathtools.NumberTools;
import systemtools.MsgBox;
import ui.HelpLink;
import ui.editor.BaseEditDialog;

/**
 * @author Alexander Herzog
 * @version 1.0
 */
public class LoadCalculatorDialog extends BaseEditDialog {
	private static final long serialVersionUID = 1388546809132066439L;

	private JTabbedPane auslastung_tabs;
	private JTextField auslastung_lambda, auslastung_kundenProTag, auslastung_betriebsstunden, auslastung_lastanteil, auslastung_muInv, auslastung_rho;
	private JLabel auslastung_result;

	private JTextField erlangB_A, erlangB_N;
	private JLabel erlangB_result;

	private JTextField erlangC_lambda, erlangC_muInv, erlangC_c, erlangC_t;
	private JLabel erlangC_result_a, erlangC_result_rho, erlangC_result_P1, erlangC_result_ENQ, erlangC_result_EN, erlangC_result_EW, erlangC_result_EV, erlangC_result;

	private JTextField erlangCext_lambda, erlangCext_muInv, erlangCext_nuInv, erlangCext_c, erlangCext_t;
	private JLabel erlangCext_result_a, erlangCext_result_rho, erlangCext_result_ENQ, erlangCext_result_EN, erlangCext_result_EW, erlangCext_result_EV, erlangCext_result_Abbruch, erlangCext_result;

	private JTextField allenCunneen_lambda, allenCunneen_bI, allenCunneen_muInv, allenCunneen_c, allenCunneen_b, allenCunneen_cvI, allenCunneen_cvS;
	private JLabel allenCunneen_result_a, allenCunneen_result_rho, allenCunneen_result_ENQ, allenCunneen_result_EN, allenCunneen_result_EW, allenCunneen_result_EV;

	private JTextField toleranz_Wartezeit, toleranz_Abbruchrate;
	private JLabel toleranz_Mittelwert;

	private JTextField addInputLine(JPanel parent, String title, String label, String defaultValue) {
		JPanel panel;
		JTextField field;

		if (title!=null && !title.isEmpty()) parent.add(new JLabel("<html><body>"+title+"</body></html>"));
		parent.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(new JLabel(label));
		panel.add(field=new JTextField(defaultValue,7));
		panel.setAlignmentX(0);
		field.addKeyListener(new Recalc());

		return field;
	}

	private void addTopInfo(JPanel parent, String text) {
		parent.add(new JLabel("<html><body><b>"+text+"</b></body></html>"));
		parent.add(Box.createVerticalStrut(10));
	}

	private void addInfoLink(JPanel parent, String text, String link) {
		JLabel info;
		parent.add(Box.createVerticalStrut(10));
		parent.add(info=new JLabel("<html><body><a href=\"\">"+text+"</a></body></html>"));
		info.addMouseListener(new LinkListener(link));
		info.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		info.setAlignmentX(0);
		parent.add(Box.createVerticalStrut(5));
		parent.add(Box.createVerticalGlue());
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param helpLink	Hilfe-Link
	 */
	public LoadCalculatorDialog(final Window owner, final HelpLink helpLink) {
		super(owner,Language.tr("LoadCalculator.Title"),null,false,helpLink.dialogLoadCalculator);
		showCloseButton=true;
		createTabsGUI(null,null,null,false,525,500,null,null);
		pack();
		Dimension d=getSize(); d.width=Math.max(d.width,525); setSize(d);
		new Recalc().recalc();
	}

	@Override
	protected void createTabs(JTabbedPane tabs) {
		JPanel panel1,panel2;

		/* Dialogseite "Auslastung" */
		tabs.addTab(Language.tr("LoadCalculator.Tab.WorkLoad"),panel1=new JPanel());
		panel1.setLayout(new BoxLayout(panel1,BoxLayout.PAGE_AXIS));
		panel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		addTopInfo(panel1,"&rho;=&lambda;/("+Language.tr("LoadCalculator.Agents")+"&middot;&mu;)");
		panel1.add(auslastung_tabs=new JTabbedPane());
		auslastung_tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		auslastung_tabs.setAlignmentX(0);

		auslastung_tabs.addTab(Language.tr("LoadCalculator.ArrivalRate"),panel2=new JPanel());
		panel2.setLayout(new BoxLayout(panel2,BoxLayout.PAGE_AXIS));
		panel2.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		auslastung_lambda=addInputLine(panel2,Language.tr("LoadCalculator.ArrivalRate")+" ("+Language.tr("LoadCalculator.Units.ClientsPerMinute")+"):","<html><body>&lambda;=</body></html>",NumberTools.formatNumber(3.5));

		auslastung_tabs.addTab(Language.tr("LoadCalculator.Units.ClientsPerDay"),panel2=new JPanel());
		panel2.setLayout(new BoxLayout(panel2,BoxLayout.PAGE_AXIS));
		panel2.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		auslastung_kundenProTag=addInputLine(panel2,Language.tr("LoadCalculator.Units.ClientsPerDay")+":",Language.tr("LoadCalculator.Units.Number")+"=","10000");
		auslastung_betriebsstunden=addInputLine(panel2,Language.tr("LoadCalculator.WorkingTimePerDay")+":",Language.tr("LoadCalculator.WorkingHours")+"=","16");
		auslastung_lastanteil=addInputLine(panel2,Language.tr("LoadCalculator.PartOfLoadForTheCallcenter")+":",Language.tr("LoadCalculator.Units.Part")+"=","100%");

		auslastung_muInv=addInputLine(panel1,Language.tr("LoadCalculator.AverageHoldingAndPostProcessingTime")+" ("+Language.tr("LoadCalculator.Units.InMinutes")+"):","<html><body>1/&mu;=</body></html>","3");
		auslastung_rho=addInputLine(panel1,Language.tr("LoadCalculator.SystemLoad")+" ("+Language.tr("LoadCalculator.Units.InPercent")+"):","<html><body>&rho;=</body></html>","85%");

		panel1.add(auslastung_result=new JLabel("<html><body><b>"+String.format(Language.tr("LoadCalculator.MinimumNumberOfAgents"),"")+"</b></body></html>"));
		panel1.add(Box.createVerticalGlue());

		/* Dialogseite "Erlang B" */
		tabs.addTab(Language.tr("LoadCalculator.Tab.ErlangB"),panel1=new JPanel());
		panel1.setLayout(new BoxLayout(panel1,BoxLayout.PAGE_AXIS));
		panel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		addTopInfo(panel1,"B=(A^N/N!) / sum (i=0..N; A^i/i!)");
		erlangB_A=addInputLine(panel1,Language.tr("LoadCalculator.OfferedWorkLoad")+" (&lambda;/&mu;):","a=","900");
		erlangB_N=addInputLine(panel1,Language.tr("LoadCalculator.NumberOfLines")+":","N=","925");
		panel1.add(erlangB_result=new JLabel("<html><body><b>"+Language.tr("LoadCalculator.ProbabilityOfBlocking")+": P(B)=000,0%</b></body></html>"));
		addInfoLink(panel1,Language.tr("LoadCalculator.Tab.ErlangB.Link.Info"),Language.tr("LoadCalculator.Tab.ErlangB.Link"));
		panel1.add(Box.createVerticalGlue());

		/* Dialogseite "Erlang C" */
		tabs.addTab(Language.tr("LoadCalculator.Tab.ErlangC"),panel1=new JPanel());
		panel1.setLayout(new BoxLayout(panel1,BoxLayout.PAGE_AXIS));
		panel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		addTopInfo(panel1,"P(W&le;t)=1-P1+exp(-&mu;(c-a)&middot;t)");
		erlangC_lambda=addInputLine(panel1,Language.tr("LoadCalculator.ArrivalRate")+" ("+Language.tr("LoadCalculator.Units.ClientsPerMinute")+"):","<html><body>&lambda;=</body></html>",NumberTools.formatNumber(3.5));
		erlangC_muInv=addInputLine(panel1,Language.tr("LoadCalculator.AverageHoldingAndPostProcessingTime")+" ("+Language.tr("LoadCalculator.Units.InMinutes")+"):","<html><body>1/&mu;=</body></html>","3");
		erlangC_c=addInputLine(panel1,Language.tr("LoadCalculator.Agents")+":","c=","13");
		erlangC_t=addInputLine(panel1,Language.tr("LoadCalculator.WaitingTime")+" ("+Language.tr("LoadCalculator.Units.InSeconds")+"):","t=","20");
		panel1.add(erlangC_result_a=new JLabel("a="));
		panel1.add(erlangC_result_rho=new JLabel("<html><body>(rho) &rho;=</body></html>"));
		panel1.add(erlangC_result_P1=new JLabel("P1="));
		panel1.add(erlangC_result_ENQ=new JLabel("<html><body>E[N<sub>Q</sub>]=</body></html>"));
		panel1.add(erlangC_result_EN=new JLabel("E[N]="));
		panel1.add(erlangC_result_EW=new JLabel("E[W]="));
		panel1.add(erlangC_result_EV=new JLabel("E[V]="));
		panel1.add(erlangC_result=new JLabel("<html><body><b>P(W&le;t)=000,0%</b></body></html>"));
		addInfoLink(panel1,Language.tr("LoadCalculator.Tab.ErlangC.Link.Info"),Language.tr("LoadCalculator.Tab.ErlangC.Link"));
		panel1.add(Box.createVerticalGlue());

		/* Dialogseite "Erlang C (erweitert)" */
		tabs.addTab(Language.tr("LoadCalculator.Tab.ErlangCext"),panel1=new JPanel());
		panel1.setLayout(new BoxLayout(panel1,BoxLayout.PAGE_AXIS));
		panel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		addTopInfo(panel1,"P(W&le;t)=1-C<sub>K</sub>&pi;<sub>0</sub>-&pi;<sub>0</sub>&Sigma;<sub>n=c..K</sub>C<sub>n</sub> Q(n-c+1;(c&mu;+&nu;)t)");
		erlangCext_lambda=addInputLine(panel1,Language.tr("LoadCalculator.ArrivalRate")+" ("+Language.tr("LoadCalculator.Units.ClientsPerMinute")+"):","<html><body>&lambda;=</body></html>",NumberTools.formatNumber(3.5));
		erlangCext_muInv=addInputLine(panel1,Language.tr("LoadCalculator.AverageHoldingAndPostProcessingTime")+" ("+Language.tr("LoadCalculator.Units.InMinutes")+"):","<html><body>1/&mu;=</body></html>","3");
		erlangCext_nuInv=addInputLine(panel1,Language.tr("LoadCalculator.AverageWaitingTimeTolerance")+" ("+Language.tr("LoadCalculator.Units.InMinutes")+"):","<html><body>1/&nu;=</body></html>","5");
		erlangCext_c=addInputLine(panel1,Language.tr("LoadCalculator.Agents")+":","c=","13");
		erlangCext_t=addInputLine(panel1,Language.tr("LoadCalculator.WaitingTime")+" ("+Language.tr("LoadCalculator.Units.InSeconds")+"):","t=","20");
		panel1.add(erlangCext_result_a=new JLabel("a="));
		panel1.add(erlangCext_result_rho=new JLabel("<html><body>(rho) &rho;=</body></html>"));
		panel1.add(erlangCext_result_ENQ=new JLabel("<html><body>E[N<sub>Q</sub>]=</body></html>"));
		panel1.add(erlangCext_result_EN=new JLabel("E[N]="));
		panel1.add(erlangCext_result_EW=new JLabel("E[W]="));
		panel1.add(erlangCext_result_EV=new JLabel("E[V]="));
		panel1.add(erlangCext_result_Abbruch=new JLabel("P(A)="));
		panel1.add(erlangCext_result=new JLabel("<html><body><b>P(W&le;t)=000,0%</b></body></html>"));
		panel1.add(Box.createVerticalGlue());

		/* Dialogseite "Allen-Cunneen" */
		tabs.addTab("Allen-Cunneen",panel1=new JPanel());
		panel1.setLayout(new BoxLayout(panel1,BoxLayout.PAGE_AXIS));
		panel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		addTopInfo(panel1,Language.tr("LoadCalculator.Tab.AllenCunneen"));

		allenCunneen_lambda=addInputLine(panel1,Language.tr("LoadCalculator.ArrivalRate")+" ("+Language.tr("LoadCalculator.Units.ClientsPerMinute")+"):","<html><body>&lambda;=</body></html>",NumberTools.formatNumber(3.5));
		allenCunneen_bI=addInputLine(panel1,Language.tr("LoadCalculator.ArrivalBatchSize")+":","b(I)=","1");
		allenCunneen_muInv=addInputLine(panel1,Language.tr("LoadCalculator.AverageHoldingTime")+" ("+Language.tr("LoadCalculator.Units.InMinutes")+"):","<html><body>1/&mu;=</body></html>","3");
		allenCunneen_c=addInputLine(panel1,Language.tr("LoadCalculator.Agents")+":","c=","13");
		allenCunneen_b=addInputLine(panel1,Language.tr("LoadCalculator.BatchSize")+":","b(S)=","1");
		allenCunneen_cvI=addInputLine(panel1,Language.tr("LoadCalculator.ArrivalRateCV")+":","CV[I]=","1");
		allenCunneen_cvS=addInputLine(panel1,Language.tr("LoadCalculator.WorkingRateCV")+":","CV[S]=","1");
		panel1.add(allenCunneen_result_a=new JLabel("a="));
		panel1.add(allenCunneen_result_rho=new JLabel("<html><body>(rho) &rho;=</body></html>"));
		panel1.add(allenCunneen_result_ENQ=new JLabel("<html><body>E[N<sub>Q</sub>]=</body></html>"));
		panel1.add(allenCunneen_result_EN=new JLabel("E[N]="));
		panel1.add(allenCunneen_result_EW=new JLabel("E[W]="));
		panel1.add(allenCunneen_result_EV=new JLabel("E[V]="));
		addInfoLink(panel1,Language.tr("LoadCalculator.TUCOnlineCalculator"),"https://www.mathematik.tu-clausthal.de/interaktiv/warteschlangentheorie/warteschlangenrechner/");
		panel1.add(Box.createVerticalGlue());

		/* Dialogseite "Wartezeittoleranz" */
		tabs.addTab(Language.tr("LoadCalculator.Tab.WaitingTimeTolerance"),panel1=new JPanel());
		panel1.setLayout(new BoxLayout(panel1,BoxLayout.PAGE_AXIS));
		panel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		addTopInfo(panel1,Language.tr("LoadCalculator.ExpAssumption"));
		panel1.add(new JLabel("E[WT]=E[W]/P(A)"));
		panel1.add(Box.createVerticalStrut(10));
		toleranz_Wartezeit=addInputLine(panel1,Language.tr("LoadCalculator.AverageWaitingTime")+" ("+Language.tr("LoadCalculator.Units.InSeconds")+"):","E[W]=","10");
		toleranz_Abbruchrate=addInputLine(panel1,Language.tr("LoadCalculator.CancelRate")+" ("+Language.tr("LoadCalculator.Units.InPercent")+"):","P(A)=","7%");
		panel1.add(toleranz_Mittelwert=new JLabel("<html><body><b>E[WT]=000,0 "+Language.tr("LoadCalculator.Units.Seconds")+"</b></body></html>"));
		panel1.add(Box.createVerticalGlue());

		tabs.addChangeListener(new Recalc());
		auslastung_tabs.addChangeListener(new Recalc());
	}

	@Override protected boolean checkData() {return true;}
	@Override protected void storeData() {}

	private class Recalc implements ChangeListener, KeyListener {
		private void recalcAuslastung() {
			int index=auslastung_tabs.getSelectedIndex();

			double lambda;

			if (index==0) {
				Double lambdaObj=NumberTools.getPositiveDouble(auslastung_lambda,true);
				if (lambdaObj==null) {auslastung_result.setText(Language.tr("LoadCalculator.InvalidInput")); return;}
				lambda=lambdaObj;
			} else {
				Long kundenProTagObj=NumberTools.getPositiveLong(auslastung_kundenProTag,true);
				Double betreibsstundenObj=NumberTools.getPositiveDouble(auslastung_betriebsstunden,true);
				Double lastanteilObj=NumberTools.getProbability(auslastung_lastanteil,true);
				if (kundenProTagObj==null || betreibsstundenObj==null || lastanteilObj==null) {auslastung_result.setText(Language.tr("LoadCalculator.InvalidInput")); return;}
				long kundenProTag=kundenProTagObj;
				double betreibsstunden=betreibsstundenObj;
				double lastanteil=lastanteilObj;
				lambda=kundenProTag*lastanteil/betreibsstunden/60;
			}

			Double muInvObj=NumberTools.getPositiveDouble(auslastung_muInv,true);
			Double rhoObj=NumberTools.getPositiveDouble(auslastung_rho,true);
			if (muInvObj==null || rhoObj==null) {auslastung_result.setText(Language.tr("LoadCalculator.InvalidInput")); return;}
			double muInv=muInvObj;
			double rho=rhoObj;

			auslastung_result.setText("<html><body><b>"+String.format(Language.tr("LoadCalculator.MinimumNumberOfAgents"),NumberTools.formatNumber(lambda*muInv/rho,2))+"</b></body></html>");
		}

		private void recalcErlangB() {
			Long A=NumberTools.getPositiveLong(erlangB_A,true);
			Long N=NumberTools.getPositiveLong(erlangB_N,true);
			if (A==null || N==null) {erlangB_result.setText(Language.tr("LoadCalculator.InvalidInput")); return;}
			double a=A;
			double n=N;
			/* B=(A^N/N!) / sum (i=0..N; A^i/i!) */
			/* 1/B=sum(i=0..N; prod(j)i+1..N; j/A) [Termumformung] */
			double invB=0;
			for (int i=0;i<=n;i++) {
				double prod=1;
				for (int j=i+1;j<=n;j++) prod*=j/a;
				invB+=prod;
			}
			erlangB_result.setText("<html><body><b>"+Language.tr("LoadCalculator.ProbabilityOfBlocking")+": P(B)="+NumberTools.formatNumber(1/invB*100)+"%</b></body></html>");
		}

		private double powerFactorial(double a, long c) {
			/* a^c/c! */
			double result=1;
			for (int i=1;i<=c;i++) result*=(a/i);
			return result;
		}

		private void recalcErlangC() {
			Double lambdaObj=NumberTools.getPositiveDouble(erlangC_lambda,true);
			Double muInvObj=NumberTools.getPositiveDouble(erlangC_muInv,true);
			Long cObj=NumberTools.getLong(erlangC_c,true);
			Double tObj=NumberTools.getPositiveDouble(erlangC_t,true);
			if (lambdaObj==null || muInvObj==null || muInvObj==0 || tObj==null || tObj==0 || cObj==null || cObj<=0) {erlangC_result.setText(Language.tr("LoadCalculator.InvalidInput")); return;}
			double lambda=lambdaObj;
			double muInv=muInvObj;
			long c=cObj;
			double t=tObj;

			double a=lambda*muInv;
			double P1=0;
			for (int i=0;i<=c-1;i++)
				P1+=powerFactorial(a,i);
			double temp=powerFactorial(a,c)*c/(c-a);
			P1=temp/(P1+temp);

			double EW=P1/(c/muInv-lambda);
			double EV=EW+muInv;
			double ENQ=P1*a/(c-a);
			double EN=lambda*EV;

			erlangC_result_a.setText(Language.tr("LoadCalculator.OfferedWorkLoad")+" a="+NumberTools.formatNumber(a,2));
			erlangC_result_rho.setText("<html><body>"+Language.tr("LoadCalculator.WorkLoad")+" (rho) &rho;="+NumberTools.formatNumber(100*a/c,2)+"%</body></html>");
			erlangC_result_P1.setText("P1="+NumberTools.formatNumber(P1,2));

			erlangC_result_ENQ.setText("<html><body>"+Language.tr("LoadCalculator.AverageQueueLength")+" E[N<sub>Q</sub>]="+NumberTools.formatNumber(ENQ,2)+"</body></html>");
			erlangC_result_EN.setText(Language.tr("LoadCalculator.AverageNumberOfClientsInTheSystem")+" E[N]="+NumberTools.formatNumber(EN,2));
			erlangC_result_EW.setText(Language.tr("LoadCalculator.AverageWaitingTime")+" E[W]="+NumberTools.formatNumber(EW*60,2)+" ("+Language.tr("LoadCalculator.Units.InSeconds")+")");
			erlangC_result_EV.setText(Language.tr("LoadCalculator.AverageResidenceTime")+" E[V]="+NumberTools.formatNumber(EV*60,2)+" ("+Language.tr("LoadCalculator.Units.InSeconds")+")");

			if (P1>=1 || P1<0) {
				erlangC_result.setText("<html><body><b>P(W&le;t) "+Language.tr("LoadCalculator.ErlangCNotCalculateable")+"</b></body></html>");
			} else {
				erlangC_result.setText("<html><body><b>P(W&le;t)="+NumberTools.formatNumber((1-P1*Math.exp(-1/muInv*(c-a)*t/60))*100,2)+"%</b></body></html>");
			}
		}

		private void recalcErlangCext() {
			Double lambdaObj=NumberTools.getPositiveDouble(erlangCext_lambda,true);
			Double muInvObj=NumberTools.getPositiveDouble(erlangCext_muInv,true);
			Double nuInvObj=NumberTools.getPositiveDouble(erlangCext_nuInv,true);
			Long cObj=NumberTools.getLong(erlangCext_c,true);
			Double tObj=NumberTools.getPositiveDouble(erlangCext_t,true);
			if (lambdaObj==null || muInvObj==null || nuInvObj==null || muInvObj==0 || nuInvObj==0 || tObj==null || tObj==0 || cObj==null || cObj<=0) {erlangCext_result.setText(Language.tr("LoadCalculator.InvalidInput")); return;}
			double lambda=lambdaObj;
			double muInv=muInvObj;
			double nuInv=nuInvObj;
			long c=cObj;
			double t=tObj;

			double a=lambda*muInv;

			final int K=1000;

			double[] Cn=ErlangC.extErlangCCn(lambda,1/muInv,1/nuInv,(int)c,K);
			double pi0=0;
			for (int i=0;i<Cn.length;i++) pi0+=Cn[i];
			pi0=1/pi0;

			double Pt;
			if (pi0==0) Pt=1; else Pt=1-Cn[K]*pi0;
			for (int n=(int)c;n<=K-1;n++) {
				Double g=0.0;
				g=Gamma.regularizedGammaQ(n-c+1,(c*1/muInv+1/nuInv)*t/60);
				Pt-=pi0*Cn[n]*g;
			}
			if (Double.isNaN(Pt) || Pt<0) Pt=0;

			double ENQ=0; for (int i=(int)(c+1);i<Cn.length;i++) ENQ+=(i-c)*Cn[i]*pi0;
			double EN=0; for (int i=1;i<Cn.length;i++) EN+=i*Cn[i]*pi0;
			double EW=ENQ/lambda;
			double EV=EW+muInv;

			erlangCext_result_a.setText(Language.tr("LoadCalculator.OfferedWorkLoad")+" a="+NumberTools.formatNumber(a,2));
			erlangCext_result_rho.setText("<html><body>"+Language.tr("LoadCalculator.WorkLoad")+" (rho) &rho;="+NumberTools.formatNumber(100*(lambda-ENQ/nuInv)*muInv/c,2)+"%</body></html>");
			erlangCext_result_ENQ.setText("<html><body>"+Language.tr("LoadCalculator.AverageQueueLength")+" E[N<sub>Q</sub>]="+NumberTools.formatNumber(ENQ,2)+"</body></html>");
			erlangCext_result_EN.setText(Language.tr("LoadCalculator.AverageNumberOfClientsInTheSystem")+" E[N]="+NumberTools.formatNumber(EN,2));
			erlangCext_result_EW.setText(Language.tr("LoadCalculator.AverageWaitingTime")+" E[W]="+NumberTools.formatNumber(EW*60,2)+" ("+Language.tr("LoadCalculator.Units.InSeconds")+")");
			erlangCext_result_EV.setText(Language.tr("LoadCalculator.AverageResidenceTime")+" E[V]="+NumberTools.formatNumber(EV*60,2)+" ("+Language.tr("LoadCalculator.Units.InSeconds")+")");
			erlangCext_result_Abbruch.setText(Language.tr("LoadCalculator.CancelRate")+" P(A)="+NumberTools.formatNumber(ENQ/nuInv/lambda*100,2)+"%");

			erlangCext_result.setText("<html><body><b>P(W&le;t)="+NumberTools.formatNumber(Pt*100,2)+"%</b></body></html>");

		}

		private void recalcAllenCunneen() {
			Double lambdaObj=NumberTools.getPositiveDouble(allenCunneen_lambda,true);
			Long bIObj=NumberTools.getLong(allenCunneen_bI,true);
			Double muInvObj=NumberTools.getPositiveDouble(allenCunneen_muInv,true);
			Long cObj=NumberTools.getLong(allenCunneen_c,true);
			Long bSObj=NumberTools.getLong(allenCunneen_b,true);
			Double cvIObj=NumberTools.getNotNegativeDouble(allenCunneen_cvI,true);
			Double cvSObj=NumberTools.getNotNegativeDouble(allenCunneen_cvS,true);

			if (lambdaObj==null || bIObj==null ||muInvObj==null || cObj==null || bSObj==null || cvIObj==null || cvSObj==null || lambdaObj==0 || bIObj<=0 || muInvObj==0 || cObj<=0 || bSObj<=0) return;

			double lambda=lambdaObj;
			long bI=bIObj;
			double muInv=muInvObj;
			long c=cObj;
			long b=bSObj;
			double cvI=cvIObj;
			double cvS=cvSObj;

			/* Umrechnung von Arrival-Batches auf einzelne Kunden */
			lambda=lambda*bI;
			cvI=Math.sqrt(bI*cvI*cvI+bI-1);

			double a=lambda*muInv;
			double rho=lambda*muInv/(b*c);

			/*
			PC1=(c*rho)^c/(c!(1-rho));
			PC=PC1/(PC1+sum(k=0...c-1; (c*rho)^k/k!))
			E[NQ]=rho/(1-rho)*PC*(SCV[I]+b*SCV[S])/2+(b-1)/2
			E[N]=E[NQ]+b*c*rho
			 */

			double PC1=powerFactorial(c*rho,c)/(1-rho);
			double PC=0; for(int i=0;i<=c-1;i++) PC+=powerFactorial(c*rho,i);
			PC=PC1/(PC1+PC);

			double ENQ=rho/(1-rho)*PC*(cvI*cvI+b*cvS*cvS)/2+(((double)b)-1)/2;
			double EN=ENQ+((double)b)*((double)c)*rho;
			double EW=ENQ/lambda;
			double EV=EW+muInv;

			allenCunneen_result_a.setText(Language.tr("LoadCalculator.OfferedWorkLoad")+" a="+NumberTools.formatNumber(a,2));
			if (rho>=1) {
				allenCunneen_result_rho.setText("<html><body>"+Language.tr("LoadCalculator.WorkLoad")+" (rho) &rho;="+NumberTools.formatNumber(100*rho,2)+"% ("+Language.tr("LoadCalculator.AllenCunneenInvalidWorkLoad")+")</body></html>");
				allenCunneen_result_ENQ.setText("<html><body>"+Language.tr("LoadCalculator.AverageQueueLength")+" E[N<sub>Q</sub>]=</body></html>");
				allenCunneen_result_EN.setText(Language.tr("LoadCalculator.AverageNumberOfClientsInTheSystem")+" E[N]=");
				allenCunneen_result_EW.setText(Language.tr("LoadCalculator.AverageWaitingTime")+" E[W]= ("+Language.tr("LoadCalculator.Units.InSeconds")+")");
				allenCunneen_result_EV.setText(Language.tr("LoadCalculator.AverageResidenceTime")+" E[V]= ("+Language.tr("LoadCalculator.Units.InSeconds")+")");
			} else {
				allenCunneen_result_rho.setText("<html><body>"+Language.tr("LoadCalculator.WorkLoad")+" (rho) &rho;="+NumberTools.formatNumber(100*rho,2)+"%</body></html>");
				allenCunneen_result_ENQ.setText("<html><body>"+Language.tr("LoadCalculator.AverageQueueLength")+" E[N<sub>Q</sub>]="+NumberTools.formatNumber(ENQ,2)+"</body></html>");
				allenCunneen_result_EN.setText(Language.tr("LoadCalculator.AverageNumberOfClientsInTheSystem")+" E[N]="+NumberTools.formatNumber(EN,2));
				allenCunneen_result_EW.setText(Language.tr("LoadCalculator.AverageWaitingTime")+" E[W]="+NumberTools.formatNumber(EW*60,2)+" ("+Language.tr("LoadCalculator.Units.InSeconds")+")");
				allenCunneen_result_EV.setText(Language.tr("LoadCalculator.AverageResidenceTime")+" E[V]="+NumberTools.formatNumber(EV*60,2)+" ("+Language.tr("LoadCalculator.Units.InSeconds")+")");
			}
		}

		private void recalcWartezeittoleranz() {
			Double wartezeitObj=NumberTools.getNotNegativeDouble(toleranz_Wartezeit,true);
			Double abrruchrateObj=NumberTools.getProbability(toleranz_Abbruchrate,true);
			if (wartezeitObj==null || abrruchrateObj==null) {toleranz_Mittelwert.setText(Language.tr("LoadCalculator.InvalidInput")); return;}
			double W=wartezeitObj;
			double A=abrruchrateObj;

			/* E[WT]=E[W]/P(A) */
			double WT=0;
			if (A>0) WT=W/A;

			toleranz_Mittelwert.setText("<html><body><b>E[WT]="+NumberTools.formatNumber(WT)+" "+Language.tr("LoadCalculator.Units.Seconds")+"</b></body></html>");
		}

		private void recalc() {
			recalcAuslastung();
			recalcErlangB();
			recalcErlangC();
			recalcErlangCext();
			recalcAllenCunneen();
			recalcWartezeittoleranz();
		}

		@Override public void stateChanged(ChangeEvent e) {recalc();}
		@Override public void keyTyped(KeyEvent e) {recalc();}
		@Override public void keyPressed(KeyEvent e) {recalc();}
		@Override public void keyReleased(KeyEvent e) {recalc();}
	}

	private class LinkListener implements MouseListener {
		private final String url;

		public LinkListener(String URL) {this.url=URL;}

		@Override
		public void mouseClicked(MouseEvent e) {
			try {Desktop.getDesktop().browse(new URI(url));} catch (IOException | URISyntaxException e1) {
				MsgBox.error(LoadCalculatorDialog.this,Language.tr("Window.Info.NoInternetConnection"),String.format(Language.tr("Window.Info.NoInternetConnection.ModelOverview"),url.toString()));
			}
		}

		@Override public void mousePressed(MouseEvent e) {}
		@Override public void mouseReleased(MouseEvent e) {}
		@Override public void mouseEntered(MouseEvent e) {}
		@Override public void mouseExited(MouseEvent e) {}
	}
}