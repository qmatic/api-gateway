package com.qmatic.apigw.filters;

import com.netflix.util.Pair;
import com.netflix.zuul.context.RequestContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyVisitLastQueueEventFilterTest {

    private String responseBody =
            "[{\"id\":85,\"eventName\":\"VISIT_CREATE\",\"eventTime\":\"2016-01-11T13:47:19.584+0100\",\"unitType\":\"ENTRY_POINT\",\"unitId\":510000000001,\"userId\":null,\"parameterMap\":{\"lastName\":null,\"queueId\":110000000001,\"serviceTargetTransTime\":300,\"serviceExtName\":\"Service 1\",\"serviceExtDesc\":null,\"type\":\"ENTRY_POINT\",\"appointmentStartTime\":null,\"serviceIntName\":\"Service 1\",\"serviceIntDesc\":null,\"ticket\":\"A007\",\"queueType\":\"QUEUE\",\"unitName\":\"VisitApp\",\"userId\":0,\"service\":1,\"serviceOrigId\":1,\"queueName\":\"Queue 1\",\"appointmentId\":null,\"firstName\":null,\"user\":\"mobile\",\"queueServiceLevel\":5}}," +
             "{\"id\":181,\"eventName\":\"VISIT_NEXT\",\"eventTime\":\"2016-01-13T17:09:34.605+0100\",\"unitType\":\"SERVICE_POINT\",\"unitId\":120000000001,\"userId\":1,\"parameterMap\":{\"servicePointName\":\"Counter 1\",\"lastName\":\"Administrator\",\"workProfileName\":\"Longest waiting time all queues\",\"servicePointLogicId\":1,\"workProfile\":4,\"queueId\":110000000001,\"queueType\":\"QUEUE\",\"userId\":1,\"service\":1,\"servicePointId\":120000000001,\"queueName\":\"Queue 1\",\"serviceOrigId\":1,\"firstName\":\"Super\",\"user\":\"superadmin\",\"queueServiceLevel\":5}}," +
             "{\"id\":184,\"eventName\":\"VISIT_CALL\",\"eventTime\":\"2016-01-13T17:09:35.006+0100\",\"unitType\":\"SERVICE_POINT\",\"unitId\":120000000001,\"userId\":null,\"parameterMap\":{\"servicePointName\":\"Counter 1\",\"lastName\":\"Administrator\",\"workProfileName\":\"Longest waiting time all queues\",\"servicePointLogicId\":1,\"workProfile\":4,\"queueId\":110000000001,\"queueType\":\"QUEUE\",\"userId\":1,\"service\":1,\"servicePointId\":120000000001,\"queueName\":\"Queue 1\",\"serviceOrigId\":1,\"firstName\":\"Super\",\"user\":\"superadmin\",\"queueServiceLevel\":5}}]";


    @BeforeMethod
    public void setUp() throws Exception {
        Map<String, List<String>> queryParams = new HashMap<>();
        List<String> queryParamValues = new ArrayList<>();
        queryParamValues.add("5");
        queryParams.put("visitId", queryParamValues);

        RequestContext context = new RequestContext();
        context.setRequest(new MockHttpServletRequest());
        context.setResponse(new MockHttpServletResponse());
        context.setRequestQueryParams(queryParams);
        context.setResponseBody(responseBody);
        context.setResponseStatusCode(HttpServletResponse.SC_OK);
        context.set("proxy","my_visit_last_queue_event");
        RequestContext.testSetCurrentContext(context);
    }

    @Test
    public void testFilterShouldRun() {
        Assert.assertTrue(new MyVisitLastQueueEventFilter().shouldFilter());
    }

    @Test
    public void testFilterRun() {
        new MyVisitLastQueueEventFilter().run();
        Assert.assertTrue(RequestContext.getCurrentContext().getResponseBody().startsWith("{\"lastEvent\":{\"id\":184"));
    }
}
