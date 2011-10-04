package org.eclipse.emf.emfstore.standalone.ui;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.emfstore.client.ui.dialogs.merge.DecisionManager;
import org.eclipse.emf.emfstore.client.ui.dialogs.merge.MergeWizard;
import org.eclipse.emf.emfstore.common.model.ModelElementId;
import org.eclipse.emf.emfstore.common.model.Project;
import org.eclipse.emf.emfstore.common.model.impl.ProjectImpl;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.common.model.util.SerializationException;
import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.eclipse.emf.emfstore.standalone.core.artifacts.Artifact;
import org.eclipse.emf.emfstore.standalone.core.artifacts.ArtifactRegistry;
import org.eclipse.emf.emfstore.standalone.core.util.FileUtil;
import org.eclipse.emf.emfstore.standalone.core.vcs.IVCSProvider;
import org.eclipse.emf.emfstore.standalone.core.workspace.ResourceFactoryRegistry;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.xml.sax.InputSource;

public class ArtifactMerger {

	private IResource resource;
//	private Project project;
//	private XMIResource historyResource;
//	private final XMIResource xmiResource;
	private IVCSProvider provider;
	private Artifact artifact;
	
	public ArtifactMerger(IVCSProvider provider, IResource resource,
			Artifact artifact) {
		this.resource = resource;
		this.artifact = artifact;
//		this.xmiResource = artifact.getModelResource();
//		this.historyResource = artifact.getHistoryResource();
//		this.project = artifact.getCollection();
		this.provider = provider;
	}
	
