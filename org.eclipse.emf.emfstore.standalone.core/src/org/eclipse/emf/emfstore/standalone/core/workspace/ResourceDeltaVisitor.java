package org.eclipse.emf.emfstore.standalone.core.workspace;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;

public class ResourceDeltaVisitor implements IResourceDeltaVisitor {
	
    private Set<IResource> addedResources;
    
    public ResourceDeltaVisitor() {
    	addedResources = new HashSet<IResource>();
    }

	public boolean visit(IResourceDelta delta) {
    	
        switch (delta.getKind()) {
        case IResourceDelta.ADDED :
        	getAddedResources().add(delta.getResource());
        	break;
        case IResourceDelta.REMOVED :
            // handle removed resource
            break;
        case IResourceDelta.CHANGED :
            // handle changed resource
            break;
        }
        
        return true;
    }

	public Set<IResource> getAddedResources() {
		return addedResources;
	}

}
