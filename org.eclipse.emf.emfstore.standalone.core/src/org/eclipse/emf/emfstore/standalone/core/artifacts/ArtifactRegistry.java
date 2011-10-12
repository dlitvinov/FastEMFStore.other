package org.eclipse.emf.emfstore.standalone.core.artifacts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.emfstore.standalone.core.util.FileUtil;

/**
 * Keeps track of which artifacts are considered to be under recorder control.
 * @author emueller
 *
 */
public class ArtifactRegistry {
	
	private Map<URI, Artifact> recordedArtifacts;
	private Set<URI> flags;
	
	private static ArtifactRegistry registry;
	
	public static ArtifactRegistry getInstance() {
		if (registry == null) {
			registry = new ArtifactRegistry();
		}
		return registry;
	}

	private ArtifactRegistry() {
		recordedArtifacts = new HashMap<URI, Artifact>();
		flags = new HashSet<URI>();
	}

	/**
	 * Adds a {@link Artifact} to the registry.
	 * @param artifact the artifact to be added 
	 * @param isLoading whether the artifact is loading or whether it is 
	 * 		considered to be a new one not yet put under recorder control
	 */
	public void register(Artifact artifact) {
		recordedArtifacts.put(artifact.getURI(), artifact);
//		artifact.initialize();
		// TODO: write versioned artifact to preference store
		//		 e.g. by means of using the filename/URI
	}
	
	public Artifact getArtifact(URI uri) {
		return recordedArtifacts.get(uri);
	}
	
	
	/**
	 * Removes the artifact 
	 * @param uri 
	 * @throws CoreException if the history file can not be deleted
	 */
	public void unregister(URI uri) throws CoreException {
		Artifact versionedArtifact = recordedArtifacts.get(uri);
		versionedArtifact.dispose();
		recordedArtifacts.remove(uri);
		IFile historyFile = FileUtil.getHistoryFile(FileUtil.getFile(uri));
		historyFile.delete(true, new NullProgressMonitor());
	}
	
	// TODO: remove?
	public boolean isRegistered(URI uri) {
		return recordedArtifacts.containsKey(uri);
	}
	
	public boolean isRegistered(IFile artifactFile) {
		return recordedArtifacts.containsKey(FileUtil.getUri(artifactFile));
	}
	
	public void flag(URI uri) {
		flags.add(uri);
	}
	
	public void flag(IFile artifactFile) {
		flags.add(FileUtil.getUri(artifactFile));
	}
	
//	public void markClearHistory(URI uri) {
//		clearHistoryFiles.put(uri, true);
//	}
//	
//	public void removeClearHistory(URI uri) {
//		clearHistoryFiles.remove(uri);
//	}
//	
//	public boolean isMarkedForClearHistory(URI uri) {
//		return clearHistoryFiles.containsKey(uri);
//	}
	
	public boolean isFlagged(URI uri) {
		return flags.contains(uri);
	}
	
	public boolean isFlagged(IFile artifactFile) {
		return flags.contains(FileUtil.getUri(artifactFile));
	}
	
	public void unflag(URI uri) {
		flags.remove(uri);
	}
}
