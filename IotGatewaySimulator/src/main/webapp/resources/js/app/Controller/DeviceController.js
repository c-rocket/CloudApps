//data-ng-controller
app.controller('DeviceController', function($scope, $window, $http, $mdSidenav, $mdToast, $interval, $mdUtil, $log,
		DeviceService, SystemConfigService) {
	var last = {
		bottom : false,
		top : true,
		left : false,
		right : true
	};
	$scope.currentId = null;

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

	function getToast(content) {
		var element = angular.element('#deviceContent');
		return $mdToast.simple().content(content).parent(element).position('top left');
	}

	function deviceHandler(deviceResponse) {
		if (deviceResponse.length != 0) {
			$scope.device = deviceResponse;
			$scope.currentId = $scope.device.id;
			extractChartData($scope.device.chartSeries, $scope.device.chartValues, $scope.device.chartLabels);
			$scope.runCounter = true;
			$scope.createText = 'Swap Device';
			$scope.infoText = "Want to change devices? Swap for a new one!";
		}
	}

	$scope.init = function() {
		$scope.labels = [];
		$scope.series = [];
		$scope.data = [];
		$scope.device = {};
		DeviceService.getCurrentDevice($http, $scope.baseUrl, deviceHandler);
	}

	function deleteDeviceHandler() {
		$scope.device = {};
		$scope.createText = "Create Device";
		$scope.infoText = "Start by creating a device here!";
	}

	$scope.deleteDevice = function(id) {
		$scope.currentId = null;
		DeviceService.deleteDevice($http, $scope.baseUrl, id, deleteDeviceHandler);
	}

	$scope.sendAlert = function(id, alertName, alertDisplay) {
		function alertHandler() {
			$mdToast.show(getToast(alertDisplay + ' Alert Sent'));
		}
		console.log('sending alert');
		DeviceService.sendAlert($http, $scope.baseUrl, id, alertName, alertHandler);
	}

	$scope.sendEvent = function(id, eventName, value, displayName) {
		function eventHandler() {
			var startEnd = (value) ? 'Started' : 'Ended';
			$mdToast.show(getToast(displayName + ' ' + startEnd));
		}
		DeviceService.toggleEvent($http, $scope.baseUrl, id, eventName, eventHandler);
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
				DeviceService.getDeviceTypes($http, $scope.baseUrl, deviceTypeHandler);
			});
		}, 200);
		return debounceFn;
	}

	function createDeviceHandler(response) {
		if (response == true) {
			$mdToast.show(getToast('Device Created'));
			DeviceService.getCurrentDevice($http, $scope.baseUrl, deviceHandler);
		} else {
			$mdToast.show(getToast('Ohhh no, your device could not be added. Guess I\'m broken'));
		}
	}
	$scope.createDevice = function() {
		$mdSidenav('left').close().then(function() {
			$log.debug("applied");
		});
		$scope.currentId = $scope.newDevice.id;
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
		$mdToast.show(getToast('Configuration Changed').position('top right'));
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

			// Start Poll for updating chart
			function metricsAndChartHandler(deviceResponse) {
				$scope.device.metrics = deviceResponse.metrics;
				if ($scope.device.metrics == null) {
					$scope.init();
				}
				extractChartData(deviceResponse.chartSeries, deviceResponse.chartValues, deviceResponse.chartLabels);
			}

			$interval(function() {
				DeviceService.getDevice($http, $scope.baseUrl, $scope.currentId, metricsAndChartHandler);
			}, 500, 1);

			$scope.$on('$destroy', function() {
				$interval.cancel(true);
			});
		}
	}

	$scope.modalShown = false;
	$scope.toggleModal = function() {
		$scope.modalShown = !$scope.modalShown;
	};
});
