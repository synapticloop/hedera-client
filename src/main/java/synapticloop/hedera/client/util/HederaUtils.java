package synapticloop.hedera.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public static String escapeRegexp(String regexp){
		String specChars = "\\$.*+?|()[]{}^";
		String result = regexp;
		for (int i=0;i<specChars.length();i++){
			Character curChar = specChars.charAt(i);
			result = result.replaceAll(
					"\\"+curChar,
					"\\\\" + (i<2?"\\":"") + curChar); // \ and $ must have special treatment
		}
		return result;
	}

	public static List<String> findGroup(String content, String pattern, int group) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(content);
		List<String> result = new ArrayList<String>();
		while (m.find()) {
			result.add(m.group(group));
		}
		return result;
	}


	public static List<String> tokenize(String content, String firstToken, String lastToken){
		String regexp = lastToken.length() > 1
				?escapeRegexp(firstToken) + "(.*?)"+ escapeRegexp(lastToken)
						:escapeRegexp(firstToken) + "([^"+lastToken+"]*)"+ escapeRegexp(lastToken);
				return findGroup(content, regexp, 1);
	}

}
