app.service('DeviceService', function() {
	this.getDevice = function(http, baseUrl, id, handler) {
		var url = baseUrl + 'device/' + id + '/show';
		http.get(url).success(handler);
	}
	this.getDeviceTypes = function(http, baseUrl, handler) {
		var url = baseUrl + 'device/types';
		http.get(url).success(handler);
	}

	this.sendAlert = function(http, baseUrl, id, alertName, alertHandler) {
		var url = baseUrl + 'device/' + id + '/alerts/' + alertName;
		http.put(url).success(alertHandler);
	}
	this.toggleEvent = function(http, baseUrl, id, eventName, eventHandler) {
		var url = baseUrl + 'device/' + id + '/events/' + eventName;
		http.put(url).success(eventHandler);
	}
	this.createDevice = function(http, baseUrl, device, createDeviceHandler) {
		var url = baseUrl + 'device/create/';
		http.post(url, device).success(createDeviceHandler);
	}
	this.deleteDevice = function(http, baseUrl, id, deleteDeviceHandler) {
		var url = baseUrl + 'device/delete/' + id;
		http.delete(url).success(deleteDeviceHandler);
	}
});