package org.opengis.cite.iso19136.util;

public enum GmlVersion {

	/**
	 * Version 3.2.1
	 */
	V321("3.2.1"),
	/**
	 * Version 3.2.2
	 */
	V322("3.2.2");

	private final String stringRepresentation;

	public final String getStringRepresentation() {
		return stringRepresentation;
	}

	GmlVersion(String stringRepresentation) {
		this.stringRepresentation = stringRepresentation;
	}

	public static GmlVersion fromString(String version) {
		for (GmlVersion c : GmlVersion.values()) {
			if (c.getStringRepresentation().equalsIgnoreCase(version)) {
				return c;
			}
		}
		throw new IllegalArgumentException(version);
	}

	public static String toString(GmlVersion version) {
		return version.stringRepresentation;
	}

	@Override
	public String toString() {
		return toString(this);
	}

}
