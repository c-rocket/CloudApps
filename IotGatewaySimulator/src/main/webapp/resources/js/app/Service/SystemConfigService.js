
'use strict';

app.factory('SystemConfigService', function($http) {
	var SystemConfigService = {
		getConfig : function() {
			return $http.get(baseUrl + 'system/config')

		},
		putConfig : function(payload) {
			return $http.put(baseUrl + 'system/config', payload);
		}
	}
	return SystemConfigService;
});