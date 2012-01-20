/*******************************************************************************
 * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
 * Technische Universitaet Muenchen.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 ******************************************************************************/
package org.eclipse.emf.emfstore.migration.edapt;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.edapt.migration.MigrationException;
import org.eclipse.emf.edapt.migration.ReleaseUtils;
import org.eclipse.emf.edapt.migration.execution.Migrator;
import org.eclipse.emf.edapt.migration.execution.MigratorRegistry;
import org.eclipse.emf.emfstore.migration.EMFStoreMigrationException;
import org.eclipse.emf.emfstore.migration.EMFStoreMigrator;

/**
 * EMFStoreMigrator implementation based on COPE.
 * 
 * @author koegel
 * 
 */
public class EMFMigrator implements EMFStoreMigrator {

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.migration.EMFStoreMigrator#migrate(java.util.List, int)
	 */
	public void migrate(List<URI> resources, int sourceModelReleaseNumber, IProgressMonitor monitor)
		throws EMFStoreMigrationException {
		if (resources.size() < 1) {
			return;
		}
		String namespaceURI = ReleaseUtils.getNamespaceURI(resources.get(0));
		Migrator migrator = MigratorRegistry.getInstance().getMigrator(namespaceURI);
		if (migrator == null) {
			throw new EMFStoreMigrationException("Cannot migrate given URIs, no COPE migrations registered.");
		}

		// MK: build in progress monitor for migration here
		try {
			migrator.migrateAndSave(resources, migrator.getRelease(sourceModelReleaseNumber), null, monitor);
		} catch (MigrationException e) {
			throw new EMFStoreMigrationException("Cope Migration failed!", e);
		}
	}
}
