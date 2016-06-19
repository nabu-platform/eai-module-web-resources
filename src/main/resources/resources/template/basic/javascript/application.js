var application = {
	configuration: {
		url: "${environment('url', 'http://127.0.0.1')}",
		host: "${environment('host', '127.0.0.1')}"
	},
	services: {
		router: new nabu.services.VueRouter({
			useHash: true,
			unknown: function(alias, parameters, anchor) {
				return application.services.router.register({
					alias: alias,
					enter: function() {
						return alias;
					}
				});
			}
		})
	},
	views: {},
	components: {},
	utils: {},
	initialize: {
		vue: function () {
			application.services.vue = new Vue({
				el: 'body',
				data: {},
				created: function () {
					this.$broadcast("vue.ready");
				}
			});
		}
	}
};
Vue.mixin({
	computed: {
		$application: function() {
			return application;
		}
	}
});