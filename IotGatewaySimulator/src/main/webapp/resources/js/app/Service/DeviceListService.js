app.factory('DeviceListService', function($http) {
	var deviceListService = {
		getDevices : function() {
			return $http.get(baseUrl + '/device/list');
		}
	}
	return deviceListService;
});
