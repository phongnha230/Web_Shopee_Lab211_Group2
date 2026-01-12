import csv
import random
from datetime import datetime

# Danh sách họ và tên tiếng Việt
ho = ['Nguyễn', 'Trần', 'Lê', 'Phạm', 'Hoàng', 'Huỳnh', 'Phan', 'Vũ', 'Võ', 'Đặng', 
      'Bùi', 'Đỗ', 'Hồ', 'Ngô', 'Dương', 'Lý', 'Đinh', 'Mai', 'Tô', 'Lâm']

ten_dem = ['Văn', 'Thị', 'Đức', 'Minh', 'Hoàng', 'Thanh', 'Hữu', 'Quốc', 'Anh', 'Tuấn',
           'Phương', 'Thu', 'Hồng', 'Kim', 'Ngọc', 'Bảo', 'Gia', 'Khánh', 'Như', 'Thúy']

ten = ['An', 'Bình', 'Cường', 'Dũng', 'Hùng', 'Khoa', 'Long', 'Nam', 'Phong', 'Quân',
       'Hà', 'Linh', 'Mai', 'Nga', 'Oanh', 'Phương', 'Quỳnh', 'Trang', 'Uyên', 'Vân',
       'Hải', 'Hiếu', 'Khánh', 'Lâm', 'Minh', 'Nhật', 'Phúc', 'Sơn', 'Tài', 'Thành',
       'Chi', 'Diệu', 'Giang', 'Hương', 'Kỳ', 'Lan', 'My', 'Nhi', 'Thảo', 'Yến']

# Các domain email phổ biến
email_domains = ['gmail.com', 'yahoo.com', 'outlook.com', 'hotmail.com', 'icloud.com',
                 'email.com', 'protonmail.com', 'zoho.com', 'aol.com', 'mail.com']

def generate_phone():
    """Tạo số điện thoại Việt Nam ngẫu nhiên"""
    prefixes = ['090', '091', '092', '093', '094', '096', '097', '098', '099',  # Mobifone
                '070', '076', '077', '078', '079',  # Mobifone
                '083', '084', '085', '081', '082',  # Vinaphone
                '088', '091', '094',  # Vinaphone
                '089', '090', '093',  # Vinaphone
                '086', '096', '097', '098',  # Viettel
                '032', '033', '034', '035', '036', '037', '038', '039']  # Viettel
    
    prefix = random.choice(prefixes)
    number = ''.join([str(random.randint(0, 9)) for _ in range(7)])
    return prefix + number

def generate_username(full_name, index):
    """Tạo username từ tên đầy đủ"""
    # Loại bỏ dấu tiếng Việt
    replacements = {
        'á': 'a', 'à': 'a', 'ả': 'a', 'ã': 'a', 'ạ': 'a', 'ă': 'a', 'ắ': 'a', 'ằ': 'a', 'ẳ': 'a', 'ẵ': 'a', 'ặ': 'a',
        'â': 'a', 'ấ': 'a', 'ầ': 'a', 'ẩ': 'a', 'ẫ': 'a', 'ậ': 'a',
        'é': 'e', 'è': 'e', 'ẻ': 'e', 'ẽ': 'e', 'ẹ': 'e', 'ê': 'e', 'ế': 'e', 'ề': 'e', 'ể': 'e', 'ễ': 'e', 'ệ': 'e',
        'í': 'i', 'ì': 'i', 'ỉ': 'i', 'ĩ': 'i', 'ị': 'i',
        'ó': 'o', 'ò': 'o', 'ỏ': 'o', 'õ': 'o', 'ọ': 'o', 'ô': 'o', 'ố': 'o', 'ồ': 'o', 'ổ': 'o', 'ỗ': 'o', 'ộ': 'o',
        'ơ': 'o', 'ớ': 'o', 'ờ': 'o', 'ở': 'o', 'ỡ': 'o', 'ợ': 'o',
        'ú': 'u', 'ù': 'u', 'ủ': 'u', 'ũ': 'u', 'ụ': 'u', 'ư': 'u', 'ứ': 'u', 'ừ': 'u', 'ử': 'u', 'ữ': 'u', 'ự': 'u',
        'ý': 'y', 'ỳ': 'y', 'ỷ': 'y', 'ỹ': 'y', 'ỵ': 'y',
        'đ': 'd', 'Đ': 'd'
    }
    
    name_lower = full_name.lower()
    for viet, eng in replacements.items():
        name_lower = name_lower.replace(viet, eng)
    
    # Tạo username từ các từ trong tên
    parts = name_lower.split()
    
    # Nhiều pattern khác nhau
    patterns = [
        ''.join(parts),  # nguyenvanan
        parts[-1] + ''.join([p[0] for p in parts[:-1]]),  # annv
        parts[-1] + str(index),  # an12345
        ''.join([p[0] for p in parts]) + str(index),  # nva12345
        parts[0][0] + parts[-1] + str(random.randint(1, 999)),  # nan123
    ]
    
    return random.choice(patterns)

