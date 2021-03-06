/*
 * Copyright (c) 2010 Dmytro Pishchukhin (http://knowhowlab.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.knowhowlab.osgi.jmx.beans;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.startlevel.StartLevel;

import java.io.InputStream;
import java.util.Dictionary;

/**
 * OSGi visitor interface. It is used to access OSGi specific services.
 *
 * @author dpishchukhin
 */
public interface OsgiVisitor {
    Bundle getBundle(long id);

    ServiceReference getServiceReferenceById(long id);

    ServiceReference[] getAllServiceReferences();

    PackageAdmin getPackageAdmin();

    StartLevel getStartLevel();

    Bundle installBundle(String location) throws BundleException;

    Bundle installBundle(String location, InputStream stream) throws BundleException;

    org.osgi.framework.launch.Framework getFramework();

    Bundle[] getBundles();

    String getProperty(String name);

    ServiceRegistration registerService(String className, Object object, Dictionary props);
}
