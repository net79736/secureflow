package com.tdd.secureflow.log;

import org.springframework.boot.ansi.AnsiColor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.pattern.color.HighlightingCompositeConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class CustomHighlightingConverter extends HighlightingCompositeConverter {

    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        int level = event.getLevel().toInt();
        switch (level) {
            case Level.TRACE_INT:
                return AnsiColor.WHITE.toString();       // TRACE - 흰색
            case Level.DEBUG_INT:
                return AnsiColor.BLUE.toString();        // DEBUG - 파랑
            case Level.INFO_INT:
                return AnsiColor.GREEN.toString();     // INFO - 초록
            case Level.WARN_INT:
                return AnsiColor.YELLOW.toString();      // WARN - 노랑
            case Level.ERROR_INT:
                return AnsiColor.RED.toString();         // ERROR - 빨강
            default:
                return AnsiColor.DEFAULT.toString();
        }
    }
}
