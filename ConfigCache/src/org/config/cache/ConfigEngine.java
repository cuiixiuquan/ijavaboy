package org.config.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.config.cache.core.IConfig;
import org.config.cache.core.IDecoder;
import org.config.cache.core.IReader;
import org.config.cache.decode.text.AreaDecoder;
import org.config.cache.decode.text.BuildingDecoder;
import org.config.cache.decode.text.BuildingPositionDecoder;
import org.config.cache.decode.text.CityDecoder;
import org.config.cache.decode.text.CityRouteDecoder;
import org.config.cache.decode.text.CityTypeDecoder;
import org.config.cache.decode.text.CountryDecoder;
import org.config.cache.decode.text.DropConfigDecoder;
import org.config.cache.decode.text.GlobalConfigDecoder;
import org.config.cache.decode.text.HeroDecoder;
import org.config.cache.decode.text.ItemDecoder;
import org.config.cache.decode.text.ItemExtendDecoder;
import org.config.cache.decode.text.MissionDecoder;
import org.config.cache.decode.text.MonsterGroupDecoder;
import org.config.cache.decode.text.MonsterRefreshDecoder;
import org.config.cache.decode.text.RoleLevelDecoder;
import org.config.cache.decode.text.ShopItemDecoder;
import org.config.cache.exception.SimpleConfigException;
import org.config.cache.parser.TextListParser;
import org.config.cache.parser.TextMapParser;
import org.config.cache.reader.LineReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ����һ��ע���demo����Ҫ�������²���
 * 
 * 1������һ������ʵ��DemoConfig��ʵ��IConfig�ӿڣ���Ӧ�����ñ���ÿһ��
 * 2������һ���н�����DemoTextDecoder,ʵ��IDecoder�ӿڣ���ɶ�demo��ÿһ�еĽ���
 * 3����{ConfigType}ö��������һ�������ӦֵΪ���ñ�������
 * 4����{ConfigEngine}��registerAll �����н���ע��
 * 
 * @author chenjie
 * 2012-12-10
 */
public final class ConfigEngine {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigEngine.class);
	
	public static final String TYPE_JSON = "json";
	public static final String TYPE_TEXT = "text";
	
	public static final String DEFAULT_TEXT_DELIM = "\t";
	
	public static final String CONFIG_FOLDER_DIR = "file:///c:/cache/"; //�����ļ�����Ŀ¼
	
	private static class InstanceHolder{
		protected static ConfigEngine instance = new ConfigEngine();
	}
	
	private Map<ConfigType, String> configUrls; //������Ψһ������Map����

	private Map<ConfigType, IDecoder<IConfig>> decoders;
	
	private ConfigEngine(){}
	
	public static final ConfigEngine getInstance(){
		
		return InstanceHolder.instance;
	}
	
	/**
	 * ��ʼ��
	 */
	public void init(){
		
		this.configUrls = new HashMap<ConfigType, String>();
		this.decoders = new HashMap<ConfigType, IDecoder<IConfig>>();
		this.registerAll();
	}
	
	/**
	 * ע���������ñ�
	 */
	private void registerAll(){
		
		this.register(ConfigType.GLOBAL_CONFIG, GlobalConfigDecoder.class);
		this.register(ConfigType.DROP, DropConfigDecoder.class);
		this.register(ConfigType.MONSTER_GROUP, MonsterGroupDecoder.class);
		this.register(ConfigType.MONSTER_REFRESH, MonsterRefreshDecoder.class);
		this.register(ConfigType.SHOP, ShopItemDecoder.class);
		this.register(ConfigType.MISSION, MissionDecoder.class);
		this.register(ConfigType.ROLE_LEVEL, RoleLevelDecoder.class);
		this.register(ConfigType.COUNTRY, CountryDecoder.class);
		this.register(ConfigType.BUILDING_POSITION, BuildingPositionDecoder.class);
		this.register(ConfigType.CITY_ROUTE, CityRouteDecoder.class);
		this.register(ConfigType.AREA, AreaDecoder.class);
		this.register(ConfigType.CITY, CityDecoder.class);
		this.register(ConfigType.CITY_TYPE, CityTypeDecoder.class);
		this.register(ConfigType.ITEM, ItemDecoder.class);
		this.register(ConfigType.BUILDING, BuildingDecoder.class);
		this.register(ConfigType.ITEMEXTEND, ItemExtendDecoder.class);
		this.register(ConfigType.HERO, HeroDecoder.class);
	}
	
	/**
	 * ��ȡ������������ص���
	 * @param configType:���ñ�����
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends IConfig> Map<String, T> getConfigMap(ConfigType configType){
		
		IDecoder<T> decoder = (IDecoder<T>) this.decoders.get(configType);
		
		if(decoder == null){
			logger.error(String.format("The decoder of %s is not registered in the ConfigEngine", configType.toString()));
			return null;
		}
		
		String url = this.configUrls.get(configType);
		
		
		Map<String, T> items = this.readTextMap(url, decoder);
		
		return items;
	}
	
	/**
	 * ��ȡ����������
	 * @param configType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends IConfig> List<T> getConfigList(ConfigType configType){
		
		IDecoder<T> decoder = (IDecoder<T>) this.decoders.get(configType);
		
		if(decoder == null){
			logger.error(String.format("The decoder of %s is not registered in the ConfigEngine", configType.toString()));
			return null;
		}
		
		String url = this.configUrls.get(configType);
		
		
		List<T> items = this.readTextList(url, decoder);
		
		return items;
	}
	
	
	/**
	 * ע��һ�����ñ���ʹ��Ĭ�ϵĵ�ַ
	 * @param type
	 * @param clazz
	 * @param decoder
	 */
	private final <T extends IConfig> void register(ConfigType type, Class<?> decoder){
		
		final String url = CONFIG_FOLDER_DIR + type.getValue()+".txt";
		
		this.register(type, decoder, url);
	}
	
	/**
	 * ע��һ�����ñ�
	 * @param type
	 * @param clazz
	 * @param decoder
	 * @param url
	 */
	@SuppressWarnings("unchecked")
	private final <T extends IConfig> void register(ConfigType type, Class<?> decoder, String url){
		
		if(!this.decoders.containsKey(type)){
			
			try {
				
				IDecoder<IConfig> decode = (IDecoder<IConfig>)decoder.newInstance();
				this.decoders.put(type, decode);
				
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
		}
		
		if(!this.configUrls.containsKey(type)){
			this.configUrls.put(type, url);
		}
		
		
	}
	
	/**
	 * ��ȡ������ָ����text�ļ�,������Map��ʽ
	 * @param clazz
	 * @param url
	 * @param decoder
	 * @return
	 */
	private final <T extends IConfig> Map<String, T> readTextMap(String url, IDecoder<T> decoder){
		
		IReader reader = new LineReader();
		
		TextMapParser<T> parser = new TextMapParser<T>(reader, decoder);
		
		try {
			
			Map<String, T> maps = parser.parse(url);
			
			return maps;
			
		} catch (SimpleConfigException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * ��ȡ������ָ����text�ļ�,������Map��ʽ
	 * @param clazz
	 * @param url
	 * @param decoder
	 * @return
	 */
	private final <T extends IConfig> List<T> readTextList(String url, IDecoder<T> decoder){
		
		IReader reader = new LineReader();
		
		TextListParser<T> parser = new TextListParser<T>(reader, decoder);
		
		try {
			
			List<T> list = parser.parse(url);
			
			return list;
			
		} catch (SimpleConfigException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}