package org.cas.lib.cdl;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CachedAccess<T,K> {
	
	T get(K k) throws ExecutionException;

	List<T> getForPath(K k, ContextAware contextAware) throws ExecutionException;
	
	
}
