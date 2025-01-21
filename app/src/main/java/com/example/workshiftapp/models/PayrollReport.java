package com.example.workshiftapp.models;

import android.content.Context;
import android.os.Environment;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PayrollReport {
    private String companyName;
    private String companyAddress;
    private final DecimalFormat currencyFormat;
    private final DecimalFormat hoursFormat;
    private final SimpleDateFormat dateFormat;

    // Deduction rates
    private static final double FEDERAL_TAX_RATE = 0.15;
    private static final double STATE_TAX_RATE = 0.05;
    private static final double SOCIAL_SECURITY_RATE = 0.062;
    private static final double MEDICARE_RATE = 0.0145;
    private static final double RETIREMENT_401K_RATE = 0.05;

    public PayrollReport() {
        this("ACME Corporation", "123 Business Ave, Suite 100, Business City, ST 12345");
    }

    public PayrollReport(String companyName, String companyAddress) {
        this.companyName = companyName;
        this.companyAddress = companyAddress;
        this.currencyFormat = new DecimalFormat("#,##0.00");
        this.hoursFormat = new DecimalFormat("0.00");
        this.dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
    }

    private String generateBorderLine(char borderChar) {
        return "+" + new String(new char[78]).replace('\0', borderChar) + "+\n";
    }

    private String centerText(String text, int width) {
        int padding = Math.max(0, width - text.length()) / 2;
        return String.format("%" + padding + "s%s%" + padding + "s", "", text, "");
    }

    public String generatePayrollReport(PayrollData data) {
        StringBuilder stub = new StringBuilder();

        // Calculate earnings
        double regularHours = Math.min(160, data.hoursWorked); // 40 hours * 4 weeks
        double overtimeHours = Math.max(0, data.hoursWorked - regularHours);
        double regularPay = regularHours * data.hourlyRate;
        double overtimePay = overtimeHours * (data.hourlyRate * 1.5);
        double grossPay = regularPay + overtimePay;

        // Calculate deductions
        double federalTax = grossPay * FEDERAL_TAX_RATE;
        double stateTax = grossPay * STATE_TAX_RATE;
        double socialSecurity = grossPay * SOCIAL_SECURITY_RATE;
        double medicare = grossPay * MEDICARE_RATE;
        double retirement = grossPay * RETIREMENT_401K_RATE;
        double totalDeductions = federalTax + stateTax + socialSecurity + medicare + retirement;
        double netPay = grossPay - totalDeductions;
        double ytdTotal = data.ytdEarnings + grossPay;

        // Generate header
        stub.append(generateBorderLine('='));
        stub.append(String.format("|%s|\n", centerText(companyName, 78)));
        stub.append(String.format("|%s|\n", centerText(companyAddress, 78)));
        stub.append(String.format("|%s|\n", centerText("NON-NEGOTIABLE", 78)));
        stub.append(generateBorderLine('='));

        // Pay period info
        String periodDate = String.format("PAY PERIOD: %s %d", data.month, data.year);
        stub.append(String.format("|%-39s CHECK NO: %05d%-34s|\n",
                periodDate, data.payPeriodNum, ""));

        // Employee info
        stub.append(generateBorderLine('-'));
        stub.append(String.format("| EMPLOYEE: %-33s EMP ID: %-29s |\n",
                data.employeeName, data.employeeId));
        stub.append(String.format("| DEPT: %-73s |\n", data.department));
        stub.append(generateBorderLine('-'));

        // Earnings section
        stub.append("| EARNINGS           HOURS     RATE          CURRENT         YTD              |\n");
        stub.append(generateBorderLine('-'));
        stub.append(String.format("| Regular Pay      %8s   %8s      %10s                 |\n",
                hoursFormat.format(regularHours),
                currencyFormat.format(data.hourlyRate),
                currencyFormat.format(regularPay)));
        stub.append(String.format("| Overtime Pay     %8s   %8s      %10s                 |\n",
                hoursFormat.format(overtimeHours),
                currencyFormat.format(data.hourlyRate * 1.5),
                currencyFormat.format(overtimePay)));
        stub.append(generateBorderLine('-'));
        stub.append(String.format("| GROSS PAY                                %10s    %10s     |\n",
                currencyFormat.format(grossPay),
                currencyFormat.format(ytdTotal)));
        stub.append(generateBorderLine('-'));

        // Deductions section
        stub.append("| DEDUCTIONS                    CURRENT         YTD                          |\n");
        stub.append(generateBorderLine('-'));
        stub.append(String.format("| Federal Tax                 %10s                                |\n",
                currencyFormat.format(federalTax)));
        stub.append(String.format("| State Tax                   %10s                                |\n",
                currencyFormat.format(stateTax)));
        stub.append(String.format("| Social Security             %10s                                |\n",
                currencyFormat.format(socialSecurity)));
        stub.append(String.format("| Medicare                    %10s                                |\n",
                currencyFormat.format(medicare)));
        stub.append(String.format("| 401(k)                      %10s                                |\n",
                currencyFormat.format(retirement)));
        stub.append(generateBorderLine('-'));
        stub.append(String.format("| TOTAL DEDUCTIONS                        %10s                       |\n",
                currencyFormat.format(totalDeductions)));
        stub.append(generateBorderLine('='));

        // Net pay section
        stub.append(String.format("| NET PAY                                 %10s                       |\n",
                currencyFormat.format(netPay)));
        stub.append(generateBorderLine('='));

        // Footer
        stub.append(String.format("| CURRENT DATE: %-66s |\n",
                dateFormat.format(new Date())));
        stub.append("| DIRECT DEPOSIT - THIS IS NOT A CHECK                                       |\n");
        stub.append(generateBorderLine('='));

        return stub.toString();
    }

    public File writeToPrivateStorage(Context context, PayrollData data, String filename) throws IOException {
        String reportContent = generatePayrollReport(data);

        if (filename == null) {
            filename = String.format("payroll_%s_%s_%d.txt",
                    data.employeeId,
                    data.month.toLowerCase(),
                    data.year);
        }

        File directory = context.getFilesDir();
        File file = new File(directory, filename);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(reportContent);
        }

        return file;
    }

    public File writeToExternalStorage(Context context, PayrollData data, String filename) throws IOException {
        String reportContent = generatePayrollReport(data);

        if (filename == null) {
            filename = String.format("payroll_%s_%s_%d.txt",
                    data.employeeId,
                    data.month.toLowerCase(),
                    data.year);
        }

        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, filename);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(reportContent);
        }

        return file;
    }

    public static class PayrollData {
        private String employeeName;
        private String employeeId;
        private String department;
        private String month;
        private int year;
        private double hoursWorked;
        private double hourlyRate;
        private double ytdEarnings;
        private int payPeriodNum;

        private PayrollData(Builder builder) {
            this.employeeName = builder.employeeName;
            this.employeeId = builder.employeeId;
            this.department = builder.department;
            this.month = builder.month;
            this.year = builder.year;
            this.hoursWorked = builder.hoursWorked;
            this.hourlyRate = builder.hourlyRate;
            this.ytdEarnings = builder.ytdEarnings;
            this.payPeriodNum = builder.payPeriodNum;
        }

        public static class Builder {
            private String employeeName;
            private String employeeId;
            private String department;
            private String month;
            private int year;
            private double hoursWorked;
            private double hourlyRate;
            private double ytdEarnings = 0.0;
            private int payPeriodNum;

            public Builder(String employeeName, String employeeId) {
                this.employeeName = employeeName;
                this.employeeId = employeeId;
            }

            public Builder department(String department) {
                this.department = department;
                return this;
            }

            public Builder month(String month) {
                this.month = month;
                return this;
            }

            public Builder year(int year) {
                this.year = year;
                return this;
            }

            public Builder hoursWorked(double hoursWorked) {
                this.hoursWorked = hoursWorked;
                return this;
            }

            public Builder hourlyRate(double hourlyRate) {
                this.hourlyRate = hourlyRate;
                return this;
            }

            public Builder ytdEarnings(double ytdEarnings) {
                this.ytdEarnings = ytdEarnings;
                return this;
            }

            public Builder payPeriodNum(int payPeriodNum) {
                this.payPeriodNum = payPeriodNum;
                return this;
            }

            public PayrollData build() {
                return new PayrollData(this);
            }
        }
    }
}
