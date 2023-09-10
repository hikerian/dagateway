package dagateway.api.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class HeaderProperties {
	private List<HeaderEntry> add;
	private List<HeaderEntry> set;
	private List<String> retain;
	private List<HeaderEntry> rename;
	
	
	public HeaderProperties() {
	}
	
	public List<HeaderEntry> getAdd() {
		return this.add;
	}
	
	public void setAdd(List<String> add) {
		this.add = this.convertToHeaderEntryList(add);
	}

	public List<HeaderEntry> getSet() {
		return this.set;
	}

	public void setSet(List<String> set) {
		this.set = this.convertToHeaderEntryList(set);
	}
	
	protected List<HeaderEntry> convertToHeaderEntryList(List<String> data) {
		return data.stream().map(nameValue -> new HeaderEntry(nameValue)).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
	}
	
	public List<String> getRetain() {
		return this.retain;
	}

	public void setRetain(List<String> retain) {
		this.retain = retain;
	}

	public List<HeaderEntry> getRename() {
		return this.rename;
	}

	public void setRename(List<String> rename) {
		this.rename = this.convertToHeaderEntryList(rename);
	}
	
	public static class HeaderEntry {
		private String name;
		private String value;


		HeaderEntry(String nameValue) {
			int idx = nameValue.indexOf('=');
			if(idx == -1) {
				throw new IllegalArgumentException("name=value: " + nameValue);
			}
			this.name = nameValue.substring(0, idx);
			this.value = nameValue.substring(idx + 1);
		}
		
		public String getName() {
			return this.name;
		}
		
		public String getValue() {
			return this.value;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.name, this.value);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			HeaderEntry other = (HeaderEntry) obj;
			return Objects.equals(this.name, other.name) && Objects.equals(this.value, other.value);
		}
	}

}
