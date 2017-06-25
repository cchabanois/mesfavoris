package mesfavoris.gdrive.connection;

import java.io.IOException;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpBackOffIOExceptionHandler;
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.util.MockBackOff;
import com.google.api.client.util.ExponentialBackOff;

public class GDriveBackOffHttpRequestInitializer implements HttpRequestInitializer {
	private static final String USER_RATE_LIMIT_EXCEEDED_REASON = "userRateLimitExceeded";
	private static final String RATE_LIMIT_EXCEEDED_REASON = "rateLimitExceeded";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private final Credential credential;

	public GDriveBackOffHttpRequestInitializer(Credential credential) {
		this.credential = credential;
	}

	@Override
	public void initialize(HttpRequest request) throws IOException {
		request.setInterceptor(credential);

		// Exponential Back-off for 5xx response and 403 rate limit exceeded
		// error
		HttpBackOffUnsuccessfulResponseHandler backOffHandler = getHttpBackOffUnsuccessfulResponseHandler();

		request.setUnsuccessfulResponseHandler(
				new CompositeHttpUnsuccessfulResponseHandler(credential, backOffHandler));

		// Back-off for socket connection error
		request.setIOExceptionHandler(getHttpBackOffIOExceptionHandler());
	}

	private HttpBackOffUnsuccessfulResponseHandler getHttpBackOffUnsuccessfulResponseHandler() {
		HttpBackOffUnsuccessfulResponseHandler backOffHandler = new HttpBackOffUnsuccessfulResponseHandler(
				new ExponentialBackOff.Builder().setInitialIntervalMillis(200).setMaxElapsedTimeMillis(60000).build());
		backOffHandler.setBackOffRequired(
				(HttpResponse response) -> response.getStatusCode() / 100 == 5 || isRateLimitExceeded(response));
		return backOffHandler;
	}

	private HttpBackOffIOExceptionHandler getHttpBackOffIOExceptionHandler() {
		MockBackOff backOff = new MockBackOff();
		backOff.setBackOffMillis(200);
		backOff.setMaxTries(5);
		return new HttpBackOffIOExceptionHandler(backOff);
	}

	private boolean isRateLimitExceeded(HttpResponse response) {
		return response.getStatusCode() == HttpStatusCodes.STATUS_CODE_FORBIDDEN
				&& isRateLimitExceeded(GoogleJsonResponseException.from(JSON_FACTORY, response));
	}

	private boolean isRateLimitExceeded(GoogleJsonResponseException ex) {
		if (ex.getDetails() == null || ex.getDetails().getErrors() == null || ex.getDetails().getErrors().size() == 0) {
			return false;
		}
		String reason = ex.getDetails().getErrors().get(0).getReason();
		return RATE_LIMIT_EXCEEDED_REASON.equals(reason) || USER_RATE_LIMIT_EXCEEDED_REASON.equals(reason);
	}

	private static class CompositeHttpUnsuccessfulResponseHandler implements HttpUnsuccessfulResponseHandler {
		private final HttpUnsuccessfulResponseHandler[] handlers;

		public CompositeHttpUnsuccessfulResponseHandler(HttpUnsuccessfulResponseHandler... handlers) {
			this.handlers = handlers;
		}

		@Override
		public boolean handleResponse(HttpRequest request, HttpResponse response, boolean supportsRetry)
				throws IOException {
			for (HttpUnsuccessfulResponseHandler handler : handlers) {
				if (handler.handleResponse(request, response, supportsRetry)) {
					return true;
				}
			}
			return false;
		}

	}

}
