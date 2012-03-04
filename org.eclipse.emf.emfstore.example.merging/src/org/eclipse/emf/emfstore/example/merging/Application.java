/**
 * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
 */
package org.eclipse.emf.emfstore.example.merging;

import java.util.List;

import org.eclipse.emf.emfstore.bowling.BowlingFactory;
import org.eclipse.emf.emfstore.bowling.League;
import org.eclipse.emf.emfstore.bowling.Player;
import org.eclipse.emf.emfstore.client.model.ProjectSpace;
import org.eclipse.emf.emfstore.client.model.Usersession;
import org.eclipse.emf.emfstore.client.model.Workspace;
import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
import org.eclipse.emf.emfstore.client.model.util.EMFStoreClientUtil;
import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
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

		new EMFStoreCommand() {
			private ProjectSpace project1;
			private League league1;
			private ProjectSpace project2;
			private League league2;
			private Workspace workspace;
			private Usersession usersession;

			@Override
			protected void doRun() {
				try {

					/*
					 * Sets up the workspace by cleaning all contents
					 */
					setupWorkspace();

					/*
					 * Create a project, share it with the server, add a model
					 * element to it and commit the changes.
					 */
					createProject1();

					/*
					 * Checks out the project1 again as project2
					 */
					checkoutProject1AsProject2();

					/*
					 * Now lets create the conflict!
					 */
					System.out.println("Creating a conflict");

					// Change the value of the league in project 1 and commit
					// the change
					league1.setName("Not conflicting change");
					project1.commit(null, null, null);

					try {
						// Changing the same value without calling
						// project2.update() will cause a conflict
						league2.setName("Uuups - conflicting change");
						project2.commit(null, null, null);
					} catch (EmfStoreException e) {
						System.out.println("Conflict occured!");

						handleConflict();
					}

					System.out.println("Client run completed.");
				} catch (AccessControlException e) {
					ModelUtil.logException(e);
				} catch (EmfStoreException e) {
					ModelUtil.logException(e);
				}
			}

			/**
			 * Checks out the project1 as project2 again.
			 * 
			 * @throws EmfStoreException
			 */
			private void checkoutProject1AsProject2() throws EmfStoreException {
				/*
				 * Now lets checkout the same project twice, modify the element
				 * and commit the changes to the server.
				 */
				project2 = workspace.checkout(usersession,
						project1.getProjectInfo());
				league2 = (League) project2.getProject().getModelElements()
						.get(0);
				System.out.println(String.format(
						"Project 2: League \"%s\" was checked out twice!",
						league1.getName()));
				league2.setName("league no. 1 - changed");
				// now lets try to commit
				project2.commit(null, null, null);
			}

			/**
			 * Creates the project1 and adds a league with players to it.
			 * 
			 * @throws EmfStoreException
			 */
			private void createProject1() throws EmfStoreException {
				// create a new local project
				// and share it with the server
				project1 = workspace.createLocalProject("projectNo1",
						"My project");
				project1.shareProject(usersession, null);

				// create a league
				// and add 2 players to it
				league1 = BowlingFactory.eINSTANCE.createLeague();
				league1.setName("league no. 1");
				league1.getPlayers().add(createPlayer("no. 1"));
				league1.getPlayers().add(createPlayer("no. 2"));
				project1.getProject().addModelElement(league1);

				// commit the changes of the project to the EMF
				// Store
				project1.commit(null, null, null);
				System.out
						.println("Project 1: The \"league no. 1\" was sent to the server!");
			}

			/**
			 * Creates a default workspace and deletes all remote projects.
			 * 
			 * @throws AccessControlException
			 * @throws EmfStoreException
			 */
			private void setupWorkspace() throws AccessControlException,
					EmfStoreException {
				workspace = WorkspaceManager.getInstance()
						.getCurrentWorkspace();
				// create a default Usersession for the purpose of this
				// tutorial.
				usersession = EMFStoreClientUtil.createUsersession();
				usersession.logIn();
				List<ProjectInfo> projectList;
				projectList = workspace.getRemoteProjectList(usersession);
				for (ProjectInfo projectInfo : projectList) {
					workspace.deleteRemoteProject(usersession,
							projectInfo.getProjectId(), true);
				}
			}
		}.run(false);
	}

	/**
	 * Handles the conflict that occured.
	 */
	protected void handleConflict() {

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
