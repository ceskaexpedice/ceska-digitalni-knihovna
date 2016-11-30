package org.cas.lib.cdl;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ContextAware {
	
	public List<List<String>> paths(String pid) throws ExecutionException;

	public List<String> pathsSelect(String pid) throws ExecutionException;
	
}
