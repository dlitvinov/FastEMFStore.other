package org.eclipse.emf.emfstore.standalone.core.artifacts;

import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.emfstore.client.model.impl.OperationRecorder;
import org.eclipse.emf.emfstore.common.model.Project;
import org.eclipse.emf.emfstore.common.model.impl.ProjectImpl;
import org.eclipse.emf.emfstore.standalone.core.exceptions.NoActiveVCSProviderException;
import org.eclipse.emf.emfstore.standalone.core.util.FileUtil;
import org.eclipse.emf.emfstore.standalone.core.vcs.IVCSProvider;
import org.eclipse.emf.emfstore.standalone.core.vcs.VCSProviderRegistry;

public class Artifact {

	private XMIResource xmiResource;
	private XMIResource historyResource;
	private OperationRecorder operationRecorder;
	private OperationPersister operationPersister;
	private boolean isInitialized;
	private Project project;
	private URI historyURI;

	/**
	 * Constructor.
	 * @param modelResource the model resource
	 */
	public Artifact(XMIResource modelResource, URI historyURI) {
		this.xmiResource = modelResource;
		this.historyURI = historyURI;
	}
	
	public void initialize() throws NoActiveVCSProviderException {
		if (getModelResource() != null) {
			initialize(getModelResource());
		}
	}
	
	public void initialize(XMIResource xmiResource) throws NoActiveVCSProviderException {
		this.xmiResource = xmiResource;
		boolean shouldClearHistoryResource = false;
		
		IVCSProvider activeProvider = VCSProviderRegistry.getInstance().getActiveProvider();
		if (activeProvider == IVCSProvider.NONE) {
			throw new NoActiveVCSProviderException();
		}
		
		try {
			project = new ProjectImpl(getModelResource());
			operationRecorder = new OperationRecorder(getProject(), getProject().getChangeNotifier());
			
			if (activeProvider.isModified(FileUtil.getFile(historyURI))) {
				shouldClearHistoryResource   = true;
			}
			
			ResourceSet resourceSet = getModelResource().getResourceSet() == null ? new ResourceSetImpl() : getModelResource().getResourceSet();
			this.historyResource = ((XMIResource) resourceSet.createResource(historyURI));
			
			if (shouldClearHistoryResource) {
				historyResource.getContents().clear();
			}
			
			historyResource.save(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		operationPersister = new OperationPersister(getModelResource(), getHistoryResource());
		operationRecorder.addOperationRecorderListener(operationPersister);		
		operationRecorder.startChangeRecording();
		isInitialized = true;
	}
	
	public void dispose() {
		// TODO: in which cases are the recorder and the persister null?
		if (operationRecorder != null){
			operationRecorder.stopChangeRecording();
			operationRecorder.removeOperationRecorderListener(operationPersister);
		}

		if (operationPersister != null){
			operationPersister.dispose();
		}
		
		this.project = null;
		operationRecorder = null;
		operationPersister = null;		
		isInitialized = false;
	}

	public boolean isInitialized() {
		return isInitialized;
	}
	
	public URI getURI() {
		return getModelResource().getURI();
	}

	// TODO: hide
	public Project getProject() {
		return project;
	}

	/**
	 * Returns the history resource of this artifact.
	 * @return the history resource
	 */
	public XMIResource getHistoryResource() {
		return historyResource;
	}
	
	/**
	 * Returns the model resource of this artifact.
	 * @return the model resource
	 */
	public XMIResource getModelResource() {
		return xmiResource;
	}

	public void setProject(Project project) {
		this.project = project;
	}
}
