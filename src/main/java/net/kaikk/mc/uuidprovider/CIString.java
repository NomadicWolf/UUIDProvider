package net.kaikk.mc.uuidprovider;

public class CIString {
	private String string, lcString;
	
	public CIString(String string) {
		this.string = string;
		this.lcString = string.toLowerCase();
	}

	@Override
	public String toString() {
		return string;
	}

	@Override
	public int hashCode() {
		return this.lcString.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CIString) {
			return this.lcString.equals(((CIString) obj).lcString);
		} else if (obj instanceof String) {
			return this.lcString.equalsIgnoreCase((String)obj);
		} else if (obj == null) {
			return false;
		} else {
			return this.lcString.equalsIgnoreCase(obj.toString());
		}
	}
}
