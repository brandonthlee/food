package app.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

import app.exception.DateConversionException;

/**
 * Utility class for handling dates and date-time conversion, validation of day
 * in a date string
 */
public class DateUtils {

	public static LocalDate convertStringToDate(String dateString) {
		try {
			List<Integer> birthSplit = Arrays.stream(dateString.split("-")).map(Integer::parseInt).toList();

			return LocalDate.of(birthSplit.get(0), birthSplit.get(1), birthSplit.get(2));

		} catch (DateTimeParseException e) {
			// If the date string is not in the correct format, handle the exception
			// For example, if an invalid date format or a non-existent date is attempted to
			// be converted, this exception will occur
			throw new DateConversionException("An error occurred during date conversion");
		}
	}

	public static String convertDateTimeToString(LocalDateTime localDateTime) {
		return localDateTime.format(DateTimeFormatter.ofPattern("MM.dd a hh:mm"));
	}

	public static boolean validateDayOfDateString(int year, int month, int day) {
		List<Integer> dayByMonth = List.of(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);

		int dayOfLeapYear = 29;

		boolean isLeap = year % 400 == 0 || (year % 4 == 0 && year % 100 != 0);

		if (month == 2 && isLeap) {
			return day <= dayOfLeapYear;
		}
		return day <= dayByMonth.get(month - 1);
	}
}
