package org.eclipse.emf.emfstore.standalone.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;

public class FileUtil {
	
	private static final String HISTORY_EXTENSION = "hist";
	private static Set<String> managedFileExtensions;

	private static void addFileExtensions() {
		managedFileExtensions.add("ecore");
		managedFileExtensions.add("umldi-history");
		managedFileExtensions.add("umldi");
		managedFileExtensions.add("uml");
		// TODO: provide extensions points to read them here
	}

	/**
	 * Determines whether a {@link IFile} has an extension that may be versioned.
	 * @param file a {@link IFile}
	 * @return true, if the file may by the {@link RecorderHeap}, false otherwise
	 */
	public static boolean hasManagedFileExtension(IFile file) {
		initFileExtensionsStore();
		String fileExtension = file.getFileExtension();
		return managedFileExtensions.contains(fileExtension);
	}

	private static void initFileExtensionsStore() {
		if (managedFileExtensions == null) {
			managedFileExtensions = new HashSet<String>();
			addFileExtensions();
		}
	}
	
	/**
	 * Returns a set of extensions that are currently managed.
	 * @return a list containing all file extensions that may be versioned
	 */
	public static Set<String> getManagedFileExtensions() {
		initFileExtensionsStore();
		return managedFileExtensions;
	}

	/**
	 * Convert a given {@link IFile} to its {@link URI} representation
	 * @param file a file
	 * @return the {@link URI} representation of the file
	 */
	public static URI getUri(IFile file) {
		return URI.createPlatformResourceURI(file.getFullPath().toString(), true);
	}
	
	/**
	 * Convert a {@link URI} to its {@link IFile} representation
	 * @param uri a {@link URI}
	 * @return the {@link URI} representation of the file
	 */
	public static IFile getFile(URI uri) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		return root.getFile(new Path(uri.toPlatformString(true)));
	}
	
	public static URI getHistoryURI(URI artifactURI) {
		return artifactURI.appendFileExtension(HISTORY_EXTENSION);
	}
		
	public static IFile getHistoryFile(IFile artifactFile) {
		String artifactFileName = artifactFile.getName() + "." +  HISTORY_EXTENSION;
		IResource resource = artifactFile.getProject().findMember(artifactFileName);
		
		if (!(resource instanceof IFile) || !resource.exists()) {
			throw new IllegalStateException("Artifact file either does not exist or is not a file");
		}
		
		return (IFile) resource;
	}
	
	/**
	 * Returns the artifact based on the history file. 
	 * 
	 * @param file the history file
	 * @return the artifact file
	 * @throws IllegalStateException if the given history file exists, but the artifact file does not
	 * 		or if the artifact file is not an instance of {@link IFile}
	 */
	public static IFile getArtifactFile(IFile historyFile) {
		String artifactFileName = historyFile.getName().replace("." + HISTORY_EXTENSION, "");
		IResource resource = historyFile.getProject().findMember(artifactFileName);
		return (IFile) resource;
	}
	
	// TODO: copied method
	public static String slurpAndClose(InputStream inputStream) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			int ch;
			while ((ch = inputStream.read()) != -1) {
				stringBuilder.append((char) ch);
			}
		} finally {
			inputStream.close();
		}
		return stringBuilder.toString();
	}
	
	// TODO: copied method
	public static List<AbstractOperation> slurpAndCloseHistoryResource(XMIResource xmiResource) throws IOException {
		xmiResource.load(null);
		List<AbstractOperation> result = new ArrayList<AbstractOperation>();
		EList<EObject> contents = xmiResource.getContents();
		for (EObject obj : contents) {
			result.add((AbstractOperation) obj);
		}
		
		return result;
	}
	
	/**
	 * Checks whether a given file is a history file.
	 * 
	 * @param file
	 * @throws IllegalArgumentException if the given file is null
	 */
	public static boolean isHistoryFile(IFile file) {
		
		if (file == null) {
			throw new IllegalArgumentException("file must not be null.");
		}
		
		return file.getFileExtension().equals(HISTORY_EXTENSION);
	}
}
