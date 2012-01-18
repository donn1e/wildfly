/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.web;

import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.WARN;

import org.jboss.as.web.deployment.JsfVersionMarker;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

/**
 * Date: 05.11.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageLogger(projectCode = "JBAS")
public interface WebLogger extends BasicLogger {

    /**
     * A logger with the category of the package name.
     */
    WebLogger ROOT_LOGGER = Logger.getMessageLogger(WebLogger.class, WebLogger.class.getPackage().getName());

    /**
     * A logger with the category {@code org.jboss.as.web.security}.
     */
    WebLogger WEB_SECURITY_LOGGER = Logger.getMessageLogger(WebLogger.class, "org.jboss.as.web.security");

    /**
     * A logger with the category {@code org.jboss.web}.
     */
    WebLogger WEB_LOGGER = Logger.getMessageLogger(WebLogger.class, "org.jboss.web");

    @LogMessage(level = ERROR)
    @Message(id = 18200, value = "Failed to start welcome context")
    void stopWelcomeContextFailed(@Cause Throwable cause);

    @LogMessage(level = ERROR)
    @Message(id = 18201, value = "Failed to destroy welcome context")
    void destroyWelcomeContextFailed(@Cause Throwable cause);

    @LogMessage(level = ERROR)
    @Message(id = 18202, value = "Error calling onStartup for servlet container initializer: %s")
    void sciOnStartupError(String sciClassName, @Cause Throwable cause);

    @LogMessage(level = ERROR)
    @Message(id = 18203, value = "Error instantiating container component: %s")
    void componentInstanceCreationFailed(String className, @Cause Throwable cause);

    @LogMessage(level = WARN)
    @Message(id = 18204, value = "Clustering not supported, falling back to non-clustered session manager")
    void clusteringNotSupported();

    @LogMessage(level = ERROR)
    @Message(id = 18205, value = "Cannot setup overlays for [%s] due to custom resources")
    void noOverlay(String webappPath);

    @LogMessage(level = ERROR)
    @Message(id = 18206, value = "Webapp [%s] is unavailable due to startup errors")
    void unavailable(String webappPath);

    @LogMessage(level = WARN)
    @Message(id = 18207, value = "Unknown JSF version %s %s will be used instead")
    void unknownJSFVersion(String version, String referenceVersion);

    @LogMessage(level = ERROR)
    @Message(id = 18208, value = "Failed to start context")
    void stopContextFailed(@Cause Throwable cause);

    @LogMessage(level = ERROR)
    @Message(id = 18209, value = "Failed to destroy context")
    void destroyContextFailed(@Cause Throwable cause);

    @LogMessage(level = INFO)
    @Message(id = 18210, value = "Registering web context: %s")
    void registerWebapp(String webappPath);

}
