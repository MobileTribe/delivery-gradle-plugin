package com.leroymerlin.plugins.cli

final class Ansi {

    public static final String SANE = "\u001B[0m"

    public static final String HIGH_INTENSITY = "\u001B[1m"
    public static final String LOW_INTENSITY = "\u001B[2m"

    public static final String ITALIC = "\u001B[3m"
    public static final String UNDERLINE = "\u001B[4m"
    public static final String BLINK = "\u001B[5m"
    public static final String RAPID_BLINK = "\u001B[6m"

    public static final String BLACK = "\u001B[30m"
    public static final String RED = "\u001B[31m"
    public static final String GREEN = "\u001B[32m"
    public static final String YELLOW = "\u001B[33m"
    public static final String BLUE = "\u001B[34m"
    public static final String MAGENTA = "\u001B[35m"
    public static final String CYAN = "\u001B[36m"
    public static final String WHITE = "\u001B[37m"

    public static final String BACKGROUND_BLACK = "\u001B[40m"
    public static final String BACKGROUND_RED = "\u001B[41m"
    public static final String BACKGROUND_GREEN = "\u001B[42m"
    public static final String BACKGROUND_YELLOW = "\u001B[43m"
    public static final String BACKGROUND_BLUE = "\u001B[44m"
    public static final String BACKGROUND_MAGENTA = "\u001B[45m"
    public static final String BACKGROUND_CYAN = "\u001B[46m"
    public static final String BACKGROUND_WHITE = "\u001B[47m"

    public static final Ansi Bold = new Ansi(HIGH_INTENSITY)
    public static final Ansi Normal = new Ansi(LOW_INTENSITY)

    public static final Ansi Italic = new Ansi(ITALIC)
    public static final Ansi Underline = new Ansi(UNDERLINE)
    public static final Ansi Blink = new Ansi(BLINK)
    public static final Ansi RapidBlink = new Ansi(RAPID_BLINK)

    public static final Ansi Black = new Ansi(BLACK)
    public static final Ansi Red = new Ansi(RED)
    public static final Ansi Green = new Ansi(GREEN)
    public static final Ansi Yellow = new Ansi(YELLOW)
    public static final Ansi Blue = new Ansi(BLUE)
    public static final Ansi Magenta = new Ansi(MAGENTA)
    public static final Ansi Cyan = new Ansi(CYAN)
    public static final Ansi White = new Ansi(WHITE)

    public static final Ansi BgBlack = new Ansi(BACKGROUND_BLACK)
    public static final Ansi BgRed = new Ansi(BACKGROUND_RED)
    public static final Ansi BgGreen = new Ansi(BACKGROUND_GREEN)
    public static final Ansi BgYellow = new Ansi(BACKGROUND_YELLOW)
    public static final Ansi BgBlue = new Ansi(BACKGROUND_BLUE)
    public static final Ansi BgMagenta = new Ansi(BACKGROUND_MAGENTA)
    public static final Ansi BgCyan = new Ansi(BACKGROUND_CYAN)
    public static final Ansi BgWhite = new Ansi(BACKGROUND_WHITE)

    final private List<String> codes = new ArrayList<>()
    final private String codes_str

    Ansi(String code) {
        codes_str = code
    }

    Ansi(String... codes) {
        this.codes.addAll(codes)
        String _codes_str = ""
        for (String code : codes) {
            _codes_str += code
        }
        codes_str = _codes_str
    }

    String colorize(String original) {
        return codes_str + original + SANE
    }
}