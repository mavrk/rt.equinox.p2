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
package org.eclipse.equinox.p2.ui.operations;

import org.eclipse.core.runtime.*;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.director.ProvisioningPlan;
import org.eclipse.equinox.p2.engine.Profile;
import org.eclipse.equinox.p2.ui.ProvisioningUtil;

/**
 * Abstract class representing provisioning profile operations
 * 
 * @since 3.4
 */
public class ProfileModificationOperation extends ProfileOperation {

	ProvisioningPlan plan;

	public ProfileModificationOperation(String label, String id, ProvisioningPlan plan) {
		super(label, new String[] {id});
		this.plan = plan;
	}

	boolean isValid() {
		return super.isValid() && plan != null && plan.getStatus().isOK();
	}

	public String getProfileId() {
		try {
			return super.getProfiles()[0].getProfileId();
		} catch (ProvisionException e) {
			return null;
		}
	}

	public Profile getProfile() {
		try {
			return super.getProfiles()[0];
		} catch (ProvisionException e) {
			return null;
		}
	}

	protected IStatus doExecute(IProgressMonitor monitor, IAdaptable uiInfo) throws ProvisionException {
		return ProvisioningUtil.performProvisioningPlan(plan, getProfile(), monitor);
	}
}
