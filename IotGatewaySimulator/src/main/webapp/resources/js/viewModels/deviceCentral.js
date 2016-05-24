define([ 'ojs/ojcore', 'knockout', 'jquery', 'ojs/ojknockout', 'ojs/ojinputtext', 'ojs/ojcheckboxset', 'ojs/ojpopup', 'ojs/ojbutton' ],
		function(oj, ko, $) {
			var baseUrl = '';

			function toaster(text) {
				vm.toastText(text)
				$('#toaster').ojPopup('open', '#form-container');
				setTimeout(function() {
					$('#toaster').ojPopup('close');
				}, 1500);
			}

			function applyConfigChanges(data, event) {
				var configData = {};
				$.each(vm.deviceTypes(), function(key, value) {
					configData[value.name] = value.devices;
				});
				
				console.log("Config Data", configData);
				var jsonData = JSON.stringify(configData);
				$.ajax({
					headers : {
						'Accept' : 'application/json',
						'Content-Type' : 'application/json'
					},
					type : 'PUT',
					url : baseUrl + '/device/setup/devicecentral',
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
					url : baseUrl + '/device/setup/devicecentral'
				}).then(function(data) {
					console.log("Data", data)
					$.each(data, function(key, value) {
						console.log("Key", key, "value", value);
						var obj = {};
						obj.name = key;
						obj.devices = value
						vm.deviceTypes.push(obj);
					});
					console.log("Device Types", vm.deviceTypes());
				});
			}

			function mainContentViewModel() {
				var self = this;
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
