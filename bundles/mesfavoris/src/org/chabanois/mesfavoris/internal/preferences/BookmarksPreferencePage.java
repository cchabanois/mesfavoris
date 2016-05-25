package org.chabanois.mesfavoris.internal.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class BookmarksPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public BookmarksPreferencePage() {
		setDescription("Expand the tree to edit preferences for a specific feature.");
	    noDefaultAndApplyButton();
	}
	
	@Override
	public void init(IWorkbench workbench) {
		
	}

	@Override
	protected Control createContents(Composite parent) {
		return new Composite(parent, SWT.NULL);
	}

}
