package mesfavoris.texteditor.internal.preferences;

import static mesfavoris.texteditor.Constants.PLACEHOLDER_HOME_NAME;

import java.text.MessageFormat;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import mesfavoris.texteditor.placeholders.PathPlaceholder;

public class PathPlaceholderTableLabelProvider extends LabelProvider implements IStyledLabelProvider, IColorProvider {
	private final PathPlaceholderStats placeholderStats;

	public PathPlaceholderTableLabelProvider(PathPlaceholderStats placeholderStats) {
		this.placeholderStats = placeholderStats;
	}

	@Override
	public StyledString getStyledText(Object element) {
		PathPlaceholder pathPlaceholder = (PathPlaceholder) element;
		StyledString sb = new StyledString();
		sb.append(pathPlaceholder.getName());
		sb.append(" (");
		if (isUnmodifiable(pathPlaceholder)) {
			sb.append("non modifiable, ");
		}
		sb.append(
				MessageFormat.format("{0} matches",
						Integer.toString(placeholderStats.getUsageCount(pathPlaceholder.getName()))),
				StyledString.COUNTER_STYLER);
		sb.append(')');
		sb.append(" - ");
		if (pathPlaceholder.getPath() != null) {
			sb.append(pathPlaceholder.getPath().toString());
		}
		return sb;
	}

	@Override
	public String getText(Object element) {
		return getStyledText(element).toString();
	}

	@Override
	public Image getImage(Object element) {
		return super.getImage(element);
	}

	@Override
	public Color getForeground(Object element) {
		PathPlaceholder pathPlaceholder = (PathPlaceholder) element;
		if (isUnmodifiable(pathPlaceholder)) {
			Display display = Display.getCurrent();
			return display.getSystemColor(SWT.COLOR_INFO_FOREGROUND);
		}
		return null;
	}

	@Override
	public Color getBackground(Object element) {
		PathPlaceholder pathPlaceholder = (PathPlaceholder) element;
		if (isUnmodifiable(pathPlaceholder)) {
			Display display = Display.getCurrent();
			return display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		}
		return null;
	}

	private boolean isUnmodifiable(PathPlaceholder pathPlaceholder) {
		return PLACEHOLDER_HOME_NAME.equals(pathPlaceholder.getName());
	}

}