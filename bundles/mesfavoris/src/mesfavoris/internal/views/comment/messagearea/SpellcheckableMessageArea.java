/*******************************************************************************
 * Copyright (C) 2010, 2015 Benjamin Muskalla <bmuskalla@eclipsesource.com> and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Benjamin Muskalla (EclipseSource) - initial implementation
 *    Tomasz Zarna (IBM) - show whitespace action, bug 371353
 *    Wayne Beaton (Eclipse Foundation) - Bug 433721
 *    Thomas Wolf (Paranor) - Hyperlink syntax coloring; bug 471355
 *******************************************************************************/
package mesfavoris.internal.views.comment.messagearea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.MarginPainter;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BidiSegmentEvent;
import org.eclipse.swt.custom.BidiSegmentListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import mesfavoris.internal.views.comment.messagearea.ActionUtils.UpdateableAction;

/**
 * Text field with support for spellchecking.
 */
public class SpellcheckableMessageArea extends Composite {

	static final int MAX_LINE_WIDTH = 72;

	private final HyperlinkSourceViewer sourceViewer;

	private TextSourceViewerConfiguration configuration;

	private IAction contentAssistAction;

	/**
	 * @param parent
	 * @param initialText
	 */
	public SpellcheckableMessageArea(Composite parent, String initialText) {
		this(parent, initialText, SWT.BORDER);
	}

	/**
	 * @param parent
	 * @param initialText
	 * @param styles
	 */
	public SpellcheckableMessageArea(Composite parent, String initialText,
			int styles) {
		this(parent, initialText, false, styles);
	}

