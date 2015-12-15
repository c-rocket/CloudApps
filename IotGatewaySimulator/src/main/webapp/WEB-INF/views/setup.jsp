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
<link rel="stylesheet" href="https://ajax.googleapis.com/ajax/libs/angular_material/0.9.4/angular-material.min.css" />
<link rel="stylesheet" href="<c:url value='/resources/charts/angular-chart.min.css'/>" />
<link rel="stylesheet" href="<c:url value='/resources/css/simulator.css'/>" />

<!-- Angular Dependencies -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js"></script>

<!-- Angular Material Dependencies -->
<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.6/angular.min.js"></script>
<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.6/angular-animate.min.js"></script>
<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.6/angular-aria.min.js"></script>

<!-- Angular Material Javascript using GitCDN to load directly from `bower-material/master` -->
<script src="https://ajax.googleapis.com/ajax/libs/angular_material/0.10.0/angular-material.min.js"></script>

<script src="<c:url value='/resources/charts/Chart.min.js'/>"></script>
<script src="<c:url value='/resources/charts/angular-chart.min.js'/>"></script>
<script src="<c:url value='/resources/js/app/SimulatorApp.js'/>"></script>

<script src="<c:url value='/resources/js/app/Service/DeviceListService.js'/>"></script>
<script src="<c:url value='/resources/js/app/Service/DeviceService.js'/>"></script>
<script src="<c:url value='/resources/js/app/Service/SystemConfigService.js'/>"></script>
<script src="<c:url value='/resources/js/app/Controller/DeviceController.js'/>"></script>
<title>IoT Device Simulator</title>
</head>
<body class="setup-page" class="ui-widget-content">
	<md-toolbar layout="row" class="md-whiteframe-z2 brand-bar">
	<div class="md-whiteframe-1dp page-padding">
		<a class="brand " href="<c:url value='/'/>">
			<div class="logo">
				<!-- image  alt="Oracle" src="css/images/t.gif"/ -->
			</div>
			<div class="app-name">IoT CS Device Simulator</div>
		</a>
		<div class="home">
			<md-button class="home-button" aria-label="Home" ng-href="<c:url value='/'/>"></md-button>
		</div>
	</div>
	</md-toolbar>
	<md-toolbar layout="row" class="md-whiteframe-z2 top-bar"> </md-toolbar>
	<div flex ng-cloak class="device-content">
		<c:if test="${not empty message}">
			<div style="width:100%; text-align: center;">
					<div class="light-green message-div">${ message }</div>
			</div>
		</c:if>
		<md-grid-list id="deviceContent" md-cols-sm="1" md-cols-md="2" md-cols-lg="3" md-cols-gt-lg="4" md-row-height="100px"
			class="content-grid" md-gutter="30"> 
		 <md-grid-tile md-rowspan="2" md-colspan-sm="1" md-colspan-md="1" md-colspan-lg="1" md-colspan-gt-lg="1"
			class="md-whiteframe-z4 config-tile new-upload"> <md-grid-tile-header class="tile-header">
		<h2>Upload A New Device</h2>
		</md-grid-tile-header> 
		<div class="content-with-header-2">
			<form:form method="post" action="setup/upload" modelAttribute="uploadForm" enctype="multipart/form-data">
				<label>Share with Device Central</label>
				<input type="checkbox" name="share" />
				<br />
				<br />
				<label>Select Device Properties file:</label>
				<input type="file" name="files[0]" class="upload" />
				<br />
				<label>Select an Image(suggested 250x250):</label>
				<input type="file" name="files[1]" />
				<br />
				<label>Industry:</label>
				<select name="industry">
				<option value="Basic Materials" selected="selected">Basic Materials</option>
				<option value="Consumer Goods">Consumer Goods</option>
				<option value="Consumer Services">Consumer Services</option>
				<option value="Financials">Financials</option>
				<option value="Health Care">Health Care</option>
				<option value="Industrials">Industrials</option>
				<option value="Oil and Gas">Oil and Gas</option>
				<option value="Technology">Technology</option>
				<option value="Telecommunications">Telecommunications</option>
				<option value="Utilities">Utilities</option>
				</select>
				<hr />
				<input type="submit" value="upload" />
			</form:form> 
		</div>
		</md-grid-tile> 
		
		<md-grid-tile md-rowspan="2" md-colspan-sm="1" md-colspan-md="2" md-colspan-lg="2" md-colspan-gt-lg="2"
			class="md-whiteframe-z4 config-tile new-upload"> <md-grid-tile-header class="tile-header">
			<h2>Enable/Disable Local Devices</h2>
			</md-grid-tile-header>
			<div class="content-with-header-2">
				<form method="POST" action="<c:url value='/device/setup/select'/>">
					<label for="devices.all"><strong>Select All</strong></label>
					<input type="checkbox" name="devices.all" id="devices.all" onClick="toggle(this)" />
					<div style="height: 98px; overflow:auto; width:100%;">
						<ul>
							<c:forEach items="${devices}" var="device" varStatus="deviceIndex">
							<li style="display: inline-block; padding-right: 10px;">
								<label for="devices.${device.name}">${device.display}:</label>
								<input type="checkbox" <c:if test="${device.enabled}">checked="checked"</c:if> id="devices.${device.name}"
									name="devices.${device.name}" />
							</li>
							</c:forEach>
						</ul>
					</div>
					<br />
					<hr/>
					<input type="submit" value="Save Config" />
				</form>
			</div>
		</md-grid-tile>
		
		<md-grid-tile md-rowspan="3" md-colspan-sm="1" md-colspan-md="2" md-colspan-lg="3" md-colspan-gt-lg="4"
			class="md-whiteframe-z4 config-tile new-upload"> <md-grid-tile-header class="tile-header">
			<h2>Download from Device Central</h2>
			</md-grid-tile-header>
				<div class="content-with-header-3">
					<form method="POST" action="<c:url value='/device/setup/centralselect'/>">
						<div style="height: 275px; overflow:auto; width:100%;">
								<c:forEach items="${centralDevices}" var="industryEntry" varStatus="industryIndex">
									<h4>${ industryEntry.key }</h4>
									<c:forEach items="${industryEntry.value}" var="centralDevice" varStatus="centralDeviceIndex">
									<ul>
										<li style="display: inline-block; padding-right: 10px;">
											<label for="devices.${centralDevice.name}">${centralDevice.display}:</label>
											<input type="checkbox" <c:if test="${centralDevice.enabled}">checked="checked"</c:if>
												<c:if test="${centralDevice.disabled}">disabled readonly</c:if> id="devices.${centralDevice.name}"
												name="devices.${centralDevice.name}" />
										</li>
									</ul>
									</c:forEach>
							<hr/>
								</c:forEach>
						</div>
						<input type="submit" value="Download" style="margin-top:10px;" />
					</form>
				</div>
		</md-grid-tile>
		</md-grid-list>
	</div>

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