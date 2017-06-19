if (!application) { var application = {} };
if (!application.definitions) { application.definitions = {} }

application.definitions.Swagger = function swagger($services) {
	this.$initialize = function() {
		// TODO: update the project name
		return new nabu.services.SwaggerClient({
			remember: function() {
				if ($services.user) {
					return $services.user.remember();
				}
				else {
					var promise = $services.q.defer();
					promise.reject();
					return promise;
				}
			},
			definition: ${project.artifacts.swagger(environment("webApplicationId"))/swagger} });
	}
}