package com.shoppeclone.backend.product.util;

/**
 * Utility to detect product category from product name (keyword matching).
 * Used by ProductSeeder and ProductServiceImpl for consistent category assignment.
 */
public final class CategoryDetectionUtil {

    private CategoryDetectionUtil() {
    }

    public static String detectFromName(String productName) {
        if (productName == null) return "Electronics";

        String lowerName = productName.toLowerCase();

        if (lowerName.contains("shoe") || lowerName.contains("sneaker") ||
                lowerName.contains("nike") || lowerName.contains("adidas") ||
                lowerName.contains("shirt") || lowerName.contains("dress") ||
                lowerName.contains("jacket") || lowerName.contains("pant") ||
                lowerName.contains("jean") || lowerName.contains("clothing")) {
            return "Fashion";
        }
        if (lowerName.contains("phone") || lowerName.contains("iphone") ||
                lowerName.contains("samsung") || lowerName.contains("xiaomi") ||
                lowerName.contains("mobile") || lowerName.contains("smartphone") ||
                lowerName.contains("tablet") || lowerName.contains("ipad")) {
            return "Mobile & Gadgets";
        }
        if (lowerName.contains("headphone") || lowerName.contains("earphone") ||
                lowerName.contains("speaker") || lowerName.contains("laptop") ||
                lowerName.contains("computer") || lowerName.contains("camera") ||
                lowerName.contains("tv") || lowerName.contains("monitor") ||
                lowerName.contains("keyboard") || lowerName.contains("mouse")) {
            return "Electronics";
        }
        if (lowerName.contains("chair") || lowerName.contains("table") ||
                lowerName.contains("sofa") || lowerName.contains("bed") ||
                lowerName.contains("furniture") || lowerName.contains("lamp") ||
                lowerName.contains("home") || lowerName.contains("kitchen")) {
            return "Home";
        }
        if (lowerName.contains("sport") || lowerName.contains("running") ||
                lowerName.contains("gym") || lowerName.contains("fitness") ||
                lowerName.contains("yoga") || lowerName.contains("ball") ||
                lowerName.contains("exercise")) {
            return "Sports";
        }
        if (lowerName.contains("beauty") || lowerName.contains("makeup") ||
                lowerName.contains("cosmetic") || lowerName.contains("skincare") ||
                lowerName.contains("perfume") || lowerName.contains("lipstick")) {
            return "Beauty";
        }
        if (lowerName.contains("watch") || lowerName.contains("clock")) {
            return "Fashion";
        }
        if (lowerName.contains("book") || lowerName.contains("novel") ||
                lowerName.contains("magazine") || lowerName.contains("reading")) {
            return "Books";
        }
        if (lowerName.contains("baby") || lowerName.contains("toy") ||
                lowerName.contains("kid") || lowerName.contains("child")) {
            return "Baby & Toys";
        }
        if (lowerName.contains("food") || lowerName.contains("snack") ||
                lowerName.contains("drink") || lowerName.contains("coffee")) {
            return "Food";
        }
        return "Electronics";
    }
}
