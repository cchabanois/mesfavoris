package mesfavoris.gdrive.test;

import java.io.IOException;
import java.net.MalformedURLException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;

import mesfavoris.gdrive.connection.auth.IAuthorizationCodeInstalledAppProvider;

public class HtmlUnitAuthorizationCodeInstalledApp extends AuthorizationCodeInstalledApp {
	private static final int WAIT_DELAY_MS = 8000;
	private final String userName;
	private final String password;
	private final IProgressMonitor monitor;
	
	public HtmlUnitAuthorizationCodeInstalledApp(AuthorizationCodeFlow flow, VerificationCodeReceiver receiver, IProgressMonitor monitor, String userName, String password) {
		super(flow, receiver);
		this.monitor = monitor;
		this.userName = userName;
		this.password = password;
	}

	@Override
	protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) throws IOException {
		monitor.subTask("Please open the following address in your browser:"
				+ authorizationUrl);
		try (final WebClient webClient = new WebClient()) {
			HtmlPage allowAccessPage = signIn(webClient, authorizationUrl.build());
			HtmlPage tokenPage = allowAccess(webClient, allowAccessPage);
		}
	}
	
	private HtmlPage signIn(WebClient webClient, String authorizationUrl) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		HtmlPage page = webClient.getPage(authorizationUrl);
		webClient.waitForBackgroundJavaScriptStartingBefore(8000);
		HtmlForm form = (HtmlForm) page.getElementById("gaia_loginform");
		HtmlSubmitInput signInButton = (HtmlSubmitInput) form.getInputByName("signIn");
		HtmlTextInput userNameField = (HtmlTextInput) form.getInputByName("Email");
		userNameField.setValueAttribute(userName);
		page = signInButton.click();
		webClient.waitForBackgroundJavaScriptStartingBefore(8000);
		form = (HtmlForm) page.getElementById("gaia_loginform");
		HtmlPasswordInput passwordField = (HtmlPasswordInput) form.getInputByName("Passwd");
		signInButton = (HtmlSubmitInput) form.getInputByName("signIn");
		passwordField.setValueAttribute(password);
		HtmlPage allowAccessPage = signInButton.click();
		webClient.waitForBackgroundJavaScriptStartingBefore(8000);
		return allowAccessPage;
	}
	
	private HtmlPage allowAccess(WebClient webClient, HtmlPage allowAccessPage) throws IOException {
		HtmlButton allowAccessButton = (HtmlButton) allowAccessPage.getElementById("submit_approve_access");
		if (allowAccessButton == null) {
			throw new RuntimeException("Cannot find allow access button in html page :\n"+allowAccessPage.asXml());
		}
		webClient.waitForBackgroundJavaScriptStartingBefore(WAIT_DELAY_MS);
		HtmlPage tokenPage = allowAccessButton.click();
		return tokenPage;
	}
	
	public static class Provider implements IAuthorizationCodeInstalledAppProvider {
		private final String userName;
		private final String password;
		
		public Provider(String userName, String password) {
			this.userName = userName;
			this.password = password;
		}
		
		@Override
		public AuthorizationCodeInstalledApp get(AuthorizationCodeFlow flow, VerificationCodeReceiver receiver,
				IProgressMonitor monitor) {
			return new HtmlUnitAuthorizationCodeInstalledApp(flow, receiver, monitor, userName, password);
		}
		
	}

}