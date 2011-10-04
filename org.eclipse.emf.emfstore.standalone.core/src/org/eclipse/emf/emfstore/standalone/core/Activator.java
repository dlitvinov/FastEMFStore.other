/*******************************************************************************
 * Copyright (c) 2008-2011 Chair for Applied Software Engineering, Technische Universitaet Muenchen. All rights
 * reserved. This program and the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 ******************************************************************************/
package org.eclipse.emf.emfstore.standalone.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author emueller
 */
public class Activator extends Plugin {

	/**
	 * The plug-in ID.
	 */
	public static final String PLUGIN_ID = "org.eclipse.emf.emfstore.teamprovider.recording";

	/**
	 * The shared instance.
	 */
	private static Activator plugin;

	// TODO: enable multiple providesr
//	public static Map<String ,IVCSProvider> vcsProviders;

	/**
	 * The constructor.
	 */
	public Activator() {
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	// BEGIN SUPRESS CATCH EXCEPTION
	@Override
	public void start(BundleContext context) throws Exception {
		// END SUPRESS CATCH EXCEPTION
		super.start(context);
		plugin = this;

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	// BEGIN SUPRESS CATCH EXCEPTION
	@Override
	public void stop(BundleContext context) throws Exception {
		// END SUPRESS CATCH EXCEPTION
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
}
