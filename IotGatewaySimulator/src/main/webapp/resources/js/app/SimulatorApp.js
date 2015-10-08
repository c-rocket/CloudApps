var app = angular.module('simulatorApp', [ 'ngMaterial', 'chart.js' ]);

app.config(function($logProvider) {
	$logProvider.debugEnabled(true);
});