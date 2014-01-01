package synapticloop.hedera.client.bean;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import synapticloop.hedera.client.util.HederaUtils;

public class Repository {
	private String url = null;
	private String type = null;

	public Repository(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		this.url = HederaUtils.getNodeValue(attributes, "url");
		this.type = HederaUtils.getNodeValue(attributes, "type");

		if(!this.url.endsWith("/")) {
			this.url += "/";
		}

		if(null == type) {
			type = "normal";
		}
	}

	public boolean getIsMaster() {
		if(null != type) {
			return(type.compareToIgnoreCase("master") == 0);
		}
		return(false);
	}

	public String getUrl() { return url; }

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("{ \"url\":\"" + url + "\", \"type\":\"" + type + "\" }\n");
		return (stringBuilder.toString());
	}
}
