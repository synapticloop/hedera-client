package synapticloop.hedera.client.util;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class HederaUtils {
	public static String getNodeValue(NamedNodeMap attributes, String name) {
		String nodeValue = null;

		Node node = attributes.getNamedItem(name);
		if(null != node) {
			nodeValue = attributes.getNamedItem(name).getNodeValue();
		}

		if(null != nodeValue && nodeValue.trim().length() == 0) {
			nodeValue = null;
		}
		return(nodeValue);
	}

}
