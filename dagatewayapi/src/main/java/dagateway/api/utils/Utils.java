package dagateway.api.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpHeaders;

import dagateway.api.context.RouteRequestContext.HeaderSpec;
import dagateway.api.context.route.HeaderProperties;


/**
 * @author Dong-il Cho
 */
public class Utils {
	/** Captures URI template variable names. */
	private static final Pattern NAMES_PATTERN = Pattern.compile("\\{([^/]+?)\\}");
	
	
	private Utils() {
	}
	
	public static String[] splitAndTrim(String data, String regexDelimiter) {
		String[] datas = data.split(regexDelimiter);
		for(int i = 0; i < datas.length; i++) {
			datas[i] = datas[i].trim();
		}
		return datas;
	}
	
	public static String applyVariable(String pattern, Map<String, String> variables) {
		if(pattern == null) {
			return pattern;
		}
		if(pattern.indexOf('{') == -1) {
			return pattern;
		}
		if(pattern.indexOf(':') != -1) {
			pattern = Utils.sanitizePattern(pattern);
		}
		
		pattern = pattern.replaceAll("[$][{]", "{");
		Matcher matcher = Utils.NAMES_PATTERN.matcher(pattern);
		StringBuilder sb = new StringBuilder();
		while(matcher.find()) {
			String match = matcher.group(1);
			String varName = Utils.getVariableName(match);
			String varValue = variables.get(varName);
			varValue = Matcher.quoteReplacement(varValue);
			matcher.appendReplacement(sb, varValue);
		}
		matcher.appendTail(sb);
		
		return sb.toString();
	}
	
	private static String sanitizePattern(String pattern) {
		int level = 0;
		int lastCharIndex = 0;
		char[] chars = new char[pattern.length()];
		for(int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			if(c == '{') {
				level++;
			}
			if(c == '}') {
				level--;
			}
			if(level > 1 || (level == 1 && c == '}')) {
				continue;
			}
			chars[lastCharIndex++] = c;
		}
		return new String(chars, 0, lastCharIndex);
	}
	
	private static String getVariableName(String match) {
		int colonIdx = match.indexOf(':');
		return (colonIdx != -1 ? match.substring(0, colonIdx) : match);
	}
	
	public static void filterHeader(HttpHeaders targetHeaders, HeaderSpec headerSpec, Map<String, String> variables) {
		if(headerSpec == null) {
			return;
		}

		// add
		List<HeaderProperties.HeaderEntry> addHeaders = headerSpec.getAdd();
		if(addHeaders != null && addHeaders.size() > 0) {
			for(HeaderProperties.HeaderEntry entry : addHeaders) {
				String name = Utils.applyVariable(entry.getName(), variables);
				String value = Utils.applyVariable(entry.getValue(), variables);
				
				targetHeaders.add(name, value);
			}
		}
		
		// set
		List<HeaderProperties.HeaderEntry> setHeaders = headerSpec.getSet();
		if(setHeaders != null && setHeaders.size() > 0) {
			for(HeaderProperties.HeaderEntry entry : setHeaders) {
				String name = Utils.applyVariable(entry.getName(), variables);
				String value = Utils.applyVariable(entry.getValue(), variables);
				
				targetHeaders.set(name, value);
			}
			
		}
	}
	
	public static void filterHeader(HttpHeaders sourceHeaders, HttpHeaders targetHeaders
			, HeaderSpec headerSpec, Map<String, String> variables) {
		if(headerSpec == null) {
			return;
		}
		
		List<String> renamedHeaderNames = new ArrayList<>();
		
		// rename
		List<HeaderProperties.HeaderEntry> renameHeaders = headerSpec.getRename();
		if(renameHeaders != null) {
			for(HeaderProperties.HeaderEntry entry : renameHeaders) {
				List<String> headerValues = sourceHeaders.get(entry.getName());
				if(headerValues != null && headerValues.size() > 0) {
					String renameHeader = entry.getValue().trim().toUpperCase();
					renameHeader = Utils.applyVariable(renameHeader, variables);
					
					targetHeaders.addAll(renameHeader, headerValues);
					// 변경된 헤더의 원본 값을 저장
					renamedHeaderNames.add(entry.getName().trim().toUpperCase());
				}
			}
		}
		
		// retain
		List<String> retainKeys = headerSpec.getRetain();
		if(retainKeys != null) {
			if(retainKeys.contains("*")) {
				Set<String> sourceHeaderNames = sourceHeaders.keySet();
				
				for(String sourceHeaderName : sourceHeaderNames) {
					if(renamedHeaderNames.indexOf(sourceHeaderName.trim().toUpperCase()) == -1) {
						targetHeaders.addAll(sourceHeaderName, sourceHeaders.get(sourceHeaderName));
					}
				}
			} else if(retainKeys.size() > 0) {
				for(String retainHeader : retainKeys) {
					List<String> headerValues = sourceHeaders.get(retainHeader);
					if(headerValues != null && headerValues.size() > 0) {
						targetHeaders.addAll(retainHeader, headerValues);
					}
				}
			}
		}
		
		// add and set
		Utils.filterHeader(targetHeaders, headerSpec, variables);
	}
	

}
