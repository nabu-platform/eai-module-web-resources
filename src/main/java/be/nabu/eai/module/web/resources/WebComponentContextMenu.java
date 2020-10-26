package be.nabu.eai.module.web.resources;

import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.EntryContextMenuProvider;
import be.nabu.eai.module.web.application.WebApplication;
import be.nabu.eai.module.web.application.WebApplicationManager;
import be.nabu.eai.module.web.application.WebFragment;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.types.DefinedTypeResolverFactory;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.ReadableContainer;
import be.nabu.utils.io.api.WritableContainer;

public class WebComponentContextMenu implements EntryContextMenuProvider {

	@Override
	public MenuItem getContext(Entry entry) {
		if (entry instanceof ResourceEntry && entry.isNode() && (WebApplication.class.isAssignableFrom(entry.getNode().getArtifactClass()))) {
			Menu menu = new Menu("Web Resources");
			try {
				ManageableContainer<?> publicDirectory = (ManageableContainer<?>) ResourceUtils.mkdirs(((ResourceEntry) entry).getContainer(), EAIResourceRepository.PUBLIC);
				ManageableContainer<?> privateDirectory = (ManageableContainer<?>) ResourceUtils.mkdirs(((ResourceEntry) entry).getContainer(), EAIResourceRepository.PRIVATE);
				ManageableContainer<?> javascript = (ManageableContainer<?>) ResourceUtils.mkdirs(publicDirectory, "resources/javascript");
//				menu.getItems().add(newMenuItem(entry.getRepository(), "Ajax v1.1", javascript, "resources/javascript/ajax-1.1.js"));
//				menu.getItems().add(newMenuItem(entry.getRepository(), "D3 v3.5.12", javascript, "resources/javascript/d3-3.5.12.js"));
//				menu.getItems().add(newMenuItem(entry.getRepository(), "Chart JS v1.0.2", javascript, "resources/javascript/chart-1.0.2.js"));
//				menu.getItems().add(newMenuItem(entry.getRepository(), "Vue JS v1.0.13", javascript, "resources/javascript/vue-1.0.13.js", "resources/javascript/vue-router-0.7.7.js"));
//				menu.getItems().add(newMenuTemplateItem(entry.getRepository(), "Chartist v0.9.5", publicDirectory, "resources/javascript/chartist-0.9.5.js", "resources/css/chartist-0.9.5.css"));
				
				Menu templates = new Menu("Templates");
//				templates.getItems().add(newMenuTemplateItem(entry.getRepository(), "Basic", publicDirectory, "resources/template/basic/index.eglue", "resources/template/basic/main.js", "resources/template/basic/main.css"));
//				templates.getItems().add(newBasicTemplate(entry, publicDirectory));
				templates.getItems().add(newBasic2Template(entry, publicDirectory, privateDirectory));
//				templates.getItems().add(newManagementTemplate(entry));
				templates.getItems().add(newPageTemplate(entry, publicDirectory, privateDirectory));
				templates.getItems().add(newPageWithCMSTemplate(entry, publicDirectory, privateDirectory));
				menu.getItems().add(templates);
				return menu;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private MenuItem newManagementTemplate(Entry entry) throws IOException {
		MenuItem item = new MenuItem("Management Module");
		item.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					ManageableContainer<?> privateDirectory = (ManageableContainer<?>) ResourceUtils.mkdirs(((ResourceEntry) entry).getContainer(), EAIResourceRepository.PRIVATE);
					String name;
					if (entry.getId().startsWith("nabu.management.")) {
						name = entry.getId().replaceAll("^nabu.management\\.([^.]+)\\..*", "$1");
					}
					else {
						name = entry.getId().replaceAll("^([^.]+)\\..*", "$1");
					}
					String fullName = "nabu.management.component." + name;
					ManageableContainer<?> gcss = (ManageableContainer<?>) ResourceUtils.mkdirs(privateDirectory, "scripts/nabu/management/component/" + name + "/gcss");
					ManageableContainer<?> tpl = (ManageableContainer<?>) ResourceUtils.mkdirs(privateDirectory, "scripts/nabu/management/component/" + name + "/tpl");
					ManageableContainer<?> js = (ManageableContainer<?>) ResourceUtils.mkdirs(privateDirectory, "scripts/nabu/management/component/" + name + "/js");
					
					writeFile(gcss, "component.gcss", "for (resource : resources(\"" + fullName + ".tpl.component\"))\n" + 
							"	if (resource ~ \".*\\.gcss\")\n" + 
							"		eval(resource(resource, \"" + fullName + ".tpl.component\"), scope(1))");
					
					writeFile(js, "component.glue", "for (resource : resources())\n" + 
							"	if (resource ~ \".*\\.js\")\n" + 
							"		echo(template(resource(resource)), \"\\n\")\n" + 
							"\n" + 
							"for (resource : resources(\"" + fullName + ".tpl.component\"))\n" + 
							"	if (resource ~ \".*\\.js\")\n" + 
							"		echo(template(resource(resource, \"" + fullName + ".tpl.component\")), \"\\n\")");
					
					ManageableContainer<?> jsComponent = (ManageableContainer<?>) ResourceUtils.mkdirs(js, "component");
					copyFiles(entry.getRepository(), jsComponent, "resources/template/basic/javascript/routes.js");
					
					writeFile(jsComponent, "application.js", "application.initialize.modules.push(function() {\n" + 
							"	application.services.vue.menu.push({\n" + 
							"		title: \"" + name + "\",\n" + 
							"		children: [{\n" + 
							"			title: \"Do Something!\",\n" + 
							"			handle: function() {\n" + 
							"				application.services.router.route(\"routeSomewhere\");\n" + 
							"			}\n" + 
							"		}]\n" + 
							"	});\n" + 
							"});");
					
					writeFile(tpl, "component.glue", "replacer = nabu.utils.lambdas()/templater\n" +
							"for (resource : resources())\n" + 
							"	if (resource ~ \".*\\.tpl\")\n" + 
							"		echo(replacer(template(resource(resource))), \"\\n\")");
				
					ManageableContainer<?> home = (ManageableContainer<?>) ResourceUtils.mkdirs(tpl, "component/views/home");
					copyFiles(entry.getRepository(), home, "resources/template/basic/home/home.gcss", "resources/template/basic/home/home.tpl", "resources/template/basic/home/home.js");
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		return item;
	}
	
	private MenuItem newBasicTemplate(Entry entry, final ManageableContainer<?> target) {
		MenuItem item = new MenuItem("Basic (v1)");
		item.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					Artifact artifact = entry.getNode().getArtifact();
					if (artifact instanceof WebApplication) {
						List<WebFragment> webFragments = ((WebApplication) artifact).getConfiguration().getWebFragments();
						if (webFragments == null) {
							webFragments = new ArrayList<WebFragment>();
							((WebApplication) artifact).getConfiguration().setWebFragments(webFragments);
						}
						boolean found = false;
						for (WebFragment fragment : webFragments) {
							if ("nabu.web.core.components".equals(fragment.getId())) {
								found = true;
								break;
							}
						}
						if (!found) {
							webFragments.add((WebFragment) entry.getRepository().getEntry("nabu.web.core.components").getNode().getArtifact());
						}
					}
					ManageableContainer<?> pages = (ManageableContainer<?>) ResourceUtils.mkdirs(target, "pages");
					ManageableContainer<?> homeView = (ManageableContainer<?>) ResourceUtils.mkdirs(target, "pages/index/views/home");
					ManageableContainer<?> javascript = (ManageableContainer<?>) ResourceUtils.mkdirs(target, "pages/javascript");
					ManageableContainer<?> applicationJavascript= (ManageableContainer<?>) ResourceUtils.mkdirs(target, "pages/javascript/application");
					ManageableContainer<?> css = (ManageableContainer<?>) ResourceUtils.mkdirs(target, "pages/css");
					ManageableContainer<?> applicationCss = (ManageableContainer<?>) ResourceUtils.mkdirs(target, "pages/css/application");
					// copy the index file
					copyFiles(entry.getRepository(), pages, "resources/template/basic/index.eglue");
					// copy the home view
					copyFiles(entry.getRepository(), homeView, "resources/template/basic/home/home.gcss", "resources/template/basic/home/home.tpl", "resources/template/basic/home/home.js");
					// copy the javascript glue files
					copyFiles(entry.getRepository(), javascript, "resources/template/basic/javascript/application.glue", 
							"resources/template/basic/javascript/nabu.glue",
							"resources/template/basic/javascript/vendor.glue");
					// copy the actual javascript files
					copyFiles(entry.getRepository(), applicationJavascript, "resources/template/basic/javascript/application.js",
							"resources/template/basic/javascript/mobile.js",
							"resources/template/basic/javascript/web.js",
							"resources/template/basic/javascript/routes.js");
					// copy the css glue file
					copyFiles(entry.getRepository(), css, "resources/template/basic/css/application.gcss");
					// copy the actual css files
					copyFiles(entry.getRepository(), applicationCss, "resources/template/basic/css/resources/application.gcss",
							"resources/template/basic/css/resources/mobile.gcss",
							"resources/template/basic/css/resources/web.gcss",
							"resources/template/basic/css/resources/media.gcss");
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		return item;
	}
	
	private MenuItem newBasic2Template(Entry entry, final ManageableContainer<?> publicDirectory, final ManageableContainer<?> privateDirectory) {
		MenuItem item = new MenuItem("Basic");
		item.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					Artifact artifact = entry.getNode().getArtifact();
					if (artifact instanceof WebApplication) {
						List<WebFragment> webFragments = ((WebApplication) artifact).getConfiguration().getWebFragments();
						if (webFragments == null) {
							webFragments = new ArrayList<WebFragment>();
							((WebApplication) artifact).getConfiguration().setWebFragments(webFragments);
						}
						boolean found = false;
						for (WebFragment fragment : webFragments) {
							if ("nabu.web.core.components".equals(fragment.getId())) {
								found = true;
								break;
							}
						}
						if (!found) {
							webFragments.add((WebFragment) entry.getRepository().getEntry("nabu.web.core.components").getNode().getArtifact());
						}
					}
					ManageableContainer<?> pages = (ManageableContainer<?>) ResourceUtils.mkdirs(publicDirectory, "pages");
					ManageableContainer<?> artifacts = (ManageableContainer<?>) ResourceUtils.mkdirs(publicDirectory, "artifacts");
					ManageableContainer<?> homeView = (ManageableContainer<?>) ResourceUtils.mkdirs(publicDirectory, "artifacts/views/home");
					ManageableContainer<?> indexView = (ManageableContainer<?>) ResourceUtils.mkdirs(publicDirectory, "artifacts/views/index");
					ManageableContainer<?> javascript = (ManageableContainer<?>) ResourceUtils.mkdirs(publicDirectory, "pages/resources/javascript");
					ManageableContainer<?> css = (ManageableContainer<?>) ResourceUtils.mkdirs(publicDirectory, "pages/resources/css");

					ManageableContainer<?> provided = (ManageableContainer<?>) ResourceUtils.mkdirs(privateDirectory, "provided");
					
					// copy the index file
					copyFiles(entry.getRepository(), pages, "resources/template/basic2/index.glue");
					// copy the home view
					copyFiles(entry.getRepository(), homeView, "resources/template/basic/home/home.gcss", "resources/template/basic/home/home.tpl", "resources/template/basic/home/home.js");
					// copy the index view
					copyFiles(entry.getRepository(), indexView, "resources/template/basic/index/index.gcss", "resources/template/basic/index/index.tpl", "resources/template/basic/index/index.js");
					// copy the javascript glue files
					copyFiles(entry.getRepository(), javascript, "resources/template/basic2/application.glue");
					// copy the actual javascript files
					copyFiles(entry.getRepository(), artifacts, "resources/template/basic2/application.js",
							"resources/template/basic2/swagger.js",
							"resources/template/basic2/web.js",
							"resources/template/basic2/mobile.js",
							"resources/template/basic2/routes.js");
					// copy the css glue file
					copyFiles(entry.getRepository(), css, "resources/template/basic2/application.gcss");
					
					// the bundle
					copyFiles(entry.getRepository(), provided, "resources/template/basic2/bundle.json");
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		return item;
	}
	
	private MenuItem newPageWithCMSTemplate(Entry entry, final ManageableContainer<?> publicDirectory, final ManageableContainer<?> privateDirectory) {
		MenuItem item = new MenuItem("Page Builder (CMS)");

		item.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				copyPageWithCms(entry, publicDirectory, privateDirectory);
			}

		});
		return item;
	}
	
