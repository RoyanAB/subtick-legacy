package cn.royan.subtick.interfaces;

import cn.royan.subtick.helpers.TickHandler;

/*
 * Represents any level-like object which contains an {@link subtick.ITickHandler}.
 */
public interface ITickHandleable {
	/*
	 * Returns the {@link subtick.ITickHandler}
	 */
	TickHandler tickHandler();
}
