<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
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

fieldset legend {
	font-variant: small-caps;
}

table td {
	padding-right: 20px;
}

table {
	font-size: 10pt;
	font-variant: small-caps;
}

.message {
	color: blue;
	font-size: 11pt;
	font-variant: small-caps;
}

.message legend {
	font-size: 9pt;
}

.message fieldset {
	border: 1px solid #999;
	border-radius: 8px;
	box-shadow: 0 0 10px #999;
}
</style>
</head>
<body>
	<c:if test="${not empty message}">
		<div class="message">
			<fieldset>
				<legend>Update</legend>
				<label>${ message }</label>
			</fieldset>
		</div>
	</c:if>
	<h2>Device Configuration</h2>
	<form:form method="post" action="setup/upload" modelAttribute="uploadForm" enctype="multipart/form-data">
		<fieldset style="width: 500px;">
			<legend>Upload New Device</legend>
			<label>Please select a .properties file to upload:</label>
			<input type="file" name="files[0]" />
			<br />
			<label>Please select an image file to upload (optional):</label>
			<input type="file" name="files[1]" />
			<br />
			<input type="submit" value="upload" />
		</fieldset>
	</form:form>
	<br />
	<br />
	<hr />
	<h2>Local Device Selection</h2>
	<table>
		<th>
			<label for="devices.all">Select All</label>
		</th>
		<td>
			<input type="checkbox" name="devices.all" id="devices.all" onClick="toggle(this)" />
		</td>
	</table>
	<form method="POST" action="<c:url value='/device/setup/select'/>">
		<fieldset style="width: 800px;">
			<legend>Enable/Disable Devices</legend>
			<table>
				<tr>
					<c:forEach items="${devices}" var="device" varStatus="deviceIndex">
						<c:if test="${deviceIndex.index%6 == 5}">
				</tr>
				<tr>
					</c:if>
					<th style="text-align: left;">
						<label for="devices.${device.name}">${device.display}:</label>
					</th>
					<td>
						<input type="checkbox" <c:if test="${device.enabled}">checked="checked"</c:if> id="devices.${device.name}"
							name="devices.${device.name}" />
						<br />
					</td>
					</c:forEach>
				</tr>
			</table>
			<br />
			<input type="submit" value="Submit" />
		</fieldset>
	</form>
	<br />
	<br />
	<hr />
	<h2>Device Central Selection</h2>
	<form method="POST" action="<c:url value='/device/setup/centralselect'/>">
		<fieldset style="width: 800px;">
			<legend>Enable/Disable Devices</legend>
			<table>
				<tr>
					<c:forEach items="${centralDevices}" var="centralDevice" varStatus="centralDeviceIndex">
						<c:if test="${centralDeviceIndex.index%6 == 5}">
				</tr>
				<tr>
					</c:if>
					<th style="text-align: left;">
						<label for="devices.${centralDevice.name}">${centralDevice.name}:</label>
					</th>
					<td>
						<input type="checkbox" <c:if test="${centralDevice.enabled}">checked="checked"</c:if> 
						<c:if test="${centralDevice.disabled}">disabled readonly</c:if> 
							id="devices.${centralDevice.name}" name="devices.${centralDevice.name}" />
						<br />
					</td>
					</c:forEach>
				</tr>
			</table>
			<br />
			<input type="submit" value="Submit" />
		</fieldset>
	</form>
	<br />
	<br />
	<hr />
	<a href="<c:url value='/'/>">Return to Main</a>
	<script type="text/javascript">
		function getCheckboxes() {
			var inputs = document.forms[1].elements;
			var checkboxes = [];
			for (var i = 0; i < inputs.length; i++) {
				if (inputs[i].type == "checkbox") {
					checkboxes.push(inputs[i]);
				}
			}
			return checkboxes;
		}

		function toggle(value) {
			var checkboxes = getCheckboxes();
			for (var i = 0, n = checkboxes.length; i < n; i++) {
				checkboxes[i].checked = value.checked;
			}
		}
	</script>
</body>
</html>