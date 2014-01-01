package synapticloop.hedera.client.bean;

import java.io.File;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class Location {
	private String name = null;
	private String dir = null;

	public Location(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		this.name = attributes.getNamedItem("name").getNodeValue();
		this.dir = attributes.getNamedItem("dir").getNodeValue();
	}

	public void init() {
		File directory = new File(dir);
		directory.mkdirs();
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("{ \"name\":\"" + name + "\", ");
		stringBuilder.append("\"dir\":\"" + dir + "\" }\n");
		return (stringBuilder.toString());
	}

	public String getName() { return name; }
	public String getDir() { return dir; }
}
