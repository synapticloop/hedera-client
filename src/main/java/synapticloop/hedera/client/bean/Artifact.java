package synapticloop.hedera.client.bean;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import synapticloop.hedera.client.exception.HederaException;
import synapticloop.hedera.client.util.HederaUtils;
import synapticloop.hedera.client.util.SimpleLogger;
import synapticloop.hedera.client.util.SimpleLogger.LoggerType;

public class Artifact {
	private boolean found = false;
	private List<String> messages = new ArrayList<String>();
	private String urlPath = null;

	private String url = null;

	private String repository = null;

	private String dependency = null;

	private static final Set<String> ALLOWABLE_DEPENDENCIES = new HashSet<String>();
	static {
		ALLOWABLE_DEPENDENCIES.add("pom.xml");
		ALLOWABLE_DEPENDENCIES.add("hedera.xml");
	}

	private List<String> scopes = new ArrayList<String>();
	private NamedNodeMap attributes;

	public Artifact(Node node) throws HederaException {
		attributes = node.getAttributes();


		// at this point it is either a simple url
		this.url = HederaUtils.getNodeValue(attributes, "url");

		this.dependency = HederaUtils.getNodeValue(attributes, "dependency");


		// or a repository - with possible tokens
		this.repository = HederaUtils.getNodeValue(attributes, "repository");

		// now for some validation
		validateDependencies(dependency);
		validateScopes(attributes);

	}

	private void validateDependencies(String dependency) throws HederaException {
		if(null == dependency) {
			return;
		}

		if(!ALLOWABLE_DEPENDENCIES.contains(dependency)) {
			// TODO dynamic list please
			throw new HederaException("Dependency of '" + dependency + "' was defined, but only the following dependencies are allowed: 'pom.xml' and 'hedera.xml''");
		}
	}

	private void validateScopes(NamedNodeMap attributes) throws HederaException {
		String scopeAttribute  = HederaUtils.getNodeValue(attributes, "scopes");
		if(null != scopeAttribute) {
			String[] splits = scopeAttribute.split(",");
			for (int i = 0; i < splits.length; i++) {
				String split = splits[i].trim();
				if(split.length() != 0) {
					scopes.add(split);
				}
			}
		}

		if(null == scopes || scopes.isEmpty()) {
			SimpleLogger.logFatal(LoggerType.ARTIFACT, "Attribut 'scopes' __MUST__ be defined for artefact " + this.toString());
			throw new HederaException("Could not validate scopes.");
		}
		
		// at his point we have valid scopes attribute, but do the exist?
		
	}

	public void download(Map<String, Repository> repositories, Map<String, Scope> scopes) throws HederaException {
		// if we have a url - skip the repositories
		this.urlPath = url;
		if(null != url) {
			downloadFile(scopes);
			return;
		}

		Repository repository = repositories.get(this.repository);

		if(null == repository) {
			throw new HederaException("Could not find repository for artifact: '" + this.toString() + "'.");
		} else {
			urlPath = replaceTokens(repository.getUrl());
			downloadFile(scopes);
		}

	}

	private String replaceTokens(String repositoryUrl) throws HederaException {
		for (int i = 0; i < attributes.getLength(); i++) {
			Node item = attributes.item(i);
			repositoryUrl = repositoryUrl.replaceAll("\\{" + item.getNodeName() + "\\}", item.getNodeValue());
		}
		SimpleLogger.logInfo(LoggerType.ARTEFACT_URL, "Found url of '" + repositoryUrl + ".'");
		if(repositoryUrl.contains("{")) {
			throw new HederaException("Un-replaced token found in repository URL '" + repositoryUrl + "'");
		}
		return(repositoryUrl);
	}

	private void downloadFile(Map<String, Scope> allScopes) throws HederaException {

		Iterator<String> scopesIterator = scopes.iterator();

		Set<String> remainingScopes = new HashSet<String>();

		while (scopesIterator.hasNext()) {
			String scope = scopesIterator.next();
			if(allScopes.containsKey(scope)) {
				Scope allScope = allScopes.get(scope);
				String outputPath = allScope.getDir() + "/" + getBinaryPath();
				File file = new File(outputPath);

				if(file.exists()) {
					SimpleLogger.logWarn(LoggerType.ARTEFACT_DOWNLOAD, "Artefact exists in scope '" + allScope.getDir() + "/" + getBinaryPath() + "', ignoring.");
				} else {
					remainingScopes.add(scope);
				}
			}
		}

		if(remainingScopes.isEmpty()) {
			SimpleLogger.logWarn(LoggerType.ARTEFACT_DOWNLOAD, "No scopes left for download.");
			found = true;
			return;
		}

		URL url;
		try {
			url = new URL(urlPath);
			URLConnection connection = url.openConnection();
			int contentLength = connection.getContentLength();

			InputStream raw = connection.getInputStream();
			InputStream in = new BufferedInputStream(raw);

			byte[] data = new byte[contentLength];
			int bytesRead = 0;
			int offset = 0;
			while (offset < contentLength) {
				bytesRead = in.read(data, offset, data.length - offset);
				if (bytesRead == -1) {
					break;
				}
				offset += bytesRead;
			}
			in.close();

			SimpleLogger.logInfo(LoggerType.ARTEFACT_DOWNLOAD, "Downloading '" + urlPath + "'.");
			found = true;

			if (offset != contentLength) {
				messages.add("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
				found = false;
			}

			if(found) {
				messages.clear();
				messages.add("Found artifact @ " + urlPath);
				// now we need to write it to the scopes
				scopesIterator = remainingScopes.iterator();
				while (scopesIterator.hasNext()) {
					String scope = scopesIterator.next();
					if(allScopes.containsKey(scope)) {
						Scope allScope = allScopes.get(scope);
						String outputPath = allScope.getDir() + "/" + getBinaryPath();
						FileOutputStream fos = new FileOutputStream(outputPath);
						fos.write(data);
						fos.close();
						messages.add("  Wrote '" + outputPath + "'.");
					} else {
						messages.add("[FATAL] Could not write '" + getBinaryPath() + "' to scope '" + scope + "', this scope does not exist.");
					}

				}
			} else {
				messages.add("Could not find artifact @" + urlPath);
			}

		} catch (MalformedURLException murlex) {
			throw new HederaException("Malformed repository scope of '" + urlPath + "'.");
		} catch (FileNotFoundException fnfex) {
			messages.add(urlPath + ": File not found");
		} catch (IOException ioex) {
			messages.add(urlPath + ": " + ioex.getMessage());
		}
	}

	private String getBinaryPath() {
		int lastIndexOf = urlPath.lastIndexOf("/") + 1;
		return(urlPath.substring(lastIndexOf));
	}

	public boolean getFound() { return(found); }
	public List<String> getMessages() { return(messages); }

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		if(null != url && url.trim().length() != 0) {
			stringBuilder.append("{ \"url\":\"" + url + "\", ");
		} else {
		}
		stringBuilder.append("\"scopes\": [ ");
		Iterator<String> scopesIterator = scopes.iterator();
		while (scopesIterator.hasNext()) {
			String scope = scopesIterator.next();
			stringBuilder.append("\"" + scope + "\"");
			if(scopesIterator.hasNext()) {
				stringBuilder.append(", ");
			}
		}
		stringBuilder.append(" ] }");
		return (stringBuilder.toString());
	}
}
