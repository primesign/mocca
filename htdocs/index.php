<?php

$domain=ereg_replace('[^\.]*\.(.*)$','\1',$_SERVER['HTTP_HOST']);
$group_name=ereg_replace('([^\.]*)\..*$','\1',$_SERVER['HTTP_HOST']);

echo '<?xml version="1.0" encoding="UTF-8"?>';
?>
<!DOCTYPE html
	PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="de">

  <head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title><?php echo $project_name; ?></title>
	<script language="JavaScript" type="text/javascript">
	<!--
	function help_window(helpurl) {
		HelpWin = window.open( helpurl,'HelpWindow','scrollbars=yes,resizable=yes,toolbar=no,height=400,width=400');
	}
	// -->
		</script>

<style type="text/css">
	<!--
	BODY {
	margin-top: 3;
	margin-left: 3;
	margin-right: 3;
	margin-bottom: 3;
	background-color: #FFFFFF;
	}
	ol,ul,p,body,td,tr,th,form { font-family: verdana,arial,helvetica,sans-serif; font-size:small;
		color: #333333; }

	h1 {
	font-size: x-large;
	font-family: verdana,arial,helvetica,sans-serif;
	background-color: #CCCCCC;
}
	h2 {
	font-size: large;
	font-family: verdana,arial,helvetica,sans-serif;
	background-color: #CCCCCC;
}
	h3 { font-size: medium; font-family: verdana,arial,helvetica,sans-serif; }
	h4 { font-size: small; font-family: verdana,arial,helvetica,sans-serif; }
	h5 { font-size: x-small; font-family: verdana,arial,helvetica,sans-serif; }
	h6 { font-size: xx-small; font-family: verdana,arial,helvetica,sans-serif; }

	pre,tt { font-family: courier,sans-serif }

	a:link { text-decoration:none }
	a:visited { text-decoration:none }
	a:active { text-decoration:none }
	a:hover { text-decoration:none; color:red }

	.titlebar { color: black; text-decoration: none; font-weight: bold; }
	a.tablink { color: black; text-decoration: none; font-weight: bold; font-size: x-small; }
	a.tablink:visited { color: black; text-decoration: none; font-weight: bold; font-size: x-small; }
	a.tablink:hover { text-decoration: none; color: black; font-weight: bold; font-size: x-small; }
	a.tabsellink { color: black; text-decoration: none; font-weight: bold; font-size: x-small; }
	a.tabsellink:visited { color: black; text-decoration: none; font-weight: bold; font-size: x-small; }
	a.tabsellink:hover { text-decoration: none; color: black; font-weight: bold; font-size: x-small; }
	a.logo { color: black; text-decoration: none; font-weight: bold; font-size: x-small; }
	a.logo:visited { color: black; text-decoration: none; font-weight: bold; font-size: x-small; }
	a.logo:hover {
	text-decoration: none;
	color: red;
	font-weight: bold;
	font-size: x-small;
}
	-->
</style>

</head>

<body>

<a href="/" class="logo"><img src="mocca-t_s.png" border="0" alt="Logo" width="281" height="100" /></a>

<h2>Welcome to the MOCCA project!</h2>
<div style="float:right;background-color:#d5d5d7;width:300px">
<div class="titlebar" style="text-align:center;margin:2px">Project summary</div>
<span>
<?php if($handle=fopen('http://'.$domain.'/export/projhtml.php?group_name='.$group_name,'r')){
$contents = '';
while (!feof($handle)) {
	$contents .= fread($handle, 8192);
}
fclose($handle);
$contents=str_replace('href="/','href="http://'.$domain.'/',$contents);
$contents=str_replace('src="/','src="http://'.$domain.'/',$contents);
echo $contents; } ?>
</span>
</div>
<p><a href="#news">News</a><br/>
  <a href="http://egovlabs.gv.at/docman/?group_id=13&amp;language_id=*">Documentation</a><br />
  <a href="http://egovlabs.gv.at/frs/?group_id=13">Downloads</a><br />
  <a href="#licenses">Licence conditions</a>
<p><strong>MOCCA - Modular Open Citizen Card Architecture</strong>
<p>MOCCA is a project initiated by the <a href="http://www.egiz.gv.at">E-Government Innovation Center (EGIZ)</a> to implement a modular, open source <a href="http://www.buergerkarte.at">citizen card environment</a>.</p>
<a name="news" id="news"/>
    <?php if ($handle=fopen('http://'.$domain.'/export/projnews.php?group_name='.$group_name,'r')){
$contents = '';
while (!feof($handle)) {
	$contents .= fread($handle, 8192);
}
fclose($handle);
$contents=str_replace('href="/','href="http://'.$domain.'/',$contents);
echo $contents; } ?>
    <h3><a name="licenses" id="licenses"></a>License conditions</h3>
    <p>The MOCCA  project as provided by the <a href="http://www.egiz.gv.at">E-Government Innovation Center (EGIZ)</a> is licensed according to the terms of the <a href="http://www.apache.org/licenses/LICENSE-2.0.html">Apache License 2.0</a>.</p>
    <p>The MOCCA project includes software provided by third parties. This software is either <a href="http://www.opensource.org">open source software</a> or commercial software. The licensee is responsible for optaining appropriate licenses.</p>
<p>The MOCCA project uses software provided by <a href="http://www.sic.st">Stiftung Secure Information and Communication Technologies (SIC)</a>. The uses of the software provided by Stiftung Secure Information and Communication Technologies (SIC) together with the unchanged software provided by the MOCCA project is free of charge. For details see <a href="http://jce.iaik.tugraz.at/sic/sales/licences/license_agreement_moa">License Agreement IAIK</a>.</p>
</td>

<!-- PLEASE LEAVE "Powered By GForge" on your site -->
<br />
<center>
<a href="http://gforge.org/"><img src="http://gforge.org/images/pow-gforge.png" alt="Powered By GForge Collaborative Development Environment" border="0" /></a>
</center>


</body>
</html>
