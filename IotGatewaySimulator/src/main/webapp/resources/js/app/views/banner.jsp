<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<div ng-controller="ConfigController">
<md-toolbar layout="row" class="md-whiteframe-z2 brand-bar">
<div class="md-whiteframe-1dp page-padding">
	<a class="brand " href="<c:url value='/'/>">
		<div class="logo">
			<!-- image  alt="Oracle" src="css/images/t.gif"/ -->
		</div>
		<div class="app-name">IoT CS Device Simulator</div>
	</a>
	<div class="config">
		<md-button class="config-button" aria-label="Settings" ng-click="openConfig();"></md-button>
	</div>
</div>
</md-toolbar>
<md-toolbar layout="row" class="md-whiteframe-z2 top-bar"> </md-toolbar>
<div ng-include="'<c:url value='/resources/js/app/views/config.jsp'/>'"></div>
</div>