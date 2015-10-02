//data-ng-controller
app.controller('DeviceController', function($scope, $window, $http, $mdSidenav, $mdToast, $interval, $mdUtil, $log,
		DeviceService, SystemConfigService) {
	var last = {
		bottom : false,
		top : true,
		left : false,
		right : true
	};
	var currentId = null;

	Chart.defaults.global.colours = [ "#0090B5", "#CC5100", "#007A6E", "#FFD800", "#3BC600", "#57007F", "#63C66F" ]

	var color = [ 'blue', 'green', 'yellow', 'purple', 'navy', 'orange' ]

	$scope.device = {};
	$scope.systemConfig = {};
	$scope.createDevice = {};
	$scope.deviceTypes = [];
	$scope.empty = true;

	$scope.labels = [];
	$scope.series = [];
	$scope.data = [];

	$scope.systemConfig = {};
	$scope.createText = "Create Device";
	$scope.infoText = "Start by creating a device here!";

	$scope.baseUrl = angular.element($('#baseUrl')).val();

	$scope.toggleSidenav = function(menuId) {
		$mdSidenav(menuId).toggle();
	};
	$scope.toastPosition = angular.extend({}, last);
	$scope.getToastPosition = function() {
		sanitizePosition();
		return Object.keys($scope.toastPosition).filter(function(pos) {
			return $scope.toastPosition[pos];
		}).join(' ');
	};
	function sanitizePosition() {
		var current = $scope.toastPosition;
		if (current.bottom && last.top)
			current.top = false;
		if (current.top && last.bottom)
			current.bottom = false;
		if (current.right && last.left)
			current.left = false;
		if (current.left && last.right)
			current.right = false;
		last = angular.extend({}, current);
	}

	function deviceHandler(deviceResponse) {
		$scope.device = deviceResponse;
		if ($scope.device) {
			currentId = $scope.device.id;
			extractChartData($scope.device.chartSeries, $scope.device.chartValues, $scope.device.chartLabels);
			$scope.runCounter = true;
			$scope.createText = 'Swap Device';
			$scope.infoText = "Want to change devices? Swap for a new one!";
		}
	}

	function deviceTypeHandler(deviceTypeResponse) {
		$scope.deviceTypes = deviceTypeResponse;
	}

	$scope.init = function() {
		$scope.labels = [];
		$scope.series = [];
		$scope.data = [];
		DeviceService.getDeviceTypes($http, $scope.baseUrl, deviceTypeHandler);
		DeviceService.getCurrentDevice($http, $scope.baseUrl, deviceHandler);
	}

	$scope.loadDevice = function(id) {
		$scope.labels = [];
		$scope.series = [];
		$scope.data = [];
		DeviceService.getDevice($http, $scope.baseUrl, id, deviceHandler);
	}

	function deleteDeviceHandler() {
		$scope.device = {};
		$scope.createText = "Create Device";
		$scope.infoText = "Start by creating a device here!";
	}
	$scope.deleteDevice = function(id) {
		currentId = null;
		DeviceService.deleteDevice($http, $scope.baseUrl, id, deleteDeviceHandler);
	}

	$scope.sendAlert = function(id, alertName, alertDisplay) {
		function alertHandler() {
			var toast = $mdToast.simple().content(alertDisplay + ' Alert Sent').position($scope.getToastPosition());
			$mdToast.show(toast);
		}
		DeviceService.sendAlert($http, $scope.baseUrl, id, alertName, alertHandler);
	}
	$scope.sendEvent = function(id, eventName, value, displayName) {
		function eventHandler() {
			var startEnd = (value) ? 'Started' : 'Ended';
			var toast = $mdToast.simple().content(displayName + ' ' + startEnd).position($scope.getToastPosition());
			$mdToast.show(toast);
		}
		DeviceService.toggleEvent($http, $scope.baseUrl, id, eventName, eventHandler);
	}

	// ____________NEW DEVICE
	$scope.newDevice = {};
	$scope.openCreate = toggleCreate('left');
	function toggleCreate(navId) {
		var debounceFn = $mdUtil.debounce(function() {
			$mdSidenav(navId).toggle().then(function() {
				$scope.newDevice = {};
			});
		}, 200);
		return debounceFn;
	}

	function createDeviceHandler(response) {
		if (response == true) {
			var toast = $mdToast.simple().content('Device Created').position($scope.getToastPosition());
			$mdToast.show(toast);
			$scope.loadDevice(currentId);
		} else {
			var toast = $mdToast.simple().content('Ohhh no, your device could not be added. Guess I\'m broken')
					.position($scope.getToastPosition());
			$mdToast.show(toast);
		}
		$mdSidenav('left').close().then(function() {
			$log.debug("Device Creation Attempted");
		});
	}
	$scope.createDevice = function() {
		$mdSidenav('right').close().then(function() {
			$log.debug("applied");
		});
		currentId = $scope.newDevice.id;
		DeviceService.createDevice($http, $scope.baseUrl, $scope.newDevice, createDeviceHandler);
	}

	$scope.cancelDevice = function() {
		$mdSidenav('left').close().then(function() {
			$log.debug("cancelled");
		});
	}

	// ____________CONFIG SIDE NAV
	function configHandler(response) {
		$scope.systemConfig = response;
	}

	$scope.openConfig = toggleConfig('right');
	function toggleConfig(navId) {
		var debounceFn = $mdUtil.debounce(function() {
			$mdSidenav(navId).toggle().then(function() {
				SystemConfigService.getCurrentConfig($http, $scope.baseUrl, configHandler);
			});
		}, 200);
		return debounceFn;
	}
	function configApplyHandler(response) {
		var toast = $mdToast.simple().content('Configuration Changed').position($scope.getToastPosition());
		$mdToast.show(toast);
	}
	$scope.cancel = function() {
		$mdSidenav('right').close().then(function() {
			$log.debug("cancelled");
		});
	};
	$scope.apply = function() {
		$mdSidenav('right').close().then(function() {
			$log.debug("applied");
		});
		SystemConfigService.setConfig($http, $scope.baseUrl, $scope.systemConfig, configApplyHandler);
	};

	// ______________CHART
	function extractChartData(chartSeries, chartValues, chartLabels) {

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

		// Start Poll for updating chart
		function metricsAndChartHandler(deviceResponse) {
			$scope.device.metrics = deviceResponse.metrics;
			extractChartData(deviceResponse.chartSeries, deviceResponse.chartValues, deviceResponse.chartLabels);
		}

		$interval(function() {
			DeviceService.getDevice($http, $scope.baseUrl, currentId, metricsAndChartHandler);
		}, 500, 1);

		$scope.$on('$destroy', function() {
			$interval.cancel(true);
		});
	}

	$scope.modalShown = false;
	$scope.toggleModal = function() {
		$scope.modalShown = !$scope.modalShown;
	};
});
