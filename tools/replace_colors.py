
import os

file_path = r'c:\webshoppe\Web_Shopee_Lab211_Group2\src\Frontend\index.html'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace Tailwind classes
new_content = content.replace('orange-', 'red-')

# Replace specific hex codes for search button (Orange to Red)
# #FB5533 (Shopee Orange) -> #EF4444 (Tailwind Red 500) or #DC2626 (Red 600)
# #fa4724 (Hover Orange) -> #B91C1C (Red 700) or #DC2626
new_content = new_content.replace('#FB5533', '#DC2626') # Vivid Red
new_content = new_content.replace('#fa4724', '#B91C1C') # Darker Red for hover

# Check if we accidentally replaced something we shouldn't have
# strict check: "range-" from "orange-" -> "red-". "orange" -> "red"
# But we used "orange-" -> "red-"
# So "border-orange-300" -> "border-red-300" OK.
# "text-orange-500" -> "text-red-500" OK.

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(new_content)

print("Successfully replaced orange theme with red.")
