window.addEventListener("load", function () {
	// make sure we can target the browser using css
	document.documentElement.setAttribute("data-useragent", navigator.userAgent);
	application.initialize().then(function() {
		// route to initial state
		var handler = function() {
			application.services.router.routeInitial();
		}
		// whether successful or not, route anyway
		application.services.swagger.remember().then(handler, handler);
	});
});