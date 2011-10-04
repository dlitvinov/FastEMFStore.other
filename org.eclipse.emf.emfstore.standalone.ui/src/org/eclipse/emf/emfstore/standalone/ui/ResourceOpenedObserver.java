package org.eclipse.emf.emfstore.standalone.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.emfstore.standalone.core.workspace.IResourceOpenedObserver;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class ResourceOpenedObserver implements IResourceOpenedObserver {

	public ResourceOpenedObserver() {

	}

	@Override
	public void resourceOpened(IResource resource) {
		
		if (!(resource instanceof IFile)) {
			return;
		}
		
		IFile artifactFile = (IFile) resource;
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		
		if (win != null) {
			IWorkbenchPage page = win.getActivePage();
			page.addPartListener(new ArtifactDisposeListener(artifactFile));
		}
	}

}
