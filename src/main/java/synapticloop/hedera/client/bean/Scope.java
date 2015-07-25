package synapticloop.hedera.client.bean;

import java.io.File;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import synapticloop.hedera.client.util.SimpleLogger;
import synapticloop.hedera.client.util.SimpleLogger.LoggerType;

public class Scope {
	private String name = null;
	private String dir = null;

	public Scope(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		this.name = attributes.getNamedItem("name").getNodeValue();
		this.dir = attributes.getNamedItem("dir").getNodeValue();
		File directory = new File(dir);
		boolean mkdirs = directory.mkdirs();

		if(mkdirs) {
			SimpleLogger.logInfo(LoggerType.SCOPE, "Creating directory '" + dir + "' for scope '" + name + "'.");
		}

		SimpleLogger.logInfo(LoggerType.SCOPE, "Mapped scope '" + name + "' to directory '" + dir + "'.");

	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("{ \"name\":\"" + name + "\", ");
		stringBuilder.append("\"dir\":\"" + dir + "\" }");
		return (stringBuilder.toString());
	}

	public String getName() { return name; }
	public String getDir() { return dir; }
}
