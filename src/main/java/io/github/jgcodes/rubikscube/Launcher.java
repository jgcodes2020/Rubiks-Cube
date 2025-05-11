package io.github.jgcodes.rubikscube;

import io.github.jgcodes.rubikscube.impl.RubiksCube;
import io.github.jgcodes.rubikscube.util.KeyTracker;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Launcher extends Application {
	private final DoubleProperty
		hAngle = new SimpleDoubleProperty(-30),
		vAngle = new SimpleDoubleProperty(-30),
		distance = new SimpleDoubleProperty(200);

	public static void main(String[] args){
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		PointLight light = new PointLight();
		light.getTransforms().add(new Translate(-100, -100, -100));

		RubiksCube cube = new RubiksCube();
		Scene scene = new Scene(new Group(cube, new AmbientLight(Color.web("#888"))), 600, 400, true);

		Timeline timeline = new Timeline();
		timeline.getKeyFrames().addAll(
			new KeyFrame(Duration.seconds(2), event -> cube.apply(
				"R U R' U' R' F R2 U' R' U' R U R' F' . R U R' U' R' F R2 U' R' U' R U R' F'"))
		);
		timeline.playFromStart();

		KeyTracker tracker = new KeyTracker();
		scene.addEventFilter(KeyEvent.KEY_PRESSED, tracker);
		scene.addEventFilter(KeyEvent.KEY_RELEASED, tracker);
		scene.setFill(Color.BLACK);

		PerspectiveCamera cam = new PerspectiveCamera(true);
		cam.setFarClip(1000);
		cam.setFieldOfView(70);
		scene.setCamera(cam);


		AnimationTimer timer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				if (tracker.isPressed(KeyCode.UP)) {
					vAngle.set(Double.max(vAngle.get() - 3, -90));
				}
				if (tracker.isPressed(KeyCode.DOWN)) {
					vAngle.set(Double.min(vAngle.get() + 3, 90));
				}
				if (tracker.isPressed(KeyCode.LEFT)) {
					hAngle.set((hAngle.get() + 3) % 360);
				}
				if (tracker.isPressed(KeyCode.RIGHT)) {
					hAngle.set((hAngle.get() - 3) % 360);
				}
				if (tracker.isPressed(KeyCode.Q)) {
					distance.set(Double.max(distance.get() - 0.5, 0));
				}
				if (tracker.isPressed(KeyCode.E)) {
					distance.set(Double.min(distance.get() + 0.5, 1000));
				}

				Point3D camPos = new Point3D(
					Math.sin(Math.toRadians(hAngle.get())) * Math.cos(Math.toRadians(vAngle.get())) * distance.get(),
					Math.sin(Math.toRadians(vAngle.get())) * distance.get(),
					Math.cos(Math.toRadians(hAngle.get())) * Math.cos(Math.toRadians(vAngle.get())) * distance.get()
				);

				lookAt(cam, camPos, Point3D.ZERO);
			}
		};
		timer.start();

		stage.setScene(scene);
		stage.setTitle("Rubik's Cube");
		stage.show();
	}
	public static void lookAt(Camera cam, Point3D cameraPosition, Point3D lookAtPos) {
		//Create direction vector
		Point3D camDirection = lookAtPos.subtract(cameraPosition).normalize();

		double xRotation = Math.toDegrees(Math.asin(-camDirection.getY()));
		double yRotation =  Math.toDegrees(Math.atan2( camDirection.getX(), camDirection.getZ()));

		Rotate rx = new Rotate(xRotation, cameraPosition.getX(), cameraPosition.getY(), cameraPosition.getZ(), Rotate.X_AXIS);
		Rotate ry = new Rotate(yRotation, cameraPosition.getX(), cameraPosition.getY(), cameraPosition.getZ(),  Rotate.Y_AXIS);

		cam.getTransforms().clear();
		cam.getTransforms().addAll( ry, rx,
			new Translate(
				cameraPosition.getX(),
				cameraPosition.getY(),
				cameraPosition.getZ()));
	}
}

