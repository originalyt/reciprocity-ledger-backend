import 'package:intl/intl.dart';

class LedgerFormatters {
  LedgerFormatters._();

  static final NumberFormat _currencyNoDecimal = NumberFormat.currency(
    locale: 'zh_CN',
    symbol: '¥',
    decimalDigits: 0,
  );

  static final NumberFormat _currencyWithDecimal = NumberFormat.currency(
    locale: 'zh_CN',
    symbol: '¥',
    decimalDigits: 2,
  );

  static final DateFormat _monthDay = DateFormat('M月d日');
  static final DateFormat _fullDate = DateFormat('yyyy年M月d日');
  static final DateFormat _yearMonth = DateFormat('yyyy年M月');

  static String amount(double value) {
    if (value == value.roundToDouble()) {
      return _currencyNoDecimal.format(value);
    }
    return _currencyWithDecimal.format(value);
  }

  static String monthDay(DateTime value) {
    return _monthDay.format(value);
  }

  static String fullDate(DateTime value) {
    return _fullDate.format(value);
  }

  static String yearMonth(DateTime value) {
    return _yearMonth.format(value);
  }
}
