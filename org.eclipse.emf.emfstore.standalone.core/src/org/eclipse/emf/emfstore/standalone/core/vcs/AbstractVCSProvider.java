package org.eclipse.emf.emfstore.standalone.core.vcs;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.common.model.util.SerializationException;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.xml.sax.InputSource;

public abstract class AbstractVCSProvider implements IVCSProvider {

	/**
	 * URI used to serialize EObject with the model util.
	 */
	public static final URI VIRTUAL_URI = URI.createURI("virtualUri");
	
	public List<AbstractOperation> getTheirOperations(IResource resource, IProgressMonitor monitor) {
		try {
			String theirRevision = getTheirRevision(resource, monitor);
			List<AbstractOperation> theirOperations = getOperations(theirRevision);
			return theirOperations;
		} catch (IOException moe) {
			// TODO Auto-generated moe catch block
			moe.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public List<AbstractOperation> getMyOperations(IResource resource, IProgressMonitor monitor) {
		try {
			String myRevision = getMyRevision(resource, monitor);
			List<AbstractOperation> myOperations = getOperations(myRevision);
			return myOperations;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return null;
	}
	
	private List<AbstractOperation> getOperations(String s) {
		// TODO Auto-generated method stub
		EList<EObject> ops1 = null;
		try {
			ops1 = stringToEObject(s);
		} catch (SerializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<AbstractOperation> result = new ArrayList<AbstractOperation>();
		for (EObject obj : ops1) {
			if (obj instanceof AbstractOperation) {
				AbstractOperation op = (AbstractOperation) obj;
				result.add(op);
			}
		}
		
		return result;
	}
	
	/**
	 * Converts a {@link String} to an {@link EObject}. <b>Note</b>:
	 * {@link String} must be the result of
	 * {@link ModelUtil#eObjectToString(EObject)}
	 * 
	 * @param object
	 *            the {@link String} representation of the {@link EObject}
	 * @return the deserialized {@link EObject}
	 * @throws SerializationException
	 *             if deserialization fails
	 */
	private EList<EObject> stringToEObject(String object)
			throws SerializationException {
		if (object == null) {
			return null;
		}

		XMIResource res = (XMIResource) (new ResourceSetImpl())
				.createResource(VIRTUAL_URI);

		try {
			res.load(new InputSource(new StringReader(object)),
					null);
		} catch (UnsupportedEncodingException e) {
			// TODO;
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		EList<EObject> result = res.getContents();
		

		// TODO: added to resolve model element map in a CreateDeleteOp
		// check whether we can generalize this
		for (EObject o : result) {
			EcoreUtil.resolveAll(o);
		}

		res.getContents().remove(result);
		return result;
	}
}