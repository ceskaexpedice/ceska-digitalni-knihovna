package org.cas.lib.cdl;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class CheckAccessible  implements IterationControl {
    
    private int notAcessible = 0;
    
    @Override
    public void onPidsIterate(List<String> pids) throws UnsupportedEncodingException {
        for (String p : pids) {
            boolean accesible = PublicConnectUtils.headFullImgSourcecKramerius(p);
            if (!accesible) {
                System.out.println("non accessible pid "+p);
                this.notAcessible +=1;
            }
        }
    }

    @Override
    public void printResult() {
        System.out.println("Pocet nedostupnych: "+this. notAcessible);
    }
}
