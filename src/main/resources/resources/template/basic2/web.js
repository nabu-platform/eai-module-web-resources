window.addEventListener("load", function () {
	// make sure we can target the browser using css
	document.documentElement.setAttribute("data-useragent", navigator.userAgent);

    // set load icon while the application is starting up
	var span = document.createElement("span");
	span.setAttribute("class", "n-icon n-icon-spinner");
	span.setAttribute("style", "position: static");
	document.body.appendChild(span);

	application.initialize().then(function($services) {
		// route to initial state
		$services.router.routeInitial();
	});
});