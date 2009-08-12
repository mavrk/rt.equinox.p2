/*******************************************************************************
 *  Copyright (c) 2007, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *   IBM Corporation - initial API and implementation
 *   Genuitec LLC - various bug fixes
 *******************************************************************************/
package org.eclipse.equinox.internal.p2.artifact.repository;

import java.net.URI;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.internal.p2.core.helpers.LogHelper;
import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.internal.p2.repository.helpers.AbstractRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.*;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.core.location.AgentLocation;
import org.eclipse.equinox.internal.provisional.p2.metadata.IArtifactKey;
import org.eclipse.equinox.internal.provisional.p2.repository.IRepository;
import org.eclipse.equinox.internal.provisional.spi.p2.artifact.repository.ArtifactRepositoryFactory;

/**
 * Default implementation of {@link IArtifactRepositoryManager}.
 * 
 * TODO the current assumption that the "location" is the dir/root limits us to 
 * having just one repository in a given URL..  
 */
public class ArtifactRepositoryManager extends AbstractRepositoryManager implements IArtifactRepositoryManager {

	public ArtifactRepositoryManager() {
		super();
	}

	public void addRepository(IArtifactRepository repository) {
		super.addRepository(repository, true, null);
	}

	public IArtifactRequest createMirrorRequest(IArtifactKey key, IArtifactRepository destination, Properties destinationDescriptorProperties, Properties destinationRepositoryProperties) {
		return new MirrorRequest(key, destination, destinationDescriptorProperties, destinationRepositoryProperties);
	}

	public IArtifactRepository createRepository(URI location, String name, String type, Map properties) throws ProvisionException {
		return (IArtifactRepository) doCreateRepository(location, name, type, properties);
	}

	protected IRepository factoryCreate(URI location, String name, String type, Map properties, IExtension extension) throws ProvisionException {
		ArtifactRepositoryFactory factory = (ArtifactRepositoryFactory) createExecutableExtension(extension, EL_FACTORY);
		if (factory == null)
			return null;
		return factory.create(location, name, type, properties);
	}

	protected IRepository factoryLoad(URI location, IExtension extension, int flags, SubMonitor monitor) throws ProvisionException {
		ArtifactRepositoryFactory factory = (ArtifactRepositoryFactory) createExecutableExtension(extension, EL_FACTORY);
		if (factory == null)
			return null;
		return factory.load(location, flags, monitor.newChild(10));
	}

	protected String getBundleId() {
		return Activator.ID;
	}

	protected String getDefaultSuffix() {
		return "artifacts.xml"; //$NON-NLS-1$
	}

	protected String getRepositoryProviderExtensionPointId() {
		return Activator.REPO_PROVIDER_XPT;
	}

	/**
	 * Restores metadata repositories specified as system properties.
	 */
	protected String getRepositorySystemProperty() {
		return "eclipse.p2.artifactRepository"; //$NON-NLS-1$
	}

	protected int getRepositoryType() {
		return IRepository.TYPE_ARTIFACT;
	}

	public IArtifactRepository loadRepository(URI location, IProgressMonitor monitor) throws ProvisionException {
		return loadRepository(location, 0, monitor);
	}

	public IArtifactRepository loadRepository(URI location, int flags, IProgressMonitor monitor) throws ProvisionException {
		return (IArtifactRepository) loadRepository(location, monitor, null, flags);
	}

	public IArtifactRepository refreshRepository(URI location, IProgressMonitor monitor) throws ProvisionException {
		return (IArtifactRepository) basicRefreshRepository(location, monitor);
	}

	/**
	 * Restore the download cache
	 */
	protected void restoreSpecialRepositories() {
		// TODO while recreating, we may want to have proxies on repo instead of the real repo object to limit what is activated.
		AgentLocation location = (AgentLocation) ServiceHelper.getService(Activator.getContext(), AgentLocation.class.getName());
		if (location == null)
			// TODO should do something here since we are failing to restore.
			return;
		try {
			loadRepository(location.getArtifactRepositoryURI(), null);
			return;
		} catch (ProvisionException e) {
			// log but still continue and try to create a new one
			if (e.getStatus().getCode() != ProvisionException.REPOSITORY_NOT_FOUND)
				LogHelper.log(new Status(IStatus.ERROR, Activator.ID, "Error occurred while loading download cache.", e)); //$NON-NLS-1$
		}
		try {
			Map properties = new HashMap(1);
			properties.put(IRepository.PROP_SYSTEM, Boolean.TRUE.toString());
			createRepository(location.getArtifactRepositoryURI(), "download cache", TYPE_SIMPLE_REPOSITORY, properties); //$NON-NLS-1$
		} catch (ProvisionException e) {
			LogHelper.log(e);
		}
	}

}