	public void merge() {
		artifact.getProject().getChangeNotifier().disableNotifications(true);
				
		try {
			// restore project state 
			String artifactFileContent = provider.getMyRevision(
					FileUtil.getFile(artifact.getModelResource().getURI()),	new NullProgressMonitor());
			artifact.setProject(stringToEObjectIntoProject(artifactFileContent));
			writeToXMIResource(artifact.getProject().getModelElements(), artifact.getModelResource());
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SerializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List theirOperations = provider.getTheirOperations(resource, new NullProgressMonitor());
		List myOperations = provider.getMyOperations(resource, new NullProgressMonitor());
		List<AbstractOperation> mergeResult = callMergeDialog(myOperations, theirOperations);
		// if mergeResult is empty, file have been auto-merged
		try {
			Artifact artifact = ArtifactRegistry.getInstance().getArtifact(ResourceFactoryRegistry.CURRENT_URI);
			applyMergeResult(myOperations, theirOperations, mergeResult);
			// resolve conflict on ecore
			IFile artifactFile = FileUtil.getFile(artifact.getModelResource().getURI());
			provider.resolveConflict(artifactFile);
			artifact.getHistoryResource().getContents().clear();
			artifact.getHistoryResource().getContents().addAll(mergeResult);
			artifact.getHistoryResource().save(null);
			provider.resolveConflict(FileUtil.getHistoryFile(artifactFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		artifact.getProject().getChangeNotifier().disableNotifications(false);
	}

	/**
	 * Applies a list of operations to the project. The change tracking is
	 * stopped and the operations are added to the projectspace.
	 * 
	 * @see #applyOperationsWithRecording(List, boolean)
	 * @param operations
	 *            list of operations
	 * @param addOperation
	 *            true if operation should be saved in project space.
	 */
	public void applyOperations(List<AbstractOperation> operations) {
		// TODO: API
//		((ProjectImpl) artifact.getProject()).getChangeNotifier().disableNotifications(true);
//		try {
			for (AbstractOperation operation : operations) {
				operation.apply(artifact.getProject());
			}
//		} finally {
//			((ProjectImpl) project).getChangeNotifier().disableNotifications(false);
//		}
	}
	
	private ChangePackage createChangePackage(List<AbstractOperation> operations) {
		ChangePackage changePackage = VersioningFactory.eINSTANCE.createChangePackage();

		for (EObject o : operations) {
			AbstractOperation copy = (AbstractOperation) EcoreUtil.copy(o);
			changePackage.getOperations().add(copy);
		}
		
		return changePackage;
	}

	private List<AbstractOperation> callMergeDialog(List<AbstractOperation> myOperations, 
			List<AbstractOperation> theirOperations) {
		
		ChangePackage myChangePackage = createChangePackage(myOperations);
		ChangePackage theirChangePackage = createChangePackage(theirOperations);
		
		PrimaryVersionSpec dummy = VersioningFactory.eINSTANCE.createPrimaryVersionSpec();
		
		DecisionManager mgr = new DecisionManager(artifact.getProject(), 
				myChangePackage, Collections.singletonList(theirChangePackage), 
				dummy, dummy);
		mgr.calcResult();
		List<AbstractOperation> mergeResult = new ArrayList<AbstractOperation>();
	
		if (mgr.getConflicts().size() > 0 ) {
			MergeWizard mergeWizard = new MergeWizard(mgr);
			WizardDialog mergeDialog = new WizardDialog(Display.getCurrent().getActiveShell(), mergeWizard);
			mergeDialog.create();
			// TODO: handle cancel case
			mergeDialog.open();

			// generate merge result and apply to local workspace
			List<AbstractOperation> acceptedMine = mgr.getAcceptedMine();
			List<AbstractOperation> rejectedTheirs = mgr.getRejectedTheirs();
		
			for (AbstractOperation operationToReverse : rejectedTheirs) {
				mergeResult.add(0, operationToReverse.reverse());
			}
			
			mergeResult.addAll(acceptedMine);
		} 
		
		return mergeResult;
	}
	
	/**
	 * Applies the given merge result upon the given {@link Artifact}. 
	 * @param artifact the {@link Artifact} upon which to apply the merge result
	 * @param myOperations the local operations
	 * @param theirOperations the incoming operations
	 * @param mergeResult the merge result as determined by the user
	 * @throws IOException
	 */
	private void applyMergeResult(List<AbstractOperation> myOperations, 
			List<AbstractOperation> theirOperations, List<AbstractOperation> mergeResult) 
		throws IOException {
		
		ChangePackage myChangePackage = createChangePackage(myOperations);
		ChangePackage theirChangePackage = createChangePackage(theirOperations);
		
		// revert our changes
		revert(myChangePackage.getOperations());

		applyOperations(theirChangePackage.getOperations());
		applyOperations(mergeResult);

		// write merge result back to model resource
//		List<EObject> modelElements = ModelUtil.clone(project.getModelElements());

		try {
			writeToXMIResource(mergeResult, artifact.getHistoryResource());
		} catch (IOException e) {
			throw new IOException(String.format("Can not write merge result {0} to history resource {1}. Reason: {2}", 
					mergeResult, artifact.getHistoryResource(), e.getMessage())); 
		}
		
		try {
			writeToXMIResource(artifact.getProject().getModelElements(), artifact.getModelResource());
		} catch (IOException e) {
			throw new IOException(String.format("Can not write model elements {0} to resource {1}. Reason: {2}", 
					artifact.getProject().getModelElements(), artifact.getModelResource(), e.getMessage())); 
		}
	}
	
	/**
	 * Reverts the 
	 * @param operations
	 */
	public void revert(List<AbstractOperation> operations) {
		while (!operations.isEmpty()) {
			AbstractOperation lastOperation = operations
					.get(operations.size() - 1);
			((ProjectImpl) artifact.getProject()).getChangeNotifier().disableNotifications(true);
			try {
				lastOperation.reverse().apply(artifact.getProject());
			} finally {
				((ProjectImpl) artifact.getProject()).getChangeNotifier().disableNotifications(false);
			}
			operations.remove(lastOperation);
		}
	}
		
	/**
	 * Write 
	 * @param modelElements
	 * @param resource
	 * @throws IOException
	 */
	private void writeToXMIResource(List<? extends EObject> modelElements, XMIResource resource) throws IOException {
//		resource.getContents().s

		// TODO: will be slow
//		int index = 0;
		for (EObject me : modelElements) {

			List<EObject> allContainedModelElements = ModelUtil.getAllContainedModelElementsAsList(me, false);
			allContainedModelElements.add(me);

			EObject copiedElement = EcoreUtil.copy(me);
			List<EObject> copiedAllContainedModelElements = ModelUtil.getAllContainedModelElementsAsList(copiedElement, false);
			copiedAllContainedModelElements.add(copiedElement);
			
			resource.getContents().add(copiedElement);

			for (int i = 0; i < allContainedModelElements.size(); i++) {
				EObject child = allContainedModelElements.get(i);
				EObject copiedChild = copiedAllContainedModelElements.get(i);
				ModelElementId modelElementId = artifact.getProject().getModelElementId(child);
				if (modelElementId == null) {
					modelElementId = artifact.getProject().getDeletedModelElementId(child);
				}				
				if (modelElementId != null) {
					resource.setID(copiedChild, modelElementId.getId());
				}
			}
		}
		
		for (int i = 0; i < modelElements.size(); i++) {
			resource.getContents().remove(0);
		}

		resource.save(null);
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
	public static Project stringToEObjectIntoProject(String object)
			throws SerializationException {
		if (object == null) {
			return null;
		}

		XMIResource res = (XMIResource) (new ResourceSetImpl())
				.createResource(ModelUtil.VIRTUAL_URI);

		try {
			res.load(new InputSource(new StringReader(object)),
					null);
		} catch (UnsupportedEncodingException e) {
			throw new SerializationException(e);
		} catch (IOException e) {
			throw new SerializationException(e);
		}

		EObject result = res.getContents().get(0);

		if (result instanceof Project) {
			return (Project) result;
		}

		// TODO: init
		Project project = null;
		try {
			project = new ProjectImpl(res);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// Do NOT catch all Exceptions ("catch (Exception e)")
			// Log AND handle Exceptions if possible
			//
			// You can just uncomment one of the lines below to log an
			// exception:
			// logException will show the logged excpetion to the user
			// ModelUtil.logException(e);
			// ModelUtil.logException("YOUR MESSAGE HERE", e);
			// logWarning will only add the message to the error log
			// ModelUtil.logWarning("YOUR MESSAGE HERE", e);
			// ModelUtil.logWarning("YOUR MESSAGE HERE");
			//
			// If handling is not possible declare and rethrow Exception
			e.printStackTrace();
		}

		return project;
	}
}
