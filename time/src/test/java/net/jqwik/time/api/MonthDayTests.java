package net.jqwik.time.api;

import java.time.*;
import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.testing.*;

import static org.assertj.core.api.Assertions.*;

import static net.jqwik.testing.ShrinkingSupport.*;
import static net.jqwik.testing.TestingSupport.*;

@Group
class MonthDayTests {

	@Property
	void validMonthDayIsGenerated(@ForAll("monthDays") MonthDay monthDay) {
		assertThat(monthDay).isNotNull();
	}

	@Provide
	Arbitrary<MonthDay> monthDays() {
		return Dates.monthDays();
	}

	@Group
	class CheckMonthDayMethods {

		@Group
		class MonthDayMethods {

			@Property
			void atTheEarliest(@ForAll("monthDays") MonthDay monthDay, @ForAll Random random) {

				Arbitrary<MonthDay> dates = Dates.monthDays().atTheEarliest(monthDay);

				assertAllGenerated(dates.generator(1000), random, md -> {
					assertThat(md).isGreaterThanOrEqualTo(monthDay);
					return true;
				});

			}

			@Property
			void atTheLatest(@ForAll("monthDays") MonthDay monthDay, @ForAll Random random) {

				Arbitrary<MonthDay> dates = Dates.monthDays().atTheLatest(monthDay);

				assertAllGenerated(dates.generator(1000), random, md -> {
					assertThat(md).isLessThanOrEqualTo(monthDay);
					return true;
				});

			}

			@Property
			void between(@ForAll("monthDays") MonthDay startMonthDay, @ForAll("monthDays") MonthDay endMonthDay, @ForAll Random random) {

				Assume.that(!startMonthDay.isAfter(endMonthDay));

				Arbitrary<MonthDay> dates = Dates.monthDays().between(startMonthDay, endMonthDay);

				assertAllGenerated(dates.generator(1000), random, md -> {
					assertThat(md).isGreaterThanOrEqualTo(startMonthDay);
					assertThat(md).isLessThanOrEqualTo(endMonthDay);
					return true;
				});
			}

			@Property
			void betweenSame(@ForAll("monthDays") MonthDay monthDay, @ForAll Random random) {

				Arbitrary<MonthDay> dates = Dates.monthDays().between(monthDay, monthDay);

				assertAllGenerated(dates.generator(1000), random, md -> {
					assertThat(md).isEqualTo(monthDay);
					return true;
				});

			}

		}

		@Group
		class MonthMethods {

			@Property
			void monthBetween(@ForAll("months") int startMonth, @ForAll("months") int endMonth, @ForAll Random random) {

				Assume.that(startMonth <= endMonth);

				Arbitrary<MonthDay> dates = Dates.monthDays().monthBetween(startMonth, endMonth);

				assertAllGenerated(dates.generator(1000), random, md -> {
					assertThat(md.getMonth()).isGreaterThanOrEqualTo(Month.of(startMonth));
					assertThat(md.getMonth()).isLessThanOrEqualTo(Month.of(endMonth));
					return true;
				});

			}

			@Property
			void monthBetweenSame(@ForAll("months") int month, @ForAll Random random) {

				Arbitrary<MonthDay> dates = Dates.monthDays().monthBetween(month, month);

				assertAllGenerated(dates.generator(1000), random, md -> {
					assertThat(md.getMonth()).isEqualTo(Month.of(month));
					return true;
				});

			}

			@Property
			void monthOnlyMonths(@ForAll @Size(min = 1) Set<Month> months, @ForAll Random random) {

				Arbitrary<MonthDay> dates = Dates.monthDays().onlyMonths(months.toArray(new Month[]{}));

				assertAllGenerated(dates.generator(1000), random, md -> {
					assertThat(md.getMonth()).isIn(months);
					return true;
				});

			}

			@Provide
			Arbitrary<Integer> months() {
				return Arbitraries.integers().between(1, 12);
			}

		}

		@Group
		class DayOfMonthMethods {

			@Property
			void dayOfMonthBetween(
					@ForAll("dayOfMonths") int startDayOfMonth,
					@ForAll("dayOfMonths") int endDayOfMonth,
					@ForAll Random random
			) {

				Assume.that(startDayOfMonth <= endDayOfMonth);

				Arbitrary<MonthDay> dates = Dates.monthDays().dayOfMonthBetween(startDayOfMonth, endDayOfMonth);

				assertAllGenerated(dates.generator(1000), random, md -> {
					assertThat(md.getDayOfMonth()).isGreaterThanOrEqualTo(startDayOfMonth);
					assertThat(md.getDayOfMonth()).isLessThanOrEqualTo(endDayOfMonth);
					return true;
				});

			}

