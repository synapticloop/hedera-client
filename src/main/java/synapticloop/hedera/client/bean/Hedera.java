package synapticloop.hedera.client.bean;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
	private HashMap<String, Location> locations = new HashMap<String, Location>();

	private ArrayList<Repository> repositories = new ArrayList<Repository>();
	private ArrayList<Artifact> artifacts = new ArrayList<Artifact>();

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
		boolean hasError = false;
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
				hasError = true;
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
		if(hasError) {
			throw new HederaException("Resolution errors in hedera.");
		}
	}

	public void executePush(ArrayList<Repository> masterRepositories, String fileName, String location) throws HederaException {
		// try and load the file
		File file = new File(fileName);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);


			byte[] data = new byte[fis.available()];
			fis.read(data);

			// now go through and push to each of the master repositories
			for (Repository repository : masterRepositories) {
				String repositoryUrl = repository.getUrl() + "api/" + location;
				URL url = new URL(repositoryUrl);
				System.out.println("Attempting push of '" + fileName + "' to repository '" + repositoryUrl + "'");
				HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
				httpCon.setDoOutput(true);
				httpCon.setRequestMethod("PUT");
				OutputStream outputStream = httpCon.getOutputStream();
				outputStream.write(data);
				outputStream.close();
				int responseCode = httpCon.getResponseCode();
				if(responseCode != 200) {
					System.out.println("FATAL: got response code of '" + responseCode + "' for repository url '" + repositoryUrl + "'.");
				}
			}
		} catch (FileNotFoundException fnfex) {
			throw new HederaException(fnfex.getMessage());
		} catch (IOException ioex) {
			throw new HederaException(ioex.getMessage());
		}
	}

	public void addLocation(Location location) { locations.put(location.getName(), location); }
	public void addRepository(Repository repository) { repositories.add(repository); }
	public void addArtifact(Artifact artifact) { artifacts.add(artifact); }

	public ArrayList<Repository> getMasterRepositories() {
		ArrayList<Repository> retVal = new ArrayList<Repository>();
		for (Repository repository : repositories) {
			if(repository.getIsMaster()) {
				retVal.add(repository);
			}
		}
		return(retVal);
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("{\n \"repositories\": [ \n");
		Iterator<Repository> repositoriesIterator = repositories.iterator();
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
		Iterator<Location> locationsIterator = locations.values().iterator();

		while (locationsIterator.hasNext()) {
			Location location = locationsIterator.next();
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
