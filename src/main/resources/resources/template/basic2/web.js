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
		var initial = $services.router.routeInitial();
		// for server-side rendering: throw away all the script tags
		if (initial && initial.then && navigator.userAgent.match(/Nabu-Renderer/)) {
			initial.then(function() {
				// break out of the execution
				setTimeout(function() {
					var scripts = document.head.getElementsByTagName("script");
					for (var i = scripts.length - 1; i >= 0; i--) {
						scripts[i].parentNode.removeChild(scripts[i]);
					}
				}, 1);
			});
		}
	});
});