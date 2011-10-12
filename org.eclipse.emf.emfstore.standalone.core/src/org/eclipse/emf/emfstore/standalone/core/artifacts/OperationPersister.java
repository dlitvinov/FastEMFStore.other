package org.eclipse.emf.emfstore.standalone.core.artifacts;

import java.io.IOException;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.emfstore.client.model.impl.OperationRecorderListener;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;

/**
 * Persists operations into a history file.
 * 
 * @author emueller
 *
 */
public class OperationPersister extends AdapterImpl implements OperationRecorderListener {

	private Resource historyResource;
	private boolean isResourceModified;
	private URI historyUri;
	private Resource resource;

	/**
	 * Constructor.
	 * 
	 * @param resource the resource, whose location will be used to create the history file.
	 * @param historyFilePath the path at which the history file will be written
	 */
	public OperationPersister(Resource resource, XMIResource historyResource) {
		init(resource, historyResource);
	}
	
	private void init(Resource resource, XMIResource historyResource) {
		this.resource = resource;
		this.historyResource = historyResource;
//		ResourceSet resourceSet = resource.getResourceSet() == null ? new ResourceSetImpl() : resource.getResourceSet();
//		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot
		resource.setTrackingModification(true);
		resource.eAdapters().add(this);
	}
	
	public void dispose() {
		resource.setTrackingModification(false);
		resource.eAdapters().remove(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void operationRecorded(AbstractOperation operation) {
		historyResource.getContents().add(operation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyChanged(Notification msg) {
		if (msg.getFeatureID(Resource.class) == Resource.RESOURCE__IS_MODIFIED) {
			isResourceModified = !isResourceModified;
		}
		
		if (!isResourceModified) {
			saveResource();
		}
	}
	
	/**
	 * Saves the history file.
	 */
	private void saveResource() {
		try {
			historyResource.save(null);
		} catch (IOException e) {
			 ModelUtil.logException(String.format("Could not write operations file %s.",
					 historyUri), e);
		}
	}
}
