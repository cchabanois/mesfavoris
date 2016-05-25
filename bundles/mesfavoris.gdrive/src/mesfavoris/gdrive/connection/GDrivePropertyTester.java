package mesfavoris.gdrive.connection;

import org.eclipse.core.expressions.PropertyTester;

public class GDrivePropertyTester extends PropertyTester {

	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		
//		ILaunchJob launchJob = AdapterUtils.getAdapter(receiver, ILaunchJob.class);
//		if ("isRunning".equals(property)) {
//			return launchJob.isRunning();
//		}
		return false;
	}

}
