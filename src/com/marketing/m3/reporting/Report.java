package com.marketing.m3.reporting;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Report {
    private final String title;
    private final LocalDateTime generatedAt;
    private final List<ReportSection> sections;

    public Report(String title, List<ReportSection> sections) {
        this.title = title;
        this.generatedAt = LocalDateTime.now();
        this.sections = sections;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public List<ReportSection> getSections() {
        return Collections.unmodifiableList(sections);
    }

    public String renderText() {
        StringBuilder builder = new StringBuilder();
        builder.append(title).append('\n');
        builder.append("Generated: ")
                .append(generatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n\n");

        for (ReportSection section : sections) {
            builder.append(section.getHeading()).append('\n');
            builder.append(section.getContent()).append("\n\n");
        }

        return builder.toString().trim();
    }

    public static class ReportSection {
        private final String heading;
        private final String content;

        public ReportSection(String heading, String content) {
            this.heading = heading;
            this.content = content;
        }

        public String getHeading() {
            return heading;
        }

        public String getContent() {
            return content;
        }
    }

    public static class MutableReportSections {
        private final List<ReportSection> sections = new ArrayList<>();

        public void add(String heading, String content) {
            sections.add(new ReportSection(heading, content));
        }

        public List<ReportSection> toList() {
            return sections;
        }
    }
}
