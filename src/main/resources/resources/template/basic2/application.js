if (!application) { var application = {} }

application.configuration = {
	scheme: {
		http: "${when(environment('secure'), 'https', 'http')}",
		ws: "${when(environment('secure'), 'wss', 'ws')}"
	},
	url: "${environment('url', 'http://127.0.0.1')}",
	host: "${environment('host', '127.0.0.1')}"
};

application.views = {};

application.initialize = function() {
	application.services = new nabu.services.ServiceManager({
		mixin: function(services) {
			Vue.mixin({
				// inject some services for use
				computed: {
					$configuration: function() { return application.configuration },
					$services: function() { return services },
					$views: function() { return application.views },
					$application: function() { return application }
				}
			});	
		},
		q: nabu.services.Q,
		swagger: application.definitions.Swagger,
		router: function router($services) {
			this.$initialize = function() {
				return new nabu.services.VueRouter({
					useHash: true,
					unknown: function(alias, parameters, anchor) {
						return application.services.router.register({
							alias: alias,
							enter: function() {
								return alias;
							}
						});
					},
					enter: function(anchor, newRoute, newParameters, oldRoute, oldParameters, newRouteReturn) {
						$services.vue.route = newRoute.alias;
						// reset scroll
						document.body.scrollTop = 0;
					}
				});
			}
		},
		vue: function vue() {
			this.$initialize = function() {
				return new Vue({
					el: "body",
					data: {
						route: null
					}
				});
			}
		},
		routes: application.routes});
	return application.services.$initialize();
};
