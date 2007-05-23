/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.simpleconfigurator.internal;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.eclipse.equinox.configurator.Configurator;
import org.eclipse.equinox.internal.simpleconfigurator.utils.*;
import org.osgi.framework.BundleContext;

/**
 * SimpleConfigurator provides ways to install bundles listed in a file accessible
 * by the specified URL and expect states for it in advance without actual application.
 * 
 * In every methods of SimpleConfiguration object,
 *  
 * 1. A value will be gotten by @{link BundleContext#getProperty(key) with 
 * {@link SimpleConfiguratorConstants#PROP_KEY_EXCLUSIVE_INSTALLATION} as a key.
 * 2. If it equals "true", it will do exclusive installation, which means that 
 * the bundles will not be listed in the specified url but installed at the time
 * of the method call except SystemBundle will be uninstalled. Otherwise, no uninstallation will not be done.
 */
public class SimpleConfiguratorImpl implements Configurator {
	final static BundleInfo[] NULL_BUNDLEINFOS = new BundleInfo[0];

	BundleContext context;

	ConfigApplier configApplier;

	private URL url = null;

	SimpleConfiguratorImpl(BundleContext context) {
		this.context = context;
	}

	public synchronized void applyConfiguration(URL url) throws IOException {
		if (Activator.DEBUG)
			System.out.println("applyConfiguration() URL=" + url);
		if (url == null)
			return;
		this.url = url;

		List bundleInfoList = SimpleConfiguratorUtils.readConfiguration(url);
		if (Activator.DEBUG)
			System.out.println("applyConfiguration() bundleInfoList.size()=" + bundleInfoList.size());
		if (bundleInfoList.size() == 0)
			return;
		if (this.configApplier == null)
			configApplier = new ConfigApplier(context, this);
		configApplier.install(Utils.getBundleInfosFromList(bundleInfoList), isExclusiveInstallation());
	}

	private boolean isExclusiveInstallation() {
		return Boolean.valueOf(context.getProperty(SimpleConfiguratorConstants.PROP_KEY_EXCLUSIVE_INSTALLATION)).booleanValue();
	}

	public synchronized void applyConfiguration() throws IOException {
		this.applyConfiguration(url);
	}

	public synchronized URL getUrlInUse() {
		return url;
	}
}