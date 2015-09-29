app.service('SystemConfigService', function() {
	this.getCurrentConfig = function(http, baseUrl, handler) {
		var url = baseUrl + 'system/config';
		console.log('Device URL: ' + url);
		http.get(url).success(handler);
	};

	this.setConfig = function(http, baseUrl, data, handler) {
		var url = baseUrl + 'system/config';
		console.log('Device URL: ' + url);
		http.put(url, data).success(handler);
	};
});