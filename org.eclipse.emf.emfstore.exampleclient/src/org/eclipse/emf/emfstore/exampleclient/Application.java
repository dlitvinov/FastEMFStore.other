package org.eclipse.emf.emfstore.exampleclient;

import java.util.List;

// import the example model
// change these to work with your own model
import library.Book;
import library.Library;
import library.LibraryFactory;

import org.eclipse.emf.emfstore.client.model.ProjectSpace;
import org.eclipse.emf.emfstore.client.model.Usersession;
import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
import org.eclipse.emf.emfstore.client.model.util.EMFStoreClientUtil;
import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
import org.eclipse.emf.emfstore.common.model.Project;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.server.exceptions.AccessControlException;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.emf.emfstore.server.model.ProjectInfo;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class Application implements IApplication {
	public Object start(IApplicationContext context) throws Exception {
		WorkspaceManager.init();
		
		// run a client that commits to the first project it can find on the server
		runClient();

		return IApplication.EXIT_OK;
	}

	private void runClient() throws AccessControlException, EmfStoreException {
		System.out.println("Client starting...");

		new EMFStoreCommand() {
			@Override
			protected void doRun() {
				try {
					// create a default Usersession for the purpose of this tutorial, login and fetch the list of projects
					// see the corrsponding Javadoc for EMFStoreClientUtil.createUsersession(...) to setup the authentication for your custom client
					Usersession usersession = EMFStoreClientUtil
							.createUsersession();
					usersession.logIn();
					List<ProjectInfo> projectList;
					projectList = usersession.getRemoteProjectList();
					
					// retrieve the first Project from the List
					ProjectInfo projectInfo = projectList.iterator().next();
					// checkout the ProjectSpace, containing all Models of the Project, into the local Workspace
					ProjectSpace projectSpace = usersession
							.checkout(projectInfo);

					// create and add a new "Book" from the example model
					// change this part to create instances of your own model
					Project project = projectSpace.getProject();
					Book book = LibraryFactory.eINSTANCE.createBook();
					book.setTitle("NEW TITLE");
					project.addModelElement(book);

					// commit the pending changes of the project to the EMF Store
					projectSpace.commit();
					
					// create and add another element from the example model
					Library library = LibraryFactory.eINSTANCE.createLibrary();
					project.addModelElement(library);
					library.getBooks().add(book);

					// commit once more
					projectSpace.commit();

					System.out.println("Client run completed.");
				} catch (AccessControlException e) {
					ModelUtil.logException(e);
				} catch (EmfStoreException e) {
					ModelUtil.logException(e);
				}
			}
		}.run(false);
	}

	public void stop() {
		// TODO Auto-generated method stub
	}
}
