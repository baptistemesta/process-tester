/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bmesta;

import java.util.Arrays;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor;
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.ContractDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.identity.UserNotFoundException;

/**
 * @author Baptiste Mesta
 */
public class LeaveRequestBuilder {

    public void createOrganization(IdentityAPI identityAPI) throws CreationException {
        try {
            identityAPI.getUserByUserName("john");
        } catch (UserNotFoundException e) {
            identityAPI.createUser("john", "bpm");
        }
        try {
            identityAPI.getUserByUserName("jack");
        } catch (UserNotFoundException e) {
            identityAPI.createUser("jack", "bpm");
        }
    }

    public BusinessArchive build() throws InvalidBusinessArchiveFormatException, InvalidProcessDefinitionException {
        BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        ProcessDefinitionBuilder processDefinitionBuilder = createProcess();
        businessArchiveBuilder.setProcessDefinition(processDefinitionBuilder.done());
        ActorMapping actorMapping = createActorMapping();
        businessArchiveBuilder.setActorMapping(actorMapping);
        return businessArchiveBuilder.done();
    }

    private ProcessDefinitionBuilder createProcess() {
        ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("LeaveRequest", "1.0");
        processDefinitionBuilder.addActor("employee");
        processDefinitionBuilder.addActor("manager");
        ContractDefinitionBuilder contractDefinitionBuilder = processDefinitionBuilder.addContract();
        contractDefinitionBuilder.addInput("type", Type.TEXT, "type of the request");
        contractDefinitionBuilder.addInput("nbDays", Type.INTEGER, "number of days");
        processDefinitionBuilder.addUserTask("validate", "manager").addContract().addInput("accepted", Type.BOOLEAN, "true if the request was validated");
        processDefinitionBuilder.setActorInitiator("employee");
        return processDefinitionBuilder;
    }

    private ActorMapping createActorMapping() {
        ActorMapping actorMapping = new ActorMapping();
        Actor employee = new Actor("employee");
        employee.addUser("john");
        Actor manager = new Actor("manager");
        manager.addUser("jack");
        actorMapping.setActors(Arrays.asList(employee, manager));
        return actorMapping;
    }

}