def generate_email(username, domain):
    """Tạo email từ username"""
    return f"{username}@{domain}"

def generate_password():
    """Tạo mật khẩu ngẫu nhiên"""
    # Tạo mật khẩu đơn giản cho dữ liệu mẫu
    passwords = [
        'Password123!', 'Admin@123', 'User2024!', 'Welcome123',
        'Test@2024', 'Sample123!', 'Demo@Pass', 'Secure123!',
        '123456', 'password', 'admin123', 'user123'
    ]
    return random.choice(passwords)

def generate_full_name():
    """Tạo họ tên đầy đủ"""
    h = random.choice(ho)
    td = random.choice(ten_dem)
    t = random.choice(ten)
    return f"{h} {td} {t}"

def generate_csv_data(num_records=10000):
    """Tạo dữ liệu CSV"""
    print(f"Đang tạo {num_records} bản ghi dữ liệu mẫu...")
    
    filename = 'sample_users_data.csv'
    
    with open(filename, 'w', newline='', encoding='utf-8-sig') as csvfile:
        fieldnames = ['username', 'email', 'password', 'fullName', 'phone']
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        
        # Ghi header
        writer.writeheader()
        
        # Tạo dữ liệu
        for i in range(1, num_records + 1):
            full_name = generate_full_name()
            username = generate_username(full_name, i)
            domain = random.choice(email_domains)
            email = generate_email(username, domain)
            password = generate_password()
            phone = generate_phone()
            
            writer.writerow({
                'username': username,
                'email': email,
                'password': password,
                'fullName': full_name,
                'phone': phone
            })
            
            # Hiển thị tiến trình
            if i % 1000 == 0:
                print(f"Đã tạo {i}/{num_records} bản ghi...")
    
    print(f"\n✅ Hoàn thành! Đã tạo file '{filename}' với {num_records} bản ghi.")
    print(f"📁 Vị trí file: {filename}")
    print(f"\n📊 Cấu trúc dữ liệu:")
    print(f"   - username: Tên đăng nhập duy nhất")
    print(f"   - email: Email (username@domain)")
    print(f"   - password: Mật khẩu mẫu")
    print(f"   - fullName: Họ và tên đầy đủ tiếng Việt")
    print(f"   - phone: Số điện thoại Việt Nam (10 số)")

if __name__ == "__main__":
    # Tạo 10,000 bản ghi (có thể thay đổi số lượng)
    generate_csv_data(10000)
    
    print("\n💡 Hướng dẫn sử dụng:")
    print("   1. File CSV đã được tạo với encoding UTF-8-BOM để hỗ trợ tiếng Việt")
    print("   2. Bạn có thể import vào MongoDB hoặc MySQL")
    print("   3. Để tạo nhiều hơn/ít hơn, thay đổi số trong generate_csv_data(10000)")
