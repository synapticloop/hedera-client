package synapticloop.hedera.client.bean;


import java.io.BufferedInputStream;
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
	private String artifactPath = null;
	private String binaryPath = null;

	private String url = null;

	private String repository = null;

	private String dependency = null;
	private String name = null;
	private String version = null;
	private String type = null;
	private String group = null;

	private static final Set<String> ALLOWABLE_DEPENDENCIES = new HashSet<String>();
	static {
		ALLOWABLE_DEPENDENCIES.add("pom.xml");
		ALLOWABLE_DEPENDENCIES.add("hedera.xml");
	}

	private List<String> scopes = new ArrayList<String>();

	public Artifact(Node node) throws HederaException {
		NamedNodeMap attributes = node.getAttributes();


		// at this point it is either a simple url
		this.url = HederaUtils.getNodeValue(attributes, "url");

		this.dependency = HederaUtils.getNodeValue(attributes, "dependency");


		// or a repository - with possible tokens
		this.repository = HederaUtils.getNodeValue(attributes, "repository");
		this.name  = HederaUtils.getNodeValue(attributes, "name");
		this.group  = HederaUtils.getNodeValue(attributes, "group");
		this.version  = HederaUtils.getNodeValue(attributes, "version");
		this.type = HederaUtils.getNodeValue(attributes, "type");

		// now for some validation
		validateDependencies(dependency);
		validateScopes(attributes);

		// now we are ready to go!

		if(null != url) {
			int lastIndexOf = url.lastIndexOf("/");
			artifactPath = url.substring(0, lastIndexOf);
			binaryPath = url.substring(lastIndexOf + 1);
		} else {
			StringBuilder artifactPathBuilder = new StringBuilder();
			StringBuilder binaryPathBuilder = new StringBuilder();
			if(null != name) {
				artifactPathBuilder.append(name.replaceAll("-", "/") + "/");
				binaryPathBuilder.append(name);
			}
			if(null != version) {
				binaryPathBuilder.append("-");
				binaryPathBuilder.append(version);
			}
			if(null != type) {
				binaryPathBuilder.append(".");
				binaryPathBuilder.append(type);
			}
			this.binaryPath = binaryPathBuilder.toString();
			this.artifactPath = artifactPathBuilder.toString();
		}


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
			SimpleLogger.logFatal(LoggerType.ARTIFACTS, "Attribut 'scopes' __MUST__ be defined for artefact " + this.toString());
			throw new HederaException("Could not validate scopes.");
		}
		
		// at his point we have valid scopes attribute, but do the exist?
		
	}

	public void download(List<Repository> repositories, Map<String, Scope> scopes) throws HederaException {
		// if we have a url - skip the repositories
		this.urlPath = url;
		if(null != url) {
			downloadFile(scopes);
		}
		// go through all of the repositories, attempting to find the artifact
		for (Repository repository : repositories) {
			if(found) {
				break;
			}

			urlPath = repository.getUrl() + "api/" + artifactPath + binaryPath;
			downloadFile(scopes);
		}

	}

	private void downloadFile(Map<String, Scope> allScopes) throws HederaException {
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

			System.out.println("Downloaded " + urlPath);
			found = true;

			if (offset != contentLength) {
				messages.add("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
				found = false;
			}

			if(found) {
				messages.clear();
				messages.add("Found artifact @ " + urlPath);
				// now we need to write it to the scopes
				Iterator<String> scopesIterator = scopes.iterator();
				while (scopesIterator.hasNext()) {
					String scope = scopesIterator.next();
					if(allScopes.containsKey(scope)) {
						Scope allScope = allScopes.get(scope);
						String outputPath = allScope.getDir() + "/" + binaryPath;
						FileOutputStream fos = new FileOutputStream(outputPath);
						fos.write(data);
						fos.close();
						messages.add("  Wrote '" + outputPath + "'.");
					} else {
						messages.add("[FATAL] Could not write '" + binaryPath + "' to scope '" + scope + "', this scope does not exist.");
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

	public boolean getFound() { return(found); }
	public List<String> getMessages() { return(messages); }

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		if(null != url && url.trim().length() != 0) {
			stringBuilder.append("{ \"url\":\"" + url + "\", ");
		} else {
			stringBuilder.append("{ \"name\":\"" + name + "\", ");
			stringBuilder.append("\"version\":\"" + version + "\", ");
			stringBuilder.append("\"type\":\"" + type + "\", ");
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
