package org.eclipse.emf.emfstore.standalone.ui.handlers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.standalone.core.exceptions.VCSProviderNotFoundException;
import org.eclipse.emf.emfstore.standalone.core.vcs.IVCSProvider;
import org.eclipse.emf.emfstore.standalone.core.vcs.VCSProviderRegistry;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewActionDelegate;

public abstract class AbstractTeamProviderHandler implements IViewActionDelegate {
	
	private static final String TEAM_PROVIDER_KEY = "org.eclipse.team.core.repository";
	protected IFile selectedFile;
	
	protected String getTeamProviderID(IFile file) {
		IProject project = file.getProject();
		String key = TEAM_PROVIDER_KEY;
		int dot = key.lastIndexOf('.');
		QualifiedName qualifiedName = new QualifiedName(key.substring(0, dot), key.substring(dot + 1));
		try {
			Object object = project.getPersistentProperties().get(qualifiedName);
			return (String) object;
		} catch (CoreException e) {
			ModelUtil.logException("Can not determine team provider.", e);
		}
		
		return null;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (!(selection instanceof StructuredSelection)) {
			return;
		}
		
		StructuredSelection ss = (StructuredSelection)selection;
		
		if (!(ss.getFirstElement() instanceof IFile)) {
			return;				
		}
		
		selectedFile = (IFile) ss.getFirstElement();
		String teamProviderID = getTeamProviderID(selectedFile);
		try {
			IVCSProvider vcsProvider = VCSProviderRegistry.getInstance().getVCSProvider(teamProviderID);
			VCSProviderRegistry.getInstance().setActiveProvider(vcsProvider);
		} catch (VCSProviderNotFoundException e) {
			// do nothing
		}
	}
	
}
