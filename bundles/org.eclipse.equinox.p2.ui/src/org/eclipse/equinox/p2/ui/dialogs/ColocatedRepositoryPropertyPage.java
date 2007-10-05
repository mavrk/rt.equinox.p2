/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.ui.dialogs;

import org.eclipse.equinox.p2.core.repository.IRepository;
import org.eclipse.equinox.p2.metadata.repository.IMetadataRepository;
import org.eclipse.equinox.p2.ui.ColocatedRepositoryInfo;

/**
 * PropertyPage that shows a repository's properties
 * 
 * @since 3.4
 */
public class ColocatedRepositoryPropertyPage extends RepositoryPropertyPage {
	protected IRepository getRepository() {
		IRepository repo = super.getRepository();
		if (repo instanceof IMetadataRepository)
			return new ColocatedRepositoryInfo((IMetadataRepository) repo);
		return repo;
	}
}
