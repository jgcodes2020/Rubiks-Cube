package io.github.jgcodes.rubikscube.impl;

import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

public class Cubie extends MeshView {
	public static final int
		BASE = 0,
		GREEN = 1,
		RED = 2,
		WHITE = 3,
		BLUE = 4,
		ORANGE = 5,
		YELLOW = 6;
	public static final int
		FRONT = 0,
		RIGHT = 1,
		UP = 2,
		BACK = 3,
		LEFT = 4,
		DOWN = 5;

	public static Image texture = new Image(
		Cubie.class.getResource("rubikscube.png").toExternalForm()
	);

	/**
	 * A specialized {@link TriangleMesh} designed for texturing a Rubik's Cube.
	 */
	public static class Mesh extends TriangleMesh {


		Mesh(final float width) {
			//Vertices
			this.getPoints().setAll(
				width, width, width,
				width, width, -width,
				width, -width, width,
				width, -width, -width,

				-width, width, width,
				-width, width, -width,
				-width, -width, width,
				-width, -width, -width
			);

			//Texture Coordinates
			this.getTexCoords().setAll(
				//Top points
				0, 0,
				1 / 8.0f, 0,
				2 / 8.0f, 0,
				3 / 8.0f, 0,
				4 / 8.0f, 0,
				5 / 8.0f, 0,
				6 / 8.0f, 0,
				7 / 8.0f, 0,
				//Bottom Points
				0, 1,
				1 / 8.0f, 1,
				2 / 8.0f, 1,
				3 / 8.0f, 1,
				4 / 8.0f, 1,
				5 / 8.0f, 1,
				6 / 8.0f, 1,
				7 / 8.0f, 1
			);

			//Faces (using BG texture)
			this.getFaces().setAll(
				//Front
				4, 8, 6, 0, 2, 1,
				0, 9, 4, 8, 2, 1,
				//Right
				5, 8, 7, 0, 6, 1,
				4, 9, 5, 8, 6, 1,
				//Up
				6, 8, 7, 0, 3, 1,
				2, 9, 6, 8, 3, 1,
				//Back
				1, 8, 3, 0, 7, 1,
				5, 9, 1, 8, 7, 1,
				//Left
				0, 8, 2, 0, 3, 1,
				1, 9, 0, 8, 3, 1,
				//Down
				0, 8, 1, 0, 5, 1,
				4, 1, 0, 9, 5, 8
			);
			this.getFaceSmoothingGroups().setAll(
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
			);

		}

		public Mesh(final float width, int... textures) {
			this(width);
			if (textures.length != 6) {
				throw new IllegalArgumentException("Mesh should have 6 textures");
			}
			for (int i = 0; i < textures.length; i++) {
				setTexture(i, textures[i]);
			}
		}


		private void setTexture(int face, int topLeft) {
			if (topLeft < 0 || topLeft > 6) {
				throw new IllegalArgumentException("Not a valid texture");
			}
			//texture indices
			final int
				topRight = topLeft + 1,
				bottomLeft = topLeft + 8,
				bottomRight = bottomLeft + 1;
			final int faceGroup = face * 12;

			final int[] quad = getFaces().toArray(faceGroup, new int[12], 12);
			quad[1] = bottomLeft;
			quad[3] = topLeft;
			quad[5] = topRight;
			quad[7] = bottomRight;
			quad[9] = bottomLeft;
			quad[11] = topRight;
			getFaces().set(faceGroup, quad, 0, 12);
		}
	}

	private Translate translate;
	private final Affine affine;
	private Transform animating;

	public Translate getTranslate() {
		return translate;
	}

	public void setTranslate(Translate t) {
		if (translate == null) {
			translate = t;
			getTransforms().add(0, translate);
		}
		else throw new IllegalStateException("Translation is already set.");
	}

	/**
	 * Get current affine transformation.
	 *
	 * @return current affine transformation.
	 */
	public Affine getAffine() {
		return affine;
	}

	public void setAnimating(Transform animating) {
		this.animating = animating;
		getTransforms().add(animating);
	}

	public void completeAnimation() {
		affine.append(animating);
		this.animating = null;
		getTransforms().remove(getTransforms().size() - 1);

	}

	public Cubie(float width, int[] cubieData) {
		if (cubieData.length != 6)
			throw new IllegalArgumentException("Cubie data must contain exactly 6 elements");

		this.setMesh(new Cubie.Mesh(width, cubieData));
		this.setCullFace(CullFace.NONE);

		PhongMaterial mat = new PhongMaterial();
		mat.setDiffuseMap(Cubie.texture);
		this.setMaterial(mat);

		affine = new Affine();
		getTransforms().addAll(affine);
	}

	public Cubie(float width, int front, int right, int up, int back, int left, int down) {
		this(width, new int[]{front, right, up, back, left, down});
	}

	public Cubie(float width) {
		this(width, 0, 0, 0, 0, 0, 0);
	}

	public Cubie.Mesh getTexturedMesh() {
		return (Cubie.Mesh) this.getMesh();
	}
}