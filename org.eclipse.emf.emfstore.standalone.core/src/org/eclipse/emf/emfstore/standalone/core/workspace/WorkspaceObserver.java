package org.eclipse.emf.emfstore.standalone.core.workspace;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.standalone.core.artifacts.ArtifactRegistry;
import org.eclipse.emf.emfstore.standalone.core.util.FileUtil;

public class WorkspaceObserver implements IResourceChangeListener {

	public void resourceChanged(IResourceChangeEvent event) {
		
		IResourceDelta delta = event.getDelta();
		ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();
		
		try {
			delta.accept(visitor);
		} catch (CoreException e) {
			ModelUtil.logException(e);

		}
		
		Set<IResource> addedResources = visitor.getAddedResources();
		
		for (IResource resource : addedResources) {
			if (!(resource instanceof IFile) || !FileUtil.isHistoryFile((IFile) resource)) {
				continue;
			}

			IFile historyFile = (IFile) resource;
			IFile artifactFile = FileUtil.getArtifactFile(historyFile);
			
			// new model resource together with its history resource must
			// have been added
			if (!ArtifactRegistry.getInstance().isRegistered(artifactFile)) {
				ArtifactRegistry.getInstance().flag(artifactFile);
			}
		}
	}
}
