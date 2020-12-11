package net.jqwik.api.time;

import java.time.*;
import java.util.*;

import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.*;

import static net.jqwik.api.time.copy.ArbitraryTestHelper.*;

@Group
public class MonthTests {

	@Property
	void validMonthIsGenerated(@ForAll("months") Month month) {
		assertThat(month).isNotNull();
	}

	@Provide
	Arbitrary<Month> months() {
		return Dates.months();
	}

	@Group
	class CheckMonthMethods {

		@Property
		void atTheEarliest(@ForAll("months") Month month) {

			Arbitrary<Month> months = Dates.months().atTheEarliest(month);

			assertAllGenerated(months.generator(1000), m -> {
				assertThat(m).isGreaterThanOrEqualTo(month);
			});

		}

		@Property
		void atTheLatest(@ForAll("months") Month month) {

			Arbitrary<Month> months = Dates.months().atTheLatest(month);

			assertAllGenerated(months.generator(1000), m -> {
				assertThat(m).isLessThanOrEqualTo(month);
			});
		}

		@Property
		void between(@ForAll("months") Month startMonth, @ForAll("months") Month endMonth) {

			Assume.that(startMonth.compareTo(endMonth) <= 0);

			Arbitrary<Month> months = Dates.months().between(startMonth, endMonth);

			assertAllGenerated(months.generator(1000), month -> {
				assertThat(month).isGreaterThanOrEqualTo(startMonth);
				assertThat(month).isLessThanOrEqualTo(endMonth);
			});

		}

		@Property
		void betweenSame(@ForAll("months") Month month) {

			Arbitrary<Month> months = Dates.months().between(month, month);

			assertAllGenerated(months.generator(1000), m -> {
				assertThat(m).isEqualTo(month);
			});
		}

		@Property
		void only(@ForAll("onlyMonths") Month[] months){

			Arbitrary<Month> monthArbitrary = Dates.months().only(months);

			assertAllGenerated(monthArbitrary.generator(1000), month -> {
				assertThat(month).isIn(months);
			});
		}

		@Provide
		Arbitrary<Month[]> onlyMonths(){
			return generateMonths();
		}

	}

	public static Arbitrary<Month[]> generateMonths(){
		Arbitrary<Month> monthArbitrary = Arbitraries.of(Month.JANUARY, Month.FEBRUARY, Month.MARCH, Month.APRIL, Month.MAY, Month.JUNE, Month.JULY, Month.AUGUST, Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER);
		Arbitrary<Integer> length = Arbitraries.integers().between(1, 12);
		Arbitrary<List<Month>> arbitrary = length.flatMap(depth -> Arbitraries.recursive(
				() -> monthArbitrary.map(v -> new ArrayList<>()),
				(v) -> addMonth(v, monthArbitrary),
				depth
		));
		return arbitrary.map(v -> v.toArray(new Month[]{}));
	}

	private static Arbitrary<List<Month>> addMonth(Arbitrary<List<Month>> listArbitrary, Arbitrary<Month> monthArbitrary){
		return Combinators.combine(listArbitrary, monthArbitrary).as(MonthTests::addToList);
	}

	private static List<Month> addToList(List<Month> list, Month month){
		if(!list.contains(month)){
			list.add(month);
		}
		return list;
	}

}