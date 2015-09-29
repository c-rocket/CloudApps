app.service('DeviceListService', function() {
	this.getDevices = function(http, baseUrl, handler) {
		var url = baseUrl + 'device/list';
		http.get(url).success(handler);
	};
});