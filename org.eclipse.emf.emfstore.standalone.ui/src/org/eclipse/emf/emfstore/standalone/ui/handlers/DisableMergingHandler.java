package org.eclipse.emf.emfstore.standalone.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.emfstore.standalone.core.artifacts.ArtifactRegistry;
import org.eclipse.emf.emfstore.standalone.core.util.FileUtil;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class DisableMergingHandler extends AbstractTeamProviderHandler implements IViewActionDelegate {

	public DisableMergingHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(IAction action) {
		if (selectedFile != null) {
			URI platformURI = URI.createPlatformResourceURI(selectedFile.getFullPath().toString(), true);
			if (ArtifactRegistry.getInstance().isRegistered(platformURI)) {
				try {
					ArtifactRegistry.getInstance().unregister(platformURI);
				} catch (CoreException e) {
					MessageDialog.openError(Display.getCurrent().getActiveShell(), 
							"Disable merging",
							"The history file can not be deleted.");
				}
			} else if (!ArtifactRegistry.getInstance().isRegistered(platformURI) || !FileUtil.hasManagedFileExtension(selectedFile)) {
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), 
						"File is not under versioned control",
						"The selected file is not put under versioned control. TODO");
			} 
		} else {
			// TODO: actually this should not get executed ever
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Disable merging", 
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
