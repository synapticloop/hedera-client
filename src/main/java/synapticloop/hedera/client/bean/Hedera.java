package synapticloop.hedera.client.bean;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import synapticloop.hedera.client.exception.HederaException;

public class Hedera {
	private HashMap<String, Location> locations= new HashMap<String, Location>();

	private ArrayList<Repository> repositories = new ArrayList<Repository>();
	private ArrayList<Artifact> artifacts= new ArrayList<Artifact>();

	public Hedera(String hederaFile) throws HederaException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		File hederaXmlFile = null;
		try {
			hederaXmlFile = new File(hederaFile);
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(hederaXmlFile);
			NodeList artifacts = document.getElementsByTagName("artifact");

			for(int i =0; i < artifacts.getLength(); i++) {
				addArtifact(new Artifact(artifacts.item(i)));
			}

			NodeList locations = document.getElementsByTagName("location");

			for(int i =0; i < locations.getLength(); i++) {
				addLocation(new Location(locations.item(i)));
			}

			NodeList repositories = document.getElementsByTagName("repository");

			for(int i =0; i < repositories.getLength(); i++) {
				addRepository(new Repository(repositories.item(i)));
			}
		} catch (ParserConfigurationException pcex) {
			System.out.println(pcex.getMessage());
			return;
		} catch (SAXException saxex) {
			System.out.println(saxex.getMessage());
			return;
		} catch (IOException ioex) {
			System.out.println(ioex.getMessage());
			return;
		}
	}

	public void execute() throws HederaException {
		// first thing that we want to do is to create the directories
		Iterator<Location> locationsIterator = locations.values().iterator();
		while (locationsIterator.hasNext()) {
			Location location = locationsIterator.next();
			location.init();
		}

		// now we want to download all of the artifacts into the correct directory
		Iterator<Artifact> artifactsIterator = artifacts.iterator();
		while (artifactsIterator.hasNext()) {
			Artifact artifact = artifactsIterator.next();
			artifact.download(repositories, locations);
		}

		// now print out all of the information
		artifactsIterator = artifacts.iterator();
		boolean first = true;
		while (artifactsIterator.hasNext()) {
			Artifact artifact = artifactsIterator.next();
			if(!artifact.getFound()) {
				if(first) {
					System.out.println("\n+-----------------------------+");
					System.out.println("| Error in Hedera resolution: |");
					System.out.println("+-----------------------------+");
					first = false;
				}
				ArrayList<String> messages = artifact.getMessages();
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
				ArrayList<String> messages = artifact.getMessages();
				for (String message : messages) {
					System.out.println("  " + message);
				}
			}
		}
	}

	public void addLocation(Location location) { locations.put(location.getName(), location); }
	public void addRepository(Repository repository) { repositories.add(repository); }
	public void addArtifact(Artifact artifact) { artifacts.add(artifact); }

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append(repositories.size() + " Repositories:\n");
		Iterator<Repository> repositoriesIterator = repositories.iterator();
		while (repositoriesIterator.hasNext()) {
			Repository repository = repositoriesIterator.next();
			stringBuilder.append("  " + repository);
		}

		stringBuilder.append(locations.size() + " Locations:\n");
		Iterator<Location> locationsIterator = locations.values().iterator();
		while (locationsIterator.hasNext()) {
			Location location = locationsIterator.next();
			stringBuilder.append("  " + location);
		}

		stringBuilder.append(artifacts.size() + " Artifacts:\n");
		Iterator<Artifact> artifactsIterator = artifacts.iterator();
		while (artifactsIterator.hasNext()) {
			Artifact artifact = artifactsIterator.next();
			stringBuilder.append("  " + artifact.toString());

		}
		return (stringBuilder.toString());
	}

}
