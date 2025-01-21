package com.example.workshiftapp.models;

import android.content.Context;
import android.os.Environment;

import com.google.android.material.snackbar.Snackbar;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PayslipGenerator {

    public static void generatePayslip(Context context, String companyName, String companyAddress,
                                       String employeeName, String position, String payPeriod, String checkNumber,
                                       double regularHours, double overtimeHours, double regularRate, double overtimeRate,
                                       double grossPay, double federalTax, double stateTax, double socialSecurity,
                                       double medicare, double retirement, double netPay) {
        try {
            // Create directory and file path in the "Downloads" folder
            File pdfDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!pdfDir.exists()) {
                pdfDir.mkdirs(); // Create the directory if it doesn't exist
            }
            File file = new File(pdfDir, employeeName + "_Payslip_" + payPeriod.replace(" ", "_") + ".pdf");

            // Initialize PDF Writer
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Use a monospaced font for consistent formatting
            PdfFont monoFont = PdfFontFactory.createFont("Courier");

            // Get the current date
            String currentDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date());

            // Add the content using precise spacing
            document.add(new Paragraph("===============================================================")
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph(String.format("ACME Corporation                                NON-NEGOTIABLE"))
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph(String.format("123 Business Ave, Suite 100, Business City, ST 12345"))
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph("===============================================================")
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph(String.format("PAY PERIOD: %-20s CHECK NO: %s", payPeriod, checkNumber))
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph("---------------------------------------------------------------")
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph(String.format("EMPLOYEE: %-30s EMP ID: %s", employeeName, "EMP001"))
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph(String.format("DEPT: %-38s", position))
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph("---------------------------------------------------------------")
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph("EARNINGS               HOURS      RATE       CURRENT     YTD")
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph("---------------------------------------------------------------")
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph(String.format("Regular Pay           %6.2f    %6.2f    %10.2f", regularHours, regularRate, regularHours * regularRate))
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph(String.format("Overtime Pay          %6.2f    %6.2f    %10.2f", overtimeHours, overtimeRate, overtimeHours * overtimeRate))
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph("---------------------------------------------------------------")
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph(String.format("GROSS PAY                               %10.2f", grossPay))
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph("---------------------------------------------------------------")
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph("DEDUCTIONS             CURRENT")
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph("---------------------------------------------------------------")
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph(String.format("Federal Tax                      %10.2f", federalTax))
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph(String.format("State Tax                        %10.2f", stateTax))
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph(String.format("Social Security                 %10.2f", socialSecurity))
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph(String.format("Medicare                        %10.2f", medicare))
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph(String.format("401(k)                          %10.2f", retirement))
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph("---------------------------------------------------------------")
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph(String.format("TOTAL DEDUCTIONS                %10.2f", federalTax + stateTax + socialSecurity + medicare + retirement))
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph("---------------------------------------------------------------")
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph(String.format("NET PAY                                  %10.2f", netPay))
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph("===============================================================")
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph(String.format("CURRENT DATE: %s", currentDate))
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph("DIRECT DEPOSIT - THIS IS NOT A CHECK")
                    .setFont(monoFont).setFontSize(10));
            document.add(new Paragraph("===============================================================")
                    .setFont(monoFont).setFontSize(10));

            // Close the document
            document.close();
            writer.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}