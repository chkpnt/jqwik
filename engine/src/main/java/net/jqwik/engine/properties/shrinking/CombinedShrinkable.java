package net.jqwik.engine.properties.shrinking;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import net.jqwik.api.*;
import net.jqwik.engine.support.*;

public class CombinedShrinkable<T> implements Shrinkable<T> {
	private final List<Shrinkable<Object>> parts;
	private final Function<List<Object>, T> combinator;

	public CombinedShrinkable(List<Shrinkable<Object>> parts, Function<List<Object>, T> combinator) {
		this.parts = parts;
		this.combinator = combinator;
	}

	@Override
	public T value() {
		return createValue(parts);
	}

	private T createValue(List<Shrinkable<Object>> shrinkables) {
		return combinator.apply(createValues(shrinkables));
	}

	private List<Object> createValues(List<Shrinkable<Object>> shrinkables) {
		return shrinkables.stream().map(Shrinkable::value).collect(Collectors.toList());
	}

	@Override
	public Stream<Shrinkable<T>> shrink() {
		return shrinkPartsOneAfterTheOther();
	}

	protected Stream<Shrinkable<T>> shrinkPartsOneAfterTheOther() {
		List<Stream<Shrinkable<T>>> shrinkPerPartStreams = new ArrayList<>();
		for (int i = 0; i < parts.size(); i++) {
			int index = i;
			Shrinkable<Object> part = parts.get(i);
			Stream<Shrinkable<T>> shrinkElement = part.shrink().flatMap(shrunkElement -> {
				List<Shrinkable<Object>> partsCopy = new ArrayList<>(parts);
				partsCopy.set(index, shrunkElement);
				return Stream.of(new CombinedShrinkable<>(partsCopy, combinator));
			});
			shrinkPerPartStreams.add(shrinkElement);
		}
		return JqwikStreamSupport.concat(shrinkPerPartStreams);
	}

	@Override
	public ShrinkingDistance distance() {
		return ShrinkingDistance.combine(parts);
	}

}
