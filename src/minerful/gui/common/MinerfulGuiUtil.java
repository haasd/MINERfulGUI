package minerful.gui.common;

import minerful.params.InputLogCmdParameters.InputEncoding;

public class MinerfulGuiUtil {
	
		// determine File encoding
		public static InputEncoding determineEncoding(String path) {
			switch(path) {
				case "txt": return InputEncoding.strings;
				case "mxml": return InputEncoding.mxml;
				default: return InputEncoding.xes;
			}
		}

}
