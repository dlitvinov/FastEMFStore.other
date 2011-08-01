/**
 * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
 */
package org.eclipse.emf.emfstore.client.backchannel;

import org.eclipse.emf.emfstore.client.model.ServerInfo;
import org.eclipse.emf.emfstore.client.model.connectionmanager.AbstractConnectionManager;
import org.eclipse.emf.emfstore.client.model.connectionmanager.xmlrpc.XmlRpcClientManager;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.server.backchannel.BackchannelInterface;
import org.eclipse.emf.emfstore.server.backchannel.connection.server.XmlRpcBackchannelConnectionHandler;
import org.eclipse.emf.emfstore.server.eventmanager.EMFStoreEventListener;
import org.eclipse.emf.emfstore.server.exceptions.ConnectionException;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.emf.emfstore.server.exceptions.UnknownSessionException;
import org.eclipse.emf.emfstore.server.model.ProjectId;
import org.eclipse.emf.emfstore.server.model.SessionId;
import org.eclipse.emf.emfstore.server.model.versioning.events.server.ServerEvent;

/**
 * Clientside connection manager for the backchannel in order to register as
 * listener on the server.
 * 
 * @author wesendon
 */
public class BackchannelConnectionManager extends
		AbstractConnectionManager<XmlRpcClientManager> implements
		BackchannelInterface {

	/**
	 * {@inheritDoc}
	 */
	public void initConnection(ServerInfo serverInfo, SessionId id)
			throws ConnectionException {
		XmlRpcClientManager clientManager = new XmlRpcClientManager(
				XmlRpcBackchannelConnectionHandler.BACKCHANNEL);
		clientManager.initConnection(serverInfo);
		addConnectionProxy(id, clientManager);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * TODO: More fault tolerant implementation would be better. And it
	 * should be allowed to unregister. SessionTimeOut should be handled.
	 */
	public void registerRemoteListener(final SessionId sessionId,
			final EMFStoreEventListener listener, final ProjectId projectId)
			throws EmfStoreException {

		Runnable runnable = new Runnable() {
			public void run() {
				try {
					while (true) {
						ServerEvent event;
						event = getConnectionProxy(sessionId).callWithResult(
								"getEvent", ServerEvent.class, sessionId,
								projectId);
						if (event != null) {
							listener.handleEvent(event);
						}
					}
				} catch (UnknownSessionException e) {
					ModelUtil.logException(e);
				} catch (EmfStoreException e) {
					ModelUtil.logException(e);
				}
			}
		};
		new Thread(runnable).start();
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendEvent(SessionId sessionId, ServerEvent event,
			ProjectId projectId) throws EmfStoreException {
		getConnectionProxy(sessionId).call("sendEvent", sessionId, event,
				projectId);
	}

}
