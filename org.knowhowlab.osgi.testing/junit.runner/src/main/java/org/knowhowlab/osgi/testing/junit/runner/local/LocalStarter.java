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

package org.knowhowlab.osgi.testing.junit.runner.local;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author dmytro.pishchukhin
 */
public class LocalStarter {
    public static void run() throws Exception {
        final URLClassLoader cl = new URLClassLoader(new URL[]{new URL("file:org.eclipse.osgi_3.2.1.R32x_v20060717.jar")});
        Thread thread = new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setContextClassLoader(cl);

                
            }
        };
        thread.start();
        thread.join();
    }
}
