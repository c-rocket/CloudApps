app.controller('ConfigController', function($scope, $http, $mdSidenav, $mdToast, $mdUtil, $log, SystemConfigService) {
	
	$scope.systemConfig = {};
	$scope.openConfig = toggleConfig('right');
	
	function configHandler(response) {
		$scope.systemConfig = response;
	}

	function toggleConfig(navId) {
		var debounceFn = $mdUtil.debounce(function() {
			$mdSidenav(navId).toggle().then(function() {
				SystemConfigService.getConfig().success(configHandler);
			});
		}, 200);
		return debounceFn;
	}
	function configApplyHandler(response) {
		$mdToast.show($scope.getToast('Configuration Changed').position('top right'));
	}
	$scope.cancel = function() {
		$mdSidenav('right').close();
	};
	$scope.apply = function() {
		$mdSidenav('right').close();
		SystemConfigService.putConfig($scope.systemConfig).success(configApplyHandler);
	};
});