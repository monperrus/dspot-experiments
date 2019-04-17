/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.rest.service.api.repository;


import RestUrls.URL_DEPLOYMENT;
import RestUrls.URL_DEPLOYMENT_RESOURCE;
import RestUrls.URL_PROCESS_DEFINITION;
import RestUrls.URL_PROCESS_DEFINITION_MODEL;
import RestUrls.URL_PROCESS_DEFINITION_RESOURCE_CONTENT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URLDecoder;
import java.util.Calendar;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.test.Deployment;
import org.flowable.rest.service.BaseSpringRestTestCase;
import org.flowable.rest.service.api.RestUrls;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test for all REST-operations related to single a Process Definition resource.
 *
 * @author Frederik Heremans
 */
public class ProcessDefinitionResourceTest extends BaseSpringRestTestCase {
    /**
     * Test getting a single process definition. GET repository/process-definitions/{processDefinitionResource}
     */
    @Test
    @Deployment(resources = { "org/flowable/rest/service/api/repository/oneTaskProcess.bpmn20.xml" })
    public void testGetProcessDefinition() throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        HttpGet httpGet = new HttpGet(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_PROCESS_DEFINITION, processDefinition.getId()))));
        CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_OK);
        JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
        closeResponse(response);
        Assert.assertEquals(processDefinition.getId(), responseNode.get("id").textValue());
        Assert.assertEquals(processDefinition.getKey(), responseNode.get("key").textValue());
        Assert.assertEquals(processDefinition.getCategory(), responseNode.get("category").textValue());
        Assert.assertEquals(processDefinition.getVersion(), responseNode.get("version").intValue());
        Assert.assertEquals(processDefinition.getDescription(), responseNode.get("description").textValue());
        Assert.assertEquals(processDefinition.getName(), responseNode.get("name").textValue());
        Assert.assertFalse(responseNode.get("graphicalNotationDefined").booleanValue());
        // Check URL's
        Assert.assertEquals(httpGet.getURI().toString(), responseNode.get("url").asText());
        Assert.assertEquals(processDefinition.getDeploymentId(), responseNode.get("deploymentId").textValue());
        Assert.assertTrue(responseNode.get("deploymentUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(URL_DEPLOYMENT, processDefinition.getDeploymentId())));
        Assert.assertTrue(URLDecoder.decode(responseNode.get("resource").textValue(), "UTF-8").endsWith(RestUrls.createRelativeResourceUrl(URL_DEPLOYMENT_RESOURCE, processDefinition.getDeploymentId(), processDefinition.getResourceName())));
        Assert.assertTrue(responseNode.get("diagramResource").isNull());
    }

    /**
     * Test getting a single process definition with a graphical notation defined. GET repository/process-definitions/{processDefinitionResource}
     */
    @Test
    @Deployment
    public void testGetProcessDefinitionWithGraphicalNotation() throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        HttpGet httpGet = new HttpGet(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_PROCESS_DEFINITION, processDefinition.getId()))));
        CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_OK);
        JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
        closeResponse(response);
        Assert.assertEquals(processDefinition.getId(), responseNode.get("id").textValue());
        Assert.assertEquals(processDefinition.getKey(), responseNode.get("key").textValue());
        Assert.assertEquals(processDefinition.getCategory(), responseNode.get("category").textValue());
        Assert.assertEquals(processDefinition.getVersion(), responseNode.get("version").intValue());
        Assert.assertEquals(processDefinition.getDescription(), responseNode.get("description").textValue());
        Assert.assertEquals(processDefinition.getName(), responseNode.get("name").textValue());
        Assert.assertTrue(responseNode.get("graphicalNotationDefined").booleanValue());
        // Check URL's
        Assert.assertEquals(httpGet.getURI().toString(), responseNode.get("url").asText());
        Assert.assertEquals(processDefinition.getDeploymentId(), responseNode.get("deploymentId").textValue());
        Assert.assertTrue(responseNode.get("deploymentUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(URL_DEPLOYMENT, processDefinition.getDeploymentId())));
        Assert.assertTrue(URLDecoder.decode(responseNode.get("resource").textValue(), "UTF-8").endsWith(RestUrls.createRelativeResourceUrl(URL_DEPLOYMENT_RESOURCE, processDefinition.getDeploymentId(), processDefinition.getResourceName())));
        Assert.assertTrue(URLDecoder.decode(responseNode.get("diagramResource").textValue(), "UTF-8").endsWith(RestUrls.createRelativeResourceUrl(URL_DEPLOYMENT_RESOURCE, processDefinition.getDeploymentId(), processDefinition.getDiagramResourceName())));
    }

    /**
     * Test getting an unexisting process-definition. GET repository/process-definitions/{processDefinitionId}
     */
    @Test
    public void testGetUnexistingProcessDefinition() throws Exception {
        HttpGet httpGet = new HttpGet(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_PROCESS_DEFINITION, "unexisting"))));
        CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_NOT_FOUND);
        closeResponse(response);
    }

    /**
     * Test suspending a process definition. POST repository/process-definitions/{processDefinitionId}
     */
    @Test
    @Deployment(resources = { "org/flowable/rest/service/api/repository/oneTaskProcess.bpmn20.xml" })
    public void testSuspendProcessDefinition() throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        Assert.assertFalse(processDefinition.isSuspended());
        ObjectNode requestNode = objectMapper.createObjectNode();
        requestNode.put("action", "suspend");
        HttpPut httpPut = new HttpPut(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_PROCESS_DEFINITION, processDefinition.getId()))));
        httpPut.setEntity(new StringEntity(requestNode.toString()));
        CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
        // Check "OK" status
        JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
        closeResponse(response);
        Assert.assertTrue(responseNode.get("suspended").booleanValue());
        // Check if process-definition is suspended
        processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        Assert.assertTrue(processDefinition.isSuspended());
    }

    /**
     * Test suspending a process definition on a certain date. POST repository/process-definitions/{processDefinitionId}
     */
    @Test
    @Deployment(resources = { "org/flowable/rest/service/api/repository/oneTaskProcess.bpmn20.xml" })
    public void testSuspendProcessDefinitionDelayed() throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        Assert.assertFalse(processDefinition.isSuspended());
        ObjectNode requestNode = objectMapper.createObjectNode();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 2);
        // Format the date using ISO date format
        DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
        String dateString = formatter.print(cal.getTimeInMillis());
        requestNode.put("action", "suspend");
        requestNode.put("date", dateString);
        HttpPut httpPut = new HttpPut(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_PROCESS_DEFINITION, processDefinition.getId()))));
        httpPut.setEntity(new StringEntity(requestNode.toString()));
        CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
        // Check "OK" status
        JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
        closeResponse(response);
        Assert.assertTrue(responseNode.get("suspended").booleanValue());
        // Check if process-definition is not yet suspended
        processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        Assert.assertFalse(processDefinition.isSuspended());
        // Force suspension by altering time
        cal.add(Calendar.HOUR, 1);
        processEngineConfiguration.getClock().setCurrentTime(cal.getTime());
        waitForJobExecutorToProcessAllJobs(7000, 100);
        // Check if process-definition is suspended
        processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        Assert.assertTrue(processDefinition.isSuspended());
    }

    /**
     * Test suspending already suspended process definition. POST repository/process-definitions/{processDefinitionId}
     */
    @Test
    @Deployment(resources = { "org/flowable/rest/service/api/repository/oneTaskProcess.bpmn20.xml" })
    public void testSuspendAlreadySuspendedProcessDefinition() throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        repositoryService.suspendProcessDefinitionById(processDefinition.getId());
        processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        Assert.assertTrue(processDefinition.isSuspended());
        ObjectNode requestNode = objectMapper.createObjectNode();
        requestNode.put("action", "suspend");
        HttpPut httpPut = new HttpPut(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_PROCESS_DEFINITION, processDefinition.getId()))));
        httpPut.setEntity(new StringEntity(requestNode.toString()));
        CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_CONFLICT);
        closeResponse(response);
    }

    /**
     * Test activating a suspended process definition. POST repository/process-definitions/{processDefinitionId}
     */
    @Test
    @Deployment(resources = { "org/flowable/rest/service/api/repository/oneTaskProcess.bpmn20.xml" })
    public void testActivateProcessDefinition() throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        repositoryService.suspendProcessDefinitionById(processDefinition.getId());
        processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        Assert.assertTrue(processDefinition.isSuspended());
        ObjectNode requestNode = objectMapper.createObjectNode();
        requestNode.put("action", "activate");
        HttpPut httpPut = new HttpPut(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_PROCESS_DEFINITION, processDefinition.getId()))));
        httpPut.setEntity(new StringEntity(requestNode.toString()));
        CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
        // Check "OK" status
        JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
        closeResponse(response);
        Assert.assertFalse(responseNode.get("suspended").booleanValue());
        // Check if process-definition is suspended
        processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        Assert.assertFalse(processDefinition.isSuspended());
    }

    /**
     * Test activating a suspended process definition delayed. POST repository/process-definitions/{processDefinitionId}
     */
    @Test
    @Deployment(resources = { "org/flowable/rest/service/api/repository/oneTaskProcess.bpmn20.xml" })
    public void testActivateProcessDefinitionDelayed() throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        repositoryService.suspendProcessDefinitionById(processDefinition.getId());
        processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        Assert.assertTrue(processDefinition.isSuspended());
        ObjectNode requestNode = objectMapper.createObjectNode();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 2);
        // Format the date using ISO date format
        DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
        String dateString = formatter.print(cal.getTimeInMillis());
        requestNode.put("action", "activate");
        requestNode.put("date", dateString);
        HttpPut httpPut = new HttpPut(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_PROCESS_DEFINITION, processDefinition.getId()))));
        httpPut.setEntity(new StringEntity(requestNode.toString()));
        CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
        // Check "OK" status
        JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
        closeResponse(response);
        Assert.assertFalse(responseNode.get("suspended").booleanValue());
        // Check if process-definition is not yet active
        processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        Assert.assertTrue(processDefinition.isSuspended());
        // Force activation by altering time
        cal.add(Calendar.HOUR, 1);
        processEngineConfiguration.getClock().setCurrentTime(cal.getTime());
        waitForJobExecutorToProcessAllJobs(7000, 100);
        // Check if process-definition is activated
        processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        Assert.assertFalse(processDefinition.isSuspended());
    }

    /**
     * Test activating already active process definition. POST repository/process-definitions/{processDefinitionId}
     */
    @Test
    @Deployment(resources = { "org/flowable/rest/service/api/repository/oneTaskProcess.bpmn20.xml" })
    public void testActivateAlreadyActiveProcessDefinition() throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        Assert.assertFalse(processDefinition.isSuspended());
        ObjectNode requestNode = objectMapper.createObjectNode();
        requestNode.put("action", "activate");
        HttpPut httpPut = new HttpPut(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_PROCESS_DEFINITION, processDefinition.getId()))));
        httpPut.setEntity(new StringEntity(requestNode.toString()));
        CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_CONFLICT);
        closeResponse(response);
    }

    /**
     * Test executing an unexisting action.
     *
     * POST repository/process-definitions/{processDefinitionId}
     */
    @Test
    @Deployment(resources = { "org/flowable/rest/service/api/repository/oneTaskProcess.bpmn20.xml" })
    public void testIllegalAction() throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        Assert.assertFalse(processDefinition.isSuspended());
        ObjectNode requestNode = objectMapper.createObjectNode();
        requestNode.put("action", "unexistingaction");
        HttpPut httpPut = new HttpPut(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_PROCESS_DEFINITION, processDefinition.getId()))));
        httpPut.setEntity(new StringEntity(requestNode.toString()));
        CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_BAD_REQUEST);
        closeResponse(response);
    }

    @Test
    @Deployment(resources = { "org/flowable/rest/service/api/repository/oneTaskProcess.bpmn20.xml" })
    public void testGetProcessDefinitionResourceData() throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        HttpGet httpGet = new HttpGet(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_PROCESS_DEFINITION_RESOURCE_CONTENT, processDefinition.getId()))));
        CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_OK);
        // Check "OK" status
        String content = IOUtils.toString(response.getEntity().getContent());
        closeResponse(response);
        Assert.assertNotNull(content);
        Assert.assertTrue(content.contains("The One Task Process"));
    }

    @Test
    @Deployment(resources = { "org/flowable/rest/service/api/repository/oneTaskProcess.bpmn20.xml" })
    public void testGetProcessDefinitionModel() throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        HttpGet httpGet = new HttpGet(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_PROCESS_DEFINITION_MODEL, processDefinition.getId()))));
        CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_OK);
        // Check "OK" status
        JsonNode resultNode = objectMapper.readTree(response.getEntity().getContent());
        closeResponse(response);
        Assert.assertNotNull(resultNode);
        JsonNode processes = resultNode.get("processes");
        Assert.assertNotNull(processes);
        Assert.assertTrue(processes.isArray());
        Assert.assertEquals(1, processes.size());
        Assert.assertEquals("oneTaskProcess", processes.get(0).get("id").textValue());
    }

    /**
     * Test getting model for an unexisting process-definition .
     */
    @Test
    public void testGetModelForUnexistingProcessDefinition() throws Exception {
        HttpGet httpGet = new HttpGet(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_PROCESS_DEFINITION_MODEL, "unexisting"))));
        CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_NOT_FOUND);
        closeResponse(response);
    }

    /**
     * Test getting resource content for an unexisting process-definition .
     */
    @Test
    public void testGetResourceContentForUnexistingProcessDefinition() throws Exception {
        HttpGet httpGet = new HttpGet(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_PROCESS_DEFINITION_RESOURCE_CONTENT, "unexisting"))));
        CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_NOT_FOUND);
        closeResponse(response);
    }

    /**
     * Test activating a suspended process definition delayed. POST repository/process-definitions/{processDefinitionId}
     */
    @Test
    @Deployment(resources = { "org/flowable/rest/service/api/repository/oneTaskProcess.bpmn20.xml" })
    public void testUpdateProcessDefinitionCategory() throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        Assert.assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionCategory("OneTaskCategory").count());
        ObjectNode requestNode = objectMapper.createObjectNode();
        requestNode.put("category", "updatedcategory");
        HttpPut httpPut = new HttpPut(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_PROCESS_DEFINITION, processDefinition.getId()))));
        httpPut.setEntity(new StringEntity(requestNode.toString()));
        CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
        // Check "OK" status
        JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
        closeResponse(response);
        Assert.assertEquals("updatedcategory", responseNode.get("category").textValue());
        // Check actual entry in DB
        Assert.assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionCategory("updatedcategory").count());
    }
}
