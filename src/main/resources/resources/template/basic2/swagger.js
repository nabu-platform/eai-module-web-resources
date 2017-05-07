if (!application) { var application = {} };
if (!application.definitions) { application.definitions = {} }

application.definitions.Swagger = function swagger($services) {
	this.$initialize = function() {
		// TODO: update the project name
		return new nabu.services.SwaggerClient({ definition: ${project.artifacts.swagger(environment("webApplicationId"))/swagger} });
	}
}