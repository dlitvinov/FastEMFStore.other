package org.eclipse.emf.emfstore.standalone.core.vcs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.standalone.core.artifacts.ArtifactRegistry;
import org.eclipse.emf.emfstore.standalone.core.exceptions.VCSProviderNotFoundException;
import org.eclipse.emf.emfstore.standalone.core.util.FileUtil;


public class VCSProviderRegistry {
	
	private static VCSProviderRegistry instance;
	private static Map<String, IVCSProvider> providers;
	// TODO: currentProvider nach ArtifactMerger umziehen oder entsprechendes Attribute anlegen
	private IVCSProvider currentProvider;
	
	private VCSProviderRegistry() {
		providers = new HashMap<String, IVCSProvider>();
	}
	
	public static VCSProviderRegistry getInstance() {
		if (instance == null) {
			instance = new VCSProviderRegistry();
		}
		
		return instance;
	}
	
	public IVCSProvider getActiveProvider() {
		return currentProvider;
	}
	
	public void setActiveProvider(IVCSProvider provider) {
		currentProvider = provider;
	}

	public void initArtifacts() throws CoreException, InvocationTargetException, IOException {
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		
		for (IProject project : root.getProjects()) {
			for (IFile file: fetchAllFiles(project, new HashSet<IFile>())) {
				
				if (!FileUtil.isHistoryFile(file)) {
					continue;
				}

				// we know that file now needs to be the history file 
				IFile artifactFile = FileUtil.getArtifactFile(file);
				
				if (!ArtifactRegistry.getInstance().isRegistered(artifactFile) 
						|| !ArtifactRegistry.getInstance().isFlagged(artifactFile)) {
					ArtifactRegistry.getInstance().flag(artifactFile);
//					VCS_PROVIDER.clearHistoryFile(FileUtil.getHistoryFile(artifactFile));
				}
			}
		}
	}
	
	/**
	 * Fetches all instances of {@link IFile} contained in a {@link IContainer}.
	 * 
	 * @param container the container to fetch the files from
	 * @param files a collection that will hold all instances of files, may be empty, but not null
	 * @return the given <code>files</code> collection with all files contained within the given container
	 * @throws CoreException if the given container either does not exist or is not accessible
	 * @throws IllegalArgumentException if <code>files</code> is null
	 */
	private Collection<IFile> fetchAllFiles(IContainer container, Collection<IFile> files) 
		throws CoreException, IllegalArgumentException {
		
		if (files == null) {
			throw new IllegalArgumentException("files must not be null");
		}
		
		for (IResource resource : container.members()) {
			if (resource instanceof IContainer) {
				IContainer c = (IContainer) resource;
				files.addAll(fetchAllFiles(c, files));
			} else if (resource instanceof IFile){
				files.add((IFile) resource);
			}
		}
		return files; 
	}
	
	public void initVCSProviders() {
//		if (vcsProviders == null) {
			// collect singleton ID resolvers
//			vcsProviders = new HashMap<String, IVCSProvider>();
			IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
					"org.eclipse.emf.emfstore.standalone.core.vcs.provider");

			for (IConfigurationElement extension : config) {
				try {
					IVCSProvider provider = (IVCSProvider) extension.createExecutableExtension("class");
					String providerId =  extension.getAttribute("id");
					providers.put(providerId, provider);					
				} catch (CoreException e) {
					ModelUtil.logWarning("Could not instantiate EMFStore Standalone VCS connector:"
							+ e.getMessage());
				}
			}
//		}
	}

	/**
	 * Returns the team provider with the given ID.
	 * @param teamProviderID the ID of a team provider, e.g. <code></code>
	 * @return the team provider
	 * @throws VCSProviderNotFoundException if the desired team provider is not registered
	 */
	public IVCSProvider getVCSProvider(String teamProviderID) throws VCSProviderNotFoundException {
		IVCSProvider provider = providers.get(teamProviderID);
		
		if (provider == null) {
			throw new VCSProviderNotFoundException();
		}
		
		return provider;
	}

}
