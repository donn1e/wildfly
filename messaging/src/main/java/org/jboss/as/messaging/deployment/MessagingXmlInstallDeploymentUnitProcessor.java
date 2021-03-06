/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.as.messaging.deployment;

import static org.jboss.as.messaging.CommonAttributes.DURABLE;
import static org.jboss.as.messaging.CommonAttributes.HORNETQ_SERVER;
import static org.jboss.as.messaging.CommonAttributes.JMS_QUEUE;
import static org.jboss.as.messaging.CommonAttributes.JMS_TOPIC;
import static org.jboss.as.messaging.CommonAttributes.SELECTOR;

import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.messaging.CommonAttributes;
import org.jboss.as.messaging.MessagingExtension;
import org.jboss.as.messaging.MessagingServices;
import org.jboss.as.messaging.jms.JMSQueueConfigurationRuntimeHandler;
import org.jboss.as.messaging.jms.JMSQueueService;
import org.jboss.as.messaging.jms.JMSServices;
import org.jboss.as.messaging.jms.JMSTopicConfigurationRuntimeHandler;
import org.jboss.as.messaging.jms.JMSTopicService;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentModelUtils;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentResourceSupport;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;

/**
 * Processor that handles the installation of the messaging subsystems deployable XML
 *
 * @author Stuart Douglas
 */
public class MessagingXmlInstallDeploymentUnitProcessor implements DeploymentUnitProcessor {


    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final List<ParseResult> parseResults = deploymentUnit.getAttachmentList(MessagingAttachments.PARSE_RESULT);
        final DeploymentResourceSupport deploymentResourceSupport = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_RESOURCE_SUPPORT);
        for (final ParseResult parseResult : parseResults) {

            for (final JmsDestination topic : parseResult.getTopics()) {
                final ServiceName hqServiceName = MessagingServices.getHornetQServiceName(topic.getServer());
                String[] jndiBindings = null;
                if (topic.getDestination().hasDefined(CommonAttributes.DESTINATION_ENTRIES.getName())) {
                    final ModelNode entries = topic.getDestination().resolve().get(CommonAttributes.DESTINATION_ENTRIES.getName());
                    jndiBindings = JMSServices.getJndiBindings(entries);
                }
                JMSTopicService.installService(topic.getName(), hqServiceName, phaseContext.getServiceTarget(), jndiBindings);

                //create the management registration
                final PathElement serverElement = PathElement.pathElement(HORNETQ_SERVER, topic.getServer());
                final PathElement destination = PathElement.pathElement(JMS_TOPIC, topic.getName());
                deploymentResourceSupport.getDeploymentSubModel(MessagingExtension.SUBSYSTEM_NAME, serverElement);
                PathAddress registration = PathAddress.pathAddress(serverElement, destination);
                createDeploymentSubModel(registration, deploymentUnit);

                JMSTopicConfigurationRuntimeHandler.INSTANCE.registerResource(topic.getServer(), topic.getName(), topic.getDestination());
            }

            for (final JmsDestination queue : parseResult.getQueues()) {

                final ServiceName hqServiceName = MessagingServices.getHornetQServiceName(queue.getServer());
                String[] jndiBindings = null;
                final ModelNode destination = queue.getDestination();
                if (destination.hasDefined(CommonAttributes.DESTINATION_ENTRIES.getName())) {
                    final ModelNode entries = destination.resolve().get(CommonAttributes.DESTINATION_ENTRIES.getName());
                    jndiBindings = JMSServices.getJndiBindings(entries);
                }
                final String selector = destination.hasDefined(SELECTOR.getName()) ? destination.get(SELECTOR.getName()).resolve().asString() : null;
                final boolean durable = destination.hasDefined(DURABLE.getName()) ? destination.get(DURABLE.getName()).resolve().asBoolean() : false;

                JMSQueueService.installService(queue.getName(), phaseContext.getServiceTarget(), hqServiceName, selector, durable, jndiBindings);

                //create the management registration
                final PathElement serverElement = PathElement.pathElement(HORNETQ_SERVER, queue.getServer());
                final PathElement dest = PathElement.pathElement(JMS_QUEUE, queue.getName());
                deploymentResourceSupport.getDeploymentSubModel(MessagingExtension.SUBSYSTEM_NAME, serverElement);
                PathAddress registration = PathAddress.pathAddress(serverElement, dest);
                createDeploymentSubModel(registration, deploymentUnit);
                JMSQueueConfigurationRuntimeHandler.INSTANCE.registerResource(queue.getServer(), queue.getName(), destination);
            }
        }
    }


    @Override
    public void undeploy(final DeploymentUnit context) {
        final List<ParseResult> parseResults = context.getAttachmentList(MessagingAttachments.PARSE_RESULT);
        for (ParseResult parseResult : parseResults) {
            for (final JmsDestination topic : parseResult.getTopics()) {
                JMSTopicConfigurationRuntimeHandler.INSTANCE.unregisterResource(topic.getServer(), topic.getName());
            }

            for (final JmsDestination queue : parseResult.getQueues()) {
                JMSQueueConfigurationRuntimeHandler.INSTANCE.unregisterResource(queue.getServer(), queue.getName());
            }
        }
    }


    static ManagementResourceRegistration createDeploymentSubModel(final PathAddress address, final DeploymentUnit unit) {
        final Resource root = unit.getAttachment(DeploymentModelUtils.DEPLOYMENT_RESOURCE);
        synchronized (root) {
            final ManagementResourceRegistration registration = unit.getAttachment(DeploymentModelUtils.MUTABLE_REGISTRATION_ATTACHMENT);
            final PathAddress subsystemAddress = PathAddress.pathAddress(PathElement.pathElement(ModelDescriptionConstants.SUBSYSTEM, MessagingExtension.SUBSYSTEM_NAME));
            final Resource subsystem = getOrCreate(root, subsystemAddress);

            final ManagementResourceRegistration subModel = registration.getSubModel(subsystemAddress.append(address));
            if (subModel == null) {
                throw new IllegalStateException(address.toString());
            }
            getOrCreate(subsystem, address);
            return subModel;
        }
    }

    static Resource getOrCreate(final Resource parent, final PathAddress address) {
        Resource current = parent;
        for (final PathElement element : address) {
            synchronized (current) {
                if (current.hasChild(element)) {
                    current = current.requireChild(element);
                } else {
                    final Resource resource = Resource.Factory.create();
                    current.registerChild(element, resource);
                    current = resource;
                }
            }
        }
        return current;
    }

}
