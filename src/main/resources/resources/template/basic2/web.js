window.addEventListener("load", function () {
	// make sure we can target the browser using css
	document.documentElement.setAttribute("data-useragent", navigator.userAgent);
	application.initialize().then(function() {
		// route to initial state
		application.services.router.routeInitial();
	});
});