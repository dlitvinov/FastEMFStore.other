package org.eclipse.emf.emfstore.standalone.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.emfstore.standalone.core.artifacts.Artifact;
import org.eclipse.emf.emfstore.standalone.core.artifacts.ArtifactRegistry;
import org.eclipse.emf.emfstore.standalone.core.util.FileUtil;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

public class ArtifactDisposeListener implements IPartListener2 {
	
	// TODO: read ID from ecore editor plugin
	public static final String ECORE_EDITOR_ID = "org.eclipse.emf.ecore.presentation.EcoreEditorID";
	private String fileName;
	private final IFile file;

	public ArtifactDisposeListener(IFile file) {
		this.file = file;
		fileName = file.getName();
	}

	public void partActivated(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	public void partClosed(IWorkbenchPartReference partRef) {
		if (partRef.getId().equals(ECORE_EDITOR_ID)) {
			String partName = partRef.getPartName();
			if (partName.equals(fileName)) {
				URI uri = FileUtil.getUri(file);
				Artifact mergerArtifact = ArtifactRegistry.getInstance().getArtifact(uri);
				// may be null in case file is not under merger control
				if (mergerArtifact != null) {
					mergerArtifact.dispose();
				}
			}
		}
		partRef.getPage().removePartListener(this);
	}

	public void partDeactivated(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	public void partOpened(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	public void partHidden(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	public void partVisible(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	public void partInputChanged(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

}
