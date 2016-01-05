'use strict';

app.factory('DeviceService', function($http) {
	var DeviceService = {
		deleteCurrent : function() {
			return $http({
				method : 'DELETE',
				url : baseUrl +'/device/current'
			});
		},
		getCurrent : function(){
			return $http.get(baseUrl + '/device/current')
		},
		getDeviceTypes : function() {
			var url = baseUrl + 'device/types';
			return $http.get(url)

		},
		createDevice : function(payload) {
			return $http.post(baseUrl + 'device/current/', payload);
		},
		toggleEvent : function(eventName) {
			return $http.put(baseUrl + 'device/current/events/' + eventName);
		},
		sendAlert : function(alertName) {
			return $http.put(baseUrl + 'device/current/alerts/' + alertName);
		}
	}
	return DeviceService;
});

