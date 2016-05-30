window.addEventListener("load", function () {
	// initialize vue
	application.initialize.vue();
	// route to initial state
	application.services.router.routeInitial("body");
});