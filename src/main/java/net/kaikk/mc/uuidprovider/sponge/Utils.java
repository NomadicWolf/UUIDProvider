package net.kaikk.mc.uuidprovider.sponge;

import javax.annotation.Nullable;

import org.spongepowered.api.util.Tristate;
public class Utils {
	/**
	 * Returns a Tristate value from a Boolean object
	 * @param b The Boolean object.
	 * @return {@link Tristate#UNDEFINED} if b is null<br>
	 *  {@link Tristate#TRUE} if b is true<br>
	 *  {@link Tristate#FALSE} if b is false
	 */
	public static Tristate tristate(@Nullable Boolean b) {
		return b==null ? Tristate.UNDEFINED : b ? Tristate.TRUE : Tristate.FALSE;
	}
}
