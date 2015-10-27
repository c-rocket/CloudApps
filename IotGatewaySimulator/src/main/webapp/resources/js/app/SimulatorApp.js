angular.module('simulatorFilters', []).filter('metricFilter', [function () {
	return function (metric) {
		if (angular.isNumber(metric)) {
			return parseFloat(Math.round(metric * 100) / 100).toFixed(2);;
		}else{
			return metric;
		}
	};
}]);
var app = angular.module('simulatorApp', [ 'ngMaterial', 'chart.js', 'simulatorFilters' ]);

app.config(function($logProvider) {
	$logProvider.debugEnabled(true);
});
