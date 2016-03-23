app.factory('DeviceListService', function($http) {
	var deviceListService = {
		getDevices : function() {
			return $http.get(baseUrl + '/device/types');
		}
	}
	return deviceListService;
});
