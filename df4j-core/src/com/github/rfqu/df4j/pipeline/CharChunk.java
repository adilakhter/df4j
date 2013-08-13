package com.github.rfqu.df4j.pipeline;

import com.github.rfqu.df4j.core.Port;

public abstract class CharChunk<C extends CharChunk<C>> extends ChunkBase<C> {
	
	public CharChunk(Port<C> returnPort) {
		super(returnPort);
	}

    public abstract CharIterator charIterator();
}