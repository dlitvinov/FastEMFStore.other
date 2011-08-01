package org.eclipse.emf.emfstore.server.backchannel.connection.server;

import org.eclipse.emf.emfstore.server.accesscontrol.AuthenticationControl;
import org.eclipse.emf.emfstore.server.backchannel.BackchannelInterface;
import org.eclipse.emf.emfstore.server.connection.ConnectionHandler;
import org.eclipse.emf.emfstore.server.connection.xmlrpc.XmlRpcWebserverManager;
import org.eclipse.emf.emfstore.server.exceptions.FatalEmfStoreException;

/**
 * Connection Handler for XML RPC Emfstore interface.
 * 
 * @author wesendon
 */
public class XmlRpcBackchannelConnectionHandler implements
		ConnectionHandler<BackchannelInterface> {

	/**
	 * String interface identifier.
	 */
	public static final String BACKCHANNEL = "Backchannel";

	private static final String NAME = "XML RPC Connection Handler for the Backchannel";

	private static AuthenticationControl accessControl;

	private static BackchannelInterface backchannelImpl;


	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("static-access")
	public void init(BackchannelInterface backchannel,
			AuthenticationControl accessControl) throws FatalEmfStoreException {
		backchannelImpl = backchannel;
		XmlRpcBackchannelConnectionHandler.accessControl = accessControl;
		XmlRpcWebserverManager webServer = XmlRpcWebserverManager.getInstance();
		webServer.initServer();
		webServer.addHandler(BACKCHANNEL, XmlRpcBackchannelImpl.class);
	}

	/**
	 * Returns Emfstore.
	 * 
	 * @return emfstore
	 */
	public static BackchannelInterface getBackChannel() {
		return backchannelImpl;
	}

	/**
	 * Returns AccessControl.
	 * 
	 * @return access control
	 */
	public static AuthenticationControl getAccessControl() {
		return accessControl;
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop(boolean force) {
		XmlRpcWebserverManager webserverManager = XmlRpcWebserverManager
				.getInstance();
		if (!webserverManager.removeHandler(BACKCHANNEL)) {
		}
	}

}
