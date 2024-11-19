package org.example;

import com.hazelcast.cache.HazelcastCachingProvider;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spi.properties.ClusterProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ExampleRunnable implements Runnable, Closeable {

    private static final Logger logger = LoggerFactory.getLogger(ExampleRunnable.class);

    private HazelcastInstance hazelcastInstance;
    private CacheManager cacheManager;
    private Cache<String, Integer> cache;

    @Override
    public void run() {
        // Initialize JCache and Hazelcast
        init();

        // Add cache values
        populate();

        // Print cache values
        print();
    }

    @Override
    public void close() throws IOException {
        cache.close();
        cacheManager.close();
        hazelcastInstance.shutdown();
    }

    private void init() {
        System.setProperty(ClusterProperty.JCACHE_PROVIDER_TYPE.getName(), "member");
        System.setProperty(ClusterProperty.LOGGING_TYPE.getName(), "slf4j");
        System.setProperty(ClusterProperty.PHONE_HOME_ENABLED.getName(), "false");
        Config hazelcastConfig = new XmlConfigBuilder().build();
        hazelcastConfig.setInstanceName(HazelcastCachingProvider.SHARED_JCACHE_INSTANCE_NAME);
        hazelcastConfig.setClusterName("hazelcast-expiration-exception");
        hazelcastInstance = Hazelcast.getOrCreateHazelcastInstance(hazelcastConfig);

        cacheManager = Caching.getCachingProvider().getCacheManager();

        // A cache configuration whose entries expire after access is required to trigger the EXPIRATION_TIME_UPDATED event
        MutableConfiguration<String, Integer> cacheConfig = new MutableConfiguration<String, Integer>()
                .setTypes(String.class, Integer.class)
                .setStoreByValue(true)
                .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, 10)))
                .setStatisticsEnabled(false);
        cache = cacheManager.createCache("cache", cacheConfig);

        // A cache entry listener (or WAN replication) is required to enable event publishing (see AbstractCacheRecordStore.isEventsEnabled),
        // which eventually leads to the IllegalArgumentException when accessing entries.
        MutableCacheEntryListenerConfiguration<String, Integer> listenerConfig = new MutableCacheEntryListenerConfiguration<>(
                FactoryBuilder.factoryOf(ExampleCacheEntryListener.class), FactoryBuilder.factoryOf(ExampleCacheEntryEventFilter.class),
                false, true);
        cache.registerCacheEntryListener(listenerConfig);
    }

    private void populate() {
        cache.put("1", 100);
        cache.put("2", 200);
    }

    private void print() {
        // cache.get triggers an IllegalArgumentException in CacheEventHandler.publishEvent, which is caught and ignored in AbstractCacheStore:
        //   java.lang.IllegalArgumentException: Event Type not defined to create an eventData during publish: EXPIRATION_TIME_UPDATED
        // This can be verified by setting an exception breakpoint on IllegalArgumentException before cache.get is called
        Integer firstAccess = cache.get("1");
        logger.info("Value for entry 1: {}", firstAccess);

        // This will trigger another IllegalArgumentException at the same location
        Integer secondAccess = cache.get("1");
        logger.info("Value for entry 1: {}", secondAccess);
    }

}
