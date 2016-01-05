<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<div flex ng-cloak class="device-content" data-ng-controller="DeviceController" data-ng-init="init()">
	<md-grid-list id="deviceContent" md-cols-sm="1" md-cols-md="2" md-cols-lg="3" md-cols-gt-lg="4" md-row-height="100px"
		class="content-grid" md-gutter="30"> <md-grid-tile md-rowspan="1" md-colspan="1"
		class="md-whiteframe-z4 light-green">
	<section layout="column">
		<div class="centered" ng-bind="infoText"></div>
		<div class="centered" ng-show="createText">
			<md-button class="create-button" ng-click="openCreate()" ng-bind="createText"></md-button>
		</div>
	</section>
	</md-grid-tile> <md-grid-tile ng-show="currentId" data-ng-repeat="(key, value) in device.metrics"
		class="md-whiteframe-z4 tile{{$index%7}}">
	<section layout="row">
		<div class="metricLabel">{{key}}</div>
		<div class="metricValue">{{value | metricFilter}}</div>
	</section>
	</md-grid-tile> <md-grid-tile md-rowspan="2" md-colspan="1" class="md-whiteframe-z4 grey" ng-show="currentId"> 
	<img ng-src="data:image/jpeg;base64,{{ device.picture }}" height="75%"
		ng-show="currentId" /> </md-grid-tile> <md-grid-tile md-rowspan="2" md-colspan="1" class="md-whiteframe-z4 purple"
		ng-show="currentId"> <md-grid-tile-header>
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
			ng-click="sendEvent(key,!value.value,value.display)" aria-label="key"> Toggle {{ value.display
		}} </md-switch>
	</section>
	</md-grid-tile> <md-grid-tile md-rowspan="1" md-colspan="1" class="md-whiteframe-z4 turquoise" ng-show="currentId"> <md-grid-tile-header>
	<h2>Device Details ({{ device.id }})</h2>
	</md-grid-tile-header>
	<div class="device-info">
		<ul>
			<li>Type: {{device.resource}}</li>
			<li>Secret: {{device.secret}}</li>
		</ul>
	</div>
	</md-grid-tile> <md-grid-tile md-rowspan="3" md-colspan="2" md-colspan-md="2" md-colspan-sm="1" class="md-whiteframe-z4"
		ng-show="currentId">
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
	<div ng-include="'<c:url value='/resources/js/app/views/device.jsp'/>'"></div>
</div>