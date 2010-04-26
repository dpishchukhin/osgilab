/*
 * Copyright (c) 2010 Dmytro Pishchukhin (http://osgilab.org)
 * This program is made available under the terms of the MIT License.
 */

package org.osgilab.tips.shell.knopflerfish;

import org.knopflerfish.service.console.CommandGroup;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.io.PrintWriter;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Knopflerfish shell adapter activator
 *
 * @author dmytro.pishchukhin
 */
public class Activator implements BundleActivator {
    private static final Logger LOG = Logger.getLogger(Activator.class.getName());

    private static final String COMMANDS_DESCRIPTION_PROPERTY = "org.osgilab.tips.shell.commands";
    private static final String GROUP_ID_PROPERTY = "org.osgilab.tips.shell.group.id";
    private static final String GROUP_NAME_PROPERTY = "org.osgilab.tips.shell.group.name";

    /**
     * Knopflerfish shell API supports groups.
     * Filter requires shell commands description and group id and group name
     */
    private static final String SHELL_COMMANDS_SERVICE_FILTER = "(&" +
            "(" + COMMANDS_DESCRIPTION_PROPERTY + "=*)" +
            "(" + GROUP_ID_PROPERTY + "=*)" +
            "(" + GROUP_NAME_PROPERTY + "=*)" +
            ")";

    /**
     * Bundle Context instance
     */
    private BundleContext bc;
    /**
     * Command provides service tracker
     */
    private ServiceTracker shellCommandsTracker;

    private Map<ServiceReference, ServiceRegistration> commandRegistrations = new HashMap<ServiceReference, ServiceRegistration>();

    public void start(BundleContext context) throws Exception {
        bc = context;
        shellCommandsTracker = new ServiceTracker(bc, bc.createFilter(SHELL_COMMANDS_SERVICE_FILTER),
                new ShellCommandsCustomizer());
        shellCommandsTracker.open();
    }

    public void stop(BundleContext context) throws Exception {
        shellCommandsTracker.close();
        shellCommandsTracker = null;

        bc = null;
    }

    /**
     * Validate Command method
     *
     * @param service     service instance
     * @param commandName command method name
     * @return <code>true</code> if method is peresent in service, <code>public</code> and
     *         has params <code>PrintStream</code> and <code>String[]</code>, otherwise - <code>false</code>
     */
    private boolean isValidCommandMethod(Object service, String commandName) {
        try {
            service.getClass().getMethod(commandName, PrintWriter.class, String[].class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Command provides service tracker customizer
     */
    private class ShellCommandsCustomizer implements ServiceTrackerCustomizer {
        public Object addingService(ServiceReference reference) {
            // get group id property
            Object groupId = reference.getProperty(GROUP_ID_PROPERTY);
            // if property value null or not String - ignore service
            if (groupId == null || !(groupId instanceof String)) {
                LOG.warning(GROUP_ID_PROPERTY + " property is null or invalid. Ignore service");
                return null;
            }
            Object groupName = reference.getProperty(GROUP_NAME_PROPERTY);
            // if property value null or not String - ignore service
            if (groupName == null || !(groupName instanceof String)) {
                LOG.warning(GROUP_NAME_PROPERTY + " property is null or invalid. Ignore service");
                return null;
            }
            // get commands description property
            Object commandsDescription = reference.getProperty(COMMANDS_DESCRIPTION_PROPERTY);
            // if property value null or not String[][] - ignore service
            if (commandsDescription == null) {
                LOG.warning(COMMANDS_DESCRIPTION_PROPERTY + " property is null. Ignore service");
                return null;
            } else if (!(commandsDescription instanceof String[][])) {
                LOG.warning(COMMANDS_DESCRIPTION_PROPERTY + " property has wrong format: not String[][]");
                return null;
            } else {
                Object service = bc.getService(reference);
                KnopflerfishCommandGroup commandGroup = new KnopflerfishCommandGroup((String) groupId, (String) groupName, service);

                String[][] commands = (String[][]) commandsDescription;
                for (String[] commandInfo : commands) {
                    // if command info is invalid - ignore command
                    if (commandInfo != null) {
                        if (commandInfo.length != 2) {
                            LOG.warning(COMMANDS_DESCRIPTION_PROPERTY + " property has wrong format: not String[][]");
                            continue;
                        }
                        String commandName = commandInfo[0];
                        String commandHelp = commandInfo[1];
                        // if command info links to wrong method - ignore command
                        if (isValidCommandMethod(service, commandName)) {
                            commandGroup.addCommandHelp(commandHelp);
                        }
                    }
                }
                if (commandGroup.getCommandsCount() > 0) {
                    Dictionary<String, Object> props = new Hashtable<String, Object>();
                    // get service ranking propety. if not null - use it on Command services registration
                    Integer ranking = (Integer) reference.getProperty(Constants.SERVICE_RANKING);
                    if (ranking != null) {
                        props.put(Constants.SERVICE_RANKING, ranking);
                    }
                    props.put(CommandGroup.GROUP_NAME, commandGroup.getGroupName());
                    commandRegistrations.put(reference,
                            bc.registerService(CommandGroup.class.getName(), commandGroup, props));
                    return service;
                } else {
                    return null;
                }
            }
        }

        public void modifiedService(ServiceReference reference, Object service) {
            // ignore
        }

        public void removedService(ServiceReference reference, Object service) {
            // unregister CommandGroup services that belongs to this service registration
            ServiceRegistration registration = commandRegistrations.remove(reference);
            if (registration != null) {
                registration.unregister();
            }
            bc.ungetService(reference);
        }
    }
}
