package mesfavoris.gdrive.operations;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public class GetFileIdFromUrlOperation {

	public Optional<String> getFileId(String url) {
//		https://docs.google.com/a/mycompany.com/document/d/0B7a_ei8brT1TMy1CQ0o5NmZQNEE/edit?usp=sharing
//		https://drive.google.com/file/d/0B7a_ei8brT1TMy1CQ0o5NmZQNEE/view?usp=sharing
//		https://drive.google.com/open?id=0B7a_ei8brT1TMy1CQ0o5NmZQNEE
		try {
			URI uri = new URI(url);
			if (uri.getHost() == null || !uri.getHost().endsWith(".google.com")) {
				return Optional.empty();
			}
			List<NameValuePair> params = URLEncodedUtils.parse(uri, "UTF-8");
			Optional<String> idValue = getParameterValue(params, "id");
			if (idValue.isPresent()) {
				return idValue;
			}
			Pattern pattern = Pattern.compile("/d/(.{25,})/");
			Matcher matcher = pattern.matcher(uri.getPath());
			if (!matcher.find()) {
				return Optional.empty();
			}
			return Optional.of(matcher.group(1));
		} catch (URISyntaxException e) {
			return Optional.empty();
		}
	}
	
	private Optional<String> getParameterValue(List<NameValuePair> params, String name) {
		return params.stream().filter(param->name.equals(param.getName())).map(param->param.getValue()).findFirst();
	}
	
}
