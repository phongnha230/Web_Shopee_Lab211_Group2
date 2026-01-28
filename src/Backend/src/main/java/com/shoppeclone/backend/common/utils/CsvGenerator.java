package com.shoppeclone.backend.common.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Random;
import java.util.regex.Pattern;

public class CsvGenerator {
    private static final String[] HO = { "Nguyen", "Tran", "Le", "Pham", "Hoang", "Huynh", "Phan", "Vu", "Vo", "Dang",
            "Bui", "Do", "Ho", "Ngo", "Duong", "Ly" };
    private static final String[] LOT = { "Van", "Thi", "Minh", "Huu", "Duc", "Thanh", "Ngoc", "Quang", "Xuan", "Hai" };
    private static final String[] TEN = { "Hung", "Lan", "Mai", "Tuan", "Hoa", "Cuong", "Linh", "Son", "Huong", "Thao",
            "Dung", "Nam", "Phuong", "Tam", "Hanh", "Vinh", "Dat", "Trang", "Hieu", "Phuc" };

    public static void main(String[] args) {
        String filename = "users_10k.csv";
        int count = 10000;
        Random random = new Random();

        System.out.println("Generating " + count + " realistic Vietnamese users...");

        try (FileWriter writer = new FileWriter(filename)) {
            // Write Header
            writer.append("email,password,fullName,phone\n");

            // 1. Generate 10,000 Valid Users
            for (int i = 1; i <= count; i++) {
                String ho = HO[random.nextInt(HO.length)];
                String lot = LOT[random.nextInt(LOT.length)];
                String ten = TEN[random.nextInt(TEN.length)];

                String fullName = ho + " " + lot + " " + ten;

                // Generate Email: ten.holot.uid@gmail.com to avoid duplicates but look real
                // Remove accents/spaces for email (simple here since arrays are ascii)
                String emailName = (ten + "." + ho + lot).toLowerCase();
                String email = emailName + random.nextInt(9999) + "@gmail.com";

                if (i <= 50) {
                    // Make first 50 look very specific as requested
                    if (i == 1) {
                        fullName = "Nguyen Van Loi";
                        email = "nguyenvanloi99@gmail.com";
                    }
                    if (i == 2) {
                        fullName = "Mai Thi Anh";
                        email = "maithianh00@gmail.com";
                    }
                }

                writer.append(String.format("%s,password123,%s,09%d\n",
                        email, fullName, 10000000 + random.nextInt(90000000)));
            }

            // 2. Generate 20 Duplicate Emails (Dirty Data)
            for (int i = 1; i <= 20; i++) {
                writer.append(
                        String.format("duplicate.user%d@gmail.com,password123,Duplicate User %d,0912345678\n", i, i));
            }

            // 3. Generate 20 Invalid Emails (Dirty Data)
            for (int i = 1; i <= 20; i++) {
                writer.append(String.format("invalid_email_%d,password123,Invalid User %d,0912345678\n", i, i));
            }

            // 4. Generate 10 Missing Fields (Dirty Data)
            for (int i = 1; i <= 10; i++) {
                writer.append(String.format("missing_data_%d@gmail.com,password123,,0912345678\n", i));
            }

            System.out.println("Done! Created '" + filename + "' with " + (count + 50) + " records.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
