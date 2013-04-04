/*******************************************************************************
 * Copyright (c) 2013 Ericsson AB and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Ericsson AB - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.internal.p2.ui.sdk.scheduler.migration;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.internal.p2.engine.EngineActivator;
import org.eclipse.equinox.internal.p2.metadata.query.UpdateQuery;
import org.eclipse.equinox.internal.p2.ui.sdk.scheduler.AutomaticUpdatePlugin;
import org.eclipse.equinox.internal.p2.ui.sdk.scheduler.PreviousConfigurationFinder;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.engine.query.UserVisibleRootQuery;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.IRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class MigrationSupport {
	private static final String ECLIPSE_P2_SKIP_MIGRATION_WIZARD = "eclipse.p2.skipMigrationWizard"; //$NON-NLS-1$
	private static final String ECLIPSE_P2_SKIP_MOVED_INSTALL_DETECTION = "eclipse.p2.skipMovedInstallDetection"; //$NON-NLS-1$

	//The return value indicates if the migration dialog has been shown or not. It does not indicate whether the migration has completed.
	public boolean performMigration(IProvisioningAgent agent, IProfileRegistry registry, IProfile currentProfile) {
		boolean skipWizard = Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty(ECLIPSE_P2_SKIP_MIGRATION_WIZARD));
		if (skipWizard)
			return false;

		IProfile previousProfile = null;
		URI[] reposToMigrate = null;
		if (!skipFirstTimeMigration() && !configurationSpecifiedManually() && isFirstTimeRunningThisSharedInstance(agent, registry, currentProfile)) {
			File searchRoot = getSearchLocation();
			if (searchRoot == null)
				return false;

			IProvisioningAgent otherConfigAgent = new PreviousConfigurationFinder(getConfigurationLocation().getParentFile()).findPreviousInstalls(searchRoot, getInstallFolder());
			if (otherConfigAgent == null) {
				return false;
			}
			previousProfile = ((IProfileRegistry) otherConfigAgent.getService(IProfileRegistry.SERVICE_NAME)).getProfile(IProfileRegistry.SELF);
			if (previousProfile == null)
				return false;

			reposToMigrate = ((IMetadataRepositoryManager) otherConfigAgent.getService(IMetadataRepositoryManager.SERVICE_NAME)).getKnownRepositories(IRepositoryManager.REPOSITORIES_NON_SYSTEM);
		}

		if (previousProfile == null && baseChangedSinceLastPresentationOfWizard(agent, registry, currentProfile))
			previousProfile = findMostRecentReset(registry, currentProfile);

		if (previousProfile == null)
			return false;

		if (needsMigration(previousProfile, currentProfile)) {
			openMigrationWizard(previousProfile, reposToMigrate);
		} else {
			//There is nothing to migrate, so we mark the migration complete
			AutomaticUpdatePlugin.getDefault().rememberMigrationCompleted(currentProfile.getProfileId());
		}
		return true;
	}

	private File getInstallFolder() {
		Location configurationLocation = (Location) ServiceHelper.getService(EngineActivator.getContext(), Location.class.getName(), Location.INSTALL_FILTER);
		return new File(configurationLocation.getURL().getPath());

	}

	//The search location is two level up from the configuration location.
	private File getSearchLocation() {
		File parent = getConfigurationLocation().getParentFile();
		if (parent == null)
			return null;
		return parent.getParentFile();
	}

	private File getConfigurationLocation() {
		Location configurationLocation = (Location) ServiceHelper.getService(EngineActivator.getContext(), Location.class.getName(), Location.CONFIGURATION_FILTER);
		File configurationFolder = new File(configurationLocation.getURL().getPath());
		return configurationFolder;
	}

	//Check if the user has explicitly specified -configuration on the command line
	private boolean configurationSpecifiedManually() {
		String commandLine = System.getProperty("eclipse.commands"); //$NON-NLS-1$
		if (commandLine == null)
			return false;
		return commandLine.contains("-configuration\n"); //$NON-NLS-1$
	}

	private boolean skipFirstTimeMigration() {
		return Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty(ECLIPSE_P2_SKIP_MOVED_INSTALL_DETECTION));
	}

	private boolean isFirstTimeRunningThisSharedInstance(IProvisioningAgent agent, IProfileRegistry registry, IProfile currentProfile) {
		long[] history = registry.listProfileTimestamps(currentProfile.getProfileId());
		boolean isInitial = !IProfile.STATE_SHARED_INSTALL_VALUE_INITIAL.equals(registry.getProfileStateProperties(currentProfile.getProfileId(), history[0]).get(IProfile.STATE_PROP_SHARED_INSTALL));
		if (isInitial) {
			if (AutomaticUpdatePlugin.getDefault().getLastMigration() >= history[0])
				return false;
			return true;
		}
		return false;
	}

	/**
	 * @param previousProfile is the profile used previous to the current one
	 * @param currentProfile is the current profile used by eclipse.
	 * @return true if set difference between previousProfile units and currentProfile units not empty, otherwise false
	 */
	private boolean needsMigration(IProfile previousProfile, IProfile currentProfile) {
		//First, try the case of inclusion
		Set<IInstallableUnit> previousProfileUnits = previousProfile.query(new UserVisibleRootQuery(), null).toSet();
		Set<IInstallableUnit> currentProfileUnits = currentProfile.available(new UserVisibleRootQuery(), null).toSet();
		previousProfileUnits.removeAll(currentProfileUnits);

		//For the IUs left in the previous profile, look for those that could be in the base but not as roots
		Iterator<IInstallableUnit> previousProfileIterator = previousProfileUnits.iterator();
		while (previousProfileIterator.hasNext()) {
			if (!currentProfile.available(QueryUtil.createIUQuery(previousProfileIterator.next()), null).isEmpty())
				previousProfileIterator.remove();
		}

		//For the IUs left in the previous profile, look for those that could be available in the root but as higher versions (they could be root or not)
		previousProfileIterator = previousProfileUnits.iterator();
		while (previousProfileIterator.hasNext()) {
			if (!currentProfile.available(new UpdateQuery(previousProfileIterator.next()), null).isEmpty())
				previousProfileIterator.remove();
		}

		return !previousProfileUnits.isEmpty();
	}

	private void openMigrationWizard(final IProfile inputProfile, final URI[] reposToMigrate) {
		Display d = Display.getDefault();
		d.asyncExec(new Runnable() {
			public void run() {
				WizardDialog migrateWizard = new WizardDialog(getWorkbenchWindowShell(), new ImportFromInstallationWizard_c(inputProfile, reposToMigrate));
				migrateWizard.create();
				migrateWizard.open();
			}
		});
	}

	private boolean baseChangedSinceLastPresentationOfWizard(IProvisioningAgent agent, IProfileRegistry registry, IProfile profile) {
		long lastProfileMigrated = AutomaticUpdatePlugin.getDefault().getLastMigration();
		long lastResetTimestamp = findMostRecentResetTimestamp(registry, profile);
		return lastProfileMigrated <= lastResetTimestamp;
	}

	//The timestamp from which we migrated or -1
	private long findMostRecentResetTimestamp(IProfileRegistry registry, IProfile profile) {
		long[] history = registry.listProfileTimestamps(profile.getProfileId());
		int index = history.length - 1;
		boolean found = false;
		while (!(found = IProfile.STATE_SHARED_INSTALL_VALUE_BEFOREFLUSH.equals(registry.getProfileStateProperties(profile.getProfileId(), history[index]).get(IProfile.STATE_PROP_SHARED_INSTALL))) && index > 0) {
			index--;
		}
		if (!found)
			return -1;
		return history[index];
	}

	private IProfile findMostRecentReset(IProfileRegistry registry, IProfile profile) {
		long ts = findMostRecentResetTimestamp(registry, profile);
		if (ts == -1)
			return null;
		return registry.getProfile(profile.getProfileId(), ts);
	}

	Shell getWorkbenchWindowShell() {
		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		return activeWindow != null ? activeWindow.getShell() : null;

	}

}