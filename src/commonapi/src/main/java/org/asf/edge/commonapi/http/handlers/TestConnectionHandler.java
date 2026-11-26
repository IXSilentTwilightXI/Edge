package org.asf.edge.commonapi.http.handlers;

import java.io.IOException;

import org.asf.connective.RemoteClient;
import org.asf.connective.handlers.HttpRequestHandler;

public class TestConnectionHandler extends HttpRequestHandler {

	@Override
	public String path() {
		return "/testconnection";
	}

	@Override
	public void handle(String path, String method, RemoteClient client) throws IOException {
		setResponseContent("text/plain", "OK");
		setResponseStatus(200, "OK");
	}

	@Override
	public HttpRequestHandler createNewInstance() {
		return new TestConnectionHandler();
	}

}
