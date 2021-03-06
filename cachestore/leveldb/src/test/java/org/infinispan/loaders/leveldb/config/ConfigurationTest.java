package org.infinispan.loaders.leveldb.config;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheLoaderConfiguration;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.loaders.leveldb.LevelDBCacheStore;
import org.infinispan.loaders.leveldb.LevelDBCacheStoreConfig.ImplementationType;
import org.infinispan.loaders.leveldb.configuration.LevelDBCacheStoreConfiguration;
import org.infinispan.loaders.leveldb.configuration.LevelDBCacheStoreConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.test.AbstractInfinispanTest;
import org.infinispan.test.TestingUtil;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * 
 * @author <a href="mailto:rtsang@redhat.com">Ray Tsang</a>
 * 
 */
@Test(groups = "unit", testName = "loaders.leveldb.configuration.ConfigurationTest")
public class ConfigurationTest extends AbstractInfinispanTest {
   private String tmpDirectory;
   private String tmpDataDirectory;
   private String tmpExpiredDirectory;
   
   protected ImplementationType getImplementationType() {
      return ImplementationType.AUTO;
   }
   
   @BeforeTest
   protected void setUpTempDir() {
      tmpDirectory = TestingUtil.tmpDirectory(this);
      tmpDataDirectory = tmpDirectory + "/data";
      tmpExpiredDirectory = tmpDirectory + "/expired";
   }

   @AfterTest(alwaysRun = true)
   protected void clearTempDir() {
      TestingUtil.recursiveFileRemove(tmpDirectory);
   }

	public void testConfigBuilder() {
		GlobalConfiguration globalConfig = new GlobalConfigurationBuilder()
				.globalJmxStatistics().transport().defaultTransport().build();

		Configuration cacheConfig = new ConfigurationBuilder().loaders()
				.addLoader(LevelDBCacheStoreConfigurationBuilder.class)
				.location(tmpDataDirectory)
				.expiredLocation(tmpExpiredDirectory)
				.implementationType(getImplementationType())
				.build();
		
		
		CacheLoaderConfiguration cacheLoaderConfig = cacheConfig.loaders().cacheLoaders().get(0);
		assertTrue(cacheLoaderConfig instanceof LevelDBCacheStoreConfiguration);
		LevelDBCacheStoreConfiguration leveldbConfig = (LevelDBCacheStoreConfiguration) cacheLoaderConfig;
		assertEquals(tmpDataDirectory, leveldbConfig.location());
		assertEquals(tmpExpiredDirectory, leveldbConfig.expiredLocation());

		EmbeddedCacheManager cacheManager = new DefaultCacheManager(
				globalConfig);

		cacheManager.defineConfiguration("testCache", cacheConfig);

		cacheManager.start();
		Cache<String, String> cache = cacheManager.getCache("testCache");
		
		cache.put("hello", "there");
		cache.stop();
		cacheManager.stop();
	}

	public void testLegacyJavaConfig() {
		GlobalConfiguration globalConfig = new GlobalConfigurationBuilder()
				.globalJmxStatistics().transport().defaultTransport().build();

		Configuration cacheConfig = new ConfigurationBuilder()
				.loaders()
				.addStore()
				.cacheStore(new LevelDBCacheStore())
				.addProperty("location", tmpDataDirectory)
				.addProperty("expiredLocation", tmpExpiredDirectory)
				.addProperty("implementationType", getImplementationType().toString())
				.build();

		EmbeddedCacheManager cacheManager = new DefaultCacheManager(
				globalConfig);

		cacheManager.defineConfiguration("testCache", cacheConfig);

      cacheManager.start();
      Cache<String, String> cache = cacheManager.getCache("testCache");
      
      cache.put("hello", "there legacy java");
		cache.stop();
		cacheManager.stop();
	}

	public void textXmlConfigLegacy() throws IOException {
		EmbeddedCacheManager cacheManager = new DefaultCacheManager(
				"config/leveldb-config-legacy-" + getImplementationType().toString().toLowerCase() + ".xml");
		Cache<String, String> cache = cacheManager.getCache("testCache");
		
		cache.put("hello", "there legacy xml");
      cache.stop();
      cacheManager.stop();
      
      TestingUtil.recursiveFileRemove("/tmp/leveldb/legacy");
	}
	
	public void testXmlConfig52() throws IOException {
		EmbeddedCacheManager cacheManager = new DefaultCacheManager(
            "config/leveldb-config-52-" + getImplementationType().toString().toLowerCase() + ".xml");
		
		Cache<String, String> cache = cacheManager.getCache("testCache");
		
		cache.put("hello", "there 52 xml");
      cache.stop();
		cacheManager.stop();
		
		TestingUtil.recursiveFileRemove("/tmp/leveldb/52");
	}
	
}
