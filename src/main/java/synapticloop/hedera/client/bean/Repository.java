package synapticloop.hedera.client.bean;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class Repository {
	private String url = null;
	public Repository(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		this.url = attributes.getNamedItem("url").getNodeValue();
		if(!this.url.endsWith("/")) {
			this.url += "/";
		}
	}

	public String getUrl() { return url; }

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("{ \"url\":\"" + url + "\" }\n");
		return (stringBuilder.toString());
	}

}
