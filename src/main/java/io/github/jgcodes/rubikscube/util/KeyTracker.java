package io.github.jgcodes.rubikscube.util;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.EnumSet;
import java.util.Set;

public class KeyTracker implements EventHandler<KeyEvent> {
	private final Set<KeyCode> keysDown = EnumSet.noneOf(KeyCode.class);
	@Override
	public void handle(KeyEvent event) {
		if (event.getEventType() == KeyEvent.KEY_PRESSED) {
			keysDown.add(event.getCode());
		}
		else if (event.getEventType() == KeyEvent.KEY_RELEASED) {
			keysDown.remove(event.getCode());
		}
	}

	/**
	 * Checks whether a key is pressed.
	 * @param code the {@link KeyCode} representing the key
	 * @return true if the key is pressed (in the window's context)
	 */
	public boolean isPressed(KeyCode code) {
		return keysDown.contains(code);
	}
}
