package org.chabanois.mesfavoris.internal.views;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.MultipleHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public class BookmarkCommentViewerConfiguration extends
		TextSourceViewerConfiguration {
	BookmarkCommentViewerConfiguration(
			IPreferenceStore preferenceStore) {
		super(preferenceStore);
	}

	public int getHyperlinkStateMask(ISourceViewer targetViewer) {
		return SWT.NONE;
	}

	@Override
	public IHyperlinkPresenter getHyperlinkPresenter(
			ISourceViewer targetViewer) {
		return new MultipleHyperlinkPresenter(PlatformUI.getWorkbench()
				.getDisplay().getSystemColor(SWT.COLOR_BLUE).getRGB()) {

			@Override
			public void hideHyperlinks() {
				// We want links to always show.
			}

		};
	}

	public IHyperlinkDetector[] getHyperlinkDetectors(
			ISourceViewer targetViewer) {
		return new IHyperlinkDetector[] { new URLHyperlinkDetector() }; // getRegisteredHyperlinkDetectors(sourceViewer);
	}
}