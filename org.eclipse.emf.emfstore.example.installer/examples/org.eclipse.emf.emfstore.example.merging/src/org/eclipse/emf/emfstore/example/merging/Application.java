/**
 * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
 */
package org.eclipse.emf.emfstore.example.merging;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.emfstore.bowling.BowlingFactory;
import org.eclipse.emf.emfstore.bowling.BowlingPackage;
import org.eclipse.emf.emfstore.bowling.League;
import org.eclipse.emf.emfstore.bowling.Player;
import org.eclipse.emf.emfstore.client.model.ProjectSpace;
import org.eclipse.emf.emfstore.client.model.Usersession;
import org.eclipse.emf.emfstore.client.model.Workspace;
import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
import org.eclipse.emf.emfstore.client.model.observers.ConflictResolver;
import org.eclipse.emf.emfstore.client.model.util.EMFStoreClientUtil;
import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
import org.eclipse.emf.emfstore.common.model.ModelElementId;
import org.eclipse.emf.emfstore.common.model.Project;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.server.exceptions.AccessControlException;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.emf.emfstore.server.model.ProjectInfo;
import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class Application implements IApplication {
	private ProjectSpace project1;
	private League league1;
	private ProjectSpace project2;
	private League league2;
	private Workspace workspace;
	private Usersession usersession;

	public Object start(IApplicationContext context) throws Exception {
		WorkspaceManager.init();

		// run a client that shows the basic features of the emf store
		runClient();

		return IApplication.EXIT_OK;
	}

	private void runClient() throws AccessControlException, EmfStoreException {
		System.out.println("Client starting...");

		new EMFStoreCommand() {

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
					league1.getPlayers().add(createPlayer("Player no. 4"));
					project1.commit(null, null, null);

					try {
						// Changing the same value without calling
						// project2.update() will cause a conflict
						league2.setName("Uuups - conflicting change");
						league2.getPlayers().remove(0);
						project2.commit(null, null, null);
					} catch (EmfStoreException e) {
						System.out.println("Conflict occured!");

						// handle the conflict of project2 and commit it
						handleConflict(project2);
						project2.commit(null, null, null);

						// now update the project1 and test, whether the
						// conflict was resolved correctly
						project1.update();
						System.out
								.println(String
										.format("league(project1).name is %s. league(project2).name is %s",
												league1.getName(),
												league2.getName()));
						System.out
								.println(String
										.format("players(project1).count is %s. players(project2).count is %s",
												Integer.toString(league1
														.getPlayers().size()),
												Integer.toString(league2
														.getPlayers().size())));

					}

					System.out.println("Client run completed.");
				} catch (AccessControlException e) {
					ModelUtil.logException(e);
				} catch (EmfStoreException e) {
					ModelUtil.logException(e);
				}
			}
		}.run(false);
	}

	/**
	 * Checks out the project1 as project2 again.
	 * 
	 * @throws EmfStoreException
	 */
	private void checkoutProject1AsProject2() throws EmfStoreException {
		/*
		 * Now lets checkout the same project twice, modify the element and
		 * commit the changes to the server.
		 */
		project2 = workspace.checkout(usersession, project1.getProjectInfo());
		league2 = (League) project2.getProject().getModelElements().get(0);
		System.out.println(String.format(
				"Project 2: League \"%s\" was checked out twice!",
				league1.getName()));
	}

	/**
	 * Creates the project1 and adds a league with players to it.
	 * 
	 * @throws EmfStoreException
	 */
	private void createProject1() throws EmfStoreException {
		// create a new local project
		// and share it with the server
		project1 = workspace.createLocalProject("projectNo1", "My project");
		project1.shareProject(usersession, null);

		// create a league
		// and add 2 players to it
		league1 = BowlingFactory.eINSTANCE.createLeague();
		league1.setName("league no. 1");
		league1.getPlayers().add(createPlayer("no. 1"));
		league1.getPlayers().add(createPlayer("no. 2"));
		league1.getPlayers().add(createPlayer("no. 3"));
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
		workspace = WorkspaceManager.getInstance().getCurrentWorkspace();
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

	/**
	 * Handles the conflicting project2.
	 * 
	 * @param conflictingProject2
	 */
	protected void handleConflict(ProjectSpace conflictingProject2) {
		try {
			// inspect all changes
			inspectChanges(conflictingProject2);

			// merge the project2 with the current version
			// and reject all changes from the server!
			// Only changes from project2 are accepted!
			conflictingProject2.merge(project1.getBaseVersion(),
					new ConflictResolver() {

						private ArrayList<AbstractOperation> acceptedMine;
						private ArrayList<AbstractOperation> rejectedTheirs;

						public boolean resolveConflicts(Project project,
								List<ChangePackage> theirChangePackages,
								ChangePackage myChangePackage,
								PrimaryVersionSpec baseVersion,
								PrimaryVersionSpec targetVersion) {

							// all local projects for project2 are accepted
							acceptedMine = new ArrayList<AbstractOperation>();
							acceptedMine.addAll(myChangePackage.getOperations());

							// reject all operations executed on project1
							rejectedTheirs = new ArrayList<AbstractOperation>();
							for (ChangePackage change : theirChangePackages) {
								rejectedTheirs.addAll(change.getOperations());
							}
							return true;
						}

						public List<AbstractOperation> getRejectedTheirs() {
							return rejectedTheirs;
						}

						public List<AbstractOperation> getAcceptedMine() {
							return acceptedMine;
						}
					});

		} catch (EmfStoreException e) {
			ModelUtil.logException(e);
		}
	}

	/**
	 * Inspects the changes occured.
	 * 
	 * @param conflictingProject
	 * @throws EmfStoreException
	 */
	private void inspectChanges(ProjectSpace conflictingProject)
			throws EmfStoreException {
		// access and list all changes occured
		List<ChangePackage> changes = conflictingProject.getChanges(
				conflictingProject.getBaseVersion(), project1.getBaseVersion());

		for (ChangePackage change : changes) {
			System.out.println(change.getLogMessage().toString());

			// use the elementId of the change to access the leagues of each
			// local project
			for (ModelElementId elementId : change
					.getAllInvolvedModelElements()) {
				EObject element = project1.getProject().getModelElement(
						elementId);
				if (element == null) {
					element = project2.getProject().getModelElement(elementId);
				}
				switch (element.eClass().getClassifierID()) {
				case BowlingPackage.PLAYER:
					Player playerOfProject1 = (Player) project1.getProject()
							.getModelElement(elementId);
					Player playerOfProject2 = (Player) project2.getProject()
							.getModelElement(elementId);
					if (playerOfProject1 != null) {
						System.out.println(String.format(
								"Player of project1. Name is %s",
								playerOfProject1.getName()));
					}
					if (playerOfProject2 != null) {
						System.out.println(String.format(
								"Player of project2. Name is %s",
								playerOfProject2.getName()));
					}
					break;
				case BowlingPackage.LEAGUE:
					League leagueOfProject1 = (League) project1.getProject()
							.getModelElement(elementId);
					League leagueOfProject2 = (League) project2.getProject()
							.getModelElement(elementId);
					System.out.println(String.format(
							"League of project1. Name is %s",
							leagueOfProject1.getName()));
					System.out.println(String.format(
							"League of project2. Name is %s",
							leagueOfProject2.getName()));
					break;
				}
			}
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
	}
}
