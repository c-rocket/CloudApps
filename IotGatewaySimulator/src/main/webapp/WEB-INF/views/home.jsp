<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE HTML>
<html data-ng-app="simulatorApp">
<head>
<link rel="shortcut icon" href="<c:url value='/resources/pictures/favicon.ico'/>" type="image/x-icon" />
<link rel="stylesheet" href="https://ajax.googleapis.com/ajax/libs/angular_material/0.9.4/angular-material.min.css">
<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=RobotoDraft:300,400,500,700,400italic">
<link rel="stylesheet" href="<c:url value='/resources/charts/angular-chart.min.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/simulator.css'/>">

<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js"></script>
<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.6/angular.min.js"></script>
<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.6/angular-animate.min.js"></script>
<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.6/angular-aria.min.js"></script>
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
<body data-ng-controller="DeviceController" data-ng-init="init()" class="ui-widget-content">
	<md-toolbar layout="row" class="md-whiteframe-z2 top-bar">
	<div class="md-toolbar-tools md-whiteframe-1dp">
		<a class="brand " href="#">
			<img src="<c:url value='/resources/pictures/logo.png'/>" height="35px" alt="logo">
		</a>
		<div class="title">IoT CS Device Simulator</div>
		<md-button class="md-icon-button config-button" aria-label="Settings" ng-click="openConfig();"> <md-icon
			md-svg-icon="resources/icons/menu.svg"></md-icon> </md-button>
	</div>
	</md-toolbar>
	<section layout="row" flex>
		<md-sidenav class="md-sidenav-left md-whiteframe-z1 side-nav" md-component-id="left" md-is-locked-open="true">
		<section layout="column" layout-sm="column" class="buttons-column">
			<md-button class="create-button" ng-click="toggleModal()">New Device</md-button>
			<md-button data-ng-repeat="listDevice in devices" class="fat-list" ng-click="loadDevice(listDevice.id);">
			<img src="<c:url value='/resources/pictures/'/>{{ listDevice.thumbnail }}" height="32px" /> <span>{{
				listDevice.id }} </span> </a> </md-button>
		</section>
		</md-sidenav>
		<md-content flex layout-padding class="content"> <md-grid-list ng-show="empty" md-cols-sm="1"
			md-cols-md="2" md-cols-gt-md="4" md-row-height-gt-md="100px" md-row-height="100px" class="content-grid"
			md-gutter="30"> <md-grid-tile md-rowspan="1" md-colspan="1" md-colspan-sm="1"
			class="md-whiteframe-z4 light-green"> <img src="<c:url value='/resources/pictures/'/>arrow.png"
			width="40px" />
		<br />
		Start building your device here!</md-grid-tile> </md-grid-list> <md-grid-list ng-show="device.id" md-cols-sm="1" md-cols-md="2" md-cols-gt-md="4"
			md-row-height-gt-md="100px" md-row-height="100px" class="content-grid" md-gutter="30"> <md-grid-tile
			data-ng-repeat="(key, value) in device.metrics" class="md-whiteframe-z4 tile{{$index%5}}">
		<section layout="row">
			<div class="metricLabel">{{key}}</div>
			<div class="metricValue">{{value | number : 2}}</div>
		</section>
		</md-grid-tile> <md-grid-tile md-rowspan="2" md-colspan="1" md-colspan-sm="1" class="md-whiteframe-z4 grey"> <img
			src="<c:url value='/resources/pictures/'/>{{ device.picture }}" height="75%" /> </md-grid-tile> <md-grid-tile md-rowspan="2"
			md-colspan="1" md-colspan-sm="1" class="md-whiteframe-z4 light-green"> <md-grid-tile-header>
		<h2>Device Details</h2>
		</md-grid-tile-header>
		<div class="tile-content">
			<ul>
				<li>Type: {{device.resource}}</li>
				<li>Secret: {{device.secret}}</li>
			</ul>
		</div>
		</md-grid-tile> <md-grid-tile md-rowspan="2" md-colspan="1" md-colspan-sm="1" class="md-whiteframe-z4 blue"> <md-grid-tile-header>
		<h2>Signal Alerts</h2>
		</md-grid-tile-header>
		<section layout="column" layout-sm="column" layout-align="center center" layout-wrap>
			<md-button class="md-raised md-warn alert-button" data-ng-repeat="(key, value) in device.alerts"
				ng-click="sendAlert(device.id,key,value)">{{value}}</md-button>
		</section>
		</md-grid-tile> <md-grid-tile md-rowspan="2" md-colspan="1" md-colspan-sm="1" class="md-whiteframe-z4 purple"> <md-grid-tile-header>
		<h2>Control Events</h2>
		</md-grid-tile-header>
		<section layout="column" layout-sm="column" layout-align="center center" layout-wrap>
			<md-switch class="event-toggle" ng-model="value.value" data-ng-repeat="(key, value) in device.events"
				ng-click="sendEvent(device.id,key,!value.value,value.display)" aria-label="key"> Toggle {{ value.display
			}} </md-switch>
		</section>
		</md-grid-tile> <md-grid-tile md-rowspan="3" md-colspan="3" class="md-whiteframe-z4">
		<div>
			<canvas id="line" class="chart chart-line" chart-data="data" chart-labels="labels" chart-legend="true"
				chart-series="series" width="700" height="250" chart-getColour="getColour()"> <!-- chart-colours="" -->
		</canvas>
		</div>
		</md-grid-tile> </md-grid-tile> <md-grid-tile md-rowspan="1" md-colspan="1" md-colspan-sm="1" class="md-whiteframe-z4 pink">
		<section layout="column" layout-sm="column" layout-align="center center" layout-wrap>
			<md-button class="md-raised md-warn alert-button" ng-click="deleteDevice(device.id)">Delete Device</md-button>
		</section>
		</md-grid-tile> </md-grid-list> </md-content>
	</section>

	<section layout="column" flex>
		<md-sidenav class="md-sidenav-right md-whiteframe-z2" md-component-id="right"> <md-toolbar
			class="md-theme-light">
		<h1 class="md-toolbar-tools">System Config</h1>
		</md-toolbar> <md-content layout-padding>
		<form>
			<md-input-container> <label for="server">IoT CS Server</label> <input type="text" id="server"
				required ng-model="systemConfig.server" md-autofocus> </md-input-container>
			<md-input-container> <label for="port">IoT CS Port</label> <input type="text" id="port" required
				ng-model="systemConfig.port" md-autofocus> </md-input-container>

			<md-switch class="event-toggle" ng-model="systemConfig.sendingMessages" aria-label="key"> Sending
			Messages </md-switch>
		</form>
		<md-button ng-click="apply()" class="md-primary">Apply</md-button> <md-button ng-click="cancel()" class="md-primary">Cancel</md-button>
		</md-content> </md-sidenav>
	</section>

	<modal-dialog show='modalShown' width='250px' height='400px'> <ng-modal-dialog-content>
	<section layout="column">
		<md-toolbar class="md-theme-light">
		<h1 class="md-toolbar-tools">Add New Device</h1>
		</md-toolbar>
		<md-content layout-padding>
		<form>
			<md-input-container> Device Type<md-select ng-model="newDevice.type" required
				aria-label="deviceType"> <md-option ng-repeat="deviceType in deviceTypes" value="{{deviceType.name}}">{{deviceType.display}}</md-option>
			</md-select> </md-input-container>
			<md-input-container> <label for="id">Device ID</label> <input type="text" id="id" required
				ng-model="newDevice.id" md-autofocus> </md-input-container>
			<md-input-container> <label for="secret">Shared Secret</label> <input type="text" id="secret"
				required ng-model="newDevice.secret" md-autofocus> </md-input-container>
		</form>
		<md-button ng-click="createDevice()" class="md-primary">Create</md-button> <md-button ng-click="cancelDevice()"
			class="md-primary">Cancel</md-button> </md-content>
	</section>
	</ng-modal-dialog-content> </modal-dialog>
	<input type="hidden" value="<c:url value='/'/>" id="baseUrl" />
</body>
</html>