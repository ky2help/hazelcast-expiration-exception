package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryRemovedListener;

public class ExampleCacheEntryListener implements CacheEntryRemovedListener<String, Integer>, CacheEntryExpiredListener<String, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(ExampleCacheEntryListener.class);

    @Override
    public void onExpired(Iterable<CacheEntryEvent<? extends String, ? extends Integer>> cacheEntryEvents) throws CacheEntryListenerException {
        logger.info("Entry expired");
    }

    @Override
    public void onRemoved(Iterable<CacheEntryEvent<? extends String, ? extends Integer>> cacheEntryEvents) throws CacheEntryListenerException {
        logger.info("Entry removed");
    }

}
