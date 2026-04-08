package com.mycompany.javasystem;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.PdfArray;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CertificateGenerator {

    private static final String TEMPLATES_PATH = "src/main/resources/certificates/";
    private static final String OUTPUT_PATH = "generated_certificates/";

    private static String getTemplateFile(String documentType) {
        if ("Certificate of Residency".equalsIgnoreCase(documentType)) {
            return "CertificateOfResidency.pdf";
        }
        else if ("Barangay Clearance".equalsIgnoreCase(documentType)) {
            return "Barangay Clearance.pdf";
        }
        else if ("Certificate of Indigency".equalsIgnoreCase(documentType)) {
            return "CertificateOfIndigency.pdf";
        }
        return null;
    }

    public static String generateCertificate(String documentType, String requestId, 
                                             String fullName, int age, String address, 
                                             String yearsResidency, String purpose,
                                             String occupationOrIncome,
                                             String barangayCity,
                                             String barangayProvince,
                                             String orNumber) {
        
        String templateFile = getTemplateFile(documentType);
        if (templateFile == null) {
            System.out.println("❌ Template not found for document type: " + documentType);
            return null;
        }
        
        try {
            new File(OUTPUT_PATH).mkdirs();
            
            String inputPath = TEMPLATES_PATH + templateFile;
            String outputPath = OUTPUT_PATH + requestId + "_" + documentType.toLowerCase().replace(" ", "_") + ".pdf";
            
            System.out.println("📄 Input: " + inputPath);
            System.out.println("📄 Output: " + outputPath);
            
            File outputFile = new File(outputPath);
            if (outputFile.exists()) {
                boolean deleted = false;
                for (int i = 0; i < 5; i++) {
                    if (outputFile.delete()) {
                        deleted = true;
                        System.out.println("✅ Deleted old file");
                        break;
                    }
                    Thread.sleep(500);
                }
                if (!deleted) {
                    outputPath = OUTPUT_PATH + requestId + "_" + System.currentTimeMillis() + "_" + documentType.toLowerCase().replace(" ", "_") + ".pdf";
                    outputFile = new File(outputPath);
                }
            }
            
            File inputFile = new File(inputPath);
            if (!inputFile.exists()) {
                System.out.println("❌ Template not found: " + inputFile.getAbsolutePath());
                return null;
            }
            
            PdfReader reader = new PdfReader(inputPath);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputPath));
            AcroFields form = stamper.getAcroFields();
            
            java.util.Set<String> fieldNames = form.getFields().keySet();
            System.out.println("📋 Fields: " + fieldNames.size());
            for (String name : fieldNames) {
                System.out.println("   - " + name);
            }
            
            if ("Certificate of Residency".equalsIgnoreCase(documentType)) {
                fillResidencyCertificate(form, fullName, age, address, yearsResidency, purpose, barangayCity, barangayProvince, orNumber);
            } 
            else if ("Barangay Clearance".equalsIgnoreCase(documentType)) {
                fillClearanceCertificate(form, fullName, age, purpose, barangayProvince, orNumber);
            } 
            else if ("Certificate of Indigency".equalsIgnoreCase(documentType)) {
                fillIndigencyCertificate(form, fullName, age, address, yearsResidency, 
                                        occupationOrIncome, purpose, barangayCity, barangayProvince, orNumber);
            }
            
            double fontSize = getFontSizeForDocType(documentType);
            applyHarshModifications(form, fontSize);
            
            stamper.setFormFlattening(true);
            stamper.close();
            reader.close();
            
            System.out.println("✅ Certificate generated: " + outputPath);
            return outputPath;
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static void fillResidencyCertificate(AcroFields form, String fullName, int age,
                                                 String address, String yearsResidency, String purpose,
                                                 String barangayCity, String barangayProvince, String orNumber) {
        try {
            System.out.println("📝 Filling Residency fields...");
            
            setFieldValueForce(form, "Text2", fullName);
            setFieldValueForce(form, "Text3", address);
            setFieldValueForce(form, "Text4", String.valueOf(age));
            setFieldValueForce(form, "Text5", yearsResidency);
            setFieldValueForce(form, "Text6", purpose);
            
            LocalDate today = LocalDate.now();
            setFieldValueForce(form, "Text7", String.valueOf(today.getDayOfMonth()));
            
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM");
            String month = today.format(monthFormatter);
            String year = String.valueOf(today.getYear());
            
            setFieldValueForce(form, "Text8", month);
            setFieldValueForce(form, "Text9", year);
            
            setFieldValueForce(form, "Text10", barangayCity);
            setFieldValueForce(form, "Text11", barangayProvince);
            
            System.out.println("✅ Residency fields filled");
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void fillClearanceCertificate(AcroFields form, String fullName, int age, 
                                                  String purpose, String barangayProvince, String orNumber) {
        try {
            System.out.println("📝 Filling Clearance fields...");
            
            setFieldValueForce(form, "Text1", fullName);
            setFieldValueForce(form, "Text2", String.valueOf(age));
            setFieldValueForce(form, "Text3", purpose);
            
            LocalDate today = LocalDate.now();
            setFieldValueForce(form, "Text4", String.valueOf(today.getDayOfMonth()));
            
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM");
            String month = today.format(monthFormatter);
            
            setFieldValueForce(form, "Text5", month);
            setFieldValueForce(form, "Text6", barangayProvince);
            setFieldValueForce(form, "Text7", orNumber);
            
            String dateIssued = today.format(DateTimeFormatter.ofPattern("MMMM dd"));
            setFieldValueForce(form, "Text8", dateIssued);
            
            System.out.println("✅ Clearance fields filled");
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void fillIndigencyCertificate(AcroFields form, String fullName, int age, 
                                               String address, String yearsResidency,
                                               String occupationOrIncome, String purpose,
                                               String barangayCity, String barangayProvince, String orNumber) {
       try {
           System.out.println("📝 Filling Indigency fields...");

           LocalDate today = LocalDate.now();
           String month = today.format(DateTimeFormatter.ofPattern("MMMM"));

           setFieldValueForce(form, "Text1", fullName);
           setFieldValueForce(form, "Text2", String.valueOf(age));
           setFieldValueForce(form, "Text3", barangayProvince);
           setFieldValueForce(form, "Text4", address);
           setFieldValueForce(form, "Text5", purpose);
           setFieldValueForce(form, "Text6", String.valueOf(today.getDayOfMonth()));
           setFieldValueForce(form, "Text7", month);
           setFieldValueForce(form, "Text8", barangayCity);

           System.out.println("✅ Indigency fields filled");

       } catch (Exception e) {
           System.out.println("❌ Error: " + e.getMessage());
           e.printStackTrace();
       }
   }
    private static void setFieldValueForce(AcroFields form, String fieldName, String value) {
        try {
            form.setField(fieldName, value);
            System.out.println("   ✓ " + fieldName + " = " + value);
        } catch (Exception e) {
            System.out.println("   ✗ Failed to set " + fieldName);
        }
    }

    private static double getFontSizeForDocType(String documentType) {
        if ("Barangay Clearance".equalsIgnoreCase(documentType)) {
            return 15.7;
        } 
        else if ("Certificate of Residency".equalsIgnoreCase(documentType)) {
            return 13.6;
        }
        else if ("Certificate of Indigency".equalsIgnoreCase(documentType)) {
            return 13.6;
        }
        return 13.6;
    }

    private static void applyHarshModifications(AcroFields form, double fontSize) {
    System.out.println("🔧 Applying formatting (Arial " + fontSize + ")...");
    
    for (String fieldName : form.getFields().keySet()) {
        try {
            AcroFields.Item item = form.getFieldItem(fieldName);
            if (item != null) {
                PdfDictionary field = item.getMerged(0);
                if (field != null) {
                    // Remove all styling
                    field.remove(PdfName.BS);
                    field.remove(PdfName.B);
                    field.remove(PdfName.BC);
                    field.remove(PdfName.BG);
                    field.remove(PdfName.AC);
                    field.remove(PdfName.RC);
                    
                    // Set border to 0
                    PdfDictionary bs = new PdfDictionary();
                    bs.put(PdfName.W, new PdfNumber(0));
                    field.put(PdfName.BS, bs);
                    
                    // Remove background completely - use transparent
                    PdfArray noColor = new PdfArray();
                    field.put(PdfName.BG, noColor);
                    
                    // Set font with no background
                    String daString = "0 0 0 rg /Arial " + fontSize + " Tf";
                    field.put(PdfName.DA, new PdfString(daString));
                    
                    // Clear appearance stream
                    PdfDictionary ap = field.getAsDict(PdfName.AP);
                    if (ap != null) {
                        ap.clear();
                    }
                    
                    // Set appearance characteristics to no fill
                    PdfDictionary mk = field.getAsDict(PdfName.MK);
                    if (mk == null) {
                        mk = new PdfDictionary();
                        field.put(PdfName.MK, mk);
                    }
                    mk.remove(PdfName.BG);
                    mk.remove(PdfName.BC);
                    
                    System.out.println("   ✓ " + fieldName);
                }
            }
        } catch (Exception e) {
            System.out.println("   ⚠️ " + fieldName);
        }
    }
    System.out.println("✅ Formatting complete");
}

    public static void main(String[] args) {
        generateCertificate(
            "Barangay Clearance",
            "REQ-2024-0001",
            "Juan Dela Cruz",
            34,
            "Blk 5 Lot 12 Sampaguita St., Brgy. San Isidro",
            "8",
            "Employment Requirements",
            null,
            "San Isidro",
            "Metro Manila",
            "OR-2026-001"
        );
    }
}