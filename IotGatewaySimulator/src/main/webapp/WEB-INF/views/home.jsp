<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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
<base href="/0.11.2/">
</head>
<body data-ng-controller="DeviceController" data-ng-init="init()" class="ui-widget-content" id="theBody">
	<md-toolbar layout="row" class="md-whiteframe-z2 brand-bar">
	<div class="md-whiteframe-1dp page-padding">
		<a class="brand " href="<c:url value='/'/>">
			<div class="logo">
				<!-- image  alt="Oracle" src="css/images/t.gif"/ -->
			</div>
			<div class="app-name">IoT CS Device Simulator</div>
		</a>
		<div class="config">
			<md-button class="config-button" aria-label="Settings" ng-click="openConfig();"></md-button>
		</div>
	</div>
	</md-toolbar>
	<md-toolbar layout="row" class="md-whiteframe-z2 top-bar"> </md-toolbar>
	<div flex ng-cloak class="device-content">
		<md-grid-list id="deviceContent" md-cols-sm="1" md-cols-md="2" md-cols-lg="3" md-cols-gt-lg="4" md-row-height="100px"
			class="content-grid" md-gutter="30"> <md-grid-tile md-rowspan="1" md-colspan="1"
			class="md-whiteframe-z4 light-green">
		<section layout="column">
			<div class="centered" ng-bind="infoText">Create your device here!</div>
			<div class="centered">
				<md-button class="create-button" ng-click="openCreate()" ng-bind="createText">Create Device</md-button>
			</div>
		</section>
		</md-grid-tile> <md-grid-tile ng-show="currentId" data-ng-repeat="(key, value) in device.metrics"
			class="md-whiteframe-z4 tile{{$index%5}}">
		<section layout="row">
			<div class="metricLabel">{{key}}</div>
			<div class="metricValue">{{value | number : 2}}</div>
		</section>
		</md-grid-tile> <md-grid-tile md-rowspan="2" md-colspan="1" class="md-whiteframe-z4 grey" ng-show="currentId"> <img
			ng-src="<c:url value='/resources/pictures/'/>{{ device.picture }}" height="75%" ng-show="currentId" /> </md-grid-tile> <md-grid-tile
			md-rowspan="2" md-colspan="1" class="md-whiteframe-z4 purple" ng-show="currentId"> <md-grid-tile-header>
		<h2>Signal Alerts</h2>
		</md-grid-tile-header>
		<section layout="column" layout-sm="column" layout-align="center center" layout-wrap>
			<md-button class="md-raised md-warn alert-button" data-ng-repeat="(key, value) in device.alerts"
				ng-click="sendAlert(device.id,key,value)">{{value}}</md-button>
		</section>
		</md-grid-tile> <md-grid-tile md-rowspan="2" md-colspan="1" class="md-whiteframe-z4 blue" ng-show="currentId"> <md-grid-tile-header>
		<h2>Control Events</h2>
		</md-grid-tile-header>
		<section layout="column" layout-align="center" layout-wrap class="events">
			<md-switch class="event-toggle" ng-model="value.value" data-ng-repeat="(key, value) in device.events"
				ng-click="sendEvent(device.id,key,!value.value,value.display)" aria-label="key"> Toggle {{ value.display
			}} </md-switch>
		</section>
		</md-grid-tile> <md-grid-tile md-rowspan="1" md-colspan="1" class="md-whiteframe-z4 turquoise" ng-show="currentId"> <md-grid-tile-header>
		<h2>Device Details</h2>
		</md-grid-tile-header>
		<div class="device-info">
			<ul>
				<li>Type: {{device.resource}}</li>
				<li>Secret: {{device.secret}}</li>
			</ul>
		</div>
		</md-grid-tile> <md-grid-tile md-rowspan="3" md-colspan="2" md-colspan-md="2" md-colspan-sm="1" class="md-whiteframe-z4" ng-show="currentId">
		<div class="scrollable chart-cover">
			<canvas id="lineChart" class="chart chart-line" chart-data="data" chart-labels="labels" chart-legend="true"
				chart-series="series" chart-getColour="getColour()"> <!-- chart-colours="" -->
		</canvas>
		</div>
		</md-grid-tile> </md-grid-tile> <md-grid-tile md-rowspan="1" md-colspan="1" class="md-whiteframe-z4 pink" ng-show="currentId">
		<section layout="column" layout-sm="column" layout-align="center center" layout-wrap>
			<md-button class="md-raised md-warn alert-button" ng-click="deleteDevice(device.id)">Delete Device</md-button>
		</section>
		</md-grid-tile> </md-grid-list>
	</div>

	<section layout="column" flex>
		<md-sidenav class="md-sidenav-right md-whiteframe-z2" md-component-id="right"> <md-toolbar
			class="md-theme-light">
		<h1 class="title">System Config</h1>
		</md-toolbar> <md-content layout-padding>
		<form class="data-form">
			<md-input-container> <label for="server">IoT CS Server</label> <input type="text" id="server"
				required ng-model="systemConfig.server" md-autofocus> </md-input-container>
			<md-input-container> <label for="port">IoT CS Port</label> <input type="text" id="port" required
				ng-model="systemConfig.port" md-autofocus> </md-input-container>
			<md-switch id="configSendMessages" class="event-toggle" ng-model="systemConfig.sendingMessages" aria-label="key">
			Sending Messages: {{systemConfig.sendingMessages}}
			</md-switch>
		</form>
		<md-button ng-click="apply()" class="md-primary">Apply</md-button> <md-button ng-click="cancel()" class="md-primary">Cancel</md-button>
		</md-content> </md-sidenav>
	</section>

	<section layout="column" flex>
		<md-sidenav class="md-sidenav-left md-whiteframe-z2" md-component-id="left"> <md-toolbar
			class="md-theme-light">
		<h1 class="title">Configure Device</h1>
		</md-toolbar> <md-content layout-padding>
		<form class="data-form">
			<md-input-container> 
				<span class="labelForSelect">Device Type*</span>
				<md-select id="createType" ng-model="newDevice.type" aria-label="createType" placeholder="Select a Device Type"> 
					<md-option ng-repeat="deviceType in deviceTypes" value="{{deviceType.name}}">{{deviceType.display}}</md-option>
				</md-select> 
			</md-input-container>
			<md-input-container> <label for="id">Device ID*</label> <input type="text" id="id"
				ng-model="newDevice.id" md-autofocus> </md-input-container>
			<md-input-container> <label for="secret">Shared Secret*</label> <input type="text" id="secret"
				ng-model="newDevice.secret" md-autofocus> </md-input-container>
		</form>
		<md-button ng-click="createDevice()" class="md-primary">Create</md-button> <md-button ng-click="cancelDevice()"
			class="md-primary">Cancel</md-button> </md-content> </md-sidenav>
	</section>
	<input type="hidden" value="<c:url value='/'/>" id="baseUrl" />
</body>
</html>