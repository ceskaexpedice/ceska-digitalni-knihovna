package cz.incad.kramerius.utils;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import cz.cas.lib.knav.ApplyMWUtils;
import cz.cas.lib.knav.ApplyMovingWall;
import cz.cas.lib.knav.ProcessCriteriumContext;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.impl.criteria.MovingWall;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class OAIMWUtils {

	public static boolean process(FedoraAccess fa, SolrAccess sa, String onePid, String userValue)
			throws IOException, RightCriteriumException, XPathExpressionException {
		ProcessCriteriumContext ctx = new ProcessCriteriumContext(onePid, fa, sa);
		MovingWall mw = new MovingWall();
		mw.setEvaluateContext(ctx);
		int wall = 0;
		if (userValue != null) {
			try {
				wall = Integer.parseInt(userValue);
			} catch (NumberFormatException e) {
				ApplyMovingWall.LOGGER.severe("Cannot parse user value");
				ApplyMovingWall.LOGGER.severe(e.getMessage());
				return false;
			}
		} else {
			wall = ApplyMWUtils.configuredWall(sa, onePid, KConfiguration.getInstance().getConfiguration());
		}
		ApplyMovingWall.LOGGER.info("Used value is: " + wall);
		mw.setCriteriumParamValues(new Object[] { "" + wall });
		EvaluatingResult result = mw.evalute();
		if (result == EvaluatingResult.TRUE) {
			return true;
		} else if (result == EvaluatingResult.FALSE) {
			return false;
		} else {
			// wrong data
			ApplyMovingWall.LOGGER.warning("cannot set flag for pid " + onePid);
			return false;
		}
	}

}