	public static void copyPageWithCms(Entry entry, final ManageableContainer<?> publicDirectory, final ManageableContainer<?> privateDirectory) {
		// copy basic template
		// we add the component to initialize an editor and the component to add password protection (optional)
		// the order is somewhat important:
		// because external components do a promote() on preprocessors, the last components are triggered first
		// we first want to pass by the password protect (if any), and only then set an administrative user
		// because the administrative user creator checks that no redirects are active, it plays well together
		// if you reverse them, it still works as the password protect is called after the administrative and overwrites it, you just get a slightly ugly redirect at the end
		copyPageTemplate(entry, publicDirectory, privateDirectory, 
				// load cms
				"nabu.cms.core.components.main", 
				// allow creation of an initial administrator user
				"nabu.web.page.cms.initialAdministrator.component", 
				// use password protect for qlty deployment etc
				"nabu.web.core.passwordProtect",
				// general cms integration stuff with the page builder
				"nabu.web.page.cms.component",
				// allow for dynamic cms components
				"nabu.cms.dynamic.component");
		
		try {
			Artifact artifact = (WebApplication) entry.getNode().getArtifact();
			if (artifact instanceof WebApplication && entry instanceof ResourceEntry) {
				
				// set stuff in the web application
				WebApplication application = (WebApplication) artifact;
				if (application.getConfig().getRealm() == null) {
					application.getConfig().setRealm(artifact.getId().replaceAll("^([^.]+).*", "$1"));
				}
				if (application.getConfig().getPasswordAuthenticationService() == null) {
					application.getConfig().setPasswordAuthenticationService((DefinedService) entry.getRepository().resolve("nabu.cms.core.providers.security.passwordAuthenticator"));
				}
				if (application.getConfig().getSecretGeneratorService() == null) {
					application.getConfig().setSecretGeneratorService((DefinedService) entry.getRepository().resolve("nabu.cms.core.providers.security.secretGenerator"));
				}
				if (application.getConfig().getSecretAuthenticationService() == null) {
					application.getConfig().setSecretAuthenticationService((DefinedService) entry.getRepository().resolve("nabu.cms.core.providers.security.secretAuthenticator"));
				}
				if (application.getConfig().getRoleService() == null) {
					application.getConfig().setRoleService((DefinedService) entry.getRepository().resolve("nabu.cms.core.providers.security.roleHandler"));
				}
				// choose: either role handler or permission handler
				// a lot of simple applications only have role handler (including management screens etc)
				// and most complex applications start simple with only the role handler, graduating to permission handler over time
				// this probably requires more settings anyway
//				if (application.getConfig().getPermissionService() == null) {
//					application.getConfig().setPermissionService((DefinedService) entry.getRepository().resolve("nabu.cms.core.providers.security.permissionHandler"));
//				}
//				if (application.getConfig().getPotentialPermissionService() == null) {
//					application.getConfig().setPotentialPermissionService((DefinedService) entry.getRepository().resolve("nabu.cms.core.providers.security.potentialPermissionHandler"));
//				}
				if (application.getConfig().getTranslationService() == null) {
					application.getConfig().setTranslationService((DefinedService) entry.getRepository().resolve("nabu.cms.core.providers.translation.translationService"));
				}
				if (application.getConfig().getLanguageProviderService() == null) {
					application.getConfig().setLanguageProviderService((DefinedService) entry.getRepository().resolve("nabu.cms.core.providers.translation.languageProvider"));
				}
				if (application.getConfig().getSupportedLanguagesService() == null) {
					application.getConfig().setSupportedLanguagesService((DefinedService) entry.getRepository().resolve("nabu.cms.core.providers.translation.supportedLanguages"));
				}
				if (application.getConfig().getDeviceValidatorService() == null) {
					application.getConfig().setDeviceValidatorService((DefinedService) entry.getRepository().resolve("nabu.cms.core.providers.security.deviceValidator"));
				}
				if (application.getConfig().getFeatureTestingRole() == null || application.getConfig().getFeatureTestingRole().isEmpty()) {
					application.getConfig().setFeatureTestingRole(new ArrayList<String>(Arrays.asList("tester", "editor")));
				}
				
				// update the CMS configuration
				ComplexContent configuration = application.getConfigurationFor(".*", (ComplexType) DefinedTypeResolverFactory.getInstance().getResolver().resolve("nabu.cms.core.configuration"));
				if (configuration == null) {
					configuration = ((ComplexType) DefinedTypeResolverFactory.getInstance().getResolver().resolve("nabu.cms.core.configuration")).newInstance();
				}
				if (configuration.get("context") == null) {
					ComplexContent context = ((ComplexType) configuration.getType().get("context").getType()).newInstance();
					context.set("type", "webApplication");
					context.set("contextResolver", "nabu.web.page.cms.providers.contextResolver");
					configuration.set("context[0]", context);
				}
				application.putConfiguration(configuration, null, false);
				
				// update the page configuration
				configuration = application.getConfigurationFor(".*", (ComplexType) DefinedTypeResolverFactory.getInstance().getResolver().resolve("nabu.web.page.core.types.configuration"));
				if (configuration == null) {
					configuration = ((ComplexType) DefinedTypeResolverFactory.getInstance().getResolver().resolve("nabu.web.page.core.types.configuration")).newInstance();
				}
				configuration.set("providers/getAllContent", "nabu.web.page.cms.providers.content.getAllContent");
				configuration.set("providers/setContent", "nabu.web.page.cms.providers.content.setContent");
				configuration.set("providers/translationProvider", "nabu.web.page.cms.providers.content.translationProvider");
				application.putConfiguration(configuration, null, false);
				
				new WebApplicationManager().save((ResourceEntry) entry, application);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private MenuItem newPageTemplate(Entry entry, final ManageableContainer<?> publicDirectory, final ManageableContainer<?> privateDirectory) {
		MenuItem item = new MenuItem("Page Builder");
		item.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				copyPageTemplate(entry, publicDirectory, privateDirectory);
				try {
					ManageableContainer<?> services = (ManageableContainer<?>) ResourceUtils.mkdirs(publicDirectory, "artifacts/services");
					copyFiles(entry.getRepository(), services, "resources/template/basic2/user.js");
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		return item;
	}
	
	public static void copyPageTemplate(Entry entry, final ManageableContainer<?> publicDirectory, final ManageableContainer<?> privateDirectory, String...components) {
		try {
			Artifact artifact = entry.getNode().getArtifact();
			if (artifact instanceof WebApplication && entry instanceof ResourceEntry) {
				List<String> componentsToLoad = new ArrayList<String>();
				if (components != null && components.length > 0) {
					componentsToLoad.addAll(Arrays.asList(components));
				}
				// always need the core (contains the resolve, the index and javascript pages etc)
				componentsToLoad.add("nabu.web.core.components");
				componentsToLoad.add("nabu.web.page.core.component");
				componentsToLoad.add("nabu.web.page.data.component");
				
				List<String> loaded = new ArrayList<String>();
				
				// close the web application
				MainController.getInstance().close(artifact.getId());
				List<WebFragment> webFragments = ((WebApplication) artifact).getConfiguration().getWebFragments();
				if (webFragments == null) {
					webFragments = new ArrayList<WebFragment>();
					((WebApplication) artifact).getConfiguration().setWebFragments(webFragments);
				}
				for (WebFragment fragment : webFragments) {
					if (fragment != null) {
						loaded.add(fragment.getId());
					}
				}
				
				if (((WebApplication) artifact).getConfig().getFeatureTestingRole() == null || ((WebApplication) artifact).getConfig().getFeatureTestingRole().isEmpty()) {
					((WebApplication) artifact).getConfig().setFeatureTestingRole(new ArrayList<String>(Arrays.asList("tester", "editor")));
				}
				
				for (String component : componentsToLoad) {
					if (!loaded.contains(component)) {
						Entry fragmentEntry = entry.getRepository().getEntry(component);
						if (fragmentEntry != null) {
							webFragments.add((WebFragment) fragmentEntry.getNode().getArtifact());
						}
						else {
							System.out.println("Skipping non-existent component: " + component);
						}
					}
				}
				// save the changes
				new WebApplicationManager().save((ResourceEntry) entry, (WebApplication) artifact);
				
			}
			ManageableContainer<?> pages = (ManageableContainer<?>) ResourceUtils.mkdirs(publicDirectory, "pages");
			ManageableContainer<?> artifacts = (ManageableContainer<?>) ResourceUtils.mkdirs(publicDirectory, "artifacts");
			ManageableContainer<?> homeView = (ManageableContainer<?>) ResourceUtils.mkdirs(publicDirectory, "artifacts/views/home");
			ManageableContainer<?> indexView = (ManageableContainer<?>) ResourceUtils.mkdirs(publicDirectory, "artifacts/views/index");
			ManageableContainer<?> javascript = (ManageableContainer<?>) ResourceUtils.mkdirs(publicDirectory, "pages/resources/javascript");
			ManageableContainer<?> css = (ManageableContainer<?>) ResourceUtils.mkdirs(publicDirectory, "pages/resources/css");
			
			ManageableContainer<?> provided = (ManageableContainer<?>) ResourceUtils.mkdirs(privateDirectory, "provided");
			
			// copy the index file
			copyFiles(entry.getRepository(), pages, "resources/template/basic2/index.glue");
			// copy the home view
			copyFiles(entry.getRepository(), homeView, "resources/template/basic/home/home.tpl", "resources/template/basic/home/home.js");
			// copy the index view
			copyFiles(entry.getRepository(), indexView, "resources/template/basic/index/index.tpl", "resources/template/basic/index/index.js");
			// copy the javascript glue files
			copyFiles(entry.getRepository(), javascript, "resources/template/basic2/application.glue");
			// copy the actual javascript files
			copyFiles(entry.getRepository(), artifacts, "resources/template/basic2/application.js",
					"resources/template/basic2/swagger.js",
					"resources/template/basic2/web.js",
					"resources/template/basic2/mobile.js",
					"resources/template/basic2/routes.js");
			// copy the css glue file
			copyFiles(entry.getRepository(), css, "resources/template/page/application.glue");
			
			// the bundle
			copyFiles(entry.getRepository(), provided, "resources/template/page/bundle.json");
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private MenuItem newMenuTemplateItem(Repository repository, String name, final ManageableContainer<?> target, final String...paths) {
		MenuItem item = new MenuItem(name);
		item.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					ManageableContainer<?> javascript = (ManageableContainer<?>) ResourceUtils.mkdirs(target, "resources/javascript");
					ManageableContainer<?> css = (ManageableContainer<?>) ResourceUtils.mkdirs(target, "resources/css");
					ManageableContainer<?> pages = (ManageableContainer<?>) ResourceUtils.mkdirs(target, "pages");
					for (String path : paths) {
						if (path.endsWith(".js")) {
							copyFiles(repository, javascript, path);
						}
						else if (path.endsWith(".eglue") || path.endsWith(".glue")) {
							copyFiles(repository, pages, path);
						}
						else if (path.endsWith(".css")) {
							copyFiles(repository, css, path);
						}
					}
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		return item;
	}

	private MenuItem newMenuItem(Repository repository, String name, final ManageableContainer<?> target, final String...paths) {
		MenuItem item = new MenuItem(name);
		item.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				copyFiles(repository, target, paths);
			}
		});
		return item;
	}

	public static void copyFiles(Repository repository, final ManageableContainer<?> target, final String...paths) {
		try {
			for (String path : paths) {
				String fileName = path.replaceAll("^.*?/([^/]+)$", "$1");
				Resource child = target.getChild(fileName);
				if (child == null) {
					child = target.create(fileName, URLConnection.guessContentTypeFromName(fileName));
				}
				ReadableContainer<ByteBuffer> readable = IOUtils.wrap(repository.getClassLoader().getResourceAsStream(path));
				try {
					WritableContainer<ByteBuffer> writable = ((WritableResource) child).getWritable();
					try {
						IOUtils.copyBytes(readable, writable);
					}
					finally {
						writable.close();
					}
				}
				finally {
					readable.close();
				}
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void writeFile(final ManageableContainer<?> target, String fileName, String content) throws IOException {
		Resource child = target.getChild(fileName);
		if (child == null) {
			child = target.create(fileName, URLConnection.guessContentTypeFromName(fileName));
		}
		WritableContainer<ByteBuffer> writable = ((WritableResource) child).getWritable();
		try {
			IOUtils.copyBytes(IOUtils.wrap(content.getBytes("UTF-8"), true), writable);
		}
		finally {
			writable.close();
		}
	}
}
