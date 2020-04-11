var languageGlobal, viewerGlobal;

function initWebApp() {  
  languageGlobal=initLanguageDirect({de: rawLanguageDataDe, en: rawLanguageDataEn},viewerLanguage);
  
  $('body').css('background-color','white');
   
  $('#status').html(languageGlobal.tr("HTMLViewer.Main.Loading"));
  var statistik=new CallcenterSimulatorLoader(languageGlobal).loadStatistikFromRawJSON(jsonData);
  if (typeof statistik==='string') {$('#status').html(languageGlobal.tr("HTMLViewer.Main.Loading.Error")+"<br><pre>"+statistik+"</pre>"); return;}
  
  $('#status').html(languageGlobal.tr("HTMLViewer.Main.InitTree"));
  var viewer=new CallcenterSimulatorViewer(languageGlobal,null,statistik,$('#title'),$('#content'),"viewerGlobal",printCSS,printJS,"https://a-herzog.github.io/Callcenter-Simulator/viewer/images/");
  viewer.tree=setupViewers(languageGlobal,statistik);
  viewer.init();
  
  viewerGlobal=viewer;

  setTimeout(function() {var api=viewer.treeElement.aciTree('api');  var item=$($('li.aciTreeLi')[0]); api.setVisible(item); api.select(item);},500);
  
  setTimeout(function() {$('#status').hide("slow");},100);
}