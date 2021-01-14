package io.github.jgcodes.rubikscube.impl;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.collections.FXCollections;
import javafx.geometry.Point3D;
import javafx.scene.Parent;
import javafx.scene.transform.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static io.github.jgcodes.rubikscube.impl.Cubie.*;

public class RubiksCube extends Parent {
	private static final int[][] CUBIE_LIST;
	private static final Duration TURN_TIME = Duration.seconds(0.5);
	private static final Duration TIME_DELAY = Duration.millis(100);

	static {
		CUBIE_LIST = new int[][]{
			{GREEN, 0, WHITE, 0, ORANGE, 0},
			{GREEN, 0, WHITE, 0, 0, 0},
			{GREEN, RED, WHITE, 0, 0, 0},
			{GREEN, 0, 0, 0, ORANGE, 0},
			{GREEN, 0, 0, 0, 0, 0},
			{GREEN, RED, 0, 0, 0, 0},
			{GREEN, 0, 0, 0, ORANGE, YELLOW},
			{GREEN, 0, 0, 0, 0, YELLOW},
			{GREEN, RED, 0, 0, 0, YELLOW},
			{0, 0, WHITE, 0, ORANGE, 0},
			{0, 0, WHITE, 0, 0, 0},
			{0, RED, WHITE, 0, 0, 0},
			{0, 0, 0, 0, ORANGE, 0},
			{0, 0, 0, 0, 0, 0},
			{0, RED, 0, 0, 0, 0},
			{0, 0, 0, 0, ORANGE, YELLOW},
			{0, 0, 0, 0, 0, YELLOW},
			{0, RED, 0, 0, 0, YELLOW},
			{0, 0, WHITE, BLUE, ORANGE, 0},
			{0, 0, WHITE, BLUE, 0, 0},
			{0, RED, WHITE, BLUE, 0, 0},
			{0, 0, 0, BLUE, ORANGE, 0},
			{0, 0, 0, BLUE, 0, 0},
			{0, RED, 0, BLUE, 0, 0},
			{0, 0, 0, BLUE, ORANGE, YELLOW},
			{0, 0, 0, BLUE, 0, YELLOW},
			{0, RED, 0, BLUE, 0, YELLOW},
		};
	}

	private final List<Cubie> cubies = FXCollections.observableArrayList();
	private boolean animating;

	public RubiksCube() {
		for (int i = 0; i <= 2; i++) {
			for (int j = 0; j <= 2; j++) {
				for (int k = 0; k <= 2; k++) {
					int[] cubieData = CUBIE_LIST[i * 9 + j * 3 + k];
					Cubie c = new Cubie(20, cubieData);
					c.setTranslate(new Translate(
						-(k - 1) * 41,
						(j - 1) * 41,
						-(i - 1) * 41
					));
					cubies.add(c);
					getChildren().add(c);
				}
			}
		}
	}

	private void add(Cubie c) {
		getChildren().add(c);
		cubies.add(c);
	}

	private void addAll(Cubie... c) {
		for (Cubie cubie: c) {
			this.add(cubie);
		}
	}

