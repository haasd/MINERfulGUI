package minerful.miner.stats.xmlenc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import minerful.concept.xmlenc.CharAdapter;
import minerful.miner.stats.LocalStatsWrapper;

public class GlobalStatsMapAdapter extends XmlAdapter<GlobalStatsMapAdapter.KeyValueList, Map<Character, LocalStatsWrapper>>{
	@XmlType(name="stats")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class KeyValueList {
		@XmlType(name="stat")
		@XmlAccessorType(XmlAccessType.FIELD)
		public static class Item {
			// This is due to the ridiculous bug of the Metro implementation of JAXB.
			// Without an Adapter, you would have an Integer encoding the Character as a result.
			@XmlAttribute(name="task")
			@XmlJavaTypeAdapter(value=CharAdapter.class)
			public Character key;
			@XmlElement(name="details")
			public LocalStatsWrapper value;

			public Item(Character key, LocalStatsWrapper value) {
				this.key = key;
				this.value = value;
			}
			public Item() {
			}
		}

		@XmlElements({
			@XmlElement(name="stats")
		})
		public final List<Item> list;
		
		public KeyValueList() {
			this.list = new ArrayList<GlobalStatsMapAdapter.KeyValueList.Item>();
		}
		public KeyValueList(List<Item> list) {
			this.list = list;
		}
	}

	@Override
	public GlobalStatsMapAdapter.KeyValueList marshal(
			Map<Character, LocalStatsWrapper> v) throws Exception {
		Set<Character> keys = v.keySet();
		ArrayList<GlobalStatsMapAdapter.KeyValueList.Item> results = new ArrayList<GlobalStatsMapAdapter.KeyValueList.Item>(v.size());
        for (Character key : keys) {
            results.add(new GlobalStatsMapAdapter.KeyValueList.Item(key, v.get(key)));
        }
        return new KeyValueList(results);
	}

	@Override
	public Map<Character, LocalStatsWrapper> unmarshal(
			GlobalStatsMapAdapter.KeyValueList v)
			throws Exception {
		Map<Character, LocalStatsWrapper> globalStatsMap = new HashMap<Character, LocalStatsWrapper>(v.list.size());
		for (GlobalStatsMapAdapter.KeyValueList.Item keyValue : v.list) {
			globalStatsMap.put(keyValue.key, keyValue.value);
		}
		return globalStatsMap;
	}
}
