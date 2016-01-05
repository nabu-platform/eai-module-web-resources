package be.nabu.eai.module.webartifact.resources;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import be.nabu.eai.developer.api.EntryContextMenuProvider;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.artifacts.web.WebArtifact;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.ReadableContainer;
import be.nabu.utils.io.api.WritableContainer;

public class WebArtifactContextMenu implements EntryContextMenuProvider {

	@Override
	public MenuItem getContext(Entry entry) {
		if (entry instanceof ResourceEntry && entry.isNode() && WebArtifact.class.isAssignableFrom(entry.getNode().getArtifactClass())) {
			Menu menu = new Menu("Web Resources");
			try {
				final ManageableContainer<?> javascript = (ManageableContainer<?>) ResourceUtils.mkdirs(((ResourceEntry) entry).getContainer(), EAIResourceRepository.PUBLIC + "/resources/javascript");
				menu.getItems().add(newMenuItem("Ajax v1.1", javascript, "resources/javascript/ajax-1.1.js"));
				menu.getItems().add(newMenuItem("D3 v3.5.12", javascript, "resources/javascript/d3-3.5.12.js"));
				menu.getItems().add(newMenuItem("Chart JS v1.0.2", javascript, "resources/javascript/chart-1.0.2.js"));
				menu.getItems().add(newMenuItem("Vue JS v1.0.13", javascript, "resources/javascript/vue-1.0.13.js", "resources/javascript/vue-router-0.7.7.js"));
				return menu;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private MenuItem newMenuItem(String name, final ManageableContainer<?> target, final String...paths) {
		MenuItem item = new MenuItem(name);
		item.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					for (String path : paths) {
						String fileName = path.replaceAll("^.*?/([^/]+)$", "$1");
						Resource child = target.getChild(fileName);
						if (child == null) {
							child = target.create(fileName, "application/javascript");
						}
						ReadableContainer<ByteBuffer> readable = IOUtils.wrap(EAIResourceRepository.getInstance().getMavenResource(path));
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
		});
		return item;
	}

}
