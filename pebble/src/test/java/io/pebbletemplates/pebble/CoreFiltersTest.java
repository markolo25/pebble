/*
 * This file is part of Pebble.
 * <p>
 * Copyright (c) 2014 by Mitchell Bösecke
 * <p>
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.pebbletemplates.pebble;

import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.TestingExtension;
import io.pebbletemplates.pebble.extension.core.*;
import io.pebbletemplates.pebble.loader.StringLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CoreFiltersTest {

  @Test
  void testAbs() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ -5 | abs }}");

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("5", writer.toString());
  }

  @Test
  void testAbsDouble() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ -5.2 | abs }}");

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("5.2", writer.toString());
  }

  @Test
  void testChainedFiltersWithNullInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ null | upper | lower }}");
    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("", writer.toString());
  }

  @Test
  void testLower() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ 'TEMPLATE' | lower }}");

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("template", writer.toString());
  }

  @Test
  void testLowerWithNullInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ null | lower }}");

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("", writer.toString());
  }

  @Test
  void testUpper() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ 'template' | upper }}");
    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("TEMPLATE", writer.toString());
  }

  @Test
  void testUpperWithNullInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ null | upper }}");
    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("", writer.toString());
  }

  @Test
  void testDate() throws ParseException, PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false)
        .defaultLocale(Locale.ENGLISH).build();

    String source = "{{ realDate | date('MM/dd/yyyy') }}{{ realDate | date(format) }}{{ stringDate | date('yyyy/MMMM/d','yyyy-MMMM-d') }}";

    PebbleTemplate template = pebble.getTemplate(source);
    Map<String, Object> context = new HashMap<>();
    DateFormat format = new SimpleDateFormat("yyyy-MMMM-d", Locale.ENGLISH);
    Date realDate = format.parse("2012-July-01");
    context.put("realDate", realDate);
    context.put("stringDate", format.format(realDate));
    context.put("format", "yyyy-MMMM-d");

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("07/01/20122012-July-12012/July/1", writer.toString());
  }

  @Test
  void testDefaultDateFormat() throws PebbleException, IOException{
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
            .strictVariables(false)
            .defaultLocale(Locale.ENGLISH).build();

    String source = "{{ stringDate | date('MM/dd/yyyy') }}";

    PebbleTemplate template = pebble.getTemplate(source);
    Map<String, Object> context = new HashMap<>();
    context.put("stringDate", "2004-02-12T15:19:21+04:00");

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("02/12/2004", writer.toString());
  }

  @Test
  void testDateJava8() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine
        .Builder()
        .loader(new StringLoader())
        .strictVariables(false)
        .defaultLocale(Locale.ENGLISH)
        .build();

    final LocalDateTime localDateTime = LocalDateTime.of(2017, 6, 30, 13, 30, 35, 0);
    final ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("GMT+0100"));
    final LocalDate localDate = localDateTime.toLocalDate();
    final LocalTime localTime = localDateTime.toLocalTime();

    StringBuilder source = new StringBuilder();
    source
        .append("{{ localDateTime | date }}")
        .append("{{ localDateTime | date('yyyy-MM-dd HH:mm:ss') }}")
        .append("{{ zonedDateTime | date('yyyy-MM-dd HH:mm:ssXXX') }}")
        .append("{{ localDate | date('yyyy-MM-dd') }}")
        .append("{{ localTime | date('HH:mm:ss') }}");

    PebbleTemplate template = pebble.getTemplate(source.toString());
    Map<String, Object> context = new HashMap<>();
    context.put("localDateTime", localDateTime);
    context.put("zonedDateTime", zonedDateTime);
    context.put("localDate", localDate);
    context.put("localTime", localTime);

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals(
        "2017-06-30T13:30:352017-06-30 13:30:352017-06-30 13:30:35+01:002017-06-3013:30:35",
        writer.toString());
  }


  @Test
  void testDateWithNamedArguments() throws ParseException, PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false)
        .defaultLocale(Locale.ENGLISH).build();

    String source = "{{ stringDate | date(existingFormat='yyyy-MMMM-d', format='yyyy/MMMM/d') }}";

    PebbleTemplate template = pebble.getTemplate(source);
    Map<String, Object> context = new HashMap<>();
    DateFormat format = new SimpleDateFormat("yyyy-MMMM-d", Locale.ENGLISH);
    Date realDate = format.parse("2012-July-01");
    context.put("stringDate", format.format(realDate));

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("2012/July/1", writer.toString());
  }

  @Test
  void testDateWithNullInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    String source = "{{ null | date(\"MM/dd/yyyy\") }}";

    PebbleTemplate template = pebble.getTemplate(source);

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("", writer.toString());
  }

  @Test
  void testDateWithNumberInput() throws IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    String source = "{{ dateAsNumber | date(\"MM/dd/yyyy\") }}";

    PebbleTemplate template = pebble.getTemplate(source);
    Map<String, Object> context = new HashMap<>();
    context.put("dateAsNumber", 1518004210000L);

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("02/07/2018", writer.toString());
  }

  @Test
  void testDateWithDateAndExplicitTimeZone() throws IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().build();

    PebbleTemplate template = pebble.getLiteralTemplate("{{ date | date(timeZone=\"Asia/Almaty\") }}");
    Map<String, Object> context = new HashMap<>();
    context.put("date", Date.from(Instant.ofEpochSecond(1595853935)));

    Writer writer = new StringWriter();
    template.evaluate(writer, context);

    assertEquals("2020-07-27T18:45:35+0600", writer.toString());
  }

  @Test
  void testDateWithDateAndFormatAndExplicitTimeZone() throws IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().build();

    PebbleTemplate template = pebble.getLiteralTemplate("{{ date | date(\"yyyy-MM-dd'T'HH:mm:ssX\", timeZone=\"Asia/Almaty\") }}");
    Map<String, Object> context = new HashMap<>();
    context.put("date", Date.from(Instant.ofEpochSecond(1595853935)));

    Writer writer = new StringWriter();
    template.evaluate(writer, context);

    assertEquals("2020-07-27T18:45:35+06", writer.toString());
  }

  @Test
  void testDateWithTimestampAndExplicitTimeZone() throws IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().build();

    PebbleTemplate template = pebble.getLiteralTemplate("{{ timestamp | date(timeZone=\"Asia/Almaty\") }}");
    Map<String, Object> context = new HashMap<>();
    context.put("timestamp", 1595853935000L);

    Writer writer = new StringWriter();
    template.evaluate(writer, context);

    assertEquals("2020-07-27T18:45:35+0600", writer.toString());
  }

  @Test
  void testDateWithOffsetDateTimeAndExplicitTimeZoneUsesTimeZoneOfInput() throws IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().build();

    PebbleTemplate template = pebble.getLiteralTemplate("{{ offsetDateTime | date(timeZone=\"Asia/Almaty\") }}");
    Map<String, Object> context = new HashMap<>();
    context.put("offsetDateTime", OffsetDateTime.of(2020, 7, 27, 16, 12, 13, 0, ZoneOffset.ofHours(3)));

    Writer writer = new StringWriter();
    template.evaluate(writer, context);

    assertEquals("2020-07-27T16:12:13+03:00", writer.toString());
  }

  @Test
  void testDateWithOffsetDateTimeAndFormatAndExplicitTimeZoneUsesTimeZoneOfInput() throws IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().build();

    PebbleTemplate template = pebble.getLiteralTemplate("{{ offsetDateTime | date(\"yyyy-MM-dd'T'HH:mm:ssX\", timeZone=\"Asia/Almaty\") }}");
    Map<String, Object> context = new HashMap<>();
    context.put("offsetDateTime", OffsetDateTime.of(2020, 7, 27, 16, 12, 13, 0, ZoneOffset.ofHours(3)));

    Writer writer = new StringWriter();
    template.evaluate(writer, context);

    assertEquals("2020-07-27T16:12:13+03", writer.toString());
  }

  @Test
  void testDateWithOffsetDateTimeAndFormatAndNoExplicitTimeZoneUsesTimeZoneOfInput() throws IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().build();

    PebbleTemplate template = pebble.getLiteralTemplate("{{ offsetDateTime | date(\"yyyy-MM-dd'T'HH:mm:ssX\") }}");
    Map<String, Object> context = new HashMap<>();
    context.put("offsetDateTime", OffsetDateTime.of(2020, 7, 27, 16, 12, 13, 0, ZoneOffset.ofHours(5)));

    Writer writer = new StringWriter();
    template.evaluate(writer, context);

    assertEquals("2020-07-27T16:12:13+05", writer.toString());
  }

  @Test
  void testDateWithInstantAndExplicitTimeZone() throws IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().build();

    PebbleTemplate template = pebble.getLiteralTemplate("{{ instant | date(timeZone=\"Asia/Almaty\") }}");
    Map<String, Object> context = new HashMap<>();
    context.put("instant", Instant.ofEpochSecond(1595853935));

    Writer writer = new StringWriter();
    template.evaluate(writer, context);

    assertEquals("2020-07-27T18:45:35+06:00[Asia/Almaty]", writer.toString());
  }

  @Test
  void testDateWithInstantAndNoExplicitTimeZoneUsesSystemTimeZone() throws IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().build();

    TimeZone defaultTimeZone = TimeZone.getDefault();
    try {
      TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Funafuti"));

      PebbleTemplate template = pebble.getLiteralTemplate("{{ instant | date() }}");
      Map<String, Object> context = new HashMap<>();
      context.put("instant", Instant.ofEpochSecond(1595853935));

      Writer writer = new StringWriter();
      template.evaluate(writer, context);

      assertEquals("2020-07-28T00:45:35+12:00[Pacific/Funafuti]", writer.toString());
    }
    finally {
      TimeZone.setDefault(defaultTimeZone);
    }
  }

  @Test
  void testDateWithUnsupportedInput() throws IOException {
    assertThrows(IllegalArgumentException.class, () -> {
      PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
          .strictVariables(false).build();

      String source = "{{ unsupportedDateType | date(\"MM/dd/yyyy\") }}";

      PebbleTemplate template = pebble.getTemplate(source);
      Map<String, Object> context = new HashMap<>();
      context.put("unsupportedDateType", TRUE);

      Writer writer = new StringWriter();
      template.evaluate(writer, context);
    });
  }

  @Test
  void testUrlEncode() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ 'The string ü@foo-bar' | urlencode }}");
    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("The+string+%C3%BC%40foo-bar", writer.toString());
  }

  @Test
  void testUrlEncodeWithNullInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ null | urlencode }}");
    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("", writer.toString());
  }

  @Test
  void testNumberFormatFilterWithFormat() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false)
        .defaultLocale(Locale.ENGLISH).build();

    PebbleTemplate template = pebble
        .getTemplate("You owe me {{ 10000.235166 | numberformat(currencyFormat) }}.");
    Map<String, Object> context = new HashMap<>();
    context.put("currencyFormat", "$#,###,###,##0.00");

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("You owe me $10,000.24.", writer.toString());
  }

  @Test
  void testNumberFormatFilterWithNamedArgument() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false)
        .defaultLocale(Locale.US).build();

    PebbleTemplate template = pebble
        .getTemplate("You owe me {{ 10000.235166 | numberformat(format=currencyFormat) }}.");
    Map<String, Object> context = new HashMap<>();
    context.put("currencyFormat", "$#,###,###,##0.00");

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("You owe me $10,000.24.", writer.toString());
  }

  @Test
  void testNumberFormatFilterWithNullInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ null | numberformat(currencyFormat) }}");

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("", writer.toString());
  }

  @Test
  void testNumberFormatFilterWithLocale() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false)
        .defaultLocale(Locale.ENGLISH).build();

    PebbleTemplate template = pebble.getTemplate("{{ 1000000 | numberformat }}");

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("1,000,000", writer.toString());
  }

  @Test
  void testAbbreviate() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble
        .getTemplate("{{ 'This is a test of the abbreviate filter' | abbreviate(16) }}");

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("This is a tes...", writer.toString());
  }

  @Test
  void testAbbreviateWithNamedArguments() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble
        .getTemplate("{{ 'This is a test of the abbreviate filter' | abbreviate(length=16) }}");

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("This is a tes...", writer.toString());
  }

  @Test
  void testAbbreviateWithNullInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ null | abbreviate(16) }}");

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("", writer.toString());
  }

  @Test
  void testAbbreviateWithSmallLength() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();
    PebbleTemplate template = pebble.getTemplate("{{ text | abbreviate(2)}}");
    Map<String, Object> context = new HashMap<>();
    context.put("text", "1234567");
    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("12", writer.toString());
  }

  @Test
  void testCapitalize() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble
        .getTemplate("{{ 'this should be capitalized.' | capitalize }}");

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("This should be capitalized.", writer.toString());
  }

  @Test
  void testCapitalizeWithLeadingWhitespace() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble
        .getTemplate("{{ ' \nthis should be capitalized.' | capitalize }}");

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals(" \nThis should be capitalized.", writer.toString());
  }

  @Test
  void testCapitalizeWithNullInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ null | capitalize }}");

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("", writer.toString());
  }

  @Test
  void testCapitalizeWithEmptyString() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ '' | capitalize }}");

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("", writer.toString());
  }

  @Test
  void testSortFilter() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble
        .getTemplate("{% for word in words|sort %}{{ word }} {% endfor %}");
    List<String> words = new ArrayList<>();
    words.add("zebra");
    words.add("apple");
    words.add(" cat");
    words.add("123");
    words.add("Apple");
    words.add("cat");

    Map<String, Object> context = new HashMap<>();
    context.put("words", words);
    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals(" cat 123 Apple apple cat zebra ", writer.toString());
  }

  @Test
  void testRsortFilter() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble
        .getTemplate("{% for word in words|rsort %}{{ word }} {% endfor %}");
    List<String> words = new ArrayList<>();
    words.add("zebra");
    words.add("apple");
    words.add(" cat");
    words.add("123");
    words.add("Apple");
    words.add("cat");

    Map<String, Object> context = new HashMap<>();
    context.put("words", words);
    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("zebra cat apple Apple 123  cat ", writer.toString());
  }

  @Test
  void testReverseFilter() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble
        .getTemplate("{% for word in words|reverse %}{{ word }} {% endfor %}");
    List<String> words = new ArrayList<>();
    words.add("one");
    words.add("two");
    words.add("three");

    Map<String, Object> context = new HashMap<>();
    context.put("words", words);
    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("three two one ", writer.toString());
  }

  @Test
  void testTitle() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate(
        "{{ null | title }} {{ 'test' | title }} {{ 'test test' | title }} {{ 'TEST TEST' | title }}");

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals(" Test Test Test TEST TEST", writer.toString());
  }

  @Test
  void testTitleWithLeadingWhitespace() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ ' \ntest' | title }}");

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals(" \nTest", writer.toString());
  }

  @Test
  void testTrim() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble
        .getTemplate("{{ '        		This should be trimmed. 		' | trim }}");

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("This should be trimmed.", writer.toString());
  }

  @Test
  void testTrimWithNullInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ null | trim }}");

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("", writer.toString());
  }

  @Test
  void testDefault() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate(
        "{{ obj|default('ONE') }} {{ null|default('TWO') }} {{ '  ' |default('THREE') }} {{ 4 |default('FOUR') }}");
    Map<String, Object> context = new HashMap<>();
    context.put("obj", null);

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("ONE TWO THREE 4", writer.toString());
  }

  /**
   * Tests if the {@link DefaultFilter} is working as
   * expected.
   *
   * @throws Exception thrown when something went wrong.
   */
  @Test
  void testDefaultFilterWithStrictMode() throws Exception {

    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(true).build();

    PebbleTemplate template = pebble.getTemplate("{{ name | default('test') }}");

    Map<String, Object> context = new HashMap<>();

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("test", writer.toString());

  }

  @Test
  void testDefaultWithNamedArguments() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ obj|default(default='ONE') }}");
    Map<String, Object> context = new HashMap<>();
    context.put("obj", null);

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("ONE", writer.toString());
  }

  @Test
  void testFirst() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ names | first }}");

    List<String> names = new ArrayList<>();
    names.add("Alex");
    names.add("Joe");
    names.add("Bob");

    Map<String, Object> context = new HashMap<>();
    context.put("names", names);

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("Alex", writer.toString());
  }

  @Test
  void testFirstWithNullInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ names | first }}");

    Map<String, Object> context = new HashMap<>();
    context.put("names", null);

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("", writer.toString());
  }

  @Test
  void testFirstWithStringInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ name | first }}");

    Map<String, Object> context = new HashMap<>();
    context.put("name", "Alex");

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("A", writer.toString());
  }

  @Test
  void testFirstWithEmptyCollection() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ names | first }}");

    Map<String, Object> context = new HashMap<>();
    context.put("names", emptyList());

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("", writer.toString());
  }

  @Test
  void testJoin() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ names | join(',') }}");

    List<String> names = new ArrayList<>();
    names.add("Alex");
    names.add("Joe");
    names.add("Bob");

    Map<String, Object> context = new HashMap<>();
    context.put("names", names);

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("Alex,Joe,Bob", writer.toString());
  }

  @Test
  void testJoinWithoutGlue() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ names | join }}");

    List<String> names = new ArrayList<>();
    names.add("Alex");
    names.add("Joe");
    names.add("Bob");

    Map<String, Object> context = new HashMap<>();
    context.put("names", names);

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("AlexJoeBob", writer.toString());
  }

  @Test
  void testJoinWithNumbers() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ numbers | join(',') }}");

    List<Integer> numbers = new ArrayList<>();
    numbers.add(1);
    numbers.add(2);
    numbers.add(3);

    Map<String, Object> context = new HashMap<>();
    context.put("numbers", numbers);

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("1,2,3", writer.toString());
  }

  @Test
  void testJoinWithNullInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ null | join(',') }}");

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("", writer.toString());
  }

  @Test
  void testJoinWithStringArray() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ names | join(',') }}");

    String[] names = new String[]{"Alex", "Joe", "Bob"};

    Map<String, Object> context = new HashMap<>();
    context.put("names", names);

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("Alex,Joe,Bob", writer.toString());
  }

  @Test
  void testJoinWithStringArrayWithoutGlue() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ names | join }}");

    String[] names = new String[]{"Alex", "Joe", "Bob"};

    Map<String, Object> context = new HashMap<>();
    context.put("names", names);

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("AlexJoeBob", writer.toString());
  }

  @Test
  void testJoinWithNumbersArray() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ numbers | join(',') }}");

    int[] numbers = new int[]{1, 2, 3};

    Map<String, Object> context = new HashMap<>();
    context.put("numbers", numbers);

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("1,2,3", writer.toString());
  }

  @Test
  void testJoinWithEmptyNumbersArray() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ numbers | join(',') }}");

    int[] numbers = new int[0];

    Map<String, Object> context = new HashMap<>();
    context.put("numbers", numbers);

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("", writer.toString());
  }

  @Test
  void testJoinWithFloatArray() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ numbers | join(',') }}");

    float[] numbers = new float[]{1.0f, 2.5f, 3.0f};

    Map<String, Object> context = new HashMap<>();
    context.put("numbers", numbers);

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("1.0,2.5,3.0", writer.toString());
  }

  @Test
  void testLast() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ names | last }}");

    List<String> names = new ArrayList<>();
    names.add("Alex");
    names.add("Joe");
    names.add("Bob");

    Map<String, Object> context = new HashMap<>();
    context.put("names", names);

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("Bob", writer.toString());
  }

  @Test
  void testLastWithNullInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ null | last }}");

    Writer writer = new StringWriter();
    template.evaluate(writer);
    assertEquals("", writer.toString());
  }

  @Test
  void testLastWithStringInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ name | last }}");

    Map<String, Object> context = new HashMap<>();
    context.put("name", "Alex");

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("x", writer.toString());
  }

  @Test
  void testLastWithArrayInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ names | last }}");

    Map<String, Object> context = new HashMap<>();
    context.put("names", new String[]{"FirstName", "FamilyName"});

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("FamilyName", writer.toString());
  }

  @Test
  void testLastWithPrimitiveArrayInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ ages | last }}");

    Map<String, Object> context = new HashMap<>();
    context.put("ages", new int[]{28, 30});

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("30", writer.toString());
  }

  @Test
  void testFirstWithArrayInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ names | first }}");

    Map<String, Object> context = new HashMap<>();
    context.put("names", new String[]{"FirstName", "FamilyName"});

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("FirstName", writer.toString());
  }

  @Test
  void testFirstWithPrimitiveArrayInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ ages | first }}");

    Map<String, Object> context = new HashMap<>();
    context.put("ages", new int[]{28, 30});

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("28", writer.toString());
  }

  public class User {

    private final String username;

    public User(String username) {
      this.username = username;
    }

    public String getUsername() {
      return this.username;
    }
  }

  @Test
  void testSliceWithNullInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ null | slice }}");

    Map<String, Object> context = new HashMap<>();

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("", writer.toString());
  }

  @Test
  void testSliceWithDefaultArgs() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ name | slice }}");

    Map<String, Object> context = new HashMap<>();
    context.put("name", "Alex");

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("Alex", writer.toString());
  }

  @Test
  void testSliceWithInvalidFirstArg() throws PebbleException, IOException {
    assertThrows(IllegalArgumentException.class, () -> {
      PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
          .strictVariables(false).build();

      PebbleTemplate template = pebble.getTemplate("{{ name | slice(-1) }}");

      Map<String, Object> context = new HashMap<>();
      context.put("name", "Alex");

      Writer writer = new StringWriter();
      template.evaluate(writer, context);
    });
  }

  @Test
  void testSliceWithIntegerArguments() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble
        .getTemplate("{{ 'abcdefghijklmnopqrstuvwxyz' | slice(from, to) }}");

    Map<String, Object> context = new HashMap<>();
    context.put("from", new Integer(2));
    context.put("to", new Integer(4));

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("cd", writer.toString());
  }

  @Test
  void testSliceWithInvalidSecondArg() throws PebbleException, IOException {
    assertThrows(PebbleException.class, () -> {
      PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
          .strictVariables(false).build();

      PebbleTemplate template = pebble.getTemplate("{{ name | slice(0,-1) }}");

      Map<String, Object> context = new HashMap<>();
      context.put("name", "Alex");

      Writer writer = new StringWriter();
      template.evaluate(writer, context);
    });
  }

  @Test
  void testSliceWithInvalidSecondArg2() throws PebbleException, IOException {
    assertThrows(PebbleException.class, () -> {
      PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
          .strictVariables(false).build();

      PebbleTemplate template = pebble.getTemplate("{{ name | slice(0,1000) }}");

      Map<String, Object> context = new HashMap<>();
      context.put("name", "Alex");

      Writer writer = new StringWriter();
      template.evaluate(writer, context);
    });
  }

  @Test
  void testSliceWithString() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ name | slice(2,5) }}");

    Map<String, Object> context = new HashMap<>();
    context.put("name", "Alexander");

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("exa", writer.toString());
  }

  @Test
  void testSliceWithList() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ names | slice(2,5) }}");

    List<String> names = new ArrayList<>();
    names.add("Alex");
    names.add("Joe");
    names.add("Bob");
    names.add("Sarah");
    names.add("Mary");
    names.add("Marge");
    Map<String, Object> context = new HashMap<>();
    context.put("names", names);

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("[Bob, Sarah, Mary]", writer.toString());
  }

  @Test
  void testSliceWithStringArray() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{% set n = names | slice(2,5) %}{{ n[0] }}");

    String[] names = new String[]{"Alex", "Joe", "Bob", "Sarah", "Mary", "Marge"};
    Map<String, Object> context = new HashMap<>();
    context.put("names", names);

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("Bob", writer.toString());
  }

  @Test
  void testSliceWithPrimitivesArray() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{% set p = primitives | slice(2,5) %}{{ p[0] }}");
    Map<String, Object> context = new HashMap<>();
    Writer writer;

    // boolean
    writer = new StringWriter();
    boolean[] booleans = new boolean[]{true, false, true, false, true, false};
    context.put("primitives", booleans);
    template.evaluate(writer, context);
    assertEquals("true", writer.toString());

    // byte
    writer = new StringWriter();
    byte[] bytes = new byte[]{0, 1, 2, 3, 4, 5};
    context.put("primitives", bytes);
    template.evaluate(writer, context);
    assertEquals("2", writer.toString());

    // char
    writer = new StringWriter();
    char[] chars = new char[]{'a', 'b', 'c', 'd', 'e', 'f'};
    context.put("primitives", chars);
    template.evaluate(writer, context);
    assertEquals("c", writer.toString());

    // double
    writer = new StringWriter();
    double[] doubles = new double[]{0.0d, 1.0d, 2.0d, 3.0d, 4.0d, 5.0d};
    context.put("primitives", doubles);
    template.evaluate(writer, context);
    assertEquals("2.0", writer.toString());

    // float
    writer = new StringWriter();
    float[] floats = new float[]{0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
    context.put("primitives", floats);
    template.evaluate(writer, context);
    assertEquals("2.0", writer.toString());

    // int
    writer = new StringWriter();
    int[] ints = new int[]{0, 1, 2, 3, 4, 5};
    context.put("primitives", ints);
    template.evaluate(writer, context);
    assertEquals("2", writer.toString());

    // long
    writer = new StringWriter();
    long[] longs = new long[]{0, 1, 2, 3, 4, 5};
    context.put("primitives", longs);
    template.evaluate(writer, context);
    assertEquals("2", writer.toString());

    // short
    writer = new StringWriter();
    short[] shorts = new short[]{0, 1, 2, 3, 4, 5};
    context.put("primitives", shorts);
    template.evaluate(writer, context);
    assertEquals("2", writer.toString());
  }

  @Test
  void testSliceWithInvalidInputType() throws PebbleException, IOException {
    assertThrows(PebbleException.class, () -> {
      PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
          .strictVariables(false).build();

      PebbleTemplate template = pebble.getTemplate("{{ names | slice(2,5) }}");

      Map<String, Object> context = new HashMap<>();
      context.put("names", Long.valueOf(1));

      Writer writer = new StringWriter();
      template.evaluate(writer, context);
    });
  }

  /**
   * Tests {@link LengthFilter} with different inputs.
   */
  @Test
  void testLengthFilterInputs() {
    LengthFilter filter = new LengthFilter();

    assertEquals(0, filter.apply(null, null, null, null, 0));
    assertEquals(4, filter.apply("test", null, null, null, 0));
    assertEquals(0, filter.apply(Collections.EMPTY_LIST, null, null, null, 0));
    assertEquals(2, filter.apply(Arrays.asList("tttt", "ssss"), null, null, null, 0));
    assertEquals(2, filter.apply(Arrays.asList("tttt", "ssss").iterator(), null, null, null, 0));
    Map<String, String> test = new HashMap<>();
    test.put("test", "test");
    test.put("other", "other");
    test.put("and_other", "other");
    assertEquals(3, filter.apply(test, null, null, null, 0));
  }

  /**
   * Tests {@link LengthFilter} if the length filter is working within templates.
   */
  @Test
  void testLengthFilterInTemplate() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("{{ names | length }}");

    Map<String, Object> context = new HashMap<>();
    context.put("names", "test");

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("4", writer.toString());
  }

  /**
   * Tests {@link ReplaceFilter} if it can handle a null input.
   */
  @Test
  void testReplaceFilterNullInput() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
            .strictVariables(false).build();

    PebbleTemplate template = pebble
            .getTemplate(
                    "{{ null |replace({'%this%': foo, '%that%': \"bar\"}) }}");

    Writer writer = new StringWriter();
    assertDoesNotThrow(() -> template.evaluate(writer, new HashMap<>()));
  }

  /**
   * Tests {@link ReplaceFilter} if the length filter is working within templates.
   */
  @Test
  void testReplaceFilterInTemplate() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .strictVariables(false).build();

    PebbleTemplate template = pebble
        .getTemplate(
            "{{ \"I like %this% and %that%.\"|replace({'%this%': foo, '%that%': \"bar\"}) }}");

    Map<String, Object> context = new HashMap<>();
    context.put("foo", "foo");

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("I like foo and bar.", writer.toString());
  }

  /**
   * Tests {@link Base64EncoderFilter} if the base64 encoding filter is working for a string value, a string constant, null.
   */
  @Test
  void testBase64EncoderFilterInTemplate() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
            .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("var=\"{{ var | base64encode }}\" const=\"{{ \"test\" | base64encode }}\" null=\"{{ null | base64encode }}\"");

    Map<String, Object> context = new HashMap<>();
    context.put("var", "test");

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("var=\"dGVzdA==\" const=\"dGVzdA==\" null=\"\"", writer.toString());
  }

  /**
   * Tests {@link Base64DecoderFilter} if the base64 decoder filter is working for a string value, a string constant, null.
   */
  @Test
  void testBase64DecoderFilterInTemplate() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
            .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("var=\"{{ var | base64decode }}\" const=\"{{ \"dGVzdA==\" | base64decode }}\" null=\"{{ null | base64decode }}\"");

    Map<String, Object> context = new HashMap<>();
    context.put("var", "dGVzdA==");

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("var=\"test\" const=\"test\" null=\"\"", writer.toString());
  }

  @Test
  void testBase64DecodeFilterBadEncodedStringFail() throws PebbleException, IOException {
    assertThrows(PebbleException.class, () -> {
      PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
              .strictVariables(false).build();

      PebbleTemplate template = pebble.getTemplate("{{ \"this is not a base64 encoded string\" | base64decode }}");

      Map<String, Object> context = new HashMap<>();

      Writer writer = new StringWriter();
      template.evaluate(writer, context);
    });
  }

  @Test
  void testBase64DecodeFilterNoStringFail() throws PebbleException, IOException {
    assertThrows(PebbleException.class, () -> {
      PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
              .strictVariables(false).build();

      PebbleTemplate template = pebble.getTemplate("{{ {'foo':1} | base64decode }}");

      Map<String, Object> context = new HashMap<>();

      Writer writer = new StringWriter();
      template.evaluate(writer, context);
    });
  }

  @Test
  void testSha256FilterNoStringFail() throws PebbleException, IOException {
    assertThrows(PebbleException.class, () -> {
      PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
              .strictVariables(false).build();

      PebbleTemplate template = pebble.getTemplate("{{ {'foo':1} | sha256 }}");

      Map<String, Object> context = new HashMap<>();

      Writer writer = new StringWriter();
      template.evaluate(writer, context);
    });
  }

  /**
   * Tests {@link Sha256Filter} if the SHA256 hashing filter is working for a string value, a string constant, null.
   */
  @Test
  void testSha256FilterInTemplate() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
            .strictVariables(false).build();

    PebbleTemplate template = pebble.getTemplate("var=\"{{ var | sha256 }}\" const=\"{{ \"test\" | sha256}}\" null=\"{{ null | sha256 }}\"");

    Map<String, Object> context = new HashMap<>();
    context.put("var", "test");

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("var=\"9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08\" const=\"9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08\" null=\"\"", writer.toString());
  }

  @Test
  void testMergeOk() throws PebbleException, IOException {
    PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
        .extension(new TestingExtension()).strictVariables(false).build();

    PebbleTemplate template = pebble
        .getTemplate(
            "{{{'one':1}|merge({'two':2})|mapToString}} {%set m1 = {'one':1}|merge(['two'])%}{{m1['two']}} {{[1]|merge([2])|listToString}} {%set l1 = [1]|merge({'two':2})%}{{l1[1].value}} {{arr1|merge(arr2)|arrayToString}}");

    Map<String, Object> context = new HashMap<>();
    context.put("arr1", new int[]{1});
    context.put("arr2", new int[]{2});

    Writer writer = new StringWriter();
    template.evaluate(writer, context);
    assertEquals("{one=1, two=2} two [1,2] 2 [1,2]", writer.toString());
  }

  @Test
  void testMergeMapWithStringAndFail() throws PebbleException, IOException {
    assertThrows(UnsupportedOperationException.class, () -> {
      PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
          .strictVariables(false).build();

      PebbleTemplate template = pebble.getTemplate("{{ {'one':1}|merge('No way!') }}");

      Map<String, Object> context = new HashMap<>();

      Writer writer = new StringWriter();
      template.evaluate(writer, context);
    });
  }

  @Test
  void testMergeListWithStringAndFail() throws PebbleException, IOException {
    assertThrows(PebbleException.class, () -> {
      PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
          .strictVariables(false).build();

      PebbleTemplate template = pebble.getTemplate("{{ [1]|merge('No way!') }}");

      Map<String, Object> context = new HashMap<>();

      Writer writer = new StringWriter();
      template.evaluate(writer, context);
    });
  }

  @Test
  void testMergeDifferentArraysAndFail() throws PebbleException, IOException {
    assertThrows(PebbleException.class, () -> {
      PebbleEngine pebble = new PebbleEngine.Builder().loader(new StringLoader())
          .strictVariables(false).build();

      PebbleTemplate template = pebble.getTemplate("{{ arr1|merge(arr2) }}");

      Map<String, Object> context = new HashMap<>();
      context.put("arr1", new int[]{1});
      context.put("arr2", new String[]{"2"});

      Writer writer = new StringWriter();
      template.evaluate(writer, context);
    });
  }

}
