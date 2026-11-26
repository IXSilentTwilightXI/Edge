package org.asf.edge.modules.gridapi.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.asf.connective.ConnectiveHttpServer;
import org.asf.connective.ContentSource;
import org.asf.connective.RemoteClient;
import org.asf.connective.handlers.HttpPushHandler;
import org.asf.connective.handlers.HttpRequestHandler;
import org.asf.connective.objects.HttpRequest;
import org.asf.connective.objects.HttpResponse;

public class CaseInsensitiveContentSource extends ContentSource {

	private String sanitizePath(String path) {
		if (path.contains("\\"))
			path = path.replace("\\", "/");
		while (path.startsWith("/"))
			path = path.substring(1);
		while (path.endsWith("/"))
			path = path.substring(0, path.length() - 1);
		while (path.contains("//"))
			path = path.replace("//", "/");
		if (!path.startsWith("/"))
			path = "/" + path;
		return path;
	}

	@Override
	public boolean process(String path, HttpRequest request, HttpResponse response, RemoteClient client,
			ConnectiveHttpServer server) throws IOException {
		// Load handlers
		ArrayList<HttpRequestHandler> reqProcessors = new ArrayList<HttpRequestHandler>(
				Arrays.asList(server.getRequestProcessors()));
		ArrayList<HttpPushHandler> pushProcessors = new ArrayList<HttpPushHandler>(
				Arrays.asList(server.getPushProcessors()));
		boolean compatible = false;
		for (HttpPushHandler proc : pushProcessors) {
			if (proc.supportsNonPush()) {
				reqProcessors.add(proc);
			}
		}

		// Find handler
		if (request.hasRequestBody()) {
			HttpPushHandler impl = null;
			for (HttpPushHandler proc : pushProcessors) {
				if (!proc.supportsChildPaths()) {
					String url = request.getRequestPath();
					if (!url.endsWith("/"))
						url += "/";

					String supportedURL = proc.path();
					if (!supportedURL.endsWith("/"))
						supportedURL += "/";

					if (url.equalsIgnoreCase(supportedURL)) {
						compatible = true;
						impl = proc;
						break;
					}
				}
			}
			if (!compatible) {
				pushProcessors.sort((t1, t2) -> {
					return -Integer.compare(sanitizePath(t1.path()).split("/").length,
							sanitizePath(t2.path()).split("/").length);
				});
				for (HttpPushHandler proc : pushProcessors) {
					if (proc.supportsChildPaths()) {
						String url = request.getRequestPath();
						if (!url.endsWith("/"))
							url += "/";

						String supportedURL = sanitizePath(proc.path());
						if (!supportedURL.endsWith("/"))
							supportedURL += "/";

						if (url.toLowerCase().startsWith(supportedURL.toLowerCase())) {
							compatible = true;
							impl = proc;
							break;
						}
					}
				}
			}
			if (compatible) {
				HttpPushHandler processor = impl.instantiate(server, request, response);
				processor.handle(path, request.getRequestMethod(), client, request.getHeaderValue("Content-Type"));
			}
		} else {
			HttpRequestHandler impl = null;
			for (HttpRequestHandler proc : reqProcessors) {
				if (!proc.supportsChildPaths()) {
					String url = request.getRequestPath();
					if (!url.endsWith("/"))
						url += "/";

					String supportedURL = proc.path();
					if (!supportedURL.endsWith("/"))
						supportedURL += "/";

					if (url.equalsIgnoreCase(supportedURL)) {
						compatible = true;
						impl = proc;
						break;
					}
				}
			}
			if (!compatible) {
				reqProcessors.sort((t1, t2) -> {
					return -Integer.compare(sanitizePath(t1.path()).split("/").length,
							sanitizePath(t2.path()).split("/").length);
				});
				for (HttpRequestHandler proc : reqProcessors) {
					if (proc.supportsChildPaths()) {
						String url = request.getRequestPath();
						if (!url.endsWith("/"))
							url += "/";

						String supportedURL = sanitizePath(proc.path());
						if (!supportedURL.endsWith("/"))
							supportedURL += "/";

						if (url.toLowerCase().startsWith(supportedURL.toLowerCase())) {
							compatible = true;
							impl = proc;
							break;
						}
					}
				}
			}
			if (compatible) {
				HttpRequestHandler processor = impl.instantiate(server, request, response);
				processor.handle(path, request.getRequestMethod(), client);
			}
		}

		// Return
		return compatible;
	}

}
