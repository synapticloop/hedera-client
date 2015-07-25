package synapticloop.hedera.client.bean;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import synapticloop.hedera.client.exception.HederaException;
import synapticloop.hedera.client.util.HederaUtils;

public class Hedera {
	private Map<String, Scope> scopes = new HashMap<String, Scope>();

	private Map<String, Repository> repositories = new HashMap<String, Repository>();
	private List<Artifact> artifacts = new ArrayList<Artifact>();

	public Hedera(String hederaFile) throws HederaException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		File hederaXmlFile = null;
		try {
			hederaXmlFile = new File(hederaFile);
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(hederaXmlFile);

			NodeList tokens = document.getElementsByTagName("repository-token");
			for (int i = 0; i < tokens.getLength(); i++) {
				if(i == 0) {
					Repository.clearTokens();
				}
				Repository.addToken(HederaUtils.getNodeValue(tokens.item(i).getAttributes(), "name"));
			}

			NodeList scopes = document.getElementsByTagName("scope");

			for(int i = 0; i < scopes.getLength(); i++) {
				addScope(new Scope(scopes.item(i)));
			}

			NodeList repositories = document.getElementsByTagName("repository");

			for(int i =0; i < repositories.getLength(); i++) {
				Repository repository = new Repository(repositories.item(i));
				addRepository(repository.getName(), repository);
			}

			NodeList artifacts = document.getElementsByTagName("artifact");

			for(int i =0; i < artifacts.getLength(); i++) {
				addArtifact(new Artifact(artifacts.item(i)));
			}


		} catch (ParserConfigurationException pcex) {
			throw new HederaException("Could not parse the hedera file '" + hederaFile + "'.", pcex);
		} catch (SAXException saxex) {
			throw new HederaException("Could not parse the hedera file '" + hederaFile + "'.", saxex);
		} catch (IOException ioex) {
			throw new HederaException("Could not parse the hedera file '" + hederaFile + "'.", ioex);
		}
	}

	public void execute() throws HederaException {
		boolean hasError = false;

		// now we want to download all of the artifacts into the correct directory
		Iterator<Artifact> artifactsIterator = artifacts.iterator();
		while (artifactsIterator.hasNext()) {
			Artifact artifact = artifactsIterator.next();
			artifact.download(repositories, scopes);
		}

		// now print out all of the information
		artifactsIterator = artifacts.iterator();
		boolean first = true;
		while (artifactsIterator.hasNext()) {
			Artifact artifact = artifactsIterator.next();
			if(!artifact.getFound()) {
				hasError = true;
				if(first) {
					System.out.println("\n+-----------------------------+");
					System.out.println("| Error in Hedera resolution: |");
					System.out.println("+-----------------------------+");
					first = false;
				}
				List<String> messages = artifact.getMessages();
				for (String message : messages) {
					System.out.println("  " + message);
				}
			}
		}

		// now print out all of the information
		artifactsIterator = artifacts.iterator();
		first = true;
		while (artifactsIterator.hasNext()) {
			Artifact artifact = artifactsIterator.next();
			if(artifact.getFound()) {
				if(first) {
					System.out.println("\n+-------------------------------+");
					System.out.println("| Success in Hedera resolution: |");
					System.out.println("+-------------------------------+");
					first = false;
				}
				List<String> messages = artifact.getMessages();
				for (String message : messages) {
					System.out.println("  " + message);
				}
			}
		}
		if(hasError) {
			throw new HederaException("Resolution errors in hedera.");
		}
	}

	public void addScope(Scope location) { scopes.put(location.getName(), location); }
	public void addRepository(String name, Repository repository) { repositories.put(name, repository); }
	public void addArtifact(Artifact artifact) { artifacts.add(artifact); }

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("{\n \"repositories\": [ \n");
		Iterator<Repository> repositoriesIterator = repositories.values().iterator();
		while (repositoriesIterator.hasNext()) {
			Repository repository = repositoriesIterator.next();
			stringBuilder.append("  " + repository);
			if(repositoriesIterator.hasNext()) {
				stringBuilder.append(", ");
			}
			stringBuilder.append("\n");
		}
		stringBuilder.append(" ]\n");

		stringBuilder.append("\"locations\": [ \n");
		Iterator<Scope> locationsIterator = scopes.values().iterator();

		while (locationsIterator.hasNext()) {
			Scope location = locationsIterator.next();
			stringBuilder.append("  " + location);
			if(locationsIterator.hasNext()) {
				stringBuilder.append(", ");
			}
			stringBuilder.append("\n");
		}
		stringBuilder.append(" ]\n");

		stringBuilder.append("\"artifacts\" : [ \n");
		Iterator<Artifact> artifactsIterator = artifacts.iterator();
		while (artifactsIterator.hasNext()) {
			Artifact artifact = artifactsIterator.next();
			stringBuilder.append("  " + artifact.toString());
			if(artifactsIterator.hasNext()) {
				stringBuilder.append(", ");
			}
			stringBuilder.append("\n");
		}
		stringBuilder.append(" ]\n}\n");
		return (stringBuilder.toString());
	}

}
