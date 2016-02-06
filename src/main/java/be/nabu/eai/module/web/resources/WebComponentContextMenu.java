package be.nabu.eai.module.web.resources;

import java.net.URLConnection;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import be.nabu.eai.developer.api.EntryContextMenuProvider;
import be.nabu.eai.module.web.application.WebApplication;
import be.nabu.eai.module.web.component.WebComponent;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.ReadableContainer;
import be.nabu.utils.io.api.WritableContainer;

public class WebComponentContextMenu implements EntryContextMenuProvider {

	@Override
	public MenuItem getContext(Entry entry) {
		if (entry instanceof ResourceEntry && entry.isNode() && (WebApplication.class.isAssignableFrom(entry.getNode().getArtifactClass()) || WebComponent.class.isAssignableFrom(entry.getNode().getArtifactClass()))) {
			Menu menu = new Menu("Web Resources");
			try {
				ManageableContainer<?> publicDirectory = (ManageableContainer<?>) ResourceUtils.mkdirs(((ResourceEntry) entry).getContainer(), EAIResourceRepository.PUBLIC);
				ManageableContainer<?> javascript = (ManageableContainer<?>) ResourceUtils.mkdirs(publicDirectory, "resources/javascript");
				menu.getItems().add(newMenuItem(entry.getRepository(), "Ajax v1.1", javascript, "resources/javascript/ajax-1.1.js"));
				menu.getItems().add(newMenuItem(entry.getRepository(), "D3 v3.5.12", javascript, "resources/javascript/d3-3.5.12.js"));
				menu.getItems().add(newMenuItem(entry.getRepository(), "Chart JS v1.0.2", javascript, "resources/javascript/chart-1.0.2.js"));
				menu.getItems().add(newMenuItem(entry.getRepository(), "Vue JS v1.0.13", javascript, "resources/javascript/vue-1.0.13.js", "resources/javascript/vue-router-0.7.7.js"));
				menu.getItems().add(newMenuTemplateItem(entry.getRepository(), "Chartist v0.9.5", publicDirectory, "resources/javascript/chartist-0.9.5.js", "resources/css/chartist-0.9.5.css"));
				
				Menu templates = new Menu("Templates");
				templates.getItems().add(newMenuTemplateItem(entry.getRepository(), "Basic", publicDirectory, "resources/template/basic/index.eglue", "resources/template/basic/main.js", "resources/template/basic/main.css"));
				menu.getItems().add(templates);
				return menu;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
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

	private void copyFiles(Repository repository, final ManageableContainer<?> target, final String...paths) {
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
}
