/*******************************************************************************
 * Copyright (c) 2008-2011 Chair for Applied Software Engineering, Technische Universitaet Muenchen. All rights
 * reserved. This program and the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 ******************************************************************************/
package org.eclipse.emf.emfstore.standalone.core.workspace;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Factory;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.standalone.core.artifacts.Artifact;
import org.eclipse.emf.emfstore.standalone.core.artifacts.ArtifactRegistry;
import org.eclipse.emf.emfstore.standalone.core.exceptions.NoActiveVCSProviderException;
import org.eclipse.emf.emfstore.standalone.core.util.FileUtil;
import org.eclipse.emf.emfstore.standalone.core.vcs.IVCSProvider;
import org.eclipse.emf.emfstore.standalone.core.vcs.VCSProviderRegistry;

/**
 *
 * @author emueller
 */
public class ResourceFactoryRegistry implements Resource.Factory, Resource.Factory.Registry {

	/**
	 * The initial resource factory is kept if a file is not EMF Store managed, so the initial resource factory has to
	 * handle the file.
	 */
	private static Map<String, Object> initialResourceFactory = new HashMap<String, Object>();
	public static URI CURRENT_URI;
	
	/**
	 * The Resource.Factory.Registry will be manipulated, so that the EMFStoreResourceFactoryWrapper will be responsible
	 * for Ecore files.
	 */
	public static void replaceSupportedFactories() {
		Map<String, Object> extensionToFactoryMap = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
		
		// replace factories for all managed file extensions
		for (String fileExtension : FileUtil.getManagedFileExtensions()) {
			Object factory = extensionToFactoryMap.get(fileExtension);
			for (String key : extensionToFactoryMap.keySet()) {
				Object currentFactory = extensionToFactoryMap.get(key);
				if (factory == currentFactory) {
					// remember initial ResourceFactory
					initialResourceFactory.put(key, currentFactory);
					// replace factory in registry with an EMF Store adapted one.
					extensionToFactoryMap.put(key, new ResourceFactoryRegistry());
				}
			}
		}
	}
	
	private Set<IResourceOpenedObserver> resourceOpenedObservers;

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.ecore.resource.Resource.Factory#createResource(org.eclipse.emf.common.util.URI)
	 */
	public Resource createResource(final URI uri) {
	
		// use initial resource factory
		Object object = initialResourceFactory.get(uri.fileExtension());
		Factory factory = null;
		if (object instanceof Resource.Factory.Registry) {
			Resource.Factory.Registry registry = (Resource.Factory.Registry) object;
			factory = registry.getFactory(uri);

		} else if (object instanceof Resource.Factory.Descriptor) {
			Resource.Factory.Descriptor descriptor = (Resource.Factory.Descriptor) object;
			factory = descriptor.createFactory();
		}

		XMIResource xmiResource = (XMIResource) factory.createResource(uri);
		IFile artifactFile = FileUtil.getFile(xmiResource.getURI());
		
		IVCSProvider activeProvider = VCSProviderRegistry.getInstance().getActiveProvider();
		
		if (activeProvider == IVCSProvider.NONE) {
			return xmiResource;
		}
		
		try {
			// file has been marked to be put under recorder control
			if (ArtifactRegistry.getInstance().isFlagged(uri)) {
				//			IFile historyFile = FileUtil.getHistoryFile(artifactFile);
				Artifact artifact = new Artifact(xmiResource, FileUtil.getHistoryURI(xmiResource.getURI()));
				ArtifactRegistry.getInstance().register(artifact);
				artifact.initialize();
				VCSProviderRegistry.getInstance().getActiveProvider().addToVCS(FileUtil.getHistoryFile(artifactFile));						
				ArtifactRegistry.getInstance().unflag(uri);

			} else if (ArtifactRegistry.getInstance().isRegistered(uri)) {
				Artifact versionedArtifact = ArtifactRegistry.getInstance().getArtifact(uri);
				if (!versionedArtifact.isInitialized()) {
					versionedArtifact.initialize(xmiResource);
				}			
			}
		} catch (NoActiveVCSProviderException e) {
			// should not happen because of guard above
		}
		
		CURRENT_URI = uri;
		
		if (resourceOpenedObservers == null) {
			initResourceOpenedObservers();
		}
		
		notifyResourceOpenedObservers(artifactFile);
		
		

		return xmiResource;
	}
	
	private void notifyResourceOpenedObservers(IResource resource) {
		for (IResourceOpenedObserver observer : resourceOpenedObservers) {
			observer.resourceOpened(resource);
		}
	}

	private void initResourceOpenedObservers() {
		resourceOpenedObservers = new HashSet<IResourceOpenedObserver>();
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"org.eclipse.emf.emfstore.standalone.core.workspace.resourceOpened");

		for (IConfigurationElement extension : config) {
			try {
				IResourceOpenedObserver observer = (IResourceOpenedObserver) extension.createExecutableExtension("class");
				resourceOpenedObservers.add(observer);
			} catch (CoreException e) {
				ModelUtil.logWarning("Could not instantiate EMFStore Standalone ResourceOpenedObserver:"
						+ e.getMessage());
			}
		}
		
		if (resourceOpenedObservers.size() == 0) {
			throw new IllegalStateException("No ResourceOpenedObserver registered.  Artifacts will cause memory leaks. Quitting.");
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.ecore.resource.Resource.Factory.Registry#getFactory(org.eclipse.emf.common.util.URI)
	 */
	public Factory getFactory(URI uri) {
		return this;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.ecore.resource.Resource.Factory.Registry#getFactory(org.eclipse.emf.common.util.URI,
	 *      java.lang.String)
	 */
	public Factory getFactory(URI uri, String contentType) {
		return this;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.ecore.resource.Resource.Factory.Registry#getProtocolToFactoryMap()
	 */
	public Map<String, Object> getProtocolToFactoryMap() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.ecore.resource.Resource.Factory.Registry#getExtensionToFactoryMap()
	 */
	public Map<String, Object> getExtensionToFactoryMap() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.ecore.resource.Resource.Factory.Registry#getContentTypeToFactoryMap()
	 */
	public Map<String, Object> getContentTypeToFactoryMap() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static Object getDefaultFactory(String extension) {
		return initialResourceFactory.get(extension);
	}
}
