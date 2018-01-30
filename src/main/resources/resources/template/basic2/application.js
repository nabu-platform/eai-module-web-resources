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
application.components = {};
application.definitions = {};
// a list of loaders that need to be run once the application has been initialized
application.loaders = [];

application.bootstrap = function(handler) {
	// we have already started the services bus, immediately execute the handler
	if (application.services) {
		handler(application.services);
	}
	// add it to the list of other things to be executed
	else {
		application.loaders.push(handler);
	}
};
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
		cookies: nabu.services.Cookies,
		swagger: application.definitions.Swagger,
		loader: function loader($services) {
			this.$initialize = function() {
				return function(element, clazz) {
					nabu.utils.elements.clear(element);
					var span = document.createElement("span");
					span.setAttribute("class", "n-icon n-icon-spinner" + (clazz ? " " + clazz : ""));
					span.setAttribute("style", "display: block; text-align: center");
					element.appendChild(span);
					return span;
				}
			}	
		},
		router: function router($services) {
			this.$initialize = function() {
				return new nabu.services.VueRouter({
					useHash: true,
					unknown: function(alias, parameters, anchor) {
						return $services.router.get("notFound");
					},
					authorizer: function(anchor, newRoute, newParameters, oldRoute, oldParameters) {
						if (newRoute.roles) {
							if (newRoute.roles.indexOf("$guest") < 0 && !$services.user.loggedIn) {
								return {
									alias: "login"
								}
							}
							else if (newRoute.roles.indexOf("$user") < 0 && $services.user.loggedIn) {
								return {
									alias: "home"
								}
							}
						}
					},
					chosen: function(anchor, newRoute, newParameters, oldRoute, oldParameters) {
						if (anchor && newRoute.slow) {
							nabu.utils.vue.render({
								target: anchor,
								content: new nabu.views.cms.core.Loader()
							});
						}	
					},
					enter: function(anchor, newRoute, newParameters, oldRoute, oldParameters, newRouteReturn) {
						$services.vue.route = newRoute.alias;
						// reset scroll
						// document.body.scrollTop = 0;
						window.scrollTo(0, 0);
					}
				});
			}
		},
		vue: function vue() {
			this.$initialize = function() {
				return new Vue({
					el: "body",
					data: function() {
						return {
							route: null
						}
					}
				});
			}
		},
		routes: application.routes,
		loaders: function($services) {
			this.$initialize = function() {
				var promises = [];
				for (var i = 0; i < application.loaders.length; i++) {
					var result = application.loaders[i]($services);
					if (result && result.then) {
						promises.push(result);
					}
				}
				return $services.q.all(promises);
			}
		}
	});
	return application.services.$initialize();
};