	/**
	 * @param parent
	 * @param initialText
	 * @param readOnly
	 * @param styles
	 */
	public SpellcheckableMessageArea(Composite parent, String initialText,
			boolean readOnly, int styles) {
		super(parent, styles);
		setLayout(new FillLayout());

		AnnotationModel annotationModel = new AnnotationModel();
		sourceViewer = new HyperlinkSourceViewer(this, null,
				SWT.MULTI | SWT.V_SCROLL | SWT.WRAP) {
			@Override
			protected void handleJFacePreferencesChange(
					PropertyChangeEvent event) {
				if (JFaceResources.TEXT_FONT.equals(event.getProperty())) {
					Font jFaceFont = JFaceResources.getTextFont();
					setFont(jFaceFont);
				} else {
					super.handleJFacePreferencesChange(event);
				}
			}
		};
		getTextWidget().setAlwaysShowScrollBars(false);

		sourceViewer.setDocument(new Document());
		int endSpacing = 2;
		int textWidth = (int) (getCharWidth() * MAX_LINE_WIDTH + endSpacing);
		int textHeight = getLineHeight() * 7;
		Point size = getTextWidget().computeSize(textWidth, textHeight);
		getTextWidget().setSize(size);

		computeBrokenBidiPlatformTextWidth(size.x);

		getTextWidget().setEditable(!readOnly);

		createMarginPainter();

		final SourceViewerDecorationSupport support = configureAnnotationPreferences();

		Document document = new Document(initialText);

		configuration = new HyperlinkSourceViewer.Configuration(
				EditorsUI.getPreferenceStore()) {

			@Override
			public int getHyperlinkStateMask(ISourceViewer targetViewer) {
				if (!targetViewer.isEditable()) {
					return SWT.NONE;
				}
				return super.getHyperlinkStateMask(targetViewer);
			}

			@Override
			protected Map<String, IAdaptable> getHyperlinkDetectorTargets(
					ISourceViewer targetViewer) {
				return getHyperlinkTargets();
			}

			@Override
			public IReconciler getReconciler(ISourceViewer viewer) {
				if (!isEditable(viewer))
					return null;
				return super.getReconciler(sourceViewer);
			}

			@Override
			public IContentAssistant getContentAssistant(ISourceViewer viewer) {
				if (!viewer.isEditable())
					return null;
				IContentAssistant assistant = createContentAssistant(viewer);
				// Add content assist proposal handler if assistant exists
				if (assistant != null)
					contentAssistAction = createContentAssistAction(
							sourceViewer);
				return assistant;
			}

			@Override
			public IPresentationReconciler getPresentationReconciler(
					ISourceViewer viewer) {
				PresentationReconciler reconciler = new PresentationReconciler();
				reconciler.setDocumentPartitioning(
						getConfiguredDocumentPartitioning(viewer));
				DefaultDamagerRepairer hyperlinkDamagerRepairer = new DefaultDamagerRepairer(
						new HyperlinkTokenScanner(this, viewer));
				reconciler.setDamager(hyperlinkDamagerRepairer,
						IDocument.DEFAULT_CONTENT_TYPE);
				reconciler.setRepairer(hyperlinkDamagerRepairer,
						IDocument.DEFAULT_CONTENT_TYPE);
				return reconciler;
			}

		};

		sourceViewer.configure(configuration);
		sourceViewer.setDocument(document, annotationModel);

		configureContextMenu();

		getTextWidget().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent disposeEvent) {
				support.uninstall();
			}
		});
	}

	private void computeBrokenBidiPlatformTextWidth(int textWidth) {
		class BidiSegmentListenerTester implements BidiSegmentListener {
			boolean called;

			@Override
			public void lineGetSegments(BidiSegmentEvent event) {
				called = true;
			}
		}
		BidiSegmentListenerTester tester = new BidiSegmentListenerTester();
		StyledText textWidget = getTextWidget();
		textWidget.addBidiSegmentListener(tester);
		textWidget.setText(" "); //$NON-NLS-1$
		textWidget.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		textWidget.removeBidiSegmentListener(tester);
	}

	private boolean isEditable(ISourceViewer viewer) {
		return viewer != null && viewer.getTextWidget().getEditable();
	}

	private void configureContextMenu() {
		final boolean editable = isEditable(sourceViewer);
		IAction quickFixAction = null;
		if (editable) {
			quickFixAction = new QuickfixAction(sourceViewer);
		}

//		final ShowWhitespaceAction showWhitespaceAction = new ShowWhitespaceAction(
//				sourceViewer, !editable);
		MenuManager contextMenu = new MenuManager();
		UpdateableAction[] standardActions = ActionUtils
				.fillStandardTextActions(sourceViewer, editable, contextMenu);
		contextMenu.add(new Separator());
//		contextMenu.add(showWhitespaceAction);
//		contextMenu.add(new Separator());

		if (editable) {
			final SubMenuManager quickFixMenu = new SubMenuManager(contextMenu);
			quickFixMenu.setVisible(true);
			quickFixMenu.addMenuListener(new IMenuListener() {
				@Override
				public void menuAboutToShow(IMenuManager manager) {
					quickFixMenu.removeAll();
					addProposals(quickFixMenu);
				}
			});
		}

		final StyledText textWidget = getTextWidget();
		List<IAction> globalActions = new ArrayList<>();
		globalActions.addAll(Arrays.asList(standardActions));
		if (quickFixAction != null) {
			globalActions.add(quickFixAction);
		}
		if (contentAssistAction != null) {
			globalActions.add(contentAssistAction);
		}
		ActionUtils.setGlobalActions(textWidget, globalActions,
				getHandlerService());

		textWidget.setMenu(contextMenu.createContextMenu(textWidget));

		sourceViewer.addSelectionChangedListener(event -> {
			if (standardActions[ITextOperationTarget.CUT] != null) {
				standardActions[ITextOperationTarget.CUT].update();
			}
			standardActions[ITextOperationTarget.COPY].update();
		});

		if (editable) {
			sourceViewer.addTextListener(event -> {
				if (standardActions[ITextOperationTarget.UNDO] != null) {
					standardActions[ITextOperationTarget.UNDO].update();
				}
				if (standardActions[ITextOperationTarget.REDO] != null) {
					standardActions[ITextOperationTarget.REDO].update();
				}
			});
		}

//		textWidget.addDisposeListener(e -> showWhitespaceAction.dispose());
	}

	private void addProposals(final SubMenuManager quickFixMenu) {
		IAnnotationModel sourceModel = sourceViewer.getAnnotationModel();
		if (sourceModel == null) {
			return;
		}
		Iterator annotationIterator = sourceModel.getAnnotationIterator();
		while (annotationIterator.hasNext()) {
			Annotation annotation = (Annotation) annotationIterator.next();
			boolean isDeleted = annotation.isMarkedDeleted();
			boolean isIncluded = !isDeleted
					&& includes(sourceModel.getPosition(annotation),
							getTextWidget().getCaretOffset());
			boolean isFixable = isIncluded && sourceViewer
					.getQuickAssistAssistant().canFix(annotation);
			if (isFixable) {
				IQuickAssistProcessor processor = sourceViewer
						.getQuickAssistAssistant().getQuickAssistProcessor();
				IQuickAssistInvocationContext context = sourceViewer
						.getQuickAssistInvocationContext();
				ICompletionProposal[] proposals = processor
						.computeQuickAssistProposals(context);

				for (ICompletionProposal proposal : proposals) {
					quickFixMenu.add(createQuickFixAction(proposal));
				}
			}
		}
	}

	private boolean includes(Position position, int caretOffset) {
		return position != null && (position.includes(caretOffset)
				|| (position.offset + position.length) == caretOffset);
	}

	private IAction createQuickFixAction(final ICompletionProposal proposal) {
		return new Action(proposal.getDisplayString()) {

			@Override
			public void run() {
				proposal.apply(sourceViewer.getDocument());
			}

			@Override
			public ImageDescriptor getImageDescriptor() {
				Image image = proposal.getImage();
				if (image != null)
					return ImageDescriptor.createFromImage(image);
				return null;
			}
		};
	}

	/**
	 * Return <code>IHandlerService</code>. The default implementation uses the
	 * workbench window's service locator. Subclasses may override to access the
	 * service by using a local service locator.
	 *
	 * @return <code>IHandlerService</code> using the workbench window's service
	 *         locator. Can be <code>null</code> if the service could not be
	 *         found.
	 */
	protected IHandlerService getHandlerService() {
		return PlatformUI.getWorkbench().getService(IHandlerService.class);
	}

	private SourceViewerDecorationSupport configureAnnotationPreferences() {
		ISharedTextColors textColors = EditorsUI.getSharedTextColors();
		IAnnotationAccess annotationAccess = new DefaultMarkerAnnotationAccess();
		final SourceViewerDecorationSupport support = new SourceViewerDecorationSupport(
				sourceViewer, null, annotationAccess, textColors);

		List<AnnotationPreference> annotationPreferences = new MarkerAnnotationPreferences()
				.getAnnotationPreferences();
		annotationPreferences.iterator()
				.forEachRemaining(support::setAnnotationPreference);

		support.install(EditorsUI.getPreferenceStore());
		return support;
	}

	/**
	 * Create margin painter and add to source viewer
	 */
	protected void createMarginPainter() {
		MarginPainter marginPainter = new MarginPainter(sourceViewer);
		marginPainter.setMarginRulerColumn(MAX_LINE_WIDTH);
		marginPainter.setMarginRulerColor(PlatformUI.getWorkbench().getDisplay().getSystemColor(
				SWT.COLOR_GRAY));
		sourceViewer.addPainter(marginPainter);
	}

	private double getCharWidth() {
		GC gc = new GC(getTextWidget());
		double charWidth = gc.getFontMetrics().getAverageCharacterWidth();
		gc.dispose();
		return charWidth;
	}

	private int getLineHeight() {
		return getTextWidget().getLineHeight();
	}

	/**
	 * @return widget
	 */
	public StyledText getTextWidget() {
		return sourceViewer.getTextWidget();
	}

	private static class QuickfixAction extends Action {

		private final ITextOperationTarget textOperationTarget;

		public QuickfixAction(ITextOperationTarget target) {
			textOperationTarget = target;
			setActionDefinitionId(
					ITextEditorActionDefinitionIds.QUICK_ASSIST);
		}

		@Override
		public void run() {
			if (textOperationTarget.canDoOperation(ISourceViewer.QUICK_ASSIST)) {
				textOperationTarget.doOperation(ISourceViewer.QUICK_ASSIST);
			}
		}

	}

	private IAction createContentAssistAction(
			final SourceViewer viewer) {
		Action proposalAction = new Action() {

			@Override
			public void run() {
				viewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
			}

		};
		proposalAction
				.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		return proposalAction;
	}

	/**
	 * Get hyperlink targets
	 *
	 * @return map of targets
	 */
	protected Map<String, IAdaptable> getHyperlinkTargets() {
		return Collections.singletonMap(EditorsUI.DEFAULT_TEXT_EDITOR_ID,
				getDefaultTarget());
	}

	/**
	 * Create content assistant
	 *
	 * @param viewer
	 * @return content assistant
	 */
	protected IContentAssistant createContentAssistant(ISourceViewer viewer) {
		return null;
	}

	/**
	 * Get default target for hyperlink presenter
	 *
	 * @return target
	 */
	protected IAdaptable getDefaultTarget() {
		return null;
	}

	/**
	 * @return text
	 */
	public String getText() {
		return getDocument().get();
	}

	/**
	 * @return document
	 */
	public IDocument getDocument() {
		return sourceViewer.getDocument();
	}

	/**
	 * @param text
	 */
	public void setText(String text) {
		if (text != null) {
			getDocument().set(text);
		}
	}

	/**
	 * Set the same background color to the styledText widget as the Composite
	 */
	@Override
	public void setBackground(Color color) {
		super.setBackground(color);
		StyledText textWidget = getTextWidget();
		textWidget.setBackground(color);
	}

	/**
	 *
	 */
	@Override
	public boolean forceFocus() {
		StyledText text = getTextWidget();
		if (text == null || text.isDisposed()) {
			return false;
		}
		return text.setFocus();
	}
	
	public HyperlinkSourceViewer getSourceViewer() {
		return sourceViewer;
	}
}
