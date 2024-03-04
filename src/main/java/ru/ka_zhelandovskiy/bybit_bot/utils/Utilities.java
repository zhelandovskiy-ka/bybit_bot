package ru.ka_zhelandovskiy.bybit_bot.utils;

public class Utilities {
    public static double roundDouble(double d) {
        if (d < 1)
            return Math.round(d * 1000.0) / 1000.0;
        return Math.round(d * 100.0) / 100.0;
    }

    public static double roundDouble(double d, int count) {
        String s = String.format("%." + count + "f", d);

        if (s.contains(","))
            s = s.replace(',', '.');

        return Double.parseDouble(s);
    }
}
