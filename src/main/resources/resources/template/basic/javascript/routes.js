application.services.router.register({
	alias: "home",
	enter: function() {
		return new application.views.Home();
	},
	url: "/"
});