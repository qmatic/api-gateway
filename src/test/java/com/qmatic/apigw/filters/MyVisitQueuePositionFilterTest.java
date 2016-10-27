package com.qmatic.apigw.filters;

import com.netflix.zuul.context.RequestContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MyVisitQueuePositionFilterTest {

    MyVisitQueuePositionFilter myVisitQueuePositionFilter = new MyVisitQueuePositionFilter();

    private final String VISIT_ID_1 = "1";
    private final String VISIT_ID_2 = "2";
    private final String VISIT_ID_3 = "3";
    private final String VISIT_ID_4 = "4";
    private final String VISIT_ID_5 = "5";
    private final String QUEUE_SIZE = "5";
    private String sequentialResponseBody =
            "[{\"ticketId\":\"A001\",\"visitId\":1,\"waitingTime\":1,\"totalWaitingTime\":0,\"appointmentId\":null,\"appointmentTime\":null,\"queueId\":1}," +
             "{\"ticketId\":\"A002\",\"visitId\":2,\"waitingTime\":2,\"totalWaitingTime\":0,\"appointmentId\":null,\"appointmentTime\":null,\"queueId\":1}," +
             "{\"ticketId\":\"A003\",\"visitId\":3,\"waitingTime\":3,\"totalWaitingTime\":0,\"appointmentId\":null,\"appointmentTime\":null,\"queueId\":1}," +
             "{\"ticketId\":\"A004\",\"visitId\":4,\"waitingTime\":4,\"totalWaitingTime\":0,\"appointmentId\":null,\"appointmentTime\":null,\"queueId\":1}," +
             "{\"ticketId\":\"A005\",\"visitId\":5,\"waitingTime\":5,\"totalWaitingTime\":0,\"appointmentId\":null,\"appointmentTime\":null,\"queueId\":1}]";

    private String nonSequentialResponseBody =
            "[{\"ticketId\":\"A001\",\"visitId\":1,\"waitingTime\":1,\"totalWaitingTime\":0,\"appointmentId\":null,\"appointmentTime\":null,\"queueId\":1}," +
                    "{\"ticketId\":\"A003\",\"visitId\":3,\"waitingTime\":3,\"totalWaitingTime\":0,\"appointmentId\":null,\"appointmentTime\":null,\"queueId\":1}," +
                    "{\"ticketId\":\"A002\",\"visitId\":2,\"waitingTime\":2,\"totalWaitingTime\":0,\"appointmentId\":null,\"appointmentTime\":null,\"queueId\":1}," +
                    "{\"ticketId\":\"A005\",\"visitId\":5,\"waitingTime\":5,\"totalWaitingTime\":0,\"appointmentId\":null,\"appointmentTime\":null,\"queueId\":1},"+
                    "{\"ticketId\":\"A004\",\"visitId\":4,\"waitingTime\":4,\"totalWaitingTime\":0,\"appointmentId\":null,\"appointmentTime\":null,\"queueId\":1}]";

    JSONArray sequentialResult;
    JSONArray nonSequentialResult;

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
        context.setResponseBody(sequentialResponseBody);
        context.setResponseStatusCode(HttpServletResponse.SC_OK);
        context.set("proxy","my_visit_queue_position");
        RequestContext.testSetCurrentContext(context);

        JSONObject obj = new JSONObject("{\"result\":" + sequentialResponseBody + "}");
        sequentialResult = obj.getJSONArray("result");
        obj = new JSONObject("{\"result\":" + nonSequentialResponseBody + "}");
        nonSequentialResult = obj.getJSONArray("result");
    }

    @Test
    public void testFilterShouldRun() {
        assertTrue(new MyVisitQueuePositionFilter().shouldFilter());
    }

    @Test
    public void testFilterRun() {
        new MyVisitQueuePositionFilter().run();
        assertTrue(RequestContext.getCurrentContext().getResponseBody().startsWith("{\"visit\":{\"position\":5,"));
        assertFalse(RequestContext.getCurrentContext().getResponseBody().startsWith("{\"visit\":{\"position\":1,"));
    }

    @Test
    public void getVisitJsonWithPosition() throws Exception {
        MyVisitQueuePositionFilter myVisitQueuePositionFilter = new MyVisitQueuePositionFilter();

        String visitOneShouldBeFirst = getJsonVisitWithPosition(VISIT_ID_1, QUEUE_SIZE);

        final String visitOneWithPosition = myVisitQueuePositionFilter.getVisitJsonWithPosition(VISIT_ID_1, sequentialResponseBody);

        assertTrue(visitOneWithPosition.equals(visitOneShouldBeFirst));
    }

    private String getJsonVisitWithPosition(String expectedPosition, String queueSize) {
        final int waitingTime = Integer.parseInt(expectedPosition);
        String comparatorForTesting =  "{\"visit\":" +
                "{\"position\":"+ expectedPosition + ",\"queueSize\":" + queueSize + ",\"appointmentTime\":null," +
                "\"waitingTime\":" + waitingTime + "," +
                "\"ticketId\":\"A001\",\"totalWaitingTime\":0,\"queueId\":1,\"appointmentId\":null,\"visitId\":1}}";

        return comparatorForTesting;
    }

    @Test
    public void getQueuingPositionWithVisitsInOriginalOrder() throws Exception {
        assertQueuingPositionOriginalOrder(5, sequentialResult);
    }

    private void assertQueuingPositionOriginalOrder(int queueSize, JSONArray result) throws Exception {
        int queuingPosition;
        for(int i = 1; i <= queueSize; ++i) {
            queuingPosition = myVisitQueuePositionFilter.getQueuingPosition(String.valueOf(i), result);
            assertTrue(queuingPosition == i);
        }
    }

    @Test
    public void getQueuingPositionWithVisitsNonOriginalOrder() throws Exception {
        final int visitOneQueuingPosition = 1;
        final int visitTwoQueuingPosition = 3;
        final int visitThreeQueuingPosition = 2;
        final int visitFourQueuingPosition = 5;
        final int visitFiveQueueingPosition = 4;

        int queuingPosition;
        queuingPosition = myVisitQueuePositionFilter.getQueuingPosition(VISIT_ID_1, nonSequentialResult);
        assertTrue(queuingPosition == visitOneQueuingPosition);

        queuingPosition = myVisitQueuePositionFilter.getQueuingPosition(VISIT_ID_2, nonSequentialResult);
        assertTrue(queuingPosition == visitTwoQueuingPosition);

        queuingPosition = myVisitQueuePositionFilter.getQueuingPosition(VISIT_ID_3, nonSequentialResult);
        assertTrue(queuingPosition == visitThreeQueuingPosition);

        queuingPosition = myVisitQueuePositionFilter.getQueuingPosition(VISIT_ID_4, nonSequentialResult);
        assertTrue(queuingPosition == visitFourQueuingPosition);

        queuingPosition = myVisitQueuePositionFilter.getQueuingPosition(VISIT_ID_5, nonSequentialResult);
        assertTrue(queuingPosition == visitFiveQueueingPosition);
    }
}
