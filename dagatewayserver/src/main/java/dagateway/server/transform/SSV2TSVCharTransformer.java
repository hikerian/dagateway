package dagateway.server.transform;

import java.io.CharArrayWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dagateway.api.resolver.http.CharDelimiterSupport;
import dagateway.api.service.ServiceFault;
import dagateway.api.transform.AbstractDataTransformer;
import dagateway.api.transform.DividedDataSupport;



/**
 * @author Dong-il Cho
 */
public class SSV2TSVCharTransformer extends AbstractDataTransformer<String, String> implements CharDelimiterSupport, DividedDataSupport {
	private final Logger log = LoggerFactory.getLogger(SSV2TSVCharTransformer.class);
	
	public static final String ARGUMENT_TYPE = "java.lang.String";
	public static final String RETURN_TYPE = "java.lang.String";
	
	//SSV
	private final static String SSV_COL_DLMTR = Character.toString((char)31);
	
	//TSV
	private final static char TSV_ROW_DLMTR = '\n';
	private final static char TSV_COL_DLMTR = '\t';
	
	private CharArrayWriter tsvBuffer = new CharArrayWriter(8192);	
	
	private boolean startDataSet = false;
	private List<Entry> constants = new ArrayList<Entry>();

	private final char[] delimiters = new char[] {(char)30};
	
	
	public SSV2TSVCharTransformer() {
	}
	
	@Override
	protected void doInit() {
	}
	
	@Override
	public String transform(String source) {
		this.log.debug("transform");
		
		if(source.startsWith("SSV:")) {
			// skip
			
		} else if(source.startsWith("Dataset:")) {
			String dataSetId = source.substring("DataSet:".length()).trim();
			this.tsvBuffer.append("@d#");
			this.tsvBuffer.append(dataSetId);
			this.tsvBuffer.append(SSV2TSVCharTransformer.TSV_ROW_DLMTR);
			this.startDataSet = true;
			this.constants.clear();

		} else if(source.startsWith("_Const_")) {
			String consts = source.substring("_Const_".length()).trim();
			String[] constKVs = consts.split(SSV2TSVCharTransformer.SSV_COL_DLMTR);
			for(String constKV : constKVs) {
				String[] constant = constKV.split("=");
				if(constant != null && constant.length == 2) {
					this.constants.add(new Entry(constant[0], constant[1]));
				}
			}
		} else if(source.startsWith("_RowType_")) {
			String rowTypes = source.substring("_RowType_".length()).trim();
			String[] headers = rowTypes.split(SSV2TSVCharTransformer.SSV_COL_DLMTR);
			boolean first = true;
			for(String header : headers) {
				int headerIdIdx = header.indexOf(':');
				if(first) {
					first = false;
				} else {
					this.tsvBuffer.append(SSV2TSVCharTransformer.TSV_COL_DLMTR);
				}
				if(headerIdIdx != -1) {
					this.tsvBuffer.append(header.substring(0, headerIdIdx));
				} else {
					this.tsvBuffer.append(header);
				}
			}
			for(Entry constant : this.constants) {
				if(first) {
					first = false;
				} else {
					this.tsvBuffer.append(SSV2TSVCharTransformer.TSV_COL_DLMTR);
				}
				this.tsvBuffer.append(constant.key());
			}
			this.tsvBuffer.append(TSV_ROW_DLMTR);
		} else {
			boolean first = true;
			if(this.startDataSet) {
				String[] colValues = source.split(SSV2TSVCharTransformer.SSV_COL_DLMTR);
				for(String colValue : colValues) {
					if(first) {
						first = false;
					} else {
						this.tsvBuffer.append(SSV2TSVCharTransformer.TSV_COL_DLMTR);
					}
					this.tsvBuffer.append(colValue);
				}
				for(Entry constant : this.constants) {
					if(first) {
						first = false;
					} else {
						this.tsvBuffer.append(SSV2TSVCharTransformer.TSV_COL_DLMTR);
					}
					this.tsvBuffer.append(constant.value());
				}
				
				this.tsvBuffer.append(SSV2TSVCharTransformer.TSV_ROW_DLMTR);
			} else {
				String[] params = source.split(SSV2TSVCharTransformer.SSV_COL_DLMTR);
				for(String param : params) {
					int eqIdx = param.indexOf('=');
					String varKey = "";
					String varVal = "";
					if(eqIdx != -1) {
						varKey = param.substring(0, eqIdx);
						varVal = param.substring(eqIdx + 1);
					} else {
						varKey = param;
					}
					
					this.tsvBuffer.append("@d#");
					this.tsvBuffer.append(varKey);
					this.tsvBuffer.append(SSV2TSVCharTransformer.TSV_COL_DLMTR);
					this.tsvBuffer.append(varVal);
					this.tsvBuffer.append(SSV2TSVCharTransformer.TSV_ROW_DLMTR);
				}
			}
		}
		
		String result = this.tsvBuffer.toString();
		this.tsvBuffer.reset();
		
		return result;
	}
	
	@Override
	public char[] delimiters() {
		return this.delimiters;
	}

	@Override
	public boolean stripDelimiter() {
		return true;
	}
	
	@Override
	public String transform(ServiceFault fault) {
		StringBuilder header = new StringBuilder();
		StringBuilder data = new StringBuilder();
		
		Map<String, Object> faultMap = fault.toMap();
		Set<Map.Entry<String, Object>> entrySet = faultMap.entrySet();
		boolean first = true;
		for(Map.Entry<String, Object> entry : entrySet) {
			if(first) {
				first = false;
			} else {
				header.append(SSV2TSVCharTransformer.TSV_COL_DLMTR);
				data.append(SSV2TSVCharTransformer.TSV_COL_DLMTR);
			}
			
			header.append(this.encode(entry.getKey()));
			data.append(this.encode(entry.getValue()));
		}
		
		return header.append(SSV2TSVCharTransformer.TSV_ROW_DLMTR)
				.append(data.toString()).toString();
	}
	
	@Override
	public void encode(char ch, CharArrayWriter writer) {
		switch(ch) {
		case '\t':
			writer.append('\\');
			writer.append('t');
			break;
		case '\n':
			writer.append('\\');
			writer.append('n');
			break;
		case '\r':
			writer.append('\\');
			writer.append('r');
			break;
		case '\b':
			writer.append('\\');
			writer.append('b');
			break;
		case '\\':
			writer.append('\\');
			writer.append('\\');
			break;
		default:
			writer.append(ch);
		}
	}
	
	private String encode(Object data) {
		if(data == null) {
			return "";
		}
		String str = data.toString();
		CharArrayWriter buffer = new CharArrayWriter();
		
		char[] chars = str.toCharArray();
		for(char ch : chars) {
			this.encode(ch, buffer);
		}
		
		return buffer.toString();
	}
	
	private static class Entry {
		private String key;
		private String value;
		
		public Entry(String key, String value) {
			this.key = key;
			this.value = value;
		}
		public String key() {
			return this.key;
		}
		public String value() {
			return this.value;
		}
	}
	

}
