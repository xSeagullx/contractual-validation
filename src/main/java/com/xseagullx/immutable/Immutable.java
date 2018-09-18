package com.xseagullx.immutable;

import java.util.function.Consumer;

public interface Immutable<T> {
	default T withWritable(Consumer<T> mutator) { throw new ClassIsNowWrappedException(); }
}
