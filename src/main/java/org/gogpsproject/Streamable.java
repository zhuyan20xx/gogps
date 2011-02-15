package org.gogpsproject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Streamable {
	public int write(DataOutputStream dos) throws IOException;
	public void read(DataInputStream dai) throws IOException;
}
