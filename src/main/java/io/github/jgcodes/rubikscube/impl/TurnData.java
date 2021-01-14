package io.github.jgcodes.rubikscube.impl;

import javafx.geometry.Point3D;

import java.util.Objects;
import java.util.function.Predicate;

class TurnData {
	final Point3D axis;
	final int rotation;
	final Predicate<Point3D> filter;

	TurnData(Point3D axis, int rotation, Predicate<Point3D> filter) {
		this.axis = axis;
		this.rotation = rotation;
		this.filter = filter;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TurnData turnData = (TurnData) o;
		return rotation == turnData.rotation && Objects.equals(axis, turnData.axis) && Objects.equals(filter, turnData.filter);
	}

	@Override
	public int hashCode() {
		return Objects.hash(axis, rotation, filter);
	}
}
