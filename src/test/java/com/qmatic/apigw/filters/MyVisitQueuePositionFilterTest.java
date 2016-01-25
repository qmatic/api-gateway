package com.qmatic.apigw.filters;

import com.netflix.util.Pair;
import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.GatewayConstants;
import com.qmatic.apigw.filters.OrchestraResponseErrorFilter;
import org.springframework.http.HttpStatus;
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

import static org.testng.AssertJUnit.assertTrue;

/**
 * Created by davtol on 2016-01-13.
 */
public class MyVisitQueuePositionFilterTest {

    private Pair<String, String> eMessage = new Pair<>(GatewayConstants.ERROR_MESSAGE, "ERROR: this line should be stopped filtered");

    private String responseBody =
            "[{\"ticketId\":\"A001\",\"visitId\":1,\"waitingTime\":1,\"totalWaitingTime\":0,\"appointmentId\":null,\"appointmentTime\":null,\"queueId\":1}," +
             "{\"ticketId\":\"A002\",\"visitId\":2,\"waitingTime\":2,\"totalWaitingTime\":0,\"appointmentId\":null,\"appointmentTime\":null,\"queueId\":1}," +
             "{\"ticketId\":\"A003\",\"visitId\":3,\"waitingTime\":3,\"totalWaitingTime\":0,\"appointmentId\":null,\"appointmentTime\":null,\"queueId\":1}," +
             "{\"ticketId\":\"A004\",\"visitId\":4,\"waitingTime\":4,\"totalWaitingTime\":0,\"appointmentId\":null,\"appointmentTime\":null,\"queueId\":1}," +
             "{\"ticketId\":\"A005\",\"visitId\":5,\"waitingTime\":5,\"totalWaitingTime\":0,\"appointmentId\":null,\"appointmentTime\":null,\"queueId\":1}]";

    @BeforeMethod
    public void setUp() throws Exception {
        Map<String, List<String>> queryParams = new HashMap<>();
        List<String> queryParamValues = new ArrayList<String>();
        queryParamValues.add("5");
        queryParams.put("visitId", queryParamValues);

        RequestContext context = new RequestContext();
        context.setRequest(new MockHttpServletRequest());
        context.setResponse(new MockHttpServletResponse());
        context.setRequestQueryParams(queryParams);
        context.setResponseBody(responseBody);
        context.setResponseStatusCode(HttpServletResponse.SC_OK);
        context.set("proxy","my_visit_queue_position");
        RequestContext.testSetCurrentContext(context);
    }

    @Test
    public void testFilterShouldRun() {
        Assert.assertTrue(new MyVisitQueuePositionFilter().shouldFilter());
    }

    @Test
    public void testFilterRun() {
        new MyVisitQueuePositionFilter().run();
        Assert.assertTrue(RequestContext.getCurrentContext().getResponseBody().startsWith("{\"visit\":{\"position\":5,"));
        Assert.assertFalse(RequestContext.getCurrentContext().getResponseBody().startsWith("{\"visit\":{\"position\":1,"));
    }
}
