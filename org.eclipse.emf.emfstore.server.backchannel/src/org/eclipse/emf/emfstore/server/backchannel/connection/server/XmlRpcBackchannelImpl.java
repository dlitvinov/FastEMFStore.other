package org.eclipse.emf.emfstore.server.backchannel.connection.server;

import org.eclipse.emf.emfstore.server.backchannel.BackchannelInterface;
import org.eclipse.emf.emfstore.server.eventmanager.EMFStoreEventListener;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.emf.emfstore.server.model.ProjectId;
import org.eclipse.emf.emfstore.server.model.SessionId;
import org.eclipse.emf.emfstore.server.model.versioning.events.server.ServerEvent;

/**
 * Implementation of xml rpc interface.
 * 
 * @author wesendon
 */
public class XmlRpcBackchannelImpl implements XmlRpcBackchannelInterface {

	private static final long serialVersionUID = 2220637232248810383L;

	/**
	 * @return the backchannel
	 */
	private BackchannelInterface getBackchannel() {
		return XmlRpcBackchannelConnectionHandler.getBackChannel();
	}

	/**
	 * {@inheritDoc}
	 */
	public ServerEvent getEvent(SessionId sessionId, ProjectId projectId)
			throws EmfStoreException {
		AsyncListener listener = new AsyncListener();
		getBackchannel().registerRemoteListener(sessionId, listener, projectId);
		return listener.getEvent(AsyncListener.NOTIMEOUT);
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendEvent(SessionId sessionId, ServerEvent serverEvent,
			ProjectId projectId) throws EmfStoreException {
		getBackchannel().sendEvent(sessionId, serverEvent, projectId);
	}

	// TODO add event buffer in order to handle events while the polling client
	// isn't connected
	private final class AsyncListener implements EMFStoreEventListener {

		private static final int NOTIMEOUT = -1;
		private ServerEvent event;

		public boolean handleEvent(ServerEvent event) {
			this.event = event;
			synchronized (this) {
				this.notifyAll();
			}
			return false;
		}

		public ServerEvent getEvent(long timeout) {
			synchronized (this) {
				try {
					if (timeout == NOTIMEOUT) {
						this.wait();
					} else {
						this.wait(timeout);
					}
					ServerEvent tmp = event;
					event = null;
					return tmp;
				} catch (InterruptedException e) {
					return null;
				}
			}
		}
	}
}
