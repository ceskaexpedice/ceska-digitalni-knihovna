package org.cas.lib.cdl;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Check if title is accessible in target system
 * @author pstastny
 */
public class CheckAccessible  implements IterationControl {
    
	public static final Logger LOGGER = Logger.getLogger(ChecksEnum.accessible.name());
	
    private int notAcessible = 0;
    
    @Override
    public void onPidsIterate(List<String> pids, String sourceKramerius) throws UnsupportedEncodingException {
        for (String p : pids) {
        	boolean accesible = sourceKramerius != null ? PublicConnectUtils.headFullImgSourcecKramerius(p, sourceKramerius) : PublicConnectUtils.headFullImgSourcecKramerius(p);
            if (!accesible) {
                LOGGER.info("non accessible pid "+p);
                this.notAcessible +=1;
            }
        }
    }

    @Override
    public void printResult() {
    	LOGGER.info("Pocet nedostupnych: "+this. notAcessible);
    }
}
