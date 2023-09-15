package dagateway.api.utils;


import java.util.HashMap;


/**
 * 대소문자 구분이 없는 Map
 *
 */
public class InsensitiveKeyMap<K, V> extends HashMap<K, V> {
	private static final long serialVersionUID = 8463207576106608881L;

	
	public InsensitiveKeyMap() {
	}

	public V get(Object key) {
		key = this.toLowerCase(key);
		return super.get(key);
	}

	public boolean containsKey(Object key) {
		key = this.toLowerCase(key);
		return super.containsKey(key);
	}

	@SuppressWarnings("unchecked")
	public V put(K key, V value) {
		key = (K)this.toLowerCase((Object)key);
		return super.put(key, value);
	}

	public V remove(Object key) {
		key = this.toLowerCase(key);
		return super.remove(key);
	}
	
	private Object toLowerCase(Object key) {
		if(key != null && key instanceof String) {
			key = ((String)key).toLowerCase();
		}
		
		return key;
	}


}