			@Property
			void dayOfMonthBetweenSame(@ForAll("dayOfMonths") int dayOfMonth, @ForAll Random random) {

				Arbitrary<MonthDay> dates = Dates.monthDays().dayOfMonthBetween(dayOfMonth, dayOfMonth);

				assertAllGenerated(dates.generator(1000), random, md -> {
					assertThat(md.getDayOfMonth()).isEqualTo(dayOfMonth);
					return true;
				});

			}

			@Provide
			Arbitrary<Integer> dayOfMonths() {
				return Arbitraries.integers().between(1, 31);
			}

		}

	}

	@Group
	class Shrinking {

		@Property
		void defaultShrinking(@ForAll Random random) {
			MonthDayArbitrary monthDays = Dates.monthDays();
			MonthDay value = falsifyThenShrink(monthDays, random);
			assertThat(value).isEqualTo(MonthDay.of(Month.JANUARY, 1));
		}

		@Property
		void shrinksToSmallestFailingValue(@ForAll Random random) {
			MonthDayArbitrary monthDays = Dates.monthDays();
			TestingFalsifier<MonthDay> falsifier = md -> md.isBefore(MonthDay.of(Month.MAY, 25));
			MonthDay value = falsifyThenShrink(monthDays, random, falsifier);
			assertThat(value).isEqualTo(MonthDay.of(Month.MAY, 25));
		}

	}

	@Group
	class ExhaustiveGeneration {

		@Example
		void containsAllValues() {
			Optional<ExhaustiveGenerator<MonthDay>> optionalGenerator = Dates.monthDays().exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<MonthDay> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(366); // Cannot know the number of filtered elements in advance
			assertThat(generator).containsExactlyElementsOf(generateAllMonthDays());
		}

		@Example
		void between() {
			Optional<ExhaustiveGenerator<MonthDay>> optionalGenerator =
					Dates.monthDays()
						 .between(MonthDay.of(Month.FEBRUARY, 27), MonthDay.of(Month.MARCH, 2))
						 .exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<MonthDay> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(5); // Cannot know the number of filtered elements in advance
			assertThat(generator).containsExactly(
					MonthDay.of(Month.FEBRUARY, 27),
					MonthDay.of(Month.FEBRUARY, 28),
					MonthDay.of(Month.FEBRUARY, 29),
					MonthDay.of(Month.MARCH, 1),
					MonthDay.of(Month.MARCH, 2)
			);
		}

		@Example
		void onlyMonthsWithSameDayOfMonths() {
			Optional<ExhaustiveGenerator<MonthDay>> optionalGenerator =
					Dates.monthDays()
						 .dayOfMonthBetween(17, 17)
						 .onlyMonths(Month.APRIL, Month.AUGUST, Month.OCTOBER)
						 .exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<MonthDay> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(214); // Cannot know the number of filtered elements in advance
			assertThat(generator).containsExactly(
					MonthDay.of(Month.APRIL, 17),
					MonthDay.of(Month.AUGUST, 17),
					MonthDay.of(Month.OCTOBER, 17)
			);
		}

		List<MonthDay> generateAllMonthDays() {
			List<MonthDay> monthDayList = new ArrayList<>();
			for (int m = 1; m <= 12; m++) {
				for (int d = 1; d <= 31; d++) {
					try {
						monthDayList.add(MonthDay.of(m, d));
					} catch (DateTimeException e) {
						//do nothing
					}
				}
			}
			return monthDayList;

		}

	}

	@Group
	class EdgeCasesTests {

		@Example
		void all() {
			MonthDayArbitrary monthDays = Dates.monthDays();
			Set<MonthDay> edgeCases = collectEdgeCases(monthDays.edgeCases());
			assertThat(edgeCases).hasSize(3);
			assertThat(edgeCases).containsExactly(
					MonthDay.of(Month.JANUARY, 1),
					MonthDay.of(Month.FEBRUARY, 29),
					MonthDay.of(Month.DECEMBER, 31)
			);
		}

		@Example
		void between() {
			MonthDayArbitrary monthDays =
					Dates.monthDays().between(MonthDay.of(Month.FEBRUARY, 25), MonthDay.of(Month.APRIL, 10));
			Set<MonthDay> edgeCases = collectEdgeCases(monthDays.edgeCases());
			assertThat(edgeCases).hasSize(3);
			assertThat(edgeCases).containsExactlyInAnyOrder(
					MonthDay.of(Month.FEBRUARY, 25),
					MonthDay.of(Month.FEBRUARY, 29),
					MonthDay.of(Month.APRIL, 10)
			);
		}

	}

}
