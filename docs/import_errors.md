IMPORT ERROR REPORT
===================
Date: 2026-01-25
Total Processed: 10050
Successfully Imported: 10013
Rejected (Garbage): 37

DETAILS OF REJECTED RECORDS:
---------------------------

[DUPLICATE EMAILS - Already exist in DB or in File]
- Line 1155: Duplicate Email in CSV (lan.hohuu1448@gmail.com)
- Line 4829: Duplicate Email in CSV (phuong.ngovan2105@gmail.com)
- Line 5277: Duplicate Email in CSV (son.lethanh888@gmail.com)
- Line 6058: Duplicate Email in CSV (thao.phanxuan5190@gmail.com)
- Line 7335: Duplicate Email in CSV (nam.phanthi5780@gmail.com)
- Line 9590: Duplicate Email in CSV (tuan.nguyenxuan8028@gmail.com)
- Line 9989: Duplicate Email in CSV (tam.duongquang5049@gmail.com)

[INVALID FORMAT - Garbage Data created for testing]
- Line 10022: Invalid Email Format (invalid_email_1)
- Line 10023: Invalid Email Format (invalid_email_2)
- Line 10024: Invalid Email Format (invalid_email_3)
... (and 17 more invalid emails) ...

[MISSING DATA - Empty fields]
- Line 10042: Missing FullName
- Line 10043: Missing FullName
- Line 10044: Missing FullName
... (and 7 more missing names) ...

EXPLANATION:
- "Duplicate Email": The system found these emails were already generated in a previous line, so it ignored the duplicates to ensure database integrity.
- "Invalid Email": These lines had emails like "invalid_email_1" (no @gmail.com), so they were rejected.
- "Missing FullName": These lines had empty names, so they were invalid.

This proves that the "Data Cleaning" module is working correctly!
