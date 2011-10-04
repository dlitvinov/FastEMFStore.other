package org.eclipse.emf.emfstore.standalone.core.workspace;

import org.eclipse.core.resources.IResource;

public interface IResourceOpenedObserver {

	void resourceOpened(IResource resource);
}
