<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE HTML>
<html data-ng-app="simulatorApp">
<head>
<meta name="viewport" content="width=device-width, minimum-scale=1.0, maximum-scale=1.0" />
<meta name="description" content="Create Devices for Oracle IoT Cloud Service">
<meta name="keywords" content="Oracle IoT Cloud Service">
<meta name="author" content="Oracle">
<link rel="shortcut icon" href="<c:url value='/resources/pictures/favicon.ico'/>" type="image/x-icon" />
<title>IoT Device Simulator Setup</title>
<style>
h2 {
	font-variant: small-caps;
}

form label {
	font-size: 10pt;
	font-variant: small-caps;
}

form input {
	font-variant: small-caps;
}
</style>
</head>
<body>
	<h2>Device Configuration</h2>
	<form method="POST" enctype="multipart/form-data" action="<c:url value='/device/setup/upload'/>">
		<fieldset style="width:500px;">
			<legend>Upload New Device</legend>
			<label for="file">Please select a .properties file to upload:</label>
			<input type="file" id="file" name="file" />
			<br />
			<input type="submit" value="upload" />
		</fieldset>
	</form>
	<br />
	<br />
	<a href="<c:url value='/'/>">Return to Main</a>
</body>
</html>