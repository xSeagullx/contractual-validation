package com.xseagullx.immutable;

import java.util.function.Consumer;

public interface HasWithWritable<T> {
	default T withWritable(Consumer<T> mutator) { throw new ClassIsNowWrappedException(); }
}
