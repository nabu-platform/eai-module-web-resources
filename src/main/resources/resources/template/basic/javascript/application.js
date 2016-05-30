var application = {
	configuration: {},
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