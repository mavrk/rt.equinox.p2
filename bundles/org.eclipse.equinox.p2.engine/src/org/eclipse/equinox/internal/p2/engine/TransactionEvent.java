/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.p2.engine;

import org.eclipse.equinox.p2.engine.IEngine;
import org.eclipse.equinox.p2.engine.IProfile;

import java.util.EventObject;

/**
 * @since 2.0
 */
public abstract class TransactionEvent extends EventObject {
	private static final long serialVersionUID = 6278706971855493984L;
	protected IProfile profile;
	protected PhaseSet phaseSet;
	protected Operand[] operands;

	public TransactionEvent(IProfile profile, PhaseSet phaseSet, Operand[] operands, IEngine engine) {
		super(engine);
		this.profile = profile;
		this.phaseSet = phaseSet;
		this.operands = operands;
	}

	public IProfile getProfile() {
		return profile;
	}
}
