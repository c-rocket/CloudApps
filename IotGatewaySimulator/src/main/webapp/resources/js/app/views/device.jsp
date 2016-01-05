<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<section layout="column" flex>
	<md-sidenav class="md-sidenav-left md-whiteframe-z2" md-component-id="left"> <md-toolbar
		class="md-theme-light">
	<h1 class="title">Configure Device</h1>
	</md-toolbar> <md-content layout-padding>
	<form class="data-form">
		<md-input-container> <span class="labelForSelect">Device Type*</span> <md-select id="createType"
			ng-model="newDevice.type" aria-label="createType" placeholder="Select a Device Type"> <md-option
			ng-repeat="deviceType in deviceTypes" value="{{deviceType.name}}">{{deviceType.display}}</md-option> </md-select> </md-input-container>
		<md-input-container> <label for="id">Device ID*</label> <input type="text" id="id"
			ng-model="newDevice.id" md-autofocus> </md-input-container>
		<md-input-container> <label for="secret">Shared Secret*</label> <input type="text" id="secret"
			ng-model="newDevice.secret" md-autofocus> </md-input-container>
	</form>
	<md-button ng-click="createDevice()" class="md-primary">Create</md-button> <md-button ng-click="cancelDevice()"
		class="md-primary">Cancel</md-button> </md-content> </md-sidenav>
</section>