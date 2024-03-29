package mesfavoris.gdrive.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlEmailInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.javascript.host.event.MouseEvent;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;

import mesfavoris.gdrive.connection.auth.IAuthorizationCodeInstalledAppProvider;

/*
 * This does not work anymore : see https://gist.github.com/ikegami-yukino/51b247080976cb41fe93
 */
public class HtmlUnitAuthorizationCodeInstalledApp extends AuthorizationCodeInstalledApp {
	private static final int WAIT_DELAY_MS = 15000;
	private final String userName;
	private final String password;
	private final Optional<String> recoveryEmail;
	private final IProgressMonitor monitor;
	private IAuthorizationListener authorizationListener;

	// public static final String INTERNET_EXPLORER = "Microsoft Internet Explorer";
	public static final BrowserVersion INTERNET_EXPLORER_6 = new BrowserVersion.BrowserVersionBuilder(
			BrowserVersion.INTERNET_EXPLORER).setUserAgent("Mozilla/4.0 (compatible; MSIE 6.0; Windows 98)")
					.setApplicationVersion("6.0").build();

	static {
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.SEVERE);
	}

	public HtmlUnitAuthorizationCodeInstalledApp(AuthorizationCodeFlow flow, VerificationCodeReceiver receiver,
			IProgressMonitor monitor, String userName, String password, Optional<String> recoveryEmail) {
		super(flow, receiver);
		this.monitor = monitor;
		this.userName = userName;
		this.password = password;
		this.recoveryEmail = recoveryEmail;
	}

	public void setAuthorizationListener(IAuthorizationListener authorizationListener) {
		this.authorizationListener = authorizationListener;
	}

	@Override
	protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) throws IOException {
		monitor.subTask("Please open the following address in your browser:" + authorizationUrl);

		// use an old browser so that we see the old sign-in page
		try (final WebClient webClient = new WebClient(INTERNET_EXPLORER_6)) {
			webClient.getOptions().setJavaScriptEnabled(true);
			webClient.setAjaxController(new NicelyResynchronizingAjaxController());
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			HtmlPage nextPage = signIn(webClient, authorizationUrl.build());
			if (isSelectChallengePage(nextPage)) {
				nextPage = selectEmailRecoveryAsSignInChallenge(webClient, nextPage);
				System.out.println("Selected recovery email confirmation as challenge");
				nextPage = confirmRecoveryEmail(webClient, nextPage);
				System.out.println("Used recovery email to confirm it's us");
				allowAccess(webClient, nextPage);
			} else if (isConfirmRecoveryEmailPage(nextPage)) {
				nextPage = confirmRecoveryEmail(webClient, nextPage);
				System.out.println("Used recovery email to confirm it's us");
				allowAccess(webClient, nextPage);
			} else {
				allowAccess(webClient, nextPage);
			}
		}
		if (authorizationListener != null) {
			authorizationListener.onAuthorization();
		}
	}

	private HtmlPage confirmRecoveryEmail(WebClient webClient, HtmlPage htmlPage) throws IOException {
		List<HtmlForm> forms = htmlPage.getForms();
		HtmlForm challengeForm = forms.stream().filter(
				form -> "challenge".equals(form.getId()) && "/signin/challenge/kpe/2".equals(form.getActionAttribute()))
				.findFirst().get();
		HtmlInput htmlInput = challengeForm.getInputByName("email");
		htmlInput.setValueAttribute(recoveryEmail.get());
		HtmlSubmitInput signInButton = (HtmlSubmitInput) challengeForm.getInputByValue("Done");
		HtmlPage nextPage = signInButton.click();
		webClient.waitForBackgroundJavaScriptStartingBefore(WAIT_DELAY_MS);
		return nextPage;
	}

	private boolean isConfirmRecoveryEmailPage(HtmlPage htmlPage) {
		List<HtmlForm> forms = htmlPage.getForms();
		Optional<HtmlForm> challengeForm = forms.stream().filter(
				form -> "challenge".equals(form.getId()) && "/signin/challenge/kpe/2".equals(form.getActionAttribute()))
				.findFirst();
		return challengeForm.isPresent();
	}

	private boolean isSelectChallengePage(HtmlPage htmlPage) {
		return htmlPage.getElementById("challengePickerList") != null;
	}

	private HtmlPage selectEmailRecoveryAsSignInChallenge(WebClient webClient, HtmlPage signInChallengePage)
			throws IOException {
		List<HtmlForm> forms = signInChallengePage.getForms();
		Optional<HtmlForm> kpeForm = forms.stream()
				.filter(form -> "/signin/challenge/kpe/2".equals(form.getActionAttribute())).findFirst();
		if (!kpeForm.isPresent()) {
			throw new RuntimeException(
					"Cannot find recovery by email form in html page :\n" + signInChallengePage.asXml());
		}
		HtmlButton button = (HtmlButton) kpeForm.get().getElementsByTagName("button").get(0);
		HtmlPage htmlPage = button.click();
		webClient.waitForBackgroundJavaScriptStartingBefore(WAIT_DELAY_MS);
		return htmlPage;
	}

	private HtmlPage signIn(WebClient webClient, String authorizationUrl)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		HtmlPage page = webClient.getPage(authorizationUrl);
		webClient.waitForBackgroundJavaScriptStartingBefore(WAIT_DELAY_MS);
		HtmlForm form = (HtmlForm) page.getElementById("gaia_loginform");
		if (form == null) {
			throw new RuntimeException("Cannot find login form :\n" + authorizationUrl + "\n" + page.asXml());
		}
		HtmlSubmitInput signInButton = (HtmlSubmitInput) form.getInputByName("signIn");
		HtmlEmailInput userNameField = (HtmlEmailInput) form.getInputByName("Email");
		userNameField.setValueAttribute(userName);
		page = signInButton.click();
		webClient.waitForBackgroundJavaScriptStartingBefore(WAIT_DELAY_MS);
		form = (HtmlForm) page.getElementById("gaia_loginform");
		HtmlPasswordInput passwordField = (HtmlPasswordInput) form.getInputByName("Passwd");
		signInButton = (HtmlSubmitInput) form.getInputByName("signIn");
		passwordField.setValueAttribute(password);
		HtmlPage allowAccessPage = signInButton.click();
		webClient.waitForBackgroundJavaScriptStartingBefore(WAIT_DELAY_MS);
		return allowAccessPage;
	}

	private HtmlPage allowAccess(WebClient webClient, HtmlPage allowAccessPage) throws IOException {
		HtmlButton allowAccessButton = (HtmlButton) allowAccessPage.getElementById("submit_approve_access");
		if (allowAccessButton == null) {
			throw new RuntimeException("Cannot find allow access button in html page :\n" + allowAccessPage.asXml());
		}
		webClient.waitForBackgroundJavaScriptStartingBefore(WAIT_DELAY_MS);
		// allowAccessButton.click() does not work because
		// allowAccessButton.isVisible() is false
		// for some reason (click() was working with htmlunit 2.23)
		HtmlPage tokenPage = clickButtonIgnoringVisibility(allowAccessButton);
		return tokenPage;
	}

	private HtmlPage clickButtonIgnoringVisibility(HtmlButton htmlButton) throws IOException {
		MouseEvent event = new MouseEvent(htmlButton, MouseEvent.TYPE_CLICK, false, false, false,
				MouseEvent.BUTTON_LEFT);
		return htmlButton.click(event, true);
	}

	public static class Provider implements IAuthorizationCodeInstalledAppProvider {
		private final String userName;
		private final String password;
		private final Optional<String> recoveryEmail;
		private IAuthorizationListener authorizationListener;

		public Provider(String userName, String password) {
			this.userName = userName;
			this.password = password;
			this.recoveryEmail = Optional.empty();
		}

		public Provider(String userName, String password, Optional<String> recoveryEmail) {
			this.userName = userName;
			this.password = password;
			this.recoveryEmail = recoveryEmail;
		}

		public void setAuthorizationListener(IAuthorizationListener authorizationListener) {
			this.authorizationListener = authorizationListener;
		}

		@Override
		public AuthorizationCodeInstalledApp get(AuthorizationCodeFlow flow, VerificationCodeReceiver receiver,
				IProgressMonitor monitor) {
			HtmlUnitAuthorizationCodeInstalledApp app = new HtmlUnitAuthorizationCodeInstalledApp(flow, receiver,
					monitor, userName, password, recoveryEmail);
			if (authorizationListener != null) {
				app.setAuthorizationListener(authorizationListener);
			}
			return app;
		}

	}

}