package org.eclipse.emf.emfstore.standalone.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.emf.emfstore.standalone.core.artifacts.Artifact;
import org.eclipse.emf.emfstore.standalone.core.artifacts.ArtifactRegistry;
import org.eclipse.emf.emfstore.standalone.core.exceptions.NoActiveVCSProviderException;
import org.eclipse.emf.emfstore.standalone.core.util.FileUtil;
import org.eclipse.emf.emfstore.standalone.core.vcs.IVCSProvider;
import org.eclipse.emf.emfstore.standalone.core.vcs.VCSProviderRegistry;
import org.eclipse.emf.emfstore.standalone.core.workspace.ResourceFactoryRegistry;
import org.eclipse.emf.emfstore.standalone.ui.ArtifactMerger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class ExecuteMergerHandler extends AbstractTeamProviderHandler implements IViewActionDelegate {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		return null;
	}

	@Override
	public void run(IAction action) {
		if (selectedFile != null) {
			
			IVCSProvider activeProvider = VCSProviderRegistry.getInstance().getActiveProvider();
			
			if (activeProvider == IVCSProvider.NONE) {
				MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Warning", 
						"The VCS provider could not be determined.");
				return;
			}
			
			Artifact versionedArtifact = ArtifactRegistry.getInstance().getArtifact(
					ResourceFactoryRegistry.CURRENT_URI);
			// TODO: how can this be not initalized?
			if (!versionedArtifact.isInitialized()) {
				try {
					versionedArtifact.initialize();
				} catch (NoActiveVCSProviderException e) {
					// should not happen because of guard above
				}
			}
			
			ArtifactMerger merger = new ArtifactMerger(activeProvider,
					selectedFile, versionedArtifact);
			merger.merge(); 
			
		} else {
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Execute merging", 
					"Please first select a conflicting history file.");
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (!(selection instanceof StructuredSelection)) {
			return;
		}

		StructuredSelection ss = (StructuredSelection) selection;
		Object firstElement = ss.getFirstElement();
		
		if (!(firstElement instanceof IFile)) {
			return;
		}
		
		IFile file = (IFile) firstElement;
		
		if (FileUtil.isHistoryFile(file)) {
			selectedFile = file;
		}		
	}

	@Override
	public void init(IViewPart view) {
		// TODO Auto-generated method stub
		
	}
}
