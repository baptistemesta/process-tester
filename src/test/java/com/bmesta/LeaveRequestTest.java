/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bmesta;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bonitasoft.engine.api.APIClient;
import com.bonitasoft.engine.test.TestEngineSP;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.test.junit.BonitaEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class LeaveRequestTest {

    @Rule
    public BonitaEngineRule engineRule = BonitaEngineRule.createWith(TestEngineSP.getInstance());
    private ProcessDefinition processDefinition;


    @Before
    public void before() throws Exception {
        APIClient apiClient = new APIClient();
        apiClient.login("install", "install");
        LeaveRequestBuilder leaveRequestBuilder = new LeaveRequestBuilder();
        leaveRequestBuilder.createOrganization(apiClient.getIdentityAPI());
        processDefinition = apiClient.getProcessAPI().deploy(leaveRequestBuilder.build());
        apiClient.getProcessAPI().enableProcess(processDefinition.getId());
        apiClient.logout();

    }

    @After
    public void after() throws Exception {
        APIClient apiClient = new APIClient();
        apiClient.login("install", "install");
        apiClient.getProcessAPI().disableProcess(processDefinition.getId());
        apiClient.getProcessAPI().deleteProcessInstances(processDefinition.getId(), 0, 1000);
        apiClient.getProcessAPI().deleteProcessDefinition(processDefinition.getId());
        apiClient.logout();

    }

    @Test(expected = Exception.class)
    public void should_start_process_with_wrong_parameter_throw_exception() throws Exception {
        //given
        APIClient apiClient = new APIClient();
        apiClient.login("john", "bpm");
        //when
        apiClient.getProcessAPI().startProcessWithInputs(processDefinition.getId(), Collections.<String, Serializable>emptyMap());

        //then:
        // exception
    }

    @Test
    public void should_start_process_result_in_pending_task_for_employee() throws Exception {
        //given
        APIClient apiClient = new APIClient();
        apiClient.login("john", "bpm");
        //when
        Map<String, Serializable> inputs = new HashMap<String, Serializable>();
        inputs.put("type", "RTT");
        inputs.put("nbDays", 10);
        ProcessInstance processInstance = apiClient.getProcessAPI().startProcessWithInputs(processDefinition.getId(), inputs);

        apiClient.logout();
        apiClient.login("jack","bpm");

        //then
        Thread.sleep(3000);
        List<HumanTaskInstance> pendingHumanTaskInstances = apiClient.getProcessAPI().getPendingHumanTaskInstances(apiClient.getSession().getUserId(), 0, 10, ActivityInstanceCriterion.DEFAULT);

        assertThat(pendingHumanTaskInstances).as("number of pending task for john").hasSize(1);
        assertThat(pendingHumanTaskInstances.get(0).getParentProcessInstanceId()).as("process instance of the task").isEqualTo(processInstance.getId());
        assertThat(pendingHumanTaskInstances.get(0).getName()).as("name of the pending task").isEqualTo("validate");
    }
}
