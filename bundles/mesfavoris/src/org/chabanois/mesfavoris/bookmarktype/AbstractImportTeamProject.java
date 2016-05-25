package org.chabanois.mesfavoris.bookmarktype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public abstract class AbstractImportTeamProject implements IImportTeamProject, IExecutableExtension {
	private ImageDescriptor imageDescriptor;
	
	public ImageDescriptor getIcon() {
		return this.imageDescriptor;
	}
	
	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		String iconResource = config.getAttribute("icon");
		this.imageDescriptor = iconResource == null ? null : AbstractUIPlugin
				.imageDescriptorFromPlugin(config.getDeclaringExtension().getContributor().getName(), iconResource);
	}
	
}
