package mesfavoris.gdrive.connection.auth;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;

public interface IAuthorizationCodeInstalledAppProvider {

	AuthorizationCodeInstalledApp get(AuthorizationCodeFlow flow,
			VerificationCodeReceiver receiver, IProgressMonitor monitor);
	
}
