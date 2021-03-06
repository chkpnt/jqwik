package net.jqwik.api.edgeCases;

import java.math.*;
import java.util.*;
import java.util.function.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.engine.properties.*;
import net.jqwik.engine.properties.shrinking.*;

import static org.assertj.core.api.Assertions.*;

import static net.jqwik.testing.ShrinkingSupport.*;

class EdgeCasesTests {

	@Example
	void fromSuppliers() {
		List<Supplier<Shrinkable<BigInteger>>> suppliers = new ArrayList<>();
		Range<BigInteger> range = Range.of(BigInteger.valueOf(-5), BigInteger.valueOf(50));
		suppliers.add(() -> new ShrinkableBigInteger(BigInteger.valueOf(42), range, BigInteger.ZERO));
		suppliers.add(() -> new ShrinkableBigInteger(BigInteger.valueOf(49), range, BigInteger.ZERO));
		EdgeCases<BigInteger> edgeCases = EdgeCases.fromSuppliers(suppliers);

		Iterator<Shrinkable<BigInteger>> iterator = edgeCases.iterator();
		assertThat(iterator.next().value()).isEqualTo(BigInteger.valueOf(42));
		assertThat(iterator.next().value()).isEqualTo(BigInteger.valueOf(49));
		assertThat(iterator.hasNext()).isFalse();
	}

	@Example
	void mappedEdgeCasesCanBeShrunk() {
		Arbitrary<String> arbitrary =
				Arbitraries.integers()
						   .between(10, 100).shrinkTowards(25)
						   .map(i -> Integer.toString(i));

		EdgeCases<String> edgeCases = arbitrary.edgeCases();
		assertThat(edgeCases).hasSize(5);

		Falsifier<String> falsifier = ignore -> TryExecutionResult.falsified(null);
		for (Shrinkable<String> edgeCase : edgeCases) {
			String shrunkValue = shrink(edgeCase, falsifier, null);
			assertThat(shrunkValue).isEqualTo("25");
		}
	}

	@Example
	void filteredEdgeCasesCanBeShrunk() {
		Arbitrary<Integer> arbitrary =
				Arbitraries.integers()
						   .between(0, 11)
						   .filter(i -> i % 2 == 1);

		EdgeCases<Integer> edgeCases = arbitrary.edgeCases();
		assertThat(edgeCases).hasSize(2);

		Falsifier<Integer> falsifier = ignore -> TryExecutionResult.falsified(null);
		for (Shrinkable<Integer> edgeCase : edgeCases) {
			int shrunkValue = shrink(edgeCase, falsifier, null);
			assertThat(shrunkValue).isEqualTo(1);
		}
	}

	@Example
	void flatMappedEdgeCasesCanBeShrunk() {
		Arbitrary<String> arbitrary =
				Arbitraries.of(1, 10)
						   .flatMap(i -> Arbitraries.strings().withCharRange('a', 'z').ofLength(i));

		EdgeCases<String> edgeCases = arbitrary.edgeCases();

		assertThat(edgeCases).hasSize(4);

		Falsifier<String> falsifier = ignore -> TryExecutionResult.falsified(null);
		for (Shrinkable<String> edgeCase : edgeCases) {
			String shrunkValue = shrink(edgeCase, falsifier, null);
			assertThat(shrunkValue).isEqualTo("a");
		}
	}
}
