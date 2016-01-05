app.service('DeviceListService', function() {
	this.getDevices = function(http) {
		var url = baseUrl + 'device/list';
		return http.get(url);
	};
});
