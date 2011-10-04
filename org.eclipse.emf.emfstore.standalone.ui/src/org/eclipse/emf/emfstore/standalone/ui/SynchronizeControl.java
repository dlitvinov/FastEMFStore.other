package org.eclipse.emf.emfstore.standalone.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.emfstore.standalone.core.vcs.VCSProviderRegistry;
import org.eclipse.emf.emfstore.standalone.core.workspace.ResourceFactoryRegistry;
import org.eclipse.emf.emfstore.standalone.core.workspace.WorkspaceObserver;
import org.eclipse.ui.IStartup;

public class SynchronizeControl extends WorkspaceObserver implements IStartup {

	
	/**
	 * {@inheritDoc}
	 */
	public void earlyStartup() {
		ResourceFactoryRegistry.replaceSupportedFactories();
		
		try {
			VCSProviderRegistry.getInstance().initVCSProviders();
			VCSProviderRegistry.getInstance().initArtifacts();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO: do we need to dispose this? I guess not?
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);	
	}


}
