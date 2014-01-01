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
import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import synapticloop.hedera.client.exception.HederaException;
import synapticloop.hedera.client.util.HederaUtils;

public class Artifact {
	private boolean found = false;
	private ArrayList<String> messages = new ArrayList<String>();
	private String urlPath = null;
	private String artifactPath = null;
	private String binaryPath = null;

	private String org = null;
	private String name = null;
	private String version = null;
	private String type = null;
	private String url = null;
	private ArrayList<String> locations = new ArrayList<String>();

	public Artifact(Node node) throws HederaException {
		NamedNodeMap attributes = node.getAttributes();

		this.org = HederaUtils.getNodeValue(attributes, "org");
		this.name  = HederaUtils.getNodeValue(attributes, "name");
		this.version  = HederaUtils.getNodeValue(attributes, "version");
		this.type = HederaUtils.getNodeValue(attributes, "type");
		this.url  = HederaUtils.getNodeValue(attributes, "url");

		String locationAttribute  = HederaUtils.getNodeValue(attributes, "locations");
		if(null != locationAttribute) {
			String[] splits = locationAttribute.split(",");
			for (int i = 0; i < splits.length; i++) {
				String split = splits[i].trim();
				if(split.length() != 0) {
					locations.add(split);
				}
			}
		}

		if(null == url && (null == name || null == type)) {
			throw new HederaException("Element 'artifact' must contain either a 'url' attribute or both 'name' and 'type' attributes, element was " + this.toString());
		}

		if(locations.size() == 0) {
			throw new HederaException("Element 'artifact' must have at least one 'locations' attribute, element was " + this.toString());
		}

		if(null == url) {
			StringBuilder artifactPathBuilder = new StringBuilder();
			StringBuilder binaryPathBuilder = new StringBuilder();
			if(null != org) {
				artifactPathBuilder.append(org + "/");
			}
			if(null != name) {
				artifactPathBuilder.append(name + "/");
				binaryPathBuilder.append(name);
			}
			if(null != version) {
				binaryPathBuilder.append("-");
				binaryPathBuilder.append(version);
			}
			if(null != type) {
				artifactPathBuilder.append(type + "/");
				binaryPathBuilder.append(".");
				binaryPathBuilder.append(type);
			}
			this.binaryPath = binaryPathBuilder.toString();
			this.artifactPath = artifactPathBuilder.toString();
		} else {
			int lastIndexOf = url.lastIndexOf("/");
			artifactPath = url.substring(0, lastIndexOf);
			binaryPath = url.substring(lastIndexOf + 1);
		}


	}

	public void download(ArrayList<Repository> repositories, HashMap<String, Location> allLocations) throws HederaException {
		// if we have a url - skip the repositories
		this.urlPath = url;
		if(null != url) {
			downloadFile(allLocations);
		}
		// go through all of the repositories, attempting to find the artifact
		for (Repository repository : repositories) {
			if(found) {
				break;
			}

			urlPath = repository.getUrl() + "api/" + artifactPath + binaryPath;
			downloadFile(allLocations);
		}

	}

	private void downloadFile(HashMap<String, Location> allLocations) throws HederaException {
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
				// now we need to write it to the locations
				Iterator<String> locationsIterator = locations.iterator();
				while (locationsIterator.hasNext()) {
					String location = locationsIterator.next();
					if(allLocations.containsKey(location)) {
						Location allLocation = allLocations.get(location);
						String outputPath = allLocation.getDir() + "/" + binaryPath;
						FileOutputStream fos = new FileOutputStream(outputPath);
						fos.write(data);
						fos.close();
						messages.add("  Wrote '" + outputPath + "'.");
					} else {
						messages.add("[FATAL] Could not write '" + binaryPath + "' to location '" + location + "', this location does not exist.");
					}

				}
			} else {
				messages.add("Could not find artifact @" + urlPath);
			}

		} catch (MalformedURLException murlex) {
			throw new HederaException("Malformed repository location of '" + urlPath + "'.");
		} catch (FileNotFoundException fnfex) {
			messages.add(urlPath + ": File not found");
		} catch (IOException ioex) {
			messages.add(urlPath + ": " + ioex.getMessage());
		}
	}

	public boolean getFound() { return(found); }
	public ArrayList<String> getMessages() { return(messages); }

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		if(null != url && url.trim().length() != 0) {
			stringBuilder.append("{ \"url\":\"" + url + "\", ");
		} else {
			stringBuilder.append("{ \"org\":\"" + org + "\", ");
			stringBuilder.append("\"name\":\"" + name + "\", ");
			stringBuilder.append("\"version\":\"" + version + "\", ");
			stringBuilder.append("\"type\":\"" + type + "\", ");
		}
		stringBuilder.append("\"locations\": [ ");
		Iterator<String> locationsIterator = locations.iterator();
		while (locationsIterator.hasNext()) {
			String location = locationsIterator.next();
			stringBuilder.append("\"" + location + "\"");
			if(locationsIterator.hasNext()) {
				stringBuilder.append(", ");
			}
		}
		stringBuilder.append(" ] }\n");
		return (stringBuilder.toString());
	}
}
