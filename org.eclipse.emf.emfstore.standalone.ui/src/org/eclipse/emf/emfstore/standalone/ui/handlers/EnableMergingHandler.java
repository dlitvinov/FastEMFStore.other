package org.eclipse.emf.emfstore.standalone.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.emfstore.standalone.core.artifacts.ArtifactRegistry;
import org.eclipse.emf.emfstore.standalone.core.util.FileUtil;
import org.eclipse.emf.emfstore.standalone.core.vcs.IVCSProvider;
import org.eclipse.emf.emfstore.standalone.core.vcs.VCSProviderRegistry;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class EnableMergingHandler extends AbstractTeamProviderHandler implements IViewActionDelegate {

	@Override
	public void run(IAction action) {
		
		if (selectedFile != null) {
			if (VCSProviderRegistry.getInstance().getActiveProvider() == IVCSProvider.NONE) {
				MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Enable merging", 
						"The VCS provider could not be determined.");
			}
			
			if (ArtifactRegistry.getInstance().isRegistered(FileUtil.getUri(selectedFile))) {
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), 
						"File already under EMFStore merger control",
						"The selected file already is under control of EMFStore merger.");
			} else if (FileUtil.hasManagedFileExtension(selectedFile)) {
				ArtifactRegistry.getInstance().flag(FileUtil.getUri(selectedFile));
			} else {
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), 
						"File can not be put under control of EMFStore merger",
						"The selected file can not be put under control of EMFStore merger. TODO"); 
			}
		} else {
			// TODO: actually this should not get executed ever
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Enable merging", 
					"Please first select a file");
		}
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		return null;
	}

	@Override
	public void init(IViewPart view) {
		// TODO Auto-generated method stub
		
	}

}
