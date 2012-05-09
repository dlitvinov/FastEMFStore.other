/**
 * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
 */
package org.eclipse.emf.emfstore.exampleclient;

import java.util.List;

import org.eclipse.emf.emfstore.bowling.BowlingFactory;
import org.eclipse.emf.emfstore.bowling.League;
import org.eclipse.emf.emfstore.bowling.Player;
import org.eclipse.emf.emfstore.client.model.ProjectSpace;
import org.eclipse.emf.emfstore.client.model.Usersession;
import org.eclipse.emf.emfstore.client.model.Workspace;
import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
import org.eclipse.emf.emfstore.client.model.util.EMFStoreClientUtil;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.server.exceptions.AccessControlException;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.emf.emfstore.server.model.ProjectInfo;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class Application implements IApplication {
	public Object start(IApplicationContext context) throws Exception {
		WorkspaceManager.init();

		// run a client that shows the basic features of the emf store
		runClient();

		return IApplication.EXIT_OK;
	}

	private void runClient() throws AccessControlException, EmfStoreException {
		System.out.println("Client starting...");

		try {

			Workspace workspace = WorkspaceManager.getInstance()
					.getCurrentWorkspace();
			// create a default Usersession for the purpose of this
			// tutorial.
			Usersession usersession = EMFStoreClientUtil.createUsersession();
			usersession.logIn();
			List<ProjectInfo> projectList;
			projectList = workspace.getRemoteProjectList(usersession);
			for (ProjectInfo projectInfo : projectList) {
				workspace.deleteRemoteProject(usersession,
						projectInfo.getProjectId(), true);
			}

			/*
			 * Create a project, share it with the server, add a model element
			 * to it and commit the changes.
			 */

			// create a new local project
			// and share it with the server
			ProjectSpace project1 = workspace.createLocalProject("projectNo1",
					"My project");
			project1.shareProject(usersession, null);

			// create a league
			// and add 2 players to it
			League league1 = BowlingFactory.eINSTANCE.createLeague();
			league1.setName("league no. 1");
			league1.getPlayers().add(createPlayer("no. 1"));
			league1.getPlayers().add(createPlayer("no. 2"));
			project1.getProject().addModelElement(league1);

			// commit the changes of the project to the EMF
			// Store
			project1.commit(null, null, null);
			System.out
					.println("Project 1: The \"league no. 1\" was sent to the server!");

			/*
			 * Now lets checkout the same project twice, modify the element and
			 * commit the changes to the server.
			 */
			ProjectSpace project2 = workspace.checkout(usersession,
					project1.getProjectInfo());
			League league2 = (League) project2.getProject().getModelElements()
					.get(0);
			System.out.println(String.format(
					"Project 2: League \"%s\" was checked out twice!",
					league1.getName()));
			league2.setName("league no. 1 - changed");
			// now lets try to commit
			project2.commit(null, null, null);

			/*
			 * Lets check the value of the league contained in project 1. Then
			 * we update the values from the server and check the contents
			 * again.
			 */
			System.out.println(String.format(
					"Project 1: Name of league is \"%s\"", league1.getName()));

			// update from server
			project1.update();

			System.out
					.println(String
							.format("Project 1: Name of league is \"%s\" after update from server",
									league1.getName()));

			/*
			 * Now lets create a conflict!
			 */
			System.out.println("Creating a conflict");
			league1.setName("Not conflicting change");
			project1.commit(null, null, null);

			try {
				league2.setName("Uuups - conflicting change");
				project2.commit(null, null, null);
			} catch (EmfStoreException e) {
				System.out
						.println("That was a conflict since league1#name was changed and commited earlier!");

				/*
				 * Now lets revert the changes, update the project and change it
				 * afterwards
				 */
				project2.revert();
				project2.update();
				league2.setName("Not conflicting anymore");
				project2.commit(null, null, null);
			}

			System.out.println("Client run completed.");
		} catch (AccessControlException e) {
			ModelUtil.logException(e);
		} catch (EmfStoreException e) {
			ModelUtil.logException(e);
		}
	}

	/**
	 * Creates a new instance of a player.
	 * 
	 * @param name
	 * @return
	 */
	private Player createPlayer(String name) {
		Player player = BowlingFactory.eINSTANCE.createPlayer();
		player.setName(String.format("Player %s", name));
		player.setEMail(String.format("%s@emfstore.org", name));
		return player;
	}

	public void stop() {
		// TODO Auto-generated method stub
	}
}
