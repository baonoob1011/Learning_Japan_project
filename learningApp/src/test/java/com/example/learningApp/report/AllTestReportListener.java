package com.example.learningApp.report;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tự động ghi báo cáo test ra file khi JUnit chạy xong.
 */
public class AllTestReportListener implements TestExecutionListener {

    private final AtomicInteger total = new AtomicInteger();
    private final AtomicInteger passed = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();
    private final AtomicInteger skipped = new AtomicInteger();
    private final List<String> failureLines = new ArrayList<>();

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        total.set(0);
        passed.set(0);
        failed.set(0);
        skipped.set(0);
        synchronized (failureLines) {
            failureLines.clear();
        }
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (!testIdentifier.isTest()) {
            return;
        }

        total.incrementAndGet();
        switch (testExecutionResult.getStatus()) {
            case SUCCESSFUL -> passed.incrementAndGet();
            case FAILED -> {
                failed.incrementAndGet();
                synchronized (failureLines) {
                    failureLines.add("FAILED: " + displayName(testIdentifier) + " -> " +
                            testExecutionResult.getThrowable().map(Throwable::getMessage).orElse("Unknown error"));
                }
            }
            case ABORTED -> skipped.incrementAndGet();
        }
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        writeReport();
        writePdfReport();
    }

    private String displayName(TestIdentifier identifier) {
        return identifier.getDisplayName() + " [" + identifier.getUniqueId() + "]";
    }

    private void writeReport() {
        Path reportDir = Paths.get(System.getProperty("user.dir"), "target", "test-reports");
        Path reportFile = reportDir.resolve("allservice-report.txt");

        try {
            Files.createDirectories(reportDir);
            List<String> lines = new ArrayList<>();
            lines.add("AllServiceSuite Test Report");
            lines.add("GeneratedAt: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            lines.add("Total: " + total.get());
            lines.add("Passed: " + passed.get());
            lines.add("Failed: " + failed.get());
            lines.add("Skipped: " + skipped.get());
            lines.add("");
            lines.add("Failures:");
            synchronized (failureLines) {
                if (failureLines.isEmpty()) {
                    lines.add("None");
                } else {
                    lines.addAll(failureLines);
                }
            }

            Files.write(reportFile, lines, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // Không làm fail test nếu chỉ lỗi ghi file report.
        }
    }

    private void writePdfReport() {
        Path reportDir = Paths.get(System.getProperty("user.dir"), "target", "test-reports");
        Path reportFile = reportDir.resolve("allservice-report.pdf");

        try {
            Files.createDirectories(reportDir);

            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                    contentStream.newLineAtOffset(50, 780);
                    contentStream.showText("AllServiceSuite Test Report");

                    contentStream.setFont(PDType1Font.HELVETICA, 11);
                    int y = 755;
                    String[] lines = {
                            "GeneratedAt: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            "Total: " + total.get(),
                            "Passed: " + passed.get(),
                            "Failed: " + failed.get(),
                            "Skipped: " + skipped.get(),
                            "",
                            "Failures:"
                    };

                    for (String line : lines) {
                        contentStream.newLineAtOffset(0, -18);
                        if (!line.isEmpty()) {
                            contentStream.showText(line);
                        }
                        y -= 18;
                    }

                    synchronized (failureLines) {
                        if (failureLines.isEmpty()) {
                            contentStream.newLineAtOffset(0, -18);
                            contentStream.showText("None");
                        } else {
                            for (String failureLine : failureLines) {
                                contentStream.newLineAtOffset(0, -18);
                                contentStream.showText(sanitizeForPdf(failureLine));
                            }
                        }
                    }

                    contentStream.endText();
                }

                document.save(reportFile.toFile());
            }
        } catch (IOException ignored) {
            // Không làm fail test nếu chỉ lỗi ghi PDF report.
        }
    }

    private String sanitizeForPdf(String text) {
        return text == null ? "" : text.replace('\n', ' ').replace('\r', ' ');
    }
}
