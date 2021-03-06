package org.infinispan.loaders.remote;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.infinispan.api.BasicCacheContainer;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.commons.CacheException;
import org.infinispan.commons.executors.ExecutorFactory;
import org.infinispan.loaders.AbstractCacheStoreConfig;
import org.infinispan.loaders.remote.logging.Log;
import org.infinispan.loaders.remote.wrapper.DefaultEntryWrapper;
import org.infinispan.loaders.remote.wrapper.EntryWrapper;
import org.infinispan.loaders.remote.wrapper.HotRodEntryMarshaller;
import org.infinispan.manager.CacheContainer;
import org.infinispan.commons.util.FileLookup;
import org.infinispan.commons.util.FileLookupFactory;
import org.infinispan.commons.util.TypedProperties;
import org.infinispan.commons.util.Util;
import org.infinispan.util.logging.LogFactory;

/**
 * Configuration for RemoteCacheStore.
 * <p/>
 * Parameters:
 * <ul>
 * <li><tt>HotRodClientPropertiesFile</tt> - the file that contains the configuration of Hot Rod client. See <a href="http://community.jboss.org/wiki/JavaHotRodclient">Hotrod Java Client</a> for more details on the Hot Rod client.
 * <li><tt>RemoteCacheName</tt> - the name of the remote cache in the remote infinispan cluster, to which to connect to</li>
 * <li><tt>UseDefaultRemoteCache</tt> - if set to true, the default remote cache will be used, as obtained by {@link org.infinispan.manager.CacheContainer#getCache()}.
 * </ul>
 *
 * @author Mircea.Markus@jboss.com
 * @since 4.1
 */
public class RemoteCacheStoreConfig extends AbstractCacheStoreConfig {

   private volatile String remoteCacheName;
   private boolean rawValues;
   private boolean hotRodWrapping;
   private static final Log log = LogFactory.getLog(RemoteCacheStoreConfig.class, Log.class);
   private final Properties hotRodClientProperties = new Properties();
   private ExecutorFactory asyncExecutorFactory = null;
   private EntryWrapper<?, ?> entryWrapper = new DefaultEntryWrapper();

   public RemoteCacheStoreConfig() {
      setCacheLoaderClassName(RemoteCacheStore.class.getName());
   }

   public void setRemoteCacheName(String remoteCacheName) {
      this.remoteCacheName = remoteCacheName;
      setProperty("remoteCacheName", remoteCacheName);
   }

   public String getRemoteCacheName() {
      return remoteCacheName;
   }

   public void setRawValues(boolean rawValues) {
      this.rawValues = rawValues;
      setProperty("rawValues", Boolean.toString(rawValues));
   }

   public boolean isRawValues() {
      return rawValues;
   }

   public boolean isHotRodWrapping() {
      return hotRodWrapping;
   }

   public void setHotRodWrapping(boolean hotRodWrapping) {
      this.hotRodWrapping = hotRodWrapping;
      setProperty("hotRodWrapping", Boolean.toString(hotRodWrapping));
      if (hotRodWrapping) {
         this.setRawValues(true);
         this.getHotRodClientProperties().put(ConfigurationProperties.MARSHALLER, HotRodEntryMarshaller.class.getName());
         try {
            this.setEntryWrapper((EntryWrapper<?, ?>) Util.getInstanceStrict("org.infinispan.loaders.remote.wrapper.HotRodEntryWrapper", getClassLoader()));
         } catch (Exception e) {
            throw log.cannotLoadHotRodEntryWrapper(e);
         }
      }
   }

   public void setUseDefaultRemoteCache(boolean useDefaultRemoteCache) {
      if (useDefaultRemoteCache) {
         setRemoteCacheName(BasicCacheContainer.DEFAULT_CACHE_NAME);
      }
      setProperty("useDefaultRemoteCache", Boolean.toString(useDefaultRemoteCache));
   }

   public boolean isUseDefaultRemoteCache() {
      return CacheContainer.DEFAULT_CACHE_NAME.equals(getRemoteCacheName());
   }

   public Properties getHotRodClientProperties() {
      // Copy properties into Hot Rod client properties for adaptation
      // between configuration objects to work
      hotRodClientProperties.putAll(properties);
      return hotRodClientProperties;
   }

   public void setHotRodClientProperties(Properties props) {
      hotRodClientProperties.putAll(props);
      // Store properties in main properties too to allow properties to be shipped
      for (Map.Entry<Object, Object> entry : props.entrySet())
         setProperty(entry.getKey().toString(), entry.getValue().toString());
   }

   public ExecutorFactory getAsyncExecutorFactory() {
      return asyncExecutorFactory;
   }

   public void setAsyncExecutorFactory(ExecutorFactory asyncExecutorFactory) {
      this.asyncExecutorFactory = asyncExecutorFactory;
   }

   public EntryWrapper<?, ?> getEntryWrapper() {
      return entryWrapper;
   }

   public void setEntryWrapper(EntryWrapper<?, ?> entryWrapper) {
      this.entryWrapper = entryWrapper;
   }

   public void setHotRodClientPropertiesFile(String hotRodClientPropertiesFile) {
      FileLookup fileLookup = FileLookupFactory.newInstance();
      InputStream inputStream = fileLookup.lookupFile(hotRodClientPropertiesFile, getClassLoader());
      try {
         hotRodClientProperties.load(inputStream);
      } catch (IOException e) {
         log.error("Issues while loading properties from file " + hotRodClientPropertiesFile, e);
         throw new CacheException(e);
      } finally {
         Util.close(inputStream);
      }
   }

   private void setProperty(String key, String value) {
      Properties p = getProperties();
      try {
         p.setProperty(key, value);
      } catch (UnsupportedOperationException e) {
         // Most likely immutable, so let's work around that
         TypedProperties writableProperties = new TypedProperties(p);
         writableProperties.setProperty(key, value);
         setProperties(writableProperties);
      }
   }

}
