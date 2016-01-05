<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE HTML>
<html data-ng-app="simulatorApp">
<head>
<meta name="viewport" content="width=device-width, minimum-scale=1.0, maximum-scale=1.0" />
<meta name="description" content="Create Devices for Oracle IoT Cloud Service">
<meta name="keywords" content="Oracle IoT Cloud Service">
<meta name="author" content="Oracle">
<link rel="shortcut icon" href="<c:url value='/resources/pictures/favicon.ico'/>" type="image/x-icon" />
<link rel="stylesheet" href="https://ajax.googleapis.com/ajax/libs/angular_material/0.9.4/angular-material.min.css" />
<link rel="stylesheet" href="<c:url value='/resources/charts/angular-chart.min.css'/>" />
<link rel="stylesheet" href="<c:url value='/resources/css/simulator.css'/>" />

<!-- Angular Dependencies -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js"></script>

<!-- Angular Material Dependencies -->
<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.6/angular.min.js"></script>
<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.6/angular-animate.min.js"></script>
<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.6/angular-aria.min.js"></script>

<!-- Angular Material Javascript using GitCDN to load directly from `bower-material/master` -->
<script src="https://ajax.googleapis.com/ajax/libs/angular_material/0.10.0/angular-material.min.js"></script>

<script src="<c:url value='/resources/charts/Chart.min.js'/>"></script>
<script src="<c:url value='/resources/charts/angular-chart.min.js'/>"></script>
<script src="<c:url value='/resources/js/app/SimulatorApp.js'/>"></script>

<script src="<c:url value='/resources/js/app/Service/DeviceListService.js'/>"></script>
<script src="<c:url value='/resources/js/app/Service/DeviceService.js'/>"></script>
<script src="<c:url value='/resources/js/app/Service/SystemConfigService.js'/>"></script>
<script src="<c:url value='/resources/js/app/Controller/DeviceController.js'/>"></script>
<script src="<c:url value='/resources/js/app/Controller/ConfigController.js'/>"></script>
<title>IoT Device Simulator</title>
<base href="/0.11.2/">
</head>
<body class="ui-widget-content" id="theBody">
	<input type="hidden" value="<c:url value='/'/>" id="baseUrl" />

	<div ng-include="'<c:url value='/resources/js/app/views/banner.jsp'/>'"></div>
	<div ng-include="'<c:url value='/resources/js/app/views/grid.jsp'/>'"></div>
</body>
</html>