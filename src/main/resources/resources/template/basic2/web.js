window.addEventListener("load", function () {
    // make sure we can target the browser using css
    document.documentElement.setAttribute("data-useragent", navigator.userAgent);
    application.initialize().then(function($services) {
        // route to initial state
        $services.router.routeInitial();
    });
});