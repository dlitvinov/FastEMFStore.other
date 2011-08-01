package org.eclipse.emf.emfstore.server.backchannel.connection.server;

import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.emf.emfstore.server.model.ProjectId;
import org.eclipse.emf.emfstore.server.model.SessionId;
import org.eclipse.emf.emfstore.server.model.versioning.events.server.ServerEvent;

/**
 * xmlrpc interface for the backchannel.
 * 
 * @author wesendon
 */
public interface XmlRpcBackchannelInterface {
	
	ServerEvent getEvent(SessionId sessionId, ProjectId projectId) throws EmfStoreException;
	
	void sendEvent(SessionId sessionId, ServerEvent event, ProjectId projectId) throws EmfStoreException;

}
