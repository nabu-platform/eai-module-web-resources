if (!application) { var application = {} };
if (!application.definitions) { application.definitions = {} }

application.definitions.Swagger = function($services) {
	
	var swaggerPath = "swagger.json";
	
	this.$initialize = function() {
		var promise = $services.q.defer();
		var service = new nabu.services.SwaggerClient({
			remember: function() {
				if ($services.user) {
					return $services.user.remember();
				}
				else {
					var promise = $services.q.defer();
					promise.reject();
					return promise;
				}
			}
		});
		promise.stage(service);
		
		nabu.utils.ajax({
			url: swaggerPath
		}).then(function(response) {
			service.loadDefinition(response.responseText);
			service.$clear = function() {
				return nabu.utils.ajax({
					url: swaggerPath
				}).then(function(response) {
					service.loadDefinition(response.responseText);	
				});
			}
			promise.resolve(service);
		}, function(error) {
			promise.reject(error);	
		});
		return promise;
	}
}