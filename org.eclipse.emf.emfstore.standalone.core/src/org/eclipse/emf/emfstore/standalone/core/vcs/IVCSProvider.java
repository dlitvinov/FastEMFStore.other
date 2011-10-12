package org.eclipse.emf.emfstore.standalone.core.vcs;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;

/**
 * Common interface for VCS connector implementations.
 * @author emueller 
 *
 */
public interface IVCSProvider {

	IVCSProvider NONE = null;

	/**
	 * Adds an {@link IFile} to be versioned by the VCS.
	 * @param file the file to be added to the VCS
	 */
	void addToVCS(IFile file);
	
	/**
	 * Determines whether a file has been modified since the last base revision
	 * @param file the file that needs to be checked
	 * @return true, if the history file has been modified, false otherwise
	 * @throws IOException if it could not be determined whether the history file 
	 * 		has been modified
	 */
	boolean isModified(IFile file) throws IOException;
	
	/**
	 * Returns the local version of the model resource file.
	 * @param resource the model resource
	 * @param monitor a {@link IProgressMonitor} instance
	 * @return the content of the model resource file
	 * @throws IOException in case the model resource file can not be read
	 * @throws InterruptedException if the {@link IProgressMonitor} has been canceled
	 * @throws RepositoryNotFoundException if the repository the given resource is 
	 * 		considered to be in, can not be found
	 */
//	String getMyModelResourceFile(IResource resource, IProgressMonitor monitor) 
//	throws IOException, InterruptedException, RepositoryNotFoundException;
	
	/**
	 * Resolves a conflict state on the given file.
	 * @param file the file being in a conflict state
	 */
	void resolveConflict(IFile file);
	
	/**
	 * Returns the {@link IRepository} the given resource is managed with.
	 * @param resource the resource, whose repository should be determined
	 * @return the repository the given resource is in
	 * @throws RepositoryNotFoundException if the repository can not be 
	 * 	determined
	 */
//	IRepository<R> getRepository(IResource resource)
//	throws RepositoryNotFoundException;

	/**
	 * Returns the local {@link IRevision} of the given resource.
	 * @param resource the resource 
	 * @param monitor a {@link IProgressMonitor} instance
	 * @return the content of the given resource as it is locally available  
	 * @throws IOException if the revision can not be determined
	 * @throws InterruptedException if the {@link IProgressMonitor} has been canceled
	 */
	String getMyRevision(IResource resource, IProgressMonitor monitor)
	throws IOException, InterruptedException;
	
	/**
	 * Returns the head {@link IRevision} of the given resource.
	 * @param resource the resource 
	 * @param monitor a {@link IProgressMonitor} instance
	 * @return the content of the given resource as it is locally available  
	 * @throws IOException if the revision can not be determined
	 * @throws InterruptedException if the {@link IProgressMonitor} has been canceled
	 */
	String getTheirRevision(IResource resource, IProgressMonitor monitor)
	throws IOException, InterruptedException;
	
	/**
	 * Returns the ancestor {@link IRevision} of the given resource.
	 * @param myRevision the local revision
	 * @param theirRevision the incoming revision
	 * @param the local repository 
	 * @param monitor a {@link IProgressMonitor} instance
	 * @return the ancestor revision of <code>myRevision</code> and <code>theirRevision</code> 
	 * @throws IOException if the revision can not be determined
	 * @throws InterruptedException if the {@link IProgressMonitor} has been canceled
	 */
//	IRevision<T> getAncestorRevision(IRevision<T> myRevision, IRevision<T> theirRevision,
//			IRepository<R> repository, IProgressMonitor monitor) 
//	throws IOException, InterruptedException;

	/**
	 * Returns the ancestor {@link IRevision} of the given resource.
	 * @param myRevision the local revision
	 * @param theirRevision the incoming revision
	 * @param the local repository 
	 * @param monitor a {@link IProgressMonitor} instance
	 * @return the ancestor revision of <code>myRevision</code> and <code>theirRevision</code> 
	 * @throws IOException if the revision can not be determined
	 * @throws InterruptedException if the {@link IProgressMonitor} has been canceled
	 */
//	IFileRevision getFileRevision(IRepository<R> repository,
//			IRevision<T> revision, String objectId);
	
	/**
	 * Returns the incoming operations.
	 * @param resource the resource, which contains the operations
	 * @param monitor a {@link IProgressMonitor} instance
	 * @return the incoming operations
	 */
	List<AbstractOperation> getTheirOperations(IResource resource, IProgressMonitor monitor);
	
	/**
	 * Returns the local operations.
	 * @param resource the resource, which contains the operations
	 * @param monitor a {@link IProgressMonitor} instance
	 * @return the local operations
	 */
	List<AbstractOperation> getMyOperations(IResource resource, IProgressMonitor monitor);
	
	/**
	 * Returns the content of given {@link IResource} as it has been in the given {@link IRevision}.
	 * @param resource a resource
	 * @param revision a revision
	 * @param monitor an {@link IProgressMonitor} instance
	 * @return the content of the given resource
	 */
//	String getFileContent(IResource resource, IRevision<T> revision, IProgressMonitor monitor);
}
