package mesfavoris.texteditor.text;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;

public class DocumentUtils {

	public static IDocument getDocument(IPath fileSystemPath) throws CoreException {
		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
		boolean connected = false;
		try {
			ITextFileBuffer buffer = manager.getTextFileBuffer(fileSystemPath, LocationKind.NORMALIZE);
			if (buffer == null) {
				// no existing file buffer..create one
				manager.connect(fileSystemPath, LocationKind.NORMALIZE, new NullProgressMonitor());
				connected = true;
				buffer = manager.getTextFileBuffer(fileSystemPath, LocationKind.NORMALIZE);
				if (buffer == null) {
					return null;
				}
			}
			return buffer.getDocument();
		} finally {
			if (connected) {
				manager.disconnect(fileSystemPath, LocationKind.NORMALIZE, new NullProgressMonitor());
			}
		}
	}

}
