package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.service.webserver.WebServerStateRetrievalScheduledTaskHandler;
import com.siemens.cto.aem.service.webserver.WebServerStateSetterWorker;
import com.siemens.cto.aem.si.ssl.hc.HttpClientRequestFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link com.siemens.cto.aem.service.webserver.WebServerStateRetrievalScheduledTaskHandler}.
 *
 * Created by Z003BPEJ on 6/19/2015.
 */
public class WebServerStateRetrievalScheduledTaskHandlerTest {

    private WebServerStateRetrievalScheduledTaskHandler webServerStateRetrievalScheduledTaskHandler;
    private HttpClientRequestFactory httpClientRequestFactory;
    private WebServer webServer1;
    private WebServer webServer2;
    private List<WebServer> webServers;
    private ClientHttpRequest request;
    private ClientHttpResponse clientHttpResponse;
    private WebServerService webServerService;
    private WebServerStateSetterWorker webServerStateSetterWorker;

    @Before
    public void setup() throws IOException {
        webServer1 = new WebServer(new Identifier<WebServer>(1L),
                                   new ArrayList<Group>(),
                                   null,
                                   "localhost",
                                   80,
                                   null,
                                   new Path("/stp.png"),
                                   null,
                                   null,
                                   null);

        webServer2 = new WebServer(new Identifier<WebServer>(2L),
                                  new ArrayList<Group>(),
                                  null,
                                  "localhost",
                                  90,
                                  null,
                                  new Path("/stp.png"),
                                  null,
                                  null,
                                  null);

        webServers = new ArrayList<>();
        webServers.add(webServer1);
        webServers.add(webServer2);

        httpClientRequestFactory = mock(HttpClientRequestFactory.class);
        request = mock(ClientHttpRequest.class);
        clientHttpResponse = mock(ClientHttpResponse.class);
        httpClientRequestFactory = mock(HttpClientRequestFactory.class);
    }

    @Test
    @Ignore
    // TODO: Fix this!
    public void testWebServerStatePollerTaskExecute() throws IOException {
        when(httpClientRequestFactory.createRequest(any(URI.class), eq(HttpMethod.GET))).thenReturn(request);
        when(request.execute()).thenReturn(clientHttpResponse);
        when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.OK);

        webServerStateRetrievalScheduledTaskHandler = new WebServerStateRetrievalScheduledTaskHandler(webServerService,
                                                                webServerStateSetterWorker);

        webServerStateRetrievalScheduledTaskHandler.execute();

//        verify(webServerStateServiceFacade, times(2)).setState(any(Identifier.class),
//                                                               eq(WebServerReachableState.WS_REACHABLE),
//                                                               any(DateTime.class));
        verify(clientHttpResponse, times(2)).close();
    }

    @Test
    @Ignore
    // TODO: Fix this!
    public void testWebServerStatePollerTaskExecuteResultIsUnreachable() throws IOException {
        when(httpClientRequestFactory.createRequest(any(URI.class), eq(HttpMethod.GET))).thenReturn(request);
        when(request.execute()).thenReturn(clientHttpResponse);
        when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);

        webServerStateRetrievalScheduledTaskHandler = new WebServerStateRetrievalScheduledTaskHandler(webServerService,
                                                                webServerStateSetterWorker);

        webServerStateRetrievalScheduledTaskHandler.execute();

//        verify(webServerStateServiceFacade, times(2)).setState(any(Identifier.class),
//                eq(WebServerReachableState.WS_UNREACHABLE),
//                any(DateTime.class));
        verify(clientHttpResponse, times(2)).close();
    }

    @Test
    @Ignore
    // TODO: Fix this!
    public void testWebServerStatePollerTaskExecuteResultIsIOException() throws IOException {
        when(httpClientRequestFactory.createRequest(any(URI.class), eq(HttpMethod.GET))).thenReturn(request);
        when(request.execute()).thenThrow(IOException.class);

        webServerStateRetrievalScheduledTaskHandler = new WebServerStateRetrievalScheduledTaskHandler(webServerService,
                                                                webServerStateSetterWorker);

        webServerStateRetrievalScheduledTaskHandler.execute();

//        verify(webServerStateServiceFacade, times(2)).setState(any(Identifier.class),
//                eq(WebServerReachableState.WS_UNREACHABLE),
//                any(DateTime.class));
        verify(clientHttpResponse, times(0)).close();
    }

}
