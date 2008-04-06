/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.p2.touchpoint.eclipse;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.equinox.internal.p2.touchpoint.eclipse.messages"; //$NON-NLS-1$
	public static String error_loading_manipulator;
	public static String BundlePool;
	public static String failed_prepareIU;
	public static String error_saving_manipulator;
	public static String error_saving_platform_configuration;
	public static String error_saving_source_bundles_list;
	public static String error_constructing_platform_configuration_url;
	public static String generator_not_available;
	public static String artifact_file_not_found;

	static {
		// load message values from bundle file and assign to fields below
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
