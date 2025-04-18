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
package language;

import java.util.Locale;

import javax.swing.JComponent;

import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.swing.JDataDistributionEditPanel;
import mathtools.distribution.swing.JDataDistributionPanel;
import mathtools.distribution.swing.JDataLoader;
import mathtools.distribution.swing.JDistributionEditorPanel;
import mathtools.distribution.swing.JDistributionPanel;
import mathtools.distribution.tools.DistributionFitter;
import mathtools.distribution.tools.DistributionFitterBase;
import mathtools.distribution.tools.DistributionTools;
import statistics.StatisticsSimulationBaseData;
import systemtools.GUITools;
import systemtools.JRegExWikipediaLinkLabel;
import systemtools.MsgBox;
import systemtools.SetupBase;
import systemtools.commandline.BaseCommandLineSystem;
import systemtools.commandline.CommandLineDialog;
import systemtools.help.HelpBase;
import systemtools.statistics.StatisticsBasePanel;
import ui.MainFrame;
import ui.MainPanel;
import xml.XMLData;
import xml.XMLTools;

/**
 * Setzt die statischen Spracheinstellungen
 * @author Alexander Herzog
 */
public class LanguageStaticLoader {
	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden. Sie stellt lediglich die statische Methode {@link #setLanguage()} zur Verf�gung.
	 */
	private LanguageStaticLoader() {}

	/**
	 * Stellt die Sprache f�r Dialog, Verteilungs-Editoren usw. ein.
	 */
	public static void setLanguage() {
		Locale locale=Locale.US;
		if (Language.tr("Numbers.Language").toLowerCase().equals("de")) locale=Locale.GERMANY;

		/* Zahlenformate und Ja/Nein-Dialoge */
		NumberTools.setLocale(locale);
		JComponent.setDefaultLocale(locale);
		Locale.setDefault(locale);

		/* Message-Dialoge */
		MsgBox.TitleInformation=Language.tr("Dialog.Title.Information");
		MsgBox.TitleWarning=Language.tr("Dialog.Title.Warning");
		MsgBox.TitleError=Language.tr("Dialog.Title.Error");
		MsgBox.TitleConfirmation=Language.tr("Dialog.Title.Confirmation");
		MsgBox.TitleAlternatives=Language.tr("Dialog.Title.Alternatives");
		MsgBox.OverwriteTitle=Language.tr("Dialog.Overwrite.Title");
		MsgBox.OverwriteInfo=Language.tr("Dialog.Overwrite.Info");
		MsgBox.OverwriteYes=Language.tr("Dialog.Overwrite.Yes");
		MsgBox.OverwriteYesInfo=Language.tr("Dialog.Overwrite.Yes.Info");
		MsgBox.OverwriteNo=Language.tr("Dialog.Overwrite.No");
		MsgBox.OverwriteNoInfo=Language.tr("Dialog.Overwrite.No.Info");
		MsgBox.OptionYes=Language.tr("Dialog.Button.Yes");
		MsgBox.OptionNo=Language.tr("Dialog.Button.No");
		MsgBox.OptionCancel=Language.tr("Dialog.Button.Cancel");
		MsgBox.OptionSaveYes=Language.tr("Dialog.SaveNow.Yes");
		MsgBox.OptionSaveYesInfo=Language.tr("Dialog.SaveNow.Yes.Info");
		MsgBox.OptionSaveNo=Language.tr("Dialog.SaveNow.No");
		MsgBox.OptionSaveNoInfo=Language.tr("Dialog.SaveNow.No.Info");
		MsgBox.OptionSaveCancelInfo=Language.tr("Dialog.SaveNow.Cancel.Info");
		MsgBox.OpenURLInfo=Language.tr("Dialog.OpenURL.Info");
		MsgBox.OpenURLInfoYes=Language.tr("Dialog.OpenURL.InfoYes");
		MsgBox.OpenURLInfoNo=Language.tr("Dialog.OpenURL.InfoNo");
		MsgBox.OptionCopyURL=Language.tr("Dialog.OpenURL.CopyURL");
		MsgBox.OptionInfoCopyURL=Language.tr("Dialog.OpenURL.CopyURLInfo");
		MsgBox.OpenURLErrorTitle=Language.tr("Window.Info.NoInternetConnection");
		MsgBox.OpenURLErrorMessage=Language.tr("Window.Info.NoInternetConnection.Address");
		MsgBox.ActiveLocale=locale;

		/* Sucheinstellungen */
		JRegExWikipediaLinkLabel.title=Language.tr("RegExWikipdiaLink.Title");
		JRegExWikipediaLinkLabel.tooltip=Language.tr("RegExWikipdiaLink.Tooltip");
		JRegExWikipediaLinkLabel.url=Language.tr("RegExWikipdiaLink.URL");

		/* Verteilungen */
		DistributionTools.DistData=Language.trAll("Distribution.Data");
		DistributionTools.DistDataWikipedia=Language.tr("Distribution.DataWikipedia");
		DistributionTools.DistNever=Language.trAll("Distribution.Never");
		DistributionTools.DistInfinite=Language.trAll("Distribution.Infinite");
		DistributionTools.DistPoint=Language.trAll("Distribution.Point");
		DistributionTools.DistPointInfo=Language.tr("Distribution.PointInfo");
		DistributionTools.DistUniform=Language.trAll("Distribution.Uniform");
		DistributionTools.DistUniformWikipedia=Language.tr("Distribution.UniformWikipedia");
		DistributionTools.DistUniformInfo=Language.tr("Distribution.UniformInfo");
		DistributionTools.DistExp=Language.trAll("Distribution.Exp");
		DistributionTools.DistExpWikipedia=Language.tr("Distribution.ExpWikipedia");
		DistributionTools.DistExpInfo=Language.tr("Distribution.ExpInfo");
		DistributionTools.DistNormal=Language.trAll("Distribution.Normal");
		DistributionTools.DistNormalWikipedia=Language.tr("Distribution.NormalWikipedia");
		DistributionTools.DistNormalInfo=Language.tr("Distribution.NormalInfo");
		DistributionTools.DistLogNormal=Language.trAll("Distribution.LogNormal");
		DistributionTools.DistLogNormalWikipedia=Language.tr("Distribution.LogNormalWikipedia");
		DistributionTools.DistLogNormalInfo=Language.tr("Distribution.LogNormalInfo");
		DistributionTools.DistErlang=Language.trAll("Distribution.Erlang");
		DistributionTools.DistErlangWikipedia=Language.tr("Distribution.ErlangWikipedia");
		DistributionTools.DistErlangInfo=Language.tr("Distribution.ErlangInfo");
		DistributionTools.DistGamma=Language.trAll("Distribution.Gamma");
		DistributionTools.DistGammaWikipedia=Language.tr("Distribution.GammaWikipedia");
		DistributionTools.DistGammaInfo=Language.tr("Distribution.GammaInfo");
		DistributionTools.DistBeta=Language.trAll("Distribution.Beta");
		DistributionTools.DistBetaWikipedia=Language.tr("Distribution.BetaWikipedia");
		DistributionTools.DistCauchy=Language.trAll("Distribution.Cauchy");
		DistributionTools.DistCauchyWikipedia=Language.tr("Distribution.CauchyWikipedia");
		DistributionTools.DistLogCauchy=Language.trAll("Distribution.LogCauchy");
		DistributionTools.DistLogCauchyWikipedia=Language.tr("Distribution.LogCauchyWikipedia");
		DistributionTools.DistWeibull=Language.trAll("Distribution.Weibull");
		DistributionTools.DistWeibullWikipedia=Language.tr("Distribution.WeibullWikipedia");
		DistributionTools.DistChi=Language.trAll("Distribution.Chi");
		DistributionTools.DistChiWikipedia=Language.tr("Distribution.ChiWikipedia");
		DistributionTools.DistChiSquare=Language.trAll("Distribution.ChiSquare");
		DistributionTools.DistChiSquareWikipedia=Language.tr("Distribution.ChiSquareWikipedia");
		DistributionTools.DistF=Language.trAll("Distribution.F");
		DistributionTools.DistFWikipedia=Language.tr("Distribution.FWikipedia");
		DistributionTools.DistJohnson=Language.trAll("Distribution.DistJohnsonSU");
		DistributionTools.DistJohnsonWikipedia=Language.tr("Distribution.DistJohnsonSUWikipedia");
		DistributionTools.DistTriangular=Language.trAll("Distribution.Triangular");
		DistributionTools.DistTriangularWikipedia=Language.tr("Distribution.TriangularWikipedia");
		DistributionTools.DistTriangularInfo=Language.tr("Distribution.TriangularInfo");
		DistributionTools.DistTrapezoid=Language.trAll("Distribution.Trapezoid");
		DistributionTools.DistTrapezoidWikipedia=Language.tr("Distribution.TrapezoidWikipedia");
		DistributionTools.DistPert=Language.trAll("Distribution.Pert");
		DistributionTools.DistPertWikipedia=Language.tr("Distribution.PertWikipedia");
		DistributionTools.DistLaplace=Language.trAll("Distribution.Laplace");
		DistributionTools.DistLaplaceWikipedia=Language.tr("Distribution.LaplaceWikipedia");
		DistributionTools.DistPareto=Language.trAll("Distribution.Pareto");
		DistributionTools.DistParetoWikipedia=Language.tr("Distribution.ParetoWikipedia");
		DistributionTools.DistLogistic=Language.trAll("Distribution.Logistic");
		DistributionTools.DistLogisticWikipedia=Language.tr("Distribution.LogisticWikipedia");
		DistributionTools.DistInverseGaussian=Language.trAll("Distribution.InverseGaussian");
		DistributionTools.DistInverseGaussianWikipedia=Language.tr("Distribution.InverseGaussianWikipedia");
		DistributionTools.DistRayleigh=Language.trAll("Distribution.Rayleigh");
		DistributionTools.DistRayleighWikipedia=Language.tr("Distribution.RayleighWikipedia");
		DistributionTools.DistLogLogistic=Language.trAll("Distribution.LogLogistic");
		DistributionTools.DistLogLogisticWikipedia=Language.tr("Distribution.LogLogisticWikipedia");
		DistributionTools.DistPower=Language.trAll("Distribution.Power");
		DistributionTools.DistPowerWikipedia=Language.tr("Distribution.PowerWikipedia");
		DistributionTools.DistGumbel=Language.trAll("Distribution.Gumbel");
		DistributionTools.DistGumbelWikipedia=Language.tr("Distribution.GumbelWikipedia");
		DistributionTools.DistFatigueLife=Language.trAll("Distribution.FatigueLife");
		DistributionTools.DistFatigueLifeWikipedia=Language.tr("Distribution.FatigueLifeWikipedia");
		DistributionTools.DistFrechet=Language.trAll("Distribution.Frechet");
		DistributionTools.DistFrechetWikipedia=Language.tr("Distribution.FrechetWikipedia");
		DistributionTools.DistHyperbolicSecant=Language.trAll("Distribution.HyperbolicSecant");
		DistributionTools.DistHyperbolicSecantWikipedia=Language.tr("Distribution.HyperbolicSecantWikipedia");
		DistributionTools.DistSawtoothLeft=Language.trAll("Distribution.SawtoothLeft");
		DistributionTools.DistSawtoothLeftWikipedia=Language.tr("Distribution.SawtoothLeftWikipedia");
		DistributionTools.DistSawtoothRight=Language.trAll("Distribution.SawtoothRight");
		DistributionTools.DistSawtoothRightWikipedia=Language.tr("Distribution.SawtoothRightWikipedia");
		DistributionTools.DistLevy=Language.trAll("Distribution.Levy");
		DistributionTools.DistLevyWikipedia=Language.tr("Distribution.LevyWikipedia");
		DistributionTools.DistMaxwellBoltzmann=Language.trAll("Distribution.MaxwellBoltzmann");
		DistributionTools.DistMaxwellBoltzmannWikipedia=Language.tr("Distribution.MaxwellBoltzmannWikipedia");
		DistributionTools.DistStudentT=Language.trAll("Distribution.StudentT");
		DistributionTools.DistStudentTWikipedia=Language.tr("Distribution.StudentTWikipedia");
		DistributionTools.DistHyperGeom=Language.trAll("Distribution.HyperGeom");
		DistributionTools.DistHyperGeomWikipedia=Language.tr("Distribution.HyperGeomWikipedia");
		DistributionTools.DistBinomial=Language.trAll("Distribution.Binomial");
		DistributionTools.DistBinomialWikipedia=Language.tr("Distribution.BinomialWikipedia");
		DistributionTools.DistPoisson=Language.trAll("Distribution.Poisson");
		DistributionTools.DistPoissonWikipedia=Language.tr("Distribution.PoissonWikipedia");
		DistributionTools.DistNegativeBinomial=Language.trAll("Distribution.NegativeBinomial");
		DistributionTools.DistNegativeBinomialWikipedia=Language.tr("Distribution.NegativeBinomialWikipedia");
		DistributionTools.DistNegativeHyperGeom=Language.trAll("Distribution.NegativeHyperGeom");
		DistributionTools.DistNegativeHyperGeomWikipedia=Language.tr("Distribution.NegativeHyperGeomWikipedia");
		DistributionTools.DistZeta=Language.trAll("Distribution.Zeta");
		DistributionTools.DistZetaWikipedia=Language.tr("Distribution.ZetaWikipedia");
		DistributionTools.DistDiscreteUniform=Language.trAll("Distribution.DiscreteUniform");
		DistributionTools.DistDiscreteUniformWikipedia=Language.tr("Distribution.DiscreteUniformWikipedia");
		DistributionTools.DistGeometric=Language.trAll("Distribution.Geometric");
		DistributionTools.DistGeometricWikipedia=Language.tr("Distribution.GeometricWikipedia");
		DistributionTools.DistLogarithmic=Language.trAll("Distribution.Logarithmic");
		DistributionTools.DistLogarithmicWikipedia=Language.tr("Distribution.LogarithmicWikipedia");
		DistributionTools.DistBorel=Language.trAll("Distribution.Borel");
		DistributionTools.DistBorelWikipedia=Language.tr("Distribution.BorelWikipedia");
		DistributionTools.DistHalfNormal=Language.trAll("Distribution.HalfNormal");
		DistributionTools.DistHalfNormalWikipedia=Language.tr("Distribution.HalfNormalWikipedia");
		DistributionTools.DistUQuadratic=Language.trAll("Distribution.UQuadratic");
		DistributionTools.DistUQuadraticWikipedia=Language.tr("Distribution.UQuadraticWikipedia");
		DistributionTools.DistReciprocal=Language.trAll("Distribution.Reciprocal");
		DistributionTools.DistReciprocalWikipedia=Language.tr("Distribution.ReciprocalWikipedia");
		DistributionTools.DistKumaraswamy=Language.trAll("Distribution.Kumaraswamy");
		DistributionTools.DistKumaraswamyWikipedia=Language.tr("Distribution.KumaraswamyWikipedia");
		DistributionTools.DistIrwinHall=Language.trAll("Distribution.IrwinHall");
		DistributionTools.DistIrwinHallWikipedia=Language.tr("Distribution.IrwinHallWikipedia");
		DistributionTools.DistSine=Language.trAll("Distribution.Sine");
		DistributionTools.DistArcsine=Language.trAll("Distribution.Arcsine");
		DistributionTools.DistWignerHalfCircle=Language.trAll("Distribution.WignerHalfCircle");
		DistributionTools.DistWignerHalfCircleWikipedia=Language.tr("Distribution.WignerHalfCircleWikipedia");
		DistributionTools.DistUnknown=Language.tr("Distribution.Unknown");
		DistributionTools.DistDataPoint=Language.tr("Distribution.DataPoint");
		DistributionTools.DistDataPoints=Language.tr("Distribution.DataPoints");
		DistributionTools.DistRange=Language.tr("Distribution.Range");
		DistributionTools.DistLocation=Language.tr("Distribution.Location");
		DistributionTools.DistScale=Language.tr("Distribution.Scale");
		DistributionTools.DistMostLikely=Language.tr("Distribution.MostLikely");
		DistributionTools.DistDegreesOfFreedom=Language.tr("Distribution.DegreesOfFreedom");
		DistributionTools.DistMean=Language.tr("Distribution.Mean");
		DistributionTools.DistMeanWikipedia=Language.tr("Distribution.Mean.Wikipedia");
		DistributionTools.DistStdDev=Language.tr("Distribution.StdDev");
		DistributionTools.DistStdDevWikipedia=Language.tr("Distribution.StdDev.Wikipedia");
		DistributionTools.DistCV=Language.tr("Distribution.CV");
		DistributionTools.DistCVWikipedia=Language.tr("Distribution.CV.Wikipedia");
		DistributionTools.DistSkewness=Language.tr("Distribution.Skewness");
		DistributionTools.DistSkewnessWikipedia=Language.tr("Distribution.Skewness.Wikipedia");
		DistributionTools.DistMode=Language.tr("Distribution.Mode");
		DistributionTools.DistModeWikipedia=Language.tr("Distribution.Mode.Wikipedia");
		DistributionTools.DistParameter=Language.tr("Distribution.Parameter");
		JDataDistributionPanel.errorString=Language.tr("Distribution.NoDistribution");

		/* DistributionFitter */
		DistributionFitterBase.ErrorInvalidFormat=Language.tr("DistributionFitter.ErrorInvalidFormat");
		DistributionFitterBase.ValueCount=Language.tr("DistributionFitter.ValueCount");
		DistributionFitterBase.ValueRange=Language.tr("DistributionFitter.ValueRange");
		DistributionFitterBase.Mean=Language.tr("DistributionFitter.Mean");
		DistributionFitterBase.StdDev=Language.tr("DistributionFitter.StdDev");
		DistributionFitter.ComparedDistributions=Language.tr("DistributionFitter.ComparedDistributions");
		DistributionFitter.MeanSquares=Language.tr("DistributionFitter.MeanSquares");
		DistributionFitter.PValue=Language.tr("DistributionFitter.PValue");
		DistributionFitter.PValueChiSqr=Language.tr("DistributionFitter.PValueChiSqr");
		DistributionFitter.PValueAndersonDarling=Language.tr("DistributionFitter.PValueAndersonDarling");
		DistributionFitter.BestFitFor=Language.tr("DistributionFitter.BestFitFor");
		DistributionFitter.FitError=Language.tr("DistributionFitter.FitError");
		DistributionFitter.NotFit=Language.tr("DistributionFitter.NotFit");

		/* Table */
		Table.BoolTrue=Language.tr("Table.BoolTrue");
		Table.BoolFalse=Language.tr("Table.BoolFalse");
		Table.TableFileTableName=Language.tr("Table.TableFileTableName");
		Table.FileTypeAll=Language.tr("FileType.AllTables");
		Table.FileTypeText=Language.tr("FileType.Text");
		Table.FileTypeCSV=Language.tr("FileType.CSV");
		Table.FileTypeCSVR=Language.tr("FileType.CSVR");
		Table.FileTypeDIF=Language.tr("FileType.DIF");
		Table.FileTypeSYLK=Language.tr("FileType.SYLK");
		Table.FileTypeDBF=Language.tr("FileType.DBF");
		Table.FileTypeExcelOld=Language.tr("FileType.ExcelOld");
		Table.FileTypeExcel=Language.tr("FileType.Excel");
		Table.FileTypeODS=Language.tr("FileType.FileTypeODS");
		Table.FileTypeSQLite=Language.tr("FileType.SQLite");
		Table.FileTypeWord=Language.tr("FileType.WordTable");
		Table.FileTypeHTML=Language.tr("FileType.HTMLTable");
		Table.FileTypeTex=Language.tr("FileType.LaTeXTable");
		Table.FileTypeTypst=Language.tr("FileType.TypstTable");
		Table.LoadErrorFirstCellInvalid=Language.tr("Table.LoadErrorFirstCellInvalid");
		Table.LoadErrorLastCellInvalid=Language.tr("Table.LoadErrorLastCellInvalid");
		Table.LoadErrorCellRangeInvalid=Language.tr("Table.LoadErrorCellRangeInvalid");
		Table.LoadErrorCellNotInTable=Language.tr("Table.LoadErrorCellNotInTable");
		Table.LoadErrorCellValueNaN=Language.tr("Table.LoadErrorCellValueNaN");
		Table.ExportTitle=MainFrame.PROGRAM_NAME;

		/* JDataLoader */
		JDataLoader.Title=Language.tr("JDataLoader.Title");
		JDataLoader.Sheet=Language.tr("JDataLoader.Sheet");
		JDataLoader.SelectArea=Language.tr("JDataLoader.SelectArea");
		JDataLoader.ButtonOk=Language.tr("Dialog.Button.Ok");
		JDataLoader.ButtonCancel=Language.tr("Dialog.Button.Cancel");
		JDataLoader.ImportErrorTitle=Language.tr("Dialog.Title.Error");
		JDataLoader.ImportErrorNoArea=Language.tr("JDataLoader.ImportErrorNoArea");
		JDataLoader.ImportErrorTooFewCells=Language.tr("JDataLoader.ImportErrorTooFewCells");
		JDataLoader.ImportErrorTooManyCells=Language.tr("JDataLoader.ImportErrorTooManyCells");
		JDataLoader.ImportErrorInvalidValue=Language.tr("JDataLoader.ImportErrorInvalidValue");
		JDataLoader.ImportErrorInvalidData=Language.tr("JDataLoader.ImportErrorInvalidData");
		JDataLoader.ImportErrorFileError=Language.tr("JDataLoader.ImportErrorFileError");

		/* JDistributionEditorPanel */
		JDistributionEditorPanel.DialogTitle=Language.tr("JDistributionEditor.Title");
		JDistributionEditorPanel.ButtonOk=Language.tr("Dialog.Button.Ok");
		JDistributionEditorPanel.ButtonCancel=Language.tr("Dialog.Button.Cancel");
		JDistributionEditorPanel.ButtonCopyData=Language.tr("Dialog.Button.Copy");
		JDistributionEditorPanel.ButtonPasteData=Language.tr("Dialog.Button.Paste");
		JDistributionEditorPanel.ButtonPasteAndFillData=Language.tr("Dialog.Button.PasteDoNotScale");
		JDistributionEditorPanel.ButtonPasteAndFillDataTooltip=Language.tr("Dialog.Button.PasteDoNotScale.Tooltip");
		JDistributionEditorPanel.ButtonLoadData=Language.tr("JDistributionEditor.Load");
		JDistributionEditorPanel.ButtonLoadDataDialogTitle=Language.tr("JDistributionEditor.Load.Title");
		JDistributionEditorPanel.ButtonSaveData=Language.tr("JDistributionEditor.Save");
		JDistributionEditorPanel.ButtonSaveDataDialogTitle=Language.tr("JDistributionEditor.Save.Title");
		JDistributionEditorPanel.DistData=Language.tr("JDistributionEditor.DataVector");
		JDistributionEditorPanel.DistMean=Language.tr("Distribution.E");
		JDistributionEditorPanel.DistStdDev=Language.tr("Distribution.StdDev");
		JDistributionEditorPanel.DistUniformStart=Language.tr("Distribution.Uniform.Start");
		JDistributionEditorPanel.DistUniformEnd=Language.tr("Distribution.Uniform.End");
		JDistributionEditorPanel.DistDegreesOfFreedom=Language.tr("Distribution.DegreesOfFreedom");
		JDistributionEditorPanel.DistDegreesOfFreedomNumerator=Language.tr("Distribution.DegreesOfFreedom.Numerator");
		JDistributionEditorPanel.DistDegreesOfFreedomDenominator=Language.tr("Distribution.DegreesOfFreedom.Denominator");
		JDistributionEditorPanel.DistRadius=Language.tr("Distribution.Radius");
		JDistributionEditorPanel.ChangeValueDown=Language.tr("JDistributionEditor.ValueDown");
		JDistributionEditorPanel.ChangeValueUp=Language.tr("JDistributionEditor.ValueUp");
		JDistributionEditorPanel.DistMostLikely=Language.tr("Distribution.MostLikely");
		JDistributionEditorPanel.SetupListTitle=Language.tr("Distribution.SetupList.Title");
		JDistributionEditorPanel.SetupListInfo=Language.tr("Distribution.SetupList.Info");
		JDistributionEditorPanel.SetupListDivier=Language.tr("Distribution.SetupList.Divider");
		JDistributionEditorPanel.SetupListInfoSingular=Language.tr("Distribution.SetupList.Info.Singular");
		JDistributionEditorPanel.SetupListInfoPlural=Language.tr("Distribution.SetupList.Info.Plural");


		/* JDataDistributionEditPanel */
		JDataDistributionEditPanel.ButtonCopy=Language.tr("Dialog.Button.Copy");
		JDataDistributionEditPanel.ButtonCopyTooltip=Language.tr("JDistributionEditor.Copy.Info");
		JDataDistributionEditPanel.ButtonCopyTable=Language.tr("JDistributionEditor.Copy.Table");
		JDataDistributionEditPanel.ButtonCopyGraphics=Language.tr("JDistributionEditor.Copy.Graphics");
		JDataDistributionEditPanel.ButtonPaste=Language.tr("Dialog.Button.Paste");
		JDataDistributionEditPanel.ButtonPasteTooltip=Language.tr("JDistributionEditor.Paste.Info");
		JDataDistributionEditPanel.ButtonLoad=Language.tr("JDistributionEditor.Load");
		JDataDistributionEditPanel.ButtonLoadTooltip=Language.tr("JDistributionEditor.Load.Info");
		JDataDistributionEditPanel.ButtonLoadDialogTitle=Language.tr("JDistributionEditor.Load.Title");
		JDataDistributionEditPanel.ButtonSave=Language.tr("JDistributionEditor.Save");
		JDataDistributionEditPanel.ButtonSaveTooltip=Language.tr("JDistributionEditor.Save.Info");
		JDataDistributionEditPanel.ButtonSaveDialogTitle=Language.tr("JDistributionEditor.Save.Title");
		JDataDistributionEditPanel.LoadError=Language.tr("JDistributionEditor.Load.Error");
		JDataDistributionEditPanel.LoadErrorTitle=Language.tr("Dialog.Title.Error");
		JDataDistributionEditPanel.SaveOverwriteWarning=Language.tr("Dialog.Overwrite.Info");
		JDataDistributionEditPanel.SaveOverwriteWarningTitle=Language.tr("Dialog.Title.Warning");
		JDataDistributionEditPanel.CountDensityLabel=Language.tr("JDistributionEditor.Density.Label");
		JDataDistributionEditPanel.CumulativeProbabilityLabel=Language.tr("JDistributionEditor.CumulativeProbability.Label");

		/* JDistributionPanel */
		JDistributionPanel.ErrorString=Language.tr("JDistributionEditor.NoDistribution");
		JDistributionPanel.EditButtonLabel=Language.tr("Dialog.Button.Edit");
		JDistributionPanel.EditButtonTooltip=Language.tr("JDistributionEditor.Edit.Info");
		JDistributionPanel.EditButtonLabelDisabled=Language.tr("JDistributionEditor.Edit.Disabled");
		JDistributionPanel.CopyButtonLabel=Language.tr("Dialog.Button.Copy");
		JDistributionPanel.CopyButtonTable=Language.tr("Dialog.Button.Copy.Table");
		JDistributionPanel.CopyButtonRandomNumbers=Language.tr("Dialog.Button.Copy.RandomNumbers");
		JDistributionPanel.CopyButtonImage=Language.tr("Dialog.Button.Copy.Image");
		JDistributionPanel.SaveButtonLabel=Language.tr("Dialog.Button.Save");
		JDistributionPanel.SaveButtonTable=Language.tr("Dialog.Button.Save.Table");
		JDistributionPanel.SaveButtonRandomNumbers=Language.tr("Dialog.Button.Save.RandomNumbers");
		JDistributionPanel.SaveButtonImage=Language.tr("Dialog.Button.Save.Image");
		JDistributionPanel.InfoButtonLabel=Language.tr("JDistributionEditor.Info");
		JDistributionPanel.InfoButtonTooltip=Language.tr("JDistributionEditor.Info.Tooltip");
		JDistributionPanel.InfoWindowParameters=Language.tr("JDistributionEditor.InfoWindow.Parameters");
		JDistributionPanel.InfoWindowMore=Language.tr("JDistributionEditor.InfoWindow.MoreInfo");
		JDistributionPanel.WebAppButtonLabel=Language.tr("JDistributionEditor.InfoWindow.WebApp");
		JDistributionPanel.WikiButtonLabel=Language.tr("JDistributionEditor.Wikipedia");
		JDistributionPanel.WikiButtonTooltip=Language.tr("JDistributionEditor.Wikipedia.Info");
		JDistributionPanel.ChangeDistributionType=Language.tr("JDistributionEditor.QuickSelect");
		JDistributionPanel.ChangeDistributionTypeHighlightList=Language.tr("JDistributionEditor.QuickSelectInfo");
		JDistributionPanel.DensityLabel=Language.tr("JDistributionEditor.Density.Label");
		JDistributionPanel.CountDensityLabel=Language.tr("JDistributionEditor.Density.Label");
		JDistributionPanel.CumulativeProbabilityLabel=Language.tr("JDistributionEditor.CumulativeProbability.Label");
		JDistributionPanel.StoreGraphicsDialogTitle=Language.tr("JDistributionEditor.Save.Graphics");
		JDistributionPanel.FileTypeJPEG=Language.tr("FileType.jpeg");
		JDistributionPanel.FileTypeGIF=Language.tr("FileType.gif");
		JDistributionPanel.FileTypePNG=Language.tr("FileType.png");
		JDistributionPanel.FileTypeBMP=Language.tr("FileType.bmp");
		JDistributionPanel.FileTypeTIFF=Language.tr("FileType.tiff");
		JDistributionPanel.GraphicsFileOverwriteWarning=Language.tr("Dialog.Overwrite.Info");
		JDistributionPanel.GraphicsFileOverwriteWarningTitle=Language.tr("Dialog.Title.Warning");
		JDistributionPanel.GraphicsOpenURLWarning=Language.tr("Dialog.OpenURL.Info");
		JDistributionPanel.GraphicsOpenURLWarningTitle=Language.tr("Dialog.Title.Warning");
		JDistributionPanel.RandomNumbersCount=Language.tr("Dialog.RandomNumbers.Count");
		JDistributionPanel.RandomNumbersError=Language.tr("Dialog.RandomNumbers.Error");
		JDistributionPanel.ToCalculationExpression=Language.tr("Dialog.ConvertDistributionToExpression");

		/* GUITools */
		GUITools.errorNoGraphicsOutputAvailable=Language.tr("Window.ErrorNoGraphics");

		/* Setup */
		SetupBase.errorSaveTitle=Language.tr("Setup.SaveError.Title");
		SetupBase.errorSaveMessage=Language.tr("Setup.SaveError.Info");

		/* Allgemeines */
		MainPanel.UNSAVED_MODEL=Language.tr("Window.UnsavedFile");

		/* Hilfe */
		HelpBase.title=Language.tr("Window.Help");
		HelpBase.buttonClose=Language.tr("Dialog.Button.Close");
		HelpBase.buttonCloseInfo=Language.tr("Help.Close.Info");
		HelpBase.buttonStartPage=Language.tr("Help.StartPage");
		HelpBase.buttonStartPageInfo=Language.tr("Help.StartPage.Info");
		HelpBase.buttonBack=Language.tr("Dialog.Button.Back");
		HelpBase.buttonBackInfo=Language.tr("Help.Back.Info");
		HelpBase.buttonNext=Language.tr("Dialog.Button.Forward");
		HelpBase.buttonNextInfo=Language.tr("Help.Forward.Info");
		HelpBase.buttonContent=Language.tr("Help.Content");
		HelpBase.buttonContentInfo=Language.tr("Help.Content.Info");
		HelpBase.buttonSearch=Language.tr("Help.Search");
		HelpBase.buttonSearchInfo=Language.tr("Help.Search.Info");
		HelpBase.buttonSearchString=Language.tr("Help.Search.SearchString");
		HelpBase.buttonSearchNoHitSelected=Language.tr("Help.Search.NoHitSelected");
		HelpBase.buttonSearchResultTypePage=Language.tr("Help.Search.Type.Page");
		HelpBase.buttonSearchResultTypeIndex=Language.tr("Help.Search.Type.Index");
		HelpBase.buttonSearchResultOnPage=Language.tr("Help.Search.ResultOnPage");
		HelpBase.buttonSearchResultCountSingular=Language.tr("Help.Search.ResultCountSingular");
		HelpBase.buttonSearchResultCountPlural=Language.tr("Help.Search.ResultCountPlural");
		HelpBase.buttonSearchResultSelect=Language.tr("Help.Search.ResultSelect");
		HelpBase.buttonSearchTabSearch=Language.tr("Help.Search.Search");
		HelpBase.buttonSearchTabAllPages=Language.tr("Help.Search.AllPages");
		HelpBase.errorNoEMailTitle=Language.tr("Window.Info.NoEMailProgram.Title");
		HelpBase.errorNoEMailInfo=Language.tr("Window.Info.NoHTMLPrint.Info");
		HelpBase.errorNoInternetTitle=Language.tr("Window.Info.NoInternetConnection");
		HelpBase.errorNoInternetInfo=Language.tr("Window.Info.NoInternetConnection.Address");

		/* XML-Daten */
		XMLData.errorRootElementName=Language.tr("XML.RootElementNameError");
		XMLData.errorOutOfMemory=Language.tr("XML.OutOfMemoryError");
		XMLTools.errorInitXMLInterpreter=Language.tr("XML.InterpreterError");
		XMLTools.errorXMLProcess=Language.tr("XML.InterpreterCouldNotProcessData");
		XMLTools.errorXMLProcessFile=Language.tr("XML.ErrorProcessingFile");
		XMLTools.errorInternalErrorNoInputObject=Language.tr("XML.NoInputObjectSelected");
		XMLTools.errorInternalErrorNoOutputObject=Language.tr("XML.NoOutputObjectSelected");
		XMLTools.errorZipCreating=Language.tr("XML.ErrorCreatingZipStream");
		XMLTools.errorZipCreatingFile=Language.tr("XML.ErrorCreatingZipFile");
		XMLTools.errorZipClosing=Language.tr("XML.ErrorClosingStream");
		XMLTools.errorOpeningFile=Language.tr("XML.ErrorOpeningFile");
		XMLTools.errorClosingFile=Language.tr("XML.ErrorClosingFile");
		XMLTools.errorCanceledByUser=Language.tr("XML.ErrorCanceledByUser");
		XMLTools.errorEncryptingFile=Language.tr("XML.ErrorEncryptingFile");
		XMLTools.errorDecryptingFile=Language.tr("XML.ErrorDecryptingFile");
		XMLTools.errorFileDoesNotExists=Language.tr("XML.FileNotFound");
		XMLTools.errorNoEmbeddedXMLData=Language.tr("XML.ErrorNoEmbeddedData");
		XMLTools.errorStreamProcessing=Language.tr("XML.ErrorProcessingStream");
		XMLTools.errorStreamClosing=Language.tr("XML.ErrorClosingStream");
		XMLTools.enterPassword=Language.tr("XML.EnterPassword");
		XMLTools.fileTypeXML=Language.tr("FileType.xml");
		XMLTools.fileTypeCompressedXML=Language.tr("FileType.xmz");
		XMLTools.fileTypeTARCompressedXML=Language.tr("FileType.targz");
		XMLTools.fileTypeJSON=Language.tr("FileType.json");
		XMLTools.fileTypeEncryptedXML=Language.tr("FileType.cs");
		XMLTools.fileTypeAll=Language.tr("FileType.AllSupportedFiles");
		XMLTools.xmlComment=String.format(Language.tr("XML.Comment"),MainFrame.PROGRAM_NAME,"https://"+MainPanel.WEB_URL);

		/* Statistik-Basis-Panel */
		StatisticsBasePanel.typeText=Language.tr("Statistic.Type.Text");
		StatisticsBasePanel.typeTable=Language.tr("Statistic.Type.Table");
		StatisticsBasePanel.typeImage=Language.tr("Statistic.Type.Image");
		StatisticsBasePanel.typeNoData=Language.tr("Statistic.Type.NoData");
		StatisticsBasePanel.overwriteTitle=Language.tr("Dialog.Overwrite.Title");
		StatisticsBasePanel.overwriteInfo=Language.tr("Dialog.Overwrite.Info");
		StatisticsBasePanel.writeErrorTitle=Language.tr("Statistic.WriteError.Title");
		StatisticsBasePanel.writeErrorInfo=Language.tr("Statistic.WriteError.Info");
		StatisticsBasePanel.treeCopyParameter=Language.tr("Statistic.Tree.Parameter");
		StatisticsBasePanel.treeCopyParameterHint=Language.tr("Statistic.Tree.Parameter.Hint");
		StatisticsBasePanel.treeBookmarkSetOn=Language.tr("Statistic.Tree.Parameter.BookmarkOn");
		StatisticsBasePanel.treeBookmarkSetOnHint=Language.tr("Statistic.Tree.Parameter.BookmarkOn.Hint");
		StatisticsBasePanel.treeBookmarkSetOff=Language.tr("Statistic.Tree.Parameter.BookmarkOff");
		StatisticsBasePanel.treeBookmarkSetOffHint=Language.tr("Statistic.Tree.Parameter.BookmarkOff.Hint");
		StatisticsBasePanel.treeBookmarkJump=Language.tr("Statistic.Tree.Parameter.BookmarkJump");
		StatisticsBasePanel.treeBookmarkJumpHint=Language.tr("Statistic.Tree.Parameter.BookmarkJump.Hint");
		StatisticsBasePanel.viewersInformation=Language.tr("Statistic.Viewer.Information");
		StatisticsBasePanel.viewersNoHTMLApplicationInfo=Language.tr("Statistic.Viewer.NoHTMLApplication.Info");
		StatisticsBasePanel.viewersNoHTMLApplicationTitle=Language.tr("Statistic.Viewer.NoHTMLApplication.Title");
		StatisticsBasePanel.viewersSaveText=Language.tr("Statistic.Viewer.SaveText");
		StatisticsBasePanel.viewersSaveTable=Language.tr("Statistic.Viewer.SaveTable");
		StatisticsBasePanel.viewersSaveImage=Language.tr("Statistic.Viewer.SaveImage");
		StatisticsBasePanel.viewersSaveImageSizeTitle=Language.tr("Statistic.Viewer.SaveImage.Size.Title");
		StatisticsBasePanel.viewersSaveImageSizePrompt=Language.tr("Statistic.Viewer.SaveImage.Size.Prompt");
		StatisticsBasePanel.viewersSaveImageSizeErrorTitle=Language.tr("Statistic.Viewer.SaveImage.Size.Error.Title");
		StatisticsBasePanel.viewersSaveImageSizeErrorInfo=Language.tr("Statistic.Viewer.SaveImage.Size.Error.Info");
		StatisticsBasePanel.viewersSaveImageErrorTitle=Language.tr("Statistic.Viewer.SaveImage.Error.Title");
		StatisticsBasePanel.viewersSaveImageErrorInfo=Language.tr("Statistic.Viewer.SaveImage.Error.Info");
		StatisticsBasePanel.viewersLoadImage=Language.tr("Statistic.Viewer.LoadImage");
		StatisticsBasePanel.viewersSaveTableErrorTitle=Language.tr("Statistic.Viewer.SaveTable.Error.Title");
		StatisticsBasePanel.viewersSaveTableErrorInfo=Language.tr("Statistic.Viewer.SaveTable.Error.Info");
		StatisticsBasePanel.viewersChartSetupTitle=Language.tr("Statistic.Viewer.DiagramSettings.Title");
		StatisticsBasePanel.viewersChartSetupDefaults=Language.tr("Statistic.Viewer.DiagramSettings.Defaults");
		StatisticsBasePanel.viewersChartSetupDefaultsHint=Language.tr("Statistic.Viewer.DiagramSettings.Defaults.Hint");
		StatisticsBasePanel.viewersChartSetupDefaultsThis=Language.tr("Statistic.Viewer.DiagramSettings.Defaults.ThisPage");
		StatisticsBasePanel.viewersChartSetupDefaultsAll=Language.tr("Statistic.Viewer.DiagramSettings.Defaults.AllPages");
		StatisticsBasePanel.viewersChartSetupFontSize=Language.tr("Statistic.Viewer.DiagramSettings.Font.Size");
		StatisticsBasePanel.viewersChartSetupFontBold=Language.tr("Statistic.Viewer.DiagramSettings.Font.Bold");
		StatisticsBasePanel.viewersChartSetupFontItalic=Language.tr("Statistic.Viewer.DiagramSettings.Font.Italic");
		StatisticsBasePanel.viewersChartSetupTitleFont=Language.tr("Statistic.Viewer.DiagramSettings.DiagramTitle");
		StatisticsBasePanel.viewersChartSetupAxisFont=Language.tr("Statistic.Viewer.DiagramSettings.Axis");
		StatisticsBasePanel.viewersChartSetupAxisLabelsFont=Language.tr("Statistic.Viewer.DiagramSettings.Axis.LabelsFont");
		StatisticsBasePanel.viewersChartSetupAxisValuesFont=Language.tr("Statistic.Viewer.DiagramSettings.Axis.ValuesFont");
		StatisticsBasePanel.viewersChartSetupLegendFont=Language.tr("Statistic.Viewer.DiagramSettings.LegendFont");
		StatisticsBasePanel.viewersChartSetupSurface=Language.tr("Statistic.Viewer.DiagramSettings.Surface");
		StatisticsBasePanel.viewersChartSetupSurfaceBackgroundColor=Language.tr("Statistic.Viewer.DiagramSettings.Background.Color");
		StatisticsBasePanel.viewersChartSetupSurfaceBackgroundGradient=Language.tr("Statistic.Viewer.DiagramSettings.Background.Gradient");
		StatisticsBasePanel.viewersChartSetupSurfaceBackgroundGradientActive=Language.tr("Statistic.Viewer.DiagramSettings.Background.Gradient.Active");
		StatisticsBasePanel.viewersChartSetupSurfaceOutlineColor=Language.tr("Statistic.Viewer.DiagramSettings.Outline.Color");
		StatisticsBasePanel.viewersChartSetupSurfaceOutlineWidth=Language.tr("Statistic.Viewer.DiagramSettings.Outline.Width");
		StatisticsBasePanel.viewersReport=Language.tr("Statistic.Viewer.Report");
		StatisticsBasePanel.viewersReportHint=Language.tr("Statistic.Viewer.Report.Hint");
		StatisticsBasePanel.viewersToolsHint=Language.tr("Statistic.Viewer.Tools.Hint");
		StatisticsBasePanel.viewersToolsShowAll=Language.tr("Statistic.Viewer.Tools.ShowAll");
		StatisticsBasePanel.viewersToolsHideAll=Language.tr("Statistic.Viewer.Tools.HideAll");
		StatisticsBasePanel.viewersReportNoTablesSelectedTitle=Language.tr("Statistic.Viewer.Report.NoTablesSelected.Title");
		StatisticsBasePanel.viewersReportNoTablesSelectedInfo=Language.tr("Statistic.Viewer.Report.NoTablesSelected.Info");
		StatisticsBasePanel.viewersReportSaveWorkbook=Language.tr("Statistic.Viewer.Report.Workbook");
		StatisticsBasePanel.viewersReportSaveWorkbookErrorTitle=Language.tr("Statistic.Viewer.Report.Workbook.Error.Title");
		StatisticsBasePanel.viewersReportSaveWorkbookErrorInfo=Language.tr("Statistic.Viewer.Report.Workbook.Error.Info");
		StatisticsBasePanel.viewersReportSaveHTMLImages=Language.tr("Statistic.Viewer.Report.SaveHTMLImages");
		StatisticsBasePanel.viewersReportSaveHTMLImagesInline=Language.tr("Statistic.Viewer.Report.SaveHTMLImages.Inline");
		StatisticsBasePanel.viewersReportSaveHTMLImagesFile=Language.tr("Statistic.Viewer.Report.SaveHTMLImages.Files");
		StatisticsBasePanel.viewersReportSaveHTMLAppTitle=Language.tr("Statistic.Viewer.Report.HTMLAppTitle");
		StatisticsBasePanel.viewersReportSaveHTMLAppInfo=Language.tr("Statistic.Viewer.Report.HTMLApp.Info");
		StatisticsBasePanel.viewersReportSaveHTMLAppJSError=Language.tr("Statistic.Viewer.Report.HTMLApp.JSError");
		StatisticsBasePanel.viewersToolbarZoom=Language.tr("Statistic.Viewer.Toolbar.Zoom");
		StatisticsBasePanel.viewersToolbarZoomHint=Language.tr("Statistic.Viewer.Toolbar.Zoom.Hint");
		StatisticsBasePanel.viewersToolbarZoomHintPanel=Language.tr("Statistic.Viewer.Toolbar.Zoom.HintPanel");
		StatisticsBasePanel.viewersToolbarCopy=Language.tr("Statistic.Viewer.Toolbar.Copy");
		StatisticsBasePanel.viewersToolbarCopyHint=Language.tr("Statistic.Viewer.Toolbar.Copy.Hint");
		StatisticsBasePanel.viewersToolbarCopyHintPlain=Language.tr("Statistic.Viewer.Toolbar.Copy.HintPlain");
		StatisticsBasePanel.viewersToolbarCopyDefaultSize=Language.tr("Statistic.Viewer.Toolbar.Copy.DefaultSize");
		StatisticsBasePanel.viewersToolbarCopyWindowSize=Language.tr("Statistic.Viewer.Toolbar.Copy.WindowSize");
		StatisticsBasePanel.viewersToolbarPrint=Language.tr("Statistic.Viewer.Toolbar.Print");
		StatisticsBasePanel.viewersToolbarPrintHint=Language.tr("Statistic.Viewer.Toolbar.Print.Hint");
		StatisticsBasePanel.viewersToolbarSave=Language.tr("Statistic.Viewer.Toolbar.Save");
		StatisticsBasePanel.viewersToolbarSaveHint=Language.tr("Statistic.Viewer.Toolbar.Save.Hint");
		StatisticsBasePanel.viewersToolbarSaveDefaultSize=Language.tr("Statistic.Viewer.Toolbar.Save.DefaultSize");
		StatisticsBasePanel.viewersToolbarSaveWindowSize=Language.tr("Statistic.Viewer.Toolbar.Save.WindowSize");
		StatisticsBasePanel.viewersToolbarSearch=Language.tr("Statistic.Viewer.Toolbar.Search");
		StatisticsBasePanel.viewersToolbarSearchHint=Language.tr("Statistic.Viewer.Toolbar.Search.Hint");
		StatisticsBasePanel.viewersToolbarSearchTitle=Language.tr("Statistic.Viewer.Toolbar.Search.DialogTitle");
		StatisticsBasePanel.viewersToolbarSearchString=Language.tr("Statistic.Viewer.Toolbar.Search.DialogSearchString");
		StatisticsBasePanel.viewersToolbarSearchCaseSensitive=Language.tr("Statistic.Viewer.Toolbar.Search.DialogCaseSensitive");
		StatisticsBasePanel.viewersToolbarSearchRegEx=Language.tr("Statistic.Viewer.Toolbar.Search.DialogRegEx");
		StatisticsBasePanel.viewersToolbarSearchNotFound=Language.tr("Statistic.Viewer.Toolbar.Search.NotFound");
		StatisticsBasePanel.viewersToolbarSettings=Language.tr("Statistic.Viewer.Toolbar.Settings");
		StatisticsBasePanel.viewersToolbarSettingsHint=Language.tr("Statistic.Viewer.Toolbar.Settings.Hint");
		StatisticsBasePanel.viewersToolbarOpenText=Language.tr("Statistic.Viewer.Toolbar.OpenText");
		StatisticsBasePanel.viewersToolbarOpenTextHint=Language.tr("Statistic.Viewer.Toolbar.OpenText.Hint");
		StatisticsBasePanel.viewersToolbarOpenTable=Language.tr("Statistic.Viewer.Toolbar.OpenTable");
		StatisticsBasePanel.viewersToolbarOpenTableHint=Language.tr("Statistic.Viewer.Toolbar.OpenTable.Hint");
		StatisticsBasePanel.viewersToolbarWord=Language.tr("Statistic.Viewer.Toolbar.OpenWord");
		StatisticsBasePanel.viewersToolbarWordHint=Language.tr("Statistic.Viewer.Toolbar.OpenWordHint");
		StatisticsBasePanel.viewersToolbarODT=Language.tr("Statistic.Viewer.Toolbar.OpenODT");
		StatisticsBasePanel.viewersToolbarODTHint=Language.tr("Statistic.Viewer.Toolbar.OpenODT.Hint");
		StatisticsBasePanel.viewersToolbarExcel=Language.tr("Statistic.Viewer.Toolbar.Excel");
		StatisticsBasePanel.viewersToolbarExcelHint=Language.tr("Statistic.Viewer.Toolbar.Excel.Hint");
		StatisticsBasePanel.viewersToolbarExcelPrefix=Language.tr("Statistic.Viewer.Toolbar.Excel.Prefix");
		StatisticsBasePanel.viewersToolbarExcelSaveErrorTitle=Language.tr("Statistic.Viewer.Toolbar.Excel.Error.Title");
		StatisticsBasePanel.viewersToolbarExcelSaveErrorInfo=Language.tr("Statistic.Viewer.Toolbar.Excel.Error.Info");
		StatisticsBasePanel.viewersToolbarODS=Language.tr("Statistic.Viewer.Toolbar.OpenODS");
		StatisticsBasePanel.viewersToolbarODSHint=Language.tr("Statistic.Viewer.Toolbar.OpenODS.Hint");
		StatisticsBasePanel.viewersToolbarPDF=Language.tr("Statistic.Viewer.Toolbar.OpenPDF");
		StatisticsBasePanel.viewersToolbarPDFHint=Language.tr("Statistic.Viewer.Toolbar.OpenPDF.Hint");
		StatisticsBasePanel.viewersToolbarNewWindow=Language.tr("Statistic.Viewer.Toolbar.NewWindow");
		StatisticsBasePanel.viewersToolbarNewWindowHint=Language.tr("Statistic.Viewer.Toolbar.NewWindow.Hint");
		StatisticsBasePanel.viewersToolbarNewWindowTitle=Language.tr("Statistic.Viewer.Toolbar.NewWindow.Title");
		StatisticsBasePanel.viewersToolbarWindowSize=Language.tr("Statistic.Viewer.Toolbar.NewWindow.Size");
		StatisticsBasePanel.viewersToolbarWindowSizeHint=Language.tr("Statistic.Viewer.Toolbar.NewWindow.Size.Hint");
		StatisticsBasePanel.viewersToolbarFullscreen=Language.tr("Statistic.Viewer.Toolbar.NewWindow.Fullscreen");
		StatisticsBasePanel.viewersToolbarFullscreenHint=Language.tr("Statistic.Viewer.Toolbar.NewWindow.Fullscreen.Hint");
		StatisticsBasePanel.viewersToolbarSelectAll=Language.tr("Statistic.Viewer.Toolbar.SelectAll");
		StatisticsBasePanel.viewersToolbarSelectAllHint=Language.tr("Statistic.Viewer.Toolbar.SelectAll.Hint");
		StatisticsBasePanel.viewersToolbarSelectNone=Language.tr("Statistic.Viewer.Toolbar.SelectNone");
		StatisticsBasePanel.viewersToolbarSelectNoneHint=Language.tr("Statistic.Viewer.Toolbar.SelectNone.Hint");
		StatisticsBasePanel.viewersToolbarSaveTables=Language.tr("Statistic.Viewer.Toolbar.SaveTables");
		StatisticsBasePanel.viewersToolbarSaveTablesHint=Language.tr("Statistic.Viewer.Toolbar.SaveTables.Hint");
		StatisticsBasePanel.contextCopy=Language.tr("Statistic.Viewer.Context.Copy");
		StatisticsBasePanel.contextCopyTable=Language.tr("Statistic.Viewer.Context.Copy.Table");
		StatisticsBasePanel.contextCopyColumn=Language.tr("Statistic.Viewer.Context.Copy.Column");
		StatisticsBasePanel.contextSort=Language.tr("Statistic.Viewer.Context.Sort");
		StatisticsBasePanel.contextSortAscending=Language.tr("Statistic.Viewer.Context.Sort.Ascending");
		StatisticsBasePanel.contextSortDescending=Language.tr("Statistic.Viewer.Context.Sort.Descending");
		StatisticsBasePanel.contextSortOriginal=Language.tr("Statistic.Viewer.Context.Sort.Reset");
		StatisticsBasePanel.contextSelectColumn=Language.tr("Statistic.Viewer.Context.SelectColumn");
		StatisticsBasePanel.contextFilter=Language.tr("Statistic.Viewer.Context.Filter");
		StatisticsBasePanel.contextFilterReset=Language.tr("Statistic.Viewer.Context.Filter.All");
		StatisticsBasePanel.contextFilterSelect=Language.tr("Statistic.Viewer.Context.Filter.Select");
		StatisticsBasePanel.contextFilterSelectTitle=Language.tr("Statistic.Viewer.Context.Filter.SelectTitle");
		StatisticsBasePanel.contextFilterSelectAll=Language.tr("Statistic.Viewer.Context.Filter.SelectAll");
		StatisticsBasePanel.contextFilterSelectNone=Language.tr("Statistic.Viewer.Context.Filter.SelectNone");
		StatisticsBasePanel.contextColWidthThis=Language.tr("Statistic.Viewer.Context.Width.This");
		StatisticsBasePanel.contextColWidthAll=Language.tr("Statistic.Viewer.Context.Width.All");
		StatisticsBasePanel.contextColWidthDefault=Language.tr("Statistic.Viewer.Context.Width.Default");
		StatisticsBasePanel.contextColWidthByContent=Language.tr("Statistic.Viewer.Context.Width.ByContent");
		StatisticsBasePanel.contextColWidthByContentAndHeader=Language.tr("Statistic.Viewer.Context.Width.ByContentAndHeader");
		StatisticsBasePanel.contextColWidthByWindowWidth=Language.tr("Statistic.Viewer.Context.Width.ByWindowWidth");
		StatisticsBasePanel.viewersSpecialTextCategory=Language.tr("Statistic.Viewer.SpecialText.Category");
		StatisticsBasePanel.viewersSpecialTextSubCategory=Language.tr("Statistic.Viewer.SpecialText.SubCategory");
		StatisticsBasePanel.viewersSpecialTextNoData=Language.tr("Statistic.Viewer.SpecialText.NoData");
		StatisticsBasePanel.viewersSpecialTextStartSimulation=Language.tr("Statistic.Viewer.SpecialText.StartSimulation");
		StatisticsBasePanel.viewersSpecialTextLoadData=Language.tr("Statistic.Viewer.SpecialText.LoadData");
		StatisticsBasePanel.viewersChartNumber=Language.tr("Statistic.Viewer.Chart.Number");
		StatisticsBasePanel.viewersChartPart=Language.tr("Statistic.Viewer.Chart.Part");
		StatisticsBasePanel.viewersChartTime=Language.tr("Statistic.Viewer.Chart.Time");
		StatisticsBasePanel.viewersChartInSeconds=Language.tr("Statistic.Viewer.Chart.InSeconds");
		StatisticsBasePanel.viewersChartInMinutes=Language.tr("Statistic.Viewer.Chart.InMinutes");
		StatisticsBasePanel.viewersChartInHours=Language.tr("Statistic.Viewer.Chart.InHours");
		StatisticsBasePanel.viewersTextSeconds=Language.tr("Statistic.Seconds");
		StatisticsBasePanel.descriptionShow=Language.tr("Statistic.Description.Show");
		StatisticsBasePanel.descriptionShowHint=Language.tr("Statistic.Description.Show.Hint");
		StatisticsBasePanel.descriptionHide=Language.tr("Statistic.Description.Hide");
		StatisticsBasePanel.descriptionHideHint=Language.tr("Statistic.Description.Hide.Hint");
		StatisticsBasePanel.previousAdd=Language.tr("Statistic.Previous");
		StatisticsBasePanel.previousAddHint=Language.tr("Statistic.Previous.Hint");
		StatisticsBasePanel.previousRemove=Language.tr("Statistic.PreviousRemove");
		StatisticsBasePanel.previousRemoveHint=Language.tr("Statistic.PreviousRemove.Hint");
		StatisticsBasePanel.internetErrorTitle=Language.tr("Statistic.Viewer.NoInternet.Title");
		StatisticsBasePanel.internetErrorInfo=Language.tr("Statistic.Viewer.NoInternet.Info");
		StatisticsBasePanel.mailErrorTitle=Language.tr("Statistic.Viewer.MailError.Title");
		StatisticsBasePanel.mailErrorInfo=Language.tr("Statistic.Viewer.MailError.Info");
		StatisticsBasePanel.fileTypeTXT=Language.tr("FileType.Text");
		StatisticsBasePanel.fileTypeRTF=Language.tr("FileType.RTF");
		StatisticsBasePanel.fileTypeHTML=Language.tr("FileType.HTML");
		StatisticsBasePanel.fileTypeHTMLJS=Language.tr("FileType.HTMLApp");
		StatisticsBasePanel.fileTypeDOCX=Language.tr("FileType.Word");
		StatisticsBasePanel.fileTypeODT=Language.tr("FileType.FileTypeODT");
		StatisticsBasePanel.fileTypePDF=Language.tr("FileType.PDF");
		StatisticsBasePanel.fileTypeMD=Language.tr("FileType.md");
		StatisticsBasePanel.fileTypeJPG=Language.tr("FileType.jpeg");
		StatisticsBasePanel.fileTypeGIF=Language.tr("FileType.gif");
		StatisticsBasePanel.fileTypePNG=Language.tr("FileType.png");
		StatisticsBasePanel.fileTypeBMP=Language.tr("FileType.bmp");
		StatisticsBasePanel.fileTypeWordWithImage=Language.tr("FileType.WordImage");
		StatisticsBasePanel.fileTypeSCE=Language.tr("FileType.SciLabScript");
		StatisticsBasePanel.fileTypeTEX=Language.tr("FileType.LaTeX");
		StatisticsBasePanel.fileTypeTYP=Language.tr("FileType.Typst");

		/* Kommandozeilen-System */
		BaseCommandLineSystem.errorBig=Language.tr("Dialog.Title.Error").toUpperCase();
		BaseCommandLineSystem.unknownCommand=Language.tr("CommandLine.UnknownCommand");
		BaseCommandLineSystem.commandCountIf=Language.tr("CommandLine.Count.If");
		BaseCommandLineSystem.commandCountThen0=Language.tr("CommandLine.Count.Then0");
		BaseCommandLineSystem.commandCountThen1=Language.tr("CommandLine.Count.Then1");
		BaseCommandLineSystem.commandCountThenN=Language.tr("CommandLine.Count.ThenN");
		BaseCommandLineSystem.commandCountThenAtLeast1=Language.tr("CommandLine.Count.ThenAtLeast1");
		BaseCommandLineSystem.commandCountThenAtLeastN=Language.tr("CommandLine.Count.ThenAtLeastN");
		BaseCommandLineSystem.commandCountThenMaximum1=Language.tr("CommandLine.Count.ThenMaximum1");
		BaseCommandLineSystem.commandCountThenMaximumN=Language.tr("CommandLine.Count.ThenMaximumN");
		BaseCommandLineSystem.commandCountThenBut0=Language.tr("CommandLine.Count.But0");
		BaseCommandLineSystem.commandCountThenBut1=Language.tr("CommandLine.Count.But1");
		BaseCommandLineSystem.commandCountThenButN=Language.tr("CommandLine.Count.ButN");
		BaseCommandLineSystem.commandReportHelp=Language.tr("CommandLine.ReportBase.Help");
		BaseCommandLineSystem.commandReportError=Language.tr("CommandLine.ReportBase.Error");
		BaseCommandLineSystem.commandReportErrorInputDoesNotExists=Language.tr("CommandLine.ReportBase.Error.Input");
		BaseCommandLineSystem.commandReportErrorOutputExists=Language.tr("CommandLine.ReportBase.Error.Output");
		BaseCommandLineSystem.commandReportDone=Language.tr("CommandLine.ReportBase.Done");
		BaseCommandLineSystem.commandHelpName=Language.tr("CommandLine.Help.Name");
		BaseCommandLineSystem.commandHelpNamesOtherLanguages=Language.trOther("CommandLine.Help.Name").toArray(String[]::new);
		BaseCommandLineSystem.commandHelpHelpShort=Language.tr("CommandLine.Help.Help.Short");
		BaseCommandLineSystem.commandHelpHelpLong=Language.tr("CommandLine.Help.Help.Long");
		BaseCommandLineSystem.commandHelpInfo1=Language.tr("CommandLine.Help.Info1");
		BaseCommandLineSystem.commandHelpInfo2=Language.tr("CommandLine.Help.Info2");
		BaseCommandLineSystem.commandHelpError=Language.tr("CommandLine.Help.Error");
		BaseCommandLineSystem.commandInteractiveName=Language.trAll("CommandLine.Interactive.Name");
		BaseCommandLineSystem.commandInteractiveNamesOtherLanguages=Language.trOther("CommandLine.Interactive.Name").toArray(String[]::new);
		BaseCommandLineSystem.commandHelpInteractiveShort=Language.tr("CommandLine.Interactive.Description.Short");
		BaseCommandLineSystem.commandHelpInteractiveLong=Language.tr("CommandLine.Interactive.Description.Long");
		BaseCommandLineSystem.commandHelpInteractiveStart=Language.tr("CommandLine.Interactive.Start");
		BaseCommandLineSystem.commandHelpInteractiveStop=Language.tr("CommandLine.Interactive.Stop");
		BaseCommandLineSystem.commandHelpInteractiveReady=Language.tr("CommandLine.Interactive.Ready");
		CommandLineDialog.title=Language.tr("CommandLine.Dialog.Title");
		CommandLineDialog.stop=Language.tr("CommandLine.Dialog.StopCommand");
		CommandLineDialog.stopHint=Language.tr("CommandLine.Dialog.StopCommand.Hint");
		CommandLineDialog.labelCommand=Language.tr("CommandLine.Dialog.Command");
		CommandLineDialog.tabDescription=Language.tr("CommandLine.Dialog.Tab.Description");
		CommandLineDialog.tabParametersAndResults=Language.tr("CommandLine.Dialog.Tab.ParametersAndResults");
		CommandLineDialog.labelParameters=Language.tr("CommandLine.Dialog.ParametersForThisCommand");
		CommandLineDialog.labelResults=Language.tr("CommandLine.Dialog.Results");

		/* Statistik */
		StatisticsSimulationBaseData.xmlNameRunDate=Language.trAll("Statistics.XML.RunDate");
		StatisticsSimulationBaseData.xmlNameRunTime=Language.trAll("Statistics.XML.RunTime");
		StatisticsSimulationBaseData.xmlNameRunTimeError=Language.tr("Statistics.XML.RunTime.Error");
		StatisticsSimulationBaseData.xmlNameRunOS=Language.trAll("Statistics.XML.RunOS");
		StatisticsSimulationBaseData.xmlNameRunUser=Language.trAll("Statistics.XML.RunUser");
		StatisticsSimulationBaseData.xmlNameRunThreads=Language.trAll("Statistics.XML.RunThreads");
		StatisticsSimulationBaseData.xmlNameNUMA=Language.trAll("Statistics.XML.RunThreads.NUMA");
		StatisticsSimulationBaseData.xmlNameDynamicBalance=Language.trAll("Statistics.XML.RunThreads.DynamicBalance");
		StatisticsSimulationBaseData.xmlNameDynamicBalanceData=Language.trAll("Statistics.XML.RunThreads.DynamicBalanceData");
		StatisticsSimulationBaseData.xmlNameRunThreadTimes=Language.trAll("Statistics.XML.ThreadRunTimes");
		StatisticsSimulationBaseData.xmlNameRunThreadsError=Language.tr("Statistics.XML.RunThreads.Error");
		StatisticsSimulationBaseData.xmlNameRunEvents=Language.trAll("Statistics.XML.RunEvents");
		StatisticsSimulationBaseData.xmlNameRunEventsError=Language.tr("Statistics.XML.RunEvents.Error");
		StatisticsSimulationBaseData.xmlNameRunRepeatCount=Language.trAll("Statistics.XML.RunRepeatCount");
		StatisticsSimulationBaseData.xmlNameRunRepeatCountError=Language.tr("Statistics.XML.RunRepeatCount.Error");
		StatisticsSimulationBaseData.xmlNameEmergencyShutDown=Language.trAll("Statistics.XML.EmergencyShutDown");
		StatisticsSimulationBaseData.xmlNameWarning=Language.trAll("Statistics.XML.Warning");
	}
}
