package org.example;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryListenerException;

public class ExampleCacheEntryEventFilter implements CacheEntryEventFilter<String, Integer> {

    @Override
    public boolean evaluate(CacheEntryEvent<? extends String, ? extends Integer> event) throws CacheEntryListenerException {
        return true;
    }

}