	private static TurnData parse(String turn) {
		Predicate<Point3D> filterTemp = point -> false;
		int rotationTemp = 0;
		Point3D axisTemp = Point3D.ZERO;

		switch (turn.charAt(0)) {
			case '.' -> {
				return null;
			}
			case 'F' -> {
				filterTemp = (point -> point.getZ() > 0.5);
				axisTemp = Rotate.Z_AXIS.multiply(-1);
			}
			case 'R' -> {
				filterTemp = (point -> point.getX() < -0.5);
				axisTemp = Rotate.X_AXIS;
			}
			case 'U' -> {
				filterTemp = (point -> point.getY() < -0.5);
				axisTemp = Rotate.Y_AXIS;
			}
			case 'B' -> {
				filterTemp = (point -> point.getZ() < -0.5);
				axisTemp = Rotate.Z_AXIS;
			}
			case 'L' -> {
				filterTemp = (point -> point.getX() > 0.5);
				axisTemp = Rotate.X_AXIS.multiply(-1);
			}
			case 'D' -> {
				filterTemp = (point -> point.getY() > 0.5);
				axisTemp = Rotate.Y_AXIS.multiply(-1);
			}
			case 'M' -> {
				filterTemp = (point -> -0.5 <= point.getX() && point.getX() <= 0.5);
				axisTemp = Rotate.X_AXIS.multiply(-1);
			}
			case 'E' -> {
				filterTemp = (point -> -0.5 <= point.getY() && point.getY() <= 0.5);
				axisTemp = Rotate.Y_AXIS.multiply(-1);
			}
			case 'S' -> {
				filterTemp = (point -> -0.5 <= point.getZ() && point.getZ() <= 0.5);
				axisTemp = Rotate.Z_AXIS.multiply(-1);
			}
			case 'x' -> {
				filterTemp = (point -> true);
				axisTemp = Rotate.X_AXIS;
			}
			case 'y' -> {
				filterTemp = (point -> true);
				axisTemp = Rotate.Y_AXIS;
			}
			case 'z' -> {
				filterTemp = (point -> true);
				axisTemp = Rotate.Z_AXIS.multiply(-1);
			}
		}
		switch (turn.substring(1)) {
			case "" -> rotationTemp = 90;
			case "'" -> rotationTemp = -90;
			case "2" -> rotationTemp = 180;
			case "2'" -> rotationTemp = -180;
		}
		return new TurnData(axisTemp, rotationTemp, filterTemp);
	}

	private void turnImpl(String turn, boolean clearFlag) {
		TurnData data = RubiksCube.parse(turn);
		if (data == null) return;

		cubies.stream()
			.filter(cubie -> data.filter.test(cubie.localToScene(Point3D.ZERO)))
			.forEach(cubie -> {
				Point3D localAxis;
				// get rotation axis in local context
				try {
					Transform localToScene = cubie.getLocalToSceneTransform();
					Affine affine = new Affine(localToScene.createInverse());
					affine.setTx(0);
					affine.setTy(0);
					affine.setTz(0);
					localAxis = affine.transform(data.axis).normalize();
				} catch (NonInvertibleTransformException e) {
					throw new Error(e);
				}
				// get pivot point in local context
				Point3D origin = cubie.sceneToLocal(Point3D.ZERO);
				// create animation
				Rotate r = new Rotate(0, origin.getX(), origin.getY(), origin.getZ(), localAxis);
				Transition t = new Transition() {
					{
						setCycleDuration(TURN_TIME);
						setOnFinished(event -> {
							cubie.completeAnimation();
							if (clearFlag)
								RubiksCube.this.animating = false;
						});
						setInterpolator(Interpolator.EASE_BOTH);
					}

					@Override
					protected void interpolate(double progress) {
						r.setAngle(progress * data.rotation);
					}
				};
				cubie.setAnimating(r);
				t.play();
			});
	}

	/**
	 * Applies a turn to this Rubik's cube.
	 *
	 * @param turn a turn in standard cube notation (F, R', U2, etc.)
	 */
	public void turn(String turn) {
		if (animating) return;
		animating = true;
		turnImpl(turn, true);
	}

	/**
	 * Applies an algorithm to this Rubik's cube.
	 *
	 * @param alg an algorithm in standard cube notation (ex. F R U R' U' F)
	 */
	public void apply(String alg) {
		if (animating) {
			CompletableFuture.completedFuture(null);
			return;
		}
		animating = true;
		String[] tokens = alg.replaceAll("[()]", "").split(" ");

		List<KeyFrame> keyFrames = new ArrayList<>();
		for (int i = 0; i < tokens.length; i++) {
			final String tokenValue = tokens[i];
			KeyFrame frame = new KeyFrame(TURN_TIME.add(TIME_DELAY).multiply(i),
				event -> this.turnImpl(tokenValue, false));
			keyFrames.add(frame);
		}
		keyFrames.add(new KeyFrame(TURN_TIME.add(TIME_DELAY).multiply(tokens.length), event -> {}));

		Timeline timeline = new Timeline(keyFrames.toArray(new KeyFrame[0]));
		timeline.setOnFinished(event -> this.animating = false);
		timeline.playFromStart();

	}
}
