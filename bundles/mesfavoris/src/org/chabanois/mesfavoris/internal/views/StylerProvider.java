package org.chabanois.mesfavoris.internal.views;

import java.util.HashMap;
import java.util.Map;

import javax.swing.text.StyledEditorKit.ForegroundAction;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;

public class StylerProvider {
	private final Map<StylerDescriptor, Styler> stylers = new HashMap<StylerDescriptor, Styler>();

	public Styler getStyler(Font font, Color foreground, Color backGround) {
		StylerDescriptor descriptor = new StylerDescriptor(font, foreground,
				backGround);
		Styler styler = stylers.get(descriptor);
		if (styler != null) {
			return styler;
		}
		styler = new FontColorStyler(descriptor);
		stylers.put(descriptor, styler);
		return styler;
	}

	private static class FontColorStyler extends Styler {
		private final StylerDescriptor stylerDescriptor;

		public FontColorStyler(StylerDescriptor stylerDescriptor) {
			this.stylerDescriptor = stylerDescriptor;
		}

		@Override
		public void applyStyles(TextStyle textStyle) {
			if (stylerDescriptor.font != null) {
				textStyle.font = stylerDescriptor.font;
			}
			if (stylerDescriptor.foreground != null) {
				textStyle.foreground = stylerDescriptor.foreground;
			}
			if (stylerDescriptor.backGround != null) {
				textStyle.background = stylerDescriptor.backGround;
			}
		}

	}

	private static class StylerDescriptor {
		private final Font font;
		private final Color foreground;
		private final Color backGround;

		public StylerDescriptor(Font font, Color foreground, Color backGround) {
			super();
			this.font = font;
			this.foreground = foreground;
			this.backGround = backGround;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((backGround == null) ? 0 : backGround.hashCode());
			result = prime * result + ((font == null) ? 0 : font.hashCode());
			result = prime * result
					+ ((foreground == null) ? 0 : foreground.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StylerDescriptor other = (StylerDescriptor) obj;
			if (backGround == null) {
				if (other.backGround != null)
					return false;
			} else if (!backGround.equals(other.backGround))
				return false;
			if (font == null) {
				if (other.font != null)
					return false;
			} else if (!font.equals(other.font))
				return false;
			if (foreground == null) {
				if (other.foreground != null)
					return false;
			} else if (!foreground.equals(other.foreground))
				return false;
			return true;
		}

	}

}
