window.addEventListener("load", function () {
	// make sure we can target the browser using css
	document.documentElement.setAttribute("data-useragent", navigator.userAgent);
	// initialize vue
	application.initialize.vue();
	// route to initial state
	application.services.router.routeInitial();
});