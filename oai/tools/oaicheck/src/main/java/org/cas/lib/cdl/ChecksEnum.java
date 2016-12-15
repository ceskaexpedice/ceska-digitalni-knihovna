package org.cas.lib.cdl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;


public enum ChecksEnum {

	
	exists {
		@Override
		public List<IterationControl> createControls() {
			return Arrays.asList(new CheckExists());
		}
	},
	accessible {
		@Override
		public List<IterationControl> createControls() {
			return Arrays.asList(new CheckAccessible());
		}
	},
	transformable {
		@Override
		public List<IterationControl> createControls() {
			return Arrays.asList(new CheckTransformationAccessible());
		}
	},
	langelm {
		@Override
		public List<IterationControl> createControls() {
			return Arrays.asList(new CheckContainsLangElement());
		}
	},
	cdkoairec {
		@Override
		public List<IterationControl> createControls() {
			return Arrays.asList(new CheckOAIRecord());
		}
	},
	
	all {
		@Override
		public List<IterationControl> createControls() {
			List<IterationControl> list = new ArrayList<>();
			ChecksEnum[] values = ChecksEnum.values();
			for (ChecksEnum che : values) {
				if (che != all) {
					list.addAll(che.createControls());
				}
			}
			return list;
		}
	};
	
	public abstract List<IterationControl> createControls();
	
	public static List<IterationControl> arguments(String[] args) {
		List<IterationControl> revals = new ArrayList<>();
		for (String arg : args) {
			try {
				revals.addAll(ChecksEnum.valueOf(arg).createControls());
			} catch (Exception e) {
				LOGGER.severe("cannot recognize param '"+arg+"'");
			}
		}
		return revals;
	}
	
	public static Logger LOGGER = Logger.getLogger(ChecksEnum.class.getName());
}
