define([ 'ojs/ojcore', 'knockout', 'jquery', 'ojs/ojknockout', 'ojs/ojselectcombobox', 'ojs/ojinputtext',
		'ojs/ojcheckboxset','ojs/ojpopup','ojs/ojbutton' ], function(oj, ko, $) {
	var baseUrl = '';
	
	function toaster(text) {
		vm.toastText(text)
		$('#toaster').ojPopup('open', '#form-container');
		setTimeout(function() {
			$('#toaster').ojPopup('close');
		}, 1500);
	}
	
	function applyConfigChanges(data, event) {
		console.log(data);
		var jsonData = JSON.stringify(vm.deviceTypes());
		$.ajax({
			headers : {
				'Accept' : 'application/json',
				'Content-Type' : 'application/json'
			},
			type : 'PUT',
			url : baseUrl + '/device/setup/types',
			dataType : 'json',
			data : jsonData,
			success : function(responseData) {
				console.log(responseData);
				getDeviceTypes();
				toaster('Devices Updated');
			},
			error : function() {
				alert('Error saving Config');
			}
		});
	}

	function getDeviceTypes() {
		vm.deviceTypes.removeAll();
		$.ajax({
			url : baseUrl + '/device/setup/types'
		}).then(function(data) {
			console.log("Data", data)
			$.each(data, function(index, value) {
				vm.deviceTypes.push(value)
			});
			console.log("Device Types", vm.deviceTypes());
		});
	}

	function mainContentViewModel() {
		var self = this;
		self.industrySelect = ko.observable();
		self.deviceTypes = ko.observableArray([]);
		self.applyChanges = applyConfigChanges;
		self.toastText = ko.observable();
	}
	var vm = new mainContentViewModel();

	$(document).ready(function() {
		baseUrl = $('#baseUrl').val();
		getDeviceTypes();
	});
	return vm;
});
