<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta content='True' name='HandheldFriendly'>
  <meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0'>
  <meta name="apple-mobile-web-app-capable" content="yes">
  <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent">
  <title>Callcenter Simulator Statistik-Anzeige</title>
  <link href="viewer-css.php" rel="stylesheet" type="text/css">
</head>
<body>

<div class="title noprint" id="title">Callcenter Simulator Viewer</div>
<div id="status">Lade System</div>

<div id="content"></div>
<div id="initinfo" class="textcontent">
  
  <p>
  Über dieses Webinterface können Sie Statistik-XML-Dateien, die mit dem Callcenter Simulator erzeugt wurden,
  ohne lokale Installation des Simulators direkt im Browser anzeigen.
  </p>

  <h2>Lokale Datei in den Statistik-Viewer laden</h2>
  <p id="fileAPIinfo"></p>
  <p id="drop_zone">Dateien hier hin ziehen</p>
  <p>Die Verarbeitung Statistik-XML-Datei wird dabei vollständig innerhalb des Browsers auf Ihrem Rechner verarbeitet. Es erfolgt keine Übertragung der Daten zum Server.</p>

  <h2>Beispieldatei laden</h2>
  <p>
  Auf dem Server sind bereits zwei Beispiel-Statistikdateien hinterlegt, die zu
  Demonstrationszwecken in den Statistik-Viewer geladen werden können:
  </p>
  <ul>
    <li><a href="javascript:initServerFile('./examples/BeispielStatistik1.xml')">Statistik eines kleinen Modells laden</a></li>
    <li style="margin-top: 10px;"><a href="javascript:initServerFile('./examples/BeispielStatistik2.xml')">Statistik eines großen Modells laden</a></li>
  </ul>
    
</div>

<script src="viewer-lang.php" type="application/javascript" charset="UTF-8"></script>
<script src="viewer-js.php" type="application/javascript" charset="UTF-8"></script>
<script src="viewer.js" type="application/javascript" charset="UTF-8"></script>
<script type="text/javascript">var viewerLanguage="de"; var printCSS="viewer-css.php"; var printJS="./viewer-js.php"; initSystem();</script>

</body>
</html>