'use strict';

var languageGlobal, viewerGlobal;

function initSystem() {
  if (typeof initLanguage=='undefined') {
    document.getElementById('status').innerHTML='<span style=\"color: red;\">Die Systembibliotheken konnten nicht geladen werden.</span>';
    return;
  }
  
  languageGlobal=initLanguageDirect({de: rawLanguageDataDe, en: rawLanguageDataEn},viewerLanguage);

  if (!window.File || !window.FileReader || !window.FileList || !window.Blob) {
    $('#fileAPIinfo').html(languageGlobal.tr("HTMLViewer.Main.FileAPI.NotAvailable"));
  }
  
  var dropZone=document.getElementById('drop_zone');
  dropZone.addEventListener('dragover',handleDragOver,false);
  dropZone.addEventListener('drop',handleFileSelect,false);
  
  setTimeout(function() {$('#status').hide("slow");},100);
}

function loadXML(xml) {
  $('body').css('background-color','white');
    
  $('#status').html(languageGlobal.tr("HTMLViewer.Main.Loading"));
  var statistik=new CallcenterSimulatorLoader(languageGlobal).loadStatistikFromXML(xml);
  if (typeof statistik==='string') {$('#status').html(languageGlobal.tr("HTMLViewer.Main.Loading.Error")+"<br><pre>"+statistik+"</pre>"); return;}
  
  $('#status').html(languageGlobal.tr("HTMLViewer.Main.InitTree"));
  var viewer=new CallcenterSimulatorViewer(languageGlobal,xml,statistik,$('#title'),$('#content'),"viewerGlobal",printCSS,printJS,"./images/");
  viewer.tree=setupViewers(languageGlobal,statistik);
  viewer.init();
  
  viewerGlobal=viewer;

  setTimeout(function() {var api=viewer.treeElement.aciTree('api');  var item=$($('li.aciTreeLi')[0]); api.setVisible(item); api.select(item);},500);
  
  setTimeout(function() {$('#status').hide("slow");},100);
}

function initServerFile(file) {
  $('#initinfo').css('display','none');
  $('#status').css('display','block');
  $('#status').html(languageGlobal.tr("HTMLViewer.Main.LoadingFromServer"));
  $.ajax({url: file}).done(loadXML);
}

function handleFileSelect(event) {
  event.stopPropagation();
  event.preventDefault();

  var files=event.dataTransfer.files;
  if (files.length!=1) {alert(languageGlobal.tr("HTMLViewer.Main.FileAPI.ErrorDropFileNumber")); return;}
  
  var reader=new FileReader();
  reader.onload=function(event) {
	$('#initinfo').css('display','none');
    $('#status').css('display','block');
	loadXML(event.currentTarget.result);
  };
  
  reader.readAsText(files[0]);
}

function handleDragOver(event) {
  event.stopPropagation();
  event.preventDefault();
  event.dataTransfer.dropEffect='copy';
}