package com.qmatic.apigw.filters;

import com.netflix.zuul.context.RequestContext;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class MyVisitCurrentStatusPreFilterTest {

    @Test
    public void filterShouldRun() {
        RequestContext ctx = new RequestContext();
        ctx.set(FilterConstants.PROXY, FilterConstants.MY_VISIT_CURRENT_STATUS);
        RequestContext.testSetCurrentContext(ctx);
        assertTrue(new MyVisitCurrentStatusPreFilter().shouldFilter());
    }
}
