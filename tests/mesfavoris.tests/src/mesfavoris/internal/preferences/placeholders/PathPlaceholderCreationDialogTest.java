package mesfavoris.internal.preferences.placeholders;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

import mesfavoris.internal.placeholders.PathPlaceholdersMap;
import mesfavoris.placeholders.IPathPlaceholders;
import mesfavoris.placeholders.PathPlaceholder;
import mesfavoris.tests.commons.ui.AbstractDialogTest;

public class PathPlaceholderCreationDialogTest extends AbstractDialogTest {

	private PathPlaceholderCreationDialog dialog;
	private IPathPlaceholders pathPlaceholders = new PathPlaceholdersMap();

	@Test
	public void testNewPathPlaceholder() {
		// Given
		openDialog(shell -> createNewPlaceholderDialog(shell, Optional.empty(), Optional.empty()));

		// When
		bot.text(0).setText("PROJECT");
		bot.text(1).setText("/home/cchabanois/myProject");
		clickOkButton();

		// Then
		assertEquals(new PathPlaceholder("PROJECT", Path.forPosix("/home/cchabanois/myProject")),
				dialog.getPathPlaceholder());
	}

	@Test
	public void testNewPathPlaceholderWithPredefinedNameAndValue() {
		// Given
		openDialog(shell -> createNewPlaceholderDialog(shell, Optional.of("PROJECT"),
				Optional.of(Path.forPosix("/home/cchabanois/myProject"))));

		// When
		clickOkButton();

		// Then
		assertEquals(new PathPlaceholder("PROJECT", Path.forPosix("/home/cchabanois/myProject")),
				dialog.getPathPlaceholder());
	}

	@Test
	public void testEditPathPlaceholder() {
		// Given
		PathPlaceholder pathPlaceholder = new PathPlaceholder("PROJECT", Path.forPosix("/home/cchabanois/myProject"));
		openDialog(shell -> createEditPlaceholderDialog(shell, pathPlaceholder));
		assertEquals("PROJECT", bot.text(0).getText());
		assertEquals("/home/cchabanois/myProject", bot.text(1).getText());

		// When
		bot.text(1).setText("/home/cchabanois/myProject2");
		clickOkButton();

		// Then
		assertEquals(new PathPlaceholder("PROJECT", Path.forPosix("/home/cchabanois/myProject2")),
				dialog.getPathPlaceholder());
	}

	private PathPlaceholderCreationDialog createNewPlaceholderDialog(Shell shell, Optional<String> initialName,
			Optional<IPath> initialPath) {
		dialog = new PathPlaceholderCreationDialog(shell, pathPlaceholders, initialName, initialPath);
		return dialog;
	}

	private PathPlaceholderCreationDialog createEditPlaceholderDialog(Shell shell, PathPlaceholder pathPlaceholder) {
		dialog = new PathPlaceholderCreationDialog(shell, pathPlaceholders, pathPlaceholder);
		return dialog;
	}

}
