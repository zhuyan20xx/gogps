package org.gogpsproject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Streamable {

	public final static String MESSAGE_OBSERVATIONS = "obs";
	public final static String MESSAGE_IONO = "ion";
	public final static String MESSAGE_EPHEMERIS = "eph";
	public final static String MESSAGE_EPHEMERIS_SET = "eps";
	public final static String MESSAGE_COORDINATES = "coo";

	public int write(DataOutputStream dos) throws IOException;
	public void read(DataInputStream dai, boolean oldVersion) throws IOException;
}
