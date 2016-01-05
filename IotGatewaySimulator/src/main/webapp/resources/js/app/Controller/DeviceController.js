//data-ng-controller
app.controller('DeviceController', function($scope, $http, $mdSidenav, $mdToast, $interval, $mdUtil, $window,
		DeviceService) {
	var last = {
		bottom : false,
		top : true,
		left : false,
		right : true
	};
	var swapTitle = "Swap Device";
	var swapText = "Want to change devices? Swap for a new one!";
	var newTitle = "Create Device";
	var newText = "Start by creating a device here!";

	$scope.currentId = null;

	Chart.defaults.global.colours = [ "#0090B5", "#CC5100", "#007A6E", "#FFD800", "#3BC600", "#57007F", "#63C66F" ]

	$scope.device = {};
	$scope.createDevice = {};
	$scope.deviceTypes = [];

	$scope.labels = [];
	$scope.series = [];
	$scope.data = [];

	$scope.createText;
	$scope.infoText = "Loading Page...";

	$scope.deviceType = null;

	$scope.getToast = function(content) {
		var element = angular.element('#deviceContent');
		return $mdToast.simple().content(content).parent(element).position('top left');
	}

	function initDeviceHandler(device) {
		if (device.length != 0) {
			$scope.device = device;
			$scope.currentId = $scope.device.id;
			$scope.deviceType = $scope.device.resource;
			$scope.createText = swapTitle;
			$scope.infoText = swapText;
			extractChartData($scope.device.chartSeries, $scope.device.chartValues, $scope.device.chartLabels);
			startInterval();
		}else{
			$scope.createText = newTitle;
			$scope.infoText = newText;
		}
	}

	$scope.init = function() {
		$scope.labels = [];
		$scope.series = [];
		$scope.data = [];
		$scope.device = {};
		DeviceService.getCurrent().success(initDeviceHandler);
	}

	function deleteDeviceHandler() {
		$scope.device = {};
		$scope.createText = newTitle;
		$scope.infoText = newText;
	}

	$scope.deleteDevice = function(id) {
		$scope.currentId = null;
		$scope.deviceType = null;

		DeviceService.deleteCurrent().success(deleteDeviceHandler);
	}

	// ____________NEW DEVICE
	$scope.newDevice = {};
	function deviceTypeHandler(deviceTypeResponse) {
		$scope.deviceTypes = deviceTypeResponse;
	}

	$scope.openCreate = toggleCreate('left');
	function toggleCreate(navId) {
		var debounceFn = $mdUtil.debounce(function() {
			$mdSidenav(navId).toggle().then(function() {
				$scope.newDevice = {};
				DeviceService.getDeviceTypes().success(deviceTypeHandler);
			});
		}, 200);
		return debounceFn;
	}

	function createDeviceHandler(response) {
		if (response == true) {
			$mdToast.show($scope.getToast('Device Created'));
			if ($scope.deviceType == null || $scope.newDevice.type == $scope.deviceType) {
				$scope.createText = swapTitle;
				$scope.infoText = swapText;
				startInterval();
			} else {
				$window.location.reload();
			}
		} else {
			$mdToast.show($scope.getToast('Ohhh no, your device could not be added. Guess I\'m broken'));
		}
	}

	$scope.createDevice = function() {
		$mdSidenav('left').close();
		$scope.currentId = $scope.newDevice.id;
		DeviceService.createDevice($scope.newDevice).success(createDeviceHandler);
	}

	$scope.cancelDevice = function() {
		$mdSidenav('left').close();
	}

	// ______________ALERTS
	$scope.sendAlert = function(id, alertName, alertDisplay) {
		function alertHandler() {
			$mdToast.show($scope.getToast(alertDisplay + ' Alert Sent'));
		}
		DeviceService.sendAlert(alertName).success(alertHandler);
	}

	// ______________EVENTS
	$scope.sendEvent = function(eventName, value, displayName) {
		function eventHandler() {
			var startEnd = (value) ? 'Started' : 'Ended';
			$mdToast.show($scope.getToast(displayName + ' ' + startEnd));
		}
		DeviceService.toggleEvent(eventName).success(eventHandler);
	}

	// ______________CHART
	function extractChartData(chartSeries, chartValues, chartLabels) {
		if (typeof chartLabels != 'undefined') {
			if ($scope.labels.length < chartLabels.length) {
				$scope.labels = chartLabels
			}
			// add the incremental chart data labels
			var keys = Object.keys(chartValues);
			// if the series have not yet been labeled then add them
			if ($scope.series.length == 0) {
				$scope.series = chartSeries;
			}
			// add the incremental chart values for each series
			$scope.data = chartValues;
		}
	}

	function intervalHandler(device) {
		if($scope.currentId != $scope.device.id){
			$scope.device = device;
			$scope.currentId = $scope.device.id;
			$scope.deviceType = $scope.device.resource;
		}else{
			$scope.device.metrics = device.metrics;
		}
		if ($scope.device.metrics == null) {
			$scope.init();
		}
		extractChartData(device.chartSeries, device.chartValues, device.chartLabels);
		intervalRunning = false;
		$interval(function() {
			startInterval();
		}, 2000, 1);
		$scope.$on('$destroy', function() {
			$interval.cancel(true);
			intervalRunning = false;
		});
	}

	var intervalRunning = false;
	function startInterval() {
		if (intervalRunning) {
			return;
		}
		intervalRunning = true;
		DeviceService.getCurrent().success(intervalHandler);
	}

	$scope.modalShown = false;
	$scope.toggleModal = function() {
		$scope.modalShown = !$scope.modalShown;
	};
});
